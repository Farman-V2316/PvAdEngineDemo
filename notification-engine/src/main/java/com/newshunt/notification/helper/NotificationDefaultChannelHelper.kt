/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.SpecialChannelHelper
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility
import com.newshunt.notification.domain.NotificationChannelUsecaseController
import com.newshunt.notification.model.entity.ChannelImportantance
import com.newshunt.notification.model.entity.NotificationFilterType

/**
 * Notification channel helper for android oreo
 *
 * @author amit.chaudhary
 */
private const val LOG_TAG="NotifyChannelHelper"

@RequiresApi(Build.VERSION_CODES.O)
fun createDefaultChannel() {
    Logger.d(LOG_TAG, "Creating channel")
    val notificationManager = CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (doesNotificationChannelExist(NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID,
                    notificationManager)) {
        migrateChannelConfig(notificationManager)
        return
    }

    val notificationChannel = NotificationChannel(NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID,
            NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
    notificationChannel.setSound(null, null)
    //Adding to special channel set
    SpecialChannelHelper.addSpecialChannel(notificationChannel.id)
    notificationManager.createNotificationChannel(notificationChannel)
}

@RequiresApi(Build.VERSION_CODES.O)
fun createTempChannel(channelId: String, groupId: String?) {
    val notificationManager = CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationChannel = NotificationChannel(channelId,
            NotificationConstants.NOTIFICATION_TEMP_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
    notificationChannel.setSound(null, null)
    if (!CommonUtils.isEmpty(groupId)) {
        if (!doesNotificationGroupExist(groupId, notificationManager)) {
            val notificationGroup = NotificationChannelGroup(groupId!!, NotificationConstants.NOTIFICATION_TEMP_GROUP_NAME)
            notificationManager.createNotificationChannelGroup(notificationGroup)
            TemporaryChannelManager.addGroup(groupId)
        }
        notificationChannel.group = groupId
    }
    notificationManager.createNotificationChannel(notificationChannel)
    TemporaryChannelManager.addChannel(channelId)


}

@RequiresApi(Build.VERSION_CODES.O)
private fun doesNotificationGroupExist(id: String?, notificationManager: NotificationManager)
        : Boolean {
    val group: NotificationChannelGroup?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        group = notificationManager.getNotificationChannelGroup(id)
    } else {
        group = notificationManager.notificationChannelGroups.find { it.id == id }
    }
    return group != null
}

private fun doesNotificationChannelExist(id: String?, notificationManager: NotificationManager) =   notificationManager.getNotificationChannel(id) != null

private fun migrateChannelConfig(notificationManager: NotificationManager) {
    val channel = notificationManager.getNotificationChannel(NotificationConstants
            .NOTIFICATION_DEFAULT_CHANNEL_ID)
    if (channel.name == NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_NAME) {
        return
    }
    channel.name = NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_NAME
    notificationManager.createNotificationChannel(channel)
}


fun createCustomChannelIfNotExist(channel: String) {
    Logger.d(LOG_TAG, "Creating channel")
    val notificationManager = CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (doesNotificationChannelExist(channel, notificationManager)) {
        migrateCustomChannelConfig(channel, notificationManager)
        return
    }
    val notificationChannel = NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_DEFAULT)
    notificationChannel.setSound(null, null)
    notificationManager.createNotificationChannel(notificationChannel)
    SpecialChannelHelper.addSpecialChannel(channel)
}

fun createCustomChannelIfNotExist(channel: String, importance: Int = NotificationManager.IMPORTANCE_DEFAULT) {
    Logger.d(LOG_TAG, "Creating channel")
    val notificationManager = CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (doesNotificationChannelExist(channel, notificationManager)) {
        migrateCustomChannelConfig(channel, notificationManager)
    }
    val notificationChannel = NotificationChannel(channel, channel, importance)
    notificationChannel.setSound(null, null)
    notificationManager.createNotificationChannel(notificationChannel)
}

private fun migrateCustomChannelConfig(id: String, notificationManager: NotificationManager) {
    val channel = notificationManager.getNotificationChannel(id)
    if (channel.name == id) {
        return
    }
    channel.name = id
    notificationManager.createNotificationChannel(channel)
}

