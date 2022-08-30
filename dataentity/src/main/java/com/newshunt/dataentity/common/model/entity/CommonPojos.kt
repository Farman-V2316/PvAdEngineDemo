/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.io.Serializable

/**
 * A common file to declare POJOs
 * Created by srikanth.ramaswamy on 08/26/2019.
 */

enum class SyncStatus {
    SYNCED, UN_SYNCED, IN_PROGRESS, MARKED;

    companion object {

        fun from(statusStr: String?): SyncStatus {
            statusStr ?: UN_SYNCED
            values().forEach {
                if (CommonUtils.equals(statusStr, it.name)) {
                    return it
                }
            }
            return UN_SYNCED
        }

        fun valuesAsList(): List<SyncStatus> {
            val arrayList = ArrayList<SyncStatus>()
            values().forEach { arrayList.add(it) }
            return arrayList
        }
    }
}

enum class ListTransformType(val prefix: String) : Serializable {
    DEFAULT(Constants.EMPTY_STRING),
    PROFILE_ACTIVITIES("ACTIVITY_"),
    PROFILE_SAVED_CAROUSEL("SAVED_"),
    PROFILE_SAVED("SAVED_");
}

enum class ImportContactsEvents {
    IMPORT_CONTACT_SKIP,
    SIGN_IN_SKIP,
    CONTACT_PERMISSION_ALLOWED
}

/**
 * A simple event which can be fired to tell whether a permission is granted or denied
 */
data class PermissionEvent(val permissionString: String, val isGranted: Boolean)