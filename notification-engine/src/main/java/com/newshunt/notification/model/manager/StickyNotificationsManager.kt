/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.manager

import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.*
import com.newshunt.dataentity.notification.asset.CommentaryState
import com.newshunt.dataentity.notification.asset.OptInEntity
import com.newshunt.dataentity.notification.asset.OptOutEntity
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.helper.*
import com.newshunt.notification.model.entity.server.StickyAudioCommentary
import com.newshunt.notification.model.entity.server.getCurrentAudioCommentary
import com.newshunt.notification.model.internal.dao.*
import io.reactivex.schedulers.Schedulers
import java.io.Serializable

/**
 * @author santhosh.kc
 */

enum class Trigger (val action : NotificationActionAnalytics? = null) : Serializable {
    APP_START, USER_OPT_IN, SERVER_OPT_IN,
    USER_OPT_OUT(NotificationActionAnalytics.WEB_OPT_OUT),
    SERVER_OPT_OUT(NotificationActionAnalytics.FORCE_EXPIRE),
    NOTIFICATION_COMPLETE (NotificationActionAnalytics.SYSTEM_EXPIRE),
    NOTIFICATION_REMOVED_BY_USER (NotificationActionAnalytics.CROSS_DELETE),
    NOTIFICATION_OVERRIDDEN (NotificationActionAnalytics.OVERRIDDEN),
    NOTIFICATION_REMOVED_BY_TTL (NotificationActionAnalytics.SYSTEM_EXPIRE),
    NOTIFICATION_RESCHEDULED, ON_ERROR, NOTIFICATION_DISABLED, NOTIFICATION_ENABLED
}

object StickyNotificationsManager {

    private val Scheduler = Schedulers.from(START_STICKY_SERVICE_EXECUTOR)
    const val TAG = "StickyNotificationsManager"
    const val MAX_META_RETRY_ATTEMPTS = 5
    private val stickySchedulingEngineMap = mapOf(
        StickyNavModelType.CRICKET.stickyType to StickySchedulingEngine(StickyNavModelType.CRICKET.stickyType, Scheduler),
        StickyNavModelType.GENERIC.stickyType to StickySchedulingEngine(StickyNavModelType.GENERIC.stickyType, Scheduler),
        StickyNavModelType.NEWS.stickyType to StickySchedulingEngine(StickyNavModelType.NEWS.stickyType, Scheduler))

    /**
     * TRIGGER #1
     * Function to schedule next sticky notification, one of trigger point
     */
    fun onAppStart() {
        checkAndSchedule(Trigger.APP_START)
    }

    /**
     * TRIGGER #2
     * Function to schedule next sticky notification, on server opt In notifications
     *
     * Step 1: Read all server opted notifications, all state Opt in/out
     *
     * Step 2: For each server opted notifications in DB, if not contained in list of server
     * optInEntities, we unschedule job(if job has been scheduled) or stop the service(if
     * ongoing) and finally delete from the DB
     *
     * Step 3: Valid the server opt in entities(check for startTime < expiry time && expiry time
     * > current time etc)
     *
     * Step 4: Insert into DB by calling function, handleUserOptInInsertionIntoDB, which takes
     * care of not overriding the optReason state if the server opt in entity is already opted by
     * user but updating info like metaurl, starttime ,expiry time etc
     *
     * Step 5: Finally call the function which checks the ongoing and schedules next possible
     * notification
     */
    fun serverOptInNotifications(serverOptInEntities: List<OptInEntity>?) {
        serverOptInEntities ?: return

        // NOTE: IMPORTANT - serverOptInEntities.isEmpty() is a valid case, so not returning, it
        // means server wants to remove all server opted notifications

        START_STICKY_SERVICE_EXECUTOR.execute {
            //Step 1
            val serverNotificationsFromDB = StickyNotificationsDBInstance.stickyNotificationDao()
                    .getNotificationsByOptReasonIncludingAllOptState(OptReason.SERVER)

            serverNotificationsFromDB?.forEach {
                val toOptInEntity = stickyNotificationEntityToOptInEntity(it)
                //Step 2
                //For now news sticky optin is not sent via this api path, hence this check
                if (!serverOptInEntities.contains(toOptInEntity) && toOptInEntity.type != NotificationConstants.STICKY_NEWS_TYPE) {
                    if (it.jobStatus == StickyNotificationStatus.SCHEDULED) {
                        Logger.d(TAG, "Server has opted out this SCHEDULED $it, so cancelling schedule job")
                        StickyNotificationJobScheduler.cancelJobSchedulerForStickyNotification(it)
                    } else if (it.jobStatus == StickyNotificationStatus.ONGOING) {
                        Logger.d(TAG, "Server has opted out this ONGOING $it, so stopping the service")
                        stickySchedulingEngineMap[it.type]?.cancelOngoingNotificationIfAny(Trigger.SERVER_OPT_OUT)
                    }
                    Logger.d(TAG, "Server has opted out of this $it, so removing from DB")
                    StickyNotificationsDBInstance.stickyNotificationDao().deleteNotification(it.id, it.type)
                }
            }

            //Step 3 and 4
            handleUserOptInInsertionIntoDB(getValidOptedInEntities(serverOptInEntities), false)
            checkAndSchedule(Trigger.SERVER_OPT_IN)
        }
    }

