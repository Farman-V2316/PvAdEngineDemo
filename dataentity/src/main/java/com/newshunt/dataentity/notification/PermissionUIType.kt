/*
 * Copyright (c) 2020  Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification

import java.io.Serializable

/**
 * created by mukesh.yadav on 08/06/20
 *
 * this field decide weather to show permission dialog directly or
 * show educate user page {@link RuntimePermissionActivity} with all UI element)
 * this field coresponds to "landingPage" in Permission deeplink {@link PermissionNavModel}
 */
enum class PermissionUIType : Serializable {
    INTERIM,
    FINAL
}