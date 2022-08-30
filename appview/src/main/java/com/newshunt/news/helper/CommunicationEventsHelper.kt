package com.newshunt.news.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.newshunt.appview.R
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.dhutil.model.entity.notifications.ChineseDeviceInfo
import com.newshunt.dataentity.dhutil.model.entity.notifications.ChineseDeviceInfoResponse
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.common.PermissionDialogUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.model.internal.service.ChineseDeviceInfoServiceImpl
import com.newshunt.dhutil.view.PrivacyDialogFragment
import com.newshunt.news.util.NewsConstants.*
import com.newshunt.notification.helper.NotificationHandler
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.lang.NumberFormatException

object PermissionDialogHelper {

  @JvmStatic
  fun handlePermissionEvent(info: EventsInfo?, minLaunchCount: Int, activity: FragmentActivity): Boolean {
    if (info?.activity == null) {
      return false
    }

    val lastPermissionDialogLaunchCount = PreferenceManager.getPreference(AppStatePreference.LAST_PERMISSION_DIALOG_COUNT, -1)
    val appLaunchCount = AppUserPreferenceUtils.getAppLaunchCount()
    if (lastPermissionDialogLaunchCount == appLaunchCount) {
      // Prevent multiple prompts while switching between News & Buzz
      return false
    }

    if (appLaunchCount < minLaunchCount) {
      return false
    }

    val rationaleProvider = DefaultRationaleProvider()
    val activityAttributes = info.activity!!.attributes

    var repeatLaunchCount = PermissionDialogUtils.PERMISSION_DIALOG_REPETITION_COUNT

    if (!CommonUtils.isEmpty(activityAttributes)) {
      for ((key, value) in activityAttributes) {

        if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
          continue
        }

        if (DailyhuntConstants.PERMISSION_REPEAT_COUNT.equals(key, ignoreCase = true)) {
          repeatLaunchCount = Integer.valueOf(value)
        }
        if (DailyhuntConstants.PERMISSION_TITLE.equals(key, ignoreCase = true)) {
          rationaleProvider.setPermissionTitle(value)
        }
        if (DailyhuntConstants.PERMISSION_DESC.equals(key, ignoreCase = true)) {
          rationaleProvider.setPermissionDesc(value)
        }
        if (DailyhuntConstants.OPEN_SETTINGS.equals(key, ignoreCase = true)) {
          rationaleProvider.setOpenSettings(value)
        }
        if (DailyhuntConstants.SETTINGS_ACTION.equals(key, ignoreCase = true)) {
          rationaleProvider.setSettingsAction(value)
        }
        if (DailyhuntConstants.LOCATION_PERM_SUBTITLE.equals(key, ignoreCase = true)) {
          rationaleProvider.setLocationSubtitle(value)
        }
        if (DailyhuntConstants.STORAGE_PERM_SUBTITLE.equals(key, ignoreCase = true)) {
          rationaleProvider.setStorageSubtitle(value)
        }
        if (DailyhuntConstants.LOCATION_PERM_DESC.equals(key, ignoreCase = true)) {
          rationaleProvider.setLocationDesc(value)
        }
        if (DailyhuntConstants.STORAGE_PERM_DESC.equals(key, ignoreCase = true)) {
          rationaleProvider.setStorageDesc(value)
        }
        if (DailyhuntConstants.PERM_POSITIVE_BTN.equals(key, ignoreCase = true)) {
          rationaleProvider.positiveBtn = value
        }
        if (DailyhuntConstants.PERM_NEGATIVE_BTN.equals(key, ignoreCase = true)) {
          rationaleProvider.negativeBtn = value
        }
      }
    }

    if (appLaunchCount == minLaunchCount) {
      return showPermissionDialog(appLaunchCount, rationaleProvider,activity)
    } else if (appLaunchCount > minLaunchCount && (appLaunchCount - minLaunchCount) % repeatLaunchCount == 0) {
      return showPermissionDialog(appLaunchCount, rationaleProvider,activity)
    }

    return false
  }

  @JvmStatic
  private fun showPermissionDialog(appLaunchCount: Int,
                                   rationaleProvider: PermissionRationaleProvider,
                                   activity: FragmentActivity): Boolean {
    //TODO::@shrikanth.Agrawal : Handle the pageReferrer
    val permissionDialog = PermissionDialogUtils.promptPermissionOnLaunch(activity, null,
        appLaunchCount, rationaleProvider)
    return permissionDialog != null
    }
}