    /**
     * TRIGGER #3
     * Function to Opt In Notification, also one of the trigger points to schedule next sticky
     * notification
     *
     * Step 1: Insert all opt in notifications into DB
     *
     * Step 2: Prepare list of live notifications and pickup the live notification has highest
     * priority and earlier starTime, that becomes to be live Notification
     *
     * Step 3: If no live notification in Step 2, call checkOnGoingAndScheduleNext() and return
     *
     * Step 4: Cancel all scheduled jobs, and mark everything as UNSCHEDULED
     *
     * Step 5: Mark the toBeLiveNotification as SCHEDULED (Even though not scheduling job) and
     * startStickyService which will take care of mark it as ONGOING (if meta present already)
     * else will mark it as ONGOING on meta url response
     */
    fun userOptInNotifications(optInEntities: List<OptInEntity>?) {
        optInEntities ?: return

        START_STICKY_SERVICE_EXECUTOR.execute {
            //Step 1
            val validOptInEntities = getValidOptedInEntities(optInEntities)

            if (CommonUtils.isEmpty(validOptInEntities)) {
                Logger.d(TAG, "None of optInEntities -> $optInEntities are valid, so returning")
                return@execute
            }

            val insertedEntities = handleUserOptInInsertionIntoDB(validOptInEntities, true)

            //Step 2
            val toBeLiveOptInEntities = getEarliestLiveOptInEntitiesGroupedByType(insertedEntities)
            if(!toBeLiveOptInEntities.isNullOrEmpty()) {
                toBeLiveOptInEntities.forEach { toBeLiveOptInEntity ->
                    StickyNotificationsDBInstance.stickyNotificationDao().getNotificationByIdAndType(toBeLiveOptInEntity.id, toBeLiveOptInEntity.type)?.let { toBeLiveNotification ->
                        Logger.d(TAG, "Opted in notification $toBeLiveNotification is LIVE")
                        //Step 4
                        val scheduledNotifications = StickyNotificationsDBInstance
                            .stickyNotificationDao().getNotificationsByStatus(StickyNotificationStatus.SCHEDULED, toBeLiveNotification.type)
                        Logger.d(TAG, "Already scheduled notifications: $scheduledNotifications")

                        StickyNotificationJobScheduler.cancelAllJobSchedulerJobs(scheduledNotifications)
                        Logger.d(TAG, "Cancelled already scheduled notifications..")
                        StickyNotificationsDBInstance.stickyNotificationDao()
                            .changeNotificationsJobStatus(StickyNotificationStatus.SCHEDULED, StickyNotificationStatus.UNSCHEDULED, toBeLiveNotification.type)
                        Logger.d(TAG, "Marked those notifications as UNSCHEDULED")
                        //Step 5
                        StickyNotificationsDBInstance.stickyNotificationDao()
                            .markNotificationStatus(toBeLiveNotification.id, toBeLiveNotification.type,
                                StickyNotificationStatus.SCHEDULED)
                        Logger.d(TAG, "Marked live notification as job SCHEDULED..")
                        stickySchedulingEngineMap[toBeLiveNotification.type]?.startStickyService(toBeLiveNotification, false, toBeLiveOptInEntity.audioInput)
                    }
                }
            } else {
                //Step 3
                Logger.d(TAG, "None of opted in notifications are LIVE")
                checkAndSchedule(Trigger.USER_OPT_IN)
            }
        }
    }

