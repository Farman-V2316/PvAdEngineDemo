/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility
import com.newshunt.notification.model.entity.NotificationFilterType
import com.newshunt.notification.sqlite.NotificationDB
import java.util.Date

/**
 * @author anshul.jain on 01/10/2017.
 *
 *
 * Helper class for deferred notifications
 */
object DeferredNotificationsHelper {
    /**
     * This method is used to start all the deferred work at the launch of the application so that
     * the cases such as mobile switch off and app upgrade can be handled.
     */
    @JvmStatic
    fun initDeferredWorksOnProcessStart() {
        CommonUtils.runInBackground { scheduleDeferredWorks() }
    }

    /**
     * This methods takes records from notification table which are deferred and loop through them
     * and try to schedule them if possible.
     */
    private fun scheduleDeferredWorks() {
        try{
            val baseModelList = NotificationDB.instance().getNotificationDao().getDeferredNotifications()
            if (CommonUtils.isEmpty(baseModelList)) {
                return
            }
            baseModelList?.forEach {
                handleDeferredNotification(it)
            }
        }catch (ex: Throwable){
            Logger.caughtException(ex)
        }
    }

    @JvmStatic
    fun handleDeferredNotification(baseModel: BaseModel?) {
        if (null == baseModel || null == baseModel.baseInfo) {
            return
        }
        val baseInfo = baseModel.baseInfo
        val notificationId = baseInfo.uniqueId.toLong()
        val displayTime = baseInfo.v4DisplayTime
        val expiryTime = baseInfo.expiryTime
        val manager = DeferredNotificationsWorkManager(notificationId)
        val displayDate = Date(displayTime)
        val currentDate = Date()
        var expiryDate: Date? = null
        if (0 < expiryTime) {
            expiryDate = Date(expiryTime)
        }
        val hasDisplayDateExpired = currentDate > displayDate
        val hasExpiryDateExpired = null != expiryDate && currentDate > expiryDate
        val isDisplayTimeGreaterThanExpiryTime = null != expiryDate && 0 < displayDate.compareTo(expiryDate)
        /**
         * If the expiryTime has expired, or expiry time is less than display time then
         * 1) Delete the notification from  the db.
         * 2) Cancel the deferred work for that notification
         * 3) Trigger event.
         */
        if (hasExpiryDateExpired || isDisplayTimeGreaterThanExpiryTime) {
            manager.cancelDeferredNotificationWork()
            NotificationDB.instance().getNotificationDao().deleteNotification(baseInfo.uniqueId)
            NotificationDB.instance().getNotificationPrefetchInfoDao().deleteEntryForNotificationWithId(baseInfo.uniqueId.toString())
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(baseModel,
                    NotificationFilterType.EXPIRED)
            return
        }

        //Save whether the notification was deferred in the database for analytics purpose.
        baseModel.baseInfo.isDeferredForAnalytics = true
        try {
            NotificationDB.instance().getNotificationDao().addDeferredNotification(baseModel, true, false)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        /** If the displayTime has expired, but the expiryTime has not expired, then immediately
         * show the notification.
         */
        if (hasDisplayDateExpired && !hasExpiryDateExpired) {
            manager.cancelDeferredNotificationWork()
            baseModel.baseInfo.isDeferred = true
            baseModel.baseInfo.isNotificationForDisplaying = true
            BusProvider.postOnUIBus(baseModel)
            return
        }
        val nextScheduledDuration = (displayDate.time - currentDate.time)
        val isInternetRequired = baseInfo.isV4IsInternetRequired
        manager.scheduleWork(displayTime, isInternetRequired, expiryTime, nextScheduledDuration,
                true /* replace this work*/)
    }
}