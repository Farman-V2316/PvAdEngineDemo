/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper

import android.content.Context
import android.content.Intent
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.sticky.STICKY_AUDIO_COMMENTARY_ENABLED
import com.newshunt.common.model.retrofit.RestAdapters
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.isValidUrl
import com.newshunt.notification.model.entity.server.StickyAudioCommentary
import com.newshunt.dataentity.notification.AudioInput
import com.newshunt.dataentity.notification.BaseInfo
import com.newshunt.dataentity.notification.StickyNavModel
import com.newshunt.dataentity.notification.StickyNavModelType
import com.newshunt.dataentity.notification.OptReason
import com.newshunt.dataentity.notification.asset.*
import com.newshunt.notification.model.internal.dao.StickyNotificationEntity
import com.newshunt.notification.model.internal.dao.StickyNotificationStatus
import com.newshunt.notification.model.internal.dao.StickyOptState
import com.newshunt.notification.model.manager.NotiRemoveFromTrayJobManager
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.model.internal.dao.StickyNotificationsDBInstance
import com.newshunt.notification.view.receiver.StickyNotificationFinishReceiver
import com.newshunt.notification.view.receiver.StickyNotificationStartReceiver
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
 * @author santhosh.kc
 */

private const val TAG = "StickyNotificationUtils"

fun areStickyNotificationsSame(A: StickyNavModel<BaseNotificationAsset,
	BaseDataStreamAsset>?, B: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?):
        Boolean {
    return CommonUtils.equals(A?.getBaseNotificationAsset()?.id, B?.getBaseNotificationAsset()?.id) &&
            CommonUtils.equals(A?.getBaseNotificationAsset()?.type, B?.getBaseNotificationAsset()?.type)
}

fun getValidOptedInEntities(optInEntities: List<OptInEntity>): List<OptInEntity> {
    val currentTime = System.currentTimeMillis()
    return optInEntities.filter {
        !CommonUtils.isEmpty(it.id) && !CommonUtils.isEmpty(it.type)
                && !CommonUtils.isEmpty(it.metaUrl) && it.metaUrl.isValidUrl() && it.startTime > 0 &&
                it.startTime < it.expiryTime && it.expiryTime > currentTime && it.priority > 0 &&
                !CommonUtils.isEmpty(it.channel)
    }
}

fun fireNotificationCompleteBroadcast(context: Context?,
                                      stickyNavModel: StickyNavModel<BaseNotificationAsset,
	                                      BaseDataStreamAsset>?) {
    context ?: return
    stickyNavModel ?: return

    stickyNavModel.getBaseNotificationAsset()?.let {
        val finishIntent = Intent(context, StickyNotificationFinishReceiver::class.java)
        AppConfig.getInstance()?.let { finishIntent.setPackage(it.packageName) }
        finishIntent.action = NotificationConstants.INTENT_ACTION_STICKY_NOTIFICATION_FINISH
        finishIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID,
                it.id)
        finishIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE,
                stickyNavModel.stickyType)
        context.sendBroadcast(finishIntent)
    }
}

fun fireNotificationStartedBroadCast(context: Context?,
                                     stickyNavModel: StickyNavModel<BaseNotificationAsset,
                                             BaseDataStreamAsset>?){
    context ?: return
    stickyNavModel ?: return

    try{
        stickyNavModel.getBaseNotificationAsset()?.let {
            val finishIntent = Intent(context, StickyNotificationStartReceiver::class.java)
            AppConfig.getInstance()?.let { finishIntent.setPackage(it.packageName) }
            finishIntent.action = NotificationConstants.INTENT_ACTION_STICKY_SERVICE_STARTED
            finishIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE,
                    stickyNavModel.stickyType)
            context.sendBroadcast(finishIntent)
        }
    }catch(ex: Exception){
        Logger.caughtException(ex)
    }
}

fun fireNotificationRescheduledBroadcast(context: Context?,
                                         stickyNavModel: StickyNavModel<BaseNotificationAsset,
	                                         BaseDataStreamAsset>?, newStartTime: Long?) {
    context ?: return
    stickyNavModel ?: return
    newStartTime ?: return

    if (newStartTime <= 0) {
        return
    }

    stickyNavModel.getBaseNotificationAsset()?.let {
        val rescheduleIntent = Intent(context, StickyNotificationFinishReceiver::class.java)
        AppConfig.getInstance()?.let { instance -> rescheduleIntent.setPackage(instance.packageName) }
        rescheduleIntent.action = NotificationConstants.INTENT_ACTION_STICKY_NOTIFICATION_RESCHEDULE
        rescheduleIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID,
                it.id)
        rescheduleIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE,
                stickyNavModel.stickyType)
        rescheduleIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_RESCHEDULE_TIME, newStartTime)
        context.sendBroadcast(rescheduleIntent)
    }
}

