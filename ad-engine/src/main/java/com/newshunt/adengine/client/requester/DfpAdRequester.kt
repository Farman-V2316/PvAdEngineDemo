package com.newshunt.adengine.client.requester

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd.OnCustomClickListener
import com.google.android.gms.ads.nativead.NativeCustomFormatAd.OnCustomFormatAdLoadedListener
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.ExternalAdResponse
import com.newshunt.adengine.model.entity.AdExitEvent
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsTimeoutHelper
import com.newshunt.adengine.util.AdsUtil.Companion.createTimeoutHelper
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.util.StringTokenizer

/**
 * Fetches ad from dfp depending on type of ad requested i.e app download or content ad
 * and adds it to ad queue on success.
 *
 * @author raunak.yadav
 */
class DfpAdRequester : AdRequester {
    private var publisherAdRequest: AdManagerAdRequest? = null
    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null
    private var timeoutHelper: AdsTimeoutHelper? = null

    override fun fetchAd(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd,
                         activity: Activity?) {
        timeoutHelper = createTimeoutHelper(externalAdResponse, LOG_TAG)
        publisherAdRequest = createDfpAdRequest(externalSdkAd)
        asyncAdImpressionReporter = AsyncAdImpressionReporter(externalSdkAd)

        val context: Context = CommonUtils.getApplication()
        val nativeAdOptionsBuilder = NativeAdOptions.Builder()

        val videoOptionsBuilder = VideoOptions.Builder()
        videoOptionsBuilder.setStartMuted(true)
        nativeAdOptionsBuilder.setVideoOptions(videoOptionsBuilder.build())

        externalSdkAd.external?.preferredAspectRatio?.let {
            nativeAdOptionsBuilder.setMediaAspectRatio(if (it.isNotEmpty()) {
                it[0].value
            } else ExternalSdkAd.CreativeOrientation.ANY.value)
        }

        val adOptions = nativeAdOptionsBuilder.build()

        val externalAdType = ExternalSdkAdType.fromAdType(externalSdkAd.external?.data)
        var isUnifiedNative = false
        var isNativeCustom = false
        when (externalAdType) {
            ExternalSdkAdType.DFP_STANDARD -> {
                fetchDfpBannerAd(externalAdResponse, externalSdkAd, context)
                return
            }
            ExternalSdkAdType.DFP_INTERSTITIAL -> {
                fetchDfpInterstitialAd(externalAdResponse, externalSdkAd, context)
                return
            }
            ExternalSdkAdType.DFP_NATIVE -> {
                // Native + Standard ads
                fetchDfpNativeAd(createComboAdLoader(externalAdResponse, externalSdkAd, context, adOptions),
                        externalAdResponse, externalAdType)
                return
            }
            ExternalSdkAdType.DFP_NATIVE_APP_DOWNLOAD,
            ExternalSdkAdType.DFP_NATIVE_CONTENT -> isUnifiedNative = true
            ExternalSdkAdType.DFP_CUSTOM_NATIVE -> {
                if (CommonUtils.isEmpty(externalSdkAd.external?.uiTemplate)) {
                    AdLogger.d(LOG_TAG, "template id missing in dfp custom ad.")
                    externalAdResponse.onResponse(null)
                    return
                }
                isNativeCustom = true
            }
            ExternalSdkAdType.DFP_NATIVE_INTERSTITIAL -> {
                isUnifiedNative = true
                isNativeCustom = true
            }
            else -> {
                AdLogger.d(LOG_TAG, "Unhandled adType $externalAdType")
                externalAdResponse.onResponse(null)
                return
            }
        }
        val adLoader = getAdLoaderBuilder(context, externalAdResponse, externalSdkAd,
                isUnifiedNative, isNativeCustom, adOptions, false).build()
        fetchDfpNativeAd(adLoader, externalAdResponse, externalAdType)
    }

