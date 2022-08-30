/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.newshunt.adengine.R
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Handles DFP native ads
 *
 * @author raunak.yadav
 */
class DfpNativeViewHelper(private var externalSdkAd: ExternalSdkAd? = null,
                          private var activity: Activity? = null) : NativeViewHelper {

    private val autoplayVisibility = AdsUtil.getMinimumAutoplayVisibilityForSdk(AdConstants.DFP_AD.toLowerCase())

    /**
     * Clearing click listeners causes ad clickability issues in dfp.
     */
    override fun shouldClearClickListeners(): Boolean {
        return false
    }

    override fun getNativeAssets(): NativeData? {

        var nativeAssets: NativeData? = null

        when (externalSdkAd?.nativeAdObject) {
            is NativeAd -> {
                nativeAssets = NativeData()
                val nativeAppInstallAd = externalSdkAd?.nativeAdObject as NativeAd

                nativeAssets.title = nativeAppInstallAd.headline?.toString()
                nativeAssets.body = nativeAppInstallAd.body?.toString()
                nativeAssets.ctaText = nativeAppInstallAd.callToAction?.toString()
                nativeAssets.iconUrl = nativeAppInstallAd.icon?.uri?.toString()
                nativeAssets.iconDrawable = nativeAppInstallAd.icon?.drawable
                nativeAssets.advertiser = nativeAppInstallAd.store?.toString()

                if (!CommonUtils.isEmpty(nativeAppInstallAd.images)) {
                    nativeAssets.wideImageUrl = nativeAppInstallAd.images[0]?.uri?.toString()
                    nativeAssets.wideImageDrawable = nativeAppInstallAd.images[0]?.drawable
                }
            }
            is NativeCustomFormatAd -> {
                nativeAssets = NativeData()
                val customAd = externalSdkAd?.nativeAdObject as NativeCustomFormatAd

                nativeAssets.title = customAd.getText(AdConstants.AD_ASSET_HEADLINE)?.toString()
                nativeAssets.body = customAd.getText(AdConstants.AD_ASSET_BODY)?.toString()
                nativeAssets.ctaText = customAd.getText(AdConstants.AD_ASSET_CALL_TO_ACTION)?.toString()
                nativeAssets.advertiser = customAd.getText(AdConstants.AD_ASSET_ADVERTISER)?.toString()
                nativeAssets.iconDrawable = customAd.getImage(AdConstants.AD_ASSET_ICON)?.drawable
                nativeAssets.iconUrl = customAd.getImage(AdConstants.AD_ASSET_ICON)?.uri?.toString()
                nativeAssets.wideImageDrawable = customAd.getImage(AdConstants.AD_ASSET_MAIN_IMAGE)?.drawable
                nativeAssets.wideImageUrl = customAd.getImage(AdConstants.AD_ASSET_MAIN_IMAGE)?.uri?.toString()
            }
        }
        nativeAssets?.let {
            val externalTag = externalSdkAd?.external
            it.shortInfo = externalTag?.shortInfo
            it.sponsoredText = AdsUtil.getExternalSdkAdItemTag(externalSdkAd)
            it.sourceAlphabet = externalSdkAd?.content?.sourceAlphabet
        }
        return nativeAssets
    }

    override fun addAdChoicesView(adContainer: ViewGroup): View? {
        return null
    }

    override fun getMediaViewIfApplicable(mediaViewLayout: RelativeLayout): View? {
        val sdkAd = externalSdkAd?.nativeAdObject ?: return null
        externalSdkAd?.external ?: return null

        return if (sdkAd is NativeCustomFormatAd) {
            if (sdkAd.videoController.hasVideoContent()) {
                sdkAd.videoMediaView
            } else null
        } else {
            activity?.layoutInflater?.inflate(R.layout.dfp_media_view,
                    mediaViewLayout, false) as? MediaView?
        }?.apply {
            if (externalSdkAd?.adPosition == AdPosition.PGI && externalSdkAd?.showOnlyImage == true) {
                this.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            }
            mediaViewLayout.addView(this)
        }
    }

    override fun getPreferredHeightMediaView(assets: NativeData): Int? {
        val aspectRatio = externalSdkAd?.nativeAdObject?.let {
            when (it) {
                is NativeAd -> it.mediaContent?.aspectRatio
//                is NativeCustomTemplateAd -> it.videoController.aspectRatio
 //               is NativeCustomFormatAd -> it.videoController.
                else -> 0f
            }
        } ?: 0f

        var viewWidth = AdsUtil.defaultWidthForWideAds
        var aspectRatioMin = AdConstants.ASPECT_RATIO_WIDE_ADS_DEFAULT
        if (externalSdkAd?.adPosition == AdPosition.PGI) {
            viewWidth = CommonUtils.getDeviceScreenWidth()
            aspectRatioMin = AdConstants.ASPECT_RATIO_PGI_MIN
        }
        return if (aspectRatio != 0f) {
            AdsUtil.getHeightWithAspectRatio(0, 0, aspectRatio, viewWidth,
                    aspectRatioMin)
        } else {
            //Prefer creative's size over ad response size
            val width = assets.wideImageDrawable?.intrinsicWidth
                    ?: externalSdkAd?.width ?: 0
            val height = assets.wideImageDrawable?.intrinsicHeight
                    ?: externalSdkAd?.height ?: 0
            AdsUtil.getHeightWithAspectRatio(width, height, aspectRatioMin, viewWidth, aspectRatioMin)
        }
    }

    override fun getAutoplayVisibility(): Int {
        return autoplayVisibility
    }

    override fun recordImpression() {
        // Need to notify sdk about viewability for custom native ads only.
        (externalSdkAd?.nativeAdObject as? NativeCustomFormatAd)?.recordImpression()
    }

    override fun registerViewForInteraction(view: View, clickableViews: List<View>) {
        if (CommonUtils.isEmpty(clickableViews)) {
            return
        }
        when {
            view is NativeAdView -> {
                clickableViews.forEach {
                    when (it.id) {
                        R.id.ad_title -> view.headlineView = it
                        R.id.ad_body -> view.bodyView = it
                        R.id.ad_icon -> view.iconView = it
                        R.id.ad_image -> view.imageView = it
                        R.id.media_view -> view.mediaView = it as MediaView
                        R.id.ad_attr -> view.advertiserView = it
                        R.id.cta_button, R.id.ad_banner_bottombar -> view.callToActionView = it
                    }
                }
                (externalSdkAd?.nativeAdObject as? NativeAd)?.let {
                    view.setNativeAd(it)
                }
            }
            externalSdkAd?.nativeAdObject is NativeCustomFormatAd -> {
                val customAd = externalSdkAd?.nativeAdObject as? NativeCustomFormatAd

                if (TextUtils.isEmpty(customAd?.getText(AdConstants.AD_ASSET_CLICK_URL))) {
                    return
                }
                val listener = View.OnClickListener { clickedView ->
                    customAd?.performClick(clickedView.getTag(R.id.ad_click_tag_id).toString())
                    AdsOpenUtility.handleBrowserSelection(activity,
                            customAd?.getText(AdConstants.AD_ASSET_CLICK_URL).toString(), externalSdkAd)
                }

                // register the views on which we want to track clicks.
                for (clickableView in clickableViews) {
                    if (clickableView.getTag(R.id.ad_click_tag_id) != null) {
                        clickableView.setOnClickListener(listener)
                    }
                }
            }
        }
    }

    override fun destroy(parentId: Int, view: View?) {
        activity = null
        externalSdkAd = null
    }
}