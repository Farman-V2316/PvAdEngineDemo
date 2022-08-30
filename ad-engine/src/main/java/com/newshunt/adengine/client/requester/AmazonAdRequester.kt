/*
 * Copyright (c) 2021 NewsHunt. All rights reserved.
 */

package com.newshunt.adengine.client.requester

import android.app.Activity
import android.content.Context
import android.view.View
import com.amazon.device.ads.DTBAdBannerListener
import com.amazon.device.ads.DTBAdInterstitial
import com.amazon.device.ads.DTBAdInterstitialListener
import com.amazon.device.ads.DTBAdView
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.ExternalAdResponse
import com.newshunt.adengine.model.entity.AdExitEvent
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsTimeoutHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.AmazonBidUtilities
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Created by helly.p on 20/12/21.
 */
class AmazonAdRequester : AdRequester {

    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null
    private var timeoutHelper: AdsTimeoutHelper? = null

    override fun fetchAd(
        externalAdResponse: ExternalAdResponse,
        externalSdkAd: ExternalSdkAd,
        activity: Activity?
    ) {

        timeoutHelper = AdsUtil.createTimeoutHelper(externalAdResponse, LOG_TAG)
        asyncAdImpressionReporter = AsyncAdImpressionReporter(externalSdkAd)

        val context: Context = CommonUtils.getApplication()
        val externalAdType = ExternalSdkAdType.fromAdType(externalSdkAd.external?.data)

        when (externalAdType) {
            ExternalSdkAdType.AMAZON_STANDARD -> {
                fetchBannerAd(externalAdResponse, externalSdkAd, context)
                return
            }
            ExternalSdkAdType.AMAZON_INTERSTITIAL -> {
                fetchInterstitialAd(externalAdResponse, externalSdkAd, activity?:context)
                return
            }
            else -> {
                AdLogger.d(LOG_TAG, "Unhandled adType $externalAdType")
                externalAdResponse.onResponse(null)
                return
            }
        }


    }

    fun fetchBannerAd(
        externalAdResponse: ExternalAdResponse,
        externalSdkAd: ExternalSdkAd,
        context: Context
    ) {

        val listener: DTBAdBannerListener = object : DTBAdBannerListener {
            override fun onAdLoaded(adView: View?) {
                AdLogger.d(LOG_TAG, "loaded view : $adView")
                timeoutHelper?.stopTimer()
                externalSdkAd.nativeAdObject = adView
                externalAdResponse.onResponse(externalSdkAd)
            }

            override fun onAdFailed(adView: View?) {
                AdLogger.d(LOG_TAG, "Failed to load amazon banner ad.")
                timeoutHelper?.stopTimer()
                externalAdResponse.onResponse(null)
            }

            override fun onAdClicked(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Banner clicked")
                /* As per Amazon's team, "Our onAdClicked callback is always triggered inside the webview's
                 OnTouchListener. Our approach is that if the difference between time of current touch motion
                 and time of last press motion is smaller than certain value, we will treat it as click motion.
                 Since different devices or OS versions may have different reaction time, it is possible that
                 it takes longer time for code to detect touch motion and in this case,
                 onAdClicked method will not be invoked."
                 Hence, using 'onAdLeftApplication' callback as it is guaranteed to be invoked for
                 a click action.
                 */
            }

            override fun onAdLeftApplication(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Banner left application")
                asyncAdImpressionReporter?.onClickEvent()
            }

            override fun onAdOpen(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Banner onAdOpen")
            }

            override fun onAdClosed(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Banner onAdClosed")
            }

            override fun onImpressionFired(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Banner onImpressionFired")
            }
        }

        val adView = DTBAdView(context, listener)
        AdLogger.d(LOG_TAG, "created view : $adView")
        val slotUnitId = externalSdkAd.external?.adUnitId
        timeoutHelper?.startTimer()
        try {
            slotUnitId?.let {
                if (AmazonBidUtilities.isExpiredBid(it)) {
                    AmazonBidUtilities.clearBidInfo(it)
                    AdLogger.d(LOG_TAG, "Bid is expired")
                    externalAdResponse.onResponse(null)
                    return
                }
                else {
                    AmazonBidUtilities.fetchBidToRender(it)?.let { htmlBid ->
                        AdLogger.d(LOG_TAG, "fetchAd for view : $adView")
                        adView.fetchAd(htmlBid)
                        AmazonBidUtilities.clearBidInfo(it)
                    }
                }
            }
        } catch (e: Exception) {
            timeoutHelper?.stopTimer()
            AdLogger.d(LOG_TAG, "Failed to fetch amazon banner ad.")
            Logger.caughtException(e)
            externalAdResponse.onResponse(null)
        }

    }