fun fireExpiryTimeChangedIntent(stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?,
                                newExpiryTime: Long?) {
    newExpiryTime ?: return
    stickyNavModel?.getBaseNotificationAsset()?.let {
        val expiryIntent = Intent(CommonUtils.getApplication(), StickyNotificationFinishReceiver::class.java)
        if (AppConfig.getInstance() != null) {
            expiryIntent.setPackage(AppConfig.getInstance()!!.packageName)
        }
        expiryIntent.action = NotificationConstants.INTENT_ACTION_STICKY_EXPIRY_TIME_CHANGED
        expiryIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID, it.id)
        expiryIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE, it.type)
        expiryIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_EXPIRY_TIME,
                newExpiryTime)
        CommonUtils.getApplication().sendBroadcast(expiryIntent)
    }
}

fun fireStickyAudioCommentaryStateChangedBroadcast(context: Context,
                                                   stickyAudioCommentary: StickyAudioCommentary?) {
    if (!STICKY_AUDIO_COMMENTARY_ENABLED) {
        return
    }

    val audioStateChangedIntent = Intent(DHConstants.INTENT_STICKY_AUDIO_COMMENTARY_STATE_CHANGED)
    if (AppConfig.getInstance() != null) {
        audioStateChangedIntent.`package` = AppConfig.getInstance().packageName
    }

    audioStateChangedIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_AUDIO_STATE, stickyAudioCommentary)
    CommonUtils.getApplication().sendBroadcast(audioStateChangedIntent)
}

fun constructAudioCommentaryState(stickyNavModel: StickyNavModel<BaseNotificationAsset,
	BaseDataStreamAsset>?): StickyAudioCommentary? {
    stickyNavModel ?: return null
    val id = stickyNavModel.getBaseNotificationAsset()?.id ?: return null
    val type = stickyNavModel.stickyType ?: return null
    val audioUrl = stickyNavModel.getBaseNotificationAsset()?.audioUrl ?: return null
    val audioLanguage = stickyNavModel.getBaseNotificationAsset()?.audioLanguage

    val stickyNavModelType = StickyNavModelType.from(stickyNavModel.stickyType)
    val cricketNotificationAsset = (stickyNavModel.getBaseNotificationAsset() as
            (CricketNotificationAsset))
    var title: String? = null
    if (CommonUtils.equals(stickyNavModelType, StickyNavModelType.CRICKET))
        title = cricketNotificationAsset.team1.teamName + " vs " + cricketNotificationAsset.team2
                .teamName

    return StickyAudioCommentary(id, type, CommentaryState.NOT_OPTED_IN, audioUrl, audioLanguage, title)
}

fun isStickNotificationExpired(stickyNotificationEntity: StickyNotificationEntity): Boolean {

    if (!isStickNotificationValid(stickyNotificationEntity)) {
        return true
    }
    stickyNotificationEntity.expiryTime?.let {
        return stickyNotificationEntity.expiryTime <= System.currentTimeMillis()
    }

    return true
}

fun firePlayStickyAudioIntent(onGoingNotificationEntity: StickyNotificationEntity?) {
    onGoingNotificationEntity ?: return
    if (!STICKY_AUDIO_COMMENTARY_ENABLED) {
        return
    }

    val cancelOngoingIntent = Intent()
    cancelOngoingIntent.`package` = CommonUtils.getApplication().packageName
    cancelOngoingIntent.action = NotificationConstants.INTENT_ACTION_PLAY_STICKY_AUDIO
    CommonUtils.getApplication().sendBroadcast(cancelOngoingIntent)
}

fun fireStopStickyAudioIntent(onGoingNotificationEntity: StickyNotificationEntity?) {
    onGoingNotificationEntity ?: return
    if (!STICKY_AUDIO_COMMENTARY_ENABLED) {
        return
    }

    val cancelOngoingIntent = Intent()
    cancelOngoingIntent.`package` = CommonUtils.getApplication().packageName
    cancelOngoingIntent.action = NotificationConstants.INTENT_ACTION_STOP_STICKY_AUDIO
    CommonUtils.getApplication().sendBroadcast(cancelOngoingIntent)
}

