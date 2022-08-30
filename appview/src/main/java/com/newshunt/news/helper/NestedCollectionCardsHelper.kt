/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleObserver
import com.newshunt.adengine.util.AdLogger
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.DistancingSpec
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.NCCImpression
import com.newshunt.dataentity.common.asset.NCCStatus
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.SCVEvent
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.NCCImpressionDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FetchCardListFromUrlUsecase
import com.newshunt.news.model.usecase.NLResp
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.squareup.otto.Subscribe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Mukesh Yadav
 */

class NestedCollectionCardsHelper @Inject constructor(
    @Named("fetchUsecase") val fetchUsecase: BundleUsecase<NLResponseWrapper>,
    val fetchDao: FetchDao,
    private val nccImpressionDao: NCCImpressionDao,
    private val ncCardDbHelper: NCCardDBHelper?,
    private val lifecycleCoroutineScope: LifecycleCoroutineScope?,
    @Named("entityId") private val entityId: String,
    @Named("location") private val location: String,
    @Named("section") private val section: String,
    @Named("fragmentId")private  val fragmentId: String?,
    @Named("nestCollectionUrl")private val nestCollectionUrl: String?) : LifecycleObserver {

    private val nestedCollectionCardList = ArrayList<PostEntity?>()
    var cardResponseAwaited = false
    private var currentNCCData: Pair<Int, PostEntity?> = INVALID_NCC_DATA
    private var nccReqUrl: String? = nestCollectionUrl
    private var id: String = fragmentId!!
    var parentId: String? = null
    private var isEndOfNCC = false
    private var disposable: Disposable? = null
    var impressionsData: List<NCCImpression> ?= null

    init {
        BusProvider.getUIBusInstance().register(this)
    }

    private fun getNestedCollectionCard(url: String?, id: String) {
        val disposableObserver = object : DisposableObserver<NLResponseWrapper>() {
            override fun onNext(t: NLResponseWrapper) {
                Logger.d(LOG_TAG, "Got the response for the ncc request")
                handleNCCResponse(t)
                impressionsData?.let {
                    lifecycleCoroutineScope?.launch {
                        updateNCCImpressionStatus(it, NCCStatus.SYNCED)
                    }
                }
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                Logger.e(LOG_TAG, "Got the error for the ncc request ${e.localizedMessage}")
                cardResponseAwaited = false
                if (e is ListNoContentException) {
                    isEndOfNCC = true
                    impressionsData?.let {
                        lifecycleCoroutineScope?.launch {
                            updateNCCImpressionStatus(it, NCCStatus.SYNCED)
                        }
                    }
                } else {
                    impressionsData?.let {
                        lifecycleCoroutineScope?.launch {
                            updateNCCImpressionStatus(it, NCCStatus.NOT_SYNCED)
                        }
                    }
                }
            }
        }
        url?.let {
            disposable = Observable.fromCallable {
                impressionsData = nccImpressionDao.getNCCImpression()
                impressionsData
            }.flatMap {
                fetchUsecase.invoke(
                    FetchCardListFromUrlUsecase.bundle(
                        url,
                        Constants.REQUEST_TYPE_POST,
                        id,
                        it.map { it.data }
                    )
                )
            }.doOnSubscribe {
                impressionsData?.let { updateNCCImpressionStatus(it, NCCStatus.SYNCING) }
                cardResponseAwaited = true
                Logger.d(LOG_TAG, "ncc Card request made")
            }.subscribeOn(Schedulers.io()).subscribeWith(disposableObserver)

        }
    }

    private  fun updateNCCImpressionStatus(impressionsData:List<NCCImpression>, nccStatus: NCCStatus) {
        lifecycleCoroutineScope?.launch(Dispatchers.IO) {
            impressionsData.forEach {
                it.status = nccStatus
            }
            nccImpressionDao.upsertNCCImpressionList(impressionsData)
        }
    }


    private fun handleNCCResponse(data: NLResponseWrapper?) {
        cardResponseAwaited = false
        if (data == null) {
            Logger.e(LOG_TAG, "The response for ncc card is EMPTY")
            return
        }
        val nlResp = data.nlResp
        nlResp.rows.forEach {
            val postEntity = it as? PostEntity
            if (postEntity?.distancingSpec==null){
                return@forEach
            }
            nestedCollectionCardList.add(it as? PostEntity)
        }
        nccReqUrl = nlResp.nextPageUrl
    }

    fun tryInsertNCCard(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        if (PreferenceManager.getPreference(AppStatePreference.ENABLE_SMALL_CARD, false)) {
            reset()
            return
        }
        var lastVisibleItem = firstVisibleItem + visibleItemCount - 1
        Logger.d(
            LOG_TAG,
            "first: $firstVisibleItem, last: $lastVisibleItem, visible: $visibleItemCount"
        )
        val minNCRemainingForNextRequest = PreferenceManager.getPreference(AppStatePreference.MIN_COLLECTION_FOR_REQUEST,Constants.MIN_COLLECTION_FOR_REQUEST)
        if (nestedCollectionCardList.isEmpty() || nestedCollectionCardList.size <= minNCRemainingForNextRequest) {
            if (!cardResponseAwaited && !isEndOfNCC) {
                getNestedCollectionCard(nccReqUrl, id)
            }
            if (nestedCollectionCardList.isEmpty()) return
        }

        val nccPostEntity :PostEntity? = nestedCollectionCardList[0]
        val nccPosition = getNCCPosition(nccPostEntity)
        // In case if we have footer visible and items not loaded yet, last visible item
        // will be more than total items in the memory
        if (lastVisibleItem >= totalItemCount) {
            lastVisibleItem = totalItemCount - 1
        }
        if (nccPosition > lastVisibleItem && nccPosition <= lastVisibleItem + NCC_INSERT_BUFFER && nccPosition <= totalItemCount) {
            var position = nccPosition
            ncCardDbHelper?.let {
                do {
                    val tempPosition = ncCardDbHelper.validateAndGetPosition(position, nccPostEntity?.distancingSpec)
                    Logger.d(LOG_TAG, "given position $position and new position $tempPosition")
                    if (tempPosition == -1){
                        return
                    }
                    if (tempPosition != position) {
                        position = tempPosition
                    }
                } while (position != tempPosition)
            }

            val success = tryInsertNCCard(nccPostEntity, ncCardDbHelper?.getItemIdBeforeIndex(position), nccPosition)
            if (success) {
                nestedCollectionCardList.removeAt(0)
                currentNCCData = Pair(nccPosition,nccPostEntity)
                Logger.d(LOG_TAG, "ncc inserted at position when nccPosition($nccPosition) >= lastVisibleItem: $lastVisibleItem"
                )
            }
        } else if (nccPosition <= lastVisibleItem && lastVisibleItem + NCC_INSERT_BUFFER <= totalItemCount) {
            var position = lastVisibleItem + NCC_INSERT_BUFFER
            Logger.d(LOG_TAG, "position falls under visible area so next eligible position : $position")
            ncCardDbHelper?.let {
                do {
                    val tempPosition = ncCardDbHelper.validateAndGetPosition(position, nccPostEntity?.distancingSpec)
                    Logger.d(LOG_TAG, "given position $position and new position $tempPosition")
                    if (tempPosition == -1){
                        return
                    }
                    if (tempPosition != position) {
                        position = tempPosition
                    }
                } while (position != tempPosition)
            }
            val success = tryInsertNCCard(nccPostEntity, ncCardDbHelper?.getItemIdBeforeIndex(position), position)
            if (success) {
                nestedCollectionCardList.removeAt(0)
                currentNCCData = Pair(position,nccPostEntity)
                Logger.d(LOG_TAG, "ncc inserted at position when nccPosition($position) < lastVisibleItem($lastVisibleItem)")
            }
        }
    }

    private fun tryInsertNCCard(nccPostEntity: PostEntity?, prevPostId: String?, nccPosition: Int): Boolean {
        val nccCard = nccPostEntity?.toCard2()
        Logger.d(LOG_TAG, "tryInsertNCC ${nccPosition}, prevPostId : $prevPostId, ncc id : ${nccCard?.i_id()}")
        if (prevPostId == null && nccPosition != 0 || nccCard == null) {
            return false
        }
        //todo mukesh need to handle error
        lifecycleCoroutineScope?.launch(Dispatchers.IO){
            fetchDao.insertAdInDB(nccCard, prevPostId, System.currentTimeMillis(), entityId,location,section)
        }
        return true
    }

    private fun getNCCPosition(nccPostEntity: PostEntity?): Int {
        val distancingSpec = nccPostEntity?.distancingSpec ?: return 0
        var position = if (currentNCCData.first == -1) {
            distancingSpec.top
        } else {
            if (currentNCCData.second?.format == Format.NESTED_COLLECTION) {
                currentNCCData.first + distancingSpec.nestedCollection
            } else {
                currentNCCData.first + distancingSpec.collection
            }
        }
        if (position < 0) {
            position = 0
        }
        Logger.d(LOG_TAG, "ncc pos calculated $position")
        return position
    }

    fun onFPResponse(fpData: NLResp?) {
        AdLogger.d(LOG_TAG, "onFPResponse ${fpData?.isFromNetwork}")
        if (fpData?.rows?.isEmpty() == true) {
            return
        }
        reset()
    }

    fun destroy(){
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        try {
            BusProvider.getUIBusInstance().unregister(this)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        reset()
    }

    fun reset() {
        cardResponseAwaited = false
        nestedCollectionCardList.clear()
        currentNCCData = INVALID_NCC_DATA
        nccReqUrl = nestCollectionUrl
        isEndOfNCC = false
    }

    companion object {
        private const val NCC_INSERT_BUFFER = 1
        private const val LOG_TAG = "NestedCollectionCardsHelper"
        private  val INVALID_NCC_DATA = Pair(-1, null)
    }

    @Subscribe
    fun onViewEvent(scvEvent: SCVEvent?) {
        scvEvent?.postEntity?.let {postEntity->
            lifecycleCoroutineScope?.launch(Dispatchers.IO) {
                postEntity.impressionData?.let{
                    nccImpressionDao.upsertNCCImpression(NCCImpression(postEntity.i_id(), it))
                }
            }
        }
    }
}

interface NCCardDBHelper {
    fun getItemIdBeforeIndex(nccPosition: Int): String?
    fun validateAndGetPosition(nccPosition : Int, distancingSpec : DistancingSpec?) : Int
}