/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.autoplay.VideoDetailClosed
import com.dailyhunt.tv.players.autoplay.VideoDetailOpened
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.customviews.ExoPlayerWrapper2
import com.dailyhunt.tv.players.model.entities.CallState
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.appview.common.ui.customview.CustomConstraintLayout
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import com.squareup.otto.Subscribe

/**
 * Created on 05/10/19.
 */
class ExoAutoplayViewHolder(private val binding: ViewDataBinding,
                            pageRef: PageReferrer?, context: Context,
                            commonVideoRequester: VideoRequester?,
                            isInCollection: Boolean,
                            cardsViewModel: CardsViewModel,
                            parentLifecycleOwner: LifecycleOwner?,
                            section: String,
                            displayCardTypeIndex: Int,
                            parentItem: CommonAsset?, uniqueScreenId: Int,
                            val videoPrefetchCallback: VideoPrefetchCallback? = null) :
        AbstractAutoplayViewHolder(binding, pageRef, context, commonVideoRequester, isInCollection,
                cardsViewModel, parentLifecycleOwner, section,
                displayCardTypeIndex, parentItem, uniqueScreenId, videoPrefetchCallback) {

    protected val LOG_TAG = "ExoAutoplayViewHolder"
    private var anyDetailOpened = false

    companion object {
        private const val THUMBNAIL_TIMER = 1
    }
    override fun releasePlayer() {
        videoWrapper?.releasePlayer()
        videoWrapper = null
        uiHandler.post { commonVideoRequester?.exoPlayerCacheHelper?.releaseVideo(this, isDetailShowing) }
    }

    override fun setPlayerCallbacks() {
        // Do nothing
    }

    override fun createPlayer() {
        Logger.d(LOG_TAG, "createPlayer ${this.hashCode()}")
        buildPlayer()
    }

    private fun buildPlayer() {
        var playerAsset = DHVideoUtils.getPlayerAsset(commonAsset)
        Logger.d(LOG_TAG, "buildPlayer ${this.hashCode()}")
        Logger.d(LOG_TAG, "buildPlayer id = $cardPosition")
        if (!AutoPlayHelper.isAutoPlayAllowed() || CommonUtils.isMemoryLow()) {
            Logger.d(LOG_TAG, "buildPlayer AutoPlay = ${!AutoPlayHelper.isAutoPlayAllowed()}")
            Logger.d(LOG_TAG, "buildPlayer LowMemory = ${CommonUtils.isMemoryLow()}")
            Logger.d(LOG_TAG, "buildPlayer >> return id = ${playerAsset?.id}")
            return
        }
        if (playerAsset is ExoPlayerAsset) {
            adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
            val exoPlayerWrapper = commonVideoRequester?.exoPlayerCacheHelper?.getOrCreateExoplayer(autoPlayable = this,
                    context = context, playerCallbacks = this,
                    playerAsset = playerAsset as ExoPlayerAsset,
                    autoPlayManager = getAutoPlayManager(),
                    isCollection = isInCollection,
                    adsTimeSpentOnLPHelper)
            if (exoPlayerWrapper != null) {
                exoPlayerWrapper.buildAnalyticHelper(null, pageReferrer, referrerFlow, referrerLead)
                exoPlayerWrapper.setStartAction(PlayerVideoStartAction.AUTOPLAY)
//                    exoPlayerWrapper.registerVideoControlsOverlay(listOf(muteButton))
                videoWrapper = exoPlayerWrapper
                observeLiveData()
                (viewBinding.body.mediaLyt as CustomConstraintLayout).isIntercept = false
            }
        }
    }

    override fun canRelease(): Boolean {
        return hasUserLeftFragment || visiblePercentage < autoPlayVisibility
    }

    override fun videoLoaded() {
        Logger.i(LOG_TAG, "Loading finished for exo video ${this.hashCode()}")
    }

    override fun videoLoadError() {
        Logger.i(LOG_TAG, "Loading error for ${this.hashCode()}")
        // handle the player error
        Logger.d(LOG_TAG, "Player Init Error received for ${hashCode()}")
        if(!DHVideoUtils.isEligibleToPrefetch(commonAsset)) {
            commonVideoRequester?.exoPlayerCacheHelper?.releaseVideo(this, isDetailShowing)
            videoWrapper = null
        }
        loadThumbnailImage()
        hideLoader()
    }

    override fun isPLaying(): Boolean {
        return videoWrapper?.isPlaying() ?: false
    }

    override fun videoReset() {
        Logger.d(LOG_TAG, "videoReset for ${hashCode()}, positon : $cardPosition")
        if (getAutoPlayManager()?.canLoadPlayer() == false || !AutoPlayHelper.isAutoPlayAllowed()) {
            videoWrapper = null
            return
        }
        uiHandler.post {
            Logger.d(VideoRequester.VIDEO_DEBUG, "Video exo reset called for   ${this.hashCode()}" +
                    " position : $cardPosition")
            var playerAsset = DHVideoUtils.getPlayerAsset(commonAsset)
            if (videoWrapper != null && videoWrapper is ExoPlayerWrapper2 && playerAsset != null) {
                adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
                val exoPlayerWrapper = commonVideoRequester?.exoPlayerCacheHelper?.reloadExoplayer(
                        exoPlayerWrapper = videoPlayerWrapper as ExoPlayerWrapper2,
                        playerAsset = playerAsset as ExoPlayerAsset, autoPlayable = this, context = context,
                        playerCallbacks = this, adsTimeSpentOnLPHelper)
                exoPlayerWrapper?.buildAnalyticHelper(null, pageReferrer, referrerFlow, referrerLead)
                exoPlayerWrapper?.setStartAction(PlayerVideoStartAction.AUTOPLAY)
                videoWrapper = exoPlayerWrapper
            }
        }
    }

    @Subscribe
    fun onVideoDetailOpened(videoDetailOpened: VideoDetailOpened) {
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            return
        }
//        Logger.d(VideoRequester.VIDEO_DEBUG, "onVideoDetailOpened is called ids ${baseAsset!!.id} : ${videoDetailOpened.id}")
//        if (baseAsset!!.id == videoDetailOpened.id) {
//            return
//        }
        anyDetailOpened = true
        if (videoWrapper == null) return
        Logger.d(VideoRequester.VIDEO_DEBUG, "onVideoDetailOpened hiding the surface view")
        (videoWrapper as ExoPlayerWrapper2).hideSurfaceView()

    }

    @Subscribe
    fun onVideoDetailClosed(videoDetailClosed: VideoDetailClosed) {
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            return
        }
