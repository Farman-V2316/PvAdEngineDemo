/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.fragment

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.dailyhunt.tv.players.R
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.constants.PlayerContants
import com.dailyhunt.tv.players.customviews.VideoEnabledChromeClient
import com.dailyhunt.tv.players.customviews.VideoEnabledWebView
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.fragment.base.BasePlayerFragment
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.helpers.PlayerInlineVideoAdBeaconHandler
import com.dailyhunt.tv.players.interfaces.YoutubeView
import com.dailyhunt.tv.players.listeners.PlayerCustomYoutubeListener
import com.dailyhunt.tv.players.listeners.PlayerYoutubeIframeListener
import com.dailyhunt.tv.players.model.entities.server.PlayerErrorInfo
import com.dailyhunt.tv.players.player.CustomYouTubeFragment
import com.dailyhunt.tv.players.player.YoutubeIframePlayer
import com.dailyhunt.tv.players.service.PlayerErrorReportServiceImpl
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.newshunt.analytics.helper.ReferrerProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NhWebViewClient
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.analytics.NewsReferrerSource
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.news.helper.VideoPlayBackTimer

/**
 * Created by Jayanth on 09/05/18.
 */
//default construtor
class PlayerFragmentYoutube : BasePlayerFragment(), YoutubeView, PlayerCustomYoutubeListener, PlayerYoutubeIframeListener {

    private var item: PlayerAsset? = null
    private lateinit var rootView: ViewGroup

    //For Media
    private var ytMediaContainer: LinearLayout? = null
    private var customYouTubeFragment: CustomYouTubeFragment? = null
    private var tvVideoWebView: VideoEnabledWebView? = null
    private var webChromeClient: VideoEnabledChromeClient? = null
    private var currentPageReferrer: PageReferrer? = null
    private var referrerProvider: ReferrerProvider? = null
    private lateinit var touch_handling_for_crash: LinearLayout
    private var youtubeIframePlayer: YoutubeIframePlayer? = null
    private var videoStartAction = PlayerVideoStartAction.UNKNOWN
    //No AdEnd Callback, Hence use this to track if ad is atleast played for 'n' seconds
    private var videoPlayBackTimer: VideoPlayBackTimer? = null
    private val pStateLiveData = MutableLiveData<PlayerEvent>()
    private var currentDuration = 0
    private var partiallyReleased = false



    private fun getCustomYoutubeFragment(): CustomYouTubeFragment? {
        val videoUrl = item!!.videoUrl
        var videoId = item!!.sourceVideoId
        if (CommonUtils.isEmpty(videoId) && !CommonUtils.isEmpty(videoUrl)) {
            videoId = PlayerUtils.extractYouTubeVideoId(videoUrl)
        }
        var customYouTubeFragment: CustomYouTubeFragment? = null
        if (videoUrl != null && activity != null && !activity!!.isFinishing) {
            customYouTubeFragment = CustomYouTubeFragment.newInstance(item!!, videoUrl,
                    true, videoId, this,
                    playerCallbacks, getSection(arguments), currentDuration)
        }
        return customYouTubeFragment
    }

    override fun onCreate(savedState: Bundle?) {
        // fix for crash due to youtube app being force stopped.
        // Reference link :- https://stackoverflow.com/questions/44379747/youtube-android-player-api-throws-badparcelableexception-classnotfoundexception
        savedState?.remove("android:support:fragments")
        super.onCreate(savedState)
        setHasOptionsMenu(true)
        val bundle = arguments
        if (bundle != null) {
            item = bundle.getSerializable(PlayerContants.PLAYER_ASSET_ITEM) as PlayerAsset?
            requireNotNull(item) { getString(R.string.err_msg_player_asset_null) }
            currentPageReferrer = bundle.get(PlayerContants.BUNDLE_FRAGMENT_REFERRER) as PageReferrer?
        }
        videoPlayBackTimer = VideoPlayBackTimer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_item_youtube,
                container, false) as ViewGroup

