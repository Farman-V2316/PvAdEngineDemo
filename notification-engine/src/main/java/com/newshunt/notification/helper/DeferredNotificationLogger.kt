/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.common.helper.common.Logger

/**
 * Created by Mukesh Yadav on 22/05/2020.
 * */
object DeferredNotificationLogger {
    private const val TAG = "DeferredNotifications"
    @JvmStatic
    fun logDeferredNotificationOnStartWork() {
        Logger.d(TAG, "The work for deferred notification has started")
    }

    fun logDeferredNotificationWorkReqId(workReqID: Int) {
        Logger.d(TAG, "The work req id is $workReqID")
    }

    fun logDeferredNotificationOnWorkCompleted() {
        Logger.d(TAG, "The work for deferred notification has completed.")
    }

    fun logNoRecordFound(notificationId: Int) {
        Logger.d(TAG, "No record found in the notification inbox for the following notification id " +
                "" + notificationId)
    }

    fun logDisplayTimeZero() {
        Logger.d(TAG, "The display time is zero for this work")
    }

    fun logExpiredNotification(notification: Int) {
        Logger.d(TAG, "The notification with the notificationId $notification has been expired")
    }

    fun logCancelDeferredNotificationWorkReq(workReqId: String) {
        Logger.d(TAG,
                "The Pull Notification work with tag [ " + workReqId + " ]" +
                        "has been cancelled")
    }

    fun logDeferredNotificationWork(work: DeferredNotificationWorkRequest?) {
        if (work == null) {
            return
        }
        Logger.d(TAG, work.toString())
    }
}