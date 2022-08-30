/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.sqlite.NotificationDB
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Helper class to schedule deletion of notification from tray
 *
 * Created by atul.anand on 07/01/22.
 */
object NotificationRemoveFromTrayHelper {

    private const val TAG = "NotificationRemoveFromTray_Helper"
    const val ADDITIONAL_DELAY_IN_MS = 3600000L //1 hr in milliseconds, a constant buffer time to help batch

    @JvmOverloads
    @JvmStatic
    fun scheduleNotificationRemovalJobFor(notificationId: Int, displayedAtTimeInMs: Long, afterDisplayDelayInMs: Long){

        Logger.d(TAG, "Scheduling job for notification with Id $notificationId")
        if(PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, -1) != Constants.UNGROUPED){
            Logger.d(TAG, "Quitting since ungrouped is not selected")
            return
        }
        NotificationDB.instance().getNotificationDao().updateNotificationDisplayedAtTimeStamp(notificationId, displayedAtTimeInMs);

        val timeFromNowInMS = displayedAtTimeInMs + afterDisplayDelayInMs - System.currentTimeMillis() + ADDITIONAL_DELAY_IN_MS
        if(afterDisplayDelayInMs < 0){
            Logger.d(TAG, "Invalid time received for removing notification from tray job")
        }

        val inputData = Data.Builder()
                        .putInt(Constants.NOTIFICATION_ID, notificationId)
                        .putLong(Constants.NOTIFICATION_DISPLAYED_TIMESTAMP, displayedAtTimeInMs)
                        .build()
        val workRequestBuilder = OneTimeWorkRequestBuilder<NotificationRemoveFromTrayWorker>()
                .addTag(TAG)
                .setInputData(inputData)

        if(timeFromNowInMS > 0){
            workRequestBuilder.setInitialDelay(timeFromNowInMS, TimeUnit.MILLISECONDS)
        }

        val workRequest = workRequestBuilder.build()

        DHWorkManager.beginUniqueWork(workRequest, (TAG + notificationId.toString()), ExistingWorkPolicy.REPLACE)
        Logger.d(TAG, "Scheduled job with delay $timeFromNowInMS for notification with notificationId $notificationId")

    }

    @JvmOverloads
    @JvmStatic
    fun cancelAllScheduledTrayRemovalJobs(){
        Logger.d(TAG, "Cancel all scheduled Jobs called")
        DHWorkManager.cancelWork(TAG)
    }

    @JvmOverloads
    @JvmStatic
    fun cancelTrayRemovalJobFor(notificationId: Int){
        Logger.d(TAG, "Cancelling job for notification with id ${notificationId}")
        DHWorkManager.cancelUniqueWork((TAG + notificationId))
    }

    @JvmOverloads
    @JvmStatic
    fun removeAllPendingNotificationsAndCancelScheduleJobs(delayFromDisplayTimeInMS: Long){
        try{
            Logger.d(TAG, "App start called removeAllPendingNotificationsAndCancelScheduleJobs")
            if(delayFromDisplayTimeInMS < 0){
                Logger.d(TAG, "Exiting rescheduleNotificationRemovalJobForAllNotifications because of invalid delayFromDisplayTimeInMS, value being $delayFromDisplayTimeInMS")
                return
            }
            if(PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, -1) != Constants.UNGROUPED){
                Logger.d(TAG, "Quitting removeAllPendingNotificationsAndCancelScheduleJobs since ungrouped is not selected")
                return
            }
            CommonUtils.runInBackground(object : Runnable{
                override fun run() {
                    val endTimeInterval = System.currentTimeMillis() - delayFromDisplayTimeInMS
                    val notifications = NotificationDB.instance().getNotificationDao().getNotificationsNonDeferredNonStickyAlreadyDisplayedNotificationForTimeInterval(endTimeInterval)
                    notifications?.let {
                        it.forEach { id ->
                            Logger.d(TAG, "deleting notification with id $id from tray")
                            NotificationUtils.removeNotificationFromTray(id.toInt())
                            NotificationDB.instance().getNotificationDao().markNotificationAsDeletedFromTray(id.toInt())
                            cancelTrayRemovalJobFor(id.toInt())
                        }
                    }
                }

            })
        }catch (ex: Exception){
            Logger.caughtException(ex)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun rescheduleAllRemovalJobs(delayFromDisplayTimeInMS: Long){
        try{
            Logger.d(TAG, "rescheduleAllRemovalJobs called")
            if(delayFromDisplayTimeInMS < 0){
                Logger.d(TAG, "Exiting rescheduleAllRemovalJobs because of invalid delayFromDisplayTimeInMS, value being $delayFromDisplayTimeInMS")
                return
            }

            CommonUtils.runInBackground(object : Runnable{
                override fun run() {
                    val notifications = NotificationDB.instance().getNotificationDao().getNonDeferredNonStickyNotificationInDecreasingTimestampOrder()
                    notifications?.let {
                        it.forEach { item ->
                            item?.baseInfo?.let{ baseInfo ->
                                baseInfo.uniqueId?.let{ id ->
                                    Logger.d(TAG, "cancelling job for notification with id $id from tray")
                                    cancelTrayRemovalJobFor(id)
                                    //If displayTime was -1, apply currentTime
                                    val displayTime = if(baseInfo.displayedAtTime == -1L){System.currentTimeMillis()}else{baseInfo.displayedAtTime}
                                    NotificationDB.instance().getNotificationDao().updateNotificationDisplayedAtTimeStamp(id, displayTime);
                                    if(displayTime + delayFromDisplayTimeInMS + ADDITIONAL_DELAY_IN_MS <= System.currentTimeMillis()){
                                        NotificationUtils.removeNotificationFromTray(id)
                                    }else{
                                        Logger.d(TAG, "Scheduling job for notification with id:- $id with display")
                                        scheduleNotificationRemovalJobFor(id, displayTime, delayFromDisplayTimeInMS)
                                    }
                                }

                            }
                        }
                    }
                }

            })
        }catch (ex: Exception){
            Logger.caughtException(ex)
        }
    }
}