    private fun handleUserOptInInsertionIntoDB(optInEntities: List<OptInEntity>?,
                                               isUserOptIn: Boolean) : List<OptInEntity>?{
        val insertedEntities = ArrayList<OptInEntity>()
        optInEntities?.forEach {
            val alreadyPresentNotification = StickyNotificationsDBInstance
                    .stickyNotificationDao().getNotificationByIdAndType(it.id, it.type)
            if (alreadyPresentNotification != null) {
                val shouldNotOptIn = (!isUserOptIn &&
                        alreadyPresentNotification.optReason == OptReason.USER &&
                        alreadyPresentNotification.optState == StickyOptState.OPT_OUT)
                if (!shouldNotOptIn) {
                    Logger.d(TAG, "Notification $alreadyPresentNotification already present, so " +
                            "marking its state as OPT_IN")
                    val notificationToInsert = if (isUserOptIn)
                        userOptInEntityToStickyNotificationEntity(it, alreadyPresentNotification)
                    else serverOptInEntityToStickyNotificationEntity(it, alreadyPresentNotification)
                    StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(notificationToInsert)
                    insertedEntities.add(it)
                } else {
                    val notificationToInsert = serverOptInEntityToStickyNotificationEntity(it, alreadyPresentNotification, alreadyPresentNotification.optState)
                    StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(notificationToInsert)
                    insertedEntities.add(it)
                    Logger.d(TAG, "User has already OPTED OUT $alreadyPresentNotification, but server trying to optIn, update the DB but do not change the opt states")
                }
            } else {
                val optedInNotification = optInEntityToStickNotificationEntity(it, if (isUserOptIn) OptReason.USER else OptReason.SERVER)
                //For some sticky, user could have opted out even when there's no active sticky in DB
                if (hasUserOptedOutOfSticky(it.id, it.type)) {
                    StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(optedInNotification.copy(optReason = OptReason.USER, optState = StickyOptState.OPT_OUT))
                } else {
                    StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(optedInNotification)
                }
                Logger.d(TAG, "Opted in notification: $optedInNotification not present, so " +
                        "inserting into DB")
                insertedEntities.add(it)
            }
        }
        return insertedEntities
    }

    fun optOutNotifications(optOutEntities: List<OptOutEntity>?, isUserOptOut: Boolean = false) {
        optOutEntities ?: return
        optOutNotifications(optOutEntities, isUserOptOut, if (isUserOptOut) Trigger.USER_OPT_OUT else
            Trigger.SERVER_OPT_OUT)
    }

    private fun optOutNotifications(optOutEntities: List<OptOutEntity>?,
                                    isUserOptOut: Boolean = false, trigger: Trigger) {
        optOutEntities?.let {
            START_STICKY_SERVICE_EXECUTOR.execute {
                val optReason = if (isUserOptOut) OptReason.USER else OptReason.SERVER
                it.forEach { optOutEntity ->
                    run {
                        Logger.d(TAG, "User has opted out notification with id : $optOutEntity.id and type : $optOutEntity.type")

                        val alreadyPresentNotification = StickyNotificationsDBInstance
                                .stickyNotificationDao().getNotificationByIdAndType(optOutEntity.id, optOutEntity.type)

                        alreadyPresentNotification?.let {
                            val shouldNotOptOut = (!isUserOptOut && it.optState == StickyOptState
                                    .OPT_IN && it.optReason == OptReason.USER)

                            if (!shouldNotOptOut) {
                                val jobStatus = if (alreadyPresentNotification.jobStatus ==
                                        StickyNotificationStatus.ONGOING)
                                    StickyNotificationStatus.ONGOING else StickyNotificationStatus.UNSCHEDULED
                                StickyNotificationsDBInstance.stickyNotificationDao()
                                        .markNotificationOptOut(optOutEntity.id, optOutEntity
                                                .type, if (it.optReason == OptReason.USER)
                                            OptReason.USER else optReason, jobStatus)
                                Logger.d(TAG, "Marked the status in DB as OPT_OUT")
                            } else {
                                Logger.d(TAG, "Server trying to Opt out $it which user opted in, " +
                                        "so not opting out")
                            }
                        }
                    }
                }
                checkAndSchedule(trigger)
            }
        }
    }

