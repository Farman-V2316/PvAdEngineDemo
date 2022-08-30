/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.customviews

import android.view.View
import android.view.ViewGroup
import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.dailyhunt.tv.players.interfaces.PlayerViewDH
import com.google.android.exoplayer2.SimpleExoPlayer
import com.newshunt.analytics.helper.ReferrerProvider
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection

/**
 * @author shrikant.agrawal
 */
interface VideoPlayerWrapper : PlayerViewDH {

    fun removeFromParent()

    fun setPageReferrer(pageReferrer: PageReferrer?)

    fun setLayoutParamsForWrapper(layoutParams: ViewGroup.LayoutParams)

    fun resetCallbacks(viewCallback: PlayerCallbacks?, referrerProvider: ReferrerProvider?)

    fun getPlayerCallbacks() : PlayerCallbacks? {
        return null
    }

    fun onAttachToNewsList()

    fun onAttachToBuzzDetail()

    fun getPlayerView() : View

    fun setPlayerMuteStatus(isMute: Boolean, isUserAction: Boolean, isFromSettings: Boolean = false)

    fun getParentView() : ViewGroup?

    fun handleBackPress() : Boolean

    fun getReferrerProvider() : ReferrerProvider?

    fun setEventSection(eventSection: NhAnalyticsEventSection)

    fun setVideoTimeListener(videoTimeListener: VideoTimeListener?)

    fun getPlayer() : SimpleExoPlayer?

    fun getAutoplayVideoId(): String {
        return ""
    }
}

interface VideoWrapperPlayCallbacks {

    fun onDisplayClick()

    fun onRequestChangeOrientation(orientation : Int)

}

interface VideoHelperCallbacks {

    fun onVideoReady()

    fun onLoadError()
}