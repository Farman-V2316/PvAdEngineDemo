/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.instream

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleObserver
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.TimeUnit

object IAdCacheManager : IAdCacheCallbacks, LifecycleObserver {

    private const val TAG = "IAdCacheManager"

    private const val AD_REQUEST_TIME_OUT = 5000  // 5sec delay
    private var uniqueRequestId = 10001
    private val requestQueue = LinkedHashMap<String, IAdCacheItem>()
    private val cachedQueue = LinkedHashMap<String, BaseAdEntity>()
    private val scheduleHandle: Handler
    private var timeWhenLastAdPlayed: Long = 0
    private val adSpecList = HashMap<String, AdSpec>()

    init {
        scheduleHandle = Handler(Looper.getMainLooper())
    }

    private val runnableExecutor = Runnable {
        scheduleHandle.removeCallbacksAndMessages(null)
        //Callback on TimeOut
        if (requestQueue.entries.iterator().hasNext()) {
            val nextRequest = requestQueue.entries.iterator().next()
            val cacheItem = nextRequest.value
            Logger.d(TAG, "runnableExecutor : id: ${cacheItem.videoId} and ${cacheItem.instreamAdsHelper}  status : ${cacheItem.requestDone}")
            if (cacheItem.requestDone) {
                IAdLogger.d(TAG, "runnableExecutor Timeout : " + cacheItem.videoId)
                removeAndRequestNext(cacheItem.instreamAdsHelper, cacheItem.videoId)
            } else {
                execNextAdRequest()
            }
        }
    }

    override val timeSinceLastAdPlayer: Long
        get() = if (timeWhenLastAdPlayed <= 0) {
            0L
        } else intoSeconds(System.currentTimeMillis() - timeWhenLastAdPlayed).toLong()

    fun getAdSpec(key : String?) : AdSpec? {
        return adSpecList.get(key)
    }

    @Synchronized
    fun addToAdSpecList(specList : Map<String, AdSpec>) {
        Logger.d(TAG, "addToAdSpecList : ${specList.size}")
        this.adSpecList.putAll(specList)
        Logger.d(TAG, "addToAdSpecList adSpecList : ${adSpecList.size}")
    }

    @Synchronized
    fun requestInstreamAd(activity: Activity?, item: ExoPlayerAsset?, dhtvAdParams: Map<String, String>,
                          index: Int, playerCacheCallbacks: IAdCachePlayerCallbacks?,
                          commonAsset: CommonAsset?, pageEntity: PageEntity?,
                          section: String?): Boolean {
        item ?: return false

        Logger.d(TAG, "requestInstreamAd ${item.id}")
        if (!CommonUtils.isEmpty(item.adUrl)) {
            IAdLogger.d(TAG,
                    "returns AdUrl already in Asset : " + item.id + " index : " + index)
            return false
        }
        if (cachedQueue.containsKey(item.id)) {
            IAdLogger.d(TAG,
                    "returns assetId already in cachedQueue : " + item.id + " index :" + index)
            return false
        }
        if (requestQueue.containsKey(item.id)) {
            IAdLogger.d(TAG,
                    "returns assetId already in requestQueue : " + item.id + " index : " + index)
            return false
        }
        val instreamAdsConfig = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.instreamAdsConfig
        if(item.isLiveStream && instreamAdsConfig?.isAdRequestForLiveVideos == false) {
            IAdLogger.d(TAG,
                    "returns as Item is Live and isAdRequestForLiveVideos: false for " +
                            item.id + " index : " + index)
            return false
        }
        val minNoAdVidLength = instreamAdsConfig?.vdoNoAdReqMinLength
                ?: Constants.VIDEO_MIN_NO_AD_VIDEO_LENGTH
        if (!item.isLiveStream && item.durationLong < minNoAdVidLength) {
            IAdLogger.d(TAG,
                    "returns Video too short for ads " + item.id + " index : " + index)
            return false
        }

        IAdLogger.d(TAG, "requestInstreamAd assetId : " + item.id + " index : " + index)
        requestQueue[item.id] = IAdCacheItem(item.id, activity, dhtvAdParams, item.adExtras, index,
                commonAsset, pageEntity, section, playerCacheCallbacks)
        if (requestQueue.size == 1) {
            execNextAdRequest()
        }
        return true
    }