fun isStickyNotificationScheduledEarlier(stickyNotificationEntity: StickyNotificationEntity):
        Boolean {
    stickyNotificationEntity.startTime?.let {
        return stickyNotificationEntity.startTime > System.currentTimeMillis()
    }
    return true
}

fun isStickNotificationValid(stickyNotificationEntity: StickyNotificationEntity): Boolean {
    return stickyNotificationEntity.startTime != null && stickyNotificationEntity.expiryTime != null &&
            stickyNotificationEntity.startTime < stickyNotificationEntity.expiryTime
}

fun isUserOptedLiveOnGoingNotification(stickyNotificationEntity: StickyNotificationEntity?): Boolean {
    return stickyNotificationEntity?.jobStatus == StickyNotificationStatus.ONGOING &&
            stickyNotificationEntity.isLiveOptIn == true
}

fun isUserOptedAndNeedsToBeLiveNotification(stickyNotificationEntity: StickyNotificationEntity?): Boolean {
    return stickyNotificationEntity?.jobStatus == StickyNotificationStatus.SCHEDULED &&
            stickyNotificationEntity.isLiveOptIn == true
}

fun pickFirstBestMatchingUnscheduledNotifications(stickyNotificationEntities:
                                                  List<StickyNotificationEntity>):
        StickyNotificationEntity? {
    val validNotifications = stickyNotificationEntities.filter { isStickNotificationValid(it) }
    if (CommonUtils.isEmpty(validNotifications)) {
        return null
    }
    val liveNotifications = validNotifications.filter { isInLiveTimeBounds(it.startTime, it.expiryTime) }

    return if (!CommonUtils.isEmpty(liveNotifications)) {
        val mutableLiveNotifications = ArrayList(liveNotifications)
        mutableLiveNotifications.sortWith(Comparator { entity1, entity2 ->
            compare(entity1?.priority, entity1?.startTime, entity2?.priority, entity2?.startTime)
        })
        mutableLiveNotifications[0]
    } else {
        validNotifications[0]
    }
}

private fun compare(p1: Int?, s1: Long?, p2: Int?, s2: Long?): Int {
    val priority1 = p1 ?: Int.MIN_VALUE
    val priority2 = p2 ?: Int.MIN_VALUE
    val startTime1 = s1 ?: Long.MAX_VALUE
    val startTime2 = s2 ?: Long.MAX_VALUE

    return when {
        //higher priority means it must be first in list
        priority1 > priority2 -> -1
        priority1 < priority2 -> 1
        else -> when {
            startTime1 < startTime2 -> -1
            startTime1 == startTime2 -> 0
            else -> 1
        }
    }
}

fun stickyNotificationEntityToOptInEntity(stickyNotificationEntity: StickyNotificationEntity):
	OptInEntity {
    return OptInEntity(id = stickyNotificationEntity.id, metaUrl = stickyNotificationEntity
	    .metaUrl ?: Constants.EMPTY_STRING, type = stickyNotificationEntity.type,
	    priority = stickyNotificationEntity.priority ?: Int.MIN_VALUE, startTime =
    stickyNotificationEntity.startTime ?: 0, expiryTime = stickyNotificationEntity.expiryTime
	    ?: 0, channel = stickyNotificationEntity.channel ?: Constants.EMPTY_STRING, channelId = stickyNotificationEntity.channelId?:Constants.EMPTY_STRING)
}

fun optInEntityToStickNotificationEntity(optInEntity: OptInEntity, optReason: OptReason):
        StickyNotificationEntity {
    return StickyNotificationEntity(id = optInEntity.id, metaUrl = optInEntity.metaUrl,
            type = optInEntity.type, priority = optInEntity.priority,
            startTime = optInEntity.startTime, expiryTime = optInEntity.expiryTime,
            channel = optInEntity.channel, optState = StickyOptState.OPT_IN,
            optReason = optReason,
            channelId = optInEntity.channelId)
}

fun userOptInEntityToStickyNotificationEntity(userOptInEntity: OptInEntity,
                                              alreadyExistingNotification:
                                              StickyNotificationEntity): StickyNotificationEntity {
    return alreadyExistingNotification.copy(metaUrl = userOptInEntity.metaUrl, priority =
    userOptInEntity.priority, startTime = userOptInEntity.startTime, expiryTime = userOptInEntity
            .expiryTime, channel = userOptInEntity.channel, optState = StickyOptState.OPT_IN,
            optReason = OptReason.USER, metaUrlAttempts = 0, channelId = userOptInEntity.channelId)
}