    private fun fetchDfpBannerAd(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd,
                                 context: Context, isInlineAdaptive: Boolean = false) {
        val publisherAdView = AdManagerAdView(context)
        publisherAdView.adListener = getDfpBannerAdListener(publisherAdView, externalSdkAd, externalAdResponse)
        publisherAdView.adUnitId = externalSdkAd.external?.adUnitId
        /*if (isInlineAdaptive) {
            publisherAdView.setAdSizes(AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context,
                    CommonUtils.getDpFromPixels(DeviceInfoHelper.getDeviceInfo().width.toInt(), context)
                            - 2 * CommonUtils.getDimensionInDp(R.dimen.ad_content_margin)))
        } else {*/
        publisherAdView.setAdSizes(*getAdSizes(externalSdkAd.external?.adSizes))
        //}
        publisherAdView.setManualImpressionsEnabled(externalSdkAd.external?.manualImpression ?: false)
        timeoutHelper?.startTimer()
        try {
            publisherAdView.loadAd(publisherAdRequest)
        } catch (e: Exception) {
            timeoutHelper?.stopTimer()
            AdLogger.d(LOG_TAG, " Failed to load dfp banner ad.")
            Logger.caughtException(e)
            externalAdResponse.onResponse(null)
        }
    }

    private fun fetchDfpNativeAd(adLoader: AdLoader, externalAdResponse: ExternalAdResponse,
                                 externalAdType: ExternalSdkAdType) {
        timeoutHelper?.startTimer()
        try {
            adLoader.loadAd(publisherAdRequest)
        } catch (e: Exception) {
            timeoutHelper?.stopTimer()
            Logger.caughtException(e)
            AdLogger.d(LOG_TAG, "Failed to load " + externalAdType.adType + " ad.")
            externalAdResponse.onResponse(null)
        }
    }

