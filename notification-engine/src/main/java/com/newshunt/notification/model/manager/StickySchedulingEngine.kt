/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.manager

import android.content.Intent
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.*
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.NewsStickyNotificationAsset
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.isValidUrl
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.notification.analytics.devEvent.NotificationDevEvent
import com.newshunt.notification.analytics.devEvent.NotificationDevEventParam
import com.newshunt.notification.analytics.devEvent.NotificationDevEventType
import com.newshunt.notification.analytics.devEvent.fireNotificationDevEvent
import com.newshunt.notification.helper.*
import com.newshunt.notification.model.internal.dao.StickyNotificationEntity
import com.newshunt.notification.model.internal.dao.StickyNotificationStatus
import com.newshunt.notification.model.internal.dao.StickyNotificationsDBInstance
import com.newshunt.notification.model.internal.dao.StickyOptState
import com.newshunt.notification.model.internal.service.DataAssetServiceImpl
import com.newshunt.notification.sqlite.NotificationDB
import io.reactivex.Scheduler

/**
 * All business logic of scheduling the Sticky service. Each sticky type will have its own instance
 * of this class
 *
 * Created by srikanth.r on 10/25/21.
 */
class StickySchedulingEngine(private val stickyType: String,
                             private val scheduler: Scheduler) {
    private val dataAssetService = DataAssetServiceImpl()
    private val LOG_TAG = "StickyEngine_$stickyType"

    /**
     * Function to check onGoing(run again If not running due to abrupt exit case) and schedule
     * next possible notification
     *
     * Step 1: Check if onGoing notification is OPTED out by user or expired
     *
     *      case a : If Expired, remove from DB
     *      case b : If Opted out, mark the status as OPTED out in DB and mark job status as
     *      UNSCHEDULED
     *
     *      else {
     *        if (service already running) just the restart the service
     *        else redownload the meta and start the service again
     *      }
     *
     * Step 2: Schedule next
     */
    fun checkOnGoingAndScheduleNotification(trigger: Trigger, stickNotificationEntity: StickyNotificationEntity? = null) {
        Logger.d(LOG_TAG, "CheckOnGoingAndScheduleNotification - Enter $trigger")
        if (trigger == Trigger.APP_START && stickyType == NotificationConstants.STICKY_NEWS_TYPE) {
            checkAndClearDND()
            NewsStickyPushScheduler.onAppStart()
            return
        }

        if(StickyNotificationServiceUtils.isStickyDisabled(stickyType)){
            Logger.d(LOG_TAG, "Sticky of type- ${stickyType} was disabled, so returning");
            return
        }
        //Add type to this query
        val onGoingNotifications = StickyNotificationsDBInstance.stickyNotificationDao()
            .getNotificationsByStatusIncludingAllOptState(StickyNotificationStatus.ONGOING, stickyType)

        var onGoingNotification = if (!CommonUtils.isEmpty(onGoingNotifications))onGoingNotifications?.get(0)
        else null

        stickNotificationEntity?.let {
            //if stickyNotificationEntity Param is not null handle the below way
            if(it.type == stickyType && handleNonNullStickyNotificationPassedAsParam(it)){
                return
            }
        }
        //Step 1
        if (checkAndHandleIfOptedOutOrExpired(onGoingNotification, trigger)) {
            Logger.d(LOG_TAG, "OnGoingNotification : $onGoingNotification is expired or opted out")
            //Step 2
            scheduleNext(null)
        } else {
            onGoingNotification?.let {
                if (!AndroidUtils.isServiceRunning(
                        CommonUtils.getApplication(),
                        StickyNotificationServiceFactory.getServiceClass(it.type))) {
                    Logger.d(
                        LOG_TAG, "Sticky notification service is not running as expected, may " +
                            "be due to some crash or unexpected behaviour in sticky process, so " +
                            "we will download the meta again and start the service..")
                    startStickyService(it, it.isLiveOptIn ?: false)
                    Logger.d(LOG_TAG, "Download of meta, will take calling scheduleNext(), so returning")
                    return
                } else {
                    Logger.d(LOG_TAG, "Sticky notification service is running, so just going to restart the service")
                    stickyNavModelFromStickyNotificiationEntity<BaseNotificationAsset, BaseDataStreamAsset>(it)?.let { stickyNavModel ->
                        Logger.d(LOG_TAG, "Starting the service again for $onGoingNotification for abrupt exit case")
                        StickyNotificationServiceUtils.startStickyNotificationService(stickyNavModel)
                    }
                }
            }
            //Step 2
            scheduleNext(onGoingNotification)
        }
        Logger.d(LOG_TAG, "CheckOnGoingAndScheduleNotification - Exit")
    }

    private fun handleNonNullStickyNotificationPassedAsParam(stickyNotificationEntity: StickyNotificationEntity): Boolean{
        return when(stickyNotificationEntity.type){
            NotificationConstants.STICKY_NEWS_TYPE -> {
                StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(stickyNotificationEntity)
                startStickyService(stickyNotificationEntity, stickyNotificationEntity.isLiveOptIn ?: false)
                true
            }
            else -> {
                false
            }
        }
    }

    /**
     *  Function looks big, but better to stay in single function to understand the protocol below
     *
     * Schedule Next Protocol
     *
     * Step 1: Check if onGoing notification is LIVE, if yes, no point in scheduling next and return
     *
     * Step 2: Get notifications which has startTime < onGoing.startTime and priority >= onGoing
     * .Priority, if nothing onGoing, then all fetch all notifications
     *
     * Step 3: Pick up the first notification from above the list , that will be
     * toBeScheduledNotification
     *
     * Step 4: Read from DB already scheduled notification
     *
     * Step 5: Check if already scheduled notification has startTime < toBeScheduledNotification
     * .startTime, if yes, no point in scheduling, so return
     *
     * Step 6: Cancel already scheduled notification and mark it in DB as UNSCHEDULED,
     *
     * Step 7: Schedule the job for toBeScheduledNotification, and mark it in DB as SCHEDULED
     */
    fun scheduleNext(onGoingNotification: StickyNotificationEntity?) {
        Logger.d(LOG_TAG, "Schedule Next - Enter")
        //Step 1
        if (isUserOptedLiveOnGoingNotification(onGoingNotification)) {
            Logger.d(LOG_TAG, "But ongoing notification $onGoingNotification is live, no point in scheduling other notifications")
            return
        }

        //Step 2
        val matchingNotifications = StickyNotificationsDBInstance.stickyNotificationDao()
            .getMatchingUnscheduledNotifications(onGoingNotification?.expiryTime ?: Long
                .MAX_VALUE, onGoingNotification?.priority ?: Int.MIN_VALUE, type = stickyType) ?: return

        Logger.d(LOG_TAG, "Matching notifications as per query logic: $matchingNotifications")

        //Step 3
        val toBeScheduledNotification =
            pickFirstBestMatchingUnscheduledNotifications(matchingNotifications)

        Logger.d(LOG_TAG, "Picked up this $toBeScheduledNotification as notification to be scheduled")

        //Step 4
        val scheduledNotifications: List<StickyNotificationEntity>? =
            StickyNotificationsDBInstance.stickyNotificationDao().getNotificationsByStatus(StickyNotificationStatus.SCHEDULED, stickyType)

        val scheduledNotification = if (!CommonUtils.isEmpty(scheduledNotifications))
            scheduledNotifications?.get(0) else null

        scheduledNotification?.let {
            Logger.d(LOG_TAG, "Already scheduled notification is $it")
            if (toBeScheduledNotification == null) {
                rePostAlreadyScheduledNotification(it)
                return
            }
        }

        toBeScheduledNotification ?: return

        val alreadyScheduledStartTime = scheduledNotification?.startTime ?: Long.MAX_VALUE
        val toBeScheduledStartTime = toBeScheduledNotification.startTime ?: return
        //Step 5
        if (alreadyScheduledStartTime < toBeScheduledStartTime) {
            Logger.d(
                LOG_TAG, " Already scheduled notification has startTime < " +
                    "toBeScheduledNotification.startTime, so no point in scheduling another ")
            rePostAlreadyScheduledNotification(scheduledNotification)
            return
        }

        //Step 6
        scheduledNotification?.let {
            StickyNotificationJobScheduler.cancelJobSchedulerForStickyNotification(it)
            Logger.d(LOG_TAG, "Cancelled the schedule job for already scheduled")
            StickyNotificationsDBInstance.stickyNotificationDao()
                .markNotificationStatus(it.id, it.type, StickyNotificationStatus.UNSCHEDULED)
            Logger.d(LOG_TAG, "Marked the already scheduled one as UNSCHEDULED")
        }

        //Step 7
        if (toBeScheduledNotification.startTime <= System.currentTimeMillis()) {
            Logger.d(LOG_TAG, "start time of to be scheduled notification is already less than " +
                    "current time, so directly starting service, instead of scheduling and " +
                    "completing the function scheduleNext()..")
            StickyNotificationsDBInstance.stickyNotificationDao()
                .markNotificationStatus(toBeScheduledNotification.id, toBeScheduledNotification.type, StickyNotificationStatus.SCHEDULED)
            StickyNotificationsManager.startStickyService(
                toBeScheduledNotification.id,
                toBeScheduledNotification.type)
        } else {
            if (StickyNotificationJobScheduler
                    .scheduleNextStickyNotificationJobByJobScheduler(toBeScheduledNotification)) {
                StickyNotificationsDBInstance.stickyNotificationDao()
                    .markNotificationStatus(toBeScheduledNotification.id,
                        toBeScheduledNotification.type, StickyNotificationStatus.SCHEDULED)
                Logger.d(LOG_TAG, "Scheduled job for $toBeScheduledNotification and marked its status as SCHEDULED in DB")
            }
        }
        Logger.d(LOG_TAG, "Schedule Next - Exit")
    }

    private fun checkAndHandleIfOptedOutOrExpired(onGoingNotification: StickyNotificationEntity?,
                                                  trigger: Trigger) : Boolean {
        onGoingNotification ?: return false
        if (isStickyNotificationScheduledEarlier(onGoingNotification)) {
            Logger.d(LOG_TAG, "Somehow, ongoing notification looks scheduled earlier, due to time " +
                    "settings preponed, so going to mark the status as UNSCHEDULED in DB ")
            StickyNotificationsDBInstance.stickyNotificationDao()
                .markNotificationStatus(onGoingNotification.id, onGoingNotification.type, StickyNotificationStatus.UNSCHEDULED)
            cancelOngoingNotificationIfAny(trigger)
            return true
        } else if (isStickNotificationExpired(onGoingNotification)) {
            Logger.d(LOG_TAG, "onGoing notification has expired, so deleting from DB")
            StickyNotificationsDBInstance.stickyNotificationDao()
                .deleteNotification(onGoingNotification.id, onGoingNotification.type)
            return true
        } else if (onGoingNotification.optState == StickyOptState.OPT_OUT) {
            Logger.d(LOG_TAG, "onGoing notification has been opted out")
            cancelOngoingNotificationIfAny(trigger)
            Logger.d(LOG_TAG, "Cancelled Sticky Service for opted out notification")
            StickyNotificationsDBInstance.stickyNotificationDao()
                .markNotificationStatus(onGoingNotification.id, onGoingNotification.type,
                    StickyNotificationStatus.UNSCHEDULED)
            Logger.d(LOG_TAG, "Marked in DB as UNSCHEDULED")
            return true
        }
        return false
    }

    /**
     * Function to startStickyService
     *
     * No matter if previous data is present or not, we hit meta url
     *
     * and till response is received, prev job
     * status SCHEDULED will be retained
     *    case a: on Success response:
     *       1. Mark the prev live notification as NON live, if new notification is LIVE
     *       2. call handleStartStickyService
     *    case b: on Error
     *       1. Dont start service and mark the status UNSCHEDULED and schedule next
     *
     */
    fun startStickyService(stickyNotificationEntity: StickyNotificationEntity?,
                                   isUserLiveOptIn: Boolean, audioInput: AudioInput? = null) {
        Logger.d(LOG_TAG, "StartStickyService(StickyNotificationEntity) - enter")

        if(StickyNotificationServiceUtils.isStickyDisabled(stickyType)){
            Logger.d(LOG_TAG, "Sticky of type- ${stickyType} was disabled, so returning");
            return;
        }

        if (stickyNotificationEntity == null || stickyNotificationEntity.jobStatus ==
            StickyNotificationStatus.UNSCHEDULED) {
            Logger.d(LOG_TAG, "But the job status of $stickyNotificationEntity is not SCHEDULED, so returning")
            checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
            return
        }

        if (isStickNotificationExpired(stickyNotificationEntity)) {
            handleExpiredNotification(stickyNotificationEntity)
            return
        }

        if (CommonUtils.isEmpty(stickyNotificationEntity.metaUrl) ||
            !stickyNotificationEntity.metaUrl.isValidUrl()) {
            Logger.d(LOG_TAG, "The Meta url is empty or not a valid url, so not point in keeping in DB and hence deleting it")
            StickyNotificationsDBInstance.stickyNotificationDao()
                .deleteNotification(stickyNotificationEntity.id,stickyNotificationEntity.type)
            Logger.d(
                LOG_TAG, "Deleted this $stickyNotificationEntity with empty meta url empty or " +
                    "invalid url")
            checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
            return
        }

        val savedPrefToSend = if(stickyNotificationEntity.type.equals(NotificationConstants.STICKY_NEWS_TYPE)) AppStatePreference.NEWS_STICKY_QUERY_PARAMS else null
        stickyNotificationEntity.metaUrl?.let {
            dataAssetService.getMetaData(it, stickyNotificationEntity.type, savedPrefToSend)
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .subscribe({ baseNotificationAsset ->
                    //Case a - on success
                    handleNotificationMetaResponse(stickyNotificationEntity.id,
                        stickyNotificationEntity.type,
                        baseNotificationAsset,
                        isUserLiveOptIn,
                        audioInput)
                }, { throwable ->
                    //Case b - on error
                    handleNotificationMetaResponseError(throwable,
                        stickyNotificationEntity, 
                        isUserLiveOptIn,
                        audioInput)
                })
        }
    }

    private fun rePostAlreadyScheduledNotification(alreadyScheduledNotification :
                                                   StickyNotificationEntity?) {
        alreadyScheduledNotification ?: return
        Logger.d(LOG_TAG, " Already scheduled notification has startTime < toBeScheduledNotification.startTime, so no point in scheduling another ")
        alreadyScheduledNotification.startTime?.let {
            if (it <= System.currentTimeMillis()) {
                Logger.d(
                    LOG_TAG, "Already scheduled time is past, cancelling job if any and " +
                        "starting service directly")
                StickyNotificationJobScheduler.cancelJobSchedulerForStickyNotification(alreadyScheduledNotification)
                StickyNotificationsManager.startStickyService(
                    alreadyScheduledNotification.id,
                    alreadyScheduledNotification.type
                )
            } else {
                Logger.d(
                    LOG_TAG, "But rescheduling job again so it will replace the " +
                        "exisiting job, in case of job scheduler is disturbed because of system " +
                        "time change")
                StickyNotificationJobScheduler.scheduleNextStickyNotificationJobByJobScheduler(alreadyScheduledNotification)
            }
        }
    }

    private fun handleNotificationMetaResponseError(throwable: Throwable,
                                                    stickyNotificationEntity:
                                                    StickyNotificationEntity,
                                                    isUserLiveOptIn: Boolean, audioInput: AudioInput?) {
        Logger.d(LOG_TAG, "HandleMetaResponse Error - Enter with error : ${throwable.message}")
        if(stickyNotificationEntity.type == NotificationConstants.STICKY_NEWS_TYPE){
            logStickyUndisplayedEvent(stickyNotificationEntity = stickyNotificationEntity, error = throwable)
        }


        val entityFromDB = StickyNotificationsDBInstance.stickyNotificationDao()
            .getNotificationByIdAndType(stickyNotificationEntity.id, stickyNotificationEntity.type)

        if (entityFromDB == null ||
            entityFromDB.jobStatus == StickyNotificationStatus.UNSCHEDULED) {
            Logger.d(
                LOG_TAG, "notification null or the job status of $entityFromDB is not " +
                    "SCHEDULED, so returning")
            checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
            return
        }

        if (entityFromDB == null || entityFromDB.metaUrlAttempts + 1 >= StickyNotificationsManager.MAX_META_RETRY_ATTEMPTS) {
            if (stickyNotificationEntity.data != null) {
                Logger.d(
                    LOG_TAG, "We have received max number of attempts to get response from meta " +
                        "url for $stickyNotificationEntity, so we will use the previously " +
                        "received data to start the notification again")
                startSuccessfulNotification(isUserLiveOptIn, stickyNotificationEntity, audioInput)
            } else {
                if (stickyType == NotificationConstants.STICKY_NEWS_TYPE) {
                    //For news sticky, retry after auto refresh interval
                    rescheduleStickyForNextRefresh(stickyNavModelFromStickyNotificiationEntity(stickyNotificationEntity))
                } else {
                    StickyNotificationsDBInstance.stickyNotificationDao()
                        .deleteNotification(stickyNotificationEntity.id, stickyNotificationEntity.type)
                    Logger.d(
                        LOG_TAG, "entityFromDB is null OR number of meta url attempts exceeded " +
                                "MAX_ATTEMPTS and so removed the notification from DB and going to schedule " +
                                "next possible notification ")
                    checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
                }
            }
        } else {
            val updatedEntity = entityFromDB.copy(metaUrlAttempts = entityFromDB.metaUrlAttempts + 1,
                jobStatus = StickyNotificationStatus.SCHEDULED)

            if (StickyNotificationJobScheduler.scheduleNextStickyNotificationJobByJobScheduler
                    (updatedEntity)) {
                StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(updatedEntity)
                Logger.d(
                    LOG_TAG, "Incremented the number of attempts by 1 and mark the status once " +
                        "again as SCHEDULED")
            }
        }
        Logger.d(LOG_TAG, "HandleMetaResponse Error - Exit")
    }

    fun cancelOngoingNotificationIfAny(trigger: Trigger) {
        Logger.d(LOG_TAG, "Sending Intent to stop any ongoing sticky notification")
        val cancelOngoingIntent = Intent()
        cancelOngoingIntent.`package` = CommonUtils.getApplication().packageName
        cancelOngoingIntent.action = NotificationConstants.INTENT_STICKY_NOTIFICATION_CANCEL_ONGOING
        cancelOngoingIntent.putExtra(
            NotificationConstants
            .INTENT_EXTRA_STICKY_NOTIFICATION_CANCEL_TRIGGER, trigger)
        cancelOngoingIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE, stickyType)
        CommonUtils.getApplication().sendBroadcast(cancelOngoingIntent)
        Logger.d(LOG_TAG, "Sent Intent to stop any ongoing sticky notification")
    }

    private fun handleExpiredNotification(stickyNotificationEntity: StickyNotificationEntity) {
        Logger.d(LOG_TAG, "But the $stickyNotificationEntity is to be started is expired, so " +
                "deleting from DB and returning")
        StickyNotificationsDBInstance.stickyNotificationDao()
            .deleteNotification(stickyNotificationEntity.id, stickyNotificationEntity.type)
        checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
    }

    private fun handleNotificationMetaResponse(id: String?, type: String,
                                               baseNotificationAsset:
                                               BaseNotificationAsset, isUserLiveOptIn: Boolean,
                                               audioInput: AudioInput? = null) {
        Logger.d(LOG_TAG, "HandleMetaResponse Success -Enter")
        id ?: return
        val notification = StickyNotificationsDBInstance.stickyNotificationDao()
            .getNotificationByIdAndType(id, type)

        if (notification == null ||
            notification.jobStatus == StickyNotificationStatus.UNSCHEDULED) {
            Logger.d(
                LOG_TAG, "notification null or the job status of $notification is not " +
                    "SCHEDULED, so returning")
            checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
            return
        }

        if (!isMetaResponseValid(baseNotificationAsset, notification)) {
            //For news sticky, retry after auto refresh interval
            if (stickyType == NotificationConstants.STICKY_NEWS_TYPE) {
                rescheduleStickyForNextRefresh(stickyNavModelFromStickyNotificiationEntity(notification))
                logStickyUndisplayedEvent(stickyNotificationEntity = notification, reason = API_ERROR)
            } else {
                handleInvalidMetaResponse(baseNotificationAsset, notification)
            }
            return
        }

        //set the list as empty for news sticky case to avoid the list getting passed in intent
        //Might lead to 1MB limit error
        //In news case we fetch from db to show items
        if(baseNotificationAsset is NewsStickyNotificationAsset){
            //Add non blocked non-already present items in db till maxItemsLimit
            baseNotificationAsset?.let {
                notification?.expiryTime?.let{ expTime ->
                    it.expiryTime = if(it.expiryTime > 0) it.expiryTime else expTime
                }

                //Read auto refresh time from pref and apply if meta response didnot contain it
                if(!(it.autoRefreshInterval > 0)){
                    it.autoRefreshInterval = PreferenceManager.getPreference(AppStatePreference.NEWS_STICKY_AUTO_REFRESH_INTERVAL, 0)/1000
                }


                PreferenceManager.savePreference(AppStatePreference.LAST_KNOWN_DISABLE_LOGGING_STATUS_NEWS_STICKY, it.disableEvents)
                //Delete all unread notifications every time sticky flow is initiated from meta path
                //Read notifications will still be used for condition check hence shouldn't be deleted

                NotificationDB.instance().getNotificationDao().deleteUnreadNotificationsForStickyType(NotificationConstants.STICKY_NEWS_TYPE)
                val listOfItemsToBeAdded = mutableListOf<BaseModel>()
                it.stickyItems?.let { items ->
                    val maxNewsItemLimit = PreferenceManager.getPreference(AppStatePreference.MAXIMUM_NUMBER_OF_ITEMS_TO_BE_SHOWN_IN_NEWS_STICKY, 5);
                    val trayManagementSelectedOption = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, -1)
                    var countOfValidItemsAdded = 0
                    for(item in items) {
                        try {
                            val newsModel = NotificationHandler.handleDeepLinkNotifications(
                                NotificationDeliveryMechanism.PULL, null, item) as? NewsNavModel;
                            newsModel?.let {item ->
                                if(newsModel.baseInfo != null && newsModel.baseInfo.uniqueId != null && !newsModel.baseInfo.sourceId.isNullOrEmpty()){
                                    val notiFromDb = NotificationDB.instance().getNotificationDao().getNotification(newsModel.baseInfo.uniqueId, false)
                                    if(SocialDB.instance().followEntityDao().isSourceIdBlocked(newsModel.baseInfo.sourceId)
                                        || (notiFromDb != null && notiFromDb.baseInfo!= null && notiFromDb.baseInfo.isRead) || notiFromDb?.isSeen?:false
                                            || (notiFromDb != null && notiFromDb.baseInfo != null && !notiFromDb.baseInfo.isGrouped && notiFromDb.stickyItemType.equals(NotificationConstants.STICKY_NONE_TYPE)
                                                    && !notiFromDb.baseInfo.wasSkippedByUser() && (trayManagementSelectedOption != Constants.ONLY_LIVE_TICKER))){
                                        // do not add this then
                                    }else{
                                        if(countOfValidItemsAdded < maxNewsItemLimit){
                                            if(notiFromDb?.baseInfo?.wasSkippedByUser()?:false){
                                                item.baseInfo?.state = NotificationConstants.NOTIFICATION_STATUS_SKIPPED_BY_USER
                                            }
                                            listOfItemsToBeAdded.add(item)
                                            countOfValidItemsAdded += 1
                                        }
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            Logger.caughtException(ex)
                        }
                    }
                    //Delete all notification marked as skipped by user, if any existing such notification was valid to be shown in sticky
                    //will anyways be added in db through the below loop
                    NotificationDB.instance().getNotificationDao().deleteSkippedByUserNotificationsForStickyType(NotificationConstants.STICKY_NEWS_TYPE)
                    for (item in listOfItemsToBeAdded){
                        //Remove notifications for this id from tray
                        NotificationUtils.removeNotificationFromTray(item.baseInfo.uniqueId)
                        item.setDisableEvents(it.disableEvents)
                        NotificationDB.instance().getNotificationDao().removeEntryIfExistsAndAdd(item, true, item?.baseInfo?.state?:null)
                    }

                    listOfItemsToBeAdded.clear()
                    //update tray once the notifications are removed
                    if(countOfValidItemsAdded > 0){
                        NotificationServiceProvider.getNotificationService().updateNotificationsInTray(true)
                    } else {
                        if (stickyType == NotificationConstants.STICKY_NEWS_TYPE) {
                            //For news sticky, retry after auto refresh interval
                            rescheduleStickyForNextRefresh(stickyNavModelFromStickyNotificiationEntity(notification))
                            if(items.size > 0){
                                logStickyUndisplayedEvent(stickyNotificationEntity = notification, reason = BLOCKED_OR_READ)
                            }else{
                                logStickyUndisplayedEvent(stickyNotificationEntity = notification, reason = EMPTY_RESPONSE)
                            }
                        } else if (!isMetaResponseValid(baseNotificationAsset, notification)) {
                            handleInvalidMetaResponse(baseNotificationAsset, notification)
                        }
                        return
                    }
                }
            }

            baseNotificationAsset.stickyItems = mutableListOf()
        }

        val successNotification = updateFromMetaResponse(notification, baseNotificationAsset)
        if (isStickNotificationExpired(successNotification)) {
            handleExpiredNotification(successNotification)
            return
        }

        if (isStickyNotificationScheduledEarlier(successNotification)) {
            handleNotificationScheduledEarlier(successNotification)
            return
        }

        startSuccessfulNotification(isUserLiveOptIn, successNotification, audioInput)
        Logger.d(LOG_TAG, "HandleMetaResponse Success -Exit")
    }

    private fun handleInvalidMetaResponse(baseNotificationAsset: BaseNotificationAsset,
                                          notificationEntity: StickyNotificationEntity) {
        Logger.d(
            LOG_TAG, "The meta response for $notificationEntity is not valid, so going to delete" +
                " from DB")
        StickyNotificationsDBInstance.stickyNotificationDao()
            .deleteNotification(notificationEntity.id,notificationEntity.type)
        Logger.d(LOG_TAG, "Deleted $notificationEntity from DB and going to schedule next possible notification")
        fireNotificationDevEvent(NotificationDevEvent(NotificationDevEventType.META_RESPONSE_MISMATCH.name,
            mapOf(NotificationDevEventParam.META_RESPONSE_ID to baseNotificationAsset.id,
                NotificationDevEventParam.META_RESPONSE_TYPE to baseNotificationAsset.type,
                NotificationDevEventParam.OPT_IN_ID to notificationEntity.id,
                NotificationDevEventParam.OPT_IN_TYPE to notificationEntity.type)))
        checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
    }

    private fun startSuccessfulNotification(isUserLiveOptIn: Boolean,
                                            successNotification: StickyNotificationEntity,
                                            audioInput: AudioInput?) {
        StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(successNotification)

        if (isUserLiveOptIn) {
            Logger.d(LOG_TAG, "StickyNotification is live user opted in")
            val liveNotification = createLiveNotificationFrom(successNotification)
            updateLiveNotificationInDB(liveNotification)
            Logger.d(LOG_TAG, "Going to start Sticky Service")
            handleStickyServiceStart(liveNotification, audioInput)
        } else {
            Logger.d(LOG_TAG, "Going to start Sticky Service")
            handleStickyServiceStart(successNotification, audioInput)
        }
    }

    private fun updateLiveNotificationInDB(liveNotification : StickyNotificationEntity) {
        Logger.d(LOG_TAG, "UpdateLiveNotification in DB - enter")
        StickyNotificationsDBInstance.stickyNotificationDao().markPrevLiveNotificationsAsNonLive()
        StickyNotificationsDBInstance.stickyNotificationDao()
            .insertNotification(liveNotification)
        Logger.d(LOG_TAG, "Marked previous live as non live and marked $liveNotification as LIVE")
        Logger.d(LOG_TAG, "UpdateLiveNotification in DB - exit")
    }

    private fun handleStickyServiceStart(stickyNotificationEntity: StickyNotificationEntity,
                                         audioInput: AudioInput? = null) {
        stickyNotificationEntity.priority ?: return
        Logger.d(LOG_TAG, "Handle Sticky Service Start - Enter")
        val stickyNavModel = stickyNavModelFromStickyNotificiationEntity<BaseNotificationAsset,
                BaseDataStreamAsset>(stickyNotificationEntity, audioInput)
            ?: return
        val onGoingNotifications = StickyNotificationsDBInstance.stickyNotificationDao().getNotificationsByStatus(StickyNotificationStatus.ONGOING, stickyType)

        val onGoingNotification = if (!CommonUtils.isEmpty(onGoingNotifications))onGoingNotifications?.get(0) else null

        if (onGoingNotification == null) {
            handleStartStickyServiceForNoOngoingCase(stickyNotificationEntity, stickyNavModel)
        } else if (CommonUtils.equals(onGoingNotification.id, stickyNotificationEntity.id) && CommonUtils
                .equals(onGoingNotification.type, stickyNotificationEntity.type)) {
            // Both ongoing notification and new notification are same, so restarting the service
            // again
            Logger.d(LOG_TAG, "Both are ongoing and new notification are same, so restarting the service")
            val onGoingNavModel = stickyNavModelFromStickyNotificiationEntity<BaseNotificationAsset, BaseDataStreamAsset>(onGoingNotification)
            StickyNotificationServiceUtils.manageStickyNotification(stickyNavModel, onGoingNavModel)
            scheduleNext(stickyNotificationEntity)
        } else if (isUserOptedAndNeedsToBeLiveNotification(stickyNotificationEntity)
            || stickyNotificationEntity.priority > onGoingNotification.priority?: Int.MIN_VALUE) {
            handleStartStickyServiceForNotificationHigherThanOngoingCase(stickyNotificationEntity, stickyNavModel, onGoingNotification)
        } else {
            handleStartStickyServiceForNotificationLesserThanOngoingCase(stickyNotificationEntity, stickyNavModel, onGoingNotification)
        }
        Logger.d(LOG_TAG, "Handle Sticky Service Start - Exit")
    }

    /**
     * CASE 1: No OnGoing while starting the sticky service for new notification
     *
     * Step 1: Start Sticky service for new notification, any exception while starting the
     * service, mark it as UNSCHEDULED in DB and Schedule Next and return
     *
     * Step 2: Mark the status as ONGOING in DB
     *
     * Step 3: Schedule Next and return
     */
    private fun handleStartStickyServiceForNoOngoingCase(stickyNotificationEntity:
                                                         StickyNotificationEntity, stickyNavModel:
                                                         StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>) {
        Logger.d(LOG_TAG, "There is no onGoing notification, so starting service directly")
        try {
            //Step 1
            StickyNotificationServiceUtils.startStickyNotificationService(stickyNavModel)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
            StickyNotificationsDBInstance.stickyNotificationDao().markNotificationStatus(stickyNotificationEntity.id, stickyNotificationEntity.type, StickyNotificationStatus.UNSCHEDULED)
            Logger.d(LOG_TAG, "Problem in starting the sticky service, so marked in DB as unscheduled")
            scheduleNext(null)
            return
        }

        //Step 2
        StickyNotificationsDBInstance.stickyNotificationDao().markNotificationStatus(stickyNotificationEntity.id, stickyNotificationEntity.type, StickyNotificationStatus.ONGOING)
        Logger.d(LOG_TAG, "Marked status for $stickyNotificationEntity as ONGOING and going to " +
                "call schedule Next")
        //Step 3
        scheduleNext(StickyNotificationsDBInstance.stickyNotificationDao().getNotificationByIdAndType(stickyNotificationEntity.id, stickyNotificationEntity.type))
    }

    /**
     * CASE 2: New notification is LIVE or has higher priority than Ongoing
     *
     * Step 1. Start Sticky Service for new and cancel the service for onGoing, if any exception
     * while starting service, we mark new as UNSCHEDULED and schedule next and return
     *
     * Step 2: Mark in DB as UNSCHEDULED for ongoing and as ONGOING for new notification
     *
     * Step 3: Schedule Next
     */
    private fun handleStartStickyServiceForNotificationHigherThanOngoingCase(stickyNotificationEntity:
                                                                             StickyNotificationEntity,
                                                                             stickyNavModel:
                                                                             StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>,
                                                                             onGoingNotification: StickyNotificationEntity) {
        Logger.d(LOG_TAG, "New notification is LIVE or having priority > ongoing priority, so " +
                "starting service directly")
        val onGoingNavModel = stickyNavModelFromStickyNotificiationEntity<BaseNotificationAsset, BaseDataStreamAsset>(onGoingNotification)

        try {
            //Step 1
            StickyNotificationServiceUtils.manageStickyNotification(stickyNavModel, onGoingNavModel)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
            StickyNotificationsDBInstance.stickyNotificationDao()
                .markNotificationStatus(stickyNotificationEntity.id,
                    stickyNotificationEntity.type, StickyNotificationStatus.UNSCHEDULED)
            Logger.d(LOG_TAG, "Problem in starting the sticky service, so marked in DB as unscheduled")
            scheduleNext(onGoingNotification)
            return
        }
        //Step 2
        StickyNotificationsDBInstance.stickyNotificationDao()
            .markNotificationStatus(onGoingNotification.id, onGoingNotification.type,
                StickyNotificationStatus.UNSCHEDULED)
        Logger.d(LOG_TAG, "Marked the onGoing notification $onGoingNotification as UNSCHEDULED")
        StickyNotificationsDBInstance.stickyNotificationDao()
            .markNotificationStatus(stickyNotificationEntity.id,
                stickyNotificationEntity.type, StickyNotificationStatus.ONGOING)
        Logger.d(
            LOG_TAG, "Marked the new notification $stickyNotificationEntity as ONGOING and going to schedule Next")
        //Step 3
        scheduleNext(StickyNotificationsDBInstance.stickyNotificationDao().getNotificationByIdAndType(stickyNotificationEntity.id, stickyNotificationEntity.type))
    }

    /**
     * CASE 3: New notification has lower priority than ONGOING
     *
     * Step 1: Continue with ongoing notification, so need to start sticky service for new
     * notification
     *
     * Step 2: Mark the new notification as UNSCHEDULED in DB
     */
    private fun handleStartStickyServiceForNotificationLesserThanOngoingCase(stickyNotificationEntity: StickyNotificationEntity,
                                                                             stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>,
                                                                             onGoingNotification: StickyNotificationEntity) {
        Logger.d(LOG_TAG, "The onGoing notification $onGoingNotification has HIGH priority than $stickyNotificationEntity")

        //Step 1, no functionality required

        //Step 2
        StickyNotificationsDBInstance.stickyNotificationDao()
            .markNotificationStatus(stickyNotificationEntity.id,
                stickyNotificationEntity.type, StickyNotificationStatus.UNSCHEDULED)
        Logger.d(LOG_TAG, "Marked new notification $stickyNotificationEntity as UNSCHEDULED and going to schedule Next")
    }

    private fun handleNotificationScheduledEarlier(notificationEntity: StickyNotificationEntity) {
        Logger.d(
            LOG_TAG, "Notification $notificationEntity is scheduled earlier than current, so " +
                "marking it as unscheduled in DB")
        val earlierNotification = notificationEntity.copy(jobStatus = StickyNotificationStatus.UNSCHEDULED)
        StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(earlierNotification)
        Logger.d(
            LOG_TAG, "Marked in DB as UNSCHEDULED and going to schedule next possible " +
                "notification")
        checkOnGoingAndScheduleNotification(Trigger.ON_ERROR, null)
    }

    fun checkAndClearDND(isForceOptIn: Boolean = false, optReason: OptReason = OptReason.SERVER, needNewScheduling: Boolean = false) {
        when (stickyType) {
            StickyNavModelType.NEWS.stickyType -> {
                val dndTime = PreferenceManager.getPreference(AppStatePreference.NEWS_STICKY_DND_TIME, 0L)
                //If user turned ON news widget OR The DND time expired, clear the DND preference and clear the user opted out state
                if (isForceOptIn || (dndTime > 0 && System.currentTimeMillis() > dndTime)) {
                    val hadUserOptedOut = hasUserOptedOutOfSticky(NotificationConstants.NEWS_STICKY_OPTIN_ID, stickyType)
                    PreferenceManager.remove(AppStatePreference.NEWS_STICKY_DND_TIME)
                    if((PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, -1) != Constants.NO_NOTIFICATIONS) ||
                        PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true)){
                        PreferenceManager.savePreference(AppStatePreference.NEWS_STICKY_ENABLED_STATE, true)
                    }
                    clearUserOptOut(NotificationConstants.NEWS_STICKY_OPTIN_ID, NotificationConstants.STICKY_NEWS_TYPE, optReason)
                    if (hadUserOptedOut && needNewScheduling) {
                        NewsStickyPushScheduler.scheduleNextBestTimeWindow()
                    }
                    StickyDndTimeResetHelper.cancelDndTimeResetWork()
                }else if(dndTime == 0L){
                    if((PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, -1) != Constants.NO_NOTIFICATIONS) ||
                        PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true)){
                        PreferenceManager.savePreference(AppStatePreference.NEWS_STICKY_ENABLED_STATE, true)
                    }
                }
            }
        }
    }

    private fun clearUserOptOut(id: String, type: String, optReason: OptReason) {
        val stickyNotification = StickyNotificationsDBInstance.stickyNotificationDao()
            .getNotificationByIdAndType(id, type)
        stickyNotification?.let {
            val copy = it.copy(optReason = optReason,
                optState = StickyOptState.OPT_IN)
            StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(copy)
        }
    }
}