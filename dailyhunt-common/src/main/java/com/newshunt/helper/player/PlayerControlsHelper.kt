/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.helper.player

/**
 * Maintains global mute mode for Ads + Content videos for List(AutoPlay) and Detail Screen
 *
 * @author raunak.yadav
 */
object PlayerControlHelper {
    @JvmStatic
    var isListMuteMode = true
        set(value) {
            field = value
            if (!value) {
                //Honor List(AutoPlay) Unmute Mode to Detail
                isDetailMuteMode = false
            }
        }

    @JvmStatic
    var isDetailMuteMode = false
        get() {
            return if (isImmersiveMuteMode) {
                isImmersiveMuteMode
            } else {
                field
            }
        }
        set(value) {
            field = value
            if (value) {
                //Honor Detail Mute Mode to List(AutoPlay)
                isListMuteMode = true
            } else if (isImmersiveMuteMode) {
                //User Unmuted, hence clear immersiveMuteMode
                isImmersiveMuteMode = false
            }
        }

    /**
     * Mute mode on immersive view
     */
    var isImmersiveMuteMode = false

    /**
     * Toggle the current mute state of List.
     *
     * @return new mute state
     */
    fun toggleMute(): Boolean {
        isListMuteMode = !isListMuteMode
        if (!isListMuteMode) {
            //Honor List(AutoPlay) Unmute Mode to Detail
            isDetailMuteMode = false
        }
        return isListMuteMode
    }

}

interface PlaySettingsListener {
    fun onPlaySettingsChanged(event: PlaySettingsChangedEvent)
}

data class PlaySettingsChangedEvent(val isMute: Boolean, val id: String = "")