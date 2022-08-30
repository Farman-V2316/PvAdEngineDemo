/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.dhutil.model.entity.launch.TimeWindow
import com.newshunt.dataentity.notification.asset.NewsStickyOptInEntity
import com.newshunt.dataentity.notification.asset.OptInEntity
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.helper.composeUrlForNewsSticky
import com.newshunt.notification.model.service.NewsStickyOptInServiceImpl
import java.util.*

const val KEY_NEWS_STICKY_START_TIME = "KEY_NEWS_STICKY_START_TIME"
const val KEY_NEWS_STICKY_END_TIME = "KEY_NEWS_STICKY_END_TIME"
const val KEY_TIME_WINDOW_ID = "KEY_TIME_WINDOW_ID"

/**
 * Work Manager Worker implementation to push the news opt in configuration to Sticky framework
 *
 * Created by srikanth.r on 12/3/21.
 */
class NewsStickyPushWorker(val context: Context,
                           workerParams: WorkerParameters): Worker(context, workerParams) {
    private var startTimeMs = -1L
    private var endTimeMs = -1L
    private var timeWindowId: String? = null
    override fun doWork(): Result {
        START_STICKY_SERVICE_EXECUTOR.execute {
            startTimeMs = inputData.getLong(KEY_NEWS_STICKY_START_TIME, -1)
            endTimeMs = inputData.getLong(KEY_NEWS_STICKY_END_TIME, -1)
            timeWindowId = inputData.getString(KEY_TIME_WINDOW_ID)

            if (startTimeMs < 0 || endTimeMs < 0) {
                return@execute
            }
            val localConfig = NewsStickyOptInServiceImpl.newInstance().getNewsStickyOptInLocal()
            handleOptInConfig(localConfig)
            //Schedule next possible TimeWindow
            NewsStickyPushScheduler.scheduleNextBestTimeWindow(TimeWindow(startTimeMs, endTimeMs, timeWindowId), localConfig)
        }
        return Result.success()
    }

    private fun handleOptInConfig(newsStickyOptInEntity: NewsStickyOptInEntity) {
        if (NewsStickyOptInServiceImpl.isNewsStickyOptinValid(newsStickyOptInEntity)) {
            newsStickyOptInEntity.timeWindows?.let {
                if (it.contains(TimeWindow(startTimeMs, endTimeMs, timeWindowId))) {
                    /**
                     * The TimeWindows are time slots between 00:00 to 23:59. They do not contain
                     * date info. Hence, everytime we schedule a push, we need to convert this
                     * TimeWindow to epoch format.
                     */
                    val stickyStartTime = computeEpochTime(startTimeMs)
                    val stickyExpiryTime = computeEpochTime(endTimeMs)
                    var metaUrl = newsStickyOptInEntity.metaUrl
                    if (!timeWindowId.isNullOrBlank()) {
                        metaUrl = composeUrlForNewsSticky(timeWindowId!!, metaUrl)
                    }

                    //Create OptInEntity and pass it on to Sticky framework to allow sticky notifications
                    OptInEntity(NotificationConstants.NEWS_STICKY_OPTIN_ID,
                        metaUrl = metaUrl ?: Constants.EMPTY_STRING,
                        type = NotificationConstants.STICKY_NEWS_TYPE,
                        priority = newsStickyOptInEntity.priority,
                        startTime = stickyStartTime,
                        expiryTime = stickyExpiryTime,
                        channel = newsStickyOptInEntity.channel ?: Constants.EMPTY_STRING,
                        channelId = newsStickyOptInEntity.channelId,
                        forceOptIn = newsStickyOptInEntity.forceShow).apply {
                        StickyNotificationsManager.newsStickyServerOptIn(listOf(this))
                    }
                }
            }
        }
    }

    /**
     * Convert the timeInMillis to epoch time
     */
    private fun computeEpochTime(timeInMillis: Long): Long {
        val seconds = (timeInMillis / 1000).toInt() % 60
        val minutes = (timeInMillis / (1000 * 60) % 60)
        val hours = (timeInMillis / (1000 * 60 * 60) % 24)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hours.toInt())
        calendar.set(Calendar.MINUTE, minutes.toInt())
        calendar.set(Calendar.SECOND, seconds)
        return calendar.timeInMillis
    }
}