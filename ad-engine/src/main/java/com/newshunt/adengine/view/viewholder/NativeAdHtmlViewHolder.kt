/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.content.ContextWrapper
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.MASTAdView.MASTAdConstants
import com.MASTAdView.MASTAdDelegate
import com.MASTAdView.MASTAdView
import com.MASTAdView.core.MraidInterface
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.databinding.LayoutHtmlFullPageAdBinding
import com.newshunt.adengine.databinding.NewsItemTypeHtmlAdBinding
import com.newshunt.adengine.listeners.AdExitListener
import com.newshunt.adengine.listeners.InteractiveAdListener
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.NativeAdHtml
import com.newshunt.adengine.model.entity.omsdk.OMTrackType
import com.newshunt.adengine.model.entity.version.AdLPBackAction
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.other.news.helper.NHWebViewJSInterface
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.OMSdkHelper
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.view.customview.NHRoundedFrameLayout
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.BrowserType
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.nhcommand.NHCommandMainHandler
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.helper.player.PlaySettingsListener
import com.newshunt.helper.player.PlayerControlHelper.isListMuteMode
import com.newshunt.helper.player.PlayerControlHelper.toggleMute
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.util.NewsConstants
import com.newshunt.viral.utils.visibility_utils.ScrollAwareViewHolder
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.Timer
import java.util.TimerTask

/**
 * Represents data to be shown in html type ad.
 *
 * @author Mukesh Yadav
 */
private const val JAVASCRIPT_METHOD_PREFIX: String = "CustomMastAdJSInterface"

