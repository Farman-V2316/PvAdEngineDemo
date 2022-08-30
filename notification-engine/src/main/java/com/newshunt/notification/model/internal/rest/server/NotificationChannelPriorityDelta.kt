/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.rest.server

import com.newshunt.notification.model.entity.NotificationChannelGroupInfo
import com.newshunt.notification.model.entity.NotificationChannelInfo

/**
 * @author Amitkumar
 */
class NotificationChannelPriorityDelta(val addedGroupInfo: List<NotificationChannelGroupInfo>,
                                       val removedGroupInfo: List<NotificationChannelGroupInfo>,
                                       val addedChannels: List<NotificationChannelInfo>,
                                       val removedChannels: List<NotificationChannelInfo>)


data class ConfigUpdatePayload(val addedGroup: List<String>,
                               val removedGroup: List<String>,
                               val addedChannels: List<NotificationChannelInfo>,
                               val removedChannels: List<NotificationChannelInfo>,
                               val systemNotificationEnabled: Boolean,
                               val appNotificationEnabled: Boolean) {

    constructor(delta: NotificationChannelPriorityDelta,
                systemNotificationEnabled: Boolean,
                appNotificationEnabled: Boolean) :
            this(delta.addedGroupInfo.map { it.id },
                    delta.removedGroupInfo.map { it.id },
                    delta.addedChannels.map { NotificationChannelInfo(it.id, it.priority, it.groupId, it.changeType) },
                    delta.removedChannels.map { NotificationChannelInfo(it.id, it.priority, it.groupId, it.changeType) },
                    systemNotificationEnabled, appNotificationEnabled)
}