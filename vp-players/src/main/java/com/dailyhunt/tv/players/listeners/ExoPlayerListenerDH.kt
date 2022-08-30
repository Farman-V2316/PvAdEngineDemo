/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.listeners

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

interface ExoPlayerListenerDH {

    fun setPlayer(exoPlayer: SimpleExoPlayer)
    fun getExoPlayerView(): PlayerView?
    fun onVideoError(exception: ExoPlaybackException?)
    fun logVideoError(exception: ExoPlaybackException?)

    /**
     * Non tag ads are the ads with sdk-owned-view
     * that do not provide the tag URL
     */
    fun showOverlayViewIfNotVisible()

    fun isViewInForeground(): Boolean
    fun onTimeUpdate(position: Long)

    fun onRenderedFirstFrame()
}