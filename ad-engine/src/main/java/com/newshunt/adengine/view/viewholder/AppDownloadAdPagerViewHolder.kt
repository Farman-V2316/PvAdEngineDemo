/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.databinding.NewsItemTypeNativeAdBinding
import com.newshunt.adengine.model.entity.AdReportInfo
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity.ItemTag
import com.newshunt.adengine.model.entity.NativeAdAppDownload
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdsOpenUtility.handleBrowserSelection
import com.newshunt.adengine.util.AdsUtil.Companion.getMergedTitle
import com.newshunt.adengine.util.AdsUtil.Companion.makeTitleUnBold
import com.newshunt.adengine.util.AdsUtil.Companion.setupAdBorder
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DeeplinkHelper
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.nhcommand.NHCommandMainHandler
import com.newshunt.dhutil.helper.theme.ThemeUtils.isNightMode
import com.newshunt.news.analytics.NhAnalyticsAppState

/**
 * Viewholder for individual ad in a carousel of adtype 'appDownload'
 *
 * @author raunak.yadav
 */
class AppDownloadAdPagerViewHolder(viewBinding: NewsItemTypeNativeAdBinding,
                                   uiComponentId: Int,
                                   lifecycleOwner: LifecycleOwner?) :
    AdsViewHolder(viewBinding, uiComponentId, lifecycleOwner) {

    private val view: View = viewBinding.root
    private val imageView: NHImageView = viewBinding.bannerImage
    private val titleView: TextView = viewBinding.bannerTitle
    private val subtitle: NHTextView = viewBinding.bannerSubtitle2
    private val bottomBanner: View = viewBinding.adBannerBottombar
    private val borderContainer: View = viewBinding.borderContainer
    private var parentActivity: Activity? = null
    private var lastClicked: Long = 0

    init {
        clearableImageViews.add(imageView)
    }

    override fun updateView(activity: Activity, baseAdEntity: BaseAdEntity) {
        parentActivity = activity
        val nativeAdAppDownload = baseAdEntity as NativeAdAppDownload
        val content = nativeAdAppDownload.content?:return
        updateView(nativeAdAppDownload)

        if (!content.itemImage?.data.isNullOrBlank()) {
            imageView.load(content.itemImage?.data).into(imageView, ImageView.ScaleType.CENTER_CROP)
            imageView.visibility = View.VISIBLE
        } else if (!content.iconLink.isNullOrBlank()) {
            imageView.load(content.iconLink).into(imageView, ImageView.ScaleType.FIT_CENTER)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.INVISIBLE
        }
        if(baseAdEntity.adPosition == AdPosition.STORY) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        }
        if (titleView.visibility == View.VISIBLE) {
            content.itemTitle?.data?.let {
                if (makeTitleUnBold(it)) {
                    titleView.setTypeface(titleView.typeface, Typeface.NORMAL)
                }
            }
            setItemTitle(titleView, content.itemTitle, content.itemDescription)
        }
        if (content.actionText.isNullOrBlank()) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.text = content.actionText
            subtitle.visibility = View.VISIBLE
        }
        setAdReportInfo(nativeAdAppDownload)
        view.setOnClickListener { handleClick(nativeAdAppDownload) }
        setAdReportInfo(nativeAdAppDownload)
    }

    private fun setItemTitle(textView: TextView, itemTitle: ItemTag?, description: String?) {
        if (description.isNullOrBlank() && itemTitle?.data.isNullOrBlank()) {
            textView.text = Constants.EMPTY_STRING
            return
        }
        var title = if (itemTitle == null) Constants.EMPTY_STRING else itemTitle.data
        if (!description.isNullOrBlank()) {
            title = getMergedTitle(title, description)
        }
        textView.text = title
        itemTitle?.let {
            val color = it.getThemeBasedTextColor(isNightMode())
            ViewUtils.getColor(color)?.let { colorInt ->
                textView.setTextColor(colorInt)
            }
        }
    }

    private fun handleClick(adEntity: NativeAdAppDownload) {
        if (adEntity.action.isNullOrBlank()) {
            return
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClicked < 1000) {
            return
        }
        lastClicked = currentTime
        adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
        adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(adEntity.adLPTimeSpentBeaconUrl)
        asyncAdImpressionReporter?.onClickEvent()

        NhAnalyticsAppState.getInstance()
            .setReferrer(NewsReferrer.AD)
            .setReferrerId(adEntity.id)
            .setEventAttribution(NewsReferrer.AD).eventAttributionId = adEntity.id
        val pageReferrer = PageReferrer(NewsReferrer.AD, adEntity.id)
        val url = adEntity.action
        val clickHandled = NHCommandMainHandler.getInstance()
            .handle(url, parentActivity, null, pageReferrer)
        if (!clickHandled && (!CommonUtils.isPlayStoreUrl(url) ||
                    !AndroidUtils.openPlayStoreForApp(parentActivity, url))) {
            if (DeeplinkHelper.isInternalDeeplinkUrl(url)) {
                CommonNavigator.launchDeeplink(parentActivity, url, null)
            } else {
                handleBrowserSelection(parentActivity, adEntity.action, adEntity)
            }
        }
    }

    private fun setAdReportInfo(adEntity: NativeAdAppDownload) {
        adEntity.content?.let { content ->
            val adReportInfo = AdReportInfo()
            adReportInfo.adTitle = content.itemTitle?.data
            adReportInfo.adDescription = content.itemDescription
            adEntity.adReportInfo = adReportInfo
        }
    }

    override fun onDestroy() {
        view.setOnClickListener(null)
    }
}