package com.newshunt.dataentity.notification

data class CreatePostNavModel(val deepLinkUrl: String? = null) : BaseModel() {

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.CREATE_POST_MODEL
    }
}