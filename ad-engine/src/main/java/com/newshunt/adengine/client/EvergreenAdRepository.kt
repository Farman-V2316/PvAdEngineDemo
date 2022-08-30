/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.client

import android.os.Bundle
import com.newshunt.adengine.ReadPersistedAdUsecase
import com.newshunt.adengine.RemovePersistedAdUsecase
import com.newshunt.adengine.model.entity.AdSelectionMeta
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdMacroUtils
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.EvergreenSplashUtil
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.sdk.network.Priority
import com.squareup.otto.Bus
import java.util.Observable
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * This repository caches Evergreen ads and differs from its parent by :
 *  1. N/w request : Only cached ads are served and no n/w request will go out.
 *  2. Selection logic: The ads are selected based on priority followed by probablistic distribution
 *  of their weights within same priority
 *  3. Ads can be reused again but will be served to view with a different UUID.
 *  4. No cache size limit. All persisted eg ads can be kept in memory.
 *
 * @author raunak.yadav
 */
internal class EvergreenAdRepository(adBus: Bus,
                                     cacheSize: Int,
                                     cacheThreshold: Int,
                                     adPosition: AdPosition,
                                     excludedBannerProvider: ExcludedBannerProvider,
                                     readPersistedAdUsecase: ReadPersistedAdUsecase,
                                     private val removePersistedAdUsecase:
                                     MediatorUsecase<Bundle, Boolean>)
    : AdRepository(adBus,
    cacheSize,
    cacheThreshold,
    adPosition,
    excludedBannerProvider,
    readPersistedAdUsecase,
    removePersistedAdUsecase, false) {

    override fun update(observable: Observable?) {
        // Do nothing. Ads need not be evicted.
    }

    /**
     * Returns the cached ads that match zone and tag.
     * The adrequest passed here will have adPosition of requested zone and so will be different
     * from Evergreen.
     *
     * NativeAdContainer with available cached ads.
     */
    override fun requestAds(adRequest: AdRequest,
                            uniqueRequestId: Int,
                            priority: Priority,
                            adsUpgradeInfo: AdsUpgradeInfo?,
                            postLocalAdsAsync: Boolean): NativeAdContainer? {
        AdLogger.w(TAG, "Fetching evergreen ad for ${adRequest.zoneType} with id =$uniqueRequestId")

        this.uniqueRequestId = uniqueRequestId

        val adContainer = serveLocalAds(adRequest, uniqueRequestId, false)
        adContainer.adPosition = adRequest.zoneType
        return adContainer
    }

    override fun getServeableAds(adRequest: AdRequest,
                                 queue: ConcurrentLinkedQueue<BaseAdEntity>): List<BaseAdEntity> {
        val uniqueAds = mutableListOf<BaseAdEntity>()
        if (queue.isEmpty()) {
            return uniqueAds
        }
        val adTags = mutableListOf<String>()
        adRequest.localRequestedAdTags?.let {
            adTags.addAll(it)
        }
        if (adTags.isEmpty() && !areSubSlotsMandatory(adRequest.zoneType)) {
            adTags.add(AdConstants.DEFAULT_ADTAG)
        }

        adTags.forEach { tag ->
            val itr = queue.iterator()
            val adCandidates = mutableListOf<SelectedAdInfo>()
            while (itr.hasNext()) {
                val baseAdEntity = itr.next()
                if (isAdTimeExpired(baseAdEntity)) {
                    AdLogger.e(TAG, "Ads endEpoch has passed. Removing ${baseAdEntity.uniqueAdIdentifier}")
                    itr.remove()
                    baseAdEntity.adGroupId?.let {
                        removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adGroupId = it))
                    }
                    continue
                }

                if (!isAdTimeValid(baseAdEntity)) {
                    continue
                }

                //Check if we have any ad supported for this zone and slot.
                val adMeta = getAdDataIfSupported(baseAdEntity, adRequest.zoneType, tag) ?: continue

                // FC check
                if (baseAdEntity.fcData != null &&
                    baseAdEntity.campaignId?.let {
                        AdsUtil.isFCLimitReachedForAd(baseAdEntity, uniqueRequestId)
                    } == true) {
                    AdLogger.e(TAG, "FC validation failed for ${baseAdEntity.uniqueAdIdentifier}")
                    continue
                }
                adCandidates.add(adMeta)
            }
            if (adCandidates.isNotEmpty()) {
                adRequest.localRequestedAdTags?.remove(tag)
            }
            val finalAd = selectAdWithWeightedLogic(adCandidates)?.run {
                getFinalAd(this, adRequest, tag)
            }
            finalAd?.let { uniqueAds.add(finalAd) }
        }

        return uniqueAds
    }

    private fun areSubSlotsMandatory(adPosition: AdPosition): Boolean {
        return when (adPosition) {
            AdPosition.SUPPLEMENT,
            AdPosition.PP1 -> true
            else -> false
        }
    }

    override fun handleForSplashAd(baseAdEntity: BaseAdEntity): Boolean {
        if (baseAdEntity !is BaseDisplayAdEntity || baseAdEntity is EmptyAd) return false
        if (baseAdEntity.supportedZones?.containsKey(AdPosition.SPLASH.value) == true) {
            AdLogger.d(TAG, "Found an Evergreen Splash ad. Save config. $baseAdEntity")

            EvergreenSplashUtil.addSplashMetaFrom(baseAdEntity,
                getAdDataIfSupported(baseAdEntity, AdPosition.SPLASH)?.selectionMeta)
        }
        return false
    }

    override fun sendAds(baseAdEntities: List<BaseAdEntity>?,
                         uniqueRequestId: Int, postOnBus: Boolean): NativeAdContainer {
        val container = NativeAdContainer(uniqueRequestId, adPosition, doneRequestProcessing = true)

        if (baseAdEntities.isNullOrEmpty()) {
            return container
        }
        container.baseAdEntities = baseAdEntities
        return container
    }

    // Does current time lies in ad's time window.
    private fun isAdTimeValid(ad: BaseAdEntity): Boolean {
        val current = System.currentTimeMillis() / 1000
        return (ad.startepoch == null || ad.startepoch!! <= current) &&
                (ad.endepoch == null || ad.endepoch!! >= current)
    }

    private fun isAdTimeExpired(ad: BaseAdEntity): Boolean {
        return ad.endepoch?.let { end ->
            val current = System.currentTimeMillis() / 1000
            end < current
        } ?: false
    }

    private fun getAdDataIfSupported(ad: BaseAdEntity, adPosition: AdPosition,
                                     slot: String? = AdConstants.DEFAULT_ADTAG): SelectedAdInfo? {
        return ad.supportedZones?.get(adPosition.value)?.get(slot)?.let {
            return SelectedAdInfo(ad, it)
        }
    }

    /**
     * Return the winning ad from a list of ads using probabilistic distribution based on weight.
     */
    private fun selectAdWithWeightedLogic(adCandidates: MutableList<SelectedAdInfo>): SelectedAdInfo? {
        if (adCandidates.isEmpty()) return null
        if (adCandidates.size == 1) return adCandidates[0]

        adCandidates.sortWith { ad1, ad2 ->
            (100 * (ad2.selectionMeta.priority - ad1.selectionMeta.priority)).toInt()
        }
        val maxPriority = adCandidates[0].selectionMeta.priority

        val highPriorityAds = adCandidates.filter {
            it.selectionMeta.priority == maxPriority
        }
        if (highPriorityAds.size == 1) {
            return highPriorityAds[0]
        }

        var totalWeight = 0
        highPriorityAds.forEach { adInfo ->
            totalWeight += adInfo.selectionMeta.weight
        }
        val randomNormalizedNumber = Math.random() * totalWeight

        var adIndex = 0
        var weightTillNow = 0
        var adInfo: SelectedAdInfo = highPriorityAds[0]

        while (weightTillNow < randomNormalizedNumber) {
            adInfo = highPriorityAds[adIndex++]
            weightTillNow += adInfo.selectionMeta.weight
        }
        return adInfo
    }

    /**
     * Populate the ad with required info for selected zone/tag.
     */
    private fun getFinalAd(adInfo: SelectedAdInfo,
                           adRequest: AdRequest,
                           tag: String?): BaseAdEntity {
        val adStr = JsonUtils.toJson(adInfo.ad)
        val adCopy = JsonUtils.fromJson(adStr, adInfo.ad.javaClass)
        overrideAdParams(adCopy, adInfo, adRequest.zoneType, tag)
        AdMacroUtils.replaceMacrosInAd(adCopy, adRequest)
        adCopy.isEvergreenAd = true
        AdLogger.d(TAG, "[${adRequest.zoneType}][$tag] Final ad : $adCopy")
        return adCopy
    }

    private fun overrideAdParams(ad: BaseAdEntity, adInfo: SelectedAdInfo,
                                 adPosition: AdPosition, tag: String?) {
        ad.adPosition = adPosition
        ad.adTag = tag
        if (ad is BaseDisplayAdEntity) {
            ad.copyFrom(adInfo.selectionMeta)
        } else if (ad is MultipleAdEntity) {
            ad.baseDisplayAdEntities.forEach {
                it.copyFrom(adInfo.selectionMeta)
            }
        }
    }

    companion object {
        const val TAG = "EvergreenAdRepository"
    }

    class SelectedAdInfo(var ad: BaseAdEntity, val selectionMeta: AdSelectionMeta)

}