/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.model.entity.PgiArticleAd
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.helper.nhcommand.NHCommandMainHandler
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * Represents data to be shown in banner type ad.
 *
 * @author heena.arora
 */
class NativeAdBannerViewHelper(private val nativeAd: BaseDisplayAdEntity,
                               private val activity: Activity,
                               private val adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?) : NativeViewHelper {

    private val asyncAdImpressionReporter: AsyncAdImpressionReporter = AsyncAdImpressionReporter(nativeAd)

    private val clickListener = View.OnClickListener {
        nativeAd.action ?: return@OnClickListener

        asyncAdImpressionReporter.onClickEvent()
        val pageReferrer = PageReferrer(NewsReferrer.AD, nativeAd.id)
        if (NHCommandMainHandler.getInstance()
                        .handle(nativeAd.action, activity, null, pageReferrer)) {
            return@OnClickListener
        }
        try {
            adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(nativeAd.adLPTimeSpentBeaconUrl)
            AdsOpenUtility.handleBrowserSelection(activity, nativeAd.action, nativeAd)
        } catch (e: Exception) {
            Logger.d(LOG_TAG, e.toString())
        }
    }

    override fun getNativeAssets(): NativeData {

        val content = nativeAd.content
        val nativeAssets = NativeData()
        content ?: return nativeAssets

        nativeAssets.title = content.itemTitle?.data
        nativeAssets.titleColor = content.itemTitle?.getThemeBasedTextColor(ThemeUtils.isNightMode())

        nativeAssets.body = content.itemSubtitle1?.data

        if (!DataUtil.isEmpty(content.sourceAlphabet)) {
            nativeAssets.sourceAlphabet = content.sourceAlphabet
        }

        nativeAssets.iconUrl = content.iconLink
        nativeAssets.wideImageUrl = if (nativeAd is PgiArticleAd) content.itemImage?.data
        else content.iconLink

        val ctaText = content.itemSubtitle2?.data ?: Constants.EMPTY_STRING
        nativeAssets.ctaText = ctaText
        //For native banner ads, cta text will be treated as advertiser for reporting.
        if (ctaText.isNotBlank())
            nativeAssets.advertiser = ctaText

        nativeAssets.sponsoredText = content.itemTag?.data ?: Constants.EMPTY_STRING
        nativeAssets.backgroundColor = content.getThemeBasedBgColor(ThemeUtils.isNightMode())
        nativeAssets.showPlayIcon = nativeAd.showPlayIcon ?: false
        return nativeAssets
    }

    override fun addAdChoicesView(adContainer: ViewGroup): View? {
        return null
    }

    override fun getMediaViewIfApplicable(mediaViewLayout: RelativeLayout): View? {
        return null
    }

    override fun registerViewForInteraction(view: View, clickableViews: List<View>) {
        view.setOnClickListener(clickListener)
        if (clickableViews.isNotEmpty()) {
            for (childView in clickableViews) {
//                childView.setOnClickListener(clickListener)
            }
        }
    }

    override fun destroy(parentId: Int, view: View?) {
        //No cleanup in case of direct ads.
    }
}

private const val LOG_TAG = "NativeAdBannerViewHelper"
