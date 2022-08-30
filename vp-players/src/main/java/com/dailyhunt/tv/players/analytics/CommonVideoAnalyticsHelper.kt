/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import com.dailyhunt.huntlytics.sdk.NHAnalyticsSession
import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.news.model.entity.server.asset.UIType
import com.newshunt.dataentity.news.model.entity.server.asset.VideoItem
import com.newshunt.news.helper.VideoPlayBackTimer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val MSG_SCREEN_PAUSED = 10002

/**
 * Helper to handle video instrumentation
 *
 * Created by umesh.isran on 11/26/19.
 */
class CommonVideoAnalyticsHelper() {
    private val LOG_TAG = CommonVideoAnalyticsHelper::class.java.simpleName
    private var videoPlayBackTimer = VideoPlayBackTimer()
    private var isVideoTimerStarted = false
    private var isNewVideoPlayEntry = true
    private var videoPlayedEventId: Long = 0
    var videoStartAction = PlayerVideoStartAction.CLICK
    private var isTimespentPaused: Boolean = false
    private var pausedEventDelayTimeMs = 0
    var videoLoadTime = 0L

    init {
        pausedEventDelayTimeMs = PreferenceManager.getPreference(
                GenericAppStatePreference.PAUSED_VIDEO_EVENT_DELAY_MS, Constants.TIMESPENT_PAUSE_DELAY)
    }

    fun startVPEvent(videoStartTime: Long, loadTime: Long, map: MutableMap<String, Any>) {
        Logger.d(LOG_TAG, "startVPEvent loadTime = " + loadTime)
        if(isNewVideoPlayEntry) {
            videoLoadTime = loadTime
            addNewEntry(videoStartTime, loadTime, map)
            Logger.d(LOG_TAG, "startVPEvent new " + videoPlayedEventId)
        } else {
            Logger.d(LOG_TAG, "startVPEvent existing " + videoPlayedEventId)
        }

        startVideoPlayBackTimer()
    }

    private fun addNewEntry(videoStartTime: Long, loadTime: Long, map: MutableMap<String, Any>) {
        videoPlayedEventId = SystemClock.elapsedRealtime()
        VideoTimespentHelper.postCreateTimespentEvent(videoPlayedEventId, map)
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.PLAYBACK_DURATION.getName(), "0")
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                AnalyticsParam.INITIAL_LOAD_TIME.getName(), java.lang.Long.toString(loadTime))
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.EVENT_NAME.getName(), NhAnalyticsAppEvent.VIDEO_PLAYED.name)
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.START_ACTION.getName(), videoStartAction.name)
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.START_TIME.getName(), java.lang.Long.toString(videoStartTime))
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.SYSTEM_VIDEO_START_TIME.getName(), System.currentTimeMillis().toString())
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.FULL_SCREEN_MODE.getName(), false.toString())
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                PlayerAnalyticsEventParams.IS_AD_PLAYING.getName(), false.toString())
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                NhAnalyticsAppEventParam.PAGE_VIEW_EVENT.getName(), true.toString())
        isNewVideoPlayEntry = false
    }

    @Synchronized
    fun logVPEvent(endAction: PlayerVideoEndAction, videoEndTime: Long,
            commonAsset: CommonAsset?, eventSection: NhAnalyticsEventSection) {
        Logger.d(LOG_TAG, "logVPEvent")
        if(endAction == null) {
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
                        PlayerAnalyticsEventParams.EVENT_SECTION.getName(), eventSection.name)
                VideoTimespentHelper.updateVideoDuration(videoPlayedEventId, duration)

                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                            NhAnalyticsAppEventParam.PAGE_VIEW_EVENT.getName(), true.toString())
                handler.sendMessageDelayed(Message.obtain(handler, MSG_SCREEN_PAUSED),
                        pausedEventDelayTimeMs.toLong())
            }

            PlayerVideoEndAction.SWIPE,
            PlayerVideoEndAction.VERTICAL_FLIP,
            PlayerVideoEndAction.SCROLL,
            PlayerVideoEndAction.COMPLETE,
            PlayerVideoEndAction.SKIP,
            PlayerVideoEndAction.APP_BACK,
            PlayerVideoEndAction.APP_EXIT -> {
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.END_ACTION.getName(), endAction.name)
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.END_TIME.getName(), java.lang.Long.toString(videoEndTime))
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        PlayerAnalyticsEventParams.EVENT_SECTION.getName(), eventSection.name)
                VideoTimespentHelper.updateVideoDuration(videoPlayedEventId, duration)
                VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId,
                        NhAnalyticsAppEventParam.PAGE_VIEW_EVENT.getName(), true.toString())
                VideoTimespentHelper.postSendTimespentEvent(videoPlayedEventId, false)
                isNewVideoPlayEntry = true
            }
        }
        resetVideoPlayBackTimer()
        PlayerUtils.addVideoToCache(getVideoItem(duration, commonAsset))
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

    fun getVideoItem(playDuration: Long, commonAsset: CommonAsset?): VideoItem? {
        commonAsset?.let {
            var uiType = UIType.NORMAL.name
            if (commonAsset!!.i_uiType() != null) {
                uiType = commonAsset!!.i_uiType()!!.name
            }
            return VideoItem(commonAsset!!.i_groupId(), commonAsset!!.i_id(),
                    NHAnalyticsSession.getSessionId(), uiType,
                    playDuration / 1000, System.currentTimeMillis())
        }
        return null
    }

    fun getPlayBackDuration(): Long {
        stopVideoPlayBackTimer()
        return videoPlayBackTimer.totalTime
    }

    private fun startVideoPlayBackTimer() {
        isVideoTimerStarted = true
        videoPlayBackTimer.start()
        removeHandlerMessagePaused()
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

    fun updateParam(paramName: String, paramValue: String?) {
        VideoTimespentHelper.postUpdateTimespentEvent(videoPlayedEventId, paramName, paramValue)
    }

    fun updateBufferDuration(paramValue: Long) {
        GlobalScope.launch {
            VideoTimespentHelper.updateBufferDuration(videoPlayedEventId, paramValue)
        }
    }
}