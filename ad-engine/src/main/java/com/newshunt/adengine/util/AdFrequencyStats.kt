/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import `in`.dailyhunt.money.frequency.FCData
import `in`.dailyhunt.money.frequency.FCEngine
import androidx.lifecycle.ProcessLifecycleOwner
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.model.entity.AdFCLimitReachedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.usecase.InsertAdFcDataUsecase
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.ads.AdFrequencyCapEntity
import com.newshunt.dataentity.model.entity.CampaignFCDataBody
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.toMediator2

/**
 * Stats for Frequency Cap data for ad campaigns.
 *
 * @author raunak.yadav
 */
object AdFrequencyStats {

    const val TAG = "AdFrequencyStats"

    private val fcStore = FCStore()

    private var persistFcUsecase = InsertAdFcDataUsecase(SocialDB.instance().adFrequencyCapDao()).toMediator2()
    private val fetchLiveData = SocialDB.instance().adFrequencyCapDao().all()

    init {
        AndroidUtils.getMainThreadHandler().post {
            fetchLiveData.observe(ProcessLifecycleOwner.get(), { data ->
                loadFCData(data.filter { it.cap > AdConstants.INVALID_FC })
            })
        }
    }

    fun loadFCData(newFcList: List<AdFrequencyCapEntity>) {
        if (newFcList.isEmpty()) return
        AdLogger.v(TAG, "loadFCData : $newFcList")
        synchronized(AdFrequencyStats::class) {
            fcStore.putAll(newFcList)
        }
    }

    /**
     * Update FC data from the ad.
     * Case 1: FCData is present -> Update FCData in Stores
     * Case 2: FCData is absent => No cap is present or has been removed now.
     *         Insert a default FCData in DB with cap = -1 to handle a case where FC config is
     *         introduced later via the FetchCampaignsAPI. Reason : Currently DB holds/refreshes
     *         FCData from received ads only and not the full response from the API. So we need a
     *         row in DB to allow this usecase.
     */
    fun updateAndPersistFCDataFrom(baseAdEntity: BaseAdEntity) {
        if (baseAdEntity.campaignId.isNullOrBlank()) return
        AdLogger.d(TAG, "updateAndPersistFCDataFrom : ${baseAdEntity.uniqueAdIdentifier}")
        baseAdEntity.campaignId?.let {
            synchronized(AdFrequencyStats::class) {
                listOfNotNull(
                    updateFCDataFrom(baseAdEntity, AdFCType.CAMPAIGN, it),
                    updateFCDataFrom(baseAdEntity, AdFCType.BANNER, it)
                ).also {
                    persistFCData(it)
                }
            }
        }
    }

    private fun updateFCDataFrom(adEntity: BaseAdEntity, type: AdFCType, campaignId: String): AdFrequencyCapEntity? {
        AdLogger.v(TAG, "updateFCDataFrom : ${adEntity.uniqueAdIdentifier} $type $campaignId")
        val capId = fcStore.id(type, adEntity) ?: return null
        val fcData = getFCFromAd(type, adEntity) ?: return AdFrequencyCapEntity(capId, type, campaignId, AdConstants.INVALID_FC)

        var data = fcStore.get(type, capId)
        if (data == null) {
            data = AdFrequencyCapEntity(capId, type, campaignId, fcData.cap, fcData.resetTime)
            fcStore.put(type, capId, data)
        } else {
            data.cap = fcData.cap
            data.resetTime = fcData.resetTime
        }
        return data
    }

    private fun getFCFromAd(type: AdFCType, adEntity: BaseAdEntity) : FCData? {
        return when (type) {
            AdFCType.BANNER -> adEntity.bannerFCData
            AdFCType.CAMPAIGN -> adEntity.fcData
        }
    }

    @JvmStatic
    fun getFcData(type: AdFCType, adEntity: BaseAdEntity?): AdFrequencyCapEntity? {
        adEntity ?: return null
        return fcStore.get(type, adEntity)
    }

    fun getFcData(type: AdFCType, id: String): AdFrequencyCapEntity? {
        return fcStore.get(type, id)
    }

    /**
     * If uniqueRequestId =-1, do not use soft counter.
     */
    @JvmStatic
    fun getImpressionCount(fcData: AdFrequencyCapEntity, uniqueRequestId: Int,
                           useSoftCounter: Boolean = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.enableSoftCounter
                               ?: false): Int {
        return fcData.impressionCounter.let {
            it.actual + if (useSoftCounter && uniqueRequestId != -1)
                (it.soft[uniqueRequestId] ?: 0) else 0
        }
    }

