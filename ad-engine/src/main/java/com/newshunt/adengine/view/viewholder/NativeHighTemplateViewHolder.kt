/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.bumptech.glide.request.transition.Transition
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.model.entity.AdReportInfo
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.NHWrappedHeightLayout
import com.newshunt.dhutil.model.entity.players.StubbornPlayable
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.sdk.network.image.Image

/**
 * ViewHolder to display native ads with AdTemplate as 'HIGH'.
 *
 * @author raunak.yadav
 */
class NativeHighTemplateViewHolder(private val viewBinding: ViewDataBinding,
                                   uniqueRequestId: Int,
                                   private var lifecycleOwner: LifecycleOwner? = null)
    : AdsViewHolder(viewBinding, uniqueRequestId, lifecycleOwner), StubbornPlayable, LifecycleObserver {

    private val view = viewBinding.root
    private val mediaViewLayout: NHWrappedHeightLayout
    private val imageView: NHImageView
    private val bottomBanner: View
    private val bottomBrandBannerBar: View
    private var sdkMediaView: View? = null
    private var adChoicesView: View? = null
    private var height: Int = 0

    private var autoplayVisibility: Int = 0
    private var videoVisiblePercentage: Int = 0
    private var autoPlayManager: AutoPlayManager? = null
    private var maxHeightNative: Int = (AdsUtil.defaultWidthForWideAds /
            AdsUtil.minAspectRatioNative).toInt()
    private var maxHeightVideo: Int = (AdsUtil.defaultWidthForWideAds /
            AdsUtil.minAspectRatioVideo).toInt()
    private var nativeHelper: NativeViewHelper? = null

    private val clickableViews: MutableList<View>

    init {
        viewBinding.lifecycleOwner = lifecycleOwner
        view.visibility = View.GONE
        clickableViews = ArrayList()

        val headlineView = view.findViewById<View>(R.id.ad_title) as TextView
        mediaViewLayout = view.findViewById<View>(R.id.mediaView) as NHWrappedHeightLayout
        imageView = view.findViewById<View>(R.id.ad_image) as NHImageView
        bottomBanner = view.findViewById(R.id.ad_banner_bottombar)
        bottomBrandBannerBar = view.findViewById(R.id.ad_banner_brand_bottombar)

        clickableViews.addAll(listOf(headlineView, mediaViewLayout, imageView, bottomBanner, bottomBrandBannerBar))
        clearableImageViews.add(imageView)
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun updateView(activity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is BaseDisplayAdEntity) {
            return
        }
        // If the same ad is bound, then just update the UI as per ad and no other tracker need be
        // fired.
        val trackLoad = !baseAdEntity.equals(this.baseAdEntity)

        nativeHelper = getNativeViewHelper(activity, baseAdEntity)
        nativeHelper ?: return

        if (nativeHelper?.shouldClearClickListeners() == true) {
            AdsUtil.removeClickListenerFromAllChilds(view as ViewGroup)
        }
        updateView(baseAdEntity, trackLoad)

        val nativeAssets = nativeHelper?.getNativeAssets() ?: return
        if (baseAdEntity.isVideoAd) {
            autoplayVisibility = nativeHelper?.getAutoplayVisibility() ?: 100
        }
        mediaViewLayout.removeAllViews()
        sdkMediaView?.let { clickableViews.remove(it) }

        view.visibility = View.VISIBLE

        val mediaView = nativeHelper?.getMediaViewIfApplicable(mediaViewLayout)
        if (mediaView != null) {
            imageView.visibility = View.GONE
            mediaViewLayout.maxHeight = if (baseAdEntity.isVideoAd) maxHeightVideo else
                maxHeightNative
            nativeHelper?.getPreferredHeightMediaView(nativeAssets)?.let {
                mediaView.layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, it)
            }
            mediaViewLayout.visibility = View.VISIBLE
            this.sdkMediaView = mediaView
            clickableViews.add(mediaView)
        } else if (!DataUtil.isEmpty(nativeAssets.wideImageUrl)) {
            imageView.visibility = View.VISIBLE
            mediaViewLayout.visibility = View.GONE
            Image.load(nativeAssets.wideImageUrl).into(imageTarget)
        } else {
            mediaViewLayout.visibility = View.GONE
            imageView.visibility = View.GONE
        }

        // Remove any adchoicesView that was previously added.
        adChoicesView?.let {
            if (it.parent is ViewGroup) {
                (it.parent as ViewGroup).removeView(it)
            }
            adChoicesView = null
        }
        adChoicesView = nativeHelper?.addAdChoicesView(view as ViewGroup)

        viewBinding.setVariable(BR.item, nativeAssets)
        viewBinding.setVariable(BR.adEntity, baseAdEntity)
        viewBinding.executePendingBindings()

        nativeHelper?.registerViewForInteraction(view, clickableViews)
        baseAdEntity.adReportInfo = getAdReportInfo(nativeAssets)
    }

    private fun getAdReportInfo(nativeAssets: NativeData?): AdReportInfo? {
        nativeAssets ?: return null

        val adReportInfo = AdReportInfo()
        adReportInfo.adTitle = nativeAssets.title
        adReportInfo.adDescription = nativeAssets.body
        adReportInfo.advertiser = nativeAssets.advertiser
        adReportInfo.category = nativeAssets.category
        return adReportInfo
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (!baseAdEntity.isShown) {
            super.onCardView(baseAdEntity)
            nativeHelper?.recordImpression()
        }
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        super.onVisible(viewVisibilityPercentage, percentageOfScreen)
        if (adEntity?.isVideoAd == true) {
            videoVisiblePercentage = ViewUtils.getVisibilityPercentage(mediaViewLayout)
        }
    }

    override fun onInVisible() {
        videoVisiblePercentage = 0
    }

    private var imageTarget: Image.ImageTarget = object : Image.ImageTarget() {
        override fun onResourceReady(drawable: Any, transition: Transition<*>?) {
            if (drawable !is Drawable) {
                return
            }
            height = AdsUtil.getHeightWithAspectRatio(drawable.intrinsicWidth, drawable.intrinsicHeight,
                    AdConstants.ASPECT_RATIO_WIDE_ADS_DEFAULT,
                    AdsUtil.defaultWidthForWideAds,
                    if (adEntity is ExternalSdkAd) AdsUtil.minAspectRatioNative else -1f)
            imageView.layoutParams.height = height
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        onDestroy(nativeHelper)
        (viewBinding.root.parent as? ViewGroup)?.removeView(viewBinding.root)
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null
    }

    override val asset: Any?
        get() = adEntity

    override fun setAutoPlayManager(autoPlayManager: AutoPlayManager?) {
        this.autoPlayManager = autoPlayManager
    }

    override fun getAutoplayPriority(fresh: Boolean): Int {
        if (adEntity?.isVideoAd != true) {
            return -1
        }
        if (fresh) {
            videoVisiblePercentage = ViewUtils.getVisibilityPercentage(mediaViewLayout)
        }
        return if (videoVisiblePercentage >= autoplayVisibility)
            videoVisiblePercentage else -1
    }

    override fun getVisibilityPercentage(): Int {
        return videoVisiblePercentage
    }

    override fun getPositionInList(): Int {
        return adapterPosition
    }
}
