/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.customviews

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.lifecycle.MutableLiveData
import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener
import com.dailyhunt.tv.players.analytics.PlayerAnalyticsHelper
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.helpers.PlayerInlineVideoAdBeaconHandler
import com.dailyhunt.tv.players.interfaces.AutoplayPlayerCallbacks
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.dailyhunt.tv.players.listeners.PlayerWebPlayerListener
import com.dailyhunt.tv.players.player.WebPlayer
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.android.exoplayer2.SimpleExoPlayer
import com.newshunt.analytics.helper.ReferrerProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.util.NewsConstants

/**
 * @author shrikant.agrawal
 */

class WebPlayerWrapper : FrameLayout, VideoPlayerWrapper, PlayerWebPlayerListener {

    private val LOG_TAG = "WEB_Autoplay"
    private lateinit var webPlayer: WebPlayer
    private lateinit var embedVideoView: VideoEnabledWebView
    var playerListener: PlayerWebPlayerListener? = null
    var wrapperCallbacks: VideoWrapperPlayCallbacks? = null
    private lateinit var playerAsset: PlayerAsset
    private lateinit var webChromeClient: VideoEnabledChromeClient
    private var referrerProvider: ReferrerProvider? = null
    private var isDetailShowing = false
    private lateinit var uiHandler: Handler
    private var viewCallback: PlayerCallbacks? = null
    private var currentDuration: Long = 0
    private var initialStartTime: Long = 0
    private var videoHelperCallbacks: VideoHelperCallbacks? = null
    private var pageReferrer: PageReferrer? = null
    private var eventSection: NhAnalyticsEventSection = NhAnalyticsEventSection.NEWS;
    private val pStateLiveData = MutableLiveData<PlayerEvent>()
    private var isPlayerReleased: Boolean = false
    private var isVideStarted: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        uiHandler = Handler()
        embedVideoView = VideoEnabledWebView(context)
        embedVideoView.setAutoplayVideo(true)
        setupChromeClient()
        playerListener = this
    }

    fun setVideoViewHelper(videoHelperCallbacks: VideoHelperCallbacks) {
        this.videoHelperCallbacks = videoHelperCallbacks
    }

    private fun setupChromeClient() {
        Logger.d(LOG_TAG, "setupChromeClient")
        webChromeClient = object : VideoEnabledChromeClient(embedVideoView,
                this) {
            override fun onProgressChanged(view: WebView, progress: Int) {}
        }

        webChromeClient.setOnToggledFullscreen(
                VideoEnabledChromeClient.ToggledFullscreenCallback { fullscreen, videoView ->
                    if (!webPlayer.isVideoReady) {
                        return@ToggledFullscreenCallback
                    }

                    if (!webPlayer.isVideoReady || webPlayer.isFullScreenCallback) {
                        //FullScreen handled by webPlayer
                        return@ToggledFullscreenCallback
                    }

                    if (fullscreen) {
                        setFullScreenMode(true)
                    } else {
                        setFullScreenMode(false)
                        //Closing from FullScreen
//                        if (webPlayer.isVideoComplete) {
//                            pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
//                        }
                    }
                })

        embedVideoView.webChromeClient = webChromeClient
    }

    fun loadVideo(playerAsset: PlayerAsset, isAutoPlayVideo: Boolean) {
        Logger.d(LOG_TAG, "loadVideo")
        isPlayerReleased = false
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_PREPARE_IN_PROGRESS, playerAsset?.id))
        this.playerAsset = playerAsset
        webPlayer = WebPlayer(context, playerAsset, embedVideoView, playerListener,
                playerAsset.type == PlayerType.DH_WEBPLAYER,
                playerMuteState, false, isAutoPlayVideo)
        webPlayer.setUpWebView()

        val params1 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        params1.gravity = Gravity.CENTER
        removeAllViews()
        addView(embedVideoView, params1)

        initialStartTime = System.currentTimeMillis()
    }

    override fun removeFromParent() {
        if (parent is ViewGroup) {
            (parent as ViewGroup).removeView(this)
        }
    }

    override fun setPageReferrer(pageReferrer: PageReferrer?) {
        this.pageReferrer = pageReferrer
    }

    override fun setLayoutParamsForWrapper(layoutParams: ViewGroup.LayoutParams) {
        this.layoutParams.width = layoutParams.width
        this.layoutParams.height = layoutParams.height
    }

    override fun getParentView(): ViewGroup? {
        if (parent is ViewGroup) {
            return parent as ViewGroup
        }
        return null
    }

    override fun getPlayerView(): View {
        return this
    }

    override fun hasVideoEnded(): Boolean {
        return webPlayer.isVideoComplete
    }


    override fun onAttachToNewsList() {
        webPlayer.setControlState(false)
        webPlayer.setPlayerListener(this)
        isDetailShowing = false

    }

    override fun onAttachToBuzzDetail() {
        webPlayer.setControlState(true)
        webPlayer.muteMode = PlayerControlHelper.isDetailMuteMode
        webPlayer.startPlay()
        isDetailShowing = true
    }

    override fun setEndAction(playerVideoEndAction: PlayerVideoEndAction) {
    }

    override fun resetCallbacks(viewCallback: PlayerCallbacks?, referrerProvider: ReferrerProvider?) {
        this.viewCallback = viewCallback
        this.referrerProvider = referrerProvider
        initialStartTime = System.currentTimeMillis()
    }

    override fun setPlayerMuteStatus(isMute: Boolean, isUserAction: Boolean, isFromSettings: Boolean) {
        webPlayer.muteMode = isMute
        if (playerAsset == null) {
            return
        }
        if (isUserAction) {
            if (isMute) {
                PlayerAnalyticsHelper.logVideoMuteStatusEvent(NewsConstants.EXPLORE_TYPE_MUTE,
                        PageReferrer(NewsReferrer.STORY_CARD, playerAsset.id))
            } else {
                PlayerAnalyticsHelper.logVideoMuteStatusEvent(NewsConstants.EXPLORE_TYPE_UNMUTE,
                        PageReferrer(NewsReferrer.STORY_CARD, playerAsset.id))
            }
        }
    }

    override fun getPlayerMuteState(): Boolean {
        return if (playerListener?.isVideoInNewsList == true) {
            PlayerControlHelper.isListMuteMode
        } else {
            PlayerControlHelper.isDetailMuteMode
        }
    }

    override fun setStartAction(startAction: PlayerVideoStartAction) {
    }

    override fun onAdEnded(currentDuration: Long) {
        PlayerInlineVideoAdBeaconHandler.getInstance().onAdEnd()
    }

    override fun getCurrentpositon(currentDuration: Long) {
        // do nothing
    }

    override fun handleFullScreen() {
        uiHandler.post({
            if (playerAsset.isInExpandMode) {
                onCollapseUI()
            } else {
                onExpandUI()
            }
        })
    }

    override fun onAdPaused(currentDuration: Long) {
        pStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_PAUSED, playerAsset?.id)
    }

    override fun isViewInForeground(): Boolean {
        viewCallback?.let {
            return it.isViewInForeground()
        }
        return true
    }

    override fun onAdSkipped(currentDuration: Long) {
        pStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_SKIPPED, playerAsset?.id)
    }

    override fun onAdStarted(currentDuration: Long) {
        if (playerAsset.sourceInfo == null ||
                CommonUtils.isEmpty(playerAsset.sourceInfo.playerKey)) {
            return
        }
        pStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_START, playerAsset?.id)
    }

    override fun onDisplayClick() {
        // set the callback to view holder
        if (wrapperCallbacks != null && !isDetailShowing) {
            wrapperCallbacks!!.onDisplayClick()
        }
    }

    override fun onFinishBuffering(currentDuration: Long) {
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_BUFFERING, playerAsset?.id))
    }

    override fun onFinishPlaying(currentDuration: Long) {
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
        this.currentDuration = currentDuration

        if (!isInFullScreenMode) {
            pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
        }
    }

    override fun onPlayStart(currentDuration: Long) {
        Logger.d(LOG_TAG, "onPlayStart")
        if (!isVideStarted) {
            isVideStarted = true
            pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, playerAsset?.id))
        } else {
            pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_PLAYING, playerAsset?.id))
        }

        viewCallback?.onRenderedFirstFrame()
    }

    override fun onPlayerError(currentDuration: Long) {
        Logger.d(LOG_TAG, "onPlayerError")
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_ERROR, playerAsset?.id))
        videoHelperCallbacks?.onLoadError()
        (viewCallback as? AutoplayPlayerCallbacks)?.hideLoader()
    }

    override fun onPlayerPause(currentDuration: Long) {
        Logger.d(LOG_TAG, "onPlayerPause")
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_PAUSED, playerAsset?.id))
    }

    override fun onPlayerReady() {
        Logger.d(LOG_TAG, "onPlayerReady")
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_READY, playerAsset?.id))
        if (videoHelperCallbacks != null) {
            videoHelperCallbacks!!.onVideoReady()
        }
    }

    override fun onStartBuffering(currentDuration: Long) {
        pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_BUFFERING, playerAsset?.id))
        (viewCallback as? AutoplayPlayerCallbacks)?.showLoader()

    }

    override fun resetEventTimer(currentDuration: Long) {
    }

    override fun handleBackPress(): Boolean {
        // handle the back press
        if (isInFullScreenMode) {
            Logger.d(LOG_TAG, "item is in expanded mode")
            if (webPlayer.isFullScreenCallback) {
                //Allow the Player to handle fullscreen close
                webPlayer.closeFullscreen()
                if (webPlayer.isVideoComplete) {
                    pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
                }
            } else {
                onCollapseUI()
            }
            embedVideoView.onBackPressed()
            return true
        }
        return false
    }

    override fun isInFullScreenMode(): Boolean {
        return playerAsset.isInExpandMode
    }

    override fun onBackPressed() {
        // handle the back press
        if (isInFullScreenMode) {
            Logger.d(LOG_TAG, "item is in expanded mode")
            if (webPlayer.isFullScreenCallback) {
                //Allow the Player to handle fullscreen close
                webPlayer.closeFullscreen()
                if (webPlayer.isVideoComplete) {
                    pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
                }
            } else {
                onCollapseUI()
            }
            embedVideoView.onBackPressed()
        }
    }

    override fun unmutePlayerOnDeviceVolumeRaised() {
        webPlayer.muteMode = false
    }

    override fun pause() {
        webPlayer.pausePlay()
        setScreenAwakeLock(false)
    }

    override fun releaseAndSetReload() {
        Logger.d(LOG_TAG, "releaseAndSetReload")
        //Not release for webPlayer, Only pause
        webPlayer.pausePlay()
    }

    override fun releasePlayer() {
        Logger.d(LOG_TAG, "releasePlayer")
        try {
            isPlayerReleased = true
            removeAllViews()
            webPlayer.pausePlay()
            isVideStarted = false
            embedVideoView.loadUrl("about:blank")
            PlayerUtils.resetWebViewState(embedVideoView)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    override fun resume() {
        setScreenAwakeLock(true)
        if (webPlayer == null || webPlayer.isVideoError || isPlayerReleased) {
            loadVideo(playerAsset, true)
        } else {
            webPlayer.muteMode = playerMuteState
            webPlayer.startPlay()
        }
    }

    override fun setFullScreenMode(fullScreenMode: Boolean) {
        // handle the full screen mode
        playerAsset.isInExpandMode = fullScreenMode

        //Set full screen mode as true ..
        if (viewCallback != null) {
            viewCallback!!.toggleUIForFullScreen(fullScreenMode)
        }
    }

    override fun getPlayerStateLiveData(): MutableLiveData<PlayerEvent>? {
        return pStateLiveData
    }

    override fun setViewLayoutParams(params: RelativeLayout.LayoutParams?) {
        // handle the view params
        if (params != null) {
            layoutParams.height = params.height
            layoutParams.width = params.width
        }
    }

    override fun restartVideo() {
        releasePlayer()
        loadVideo(playerAsset, true)
    }

    override fun getReferrerProvider(): ReferrerProvider? {
        // to do handle the refererr provider
        return referrerProvider
    }

    private fun onExpandUI() {
        try {
            Logger.d(LOG_TAG, "On Expand UI")
            // Calculate the Expanded Scale ..
            val params: RelativeLayout.LayoutParams
            val contentHeight = playerAsset.height
            val contentWidth = playerAsset.width

            val scale = PlayerUtils.getScale(context, contentWidth, contentHeight,
                    CommonUtils.getDeviceScreenHeight(), CommonUtils.getDeviceScreenWidth())
            val c_w = CommonUtils.getDeviceScreenHeight()
            val c_h = CommonUtils.getDeviceScreenWidth()

            setFullScreenMode(true)
            Logger.d(LOG_TAG, " width : " + scale.width + " height : " + scale.height)
            Logger.d(LOG_TAG, "c_width : " + c_w + "c_h : " + c_h)
            //Item  Full expanded scale ..
            playerAsset.dataExpandScale = scale

            // Vide view layout ..
            params = RelativeLayout.LayoutParams(scale.width, scale.height)
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            setViewLayoutParams(params)

        } catch (e: Exception) {
            Logger.caughtException(e)
        }

    }

    private fun onCollapseUI() {
        try {
            Logger.d(LOG_TAG, "On Collapse UI")
            setFullScreenMode(false)

            //On fullscreen close, check video complete and move to next video ..
//            if (hasVideoEnded()) {
//                pStateLiveData.postValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
//            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    override fun setEventSection(eventSection: NhAnalyticsEventSection) {
        this.eventSection = eventSection;
    }

    override fun onFirstQuartile() {
        //Nothing here
    }

    override fun onMidQuartile() {
        //Nothing here
    }

    override fun onThirdQuartile() {
        //Nothing here
    }

    override fun isVideoInNewsList(): Boolean {
        return viewCallback?.isVideoInNewsList() ?: false
    }

    override fun resumeOnNetworkError(isPaused: Boolean) {
        //Nothing here
        if (playerAsset == null) {
            return
        }

        //Considering isVideoReady in case of resume from network
        if(webPlayer == null || !webPlayer.isVideoReady ||
                webPlayer.isVideoError || isPlayerReleased) {
            loadVideo(playerAsset, true)
        } else if(!isPaused) {
            webPlayer.muteMode = playerMuteState
            webPlayer.startPlay()
        }
    }

    override fun setVideoTimeListener(videoTimeListener: VideoTimeListener?) {
        webPlayer?.setVideoTimeListener(videoTimeListener)
    }

    override fun getPlayer(): SimpleExoPlayer? {
        return null
    }

    override fun getAutoplayVideoId(): String {
        return getAssetId()
    }

    fun getAssetId(): String {
        return playerAsset.id
    }

    private fun setScreenAwakeLock(lockState: Boolean) {
        Logger.d(LOG_TAG, "setScreenLock >> Setting awake lock $lockState")
        ViewUtils.setScreenAwakeLock(lockState, this, View.VIEW_LOG_TAG)
    }
}

