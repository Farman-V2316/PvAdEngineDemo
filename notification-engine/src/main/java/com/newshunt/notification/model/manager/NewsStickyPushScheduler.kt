/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.manager

import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.launch.TIME_23_59_HOURS_MS
import com.newshunt.dataentity.dhutil.model.entity.launch.TimeWindow
import com.newshunt.dataentity.notification.StickyNavModelType
import com.newshunt.dataentity.notification.asset.NewsStickyOptInEntity
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.helper.DHWorkManager
import com.newshunt.notification.helper.hasUserOptedOutOfSticky
import com.newshunt.notification.model.service.NewsStickyOptInServiceImpl
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

/**
 * Helper class to schedule periodic push of news sticky opt in based on versioned API config
 *
 * Created by srikanth.r on 12/3/21.
 */
private const val LOG_TAG = "NewsStickyPushScheduler"
private const val WORKER_TAG = "NewsStickyPushWorker"
object NewsStickyPushScheduler {
    private val PUSH_BUFFER_TIME = TimeUnit.MINUTES.toMillis(1)

    /**
     * Based on current time and the config from versioned API, schedules a Worker to push the Opt
     * in entity to StickyNotificationManager on time, just before start time.
     */
    fun scheduleNextBestTimeWindow(currentWindow: TimeWindow? = null, localConfig: NewsStickyOptInEntity? = null) {
        START_STICKY_SERVICE_EXECUTOR.execute {
            val config = localConfig ?: NewsStickyOptInServiceImpl.newInstance().getNewsStickyOptInLocal()

            //If config is already expired or invalid config, stop scheduling
            if (config.expiryTime < System.currentTimeMillis() ||
                !NewsStickyOptInServiceImpl.isNewsStickyOptinValid(config) ||
                localConfig?.forceShow != true && hasUserOptedOutOfSticky(NotificationConstants.NEWS_STICKY_OPTIN_ID, NotificationConstants.STICKY_NEWS_TYPE)) {
                Logger.e(LOG_TAG, "CAN NOT SCHEDULE. Either expired or invalid config or user has opted out")
                cancelAll()
                return@execute
            }
            config.timeWindows?.let { timeWindows ->
                //First sort the timewindows based on their start time
                val sortedTimeWindows = timeWindows.sortedBy {
                    it.startTimeMs
                }
                val currentWallclockTimeMs = currentTimeInMillis()
                var nextTimeWindow: TimeWindow? = null
                for (sortedWindow in sortedTimeWindows) {
                    //Skip the window we are currently scheduling, if any!
                    if (currentWindow == sortedWindow) {
                        continue
                    }
                    /**
                     * Find the next TimeWindow to schedule.
                     * If the current wallclock time falls between start and end of a time window,
                     * we need to start now!
                     * Else, pick the first TimeWindow whose start time > current time
                     */
                    if ((sortedWindow.startTimeMs <= currentWallclockTimeMs && sortedWindow.endTimeMs > currentWallclockTimeMs) || sortedWindow.startTimeMs >= currentWallclockTimeMs) {
                        nextTimeWindow = sortedWindow
                        break
                    }
                }
                /**
                 * If we could not find a TimeWindow in the loop above, it means all TimeWindows
                 * are in the past wrt current wallclock time. So, pick the earliest one to schedule
                 * next (i.e) first in the sorted list.
                 */
                if (nextTimeWindow == null) {
                    nextTimeWindow = sortedTimeWindows.first()
                }
                /**
                 * 4 cases to cover for Worker's initial delay:
                 * 1. Only one window is configured hence the next window is for tomorrow at the same time
                 * 2. We are in between a TimeWindow, we are already late. Schedule ASAP
                 * 3. The next window is in the past wrt current wallclock time. So, can only be
                 * scheduled for tomorrow. Hence, subtract the time diff from 24 hrs
                 * 4. The next window is in the future, just find the diff
                 */
                val timeDiff = nextTimeWindow.startTimeMs - currentWallclockTimeMs
                var remainingTime = when {
                    (nextTimeWindow == currentWindow) -> (TIME_23_59_HOURS_MS - timeDiff.absoluteValue)
                    (timeDiff < 0 && nextTimeWindow.endTimeMs > currentWallclockTimeMs) -> 0
                    (timeDiff < 0) -> (TIME_23_59_HOURS_MS - timeDiff.absoluteValue)
                    else -> timeDiff
                }

                remainingTime = if(remainingTime > PUSH_BUFFER_TIME) {
                    remainingTime - PUSH_BUFFER_TIME
                } else remainingTime
                val workData = workDataOf(KEY_NEWS_STICKY_START_TIME to nextTimeWindow.startTimeMs,
                    KEY_NEWS_STICKY_END_TIME to nextTimeWindow.endTimeMs,
                    KEY_TIME_WINDOW_ID to nextTimeWindow.id
                )
                Logger.d(LOG_TAG, "currentWindow: $currentWindow, nextTimeWindow scheduled at: $nextTimeWindow, remaining time: $remainingTime ms, , timeDiff: $timeDiff")
                val workRequest = OneTimeWorkRequestBuilder<NewsStickyPushWorker>()
                    .setInitialDelay(remainingTime, TimeUnit.MILLISECONDS)
                    .setInputData(workData)
                    .addTag(WORKER_TAG)
                    .build()
                DHWorkManager.beginWork(workRequest, true)
            }
        }
    }

    fun cancelAll() {
        Logger.d(LOG_TAG, "cancelAll")
        StickyNotificationsManager.onNotificationComplete(NotificationConstants.NEWS_STICKY_OPTIN_ID, NotificationConstants.STICKY_NEWS_TYPE)
        Intent(NotificationConstants.INTENT_STICKY_NOTIFICATION_CANCEL_ONGOING)
            .apply {
                `package` = CommonUtils.getApplication().packageName
                putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE, StickyNavModelType.NEWS.stickyType)
                CommonUtils.getApplication().sendBroadcast(this)
            }
        DHWorkManager.cancelWork(WORKER_TAG)
    }

    fun onAppStart() {
        Logger.d(LOG_TAG, "onAppStart, start fresh scheduling")
        DHWorkManager.cancelWork(WORKER_TAG)
        scheduleNextBestTimeWindow(null)
    }

    private fun currentTimeInMillis(): Long {
        //Calculate the device time w.r.t timezone in milliseconds
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        return TimeUnit.HOURS.toMillis(calendar[Calendar.HOUR_OF_DAY].toLong()) +
                TimeUnit.MINUTES.toMillis(calendar[Calendar.MINUTE].toLong()) +
                TimeUnit.SECONDS.toMillis(calendar[Calendar.SECOND].toLong()) +
                calendar[Calendar.MILLISECOND]
    }
}