/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.view.service

import android.app.Notification
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.HandlerThread
import com.bumptech.glide.request.transition.Transition
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.dataentity.notification.DeeplinkModel
import com.newshunt.dataentity.notification.NewsNavModel
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism
import com.newshunt.dataentity.notification.asset.DataStreamResponse
import com.newshunt.dataentity.notification.asset.NewsStickyDataStreamAsset
import com.newshunt.dataentity.notification.asset.NewsStickyNotificationAsset
import com.newshunt.dataentity.notification.asset.OptInEntity
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.helper.API_ERROR
import com.newshunt.notification.helper.BLOCKED_OR_READ
import com.newshunt.notification.helper.EMPTY_RESPONSE
import com.newshunt.notification.helper.NotificationActionAnalytics
import com.newshunt.notification.helper.NotificationHandler
import com.newshunt.notification.helper.NotificationUtils
import com.newshunt.notification.helper.fireNotificationStartedBroadCast
import com.newshunt.notification.helper.logStickyNotificationActionEvent
import com.newshunt.notification.helper.logStickyStickyDeliveredEvent
import com.newshunt.notification.helper.logStickyUndisplayedEvent
import com.newshunt.notification.helper.rescheduleStickyForNextRefresh
import com.newshunt.notification.model.internal.dao.NotificationContentProviderHelper
import com.newshunt.notification.sqlite.NotificationProvider
import com.newshunt.notification.view.receiver.LOG_TAG
import com.newshunt.notification.view.view.NewsStickyNotificationView
import com.newshunt.notification.view.view.StickyNotificationView
import com.newshunt.sdk.network.image.Image
import java.io.Serializable
import java.util.*

/**
 * News Sticky foreground service implementation
 *
 * Created by srikanth.r on 10/13/21.
 */
class NewsStickyService: StickyNotificationService() {
    private var maxItemLimitInNewsSticky = 5;
    private var mutableItemList: MutableList<BaseModel>? = null
    private var mutableItemBitmapList: MutableList<Bitmap?>? = null
    private val tempCopyOfItem: MutableList<BaseModel> = mutableListOf()
    private val tempCopyOfBitmaps: MutableList<Bitmap?> = mutableListOf()
    private var imageDownloadingThread: HandlerThread? = null
    private var imageDownloadingThreadHandler: Handler? = null
    private val NEWS_STICKY_EXECUTOR = AndroidUtils.newSingleThreadExecutor("TAG_${System.currentTimeMillis()}")
    companion object{
        val TAG = "NewsStickyService"
    }

    override fun inflateNotificationView(): StickyNotificationView {
        Logger.d(TAG, "infalteView called")
        val view: NewsStickyNotificationView = NewsStickyNotificationView(stickyNavModel, refresher, this)
        Logger.d(TAG, "Build notification called from inflateNotificationView for list of size ${mutableItemList?.size ?: 0}")
        view.buildNotification(null, null, false, false, null, null, null, false)
        try{
            NEWS_STICKY_EXECUTOR.execute(getDbFetchAndNotificationUpdateRunnable(null, null))
        }catch (ex: Exception){
            Logger.d(TAG, ex.message.toString())
        }
        return view
    }

    override fun onCreate() {
        Logger.d(TAG, "on Create called")
        super.onCreate()
        buildDummyNotification(NotificationConstants.STICKY_NEWS_TYPE.hashCode())
        maxItemLimitInNewsSticky = PreferenceManager.getPreference(AppStatePreference.MAXIMUM_NUMBER_OF_ITEMS_TO_BE_SHOWN_IN_NEWS_STICKY, 5);
    }

    fun getDbFetchAndNotificationUpdateRunnable(channelId: String?, priority: Int?): Runnable{
        Logger.d(TAG, "Fetching list from")
        val runnable = object : Runnable {
            override fun run() {
                Logger.d(TAG, "Running runnable")
                doDbFetchAndNotificationUpdate(channelId, priority)
            }

        }

        return runnable
    }