    fun fetchInterstitialAd(
        externalAdResponse: ExternalAdResponse,
        externalSdkAd: ExternalSdkAd,
        context: Context
    ) {

        var interstitialAdView: DTBAdInterstitial? = null
        val listener: DTBAdInterstitialListener = object : DTBAdInterstitialListener {
            override fun onAdLoaded(adView: View?) {
                AdLogger.d(LOG_TAG, "Interstitial loaded view : $interstitialAdView")
                timeoutHelper?.stopTimer()
                externalSdkAd.nativeAdObject = interstitialAdView
                externalAdResponse.onResponse(externalSdkAd)
            }

            override fun onAdFailed(adView: View?) {
                timeoutHelper?.stopTimer()
                AdLogger.d(LOG_TAG, "Failed to load amazon interstitial ad.")
                externalAdResponse.onResponse(null)
            }

            override fun onAdClicked(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Interstitial clicked")
                /* As per Amazon's team, "Our onAdClicked callback is always triggered inside the webview's
                 OnTouchListener. Our approach is that if the difference between time of current touch motion
                 and time of last press motion is smaller than certain value, we will treat it as click motion.
                 Since different devices or OS versions may have different reaction time, it is possible that
                 it takes longer time for code to detect touch motion and in this case,
                 onAdClicked method will not be invoked."
                 Hence, using 'onAdLeftApplication' callback as it is guaranteed to be invoked for
                 a click action.
                 */
            }

            override fun onAdLeftApplication(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Interstitial left application")
                asyncAdImpressionReporter?.onClickEvent()
                if (externalSdkAd.adPosition == AdPosition.EXIT_SPLASH) {
                    BusProvider.postOnUIBus(AdExitEvent(externalSdkAd, AdInteraction.USER_CLICK))
                }
            }

            override fun onAdOpen(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Interstitial onAdOpen")
            }

            override fun onAdClosed(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Interstitial onAdClosed")
                if (externalSdkAd.adPosition == AdPosition.EXIT_SPLASH) {
                    BusProvider.postOnUIBus(AdExitEvent(externalSdkAd, AdInteraction.USER_CLOSE))
                }
            }

            override fun onImpressionFired(adView: View?) {
                AdLogger.d(LOG_TAG, "Amazon Ad Interstitial onImpressionFired")
            }

        }
        interstitialAdView = DTBAdInterstitial(context, listener)
        val slotUnitId = externalSdkAd.external?.adUnitId
        timeoutHelper?.startTimer()
        try {
            slotUnitId?.let {
                if (AmazonBidUtilities.isExpiredBid(it)) {
                    AmazonBidUtilities.clearBidInfo(it)
                    AdLogger.d(LOG_TAG, "Bid is expired")
                    externalAdResponse.onResponse(null)
                    return
                }
                else {
                    AmazonBidUtilities.fetchBidToRender(it)?.let { htmlBid ->
                        AdLogger.d(LOG_TAG, "htmlBid : $htmlBid")
                        interstitialAdView.fetchAd(htmlBid)
                        AmazonBidUtilities.clearBidInfo(it)
                    }
                }
            }
        } catch (e: Exception) {
            timeoutHelper?.stopTimer()
            AdLogger.d(LOG_TAG, "Failed to fetch amazon banner ad.")
            Logger.caughtException(e)
            externalAdResponse.onResponse(null)
        }
    }

    companion object {
        private const val LOG_TAG = "AmazonAdRequester"
    }


}