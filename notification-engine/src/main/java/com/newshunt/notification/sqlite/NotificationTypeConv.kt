/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.sqlite

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.notification.InAppNotificationInfo

/**
 * @author aman.roy
 */
class NotificationTypeConv {
    @TypeConverter
    fun listToString(tags : List<String>?) : String = JsonUtils.toJson(tags)

    @TypeConverter
    fun stringtoList(tags : String) : List<String> {
        return JsonUtils.fromJson<List<String>>(
                tags, object : TypeToken<List<String>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toInAppConfig(data: String) : InAppNotificationInfo? {
        return JsonUtils.fromJson(data, InAppNotificationInfo::class.java)
    }

    @TypeConverter
    fun fromInAppConfig(inAppNotificationInfo: InAppNotificationInfo?) : String {
        return JsonUtils.toJson(inAppNotificationInfo)
    }
}
