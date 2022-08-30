/*
 *
 *  * Copyright (c) 2018 Newshunt. All rights reserved.
 * /
 */

package com.newshunt.dhutil.helper

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.FileUtil
import com.newshunt.common.helper.common.Logger

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
private const val OPPO_ATTRIB_FILE = "data/etc/appchannel/dailyhunt_preburn_oppo.txt"

class OppoPreloadInfoProvider : CommonPreloadInfoProvider() {
    override fun getPreloadedSource(): String {
        var preloadedSource = Constants.EMPTY_STRING
        try {
            preloadedSource = FileUtil.readStringFromFile(OPPO_ATTRIB_FILE).trim()
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        } finally {
            return preloadedSource
        }
    }
}