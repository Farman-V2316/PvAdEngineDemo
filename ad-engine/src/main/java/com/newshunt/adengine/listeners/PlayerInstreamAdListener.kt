/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.listeners

import android.view.View
import android.view.ViewGroup
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.android.exoplayer2.ui.PlayerView
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity

interface PlayerInstreamAdListener {

    fun onAdStarted(ad: Ad?, baseDisplayAdEntity: BaseDisplayAdEntity?,
                    companionAdLoaded: Boolean = false)
    fun onAdTapped() {}
    fun onAdComplete()
    fun onAdSkipped() {}
    fun onAdPaused()
    fun onAdResumed()
    fun onAdError(message: String?)
    fun onAllAdComplete()
    fun getAdViewGroup(): ViewGroup
    fun getAdOverlayViews(): Array<View>?
    fun getExoPlayerView(): PlayerView?
}