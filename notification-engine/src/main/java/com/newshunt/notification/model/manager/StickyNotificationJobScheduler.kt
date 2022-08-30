/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.manager

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.ExistingWorkPolicy
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.notification.helper.DHWorkManager
import com.newshunt.notification.helper.isStickNotificationValid
import com.newshunt.notification.model.internal.dao.StickyNotificationEntity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author santhosh.kc
 */
const val KEY_INPUT_STICKY_NOTIFICATION_ID = "stickyNotificationId"
const val KEY_INPUT_STICKY_NOTIFICATION_TYPE = "stickyNotificationType"
const val START_STICKY_SERVICE_JOB_PREFIX = "Job_sticky_service_"

/**
 * Common executor to run start sticky service job scheduling and starting sticky notification
 * service to avoid race conditions, as there is possibility that even though we cancel scheduled
 * notifcation, which worker may have started but may not have been marked it as ongoing yet
 *
 * TODO (santhosh.kc) to remove commented work manager code
 */
val START_STICKY_SERVICE_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()

object StickyNotificationJobScheduler {

    private const val META_RETRY_DURATION = 30000L

    fun scheduleNextStickyNotificationJobByJobScheduler(stickyNotificationEntity:
                                                        StickyNotificationEntity): Boolean {
        if (!isStickNotificationValid(stickyNotificationEntity)) {
            return false
        }

        stickyNotificationEntity.startTime?.let {
            val newStartTime = it + (stickyNotificationEntity.metaUrlAttempts * META_RETRY_DURATION)

            val duration = if (newStartTime < System.currentTimeMillis()) 0L
            else (newStartTime - System.currentTimeMillis())
            val workData = workDataOf(KEY_INPUT_STICKY_NOTIFICATION_ID to
                    stickyNotificationEntity.id, KEY_INPUT_STICKY_NOTIFICATION_TYPE to
                    stickyNotificationEntity.type)
            val constraintsBuilder = Constraints.Builder() // The Worker needs Network connectivity
                    .setRequiredNetworkType(NetworkType.CONNECTED)
            val workerRequest = OneTimeWorkRequestBuilder<StartStickyServiceJobService>()
                    .setInitialDelay(duration, TimeUnit.MILLISECONDS)
                    .setInputData(workData)
                    .setConstraints(constraintsBuilder.build())
                    .addTag(getTag(stickyNotificationEntity)).build()

            DHWorkManager.beginUniqueWork(workerRequest,
                getTag(stickyNotificationEntity),
                ExistingWorkPolicy.REPLACE)
            Logger.d(StickyNotificationsManager.TAG, "Scheduling Worker with initial Delay: $duration, id: ${stickyNotificationEntity.id}, type: ${stickyNotificationEntity.type}")
            return true
        }

        return false
    }

    fun cancelJobSchedulerForStickyNotification(alreadyScheduledNotification:
                                                StickyNotificationEntity) {
        DHWorkManager.cancelUniqueWork(getTag(alreadyScheduledNotification))
    }

    fun cancelAllJobSchedulerJobs(scheduledNotifications: List<StickyNotificationEntity>?) {
        scheduledNotifications?.forEach {
            DHWorkManager.cancelUniqueWork(getTag(it))
        }
    }

    private fun getTag(stickyNotificationEntity: StickyNotificationEntity): String {
        return START_STICKY_SERVICE_JOB_PREFIX + stickyNotificationEntity.id + Constants
                .UNDERSCORE_CHARACTER + stickyNotificationEntity.type
    }

}

class StartStickyServiceJobService(val context: Context, private val workerParams: WorkerParameters) :
        Worker(context, workerParams) {
    override fun doWork(): Result {
        START_STICKY_SERVICE_EXECUTOR.execute {
            val toBeExecutedId = inputData.getString(KEY_INPUT_STICKY_NOTIFICATION_ID)
                    ?: return@execute
            val toBeExecutedType = inputData.getString(KEY_INPUT_STICKY_NOTIFICATION_TYPE)
                    ?: return@execute
            Logger.d(StickyNotificationsManager.TAG, "WorkManager - Do Work - Enter")
            StickyNotificationsManager.startStickyService(toBeExecutedId, toBeExecutedType)
            Logger.d(StickyNotificationsManager.TAG, "WorkManager - Do Work - Exit")
        }
        return Result.success()
    }

}