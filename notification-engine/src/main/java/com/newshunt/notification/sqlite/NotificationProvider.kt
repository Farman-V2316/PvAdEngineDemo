/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.sqlite

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.dataentity.notification.asset.OptInEntity
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.notification.model.internal.dao.NotificationContentProviderHelper
import com.newshunt.notification.model.internal.dao.StickyNotificationsDBInstance
import com.newshunt.notification.view.service.ContainerClassForJsonCreation

/**
 * Content provider implementation for notification DB
 *
 * Created by srikanth.r on 10/22/21.
 */
private const val LOG_TAG = "NotificationProvider"
class NotificationProvider: ContentProvider() {
    override fun onCreate(): Boolean {
      Logger.d(LOG_TAG, "NotificationProvider.onCreate")
      return true
    }

    override fun query(uri: Uri,
                       projection: Array<out String>?,
                       selection: String?,
                       selectionArgs: Array<out String>?,
                       sortOrder: String?): Cursor? {
        return if(initFailures()){
            null
        }else{
            when(selection) {
                QUERY_FILTER_READ_SEEN_AND_BLOCKED_NOTIFICATIONS -> {
                    filterReadSeenAndBlockedSourceNotifications(selectionArgs)
                }
                QUERY_GET_NEWS_STICKY_ITEMS-> {
                    var  limit: Int? = null
                    selectionArgs?.let {
                        if(it.size > 0){
                            limit = it.get(0).toInt()
                        }
                    }
                    getNewsStickyItems(limit)
                }
                QUERY_GET_EXISTING_NEWS_STICKY_ITEMS_FOR_IDS ->{
                    getNewsStickyItemsForIds(selectionArgs)
                }
                QUERY_UPDATE_NEWS_STICKY_ENTRY ->{
                    updateNewsStickyEntry(selectionArgs)
                }
                QUERY_UPDATE_NOTIFICATION_STATE_AS_SKIPPED -> {
                    updateNotificationState(selectionArgs)
                }
                QUERY_GET_GROUPED_NOTIFICATIONS ->{
                    getGroupedNotificationsFor(selectionArgs)
                }
                QUERY_ADD_STICKY_NOTIFICATIONS_TO_INBOX_ONLY ->{
                    convertStickyItemsToNormalNotificationsAndAddToInbox(selectionArgs)
                }
                else -> {
                    null
                }
            }
        }

    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if(initFailures() || values == null){
            return null
        }

        (values[NOTIFICATION_DETAILS_STRING] as? String?)?.let {
            val obj = JsonUtils.fromJson(it, ContainerClassForJsonCreation::class.java)
            val modelType = obj.notification_base_model_type
            if (!modelType.isNullOrEmpty()) {
                val baseModelType = BaseModelType.getValue(modelType)
                val baseModelString = obj.notification_base_model
                val baseModel = BaseModelType.convertStringToBaseModel(baseModelString, baseModelType, "")
                notificationDao.removeEntryIfExistsAndAdd(baseModel, true)
            }
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String?>?): Int {
        if (selectionArgs.isNullOrEmpty() || initFailures()) {
            return 0
        }
        Logger.d(LOG_TAG, "Deleting the notification id: $selectionArgs")
        return notificationDao.deleteNotifications(selectionArgs)
    }

    override fun update(uri: Uri,
                        values: ContentValues?,
                        selection: String?,
                        selectionArgs: Array<out String>?): Int {
        //TODO: Implement the update method
        return 0
    }

    private fun filterReadSeenAndBlockedSourceNotifications(selectionArgs: Array<out String>?): Cursor?{
        if(selectionArgs.isNullOrEmpty()){
            return null
        }else{
            val tempList = mutableListOf<BaseModel>()
            val trayManagementSelectedOption = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, -1)
            for(item in selectionArgs){
                val obj = JsonUtils.fromJson(item, ContainerClassForJsonCreation::class.java)
                val modelType = obj.notification_base_model_type
                if(!modelType.isNullOrEmpty()){
                    val baseModelType = BaseModelType.getValue(modelType)
                    val baseModelString = obj.notification_base_model
                    var baseModel: BaseModel? = null
                    try{
                        baseModel = BaseModelType.convertStringToBaseModel(baseModelString, baseModelType, "")
                    }catch(ex: Exception){
                        Logger.caughtException(ex)
                    }
                    if(baseModel != null && baseModel.baseInfo != null && baseModel.baseInfo.uniqueId != null && !baseModel.baseInfo.sourceId.isNullOrEmpty()){
                        val dbNoti = notificationDao.getNotification(baseModel.baseInfo.uniqueId, false)
                        if(SocialDB.instance().followEntityDao().isSourceIdBlocked(baseModel.baseInfo.sourceId)
                                || (dbNoti != null && dbNoti.baseInfo!= null && dbNoti.baseInfo.isRead) || (dbNoti?.isSeen?:false)
                                || (dbNoti != null && dbNoti.baseInfo != null && !dbNoti.baseInfo.isGrouped && dbNoti.stickyItemType.equals(NotificationConstants.STICKY_NONE_TYPE)
                                        && !dbNoti.baseInfo.wasSkippedByUser() && (trayManagementSelectedOption != Constants.ONLY_LIVE_TICKER))){
                            //This item is filtered in this case
                        }else{
                            if(dbNoti?.baseInfo?.wasSkippedByUser()?:false){
                                baseModel.baseInfo.state = NotificationConstants.NOTIFICATION_STATUS_SKIPPED_BY_USER
                            }
                            tempList.add(baseModel)
                        }
                    }
                }
            }
            return NotificationContentProviderHelper.cursorFromBaseModels(tempList)
        }
    }

