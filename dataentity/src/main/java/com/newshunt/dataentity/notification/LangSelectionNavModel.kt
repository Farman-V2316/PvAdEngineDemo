/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification

import android.net.Uri
import java.io.Serializable

/**
 * @author satosh.dhanyamraju
 */
class LangSelectionNavModel(private val deeplink: String) : BaseModel(), Serializable {
    val uri = kotlin.runCatching { Uri.parse(deeplink) }.getOrNull()
    val referrer = uri?.getQueryParameter("referrer")
    val referrerId = uri?.getQueryParameter("referrerId")

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.LANG_SELECTION;
    }
    companion object {
        @JvmStatic private val serialVersionUID = 1
    }
}