    fun getInstreamAd(item: ExoPlayerAsset?, position: Int): BaseAdEntity? {
        if (item == null) {
            IAdLogger.d(TAG, "getInstreamAd item is NULL, pos : $position")
            return null
        }
        IAdLogger.d(TAG, "getInstreamAd pos : $position itemId : ${item.id}")
        if (item.isDisableAds) {
            IAdLogger.d(TAG, "getInstreamAd ad disabled on item : ${item.id}")
            return null
        }
        try {
            if (isNextAdQualified(item)) {
                IAdLogger.d(TAG, "getInstreamAd Ad qualified to" +
                        " Show (CacheSize : ${cachedQueue.size} for ItemId - ${item.id}")

//                        if (true) {
//
//                            //Pre and mid Roll Ad
//                            var url = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";
//
//                            url = URLEncoder.encode(url, "UTF-8")
//
//                            val sdkAd = createAdEnity(url)
//                            if (sdkAd != null) {
//                                cachedQueue[item.id] = sdkAd
//                            }
//                            return sdkAd
//
//
//                          //m3u8 Test URl to handle error case
//                          //return "http://ads.newshunt.in/getvmap?host=stg1&uid=2039785bd83094439a79.46354676";
//                        }

                if (!CommonUtils.isEmpty(item.adUrl)) {
                    IAdLogger.d(TAG,
                            "getInstreamAd adUrl present in Asset : " + item.id + "\nadUrl :: " +
                                    item.adUrl)
                    val sdkAd = createAdEnity(item.adUrl)
                    if (sdkAd != null) {
                        cachedQueue[item.id] = sdkAd
                    }
                    return sdkAd
                }

                cachedQueue[item.id]?.let { return it }
            }
        } catch (e: Exception) {
            IAdLogger.d(TAG, "getInstreamAd Exception : ${e.message}")
        }

        IAdLogger.d(TAG, "getInstreamAd ad not found in Cache, returning NULL : ${item.id}")
        return null
    }

    private fun isNextAdQualified(item: ExoPlayerAsset): Boolean {

        val timeSinceAdPlayed = System.currentTimeMillis() - timeWhenLastAdPlayed
        Logger.d(TAG, "isNextAdQualified timeSinceAdPlayed : $timeSinceAdPlayed")
        Logger.d(TAG, "isNextAdQualified videoDuration : ${item.durationLong}")

        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        val minNoAdVidLength = adsUpgradeInfo?.instreamAdsConfig?.vdoNoAdReqMinLength
                ?: Constants.VIDEO_MIN_NO_AD_VIDEO_LENGTH
        val minAdDistanceMs = getMinAdDistanceMS(item)
        val ignoreVideoDuration = getIgnoreVideoDuration(item)

        Logger.d(TAG, "isNextAdQualified minNoAdVidLength : $minNoAdVidLength")
        Logger.d(TAG, "isNextAdQualified minAdDistanceMs : $minAdDistanceMs")
        Logger.d(TAG, "isNextAdQualified minVideoDuration : $ignoreVideoDuration")

        val value = item.durationLong > minNoAdVidLength &&
                (item.durationLong > ignoreVideoDuration || timeWhenLastAdPlayed <= 0 ||
                        timeSinceAdPlayed > minAdDistanceMs)

        IAdLogger.d(TAG, "isNextAdQualified returns : $value")
        return value
    }

    private fun getMinAdDistanceMS(item: ExoPlayerAsset): Long {
        cachedQueue[item.id]?.let {
            if (it.adDistanceMs > AdConstants.AD_NEGATIVE_DEFAULT) {
                Logger.d(TAG, "picked from AdResponse adDistanceMs : ${it.adDistanceMs}")
                return it.adDistanceMs.toLong()
            }
        }

        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        return adsUpgradeInfo?.dhTvAdConfig?.adDistanceMs
                ?: Constants.VIDEO_MIN_AD_RECENT_PLAYED_DURATION
    }

    private fun getIgnoreVideoDuration(item: ExoPlayerAsset): Long {
        cachedQueue[item.id]?.let {
            if (it.ignoreVideoDuration > -1) {
                Logger.d(TAG, "picked from AdResponse ignoreVideoDuration : ${it.ignoreVideoDuration}")
                return it.ignoreVideoDuration.toLong()
            }
        }

        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        return adsUpgradeInfo?.dhTvAdConfig?.ignoreVideoDuration
                ?: Constants.VIDEO_IGNORE_ITEM_DURATION
    }

