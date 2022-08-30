/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.instream

import com.newshunt.common.helper.common.Logger

/**
 * Logger for Instream Ads
 *
 * @author vinod.bc
 */

object IAdLogger {

    private const val pattern = "############### - >"
    private val ENABLED = true

    fun d(aTag: String, aMessage: String) {
        if (ENABLED) {
            Logger.d(aTag, pattern + aMessage)
        }
    }

    fun w(aTag: String, aMessage: String) {
        if (ENABLED) {
            Logger.w(aTag, pattern + aMessage)
        }
    }

    fun e(aTag: String, aMessage: String) {
        if (ENABLED) {
            Logger.e(aTag, pattern + aMessage)
        }
    }

    fun i(aTag: String, aMessage: String) {
        if (ENABLED) {
            Logger.i(aTag, pattern + aMessage)
        }
    }
}
