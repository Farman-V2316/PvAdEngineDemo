/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.newshunt.adengine.databinding.AdFbNativePgiBinding
import com.newshunt.adengine.databinding.DfpPgiNativeAdBinding
import com.newshunt.adengine.databinding.PgiNativeAdBinding
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.AdViewedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativePgiAdAsset
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.ExternalNativePGIViewHolder
import com.newshunt.adengine.view.viewholder.ExternalSdkViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.adengine.view.viewholder.PgiNativeAdViewHolder
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.databinding.FragmentPgiNativeAdBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dataentity.common.asset.BaseDetailList
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.news.util.NewsConstants

/**
 * This is a Fragment to show native Pgi ads.
 *
 * @author Mukesh Yadav
 */
class PgiNativeAdFragment : BaseSupportFragment(),
        NativeAdHtmlViewHolder.SwipeableHTMLAdInteractionListener{
    private lateinit var binding: FragmentPgiNativeAdBinding
    private var nativePgiAdAsset: NativePgiAdAsset? = null
    private var baseAdEntity: BaseDisplayAdEntity? = null
    private var pageReferrer: PageReferrer? = null
    private var updateableAdView: UpdateableAdView? = null
    private var pgiNativeAdFragmentInterface: PgiNativeAdFragmentInterface? = null
    private var isAdInserted = false
    private var showAdOnlyOnVisible: Boolean = false
    private var reportAdsMenuListener: ReportAdsMenuListener? = null
    private val backListener = View.OnClickListener {
        if (pgiNativeAdFragmentInterface == null) {
            activity?.finish()
        } else {
            pgiNativeAdFragmentInterface?.onFragmentBackPressed()
            activity?.onBackPressed()
        }
    }

    val story: BaseDetailList?
        get() = nativePgiAdAsset

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            readValuesFromBundle(bundle)
        }
        if (baseAdEntity == null && savedInstanceState != null) {
            readValuesFromBundle(savedInstanceState)
        }
    }

    private fun readValuesFromBundle(bundle: Bundle){
        nativePgiAdAsset = bundle.getSerializable(NewsConstants.STORY_EXTRA) as NativePgiAdAsset?
        require(nativePgiAdAsset?.baseAdEntity is BaseDisplayAdEntity) { "Ad passed is not BaseDisplayAdEntity" }
        baseAdEntity = nativePgiAdAsset?.baseAdEntity as BaseDisplayAdEntity
        showAdOnlyOnVisible = baseAdEntity?.showHTMLPgiOnlyOnVisible ?: false
        pageReferrer = bundle.getSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundle = arguments
        if (bundle != null) {
            outState.putSerializable(NewsConstants.STORY_EXTRA, bundle.getSerializable(NewsConstants.STORY_EXTRA))
            outState.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, bundle.getSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pgi_native_ad, container, false)
        binding.setVariable(BR.item, baseAdEntity)

        initActionBar()
        reportAdsMenuListener = ReportAdsMenuFeedbackHelper(this, parentFragment as? OnAdReportedListener?)
        insertAd()
        binding.executePendingBindings()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            pgiNativeAdFragmentInterface = parentFragment as PgiNativeAdFragmentInterface?
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement PgiNativeAdFragmentInterface")
        }
    }

    private fun initActionBar() {
        binding.actionbar.actionbarBackButtonLayout.setOnClickListener(backListener)
    }

    private fun needToolbar(): Boolean {
        return PgiAdsConfig.HTMLPgiDisplayType.SWIPEABLE_WITH_TOPBAR == baseAdEntity?.interstitialDisplayType
    }

    private fun needTopBackButton(): Boolean {
        return PgiAdsConfig.HTMLPgiDisplayType.SWIPEABLE_WITH_BACK == baseAdEntity?.interstitialDisplayType
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdInserted) {
            if (updateableAdView is PgiNativeAdViewHolder) {
                (updateableAdView as PgiNativeAdViewHolder).onAdViewVisibilityChange(isVisibleToUser)
            } else if (updateableAdView is NativeAdHtmlViewHolder) {
                (updateableAdView as NativeAdHtmlViewHolder).onAdViewVisibilityChange(isVisibleToUser, showAdOnlyOnVisible)
            }
        }
        if (!isVisibleToUser) {
            if (activity != null) {
                AndroidUtils.hideKeyBoard(activity)
            }
            return
        }

        if (showAdOnlyOnVisible && updateableAdView != null) {
            binding.adContainer.visibility = View.VISIBLE
            if (!isAdInserted || updateableAdView is NativeAdHtmlViewHolder && (updateableAdView
                            as NativeAdHtmlViewHolder).isUpdateNeeded) {
                updateableAdView?.updateView(activity, baseAdEntity)
                isAdInserted = true
            }
        }
        ViewUtils.screenChanged()
        onCardView(baseAdEntity)

        //Send event so that other fragments may prefetch masthead ad now.
        BusProvider.getUIBusInstance().post(AdViewedEvent(Constants.EMPTY_STRING, uniqueScreenId, null,
                AdPosition.MASTHEAD))
    }

    private fun insertAd() {
        baseAdEntity?.type ?: return
        val cardType: Int? = baseAdEntity?.let {
            AdsUtil.getCardTypeForAds(it)
        }
        if (cardType == null || cardType == -1) {
            return
        }

        if (needToolbar()) {
            binding.actionbar.actionbar.visibility = View.VISIBLE
        }
        if (needTopBackButton()) {
            binding.backButton.visibility = View.VISIBLE
            binding.backButton.setOnClickListener(backListener)
        }
        updateableAdView = getUpdateableAdView(cardType, binding.adContainer)
        if (showAdOnlyOnVisible) {
            isAdInserted = false
        } else {
            binding.adContainer.visibility = View.VISIBLE
            updateableAdView?.updateView(activity, baseAdEntity)
            isAdInserted = true
        }
    }

    private fun getUpdateableAdView(cardType: Int, parent: ViewGroup): UpdateableAdView? {
        var updateableAdView: UpdateableAdView? = null
        val context = activity ?: return null
        val layoutInflater = LayoutInflater.from(context)
        var viewDataBinding: ViewDataBinding? = AdsViewHolderFactory.getViewBinding(cardType,
                layoutInflater, parent)

        when (cardType) {
            AdDisplayType.PGI_ARTICLE_AD.index -> {
                viewDataBinding = DataBindingUtil.inflate<PgiNativeAdBinding>(layoutInflater,
                        R.layout.pgi_native_ad, parent, false)
                updateableAdView = PgiNativeAdViewHolder(viewDataBinding, lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.EXTERNAL_SDK.index -> {
                viewDataBinding?.let { updateableAdView = ExternalSdkViewHolder(it, parentLifecycleOwner = viewLifecycleOwner) }
            }
            AdDisplayType.AD_FB_NATIVE.index -> {
                viewDataBinding = DataBindingUtil.inflate<AdFbNativePgiBinding>(layoutInflater,
                        R.layout.ad_fb_native_pgi, parent, false)
                updateableAdView = ExternalNativePGIViewHolder(viewDataBinding, lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.NATIVE_DFP_AD.index -> {
                viewDataBinding = DataBindingUtil.inflate<DfpPgiNativeAdBinding>(layoutInflater,
                        R.layout.dfp_pgi_native_ad, parent, false)
                updateableAdView = ExternalNativePGIViewHolder(viewDataBinding,lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.EXTERNAL_NATIVE_PGI.index -> {
                viewDataBinding = DataBindingUtil.inflate<PgiNativeAdBinding>(layoutInflater,
                        R.layout.pgi_native_ad, parent, false)
                updateableAdView = ExternalNativePGIViewHolder(viewDataBinding,lifecycleOwner = viewLifecycleOwner)
            }
            AdDisplayType.HTML_AD_FULL.index -> {
                viewDataBinding?.let {
                    updateableAdView = NativeAdHtmlViewHolder(it, uniqueScreenId,
                        lifecycleOwner = viewLifecycleOwner,
                        swipeableHTMLAdInteractionListener = this
                    )
                }
            }
        }
        viewDataBinding ?: return null
        viewDataBinding.setVariable(BR.adReportListener, reportAdsMenuListener)
        viewDataBinding.setVariable(BR.appSettingsProvider,AppSettingsProvider)
        parent.removeAllViews()
        parent.addView(viewDataBinding.root)
        return updateableAdView
    }

    private fun onCardView(baseAdEntity: BaseAdEntity?) {
        updateableAdView?.onCardView(baseAdEntity as BaseDisplayAdEntity?)
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            (binding.root as ViewGroup).removeView(binding.adContainer)
        }
        updateableAdView?.onDestroy()
        super.onDestroy()
    }

    /**
     * Interface to talk to activity
     */
    interface PgiNativeAdFragmentInterface {
        fun onFragmentBackPressed()

        fun onPgiAdClosed()
    }

    //This was causing crash on AppBack, discussed with Raunak and commenting it as its not needed
    // refLink: https://stackoverflow.com/questions/56925777/boolean-androidx-fragment-app-fragmentmanagerimpl-isdestroyed-on-a-null-obje
//    override fun onDetach() {
//        try {
//            super.onDetach()
//            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
//            childFragmentManager.isAccessible = true
//            childFragmentManager.set(this, null)
//
//        } catch (e: Exception) {
//            Logger.caughtException(e)
//        }
//
//    }

    override fun onSwipeableHtmlAdClosed() {
        pgiNativeAdFragmentInterface?.onPgiAdClosed()
    }

    companion object {
        @JvmStatic
        fun newInstance(baseDetailList: BaseDetailList, pageReferrer: PageReferrer? = null):
                PgiNativeAdFragment {
            val pgiNativeAdFragment = PgiNativeAdFragment()
            pgiNativeAdFragment.arguments = Bundle()
            pgiNativeAdFragment.arguments?.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            pgiNativeAdFragment.arguments?.putSerializable(NewsConstants.STORY_EXTRA, baseDetailList)
            return pgiNativeAdFragment
        }
    }

}// Required empty public constructor