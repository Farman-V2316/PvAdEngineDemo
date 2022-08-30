/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client.requester

import android.app.Activity
import android.content.Context
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdBase
import com.facebook.ads.NativeAdListener
import com.facebook.ads.NativeBannerAd
import com.newshunt.ContextHolder
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.ExternalAdResponse
import com.newshunt.adengine.model.entity.AdExitEvent
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsTimeoutHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Fetches ad from facebook depending on type of ad requested and adds it to ad cache on success.
 *
 * @author raunak.yadav
 */
class FacebookAdRequester : AdRequester {

    private var nativeAd: NativeAdBase? = null
    private var interstitialAd: InterstitialAd? = null
    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null
    private var externalAdType: ExternalSdkAdType? = null
    private var timeoutHelper: AdsTimeoutHelper? = null

    override fun fetchAd(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd,
                         activity: Activity?) {
        if (!AudienceNetworkAds.isInitialized(CommonUtils.getApplication())) {
            AdLogger.e(LOG_TAG, "FB sdk not initialized yet")
            externalAdResponse.onResponse(null)
            return
        }
        timeoutHelper = AdsUtil.createTimeoutHelper(externalAdResponse, LOG_TAG)
        asyncAdImpressionReporter = AsyncAdImpressionReporter(externalSdkAd)
        externalAdType = ExternalSdkAdType.fromAdType(externalSdkAd.external?.data)

        when (externalAdType) {
            ExternalSdkAdType.FB_NATIVE_AD,
            ExternalSdkAdType.FB_NATIVE_INTERSTITIAL -> requestFBNativeAd(externalAdResponse, externalSdkAd, false)

            ExternalSdkAdType.FB_INTERSTITIAL_AD -> requestFBInterstitialAd(externalAdResponse, externalSdkAd, activity)

            ExternalSdkAdType.FB_NATIVE_BID,
            ExternalSdkAdType.FB_NATIVE_INTERSTITIAL_BID -> requestFBNativeAd(externalAdResponse, externalSdkAd, true)
            else -> externalAdResponse.onResponse(null)
        }
    }

    override fun decodeAdMetaData(externalSdkAd: ExternalSdkAd) {
        val external = externalSdkAd.external ?: return
        if (!CommonUtils.isEmpty(external.extras)) {
            try {
                external.extras = URLDecoder.decode(external.extras, Constants.TEXT_ENCODING_UTF_8)
            } catch (e: UnsupportedEncodingException) {
                Logger.caughtException(e)
            }
        }
    }

    private fun requestFBNativeAd(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd,
                                  isBiddingAd: Boolean) {
        timeoutHelper?.startTimer()
        nativeAd = getNativeAd(externalSdkAd).apply {
            val builder = buildLoadAdConfig()
            builder.withAdListener(getNativeAdListener(externalSdkAd, externalAdResponse))
            if (isBiddingAd && !CommonUtils.isEmpty(externalSdkAd.external?.extras)) {
                builder.withBid(externalSdkAd.external?.extras)
            }
            loadAd(builder.build())
        }
    }

    private fun getNativeAd(externalSdkAd: ExternalSdkAd): NativeAdBase {
        return if (externalSdkAd.adTemplate == AdTemplate.HIGH || externalSdkAd.adPosition == AdPosition.PGI) {
            NativeAd(CommonUtils.getApplication(), externalSdkAd.external?.adUnitId)
        } else NativeBannerAd(CommonUtils.getApplication(), externalSdkAd.external?.adUnitId)
    }

    private fun requestFBInterstitialAd(externalAdResponse: ExternalAdResponse,
                                        externalSdkAd: ExternalSdkAd,
                                        context: Context?) {
        timeoutHelper?.startTimer()
        ContextHolder.contextHolder.context = context ?: CommonUtils.getApplication()
        interstitialAd = InterstitialAd(ContextHolder.contextHolder, externalSdkAd.external?.adUnitId)
                .apply {
                    loadAd(buildLoadAdConfig()
                            .withAdListener(getInterstitialAdListener(externalSdkAd, externalAdResponse))
                            .build())
                }
    }

