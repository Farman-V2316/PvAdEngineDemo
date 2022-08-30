/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.player

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.dailyhunt.tv.exolibrary.entities.StreamConfigAsset
import com.dailyhunt.tv.exolibrary.interceptors.HttpNotFoundInterceptor
import com.dailyhunt.tv.exolibrary.util.ExoUtils
import com.dailyhunt.tv.ima.exo.ImaAdsLoader
import com.dailyhunt.tv.players.ads.AdsMediaSourceListener
import com.dailyhunt.tv.players.analytics.PlayerAnalyticsHelper
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.helpers.ExoMediaSourceGenerator
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.listeners.ExoPlayerListenerDH
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.video.VideoListener
import com.newshunt.adengine.instream.IAdLogger
import com.newshunt.adengine.listeners.PlayerInstreamAdListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.sdk.network.NetworkSDK
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.squareup.otto.Subscribe
import java.io.IOException
import java.net.URLDecoder

/**
 * @author vinod
 */
private const val TAG = "ExoPlayerDH"
class ExoPlayerDH(private val configAsset: StreamConfigAsset,
                  var playerListener: ExoPlayerListenerDH?,
                  private var playerAsset: ExoPlayerAsset? = null) {
    private var exoPlayer: SimpleExoPlayer
    private var adsLoader: ImaAdsLoader? = null
    private var externalSdkAd: ExternalSdkAd? = null
    private var audioAttributes: AudioAttributes? = null
    private var lastPlayedDuration: Long = -1
    private var lastWindowIndex = 0
    private var playerErrorFromSource = false
    private var play_complete = false
    private var isPlayReady: Boolean = false
    private var isPlayStart: Boolean = false
    private var handler = Handler(Looper.getMainLooper())
    private var BUFFER_HACK_WAIT_TIME: Long = 8000L
    private var isBusRegistered: Boolean = false
    private var mediaSource: MediaSource? = null
    private var adEntity: BaseAdEntity? = null
    private var adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper? = null
    private var adsMediaSourceListener: AdsMediaSourceListener? = null
    val playerStateLiveData = MutableLiveData<PlayerEvent>()
    private var playerState: PLAYER_STATE = PLAYER_STATE.STATE_IDLE
    private var isSeekVideoAfterEnd: Boolean = false
    private var isFirstRenderDone: Boolean = false

    init {
        exoPlayer = ExoUtils.buildPlayer(configAsset, ExoVideoListener(), PlayerListener(),
                playerAsset ?: "Live${SystemClock.elapsedRealtime()}",
                PlayerAnalyticsHelper::logPA)
        audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
        registerBus()
    }

    fun getPlayer(): SimpleExoPlayer {
        return exoPlayer
    }

    fun mute(state: Boolean) {
        if (state) {
            exoPlayer.volume = 0.0f
        } else {
            exoPlayer.volume = 1.0f
            if (exoPlayer.currentPosition > 10 && exoPlayer.playWhenReady) {
                exoPlayer.setAudioAttributes(audioAttributes!!, true)
            }
        }
        adsLoader?.onPlayerVolumeChanged(exoPlayer.volume)
    }

    fun registerBus() {
        if (isBusRegistered.not()) {
            BusProvider.getUIBusInstance().register(this)
            isBusRegistered = true
            NetworkSDK.bus().register(this)
        }
    }

    fun unregisterBus() {
        try {
            if (isBusRegistered) {
                isBusRegistered = false
                BusProvider.getUIBusInstance().unregister(this)
                NetworkSDK.bus().unregister(this)
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    fun rePrepareVideo(autoPlay: Boolean) {
        registerBus()
        Logger.d(TAG, "repreparing media source for playing again")
        if (playerAsset != null) {
            isPlayStart = false
            exoPlayer = ExoUtils.buildPlayer(configAsset, ExoVideoListener(), PlayerListener(),
                    playerAsset ?: "Live${SystemClock.elapsedRealtime()}",
                    PlayerAnalyticsHelper::logPA)

            loadVideo(playerAsset!!, autoPlay, adEntity, adsTimeSpentOnLPHelper)
            handler.post(updateProgressAction)
            if (lastPlayedDuration > 0 && !playerAsset!!.isGif && playerAsset!!.loopCount == 0) {
                exoPlayer.seekTo(lastWindowIndex, lastPlayedDuration)
            }
        }
    }


    @Synchronized
    fun loadVideo(asset: ExoPlayerAsset, autoPlay: Boolean, adEntity: BaseAdEntity?,
        adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?, companionView: ViewGroup? = null) {
        Logger.d(TAG, "loadVideo id :: ${asset.id}")
        this.adEntity = adEntity
        this.adsTimeSpentOnLPHelper = adsTimeSpentOnLPHelper
        val filteredAd = filterAd(adEntity)

        if (filteredAd is ExternalSdkAd) {
            this.externalSdkAd = filteredAd
        }
        this.playerAsset = asset
        exoPlayer.playWhenReady = autoPlay
        playerListener?.showOverlayViewIfNotVisible()

        if(Logger.loggerEnabled()) {
            Logger.d(TAG, "ExoDownload:: loadVideo playing URL :: ${playerAsset?.videoUrl}")
            if(!CommonUtils.isEmpty(externalSdkAd?.external?.tagURL))
                Logger.d(TAG, "loadVideo externalSdkAd :: " + URLDecoder.decode(externalSdkAd?.external?.tagURL))
        }

        mediaSource = updateMediaSourceBasedOnAd(companionView)
        playerState = PLAYER_STATE.STATE_PREPARE_IN_PROGRESS
        playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PREPARE_IN_PROGRESS, playerAsset?.id))
        if (mediaSource == null) {
            //don't prepare
            return
        }
        exoPlayer.prepare(mediaSource!!, false, false)
        if(playerAsset?.isGif == true || playerAsset?.isHideControl == true) {
            mute(true)
        } else {
            mute(configAsset.mute)
        }
        playerListener?.setPlayer(exoPlayer)
        if (!autoPlay) {
            Logger.d(TAG, "autoPlay returning :: $playerAsset")
            return
        }
        Logger.d(TAG, "loadVideo playing :: $playerAsset")

    }

    @Synchronized
    fun reloadVideoOnAdError(seekPosition: Long) {
        Logger.d(TAG, "reloadVideoOnAdError - seekPosition : $seekPosition")
        val mediaSource = ExoMediaSourceGenerator.getMappedMediaSourceOnly(playerAsset!!)
        exoPlayer.prepare(mediaSource, true, false)
        if (seekPosition > 0) {
            exoPlayer.seekTo(seekPosition)
        }
        exoPlayer.playWhenReady = true
    }

    fun pause(): Boolean {
        handler.removeCallbacks(updateProgressAction)
        Logger.d(TAG, "pause exoPlayer.playWhenReady -  ${exoPlayer.playWhenReady} for id : ${playerAsset?.id}")
        if (exoPlayer.playWhenReady) {
            //TO handle IllegalStateException : Handler sending message on a dead thread
            handler.post {
                exoPlayer.playWhenReady = false
            }
            return true
        }
        return false
    }

    fun resume(): Boolean {
        Logger.d(TAG, "resume exoPlayer.playWhenReady -  ${exoPlayer.playWhenReady} for id : ${playerAsset?.id}")
        var changeStatus = false
        if (!exoPlayer.playWhenReady) {
            exoPlayer.playWhenReady = true
            changeStatus = true
        }
        handler.post(updateProgressAction)
        return changeStatus
    }

    fun restart() {
        Logger.d(TAG, "restart :: ")
        playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_IDLE, playerAsset?.id))
        //TO handle IllegalStateException : Handler sending message on a dead thread
        handler.post {
            exoPlayer.playWhenReady = true
        }
        isSeekVideoAfterEnd = false
        resume()
    }

    fun seekToStartAndPause() {
        isSeekVideoAfterEnd = true
        exoPlayer.seekTo(0, 0)
        pause()
    }

    @Subscribe
    fun receive404Event(event: HttpNotFoundInterceptor.VideoEntityNotFoundEvent) {
        Logger.d(TAG, " :: receive404Event ::")
//        if (currentWindow < playlistArray.size - 1) {
//            playNext()
//        }
    }

    /**
     * Whenever resetting mediaSource in this player, need to reset ads UI elements too.
     */
    fun resetAdsLoader() {
        Logger.d(TAG, "Reset ads loader")
        adsLoader?.release()
        adsLoader = null
        adsMediaSourceListener = null
    }

    fun release(reload : Boolean) {
        Logger.d(TAG, "release ${playerAsset?.id}")
        lastPlayedDuration = exoPlayer.contentPosition
        lastWindowIndex = exoPlayer.currentWindowIndex
        if (!reload) {
            resetAdsLoader()
            playerListener = null
        }
        try {
            unregisterBus()
            handler.removeCallbacksAndMessages(null)
            adsLoader?.setPlayer(null)
            mediaSource?.removeEventListener(adsMediaSourceListener)
            exoPlayer.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateMediaSourceBasedOnAd(companionView: ViewGroup?): MediaSource {
        Logger.d(TAG, "updateMediaSourceBasedOnAd")
        if (adsLoader == null && adsMediaSourceListener == null) {
            adsMediaSourceListener = AdsMediaSourceListener(playerAsset!!.id, externalSdkAd, playerListener
                    as PlayerInstreamAdListener, adsTimeSpentOnLPHelper)
            adsLoader = ExoMediaSourceGenerator.buildAdsLoader(externalSdkAd,
                    adsMediaSourceListener!!, companionView)
        }
        adsLoader?.setPlayer(exoPlayer)

        val mediaSource = ExoMediaSourceGenerator.getMappedAdMediaSource(playerAsset!!, adsLoader,
                playerListener as PlayerInstreamAdListener)
        if (mediaSource is AdsMediaSource) {
            IAdLogger.d(TAG, "updateMediaSourceBasedOnAd return AdsMediaSource")
            mediaSource.addEventListener(Handler(), adsMediaSourceListener)
        }
        return mediaSource
    }

//    private fun getMediaSourceWithOutAd(): MediaSource {
//        return DHTVMediaSourceGenerator.getMappedMediaSourceOnly(playerAsset!!)
//    }

    inner class ExoVideoListener : VideoListener {
        override fun onRenderedFirstFrame() {
            playerListener?.onRenderedFirstFrame()
            Logger.d(TAG, " :: ExoVideoListener :: onRenderedFirstFrame ")
            playerListener?.onRenderedFirstFrame()
            isFirstRenderDone = true
            if (exoPlayer.isPlayingAd || isSeekVideoAfterEnd) {
                isSeekVideoAfterEnd = false
                return
            }
            playerState = PLAYER_STATE.STATE_PLAYING
            if (!isPlayStart) {
                isPlayStart = true
                playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, playerAsset?.id))
            } else {
                playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PLAYING, playerAsset?.id))
            }
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            Logger.d(TAG, " :: ExoVideoListener :: onSurfaceSizeChanged ")
        }

        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            Logger.d(TAG, " :: ExoVideoListener :: onVideoSizeChanged ")
        }
    }


    inner class PlayerListener : Player.EventListener {

        override fun onPlaybackParametersChanged(p0: PlaybackParameters) {
            Logger.d(TAG, " :: Playlist manager :: onPlaybackParametersChanged ")
        }

        override fun onSeekProcessed() {
            Logger.d(TAG, " :: Playlist manager :: onSeekProcessed ")
        }


        override fun onTracksChanged(p0: TrackGroupArray, p1: TrackSelectionArray) {
            Logger.d(TAG, " :: Playlist manager :: onTracksChanged ")
            playerState = PLAYER_STATE.STATE_PLAYING
            if (!isPlayStart) {
                isPlayStart = true
                playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, playerAsset?.id))
            } else {
                playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PLAYING, playerAsset?.id))
            }
        }

        override fun onPlayerError(ex: ExoPlaybackException) {
            Logger.d(TAG, "onPlayerError  :: ExoPlaybackException type ${ex?.type}, " +
                    "msg ${ex?.cause?.message}")
            when (ex?.type) {
                ExoPlaybackException.TYPE_SOURCE -> Logger.d(TAG, ex.sourceException.message)
                ExoPlaybackException.TYPE_RENDERER -> Logger.d(TAG, ex.rendererException.message)
                ExoPlaybackException.TYPE_UNEXPECTED -> Logger.d(TAG, ex.unexpectedException.message)
            }
            playerListener?.logVideoError(ex)
            if (ExoUtils.exoPlayerExceptionHandler(ex, playerAsset?.isLiveStream == true)) {
                reloadVideoOnAdError(0)
                return
            }

            playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_ERROR, playerAsset?.id, ex.toString()))
            if (ex?.type == ExoPlaybackException.TYPE_SOURCE || ex?.type == ExoPlaybackException.TYPE_UNEXPECTED) {
                playerErrorFromSource = true
                lastPlayedDuration = exoPlayer.contentPosition
            }
            playerListener?.onVideoError(ex)
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Logger.d(TAG, " :: Playlist manager :: onLoadingChanged $isLoading")
            Logger.d(TAG, " :: Playlist manager :: isAdPlaying ${exoPlayer.isPlayingAd}")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            Logger.d(TAG, " :: Playlist manager :: onPositionDiscontinuity :: $repeatMode")
        }

        override fun onShuffleModeEnabledChanged(p0: Boolean) {
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?, @Player.TimelineChangeReason reason: Int) {
            Logger.d(TAG, "tineline changed :: ")
            Logger.d(TAG, " :: Playlist manager :: onTimelineChanged ::  $timeline :: $manifest")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Logger.d(TAG, " :: onPlayerStateChanged  :: playbackState ::  $playbackState :: " +
                    "playWhenReady :: $playWhenReady and id - ${playerAsset?.id}")
            handler.removeCallbacks(runnableForSeekHack)
            when (playbackState) {
                Player.STATE_READY -> {
                    playerState = PLAYER_STATE.STATE_READY
                    if (!isPlayReady) {
                        isPlayReady = true
                        playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_READY, playerAsset?.id))
                        handler.post(updateProgressAction)
                    }
                    play_complete = false
                    Logger.d(TAG, " ::Player state : ${exoPlayer.playWhenReady}")
                    if (playerListener != null && playerListener?.isViewInForeground() == false) {
                        Logger.d(TAG, " ::Player paused bcos video not in foreground ")
                        //pause the playing video
                        exoPlayer.playWhenReady = false
                        return
                    }

                    if (exoPlayer.playWhenReady && exoPlayer.volume > 0 && audioAttributes != null) {
                        exoPlayer.setAudioAttributes(audioAttributes!!, true)
                    }

                    if (!exoPlayer.playWhenReady) {
                        playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PAUSED, playerAsset?.id))
                    }

                    //After FirstFrameRender, Call Ready should be honor to pass state Playing
                    if (isFirstRenderDone && exoPlayer.playWhenReady) {
                        Logger.d(TAG, " :: missedOnFirstRender > Re trigger Event")
                        if (!isPlayStart) {
                            isPlayStart = true
                            playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_START, playerAsset?.id))
                        } else {
                            playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_PLAYING, playerAsset?.id))
                        }
                    }
                }
                Player.STATE_ENDED -> {
                    if (!play_complete) {
                        playerState = PLAYER_STATE.STATE_VIDEO_END
                        handler.removeCallbacks(updateProgressAction)
                        Logger.d(TAG, " :: On player state changed  :: Player.STATE_ENDED ")
                        if (!play_complete) {
                            playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_VIDEO_END, playerAsset?.id))
                        }
                        play_complete = true
                    }
                }
                Player.STATE_BUFFERING -> {
                    if (Build.VERSION.SDK_INT < 27) {
                        Logger.d(TAG, "::scheduleProceedBySeekHack ::Hack Timer Started")
                        handler.postDelayed(runnableForSeekHack, BUFFER_HACK_WAIT_TIME)
                    }
                    playerState = PLAYER_STATE.STATE_BUFFERING
                    playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_BUFFERING, playerAsset?.id))
                }
                Player.STATE_IDLE -> {
                    playerState = PLAYER_STATE.STATE_IDLE
                    playerStateLiveData.setValue(PlayerEvent(PLAYER_STATE.STATE_IDLE, playerAsset?.id))
                }
            }
        }
    }

    private val runnableForSeekHack = Runnable {
        if (playerListener != null && playerListener?.isViewInForeground() == true) {
            if (exoPlayer?.currentPosition > 0) {
                Logger.d(TAG, " ::scheduleProceedBySeekHack seek to ${exoPlayer?.currentPosition}")
                exoPlayer?.seekTo(exoPlayer?.currentPosition + 500)
                Logger.d(TAG, " ::scheduleProceedBySeekHack playState ${exoPlayer?.playWhenReady}")
                Logger.d(TAG, " ::scheduleProceedBySeekHack manager :: isAdPlaying ${exoPlayer?.isPlayingAd}")
            }
        }
    }

    private val updateProgressAction: Runnable = Runnable { updateProgress() }
    private var playerPosition: Long? = null

    private fun updateProgress() {
        var position = 0L
        if (exoPlayer.playWhenReady) {
            position = C.usToMs(0L)
            position += if (exoPlayer.isPlayingAd) {
                exoPlayer.contentPosition
            } else {
                exoPlayer.currentPosition
            }
        }
        handler.removeCallbacks(updateProgressAction)
        if (playerPosition != position) {
            playerPosition = position
            playerListener?.onTimeUpdate(position)
        }
        val playbackState = exoPlayer.playbackState
        if (playbackState != 1 && playbackState != 4) {
            val delayMs: Long
            if (exoPlayer.playWhenReady && playbackState == 3) {
                val playbackSpeed = exoPlayer.playbackParameters.speed
                when {
                    playbackSpeed <= 0.1f -> delayMs = 1000L
                    playbackSpeed <= 5.0f -> {
                        val mediaTimeUpdatePeriodMs = 1000 / Math.max(1, Math.round(1.0f / playbackSpeed))
                        var mediaTimeDelayMs = mediaTimeUpdatePeriodMs - position % mediaTimeUpdatePeriodMs
                        if (mediaTimeDelayMs < mediaTimeUpdatePeriodMs / 5L) {
                            mediaTimeDelayMs += mediaTimeUpdatePeriodMs
                        }
                        delayMs = if (playbackSpeed == 1.0f)
                            mediaTimeDelayMs
                        else
                            (mediaTimeDelayMs.toFloat() / playbackSpeed).toLong()
                    }
                    else -> delayMs = 200L
                }
            } else {
                delayMs = 1000L
            }
            handler.postDelayed(updateProgressAction, delayMs)
        }
    }

    fun getPlayerState(): PLAYER_STATE {
        return playerState
    }

    fun isPlaying(): Boolean {
        return exoPlayer.playWhenReady
    }

    fun registerVideoControlsOverlay(views: List<View>) {
        adsLoader?.registerVideoControlsOverlay(views)
    }

    private fun filterAd(ad: BaseAdEntity?): BaseAdEntity? {
        ad ?: return null

        if (ad !is MultipleAdEntity) {
            return ad
        }
        if (CommonUtils.isEmpty(ad.baseDisplayAdEntities))
            return null

        ad.baseDisplayAdEntities.forEach {
            if (AdsUtil.isIMAVideoAd(it)) {
                return it
            }
        }
        return null
    }
}