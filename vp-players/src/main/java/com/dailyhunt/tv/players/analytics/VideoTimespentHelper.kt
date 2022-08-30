/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics

import android.content.ContentValues
import android.database.Cursor
import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.NewsDetailTimespentEvent
import com.newshunt.dhutil.model.sqlite.TimespentEventSqliteHelper
import com.newshunt.news.model.internal.cache.StoryPageViewerCache
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Helper class to handle video played event generation and processing for videos.
 *
 * @author umesh.isran
 */

object VideoTimespentHelper {

    private val TAG = VideoTimespentHelper::class.java.simpleName

    private var timespentSqliteHelper: TimespentEventSqliteHelper
    const val IS_PAUSED = "IS_PAUSED"

    private var subject: PublishSubject<NewsDetailTimespentEvent> = PublishSubject.create()

    init {
        // Observe on non-UI thread for processing events in FIFO order as pushed by story page.
        subject.toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(Schedulers.single())
                .doOnNext { value ->
                    try {
                        if (value.isCreateEvent) {
                            val event = value as NewsDetailTimespentEvent.NewsDetailCreateTimespentEvent
                            createTimespentEvents(event.fragmentId, event.params)
                        } else if (value.isUpdateParamEvent) {
                            val event = value as NewsDetailTimespentEvent.NewsDetailUpdateTimespentEvent
                            updateTimespentEvent(event.fragmentId, event.paramName,
                                    event.paramValue)
                        } else if (value.isSendEvent) {
                            val event = value as NewsDetailTimespentEvent.NewsDetailSendTimespentEvent
                            sendTimespentEvents(event.fragmentId!!, event.isPaused)
                        } else if (value.isClearStaleEvent) {
                            clearStaleEvents()
                        } else if (value.isDeleteEvent) {
                            val event = value as NewsDetailTimespentEvent.NewsDetailDeleteTimespentEvent
                            deleteEvent(event.fragmentId)
                        }
                    } catch (ex: Exception) {
                        Logger.e(TAG, "Error processing timespent event", ex)
                    }


                }.subscribe()

        timespentSqliteHelper = TimespentEventSqliteHelper(CommonUtils.getApplication())
    }

    /**
     * Utility to post create timespent event
     *
     * @param fragmentId Unique Id for a fragment
     * @param params     Map of initial params copied from corresponding story page view event.
     */
    fun postCreateTimespentEvent(fragmentId: Long?, params: Map<String, Any>?) {
        subject.onNext(NewsDetailTimespentEvent.NewsDetailCreateTimespentEvent(fragmentId, params))
    }

    /**
     * Utility to post update an existing timespent event
     *
     * @param fragmentId Unique Id for a fragment
     * @param paramName  Name of the param
     * @param paramValue Value for the param
     */
    fun postUpdateTimespentEvent(fragmentId: Long?, paramName: String, paramValue: String?) {
        subject.onNext(NewsDetailTimespentEvent.NewsDetailUpdateTimespentEvent(fragmentId,
                paramName, paramValue))
    }

    /**
     * Utility to post request to post timespent event to analytics.
     *
     * @param fragmentId Unique Id for a fragment
     * @param isPaused   Flag to indicate if original event has to saved for future use.
     */
    fun postSendTimespentEvent(fragmentId: Long?, isPaused: Boolean) {
        subject.onNext(
                NewsDetailTimespentEvent.NewsDetailSendTimespentEvent(fragmentId,
                        Collections.singletonMap(0, 0L), isPaused, NhAnalyticsUserAction.IDLE))
    }

    /**
     * Utility to delete timespent event params.
     *
     * @param fragmentId Unique Id for a fragment
     */
    fun postDeleteTimespentEvent(fragmentId: Long?) {
        subject.onNext(
                NewsDetailTimespentEvent.NewsDetailDeleteTimespentEvent(fragmentId))
    }

    /**
     * Utility to post request to clear all stale events from DB
     */
    @Synchronized
    fun postClearStaleEvents() {
        subject.onNext(NewsDetailTimespentEvent.NewsDetailClearTimespentEvent())
    }

