/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.autoplay.VideoRequester.Companion.VIDEO_DEBUG
import com.dailyhunt.tv.players.customviews.WebPlayerWrapper
import com.dailyhunt.tv.players.model.entities.CallState
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.appview.common.ui.customview.CustomConstraintLayout
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import com.squareup.otto.Subscribe

/**
 * VideoViewHolder for WebPlayers
 *
 * @author karthik.r
 */
class WebAutoplayViewHolder(private val binding: ViewDataBinding,
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
                displayCardTypeIndex, parentItem, uniqueScreenId) {

    override fun releasePlayer() {
        Logger.i(VIDEO_DEBUG , "releasePlayer  $adapterPosition")
        videoWrapper?.releasePlayer()
        videoWrapper = null
        uiHandler.post { commonVideoRequester?.webPlayerCacheHelper?.releaseVideo(this, isDetailShowing) }
    }

    override fun setPlayerCallbacks() {
        (videoWrapper!! as WebPlayerWrapper).wrapperCallbacks = this
    }

    override fun onDisplayClick() {
//        uiHandler.post({ openVideoFragment() })
    }

    override fun canRelease(): Boolean {
        return hasUserLeftFragment || visiblePercentage < autoPlayVisibility
    }

    override fun videoLoaded() {
        Logger.i(VIDEO_DEBUG , "Loading finished for  ${this.hashCode()}")
    }

    override fun videoLoadError() {
        Logger.i(VIDEO_DEBUG , "Loading error for  ${this.hashCode()}")
    }

    override fun createPlayer() {
        if (!AutoPlayHelper.isAutoPlayAllowed() || CommonUtils.isMemoryLow()) {
            return
        }
        uiHandler.post {
            val playerAsset = DHVideoUtils.getPlayerAsset(commonAsset)
            if(playerAsset is PlayerAsset){
                val webVideoWrapper = commonVideoRequester?.webPlayerCacheHelper?.getOrCreateWebPlayer(
                        autoPlayable = this, context = context, playerAsset = playerAsset,
                        playerCallbacks = this, autoPlayManager = getAutoPlayManager())
                if (webVideoWrapper != null) {
                    webVideoWrapper.setStartAction(PlayerVideoStartAction.AUTOPLAY)
                    videoWrapper = webVideoWrapper
                    observeLiveData()
                    (viewBinding.body.mediaLyt as CustomConstraintLayout).isIntercept = true
                }
            }
        }
//        Logger.e(VIDEO_DEBUG , "Loading the web player for video holder ${this.hashCode()}")
//        videoViewHelper = WebVideoHelper(context, videoReadyInterface, PlayerControlHelper.isMuteMode)
//        Logger.d(LOG_TAG, "loadPlayer : ${videoViewHelper?.hashCode()}")
//        videoViewHelper?.loadVideo(this.baseAsset!!, null, null, null, PlayerVideoStartAction.CLICK)
//        commonVideoRequester.addWebPlayer(this)
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

    override fun isAnyActivePlayer() : Boolean {
        return getAutoPlayManager()?.isAnyActivePlayer() == true
    }

    override fun onRenderedFirstFrame() {
        videoPrefetchCallback?.onRenderedFirstFrame(cardPosition, commonAsset)
    }

}