    private fun getNewsStickyItemsForIds(selectionArgs: Array<out String>?): Cursor?{
        selectionArgs?.let{ ids ->
            val idsInInt = mutableListOf<Int>()
            for(id in ids){
                idsInInt.add(id.toInt())
            }
            notificationDao.fetchExistingStickItemsFor(NotificationConstants.STICKY_NEWS_TYPE, idsInInt)?.let{ items ->
                val tempList = mutableListOf<BaseModel>()
                for(item in items){
                    item?.let{
                        tempList.add(it)
                    }
                }
                return NotificationContentProviderHelper.cursorFromBaseModels(tempList)
            }

        }
        return null
    }

    private fun getNewsStickyItems(limit: Int?):Cursor?{
        val dbList = notificationDao.getStickyNotifications(NotificationConstants.STICKY_NEWS_TYPE, limit?: Int.MAX_VALUE)
        val filteredList = mutableListOf<BaseModel>()
        dbList?.let{items ->
            for(item in items){
                if(item != null && item.baseInfo != null && item.baseInfo.sourceId != null && !SocialDB.instance().followEntityDao().isSourceIdBlocked(item.baseInfo.sourceId)){
                    filteredList.add(item)
                }
            }

            Logger.d(LOG_TAG, "limit passed is $limit and size of list returned is ${filteredList.size}")
            return NotificationContentProviderHelper.cursorFromBaseModels(filteredList)
        }
        return null
    }

    private fun updateNewsStickyEntry(selectionArgs: Array<out String>?): Cursor?{
        selectionArgs?.let{
            if(it.size > 0){
                val optInEntity = JsonUtils.fromJson(it.get(0), OptInEntity::class.java)
                optInEntity?.let{ entity ->
                    val dbEntry = StickyNotificationsDBInstance.stickyNotificationDao().getNotificationByIdAndType(NotificationConstants.NEWS_STICKY_OPTIN_ID, NotificationConstants.STICKY_NEWS_TYPE)
                    val expiryTime = if(entity.expiryTime > 0) entity.expiryTime else dbEntry?.expiryTime?:0L
                    val startTime = if(entity.startTime > 0) entity.startTime else dbEntry?.startTime?:0L
                    StickyNotificationsDBInstance.stickyNotificationDao().updateNotificationDataWithFetchedConfigData(entity.id, entity.type,entity.channelId,entity.metaUrl)
                    StickyNotificationsDBInstance.stickyNotificationDao().updateNotificationDataWith(NotificationConstants.NEWS_STICKY_OPTIN_ID, NotificationConstants.STICKY_NEWS_TYPE,startTime, expiryTime)
                }
            }
        }
        return null
    }

    private fun updateNotificationState(selectionArgs: Array<out String>?): Cursor? {
        selectionArgs?.let {
            for (id in it) {
                notificationDao.markNotificationAsSkippedByUser(id)
            }
        }
        return null
    }

    private fun getGroupedNotificationsFor(selectionArgs: Array<out String>?): Cursor?{
        val groupedNotificationsList = mutableListOf<BaseModel>()
        selectionArgs?.let{ ids ->
            notificationDao.getGroupedNotificationsForIds(ids)?.let{
                it.forEach { item ->
                    if(item != null){
                        groupedNotificationsList.add(item)
                    }
                }
            }
        }

        return NotificationContentProviderHelper.cursorFromBaseModels(groupedNotificationsList)
    }

    private fun convertStickyItemsToNormalNotificationsAndAddToInbox(selectionArgs: Array<out String>?): Cursor?{

        if(!selectionArgs.isNullOrEmpty()){
            notificationDao.markNotificationAsNormalNotificationAndDeletedFromTray(selectionArgs)
        }
        return null
    }

    private fun initFailures():Boolean{
        var initFailure = false

        if(CommonUtils.getApplication() == null){
            initFailure = true
        }
        try{
            if(notificationDao == null){
                initFailure = true
            }
        }catch(ex: java.lang.Exception){
            initFailure = true
        }

        return initFailure
    }

    companion object {
        val NOTIFICATION_URI: Uri = Uri.parse("content://${AppConfig.getInstance().packageName}.notificationProvider/notification_info")
        const val QUERY_FILTER_READ_SEEN_AND_BLOCKED_NOTIFICATIONS = "filter_read_seen_and_blocked_notifications"
        const val QUERY_GET_NEWS_STICKY_ITEMS = "get_news_sticky_items"
        const val QUERY_GET_EXISTING_NEWS_STICKY_ITEMS_FOR_IDS = "get_existing_news_sticky_items_for_ids"
        const val QUERY_UPDATE_NEWS_STICKY_ENTRY = "update_news_sticky_db_entry"
        const val NOTIFICATION_BASE_MODEL = "notification_base_model"
        const val NOTIFICATION_BASE_MODEL_TYPE = "notification_base_model_type"
        const val NOTIFICATION_DETAILS_STRING = "notification_details_string"
        const val QUERY_UPDATE_NOTIFICATION_STATE_AS_SKIPPED = "update_notification_state_as_skipped_by_user"
        const val QUERY_GET_GROUPED_NOTIFICATIONS = "notification_get_grouped_notifications"
        const val QUERY_ADD_STICKY_NOTIFICATIONS_TO_INBOX_ONLY = "add_sticky_notifications_to_inbox"
        private val notificationDao by lazy {
            NotificationDB.instance().getNotificationDao()
        }
    }
}