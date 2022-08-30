/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model

import com.newshunt.common.helper.common.Logger

/**
 * Different behaviors of PlayerView tap.
 * To be checked in sequence till an action can execute.
 *
 * @author raunak.yadav
 */
class PlayerViewTapHandler(private val listener: TapListener) {
    private val actions = listOf(ClickAction.IMMERSIVE,
        ClickAction.LANDING_PAGE,
        ClickAction.SDK_CLICK,
        ClickAction.PLAY_PAUSE)

    fun handleTap() {
        actions.forEach {
            Logger.d("ImaAdHelper", "Processing tap for :$it")
            if (listener.executeTapIfAllowed(it)) {
                return
            }
        }
    }
}

interface TapListener {
    fun executeTapIfAllowed(action: ClickAction): Boolean
}

enum class ClickAction {
    IMMERSIVE, LANDING_PAGE, SDK_CLICK, PLAY_PAUSE
}