package com.newshunt.notification.model.entity.server

import com.google.gson.JsonArray
import com.newshunt.dataentity.notification.DeeplinkModel

/**
 * Created by karthik.r on 2019-11-07.
 */
data class NotificationSyncResponse(val notifications: JsonArray? = null,
                                    val first_marker: String? = null,
                                    val last_marker: String? = null)

data class NotificationModel(val data: String)

data class NotificationData(val message_v3: DeeplinkModel, val type: String, val version: String)
