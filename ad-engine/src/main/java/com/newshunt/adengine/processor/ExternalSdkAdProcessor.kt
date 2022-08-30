/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import android.app.Activity
import com.newshunt.adengine.client.requester.AdRequester
import com.newshunt.adengine.client.requester.AmazonAdRequester
import com.newshunt.adengine.client.requester.DfpAdRequester
import com.newshunt.adengine.client.requester.FacebookAdRequester
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.ExternalAdResponse
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.AndroidUtils

/**
 * Processes [ExternalSdkAd]
 *
 * @author raunak.yadav
 */
class ExternalSdkAdProcessor(baseAdEntity: BaseAdEntity?,
                             private var adReadyHandler: AdReadyHandler?)
    : BaseAdProcessor, ExternalAdResponse {

    private val externalSdkAd: ExternalSdkAd? = baseAdEntity as? ExternalSdkAd?

    override fun processAdContent(adRequest: AdRequest?) {
        fetchSdkAd(this, adRequest?.activity)
    }

    private fun fetchSdkAd(externalAdResponse: ExternalAdResponse,
                           activity: Activity?) {
        if (externalSdkAd?.external == null) {
            AdLogger.d(LOG_TAG, "externalSdkAd or externalAdTag is null")
            externalAdResponse.onResponse(null)
            return
        }

        val externalAdType = ExternalSdkAdType.fromAdType(externalSdkAd.external?.data)

        if (!isAdValidForZone(externalAdType)) {
            externalAdResponse.onResponse(null)
            return
        }

        if (ExternalSdkAdType.IMA_SDK == externalAdType) {
            externalSdkAd.isVideoAd = true
            adReadyHandler?.let {
                if (externalSdkAd.isContentEnabledAd()) {
                    ContentAdProcessor(externalSdkAd, it).processAdContent()
                    return
                }
            }
        }

        if (!needsAdRequester(externalAdType)) {
            AdLogger.d(LOG_TAG, "$externalAdType requested")
            externalAdResponse.onResponse(externalSdkAd)
            return
        }

        val adRequester = getAdRequesterForAdType(externalAdType)
        if (adRequester == null) {
            AdLogger.d(LOG_TAG, "native ad requester is null")
            externalAdResponse.onResponse(null)
            return
        }
        adRequester.decodeAdMetaData(externalSdkAd)

        AndroidUtils.getMainThreadHandler().post {
            try {
                AdLogger.d(LOG_TAG, "Requesting for $externalAdType with id : " + "${externalSdkAd.external?.adUnitId}")
                adRequester.fetchAd(externalAdResponse, externalSdkAd, activity)
            } catch (e: Exception) {
                AdLogger.e(LOG_TAG, "External sdk crash $adRequester ${e.message}")
                externalAdResponse.onResponse(null)
            }
        }
    }

    private fun isAdValidForZone(externalAdType: ExternalSdkAdType?): Boolean {
        if (externalAdType == null) {
            AdLogger.d(LOG_TAG, "externalAdType is null")
            return false
        }

        //currently buzz supports only few external sdk ads, so discarding the unsupported one's
        if (AdsUtil.isBuzzZone(externalSdkAd?.adPosition) && !AdsUtil.isValidBuzzAd(externalSdkAd)) {
            AdLogger.d(LOG_TAG, "invalid buzz ad $externalAdType")
            return false
        }

        if (AdsUtil.isNewsZone(externalSdkAd?.adPosition) && !AdsUtil.isValidNewsAd(externalSdkAd)) {
            AdLogger.d(LOG_TAG, "invalid ad : $externalAdType at zone : ${externalSdkAd?.adPosition}")
            return false
        }
        return true
    }

    private fun needsAdRequester(externalSdkAdType: ExternalSdkAdType?): Boolean {
        return when (externalSdkAdType) {
            ExternalSdkAdType.IMA_SDK, ExternalSdkAdType.INLINE_VIDEO_AD -> false
            else -> true
        }
    }

    private fun getAdRequesterForAdType(externalSdkAdType: ExternalSdkAdType): AdRequester? {
        return when (externalSdkAdType) {
            ExternalSdkAdType.DFP_NATIVE,
            ExternalSdkAdType.DFP_NATIVE_APP_DOWNLOAD,
            ExternalSdkAdType.DFP_NATIVE_CONTENT,
            ExternalSdkAdType.DFP_STANDARD,
            ExternalSdkAdType.DFP_INTERSTITIAL,
            ExternalSdkAdType.DFP_NATIVE_INTERSTITIAL,
            ExternalSdkAdType.DFP_CUSTOM_NATIVE -> DfpAdRequester()

            ExternalSdkAdType.AMAZON_STANDARD,
            ExternalSdkAdType.AMAZON_INTERSTITIAL -> AmazonAdRequester()

            ExternalSdkAdType.FB_NATIVE_AD,
            ExternalSdkAdType.FB_INTERSTITIAL_AD,
            ExternalSdkAdType.FB_NATIVE_INTERSTITIAL,
            ExternalSdkAdType.FB_NATIVE_BID,
            ExternalSdkAdType.FB_NATIVE_INTERSTITIAL_BID -> {
                 if (AppConfig.getInstance()?.isGoBuild == true) {
                    null
                } else FacebookAdRequester()
            }
            else -> null
        }
    }

    override fun onResponse(externalSdkAd: ExternalSdkAd?) {
        if (externalSdkAd != null) {
            val externalContent = externalSdkAd.external?.data
            AdLogger.d(LOG_TAG, "Sending external ad  with type = $externalContent")
            val externalAdType = ExternalSdkAdType.fromAdType(externalContent)
            if (isAdValidForZone(externalAdType)) {
                adReadyHandler?.onReady(externalSdkAd)
            }
            adReadyHandler = null
            return
        }
        adReadyHandler?.onReady(null)
    }

    companion object {
        private const val LOG_TAG = "ExternalSdkAdProcessor"
    }
}