    fun doDbFetchAndNotificationUpdate(channelId: String?, priority: Int?){
        val dbItems = fetchNotificationsFromDb()

        if(dbItems != null){
            mutableItemList?.clear()
            mutableItemBitmapList?.clear()
            mutableItemList = Collections.synchronizedList(mutableListOf<BaseModel>())
            mutableItemBitmapList = Collections.synchronizedList(mutableListOf<Bitmap?>())
            Logger.d(TAG, "list of items fetched from db is ${dbItems.size}")
            for(item in dbItems){
                //For all items put in sticky log an event
                logStickyStickyDeliveredEvent(null, stickyNavModel, NotificationDeliveryMechanism.PULL, (item as? NewsNavModel))
                mutableItemList?.add(item)
                mutableItemBitmapList?.add(null)
            }
        }
        Logger.d(TAG, "Size of items to be shown is:- ${mutableItemList?.size ?: 0}")
        if (stickyNotificationView != null){
            if (finishServiceIfNoItems()) {
                return
            } else{
                Logger.d(TAG, "Build notification called from fetchImage list of size ${mutableItemList?.size ?: 0}")
                (stickyNotificationView as? NewsStickyNotificationView)?.buildNotification(mutableItemList?.toList(), mutableItemBitmapList?.toList(), true, true, channelId, priority, null, false)
            }
        }

        fetchImage(true)
    }

