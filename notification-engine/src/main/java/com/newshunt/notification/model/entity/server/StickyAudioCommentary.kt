/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.entity.server

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.newshunt.app.helper.AudioStateTrigger
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.sticky.STICKY_AUDIO_COMMENTARY_ENABLED
import com.newshunt.common.view.customview.NhWebView
import com.newshunt.notification.IStickyNotificationService
import com.newshunt.dataentity.notification.asset.CommentaryState
import com.newshunt.notification.model.manager.StickyNotificationsManager
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 * @author santhosh.kc
 */

@JvmField
val audioCommentaryStateLiveData = MutableLiveData<Any>()

@JvmField
val audioCommentaryStateJsonLiveData: LiveData<String>? =
        Transformations.map(audioCommentaryStateLiveData) { JsonUtils.toJson(it) }

data class StickyAudioCommentary(val id: String, val type: String, var state: CommentaryState?,
                                 val audioUrl: String, val audioLanguage: String? = null, val
                                 title: String? = null, var trigger: AudioStateTrigger? = null,
                                 var userPlayRequestTime : Long = 0)
    : Serializable {
    override fun toString(): String {
        return "(id : $id, type: $type, audioUrl : $audioUrl, state: $state )"
    }
}

fun readCurrentAudioCommentaryFromStickyProcess(serviceIntent: Intent?) {
    Logger.d(StickyNotificationsManager.TAG, "Going to bind to sticky notification service")

    if (!STICKY_AUDIO_COMMENTARY_ENABLED) {
        return
    }
    serviceIntent?.let {
        CommonUtils.getApplication().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
    }
}

fun getCurrentAudioCommentary(): StickyAudioCommentary? {
    return audioCommentaryStateLiveData.value as? StickyAudioCommentary
}

val serviceConnection = object : ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Logger.d(StickyNotificationsManager.TAG, "On Bound to Sticky Notification Service")
        val stickyNotificationService = IStickyNotificationService.Stub.asInterface(service)
        val currentAudioCommentary =
                JsonUtils.fromJson(stickyNotificationService?.currentAudioCommentaryState
                        ?: Constants.EMPTY_STRING, StickyAudioCommentary::class.java)

        if (currentAudioCommentary != null) {
            Logger.d(StickyNotificationsManager.TAG, "CurrentAudioCommentary after bound to service is $currentAudioCommentary")
        } else {
            Logger.d(StickyNotificationsManager.TAG, "CurrentAudioCommentary is null after bound " +
                    "to service")
        }
        audioCommentaryStateLiveData.postValue(currentAudioCommentary)
        CommonUtils.getApplication().unbindService(this)
    }

}

fun registerWebViewForCommentaryStateUpdate(webView: NhWebView?, context : Context?) {
	registerWebViewForCommentaryStateUpdate(webView, context as? LifecycleOwner)
}

fun registerWebViewForCommentaryStateUpdate(webView : NhWebView?, owner: LifecycleOwner?) {
    webView ?: return
    owner ?: return

    audioCommentaryStateJsonLiveData?.observe(owner,  Observer { webView
            .updateAudioCommentaryState(it) })
}

fun registerWebViewForCommentaryStateUpdate(observer: WebViewStickyAudioStateChangeObserver?,
                                            owner: LifecycleOwner?) {
    observer ?: return
    owner ?: return

    audioCommentaryStateJsonLiveData?.observe(owner, observer)
}

fun unRegisterWebViewForCommentaryStateUpdate(context : Context?) {
    unRegisterWebViewForCommentaryStateUpdate(context as? LifecycleOwner)
}

fun unRegisterWebViewForCommentaryStateUpdate(owner: LifecycleOwner?) {
    owner ?: return
    audioCommentaryStateJsonLiveData?.removeObservers(owner)
}

class WebViewStickyAudioStateChangeObserver(webView: NhWebView?) : Observer<String> {
    private val webViewRef = WeakReference<NhWebView>(webView)

    override fun onChanged(t: String?) {
        val webView = webViewRef.get()
        webView?.let {
            it.updateAudioCommentaryState(t)
        }
    }
}