    private fun getNativeAdListener(externalSdkAd: ExternalSdkAd, externalAdResponse: ExternalAdResponse)
            : NativeAdListener {
        return object : NativeAdListener {
            override fun onError(ad: Ad?, adError: AdError?) {
                AdLogger.d(LOG_TAG, "failed to load fb ad  : ${adError?.errorMessage} adType: " +
                        "$externalAdType errorcode: ${adError?.errorCode}")
                timeoutHelper?.stopTimer()
                externalSdkAd.nativeAdObject = null
                externalAdResponse.onResponse(null)
            }

            override fun onAdLoaded(ad: Ad?) {
                AdLogger.d(LOG_TAG, "fb ad loaded, adType: $externalAdType")
                timeoutHelper?.stopTimer()
                if (ad != nativeAd) {
                    externalAdResponse.onResponse(null)
                    return
                }
                externalSdkAd.nativeAdObject = nativeAd
                if (nativeAd is NativeAd) {
                    externalSdkAd.isVideoAd = (nativeAd as NativeAd).adCreativeType == NativeAd.AdCreativeType.VIDEO
                }
                externalAdResponse.onResponse(externalSdkAd)
            }

            override fun onAdClicked(ad: Ad?) {
                AdLogger.d(LOG_TAG, "Facebook Native Ad Clicked adType: $externalAdType")
                asyncAdImpressionReporter?.onClickEvent()
                if (externalSdkAd.adPosition == AdPosition.EXIT_SPLASH) {
                    BusProvider.postOnUIBus(AdExitEvent(externalSdkAd, AdInteraction.USER_CLICK))
                }
            }

            override fun onLoggingImpression(ad: Ad?) {
                AdLogger.d(LOG_TAG, "FB sdk onLoggingImpression")
            }

            override fun onMediaDownloaded(ad: Ad?) {
                AdLogger.v(LOG_TAG, "FB onMediaDownloaded")
            }
        }

    }

    private fun getInterstitialAdListener(externalSdkAd: ExternalSdkAd,
                                          externalAdResponse: ExternalAdResponse): InterstitialAdListener {
        return object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad?) {
                AdLogger.d(LOG_TAG, "fb interstitial ad viewed")
            }

            override fun onInterstitialDismissed(ad: Ad?) {
                AdLogger.d(LOG_TAG, "fb interstitial ad dismissed")
                BusProvider.postOnUIBus(AdExitEvent(externalSdkAd, AdInteraction.USER_CLOSE))
            }

            override fun onError(ad: Ad?, adError: AdError?) {
                AdLogger.d(LOG_TAG, "failed to load fb interstitial ad with error: ${adError?.errorMessage}")
                timeoutHelper?.stopTimer()
                externalSdkAd.nativeAdObject = null
                externalAdResponse.onResponse(null)
            }

            override fun onAdLoaded(ad: Ad?) {
                AdLogger.d(LOG_TAG, "fb interstitial ad loaded")
                timeoutHelper?.stopTimer()
                if (ad != interstitialAd) {
                    return
                }
                externalSdkAd.nativeAdObject = interstitialAd
                externalAdResponse.onResponse(externalSdkAd)
            }

            override fun onAdClicked(ad: Ad?) {
                AdLogger.v(LOG_TAG, "Facebook Interstitial Ad Clicked ")
                asyncAdImpressionReporter?.onClickEvent()
                BusProvider.postOnUIBus(AdExitEvent(externalSdkAd, AdInteraction.USER_CLICK))
            }

            override fun onLoggingImpression(ad: Ad?) {

            }
        }
    }

    companion object {
        private const val LOG_TAG = "FacebookAdRequester"

        init {
            AudienceNetworkAds.initialize(CommonUtils.getApplication())
        }
    }
}