fun serverOptInEntityToStickyNotificationEntity(serverOptInEntity: OptInEntity,
                                                alreadyExistingNotification: StickyNotificationEntity,
                                                optState: StickyOptState? = StickyOptState.OPT_IN): StickyNotificationEntity {
    return alreadyExistingNotification.copy(metaUrl = serverOptInEntity.metaUrl, priority =
    serverOptInEntity.priority, startTime = serverOptInEntity.startTime, expiryTime = serverOptInEntity
            .expiryTime, channel = serverOptInEntity.channel, optState = optState,
            metaUrlAttempts = 0, channelId = serverOptInEntity.channelId)
}

fun isInLiveTimeBounds(startTime: Long?, endTime: Long?): Boolean {
    startTime ?: return false
    endTime ?: return false
    val currentTime = System.currentTimeMillis()
    return currentTime in (startTime + 1) until endTime
}

fun getEarliestLiveOptInEntitiesGroupedByType(optInEntities: List<OptInEntity>?): List<OptInEntity>? {
    optInEntities ?: return null

    val groupedList = optInEntities.groupBy {
        it.type
    }

    val returnList = ArrayList<OptInEntity>()
    groupedList.forEach { entry ->
        val liveEntities = ArrayList<OptInEntity>()
        entry.value.forEach {
            if (isInLiveTimeBounds(it.startTime, it.expiryTime)) {
                liveEntities.add(it)
            }
        }
        if (liveEntities.isNotEmpty()) {
            val mutableLiveEntities = ArrayList(liveEntities)
            mutableLiveEntities.sortWith { entity1, entity2 ->
                compare(entity1?.priority, entity1?.startTime, entity2?.priority, entity2?.startTime)
            }
            returnList.add(mutableLiveEntities[0])
        }
    }
    return returnList
}

fun createLiveNotificationFrom(entity: StickyNotificationEntity):
        StickyNotificationEntity {
    return StickyNotificationEntity(id = entity.id, metaUrl = entity.metaUrl,
            type = entity.type, priority = entity.priority, startTime = entity.startTime,
            expiryTime = entity.expiryTime, channel = entity.channel, data = entity.data,
            optState = StickyOptState.OPT_IN, optReason = OptReason.USER, isLiveOptIn = true,
            jobStatus = StickyNotificationStatus.SCHEDULED, channelId = entity.channelId)
}

fun <K : BaseNotificationAsset, V : BaseDataStreamAsset>
        stickyNavModelFromStickyNotificiationEntity(entity: StickyNotificationEntity?,
                                                    audioInput: AudioInput? = null):
        StickyNavModel<K, V>? {
    entity ?: return null
    entity.data ?: return null

    val basicNotificationAssetString = String(entity.data)

    val map: Map<Any?, Any?> = JsonUtils.fromJson(basicNotificationAssetString, object :
            TypeToken<Map<Any?, Any?>>() {}.type) ?: return null

    val stickNavModel = StickyNavModel<K, V>()
    stickNavModel.baseInfo = BaseInfo()
    //  stickNavModel.baseInfo.uniqueId = try { Integer.parseInt(entity.id) } catch (e: Exception) { 0 }
    stickNavModel.baseInfo.priority = entity.priority ?: Int.MIN_VALUE
    stickNavModel.baseInfo.channelId = entity.channelId?: Constants.EMPTY_STRING


    val stickyNavModelType = StickyNavModelType.from(map[STICKY_NAV_MODEL_TYPE_FIELD] as? String)
            ?: return null
    stickNavModel.stickyType = stickyNavModelType.stickyType
    stickNavModel.optReason = entity.optReason
    stickNavModel.priority = entity.priority ?: Int.MIN_VALUE
    stickNavModel.channelId = entity.channelId

    stickNavModel.setBaseNotificationAsset(
            when (stickyNavModelType) {
                StickyNavModelType.CRICKET ->
                    JsonUtils.fromJson(basicNotificationAssetString, object : TypeToken<CricketNotificationAsset>() {}.type)
                StickyNavModelType.NEWS ->
                    JsonUtils.fromJson(basicNotificationAssetString, object :TypeToken<NewsStickyNotificationAsset>(){}.type)
                else ->
                    JsonUtils.fromJson(basicNotificationAssetString, object : TypeToken<GenericNotificationAsset>() {}.type)
            }

    )

    stickNavModel.getBaseNotificationAsset()?.channel = entity.channel
    stickNavModel.getBaseNotificationAsset()?.id = entity.id
    stickNavModel.getBaseNotificationAsset()?.type = stickyNavModelType.stickyType

    stickNavModel.baseInfo.uniqueId =
            getUniqueIdForNotificationInTray(stickNavModel.getBaseNotificationAsset()?.type, stickNavModel.getBaseNotificationAsset()?.id)
    stickNavModel.deeplinkUrl = stickNavModel.getBaseNotificationAsset()?.deeplinkUrl
    stickNavModel.optOutMeta = stickNavModel.getBaseNotificationAsset()?.optOutMeta

    stickNavModel.getBaseNotificationAsset()?.startTime = entity.startTime ?: 0

    stickNavModel.baseInfo?.expiryTime = entity.expiryTime ?: 0
    stickNavModel.getBaseNotificationAsset()?.expiryTime = entity.expiryTime ?: 0

    stickNavModel.audioPlayAtStart =
            !CommonUtils.isEmpty(stickNavModel.getBaseNotificationAsset()?.audioUrl) && audioInput?.audioCommand == AudioCommand.PLAY

    return stickNavModel
}

