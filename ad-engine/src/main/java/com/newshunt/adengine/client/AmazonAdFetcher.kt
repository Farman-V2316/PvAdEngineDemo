/*
 * Copyright (c) 2021 NewsHunt. All rights reserved.
 */

package com.newshunt.adengine.client

import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import com.amazon.device.ads.DTBAdSize
import com.amazon.device.ads.AdError
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.AmazonBidUtilities

import com.newshunt.common.helper.common.Logger
import com.amazon.device.ads.SDKUtilities
import com.newshunt.adengine.util.AdLogger


/**
 * Created by helly.p on 25/11/21.
 */
private const val TAG = "AmazonAdFetcher"

class AmazonAdFetcher {

    fun fetchBannerAd(width: Int, height: Int, slotUUID: String, adPosition:String, tag:String?) {
        val loader = DTBAdRequest()

        loader.setSizes(DTBAdSize(width, height, slotUUID))
        // for test : loader.setSizes(DTBAdSize(300, 250, "4761d13f-d3b8-4b1a-9297-45cbf9f4899c"))

        loader.loadAd(object : DTBAdCallback {
            override fun onFailure(adError: AdError) {
                AdLogger.e(TAG, "Oops banner ad load has failed: " + adError.message)
            }

            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                val customParam = dtbAdResponse.defaultDisplayAdsRequestCustomParams
                AdLogger.d(TAG, "Map of params : $customParam")
                val bidInfo = SDKUtilities.getBidInfo(dtbAdResponse) // for later use to fetch the ad from amazon server
                AmazonBidUtilities.saveBidInfo(slotUUID, customParam, bidInfo)
                var position = adPosition
                if(adPosition == AdPosition.SUPPLEMENT.value && !tag.isNullOrEmpty()) {
                    position = AdsUtil.getAdSlotName(tag, AdPosition.SUPPLEMENT)
                }
                AmazonBidUtilities.saveSlotUUIDForZone(position, slotUUID) // save slotIds per zone for ads BE adRequest
            }
        })
    }

    fun fetchInterstitialAd(slotUUID: String, adPosition:String) {
        val loader = DTBAdRequest()
        loader.setSizes(DTBAdSize.DTBInterstitialAdSize(slotUUID))
        loader.loadAd(object : DTBAdCallback {
            override fun onFailure(adError: AdError) {
                AdLogger.e(TAG, "Failed to load the interstitial ad" + adError.message)
            }

            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                val customParam = dtbAdResponse.defaultDisplayAdsRequestCustomParams
                AdLogger.d(TAG, "Map of params : $customParam")
                val bidInfo = SDKUtilities.getBidInfo(dtbAdResponse)
                AmazonBidUtilities.saveBidInfo(slotUUID, customParam, bidInfo)
                AmazonBidUtilities.saveSlotUUIDForZone(adPosition, slotUUID)
            }
        })
    }

}