object PrivacyDialogHelper {
  private const val LOG_TAG = "PrivacyDialogHelper"

  @JvmStatic
  fun handlePrivacyEvent(info: EventsInfo?, minLaunchCount: Int, activity: FragmentActivity, hostId: Int): Boolean {
    if (info?.activity == null) {
      Logger.e(LOG_TAG, "handlePrivacyEvent, activity is null")
      return false
    }

    if (PreferenceManager.getPreference(AppStatePreference.PRIVACY_V2_ACCEPTED, false)) {
      // User already provided his preference
      Logger.e(LOG_TAG, "handlePrivacyEvent, user has already accepted")
      return false
    }
    val minLaunchCountToConsider = PreferenceManager.getPreference(GenericAppStatePreference.PRE_UPGRADE_APP_LAUNCH_COUNT, 0) + minLaunchCount
    val appLaunchCount = AppUserPreferenceUtils.getAppLaunchCount()

    if (appLaunchCount < minLaunchCountToConsider) {
      Logger.e(LOG_TAG, "handlePrivacyEvent, ineligible, appLaunchCount: $appLaunchCount, minLaunchCount: $minLaunchCountToConsider")
      return false
    }
    val lastDialogShownCount = PreferenceManager.getPreference(AppStatePreference.PRIVACY_DIALOG_SHOWN_LAUNCH_COUNT, -1)
    if (lastDialogShownCount > 0) {
      val gapCount = try {
        info.precondition?.get(COMM_GAP_COUNT)?.toInt() ?: -1
      } catch (exception: NumberFormatException) {
        Logger.e(LOG_TAG, "handlePrivacyEvent: NumberFormatException ${exception.message}")
        -1
      }
      if (gapCount < 0) {
        Logger.e(LOG_TAG, "handlePrivacyEvent, invalid gap count. Can not show more than once")
        return false
      }
      if (gapCount > (appLaunchCount - lastDialogShownCount)) {
        Logger.e(LOG_TAG, "handlePrivacyEvent, can not show dialog, gapCount: $gapCount, lastDialogShownCount: $lastDialogShownCount, runningCount: $appLaunchCount")
        return false
      }
    }

    val activityAttributes = info.activity!!.attributes
    var title: String? = null
    var description: String? = null
    var positiveString: String? = null
    var negativeString: String? = null

    if (!CommonUtils.isEmpty(activityAttributes)) {
      for ((key, value) in activityAttributes) {

        if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
          continue
        }

        if (DailyhuntConstants.PRIVACY_TITLE.equals(key, ignoreCase = true)) {
          title = value
        }
        if (DailyhuntConstants.PRIVACY_DESC.equals(key, ignoreCase = true)) {
          description = value
        }
        if (DailyhuntConstants.PRIVACY_POSITIVE_BTN.equals(key, ignoreCase = true)) {
          positiveString = value
        }
        if (DailyhuntConstants.PRIVACY_NEGATIVE_BTN.equals(key, ignoreCase = true)) {
          negativeString = value
        }
      }
    }
    PrivacyDialogFragment.newInstance(hostId,
      title,
      description,
      positiveString,
      negativeString,
      PageReferrer(NhGenericReferrer.ONBOARDING))?.let { privacyDialogFragment ->
      privacyDialogFragment.show(activity.supportFragmentManager, PrivacyDialogFragment.LOG_TAG)
      Logger.d(LOG_TAG, "Showing Privacy count at appLaunchCount: $appLaunchCount")
      PreferenceManager.savePreference(AppStatePreference.PRIVACY_DIALOG_SHOWN_LAUNCH_COUNT, appLaunchCount)
      return true
    }
    return false
  }
}

object BatterDialogHelper {

