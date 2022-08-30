package com.newshunt.news.helper

import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.appview.R
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.view.entity.Gender
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.APIUtils
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AstroPreference
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.model.repo.PageSyncRepo
import com.newshunt.news.model.usecase.AddPageUsecase
import com.newshunt.news.model.usecase.AstroSubscribeUsecase
import com.newshunt.news.model.usecase.StoreHomePagesUsecase
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.dialog.AstroSubscriptionDialog
import com.newshunt.news.view.fragment.DatePickerFragment
import com.newshunt.news.view.listener.AstroDateSelectedListener
import com.newshunt.news.view.listener.AstroSubscriptionResultListener
import com.newshunt.news.view.listener.AstroSubscriptionView
import com.newshunt.newshome.view.entity.AstroDialogStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object AstroHelper {

  /**
   * @return - Gender saved in the shared preferences. null otherwise.
   */
  @JvmStatic
  val gender: Gender?
    get() {
      val savedGender = PreferenceManager.getPreference(AstroPreference.USER_GENDER, Constants.EMPTY_STRING)
      return if (CommonUtils.isEmpty(savedGender)) {
        null
      } else Gender.getGender(savedGender)
    }

  /**
   * @return - An instance of Calendar if the date of birth is saved in the shared preferences,
   * null otherwise.
   */
  @JvmStatic
  val calendarFromSavedDate: Calendar?
    get() {
      val astroDob = PreferenceManager.getPreference(AstroPreference.USER_DOB, Constants.EMPTY_STRING)
      if (CommonUtils.isEmpty(astroDob)) {
        return null
      }
      val date: Date
      try {
        val astroDateFormat = SimpleDateFormat(DailyhuntConstants.astroDateFormat)
        date = astroDateFormat.parse(astroDob)
      } catch (e: ParseException) {
        Logger.caughtException(e)
        return null
      }

      val calendar = Calendar.getInstance()
      calendar.time = date
      return calendar
    }

  //Returns the status of the Astro Dialog.
  @JvmStatic
  val astroDialogStatus: AstroDialogStatus
    get() {
      val status = PreferenceManager.getPreference(AstroPreference
          .ASTRO_DIALOG_STATUS, AstroDialogStatus.NEVER_SHOWN.status)
      return AstroDialogStatus.getAstroDialogStatus(status)
    }

  /**
   * Helper method to return the view order of the tab.
   *
   * @return - View order of the tab.
   */
  //It means that if Astro is to be inserted at 3rd position, then the index of the tab will
  // be 2.
  @JvmStatic
  val astroViewOrder: Int =  DailyhuntConstants.VIEW_ORDER

  // A helper method for launching the Date Picker in Android.
  @JvmStatic
  fun launchAndroidDatePicker(fragmentManager: FragmentManager,
                              astroDateSelectedListener: AstroDateSelectedListener) {
    val calendar = calendarFromSavedDate
    val datePickerFragment = DatePickerFragment.newInstance(astroDateSelectedListener, calendar)
    datePickerFragment.show(fragmentManager, DailyhuntConstants.DATE_PICKER)
  }

  /**
   * Initiates a post request for subscription API.
   *
   * @param astroSubscriptionView - An instance of AstroSubscriptionView
   * @param gender                     - The gender of the user.
   * @param dob                        - The dob of the user.
   */
  @JvmStatic
  fun doAstroPostRequest(astroSubscriptionView: AstroSubscriptionView?,
                         gender: String, dob: String, entityId: String) {
    if (CommonUtils.isEmpty(gender) || CommonUtils.isEmpty(dob)) {
      return
    }
    val disposableObserver = object : DisposableObserver<Any>() {
      override fun onNext(t: Any) {
        astroSubscriptionView?.hideProgressBar()
        astroSubscriptionView?.onAstroSubscriptionSuccess()
      }

      override fun onComplete() {
        dispose()
      }

      override fun onError(e: Throwable) {
        astroSubscriptionView?.hideProgressBar()

        val baseError = APIUtils.getError(e)
        var errorMessage: String? = CommonUtils.getString(R.string.error_generic)
        if (baseError != null) {
          errorMessage = baseError.message
        }
        astroSubscriptionView?.onAstroSubscriptionFailed(errorMessage)
        dispose()
      }
    }

    AstroSubscribeUsecase().invoke(bundleOf(AstroSubscribeUsecase.BUNDLE_DOB to dob,
        AstroSubscribeUsecase.BUNDLE_ENTITY_ID to entityId,
        AstroSubscribeUsecase.BUNDLE_GENDER to gender)).
        flatMap {
          AddPageUsecase().invoke(bundleOf(NewsConstants.NEWS_PAGE_ENTITY to it)).map {
          StoreHomePagesUsecase(PageSyncRepo(PageSection.NEWS.section)).invoke(PageSection.NEWS.section)
      }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(disposableObserver)
  }

  //Saves the gender in the Shared Preferences.
  @JvmStatic
  fun saveGender(gender: String) {
    if (CommonUtils.isEmpty(gender)) {
      return
    }
    PreferenceManager.savePreference(AstroPreference.USER_GENDER, gender)
  }

  /**
   * A helper method for determining whether the subscribe method can be enabled or not. It has
   * be  enabled only when the Gender and DOB are filled by the user.
   *
   * @return
   */
  @JvmStatic
  fun canEnableSubscribeButton(): Boolean {
    val userFilledGender = gender != null
    val userFilledDob = calendarFromSavedDate != null
    return userFilledGender && userFilledDob
  }

  // A helper method for regular text
  @JvmStatic
  fun setUpNormalTextViewForDialog(textView: TextView, stringResId: Int) {
    val text = CommonUtils.getString(stringResId)
    textView.text = text
  }

  // A helper method for bold text.
  @JvmStatic
  fun setUpBoldTextViewForDialog(textView: TextView, stringResId: Int) {
    val text = CommonUtils.getString(stringResId)
    textView.text = text
  }

  /**
   * This method is used from various places when the astro subscription is success.
   * It saves the status in the shared preferences
   */
  @JvmStatic
  fun handleAstroSuccessfulSubscription() {
    PreferenceManager.savePreference(AstroPreference.ASTRO_DIALOG_STATUS, AstroDialogStatus
        .SUBSCRIPTION_SUCCESSFUL.status)
    PreferenceManager.savePreference(AstroPreference.ASTRO_SUBSCRIBED, true)
  }

  /**
   * A helper method to fire astro subscription event.
   *
   * @param type
   */
  @JvmStatic
  fun fireAstroSubscriptionEvent(type: String) {
    //Fire an event for Astro Subscription
    val gender = PreferenceManager.getPreference(AstroPreference.USER_GENDER, Constants.EMPTY_STRING)
    val dob = PreferenceManager.getPreference(AstroPreference.USER_DOB, Constants.EMPTY_STRING)

    val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any>()
    paramsMap[NhAnalyticsNewsEventParam.USER_GENDER] = gender
    paramsMap[NhAnalyticsNewsEventParam.USER_DOB] = dob
    paramsMap[NhAnalyticsNewsEventParam.SUBSCRIPTION_TYPE] = type
    AnalyticsClient.log(NhAnalyticsNewsEvent.SUBSCRIBED, NhAnalyticsEventSection.NEWS, paramsMap)
  }

  /**
   * This method is used for saving the date of birth of the user in yyyyMMdd format.
   *
   * @param calendar - An instance of Calendar.
   */
  @JvmStatic
  fun saveUserDateOfBirth(calendar: Calendar) {
    try {
      val astroDateFormat = SimpleDateFormat(DailyhuntConstants.astroDateFormat)
      val dateStr = astroDateFormat.format(calendar.time)
      PreferenceManager.savePreference(AstroPreference.USER_DOB, dateStr)
    } catch (e: Exception) {
      Logger.caughtException(e)
    }

  }

  /**
   * Handle the event configuration to show the Astro dialog.
   *
   * @param info      - Events info
   * @param minLaunch - Minimum number of launches to show the astro dialog.
   */
  @JvmStatic
   fun handleAstroEvent(info: EventsInfo?, minLaunch: Int,
                        activity: FragmentActivity, activityId: Int,
                        astroSubscriptionResultListener: AstroSubscriptionResultListener): Boolean {

    if (info?.activity?.action == null) {
      return false
    }

    val activityAttributes = info.activity!!.attributes
    if (!CommonUtils.isEmpty(activityAttributes)) {
      for ((key, value) in activityAttributes) {

        if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
          continue
        }

        when (key) {
          DailyhuntConstants.ASTRO_SUPPORTED_LANGUAGES -> PreferenceManager.savePreference(AstroPreference.ASTRO_SUPPORTED_LANGUAGES, value)
          DailyhuntConstants.ASTRO_LANGUAGES_PRIORITY -> PreferenceManager.savePreference(AstroPreference.ASTRO_PRIORITY_LANGUAGES, value)
        }
      }
    }

    val isAstroSubscribed = PreferenceManager.getPreference(AstroPreference.ASTRO_SUBSCRIBED, false)
    //If the user has already subscribed to Astro, then do not show the dialog again.
    if (isAstroSubscribed) {
      return false
    }

    //Get the status for the astro dialog
    val astroDialogStatus = astroDialogStatus
    val shouldDisplayAstroDialog = astroDialogStatus != AstroDialogStatus.DISMISSED_TWICE && astroDialogStatus != AstroDialogStatus.SUBSCRIPTION_SUCCESSFUL

    //If the user has already dismissed the dialog twice or has already subscribed, then return it.
    if (!shouldDisplayAstroDialog) {
      return false
    }

    val actionType = info.activity!!.action.type
    if (CommonUtils.isEmpty(actionType) || !DailyhuntConstants.ASTRO_ACTIVITY_EVENT_ACTION_TYPE.equals
        (actionType, ignoreCase = true)) {
      return false
    }

    val attributes = info.activity!!.action.attributes ?: return false

    for ((key, value) in attributes) {
      if (CommonUtils.equalsIgnoreCase(DailyhuntConstants.ASTRO_VIEW_ORDER, key) && !CommonUtils.isEmpty(value)) {
        PreferenceManager.savePreference(AstroPreference.ASTRO_VIEW_ORDER, value)
      }
    }
    return displayAstroDialog(minLaunch, astroDialogStatus, activity, activityId, astroSubscriptionResultListener)
  }

  @JvmStatic
  private fun displayAstroDialog(minimumLaunchCount: Int, astroDialogStatus: AstroDialogStatus,
                                 activity: FragmentActivity, activityId: Int,
                                 astroSubscriptionResultListener: AstroSubscriptionResultListener): Boolean {
    var minCount = minimumLaunchCount
    val newsHomeLaunchCount = AppUserPreferenceUtils.getAppLaunchCount()
    if (astroDialogStatus == AstroDialogStatus.DISMISSED_ONCE) {
      //If the user has already dismissed the dialog once earlier, then display it after x+10
      // launches.
      minCount += DailyhuntConstants.ASTRO_NUM_LAUNCHES_SECOND_TIME
    }
    return if (newsHomeLaunchCount < minimumLaunchCount) {
      false
    } else showAstroDialog(activity, activityId, astroSubscriptionResultListener)
  }

  @JvmStatic
  private fun showAstroDialog(activity: FragmentActivity, activityId:Int,
                              astroSubscriptionResultListener: AstroSubscriptionResultListener) : Boolean {
    val dialog = AstroSubscriptionDialog.newInstance(
        activity.supportFragmentManager,
        astroSubscriptionResultListener,
        BusProvider .getUIBusInstance(),
        activityId)
    try {
      dialog.show(activity.supportFragmentManager, "astroDialog")
    } catch (e: IllegalStateException) {
      Logger.caughtException(e)
      return false
    }

    return true
  }
}