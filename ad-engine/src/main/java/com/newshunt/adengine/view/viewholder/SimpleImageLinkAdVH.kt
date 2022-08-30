/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.BR
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.NativeAdImageLink
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.NHRoundedCornerImageView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.helper.nhcommand.NHCommandMainHandler
import com.newshunt.news.analytics.NhAnalyticsAppState

/**
 * View takes care of rendering just the image for a [NativeAdImageLink] Ad.
 *
 * @author raunak.yadav
 */
abstract class SimpleImageLinkAdVH(private val viewBinding: ViewDataBinding,
                                   uniqueRequestId: Int, parentLifecycleOwner: LifecycleOwner?) :
    AdsViewHolder(viewBinding, uniqueRequestId, parentLifecycleOwner) {

    protected var borderContainer: ViewGroup? = null
    private lateinit var imageView: NHRoundedCornerImageView
    private var parentActivity: Activity? = null
    private var view: View = viewBinding.root

    init {
        viewBinding.root.visibility = View.GONE
    }

    override fun updateView(parentActivity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is NativeAdImageLink) return

        this.view.visibility = View.VISIBLE
        imageView = getImageView()
        imageView.setImageDrawable(null)
        this.parentActivity = parentActivity

        super.updateView(baseAdEntity, true)
        setImageHeight(baseAdEntity)

        AdsUtil.setUpAdContainerBackground(baseAdEntity, borderContainer)
        addOnClickListener(baseAdEntity)

        viewBinding.setVariable(BR.adEntity, baseAdEntity)
        viewBinding.executePendingBindings()
    }

    abstract fun getImageView(): NHRoundedCornerImageView

    open fun setImageHeight(adEntity: NativeAdImageLink) {}

    abstract fun getLogTag(): String

    private fun addOnClickListener(nativeAdImageLink: NativeAdImageLink) {
        view.setOnClickListener {
            onAdClicked(nativeAdImageLink)
        }
    }

    open fun onAdClicked(nativeAdImageLink: NativeAdImageLink) {
        val asyncAdImpressionReporter = AsyncAdImpressionReporter(nativeAdImageLink)
        asyncAdImpressionReporter.onClickEvent()
        if (!DataUtil.isEmpty(nativeAdImageLink.action)) {
            NhAnalyticsAppState.getInstance()
                .setReferrer(NewsReferrer.AD)
                .setReferrerId(nativeAdImageLink.id)
                .setEventAttribution(NewsReferrer.AD).eventAttributionId = nativeAdImageLink.id

            val pageReferrer = PageReferrer(NewsReferrer.AD, nativeAdImageLink.id)
            if (NHCommandMainHandler.getInstance().handle(
                    nativeAdImageLink.action, parentActivity, null, pageReferrer)) {
                return
            }

            try {
                adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
                adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(nativeAdImageLink.adLPTimeSpentBeaconUrl)
                AdsOpenUtility.handleBrowserSelection(parentActivity, nativeAdImageLink.action,
                    nativeAdImageLink)
            } catch (e: Exception) {
                Logger.e(getLogTag(), e.toString())
            }
        }
    }

    override fun onDestroy() {
        onDestroy(null)
    }

}
