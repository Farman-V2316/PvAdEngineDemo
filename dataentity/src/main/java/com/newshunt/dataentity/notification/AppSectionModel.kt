/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

import android.net.Uri
import com.newshunt.common.helper.common.Constants
import java.io.Serializable

/**
 * Created by kajal.kumari on 02/05/22.
 */
class AppSectionModel (private val deeplink: String) : BaseModel(), Serializable {
    val uri = kotlin.runCatching { Uri.parse(deeplink) }.getOrNull()
    val referrer = uri?.getQueryParameter("referrer")
    val referrerId = uri?.getQueryParameter("referrerId")
    var appSectionId = Constants.EMPTY_STRING

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.APP_SECTION_MODEL;
    }

    companion object {
        @JvmStatic private val serialVersionUID = 1L
    }
}