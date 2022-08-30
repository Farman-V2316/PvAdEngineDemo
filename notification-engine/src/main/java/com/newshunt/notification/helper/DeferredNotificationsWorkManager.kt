/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.common.helper.common.Logger
import com.newshunt.notification.helper.DHWorkManager.beginWork
/**
 * Created by Mukesh Yadav on 22/05/2020.
 * */
class DeferredNotificationsWorkManager(private val notificationId: Long) {
    fun scheduleWork(displayTime: Long, isInternetRequired: Boolean, expiryTime: Long, nextScheduleDuration: Long, workCanBeReplaced: Boolean) {
        val deferredNotificationWork = DeferredNotificationWorkRequest(notificationId,
                displayTime, expiryTime, workCanBeReplaced, isInternetRequired, nextScheduleDuration)
        DeferredNotificationLogger.logDeferredNotificationWork(deferredNotificationWork)
        try {
            val work = deferredNotificationWork.create()
            beginWork(work, workCanBeReplaced)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    fun cancelDeferredNotificationWork() {
        val workReqId = notificationId.toString()
        DeferredNotificationLogger.logCancelDeferredNotificationWorkReq(workReqId)
        DHWorkManager.cancelWork(workReqId)
    }

}