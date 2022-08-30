/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.app.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * @author anshul.jain
 * A class for managing audio in the app.
 */
class DhAudioManager(private val dhAudioFocusManagerInterface: DhAudioFocusManagerInterface? =
                             null) : AudioManager.OnAudioFocusChangeListener {

    private var audioManager = CommonUtils.getApplication().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private var request: AudioFocusRequest? = null

    fun getAurReleaseAudioFocus(getFocus: Boolean) {
        if (getFocus) getAudioFocusRequest() else releaseAudioFocus()
    }

    fun releaseAudioFocus() {
        request?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    fun getAudioFocusRequest() {
        var focusRequest = AudioManager.AUDIOFOCUS_NONE
        request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes).setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this).build()
        request?.let {
            focusRequest = audioManager.requestAudioFocus(it)
        }
        if (focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            dhAudioFocusManagerInterface?.onAudioFocusRequestGranted()
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> dhAudioFocusManagerInterface?.onAudioFocusGained()
            AudioManager.AUDIOFOCUS_LOSS -> dhAudioFocusManagerInterface?.onAudioFocusLost()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> dhAudioFocusManagerInterface?.onAudioFocusLostTransient()
            else -> {
            }
        }
    }
}

interface DhAudioFocusManagerInterface {

    fun onAudioFocusRequestGranted() {}

    fun onAudioFocusGained() {}

    fun onAudioFocusLost() {}

    fun onAudioFocusLostTransient() {}

}