/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.content.Intent
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference

/**
 * Helper class to approximately identify multiple notification swipe dismiss within a short period.
 * This is a hack to emulate capturing clear all from notification panel.
 *
 * Created by srikanth.r on 12/13/21.
 */
private const val LOG_TAG = "NotificationClearAllHandler"
object NotificationClearAllHandler {
    private var firstDeleteTimestampMs: Long = 0L
    private var deletedNotificationsCount: Int = 0

    fun onNotificationSwipeDismissed() {
        val currentTime = System.currentTimeMillis()
        val clearAllTimeThreshold = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_CLEAR_ALL_TIME_THRESHOLD, Constants.NOTIFICATION_CLEAR_ALL_TIME_THRESHOLD)
        val clearAllCountThreshold = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_CLEAR_ALL_COUNT_THRESHOLD, Constants.NOTIFICATION_CLEAR_ALL_COUNT_THRESHOLD)
        if (currentTime - firstDeleteTimestampMs < clearAllTimeThreshold) {
            //Quick back to back swipe dismiss identified as clear all, if number of deletedNotificationsCount > threshold
            deletedNotificationsCount++
            if (deletedNotificationsCount == clearAllCountThreshold) {
                Logger.e(LOG_TAG, "sending broadcast INTENT_ACTION_NOTIFICATION_CLEAR_ALL, deletedNotificationsCount: $deletedNotificationsCount")
                Intent(NotificationConstants.INTENT_ACTION_NOTIFICATION_CLEAR_ALL).apply {
                    `package` = AppConfig.getInstance().packageName
                    CommonUtils.getApplication().sendBroadcast(this)
                }
            } else {
                Logger.d(LOG_TAG, "onNotificationSwipeDismissed, deletedNotificationsCount: $deletedNotificationsCount")
            }
        } else {
            //This is the first dismiss in sometime, reset the time and counter
            firstDeleteTimestampMs = currentTime
            deletedNotificationsCount = 1
            Logger.e(LOG_TAG, "Notification dismiss detected, reset states")
        }
    }
}