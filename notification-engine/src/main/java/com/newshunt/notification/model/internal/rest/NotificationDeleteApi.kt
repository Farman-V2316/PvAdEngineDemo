package com.newshunt.notification.model.internal.rest

import com.newshunt.notification.model.entity.DeleteNotificationPayload
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationDeleteApi {
    @POST("/api/obelix/delete_notification/{cid}")
    fun deleteNotifications(@Body requestPayload: DeleteNotificationPayload, @Path("cid") cid: String):
            Observable<Response<Void>>
}
