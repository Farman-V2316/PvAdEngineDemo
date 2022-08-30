/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.internal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.notification.model.entity.NotificationPrefetchEntity

/**
 * @author atul.anand
 */
@Dao
abstract class NotificationPrefetchInfoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEntry(entry: NotificationPrefetchEntity)

    @Query("SELECT * FROM notification_cache_details WHERE notification_cached is :cached ORDER BY received_time DESC")
    abstract fun getNotificationWhichAre(cached: Boolean): List<NotificationPrefetchEntity>

    @Delete
    abstract fun deleteEntry(entry: NotificationPrefetchEntity)

    @Query("DELETE FROM notification_cache_details WHERE id is :id")
    abstract fun deleteEntryForNotificationWithId(id: String)

    @Query("DELETE FROM notification_cache_details")
    abstract fun deleteAllEntries()

    @Query("UPDATE notification_cache_details SET notification_cached = :isCached, retry_number = :retryCount, last_retry_time = :lastRetryTime, post_notification = :shouldPostNotification WHERE pk = :id ")
    abstract fun updateNotificationCachedStatus(isCached: Boolean, retryCount: Int, lastRetryTime: Long, shouldPostNotification: Boolean, id: Int)

    @Query("UPDATE notification_cache_details SET retry_number = :retryCount, last_retry_time = :lastRetryTime WHERE pk = :id ")
    abstract fun updateRetryCount(retryCount: Int, lastRetryTime: Long, id: Int)

    @Query("UPDATE notification_cache_details SET retry_number = :retryCount WHERE pk = :id ")
    abstract fun updateRetryCount(retryCount: Int, id: Int)

    @Query("UPDATE notification_cache_details SET post_notification = :shouldPostNotification, last_retry_time = :lastRetryTime WHERE pk = :id ")
    abstract fun updateNotificationPostStatus(shouldPostNotification: Boolean, lastRetryTime: Long, id: Int)

    @Query("UPDATE notification_cache_details SET post_notification = :shouldPostNotification WHERE pk = :id ")
    abstract fun updateNotificationPostStatus(shouldPostNotification: Boolean, id: Int)

    @Query("UPDATE notification_cache_details SET last_retry_time = :lastRetryTime WHERE pk = :id")
    abstract fun updateLastRetryTime(lastRetryTime: Long, id: Int)

    @Query("SELECT * FROM notification_cache_details WHERE id = :id")
    abstract fun getPrefetchEntryForNotificationWithId(id: Int): NotificationPrefetchEntity?

    fun addEntryToPrefetchDb(baseModel: BaseModel, shouldPostNotification: Boolean, retryCount: Int, lastRetryTime: Long, receivedAtTimestamp: Long, isCached: Boolean)
    {
        val entry: NotificationPrefetchEntity = NotificationPrefetchEntity(notificationId = baseModel.baseInfo.uniqueId.toString(),
                                                                            shouldPostNotification = shouldPostNotification,
                                                                            retryNumber = retryCount,
                                                                            lastRetryTimestamp = lastRetryTime,
                                                                            receivedAtTimestamp = receivedAtTimestamp,
                                                                            isNotificationCached = isCached)
        insertEntry(entry)

    }

}