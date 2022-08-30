package com.newshunt.dataentity.notification

/**
 * Created by priya.gupta on 14/10/2020
 */
data class LocalNavModel(val deepLinkUrl: String? = null) : BaseModel() {

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.LOCAL_MODEL
    }
}