fun getChannelImportantceEnumValue(importantance: Int): ChannelImportantance {
    return when (importantance) {
        NotificationManager.IMPORTANCE_MAX -> ChannelImportantance.MAX
        NotificationManager.IMPORTANCE_HIGH -> ChannelImportantance.HIGH
        NotificationManager.IMPORTANCE_DEFAULT -> ChannelImportantance.DEFAULT
        NotificationManager.IMPORTANCE_LOW -> ChannelImportantance.LOW
        NotificationManager.IMPORTANCE_MIN -> ChannelImportantance.MIN
        else -> ChannelImportantance.NONE
    }
}

fun getChannelImportantceIntValue(importantance: ChannelImportantance?): Int {
    return when (importantance) {
        ChannelImportantance.MAX -> NotificationManager.IMPORTANCE_MAX
        ChannelImportantance.HIGH -> NotificationManager.IMPORTANCE_HIGH
        ChannelImportantance.DEFAULT -> NotificationManager.IMPORTANCE_DEFAULT
        ChannelImportantance.LOW -> NotificationManager.IMPORTANCE_LOW
        ChannelImportantance.MIN -> NotificationManager.IMPORTANCE_MIN
        else -> NotificationManager.IMPORTANCE_NONE
    }
}

fun getChannelImportantceIntValue(enabled: Boolean): Int {
    return if (enabled) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_NONE
}

fun selectChannelId(postAnalyticsLog: Boolean,
                    baseModel: BaseModel,
                    context: Context): String {
    val receivedChannelId = baseModel.baseInfo.channelId
    val receivedGroupId = baseModel.baseInfo.channelGroupId
    val notificationId = baseModel.baseInfo.id
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var selectedChannel = NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID
    var selectedGroup: String? = Constants.EMPTY_STRING
    if (!CommonUtils.isEmpty(receivedChannelId)) {
        val channel = notificationManager.getNotificationChannel(receivedChannelId)
        if (channel != null) {
            if (postAnalyticsLog && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                logNotificationChannelDisabled(receivedChannelId!!,
                        notificationId)
            }
            selectedChannel = receivedChannelId!!
            selectedGroup = channel.group
        } else {
            val defaultChannel = notificationManager.getNotificationChannel(NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID)
            if (postAnalyticsLog &&
                    (defaultChannel == null ||
                            defaultChannel.importance == NotificationManager.IMPORTANCE_NONE)) {
                logNotificationChannelMissing(
                        receivedChannelId!!, notificationId)
                createTempChannel(receivedChannelId, receivedGroupId)
                NotificationChannelUsecaseController().requestNotificationChannelData()
                selectedChannel = receivedChannelId
            }
        }
    }
    logForMissingNotification(selectedChannel, selectedGroup, notificationManager,
            context, baseModel)
    return selectedChannel
}

fun logForMissingNotification(channelId: String, groupId: String?,
                              notificationManager: NotificationManager, context: Context, baseModel: BaseModel) {
    val notificationManagerCompat = NotificationManagerCompat.from(context)
    if (!notificationManagerCompat.areNotificationsEnabled()) {
        return
    }
    if (!CommonUtils.isEmpty(groupId) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val group = notificationManager.getNotificationChannelGroup(groupId)
        if (group != null && group.isBlocked) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(baseModel,
                    NotificationFilterType.NOTIFICATION_GROUP_DISABLED)
            return
        }
    }
    val channel = notificationManager.getNotificationChannel(channelId)
    if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
        NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(baseModel,
                NotificationFilterType.NOTIFICATION_CHANNEL_DISABLED)
    }
}

//priority to be NotificationManager.IMPORTANCE_HIGH etc
fun createPostChannelIfNotExist(channelName: String, priority: Int) {
    val notificationManager =
        CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (doesNotificationChannelExist(channelName, notificationManager)) {
        migrateCustomChannelConfig(channelName, notificationManager)
        return
    }
    val notificationChannel = NotificationChannel(
        channelName, channelName, priority
    )
    notificationChannel.setSound(null, null)
    notificationManager.createNotificationChannel(notificationChannel)
}

fun getChannelId(channelId: String?): String?{
    val notificationManager = CommonUtils.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return if(!CommonUtils.isEmpty(channelId) && doesNotificationChannelExist(channelId, notificationManager)){
        channelId
    }else{
        if(doesNotificationChannelExist(NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID, notificationManager)){
            NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID
        }else{
            null
        }
    }
}