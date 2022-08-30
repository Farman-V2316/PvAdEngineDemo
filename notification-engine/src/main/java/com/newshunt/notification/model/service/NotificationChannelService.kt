/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.service

import com.newshunt.notification.model.entity.NotificationChannelResponse
import com.newshunt.notification.model.internal.rest.server.NotificationChannelPriorityDelta
import io.reactivex.Observable

/**
 * @author Amitkumar
 */
interface NotificationChannelService {
    fun requestChannelInfo(baseUrl:String)
            : Observable<NotificationChannelResponse?>

    fun updateChannelPriorityConfig(priorityConfig: NotificationChannelPriorityDelta,
                                    appNotificationEnabled: Boolean,
                                    systemNotificationEnabled: Boolean)
            : Observable<NotificationChannelPriorityDelta>
}