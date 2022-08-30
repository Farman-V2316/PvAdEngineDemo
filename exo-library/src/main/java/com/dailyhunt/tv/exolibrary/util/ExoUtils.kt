/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.util

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.dailyhunt.tv.exolibrary.entities.StreamConfigAsset
import com.dailyhunt.tv.exolibrary.ui.CustomRenderersFactory
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.video.VideoListener
import com.newshunt.common.helper.UserConnectionHolder
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import java.util.*


const val BUFFER_SEGMENT_SIZE = 64 * 1024
const val MIN_DURATION_RETAIN_AFTER_BUFFER_DISCARD = 2000
const val MIN_TIME_BETWEEN_BUFFER_REEVALUTATION = 5000
const val DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2000

class ExoUtils {
    companion object {
        var bwEventListener: BandwidthMeter.EventListener? = null
        private val TAG = "ExoUtils"
        const val PREF_ENABLE_PERF_ANALYTICS = "PREF_ENABLE_PERF_ANALYTICS"
        private var applicationIOScope: CoroutineScope? = null
        @JvmStatic
        val BANDWIDTH_METER by lazy {
            val meter = DefaultBandwidthMeter.Builder(CommonUtils.getApplication())
                .setSlidingWindowMaxWeight(UserConnectionHolder.exoSlidingPercentileMaxWeight)
                .build()
            var mUiUpdateHandler = Handler(Looper.getMainLooper())
            if (bwEventListener == null) {
                Logger.e(TAG, "bwEventListener == null")
            }else {
                meter. addEventListener(mUiUpdateHandler, bwEventListener)
            }
            meter
        }
        @JvmStatic
        @JvmOverloads
        fun buildPlayer(config: StreamConfigAsset? = null,
                        videoListener: VideoListener,
                        listener: Player.EventListener,
                        streamAsset : Any = SystemClock.elapsedRealtime(),
                        analyticsLogger: (PA)-> Unit = {},
                        addLogger: Boolean = false,
                        addAnalyticsListener : Boolean = PreferenceManager.getBoolean(Companion
                            .PREF_ENABLE_PERF_ANALYTICS, false)):
            SimpleExoPlayer {
            if (config?.islive == true && ExoBufferSettings.isShouldUseDefault()) { // use default configs for live-videos
              val builder = SimpleExoPlayer.Builder(CommonUtils.getApplication())
//              builder.setBandwidthMeter(BANDWIDTH_METER)
              val exoPlayer = builder.build()
              exoPlayer.addListener(listener)
              exoPlayer.addVideoListener(videoListener)
              if (addAnalyticsListener) {
                exoPlayer.addAnalyticsListener(PAListener(id = streamAsset, onComplete = analyticsLogger))
              }
              return exoPlayer
            }

            Logger.d(TAG, "****** adaptive settings ****** \n" +
                    "minTimeSwitch Up  :: ${ExoBufferSettings.getHlsMinTimeForSwitchUpMs() / 1000} sec \n" +
                    "minTimeSwitch down :: ${ExoBufferSettings.getHlsMaxTimeForSwitchDownMs() / 1000} sec\n" +
                    "minDuration after buffer discard :: ${MIN_DURATION_RETAIN_AFTER_BUFFER_DISCARD / 1000} sec \n" +
                    "default b/w fraction :: ${AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION} \n" +
                    "minTime buffer reevaluation :: ${MIN_TIME_BETWEEN_BUFFER_REEVALUTATION / 1000} sec \n")


          val adaptivetrackFactory = AdaptiveTrackSelection.Factory(
                    ExoBufferSettings.getHlsMinTimeForSwitchUpMs(),
                    ExoBufferSettings.getHlsMaxTimeForSwitchDownMs(),
                    MIN_DURATION_RETAIN_AFTER_BUFFER_DISCARD,
                    AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION,
                    AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION,
                    MIN_TIME_BETWEEN_BUFFER_REEVALUTATION * 1L,
                    Clock.DEFAULT)
            val trackSelector = DefaultTrackSelector(adaptivetrackFactory)

            var bufferMax = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
            var bufferMin = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS

            if (config != null) {
                if (ExoBufferSettings.getBufferMaxSize() > 0) {
                    bufferMax = ExoBufferSettings.getBufferMaxSize()
                }
                if (ExoBufferSettings.getBufferMinSize() > 0) {
                    bufferMin = ExoBufferSettings.getBufferMinSize()
                }
            }

            if (bufferMin > bufferMax) {
                //switch back to old defaults
                bufferMax = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
                bufferMin = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
            }


            if (Logger.loggerEnabled()) Logger.d("ExoBuffer", "bufferMin - " + bufferMin)
            if (Logger.loggerEnabled()) Logger.d("ExoBuffer", "bufferMax - " + bufferMax)

            val renderersFactory = CustomRenderersFactory(CommonUtils.getApplication())

            //assertion of buffer segment size from server config or fall back to default 65kb size
            var allocator: DefaultAllocator
            try {
                Assertions.checkArgument(ExoBufferSettings.getBufferSegmentSize() > 0)
                allocator = DefaultAllocator(true, ExoBufferSettings.getBufferSegmentSize())
            } catch (e: IllegalArgumentException) {
                allocator = DefaultAllocator(true, BUFFER_SEGMENT_SIZE)
            }

            Logger.d(TAG, "****** Load Control Settings ******* \n" +
                    "bufferSeg size :: ${ExoBufferSettings.getBufferSegmentSize()} Bytes \n" +
                    "initialPlayback buffer :: ${ExoBufferSettings.getInitialBufferMs() / 1000} sec \n" +
                    "playbackDuration after rebuffer :: ${ExoBufferSettings.getPlaybackDurationAfterRebuffer() / 1000} sec \n")

            val loadControlBuilder = DefaultLoadControl.Builder()
                    .setAllocator(allocator)
                    .setBufferDurationsMs(bufferMin, bufferMax,
                            ExoBufferSettings.getInitialBufferMs(),
                            ExoBufferSettings.getPlaybackDurationAfterRebuffer())

            val exoPlayer = SimpleExoPlayer.Builder(CommonUtils.getApplication(), renderersFactory)
                .setTrackSelector(trackSelector).setLoadControl(loadControlBuilder.createDefaultLoadControl())
                .setBandwidthMeter(BANDWIDTH_METER).build()
            exoPlayer.addListener(listener)
            exoPlayer.addVideoListener(videoListener)
            if (addLogger) {
                val logger = EventLogger(trackSelector)
                exoPlayer.addListener(logger)
                exoPlayer.addMetadataOutput(logger)
                exoPlayer.setAudioDebugListener(logger)
                exoPlayer.setVideoDebugListener(logger)
            }
            return exoPlayer
        }

        @JvmStatic
        fun exoPlayerExceptionHandler(e: ExoPlaybackException?, islive: Boolean): Boolean {
          val typeSourceOrUnexpected = e?.type == ExoPlaybackException.TYPE_SOURCE
              || e?.type == ExoPlaybackException.TYPE_UNEXPECTED
          val networkAvailable = NetworkSDKUtils.isNetworkAvailable(CommonUtils.getApplication())
          Logger.d(TAG, "exoPlayerExceptionHandler: $typeSourceOrUnexpected, " +
              "$networkAvailable, $islive")
          return typeSourceOrUnexpected && networkAvailable && islive
        }

        @JvmStatic
        fun stringForTime(timeMs: Long): String? {
            var timeMs = timeMs
            if (timeMs == C.TIME_UNSET) {
                timeMs = 0
            }
            val totalSeconds = (timeMs + 500) / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600
            val fb = StringBuilder()
            fb.setLength(0)
            val fm = Formatter(fb, Locale.getDefault())
            return if (hours > 0) fm.format("%d:%d:%02d", hours, minutes, seconds).toString() else fm.format("%d:%02d", minutes, seconds).toString()
        }

        @JvmStatic
        fun setApplicationIOScope() {
            applicationIOScope = CoroutineScope(SupervisorJob() + IO)
        }

        @JvmStatic
        fun getApplicationIOScope(): CoroutineScope? {
            return applicationIOScope
        }
    }
}