//        Logger.d(VideoRequester.VIDEO_DEBUG, "onVideoDetailClosed is called ids ${baseAsset!!.id} : ${videoDetailClosed.id}")
//        if (baseAsset!!.id == videoDetailClosed.id) {
//            return
//        }
        anyDetailOpened = false
        if (videoWrapper == null) return
        Logger.d(VideoRequester.VIDEO_DEBUG, "onVideoDetailClosed so setting the proper player height")
        //setVideoHeight()
        (videoWrapper as ExoPlayerWrapper2).showSurfaceView()
    }

    override fun resetVideoState() {
        loadThumbnailImage()
        videoWrapper = null
    }

    override fun isAnyActivePlayer(): Boolean {
        return getAutoPlayManager()?.isAnyActivePlayer() == true
    }

    @Subscribe
    fun onConnectivityChangedEvent(connectionSpeedEvent: ConnectionSpeedEvent) {
        super.onConnectivityChanged(connectionSpeedEvent)
    }

    @Subscribe
    fun onReceiveCall(callState: CallState) {
        super.onReceive(callState)
    }

    @Subscribe
    fun onPlaySettingsChangedEvent(event: PlaySettingsChangedEvent) {
        super.onPlaySettingsChanged(event)
    }

    override fun onRenderedFirstFrame() {
        videoPrefetchCallback?.onRenderedFirstFrame(cardPosition, commonAsset)
        scheduleThumbnailTimer()
    }

    private fun scheduleThumbnailTimer() {
        videoThumbnailTimerHandler.removeCallbacksAndMessages(null)

        if (!viewBinding.body.newsImage.isShown) {
            //Thumbnail is not shown, we dont need of this timer
            Logger.d(LOG_TAG, "scheduleThumbnailTimer return >")
            return
        }
        val initialVideoThumbnailDelay = PreferenceManager.getLong(
                AppStatePreference.INITIAL_VIDEO_THUMBNAIL_DELAY.getName(), 0)
        Logger.d(LOG_TAG, "scheduleThumbnailTimer sendEmptyMessageDelayed " + initialVideoThumbnailDelay)
        videoThumbnailTimerHandler.sendEmptyMessageDelayed(THUMBNAIL_TIMER, initialVideoThumbnailDelay)
    }

    private val videoThumbnailTimerHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            removeCallbacksAndMessages(null)
            if (videoWrapper != null) {
                val currentProgressPos = videoWrapper!!.currentDuration ?: 0
                if (viewBinding.body.newsImage.isShown && currentProgressPos > 10) {
                    viewBinding.body.newsImage.visibility = View.GONE
                } else if (viewBinding.body.newsImage.isShown) {
                    //Thumbnail is not shown, we don't need of this timer
                    val videoThumbnailDelay = PreferenceManager.getLong(
                            AppStatePreference.VIDEO_THUMBNAIL_DELAY.getName(), 100)
                    Logger.d(LOG_TAG, "videoThumbnailTimerHandler sendEmptyMessageDelayed" + videoThumbnailDelay)
                    sendEmptyMessageDelayed(THUMBNAIL_TIMER, videoThumbnailDelay)
                }
            }
        }
    }

}