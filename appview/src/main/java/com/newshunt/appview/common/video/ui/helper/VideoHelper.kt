package com.newshunt.appview.common.video.ui.helper

import androidx.lifecycle.MutableLiveData
import com.dailyhunt.tv.players.entity.PLAYER_STATE

object VideoHelper {
    val videoStateLiveData = MutableLiveData<PlayerState>()

    val menuStateLiveData = MutableLiveData<MenuState>()

    val handleBackPressState = MutableLiveData<Int>()

    val topFragmentId = MutableLiveData<Int>()

    var timeSinceWebPlayer : Long = 0L

    fun getDelayBasedOnLastPlayed() : Long {
        if(timeSinceWebPlayer <= 0L) return 500L

        return if(System.currentTimeMillis() - timeSinceWebPlayer < 500L) 600L else 500L
    }
}

class MenuState(val isShowing: Boolean, val isHideCard: Boolean) {}

class PlayerState(val state: PLAYER_STATE, val id: String?){}

class NetworkRetry(val userRetry : Boolean) {}
