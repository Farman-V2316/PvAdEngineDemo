/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.view

import com.newshunt.adengine.listeners.PlayerInstreamAdListener

/**
 * @author raunak.yadav
 */
interface InstreamAdWrapper {

    fun isValid(): Boolean

    fun startPlayingAd()

    fun setVisibility(visible: Boolean) {}

    fun setAdStateListener(listener: PlayerInstreamAdListener?) {}

    fun setLayoutParams(width: Int, height: Int) {}

    fun rotate(angle: Float) {}

    fun destroy() {}
}