/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.model.entity.players

import com.newshunt.helper.player.AutoPlayManager

/**
 * Views that have autoplaying elements.
 *
 * @author raunak.yadav
 */
interface AutoPlayable {

    val asset: Any?

    fun setAutoPlayManager(autoPlayManager: AutoPlayManager?)

    fun getAutoplayPriority(fresh: Boolean): Int

    fun play()

    fun pause()

    fun canRelease(): Boolean

    fun releaseVideo()

    fun getVisibilityPercentage(): Int

    fun getPositionInList(): Int

    fun resetVideoState()

    fun isPLaying(): Boolean {
        return false
    }
}

/**
 * Autoplay for some Ads SDK-owned mediaViews cannot be controlled.
 * Calling play/pause has no effect.
 * Observed that they too have some minimum criteria to start autoplay.
 * This percent will come per-sdk via Ads handshake.
 */
interface StubbornPlayable : AutoPlayable {

    override fun play() {}

    override fun pause() {}

    override fun canRelease(): Boolean {
        return false
    }

    override fun releaseVideo() {}

    override fun resetVideoState() {}

    override fun isPLaying(): Boolean {
        return false
    }
}