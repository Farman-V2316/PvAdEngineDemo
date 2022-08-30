/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.helper


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FetchCardListFromUrlUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Vinod BC
 */
class RelatedVideoHelper @Inject constructor(
    @Named("fetchUsecase") val fetchUsecase : BundleUsecase<NLResponseWrapper>,
    val fetchDao: FetchDao) :
        LifecycleObserver {

  private fun logTag() = "${Constants.RELATED_VIDEO_FEED}[${this.position}]"

  private var isVisible = false
  private var hasRequested = false

  var position: Int = -1
  var asset: PostEntity? = null
  var parentId: String? = null
  var lifecycleOwner: LifecycleOwner? = null
  set(value) {
    field = value
    field?.lifecycle?.addObserver(this)
  }

  val relatedCardsLiveData = MutableLiveData<RelatedCards>()
  val list : LiveData<RelatedCards>
    get() = relatedCardsLiveData

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onStart() {
    isVisible = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onStop() {
    isVisible = false
  }

  fun getRelated(url: String, id: String) {
    if (hasRequested) {
      Logger.d(logTag(), "GetRelated hasRequested : $hasRequested")
      return
    }
    parentId = id
    val disposableObserver = object : DisposableObserver<NLResponseWrapper>() {
      override fun onNext(t: NLResponseWrapper) {
        Logger.d(logTag(), "Got the response for the Related video request")
        handleRelatedVideoResponse(t)
      }

      override fun onComplete() {
        dispose()
      }

      override fun onError(e: Throwable) {
        Logger.e(logTag(), "Got the error for the related video request ${e?.localizedMessage}")
        dispose()
      }
    }
    fetchUsecase.invoke(FetchCardListFromUrlUsecase.bundle(url, "POST",id)).
        subscribeOn(Schedulers.io()).subscribeWith(disposableObserver)
  }

  private fun handleRelatedVideoResponse(data : NLResponseWrapper?) {
    if (data == null) {
      Logger.e(logTag(), "The response for related video card is EMPTY")
      return
    }
    val nlResp = data.nlResp
    // handle the data
    Logger.d(logTag(), "Got the response from the related feed")
    val relatedCards = nlResp.rows.filterIsInstance(PostEntity::class.java)
    if (!CommonUtils.isEmpty(relatedCards) && parentId != null) {
      fetchDao.insertRelatedVideoList(relatedCards,parentId!!)
//      val fetchEntity = FetchInfoEntity(parentId, locationUsed, "", 0, section = section)
//      fetchDao.insertStoriesinFetchDB(fetchEntity, relatedCards)
      relatedCardsLiveData.postValue(RelatedCards(parentId, relatedCards))
      Logger.d(logTag(), "updated live data")
      //TODO:: VINOD - Define a LiveData to consume result
//      NonLinearStore.insertStories(parentId!!, nlfcCards)

    } else {
      Logger.e(logTag(), "NLF Cards are empty after the filter")
    }
  }


  class RelatedCards(val parentId: String?, val cardsList: List<PostEntity>?) {
  }

}