/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.listeners

import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction

/**
 * Created on 30/10/2019.
 */
interface DHPlaybackControlListener {
    fun setVideoEndAction(endAction: PlayerVideoEndAction)
    fun setVideoStartAction(startAction: PlayerVideoStartAction)
}