        ytMediaContainer = rootView.findViewById(R.id.yt_media_container)
        touch_handling_for_crash = rootView.findViewById(R.id.touch_handling_for_crash)
        setYoutubeLayoutParams()
        loadYoutubeVideo()
        return rootView
    }

    private fun handleTouchTillYoutubeLoads() {
        touch_handling_for_crash.visibility = View.VISIBLE
        touch_handling_for_crash.setOnTouchListener { view, motionEvent -> true }
    }

    private fun releaseTouchHandling() {
        touch_handling_for_crash.visibility = View.GONE
        touch_handling_for_crash.setOnClickListener(null)
    }

    /**
     * Used to reload the already played video - with current duration
     */
    override fun partiallyReleasePlayer() {
        if(customYouTubeFragment != null) {
            if(hasVideoEnded()) {
                currentDuration = 0
            } else {
                currentDuration = customYouTubeFragment?.getCurrentDuration() ?: 0
            }
            partiallyReleased = true
        }

        releasePlayer()
    }

    override fun releasePlayer() {
        Logger.d(LOG_TAG, "releasePlayer")
        logAdEndEvent()
        ytMediaContainer?.removeAllViews()
        youtubeIframePlayer?.pausePlay()

        if (null != tvVideoWebView) {
            PlayerUtils.resetWebViewState(tvVideoWebView)
            tvVideoWebView = null
        }
        youtubeIframePlayer = null
        if (customYouTubeFragment?.isInitializationHappened == true) {
            customYouTubeFragment!!.release()
            customYouTubeFragment = null
        }
    }


    override fun setFullScreenMode(fullScreenMode: Boolean) {
        customYouTubeFragment?.setFullScreeMode(fullScreenMode)
    }

    override fun isInFullScreenMode(): Boolean {
        return customYouTubeFragment?.isInFullScreenMode == true
    }

    override fun hasVideoEnded(): Boolean {
        customYouTubeFragment?.let {
            return customYouTubeFragment?.isVideoComplete == true
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        if (null != currentPageReferrer && currentPageReferrer!!.referrerSource !== NewsReferrerSource.NEWS_DETAIL_VIEW) {
        }
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun pause() {
        ViewUtils.setScreenAwakeLock(false, context, LOG_TAG)
        Logger.d(LOG_TAG, "pause")
        if (youtubeIframePlayer != null) {
            youtubeIframePlayer!!.pausePlay()
        }
        customYouTubeFragment?.pause()
    }

    fun handlePlayerState(playerEvent: PlayerEvent) {
        Logger.d(LOG_TAG, "handlePlayerState, id: ${item?.id} and Event: " +
                "${playerEvent.playerState}, msg: ${playerEvent.msg}")
        when (playerEvent.playerState) {
            //Anything specfic to handle here
        }
        pStateLiveData.setValue(playerEvent)
    }


    override fun setEndAction(endAction: PlayerVideoEndAction) {
        if (youtubeIframePlayer != null) {
            youtubeIframePlayer!!.setEndAction(endAction)
        }
        customYouTubeFragment?.setEndAction(endAction, NhAnalyticsEventSection.TV)
    }

    //For Youtube Ads
    fun setEndAction() {
        if (youtubeIframePlayer != null) {
            youtubeIframePlayer!!.setEndAction(PlayerVideoEndAction.COMPLETE)
        }
        customYouTubeFragment?.setEndAction(PlayerVideoEndAction.COMPLETE,
                NhAnalyticsEventSection.ADS)
    }

    override fun setStartAction(startAction: PlayerVideoStartAction) {
        ViewUtils.setScreenAwakeLock(true, context, LOG_TAG)
        videoStartAction = startAction
        if (youtubeIframePlayer != null) {
            youtubeIframePlayer!!.setStartAction(startAction)
        }
        customYouTubeFragment?.setStartAction(startAction)
    }

    override fun resume() {
        if(partiallyReleased && customYouTubeFragment == null) {
            loadYoutubeVideo()
            partiallyReleased = false
        } else {
            customYouTubeFragment?.play()
            youtubeIframePlayer?.startPlay()
        }
    }

    override fun onBackPressed() {
        customYouTubeFragment?.closeFullscreen()
        if (customYouTubeFragment?.isVideoComplete == true) {
            pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, item?.id))
        }
        if (null != tvVideoWebView) {
            tvVideoWebView!!.onBackPressed()
        }
//        if (youtubeIframePlayer != null && youtubeIframePlayer!!.isVideoCompplete &&
//                playerCallbacks != null) {
//            //TODO::Vinod::Handle
//            playerCallbacks.onVideoEnd()
//        }
    }

    override fun unmutePlayerOnDeviceVolumeRaised() {
    }

    override fun getPlayerStateLiveData(): MutableLiveData<PlayerEvent>? {
        return pStateLiveData
    }

    override fun restartVideo() {
        customYouTubeFragment?.restart()
    }

    override fun releaseAndSetReload() {
        partiallyReleasePlayer()
    }

    override fun setViewLayoutParams(params: RelativeLayout.LayoutParams) {
        //Nothing to do here ..
    }

    private fun setYoutubeLayoutParams() {
        val width = CommonUtils.getDeviceScreenWidth()
        val height = width * 9 / 16
        val youtubeLayoutParams = RelativeLayout.LayoutParams(width,
                height + 2)
        youtubeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)

        ytMediaContainer?.layoutParams = youtubeLayoutParams
        touch_handling_for_crash!!.layoutParams = youtubeLayoutParams
    }


    private fun loadYoutubeVideo() {
        Logger.d(LOG_TAG, "loadYoutubeVideo") //Clear any previous player
        releasePlayer()
        ViewUtils.setScreenAwakeLock(true, context, LOG_TAG)
        if (!isAdded) {
            return
        }

        if (item!!.isUseIFrameForYTVideos || PlayerUtils.isUseYoutubeWebviewIFrame()) {
            loadVideoInWebView()
        } else {
            loadVideoInYoutbePlayer()
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        // This makes sure that the container activity has implemented
        // the callback interface. I
        if (activity != null && activity is ReferrerProvider) {
            referrerProvider = activity
        }
    }

    private fun loadVideoInYoutbePlayer() {
        Logger.d(LOG_TAG, "loadVideoInYoutbePlayer")
        customYouTubeFragment = getCustomYoutubeFragment()
        handleTouchTillYoutubeLoads()
        pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PREPARE_IN_PROGRESS, item?.id))
        ytMediaContainer?.visibility = View.VISIBLE


        val fragmentManager = childFragmentManager ?: return

        customYouTubeFragment?.playerStateLiveData?.observe(this,
                androidx.lifecycle.Observer {
                    handlePlayerState(it)
                })


        customYouTubeFragment?.let {
            customYouTubeFragment?.setStartAction(videoStartAction)
            if(customYouTubeFragment?.youtubePlayerFragment != null) {
                fragmentManager.beginTransaction().replace(R.id.yt_media_container,
                        customYouTubeFragment?.youtubePlayerFragment as Fragment).commitAllowingStateLoss()
            }
        }
    }

    private fun loadVideoInWebView() {
        Logger.d(LOG_TAG, "loadVideoInWebView")
        //Release Previous Player
        if (!isAdded || activity == null) {
            return
        }
        releasePlayer()
        handleTouchTillYoutubeLoads()
        pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PREPARE_IN_PROGRESS, item?.id))
        ytMediaContainer?.visibility = View.VISIBLE
        PlayerUtils.setYoutubeIFrame()

        val width = CommonUtils.getDeviceScreenWidth()
        val height = width * 9 / 16
        val youtubeLayoutParams = RelativeLayout.LayoutParams(width, height)

        ytMediaContainer?.layoutParams = youtubeLayoutParams
        tvVideoWebView = VideoEnabledWebView(rootView!!.context)
        tvVideoWebView!!.settings.setSupportZoom(false)
        webChromeClient = object : VideoEnabledChromeClient(tvVideoWebView,
                activity!!.window.decorView as FrameLayout) {
            override fun onProgressChanged(view: WebView, progress: Int) {}
        }
        webChromeClient!!.setOnToggledFullscreen { fullscreen, videoView ->
            if (fullscreen) {
                activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                setFullScreenMode(true)
            } else {
                activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                setFullScreenMode(false)
            }
        }

        tvVideoWebView!!.webChromeClient = webChromeClient
        youtubeIframePlayer = YoutubeIframePlayer(activity, item, tvVideoWebView, this,
                referrerProvider, currentPageReferrer, playerCallbacks, getSection(arguments))
        youtubeIframePlayer!!.setStartAction(videoStartAction)
        youtubeIframePlayer!!.setUpWebView()
        tvVideoWebView!!.layoutParams = youtubeLayoutParams
        ytMediaContainer?.setBackgroundColor(Color.BLACK)
        ytMediaContainer?.addView(tvVideoWebView)
        tvVideoWebView!!.webViewClient = InsideWebViewClient()
    }

    override fun isViewInForeground(): Boolean {
        playerCallbacks?.let {
            return it.isViewInForeground()
        }
        return true
    }

    override fun onAdStarted() {
        ViewUtils.setScreenAwakeLock(true, context, LOG_TAG)
        videoPlayBackTimer!!.start()

        //Ad started to play
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
                Runnable {
                    if (!isAdded) {
                        return@Runnable
                    }
                    releaseTouchHandling()
                    pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_AD_START, item?.id))
                    if (item!!.sourceInfo != null && playerCallbacks != null) {
                        PlayerInlineVideoAdBeaconHandler.getInstance()
                                .onAdStart(null, item!!.sourceInfo.sourceId, fragmentId)
                    }
                }, 500)

    }

    override fun onVideoStarted() {
        //Video is ready to Play
        releaseTouchHandling()
        pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, item?.id))
        logAdEndEvent()
    }

    private fun logAdEndEvent() {
        if (videoPlayBackTimer == null) {
            return
        }
        videoPlayBackTimer!!.stop()

        if (videoPlayBackTimer!!.totalTime > 1) {
            PlayerInlineVideoAdBeaconHandler.getInstance().onAdEnd()
            setEndAction()
            videoPlayBackTimer!!.reset()
        }
    }

    override fun onVideoEnded() {
        //Youtube Video ended callback from player
        pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, item?.id))
    }

    override fun releaseTouchHandler() {
        releaseTouchHandling()
    }

    override fun onError(errorReason: YouTubePlayer.ErrorReason) {
        Logger.d(LOG_TAG, "YouTubePlayer.ErrorReason::$errorReason")

        releaseTouchHandling()
        if (errorReason != YouTubePlayer.ErrorReason.NETWORK_ERROR && errorReason != YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY) {
            val tvErrorReportService = PlayerErrorReportServiceImpl(activity)
            tvErrorReportService.reportVideoError(PlayerErrorInfo(item!!, errorReason.name))
            //Depending on error type switch to iFrame
            if (shouldSwitchToIFrame(errorReason)) {
                loadVideoInWebView()
            }else {
                partiallyReleasePlayer()
                pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_ERROR, item?.id, errorReason.name))
            }
        }
    }

    private fun shouldSwitchToIFrame(errorReason: YouTubePlayer.ErrorReason): Boolean {

        if(!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())){
            return false
        }

        when (errorReason) {

            //Switch to iFrame
            YouTubePlayer.ErrorReason.NETWORK_ERROR -> return CommonUtils.isNetworkAvailable(CommonUtils.getApplication())
            YouTubePlayer.ErrorReason.BLOCKED_FOR_APP,
            YouTubePlayer.ErrorReason.PLAYER_VIEW_TOO_SMALL,
            YouTubePlayer.ErrorReason.PLAYER_VIEW_NOT_VISIBLE,
            YouTubePlayer.ErrorReason.INTERNAL_ERROR,
            YouTubePlayer.ErrorReason.UNKNOWN -> return true
            //stay back in player with error state
            YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY ->
                //No explicit Toast needed as player gets pause
                return false
            YouTubePlayer.ErrorReason.NOT_PLAYABLE,
            YouTubePlayer.ErrorReason.EMBEDDING_DISABLED,
            YouTubePlayer.ErrorReason.EMPTY_PLAYLIST,
            YouTubePlayer.ErrorReason.USER_DECLINED_RESTRICTED_CONTENT,
            YouTubePlayer.ErrorReason.USER_DECLINED_HIGH_BANDWIDTH,
            YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION -> {
                showPlayErrorToast(CommonUtils.getString(R.string.tv_media_player_error), true)
                return false
            }
            YouTubePlayer.ErrorReason.AUTOPLAY_DISABLED -> {
                showPlayErrorToast(CommonUtils.getString(R.string.tv_media_player_error), false)
                return false
            }
            else -> return false
        }

    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer,
                                         wasRestored: Boolean) {
        Logger.d(LOG_TAG, "onInitializationSuccess")
        PlayerUtils.setYoutubePlayerAvailable(true)
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider,
                                         youTubeInitializationResult: YouTubeInitializationResult) {
        Logger.d(LOG_TAG, "onInitializationFailure::$youTubeInitializationResult")
        if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
            //Open video in IFrame Player
            loadVideoInWebView()
        } else {
            partiallyReleasePlayer()
            pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_ERROR, item?.id, "YT initialization Failed"))
            PlayerUtils.setYoutubePlayerAvailable(false)
        }
    }

    override fun onFullscreen(isFullscreen: Boolean) {
        if(!isVisible) {
            return
        }
        item!!.isInExpandMode = isFullscreen
        setFullScreenMode(isFullscreen)
    }

    private fun hideAfterDelay() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            if (!isAdded) {
                return@Runnable
            }
            releaseTouchHandling()
        }, 500)
    }

    override fun onYIFramePlayerReady() {
        if (!isAdded) {
            return
        }
        Logger.d(LOG_TAG, "onYIFramePlayerReady")
        //Video is ready to Play
        hideAfterDelay()

        pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, item?.id))
    }

    override fun onYIFramePlayerError() {
        if (!isAdded) {
            return
        }
        Logger.d(LOG_TAG, "onYIFramePlayerError")
        releaseTouchHandling()
    }

    override fun onYIFramePlayerComplete() {
        pStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, item?.id))
    }

    override fun isFragmentAdded(): Boolean {
        return isAdded
    }


    fun setCustomYouTubeFragment(customYouTubeFragment: CustomYouTubeFragment) {
        if (!isAdded) {
            return
        }
        this.customYouTubeFragment = customYouTubeFragment
    }


    private fun showNoNetworkToast() {
        if (!isAdded) {
            return
        }
        try {
            FontHelper.showCustomFontToast(activity, getString(com.newshunt.common.util.R.string.no_connection_error),
                    Toast.LENGTH_SHORT)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

    }

    private fun showPlayErrorToast(message: String, moveToNextVideo: Boolean) {
        if (!isAdded) {
            return
        }
        try {
            FontHelper.showCustomFontToast(activity, message, Toast.LENGTH_SHORT)
            if (moveToNextVideo) {
                onYIFramePlayerComplete()
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

    }

    private inner class InsideWebViewClient : NhWebViewClient() {
        override// Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return true
        }
    }

    override fun onDestroyView() {
        if (null != tvVideoWebView) {
            tvVideoWebView!!.destroy()
        }
        super.onDestroyView()
    }

    override fun resumeOnNetworkError(isPaused: Boolean) {
        when {
            customYouTubeFragment != null -> customYouTubeFragment!!.play()
            youtubeIframePlayer != null -> youtubeIframePlayer!!.startPlay()
            else -> loadYoutubeVideo()
        }
    }

    companion object {
        private val LOG_TAG = "PlayerFragmentYoutube"
    }
}
