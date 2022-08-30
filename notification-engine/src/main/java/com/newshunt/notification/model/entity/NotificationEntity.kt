/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.notification.InAppNotificationInfo
import com.newshunt.dataentity.notification.util.NotificationConstants

/**
 * @author aman.roy
 */

@Entity(tableName = "notification_info", indices = [Index(value = ["id"], unique = true)])
data class NotificationEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk") val pk: Int = 0,
                              @ColumnInfo(name="id") val id: String,
                              @ColumnInfo(name = "time_stamp") val timeStamp:String? = null,
                              @ColumnInfo(name="priority") val priority:Int? = null,
                              @ColumnInfo(name="section") val section: String? = null,
                              @ColumnInfo(name="data") val data:ByteArray? = null,
                              @ColumnInfo(name="expiry_time") val expiryTime: String? = null,
                              @ColumnInfo(name="state") val state: Int? = null,
                              @ColumnInfo(name="removed_from_tray") val removedFromTray : Boolean? = null,
                              @ColumnInfo(name="grouped") val grouped: Boolean? = null,
                              @ColumnInfo(name="seen") val seen: Boolean? = null,
                              @ColumnInfo(name="delivery_mechanism") val delieveryMechanism: Int? = null,
                              @ColumnInfo(name="synced") val synced : Boolean? = null,
                              @ColumnInfo(name="base_id") val baseId:String? = null,
                              @ColumnInfo(name="display_time") val displayTime: String? = null,
                              @ColumnInfo(name="shown_as_headsup") val shownAsHeadsup: Boolean? = null,
                              @ColumnInfo(name="removed_by_app") val removedByApp: Boolean? = null,
                              @ColumnInfo(name="pending_posting") val pendingPosting: Boolean? = null,
                              @ColumnInfo(name="placement") val placement: String? = null,
                              @ColumnInfo(name="type") val type: String? = null,
                              @ColumnInfo(name="subType") val subType : String? = null,
                              @ColumnInfo(name="priority_expiry_time") val priority_expiry_time : Long? = null,
                              @ColumnInfo(name="isGroupable") val isGroupable : Boolean? = null ,
                              @ColumnInfo(name="isPriority") val isPriority : Int? = null,
                              @ColumnInfo(name="tags") val tags : List<String>? = null, // all previous notifications should have tags as empty.
                              @ColumnInfo(name="description") val description: String? = null,
                              @ColumnInfo(name="sticky_item_type") val stickyItemType: String = NotificationConstants.STICKY_NONE_TYPE,
                              @ColumnInfo(name="disable_events")val disableEvents: Boolean = false,
                              @ColumnInfo(name="displayed_at_timestamp")val displayedAtTimestamp: String? = null
) {
    constructor(pk: Int, id: String, timeStamp: String, priority: Int, sectionStr: String, dataBlob: ByteArray, expiryTime: String, state: Int, removedFromTray: Boolean, grouped: Boolean, seen: Boolean, deliveryMechanism: Int, synced: Boolean, baseId: String, displayTime: String, shownAsHeadsup: Boolean, removedByApp: Boolean, pendingPosting: Boolean, placement: String, type: String, subType: String, priority_expiry_time: Any, isGroupable: Boolean, isPriority: Int, tags: Any) : this(pk, id, timeStamp, priority, sectionStr, dataBlob, expiryTime, state, removedFromTray, grouped, seen, deliveryMechanism, synced, baseId, displayTime, shownAsHeadsup, removedByApp, pendingPosting, placement, type, subType, priority_expiry_time as Long?, isGroupable, isPriority, tags as List<String>?, null, NotificationConstants.STICKY_NONE_TYPE ) {

    }
}

@Entity(tableName = "notification_present_id", indices = [Index(value = ["id"], unique = true)])
data class NotificationPresentEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk") val pk: Int = 0,
                              @ColumnInfo(name="id") val id: String,
                              @ColumnInfo(name="base_id") val baseId:String? = null,
                              @ColumnInfo(name="filter_type") val filterType:Int? = null
)

@Entity(tableName = "notification_delete", indices = [Index(value = ["base_id"], unique = true)])
data class NotificationDeleteEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk") val pk: Int = 0,
                                     @ColumnInfo(name="base_id") val baseId: String? = null,
                                     @ColumnInfo(name="synced") val synced:Boolean? = null,
                                     @ColumnInfo(name="time_stamp") val timeStamp: String? = null
)

/**
 * NotificationCaching related entity
 */
@Entity(tableName = "notification_cache_details", foreignKeys = [ForeignKey(entity = NotificationEntity::class,
                                                                                                    parentColumns = arrayOf("id"),
                                                                                                    childColumns = arrayOf("id"),
                                                                                                    onDelete = ForeignKey.CASCADE)])
data class NotificationPrefetchEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk") val pk: Int = 0,
                                      @ColumnInfo(name = "id") @NonNull val notificationId: String,
                                      @ColumnInfo(name = "post_notification") val shouldPostNotification: Boolean = true,
                                      @ColumnInfo(name = "retry_number") val retryNumber: Int,
                                      @ColumnInfo(name = "last_retry_time") val lastRetryTimestamp: Long,
                                      @ColumnInfo(name = "received_time") val receivedAtTimestamp: Long,
                                      @ColumnInfo(name = "notification_cached") val isNotificationCached: Boolean = false)

@Entity(tableName = "in_app_notification", indices = [Index(value = ["id"], unique = true)])
data class InAppNotificationEntity(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk") val pk: Int = 0,
                                   @ColumnInfo(name="id") val id: String,
                                   @ColumnInfo(name="data") val data:ByteArray? = null,
                                   @ColumnInfo(name = "time_stamp") val timeStamp:Long? = null,
                                   @ColumnInfo(name="priority") val priority:Int? = null,
                                   @ColumnInfo(name="language") val language: String ?= null,
                                   @ColumnInfo(name = "endTime") val endTime: Long?= 0L,
                                   @ColumnInfo(name = "inAppNotificationCtaLink")val inAppNotificationCtaLink: String ?= null,
                                   @ColumnInfo(name="in_app_notification_info") val inAppInfo: InAppNotificationInfo,
                                   @ColumnInfo(name="disable_lang_filter")val disableLangFilter: Boolean = false,
                                   @ColumnInfo(name="status") val status: String = Constants.NOT_SEEN)





