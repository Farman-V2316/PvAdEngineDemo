/*
 * Copyright (c) 2020  Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification

import java.io.Serializable

/**
 * created by mukesh.yadav on 07/06/20
 */
data class PermissionNavModel(val url: String? = null,
                              val image: String? = null,
                              val heading: String? = null,
                              val subHeading: String? = null,
                              val ctaAllow: String? = null,
                              val ctaDeny: String? = null,
                              val landingPage: String? = null,
                              val permissions: ArrayList<Permission>? = null,
                              val successTrackers: ArrayList<String>? = null) : BaseModel(), Serializable {

    override fun getBaseModelType(): BaseModelType? {
        return BaseModelType.RUNTIME_PERMISSIONS
    }

}

data class Permission(val name: String, val required: Boolean) : Serializable