    private fun fetchDfpInterstitialAd(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd,
                                       context: Context) {

        val fullScreenContentCallback: FullScreenContentCallback = getInterstitialAdListener(externalSdkAd)

        timeoutHelper?.startTimer()
        try {
            AdManagerInterstitialAd.load(context,externalSdkAd.external?.adUnitId, publisherAdRequest,
                    object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    timeoutHelper?.stopTimer()
                    AdLogger.d(LOG_TAG, "Failed to load dfp interstitial ad with error code: $adError")
                    externalAdResponse.onResponse(null)
                }

                override fun onAdLoaded(ad: AdManagerInterstitialAd) {
                    timeoutHelper?.stopTimer()
                    AdLogger.d(LOG_TAG, "Dfp Interstitial ad loaded")
                    externalSdkAd.nativeAdObject = ad
                    externalAdResponse.onResponse(externalSdkAd)
                    ad.fullScreenContentCallback = fullScreenContentCallback
                }
            })
        } catch (ex: Exception) {
            timeoutHelper?.stopTimer()
            AdLogger.d(LOG_TAG, "Failed to load dfp interstitial ad.")
            Logger.caughtException(ex)
            externalAdResponse.onResponse(null)
        }
    }

    private fun getInterstitialAdListener(ad: ExternalSdkAd): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdClicked() {
                AdLogger.d(LOG_TAG, "DFP Ad Interstitial clicked")
                asyncAdImpressionReporter?.onClickEvent()
                if (ad.adPosition == AdPosition.EXIT_SPLASH) {
                    BusProvider.postOnUIBus(AdExitEvent(ad, AdInteraction.USER_CLICK))
                }
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                AdLogger.d(LOG_TAG, "Ad dismissed fullscreen content.")
                if (ad.adPosition == AdPosition.EXIT_SPLASH) {
                    BusProvider.postOnUIBus(AdExitEvent(ad, AdInteraction.USER_CLOSE))
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                AdLogger.e(LOG_TAG, "Ad failed to show fullscreen content.")
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                AdLogger.d(LOG_TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                AdLogger.d(LOG_TAG, "Ad showed fullscreen content.")
            }
        }
    }

    private fun getAdSizes(sizes: List<String>?): Array<AdSize?> {
        val adSizes = ArrayList<AdSize>()
        sizes?.forEach { size ->
            when (size) {
                "SMART_BANNER" -> adSizes.add(AdSize.SMART_BANNER)
                "FLUID" -> adSizes.add(AdSize.FLUID)
                else -> {
                    val index = size.indexOf("x")
                    if (index != -1) {
                        try {
                            val width = DataUtil.parseInt(size.substring(0, index), 0)
                            val height = DataUtil.parseInt(size.substring(index + 1), 0)
                            if (width != 0 && height != 0) {
                                adSizes.add(AdSize(width, height))
                            }
                        } catch (e: Exception) {
                            Logger.caughtException(e)
                        }
                    }
                }
            }
        }
        if (adSizes.isEmpty()) {
            adSizes.add(AdSize.BANNER)
        }
        return adSizes.toTypedArray()
    }

    private fun createComboAdLoader(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd,
                                    context: Context, adOptions: NativeAdOptions): AdLoader {
        val adSizes = getAdSizes(externalSdkAd.external?.adSizes)
        val builder = getAdLoaderBuilder(context, externalAdResponse, externalSdkAd,
                isUnifiedNative = true, isNativeCustom = true, adOptions = adOptions, updateAdType = true)
        return builder.forAdManagerAdView(OnAdManagerAdViewLoadedListener { publisherAdView: AdManagerAdView? ->
            timeoutHelper?.stopTimer()
            if (publisherAdView == null) {
                AdLogger.d(LOG_TAG, "publisherAdView ad is null")
                externalAdResponse.onResponse(null)
                return@OnAdManagerAdViewLoadedListener
            }
            AdLogger.d(LOG_TAG, "Dfp Standard ad loaded")
            externalSdkAd.external?.data = ExternalSdkAdType.DFP_STANDARD.adType
            externalSdkAd.nativeAdObject = publisherAdView
            externalAdResponse.onResponse(externalSdkAd)
        }, *adSizes).build()
    }

    private fun createDfpAdRequest(externalSdkAd: ExternalSdkAd): AdManagerAdRequest {
        if (externalSdkAd.external?.extras == null) {
            return AdManagerAdRequest.Builder()
                    .build()
        }
        val bundle = Bundle()
        try {
            val stringTokenizer = StringTokenizer(externalSdkAd.external?.extras, ":,")
            while (stringTokenizer.hasMoreTokens()) {
                val key = stringTokenizer.nextToken()
                val value = stringTokenizer.nextToken()
                bundle.putString(key, value)
            }
        } catch (e: NoSuchElementException) {
            Logger.caughtException(e)
        }
        return AdManagerAdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
                .build() as AdManagerAdRequest
    }

    private fun getDfpBannerAdListener(publisherAdView: AdManagerAdView, externalSdkAd: ExternalSdkAd,
                                       externalAdResponse: ExternalAdResponse): AdListener {
        return object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                timeoutHelper?.stopTimer()
                AdLogger.d(LOG_TAG, "Failed to load dfp banner ad. Error code: $errorCode")
                externalAdResponse.onResponse(null)
            }

            override fun onAdLoaded() {
                timeoutHelper?.stopTimer()
                AdLogger.d(LOG_TAG, "Dfp banner ad loaded")
                externalSdkAd.nativeAdObject = publisherAdView
                externalAdResponse.onResponse(externalSdkAd)
            }
        }
    }

    private fun getNativeAdListener(externalAdResponse: ExternalAdResponse, externalSdkAd: ExternalSdkAd?): AdListener {
        return object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                timeoutHelper?.stopTimer()
                AdLogger.d(LOG_TAG, "Failed to load dfp native ad. ErrorCode: $errorCode")
                externalAdResponse.onResponse(null)
            }

            override fun onAdClicked() {
                AdLogger.d(LOG_TAG, "DFP Native Ad Clicked")
                asyncAdImpressionReporter?.onClickEvent()
                externalSdkAd?.let {
                    if (it.adPosition == AdPosition.EXIT_SPLASH) {
                        BusProvider.postOnUIBus(AdExitEvent(it, AdInteraction.USER_CLICK))
                    }
                }
            }
        }
    }

    private inner class NativeAdLoadListener internal constructor(private val externalAdResponse: ExternalAdResponse, private val externalSdkAd: ExternalSdkAd,
                                                                  private val updateAdType: Boolean)
        : NativeAd.OnNativeAdLoadedListener, OnCustomFormatAdLoadedListener {

        override fun onNativeAdLoaded(ad: NativeAd) {
            timeoutHelper?.stopTimer()
            AdLogger.d(LOG_TAG, "Dfp Unified Native ad loaded")
            if (updateAdType) {
                externalSdkAd.external?.data = ExternalSdkAdType.DFP_NATIVE_CONTENT.adType
            }
            if (externalSdkAd.adTemplate == AdTemplate.HIGH &&
                    (CommonUtils.isEmpty(ad.images) || ad.images[0] == null)) {
                externalSdkAd.adTemplate = AdTemplate.LOW
            }
            externalSdkAd.isVideoAd = ad.mediaContent?.videoController?.hasVideoContent() ?: false
            if (externalSdkAd.isVideoAd) {
                ad.mediaContent?.videoController?.videoLifecycleCallbacks = asyncAdImpressionReporter?.let {
                    NativeVideoCallbackListener(externalSdkAd, it) }
            }
            externalSdkAd.nativeAdObject = ad
            externalAdResponse.onResponse(externalSdkAd)
        }

        override fun onCustomFormatAdLoaded(customAd: NativeCustomFormatAd) {
            timeoutHelper?.stopTimer()
            AdLogger.d(LOG_TAG, "Dfp custom native ad loaded")
            if (updateAdType) {
                externalSdkAd.external?.data = ExternalSdkAdType.DFP_CUSTOM_NATIVE.adType
            }
            if (externalSdkAd.adTemplate == AdTemplate.HIGH &&
                    customAd.getImage(AdConstants.AD_ASSET_MAIN_IMAGE) == null) {
                externalSdkAd.adTemplate = AdTemplate.LOW
            }
            externalSdkAd.isVideoAd = customAd.videoController.hasVideoContent()
            externalSdkAd.nativeAdObject = customAd
            externalAdResponse.onResponse(externalSdkAd)
        }

    }

    private class NativeVideoCallbackListener(private val externalSdkAd: ExternalSdkAd,
                                              private val asyncAdImpressionReporter: AsyncAdImpressionReporter)
        : VideoController.VideoLifecycleCallbacks() {

        override fun onVideoStart() {
            super.onVideoStart()
            AdLogger.d(LOG_TAG, "Dfp native ad [${externalSdkAd.uniqueAdIdentifier}] : VIDEO STARTED")
            asyncAdImpressionReporter.hitTrackerUrl(externalSdkAd.customVideoTrackers?.adVideoStart)
        }

        override fun onVideoEnd() {
            super.onVideoEnd()
            AdLogger.d(LOG_TAG, "Dfp native ad [${externalSdkAd.uniqueAdIdentifier}] : VIDEO ENDED")
            asyncAdImpressionReporter.hitTrackerUrl(externalSdkAd.customVideoTrackers?.adVideoEnd)
        }

        override fun onVideoPause() {
            super.onVideoPause()
            AdLogger.d(LOG_TAG, "Dfp native ad [${externalSdkAd.uniqueAdIdentifier}] : VIDEO PAUSED")
            asyncAdImpressionReporter.hitTrackerUrl(externalSdkAd.customVideoTrackers?.adVideoPause)
        }

        override fun onVideoPlay() {
            super.onVideoPlay()
            AdLogger.d(LOG_TAG, "Dfp native ad [${externalSdkAd.uniqueAdIdentifier}] : VIDEO PLAYED")
            asyncAdImpressionReporter.hitTrackerUrl(externalSdkAd.customVideoTrackers?.adVideoPlay)
        }

        override fun onVideoMute(isMuted: Boolean) {
            super.onVideoMute(isMuted)
            AdLogger.d(LOG_TAG, "Dfp native ad [${externalSdkAd.uniqueAdIdentifier}] : VIDEO MUTED : $isMuted")
            asyncAdImpressionReporter.hitTrackerUrl(externalSdkAd.customVideoTrackers?.let { if (isMuted) it.adVideoMute else it.adVideoUnMute })
        }
    }

    private fun getAdLoaderBuilder(context: Context, externalAdResponse: ExternalAdResponse,
                                   externalSdkAd: ExternalSdkAd, isUnifiedNative: Boolean,
                                   isNativeCustom: Boolean, adOptions: NativeAdOptions?,
                                   updateAdType: Boolean): AdLoader.Builder {
        val builder = AdLoader.Builder(context, externalSdkAd.external?.adUnitId)
        val adLoadListener = NativeAdLoadListener(externalAdResponse, externalSdkAd, updateAdType)
        if (isUnifiedNative) {
            builder.forNativeAd(adLoadListener)
        }
        if (isNativeCustom && !CommonUtils.isEmpty(externalSdkAd.external?.uiTemplate)) {
            builder.forCustomFormatAd(externalSdkAd.external?.uiTemplate, adLoadListener,
                    OnCustomClickListener { _: NativeCustomFormatAd?, s: String? ->
                        AdLogger.d(LOG_TAG, "DFP Custom Native ad clicked $s")
                        asyncAdImpressionReporter?.onClickEvent()
                    })
        }
        adOptions?.let {
            builder.withNativeAdOptions(it)
        }
        builder.withAdListener(getNativeAdListener(externalAdResponse, externalSdkAd)).build()
        return builder
    }

    companion object {
        private const val LOG_TAG = "DfpAdRequester"
    }
}