class NativeAdHtmlViewHolder(val viewBinding: ViewDataBinding,
                             uniqueRequestId: Int = -1,
                             private val lifecycleOwner: LifecycleOwner? = null,
                             private val adInteractionListener: InteractiveAdListener? = null,
                             private val swipeableHTMLAdInteractionListener:
                             SwipeableHTMLAdInteractionListener? = null,
                             private val webViewProvider: CachedWebViewProvider? = null,
                             private val parentHeight: Int? = null,
                             private val adExitListener: AdExitListener? = null) :
        AdsViewHolder(viewBinding, uniqueRequestId, lifecycleOwner), MASTAdDelegate.AdActivityEventHandler,
        MASTAdDelegate.RichmediaEventHandler, MASTAdDelegate.AdDownloadEventHandler,
        AutoPlayable, PlaySettingsListener, LifecycleObserver, ScrollAwareViewHolder {
    private val autoplayVisibility: Int
    private var mastAdView: MASTAdView? = null
    private var isInterstitial: Boolean = false
    private var isPopupInterstitial: Boolean = false
    private var isPopupInterstitialShown: Boolean = false
    private var relativeLayout: RelativeLayout? = null
    private var bottomBanner: View? = null
    private var bottomBrandBanner: View? = null
    private var borderContainer: NHRoundedFrameLayout? = null
    private var adContainer: View? = null
    private var nativeAdHtml: NativeAdHtml? = null
    private var parentActivity: Activity? = null
    private var canShowInteractiveAd: Boolean = false
    private var isAdLoaded: Boolean = false
    private var visiblePercentage: Int = 0
    private var autoPlayManager: AutoPlayManager? = null
    private var isVideoAd: Boolean = false
    private var visible: Boolean = false
    private var adState: String? = null
    private var isVideoPausedByUser: Boolean = false

    private val isFullWidthAd: Boolean
        get() = AdsUtil.isFullScreenAd(nativeAdHtml) || nativeAdHtml?.interactiveAd == true ||
                isInterstitial || isPopupInterstitial

    val isUpdateNeeded: Boolean
        get() = mastAdView == null

    private val adInViewJavaScriptUrl: String
        get() = AdConstants.SCRIPT_AD_IN_VIEW

    private val adOutOfViewJavaScriptUrl: String
        get() = AdConstants.SCRIPT_AD_OUT_VIEW

    // Autoplayable overrides
    override val asset: Any?
        get() = nativeAdHtml

    init {
        this.viewBinding.root.visibility = View.GONE
        if (viewBinding is NewsItemTypeHtmlAdBinding) {
            //bottomBanner = viewBinding.adBannerBottombar.adBannerBottombar
            adContainer = viewBinding.adDefaultContainer
            borderContainer = viewBinding.borderContainer
            relativeLayout = viewBinding.htmlAdLayout
           // bottomBrandBanner = viewBinding.adBannerBrandBottombarOsv.adOsvBottombar
        } else if (viewBinding is LayoutHtmlFullPageAdBinding) {
            relativeLayout = viewBinding.htmlAdLayout
        }

        viewBinding.lifecycleOwner = lifecycleOwner

        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        autoplayVisibility = adsUpgradeInfo?.minVisibilityForAutoplay
                ?: AdConstants.MIN_VISIBILITY_AUTOPLAY_VIDEO
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun updateView(parentActivity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is NativeAdHtml) {
            return
        }
        nativeAdHtml = baseAdEntity
        visible = false

        viewBinding.root.visibility = View.VISIBLE
        this.parentActivity = parentActivity

        if (baseAdEntity.uniqueAdIdentifier != nativeAdHtml?.uniqueAdIdentifier) {
            isVideoPausedByUser = false
            adState = null
        }
        nativeAdHtml?.let {
            super.updateView(it,false)
        }

        isVideoAd = nativeAdHtml?.coolAd?.isVideoAd == true
        isInterstitial = nativeAdHtml?.adPosition == AdPosition.PGI
        isPopupInterstitial = isInterstitial && PgiAdsConfig.HTMLPgiDisplayType.POPUP ==
                nativeAdHtml?.interstitialDisplayType
        setupMastAdview()
        nativeAdHtml?.let {
            setMastAdViewDimensions(it)
        }

        if (nativeAdHtml?.showBorder == true) {
            val containerBackgroundColor = nativeAdHtml?.containerBackgroundColor
            if (containerBackgroundColor != null) {
                borderContainer?.fillColor(ViewUtils.getColor(containerBackgroundColor, Color
                        .TRANSPARENT))
            } else {
                borderContainer?.fillColor(
                        ThemeUtils.getThemeColorByAttribute(borderContainer?.context,
                                com.newshunt.dhutil.R.attr.ads_container_background_color))
            }
        } else {
            adContainer?.apply {
                nativeAdHtml?.let {
                    this.background = AdsUtil.setUpAdContainerBackgroundWithBorder(nativeAdHtml, this)
                }
            }
        }

        val browserType = nativeAdHtml?.useInternalBrowser
        mastAdView?.useInternalBrowser = BrowserType.NH_BROWSER == browserType
        val parent = mastAdView?.parent as? ViewGroup
        parent?.removeView(mastAdView)
        relativeLayout?.removeAllViews()
        //no full bleed
        relativeLayout?.addView(mastAdView)
        val html5WebView = mastAdView?.adWebView?.html5WebView
        html5WebView?.settings?.useWideViewPort = nativeAdHtml?.useWideViewPort == true
        html5WebView?.addJavascriptInterface(CustomMastAdJSInterface(canShowInteractiveAd), JAVASCRIPT_METHOD_PREFIX)
        val activity =  if (itemView.context is Activity) {
            itemView.context as Activity
        } else if (itemView.context is ContextWrapper) {
            (itemView.context as ContextWrapper).baseContext as Activity
        } else {
            return
        }
        mastAdView?.let {
            html5WebView?.addJavascriptInterface(NHWebViewJSInterface(it.adWebView.html5WebView,
                    activity), NHWebViewJSInterface.INTERFACE_NAME)
        }
        // Inject OM sdk's js for tracking if applicable.
        if (OMSdkHelper.isOMSdkEnabled && nativeAdHtml?.omTrackType == OMTrackType.WEB ||
                nativeAdHtml?.omTrackType == OMTrackType.WEB_VIDEO) {
            nativeAdHtml?.mastAdViewData?.richContent =
                    OMSdkHelper.injectOMJSInCreative(nativeAdHtml?.mastAdViewData?.richContent,
                            nativeAdHtml?.omTrackType)
            mastAdView?.setOmTrackingEnabled(true)
        }
        mastAdView?.setOfflineAdData(nativeAdHtml?.mastAdViewData)
        mastAdView?.setCurrentLocation()
        mastAdView?.setActivityContext(parentActivity, NewsConstants.MRAID_PLACEMENT_PAGE)
        mastAdView?.adDelegate?.adActivityEventHandler = this
        mastAdView?.adDelegate?.richmediaEventHandler = this
        mastAdView?.adDelegate?.adDownloadHandler = this
        mastAdView?.let { mastAdView ->
           // bottomBanner?.setOnClickListener { onAdClicked(mastAdView, baseAdEntity.action) }
           // bottomBrandBanner?.setOnClickListener{ onAdClicked(mastAdView, baseAdEntity.action) }
        }
        viewBinding.setVariable(BR.adEntity, nativeAdHtml)
        viewBinding.executePendingBindings()
        translate()
    }

    private fun setAdSize(width: Int, height: Int) {
        if (nativeAdHtml?.interactiveAd == true) {
            // htmlAdLayout has height 'wrap_content' in xml. Change it to the height of window needed for parallax.
            if (viewBinding is NewsItemTypeHtmlAdBinding) {
                viewBinding.htmlAdLayout.layoutParams = viewBinding.htmlAdLayout.layoutParams.also {
                    it.height = height
                }
            }
            mastAdView?.layoutParams = RelativeLayout.LayoutParams(CommonUtils.getDeviceScreenWidth(),
                    parentHeight ?: CommonUtils.getRealScreenHeight(parentActivity)).also {
                    it.marginEnd = -1 * CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.ad_content_margin)
                    it.marginStart = -1 * CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.ad_content_margin)
                }
        } else {
            mastAdView?.layoutParams = RelativeLayout.LayoutParams(width, height)
        }
    }

    private fun setupMastAdview() {
        //Try fetching a cached webview for this particular Ad instance
        nativeAdHtml?.uniqueAdIdentifier?.let {
            val weakCachedView = webViewProvider?.getWebView(it)
            val cachedView = weakCachedView?.get()
            if (cachedView != null) {
                Logger.d(LOG_TAG, "Reusing a cached MASTAdView for " + nativeAdHtml?.uniqueAdIdentifier)
                mastAdView = cachedView
            } else {
                Logger.d(LOG_TAG,
                        "Creating a new instance of MASTAdView " + nativeAdHtml?.uniqueAdIdentifier)
                //make bg color transparent for splash ad
                val isBgColorTransparent = nativeAdHtml?.adPosition == AdPosition.SPLASH
                mastAdView = MASTAdView(parentActivity, 0, 0, null, isPopupInterstitial, isBgColorTransparent)
                mastAdView?.adWebView?.settings?.javaScriptEnabled = true
                //Add this MASTAdView to cache
                if (!CommonUtils.isEmpty(nativeAdHtml?.uniqueAdIdentifier)) {
                    webViewProvider?.putWebView(it,
                            WeakReference<MASTAdView>(mastAdView))
                }
            }
        }
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        val isAdShown = nativeAdHtml?.isShown == true
        super.onCardView(baseAdEntity)

        if (!isAdShown) {
            if (nativeAdHtml?.coolAd?.actmethod != null) {
                if ("autoload".equals(nativeAdHtml?.coolAd?.actmethod, ignoreCase = true)) {
                    startDelayedExpandTimer(nativeAdHtml)
                }
            }
            if (isInterstitial && !isPopupInterstitial) {
                PgiAdHandler.reset(parentActivity)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        lifecycleOwner?.lifecycle?.removeObserver(this)
        onViewStopped()
        onDestroy(null)
        // Need to keep reference to webview for atleast 1 sec to allow js
        // to trigger sessionFinish event.
        if (mastAdView != null) {
            AndroidUtils.getMainThreadHandler()
                    .postDelayed({ mastAdView?.destroy() },
                            (if (mastAdView?.isOMTrackingEnabled == true) AdConstants
                                    .OMID_WEBVIEW_DESTROY_DELAY else 0).toLong())
        }
    }

    fun refresh() {
        viewBinding.root.invalidate()
        mastAdView?.adWebView?.invalidate()
        mastAdView?.refreshDrawableState()
    }

    private fun startDelayedExpandTimer(nativeAdHtml: NativeAdHtml?) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                mastAdView?.adWebView?.javascriptInterface?.expand("")
            }
        }, (nativeAdHtml?.coolAd?.autoexpandTimer ?: 0) * NewsConstants.MRAID_AUTO_EXPAND_TIME
                .toLong())
    }

    override fun onAdAttachedToActivity(sender: MASTAdView) {
        //Nothing to do
    }

    override fun onAdDetachedFromActivity(sender: MASTAdView) {
        //Nothing to do
    }

    private fun stopVideo() {
        mastAdView?.adWebView?.mraidInterface?.stopVideo()
    }

    override fun onAdClicked(sender: MASTAdView, url: String?): Boolean {
        url ?: return false
        val pageReferrer = PageReferrer(
                NewsReferrer.AD, nativeAdHtml?.id)

        NhAnalyticsAppState.getInstance()
                .setReferrer(NewsReferrer.AD)
                .setReferrerId(nativeAdHtml?.id)
                .setEventAttribution(NewsReferrer.AD).eventAttributionId = nativeAdHtml?.id
        adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
        adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(nativeAdHtml?.adLPTimeSpentBeaconUrl)
        if (isInterstitial) {
            asyncAdImpressionReporter?.onClickEvent()
        }
        if (NHCommandMainHandler.getInstance().handle(
                        url, parentActivity, null, pageReferrer)) {
            return true
        }
        try {
            if (isPopupInterstitial) {
                mastAdView?.closeInterstitial()
            }

            if (nativeAdHtml?.adPosition == AdPosition.EXIT_SPLASH) {
                when (nativeAdHtml?.backFromLpAction) {
                    AdLPBackAction.EXIT_APP -> adExitListener?.closeToExitApp(nativeAdHtml, AdInteraction.USER_CLICK)
                    AdLPBackAction.BACK_TO_APP -> adExitListener?.cancelExitApp(AdInteraction.USER_CLICK)
                }
            }

            val trackerTag = nativeAdHtml?.coolAd?.tracker
            if (trackerTag == null) {
                AdsOpenUtility.handleBrowserSelection(parentActivity, url, nativeAdHtml)
                return true
            }

            var destinationUrl = url
            if (trackerTag.redirectWebUrl && trackerTag.data != null) {
                destinationUrl = trackerTag.data?.replace(NewsConstants.HTML_AD_MICRO, URLEncoder.encode(destinationUrl, "utf-8"))
            } else {
                asyncAdImpressionReporter?.hitTrackerUrl(true, trackerTag.data)
            }
            AdsOpenUtility.handleBrowserSelection(parentActivity, destinationUrl, nativeAdHtml)
            return true
        } catch (e: Exception) {
            Logger.d(LOG_TAG, e.toString())
        }
        return false
    }

    override fun onAdExpanded(sender: MASTAdView, height: Int, width: Int) {
        if (!isInterstitial) {
            asyncAdImpressionReporter?.onClickEvent()
        }
        if (nativeAdHtml?.interactiveAd == true) {
            adInteractionListener?.onInteractiveAdExpanded()
        }
    }

    override fun onAdResized(sender: MASTAdView, height: Int, width: Int) {}

    override fun onAdCollapsed(sender: MASTAdView) {
        if (isInterstitial && !isPopupInterstitial) {
            swipeableHTMLAdInteractionListener?.onSwipeableHtmlAdClosed()
        }
    }

    override fun onRichmediaEvent(sender: MASTAdView, methodName: String, params: String?) {
        if (!isPopupInterstitial || isPopupInterstitialShown) {
            return
        }
        if (methodName.equals(MASTAdConstants.CUSTOM_METHOD_SET_STATE_CHANGE, ignoreCase = true) &&
                mastAdView?.adWebView?.mraidInterface?.state == MraidInterface.STATES.DEFAULT) {
            isPopupInterstitialShown = true
            parentActivity?.runOnUiThread { mastAdView?.showInterstitial(false) }
        }
    }

    override fun onDownloadBegin(sender: MASTAdView) {}
    override fun onDownloadEnd(sender: MASTAdView) {}
    override fun onAdViewable(sender: MASTAdView) {
        registerOMAdSession(sender)
        Logger.d(LOG_TAG, "onPageLoaded callback")
        asyncAdImpressionReporter?.onAdInflated()
        isAdLoaded = true
    }

    /**
     * Create the OM ad session and register the view for tracking.
     */
    private fun registerOMAdSession(mastAdView: MASTAdView?) {
        if (nativeAdHtml != null && mastAdView != null && mastAdView.adWebView != null) {
            startTrackingOnAdLoad(mastAdView.adWebView.html5WebView)
        }
    }

    override fun onDownloadError(sender: MASTAdView, error: String) {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onViewStopped() {
        //For mraid ads feature.
        stopVideo()
        //For web video
        pause()
    }

    override fun onSetAdInBackground() {
        // call mraid.close from script instead.
    }

    fun onAdViewVisibilityChange(isVisible: Boolean, showOnlyOnVisible: Boolean = false) {
        if (mastAdView == null) {
            return
        }

        val adWebView = mastAdView?.adWebView
        var html5WebView: WebView? = null
        if (adWebView != null) {
            html5WebView = adWebView.html5WebView
        }
        if (adWebView == null || html5WebView == null) {
            return
        }
        if (isVisible) {
            html5WebView.onResume()
            adWebView.injectJavaScript(adInViewJavaScriptUrl)
        } else {
            html5WebView.onPause()
            adWebView.injectJavaScript(adOutOfViewJavaScriptUrl)
            /*
       * For Tag HTML ads, we have no way to stop the video playback when fragment is not visible
       * anymore. Hence, forcefully destroy the webview when the fragment is not visible anymore
       * Next time we need this viewholder to show the webview, updateView must be called.
       */if (showOnlyOnVisible && !isAdLoaded) {
                relativeLayout?.removeAllViews()
                html5WebView.destroy()
                mastAdView = null
            }
        }
    }

    private fun setMastAdViewDimensions(nativeAdHtml: NativeAdHtml) {
        if (AdsUtil.isFullScreenAd(nativeAdHtml) && parentActivity != null) {
            setAdSize(CommonUtils.getDeviceScreenWidth(), CommonUtils.getRealScreenHeight(parentActivity))
        } else {
            val adWidth = nativeAdHtml.width ?: AdConstants.DEFAULT_AD_SIZE
            val adHeight = nativeAdHtml.height ?: AdConstants.DEFAULT_AD_SIZE

            var containerWidth = adWidth
            val maxWidthWithPadding = CommonUtils.getDeviceScreenWidth() - 2 *
                    CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.ad_content_margin)
            if (isFullWidthAd) {
                containerWidth = CommonUtils.getDeviceScreenWidth()
            } else if (containerWidth > maxWidthWithPadding) {
                containerWidth = maxWidthWithPadding
            }

            val ratio = adHeight.toFloat() / adWidth
            val height = ratio * containerWidth
            setAdSize(containerWidth, height.toInt())
        }
    }

    override fun onScrolled(dx: Int, dy: Int) {
        // onScrolled is called for visible VH only.
        if (!visible) {
            visible = true
            onAdViewVisibilityChange(isVisible = true)
        }
        translate()
    }

    /**
     * Parallax behavior is achieved by moving the view using -ve margins in top and bottom
     * directions. We set the margins equal to the top and bottom offset of the moving window.
     * The webview is as tall as the recyclerview but is clipped due to the viewgroup clipping
     * its out of bounds children .
     */
    private fun translate() {
        if (nativeAdHtml?.interactiveAd == true) {
            parentHeight?.let { viewPortHeight ->
                mastAdView?.let { wv ->
                    val fromTop = itemView.y + (adContainer?.y ?: 0f)
                    val lp = wv.layoutParams as RelativeLayout.LayoutParams
                    lp.topMargin = -1 * (fromTop).toInt()
                    lp.bottomMargin = -1 * (viewPortHeight - (fromTop + (relativeLayout?.height ?: 0)).toInt())
                    wv.layoutParams = lp
                }
            }
        }
    }

    //Visibility Calculator overrides
    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        super.onVisible(viewVisibilityPercentage, percentageOfScreen)
        visiblePercentage = ViewUtils.getVisibilityPercentage(relativeLayout)
        // Notify that ad is in visible area
        onAdInView()
        onScrolled(0, 0)
    }

    override fun onInVisible() {
        visiblePercentage = 0
        onAdOutOfView()
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        onVisible(viewVisibilityPercentage, percentageOfScreen)
    }

    private fun onAdInView() {
        if (!visible) {
            visible = true
            onAdViewVisibilityChange(isVisible = true, showOnlyOnVisible = false)
        }
        // If video ad, check and notify if it can play.
        if (isVideoAd) {
            if (visiblePercentage < autoplayVisibility) {
                pause()
                return
            }
            if (autoPlayManager?.isCurrentPlayingAsset(nativeAdHtml) == true) {
                play()
            }
        }
    }

    private fun onAdOutOfView() {
        if (!visible) {
            return
        }
        visible = false
        pause()
        onAdViewVisibilityChange(isVisible = false, showOnlyOnVisible = false)
    }

    private fun canPlayAd(): Boolean {
        return !(AdConstants.AD_ERROR == adState || AdConstants.ALL_ADS_COMPLETE == adState
                || AdConstants.AD_ENDED == adState || isVideoPausedByUser)
    }

    override fun setAutoPlayManager(autoPlayManager: AutoPlayManager?) {
        this.autoPlayManager = autoPlayManager
    }

    override fun getAutoplayPriority(fresh: Boolean): Int { //No ad -> invalid priority
        if (nativeAdHtml == null) {
            return -1
        }
        if (fresh) {
            visiblePercentage = ViewUtils.getVisibilityPercentage(relativeLayout)
        }
        // Check for autoplay eligibility
        return if (visiblePercentage < autoplayVisibility || !canPlayAd()) {
            -1
        } else visiblePercentage * 2 + 1
    }

    override fun play() {
        Logger.d(LOG_TAG, " Play web ad")
        mastAdView?.adWebView?.injectJavaScript(AdConstants.SCRIPT_PLAY)
    }

    override fun pause() {
        Logger.d(LOG_TAG, " Pause web ad")
        mastAdView?.adWebView?.injectJavaScript(AdConstants.SCRIPT_PAUSE)
    }

    override fun canRelease(): Boolean {
        return false
    }

    override fun releaseVideo() { // do nothing, only for exoplayer
    }

    override fun getVisibilityPercentage(): Int {
        return visiblePercentage
    }

    override fun getPositionInList(): Int {
        return adapterPosition
    }

    override fun resetVideoState() { // do nothing, only for exoplayer
    }

    internal inner class CustomMastAdJSInterface(private val isInteractive: Boolean) {

        @JavascriptInterface
        fun isInteractive(): Boolean {
            return isInteractive
        }

        @get:JavascriptInterface
        val appVersion: String
            get() {
                return ClientInfoHelper.getAppVersion()
            }

        @get:JavascriptInterface
        @set:JavascriptInterface
        var muteState: Boolean
            get() {
                return isListMuteMode
            }
            set(isMute) {
                if (isListMuteMode != isMute) {
                    toggleMute()
                    if (adEntity != null) {
                        AndroidUtils.getMainThreadHandler().post {
                            BusProvider.getUIBusInstance().post(PlaySettingsChangedEvent(isMute,
                                    getAdEntity()?.uniqueAdIdentifier ?: ""))
                        }
                    }
                }
            }

        @JavascriptInterface
        fun updateAdState(adState: String?) {
            Logger.d(LOG_TAG, "Adstate is $adState")
            if (adState == null || (AdConstants.AD_INVALID == adState)) {
                return
            }
            this@NativeAdHtmlViewHolder.adState = adState
            when (adState) {
                AdConstants.AD_PAUSE_BY_TAP -> isVideoPausedByUser = true
                AdConstants.AD_RESUMED -> {
                    isVideoPausedByUser = false
                    if (autoPlayManager != null) {
                        autoPlayManager?.replaceCurrentPlayingView(this@NativeAdHtmlViewHolder)
                    }
                }
                AdConstants.AD_CLICK -> asyncAdImpressionReporter?.onClickEvent()
            }
        }

        @JavascriptInterface
        fun getParentHeight(): Int {
            return parentHeight ?: -1
        }
    }

    interface SwipeableHTMLAdInteractionListener {
        fun onSwipeableHtmlAdClosed()
    }

    interface CachedWebViewProvider {
        fun putWebView(key: String, value: WeakReference<MASTAdView>?)

        fun getWebView(key: String): WeakReference<MASTAdView>?
    }

    override fun onPlaySettingsChanged(event: PlaySettingsChangedEvent) {
        val adEntity = adEntity
        if (adEntity == null || adEntity.uniqueAdIdentifier == event.id
                || mastAdView == null) {
            return
        }
        val adWebView = mastAdView?.adWebView
        adWebView?.injectJavaScript(String.format(AdConstants.SCRIPT_MUTE, event.isMute))
    }

    override fun isPLaying(): Boolean {
        return false
    }

    companion object {
        const val LOG_TAG = "nativeAdHtmlViewHolder"
    }
}