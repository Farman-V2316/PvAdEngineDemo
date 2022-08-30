/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.entity

import com.newshunt.common.helper.common.Constants

/**
 * @author Amitkumar
 */
data class NotificationChannelInfo(val name: String?,
                                   val id: String,
                                   val description: String?,
                                   val showBadge: Boolean?,
                                   val makeSound: Boolean?,
                                   val enableLights: Boolean?,
                                   val enableVibration: Boolean?,
                                   val viberationPattern: Array<Long>?,
                                   val lightColor: String?,
                                   val soundUrl: String?,
                                   val groupId: String?,
                                   val priority: ChannelImportantance?,
        /*
        * To know whether this channel was created or updated during
        * logging analytics event
        * */
                                   @Transient val changeType: Int = 0) {


    constructor(id: String, priority: ChannelImportantance?, groupId: String?, changeType: Int) : this(null,
            id,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            groupId,
            priority, changeType)
}

data class NotificationChannelGroupInfo(val name: String? = null,
                                        val id: String = Constants.EMPTY_STRING,
                                        val description: String? = null,
                                        val channelItems: List<NotificationChannelInfo>? = null,
                                        @Transient val enabled: Boolean = true,
                                        @Transient val changeType: Int) {
    constructor(id: String, enabled: Boolean, changeType: Int) : this(null, id, null, null,
            enabled, changeType)
}

data class NotificationChannelResponse(val channelGroups: List<NotificationChannelGroupInfo>?,
                                       val otherChannelGroups: List<NotificationChannelGroupInfo>?,
                                       val impChannelGroups: List<NotificationChannelGroupInfo>?,
                                       val stickyChannelGroups: List<NotificationChannelGroupInfo>?,
                                       val version: String)


class ChannelNotFoundException(channel: String) : Exception("Notification channel $channel not found")

data class NotificationChannelGroupPair(val channelId: String, val groupId: String)

enum class ChannelImportantance {
    MAX, HIGH, DEFAULT, LOW, MIN, NONE
}