/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.helper

import android.content.Context

import com.dailyhunt.tv.ima.playerholder.ContentPlayerHolder
import com.newshunt.adengine.model.entity.BaseAdEntity

/**
 * Manages mediaplayer states as per IMA events and view visibility.
 *
 * @author raunak.yadav
 */
interface ImaPlayerManager {
    fun handleMediaOnAdUpdate(baseAdEntity: BaseAdEntity): Boolean

    fun setupDHMediaView(adPlayerHolder: ContentPlayerHolder, context: Context): Boolean

    fun startPlayingAd()

    fun resumeAdIfAllowed()

    fun onPlayerInView()

    fun onPlayerOutOfView()

    fun canAutoPlay(): Boolean
}
