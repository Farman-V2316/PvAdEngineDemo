package com.newsdistill.pvadenginedemo

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.newshunt.adengine.databinding.LayoutHtmlFullPageAdBinding
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.*
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.squareup.otto.Subscribe
import io.reactivex.plugins.RxJavaPlugins

class MainActivity : AppCompatActivity() {

    private lateinit var  adContainer: RelativeLayout
    private  var uiBus = BusProvider.getUIBusInstance()
    private val uniqueRequestId = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adContainer = findViewById(R.id.ad_container)

        adInit()
    }


    private fun adInit() {
        //AD init
        val useCase = GetAdUsecaseController(uiBus, uniqueRequestId)
        useCase.requestAds(AdRequest(AdPosition.PGI, 1, skipCacheMatching = true))
    }

    override fun onStart() {
        super.onStart()
        uiBus.register(this)
        tempFix()
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        println("panda: setAdResponse-------------------> $nativeAdContainer")

        if (nativeAdContainer.baseAdEntities == null ||
            nativeAdContainer.uniqueRequestId != uniqueRequestId) {
            return
        }
        renderAd(nativeAdContainer)
    }

    // fix rxjava2 crash
    private fun tempFix() {
        RxJavaPlugins.setErrorHandler {
            it?.printStackTrace()
        }
    }


    fun renderAd(nativeAdContainer: NativeAdContainer?) {
        if (nativeAdContainer == null || CommonUtils.isEmpty(nativeAdContainer.baseAdEntities)) {
            return
        }
        val baseAdEntity = nativeAdContainer.baseAdEntities!![0]
        println("panda: Ad to be shown: ${baseAdEntity}")
        renderAd(this, baseAdEntity, adContainer)
    }

    private fun renderAd(activity: Activity?, baseAdEntity: BaseAdEntity,
                         adContainer: RelativeLayout) {
        val cardType = AdsUtil.getCardTypeForAds(baseAdEntity)
        println("panda cardType for the ad: $cardType")
       getUpdatableAdView(this, cardType, adContainer, baseAdEntity)
    }

    private fun getUpdatableAdView(context: Context?, cardType: Int,
                                   adContainer: RelativeLayout, baseAdEntity: BaseAdEntity): UpdateableAdView? {
        if (context == null) {
            return null
        }
        var updateableAdView: UpdateableAdView? = null
        var viewDataBinding: ViewDataBinding? = null
        when (cardType) {
            AdDisplayType.HTML_AD_FULL.index -> {
                viewDataBinding = AdsViewHolderFactory.getViewBinding(cardType,
                    LayoutInflater.from(context), adContainer)
                println("panda: viewDataBinding-> $viewDataBinding")
                viewDataBinding?.let {
                    updateableAdView = AdsViewHolderFactory.getViewHolder(
                        cardType,
                        it,
                        -1, adContainer
                    ) as? UpdateableAdView
                }
            }
        }

        println("panda: updateableAdView-> $updateableAdView")
        viewDataBinding?.let {
            adContainer.removeAllViews()
            val rLParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

            adContainer.addView(viewDataBinding.root, rLParams)


            updateableAdView?.updateView(this, baseAdEntity)
        }
        return updateableAdView
    }

    override fun onStop() {
        super.onStop()
        uiBus.unregister(this)
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