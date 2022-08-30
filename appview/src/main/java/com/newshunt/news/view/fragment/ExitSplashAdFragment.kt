/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.newshunt.adengine.databinding.AdFbNativePgiBinding
import com.newshunt.adengine.databinding.DfpPgiNativeAdBinding
import com.newshunt.adengine.databinding.PgiNativeAdBinding
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.entity.AdExitEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.version.AdLPBackAction
import com.newshunt.adengine.model.entity.version.AdUIType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.ExternalNativePGIViewHolder
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.AdExitState
import com.newshunt.appview.common.viewmodel.AdsExitHandler
import com.newshunt.appview.databinding.FragmentExitSplashAdBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.AdDummyActivity
import com.squareup.otto.Subscribe

/**
 * This is a Fragment to show splash-exit ads.
 * TODO(raunak): SDK interstitial ad Auto-close works only if the app is in started state
 * when timer expires. If minimized before expiry, the activity remains and will be seen if app
 * is launched again.
 *
 * @author raunak.yadav
 */
class ExitSplashAdFragment : BaseSupportFragment(), OnAdReportedListener {

    private lateinit var binding: FragmentExitSplashAdBinding
    private var adEntity: BaseDisplayAdEntity? = null
    private var pageReferrer: PageReferrer? = null
    private var updateableAdView: UpdateableAdView? = null
    private var reportAdsMenuListener: ReportAdsMenuListener? = null
    private var adExitListener = AdsExitHandler()
    private var lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            when (adExitListener.state) {
                AdExitState.CANCELED_ON_CLICK,
                AdExitState.COMPLETE -> {
                    AdLogger.d(TAG, "Ad exit state ${adExitListener.state}. Return")
                    return
                }
                else -> {
                    AdLogger.d(TAG, "App onStop. Clean up after exit splash.")
                    activity?.let {
                        adExitListener.trackAdClose(adEntity, AdInteraction.USER_NAV)
                        NavigationHelper.finalAppCleanup(it)
                    }
                }
            }
            super.onStop(owner)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            readValuesFromBundle(bundle)
        }
        if (adEntity == null && savedInstanceState != null) {
            readValuesFromBundle(savedInstanceState)
        }
        BusProvider.getUIBusInstance().register(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    private fun readValuesFromBundle(bundle: Bundle) {
        adEntity = bundle.getSerializable(Constants.BUNDLE_AD_EXTRA) as BaseDisplayAdEntity?
        pageReferrer =
            bundle.getSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundle = arguments
        if (bundle != null) {
            outState.putSerializable(Constants.BUNDLE_AD_EXTRA,
                bundle.getSerializable(Constants.BUNDLE_AD_EXTRA))
            outState.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER,
                bundle.getSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_exit_splash_ad, container, false)
        binding.setVariable(BR.adEntity, adEntity)

        reportAdsMenuListener = ReportAdsMenuFeedbackHelper(this, this)
        insertAd()
        binding.setVariable(BR.adExitListener, adExitListener)
        binding.executePendingBindings()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        restartAdTimer()
    }

    private fun restartAdTimer() {
        if (adExitListener.state == AdExitState.CANCELED) {
            AdLogger.d(TAG, "Restart exit-splash timer")
            adEntity?.spanInMS?.let {
                if (it > 0)
                    adExitListener.closeToExitAppwithDelay(adEntity, AdInteraction.AUTO_TIMER, it) {
                        if (AdsUtil.isExternalPopUpAd(adEntity)) {
                            launchDummyActivityToCloseApp()
                        }
                    }
            }
        }
    }

    override fun onStop() {
        AdLogger.d(TAG, "Stop exit-splash timer")
        adExitListener.cancelExitApp()
        super.onStop()
    }

    private fun insertAd() {
        adEntity?.let { ad ->
            val cardType: Int = AdsUtil.getCardTypeForAds(ad)

            if (cardType == -1) {
                return
            }
            if (ad.displayType == AdUIType.MINI_SCREEN) {
                setupAdContainerForMini(ad)
            }
            if (AdsUtil.isExternalPopUpAd(ad)) {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }
            updateableAdView = getUpdateableAdView(cardType, binding.adContainer) ?: return@let
            AdLogger.d(TAG, "Inserting Ad $ad")

            updateableAdView?.updateView(activity, ad)
            binding.adContainer.visibility = View.VISIBLE
            updateableAdView?.onCardView(ad)


            ad.spanInMS?.let {
                if (it > 0)
                    adExitListener.closeToExitAppwithDelay(ad, AdInteraction.AUTO_TIMER, it) {
                        if (AdsUtil.isExternalPopUpAd(ad)) {
                            launchDummyActivityToCloseApp()
                        }
                    }
            }
        }
    }

    private fun setupAdContainerForMini(ad: BaseDisplayAdEntity) {
        binding.adContainer.setCornerRadius(CommonUtils.getDimension(R.dimen.ad_container_corner_radius))

        val lp = binding.adContainer.layoutParams
        lp.height = ad.height ?: AdConstants.DEFAULT_AD_SIZE
        lp.width = ad.width ?: AdConstants.DEFAULT_AD_SIZE
        binding.adContainer.layoutParams = lp
    }

    private fun getUpdateableAdView(cardType: Int, parent: ViewGroup): UpdateableAdView? {
        var updateableAdView: UpdateableAdView? = null
        val context = activity ?: return null
        val layoutInflater = LayoutInflater.from(context)
        var viewDataBinding: ViewDataBinding? = AdsViewHolderFactory.getViewBinding(cardType,
            layoutInflater, parent)

        when (cardType) {
            AdDisplayType.AD_FB_NATIVE.index -> {
                viewDataBinding = DataBindingUtil.inflate<AdFbNativePgiBinding>(layoutInflater,
                    R.layout.ad_fb_native_pgi, parent, false)
                updateableAdView = ExternalNativePGIViewHolder(viewDataBinding,
                    lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.NATIVE_DFP_AD.index -> {
                viewDataBinding = DataBindingUtil.inflate<DfpPgiNativeAdBinding>(layoutInflater,
                    R.layout.dfp_pgi_native_ad, parent, false)
                updateableAdView = ExternalNativePGIViewHolder(viewDataBinding,
                    lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.EXTERNAL_NATIVE_PGI.index -> {
                viewDataBinding = DataBindingUtil.inflate<PgiNativeAdBinding>(layoutInflater,
                    R.layout.pgi_native_ad, parent, false)
                updateableAdView = ExternalNativePGIViewHolder(viewDataBinding,
                    lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.EXTERNAL_SDK.index,
            AdDisplayType.IMAGE_LINK_FULL.index,
            AdDisplayType.HTML_AD_FULL.index -> {
                viewDataBinding?.let {
                    updateableAdView = AdsViewHolderFactory.getViewHolder(cardType, it,
                        uniqueScreenId, parent, parentLifecycleOwner = viewLifecycleOwner,
                        adExitListener = adExitListener
                    ) as UpdateableAdView?
                }
            }
        }
        viewDataBinding ?: return null
        viewDataBinding.setVariable(BR.adExitListener, adExitListener)
        viewDataBinding.setVariable(BR.adReportListener, reportAdsMenuListener)
        viewDataBinding.setVariable(BR.appSettingsProvider, AppSettingsProvider)
        parent.removeAllViews()
        parent.addView(viewDataBinding.root)
        return updateableAdView
    }

    override fun handleBackPress(): Boolean {
        AdLogger.d(TAG, "handle Backpress")
        if (updateableAdView == null) {
            AdLogger.d(TAG, "handle Backpress return")
            return true
        }
        adExitListener.closeToExitApp(adEntity, AdInteraction.USER_BACK_NAVIGATION)
        return true
    }

    @Subscribe
    fun onAppExitInitiated(event: AdExitEvent) {
        // This event is currently posted only by DFP/FB ads where the click/exit listeners can be
        // initiated only in requester.
        if (event.ad.uniqueAdIdentifier == adEntity?.uniqueAdIdentifier) {
            AdLogger.d(TAG, "onAppExitInitiated $event")

            if (event.adInteraction == AdInteraction.USER_CLICK) {
                when (event.ad.backFromLpAction) {
                    AdLPBackAction.BACK_TO_APP -> adExitListener.cancelExitApp()
                    else -> {
                        adExitListener.closeToExitApp(adEntity, AdInteraction.USER_CLICK)
                    }
                }
            } else {
                adExitListener.closeToExitApp(adEntity, event.adInteraction ?: AdInteraction.USER_CLOSE)
            }
        }
    }

    //Called only for cases where 3rd party AdActivity is on top and cannot be closed otherwise.
    private fun launchDummyActivityToCloseApp() {
        AdLogger.d(TAG, "exiting app via Dummy activity hack.")
        val intent = Intent(activity, AdDummyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
    }

    override fun onAdReported(reportedAdEntity: BaseAdEntity?,
                              reportedParentAdIdIfCarousel: String?) {
        AdLogger.d(TAG, "onAdReported $reportedAdEntity")
        if (reportedAdEntity is BaseDisplayAdEntity) {
            adExitListener.closeToExitApp(reportedAdEntity, AdInteraction.USER_FEEDBACK_CLICK)
        }
    }

    override fun onAdReportDialogDismissed(reportedAdEntity: BaseAdEntity?,
                                           reportedParentAdIdIfCarousel: String?) {
        AdLogger.d(TAG, "Ad Report dialog dismiss")
        restartAdTimer()
    }

    override fun onDestroy() {
        AdLogger.d(TAG, "On destroy")
        updateableAdView?.onDestroy()
        if (::binding.isInitialized) {
            (binding.root as ViewGroup).removeView(binding.adContainer)
        }
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        BusProvider.getUIBusInstance().unregister(this)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ExitSplashAdFragment"

        @JvmStatic
        fun newInstance(intent: Intent): ExitSplashAdFragment {
            val fragment = ExitSplashAdFragment()
            fragment.arguments = intent.extras
            return fragment
        }
    }

}