    /**
     * TRIGGER #5
     *
     * Function to mark onNotificationComplete, also one of the trigger points to schedule next
     * sticky notification
     */
    fun onNotificationComplete(id: String?, type: String?) {
        id ?: return
        type ?: return
        START_STICKY_SERVICE_EXECUTOR.execute {
            Logger.d(TAG, "Notification with id : $id and type : $type is completed")
            StickyNotificationsDBInstance.stickyNotificationDao().deleteNotification(id, type)
            Logger.d(TAG, "Removed the notification from DB")
            cancelNotificationRemoveFromTrayJob(id, type)
            stickySchedulingEngineMap[type]?.checkOnGoingAndScheduleNotification(Trigger.NOTIFICATION_COMPLETE, null)
        }
    }

    /**
     * TRIGGER #6
     *
     * Function to change expiry time of ongoing notification and schedule next possible
     * notitification
     */
    fun onExpiryTimeChanged(id: String?, type: String?, expiryTime : Long) {
        id ?: return
        type ?: return
        if (expiryTime <= 0) {
            return
        }

        START_STICKY_SERVICE_EXECUTOR.execute {
            Logger.d(TAG, "Expiry time changed for notification with id: $id and type: $type and " +
                    "new expiry time $expiryTime")
            val notification = StickyNotificationsDBInstance.stickyNotificationDao()
                    .getNotificationByIdAndType(id, type)
            notification ?: return@execute

            val updatedNotification = notification.copy(expiryTime = expiryTime)
            StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(updatedNotification)

            Logger.d(TAG, "Added job for removing from notification tray for $updatedNotification" +
                    " for new expiry time : $expiryTime")
            addNotificationRemoveFromTrayJob(updatedNotification.id, updatedNotification.type, expiryTime)

            Logger.d(TAG, "Scheduled next on change in expiry time")
            stickySchedulingEngineMap[type]?.scheduleNext(updatedNotification)
        }
    }

    /**
     * TRIGGER #7
     *
     * Function to reschedule ongoing notification
     */
    fun onNotificationRescheduled(id: String?, type: String?, newStartTime : Long) {
        id ?: return
        type ?: return

        if (newStartTime <= 0) {
            return
        }

        START_STICKY_SERVICE_EXECUTOR.execute {
            Logger.d(TAG, "Start time changed for notification with id: $id and type: $type and " +
                    "new expiry time $newStartTime and going to reschedule")
            val notification = StickyNotificationsDBInstance.stickyNotificationDao()
                    .getNotificationByIdAndType(id, type)
            notification ?: return@execute

            val updatedNotification = notification.copy(startTime = newStartTime, jobStatus = StickyNotificationStatus.UNSCHEDULED)
            StickyNotificationsDBInstance.stickyNotificationDao().insertNotification(updatedNotification)

            Logger.d(TAG, "Update start time and marked the notification as unscheduled in DB, " +
                    "now going to look to schedule next possible notification")
            stickySchedulingEngineMap[type]?.checkOnGoingAndScheduleNotification(Trigger.NOTIFICATION_RESCHEDULED, null)
        }
    }

    fun onNotificationStopped(type: String){
        START_STICKY_SERVICE_EXECUTOR.execute {
            StickyNotificationServiceUtils.handleStickyStartStopRelatedGroupedNotificationsUpdate(false, type)
        }
    }

    fun onNotificationStarted(type: String){
        START_STICKY_SERVICE_EXECUTOR.execute {
            StickyNotificationServiceUtils.handleStickyStartStopRelatedGroupedNotificationsUpdate(true, type)
        }
    }

