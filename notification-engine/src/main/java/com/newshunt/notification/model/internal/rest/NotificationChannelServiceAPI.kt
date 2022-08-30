/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.notification.model.entity.NotificationChannelResponse
import com.newshunt.notification.model.internal.rest.server.ConfigUpdatePayload
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * @author Amitkumar
 */
interface NotificationChannelServiceAPI {
    @GET("/notification-pull/v2/getTrayChannels")
    fun requestNotificationsChannels():
            Observable<ApiResponse<NotificationChannelResponse>>

    @POST("/api/v2/notification/channel/update")
    fun updateNotificationsChannelsPriority(
            @Body priorityConfig: ConfigUpdatePayload):
            Observable<Response<Void>>
}