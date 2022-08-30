package com.newshunt.news.helper

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.NonLinearConfigurations
import com.newshunt.dataentity.social.entity.CardsPayload
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FetchCardListFromUrlUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

/**
 * @author shrikant.agrawal
 */
class NonLinearFeedHelper @Inject constructor(
    @Named("fetchUsecase") val fetchUsecase : BundleUsecase<NLResponseWrapper>,
    val fetchDao: FetchDao) : LifecycleObserver {

  private fun logTag() = "${Constants.NON_LINEAR_FEED}[${this.position}]"

  private var isVisible = false
  private var hasNonLinearCardRequested = false
  private var timespent = 0L
  private var nonLinearRequestTime = 15
  private var nonLinearDisplayTime = 20
  var position: Int = -1
  var asset: PostEntity? = null
  set(value) {
    field = value
    if (isVisible) startTimeCalculation()
  }
  var parentId: String? = null
  var lifecycleOwner: LifecycleOwner? = null
  set(value) {
    field = value
    field?.lifecycle?.addObserver(this)

  }

  private val nonLinearHandler = object : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
      Logger.i(logTag(), "Calling the non linear api from fragment")
//      val displayTime = System.currentTimeMillis() + (nonLinearDisplayTime - nonLinearRequestTime)
      getNLFC(asset?.nonLinearPostUrl!!, asset?.id!!)
      hasNonLinearCardRequested = true
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onStart() {
    isVisible = true
    startTimeCalculation()
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onStop() {
    isVisible = false
    stopTimeCalculation()
  }

  private fun startTimeCalculation() {
      if (hasNonLinearCardRequested) {
        Logger.i(logTag(), "Non Linear card already requested hence returning")
        return
      }

      timespent = System.currentTimeMillis()
      // remove all previous messages
      nonLinearHandler.removeCallbacksAndMessages(null)

      if (CommonUtils.isEmpty(asset?.nonLinearPostUrl)) {
        Logger.d(logTag(), "No non linear feed url NOT present hence returning")
        return
      }

      val nonLinearConfigurations = PreferenceManager.getPreference(
          GenericAppStatePreference.NON_LINEAR_CONFIGURATIONS, Constants.EMPTY_STRING)
      if (CommonUtils.isEmpty(nonLinearConfigurations)) {
        Logger.i(logTag(), "Non Linear Preferences are not present hence returning")
        return
      }

      val  configurations = JsonUtils.fromJson(nonLinearConfigurations, NonLinearConfigurations::class.java)
      if (configurations == null) {
        Logger.i(logTag(), "Non Linear Preferences are not present hence returning")
        return
      } else {
        nonLinearDisplayTime = configurations.storyDetail.display
        nonLinearRequestTime = configurations.storyDetail.request
      }
      Logger.i(logTag(),"Starting the time calculation")
      nonLinearHandler.sendEmptyMessageDelayed(1, (nonLinearRequestTime * 1000).toLong())
  }

  private fun stopTimeCalculation() {
    if (timespent > 0 && !CommonUtils.isEmpty(asset?.nonLinearPostUrl)) {
      timespent = System.currentTimeMillis() - timespent
      NonLinearStore.updateTimespent(asset?.id!!, timespent / 1000)
      timespent = 0L
    }
    Logger.w(logTag(), "Stopping the time calculation")
    nonLinearHandler.removeCallbacksAndMessages(null)
  }

  fun getNLFC(url: String, id: String, followBlockRequest: CardsPayload.FollowBlockRequest?= null) {
    val disposableObserver = object : DisposableObserver<NLResponseWrapper>() {
      override fun onNext(t: NLResponseWrapper) {
        Logger.d(logTag(), "Got the response for the NLFC request")
        handleNLFCResponse(t)
      }

      override fun onComplete() {
        dispose()
      }

      override fun onError(e: Throwable) {
        Logger.e(logTag(), "Got the error for the NLFC request ${e?.localizedMessage}")
        dispose()
      }
    }

      followBlockRequest?.let {
          fetchUsecase.invoke(
              FetchCardListFromUrlUsecase.bundle(
                  url,
                  Constants.REQUEST_TYPE_POST,
                  id,
                  recommendFollowBlock = followBlockRequest
              )
          ).subscribeOn(Schedulers.io()).subscribeWith(disposableObserver)
          return
      }

      fetchUsecase.invoke(FetchCardListFromUrlUsecase.bundle(url, Constants.REQUEST_TYPE_POST, id))
          .subscribeOn(Schedulers.io()).subscribeWith(disposableObserver)
  }


  private fun handleNLFCResponse(data : NLResponseWrapper?) {
    if (data == null) {
      Logger.e(logTag(), "The response for non linear card is EMPTY")
      return
    }
    val nlResp = data.nlResp
    // handle the data
    Logger.d(logTag(), "Got the response from the non linear feed")
    val nlfcCards = nlResp.rows.filterIsInstance(PostEntity::class.java)
    if (!CommonUtils.isEmpty(nlfcCards) && parentId != null) {
      fetchDao.insertNonLinearPostList(nlfcCards,parentId!!)
      NonLinearStore.insertStories(parentId!!, nlfcCards)

    } else {
      Logger.e(logTag(), "NLF Cards are empty after the filter")
    }
  }

}