fun getNoOpStickyCommentary(stickyNavModel: StickyNavModel<BaseNotificationAsset,
	BaseDataStreamAsset>?): StickyAudioCommentary? {
    val commentaryState = constructAudioCommentaryState(stickyNavModel) ?: return null
    return commentaryState.copy(state = null)
}

fun getUniqueIdForNotificationInTray(id: String?, type: String?): Int {
    id ?: return 0
    type ?: return 0
    return (id + type).hashCode()
}

fun updateFromMetaResponse(stickyNotificationEntity: StickyNotificationEntity,
                           baseNotificationAsset: BaseNotificationAsset): StickyNotificationEntity {
    //Update from metaResponse in case of news sticky
    var metaUrl = stickyNotificationEntity.metaUrl
    var channelId = stickyNotificationEntity.channelId
    if(stickyNotificationEntity.type.equals(NotificationConstants.STICKY_NEWS_TYPE) && !baseNotificationAsset.streamUrl.isNullOrEmpty()){
        metaUrl = baseNotificationAsset.streamUrl
        (baseNotificationAsset as? NewsStickyNotificationAsset)?.let{
            if(!it.channelId.isNullOrEmpty()){
                channelId = it.channelId
            }
        }
    }
    return StickyNotificationEntity(id = stickyNotificationEntity.id,
            metaUrl = metaUrl, type = stickyNotificationEntity.type,
            priority = if (baseNotificationAsset.priority > 0) baseNotificationAsset.priority
            else stickyNotificationEntity.priority,
            startTime = if (baseNotificationAsset.startTime > 0) baseNotificationAsset.startTime
            else stickyNotificationEntity.startTime,
            expiryTime = if (baseNotificationAsset.expiryTime > 0) baseNotificationAsset.expiryTime
            else stickyNotificationEntity.expiryTime,
            channel = baseNotificationAsset.channel?.let {
                if (!CommonUtils.isEmpty(it)) it else
                    stickyNotificationEntity.channel
            } ?: stickyNotificationEntity.channel,
            data = JsonUtils.toJson(baseNotificationAsset).toByteArray(),
            optState = stickyNotificationEntity.optState,
            optReason = stickyNotificationEntity.optReason,
            isLiveOptIn = stickyNotificationEntity.isLiveOptIn, jobStatus = stickyNotificationEntity.jobStatus,
            channelId = channelId )
}

fun getNotificationRemoveFromTrayJobTag(id: String?, stickyType: String?): String? {
    id ?: return null
    stickyType ?: return null

    return DailyhuntConstants.JOB_PREFIX + id + Constants.UNDERSCORE_CHARACTER + stickyType
}

fun cancelNotificationRemoveFromTrayJob(id: String?, stickyType: String?) {
    val tag = getNotificationRemoveFromTrayJobTag(id, stickyType) ?: return
    Logger.d(TAG, "Cancelling notification tray remove job for tag $tag")
    DHWorkManager.cancelWork(tag)
}

