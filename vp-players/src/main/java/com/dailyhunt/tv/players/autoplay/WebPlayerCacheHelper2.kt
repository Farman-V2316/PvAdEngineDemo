package com.dailyhunt.tv.players.autoplay

import android.content.Context
import android.view.LayoutInflater
import com.dailyhunt.tv.players.R
import com.dailyhunt.tv.players.customviews.WebPlayerWrapper
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.player.AutoPlayManager

class WebPlayerCacheHelper2 {

  private val webPlayerCacheList = mutableMapOf<AutoPlayable, WebPlayerWrapper>()

  fun updateWebplayerInCache(autoPlayable: AutoPlayable, webPlayerWrapper: WebPlayerWrapper) {
    webPlayerCacheList.put(autoPlayable, webPlayerWrapper)
  }

  fun getOrCreateWebPlayer(autoPlayable: AutoPlayable, context: Context,
                           playerCallbacks: PlayerCallbacks?, playerAsset: PlayerAsset,
                           autoPlayManager: AutoPlayManager?) : WebPlayerWrapper? {
    if (autoPlayManager?.canLoadPlayer() == false) {
      return null
    }

    if (webPlayerCacheList.containsKey(autoPlayable)) {
      val webPlayerWrapper = webPlayerCacheList.get(autoPlayable)
      val id = webPlayerWrapper?.getAssetId()
      if (id != null && id != playerAsset.id) {
        return reloadWebPlayer(webPlayerWrapper, playerAsset, autoPlayable,
            context, playerCallbacks)
      } else {
        Logger.w(VideoRequester.VIDEO_DEBUG, "Same webPlayer is return for the same base asset")
        return webPlayerWrapper
      }
    }

    if (webPlayerCacheList.size < 2 && !webPlayerCacheList.containsKey(autoPlayable)) {
      return createWebPlayer(autoPlayable, context, playerCallbacks, playerAsset)
    }

    webPlayerCacheList.forEach {
      Logger.d(VideoRequester.VIDEO_DEBUG, "Checking for view ${it.key.hashCode()} and its position ${it.key.getPositionInList()}")
      if (autoPlayManager?.canExchangeAutoPlayer(autoPlayable, it.key) == true) {
        Logger.d(VideoRequester.VIDEO_DEBUG, "Reassigning the player for ${it.key.hashCode()} and its position ${it.key.getPositionInList()}")
        val webPlayerWrapper = reloadWebPlayer(it.value, playerAsset, autoPlayable, context, playerCallbacks)
        it.key.resetVideoState()
        webPlayerCacheList.remove(it.key)
        webPlayerCacheList.put(autoPlayable, webPlayerWrapper)
        return webPlayerWrapper
      } else {
        Logger.d(VideoRequester.VIDEO_DEBUG, "Cannot reassign the player for ${it.key.hashCode()} and its position ${it.key.getPositionInList()}")
      }
    }

    return null
  }


  private fun createWebPlayer(autoPlayable: AutoPlayable, context: Context,
                              playerCallbacks: PlayerCallbacks?, playerAsset: PlayerAsset):
      WebPlayerWrapper {
    Logger.i(VideoRequester.VIDEO_DEBUG, "Creating the new webplayer for id = ${playerAsset.id}")
    val view = LayoutInflater.from(context).inflate(R.layout.layout_item_dh_webplayer, null)
    val webPlayerWrapper = view.findViewById<WebPlayerWrapper>(R.id.frame_layout_holder)
    webPlayerWrapper.loadVideo(playerAsset, true)
    updateWebplayerInCache(autoPlayable, webPlayerWrapper)
    return webPlayerWrapper
  }

  fun reloadWebPlayer(webPlayerWrapper: WebPlayerWrapper, playerAsset: PlayerAsset,
                      autoPlayable: AutoPlayable, context: Context,
                     playerCallbacks: PlayerCallbacks?) : WebPlayerWrapper {
    Logger.e(VideoRequester.VIDEO_DEBUG, "releasing the video for ${webPlayerWrapper.getAssetId()}")
    webPlayerWrapper.releasePlayer()
    webPlayerCacheList.remove(autoPlayable)
    return createWebPlayer(autoPlayable, context, playerCallbacks, playerAsset)
  }

  fun releaseVideo(autoPlayable: AutoPlayable, isDetailShowing: Boolean) {
    Logger.w(VideoRequester.VIDEO_DEBUG, "Releasing the video for ${autoPlayable.hashCode()}")
    val webPlayerWrapper = webPlayerCacheList.get(autoPlayable)
    if (webPlayerWrapper != null) {
      Logger.e(VideoRequester.VIDEO_DEBUG, "releasing the video for ${webPlayerWrapper.getAssetId()}")
      if (!isDetailShowing)
        webPlayerWrapper.releasePlayer()
    }
    webPlayerCacheList.remove(autoPlayable)
  }

  fun clearPlayerInstances(){
    webPlayerCacheList.forEach {
      it.key.resetVideoState()
      it.value.releasePlayer()
    }
    webPlayerCacheList.clear()
  }
}