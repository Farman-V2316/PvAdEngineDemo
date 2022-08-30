/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.model.entity

import `in`.dailyhunt.money.frequency.FCData
import com.newshunt.dataentity.ads.AdFrequencyCapEntity
import com.newshunt.dataentity.ads.FcCounter
import java.io.Serializable

/**
 * @author raunak.yadav
 */
class AdCampaignsInfo : Serializable {
    var campaigns: Map<String, CampaignInfo>? = null
}

data class CampaignInfo(val fcData: FCData?,
                        val banners: Map<String, CampaignInfo>?)

/**
 * Used in AdRequest Post Body to send FC Met Campaign data
 */
class CampaignFCDataBody(fcData: AdFrequencyCapEntity?) : FCData() {
    private var impressionCounter: FcCounter
    val banners: HashMap<String, AdFrequencyCapEntity> = HashMap()

    init {
        this.cap = fcData?.cap ?: -1
        this.resetTime = fcData?.resetTime ?: -1L
        this.firstImpressionTime = fcData?.firstImpressionTime ?: -1L
        this.impressionCounter = fcData?.impressionCounter ?: FcCounter(0)
    }
}