    /**
     * Create timespent event entry in DB
     *
     * @param fragmentId Unique Id for a fragment
     * @param params     Map of initial params copied from corresponding story page view event.
     */
    @Synchronized
    private fun createTimespentEvents(fragmentId: Long?,
                                      params: Map<String, Any>) {
        val database = timespentSqliteHelper.writableDatabase
        database.beginTransaction()
        for (key in params.keys) {
            if (params[key] == null) {
                continue
            }

            val values = ContentValues()
            values.put(TimespentEventSqliteHelper.COLUMN_EVENT_ID, fragmentId)
            values.put(TimespentEventSqliteHelper.COLUMN_PARAM_NAME, key)
            values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE, params[key]!!.toString())
            database.insert(TimespentEventSqliteHelper.TABLE_NAME, null, values)
        }

        database.setTransactionSuccessful()
        database.endTransaction()
    }

    /**
     * Update an existing timespent event with new params
     *
     * @param fragmentId Unique Id for a fragment
     * @param paramName  Name of the param
     * @param paramValue Value for the param
     */
    private fun updateTimespentEvent(fragmentId: Long?, paramName: String, paramValue: String) {
        val database = timespentSqliteHelper.writableDatabase
        val values = ContentValues()
        values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE, paramValue)

        database.beginTransaction()
        val update = database.update(TimespentEventSqliteHelper.TABLE_NAME, values,
                TimespentEventSqliteHelper.COLUMN_EVENT_ID + " = '" + java.lang.Long.toString(fragmentId!!)
                        + "' AND " + TimespentEventSqliteHelper.COLUMN_PARAM_NAME + " = '" + paramName + "'", null)

        if (update <= 0) {
            values.put(TimespentEventSqliteHelper.COLUMN_EVENT_ID, java.lang.Long.toString(fragmentId!!))
            values.put(TimespentEventSqliteHelper.COLUMN_PARAM_NAME, paramName)
            values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE, paramValue)
            database.insert(TimespentEventSqliteHelper.TABLE_NAME, null, values)
        }
        database.setTransactionSuccessful()
        database.endTransaction()
    }


    /**
     * Send timespent event for given ID and specified time
     *
     * @param fragmentId Unique Id for a fragment
     * @param isPaused   Flag to indicate if original event has to saved for future use.
     */
    private fun sendTimespentEvents(fragmentId: Long, isPaused: Boolean) {
        val database = timespentSqliteHelper.writableDatabase
        var cursor: Cursor? = null
        try {
            cursor = database.query(TimespentEventSqliteHelper.TABLE_NAME, null,
                    TimespentEventSqliteHelper.COLUMN_EVENT_ID + " = ? ",
                    arrayOf(java.lang.Long.toString(fragmentId)), null, null, null)

            val params = HashMap<String, Any>()
            var isPausedEarlier = false
            if (cursor!!.moveToFirst()) {
                var eventSection = NhAnalyticsEventSection.TV.name
                var videoDuration = "0"
                do {
                    val paramName = cursor!!.getString(cursor!!.getColumnIndex(TimespentEventSqliteHelper
                            .COLUMN_PARAM_NAME))
                    val paramValue = cursor!!.getString(
                            cursor!!.getColumnIndex(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE))
                    if (IS_PAUSED == paramName) {
                        isPausedEarlier = java.lang.Boolean.parseBoolean(paramValue)
                    } else if(PlayerAnalyticsEventParams.EVENT_SECTION.getName().equals(paramName)) {
                        //Dont include duplicate event_section property
                        eventSection = paramValue
                    } else if (PlayerAnalyticsEventParams.EVENT_NAME.getName() != paramName) { //Dont include duplicate event_name property
                        if(PlayerAnalyticsEventParams.PLAYBACK_DURATION.getName().equals(paramName)) {
                            videoDuration = paramValue
                        }
                        params[paramName] = paramValue
                    }
                } while (cursor!!.moveToNext())

                if (!isPausedEarlier) {
                    if(!CommonUtils.isEmpty(videoDuration) && !"0".equals(videoDuration)) {
                        StoryPageViewerCache.getInstance().onTimeSpent(params)
                        AnalyticsClient.logProcessedDynamic(NhAnalyticsAppEvent.VIDEO_PLAYED.toString(),
                                eventSection, params)
                    }
                }
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }

        if (!isPaused) {
            database.beginTransaction()
            database.delete(TimespentEventSqliteHelper.TABLE_NAME, (TimespentEventSqliteHelper
                    .COLUMN_EVENT_ID + " = ? "), arrayOf(java.lang.Long.toString(fragmentId)))
            database.setTransactionSuccessful()
            database.endTransaction()
        } else {
            updateTimespentEvent(fragmentId, IS_PAUSED, java.lang.Boolean.TRUE.toString())

            // Clear used time durations
            database.beginTransaction()
            database.delete(TimespentEventSqliteHelper.TABLE_NAME, (TimespentEventSqliteHelper
                    .COLUMN_EVENT_ID + " = ?"), arrayOf(java.lang.Long.toString(fragmentId)))
            database.setTransactionSuccessful()
            database.endTransaction()
        }
    }

    /**
     * Update video duration for specified fragment id.
     *
     * @param fragmentId         Unique Id for a fragment
     * @param newDuration News video duration.
     */
    fun updateVideoDuration(fragmentId: Long, newDuration: Long) {
        val database = timespentSqliteHelper.writableDatabase
        var cursor: Cursor? = null
        var chunkDuraton = 0L
        try {
            cursor = database.query(TimespentEventSqliteHelper.TABLE_NAME, null,
                    (TimespentEventSqliteHelper.COLUMN_EVENT_ID + " = '" + java.lang.Long.toString(fragmentId!!) + "' AND "
                            + TimespentEventSqliteHelper.COLUMN_PARAM_NAME + " = '" + PlayerAnalyticsEventParams.PLAYBACK_DURATION.getName() + "'"),
                    null, null, null, null)

            // Add existing values to new values
            while (cursor!!.moveToNext()) {
                chunkDuraton = cursor!!.getLong(cursor!!.getColumnIndex(TimespentEventSqliteHelper
                        .COLUMN_PARAM_VALUE))
                if (newDuration != null) {
                    chunkDuraton += newDuration!!
                }
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }

        // Update values to DB
        updateTimespentEvent(fragmentId, PlayerAnalyticsEventParams.PLAYBACK_DURATION.getName(),
                java.lang.Long.toString(chunkDuraton))

    }

    /**
     * Update buffer duration for specified eventId id.
     *
     * @param eventId         Unique Id
     * @param newDuration News video duration.
     */
    fun updateBufferDuration(eventId: Long, newDuration: Long) {
        val database = timespentSqliteHelper.writableDatabase
        var cursor: Cursor? = null
        var chunkDuraton = 0L
        try {
            cursor = database.query(TimespentEventSqliteHelper.TABLE_NAME, null,
                    (TimespentEventSqliteHelper.COLUMN_EVENT_ID + " = '" + java.lang.Long.toString(eventId!!) + "' AND "
                            + TimespentEventSqliteHelper.COLUMN_PARAM_NAME + " = '" + AnalyticsParam.BUFFER_TIME_MS.getName() + "'"),
                    null, null, null, null)

            // Add existing values to new values
            while (cursor!!.moveToNext()) {
                chunkDuraton = cursor!!.getLong(cursor!!.getColumnIndex(TimespentEventSqliteHelper
                        .COLUMN_PARAM_VALUE))
                if (newDuration != null) {
                    chunkDuraton += newDuration!!
                }
            }
            if(chunkDuraton == 0L) {
                chunkDuraton = newDuration
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }

        // Update values to DB
        updateTimespentEvent(eventId, AnalyticsParam.BUFFER_TIME_MS.getName(),
                java.lang.Long.toString(chunkDuraton))

    }

    @Synchronized
    private fun deleteEvent(fragmentId: Long?) {
        val database = timespentSqliteHelper.readableDatabase
        database.beginTransaction()
        database.delete(TimespentEventSqliteHelper.TABLE_NAME, (TimespentEventSqliteHelper
                .COLUMN_EVENT_ID + " = ? "), arrayOf(java.lang.Long.toString(fragmentId!!)))
        database.setTransactionSuccessful()
        database.endTransaction()
    }

    @Synchronized
    private fun clearStaleEvents() {
        val database = timespentSqliteHelper.readableDatabase
        val cursor = database.query(true, TimespentEventSqliteHelper.TABLE_NAME,
                arrayOf(TimespentEventSqliteHelper.COLUMN_EVENT_ID),
                (TimespentEventSqliteHelper.COLUMN_PARAM_VALUE + " = '" + NhAnalyticsAppEvent.VIDEO_PLAYED.name + "'"),
                null, null, null, null, null)

        val pendingFragmentIds = ArrayList<Long>()
        while (cursor.moveToNext()) {
            val fragmentId = cursor.getLong(0)
            pendingFragmentIds.add(fragmentId)
        }

        cursor.close()

        for (fragmentId in pendingFragmentIds) {
            sendTimespentEvents(fragmentId!!, false)
        }

        Logger.i(TAG, "Stale Timespent events cleared: " + pendingFragmentIds.size)
    }

}