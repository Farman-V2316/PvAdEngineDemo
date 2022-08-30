package com.dailyhunt.tv.players.player

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.dailyhunt.tv.players.listeners.PlayerCustomYoutubeListener
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.news.util.NewsConstants

/**
 * Wrapper over YoutubePlayerSupportFragment to allow releasing of player and extracting videoId
 *
 * @author shashikiran.nr on 8/1/2016.
 */
class CustomYouTubeFragment(val youtubePlayerFragment: YouTubePlayerSupportFragment,
                            videoUrl: String, private val autoPlay: Boolean, private val item: PlayerAsset,
                            videoId: String,
                            private var customYoutubeListener: PlayerCustomYoutubeListener?,
                            private val playerCallbacks: PlayerCallbacks?,
                            section: NhAnalyticsEventSection,
                            private val currentDuration: Int) : YouTubePlayer.OnInitializedListener {
    private var videoId: String? = null
    private var youtubePlayer: YouTubePlayer? = null
    private var isReleased: Boolean = false
    private var isReleaseDeferred: Boolean = false
    var isInitializationHappened = false
    private var isPaused = false
    private var initialStartTime: Long = 0
    private var videoPaused: Boolean = false
    var isVideoComplete: Boolean = false
    var isFullScreen = false
    val playerStateLiveData = MutableLiveData<PlayerEvent>()

    /**
     * Youtube playback event listener
     */
    private val playbackEventListener = object : YouTubePlayer.PlaybackEventListener {

        override fun onBuffering(arg0: Boolean) {
            Logger.d(TAG, "onBuffering")
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_BUFFERING, item?.id)
        }

        override fun onPaused() {
            Logger.d(TAG, "onPaused")
            isPaused = true
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_PAUSED, item?.id)
        }

        override fun onPlaying() {
            Logger.d(TAG, "onPlaying")
            if (initialStartTime > 0L) {
                initialStartTime = 0L
            }
            if (isPaused) {
                playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, item?.id)
            } else {
                playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_PLAYING, item?.id)
            }

            isPaused = false
        }

        override fun onSeekTo(arg0: Int) {

            Logger.d(TAG, "onSeekTo")
        }

        override fun onStopped() {
            Logger.d(TAG, "onStopped")
            if (videoPaused && null != customYoutubeListener) {
                customYoutubeListener!!.releaseTouchHandler()
            }
        }
    }

    /**
     * Youtube play state change listener
     */
    private val playerStateChangeListener = object : YouTubePlayer.PlayerStateChangeListener {
        override fun onLoading() {
            Logger.d(TAG, "onLoading")
            isVideoComplete = false
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_PREPARE_IN_PROGRESS, item?.id)
        }

        override fun onLoaded(s: String) {
            Logger.d(TAG, "onLoaded")
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_READY, item?.id)
        }

        override fun onAdStarted() {
            Logger.d(TAG, "onAdStarted")
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_AD_START, item?.id)
            if (null != customYoutubeListener) {
                customYoutubeListener!!.onAdStarted()
            }
        }

        override fun onVideoStarted() {
            Logger.d(TAG, "onVideoStarted")
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, item?.id)
            isVideoComplete = false
            if (null != customYoutubeListener) {
                customYoutubeListener!!.onVideoStarted()
            }

            if (videoPaused && null != customYoutubeListener) {
                customYoutubeListener!!.releaseTouchHandler()
                videoPaused = false
            }

        }

        override fun onVideoEnded() {
            Logger.d(TAG, "onVideoEnded")
            isVideoComplete = true
            playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, item?.id)

            if (null != customYoutubeListener) {
                customYoutubeListener!!.onVideoEnded()
            }
        }

        override fun onError(errorReason: YouTubePlayer.ErrorReason) {
            Logger.d(TAG, "onError")
            if (null != customYoutubeListener) {
                customYoutubeListener!!.onError(errorReason)
            } else {
                playerStateLiveData.value = PlayerEvent(PLAYER_STATE.STATE_ERROR, item?.id, errorReason.name)
            }
        }
    }

    val isInFullScreenMode: Boolean
        get() = isFullScreen


    init {
        this.videoId = videoId
        if (CommonUtils.isEmpty(videoId)) {
            this.videoId = PlayerUtils.extractYouTubeVideoId(videoUrl)
        }

        youtubePlayerFragment.initialize(NewsConstants.YOUTUBE_DEVELOPER_KEY, this)
        callYouTubeHandler()
    }

    fun release() {
        if (isReleased) {
            return
        }
        if (youtubePlayer != null && isInitializationHappened) {
            try {
                //patch for Youtube playing in background
                youtubePlayer!!.pause()
                youtubePlayer!!.seekToMillis(youtubePlayer!!.durationMillis)
            } catch (e: Exception) { //Safer catch for IllegalStateException
                Logger.caughtException(e)
            }

            youtubePlayer!!.release()
            youtubePlayer = null
            isReleased = true
        } else {
            isReleaseDeferred = true
        }
    }


    fun getCurrentDuration(): Int {
        return youtubePlayer?.currentTimeMillis?:0
    }

    fun pause() {
        Logger.d(TAG, "pause")
        videoPaused = true
        if (youtubePlayer != null && isInitializationHappened && !isReleased) {
            try {
                youtubePlayer!!.pause()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun play() {
        Logger.d(TAG, "play")
        videoPaused = false
        if (youtubePlayer != null && isInitializationHappened && !isReleased) {
            try {
                youtubePlayer!!.play()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    fun restart() {
        Logger.d(TAG, "restart")
        videoPaused = false
        if (youtubePlayer != null && isInitializationHappened && !isReleased) {
            try {
                youtubePlayer!!.seekToMillis(0)
                youtubePlayer!!.play()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    fun setPlayerStyle(playerStyle: YouTubePlayer.PlayerStyle) {
        Logger.d(TAG, "setting the player style")
        if (youtubePlayer != null && isInitializationHappened && !isReleased) {
            try {
                youtubePlayer!!.setPlayerStyle(playerStyle)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }


    fun setEndAction(endAction: PlayerVideoEndAction, section: NhAnalyticsEventSection) {
        Logger.d(TAG, "setEndAction")
            if (youtubePlayer != null) {
                var elapsedTimeInMillis: Long = 0
                try {
                    if (!isReleased) {
                        elapsedTimeInMillis = youtubePlayer!!.currentTimeMillis.toLong()
                    }
                } catch (e: Exception) {
                    Logger.caughtException(e)
                }
            }
    }

    fun setStartAction(startAction: PlayerVideoStartAction) {
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider,
                                         youTubePlayer: YouTubePlayer,
                                         wasRestored: Boolean) {
        isInitializationHappened = true
        HANDLER.removeCallbacksAndMessages(null)
        youtubePlayer = youTubePlayer
        if (null != customYoutubeListener) {
            customYoutubeListener!!.onInitializationSuccess(provider, youTubePlayer, wasRestored)
        }

        /*
    Wrapping it inside try-catch block because if onInitializationSuccess is called after the
    fragment is destroyed, then it will throw exception.
     */
        try {
            youtubePlayer!!.fullscreenControlFlags = YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION or YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI

            youtubePlayer!!.setPlaybackEventListener(playbackEventListener)
            youtubePlayer!!.setPlayerStateChangeListener(playerStateChangeListener)

            if (isReleaseDeferred) {
                youTubePlayer.release()
                return
            }

            initialStartTime = System.currentTimeMillis()

            if (!wasRestored) {
                if (!videoPaused && customYoutubeListener?.isViewInForeground() == true) {
                    youtubePlayer!!.loadVideo(videoId, currentDuration)
                } else {
                    youTubePlayer.cueVideo(videoId)
                    customYoutubeListener?.releaseTouchHandler()
                }
            }
            youTubePlayer.setOnFullscreenListener {
                _isFullScreen -> customYoutubeListener!!.onFullscreen(_isFullScreen)
                isFullScreen = _isFullScreen
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider,
                                         youTubeInitializationResult: YouTubeInitializationResult) {
        isInitializationHappened = true
        HANDLER.removeCallbacksAndMessages(null)
        if (null != customYoutubeListener) {
            customYoutubeListener!!.onInitializationFailure(provider, youTubeInitializationResult)
        }
    }

    fun closeFullscreen() {
        if (youtubePlayer != null) {
            youtubePlayer!!.setFullscreen(false)
        }
    }

    private fun callYouTubeHandler() {
        HANDLER.removeCallbacksAndMessages(null)
        HANDLER.postDelayed({ isInitializationHappened = true }, YOUTUBE_PLAYER_INIT_TIMEOUT.toLong())
    }

    fun setFullScreeMode(fullScreeMode: Boolean) {
        playerCallbacks?.toggleUIForFullScreen(fullScreeMode)
    }

    fun releaseHandler() {
        HANDLER?.removeCallbacksAndMessages(null)
    }

    companion object {

        private val TAG = "CustomYouTubeFragment"
        private val HANDLER = Handler()
        private val YOUTUBE_PLAYER_INIT_TIMEOUT = 5000

        fun newInstance(item: PlayerAsset, videoUrl: String,
                        autoPlay: Boolean,
                        videoId: String,
                        customYoutubeListener: PlayerCustomYoutubeListener,
                        playerCallbacks: PlayerCallbacks?,
                        section: NhAnalyticsEventSection,
                        currentDuration: Int): CustomYouTubeFragment {
            return CustomYouTubeFragment(YouTubePlayerSupportFragment.newInstance(), videoUrl,
                    autoPlay, item, videoId, customYoutubeListener,
                    playerCallbacks, section, currentDuration)
        }
    }

}
