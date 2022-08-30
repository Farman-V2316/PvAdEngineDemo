/*
 * Copyright (c) 2021 NewsHunt. All rights reserved.
 */

package com.newshunt.adengine.view.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.amazon.device.ads.DTBAdInterstitial
import com.amazon.device.ads.DTBAdView
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.NHRoundedFrameLayout
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Created by helly.p on 20/12/21.
 */

private const val TAG = "AmazonAdViewHelper"

class AmazonAdViewHelper(view: View, private var activity: Activity) {

    private var adContainerLayout: ConstraintLayout? = null
    private var innerAdContainer: NHRoundedFrameLayout? = null
    private var sponsoredText: NHTextView? = null
    private var view: View? = view
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
            ExternalSdkAdType.AMAZON_STANDARD -> handleAmazonBanner(externalSdkAd)
            ExternalSdkAdType.AMAZON_INTERSTITIAL -> handleAmazonInterstitial(externalSdkAd)
            else -> {
            }
        }
    }

    private fun handleAmazonBanner(externalSdkAd: ExternalSdkAd) {
        val dtbAdView = externalSdkAd.nativeAdObject as? DTBAdView
        if (dtbAdView == null) {
            AdLogger.d(TAG, "NativeAdObject is null. Cannot updateView.")
            return
        }

        dtbAdView.id = R.id.amazon_ad_id

        if(dtbAdView.parent != null) {
            val parent = dtbAdView.parent as ViewGroup
            parent.removeView(dtbAdView)
        }

        adContainerLayout?.let { container ->
            container.setPadding(CommonUtils.getDimension(R.dimen.ad_content_margin), 0, CommonUtils.getDimension(R.dimen.ad_content_margin), 0)

            val width = externalSdkAd.width?: AdConstants.DEFAULT_AD_SIZE
            val height = externalSdkAd.height?: AdConstants.DEFAULT_AD_SIZE
            container.addView(dtbAdView, width, height)

            val set = ConstraintSet()
            set.clone(container)
            set.connect(dtbAdView.id, ConstraintSet.START, container.id,
                ConstraintSet.START, CommonUtils.getDimension(R.dimen.ad_content_margin))
            set.connect(dtbAdView.id, ConstraintSet.END, container.id,
                ConstraintSet.END, CommonUtils.getDimension(R.dimen.ad_content_margin))
            sponsoredText?.let { sponsoredText ->
                set.connect(sponsoredText.id, ConstraintSet.TOP, dtbAdView.id,
                    ConstraintSet.BOTTOM, CommonUtils.getDimension(R.dimen.sponsored_text_margin_top))
            }
            innerAdContainer?.let {innerContainer ->
                set.connect(innerContainer.id, ConstraintSet.BOTTOM, dtbAdView.id, ConstraintSet.BOTTOM)
            }
            set.applyTo(container)

            innerAdContainer?.let {
                AdsUtil.setUpAdContainerBackground(externalSdkAd, it)
                AdsUtil.setUpAdContainerBorder(externalSdkAd, it)
            }

            view?.visibility = View.VISIBLE
            container.setTag(R.id.omid_adview_tag_id, Constants.OM_WEBVIEW_TAG)
        }

    }

    private fun handleAmazonInterstitial(externalSdkAd: ExternalSdkAd) {
        val dtbAdView = externalSdkAd.nativeAdObject as? DTBAdInterstitial
        if (dtbAdView == null) {
            AdLogger.d(TAG, "NativeAdObject is null. Cannot updateView.")
            return
        }
        dtbAdView.show()
    }

}