/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.helper

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import com.dailyhunt.tv.ima.ContentPlayerController
import com.dailyhunt.tv.ima.ContentPlayerStateManager
import com.dailyhunt.tv.ima.callback.AdPlayerCallBack
import com.dailyhunt.tv.ima.entity.model.ContentAdType
import com.dailyhunt.tv.ima.entity.model.ContentData
import com.dailyhunt.tv.ima.entity.state.AdState
import com.dailyhunt.tv.ima.player.exo.VideoPlayerWithAdPlayback
import com.dailyhunt.tv.ima.player.exo.VideoPlayerWithAdPlayback.AdControlsListener
import com.dailyhunt.tv.ima.playerholder.ContentPlayerHolder
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.android.exoplayer2.ui.PlayerView
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.ClickAction
import com.newshunt.adengine.model.PlayerViewTapHandler
import com.newshunt.adengine.model.TapListener
import com.newshunt.adengine.model.entity.AdErrorType
import com.newshunt.adengine.model.entity.AdErrorRequestBody
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.fragment.DismissListenerImmersiveDialog
import com.newshunt.adengine.view.fragment.IMAImmersiveFragment
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.VideoAdFallback
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.nhcommand.NHCommandMainHandler
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.helper.player.PlayerControlHelper.isListMuteMode
import com.newshunt.news.analytics.NhAnalyticsAppState
import java.net.URLDecoder

/**
 * @author raunak.yadav
 */
