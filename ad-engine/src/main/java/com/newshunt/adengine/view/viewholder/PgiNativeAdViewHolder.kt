/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.PgiArticleAd
import com.newshunt.adengine.model.entity.omsdk.OMTrackType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.OMSdkHelper
import com.newshunt.adengine.util.setupPgiIconPosition
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdBindUtils
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NhWebViewClient
import com.newshunt.common.helper.font.HtmlFontHelper
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.helper.nhcommand.NHCommandMainHandler
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.util.NewsConstants
import com.newshunt.sdk.network.image.Image
import java.net.URLEncoder

/**
 * This view holder holds view to show pgi native ad.
 *
 * @author raunak.yadav
 */
class PgiNativeAdViewHolder(private val viewBinding: ViewDataBinding, lifecycleOwner: LifecycleOwner)
    : AdsViewHolder(viewBinding, -1, lifecycleOwner), UpdateableAdView, View.OnClickListener {

    private val view = viewBinding.root
    private val pgiNativeAdDetailImage: ImageView
    private val pgiNativeAdWebView: WebView
    private val actionBtn: NHTextView
    private val shareIconTop: ImageView
    private val shareIconBottom: ImageView
    private var activity: Activity? = null
    private var pgiArticleAd: PgiArticleAd? = null
    private var needsWebTracking: Boolean = false


    private val adInViewJavaScriptUrl: String
        get() = "javascript:(function () { " +
                "onAdInView();" + "})()"

    private val adOutOfViewJavaScriptUrl: String
        get() = "javascript:(function () { " +
                "onAdOutOfView();" + "})()"

    init {
        pgiNativeAdDetailImage = view.findViewById(R.id.ad_image)
        pgiNativeAdWebView = view.findViewById(R.id.pgi_ad_details_webview)
        actionBtn = view.findViewById(R.id.cta_button)
        shareIconTop = view.findViewById(R.id.share_icon_top)
        shareIconBottom = view.findViewById(R.id.share_icon_bottom)
        viewBinding.lifecycleOwner = lifecycleOwner
        initializeWebView(pgiNativeAdWebView)
        setWebViewClient()
    }

    private fun initializeWebView(webView: WebView) {
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.useWideViewPort = false
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.setSupportZoom(false)
    }

    private fun setWebViewClient() {
        val objNewsDetailWebViewClient = NewsDetailWebViewClient()
        pgiNativeAdWebView.webViewClient = objNewsDetailWebViewClient
    }

    override fun updateView(activity: Activity, baseDisplayAdEntity: BaseAdEntity?) {
        if (baseDisplayAdEntity !is PgiArticleAd) {
            return
        }
        this.activity = activity
        pgiArticleAd = baseDisplayAdEntity
        val content = baseDisplayAdEntity.content ?: return

        val omTrackType = baseDisplayAdEntity.omTrackType
        needsWebTracking = omTrackType == OMTrackType.WEB || omTrackType == OMTrackType.WEB_VIDEO
        super.updateView(baseDisplayAdEntity, !needsWebTracking)
        adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
        val nativeAssets = NativeAdBannerViewHelper(baseDisplayAdEntity, activity, adsTimeSpentOnLPHelper).getNativeAssets()
        val parentLayout = view.findViewById<View>(R.id.ad_container)
        parentLayout.setOnClickListener(this)
        actionBtn.setOnClickListener(this)

        showNativePgiAdImage(content.itemImage)
        //If image is not available margin is not required on html body description
        val isImageNotAvalable = DataUtil.isEmpty(content.itemImage?.data)
        showAdDescription(content.htmlBody, isImageNotAvalable, omTrackType)

        baseDisplayAdEntity.adReportInfo = AdsUtil.getAdReportInfo(nativeAssets)

        if (AdBindUtils.isShareSupported(pgiArticleAd)) {
            setupPgiIconPosition(actionBtn, shareIconBottom, nativeAssets.ctaText.isNullOrBlank())
        }

        viewBinding.setVariable(BR.category, nativeAssets.sponsoredText)
        viewBinding.setVariable(BR.adEntity, baseDisplayAdEntity)
        viewBinding.setVariable(BR.item, nativeAssets)
        viewBinding.executePendingBindings()
    }

    private fun showNativePgiAdImage(itemImage: BaseDisplayAdEntity.ItemImage?) {
        itemImage ?: return

        if (pgiArticleAd?.showOnlyImage == true) {
            (pgiNativeAdDetailImage.parent as? View)?.let {parent ->
                val params = pgiNativeAdDetailImage.layoutParams as? ConstraintLayout.LayoutParams
                params?.bottomToBottom = parent.id
                params?.height = 0
            }
        } else {
            pgiNativeAdDetailImage.layoutParams.height = AdsUtil.getIntValue(itemImage.height, 0)
            pgiNativeAdDetailImage.layoutParams.width = AdsUtil.getIntValue(itemImage.width, 0)
        }
        if (CommonUtils.isEmpty(itemImage.data)) {
            pgiNativeAdDetailImage.setBackgroundResource(com.newshunt.dhutil.R.drawable.default_news_img)
        } else {
            Image.load(itemImage.data).placeHolder(com.newshunt.common.util.R.color.empty_image_color)
                    .into(pgiNativeAdDetailImage, ImageView.ScaleType.CENTER_CROP)
        }
        pgiNativeAdDetailImage.visibility = View.VISIBLE
    }

    private fun showAdDescription(description: String?, showFullScreenHtml: Boolean,
                                  omTrackType: OMTrackType?) {
        var description = description
        if (DataUtil.isEmpty(description)) {
            pgiNativeAdWebView.visibility = View.GONE
        } else {
            if (showFullScreenHtml) {
                val params = pgiNativeAdWebView.layoutParams as ConstraintLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                pgiNativeAdWebView.layoutParams = params
            }
            pgiNativeAdWebView.visibility = View.VISIBLE

            // Inject OM sdk's js for tracking if applicable.
            if (omTrackType != null && OMSdkHelper.isOMSdkEnabled) {
                description = OMSdkHelper.injectOMJSInCreative(description, omTrackType)
            }
            pgiNativeAdWebView.loadDataWithBaseURL(pgiArticleAd?.contentBaseUrl,
                    HtmlFontHelper.wrapToFontHTML(description, AdsUtil.isUrdu(pgiArticleAd),
                            ThemeUtils.isNightMode()),
                    NewsConstants.HTML_MIME_TYPE, NewsConstants.HTML_UTF_ENCODING, null)
        }
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (pgiArticleAd?.isShown == false) {
            super.onCardView(baseAdEntity)
            PgiAdHandler.reset(activity)
        }
    }

    private fun handleClickEvent(url: String?) {
        if (pgiArticleAd == null) {
            return
        }
        asyncAdImpressionReporter?.onClickEvent()

        if (DataUtil.isEmpty(url)) {
            return
        }

        NhAnalyticsAppState.getInstance()
                .setReferrer(NewsReferrer.AD)
                .setReferrerId(pgiArticleAd?.aduid)
                .setEventAttribution(NewsReferrer.AD).eventAttributionId = pgiArticleAd?.aduid

        val pageReferrer = PageReferrer(NewsReferrer.AD, pgiArticleAd?.aduid)
        if (NHCommandMainHandler.getInstance().handle(url, activity, null, pageReferrer)) {
            return
        }
        adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(pgiArticleAd?.adLPTimeSpentBeaconUrl)
        handleTrackerTag(url)
    }

    private fun handleTrackerTag(url: String?) {
        var url = url

        val trackerTag = pgiArticleAd?.tracker
        if (trackerTag == null) {
            AdsOpenUtility.handleBrowserSelection(activity, url, pgiArticleAd)
            return
        }

        try {
            if ("true".equals(trackerTag.redirectWebUrl, ignoreCase = true)
                    && trackerTag.data != null) {
                url = trackerTag.data + URLEncoder.encode(url, "utf-8")
            } else {
                asyncAdImpressionReporter?.hitTrackerUrl(true, trackerTag.data)
            }
            AdsOpenUtility.handleBrowserSelection(activity, url, pgiArticleAd)
        } catch (ex: Exception) {
            Logger.e(LOG_TAG, ex.message)
        }
    }

    override fun onClick(v: View) {
        pgiArticleAd?.action?.let {
            handleClickEvent(it)
        }
    }

    /**
     * This is a custom class to track if any link inside the page is clicked.
     */
    private inner class NewsDetailWebViewClient : NhWebViewClient() {

        override fun onPageLoaded(view: WebView, url: String) {
            view.clearHistory()
            registerOMAdSession(view)
            asyncAdImpressionReporter?.onAdInflated()
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            handleClickEvent(url)
            return true
        }
    }

    /**
     * Create the OM ad session and register the view for tracking.
     */
    private fun registerOMAdSession(webView: WebView?) {
        webView ?: return
        startTrackingOnAdLoad(webView)
    }

    override fun onDestroy() {
        onDestroy(null)
        activity = null
        AndroidUtils.getMainThreadHandler().postDelayed({
            pgiNativeAdWebView.removeAllViews()
            pgiNativeAdWebView.destroy()
        }, (if (needsWebTracking) AdConstants.OMID_WEBVIEW_DESTROY_DELAY else 0).toLong())
    }

    fun onAdViewVisibilityChange(isVisible: Boolean) {
        val javaScriptFunctionCall = if (isVisible) {
            adInViewJavaScriptUrl
        } else {
            adOutOfViewJavaScriptUrl
        }
        pgiNativeAdWebView.loadUrl(javaScriptFunctionCall)
    }

    companion object {
        private const val LOG_TAG = "PgiNativeAdViewHolder"
    }
}
