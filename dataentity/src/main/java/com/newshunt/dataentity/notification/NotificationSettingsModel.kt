/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

import android.net.Uri
import java.io.Serializable

/**
 * Created by kajal.kumari on 18/05/22.
 */
class NotificationSettingsModel (private val deeplink: String) : BaseModel(), Serializable {
    val uri = kotlin.runCatching { Uri.parse(deeplink) }.getOrNull()
    val referrer = uri?.getQueryParameter("referrer")
    val referrerId = uri?.getQueryParameter("referrerId")

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.NOTIFICATION_SETTINGS
    }

    companion object {
        @JvmStatic private val serialVersionUID = 1L
    }
}