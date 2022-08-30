/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.helper

import com.newshunt.adengine.util.AdsUtil

/**
 * This class maintains the swipe counts to show pgi ads.
 *
 * @author raunak.yadav
 */
class SwipeCounter {
    // swipe count will be persisted across sessions
    var swipeCount: Int = 0
        set(value) {
            field = value
            AdsUtil.saveSwipeCount(value)

        }
    // swipe count in current session
    var sessionSwipeCount: Int = 0

    init {
        //If App launch count is more than minimum session count to start persisting swipe count
        // across session, we will start persisting and using swipe count across sessions
        swipeCount = if (AdsUtil.minSessionsToPersistSwipeCount != -1 &&
                AdsUtil.appLaunchCount > AdsUtil.minSessionsToPersistSwipeCount) {
            AdsUtil.savedSwipeCount
        } else {
            0
        }
    }

    fun resetSwipeCount() {
        swipeCount = 0
        sessionSwipeCount = 0
    }

    fun incrementSwipeCount() {
        swipeCount++
        sessionSwipeCount++
    }
}