    /**
     * TRIGGER #8
     *
     * Function to opt out notification as it is dismissed from tray
     */
    fun onNotificationDimissedFromTrayByUser(id: String?, type: String?) {
        id ?: return
        type ?: return

        Logger.d(TAG, "User dismissed the notification from tray for $id and $type")
        optOutNotifications(listOf(OptOutEntity(id, type)), true, Trigger.NOTIFICATION_REMOVED_BY_USER)
    }

    /**
     * TRIGGER #9
     *
     * Function to remove notification from DB on notification removed from tray
     */
    fun onNotificationRemovedFromTrayJobDone(id: String?, type: String?) {
        id ?: return
        type ?: return
        START_STICKY_SERVICE_EXECUTOR.execute {
            Logger.d(TAG, "Notification with id : $id and type : $type is removed from tray by " +
                    "job service")
            StickyNotificationsDBInstance.stickyNotificationDao().deleteNotification(id, type)
            Logger.d(TAG, "Removed the notification from DB")
            stickySchedulingEngineMap[type]?.cancelOngoingNotificationIfAny(Trigger.NOTIFICATION_COMPLETE)
            stickySchedulingEngineMap[type]?.checkOnGoingAndScheduleNotification(Trigger.NOTIFICATION_REMOVED_BY_TTL, null)
        }
    }

    /**
     * Function used by JS Callback to stop Audio for onGoing notification
     */
    fun jsCallbackStopAudio(optOutEntity: OptOutEntity?) {
        optOutEntity ?: return

        START_STICKY_SERVICE_EXECUTOR.execute {
            Logger.d(TAG, "JS Callback to stop audio of $optOutEntity")
            val onGoingNotification = StickyNotificationsDBInstance.stickyNotificationDao()
                    .getNotificationsByStatus(StickyNotificationStatus.ONGOING, optOutEntity.type)?.firstOrNull()
                    ?: return@execute

            if (!CommonUtils.equals(onGoingNotification.id, optOutEntity.id) || !CommonUtils.equals
                    (onGoingNotification.type, optOutEntity.type)) {
                Logger.d(TAG, "But the onGoing notification $onGoingNotification and " +
                        "$optOutEntity are not same, so returning")
                return@execute
            }

            fireStopStickyAudioIntent(onGoingNotification)
            Logger.d(TAG, "Fired stop audio intent for above notification")
        }
    }

    /**
     * Function to get ids of opted in notifications for given type
     */
    fun getOptedInNotificationIds(type : String?) : List<String>? {
        type ?: return null

        return StickyNotificationsDBInstance.stickyNotificationDao().getOptInNotificationIds(type)
                ?: return null
    }

    /**
     * Function to get optIn state for notification with id and type
     *
     * return true if Opted In else false
     */
    fun getOptInState(id : String?, type: String?) : Boolean {
        id ?: return false
        type ?: return false

        val stickyOptState = StickyNotificationsDBInstance.stickyNotificationDao()
                .getOptInState(id, type) ?: return false

        return stickyOptState == StickyOptState.OPT_IN
    }

    /**
     * Function to return list of Opted In Ids from list of notifications Ids of a particular type
     */
    fun getOptInSeries(idsToCheck : List<String>?, type : String?) : List<String>? {
        idsToCheck ?: return null
        type ?: return null

        return StickyNotificationsDBInstance.stickyNotificationDao().getOptInSeriesFrom(idsToCheck,
                type) ?: return null
    }

    /**
     * Function to know if audio commentary is played for notification with given id and type
     */
    fun getAudioCommentaryState(id: String?, type: String?): StickyAudioCommentary? {
        id ?: return null
        type ?: return null

        val onGoingNotification = StickyNotificationsDBInstance.stickyNotificationDao()
                .getNotificationsByStatus(StickyNotificationStatus.ONGOING, type)?.firstOrNull()
                ?: return null

        if (!CommonUtils.equals(id, onGoingNotification.id) ||
                !CommonUtils.equals(type, onGoingNotification.type)) {
            return null
        }

        return getCurrentAudioCommentary()?.let {
            if (CommonUtils.equals(it.id, id) && CommonUtils.equals(it.type, type) && it.state ==
                    CommentaryState.PLAYING) it else null
        }
    }

