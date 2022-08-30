/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.*
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.AppSettingsProvider.getNotificationLiveData
import com.newshunt.news.model.daos.BaseDao
import com.newshunt.notification.helper.NOTIFICATION_FILTER_ALL
import com.newshunt.notification.helper.NOTIFICATION_FILTER_CONTENT
import com.newshunt.notification.helper.NOTIFICATION_FILTER_SOCIAL
import com.newshunt.notification.helper.NotificationUtils
import com.newshunt.notification.model.entity.*
import com.newshunt.notification.sqlite.NotificationDB

/**
 * @author aman.roy
 */

const val SOCIAL = "social"

@Dao
abstract class NotificationDao : BaseDao<NotificationEntity> {

    companion object {
       const val NOTIFICATION_EXPIRY_TIME: Long= 7 * 24 * 60 * 60 * 1000; // 7 days
    }

    @Transaction
    open fun addDeferredNotification(notificationModel: BaseModel, postUIUpdate: Boolean, addEntryToPrefetchDb: Boolean) {
        val values = setNotificationInfo(notificationModel) ?: return
        insertNotification(values)
        if (notificationModel.filterValue == NOTIFICATION_FILTER_ALL && !notificationModel.isFullSync) {
            val isSocial = notificationModel.baseInfo != null &&
                    SOCIAL.equals(notificationModel.baseInfo.notifType, ignoreCase = true)
            if (isSocial) {
                insertNotificationId(NotificationPresentEntity(id = notificationModel.baseInfo.uniqueId.toString(),
                        baseId = notificationModel.baseInfo.id, filterType = NOTIFICATION_FILTER_SOCIAL))
            } else {
                insertNotificationId(NotificationPresentEntity(id = notificationModel.baseInfo.uniqueId.toString(),
                        baseId = notificationModel.baseInfo.id, filterType = NOTIFICATION_FILTER_CONTENT))
            }
        } else {
            insertNotificationId(NotificationPresentEntity(
                    id = notificationModel.baseInfo.uniqueId.toString(),
                    baseId = notificationModel.baseInfo.id,
                    filterType = notificationModel.filterValue))
        }
        val unseenNotificationCount = getUnseenNotificationCount()
        if(addEntryToPrefetchDb){
            insertEntryInNotificationCacheDb(notificationModel)
        }
        // updating the UI
        if (postUIUpdate) {
            getNotificationLiveData().postValue(unseenNotificationCount > 0)
        }
    }

    @Transaction
    open fun addNotification(notificationModel: BaseModel?, postUIUpdate: Boolean, state: Int? = null) {
        val values : NotificationEntity = setNotificationInfo(notificationModel, state) ?: return

        val needToInsertInfo = !isDuplicateNotificationInfo(notificationModel)
        if(needToInsertInfo) insertNotification(values)
        if (notificationModel!!.filterValue == NOTIFICATION_FILTER_ALL) {
            val isSocial = notificationModel.baseInfo != null &&
                    SOCIAL.equals(notificationModel.baseInfo.notifType, ignoreCase = true)
            if (isSocial) {
                insertNotificationId(NotificationPresentEntity(id = notificationModel.baseInfo.uniqueId.toString(),
                        baseId = notificationModel.baseInfo.id, filterType = NOTIFICATION_FILTER_SOCIAL))
            } else {
                insertNotificationId(NotificationPresentEntity(id = notificationModel.baseInfo.uniqueId.toString(),
                    baseId = notificationModel.baseInfo.id, filterType = NOTIFICATION_FILTER_CONTENT))
            }
        } else {
            insertNotificationId(NotificationPresentEntity(
                    id = notificationModel.baseInfo.uniqueId.toString(),
                    baseId = notificationModel.baseInfo.id,
                    filterType = notificationModel.filterValue))
        }
        val unseenNotificationCount = getUnseenNotificationCount()
        if(!notificationModel.baseModelType.equals(BaseModelType.SILENT_MODEL)){
            insertEntryInNotificationCacheDb(notificationModel)
        }
        // updating the UI
        if (postUIUpdate) {
            getNotificationLiveData().postValue(unseenNotificationCount > 0)
        }
    }

    @Transaction
    open fun addAdjunctStickyNotification(notificationStickyModel:BaseModel?) {
        val values : NotificationEntity = setNotificationInfo(notificationStickyModel) ?: return
        insertNotification(values)
    }

    @Transaction
    open fun getAdjunctStickyNavModel():List<AdjunctLangStickyNavModel> {
        val adjunctNotificationEntities = getAdjunctStickyNotifications()
        return executeGetAdjunctStickyNotificationQuery(adjunctNotificationEntities)
    }

    @Transaction
    open fun deleteExpiredAdjunctNotifications():List<String> {
        val expiredIds = getExpiredAdjunctStickyNavModelId(System.currentTimeMillis())
        deleteAdjunctStickyNotifications(expiredIds)
        return expiredIds
    }

