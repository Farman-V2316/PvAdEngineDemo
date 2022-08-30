/*
 * Copyright (c) 2021 NewsHunt. All rights reserved.
 */

package com.newshunt.adengine.util

import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.mutableMapOf
import kotlin.collections.set

/**
 * Created by helly.p on 02/12/21.
 */

private const val LOG_TAG = "AmazonBidResponse"
object AmazonBidUtilities {

    private var bid: MutableMap<String, Bid> = mutableMapOf()

    private var zoneSlotIdMapping = HashMap<String, MutableList<String>>()

    fun saveBidInfo(slotUUID: String, bidInfo: Map<String, List<String>>, sdkBidInfo: String) {
        val bidItem = Bid(bidInfo, sdkBidInfo, System.currentTimeMillis())
        bid[slotUUID] = bidItem
    }

    fun clearBidInfo(slotUUID: String) {
        bid.remove(slotUUID)
    }

    fun isExpiredBid(slotUUID: String) : Boolean {
        if (bid.containsKey(slotUUID)) {
            bid[slotUUID]?.let {
                val cacheTTL = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.amazonSDK?.cacheTTL
                    ?: AdConstants.DEFAULT_AMAZON_ADS_TTL
                return CommonUtils.isTimeExpired(it.receiveTime, cacheTTL*1000L)
            }
        }
        AdLogger.d(LOG_TAG, "SlotId is not present in cache")
        return true
    }

    fun saveSlotUUIDForZone(adPosition: String, slotUUID: String) {
        if (!zoneSlotIdMapping.containsKey(adPosition)) {
            zoneSlotIdMapping[adPosition] = ArrayList()
        }
        zoneSlotIdMapping[adPosition]?.let {
            if(!it.contains(slotUUID))
                it.add(slotUUID)
        }
    }

    fun removeSlots(adPosition: String) {
        if(zoneSlotIdMapping.containsKey(adPosition)) {
            zoneSlotIdMapping.remove(adPosition)
        }
    }

    fun fetchBid(slotUUID: String): Map<String, List<String>>? {
        return bid[slotUUID]?.customParam
    }

    fun fetchBidToRender(slotUUID: String): String? {
        return bid[slotUUID]?.htmlBidToFetch
    }

    fun fetchSlotUUIDForZone(adPosition: String): MutableList<String>? {
        return zoneSlotIdMapping[adPosition]
    }
}

data class Bid(
    var customParam: Map<String, List<String>>,
    var htmlBidToFetch: String,
    var receiveTime: Long = 0L
)