    /**
     * Function to play/Stop audio commentary for ongoing notification if any
     */
    fun playOrStopAudioCommentary(play : Boolean) {
        START_STICKY_SERVICE_EXECUTOR.execute {
            Logger.d(TAG, "Play Audio Commentary - Enter")
            val onGoingNotification = StickyNotificationsDBInstance.stickyNotificationDao()
                    .getNotificationsByStatus(StickyNotificationStatus.ONGOING, StickyNavModelType.CRICKET.name)?.firstOrNull()
                    ?: return@execute
            Logger.d(TAG, "Ongoing notification is: $onGoingNotification")
            if (play) {
                firePlayStickyAudioIntent(onGoingNotification)
                Logger.d(TAG, "Fired play audio intent for above notification")
            } else {
                fireStopStickyAudioIntent(onGoingNotification)
                Logger.d(TAG, "Fired stop audio intent for above notification")
            }
            Logger.d(TAG, "Play Audio Commentary - Exit")
        }
    }

    /**
     * Callback function from WorkManager with id and type and we startStickyService()
     */
    fun startStickyService(stickyNotificationEntityId: String, stickyNotificationType : String) {
        Logger.d(TAG, "StartStickyService(id), type: $stickyNotificationType - enter")
        val stickyNotificationEntity = StickyNotificationsDBInstance.stickyNotificationDao()
                .getNotificationByIdAndType(stickyNotificationEntityId, stickyNotificationType) ?: return
        stickySchedulingEngineMap[stickyNotificationType]?.startStickyService(stickyNotificationEntity, false)
        Logger.d(TAG, "StartStickyService(id), type: $stickyNotificationType - exit")
    }

    private fun checkAndSchedule(trigger: Trigger, stickyNotificationEntity: StickyNotificationEntity? = null) {
        START_STICKY_SERVICE_EXECUTOR.execute {
            stickySchedulingEngineMap.forEach { entry ->
                Logger.d(TAG, "Trigger $trigger ${entry.key} - Enter")
                entry.value.checkOnGoingAndScheduleNotification(trigger, stickyNotificationEntity)
                Logger.d(TAG, "Trigger $trigger ${entry.key} - Exit")
            }
        }
    }

    fun userOptedIn(id: String, type: String) {
        START_STICKY_SERVICE_EXECUTOR.execute {
            stickySchedulingEngineMap[type]?.let { engine ->
                engine.checkAndClearDND(isForceOptIn = true, OptReason.USER, needNewScheduling = true)
            }
        }
    }

    /**
     * TRIGGER #2
     * Function to schedule next sticky notification, on server opt In notifications
     *
     * Step 1: Read all server opted notifications, all state Opt in/out
     *
     * Step 2: For each server opted notifications in DB, if not contained in list of server
     * optInEntities, we unschedule job(if job has been scheduled) or stop the service(if
     * ongoing) and finally delete from the DB
     *
     * Step 3: Valid the server opt in entities(check for startTime < expiry time && expiry time
     * > current time etc)
     *
     * Step 4: Insert into DB by calling function, handleUserOptInInsertionIntoDB, which takes
     * care of not overriding the optReason state if the server opt in entity is already opted by
     * user but updating info like metaurl, starttime ,expiry time etc
     *
     * Step 5: Finally call the function which checks the ongoing and schedules next possible
     * notification
     */
    fun newsStickyServerOptIn(serverOptInEntities: List<OptInEntity>?) {
        serverOptInEntities ?: return

        // NOTE: IMPORTANT - serverOptInEntities.isEmpty() is a valid case, so not returning, it
        // means server wants to remove all server opted notifications

        START_STICKY_SERVICE_EXECUTOR.execute {
            serverOptInEntities.forEach {
                //If Any of the opt in entities were forced, clear the DND states
                if (it.forceOptIn) {
                    Logger.d(TAG, "Force OptIn Sticky for type: ${it.type}")
                    stickySchedulingEngineMap[it.type]?.checkAndClearDND(isForceOptIn = true)
                }
            }

            //Step 1
            val serverNotificationsFromDB = StickyNotificationsDBInstance.stickyNotificationDao()
                .getNotificationsByOptReasonIncludingAllOptState(OptReason.SERVER)

            var serverStickyNotificationEntity: StickyNotificationEntity? = null

            serverNotificationsFromDB?.forEach {
                val toOptInEntity = stickyNotificationEntityToOptInEntity(it)
                //Step 2
                if(toOptInEntity.type == NotificationConstants.STICKY_NEWS_TYPE){
                    serverOptInEntities?.forEach { optInEntity ->
                        if(optInEntity.type == NotificationConstants.STICKY_NEWS_TYPE && it.jobStatus == StickyNotificationStatus.ONGOING){
                            Logger.d(TAG, "New optIn for news sticky received $it, so stopping the " +
                                    "service")
                            stickySchedulingEngineMap[it.type]?.cancelOngoingNotificationIfAny(Trigger.SERVER_OPT_OUT)
                            serverStickyNotificationEntity = optInEntityToStickNotificationEntity(optInEntity.copy(),OptReason.SERVER)
                        }
                    }
                }
            }
            //Step 3 and 4
            handleUserOptInInsertionIntoDB(getValidOptedInEntities(serverOptInEntities), false)
            checkAndSchedule(Trigger.SERVER_OPT_IN, serverStickyNotificationEntity)
        }
    }

