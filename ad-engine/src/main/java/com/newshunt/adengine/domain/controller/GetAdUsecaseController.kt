/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.domain.controller

import android.os.Handler
import android.os.Looper
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.domain.usecase.GetAdUsecase
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.EvergreenFallbackReason
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdStatisticsHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.EvergreenAdsHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.adengine.other.news.di.qualifiers.UiBus
import com.newshunt.sdk.network.Priority
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

/**
 * Implementation of [GetAdUsecase].
 *
 * @author shreyas.desai
 */
@AutoFactory

class GetAdUsecaseController @JvmOverloads constructor(@param:Provided @param:UiBus private val uiBus: Bus,
                                                       private val uniqueRequestId: Int,
                                                       private val needBusRegistration: Boolean = true) :
    GetAdUsecase {
    private var isRegistered = false
    private var evergreenAdHandler = Handler(Looper.getMainLooper())
    private var taskMap = HashMap<AdPosition, Runnable>()
    private var adRequestStatus = HashSet<String>()

    override fun requestAds(adRequest: AdRequest): NativeAdContainer? {
        return requestAds(adRequest, AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo, true)
    }

    override fun requestAds(adRequest: AdRequest,
                            adsUpgradeInfo: AdsUpgradeInfo?): NativeAdContainer? {
        return requestAds(adRequest, adsUpgradeInfo, true)
    }

    /**
     * Returns local ads available in cache and additionaly sends out a fetch request if eligible.
     * If contextual, return empty container.
     * If ad is blocked for zone, return null.
     */
    override fun requestAds(adRequest: AdRequest, adsUpgradeInfo: AdsUpgradeInfo?,
                            postLocalAdsAsync: Boolean): NativeAdContainer? {

        // if ad request is eligible to be discarded, discard it.
        if (discardAdRequest()) {
            AdLogger.d("GetAdUsecaseController", "Discarded ad request : $uniqueRequestId. " +
                    "Zone: ${adRequest.zoneType} " +
                    "entityId: " + "${adRequest.entityId} " + "entityType: ${adRequest.entityType}"
            )
            if (postLocalAdsAsync) {
                AndroidUtils.getMainThreadHandler().post {
                    val nativeAdContainer = NativeAdContainer(uniqueRequestId,
                        adRequest.zoneType,
                        doneRequestProcessing = true)
                    sendAdsToPresenter(nativeAdContainer)
                }
            }
            return null
        }

        if (!isRegistered && needBusRegistration) {
            BusProvider.getAdBusInstance().register(this)
            isRegistered = true
        }
        val priority = when (adRequest.zoneType) {
            AdPosition.PP1,
            AdPosition.P0 -> if (adRequest.isPrefetch) Priority.PRIORITY_NORMAL else Priority.PRIORITY_HIGHEST
            AdPosition.MASTHEAD -> Priority.PRIORITY_HIGH
            else -> Priority.PRIORITY_NORMAL
        }

        initAdRequestStatus(adRequest)

        //Fetch evergreen ads if applicable.
        var egTimeout: Long = -1L
        if (!AdsUtil.zonesWhichSkipEvergreenAds.contains(adRequest.zoneType)) {
            egTimeout = fetchEvergreenAds(adsUpgradeInfo, adRequest, priority, postLocalAdsAsync)
        }
        //Fetch regular ads.
        getInventoryInstance(adRequest.zoneType)?.requestAds(adRequest, uniqueRequestId,
            priority, adsUpgradeInfo, true)

        // All views can handle async delivery of ads, but only some handle the sync delivery of
        // cached ads. AdRequest must be marked served only by the views that support sync serving.
        // TODO (raunak) :Handle the sync serving of ads to the views who request it. For now serve
        // all async.
        return NativeAdContainer(uniqueRequestId, adRequest.zoneType)
       /* val localRegAds = getInventoryInstance(adRequest.zoneType)?.requestAds(adRequest, uniqueRequestId,
            priority, adsUpgradeInfo, true)
        return if (egTimeout == 0L) {
            NativeAdContainer(uniqueRequestId, adRequest.zoneType)
        } else {
            markServed(localRegAds)
            localRegAds
        }*/
    }

    /**
     * Returns the timeout for evergreen ad if applicable, -1L otherwise.
     */
    private fun fetchEvergreenAds(adsUpgradeInfo: AdsUpgradeInfo?, adRequest: AdRequest,
                                  priority: Priority, postLocalAdsAsync: Boolean): Long {
        var egTimeout = -1L
        adsUpgradeInfo?.evergreenAds?.let { config ->
            if (config.enabled) {
                getInventoryInstance(AdPosition.EVERGREEN)?.let { inventoryManager ->
                    //Process pending ads, if any.
                    inventoryManager.readPersistedAds()

                    EvergreenAdsHelper.getTimeoutValue(config)?.let { timeOut ->
                        egTimeout = timeOut
                        val egAdRequest = adRequest.copyWith(adRequest.localRequestedAdTags)

                        //After timeout fetch EG ad and serve
                        val task = Runnable {
                            inventoryManager.requestAds(egAdRequest,
                                uniqueRequestId, priority, adsUpgradeInfo, postLocalAdsAsync)?.let {
                                AdLogger.d("Evergreen", "Serving EG ads : ${it.baseAdEntities}")
                                taskMap.remove(egAdRequest.zoneType)

                                // Post Evergreen response only if any ad is available
                                // as some zones (instream-vdo) stop listening for ads thereafter.
                                if (!it.baseAdEntities.isNullOrEmpty()) {
                                    val fallbackReason = EvergreenFallbackReason(config.isRegUser ?: true,
                                        timeOut, AdStatisticsHelper.adStatistics.totalSeenAds == 0)
                                    it.baseAdEntities?.map { ad ->
                                        ad.fallbackReason = fallbackReason
                                    }
                                    onAdsResponse(it)
                                }
                            }
                        }
                        taskMap[egAdRequest.zoneType] = task
                        AdLogger.v("Evergreen", "Posting task for EG ads ${egAdRequest.zoneType}:$timeOut for id :$uniqueRequestId")
                        evergreenAdHandler.postDelayed(task, timeOut)
                    }
                }
            }
        }
        return egTimeout
    }

    @Subscribe
    override fun onAdsResponse(nativeAdContainer: NativeAdContainer) {
        if (nativeAdContainer.uniqueRequestId == uniqueRequestId) {
            if (canServe(nativeAdContainer)) {
                sendAdsToPresenter(nativeAdContainer)
                if (nativeAdContainer.doneRequestProcessing) {
                    adRequestCompleted(nativeAdContainer.adPosition)
                }
            }
        }
    }

    override fun sendAdsToPresenter(nativeAdContainer: NativeAdContainer) {
        uiBus.post(nativeAdContainer)
    }

    override fun destroy() {
        if (isRegistered) {
            BusProvider.getAdBusInstance().unregister(this)
            isRegistered = false
            evergreenAdHandler.removeCallbacksAndMessages(null)
            taskMap.clear()
        }
    }

    fun getInventoryInstance(adPosition: AdPosition?): NativeAdInventoryManager? {
        if (adPosition == null) {
            return null
        }
        return when (adPosition) {
            AdPosition.SPLASH -> NativeAdInventoryManager.getSplashInstance()
            AdPosition.P0 -> NativeAdInventoryManager.getP0Instance()
            AdPosition.LIST_AD -> NativeAdInventoryManager.getCardP1Instance()
            AdPosition.MASTHEAD -> NativeAdInventoryManager.getMastHeadInstance()
            AdPosition.STORY -> NativeAdInventoryManager.getStoryInstance()
            AdPosition.SUPPLEMENT -> NativeAdInventoryManager.getSupplementAdsInstance()
            AdPosition.PGI -> NativeAdInventoryManager.getPgiInstance()
            AdPosition.INSTREAM_VIDEO -> NativeAdInventoryManager.getInStreamVideoInstance()
            AdPosition.INLINE_VIDEO -> NativeAdInventoryManager.getInlineVideoInstance()
            AdPosition.VDO_PGI -> NativeAdInventoryManager.getVdoPgiInstance()
            AdPosition.DHTV_MASTHEAD -> NativeAdInventoryManager.getDhtvMastHeadInstance()
            AdPosition.PP1 -> NativeAdInventoryManager.getPP1Instance()
            AdPosition.SPLASH_DEFAULT -> NativeAdInventoryManager.getDefaultSplashInstance()
            AdPosition.EVERGREEN -> NativeAdInventoryManager.getEvergreenCacheInstance()
            AdPosition.EXIT_SPLASH -> NativeAdInventoryManager.getExitSplashInstance()
        }
    }

    /**
     * As per requirement we want to discard few ad requests when they meet certain conditions.
     *
     * @return true/false
     */
    private fun discardAdRequest(): Boolean {
        //PANDA: removed manually for testing
       /* if (AndroidUtils.isInRestrictedMonkeyMode()) {
            AdLogger.d(Constants.MONKEY_LOG_TAG, "Discarding ad request in monkey test mode")
            return true
        }*/
        return false
    }

    private fun initAdRequestStatus(adRequest: AdRequest) {
        val adPosition = adRequest.zoneType
        if (adRequest.localRequestedAdTags.isNullOrEmpty()) {
            adRequestStatus.add(adPosition.name.plus(AdConstants.DEFAULT_ADTAG))
        } else {
            adRequest.localRequestedAdTags.forEach { tag ->
                adRequestStatus.add(adPosition.name.plus(tag))
            }
        }
    }

    private fun canServe(nativeAdContainer: NativeAdContainer): Boolean {
        if (adRequestStatus.isEmpty()) return false
        // If the container was originally empty, it should be served to view that might mark its
        // request done.
        if (nativeAdContainer.baseAdEntities.isNullOrEmpty()) return true

        val eligibleAds = mutableListOf<BaseAdEntity>()
        nativeAdContainer.baseAdEntities?.forEach { ad ->
            val key = ad.adPosition?.name.plus(ad.adTag ?: AdConstants.DEFAULT_ADTAG)
            if (adRequestStatus.contains(key)) {
                eligibleAds.add(ad)
                adRequestStatus.remove(key)
            }
        }
        nativeAdContainer.baseAdEntities = eligibleAds
        return !eligibleAds.isNullOrEmpty()
    }

    private fun markServed(localRegAds: NativeAdContainer?) {
        localRegAds?.baseAdEntities?.forEach { ad ->
            val key = ad.adPosition?.name.plus(ad.adTag ?: AdConstants.DEFAULT_ADTAG)
            adRequestStatus.remove(key)
        }
    }

    private fun adRequestCompleted(adPosition: AdPosition) {
        adRequestStatus.forEach {
            if (it.startsWith(adPosition.value, ignoreCase = true)) {
                //some tag might still not be fulfilled. Do not mark complete.
                return
            }
        }
        taskMap[adPosition]?.let {
            AdLogger.v("Evergreen", " Removing task for eg ads $adPosition")
            evergreenAdHandler.removeCallbacks(it)
        }
        taskMap.remove(adPosition)
    }
}