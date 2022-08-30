/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.sticky

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.view.view.DisplayStickAudio
import java.lang.ref.WeakReference

const val STICKY_AUDIO_COMMENTARY_ENABLED = false

/**
 * Interface to control UI position as well as play/stop function of sticky audio commentary on
 * an activity screen based on Activity lifecycle callbacks
 *
 * @author santhosh.kc
 */
interface StickyAudioPlayControlInterface {

    fun onActivityResumed(displayStickAudio: DisplayStickAudio, commentaryData : Any? = null)

    fun onActivityPaused(displayStickAudio: DisplayStickAudio)

    fun onStickyAudioCommentaryStateChanged(intent: Intent?, displayStickAudio: DisplayStickAudio)

    fun resetPlayControlVisibility()

    fun getAudioCommentaryLiveData() : MutableLiveData<Any>
}

/**
 * Provider of StickyAudioPlayControlInterface
 */
object StickyAudioPlayControlInterfaceProvider {

    private var stickyAudioPlayControlInterface : StickyAudioPlayControlInterface? = null

    fun getStickyPlayControlInterface() : StickyAudioPlayControlInterface? {
        return stickyAudioPlayControlInterface
    }

    fun setStickyPlayControlInterface(stickyAudioPlayControlInterface:
                                      StickyAudioPlayControlInterface) {
        StickyAudioPlayControlInterfaceProvider.stickyAudioPlayControlInterface = stickyAudioPlayControlInterface
    }
}

class StickyAudioCommentaryStateReceiver(callback : Callback) : BroadcastReceiver() {

    private val callbackWeakRef = WeakReference<Callback>(callback)

    override fun onReceive(context: Context?, intent: Intent?) {
        callbackWeakRef.get()?.onCommentaryStateChanged(intent)
    }

    interface Callback {
        fun onCommentaryStateChanged(updatedIntent: Intent?)
    }
}

fun registerStickyAudioStateReceiver(context: Context, callback: StickyAudioCommentaryStateReceiver
.Callback?)
        : StickyAudioCommentaryStateReceiver? {
    callback ?: return null
    val intentFilter = IntentFilter()
    intentFilter.addAction(DHConstants.INTENT_STICKY_AUDIO_COMMENTARY_STATE_CHANGED)
    val receiver = StickyAudioCommentaryStateReceiver(callback)
    context.registerReceiver(receiver, intentFilter)
    return receiver
}

fun unRegisterStickyAudioStateReceiver(context: Context, receiver:
StickyAudioCommentaryStateReceiver?) {
    receiver ?: return
    context.unregisterReceiver(receiver)
}