    /**
     * Function to handle Enabling/Disabling of cricket Sticky From Settings
     *
     * For enabling case
     * CheckAndSchedule from entries present in db
     *
     * For disabling case
     * Step 1: Cancel  Ongoing Cricket notifications and mark them as UNSCHEDULED
     *
     * Step 2: Mark all SCHEDULED Cricket notifications UNSCHEDULED and cancel all scheduled jobs
     *
     */
    fun handleCricketStickyEnableDisableToggleFromSettings(enabled: Boolean){
        START_STICKY_SERVICE_EXECUTOR.execute {
            if(enabled){
                PreferenceManager.savePreference(AppStatePreference.CRICKET_STICKY_ENABLED_STATE, true)
                stickySchedulingEngineMap[StickyNavModelType.CRICKET.stickyType]?.checkOnGoingAndScheduleNotification(Trigger.NOTIFICATION_ENABLED)
            }else{

                PreferenceManager.savePreference(AppStatePreference.CRICKET_STICKY_ENABLED_STATE, false)
                //Step 1
                stickySchedulingEngineMap[StickyNavModelType.CRICKET.stickyType]?.cancelOngoingNotificationIfAny(Trigger.NOTIFICATION_DISABLED)
                StickyNotificationsDBInstance.stickyNotificationDao()
                        .changeNotificationsJobStatus(StickyNotificationStatus.ONGOING, StickyNotificationStatus.UNSCHEDULED, StickyNavModelType.CRICKET.stickyType)
                Logger.d(TAG, "Marked ongoing notifications as UNSCHEDULED")

                //Step 2
                val scheduledNotifications = StickyNotificationsDBInstance
                        .stickyNotificationDao().getNotificationsByStatus(StickyNotificationStatus.SCHEDULED, StickyNavModelType.CRICKET.stickyType)
                Logger.d(TAG, "Already scheduled notifications: $scheduledNotifications")
                StickyNotificationJobScheduler.cancelAllJobSchedulerJobs(scheduledNotifications)
                Logger.d(TAG, "Cancelled already scheduled notifications..")
                StickyNotificationsDBInstance.stickyNotificationDao()
                        .changeNotificationsJobStatus(StickyNotificationStatus.SCHEDULED, StickyNotificationStatus.UNSCHEDULED, StickyNavModelType.CRICKET.stickyType)
                Logger.d(TAG, "Marked those notifications as UNSCHEDULED")
            }
        }

    }

