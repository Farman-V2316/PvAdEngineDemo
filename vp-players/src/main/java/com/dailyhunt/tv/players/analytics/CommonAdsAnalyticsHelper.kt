/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.news.helper.VideoPlayBackTimer

private const val MSG_SCREEN_PAUSED = 10001

/**
 * Helper to handle video ads instrumentation
 *
 * Created by umesh.isran on 11/26/19.
 */
class CommonAdsAnalyticsHelper() {

    private val LOG_TAG = CommonAdsAnalyticsHelper::class.java.simpleName

    private var videoPlayBackTimer = VideoPlayBackTimer()
    private var isVideoTimerStarted = false
    var isNewVideoPlayEntry = true
    var videoPlayedEventId: Long = 0
    private var isTimespentPaused: Boolean = false
    private var pausedEventDelayTimeMs = 0

    init {
        pausedEventDelayTimeMs = PreferenceManager.getPreference(
                GenericAppStatePreference.PAUSED_VIDEO_EVENT_DELAY_MS, Constants.TIMESPENT_PAUSE_DELAY)
    }

    fun startVPEvent(videoStartTime: Long, map: MutableMap<String, Any>) {
        Logger.d(LOG_TAG, "startVPEvent")
        if(isNewVideoPlayEntry) {
            addNewEntry(videoStartTime, map)
            Logger.d(LOG_TAG, "startVPEvent new " + videoPlayedEventId)
        } else {
            Logger.d(LOG_TAG, "startVPEvent existing " + videoPlayedEventId)
        }

        startVideoPlayBackTimer()
    }

    private fun addNewEntry(videoStartTime: Long, map: MutableMap<String, Any>) {
        videoPlayedEventId = SystemClock.elapsedRealtime()
        VideoTimespentHelper.postCreateTimespentEvent(videoPlayedEventId, map)
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.PLAYBACK_DURATION.getName(), "0")
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.EVENT_NAME.getName(), NhAnalyticsAppEvent.VIDEO_PLAYED.name)
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.START_TIME.getName(), java.lang.Long.toString(videoStartTime))
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.SYSTEM_VIDEO_START_TIME.getName(), System.currentTimeMillis().toString())
        isNewVideoPlayEntry = false
    }

    @Synchronized
    fun logVPEvent(endAction: PlayerVideoEndAction, videoEndTime: Long) {
        Logger.d(LOG_TAG, "logVPEvent")
        if(endAction == null || isNewVideoPlayEntry) {
            return
        }
        val duration= getPlayBackDuration()
        resetVideoPlayBackTimer()
        if(duration <= 1000L) {
            Logger.d(LOG_TAG, "played duration <= 1sec " + endAction)
            return
        } else {
            Logger.d(LOG_TAG, "played duration = " + duration + endAction)
        }
        when (endAction) {
            PlayerVideoEndAction.PAUSE,
            PlayerVideoEndAction.MINIMIZE,
            PlayerVideoEndAction.AD_START,
            PlayerVideoEndAction.BOTTOM_SHEET_EXPAND -> {
                isTimespentPaused = true
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.END_ACTION.getName(), endAction.name)
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.END_TIME.getName(), java.lang.Long.toString(videoEndTime))
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.EVENT_SECTION.getName(), NhAnalyticsEventSection.ADS.name)
                VideoTimespentHelper.updateVideoDuration(videoPlayedEventId, duration)
                handler.sendMessageDelayed(Message.obtain(handler, MSG_SCREEN_PAUSED),
                        pausedEventDelayTimeMs.toLong())
            }

            PlayerVideoEndAction.SWIPE,
            PlayerVideoEndAction.SCROLL,
            PlayerVideoEndAction.COMPLETE,
            PlayerVideoEndAction.APP_BACK,
            PlayerVideoEndAction.APP_EXIT,
            PlayerVideoEndAction.SKIP -> {
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.END_ACTION.getName(), endAction.name)
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.END_TIME.getName(), java.lang.Long.toString(videoEndTime))
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.EVENT_SECTION.getName(), NhAnalyticsEventSection.ADS.name)
                VideoTimespentHelper.updateVideoDuration(videoPlayedEventId, duration)
                VideoTimespentHelper.postSendTimespentEvent(videoPlayedEventId, false)
                isNewVideoPlayEntry = true
            }
        }
        resetVideoPlayBackTimer()
    }

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_SCREEN_PAUSED && videoPlayedEventId != 0L) {
                VideoTimespentHelper.postSendTimespentEvent(videoPlayedEventId, true)
                Logger.d(LOG_TAG, "flushing paused video event")
                isNewVideoPlayEntry = true
            }
        }
    }

    private fun removeHandlerMessages() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun removeHandlerMessagePaused() {
        handler.removeMessages(MSG_SCREEN_PAUSED)
    }

    fun onFragmentResume() {
        removeHandlerMessagePaused()
        if (isTimespentPaused) {
            VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                    VideoTimespentHelper.IS_PAUSED, java.lang.Boolean.FALSE.toString())
            isTimespentPaused = false
        }
    }

    fun onFragmentDestroy() {
        removeHandlerMessages()
        VideoTimespentHelper.postSendTimespentEvent(videoPlayedEventId, false)
    }

    private fun getPlayBackDuration(): Long {
        stopVideoPlayBackTimer()
        return videoPlayBackTimer.totalTime
    }

    private fun startVideoPlayBackTimer() {
        isVideoTimerStarted = true
        videoPlayBackTimer.start()
        Logger.d(LOG_TAG, "startVideoPlayBackTimer - " + videoPlayBackTimer.totalTime)
    }

    fun stopVideoPlayBackTimer() {
        if(isVideoTimerStarted) {
            videoPlayBackTimer.stop()
            isVideoTimerStarted = false
            Logger.d(LOG_TAG, "stopVideoPlayBackTimer - " + videoPlayBackTimer.totalTime)
        }
    }

    private fun resetVideoPlayBackTimer() {
        videoPlayBackTimer.reset()
        Logger.d(LOG_TAG, "resetVideoPlayBackTimer")
    }

    fun updateParam(paramName: String, paramValue: String) {
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId, paramName, paramValue)
    }
}