    fun fetchImage(newStart: Boolean = false) {
        if (newStart) {
            imageDownloadingThread?.quit()
            imageDownloadingThreadHandler = null
            Logger.d(TAG, "Initiating a new imageDownloaderThread")
            imageDownloadingThread = object : HandlerThread(TAG + System.currentTimeMillis()) {
                override fun onLooperPrepared() {
                    Logger.d(TAG, "Image downloader looper prepared")
                    imageDownloadingThreadHandler = Handler(looper)
                    imageDownloadingThreadHandler?.post(object : Runnable {
                        override fun run() {
                            clearTempList()
                            fetchImage(false)
                        }

                    })
                }
            }
            imageDownloadingThread?.start()
        } else {
            try{
                imageDownloadingThreadHandler?.post(object : Runnable {
                    override fun run() {
                        if (!tempCopyOfItem.isNullOrEmpty()) {
                            val item = tempCopyOfItem.removeAt(0)
                            var imageUrl = item?.baseInfo?.inboxImageLink
                            if (CommonUtils.isEmpty(imageUrl)) {
                                imageUrl = item?.baseInfo?.imageLinkV2
                            }
                            if (CommonUtils.isEmpty(imageUrl)) {
                                imageUrl = item?.baseInfo?.imageLink
                            }
                            if (!CommonUtils.isEmpty(imageUrl)) {
                                Image.load(imageUrl, true)
                                        .into(object : Image.ImageTarget() {
                                            override fun onResourceReady(resource: Any, transition: Transition<*>?) {
                                                Logger.d(TAG, "Image download successful for imageLink $imageUrl")
                                                super.onResourceReady(resource, transition)
                                                val bitmap = resource as? Bitmap
                                                if (bitmap != null) {
                                                    tempCopyOfBitmaps.add(bitmap)
                                                } else {
                                                    tempCopyOfBitmaps.add(null)
                                                }
                                                fetchImage(false)
                                            }

                                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                                super.onLoadFailed(errorDrawable)
                                                Logger.d(TAG, "Image download failed for imageLink $imageUrl")
                                                tempCopyOfBitmaps.add(null)
                                                fetchImage(false)
                                            }
                                        })
                            } else {
                                tempCopyOfBitmaps.add(null)
                                fetchImage(false)
                            }
                        } else {
                            if (mutableItemList?.size ?: 0 == tempCopyOfBitmaps?.size ?: 0) {
                                tempCopyOfBitmaps?.let {
                                    mutableItemBitmapList?.clear()
                                    mutableItemBitmapList?.addAll(it)
                                }
                            }
                            Logger.d(TAG, "Image download finished, Build notification called from fetchImage length of bitmap list is ${mutableItemBitmapList?.size ?: 0} and itemList of size ${mutableItemList?.size ?: 0}")
                            (stickyNotificationView as? NewsStickyNotificationView)?.buildNotification(mutableItemList?.toList(), mutableItemBitmapList?.toList(), true, false, null, null, -1, true)

                        }
                    }
                })
            }catch (ex: java.lang.Exception){
                Logger.caughtException(ex)
            }
        }
    }

    fun clearTempList(){
        tempCopyOfBitmaps?.clear()
        tempCopyOfItem?.clear()
        mutableItemList?.let {
            tempCopyOfItem.addAll(it)
        }
    }

    public fun fetchNotificationsFromDb(): List<BaseModel>? {
        contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                null,
                NotificationProvider.QUERY_GET_NEWS_STICKY_ITEMS,
                arrayOf(maxItemLimitInNewsSticky.toString()), null)?.let { cursor ->
            NotificationContentProviderHelper.baseModelsFromCursor(cursor)?.let { items ->
                cursor.close()
                return items
            }
            return null
        }
        return null
    }

    fun fetchListOfAlreadyExistingStickyItemsFor(items: List<BaseModel>?): List<BaseModel>?{

        try{
            val listOfIds = mutableListOf<String>()
            items?.let{
                for(item in it){
                    if(item.baseInfo != null && item.baseInfo.uniqueId != null)
                        listOfIds.add(item.baseInfo.uniqueId.toString())
                }
                if(listOfIds.size > 0){
                    contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                        null,
                        NotificationProvider.QUERY_GET_EXISTING_NEWS_STICKY_ITEMS_FOR_IDS,
                        listOfIds.toTypedArray(), null)?.let { cursor ->
                        NotificationContentProviderHelper.baseModelsFromCursor(cursor)?.let { items ->
                            cursor.close()
                            return items
                        }
                    }
                }
            }
        }catch(exception: Throwable){
            Logger.caughtException(exception)
        }
        return null
    }

    fun handleStreamResponse(dataStreamResponse: DataStreamResponse){
        Logger.d(TAG, "handle stream response enter")

        stickyNavModel.getBaseNotificationAsset()?.let{
            if( it.expiryTime < System.currentTimeMillis()){
                Logger.d(TAG, "News sticky expired, stop service")
                stopStickyService(true, false)
                return
            }
        }

        val streamAsset = dataStreamResponse.baseStreamAsset as? NewsStickyDataStreamAsset

        try{
            NEWS_STICKY_EXECUTOR.execute(object : Runnable {
                override fun run() {
                    //Update db with values from response
                    streamAsset?.let {
                        stickyNavModel.setBaseStreamAsset(it)
                        PreferenceManager.savePreference(CommonUtils.getApplication(), AppStatePreference.LAST_KNOWN_DISABLE_LOGGING_STATUS_NEWS_STICKY, it.disableEvents, this@NewsStickyService)
                        val expiryTime = it.expiryTime
                        if (expiryTime > 0) {
                            stickyNavModel.getBaseNotificationAsset()?.expiryTime = expiryTime
                            if (expiryTime < System.currentTimeMillis()) {
                                Logger.d(TAG, "News sticky streamAsset expired, stop service")
                                stopStickyService(true, false)
                                return
                            }
                        }
                        if (it.refreshInterval > 0) {
                            PreferenceManager.savePreference(CommonUtils.getApplication(), AppStatePreference.NEWS_STICKY_AUTO_REFRESH_INTERVAL, it.refreshInterval.toInt(), this@NewsStickyService)
                        }
                        val optInEntity = JsonUtils.toJson(OptInEntity(NotificationConstants.NEWS_STICKY_OPTIN_ID, it.url,
                                NotificationConstants.STICKY_NEWS_TYPE, it.priority, 0, expiryTime,
                                Constants.EMPTY_STRING, null, Constants.EMPTY_STRING, it.channelId, it.forceShow))
                        optInEntity?.let { entityStr ->
                            contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                                    null, NotificationProvider.QUERY_UPDATE_NEWS_STICKY_ENTRY,
                                    arrayOf<String>(entityStr), null)?.let { cursor -> cursor.close() }
                        }
                    }

                    val items = streamAsset?.stickyItems
                    if (items != null && items.isNotEmpty()) {
                        if (insertNotificationsIntoDb(items)) {
                            //handler?.post(getDbFetchAndNotificationUpdateRunnable(streamAsset?.channelId, streamAsset?.priority))
                            doDbFetchAndNotificationUpdate(streamAsset?.channelId, streamAsset?.priority)
                        } else {
                            finishServiceIfNoItems()
                        }
                    } else {
                        logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, reason = EMPTY_RESPONSE)
                    }
                }
            })
        }catch (ex: Exception){
            Logger.caughtException(ex)
        }
    }

    fun handleStreamError(dataStreamResponse: DataStreamResponse?) {
        if (finishServiceIfNoItems()) {
            if(dataStreamResponse?.error?:null != null){
                logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, error = dataStreamResponse?.error)
            }else{
                logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, reason = API_ERROR)
            }
            Logger.d(TAG, "handleStreamError, stopping service")
        }
    }

    private fun insertNotificationsIntoDb(items: List<DeeplinkModel>): Boolean{
        if(items != null){
            val parsedAndFilteredList = mutableListOf<BaseModel>()
            val existingList = mutableListOf<BaseModel>()
            fetchNotificationsFromDb()?.let { items -> existingList.addAll(items) }//In Ascending timestamp order
            val parsedList = mutableListOf<BaseModel>()
            for(item in items) {
                try {
                    val newsModel = NotificationHandler.handleDeepLinkNotifications(NotificationDeliveryMechanism.PULL, null, item) as? NewsNavModel;
                    newsModel?.let {
                        parsedList.add(it)
                    }
                } catch (ex: Exception) {
                    Logger.caughtException(ex)
                }
            }

            if(parsedList.size > 0){
                filterReadSeenAndBlockedNotifications(parsedList)?.let { filteredList ->
                    parsedAndFilteredList.addAll(filteredList)
                }
            }else{
                logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, reason = API_ERROR)
                return false
            }

            val listOfRepeatingItems = fetchListOfAlreadyExistingStickyItemsFor(parsedAndFilteredList)
            if(parsedAndFilteredList.size == 0){
                logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, reason = BLOCKED_OR_READ)
                return false
            }else if(parsedAndFilteredList.size >= maxItemLimitInNewsSticky){
                // delete existing list
                markItemsAsNonStickyAndAddToInbox(existingList, 0, existingList?.size ?: 0)
                //inserting parsed limit entries from 0 till maxItemLimit
                insertNotificationIntoDb(parsedAndFilteredList)
                return true
            }else {

                listOfRepeatingItems?.let{
                    for(item in it){
                        existingList.remove(item)
                    }
                }
                if (!existingList.isNullOrEmpty()) {
                    var fromInd = existingList.size - (maxItemLimitInNewsSticky - parsedAndFilteredList.size)
                    if (fromInd < 0) {
                        fromInd = 0
                    } else {
                        fromInd = if(fromInd > existingList.size) existingList.size else fromInd
                        markItemsAsNonStickyAndAddToInbox(existingList, 0, fromInd)
                    }
                    for (i in (fromInd until existingList.size).reversed()) {
                        parsedAndFilteredList.add(0, existingList.get(i))
                    }
                }
                insertNotificationIntoDb(parsedAndFilteredList)
                return true
            }
        }else{
            logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, reason = EMPTY_RESPONSE)
            return false
        }
    }

    fun markItemsAsNonStickyAndAddToInbox(list: List<BaseModel>?, startInd: Int, toIndex: Int){
        list?.let{ items ->
            val end = if(toIndex > items.size)items.size else toIndex
            val toMarkAndInsertItems = mutableListOf<String>()
            for(i in startInd until end){
                val item = list.get(i)
                if(item.baseInfo != null && item.baseInfo.uniqueId != null){
                    toMarkAndInsertItems.add(item.baseInfo.uniqueId.toString())
                }
            }
            if(toMarkAndInsertItems.size > 0){
                contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                    null, NotificationProvider.QUERY_ADD_STICKY_NOTIFICATIONS_TO_INBOX_ONLY,
                    toMarkAndInsertItems.toTypedArray(), null)?.let { cursor -> cursor.close() }
            }
        }
    }

    fun insertNotificationIntoDb(items: List<BaseModel>){
        var toInd = maxItemLimitInNewsSticky
        if(maxItemLimitInNewsSticky > items.size){
            toInd = items.size
        }
        for(i in 0 until toInd){
            val item = items.get(i)
            //Remove any notification with this id from tray
            item.baseInfo?.uniqueId?.let{
                NotificationUtils.removeNotificationFromTray(it)
            }
            //mark notification event logging equivalent to news_sticky event logging
            stickyNavModel?.let{
                var isLoggingNotificationEventsDisabled = false
                (stickyNavModel.getBaseNotificationAsset() as? NewsStickyNotificationAsset)?.let{ newsStickyNotificationAsset ->
                    isLoggingNotificationEventsDisabled = newsStickyNotificationAsset.disableEvents
                }

                (stickyNavModel.getBaseStreamAsset() as? NewsStickyDataStreamAsset)?.let{ newsStickyDataStreamAsset ->
                    isLoggingNotificationEventsDisabled = (isLoggingNotificationEventsDisabled || newsStickyDataStreamAsset.disableEvents)
                }
                item.setDisableEvents(isLoggingNotificationEventsDisabled)
            }
            val contentValues = ContentValues()
            contentValues.put(NotificationProvider.NOTIFICATION_DETAILS_STRING, JsonUtils.toJson(ContainerClassForJsonCreation(BaseModelType.convertModelToString(item) ?: "", item.baseModelType.name)))
            contentResolver.insert(NotificationProvider.NOTIFICATION_URI, contentValues)
        }
        if(items.size > 0){
            updateGroupedNotificationInTrayPostInsert()
        }

    }

    fun filterReadSeenAndBlockedNotifications(items: List<BaseModel>): List<BaseModel>?{
        val filteredList = mutableListOf<BaseModel>()
        val selectionArgs = mutableListOf<String>()
        for(item in items){
            if(item.baseModelType != null && item.baseModelType.name != null){
                selectionArgs.add(JsonUtils.toJson(ContainerClassForJsonCreation(BaseModelType.convertModelToString(item) ?: "", item.baseModelType.name)))
            }
        }

        (contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                null, NotificationProvider.QUERY_FILTER_READ_SEEN_AND_BLOCKED_NOTIFICATIONS,
                selectionArgs.toTypedArray(),
                null))?.let { cursor ->
            NotificationContentProviderHelper.baseModelsFromCursor(cursor)?.let { list->
                filteredList.addAll(list)
                cursor.close()
            }
        }
        if(filteredList.size == 0){
            return null
        }else{
            return filteredList
        }
    }

    fun handlePrevNextClick(i: Int, gotToNext: Boolean, id: String?){
        NEWS_STICKY_EXECUTOR.execute(Runnable {
            if(i < 0){
                Logger.d(TAG, "Wrong index for next_prev click:- ${i}")
                return@Runnable
            }
            try{
                id?.let {
                    contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                            null, NotificationProvider.QUERY_UPDATE_NOTIFICATION_STATE_AS_SKIPPED,
                            arrayOf(it),
                            null)?.close()
                }
                val correctionVal = reorderNotificationsShownBasedOn(i, gotToNext)
                Logger.d(TAG, "Build notification called from handlePrevNext for list of size ${mutableItemList?.size ?: 0}")
                (stickyNotificationView as? NewsStickyNotificationView)?.buildNotification(mutableItemList?.toList(), mutableItemBitmapList?.toList(), true, false, null, null, correctionVal, true)
            }catch (ex: Exception){
                Logger.caughtException(ex)
            }
        })
    }

    fun onCancelNewsSticky(initiatedFromSettings: Boolean, initiatedFromInbox: Boolean) {
        if(initiatedFromSettings){
            Logger.d(LOG_TAG, "Sticky closed due to button toggle on Settings Screen")
        }else if(initiatedFromInbox){
            Logger.d(TAG, "rescheduleStickyForNextRefresh called while coming from inbox")
            rescheduleStickyForNextRefresh(stickyNavModel)
        }
    }

    fun reorderNotificationsShownBasedOn(i: Int, gotToNext: Boolean): Int {
        if(mutableItemList?.size?:0 > i){
            val size = (mutableItemList?.size?:maxItemLimitInNewsSticky)
            var itemIndexToGoToStartingSpot = 0;
            if(gotToNext){
                itemIndexToGoToStartingSpot = (i+1)%size

            }else{
                itemIndexToGoToStartingSpot = (i - 1 + size)% size
            }

            if(itemIndexToGoToStartingSpot != 0 && (mutableItemList?.size?:0 > 0)){

                for(i in 0 until itemIndexToGoToStartingSpot){
                    if(mutableItemList?.size?:0 == mutableItemBitmapList?.size?:0){
                        mutableItemBitmapList?.add(mutableItemBitmapList?.removeAt(0)?:null)
                    }
                    val entryToAddAtEnd = mutableItemList?.removeAt(0)?:null
                    entryToAddAtEnd?.let { it -> mutableItemList?.add(it)}
                }
            }

            return itemIndexToGoToStartingSpot

        }
        return 0
    }

    fun handleItemClicked(i: Int) {
        NEWS_STICKY_EXECUTOR.execute {
            if (mutableItemList?.size ?: 0 > i) {
                mutableItemList?.removeAt(i)
                if (mutableItemBitmapList?.size ?: 0 > i) {
                    mutableItemBitmapList?.removeAt(i)
                }
                if (!mutableItemList.isNullOrEmpty()) {
                    Logger.d(TAG, "Build notification called from handleItemClicked for list of size ${mutableItemList?.size ?: 0}")
                    (stickyNotificationView as? NewsStickyNotificationView)?.buildNotification(mutableItemList?.toList(),
                            mutableItemBitmapList?.toList(),
                            true,
                            false,
                            null,
                            null,
                            -1, true)
                } else {
                    //User has run out of items in the sticky, lets refresh!
                    Logger.d(TAG, "Running out of items, call force refresh!")
                    setupRefresher(true)
                }
            }
        }

    }

    fun onNotificationClearAll() {
        Logger.d(TAG, "onNotificationClearAll, reschedule and stop")
        logStickyNotificationActionEvent(stickyNavModel, NotificationActionAnalytics.CLEAR_ALL, System.currentTimeMillis(), NhAnalyticsEventSection.NOTIFICATION)
        rescheduleStickyForNextRefresh(stickyNavModel)
        stopStickyService(false, true)
    }

    fun onNormalNotificationReceived(id: Int){
        NEWS_STICKY_EXECUTOR.execute {
            if (id != NotificationConstants.NOTIFICATION_TRAY_ID_TO_OPEN_INBOX) {
                var itemToBeRemovedAt: Int? = null
                mutableItemList?.let {
                    for (index in 0 until it.size) {
                        if (it.get(index).baseInfo.uniqueId == id) {
                            itemToBeRemovedAt = index
                            break;
                        }
                    }
                }

                itemToBeRemovedAt?.let {
                    mutableItemList?.removeAt(it)
                    if (mutableItemBitmapList?.size ?: 0 > it) {
                        mutableItemBitmapList?.removeAt(it)
                    }
                    if (mutableItemList.isNullOrEmpty()) {
                        Logger.d(TAG, "Running out of items, call force refresh!")
                        setupRefresher(true)
                    } else {
                        (stickyNotificationView as? NewsStickyNotificationView)?.buildNotification(mutableItemList?.toList(),
                                mutableItemBitmapList?.toList(),
                                true,
                                false,
                                null,
                                null,
                                -1, true)
                    }
                }
            } else {
                //For grouped case update on tray
                try {
                    val listOfIds = mutableListOf<String>()
                    mutableItemList?.let {
                        for (item in it) {
                            if (item.baseInfo != null && item.baseInfo.uniqueId != null)
                                listOfIds.add(item.baseInfo.uniqueId.toString())
                        }
                    }
                    if (listOfIds.isNotEmpty()) {
                        contentResolver.query(NotificationProvider.NOTIFICATION_URI,
                                null,
                                NotificationProvider.QUERY_GET_GROUPED_NOTIFICATIONS,
                                listOfIds.toTypedArray(), null)?.let { cursor ->
                            NotificationContentProviderHelper.baseModelsFromCursor(cursor)?.let { items ->
                                items.forEach { item ->
                                    onNormalNotificationReceived(item.baseInfo.uniqueId)
                                }
                                cursor.close()
                            }
                        }
                    }
                } catch (ex: Exception) {
                    Logger.caughtException(ex)
                }
            }
        }
    }

    override fun addNotificationToTray(notificationId: Int, notification: Notification?, isUpdate: Boolean) {
        try{
            if(isUpdate){
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notification)
            }else{
                fireNotificationStartedBroadCast(this, stickyNavModel)
                startForeground(notificationId, notification!!)
            }
        }catch (ex: Exception){
            Logger.caughtException(ex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mutableItemList?.clear()
        mutableItemBitmapList?.clear()
        tempCopyOfBitmaps.clear()
        tempCopyOfItem.clear()
        imageDownloadingThread?.quit()
        imageDownloadingThreadHandler = null
        NEWS_STICKY_EXECUTOR.shutdown()
    }

    private fun updateGroupedNotificationInTrayPostInsert(){
        try{
            val intent = Intent(CommonUtils.getApplication(), NewsStickyHelperService::class.java)
            intent.action = NotificationConstants.INTENT_ACTION_UPDATE_NOTIFICATION_TRAY
            startService(intent)
        }catch(ex : Exception){
            Logger.caughtException(ex)
        }
    }

    private fun finishServiceIfNoItems(): Boolean {
        if (mutableItemList.isNullOrEmpty()) {
            Logger.d(TAG, "Stopping the service since we have no more items, retry after: ${stickyNavModel.getBaseNotificationAsset()?.autoRefreshInterval} secs")
            rescheduleStickyForNextRefresh(stickyNavModel)
            stopStickyService(false, true)
            return true
        }
        return false
    }
}

data class ContainerClassForJsonCreation(val notification_base_model: String, val notification_base_model_type: String): Serializable