  @JvmStatic
  fun handleBatteryOptimizationEvent(info: EventsInfo, activity: FragmentActivity): Boolean {

    if (NotificationHandler.getIsPushNotificationWorkingInBg()) {
      return false
    }

    if (PreferenceManager.getPreference(
            GenericAppStatePreference.IS_AUTOSTART_ENABLE_DIALOG_SHOWN, false)) {
      return false
    }

    if (info.precondition == null) {
      return false
    }

    if (!CommonUtils.isValidInteger(info.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
          COMM_MIN_OCCURENCES_GO
        else
          COMM_MIN_OCCURENCES]) || !CommonUtils
            .isValidInteger(info.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
              COMM_MIN_APP_LAUNCH_COUNT_GO
            else
              COMM_MIN_APP_LAUNCH_COUNT])) {
      return false
    }
    val requiredAppLaunchCount = Integer.parseInt(info.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
      COMM_MIN_APP_LAUNCH_COUNT_GO
    else
      COMM_MIN_APP_LAUNCH_COUNT]!!)
    val requiredNewsHomeLaunchCount = Integer.parseInt(info.precondition!![if (AppConfig.getInstance()!!.isGoBuild) COMM_MIN_OCCURENCES_GO else COMM_MIN_OCCURENCES]!!)

    if (AppUserPreferenceUtils.getAppLaunchCount() < requiredAppLaunchCount || AppUserPreferenceUtils.getNewsLaunchCount() < requiredNewsHomeLaunchCount) {
      return false
    }

    getChineseDeviceInfo(activity)
    return true
  }

  private fun getChineseDeviceInfo(activity: FragmentActivity) {
    val chineseDeviceInfoService = ChineseDeviceInfoServiceImpl()
    val disposableObserver = object : DisposableObserver<ChineseDeviceInfoResponse>() {
      override fun onNext(value: ChineseDeviceInfoResponse) {
        handleChineseDeviceInfoResponse(value, activity)
      }

      override fun onError(e: Throwable) {
        dispose()
      }

      override fun onComplete() {
        dispose()
      }
    }
    chineseDeviceInfoService.getStoredChineseDeviceInfo(VersionMode.CACHE)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith<DisposableObserver<ChineseDeviceInfoResponse>>(disposableObserver)
  }

  @JvmStatic
  fun handleChineseDeviceInfoResponse(response: ChineseDeviceInfoResponse?, activity: FragmentActivity) {
    if (response == null || CommonUtils.isEmpty(response.deviceInfo)) {
      return
    }
    if (NotificationHandler.getIsPushNotificationWorkingInBg()) {
      return
    }
    handleChineseDeviceInfo(response.deviceInfo, activity)
  }

  @JvmStatic
  fun handleChineseDeviceInfo(response: List<ChineseDeviceInfo>, activity: FragmentActivity) {
    BatteryOptimizationDeviceInfoHelper.handleChineseDeviceInfoResponse(activity, response,
        PageReferrer(NewsReferrer.NEWS_HOME))
  }
}

object SocialCoachmarkHelper {

  @JvmStatic
  fun handleSocialCoachMark(info: EventsInfo?, minLaunchCount: Int, socialCoachMarkView: ViewGroup) : Boolean {

    if (info == null || CommonUtils.isEmpty(info.precondition) || info.activity == null) {
      return false
    }

    val coachMarkDisplayCount = PreferenceManager.getPreference(AppStatePreference.SOCIAL_COACHMARK_DISPLAY_COUNT, -1)
    if (minLaunchCount > AppUserPreferenceUtils.getAppLaunchCount() || coachMarkDisplayCount > 0) {
      return false
    }

    val activity = info.activity
    val attributes = activity?.attributes
    if (attributes == null) {
      return false
    } else {
      val profileText = attributes[DailyhuntConstants.TEXT_PROFILE]?:Constants.EMPTY_STRING
      val topicText = attributes[DailyhuntConstants.TEXT_TOPIC]?:Constants.EMPTY_STRING
      val shareText = attributes[DailyhuntConstants.TEXT_SHARE]?:Constants.EMPTY_STRING
      socialCoachMarkView.findViewById<NHTextView>(R.id.txt_profile).text = profileText
      socialCoachMarkView.findViewById<NHTextView>(R.id.txt_share).text = shareText
      socialCoachMarkView.findViewById<NHTextView>(R.id.txt_topic).text = topicText
      AnalyticsHelper2.logFeatureNudgeEvent("cm_viewed")
      PreferenceManager.savePreference(AppStatePreference.SOCIAL_COACHMARK_DISPLAY_COUNT, 1)
      socialCoachMarkView.visibility = View.VISIBLE
      return true
    }
  }
}