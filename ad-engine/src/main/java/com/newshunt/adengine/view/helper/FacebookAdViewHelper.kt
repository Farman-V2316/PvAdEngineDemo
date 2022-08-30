/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.helper

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.facebook.ads.AdOptionsView
import com.facebook.ads.InterstitialAd
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdBase
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeBannerAd
import com.newshunt.adengine.R
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Represents data required to show Facebook ad.
 *
 * @author raunak.yadav
 */
class FacebookAdViewHelper : NativeViewHelper {
    private var activity: Activity? = null
    private var mediaView: MediaView? = null
    private var externalSdkAd: ExternalSdkAd? = null
    private val autoplayVisibility = AdsUtil.getMinimumAutoplayVisibilityForSdk(AdConstants.FB_AD.toLowerCase())

    constructor(externalSdkAd: ExternalSdkAd, activity: Activity?) {
        this.activity = activity
        this.externalSdkAd = externalSdkAd
    }

    constructor(activity: Activity?) {
        this.activity = activity
    }

    fun updateView(externalSdkAd: ExternalSdkAd) {
        this.externalSdkAd = externalSdkAd
        val externalSdkAdType = ExternalSdkAdType.fromAdType(
                externalSdkAd.external?.data)
        if (externalSdkAdType == ExternalSdkAdType.FB_INTERSTITIAL_AD) {
            handleInterstitialAd(externalSdkAd)
        }
    }

    private fun handleInterstitialAd(externalSdkAd: ExternalSdkAd) {
        (externalSdkAd.nativeAdObject as? InterstitialAd)?.show()
    }

    override fun getNativeAssets(): NativeData? {
        return (externalSdkAd?.nativeAdObject as? NativeAdBase)?.let {
            it.unregisterView()

            val nativeAssets = NativeData()
            nativeAssets.advertiser = it.advertiserName
            //As per FB policy, Adv name needs to be present in title.
            nativeAssets.title = "${it.advertiserName} : ${it.adHeadline}"
            nativeAssets.body = it.adBodyText
            nativeAssets.ctaText = it.adCallToAction

            val external = externalSdkAd?.external
            nativeAssets.shortInfo = external?.shortInfo
            nativeAssets.sponsoredText = if (it.adSocialContext.isNullOrBlank())
                AdsUtil.getExternalSdkAdItemTag(externalSdkAd) else it.adSocialContext
            nativeAssets.sourceAlphabet = externalSdkAd?.content?.sourceAlphabet
            return nativeAssets
        }
    }

    override fun addAdChoicesView(adContainer: ViewGroup): View? {
        if (externalSdkAd?.nativeAdObject !is NativeAdBase || adContainer !is NativeAdLayout) {
            return null
        }
        val adOptionsView = AdOptionsView(activity, externalSdkAd?.nativeAdObject as NativeAdBase, adContainer)
        adOptionsView.id = R.id.ad_choices_view
        adOptionsView.setIconColor(CommonUtils.getColor(R.color.sponsored_text_color))

        val adView = adContainer.findViewById<ViewGroup>(R.id.ad_container)
        adView.addView(adOptionsView)
        adView.setPadding(adView.paddingLeft, 0, adView.paddingRight, adView.paddingBottom)

        if (adView is ConstraintLayout) {
            // FB sometimes adds a view without id, causing crash.
            ViewUtils.setMissingViewIds(adView)
            val set = ConstraintSet()
            set.clone(adView)
            // views must have valid ids set in xml or code for constraint to work correctly.
            set.connect(adOptionsView.id, ConstraintSet.END, adView.getId(), ConstraintSet.END, CommonUtils.getDimension(R.dimen.ad_content_margin))
            set.connect(adOptionsView.id, ConstraintSet.TOP, adView.getId(), ConstraintSet.TOP, 0)
            set.connect(R.id.border_container, ConstraintSet.TOP, adOptionsView.id, ConstraintSet.BOTTOM,
                    0)
            set.applyTo(adView)
        }
        return adOptionsView
    }

    override fun getMediaViewIfApplicable(mediaViewLayout: RelativeLayout): View? {
        return activity?.let {
            mediaView = MediaView(it).apply {
                (parent as? ViewGroup)?.removeView(this)
                mediaViewLayout.addView(this)
                gravity = Gravity.CENTER
            }
            mediaView
        }
    }

    override fun getPreferredHeightMediaView(assets: NativeData): Int? {
        return externalSdkAd?.nativeAdObject?.let {

            val viewWidth = if(externalSdkAd?.adPosition == AdPosition.PGI)
                CommonUtils.getDeviceScreenWidth() else AdsUtil.defaultWidthForWideAds
            val aspectRatio = if (it is NativeAd) it.aspectRatio else null
            val aspectRatioMin = when {
                externalSdkAd?.adPosition == AdPosition.PGI -> {
                    AdConstants.ASPECT_RATIO_PGI_MIN
                }
                externalSdkAd?.isVideoAd == true -> AdsUtil.minAspectRatioVideo
                else -> AdsUtil.minAspectRatioNative
            }
            return if (aspectRatio != null && aspectRatio > 0f) {
                AdsUtil.getHeightWithAspectRatio(0, 0, aspectRatio, viewWidth,
                        aspectRatioMin)
            } else null
        }
    }

    override fun getAutoplayVisibility(): Int {
        return autoplayVisibility
    }

    override fun registerViewForInteraction(view: View, clickableViews: List<View>) {
        var iconView: ImageView? = null
        clickableViews.forEach {
            if (it.getTag(R.id.ad_click_tag_id) == AdConstants.AD_ASSET_ICON) {
                iconView = it as? ImageView
                return@forEach
            }
        }
        when (val nativeAd = externalSdkAd?.nativeAdObject) {
            is NativeAd ->
                nativeAd.registerViewForInteraction(view, mediaView, iconView, clickableViews)
            is NativeBannerAd ->
                nativeAd.registerViewForInteraction(view, iconView, clickableViews)
        }
    }

    override fun destroy(parentId: Int, view: View?) {
    }
}