    fun removeFromCache(itemId: String) {
        if (CommonUtils.isEmpty(itemId)) {
            return
        }
        try {
            cachedQueue.remove(itemId)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

    }

    @Synchronized
    private fun removeFromQueue(prevRequest: IAdHelper?, videoId: String) {
        val cacheItem = requestQueue.remove(videoId)
        cacheItem?.playerCacheCallbacks?.proceedWithStartVideo()
        prevRequest?.stop()
    }

    @Synchronized
    private fun removeAndRequestNext(inStreamAdsHelper: IAdHelper?, videoId: String) {
        removeFromQueue(inStreamAdsHelper, videoId)
        Logger.d(TAG, "removeAndRequestNext requestQueue.size() : " + requestQueue.size)
        execNextAdRequest()
    }

    override fun onAdReponseReceived(inStreamAdsHelper: IAdHelper) {
        scheduleHandle.removeCallbacks(runnableExecutor)
        IAdLogger.d(TAG, "onAdReponseReceived success ID : " + inStreamAdsHelper.cacheItem.videoId +
                " position : " + inStreamAdsHelper.cacheItem.position)
        val adEntity = inStreamAdsHelper.getCurrentInstreamAd
        adEntity?.let {
            if (AdsUtil.isIMAVideoAd(it)) {
                try {
                    var tagUrl = (it as ExternalSdkAd).external?.tagURL
                    tagUrl = URLDecoder.decode(tagUrl, "UTF-8")
                    IAdLogger.d(TAG, "onAdReponseReceived success URL : $tagUrl")
                } catch (e: Exception) {

                }
            }
            cachedQueue[inStreamAdsHelper.cacheItem.videoId] = adEntity
        }
        inStreamAdsHelper.stop()
        removeAndRequestNext(inStreamAdsHelper, inStreamAdsHelper.cacheItem.videoId)
    }

    override fun onAdResponseError(inStreamAdsHelper: IAdHelper) {
        scheduleHandle.removeCallbacks(runnableExecutor)
        IAdLogger.e(TAG, "onAdResponseError : " + inStreamAdsHelper.cacheItem.videoId +
                " position : " + inStreamAdsHelper.cacheItem.position)
        removeAndRequestNext(inStreamAdsHelper, inStreamAdsHelper.cacheItem.videoId)
        inStreamAdsHelper.stop()
    }

    @Synchronized
    private fun execNextAdRequest() {
        scheduleHandle.removeCallbacks(runnableExecutor)

        Logger.d(TAG, "execNextAdRequest requestQueue.size() : " + requestQueue.size)
        if (requestQueue.entries.iterator().hasNext()) {
            val nextRequest = requestQueue.entries.iterator().next()
            val cacheItem = nextRequest.value
            Logger.d(TAG, "execNextAdRequest next request Id : " + cacheItem.videoId +
                    " position :" + cacheItem.position)
            val instreamAdsHelper = IAdHelper(cacheItem.activity,
                    getUniqueRequestId(cacheItem.videoId), cacheItem, this)
            cacheItem.instreamAdsHelper = instreamAdsHelper
            cacheItem.requestDone = true
            Logger.d(TAG, "execNextAdRequest : ${cacheItem.instreamAdsHelper} and size : ${requestQueue.size}")
            instreamAdsHelper.start()

//            scheduleHandle.postDelayed(runnableExecutor, AD_REQUEST_TIME_OUT.toLong())
        }

    }

    private fun getUniqueRequestId(itemId: String): Int {
        ++uniqueRequestId
        IAdLogger.d(TAG, "getUniqueRequestId itemId : " + itemId +
                " uniqueRequestId :" + uniqueRequestId)
        return uniqueRequestId
    }

    private fun intoSeconds(milli: Long): Int {
        return TimeUnit.MILLISECONDS.toSeconds(milli).toInt()
    }

    fun onAdLoaded() {
        timeWhenLastAdPlayed = System.currentTimeMillis()
        IAdLogger.d(TAG, "Ad Displayed at $timeWhenLastAdPlayed")
    }

    private fun createAdEnity(adUrl: String): ExternalSdkAd? {
        Logger.d(TAG, "createAdEnity - InLine AD")
        try {
            //Copied from Buzz PlayerAdExoPlayer
            val externalTag = ExternalSdkAd.External()
            externalTag.tagURL = adUrl
            externalTag.data = ExternalSdkAdType.IMA_SDK.adType

            val adEntity = ExternalSdkAd()
            adEntity.adPosition = AdPosition.INLINE_VIDEO
            adEntity.external = externalTag

            return adEntity
        } catch (e: Exception) {
        }

        return null
    }

    fun clearInstance() {
        Logger.e(TAG, "clear queues requests : ${requestQueue.size} cache : ${cachedQueue.size}")
        requestQueue.forEach { it.value.instreamAdsHelper?.stop() }
        requestQueue.clear()
        cachedQueue.clear()
        adSpecList.clear()
        timeWhenLastAdPlayed = 0
    }
}