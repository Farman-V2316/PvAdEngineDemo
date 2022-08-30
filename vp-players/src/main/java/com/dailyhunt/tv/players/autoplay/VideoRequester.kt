/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 * */
package com.dailyhunt.tv.players.autoplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.helper.player.PlayerControlHelper

/**
 * @author shrikant.agrawal
 * Class to manage video service managers for different tabs
 */
class VideoRequester(val screenId: Int) : BroadcastReceiver(), LifecycleObserver {

    val exoPlayerCacheHelper = ExoPlayerCacheHelper2()
    val webPlayerCacheHelper = WebPlayerCacheHelper2()
    private var pageEntity: PageEntity? = null

    companion object {
        const val VIDEO_DEBUG = "VideoDebug"
        const val TAG = "VideoRequester"
    }

    override fun onReceive(p0: Context?, intent: Intent?) {
        Logger.i(TAG, "Received the intent for the audio started")
        // handle the intent
        if (intent != null) {
            // change the state of the video to mute
            PlayerControlHelper.isListMuteMode = true
            BusProvider.getUIBusInstance().post(PlaySettingsChangedEvent(PlayerControlHelper.isListMuteMode))
        }
    }

    fun setPageEntity(pageEntity: PageEntity?){
        this.pageEntity = pageEntity
    }

    fun getPageEntity(): PageEntity? {
        return this.pageEntity
    }

    fun clearPlayerInstances() {
        exoPlayerCacheHelper.clearPlayerInstances()
        webPlayerCacheHelper.clearPlayerInstances()
    }


}