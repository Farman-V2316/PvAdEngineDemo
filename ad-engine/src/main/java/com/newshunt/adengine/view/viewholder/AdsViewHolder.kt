/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAdView
import com.newshunt.adengine.BR
import com.newshunt.adengine.ImmersiveRuleUpdate
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.AdViewedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeAdBanner
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.presenter.OMTrackController
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdMacroUtils
import com.newshunt.adengine.util.AdsActionHandler
import com.newshunt.adengine.util.AdsShareViewHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.OMSdkHelper
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.DfpNativeViewHelper
import com.newshunt.adengine.view.helper.FacebookAdViewHelper
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.ClearableCard
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder
import java.util.ArrayList

/**
 * ViewHolder with common code for all ads viewholders.
 *
 * @author raunak.yadav
 */
abstract class AdsViewHolder(private val viewBinding: ViewDataBinding, private val uiComponentId: Int = -1, val viewLifeCycleOwner: LifecycleOwner? = null)
    : RecyclerView.ViewHolder(viewBinding.root), UpdateableAdView, VisibilityAwareViewHolder, ClearableCard, LifecycleObserver {
    protected var baseAdEntity: BaseAdEntity? = null
    // For ads with delayed loading like web ads, this will be false.
    private var adLoadsOnBind: Boolean = false
    private var trackController: OMTrackController? = null
    protected val clearableImageViews: MutableList<ImageView?> = ArrayList()
    protected var adsShareViewHelper: AdsShareViewHelper
    private var adsActionHandler: AdsActionHandler

    var contentUrl: String? = null
    open var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null
    protected var adsTimeSpentOnLPHelper : AdsTimeSpentOnLPHelper? = null

    init {
        if (viewBinding.root is NativeAdView) {
            viewBinding.root.setTag(com.newshunt.common.util.R.id.omid_adview_tag_id, Constants.OM_WEBVIEW_TAG)
        }
        viewBinding.lifecycleOwner = viewLifeCycleOwner
        adsShareViewHelper = AdsShareViewHelper(viewBinding.root.context)
        adsActionHandler = AdsActionHandler()
        viewLifeCycleOwner?.lifecycle?.addObserver(this)
    }

    @JvmOverloads
    fun updateView(baseAdEntity: BaseAdEntity, adLoadsOnBind: Boolean = true) {
        this.baseAdEntity = baseAdEntity
        this.adLoadsOnBind = adLoadsOnBind

        if (baseAdEntity is BaseDisplayAdEntity) {
            asyncAdImpressionReporter = AsyncAdImpressionReporter(baseAdEntity)
        }
        if (adLoadsOnBind) {
            startTrackingOnAdLoad(itemView)
            asyncAdImpressionReporter?.onAdInflated()
        }
        viewBinding.setVariable(BR.adsShareViewHelper, adsShareViewHelper)
        viewBinding.setVariable(BR.appSettingsProvider, AppSettingsProvider)
        viewBinding.setVariable(BR.adsActionHandler, adsActionHandler)
    }

    /**
     * To be called once ad creative is attached to view and ready.
     *
     * @param adView adView
     */
    protected fun startTrackingOnAdLoad(adView: View) {
        if (baseAdEntity is BaseDisplayAdEntity) {
            val adEntity = baseAdEntity as BaseDisplayAdEntity
            trackController = OMTrackController.createTracker(adEntity)?.apply {
                if (adEntity.omSessionState == null) {
                    adEntity.omSessionState = HashMap()
                }
                val omSession = adEntity.omSessionState?.get(uiComponentId)
                if (omSession == null) {
                    //1st binding of ad in this screen.
                    val sessionState = startTracking(adView, contentUrl)
                    adEntity.omSessionState?.put(uiComponentId, sessionState)
                } else {
                    //register view again, in case it changed because of reuse.
                    registerViewToTrack(omSession.adSession, adView)
                }
            }
            if (adEntity.isShown) {
                recordOMImpression(adEntity)
            }
        }
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (!baseAdEntity.isShown) {
            baseAdEntity.isShown = true
            baseAdEntity.notifyObservers()

            AdFrequencyStats.onAdViewed(baseAdEntity, uniqueRequestId = uiComponentId)

            if (baseAdEntity is BaseDisplayAdEntity) {
                recordOMImpression(baseAdEntity)
                if (asyncAdImpressionReporter == null) {
                    asyncAdImpressionReporter = AsyncAdImpressionReporter(baseAdEntity)
                }
                val adPositionIndex = AdsUtil.computeAdInsertedIndex(baseAdEntity, adapterPosition)
                asyncAdImpressionReporter?.onCardView(PlayerControlHelper.isListMuteMode,
                        adPositionIndex)
            }
            //Reporting for MultipleAdEntity will be done by individual ads.

            // Remove the ad from other lists now.
            // If Masthead, fire event to notify adjacent fragments.
            // Some zones refill cache on adView.
            AndroidUtils.getMainThreadHandler().postDelayed({
                val event = AdViewedEvent(baseAdEntity.uniqueAdIdentifier,
                        uiComponentId, baseAdEntity.parentIds, baseAdEntity.adPosition!!, baseAdEntity.adTag)
                event.parentIds?.remove(uiComponentId)
                BusProvider.getUIBusInstance().post(event)
            }, if (baseAdEntity.adPosition == AdPosition.MASTHEAD) 200L else 0L)
        } else if (!adLoadsOnBind) {
            //For delayed ads, OM impression might not have been fired even if ad.isShown() is true.
            recordOMImpression(baseAdEntity)
        }
    }

    /**
     * Fire impression for OM sdk. Must be fired only once.
     */
    private fun recordOMImpression(adEntity: BaseAdEntity) {
        if (adEntity is BaseDisplayAdEntity && !adEntity.omImpressionFired) {
            trackController?.let {
                OMSdkHelper.recordImpression(adEntity, uiComponentId)
            }
        }
    }

    override fun getAdEntity(): BaseAdEntity? {
        return baseAdEntity
    }

    fun onDestroy(helper: NativeViewHelper?) {
        helper?.destroy(uiComponentId)
        //Do not destroy ad for pgi since we can swipe backwards and still see it. Will be
        // destroyed from NewsDetailsActivity.
        if (!AdsUtil.isExternalSdkNativePgiAd(baseAdEntity)) {
            AdsUtil.destroyAd(baseAdEntity, uiComponentId)
        }
        baseAdEntity = null
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        onCardView(baseAdEntity ?: return)
    }

    override fun onInVisible() {

    }

    override fun onUserLeftFragment() {
        onInVisible()
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        onCardView(baseAdEntity ?: return)
    }

    override fun recycleView() {
        clearableImageViews.forEach {
            if (it != null) {
                it.setImageBitmap(null)
                it.setImageDrawable(null)
            }
        }
        baseAdEntity?.let {
            fireImmersiveUpdateRule(it, false)
        }
    }

    fun fireImmersiveUpdateRule(adEntity: BaseAdEntity, playedInImmersive: Boolean = false) {
        ImmersiveRuleUpdate(SocialDB.instance().immersiveRuleDao())
                .toMediator2().apply {
                    execute(bundleOf(
                            AdConstants.B_AD_ENTITY to adEntity,
                            ImmersiveRuleUpdate.B_AD_PLAYED_IMMERSIVE to playedInImmersive,
                            AdConstants.B_AD_POS to position))
                }
    }


    protected fun getNativeViewHelper(activity: Activity,
                                      baseDisplayAdEntity: BaseDisplayAdEntity?): NativeViewHelper? {
        baseDisplayAdEntity ?: return null

        if (baseDisplayAdEntity is NativeAdBanner) {
            adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
            return NativeAdBannerViewHelper(baseDisplayAdEntity, activity,adsTimeSpentOnLPHelper)
        }

        val externalSdkAd = baseDisplayAdEntity as ExternalSdkAd? ?: return null

        if (externalSdkAd.external?.data?.startsWith(AdConstants.FB_AD) == true) {
            return FacebookAdViewHelper(externalSdkAd, activity)
        }
        if (externalSdkAd.external?.data?.startsWith(AdConstants.DFP_AD) == true) {
            return DfpNativeViewHelper(externalSdkAd, activity)
        }
        return null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResume() {
        adsTimeSpentOnLPHelper?.stopAdsTimeSpentOnLPTimerAndTriggerEvent()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        viewLifeCycleOwner?.lifecycle?.removeObserver(this)
    }
}
