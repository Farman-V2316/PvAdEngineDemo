/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.utils

import android.app.Activity
import android.content.Context
import android.view.View
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.helper.TVConstants
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.asset.VideoAsset
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType
import com.newshunt.dataentity.news.model.entity.server.asset.SourceInfo
import com.newshunt.dhutil.helper.PlayerDataProvider
import com.newshunt.helper.player.PlayerControlHelper
import kotlinx.coroutines.CoroutineScope
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created on 08/28/2019.
 */
object DHVideoUtils {
    private val LOG_TAG = "DHVideoUtils"
    private val PREFETCH_PATTERN = "{1}.m3u8"
    @JvmStatic
    fun hideSystemUI(activity: Activity): Int {
        val mDecorView = activity.window?.decorView
        val systemUIVisibilityFlag = mDecorView?.systemUiVisibility ?: 0
        // Set the IMMERSIVE flag.Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        return systemUIVisibilityFlag
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @JvmStatic
    fun showSystemUI(activity: Activity, systemUIVisibilityFlag: Int) {
        val decorView = activity.window?.decorView
        decorView?.systemUiVisibility = systemUIVisibilityFlag
    }

    fun getPlayerAsset(asset: CommonAsset?): PlayerAsset? {
        if (asset?.i_videoAsset() == null) {
            return null
        }
        return getPlayerAsset(asset, PlayerControlHelper.isListMuteMode)
    }

    private fun getPlayerAsset(asset: CommonAsset, muteMode: Boolean): PlayerAsset? {
        if (asset.i_videoAsset() == null) {
            Logger.e(LOG_TAG, "VideoAsset cannot be Null")
            return null
        }
        val item = asset.i_videoAsset()!!
        val source = asset.i_source()
        val langCode = asset.i_langCode()

        //Using PostId as video assetId
        return if (isExoPlayer(item)) {
            ExoPlayerAsset(asset.i_id(), getPlayerType(item), getSourceInfo(source),
                    PlayerUtils.getQualifiedVideoUrl(item), item.autoplayable,
                    item.srcVideoId, (item.videoDurationInSecs.times(1000).toLong()), item.useEmbed,
                    item.width, item.height, item.isGif, item.loopCount, item.hideControl,
                    item.liveStream, item.applyPreBufferSetting, item.adUrl, item.playerType,
                    "", "", "",
                    item.disableAds, muteMode, item.replaceableParams, "",
                    langCode, "", "", null)
        } else PlayerAsset(asset.i_id(), getPlayerType(item), getSourceInfo(source),
                PlayerUtils.getQualifiedVideoUrl(item), item.autoplayable, item.srcVideoId,
                (item.videoDurationInSecs.times(1000).toLong()), item.useEmbed, item.width,
                item.height, muteMode, item.replaceableParams,
                langCode, "", "", "")

    }

    fun isExoPlayer(item: VideoAsset?): Boolean {
        item.let {
            return (PlayerType.M3U8.name.equals(item?.playerType, ignoreCase = true) ||
                    PlayerType.MP4.name.equals(item?.playerType, ignoreCase = true) ||
                    PlayerType.DASH.name.equals(item?.playerType, ignoreCase = true))
        }
        return false
    }

    fun isEmbedPlayer(item: PlayerAsset?): Boolean {
        item.let {
            return (PlayerType.DH_WEBPLAYER == item?.type) ||
                    (PlayerType.DH_EMBED_WEBPLAYER == item?.type)
        }
        return false
    }

    fun isEmbedPlayer(commonAsset: CommonAsset?): Boolean {
        commonAsset.let {
            return isEmbedPlayer(getPlayerAsset(commonAsset))
        }
        return false
    }

    fun isYoutubePlayer(item: VideoAsset?): Boolean {
        item.let {
            return (PlayerType.YOUTUBE.name.equals(item?.playerType, ignoreCase = true))
        }
        return false
    }

    fun isAutoPlaySupported(item: VideoAsset?): Boolean {
        item.let {
            return (PlayerType.M3U8.name.equals(item?.playerType, ignoreCase = true) ||
                    PlayerType.MP4.name.equals(item?.playerType, ignoreCase = true) ||
                    PlayerType.DASH.name.equals(item?.playerType, ignoreCase = true) ||
                    PlayerType.DH_EMBED_WEBPLAYER.name.equals(item?.playerType, ignoreCase = true) ||
                    PlayerType.DH_WEBPLAYER.name.equals(item?.playerType, ignoreCase = true))
        }
        return false
    }

    fun isEligibleToPrefetch(asset: CommonAsset?): Boolean {
        if (CacheConfigHelper.disableCache) {
            return false
        }
        return  asset?.i_videoAsset()?.isPrefetch == true
                && asset?.i_videoAsset()?.url?.contains(PREFETCH_PATTERN) == true
                && asset?.i_videoAsset()?.liveStream == false
                && asset?.i_uiType() == UiType2.AUTOPLAY
                && isExoPlayer(asset?.i_videoAsset())
    }

    fun isEligibleToPrefetchInDetail(asset: CommonAsset?): Boolean {
        if (CacheConfigHelper.disableCache) {
            return false
        }
        return  asset?.i_videoAsset()?.isPrefetch == true
                && asset?.i_videoAsset()?.url?.contains(PREFETCH_PATTERN) == true
                && asset?.i_videoAsset()?.liveStream == false
                && isExoPlayer(asset?.i_videoAsset())
    }

    private fun getPlayerType(item: VideoAsset): PlayerType {
        if (item.playerType.equals(PlayerType.YOUTUBE.name, ignoreCase = true)) {
            return PlayerType.YOUTUBE
        } else if (item.playerType.equals(PlayerType.M3U8.name, ignoreCase = true)) {
            return PlayerType.M3U8
        } else if (item.playerType.equals(PlayerType.MP4.name, ignoreCase = true)) {
            return PlayerType.MP4
        } else if (item.playerType.equals(PlayerType.DASH.name, ignoreCase = true)) {
            return PlayerType.DASH
        } else if (item.playerType.equals(PlayerType.FACEBOOK.name, ignoreCase = true)) {
            return PlayerType.FACEBOOK
        } else if (item.playerType.equals(PlayerType.DAILYMOTION.name, ignoreCase = true)) {
            return PlayerType.DAILYMOTION
        } else if (item.playerType.equals(PlayerType.DH_EMBED_WEBPLAYER.name, ignoreCase = true)) {
            return PlayerType.DH_EMBED_WEBPLAYER
        } else if (item.playerType.equals(PlayerType.DH_WEBPLAYER.name, ignoreCase = true)) {
            return PlayerType.DH_WEBPLAYER
        }
        return PlayerType.YOUTUBE
    }

    private fun getSourceInfo(sourceAsset: PostSourceAsset?): SourceInfo {
        val sourceInfo = SourceInfo()
        if (sourceAsset != null) {
            sourceInfo.sourceName = sourceAsset.sourceName
            sourceInfo.sourceId = sourceAsset.id
            sourceInfo.sourceType = sourceAsset.entityType
            sourceInfo.sourceSubType = sourceAsset.type
            sourceInfo.legacyKey = sourceAsset.legacyKey
            sourceInfo.playerKey = sourceAsset.playerKey
        }
        return sourceInfo
    }

    fun getVideoList(): List<PostEntity> {
        val type = object : TypeToken<List<PostEntity>>() {}.type
        val videoList = Gson().fromJson(readFromAsset("video_asset.json",
                CommonUtils.getApplication()), type) as List<PostEntity>
        return videoList
    }


    fun readFromAsset(fileName: String, context: Context): String {
        val returnString = StringBuilder()
        var fIn: InputStream? = null
        var isr: InputStreamReader? = null
        var input: BufferedReader? = null
        try {
            fIn = context.assets.open(fileName)
            isr = InputStreamReader(fIn)
            input = BufferedReader(isr)
            var line = ""
            line = input.readLine()
            while (line != null) {
                returnString.append(line)
                line = input.readLine()
            }
        } catch (e: Exception) {
            e.message
        } finally {
            try {
                isr?.close()
                fIn?.close()
                input?.close()
            } catch (e2: Exception) {
                e2.message
            }

        }
        return returnString.toString()
    }

    fun isVideoCarousel(card: CommonAsset?): Boolean {
        if(card?.i_subFormat() == SubFormat.VIDEO && (card?.i_uiType() == UiType2.CAROUSEL_1
                        || card?.i_uiType() == UiType2.CAROUSEL_7 || card?.i_uiType() == UiType2.CAROUSEL_8)) {
            return true
        }
        return false
    }

}