    @Query("SELECT `id` FROM `notification_info` where expiry_time <= :expiryTime AND `type` ='adjunct_sticky'")
    abstract fun getExpiredAdjunctStickyNavModelId(expiryTime:Long):List<String>

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events FROM `notification_info` ni where ni.type='adjunct_sticky' AND ni.removed_from_tray = 0  order by time_stamp""")
    abstract fun getAdjunctStickyNotifications():List<NotificationEntity>

    @Query("UPDATE `notification_info` SET `removed_from_tray`=1 where `id` IN (:ids)")
    abstract fun deleteAdjunctStickyNotifications(ids:List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract  fun insertNotificationId(notificationPresentEntity: NotificationPresentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract  fun insertNotificationId(notificationPresentEntity: List<NotificationPresentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNotification(notificationEntity: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNotification(notificationEntity: List<NotificationEntity>)

    fun setNotificationInfo(notification: BaseModel?, state: Int? = null) : NotificationEntity? {
        if (notification?.baseModelType == null) {
            return null
        }

        val baseInfo = when (notification.baseModelType) {
            BaseModelType.NEWS_MODEL,
            BaseModelType.TV_MODEL,
            BaseModelType.ADS_MODEL,
            BaseModelType.WEB_MODEL,
            BaseModelType.NAVIGATION_MODEL,
            BaseModelType.STICKY_MODEL,
            BaseModelType.LIVETV_MODEL,
            BaseModelType.EXPLORE_MODEL,
            BaseModelType.FOLLOW_MODEL,
            BaseModelType.DEEPLINK_MODEL,
            BaseModelType.SOCIAL_COMMENTS_MODEL,
            BaseModelType.PROFILE_MODEL,
            BaseModelType.SEARCH_MODEL,
            BaseModelType.GROUP_MODEL,
            BaseModelType.SILENT_MODEL,
            BaseModelType.ADJUNCT_MESSAGE-> notification.baseInfo
            else -> return null
        }

        val gson = Gson()
        val jsonData = gson.toJson(notification)
        val timeStamp =  if (baseInfo.timeStamp > 0) baseInfo.timeStamp else System.currentTimeMillis()
        val sectionType = baseInfo.sectionType?.toString()
        val displayAtTimeStr = if(baseInfo.displayedAtTime == -1L)null else baseInfo.displayedAtTime.toString()
        return NotificationEntity( data = jsonData.toByteArray(),
                id = baseInfo.uniqueId.toString(),
                priority = baseInfo.priority,
                state = state?:NotificationConstants.NOTIFICATION_STATUS_UNREAD,
                removedFromTray = false,
                grouped = false,
                seen = false,
                delieveryMechanism = baseInfo.deliveryType.value,
                synced = baseInfo.isSynced,
                baseId = baseInfo.id,
                shownAsHeadsup = false,
                type = baseInfo.type,
                subType = baseInfo.subType,
                pendingPosting = true,
                timeStamp = timeStamp.toString(),
                expiryTime = if(baseInfo.expiryTime > 0) baseInfo.expiryTime.toString() else  (timeStamp + NotificationConstants.NOTIFICATION_EXPIRY_TIME).toString(),
                section = sectionType,
                placement = if (baseInfo.placement == null) { NotificationPlacementType.TRAY_AND_INBOX.name } else { baseInfo.placement.name },
                displayTime = if(baseInfo.isDeferred) baseInfo.v4DisplayTime.toString() else null,
                priority_expiry_time = if(baseInfo.imp == null) 0 else baseInfo.imp.end,
                isPriority = if(baseInfo.imp == null) 0 else baseInfo.imp.p,
                isGroupable = baseInfo.groupable,
                tags = baseInfo.tags,
                description = notification.description,
                stickyItemType = notification.stickyItemType,
                disableEvents = notification.isLoggingNotificationEventsDisabled,
                displayedAtTimestamp = displayAtTimeStr
        )
    }

    @Transaction
    open fun getNonDeferredNotifications(excludeTrayOnly: Boolean, filterTypes: List<Int>? = null): List<BaseModel?>? {
        var notifications : List<NotificationEntity?>? = null
        val filterList = filterTypes?:getAllFilterTypes()
        if(filterList.isNullOrEmpty()){
            return mutableListOf<BaseModel?>()
        }
        notifications = if (excludeTrayOnly) getNonDeferredNotifications(filterList)
        else getNonDeferredNotificationExcludeTrayOnly(filterList)

        return executeGetNotificationQuery(notifications, false)
    }

    @Query("""SELECT filter_type FROM notification_present_id""")
    abstract fun getAllFilterTypes() : List<Int>?

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON 
        np.id = ni.id WHERE
        np.filter_type in (:filterTypes) AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' AND ni.display_time is null ORDER BY
        ni.time_stamp DESC
    """)
    abstract fun getNonDeferredNotifications(filterTypes: List<Int>) : List<NotificationEntity?>?

    //TODO Change TRAY_ONLY to NotificationPlacementType.TRAY_ONLY

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON 
        np.id = ni.id WHERE 
        np.filter_type in (:filterTypes) AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' AND ni.placement is not 'TRAY_ONLY' AND ni.display_time is null ORDER BY
        ni.time_stamp DESC
    """)
    abstract fun getNonDeferredNotificationExcludeTrayOnly(filterTypes: List<Int>) : List<NotificationEntity?>?


    @Transaction
    open fun getDeferredNotifications(): List<BaseModel?>? {
        val notifications = getDeferredNotificationsQuery()
        return executeGetNotificationQuery(notifications, false)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
         FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is not null ORDER BY ni.time_stamp DESC
    """)
    abstract fun getDeferredNotificationsQuery() : List<NotificationEntity?>?

    @Transaction
    open fun getNonDeferredNonStickyNewsSectionNotifications(): List<NewsNavModel?>? {
        val notifications = getNonDeferredNonStickyNewsSectionNotificationsQuery()
        return executeGetNewsNotificationQuery(notifications)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.section = 'NEWS' AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp DESC 
    """)
    abstract fun getNonDeferredNonStickyNewsSectionNotificationsQuery() : List<NotificationEntity?>?

    @Transaction
    open fun executeGetNewsNotificationQuery(notifications: List<NotificationEntity?>?) : List<NewsNavModel?> {
        val notificationsModel:MutableList<NewsNavModel?> = mutableListOf()
        if (notifications.isNullOrEmpty()) {
            return notificationsModel
        }

        notifications.forEach { notificationEntity ->
            var navigationModel:NewsNavModel? = null
            val dataBlob = notificationEntity?.data
            val dataJson = dataBlob?.let { String(it) }
            val builder = GsonBuilder()
            val gson = builder.enableComplexMapKeySerialization().create()
            navigationModel = try {
                gson.fromJson(dataJson,NewsNavModel::class.java)
            } catch (e: java.lang.Exception) {
                Logger.caughtException(e)
                NotificationUtils.handleWrongExpiryTimeValue(navigationModel,
                        NotificationSectionType.NEWS, dataJson, gson) as NewsNavModel
            }
            navigationModel?.baseInfo?.state = notificationEntity.let { if (it == null) 0 else it.state.let { it1 -> if(it1 == null) 0 else it1 } }
            navigationModel?.setDisableEvents(notificationEntity?.disableEvents?: false)
            notificationsModel.add(navigationModel)
        }

        return notificationsModel

    }

    @Transaction
    open fun executeGetAdjunctStickyNotificationQuery(notifications: List<NotificationEntity?>?) : List<AdjunctLangStickyNavModel> {
        val notificationsModel:MutableList<AdjunctLangStickyNavModel> = mutableListOf()
        if (notifications.isNullOrEmpty()) {
            return notificationsModel
        }

        notifications.forEach { notificationEntity ->
            lateinit var navigationModel:AdjunctLangStickyNavModel
            val dataBlob = notificationEntity?.data
            val dataJson = dataBlob?.let { String(it) }
            val builder = GsonBuilder()
            val gson = builder.enableComplexMapKeySerialization().create()
            navigationModel = try {
                gson.fromJson(dataJson,AdjunctLangStickyNavModel::class.java)
            } catch (e: java.lang.Exception) {
                Logger.caughtException(e)
                NotificationUtils.handleWrongExpiryTimeValue(navigationModel,
                    NotificationSectionType.NEWS, dataJson, gson) as AdjunctLangStickyNavModel
            }
            navigationModel.baseInfo?.state = notificationEntity.let { if (it == null) 0 else it.state.let { it1 -> if(it1 == null) 0 else it1 } }
            navigationModel.setDisableEvents(notificationEntity?.disableEvents?: false)
            notificationsModel.add(navigationModel)
        }

        return notificationsModel

    }

    @Transaction
    open fun getTopNonDeferredNonStickyNotificationsForTray(limit: Int): List<BaseModel?>? {
        val notifications = getTopNonDeferredNonStickyNotificationsForTrayQuery(limit)
        return executeGetNotificationQuery(notifications, false)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events, ni.displayed_at_timestamp
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND ni.removed_from_tray = 0 
        AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp DESC LIMIT :limit
    """)
    abstract fun getTopNonDeferredNonStickyNotificationsForTrayQuery(limit: Int):List<NotificationEntity?>?

    @Transaction
    open fun getGroupedNonDeferredNonStickyNotifications(): List<BaseModel?> {
        val notificationPlacementFilter = listOf(NotificationPlacementType.TRAY_ONLY.name,
                NotificationPlacementType.TRAY_AND_INBOX.name)

        val notifications = getGroupedNonDeferredNonStickyNotificationsQuery(notificationPlacementFilter)
        return executeGetNotificationQuery(notifications, false)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events, ni.displayed_at_timestamp
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND ni.removed_from_tray = 0
        AND ni.grouped = 1 AND ni.placement in (:notificationPlacementFilter)
        AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp 
    """)
    abstract fun getGroupedNonDeferredNonStickyNotificationsQuery(notificationPlacementFilter: List<String>):List<NotificationEntity?>?

    @Transaction
    open fun getNonGroupedNonDeferredNonStickyNotification(): List<BaseModel?>? {
        val notificationPlacementFilter = listOf(NotificationPlacementType.TRAY_ONLY.name,
                NotificationPlacementType.TRAY_AND_INBOX.name)

        val notifications = getNonGroupedNonDeferredNonStickyNotificationQuery(notificationPlacementFilter)
        return executeGetNotificationQuery(notifications, false)
    }

    @Query(""" SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events, ni.displayed_at_timestamp
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND ni.removed_from_tray = 0
        AND ni.grouped = 0 AND ni.placement in (:notificationPlacementFilter)
        AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp
    """)
    abstract fun getNonGroupedNonDeferredNonStickyNotificationQuery(notificationPlacementFilter: List<String>):List<NotificationEntity?>?

    @Transaction
    open fun getNonDeferredNonStickyNotification(): List<BaseModel?> {
        val notificationPlacementFilter = listOf(NotificationPlacementType.TRAY_ONLY.name,
                NotificationPlacementType.TRAY_AND_INBOX.name)

        val notifications = getNonDeferredNonStickyNotificationQuery(notificationPlacementFilter)
        return executeGetNotificationQuery(notifications, false)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events, ni.displayed_at_timestamp
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND removed_from_tray = 0 AND grouped = 0 
        AND placement in (:notificationPlacementFilter)
        AND type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY time_stamp DESC
    """)
    abstract fun getNonDeferredNonStickyNotificationQueryDecreasingTimeStampOrder(notificationPlacementFilter: List<String>):List<NotificationEntity?>?

    @Transaction
    open fun getNonDeferredNonStickyNotificationInDecreasingTimestampOrder(): List<BaseModel?>{
        val notificationPlacementFilter = listOf(NotificationPlacementType.TRAY_ONLY.name,
                NotificationPlacementType.TRAY_AND_INBOX.name)

        val notifications = getNonDeferredNonStickyNotificationQueryDecreasingTimeStampOrder(notificationPlacementFilter)
        return executeGetNotificationQuery(notifications, false)
    }

    private fun queryConditionForAllowedPlacement(stringList: List<String>) : String {
        val sb = StringBuilder();
        for (i in 0..stringList.size - 1) {
            sb.append(NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT)
            sb.append(" IS ")
            sb.append("'")
            sb.append(stringList[i])
            sb.append("'")
            sb.append(" OR ");
        }
        return sb.toString();
  }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events, ni.displayed_at_timestamp
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND removed_from_tray = 0 AND grouped = 0 
        AND placement in (:notificationPlacementFilter)
        AND type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY isPriority DESC,  time_stamp DESC
    """)
    abstract fun getNonDeferredNonStickyNotificationQuery(notificationPlacementFilter: List<String>):List<NotificationEntity?>?

    @Transaction
    open fun getUnpostedNonDeferredNonStickyNotification(): List<BaseModel?>? {
        val notifications = getUnpostedNonDeferredNonStickyNotificationQuery()
        return executeGetNotificationQuery(notifications, false)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events, ni.displayed_at_timestamp
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND ni.removed_from_tray = 0
        AND ni.pending_posting = 1 
        AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp
    """)
    abstract fun getUnpostedNonDeferredNonStickyNotificationQuery():List<NotificationEntity?>?

    @Query("UPDATE notification_info SET state = ${NotificationConstants.NOTIFICATION_STATUS_READ}, seen = 1 WHERE id = :notId")
    abstract fun markNotificationAsRead(notId: String?)

    @Query("UPDATE notification_info SET seen = 1 WHERE  display_time is null AND type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND (sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' OR sticky_item_type is '${NotificationConstants.STICKY_NEWS_TYPE}')")
    abstract fun markAllNotificationAsSeen()

    @Query("UPDATE notification_info SET removed_from_tray = 1 WHERE id = :notId")
    abstract fun markNotificationAsDeletedFromTray(notId: Int)

    @Query("UPDATE notification_info SET pending_posting = 1 WHERE id = :notId")
    abstract fun markNotificationAsPostedToTray(notId: Int)

    @Query("""UPDATE notification_info SET removed_from_tray = 1
        WHERE grouped = 1
    """)
    abstract fun markGroupedNotificationAsDeletedFromTray()

    @Query("""UPDATE notification_info SET removed_from_tray = 1 
        WHERE state is not ${NotificationConstants.NOTIFICATION_STATUS_READ} AND 
        display_time is null AND 
        type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND (sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' OR sticky_item_type is '${NotificationConstants.STICKY_NEWS_TYPE}')
    """)
    abstract fun markAllNotificationAsDeletedFromTray()

    @Query("UPDATE notification_info SET synced = 1")
    abstract fun markAllNotificationsAsSynced()

    /**
     * This method is used to get a list of all notifications which are not synced. These
     * notification ids are passed as a parameter in a pull request.
     *
     * @param limit
     * @return
     */
    @Transaction
    open fun getUnsyncedNotificationIdList(limit: Int): List<String?>? {

        var notifications : List<NotificationEntity?>? = null
        if (limit > 0) notifications = getUnsyncedNotificationIdListQuery(limit)
        else notifications = getUnsyncedNotificationIdListQuery()
        return executeGetNotificationIdListQuery(notifications)
    }

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.synced = 0 AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp DESC LIMIT :limit""")
    abstract fun getUnsyncedNotificationIdListQuery(limit: Int):List<NotificationEntity?>?

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.synced = 0 AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' ORDER BY ni.time_stamp DESC""")
    abstract fun getUnsyncedNotificationIdListQuery():List<NotificationEntity?>?

    @Transaction
    open fun executeGetNotificationIdListQuery(notifications: List<NotificationEntity?>?): List<String?>? {
            val notificationIds: MutableList<String> = mutableListOf()
        if (notifications.isNullOrEmpty()) {
            return notificationIds
        }
        notifications.forEach {
                notification -> notification?.baseId?.let { notificationIds.add(notification.baseId) }
        }
        return notificationIds
    }

    @Query("DELETE FROM notification_info WHERE id = :notId")
    abstract fun deleteNotification(notId: Int)

    @Query("UPDATE notification_info SET isPriority = 0 WHERE priority_expiry_time < :expiryTime")
    abstract fun markNotificationDePriority(expiryTime: Long)

    @Query("DELETE FROM notification_info WHERE id IN (:notIds)")
    abstract fun deleteNotifications(notIds: Array<String?>?): Int

    @Query("DELETE FROM notification_info WHERE section = 'SOCIAL_SECTION'")
    abstract fun deleteSocialNotification(): Int

    @Transaction
    open fun getNotification(notId: Int, allowSilentNotification: Boolean): BaseModel? {
        val notifications = getNotificationQuery(notId)
        var notificationsModel:List<BaseModel?> = mutableListOf()
        notificationsModel = executeGetNotificationQuery(notifications, allowSilentNotification)
        return if (notificationsModel.size > 0) {
            notificationsModel[0]
        } else null
    }

    @Query(""" SELECT ni.pk,ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_info ni WHERE ni.id = :notId""")
    abstract fun getNotificationQuery(notId: Int) : List<NotificationEntity?>?

    @Transaction
    open fun getNotificationDeliveryTypeByBaseInfoId(baseInfoId: String?): NotificationDeliveryMechanism? {
        var deliveryMechanism: NotificationDeliveryMechanism? = null
        val delieverType = getNotificationDeliveryTypeByBaseInfoIdQuery(baseInfoId)
        if(delieverType != null) {
            deliveryMechanism = NotificationDeliveryMechanism.fromDeliveryType(delieverType)
        }
        return deliveryMechanism
    }

    @Query("SELECT delivery_mechanism FROM notification_info WHERE base_id = :baseInfoId limit 1")
    abstract fun getNotificationDeliveryTypeByBaseInfoIdQuery(baseInfoId: String?):Int?

    @Transaction
    open fun isDuplicateNotification(baseModel: BaseModel?): Boolean {
        if (null == baseModel || null == baseModel.baseInfo) {
            return false
        }
        val baseInfoId = baseModel.baseInfo.id

        return getCountDuplicateNotPresentId(baseInfoId) > 0
    }

    @Query("SELECT COUNT(base_id) FROM notification_present_id WHERE base_id = :baseInfoId")
    abstract fun getCountDuplicateNotPresentId(baseInfoId: String?):Int

    @Transaction
    open fun isDuplicateNotificationInfo(baseModel: BaseModel?): Boolean {
        if (null == baseModel || null == baseModel.baseInfo) {
            return false
        }
        val baseInfoId = baseModel.baseInfo.id

        return getCountDuplicateNotInfo(baseInfoId) > 0
    }

    @Query("SELECT COUNT(base_id) FROM notification_info WHERE base_id = :baseInfoId")
    abstract fun getCountDuplicateNotInfo(baseInfoId: String?):Int

    @Transaction
    open fun isNotificationDeleted(baseInfoId: String?): Boolean {
        return getCountDeletedNotDelete(baseInfoId) > 0
    }

    @Query("SELECT COUNT(base_id) FROM notification_delete WHERE base_id = :baseInfoId")
    abstract fun getCountDeletedNotDelete(baseInfoId: String?):Int

    @Transaction
    open fun getNotificationToDelete(): List<NotificationId>? {
        val notifications = getNotificationToDeleteQuery()
        return executeGetDeletedNotificationQuery(notifications)
    }

    @Query("SELECT nd.pk,nd.base_id,nd.synced,nd.time_stamp FROM notification_delete nd WHERE nd.synced is 0")
    abstract fun getNotificationToDeleteQuery() : List<NotificationDeleteEntity>?


    @Transaction
    open fun executeGetNotificationQuery(notifications: List<NotificationEntity?>?, allowSilentNotifications: Boolean): List<BaseModel?> {
        val notificationsModel = mutableListOf<BaseModel?>()
        if (notifications.isNullOrEmpty()) return notificationsModel
        notifications.forEach {
                notification -> notification?.let{
            val sectionStr = it.section
            val sectionType = NotificationSectionType.getSectionType(sectionStr)
            val dataBlob = notification.data
            val dataJson = dataBlob?.let { it1 -> String(it1) }
            val builder = GsonBuilder()
            val gson = builder.enableComplexMapKeySerialization().create()
            var baseInfo: BaseInfo? = null
            var baseModel: BaseModel? = null
            val notificationType = notification.type
            val notificationSubType = notification.subType
            try {
                baseModel = extraBaseModel(notificationType,notificationSubType,sectionType,dataJson,gson)
            } catch (e: Exception) {
                Logger.caughtException(e)
                baseModel = NotificationUtils.handleWrongExpiryTimeValue(baseModel, sectionType, dataJson, gson)
            }
            if (baseModel!=null) {
                baseInfo = baseModel.baseInfo
                baseModel.stickyItemType = notification.stickyItemType
                baseModel.description = notification.description
                baseModel.setDisableEvents(notification.disableEvents)
                baseModel.isSeen = notification.seen?:false
            }
            if(baseInfo != null) {
                baseInfo.displayedAtTime = notification.displayedAtTimestamp?.toLong()?:-1L
                baseInfo.state = notification.state ?: 0
                baseInfo.setIsRemovedFromTray(notification.removedFromTray ?: false)
                baseInfo.setIsGrouped(notification.grouped ?: false)
                baseInfo.setIsSynced(notification.synced ?: false)
                baseModel?.baseInfo?.deliveryType = NotificationDeliveryMechanism.fromDeliveryType (
                    notification.delieveryMechanism ?: 0
                )
            }

            if (baseModel != null){
                if(!allowSilentNotifications && baseModel.baseInfo.sectionType.equals(NotificationSectionType.SILENT)){

                }else{
                    notificationsModel.add(baseModel)
                }

            }
        }
        }
        return notificationsModel
    }


    @Transaction
    open fun executeGetDeletedNotificationQuery(notifications: List<NotificationDeleteEntity>?): List<NotificationId> {
        val notificationIds = mutableListOf<NotificationId>()
        if (notifications.isNullOrEmpty()) {
            return notificationIds
        }
        notifications.forEach {
            var notificationId: NotificationId? = null
            val id = it.baseId
            val ts = it.timeStamp?.toLongOrNull()
            try {
                notificationId = NotificationId(id!!, ts!!)
            } catch (e: java.lang.Exception) {
                Logger.caughtException(e)
            }
            if (notificationId != null) {
                notificationIds.add(notificationId)
            }
        }
        return notificationIds
    }

    @Transaction
    open fun extraBaseModel(notificationType: String?, notificationSubType: String?, sectionType: NotificationSectionType?, dataJson: String?, gson: Gson): BaseModel? {
        var baseModel: BaseModel? = null
        if (NotificationConstants.NOTIFICATION_TYPE_STICKY == notificationType && !CommonUtils.isEmpty(notificationSubType)) {
            when (notificationSubType) {
                NotificationConstants.STICKY_CRICKET_TYPE -> {
                    val collectionType = object : TypeToken<StickyNavModel<CricketNotificationAsset, CricketDataStreamAsset>>() {}.type
                    baseModel = gson.fromJson<BaseModel>(dataJson, collectionType)
                }
            }
            return baseModel
        }
        when (sectionType) {
            NotificationSectionType.APP -> baseModel = gson.fromJson(dataJson, NavigationModel::class.java)
            NotificationSectionType.NEWS -> baseModel = gson.fromJson(dataJson, NewsNavModel::class.java)
            NotificationSectionType.TV -> baseModel = gson.fromJson(dataJson, TVNavModel::class.java)
            NotificationSectionType.LIVETV -> baseModel = gson.fromJson(dataJson, LiveTVNavModel::class.java)
            NotificationSectionType.WEB -> baseModel = gson.fromJson(dataJson, WebNavModel::class.java)
            NotificationSectionType.ADS -> baseModel = gson.fromJson(dataJson, AdsNavModel::class.java)
            NotificationSectionType.EXPLORE_SECTION -> baseModel = gson.fromJson(dataJson, ExploreNavModel::class.java)
            NotificationSectionType.FOLLOW_SECTION -> baseModel = gson.fromJson(dataJson, FollowNavModel::class.java)
            NotificationSectionType.DEEPLINK_SECTION -> baseModel = gson.fromJson(dataJson, DeeplinkModel::class.java)
            NotificationSectionType.SOCIAL_SECTION -> baseModel = gson.fromJson(dataJson, SocialCommentsModel::class.java)
            NotificationSectionType.PROFILE_SECTION -> baseModel = gson.fromJson(dataJson, ProfileNavModel::class.java)
            NotificationSectionType.SEARCH_SECTION -> baseModel = gson.fromJson(dataJson, SearchNavModel::class.java)
            NotificationSectionType.GROUP_SECTION -> baseModel = gson.fromJson(dataJson, GroupNavModel::class.java)
            NotificationSectionType.SILENT -> baseModel = gson.fromJson(dataJson, SilentNotificationModel::class.java)
            else -> {

            }
        }
        return baseModel
    }

    @Query("UPDATE notification_info SET synced = 1 WHERE synced = 0")
    abstract fun markAllNotificationSynced()

    @Transaction
    open fun insertNotificationToDelete(notificationIds: List<NotificationId?>?) {
        notificationIds?.map {
            val contentValues = NotificationDeleteEntity(baseId = it?.id,synced = false, timeStamp = it?.ts.toString())
            insertDeleteNotificationWithConflict(contentValues)
        }

    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDeleteNotificationWithConflict(contentValues: NotificationDeleteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDeleteNotificationWithConflict(contentValues: List<NotificationDeleteEntity>)

    @Query("DELETE FROM notification_delete WHERE synced is 1")
    abstract fun deleteAllDeleteNotificationSynced()

    /***
     * @note : this function is only used for getting count for unseen notification and set badge
     * on notification inbox icon. but if we use this for any different purpose than will need to
     * add condition to include "TRAY_ONLY" notification if required.
     */
    @Query("""SELECT COUNT(*) FROM notification_present_id INNER JOIN notification_info ON notification_present_id.id = notification_info.id WHERE 
        display_time is null AND 
        seen = 0 AND 
        type is not 'sticky' AND
        placement is not 'TRAY_ONLY'
    """)
    abstract fun getUnseenNotificationCount(): Int

    @Query("DELETE FROM notification_info WHERE display_time is null AND time_stamp < :time")
    abstract fun deleteOldNonDeferredNotifications(time:Long)

    @Transaction
    open fun deleteOldDeferredNotifications() {
        val expiryTime = System.currentTimeMillis() - NOTIFICATION_EXPIRY_TIME
        deleteNotificationInfo(expiryTime)
        deleteNonPresentNotificationId()
    }
    @Query("DELETE FROM notification_info WHERE display_time is not null AND display_time < :expiryTime")
    abstract fun deleteNotificationInfo(expiryTime:Long)

    @Query("DELETE FROM notification_present_id WHERE id not in (SELECT id FROM notification_info)")
    abstract fun deleteNonPresentNotificationId()


    @Query("UPDATE notification_info SET grouped = 1 WHERE id in (:notificationIds)")
    abstract fun markNotificationAsGrouped(notificationIds:List<Int>)

    @Query("DELETE FROM notification_present_id")
    abstract fun clearIdList()

    @Query("DELETE FROM notification_present_id WHERE filter_type & :filter is NOT 0")
    abstract fun clearIdListWithFilter(filter: Int)

    @Query("UPDATE notification_info SET shown_as_headsup = 1 WHERE id=:notificationId")
    abstract fun markNotificationAsHeadsUp(notificationId: Int)

    @Query("""
        SELECT COUNT(*) FROM notification_present_id p INNER JOIN notification_info i ON p.id = i.id 
        WHERE i.state = :isRead AND i.section = :appSection
    """)
    abstract fun getAnalyticsCount(appSection: String, isRead: Boolean) : Int


    @Query("""
        DELETE FROM notification_info WHERE expiry_time IS NOT NULL AND expiry_time < :currentTime AND sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}'
    """)
    abstract fun deleteExpiredNotifications(currentTime: Long)

    @Transaction
    open fun clearExpiredNotifications() {
        deleteExpiredNotifications(System.currentTimeMillis())
    }

    @Query("""
        SELECT COUNT(*) FROM notification_info WHERE id = :notificationId AND shown_as_headsup = 1 AND removed_by_app = 1
    """)
    abstract fun isNotificationAlreadyShownAsHeadsUp(notificationId: Int): Boolean


    @Query("""
        UPDATE notification_info SET removed_by_app = 1 WHERE id IN (:notificationIds)
    """)
    abstract fun markNotificationAsRemovedFromTrayByApp(notificationIds: List<Int>)

    @Query("""
        DELETE FROM notification_info WHERE time_stamp > :start AND time_stamp < :end AND sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}'
    """)
    abstract fun flushTimeRangeNotifications(start:Long, end: Long)

    fun insertEntryInNotificationCacheDb(baseModel: BaseModel?) {
        if(baseModel == null || (baseModel != null && baseModel.baseInfo == null)) {
            return;
        }
        try {
            val uniqueId = baseModel.baseInfo.uniqueId;
            val entry = NotificationDB.instance().getNotificationPrefetchInfoDao().getPrefetchEntryForNotificationWithId(uniqueId);
            if(entry == null && (getNotification(baseModel.baseInfo.uniqueId, false)) != null) {
                val currentTime = System.currentTimeMillis()
                NotificationDB.instance().getNotificationPrefetchInfoDao().addEntryToPrefetchDb(baseModel, true, 0, 0, currentTime, false);
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
    }

    @Query("""UPDATE notification_info SET sticky_item_type = :toType WHERE sticky_item_type = :fromType """)
    abstract fun updateStickyItemType(fromType: String, toType: String)

    @Query("""UPDATE notification_info SET sticky_item_type = :toType WHERE id IN (:notificationIds)""")
    abstract fun updateNotificationStickyItemType(notificationIds: List<Int>, toType: String)

    @Query("""DELETE FROM notification_info WHERE id IN (:notificationIds)""")
    abstract fun deleteNotificationsFor(notificationIds: List<Int>)

    @Query(""" SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE
        ni.display_time is null AND ni.state is not ${NotificationConstants.NOTIFICATION_STATUS_READ} AND ni.removed_from_tray = 0 AND ni.seen = 0
        AND ni.sticky_item_type is :stickyType LIMIT :limit
    """)
    abstract fun getStickyNotificationItems(stickyType: String, limit: Int): List<NotificationEntity?>?

    @Transaction
    open fun getStickyNotifications(type: String, limit: Int = Int.MAX_VALUE): List<BaseModel?>?{
        val notifications = getStickyNotificationItems(type, limit)
        notifications?.let{
            return executeGetNotificationQuery(notifications, false)
        }
        return null
    }

    @Query(""" SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state is not ${NotificationConstants.NOTIFICATION_STATUS_READ} AND ni.removed_from_tray = 0 AND ni.seen = 0
        AND ni.sticky_item_type IS :type AND ni.id IN (:ids)""")
    abstract fun getExistingStickyItemsFor(type: String, ids: List<Int>): List<NotificationEntity?>?

    @Transaction
    open fun fetchExistingStickItemsFor(type: String, ids: List<Int>): List<BaseModel?>?{
        getExistingStickyItemsFor(type, ids)?.let{ items ->
            return executeGetNotificationQuery(items, false)
        }
        return null
    }

    @Query("DELETE FROM notification_present_id WHERE id =:id")
    abstract fun deleteNotificationInfoWithId(id:Int)

    @Transaction
    open fun deleteIdFromNotificationTableAndNotificationInfoTable(id: Int){
        deleteNotification(id)
        deleteNotificationInfoWithId(id)
    }

    @Transaction
    open fun removeEntryIfExistsAndAdd(notificationModel: BaseModel?, postUIUpdate: Boolean, state: Int? = null){
        notificationModel?.baseInfo?.uniqueId?.let{
            deleteIdFromNotificationTableAndNotificationInfoTable(it)
        }
        addNotification(notificationModel, postUIUpdate, state)
    }

    @Query("DELETE FROM notification_info WHERE sticky_item_type =:type AND state is ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND seen = 0")
    abstract fun deleteUnreadNotificationsForStickyType(type: String)

    @Query("UPDATE notification_info SET displayed_at_timestamp = :timestamp WHERE id = :notId")
    abstract fun updateNotificationDisplayedAtTimeStamp(notId: Int, timestamp: Long)

    @Query("SELECT displayed_at_timestamp FROM notification_info WHERE id = :notId")
    abstract fun getDisplayedAtTime(notId: Int): String?

    @Transaction
    open fun getDisplayAtTimeFor(notId: Int): Long{
        val displayAtTime = getDisplayedAtTime(notId)
        displayAtTime?.let {
            return it.toLong()
        }

        return -1L
    }

    @Query("""SELECT ni.id
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND removed_from_tray = 0 AND grouped = 0 AND ni.seen = 0
        AND type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' 
        AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' AND ni.displayed_at_timestamp < :intervalEnd
    """)
    abstract fun getNotificationsNonDeferredNonStickyAlreadyDisplayedNotificationForTimeInterval(intervalEnd: Long): List<String>?

    @Query("UPDATE notification_info SET state = ${NotificationConstants.NOTIFICATION_STATUS_SKIPPED_BY_USER} WHERE id = :notId")
    abstract fun markNotificationAsSkippedByUser(notId: String?)

    @Query("DELETE FROM notification_info WHERE sticky_item_type =:type AND state is ${NotificationConstants.NOTIFICATION_STATUS_SKIPPED_BY_USER} AND seen = 0")
    abstract fun deleteSkippedByUserNotificationsForStickyType(type: String)

    @Query("""SELECT ni.pk, ni.id,ni.time_stamp,ni.priority,ni.section,ni.data,ni.expiry_time,ni.state,ni.removed_from_tray,ni.grouped,ni.seen,ni.delivery_mechanism,
        ni.synced,ni.base_id,ni.display_time,ni.shown_as_headsup,ni.removed_by_app,ni.pending_posting,ni.placement,ni.type,ni.subType,ni.priority_expiry_time,ni.isGroupable,
        ni.isPriority,ni.tags,ni.description,ni.sticky_item_type, ni.disable_events
        FROM notification_present_id np INNER JOIN notification_info ni ON np.id = ni.id WHERE 
        ni.display_time is null AND ni.state = ${NotificationConstants.NOTIFICATION_STATUS_UNREAD} AND ni.removed_from_tray = 0
        AND ni.grouped = 1 AND ni.type is not '${NotificationConstants.NOTIFICATION_TYPE_STICKY}' AND ni.sticky_item_type is '${NotificationConstants.STICKY_NONE_TYPE}' AND ni.id in (:ids) 
    """)
    abstract fun getGroupedNotificationsFor(ids: Array<out String>): List<NotificationEntity?>?

    @Transaction
    open fun getGroupedNotificationsForIds(list: Array<out String>): List<BaseModel?>{
        val notifications = getGroupedNotificationsFor(list)
        return executeGetNotificationQuery(notifications, false)
    }

    @Query("UPDATE notification_info SET removed_from_tray = 1, sticky_item_type = '${NotificationConstants.STICKY_NONE_TYPE}' WHERE id in (:notIds)")
    abstract fun markNotificationAsNormalNotificationAndDeletedFromTray(notIds: Array<out String>)


}