    /**
     * Provides FC met data to be sent in AdRequest.
     * Banners data is collated under corresponding Campaign's data.
     */
    @JvmStatic
    fun getFcMetCampaignsFor(uniqueRequestId: Int): Map<String, CampaignFCDataBody> {
        val fcMetMap = HashMap<String, CampaignFCDataBody>()

        synchronized(AdFrequencyStats::class) {
            val campaignMap = fcStore.getMap(AdFCType.CAMPAIGN)

            fcStore.getMap(AdFCType.BANNER).values.forEach {
                if (FCEngine.isLimitReached(it.campaignId, it, getImpressionCount(it, uniqueRequestId))) {
                    it.impressionCounter.softCounter = it.impressionCounter.soft[uniqueRequestId] ?: 0

                    if (!fcMetMap.containsKey(it.campaignId)) {
                        fcMetMap[it.campaignId] = CampaignFCDataBody(campaignMap[it.campaignId])
                    }
                    fcMetMap[it.campaignId]?.banners?.put(it.capId, it)
                }
            }
            campaignMap.values.forEach {
                if (FCEngine.isLimitReached(it.campaignId, it, getImpressionCount(it, uniqueRequestId))) {
                    it.impressionCounter.softCounter = it.impressionCounter.soft[uniqueRequestId] ?: 0
                    if (!fcMetMap.containsKey(it.campaignId)) {
                        fcMetMap[it.campaignId] = CampaignFCDataBody(it)
                    }
                }
            }
        }
        return fcMetMap
    }

    /**
     * Need not call for ads that do not need soft counter logic like PGI.
     */
    @JvmStatic
    fun onAdInsertedInView(adEntity: BaseAdEntity, uniqueRequestId: Int = -1) {
        if (adEntity.campaignId.isNullOrBlank())
            return
        AdLogger.v(TAG, "onAdInsertedInView : ${adEntity.uniqueAdIdentifier}")

        adInserted(adEntity, AdFCType.CAMPAIGN, uniqueRequestId)
        adInserted(adEntity, AdFCType.BANNER, uniqueRequestId)
    }

    @Synchronized
    private fun adInserted(adEntity: BaseAdEntity, type: AdFCType, uniqueRequestId: Int = -1) {
        val capId = fcStore.id(type, adEntity) ?: return
        val data = fcStore.get(type, capId) ?: return
        if (data.firstImpressionTime != 0L) {
            // Reset only if first ad is viewed , else insertion count will be reset.
            fcDataNeedsReset(data)
        }
        if (uniqueRequestId != -1) {
            data.impressionCounter.incSoft(uniqueRequestId)
        }
        AdLogger.v(TAG, "Ad ${adEntity.uniqueAdIdentifier} inserted in view : $capId :${data.impressionCounter}")
    }

    /**
     * Increment the FC counter and persist it.
     * In case where uniqueRequestId is passed as -1, soft counter does not matter
     */
    @JvmStatic
    fun onAdViewed(adEntity: BaseAdEntity, uniqueRequestId: Int = -1) {
        if (adEntity.campaignId.isNullOrBlank())
            return
        AdLogger.v(TAG, "onAdViewed : ${adEntity.uniqueAdIdentifier} ${adEntity.campaignId}")
        synchronized(AdFrequencyStats::class) {
            listOfNotNull(
                adViewed(adEntity, AdFCType.CAMPAIGN, uniqueRequestId),
                adViewed(adEntity, AdFCType.BANNER, uniqueRequestId)
            ).also {
                persistFCData(it)
            }
        }
    }

