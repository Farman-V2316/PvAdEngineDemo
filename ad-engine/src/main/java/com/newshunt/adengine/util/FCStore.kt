/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.ads.AdFrequencyCapEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * Store to keep banner and Campaign specific FC data.
 *
 * @author raunak.yadav
 */
class FCStore {

    /**
     * Map of AdCampaignIds to their Frequency Cap info
     */
    private var campaignFCMap = ConcurrentHashMap<String, AdFrequencyCapEntity>()

    /**
     * Map of Ad BannerIds to their Frequency Cap info
     */
    private var bannerFCMap = ConcurrentHashMap<String, AdFrequencyCapEntity>()

    private fun map(type: AdFCType): ConcurrentHashMap<String, AdFrequencyCapEntity> {
        return when (type) {
            AdFCType.BANNER -> bannerFCMap
            AdFCType.CAMPAIGN -> campaignFCMap
        }
    }

    /**
     * Get FrequencyCap Map as per capping type.
     */
    fun getMap(type: AdFCType): Map<String, AdFrequencyCapEntity> {
        return map(type)
    }

    fun id(type: AdFCType, ad: BaseAdEntity): String? {
        return when (type) {
            AdFCType.BANNER -> if (ad is BaseDisplayAdEntity) {
                ad.bannerId
            } else if (ad is MultipleAdEntity) {
                ad.baseDisplayAdEntities.firstOrNull()?.bannerId
            } else null
            AdFCType.CAMPAIGN -> ad.campaignId
        }
    }

    fun put(type: AdFCType, ad: BaseAdEntity, fcData: AdFrequencyCapEntity) {
        id(type, ad)?.let {
            map(type)[it] = fcData
        }
    }

    fun put(type: AdFCType, id: String?, fcData: AdFrequencyCapEntity) {
        id?.let {
            map(type)[it] = fcData
        }
    }

    fun get(type: AdFCType, ad: BaseAdEntity): AdFrequencyCapEntity? {
        return id(type, ad)?.let {
            map(type)[it]
        }
    }

    fun get(type: AdFCType, id: String?): AdFrequencyCapEntity? {
        return id?.let {
            map(type)[it]
        }
    }

    fun contains(type: AdFCType, ad: BaseAdEntity): Boolean {
        return id(type, ad)?.let { map(type).contains(it) } ?: false
    }

    fun contains(type: AdFCType, id: String?): Boolean {
        return id?.let { map(type).contains(it) } ?: false
    }

    fun remove(type: AdFCType, id: String?) {
        id?.let { map(type).remove(it) }
    }

    fun putAll(fcList: List<AdFrequencyCapEntity>) {
        fcList.forEach {
            when (it.type) {
                AdFCType.BANNER -> bannerFCMap[it.capId] = it
                AdFCType.CAMPAIGN -> campaignFCMap[it.capId] = it
            }
        }
    }

}