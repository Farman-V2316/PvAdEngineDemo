/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.customviews

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.lifecycle.MutableLiveData
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.StreamConfigAsset
import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener
import com.dailyhunt.tv.exolibrary.util.ExoBufferSettings
import com.dailyhunt.tv.players.R
import com.dailyhunt.tv.players.analytics.PlayerAnalyticsHelper
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.interfaces.AutoplayPlayerCallbacks
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.dailyhunt.tv.players.interfaces.PlayerExoCallbacks
import com.dailyhunt.tv.players.listeners.ExoPlayerListenerDH
import com.dailyhunt.tv.players.player.ExoPlayerDH
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.instream.IAdCacheManager
import com.newshunt.adengine.listeners.PlayerInstreamAdListener
import com.newshunt.adengine.model.entity.AdErrorType
import com.newshunt.adengine.model.entity.AdErrorRequestBody
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.helper.InstreamAdViewsManager
import com.newshunt.analytics.helper.ReferrerProvider
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.view.DetachableWebView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.util.NewsConstants
import java.util.*

class ExoPlayerWrapper2 : FrameLayout, VideoPlayerWrapper, ExoPlayerListenerDH,
        PlayerInstreamAdListener, DetachableWebView {

    private var playerAsset: ExoPlayerAsset? = null
    private var adEntity: BaseAdEntity? = null
    private var playerCallbacks: PlayerCallbacks? = null
    private var currentPageReferrer: PageReferrer? = null
    private var referrerProvider: ReferrerProvider? = null
    private var videoStartTime: Long = 0

    private lateinit var adContainer: RelativeLayout
    private lateinit var playerView: PlayerView
    private var companionView: CompanionAdView? = null
    private var exoPlayerDH: ExoPlayerDH? = null
    //Non-IMA instream ad, if any
    private var instreamAdManager: InstreamAdViewsManager? = null
    private var isAdDisplaying = false

    private var isReleased: Boolean = false
    private var isVideoComplete: Boolean = false
    private var eventSection = NhAnalyticsEventSection.NEWS
    private var reloadSource = false
    private var videoTimeListener: VideoTimeListener? = null
    val playerLiveData = MutableLiveData<PlayerEvent>()
    var adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper? = null
    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
        playerView.setKeepContentOnPlayerReset(true)
    }

    private fun initView() {
        playerView = rootView.findViewById(R.id.exo_player_view)
        adContainer = rootView.findViewById(R.id.ad_container)
        companionView = rootView.findViewById(R.id.companion_view)
    }

    fun reloadPlayer(playerAsset: ExoPlayerAsset?) {
        if (playerAsset == null) return
        isAdDisplaying = false
        exoPlayerDH?.resetAdsLoader()
        buildPlayer(playerAsset, playerCallbacks, adsTimeSpentOnLPHelper)
    }

    fun buildPlayer(playerAsset: ExoPlayerAsset, playerCallbacks: PlayerCallbacks? = null, adsTimeSpentOnLPHelper:AdsTimeSpentOnLPHelper?) {
        Logger.d(TAG, "buildPlayer id: ${playerAsset?.id}")
        this.playerAsset = playerAsset
        this.playerCallbacks = playerCallbacks

        //Read Buffer Settings
        PlayerUtils.getDesiredVideoQuality()

        //Add ad data, if any.
        adEntity = IAdCacheManager.getInstreamAd(playerAsset, 1)
        if (adEntity != null) {
            Logger.d(TAG, "buildPlayer adEntity: ${adEntity?.adTag}")
        }
        this.adsTimeSpentOnLPHelper = adsTimeSpentOnLPHelper
        exoPlayerDH = ExoPlayerDH(StreamConfigAsset(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                islive = playerAsset.isLiveStream, mute = playerAsset.isMuteMode),
                this)

        observePlayerStates(playerCallbacks)
        loadContent()
    }

    private fun observePlayerStates(playerCallbacks: PlayerCallbacks? = null) {
        playerCallbacks?.lifeCycleOwner?.let {
            exoPlayerDH?.playerStateLiveData?.observe(playerCallbacks.lifeCycleOwner,
                    androidx.lifecycle.Observer {
                        handlePlayerState(it)
                    })
        }
    }

    fun buildAnalyticHelper(adEntity: BaseDisplayAdEntity?, pageReferrer: PageReferrer?,
                            referrerFlow: PageReferrer?, referrerLead: PageReferrer?) {
        //Read Buffer Settings
        PlayerUtils.getDesiredVideoQuality()

        var pageReferrer = pageReferrer
        if (pageReferrer == null) {
            pageReferrer = this.currentPageReferrer
        }
        this.adEntity = adEntity
    }

    fun handlePlayerState(playerEvent: PlayerEvent) {
        if (playerLiveData.value?.playerState == playerEvent.playerState) {
            Logger.d(TAG, "handlePlayerState, id: ${playerAsset?.id} return >>")
            return
        }
        Logger.d(TAG, "handlePlayerState, id: ${playerLiveData.value?.id} != ${playerAsset?.id}")
        Logger.d(TAG, "handlePlayerState, id: ${playerAsset?.id} and Event: " +
                "${playerEvent.playerState}, msg: ${playerEvent.msg}")
        playerLiveData.value = playerEvent
        when (playerEvent.playerState) {
            PLAYER_STATE.STATE_VIDEO_END -> {
                onVideoComplete()
            }
            PLAYER_STATE.STATE_VIDEO_START -> {
                onVideoStart()
            }
            PLAYER_STATE.STATE_PLAYING -> {
                onVideoPlaying()
            }
            PLAYER_STATE.STATE_PAUSED,
            PLAYER_STATE.STATE_ERROR -> {
                setScreenAwakeLock(false)
            }
        }
    }

    private fun setScreenAwakeLock(lockState: Boolean) {
        if (lockState) {
            Logger.d(TAG, "setScreenLock >> Setting awake lock true")
            ViewUtils.setScreenAwakeLock(true, this, View.VIEW_LOG_TAG)
        } else {
            Logger.d(TAG, "setScreenLock >> Setting awake lock false")
            ViewUtils.setScreenAwakeLock(false, this, View.VIEW_LOG_TAG)
        }
    }

    fun getPlayerAsset(): ExoPlayerAsset? {
        return playerAsset
    }

    fun getPlayerState(): PLAYER_STATE {
        if (exoPlayerDH != null) {
            return exoPlayerDH!!.getPlayerState()
        }
        return PLAYER_STATE.STATE_UNKNOW
    }

    private fun loadContent() {
        Logger.d(TAG, "loadContent ${playerAsset?.id}")
        isReleased = false

        //AD Entity processing.
        instreamAdManager = InstreamAdViewsManager.create(adEntity, adContainer)
        instreamAdManager?.setAdStateListener(this)

        //To play any preroll ad, give an update for start position. or should we wait for video
        // to load?
        instreamAdManager?.onTimeUpdate(0)

        val autoPlayFlag = instreamAdManager?.isPlayingAd() ?: false
        playerCallbacks?.companionAdView?.let {
            companionView = it
        }
        exoPlayerDH?.loadVideo(playerAsset!!, !autoPlayFlag, adEntity, adsTimeSpentOnLPHelper, companionView?.getAdContainer())

        videoStartTime = System.currentTimeMillis()
    }

    // *** ExoPlayerListenerDH Callbacks ***
    override fun setPlayer(exoPlayer: SimpleExoPlayer) {
        this.playerView.player = exoPlayer
        if (playerCallbacks != null && playerCallbacks is PlayerExoCallbacks) {
            (playerCallbacks as PlayerExoCallbacks).setPlayer(exoPlayer)
        }
        exoPlayerDH?.mute(playerMuteState)
    }

    override fun getExoPlayerView(): PlayerView? {
        if (::playerView.isInitialized) {
            return playerView
        }
        return null
    }

    private fun onVideoStart() {
        setScreenAwakeLock(true)
        isVideoComplete = false
    }

    private fun onVideoPlaying() {
        isVideoComplete = false
        setScreenAwakeLock(true)
    }

    private fun onVideoComplete() {
        this.isVideoComplete = true
        companionView?.hideAd()
        setScreenAwakeLock(false)
        if(playerAsset != null && playerAsset!!.isGif || playerAsset!!.loopCount > 0) {
            releaseAndSetReload()
        } else {
            exoPlayerDH?.seekToStartAndPause()
        }
    }

    override fun onVideoError(exception: ExoPlaybackException?) {
        setScreenAwakeLock(false)
        reloadSource = true
    }

    override fun showOverlayViewIfNotVisible() {

    }

    fun showVideoLoading(state: Boolean) {
        if (state) {
            (playerCallbacks as? AutoplayPlayerCallbacks)?.showLoader()
        } else {
            (playerCallbacks as? AutoplayPlayerCallbacks)?.hideLoader()
        }
        (playerCallbacks as? PlayerExoCallbacks)?.showVideoLoading(state)
    }

    override fun isViewInForeground(): Boolean {
        playerCallbacks?.let {
            return it.isViewInForeground()
        }
        return true
    }

    override fun getPlayerStateLiveData(): MutableLiveData<PlayerEvent>? {
        return playerLiveData
    }

    override fun setViewLayoutParams(params: RelativeLayout.LayoutParams?) {
//        layoutParams = params
    }

    override fun setFullScreenMode(fullScreenMode: Boolean) {
        playerAsset?.isInExpandMode = fullScreenMode
    }

    override fun isInFullScreenMode(): Boolean {
        return playerAsset != null && playerAsset?.isInExpandMode!!
    }

    override fun pause() {
        Logger.d(TAG, "pause id : ${playerAsset?.id}")
        exoPlayerDH?.pause()
        setScreenAwakeLock(false)
    }

    fun resumeWithReload() {
        Logger.d(TAG, "resumeWithReload id : ${playerAsset?.id}")
        if (!reloadSource) {
            return
        }

        reloadSource = false
        if (exoPlayerDH != null) {
            val muteState = playerMuteState
            exoPlayerDH?.rePrepareVideo(true)
            exoPlayerDH?.mute(muteState)
            playerCallbacks?.onVideoResumed()
        } else {
            reloadPlayer(playerAsset)
        }
    }

    override fun releaseAndSetReload() {
        Logger.d(TAG, "releaseAndSetReload")
        exoPlayerDH?.pause()
        exoPlayerDH?.release(true)
        reloadSource = true
    }

    override fun pauseWithOutAction() {
        Logger.d(TAG, "pauseWithOutAction id : ${playerAsset?.id}")
        exoPlayerDH?.pause()
        setScreenAwakeLock(false)
    }

    override fun resume() {
        Logger.d(TAG, "resume id : ${playerAsset?.id}")
        setScreenAwakeLock(true)
        if (instreamAdManager?.isPlayingAd() == true) {
            return
        }

        if (reloadSource) {
            resumeWithReload()
            return
        }

        exoPlayerDH?.mute(playerMuteState)

        val changeStatus = exoPlayerDH?.resume()
        if (changeStatus == true) playerCallbacks?.onVideoResumed()
    }

    override fun onBackPressed() {

    }

    override fun unmutePlayerOnDeviceVolumeRaised() {
        exoPlayerDH?.mute(false)
    }

    override fun releasePlayer() {
        setScreenAwakeLock(false)
        exoPlayerDH?.release(false)
        exoPlayerDH = null
        playerView.player = null
        companionView = null
        instreamAdManager?.destroy()
        instreamAdManager = null
        playerCallbacks = null
    }

    override fun setEndAction(endAction: PlayerVideoEndAction) {

    }

    override fun setStartAction(startAction: PlayerVideoStartAction) {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    // **** PlayerInstreamAd Listener Callbacks
    override fun onAdStarted(ad: Ad?, baseDisplayAdEntity: BaseDisplayAdEntity?,
                             companionAdLoaded: Boolean) {
        IAdCacheManager.onAdLoaded()
        isAdDisplaying = true
        if (instreamAdManager?.isPlayingAd() == true) {
            pause()
        }
        companionView?.isFilled = companionAdLoaded
        playerCallbacks?.companionAdView?.onCompanionSlotLoaded(companionAdLoaded)

        baseDisplayAdEntity?.let { baseAd ->
            asyncAdImpressionReporter = AsyncAdImpressionReporter(baseAd)
            ad?.let {adItem ->
                AndroidUtils.getMainThreadHandler().postDelayed({
                    if (!adItem.isSkippable && baseAd.adPosition == AdPosition.INSTREAM_VIDEO) {
                        val ss = takeScreenshot()
                        fireErrorBeacon(AdsUtil.getErrorCodeFor(AdErrorType.INSTREAM_NO_SKIP), ss, baseAd)
                    }
                }, AdConstants.SCREENSHOT_CAPTURE_DELAY)
            }
        }

        val curTime = exoPlayerDH?.getPlayer()?.currentPosition
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_START, playerAsset?.id)
    }

    private fun fireErrorBeacon(errorCode: Int?, ss: Bitmap?, baseDisplayAdEntity: BaseDisplayAdEntity) {
        ss?.let {
            val file = AdsUtil.saveMediaToStorage(it)
            Logger.d(TAG, "image file : $file")
            asyncAdImpressionReporter?.hitErrorBeacon(
                AdErrorRequestBody(
                    errorCode = errorCode,
                    url = (baseDisplayAdEntity as ExternalSdkAd).external?.tagURL
                ),
                screenshotFilePath = file.absolutePath
            )
        }
    }

    fun takeScreenshot() : Bitmap? {
        val textureView = playerView.videoSurfaceView as TextureView
        return textureView.bitmap
    }

    override fun onAdTapped() {
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_CLICK, playerAsset?.id)
    }

    override fun onAdPaused() {
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_PAUSED, playerAsset?.id)
    }

    override fun onAdResumed() {
    }

    override fun onAdSkipped() {
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_SKIPPED, playerAsset?.id)
    }

    override fun onAdComplete() {
        setStartAction(PlayerVideoStartAction.AD_END)
        onVideoStart()
        companionView?.onVideoAdEnded(PlayerUtils.canShowCompanionAd(playerAsset!!))
        isAdDisplaying = false
        instreamAdManager?.onAdComplete()
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_END, playerAsset?.id)
    }

    override fun onAdError(message: String?) {
        isAdDisplaying = false
        instreamAdManager?.setVisibility(false)
        companionView?.hideAd()
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_END, playerAsset?.id)
    }

    override fun onAllAdComplete() {
        isAdDisplaying = false
        if (instreamAdManager?.hasUnseenEmptyAds() == false) {
            instreamAdManager?.destroy()
            instreamAdManager = null
        }
        playerLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_END, playerAsset?.id)
    }

    override fun getAdViewGroup(): ViewGroup {
        return playerView.adViewGroup
    }

    override fun getAdOverlayViews(): Array<View>? {
        return playerView.adOverlayViews
    }

    override fun removeFromParent() {
        if (parent is ViewGroup) {
            (parent as ViewGroup).removeView(this)
        }
        //remove the companion view too.
        playerCallbacks?.companionAdView?.let {
            (it.parent as? ViewGroup)?.removeView(it)
            addView(it)
            it.visibility = View.GONE
        }
    }

    override fun setPageReferrer(pageReferrer: PageReferrer?) {
        this.currentPageReferrer = pageReferrer
    }

    override fun setLayoutParamsForWrapper(layoutParams: ViewGroup.LayoutParams) {
        setLayoutParams(layoutParams)
        val params = playerView.layoutParams
        params.height = layoutParams.height
        params.width = layoutParams.width
        playerView.layoutParams = params
    }

    override fun resetCallbacks(viewCallback: PlayerCallbacks?, referrerProvider: ReferrerProvider?) {
        this.referrerProvider = referrerProvider
        this.playerCallbacks = viewCallback
        observePlayerStates(playerCallbacks)
    }

    override fun getPlayerCallbacks(): PlayerCallbacks? {
        return playerCallbacks
    }


    override fun onAttachToNewsList() {
        //To override start_action, that comes from buzz detail[resume to click/autoplay]
        if (AutoPlayHelper.isAutoPlayAllowed()) {
            setStartAction(PlayerVideoStartAction.AUTOPLAY)
        } else {
            setStartAction(PlayerVideoStartAction.CLICK)
        }

        try {
            // Hack to make video resume in some devices, where minimise and open not playing
            // video , always be in buffering state
            if (Build.VERSION.SDK_INT < 27 && playerView.player != null) {
                if (!playerView.player!!.isPlayingAd && playerView.player!!.currentPosition > 0) {
                    playerView.player!!.seekTo((playerView.player!!.currentPosition + 1))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        eventSection = NhAnalyticsEventSection.NEWS
    }

    override fun onAttachToBuzzDetail() {

        setScreenAwakeLock(true)
        try {
            // Hack to make video resume in some devices, where minimise and open not playing
            // video , always be in buffering state
            if (Build.VERSION.SDK_INT < 27 && playerView.player != null) {
                if (!playerView.player!!.isPlayingAd && playerView.player!!.currentPosition > 0) {
                    playerView.player!!.seekTo((playerView.player!!.currentPosition + 1))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        eventSection = NhAnalyticsEventSection.TV
    }

    override fun getPlayerView(): View {
        return this
    }

    override fun hasVideoEnded(): Boolean {
        return isVideoComplete
    }

    override fun setPlayerMuteStatus(isMute: Boolean, isUserAction: Boolean, isFromSettings: Boolean) {
        exoPlayerDH?.mute(isMute)
        if (isUserAction) {
            if (isMute) {
                PlayerAnalyticsHelper.logVideoMuteStatusEvent(NewsConstants.EXPLORE_TYPE_MUTE,
                        PageReferrer(NewsReferrer.STORY_CARD, playerAsset?.getId()))
            } else {
                PlayerAnalyticsHelper.logVideoMuteStatusEvent(NewsConstants.EXPLORE_TYPE_UNMUTE,
                        PageReferrer(NewsReferrer.STORY_CARD, playerAsset?.getId()))
            }
        }
    }

    override fun getParentView(): ViewGroup? {
        return if (parent is ViewGroup) {
            parent as ViewGroup
        } else null
    }

    override fun handleBackPress(): Boolean {
        playerCallbacks?.toggleUIForFullScreen(false)
        setEndAction(PlayerVideoEndAction.APP_BACK)
        return false
    }

    override fun restartVideo() {
        isVideoComplete = false
        if (reloadSource) {
            resumeWithReload()
        } else {
            exoPlayerDH?.restart()
        }
    }

    override fun getReferrerProvider(): ReferrerProvider? {
        return referrerProvider
    }

    override fun getPlayerMuteState(): Boolean {
        if(playerAsset?.isGif == true || playerAsset?.isHideControl == true) {
            return true
        }
        return if (playerCallbacks?.isVideoInNewsList == true) {
            PlayerControlHelper.isListMuteMode
        } else {
            PlayerControlHelper.isDetailMuteMode
        }
    }

    override fun setEventSection(eventSection: NhAnalyticsEventSection) {
        this.eventSection = eventSection
    }

    override fun setVideoTimeListener(videoTimeListener: VideoTimeListener?) {
        this.videoTimeListener = videoTimeListener
        this.videoTimeListener?.showTimeLeft(true)
    }

    override fun getPlayer(): SimpleExoPlayer? {
        return exoPlayerDH?.getPlayer()
    }

    override fun isAdDisplaying(): Boolean {
        return isAdDisplaying
    }

    override fun isPlaying(): Boolean {
        return exoPlayerDH?.isPlaying() ?: false
    }

    override fun getAutoplayVideoId(): String {
        playerAsset?.let {
            return playerAsset!!.id
        }
        return ""
    }

    fun hideSurfaceView() {
        playerView.videoSurfaceView?.visibility = View.GONE
    }


    override fun duration(): Long {
        return if (playerView.player != null) playerView.player!!.currentPosition else -1
    }

    override fun totalDuration(): Long {
        return if (playerView.player != null) playerView.player!!.duration else 0L
    }

    fun showSurfaceView() {
        playerView.videoSurfaceView?.visibility = View.VISIBLE
    }

    override fun onTimeUpdate(position: Long) {
        playerCallbacks?.canShowUpNextVideoCard()
        updateProgress(position)
    }

    override fun onRenderedFirstFrame() {
        videoTimeListener?.onRenderedFirstFrame()
        playerCallbacks?.onRenderedFirstFrame()
    }

    private fun updateProgress(position: Long) {
        var duration = 0L
        if (playerView.player != null) {
            duration = playerView.player!!.duration
        } else if (playerAsset != null) {
            duration = playerAsset!!.durationLong
        }
        if (duration < 0) {
            return
        }
        val timeText = stringForTime(duration - position)
        videoTimeListener?.onTimeUpdate(timeText, position)
        instreamAdManager?.onTimeUpdate(position)

        if (Logger.loggerEnabled()) {
            Logger.d(TAG, "updateProgress contentId : " + playerAsset?.id)
            Logger.d(TAG, "updateProgress position : $position" )
            Logger.d(TAG, "updateProgress bufferedPosition: " + exoPlayerDH?.getPlayer()?.bufferedPosition)
            Logger.d(TAG, "updateProgress contentBufferedPosition: " + exoPlayerDH?.getPlayer()?.contentBufferedPosition)
            Logger.d(TAG, "updateProgress totalBufferedDuration: " + exoPlayerDH?.getPlayer()?.totalBufferedDuration)
        }
    }

    private fun stringForTime(timeMs: Long): String {
        val formatBuilder = StringBuilder()
        val formatter = Formatter(formatBuilder, Locale.getDefault());
        var timeMs = timeMs
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0
        }
        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hours > 0)
            formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        else
            formatter.format("%d:%02d", minutes, seconds).toString()
    }

    fun registerVideoControlsOverlay(views: List<View>) {
        exoPlayerDH?.registerVideoControlsOverlay(views)
    }

    companion object {
        private const val TAG = "ExoPlayerWrapper2"
    }

    override fun logVideoError(exception: ExoPlaybackException?) {
        PlayerAnalyticsHelper.logVideoErrorAnalytics(exception, playerAsset, eventSection)
    }

    override fun getCurrentDuration(): Long? {
        return exoPlayerDH?.getPlayer()?.currentPosition ?: 0
    }

    override fun resumeOnNetworkError(isPausedState: Boolean) {
        if (exoPlayerDH == null || getCurrentDuration() == 0L) {
            reloadPlayer(playerAsset)
        } else if(!isPausedState) {
            resume()
        }
    }
}