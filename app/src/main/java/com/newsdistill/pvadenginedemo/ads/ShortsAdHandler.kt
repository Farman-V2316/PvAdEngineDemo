package com.newsdistill.pvadenginedemo.ads

import android.app.Activity
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.newshunt.adengine.databinding.LayoutHtmlFullPageAdBinding
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.squareup.otto.Bus
import io.reactivex.plugins.RxJavaPlugins

class ShortsAdHandler {

    fun initAd(adPosition: AdPosition, uniqueRequestId: Int, uiBus: Bus) {
        val useCase = GetAdUsecaseController(uiBus, uniqueRequestId)
        useCase.requestAds(AdRequest(adPosition, 1, skipCacheMatching = true))

        tempFix()
    }

    // fix rxjava2 crash
    private fun tempFix() {
        RxJavaPlugins.setErrorHandler {
            it?.printStackTrace()
        }
    }

    fun insertAd(
        activity: Activity?,
        nativeAdContainer: NativeAdContainer?,
        adContainer: RelativeLayout
    ) {
        if (nativeAdContainer == null || CommonUtils.isEmpty(nativeAdContainer.baseAdEntities)) {
            return
        }
        val baseAdEntity = nativeAdContainer.baseAdEntities!![0]
        println("panda: Ad to be shown: ${baseAdEntity}")
        renderAd(activity, baseAdEntity, adContainer)
    }

    private fun renderAd(activity: Activity?, baseAdEntity: BaseAdEntity,
                         adContainer: RelativeLayout
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
                }


                println("panda: updateableAdView-> $updateableAdView")
                viewDataBinding?.let {
                    adContainer.removeAllViews()
                    val rLParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                    rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

                    adContainer.addView(viewDataBinding.root, rLParams)

                    updateableAdView?.updateView(activity, baseAdEntity)
                }
            }

            AdDisplayType.PGI_ARTICLE_AD.index -> {
                //TODO..
            }
        }


        return updateableAdView
    }

    private fun showPgiAd(activity: Activity?, baseAdEntity: BaseAdEntity) {
        val viewDataBinding = DataBindingUtil.inflate<LayoutHtmlFullPageAdBinding>(
            LayoutInflater.from(activity),
            com.newshunt.adengine.R.layout.layout_html_full_page_ad, null, false)
            .apply {
            }
        val nativeAdHtmlViewHolder = NativeAdHtmlViewHolder(viewDataBinding)
        activity?.let { activity ->
            nativeAdHtmlViewHolder.updateView(activity, baseAdEntity)
        }
    }
}