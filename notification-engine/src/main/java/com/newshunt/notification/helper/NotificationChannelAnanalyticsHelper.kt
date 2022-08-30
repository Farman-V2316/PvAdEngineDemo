/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.notification.analytics.NhAnalyticsNotificationEventParam
import com.newshunt.notification.analytics.NhNotificationEvent
import com.newshunt.notification.model.entity.ChannelImportantance

/**
 * @author Amitkumar
 */
fun logNotificationChannelMissing(channelId: String, notificationId: String) {
    val map = mapOf<NhAnalyticsEventParam, Any>(
            NhAnalyticsNotificationEventParam.CHANNEL_ID to channelId,
            NhAnalyticsNotificationEventParam.NOTIFICATION_ID to notificationId)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_CHANNEL_MISSING,
            NhAnalyticsEventSection.NOTIFICATION, map)
}

fun logNotificationChannelDisabled(channelId: String, notificationId: String) {
    val map = mapOf<NhAnalyticsEventParam, Any>(
            NhAnalyticsNotificationEventParam.CHANNEL_ID to channelId,
            NhAnalyticsNotificationEventParam.NOTIFICATION_ID to notificationId)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_CHANNEL_DISABLED,
            NhAnalyticsEventSection.NOTIFICATION, map)
}


fun logNotificationChannelStateChange(channelId: String, priority: ChannelImportantance) {
    val map = mapOf<NhAnalyticsEventParam, Any>(
            NhAnalyticsNotificationEventParam.CHANNEL_ID to channelId,
            NhAnalyticsNotificationEventParam.PRIORITY to priority)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_CHANNEL_STATE_CHANGE,
            NhAnalyticsEventSection.NOTIFICATION, map)
}

fun logNotificationGroupStateChange(groupId: String, enabled: Boolean) {
    val map = mapOf<NhAnalyticsEventParam, Any>(
            NhAnalyticsNotificationEventParam.GROUP_ID to groupId,
            NhAnalyticsNotificationEventParam.STATE_ENABLED to enabled)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_GROUP_STATE_CHANGE,
            NhAnalyticsEventSection.NOTIFICATION, map)
}

fun logNotificationChannelCreated(channelId: String, channelName: String?, priority:
ChannelImportantance) {
    val map = mapOf<NhAnalyticsEventParam, Any?>(
            NhAnalyticsNotificationEventParam.CHANNEL_ID to channelId,
            NhAnalyticsNotificationEventParam.CHANNEL_NAME to channelName,
            NhAnalyticsNotificationEventParam.PRIORITY to priority)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_CHANNEL_CREATED,
            NhAnalyticsEventSection.NOTIFICATION, map)
}

fun logNotificationGroupCreated(groupId: String, groupName: String?) {
    val map = mapOf<NhAnalyticsEventParam, Any?>(
            NhAnalyticsNotificationEventParam.GROUP_ID to groupId,
            NhAnalyticsNotificationEventParam.GROUP_NAME to groupName)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_GROUP_CREATED,
            NhAnalyticsEventSection.NOTIFICATION, map)
}

fun logNotificationChannelDeleted(channelId: String, channelName: String?) {
    val map = mapOf<NhAnalyticsEventParam, Any?>(
            NhAnalyticsNotificationEventParam.CHANNEL_ID to channelId,
            NhAnalyticsNotificationEventParam.CHANNEL_NAME to channelName)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_CHANNEL_DELETED,
            NhAnalyticsEventSection.NOTIFICATION, map)
}

fun logNotificationGroupDeleted(groupId: String, groupName: String?) {
    val map = mapOf<NhAnalyticsEventParam, Any?>(
            NhAnalyticsNotificationEventParam.GROUP_ID to groupId,
            NhAnalyticsNotificationEventParam.GROUP_NAME to groupName)
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_GROUP_DELETED,
            NhAnalyticsEventSection.NOTIFICATION, map)
}