class ImaAdHelper(private val adPlayerCallBack: AdPlayerCallBack?, private var activity: Activity?,
                  private val controlsListener: AdControlsListener?, private val followsMute: Boolean,
                  private val adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?)
    : NativeViewHelper, ImaPlayerManager, AdPlayerCallBack, AdControlsListener, TapListener {

    private var externalSdkAd: ExternalSdkAd? = null
    private var fallbackAd: ExternalSdkAd? = null
    var videoAdFallback: VideoAdFallback? = null
        private set
    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null
    private var videoPlayerWithAdPlayback: VideoPlayerWithAdPlayback? = null

    //Player state trackers
    private var playerController: ContentPlayerController? = null
    private var videoAdState: AdState? = null
    var isAdPlaying: Boolean = false
        private set
    // To track play/pause on resume
    private var isVideoPausedByTap: Boolean = false
    private var adPlayingBeforeInvisible: Boolean = false
    private var immersiveFrag: IMAImmersiveFragment? = null
    private var timerPrefix: String? = TIMER_PREFIX
    private lateinit var adPlayerHolder: View

    private var playerViewTapHandler = PlayerViewTapHandler(this)

    private val clickListener = View.OnClickListener {
        handleCTA()
    }

    private fun handleCTA() {
        externalSdkAd?.let { ad ->
            if (!DataUtil.isEmpty(ad.action)) {
                asyncAdImpressionReporter?.onClickEvent()

                if (immersiveFrag == null && ad.enableImmersiveView) {
                    asyncAdImpressionReporter?.hitTrackerUrl(
                        ad.customTracking?.customImmersiveViewEventTrackers?.get(AdConstants.NON_IMMERSIVE_CLICK) ?: ""
                    )
                }
                NhAnalyticsAppState.getInstance()
                    .setReferrer(NewsReferrer.AD)
                    .setReferrerId(ad.id)
                    .setEventAttribution(NewsReferrer.AD).eventAttributionId = ad.id
                val pageReferrer = PageReferrer(NewsReferrer.AD, ad.id)
                if (NHCommandMainHandler.getInstance()
                        .handle(ad.action, activity, null, pageReferrer)) {
                    return
                }

                try {
                    adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(externalSdkAd?.adLPTimeSpentBeaconUrl)
                    AdsOpenUtility.handleBrowserSelection(activity, ad.action, ad)
                } catch (e: Exception) {
                    Logger.caughtException(e)
                }
            } else if (playerController?.requestClick() == true) {
               Logger.d(LOG_TAG, "Clicked via Ads Manager")
           }
        }
    }

    val isVideoPlayableInCurrentState: Boolean
        get() {
            return when (videoAdState) {
                AdState.ALL_ADS_COMPLETE,
                AdState.AD_PLAY_ENDED,
                AdState.AD_ERROR,
                AdState.AD_UNKNOWN -> false
                else -> true
            }
        }

    fun updateAdEntity(externalSdkAd: ExternalSdkAd) {
        this.externalSdkAd = externalSdkAd
        asyncAdImpressionReporter = AsyncAdImpressionReporter(externalSdkAd)
        timerPrefix = if (!AdBindUtils.canShowAdsReportIcon(externalSdkAd) || externalSdkAd.adTemplate == AdTemplate.ENHANCED_HIGH) {
                null
            } else TIMER_PREFIX
    }

    override fun getNativeAssets(): NativeData? {
        val nativeAssets = NativeData()
        val external = externalSdkAd?.external
        external ?: return null

        nativeAssets.videoTagUrl = external.tagURL
        nativeAssets.sponsoredText = AdsUtil.getExternalSdkAdItemTag(externalSdkAd)

        nativeAssets.sourceAlphabet = externalSdkAd?.content?.sourceAlphabet
        if (!externalSdkAd?.action.isNullOrBlank())
            nativeAssets.ctaText = externalSdkAd?.content?.itemSubtitle2?.data

        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        nativeAssets.wideImageUrl = if(externalSdkAd?.width ?: 0 <  externalSdkAd?.height ?: 0) adsUpgradeInfo?.videoAdFallback?.verticalImageUrl else adsUpgradeInfo?.videoAdFallback?.imageUrl
        return nativeAssets
    }

    fun updateDataOnVideoError() {
        if (fallbackAd == null) {
            fallbackAd = ExternalSdkAd()
            //set same id so that viewholder is not refreshed if it has the same ad.
            fallbackAd!!.adPosition = externalSdkAd?.adPosition
            fallbackAd!!.uniqueAdId = externalSdkAd!!.uniqueAdId
            fallbackAd!!.adPosition = externalSdkAd?.adPosition
            fallbackAd!!.action = videoAdFallback?.action
            fallbackAd!!.beaconUrl = videoAdFallback?.beaconUrl
            fallbackAd!!.landingUrl = videoAdFallback?.landingUrl

            externalSdkAd = fallbackAd
            externalSdkAd?.adReportInfo = null
            //hit fallback ad's beacon.
            asyncAdImpressionReporter = AsyncAdImpressionReporter(fallbackAd!!)
        }
    }

    fun updateDataOnVideoLoad(assets: NativeData?, ad: Ad?) {
        assets ?: return
        ad ?: return
        assets.title = ad.title
        assets.body = ad.description
        assets.advertiser = ad.advertiserName
        externalSdkAd?.adReportInfo = AdsUtil.getAdReportInfo(assets)
    }

    override fun addAdChoicesView(adContainer: ViewGroup): View? {
        return null
    }

    override fun getMediaViewIfApplicable(mediaViewLayout: RelativeLayout): View {
        adPlayerHolder = LayoutInflater.from(activity).inflate(com.dailyhunt.tv.ima.R.layout.layout_ad_content_holder, mediaViewLayout, false)
        if (adPlayerHolder.parent != null) {
            val parent = adPlayerHolder.parent as ViewGroup
            parent.removeView(adPlayerHolder)
        }
        // All videos that start muted, follow global mute logic. Unmuted videos like in buzz will
        // keep mute property local to each ad.
        videoPlayerWithAdPlayback =
            adPlayerHolder.findViewById<VideoPlayerWithAdPlayback>(com.dailyhunt.tv.ima.R.id.videoPlayerWithAdPlayback)
                ?.apply {
                    setFollowGlobalMute(followsMute)
                    setStartMuted(followsMute && isListMuteMode)
                    setAdControlsListener(this@ImaAdHelper)
                    externalSdkAd?.let {
                        shouldShowCustomCTA(
                            it.showLearnMore ?: false, it.content?.learnMoreText?.data,
                            it.content?.learnMoreText?.getThemeBasedTextColor(ThemeUtils.isNightMode())
                        )
                    }
                }
        mediaViewLayout.addView(adPlayerHolder)
        return adPlayerHolder
    }

    override fun registerViewForInteraction(view: View, clickableViews: List<View>) {
        if (!CommonUtils.isEmpty(clickableViews)) {
            for (clickableView in clickableViews) {
                clickableView.setOnClickListener(clickListener)
            }
        }
    }

    override fun destroy(parentId: Int, view: View?) {
        ViewUtils.setScreenAwakeLock(false, activity, LOG_TAG)
        activity = null
        videoPlayerWithAdPlayback?.let {
            it.setAdControlsListener(null)
            it.releasePlayer()
        }
        playerController?.resetOldAdsLoaderPlusManager()
        playerController = null
    }

    fun onCardView() {
        fallbackAd?.let {
            if (!it.isShown) {
                it.notifyObservers()
                asyncAdImpressionReporter?.onCardView()
                it.isShown = true
            }
        }
    }

    override fun handleMediaOnAdUpdate(baseAdEntity: BaseAdEntity): Boolean {
        return handleMediaOnAdUpdate(baseAdEntity, null)
    }

    fun handleMediaOnAdUpdate(baseAdEntity: BaseAdEntity, autoPlayManager: AutoPlayManager?): Boolean {
        //release the previous mediaplayer if the view is being used for a different ad.
        if (externalSdkAd != null && baseAdEntity.uniqueAdIdentifier != externalSdkAd?.uniqueAdIdentifier) {
            resetPlayerState()
        } else if (playerController != null) {
            if (autoPlayManager == null && canAutoPlay()) {
                resumeAdIfAllowed()
            }
            return true
        }
        return false
    }

    override fun setupDHMediaView(adPlayerHolder: ContentPlayerHolder, context: Context): Boolean {
        val stateManager = ContentPlayerStateManager(this, ContentAdType.IN_STREAM_INHOUSE)
        playerController = ContentPlayerController(context, adPlayerHolder,
                stateManager, false, false, externalSdkAd?.enableImmersiveView ?: false)

        val decodedAdUrl: String
        try {
            val tagUrl = externalSdkAd?.external?.tagURL
            decodedAdUrl = URLDecoder.decode(tagUrl, Constants.TEXT_ENCODING_UTF_8)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
            return false
        }

        val contentData = ContentData(Constants.EMPTY_STRING, decodedAdUrl, false)
        externalSdkAd?.nativeAdObject = playerController
        return playerController!!.requestForContent(contentData)
    }

    override fun resumeAdIfAllowed() {
        if (!isAdPlaying && canPlayAd()) {
            playerController?.resumeAdManager()
        }
    }

    fun canPlayAd(): Boolean {
        return playerController != null && !isVideoPausedByTap && isVideoPlayableInCurrentState
    }

    override fun onPlayerInView() {
        // AdLoaded -> start adsManager (if autoplay)
        // else -> try resume.
        if (videoAdState == null) {
            return
        }
        val autoplay = AutoPlayHelper.isAutoPlayAllowed()
        if (videoAdState == AdState.AD_LOADED && autoplay) {
            handleMuteState()
            startPlayingAd()
        } else if (adPlayingBeforeInvisible || autoplay && !isAdPlaying) {
            handleMuteState()
            resumeAdIfAllowed()
        }
    }

    /**
     * Update mute state as per global flag, if the ad allows.
     */
    fun handleMuteState() {
        videoPlayerWithAdPlayback?.setMuteState(isListMuteMode)
    }

    override fun onPlayerOutOfView() {
        if (playerController != null) {
            if (videoAdState == AdState.AD_LOADED) {
                playerController?.showOrHideProgress(false)
            }
            if (isAdPlaying) {
                adPlayingBeforeInvisible = true
            }
            playerController?.pauseAdManager()
        }
    }

    private fun resetPlayerState() {
        playerController?.destroy()
        playerController = null
        fallbackAd = null
        videoAdState = null
        isAdPlaying = false
        isVideoPausedByTap = false
        adPlayingBeforeInvisible = AutoPlayHelper.isAutoPlayAllowed()
    }

    override fun onAdStateChanged(ad: Ad?, adState: AdState, adType: ContentAdType,
                                  companionAdLoaded: Boolean) {
        if (activity == null || activity?.isFinishing == true || activity?.isDestroyed == true) {
            return
        }
        if (adState != AdState.AD_TAPPED && adState != AdState.AD_CLICKED) {
            videoAdState = adState
        }
        when (adState) {
            AdState.AD_TAPPED -> onPlayerTapped(false)
            AdState.AD_CLICKED -> {
                asyncAdImpressionReporter?.onClickEvent()
                if(immersiveFrag == null && externalSdkAd?.enableImmersiveView == true) {
                    asyncAdImpressionReporter?.hitTrackerUrl(externalSdkAd?.customTracking?.customImmersiveViewEventTrackers?.get(AdConstants.NON_IMMERSIVE_CLICK) ?: "")
                }
                adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(externalSdkAd?.adLPTimeSpentBeaconUrl)
            }
            AdState.AD_PLAY_STARTED -> {
                ViewUtils.setScreenAwakeLock(true, activity, LOG_TAG)
                isAdPlaying = true
                ad?.let {
                    AndroidUtils.getMainThreadHandler().postDelayed({
                        if(it.isSkippable && externalSdkAd?.adPosition != AdPosition.INSTREAM_VIDEO) {
                            val ss = takeScreenshot()
                            fireErrorBeacon(AdsUtil.getErrorCodeFor(AdErrorType.OUTSTREAM_SKIP), ss)
                        }
                    },  AdConstants.SCREENSHOT_CAPTURE_DELAY)
                }
            }
            AdState.AD_RESUMED -> {
                ViewUtils.setScreenAwakeLock(true, activity, LOG_TAG)
                isAdPlaying = true
            }
            AdState.AD_ERROR,
            AdState.AD_UNKNOWN,
            AdState.AD_PAUSED,
            AdState.AD_PLAY_ENDED,
            AdState.ALL_ADS_COMPLETE -> {
                ViewUtils.setScreenAwakeLock(false, activity, LOG_TAG)
                isAdPlaying = false
                immersiveFrag?.dismissAllowingStateLoss()
            }
            AdState.AD_LOADED -> ViewUtils.setScreenAwakeLock(true, activity, LOG_TAG)
            else -> {
            }
        }
        adPlayerCallBack?.onAdStateChanged(ad, adState, adType, companionAdLoaded)
    }

    override fun canAutoPlay(): Boolean {
        return externalSdkAd!!.isShown && AutoPlayHelper.isAutoPlayAllowed()
    }

    override fun startPlayingAd() {
        playerController?.startPlayingAd()
    }

    override fun onPlayTapped() {
        isVideoPausedByTap = false
        controlsListener?.onPlayTapped()
    }

    override fun onPauseTapped() {
        isVideoPausedByTap = true
        controlsListener?.onPauseTapped()
    }

    override fun onPlayerTapped(defaultClick: Boolean) {
        if (defaultClick) {
            handleCTA()
        } else {
            playerViewTapHandler.handleTap()
        }
    }

    override fun executeTapIfAllowed(action: ClickAction): Boolean {
        when (action) {
            ClickAction.IMMERSIVE -> {
                val success = immersiveADview() != null
                if (immersiveFrag != null && isListMuteMode)
                    videoPlayerWithAdPlayback?.toggleForImmersiveMode(false)
                return success
            }
            ClickAction.LANDING_PAGE -> {
                if (externalSdkAd?.showLearnMore == false && !externalSdkAd?.action.isNullOrBlank()) {
                    handleCTA()
                    return true
                }
                return false
            }
            ClickAction.SDK_CLICK ->
                return externalSdkAd?.showLearnMore == false && playerController?.requestClick() == true
            ClickAction.PLAY_PAUSE -> {
                //No requirement for this currently.
                return false
            }
        }
    }

    override fun onAdProgress(timeLeft: String) {
        controlsListener?.onAdProgress(
            (timerPrefix ?: Constants.EMPTY_STRING).plus('(').plus(timeLeft).plus(')')
        )
    }

    override fun immersiveADview(): View? {
        if (activity?.isFinishing == true || immersiveFrag != null
                || externalSdkAd?.enableImmersiveView == false) return null


        val sharedView = controlsListener?.immersiveADview() ?: return null
        val fm = (activity as? FragmentActivity)?.supportFragmentManager
        fm ?: return null
        fm.beginTransaction().let {
            immersiveFrag = IMAImmersiveFragment.instance(bundleOf(
                    IMAImmersiveFragment.AD_ENTITY to externalSdkAd,
                    IMAImmersiveFragment.NATIVE_ENTITY to getNativeAssets()),
                    asyncAdImpressionReporter!!, adsTimeSpentOnLPHelper)
            immersiveFrag?.dismissListenerCallback = object : DismissListenerImmersiveDialog {
                override fun onDismiss(isCtaClick: Boolean, isClickNonSDKViewElement: Boolean) {
                    if (!isCtaClick && videoAdState != AdState.AD_PAUSED) {
                        //send tracking for exit from non cta click
                        asyncAdImpressionReporter?.hitTrackerUrl(
                                externalSdkAd?.customTracking?.customImmersiveViewEventTrackers?.get(AdConstants.IMMERSIVE_VIEW_EXIT)
                                        ?: "")
                    } else {
                        if(isClickNonSDKViewElement) asyncAdImpressionReporter?.onClickEvent()
                        asyncAdImpressionReporter?.hitTrackerUrl(
                                externalSdkAd?.customTracking?.customImmersiveViewEventTrackers?.get(AdConstants.IMMERSIVE_VIEW_CLICK)
                                        ?: "")
                    }
                    controlsListener.reinsertAdview(immersiveFrag?.getSharedView)
                    videoPlayerWithAdPlayback?.showImmersiveView(false)
                    immersiveFrag = null
                    //disable mute based on global mute status
                    if(isListMuteMode) videoPlayerWithAdPlayback?.toggleForImmersiveMode(true)
                }
            }
            if (sharedView is ViewGroup) {
                sharedView.findViewById<View>(R.id.ad_timer)?.visibility = View.GONE
                sharedView.findViewById<View>(R.id.ad_report)?.visibility = View.GONE
            }
            immersiveFrag?.showNow(fm, IMAImmersiveFragment.TAG)
            immersiveFrag?.attachVHToImmersive(sharedView)

        }
        return sharedView
    }

    private fun fireErrorBeacon(errorCode: Int?, ss: Bitmap?) {
        ss?.let {
            val file = AdsUtil.saveMediaToStorage(it)
            AdLogger.d(LOG_TAG, "image file : $file")
            asyncAdImpressionReporter?.hitErrorBeacon(
                AdErrorRequestBody(
                    errorCode = errorCode,
                    url = externalSdkAd?.external?.tagURL
                ),
                screenshotFilePath = if(AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.enableErrorScreenShot == true) file.absolutePath else null
            )
        }
    }

    private fun takeScreenshot(): Bitmap? {
        adPlayerHolder.findViewById<PlayerView>(com.dailyhunt.tv.ima.R.id.videoPlayer)?.let { playerView ->
            val textureView = playerView.videoSurfaceView as TextureView
            return textureView.bitmap
        }
        return null
    }
}

private const val LOG_TAG = "ImaAdHelper"
private const val TIMER_PREFIX = ": "
