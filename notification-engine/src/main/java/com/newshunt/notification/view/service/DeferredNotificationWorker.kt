/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/

package com.newshunt.notification.view.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.helper.KillProcessAlarmManager.Companion.onAppProcessInvokedInBackground
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility
import com.newshunt.notification.helper.DHWorkManager
import com.newshunt.notification.helper.DeferredNotificationLogger
import com.newshunt.notification.model.entity.NotificationFilterType
import com.newshunt.notification.sqlite.NotificationDB
import java.util.Date

/**
 * Created by Mukesh Yadav on 22/05/2020.
 *
 *
 * A Worker which runs when the scheduler framework schedules the work request for deferred
 * notifications.
 */
class DeferredNotificationWorker(context: Context,
                                 var workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        onAppProcessInvokedInBackground()
        DeferredNotificationLogger.logDeferredNotificationOnStartWork()
        // Offload work to bg thread.
        val deferredNotificationWorkHandler = DeferredNotificationWorkHandler(workerParameters)
        try {
            deferredNotificationWorkHandler.startDeferredNotificationWork()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
    }

    private class DeferredNotificationWorkHandler internal constructor(private val workerParameters: WorkerParameters?) {
        var notificationId = 1

        @Throws(Exception::class)
        fun startDeferredNotificationWork() {
            if (workerParameters == null) {
                workCompleted(notificationId)
                return
            }
            val bundle = workerParameters.inputData
            val expiryDuration = bundle.getLong(Constants.BUNDLE_EXPIRY_TIME, 0)
            val displayDuration = bundle.getLong(Constants.BUNDLE_DISPLAY_TIME, 0)
            notificationId = bundle.getInt(Constants.BUNDLE_DEFERRED_NOTIFICATION_ID, 1)
            DeferredNotificationLogger.logDeferredNotificationWorkReqId(notificationId)
            val baseModel = NotificationDB.instance().getNotificationDao().getNotification(notificationId, false)
            if (baseModel == null || displayDuration <= 0) {
                workCompleted(notificationId);
                DeferredNotificationLogger.logNoRecordFound(notificationId)
                return
            }
            if (displayDuration == 0L) {
                workCompleted(notificationId);
                DeferredNotificationLogger.logDisplayTimeZero()
                return
            }
            val currentDate = Date()
            var expiryDate: Date? = null
            if (expiryDuration > 0) {
                expiryDate = Date(expiryDuration)
            }

            //1) The notification has expired. Trigger a notification_action event.
            //2)  delete the notification from the database.
            if (expiryDate != null && currentDate.compareTo(expiryDate) > 0) {
                DeferredNotificationLogger.logExpiredNotification(notificationId)
                NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(baseModel,
                        NotificationFilterType.EXPIRED)
                NotificationDB.instance().getNotificationDao().deleteNotification(notificationId)
                workCompleted(notificationId)
                return
            }

            workCompleted(notificationId)
            baseModel.baseInfo.isDeferred = true
            baseModel.baseInfo.isNotificationForDisplaying = true
            BusProvider.postOnUIBus(baseModel)
        }

        private fun workCompleted(notificationId: Int) {
            DeferredNotificationLogger.logDeferredNotificationOnWorkCompleted()
            DHWorkManager.cancelWork(notificationId.toString())
        }

    }

}