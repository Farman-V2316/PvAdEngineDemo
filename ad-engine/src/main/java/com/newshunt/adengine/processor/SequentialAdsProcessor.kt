/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.processor

import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.AdsFallbackEntity
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.view.BackupAdsCache
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.util.Collections
import java.util.HashMap
import java.util.concurrent.ExecutorService
import kotlin.collections.set

/**
 * Processes MultipleAdEntity sequentially
 * AdsFallbackEntity will only contain MultipleAdEntity for clubType = sequence.
 *
 * Will handle following cases :
 *
 * 1.  A + B + C
 *
 * 2.  A + B + (C -> X)
 *
 * - If timeOffset of two ads is same, the grouping is for fallback. (e.g. C -> X)
 * @author raunak.yadav
 */
class SequentialAdsProcessor(private val multipleAdEntity: MultipleAdEntity,
                             private val adReadyHandler: AdReadyHandler?,
                             private val backupAdsCache: BackupAdsCache?,
                             private val responseExecutor: ExecutorService)
    : BaseAdProcessor, AdReadyHandler {

    private var finalAd = MultipleAdEntity()
    private var adCount: Int = 0

    init {
        finalAd.setAdContentType(multipleAdEntity.type)
    }

    override fun processAdContent(adRequest: AdRequest?) {
        if (processTogether()) {
            adCount = 1
            AdsFallbackProcessor(AdsFallbackEntity().apply { addBaseAdEntity(multipleAdEntity) }, this, backupAdsCache, responseExecutor)
                    .processAdContent(adRequest)
        } else {
            val adsList = classifyUsingTimeOffset()
            adCount = adsList.size
            for (ad in adsList) {
                AdsFallbackProcessor(ad, this, backupAdsCache, responseExecutor).processAdContent(adRequest)
            }
        }
    }

    // Ads with same timeOffset will be treated as a fallback group
    private fun classifyUsingTimeOffset(): MutableList<AdsFallbackEntity> {
        val clubbedEntities = HashMap<String, MutableList<BaseDisplayAdEntity>>()
        for (adEntity in multipleAdEntity.baseDisplayAdEntities) {
            clubAdsWithSameGroupId(clubbedEntities, adEntity)
        }

        val adsList: MutableList<AdsFallbackEntity> = ArrayList()
        // Combine ads in same group to a single fallback entity.
        for (clubbedEntity in clubbedEntities.values) {
            val adsFallbackEntity = AdsFallbackEntity()
            for (adEntity in clubbedEntity) {
                adsFallbackEntity.addBaseAdEntity(adEntity)
            }
            adsList.add(adsFallbackEntity)
        }
        return adsList
    }

    private fun clubAdsWithSameGroupId(clubbedEntities: HashMap<String,
            MutableList<BaseDisplayAdEntity>>,
                                       displayAdEntity: BaseDisplayAdEntity) {
        val key = displayAdEntity.timeOffset.toString()
        if (!clubbedEntities.containsKey(key)) {
            clubbedEntities[key] = ArrayList()
        }
        clubbedEntities[key]?.add(displayAdEntity)

        AdLogger.d(TAG, "Adding to group id : " + key + " ad with type :" +
                displayAdEntity.type)
    }

    private var adComparator = { lhs: BaseDisplayAdEntity, rhs: BaseDisplayAdEntity ->
        Integer.compare(lhs.timeOffset.toInt(), rhs.timeOffset.toInt())
    }

    private fun processTogether(): Boolean {
        return when (multipleAdEntity.type) {
            AdContentType.APP_DOWNLOAD,
            AdContentType.CONTENT_AD -> true
            else -> false
        }
    }

    override fun onReady(baseAdEntity: BaseAdEntity?) {
        AdLogger.d(TAG, "$adCount Response received : $baseAdEntity")
        when (baseAdEntity) {
            is BaseDisplayAdEntity -> finalAd.addBaseDisplayAdEntity(baseAdEntity)
            is MultipleAdEntity -> finalAd = baseAdEntity
        }
        adCount--
        if (adCount <= 0) {
            if (CommonUtils.isEmpty(finalAd.baseDisplayAdEntities)) {
                AdLogger.d(TAG, "No-fill received for ad group id : ${multipleAdEntity.adGroupId}")
                adReadyHandler?.onReady(null)
            } else {
                Collections.sort(finalAd.baseDisplayAdEntities, adComparator)
                adReadyHandler?.onReady(finalAd)
            }
        }
    }

    companion object {
        private const val TAG = "SequentialAdsProcessor"
    }
}