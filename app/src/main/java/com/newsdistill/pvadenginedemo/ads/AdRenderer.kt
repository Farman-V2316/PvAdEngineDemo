package com.newsdistill.pvadenginedemo.ads

import android.app.Activity
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.R
import com.newshunt.adengine.databinding.AdsNativeHighLayoutBinding
import com.newshunt.adengine.databinding.AdsNativeLowLayoutBinding
import com.newshunt.adengine.databinding.NewsItemTypeHtmlAdBinding
import com.newshunt.adengine.databinding.NewsItemTypeImageLinkAdBinding
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdImageLinkViewHolder
import com.newshunt.adengine.view.viewholder.NativeViewHolder
import com.newshunt.adengine.view.viewholder.PgiNativeAdViewHolder
import com.newshunt.dataentity.common.asset.AdDisplayType

class AdRenderer(var lifecycleOwner: LifecycleOwner) {

     fun renderAd(
        activity: Activity?, baseAdEntity: BaseAdEntity,
        adContainer: RelativeLayout,
    ) {
        val cardType = AdsUtil.getCardTypeForAds(baseAdEntity)
        println("panda cardType for the ad: $cardType")
        getUpdatableAdView(activity, cardType, adContainer, baseAdEntity)
    }

    private fun getUpdatableAdView(activity: Activity?, cardType: Int,
                           adContainer: RelativeLayout, baseAdEntity: BaseAdEntity
    ): UpdateableAdView? {
        if (activity == null) {
            return null
        }
        var updateableAdView: UpdateableAdView? = null
        var viewDataBinding: ViewDataBinding? = null
        when (cardType) {
            AdDisplayType.HTML_AD_FULL.index -> {
                viewDataBinding = AdsViewHolderFactory.getViewBinding(cardType,
                    LayoutInflater.from(activity), adContainer)
                println("panda: viewDataBinding-> $viewDataBinding")
                viewDataBinding?.let {
                    updateableAdView = AdsViewHolderFactory.getViewHolder(
                        cardType,
                        it,
                        -1, adContainer
                    ) as? UpdateableAdView

                    println("panda: updateableAdView-> $updateableAdView")
                    adContainer.removeAllViews()
                    val rLParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                    rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

                    adContainer.addView(viewDataBinding!!.root, rLParams)

                    updateableAdView?.updateView(activity, baseAdEntity)
                }
            }

            AdDisplayType.PGI_ARTICLE_AD.index -> {
                val layoutInflater = LayoutInflater.from(activity)
                viewDataBinding = DataBindingUtil.inflate(layoutInflater,
                    R.layout.pgi_native_ad, adContainer, false)
                updateableAdView = PgiNativeAdViewHolder(viewDataBinding, lifecycleOwner = lifecycleOwner)

                adContainer.addView(viewDataBinding.root)
                updateableAdView?.updateView(activity, baseAdEntity)
            }

            AdDisplayType.IMAGE_LINK.index -> {
                viewDataBinding = DataBindingUtil.inflate<NewsItemTypeImageLinkAdBinding>(
                    LayoutInflater.from(activity), R.layout.news_item_type_image_link_ad,
                    adContainer, false)

                updateableAdView = NativeAdImageLinkViewHolder(viewDataBinding as NewsItemTypeImageLinkAdBinding,
                    100, lifecycleOwner)

                adContainer.removeAllViews()
                adContainer.addView(viewDataBinding.root)

                (updateableAdView as NativeAdImageLinkViewHolder).updateView(activity, baseAdEntity)
            }

            AdDisplayType.NATIVE_AD.index -> {
                viewDataBinding =  DataBindingUtil.inflate<AdsNativeLowLayoutBinding>(LayoutInflater.from(activity),
                    R.layout.ads_native_low_layout, adContainer, false)

                updateableAdView = NativeViewHolder(viewDataBinding, 100, lifecycleOwner)
                adContainer.removeAllViews()
                adContainer.addView(viewDataBinding.root)
                (updateableAdView as NativeViewHolder).updateView(activity, baseAdEntity)
            }

            AdDisplayType.NATIVE_HIGH_AD.index -> {
                viewDataBinding = DataBindingUtil.inflate<AdsNativeHighLayoutBinding>(LayoutInflater.from(activity),
                        R.layout.ads_native_high_layout, adContainer, false)

                updateableAdView = NativeViewHolder(viewDataBinding, 100, lifecycleOwner)
                adContainer.removeAllViews()
                adContainer.addView(viewDataBinding.root)
                (updateableAdView as NativeViewHolder).updateView(activity, baseAdEntity)
            }

            AdDisplayType.HTML_AD.index -> {
               viewDataBinding = DataBindingUtil.inflate<NewsItemTypeHtmlAdBinding>(LayoutInflater.from(activity),
                    R.layout.news_item_type_html_ad, adContainer, false)

               updateableAdView = NativeAdHtmlViewHolder(viewDataBinding, 100, lifecycleOwner)

                adContainer.removeAllViews()
                adContainer.addView(viewDataBinding.root)
                (updateableAdView as NativeAdHtmlViewHolder).updateView(activity, baseAdEntity)
            }
        }

        return updateableAdView
    }
}
