/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.BannerFill
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.customview.NHRoundedFrameLayout
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Represents data required to show dfp banner/interstitial ad.
 *
 * @author raunak.yadav
 */
class DfpViewHelper(view: View, private var activity: Activity) {
    private var adContainerLayout: ConstraintLayout? = null
    private var innerAdContainer: NHRoundedFrameLayout? = null
    private var view: View? = view
    private var sponsoredText: NHTextView? = null
    private var externalSdkAd: ExternalSdkAd? = null
    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null

    init {
        adContainerLayout = view.findViewById(R.id.external_ad_container)
        innerAdContainer = view.findViewById(R.id.external_ad_layout)
        sponsoredText = view.findViewById(R.id.sponsored_text)
    }

    fun updateView(externalSdkAd: ExternalSdkAd) {
        this.externalSdkAd = externalSdkAd
        asyncAdImpressionReporter = AsyncAdImpressionReporter(externalSdkAd)

        when (ExternalSdkAdType.fromAdType(externalSdkAd.external?.data)) {
            ExternalSdkAdType.DFP_STANDARD -> handlerDfpBanner(externalSdkAd)
            ExternalSdkAdType.DFP_INTERSTITIAL -> handleDfpInterstitial(externalSdkAd)
            else -> {
            }
        }
    }

    private fun handlerDfpBanner(externalSdkAd: ExternalSdkAd) {
        val publisherAdView = externalSdkAd.nativeAdObject as? AdManagerAdView
        if (publisherAdView == null) {
            AdLogger.d(TAG, "NativeAdObject is null. Cannot updateView.")
            return
        }
        publisherAdView.adListener = getDfpBannerAdListener(publisherAdView, externalSdkAd)
        publisherAdView.id = R.id.publisher_ad_id
        if (publisherAdView.parent != null) {
            val parent = publisherAdView.parent as ViewGroup
            parent.removeView(publisherAdView)
        }

        var canAddPadding = true
        val adSize = publisherAdView.adSize
        adSize?.let {
            val adsWidth = adSize.width
            val availableWidth = CommonUtils.getDeviceScreenWidthInDp() - 2 * CommonUtils.getDimensionInDp(
                com.dailyhunt.tv.ima.R.dimen
                    .ad_content_margin)
            canAddPadding = !adSize.isFullWidth && !adSize.isFluid && availableWidth >= adsWidth
        }

        adContainerLayout?.let {
            if (canAddPadding && externalSdkAd.bannerFill == BannerFill.CENTER) {
                it.setPadding(CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.ad_content_margin), 0,
                        CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.ad_content_margin), 0)
            } else {
                it.setPadding(0, 0, 0, 0)
            }

            it.addView(publisherAdView)
            ViewUtils.setMissingViewIds(it)

            val set = ConstraintSet()
            set.clone(it)
            set.connect(publisherAdView.id, ConstraintSet.LEFT, adContainerLayout!!.id,
                    ConstraintSet.LEFT, 0)
            set.connect(publisherAdView.id, ConstraintSet.RIGHT, adContainerLayout!!.id,
                    ConstraintSet.RIGHT, 0)
            sponsoredText?.let { sponsoredText ->
                set.connect(sponsoredText.id, ConstraintSet.TOP, publisherAdView.id,
                    ConstraintSet.BOTTOM, CommonUtils.getDimension(R.dimen.sponsored_text_margin_top))
            }
            set.applyTo(it)
            innerAdContainer?.let {
                AdsUtil.setUpAdContainerBackground(externalSdkAd, it)
                AdsUtil.setUpAdContainerBorder(externalSdkAd, it)
            }

            view?.visibility = View.VISIBLE
            it.setTag(R.id.omid_adview_tag_id, Constants.OM_WEBVIEW_TAG)
        }
    }

    private fun handleDfpInterstitial(externalSdkAd: ExternalSdkAd) {
        val publisherInterstitialAd = externalSdkAd.nativeAdObject as AdManagerInterstitialAd
        publisherInterstitialAd.show(activity)

    }

    private fun getDfpBannerAdListener(publisherAdView: AdManagerAdView, externalSdkAd: ExternalSdkAd): AdListener {
        return object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                AdLogger.d(TAG, "Failed to load dfp banner ad. Error code: $errorCode")
            }

            override fun onAdClicked() {
                AdLogger.d(TAG, "DFP Ad Banner clicked")
                asyncAdImpressionReporter?.onClickEvent()
            }

            override fun onAdLoaded() {
                AdLogger.d(TAG, "Dfp banner ad loaded")
                externalSdkAd.nativeAdObject = publisherAdView
            }
        }
    }
}

private const val TAG = "DfpViewHelper"
