/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.common.helper.common

/**
 * For logging dev_event; required because this module has no dependency on analytics.
 * Will be instantiated by app controller
 * @author satosh.dhanyamraju
 */
interface PasswordEncDevEventLogger {
    fun logEvent(errrorMessage: String,
                 key: String,
                 keyVersion: String)
}