    private fun adViewed(adEntity: BaseAdEntity, type: AdFCType, uniqueRequestId: Int = -1) : AdFrequencyCapEntity? {
        val capId = fcStore.id(type, adEntity) ?: return null
        val data = fcStore.get(type, capId) ?: return null
        if (data.cap == AdConstants.INVALID_FC) {
            return null
        }

        if (fcDataNeedsReset(data)) {
            data.firstImpressionTime = System.currentTimeMillis()
        }
        data.impressionCounter.actual++
        if (uniqueRequestId != -1) {
            data.impressionCounter.decSoft(uniqueRequestId)
        }
        AdLogger.d(TAG, "Ad viewed : $capId : ${data.impressionCounter}")

        if (AdsUtil.isFCLimitReachedForAd(adEntity, uniqueRequestId, useSoftCounter = false)) {
            AdLogger.d(TAG, "FC limit exhausted for : $type-$capId via ad : ${adEntity.uniqueAdIdentifier}")
            AndroidUtils.getMainThreadHandler().post {
                val event = AdFCLimitReachedEvent(adEntity.uniqueAdIdentifier, capId, type)
                BusProvider.getUIBusInstance().post(event)
            }
            NativeAdInventoryManager.clearFCExhaustedAds(capId, type)
        }
        return data
    }

    /**
     * Decrement the FC counter as impression has failed and persist it.
     */
    @JvmStatic
    fun onAdImpressionFailed(adEntity: BaseAdEntity) {
        adEntity.campaignId?.let {
            synchronized(AdFrequencyStats::class) {
                listOfNotNull(
                    adImpressionFailed(adEntity, AdFCType.CAMPAIGN),
                    adImpressionFailed(adEntity, AdFCType.BANNER)
                ).also {
                    persistFCData(it)
                }
            }
        }
    }

    private fun adImpressionFailed(adEntity: BaseAdEntity, type: AdFCType) : AdFrequencyCapEntity? {
        return fcStore.get(type, adEntity)?.let {
            if (!fcDataNeedsReset(it)) {
                it.impressionCounter.actual--
            }
            AdLogger.d(TAG, "Ad[${adEntity.uniqueAdIdentifier}] impression failed : ${it.capId} : ${it.impressionCounter}")
            it
        }
    }

    private fun persistFCData(data: List<AdFrequencyCapEntity>) {
        if (data.isEmpty()) {
            return
        }
        persistFcUsecase.execute(data)
    }

    /**
     * Remove view related soft counters for all ad campaigns.
     */
    @JvmStatic
    fun onViewDestroyed(uniqueRequestId: Int) {
        AdLogger.v(TAG, "Remove soft counter for destroyed view id: $uniqueRequestId")
        synchronized(AdFrequencyStats::class) {
            fcStore.getMap(AdFCType.CAMPAIGN).values.forEach {
                it.impressionCounter.soft.remove(uniqueRequestId)
            }
            fcStore.getMap(AdFCType.BANNER).values.forEach {
                it.impressionCounter.soft.remove(uniqueRequestId)
            }
        }
    }

    @JvmStatic
    fun canFCDataResetFor(adEntity: BaseAdEntity): Boolean {
        return adEntity.campaignId?.let {
            synchronized(AdFrequencyStats::class) {
                val changedFCs = listOfNotNull(
                    resetFCIfApplicable(adEntity, AdFCType.CAMPAIGN),
                    resetFCIfApplicable(adEntity, AdFCType.BANNER)
                )
                persistFCData(changedFCs)
                return@let changedFCs.isNotEmpty()
            }
        } ?: false
    }

    private fun resetFCIfApplicable(adEntity: BaseAdEntity, type: AdFCType): AdFrequencyCapEntity? {
        return fcStore.get(type, adEntity)?.let {
            if (fcDataNeedsReset(it)) {
                return@let it
            }
            null
        }
    }

    fun canFCDataResetFor(type: AdFCType, capId: String?): Boolean {
        return capId?.let {
            synchronized(AdFrequencyStats::class) {
                val changedFCs = listOfNotNull(
                    resetFCIfApplicable(capId, type)
                )
                persistFCData(changedFCs)
                return@let changedFCs.isNotEmpty()
            }
        } ?: false
    }
    private fun resetFCIfApplicable(capId: String, type: AdFCType): AdFrequencyCapEntity? {
        return fcStore.get(type, capId)?.let {
            if (fcDataNeedsReset(it)) {
                return@let it
            }
            null
        }
    }

    //check if frequency cap slot needs reset.
    private fun fcDataNeedsReset(data: AdFrequencyCapEntity): Boolean {
        if (data.firstImpressionTime < 0) {
            return false
        }
        if (data.firstImpressionTime + data.resetTime * 1000 < System.currentTimeMillis()) {
            data.firstImpressionTime = 0
            data.impressionCounter.reset()
            AdLogger.d(TAG, "Ad FC slot reset for campaignId : ${data.campaignId}")
            return true
        }
        return false
    }
}