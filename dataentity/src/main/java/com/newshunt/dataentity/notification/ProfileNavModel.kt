/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

/**
 * @author santhosh.kc
 */
data class ProfileNavModel(var userId: String? = null, var tabType: String? = null,
                           var userHandle: String? = null,var defaultTabId:String? = null) :
        BaseModel() {

    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.PROFILE_MODEL
    }
}