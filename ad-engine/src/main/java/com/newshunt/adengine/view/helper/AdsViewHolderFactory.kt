/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.databinding.AdFbNativeHighBinding
import com.newshunt.adengine.databinding.AdFbNativeLowBinding
import com.newshunt.adengine.databinding.AdsNativeHighLayoutBinding
import com.newshunt.adengine.databinding.AdsNativeLowLayoutBinding
import com.newshunt.adengine.databinding.AdsPagerLayoutBinding
import com.newshunt.adengine.databinding.DfpNativeAdBinding
import com.newshunt.adengine.databinding.DfpNativeAdHighLayoutBinding
import com.newshunt.adengine.databinding.EmptyAdLayoutBinding
import com.newshunt.adengine.databinding.ExternalAdContainerBinding
import com.newshunt.adengine.databinding.LayoutHtmlFullPageAdBinding
import com.newshunt.adengine.databinding.LayoutImaVideoAdsBinding
import com.newshunt.adengine.databinding.LayoutImageLinkFullAdBinding
import com.newshunt.adengine.databinding.NewsItemTypeHtmlAdBinding
import com.newshunt.adengine.databinding.NewsItemTypeImageLinkAdBinding
import com.newshunt.adengine.listeners.AdExitListener
import com.newshunt.adengine.listeners.InteractiveAdListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.viewholder.EmptyAdsViewHolder
import com.newshunt.adengine.view.viewholder.ExternalSdkViewHolder
import com.newshunt.adengine.view.viewholder.IMAVideoAdViewHolder
import com.newshunt.adengine.view.viewholder.FullPageImageAdViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdImageLinkViewHolder
import com.newshunt.adengine.view.viewholder.NativeHighTemplateViewHolder
import com.newshunt.adengine.view.viewholder.NativeViewHolder
import com.newshunt.adengine.view.viewholder.SwipableAdsHolder
import com.newshunt.dataentity.common.asset.AdDisplayType

/**
 * Provides viewBinding/ViewHolder if cardType is in {@see AdDisplayType}
 *
 * @author raunak.yadav
 */
class AdsViewHolderFactory {

