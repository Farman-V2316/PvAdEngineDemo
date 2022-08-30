package com.dailyhunt.tv.players.managers

import android.content.Context
import android.view.LayoutInflater
import com.dailyhunt.tv.players.R
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.customviews.DHPlaybackControlView
import com.dailyhunt.tv.players.customviews.ExoPlayerWrapper2
import com.dailyhunt.tv.players.customviews.WebPlayerWrapper
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset

class PlayerBuilder {

    companion object {


        fun createExoplayer(context: Context, playerCallbacks: PlayerCallbacks?, adsTimeSpentOnLPHelper : AdsTimeSpentOnLPHelper? = null,
                            playerAsset: ExoPlayerAsset, pageReferrer: PageReferrer?, section: NhAnalyticsEventSection,
                            referrerFlow: PageReferrer?, referrerLead: PageReferrer?): ExoPlayerWrapper2 {
            Logger.i("PlayerProvider", "Creating the new exoplay for id = ${playerAsset.id}")
            val view = LayoutInflater.from(context).inflate(R.layout.layout_card_videos_big, null)
            val exoPlayerWrapper = view.findViewById<ExoPlayerWrapper2>(R.id.exo_player_wrapper)
            exoPlayerWrapper.buildPlayer(playerAsset, playerCallbacks, adsTimeSpentOnLPHelper)
            exoPlayerWrapper.buildAnalyticHelper(null, pageReferrer, referrerFlow, referrerLead)
//            exoPlayerWrapper.setStartAction(PlayerVideoStartAction.AUTOPLAY)
            return exoPlayerWrapper
        }

        fun createVideoController(context: Context): DHPlaybackControlView {
            Logger.i("PlayerProvider", "Creating the new DHPlaybackControlView")
            val playbackController = LayoutInflater.from(context).inflate(R.layout
                    .dh_playback_video_controls, null) as DHPlaybackControlView
            return playbackController
        }

        fun createEmbedplayer(context: Context,
                                    playerCallbacks: PlayerCallbacks?, playerAsset: PlayerAsset):
                WebPlayerWrapper {
            Logger.i(VideoRequester.VIDEO_DEBUG, "Creating the new webplayer for id = ${playerAsset.id}")
            val view = LayoutInflater.from(context).inflate(R.layout.layout_item_dh_webplayer_wrapper, null)
            val webPlayerWrapper = view.findViewById<WebPlayerWrapper>(R.id.frame_layout_holder)
            webPlayerWrapper.resetCallbacks(playerCallbacks, webPlayerWrapper.getReferrerProvider())
            webPlayerWrapper.loadVideo(playerAsset, false)
            return webPlayerWrapper
        }
    }
}