    /**
     * Function to handle Enabling/Disabling of election Sticky From Settings
     *
     * For enabling case
     * CheckAndSchedule from entries present in db
     *
     * For disabling case
     * Step 1: Cancel  Ongoing election notifications and mark them as UNSCHEDULED
     *
     * Step 2: Mark all SCHEDULED election notifications UNSCHEDULED and cancel all scheduled jobs
     *
     */
    fun handleGenericStickyEnableDisableToggleFromSettings(enabled: Boolean){
        START_STICKY_SERVICE_EXECUTOR.execute {
            if(enabled){
                PreferenceManager.savePreference(AppStatePreference.ELECTION_STICKY_ENABLED_STATE, true)
                stickySchedulingEngineMap[StickyNavModelType.GENERIC.stickyType]?.checkOnGoingAndScheduleNotification(Trigger.NOTIFICATION_ENABLED)
            }else{

                PreferenceManager.savePreference(AppStatePreference.ELECTION_STICKY_ENABLED_STATE, false)
                //Step 1
                stickySchedulingEngineMap[StickyNavModelType.GENERIC.stickyType]?.cancelOngoingNotificationIfAny(Trigger.NOTIFICATION_DISABLED)
                StickyNotificationsDBInstance.stickyNotificationDao()
                        .changeNotificationsJobStatus(StickyNotificationStatus.ONGOING, StickyNotificationStatus.UNSCHEDULED, StickyNavModelType.GENERIC.stickyType)
                Logger.d(TAG, "Marked ongoing notifications as UNSCHEDULED")

                //Step 2
                val scheduledNotifications = StickyNotificationsDBInstance
                        .stickyNotificationDao().getNotificationsByStatus(StickyNotificationStatus.SCHEDULED, StickyNavModelType.GENERIC.stickyType)
                Logger.d(TAG, "Already scheduled notifications: $scheduledNotifications")
                StickyNotificationJobScheduler.cancelAllJobSchedulerJobs(scheduledNotifications)
                Logger.d(TAG, "Cancelled already scheduled notifications..")
                StickyNotificationsDBInstance.stickyNotificationDao()
                        .changeNotificationsJobStatus(StickyNotificationStatus.SCHEDULED, StickyNotificationStatus.UNSCHEDULED, StickyNavModelType.GENERIC.stickyType)
                Logger.d(TAG, "Marked those notifications as UNSCHEDULED")
            }
        }

    }

    /**
     * Function to handle Enabling/Disabling of news Sticky From Settings
     *
     * For enabling case
     * CheckAndSchedule from entries present in db
     *
     * For disabling case
     * Step 1: Cancel  Ongoing election notifications and mark them as UNSCHEDULED
     *
     * Step 2: Mark all SCHEDULED news notifications UNSCHEDULED and cancel all scheduled jobs
     *
     */
    fun handleNewsStickyEnableDisableToggled(enabled: Boolean){
        START_STICKY_SERVICE_EXECUTOR.execute {
            if(enabled){
                PreferenceManager.savePreference(AppStatePreference.NEWS_STICKY_ENABLED_STATE, true)
            }else{

                PreferenceManager.savePreference(AppStatePreference.NEWS_STICKY_ENABLED_STATE, false)
                //Step 1
                stickySchedulingEngineMap[StickyNavModelType.NEWS.stickyType]?.cancelOngoingNotificationIfAny(Trigger.NOTIFICATION_DISABLED)
                StickyNotificationsDBInstance.stickyNotificationDao()
                        .changeNotificationsJobStatus(StickyNotificationStatus.ONGOING, StickyNotificationStatus.UNSCHEDULED, StickyNavModelType.NEWS.stickyType)
                Logger.d(TAG, "Marked ongoing notifications as UNSCHEDULED")

                //Step 2
                val scheduledNotifications = StickyNotificationsDBInstance
                        .stickyNotificationDao().getNotificationsByStatus(StickyNotificationStatus.SCHEDULED, StickyNavModelType.NEWS.stickyType)
                Logger.d(TAG, "Already scheduled notifications: $scheduledNotifications")
                StickyNotificationJobScheduler.cancelAllJobSchedulerJobs(scheduledNotifications)
                Logger.d(TAG, "Cancelled already scheduled notifications..")
                StickyNotificationsDBInstance.stickyNotificationDao()
                        .changeNotificationsJobStatus(StickyNotificationStatus.SCHEDULED, StickyNotificationStatus.UNSCHEDULED, StickyNavModelType.NEWS.stickyType)
                Logger.d(TAG, "Marked those notifications as UNSCHEDULED")
            }
        }

    }
}
