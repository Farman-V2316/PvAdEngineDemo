/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.notification.view.service

import android.content.Intent
import android.os.IBinder
import com.newshunt.app.helper.AudioStateTrigger
import com.newshunt.app.helper.DHAudioPlayerCallback
import com.newshunt.app.helper.DHMediaPlayerManger.setDhAudioManagerCallback
import com.newshunt.app.helper.DHMediaPlayerManger.start
import com.newshunt.app.helper.DHMediaPlayerManger.stop
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.sticky.STICKY_AUDIO_COMMENTARY_ENABLED
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.StickyNavModel
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.CommentaryState
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.IStickyNotificationService
import com.newshunt.notification.helper.areStickyNotificationsSame
import com.newshunt.notification.helper.constructAudioCommentaryState
import com.newshunt.notification.helper.fireStickyAudioCommentaryStateChangedBroadcast
import com.newshunt.notification.helper.getNoOpStickyCommentary
import com.newshunt.notification.model.entity.server.StickyAudioCommentary
import com.newshunt.notification.model.manager.StickyNotificationsManager
import com.newshunt.notification.view.view.CricketNotificationView
import com.newshunt.notification.view.view.StickyNotificationView

/**
 * Cricket Sticky foreground service implementation
 *
 * Created by srikanth.r on 10/13/21.
 */
class CricketStickyService: StickyNotificationService(), DHAudioPlayerCallback {
    private var binder: IStickyNotificationService.Stub? = null
    private var currentStickyAudioCommentary: StickyAudioCommentary? = null

