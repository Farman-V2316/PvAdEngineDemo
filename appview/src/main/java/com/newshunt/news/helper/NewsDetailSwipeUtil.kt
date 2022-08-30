/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.os.Bundle
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.news.util.NewsConstants
import com.newshunt.pref.NewsPreference

/**
 * Utility functions related to newsdetail swipeUrl
 *
 * @author satosh.dhanymaraju
 */

fun autoFetchSwipeUrl(extras: Bundle?): Boolean {
    val disabledInHandshake = PreferenceManager.getPreference(NewsPreference
    .DONOT_AUTOFETCH_SWIPEURL, false)?:false
    val disabledInPayload = extras?.getBoolean(NewsConstants
            .BUNDLE_NOTF_DONOT_AUTO_FETCH_SWIPEURL, false) ?: false
    return !disabledInHandshake && !disabledInPayload
}