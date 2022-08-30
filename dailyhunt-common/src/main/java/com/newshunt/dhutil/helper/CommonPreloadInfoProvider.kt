/*
 *
 *  * Copyright (c) 2018 Newshunt. All rights reserved.
 * /
 */
package com.newshunt.dhutil.helper

import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
* Protocol here:
* 1. If user activates preloaded APK, directly return the install source from the APK
* 2. If user does not activate but updates the APK, if preloaded, return the preloaded source if
* present in the device binary
* 3. If user does not activate but uninstalls and reinstalls, return the preloaded source if
* present in the device binary
*
* Created by srikanth.ramaswamy on 08/27/2018
*/
abstract class CommonPreloadInfoProvider : PreloadInfoProvider {
    override fun getInstallSource(): String {
        val preloadSrc = getPreloadedSource()
        //If the preloaded source is available, use it as is else return the default utm source
        return if (!CommonUtils.isEmpty(preloadSrc)) preloadSrc else AppConfig.getInstance()!!.defaultUtmSource
    }

    /**
     * OEM specific method to be overridden with logic to read the preload source String
     */
    abstract fun getPreloadedSource(): String
}