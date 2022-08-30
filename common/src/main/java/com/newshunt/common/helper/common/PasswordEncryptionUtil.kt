/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.common.helper.common
/**
* constants and state holder
* @author satosh.dhanyamraju
*/
object PasswordEncryptionUtil {
    const val DEV_CUSTOM_ERROR_PARAM_ERROR = "error"
    const val DEV_CUSTOM_ERROR_PARAM_KEY = "key"
    const val DEV_CUSTOM_ERROR_PARAM_KEY_VERSION = "key_version"
    @JvmStatic
    var logger: PasswordEncDevEventLogger? = null;
}