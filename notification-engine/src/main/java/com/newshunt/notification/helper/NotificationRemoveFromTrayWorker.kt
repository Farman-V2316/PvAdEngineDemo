/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.notification.sqlite.NotificationDB
import java.lang.Exception

/**
 * Worker class for deletion of notification from tray
 *
 * Created by atul.anand on 07/01/22.
 */
class NotificationRemoveFromTrayWorker(context: Context,
                                          workerParams: WorkerParameters): Worker(context, workerParams) {
    final val LOG_TAG = "NotificationRemoveFromTray_Worker"
    override fun doWork(): Result {
        Logger.d(LOG_TAG, "${LOG_TAG} started")

        try{
            val notificationId = inputData.getInt(Constants.NOTIFICATION_ID, -1)
            val notificationDisplayedTimeStamp = inputData.getLong(Constants.NOTIFICATION_DISPLAYED_TIMESTAMP, -1L)
            NotificationUtils.removeNotificationFromTray(notificationId)
            NotificationDB.instance().getNotificationDao().markNotificationAsDeletedFromTray(notificationId)
            Logger.d(LOG_TAG, "Executed for notificationId ${notificationId}")
            val notificationIdList = NotificationDB.instance().getNotificationDao().getNotificationsNonDeferredNonStickyAlreadyDisplayedNotificationForTimeInterval( (notificationDisplayedTimeStamp + NotificationRemoveFromTrayHelper.ADDITIONAL_DELAY_IN_MS))
            notificationIdList?.let {
                for(id in it){
                    Logger.d(LOG_TAG, "Removing also notification with id ${id} with worker initiated for id ${notificationId}")
                    NotificationUtils.removeNotificationFromTray(id.toInt())
                    NotificationDB.instance().getNotificationDao().markNotificationAsDeletedFromTray(id.toInt())
                    NotificationRemoveFromTrayHelper.cancelTrayRemovalJobFor(id.toInt())
                }
            }
        }catch (ex: Exception){
            Logger.caughtException(ex)
        }

        return Result.success()
    }
}