    override fun onCreate() {
        super.onCreate()
        buildDummyNotification(NotificationConstants.STICKY_CRICKET_TYPE.hashCode())
        setDhAudioManagerCallback(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (binder == null) {
            binder = StickyNotificationServiceBinder(currentStickyAudioCommentary, stickyNavModel)
        }
        return binder
    }

    override fun onAudioBufferingStarted() {
        currentStickyAudioCommentary?.let { commentary ->
            if (commentary.audioUrl.isBlank()) {
                return
            }
            if (stickyNavModel != null && stickyNavModel.getBaseNotificationAsset() != null && stickyNavModel.stickyType != null) {
                commentary.state = CommentaryState.BUFFERING
                fireStickyAudioCommentaryStateChangedBroadcast(this, commentary)
            }
        }
    }

    override fun onAudioBufferingEnded(bufferingEndedToPlay: Boolean) {
        currentStickyAudioCommentary?.let { commentary ->
            if (commentary.audioUrl.isBlank() || commentary.state !== CommentaryState.BUFFERING) {
                return
            }

            if (stickyNavModel != null && stickyNavModel.getBaseNotificationAsset() != null && stickyNavModel.stickyType != null) {
                commentary.state = if (bufferingEndedToPlay) CommentaryState.PLAYING else CommentaryState.STOPPED
                fireStickyAudioCommentaryStateChangedBroadcast(this, currentStickyAudioCommentary)
            }
        }
    }

    override fun onAudioStarted(trigger: AudioStateTrigger?) {
        updateAudioStarted(currentStickyAudioCommentary, trigger)
    }

    override fun onAudioStopped(trigger: AudioStateTrigger?) {
        if (trigger === AudioStateTrigger.DEPENDENT_REMOVED) {
            updateAudioRemoved()
        } else {
            updateAudioStopped(currentStickyAudioCommentary, trigger)
        }
    }

    override fun updateAudioCommentary(audioUrl: String?, audioLanguage: String?) {
        currentStickyAudioCommentary = updateAudioCommentaryFrom(
            audioUrl,
            audioLanguage, currentStickyAudioCommentary,
            stickyNavModel, true, AudioStateTrigger.BACKEND
        )
    }

    override fun onAudioComplete(trigger: AudioStateTrigger?) {
        updateAudioRemoved()
    }

    override fun inflateNotificationView(): StickyNotificationView {
        val prevStickyNavModel = stickyNavModel
        val playAtStart = extractAudioCommentary(stickyNavModel, prevStickyNavModel)
        if (playAtStart) {
            startAudioStream(currentStickyAudioCommentary, AudioStateTrigger.USER)
        }

        currentStickyAudioCommentary?.let { commentary ->
            stickyNavModel.getBaseNotificationAsset()?.state = commentary.state
        }
        val view = CricketNotificationView(stickyNavModel, refresher, this)
        view.buildNotification(false, true, null)
        return view
    }

    override fun serviceStopping() {
        Logger.d(TAG, "removeAudioStream")
        if (currentStickyAudioCommentary?.audioUrl?.isNotBlank() == true) {
            stop(AudioStateTrigger.DEPENDENT_REMOVED)
        }
    }

    override fun onAudioPlayEvent() {
        currentStickyAudioCommentary?.let {
            startAudioStream(it, AudioStateTrigger.USER)
        }

    }

    override fun onAudioStopEvent() {
        currentStickyAudioCommentary?.let {
            stopAudioStream(it, AudioStateTrigger.USER)
        }
    }

    override fun showLiveAudioCommentaryOption(): Boolean {
        return currentStickyAudioCommentary != null
    }

    private fun updateAudioStarted(currentStickyAudioCommentary: StickyAudioCommentary?, trigger: AudioStateTrigger?) {
        if (currentStickyAudioCommentary == null || CommonUtils.isEmpty(currentStickyAudioCommentary.audioUrl)) {
            return
        }
        if (stickyNavModel != null && stickyNavModel.getBaseNotificationAsset() != null && stickyNavModel.stickyType != null) {
            Logger.d(TAG, "startAudioStream")
            currentStickyAudioCommentary.state = CommentaryState.PLAYING
            currentStickyAudioCommentary.trigger = trigger
            fireStickyAudioCommentaryStateChangedBroadcast(this, currentStickyAudioCommentary)
        }
    }

    private fun updateAudioRemoved() {
        if (stickyNavModel == null ||
            stickyNavModel.getBaseNotificationAsset() == null ||
            currentStickyAudioCommentary == null ||
            currentStickyAudioCommentary?.audioUrl.isNullOrBlank()){
            return
        }
        fireStickyAudioCommentaryStateChangedBroadcast(this, getNoOpStickyCommentary(stickyNavModel))
        currentStickyAudioCommentary = null
    }

    private fun updateAudioStopped(currentStickyAudioCommentary: StickyAudioCommentary?, trigger: AudioStateTrigger?) {
        if (currentStickyAudioCommentary == null || CommonUtils.isEmpty(currentStickyAudioCommentary.audioUrl)) {
            return
        }
        Logger.d(TAG, "stopAudioStream")
        if (stickyNavModel != null && stickyNavModel.getBaseNotificationAsset() != null && stickyNavModel.stickyType != null) {
            currentStickyAudioCommentary.state = CommentaryState.STOPPED
            currentStickyAudioCommentary.trigger = trigger
            fireStickyAudioCommentaryStateChangedBroadcast(this, currentStickyAudioCommentary)
        }
    }
    private fun updateAudioCommentaryFrom(audioUrl: String?,
                                          audioLanguage: String?,
                                          currentStickyAudioCommentary: StickyAudioCommentary?,
                                          stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?,
                                          isUpdate: Boolean,
                                          trigger: AudioStateTrigger): StickyAudioCommentary? {
        if (stickyNavModel?.getBaseNotificationAsset() == null) {
            return null
        }
        if (CommonUtils.isEmpty(audioUrl)) {
            stopAudioStream(currentStickyAudioCommentary, trigger)
            return null
        }
        if (currentStickyAudioCommentary != null && CommonUtils.equals(audioUrl, currentStickyAudioCommentary.audioUrl)) {
            //no change in url, so maintain current state and return
            return currentStickyAudioCommentary
        }
        stickyNavModel.getBaseNotificationAsset()!!.audioUrl = audioUrl
        stickyNavModel.getBaseNotificationAsset()!!.audioLanguage = audioLanguage
        val newState = constructAudioCommentaryState(stickyNavModel)
        return if (currentStickyAudioCommentary == null || newState == null) {
            newState
        } else {
            if (currentStickyAudioCommentary.state === CommentaryState.PLAYING) {
                stopAudioStream(currentStickyAudioCommentary, trigger)
                if (isUpdate) {
                    startAudioStream(newState, trigger)
                }
            } else {
                newState.state = currentStickyAudioCommentary.state
            }
            newState
        }
    }

    private fun startAudioStream(currentStickyAudioCommentary: StickyAudioCommentary?,
                                 trigger: AudioStateTrigger) {
        if (!STICKY_AUDIO_COMMENTARY_ENABLED
            || currentStickyAudioCommentary == null || CommonUtils.isEmpty(currentStickyAudioCommentary.audioUrl)) {
            return
        }
        val audioUrl = currentStickyAudioCommentary.audioUrl
        if (trigger === AudioStateTrigger.USER) {
            currentStickyAudioCommentary.userPlayRequestTime = System.currentTimeMillis()
        }
        if (!CommonUtils.isEmpty(audioUrl)) {
            start(audioUrl, currentStickyAudioCommentary.audioLanguage, currentStickyAudioCommentary.id, trigger)
        }
    }

    private fun stopAudioStream(currentStickyAudioCommentary: StickyAudioCommentary?, trigger: AudioStateTrigger) {
        if (currentStickyAudioCommentary == null || CommonUtils.isEmpty(currentStickyAudioCommentary.audioUrl)) {
            return
        }
        val audioUrl = currentStickyAudioCommentary.audioUrl
        if (!CommonUtils.isEmpty(audioUrl)) {
            stop(trigger)
        }
    }

    private fun extractAudioCommentary(newStickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>,
                                       prevStickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?): Boolean {
        if (!STICKY_AUDIO_COMMENTARY_ENABLED) {
            return false
        }
        if (prevStickyNavModel == null || !areStickyNotificationsSame(prevStickyNavModel, newStickyNavModel)) {
            currentStickyAudioCommentary = constructAudioCommentaryState(newStickyNavModel)
            return newStickyNavModel.audioPlayAtStart
        }
        val audioUrl =
            if (newStickyNavModel.getBaseNotificationAsset() != null) newStickyNavModel.getBaseNotificationAsset()!!
                .audioUrl else null
        val audioLanguage = if (newStickyNavModel.getBaseNotificationAsset() != null) newStickyNavModel.getBaseNotificationAsset()!!.audioLanguage else null
        currentStickyAudioCommentary = updateAudioCommentaryFrom(audioUrl, audioLanguage, currentStickyAudioCommentary, newStickyNavModel, false, AudioStateTrigger.USER)
        currentStickyAudioCommentary?.let { commentary ->
            if(commentary.state == CommentaryState.PLAYING) {
                commentary.userPlayRequestTime = System.currentTimeMillis()
                fireStickyAudioCommentaryStateChangedBroadcast(this, commentary)
            }
        }
        return newStickyNavModel.audioPlayAtStart && currentStickyAudioCommentary?.state !== CommentaryState.PLAYING
    }

    class StickyNotificationServiceBinder(private val currentStickyAudioCommentary: StickyAudioCommentary?,
                                          private val stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?) : IStickyNotificationService.Stub() {
        override fun getCurrentAudioCommentaryState(): String {
            Logger.d(StickyNotificationsManager.TAG, "OnBinder - gettingCurrentStickyAudioCommentary")
            if (!STICKY_AUDIO_COMMENTARY_ENABLED) {
                return Constants.EMPTY_STRING
            }
            if (currentStickyAudioCommentary == null) {
                Logger.d(StickyNotificationsManager.TAG, "CurrentStickyAudioCommentary is null")
            } else {
                Logger.d(StickyNotificationsManager.TAG, "CurrentStickyAudioCommentary is :$currentStickyAudioCommentary")
            }
            return JsonUtils.toJson(currentStickyAudioCommentary ?: getNoOpStickyCommentary(stickyNavModel))
        }
    }
}