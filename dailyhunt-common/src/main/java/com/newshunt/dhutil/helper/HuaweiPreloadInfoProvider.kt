/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger

const val HUAWEI_BUILD_PROP = "ro.trackingId.com.eterno"

/**
 * Protocol here:
 * 1. If user activates preloaded APK, directly return the install source from the APK
 * 2. If user does not activate but updates the APK, if preloaded, return the preloaded source if
 * present in the device binary
 * 3. If user does not activate but uninstalls and reinstalls, return the preloaded source if
 * present in the device binary
 *
 * Created by srikanth.ramaswamy on 07/05/2018.
 */
class HuaweiPreloadInfoProvider : CommonPreloadInfoProvider() {

    override fun getPreloadedSource(): String {
        var preloadSrc: String = Constants.EMPTY_STRING
        try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            preloadSrc = systemProperties.getMethod("get", String::class.java).invoke(systemProperties,
                    HUAWEI_BUILD_PROP) as? String? ?: Constants.EMPTY_STRING
        } catch (e: Throwable) {
            Logger.caughtException(e)
        }
        return preloadSrc
    }
}