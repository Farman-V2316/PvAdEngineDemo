/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.ads

import `in`.dailyhunt.money.frequency.FCData
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author raunak.yadav
 */
@Entity(tableName = "ads_frequency_cap_data",
        indices = [Index(value = ["capId", "type"], unique = true)])
data class AdFrequencyCapEntity(@PrimaryKey(autoGenerate = true) var id: Int = 0,
                                val capId: String,
                                val type: AdFCType,
                                val campaignId: String,
                                /**
                                 * impressions made till now in this slot
                                 */
                                var impressionCounter: FcCounter = FcCounter(0)) : FCData() {

    constructor(capId: String, type: AdFCType, campaignId: String, cap: Int = -1, resetTime: Long = -1L,
                firstImpressionTime: Long = 0L, impressionCounter: FcCounter = FcCounter(0))
            : this(capId = capId, campaignId = campaignId, type = type, impressionCounter = impressionCounter) {
        this.cap = cap
        this.resetTime = resetTime
        this.firstImpressionTime = firstImpressionTime
    }

    override fun toString(): String {
        return "AdFrequencyCapEntity(id=$id, capId='$capId', type=$type, campaignId='$campaignId', impressionCounter=$impressionCounter, ${super.toString()} )"
    }
}

enum class AdFCType {
    BANNER, CAMPAIGN
}
