/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.notification.view.service.DeferredNotificationWorker
import java.util.concurrent.TimeUnit

/**
 * Created by Mukesh Yadav on 22/05/2020.
 * */
class DeferredNotificationWorkRequest(private val notificationId: Long, private val displayTime: Long,
                                      private val expiryTime: Long, private val canBeReplaced: Boolean,
                                      private val isInternetRequired: Boolean, private val nextScheduledDuration: Long) {
    fun create(): OneTimeWorkRequest {
        var workReqId = Constants.EMPTY_STRING
        try {
            workReqId = notificationId.toString()
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        val inputData = Data.Builder()
                .putBoolean(Constants.CAN_BE_REPLACED, canBeReplaced)
                .putInt(Constants.BUNDLE_DEFERRED_NOTIFICATION_ID, notificationId.toInt())
                .putLong(Constants.BUNDLE_DISPLAY_TIME, displayTime)
                .putLong(Constants.BUNDLE_EXPIRY_TIME, expiryTime)
                .build()
        val constraintsBuilder = Constraints.Builder()
        if (isInternetRequired) {
            constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED)
        }
        return OneTimeWorkRequest.Builder(DeferredNotificationWorker::class.java)
                .setInputData(inputData)
                .setInitialDelay(nextScheduledDuration, TimeUnit.MILLISECONDS)
                .setConstraints(constraintsBuilder.build())
                .addTag(workReqId)
                .build()
    }

    override fun toString(): String {
        return "Deferred Notification work Created with tag [ " + notificationId +
                " ], " + "canReplaceExistingWork [ " + canBeReplaced + "" +
                " ], requiresInternet [ " + isInternetRequired + " ] scheduled after [" + " " +
                nextScheduledDuration + " ]"
    }
}