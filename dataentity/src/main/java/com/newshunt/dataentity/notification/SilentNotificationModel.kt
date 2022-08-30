/*
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

/**
 * @author  atul.anand on 23/10/21.
 */
data class SilentNotificationModel(val refreshInterval: Int, val url: String, val metaUrl: String, val forceShow: Boolean, val langFilter: String): BaseModel(){

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.SILENT_MODEL
    }
}