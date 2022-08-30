package com.dailyhunt.tv.players.autoplay

import android.content.Context
import android.view.LayoutInflater
import com.dailyhunt.tv.players.R
import com.dailyhunt.tv.players.autoplay.VideoRequester.Companion.VIDEO_DEBUG
import com.dailyhunt.tv.players.customviews.ExoPlayerWrapper2
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.player.AutoPlayManager
import java.lang.ref.WeakReference
import java.util.*

class ExoPlayerCacheHelper2 {

    private val exoplayerCacheList = WeakHashMap<AutoPlayable, ExoPlayerWrapper2>()

    fun updateExoplayerInCache(autoPlayable: AutoPlayable, exoPlayerWrapper: ExoPlayerWrapper2) {
        exoplayerCacheList[autoPlayable] = exoPlayerWrapper
    }

    fun getOrCreateExoplayer(autoPlayable: AutoPlayable, context: Context,
                             playerCallbacks: PlayerCallbacks?, playerAsset: ExoPlayerAsset,
                             autoPlayManager: AutoPlayManager?, isCollection: Boolean,
                             adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?): ExoPlayerWrapper2? {
        if (autoPlayManager?.canLoadPlayer() == false) {
            return null
        }

        if (exoplayerCacheList.containsKey(autoPlayable)) {
            val exoPlayerWrapper = exoplayerCacheList[autoPlayable]
            val id = exoPlayerWrapper?.getPlayerAsset()?.id
            if (id != null && id != playerAsset.id) {
                return reloadExoplayer(exoPlayerWrapper, playerAsset, autoPlayable,
                        context, playerCallbacks, adsTimeSpentOnLPHelper)
            } else {
                Logger.w(VIDEO_DEBUG, "Same exoplayer is return for the same base asset")
                return exoPlayerWrapper
            }
        }

        if (exoplayerCacheList.size < 2 && !exoplayerCacheList.containsKey(autoPlayable)) {
            return createExoplayer(autoPlayable, context, playerCallbacks, playerAsset, adsTimeSpentOnLPHelper)
        }

        exoplayerCacheList.forEach {
            Logger.d(VIDEO_DEBUG, "Checking for view ${it.key.hashCode()} and its position " +
                    "${it.key?.getPositionInList()}")
            if (isCollection || autoPlayManager?.canExchangeAutoPlayer(autoPlayable, it.key) == true) {
                Logger.d(VIDEO_DEBUG, "Reassigning the player for ${it.key.hashCode()} and its " +
                        "position ${it.key.getPositionInList()}")
                val exoPlayerWrapper = reloadExoplayer(it.value, playerAsset,
                        autoPlayable, context, playerCallbacks, adsTimeSpentOnLPHelper)
                it.key.resetVideoState()
                exoplayerCacheList.remove(it.key)
                exoplayerCacheList[autoPlayable] = exoPlayerWrapper
                return exoPlayerWrapper
            } else {
                Logger.d(VIDEO_DEBUG, "Cannot reassign the player for ${it.key.hashCode()} and " +
                        "its position ${it.key.getPositionInList()}")
            }
        }

        return null
    }


    private fun createExoplayer(autoPlayable: AutoPlayable, context: Context,
                                playerCallbacks: PlayerCallbacks?, playerAsset: ExoPlayerAsset,
                                adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?): ExoPlayerWrapper2 {
        Logger.i(VIDEO_DEBUG, "Creating the new exoplay for id = ${playerAsset.id}")
        val view = LayoutInflater.from(context).inflate(R.layout.layout_card_videos_big, null)
        val exoPlayerWrapper = view.findViewById<ExoPlayerWrapper2>(R.id.exo_player_wrapper)
        exoPlayerWrapper.buildPlayer(playerAsset, playerCallbacks, adsTimeSpentOnLPHelper)
        updateExoplayerInCache(autoPlayable, exoPlayerWrapper)
        return exoPlayerWrapper
    }

    fun reloadExoplayer(exoPlayerWrapper: ExoPlayerWrapper2, playerAsset: ExoPlayerAsset,
                        autoPlayable: AutoPlayable, context: Context, playerCallbacks: PlayerCallbacks?,
                        adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?): ExoPlayerWrapper2 {
        Logger.e(VIDEO_DEBUG, "releasing the video for ${(exoPlayerWrapper.getPlayerAsset()?.id)} " +
                "& creating new player for ${playerAsset.id}")
        exoPlayerWrapper.releasePlayer()
        exoplayerCacheList.remove(autoPlayable)
        return createExoplayer(autoPlayable, context, playerCallbacks, playerAsset, adsTimeSpentOnLPHelper)
    }

    fun releaseVideo(autoPlayable: AutoPlayable, isDetailShowing: Boolean) {
        Logger.w(VIDEO_DEBUG, "Releasing the video for ${autoPlayable.hashCode()}, " +
                "isDetailShowing : $isDetailShowing")
        val exoPlayerWrapper = exoplayerCacheList.get(autoPlayable)
        if (exoPlayerWrapper != null) {
            Logger.e(VIDEO_DEBUG, "releasing the video for ${(exoPlayerWrapper.getPlayerAsset()?.id)}")
            if (isDetailShowing)
                exoPlayerWrapper.releaseAndSetReload()
            else
                exoPlayerWrapper.releasePlayer()
        }

        exoplayerCacheList.remove(autoPlayable)
    }

    fun clearPlayerInstances() {
        exoplayerCacheList.forEach {
            it.key.resetVideoState()
            it.value.releasePlayer()
        }
        exoplayerCacheList.clear()
    }
}