fun addNotificationRemoveFromTrayJob(id: String?, stickyType: String?, expiryTime: Long?) {
    expiryTime ?: return
    val currentTime = System.currentTimeMillis()
    if (expiryTime < currentTime) {
        return
    }
    val notificationTrayId = getUniqueIdForNotificationInTray(id, stickyType)
    val trayManager = NotiRemoveFromTrayJobManager()
    Logger.d(TAG, "Scheduled notification tray remove job for id $id and type :$stickyType")
    trayManager.scheduleJob(notificationTrayId, id, stickyType, ((expiryTime - currentTime) / 1000f)
            .toInt())
}

fun addNotificationRemoveFromTrayJob(stickyNavModel: StickyNavModel<BaseNotificationAsset,
	BaseDataStreamAsset>?) {
    stickyNavModel ?: return
    val id = stickyNavModel.getBaseNotificationAsset()?.id ?: return
    val type = stickyNavModel.stickyType ?: return
    val expiryTime = stickyNavModel.getBaseNotificationAsset()?.expiryTime ?: return

    addNotificationRemoveFromTrayJob(id, type, expiryTime)
}

fun isMetaResponseValid(baseNotificationAsset: BaseNotificationAsset,
                        stickyNotificationEntity: StickyNotificationEntity): Boolean {
    when(stickyNotificationEntity.type){
        NotificationConstants.STICKY_NEWS_TYPE -> return isMetaResponseValidForNewsSticky(baseNotificationAsset, stickyNotificationEntity)
        else -> {return (CommonUtils.equals(stickyNotificationEntity.id, baseNotificationAsset.id) &&
                CommonUtils.equals(stickyNotificationEntity.type, baseNotificationAsset.type)) &&
                baseNotificationAsset.streamUrl.isValidUrl()}
    }
}

fun isMetaResponseValidForNewsSticky(baseNotificationAsset: BaseNotificationAsset, stickyNotificationEntity: StickyNotificationEntity): Boolean{
    (baseNotificationAsset as? NewsStickyNotificationAsset)?.let{
        if(!it.stickyItems.isNullOrEmpty() && (CommonUtils.equals(stickyNotificationEntity.id, baseNotificationAsset.id) &&
                CommonUtils.equals(stickyNotificationEntity.type, baseNotificationAsset.type)) &&
                baseNotificationAsset.streamUrl.isValidUrl()){
            return true
        }
    }
    return false
}

// Function to check if current notification has any common tags with any sticky notifications.
fun hasCommonTagsWithStickyNotifications(tags : List<String>?) : Boolean {
    val onGoingNotifications = StickyNotificationsDBInstance.stickyNotificationDao()
            .getNotificationsByStatusIncludingAllOptState(StickyNotificationStatus.ONGOING)

    onGoingNotifications?.forEach {
        val stickNavModel = stickyNavModelFromStickyNotificiationEntity<BaseNotificationAsset, BaseDataStreamAsset>(it)
        val stickyTags = stickNavModel?.getBaseNotificationAsset()?.excludeNotificationTags
        stickyTags?.forEach {
            tags?.forEach { it1 -> if (it == it1) return true }
        }
    }
    return false
}

fun hasUserOptedOutOfSticky(id: String, type: String): Boolean {
    return if (type == NotificationConstants.STICKY_NEWS_TYPE) {
        val dndTime = PreferenceManager.getPreference(AppStatePreference.NEWS_STICKY_DND_TIME, 0L)
        dndTime > 0
    } else {
        false
    }
}

fun rescheduleStickyForNextRefresh(stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?) {
    val notification = stickyNavModel?.getBaseNotificationAsset()
    notification ?: return

    val nextRefreshTime = TimeUnit.SECONDS.toMillis(notification.autoRefreshInterval.toLong()) + System.currentTimeMillis()
    if (nextRefreshTime < notification.expiryTime) {
        fireNotificationRescheduledBroadcast(CommonUtils.getApplication(), stickyNavModel, nextRefreshTime)
    }
}

fun composeUrlForNewsSticky(id: String, url: String?): String? {
    var timeWindowId = id
    if (id.contains(NEWS_STICKY_TIME_WINDOW_MACRO)) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DATE)
       timeWindowId = id.replace(NEWS_STICKY_TIME_WINDOW_MACRO, "$year$month$day")
    }
    return RestAdapters.getCompleteUrlFrom(url, mapOf(NEWS_STICKY_QUERY_PARAM_ID to timeWindowId), null)
}