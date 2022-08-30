/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdsShareViewHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.setupPgiIconPosition
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdBindUtils
import com.newshunt.adengine.view.helper.DfpNativeViewHelper
import com.newshunt.adengine.view.helper.FacebookAdViewHelper
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.view.customview.NHWrappedHeightLayout
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.image.Image

/**
 * ViewHolder to display Native PGI ads from external SDKs.
 *
 * @author raunak.yadav
 */
open class ExternalNativePGIViewHolder(private val viewBinding: ViewDataBinding, lifecycleOwner: LifecycleOwner)
    : RecyclerView.ViewHolder(viewBinding.root), UpdateableAdView {

    private val view: View = viewBinding.root
    private val ctaButton: NHTextView
    private val mediaViewLayout: NHWrappedHeightLayout?
    private val fullImageView: ImageView
    private val shareIconTop: ImageView
    private val shareIconBottom: ImageView
    private var adsShareViewHelper: AdsShareViewHelper? = null

    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null
    private var nativeHelper: NativeViewHelper? = null
    private var externalSdkAd: ExternalSdkAd? = null
    private var activity: Activity? = null
    private var adChoicesView: View? = null
    private val clickableList: MutableList<View>

    private val maxHeightNative = (CommonUtils.getDeviceScreenWidth() / AdsUtil.minAspectRatioNative).toInt()
    private val maxHeightVideo = (CommonUtils.getDeviceScreenWidth() / AdsUtil.minAspectRatioVideo).toInt()

    init {
        this.view.visibility = View.GONE

        val scrollView = view.findViewById<View>(R.id.pgi_detail_scrollview)
        val headlineView = view.findViewById<View>(R.id.ad_title)
        val categoryNameTextView = view.findViewById<View>(R.id.ad_attr)
        val pgiNativeAdDetail = view.findViewById<View>(R.id.ad_body)
        ctaButton = view.findViewById<View>(R.id.cta_button) as NHTextView
        mediaViewLayout = view.findViewById(R.id.mediaView)
        fullImageView = view.findViewById<View>(R.id.ad_image) as ImageView
        val iconView = view.findViewById<View>(R.id.ad_icon)
        val shortInfo = view.findViewById<View>(R.id.short_info)
        shareIconTop = view.findViewById(R.id.share_icon_top)
        shareIconBottom = view.findViewById(R.id.share_icon_bottom)

        clickableList = mutableListOf(scrollView, headlineView, iconView, categoryNameTextView,
                pgiNativeAdDetail, ctaButton, shortInfo, view)
        viewBinding.lifecycleOwner = lifecycleOwner
    }

    override fun updateView(activity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is ExternalSdkAd) {
            return
        }
        this.activity = activity
        externalSdkAd = baseAdEntity
        nativeHelper = getExternalNativeViewHelper(baseAdEntity)
        nativeHelper ?: return

        view.visibility = View.VISIBLE
        val nativeAssets = nativeHelper?.getNativeAssets() ?: return

        // Remove any adchoicesView that was previously added.
        val parent = adChoicesView?.parent as? ViewGroup
        parent?.removeView(adChoicesView)
        adChoicesView = nativeHelper?.addAdChoicesView(view as ViewGroup)

        val category = if (!DataUtil.isEmpty(nativeAssets.category)) {
            nativeAssets.category
        } else if (!DataUtil.isEmpty(nativeAssets.sponsoredText)) {
            nativeAssets.sponsoredText
        } else null

        mediaViewLayout?.removeAllViews()

        val mediaView = nativeHelper?.getMediaViewIfApplicable(mediaViewLayout!!)
        if (mediaView != null) {
            fullImageView.visibility = View.GONE

            if (baseAdEntity.showOnlyImage != true) {
                mediaViewLayout?.maxHeight = if (baseAdEntity.isVideoAd) maxHeightVideo else maxHeightNative
                nativeHelper?.getPreferredHeightMediaView(nativeAssets)?.let {
                    mediaView.layoutParams = RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, it)
                    mediaViewLayout?.maxHeight = it
                }
            } else {
                (mediaViewLayout?.parent as? View)?.let {
                    val params = mediaViewLayout.layoutParams as? ConstraintLayout.LayoutParams
                    params?.bottomToBottom = it.id
                    params?.height = 0
                    mediaView.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT)
                }
            }
            mediaViewLayout?.visibility = View.VISIBLE
            clickableList.add(mediaView)
            clickableList.add(mediaViewLayout!!)
        } else {
            mediaViewLayout?.visibility = View.GONE
            fullImageView.visibility = View.VISIBLE
            if (!DataUtil.isEmpty(nativeAssets.wideImageUrl)) {
                Image.load(nativeAssets.wideImageUrl).placeHolder(com.newshunt.dhutil.R.drawable.default_news_img).into(fullImageView)
            } else {
                fullImageView.layoutParams.height = AdsUtil.getActionBarHeight(activity)
            }
            clickableList.add(fullImageView)
        }

        adsShareViewHelper = AdsShareViewHelper(view.context)
        if (AdBindUtils.isShareSupported(externalSdkAd)) {
            setupPgiIconPosition(ctaButton, shareIconBottom, ctaButton.visibility == View.GONE)
        }

        viewBinding.setVariable(BR.category, category)
        viewBinding.setVariable(BR.adEntity, baseAdEntity)
        viewBinding.setVariable(BR.item, nativeAssets)
        viewBinding.setVariable(BR.adsShareViewHelper, adsShareViewHelper)
        viewBinding.executePendingBindings()

        nativeHelper?.registerViewForInteraction(view, clickableList)

        externalSdkAd?.adReportInfo = AdsUtil.getAdReportInfo(nativeAssets)
        asyncAdImpressionReporter = AsyncAdImpressionReporter(baseAdEntity)
        asyncAdImpressionReporter?.onAdInflated()
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (!baseAdEntity.isShown) {
            baseAdEntity.notifyObservers()

            AdFrequencyStats.onAdViewed(baseAdEntity)
            asyncAdImpressionReporter?.onCardView()
            baseAdEntity.isShown = true
            if (AdsUtil.isExternalSdkNativePgiAd(baseAdEntity)) {
                PgiAdHandler.reset(activity)
            }
            nativeHelper?.recordImpression()
        }
    }

    override fun getAdEntity(): BaseAdEntity? {
        return externalSdkAd
    }

    override fun onDestroy() {
        nativeHelper?.destroy(-1)
        (viewBinding.root.parent as? ViewGroup)?.removeView(viewBinding.root)
    }

    private fun getExternalNativeViewHelper(externalSdkAd: ExternalSdkAd): NativeViewHelper? {
        externalSdkAd.external?.data ?: return null

        if (externalSdkAd.external?.data?.startsWith(AdConstants.FB_AD) == true) {
            return FacebookAdViewHelper(externalSdkAd, activity)
        }
        return if (externalSdkAd.external?.data?.startsWith(AdConstants.DFP_AD) == true) {
            return DfpNativeViewHelper(externalSdkAd, activity)
        } else null
    }
}