    companion object {
        fun getViewBinding(cardType: Int, layoutInflater: LayoutInflater, parent: ViewGroup?):
                ViewDataBinding? {
            return when (cardType) {

                AdDisplayType.EXTERNAL_SDK.index -> {
                    DataBindingUtil.inflate<ExternalAdContainerBinding>(layoutInflater,
                            R.layout.external_ad_container, parent, false)
                }
                AdDisplayType.NATIVE_AD.index -> {
                    DataBindingUtil.inflate<AdsNativeLowLayoutBinding>(layoutInflater,
                            R.layout.ads_native_low_layout, parent, false)
                }
                AdDisplayType.NATIVE_ENHANCED_HIGH_AD.index,
                AdDisplayType.NATIVE_HIGH_AD.index -> {
                    DataBindingUtil.inflate<AdsNativeHighLayoutBinding>(layoutInflater,
                            R.layout.ads_native_high_layout, parent, false)
                }
                AdDisplayType.NATIVE_DFP_AD.index -> {
                    DataBindingUtil.inflate<DfpNativeAdBinding>(layoutInflater,
                            R.layout.dfp_native_ad, parent, false)
                }
                AdDisplayType.NATIVE_DFP_HIGH_AD.index -> {
                    DataBindingUtil.inflate<DfpNativeAdHighLayoutBinding>(layoutInflater,
                            R.layout.dfp_native_ad_high_layout, parent, false)
                }
                AdDisplayType.AD_FB_NATIVE.index -> {
                    DataBindingUtil.inflate<AdFbNativeLowBinding>(layoutInflater,
                            R.layout.ad_fb_native_low, parent, false)
                }
                AdDisplayType.AD_FB_NATIVE_HIGH.index -> {
                    DataBindingUtil.inflate<AdFbNativeHighBinding>(layoutInflater,
                            R.layout.ad_fb_native_high, parent, false)
                }
                AdDisplayType.HTML_AD.index -> {
                    DataBindingUtil.inflate<NewsItemTypeHtmlAdBinding>(layoutInflater, R.layout
                            .news_item_type_html_ad, parent, false)
                }
                AdDisplayType.HTML_AD_FULL.index -> {
                    DataBindingUtil.inflate<LayoutHtmlFullPageAdBinding>(layoutInflater, R.layout
                            .layout_html_full_page_ad, parent, false)
                }
                AdDisplayType.IMAGE_LINK.index -> {
                    DataBindingUtil.inflate<NewsItemTypeImageLinkAdBinding>(layoutInflater, R.layout
                            .news_item_type_image_link_ad, parent, false)
                }
                AdDisplayType.IMAGE_LINK_FULL.index -> {
                    DataBindingUtil.inflate<LayoutImageLinkFullAdBinding>(layoutInflater, R.layout
                        .layout_image_link_full_ad, parent, false)
                }
                AdDisplayType.APP_DOWNLOAD.index -> {
                    DataBindingUtil.inflate<AdsPagerLayoutBinding>(layoutInflater, R.layout
                            .ads_pager_layout, parent, false)
                }
                AdDisplayType.IMA_VIDEO_AD.index -> {
                    DataBindingUtil.inflate<LayoutImaVideoAdsBinding>(layoutInflater, R.layout
                            .layout_ima_video_ads, parent, false)

                }
                AdDisplayType.EMPTY_AD.index -> {
                    DataBindingUtil.inflate<EmptyAdLayoutBinding>(layoutInflater, R.layout
                            .empty_ad_layout, parent,false)
                }
                else -> null
            }
        }

        fun getViewHolder(cardType: Int, viewDataBinding: ViewDataBinding, uniqueRequestId: Int,
                          parent: ViewGroup?, contentUrl: String? = null,
                          parentLifecycleOwner: LifecycleOwner? = null,
                          adEntityReplaceHandler: AdEntityReplaceHandler? = null,
                          interactiveAdListener: InteractiveAdListener? = null,
                          webViewProvider: NativeAdHtmlViewHolder.CachedWebViewProvider? = null,
                          swipeableHTMLAdInteractionListener: NativeAdHtmlViewHolder
                          .SwipeableHTMLAdInteractionListener? = null,
                          reportAdsMenuListener: ReportAdsMenuListener? = null,
                          adExitListener: AdExitListener? = null)
                : RecyclerView.ViewHolder? {
            viewDataBinding.setVariable(BR.adReportListener, reportAdsMenuListener)
            viewDataBinding.setVariable(BR.adExitListener, adExitListener)

            return when (cardType) {
                AdDisplayType.EXTERNAL_SDK.index -> ExternalSdkViewHolder(viewDataBinding,
                    uniqueRequestId, parentLifecycleOwner)

                AdDisplayType.NATIVE_AD.index,
                AdDisplayType.NATIVE_DFP_AD.index,
                AdDisplayType.AD_FB_NATIVE.index ->
                    NativeViewHolder(viewDataBinding, uniqueRequestId, parentLifecycleOwner)

                AdDisplayType.NATIVE_ENHANCED_HIGH_AD.index,
                AdDisplayType.NATIVE_HIGH_AD.index,
                AdDisplayType.NATIVE_DFP_HIGH_AD.index,
                AdDisplayType.AD_FB_NATIVE_HIGH.index ->
                    NativeHighTemplateViewHolder(viewDataBinding, uniqueRequestId, parentLifecycleOwner)

                AdDisplayType.HTML_AD.index, AdDisplayType.HTML_AD_FULL.index ->
                    NativeAdHtmlViewHolder(viewDataBinding, uniqueRequestId, parentLifecycleOwner,
                        interactiveAdListener, swipeableHTMLAdInteractionListener,
                        webViewProvider = webViewProvider, parentHeight = parent?.height,
                        adExitListener = adExitListener)
                AdDisplayType.IMAGE_LINK.index ->
                    NativeAdImageLinkViewHolder(viewDataBinding as NewsItemTypeImageLinkAdBinding,
                            uniqueRequestId, parentLifecycleOwner)
                AdDisplayType.IMAGE_LINK_FULL.index ->
                    FullPageImageAdViewHolder(viewDataBinding as LayoutImageLinkFullAdBinding,
                        uniqueRequestId, parentLifecycleOwner, adExitListener = adExitListener)
                AdDisplayType.APP_DOWNLOAD.index ->
                    SwipableAdsHolder(viewDataBinding as AdsPagerLayoutBinding, uniqueRequestId,parentLifecycleOwner)
                AdDisplayType.IMA_VIDEO_AD.index ->
                    IMAVideoAdViewHolder(viewDataBinding as LayoutImaVideoAdsBinding, uniqueRequestId,
                            parentLifecycleOwner, adEntityReplaceHandler = adEntityReplaceHandler)
                AdDisplayType.EMPTY_AD.index ->
                    EmptyAdsViewHolder(viewDataBinding as EmptyAdLayoutBinding, uniqueRequestId)
                else -> null
            }?.apply {
                this.contentUrl = contentUrl
            }
        }
    }
}