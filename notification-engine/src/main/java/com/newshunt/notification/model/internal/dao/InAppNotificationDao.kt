/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.dao

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.notification.*
import com.newshunt.news.model.daos.BaseDao
import com.newshunt.notification.model.entity.InAppNotificationEntity

/**
 * Created by kajal.kumari on 11/05/22.
 */

@Dao
abstract class InAppNotificationDao : BaseDao<InAppNotificationEntity> {


    @Transaction
    open fun addInAppNotification(notificationModel: BaseModel?) {
        val values : InAppNotificationEntity = setInAppNotification(notificationModel) ?: return
        insertInAppNotification(values)
    }

    fun setInAppNotification(notification: BaseModel?) : InAppNotificationEntity? {
        if (notification?.baseModelType == null) {
            return null
        }
        val baseInfo = notification.baseInfo
        val gson = Gson()
        val jsonData = gson.toJson(notification)
        val timeStamp =  if (baseInfo.timeStamp > 0) baseInfo.timeStamp else System.currentTimeMillis()
        return InAppNotificationEntity(id = baseInfo.uniqueId.toString(),
            timeStamp = timeStamp,
            data = jsonData.toByteArray(),
            priority = baseInfo.priority,
            language = baseInfo.language,
            endTime = baseInfo.inAppInfo.endTimeMs,
            inAppNotificationCtaLink = baseInfo.inAppInfo.notificationCtaLink,
            inAppInfo = baseInfo.inAppInfo,
            disableLangFilter = baseInfo?.isDisableLangFilter?:false,
            status = Constants.NOT_SEEN )
    }

    @Transaction
    open fun getHighestPriorityInAppNotificationsToBeShown(): List<InAppNotificationModel?>{
        val notifications = mutableListOf<InAppNotificationEntity>()
        notifications.add(getHighestPriorityInAppNotifications())
        return executeGetNotificationQuery(notifications)
    }

    @Transaction
    open fun getExpiredInAppNotifications(currentTime: Long): List<InAppNotificationModel?>{
        val notifications = getExpiredInAppNotification(currentTime)
        return executeGetNotificationQuery(notifications)
    }

    @Transaction
    open fun executeGetNotificationQuery(notifications: List<InAppNotificationEntity?>?): List<InAppNotificationModel?> {
        val notificationsModel = mutableListOf<InAppNotificationModel?>()
        if (notifications.isNullOrEmpty()) return notificationsModel
        notifications.forEach {
                notification -> notification?.let{
            val dataBlob = notification.data
            val dataJson = dataBlob?.let { it1 -> String(it1) }
            val builder = GsonBuilder()
            val gson = builder.enableComplexMapKeySerialization().create()
            var baseModel: InAppNotificationModel? = null
            try {
                baseModel = gson.fromJson(dataJson, InAppNotificationModel::class.java)
            } catch (e: Exception) {
                Logger.caughtException(e)
            }

            if (baseModel != null) { notificationsModel.add(baseModel) }
            }
        }
        return notificationsModel
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertInAppNotification(inAppNotificationEntity: InAppNotificationEntity)

    @Query("SELECT * FROM in_app_notification WHERE id = :id")
    abstract fun getInAppNotification(id: String): InAppNotificationEntity

    @Query("SELECT * FROM in_app_notification WHERE status != 'SEEN' AND status != 'EXPIRED' ORDER BY priority DESC, time_stamp DESC LIMIT 1")
    abstract fun getHighestPriorityInAppNotifications(): InAppNotificationEntity

    @Query("SELECT * FROM in_app_notification WHERE endTime < :currentTime AND status = 'NOT_SEEN' ")
    abstract fun getExpiredInAppNotification(currentTime: Long): List<InAppNotificationEntity?>?

    @Query("DELETE FROM in_app_notification WHERE id = :id")
    abstract fun deleteInAppNotification(id: String)

    @Query("UPDATE in_app_notification SET status = :status WHERE id = :id")
    abstract fun markInAppNotificationStatus(id: String, status:String)

    @Query("DELETE FROM in_app_notification WHERE time_stamp <:time AND endTime < :currentTime ")
    abstract fun deleteExpiredInAppNotification(time:Long, currentTime: Long)
}