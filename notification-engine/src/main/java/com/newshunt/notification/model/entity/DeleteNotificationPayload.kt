package com.newshunt.notification.model.entity

data class DeleteNotificationPayload(
        val cid: String,
        val notifications: List<NotificationId>
)

data class NotificationId(
        val id: String,
        val ts: Long = 0
)