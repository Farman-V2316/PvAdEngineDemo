/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.utils

import android.net.Uri
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.MediaItem
import com.newshunt.dataentity.common.asset.ItemCacheType
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.connection.ConnectionManager

object MediaUtil {

    @JvmStatic
    fun getMediaItem(asset: CommonAsset): MediaItem? {
        return getMediaItem(asset, false)
    }

    @JvmStatic
    fun getMediaItem(asset: CommonAsset, play: Boolean): MediaItem? {
        if (CommonUtils.isEmpty(asset.i_videoAsset()?.url)) {
            return null
        }

        val videoAsset = asset.i_videoAsset()!!
        var videoUrl = PlayerUtils.getQualifiedVideoUrl(videoAsset)
        var mediaItem = MediaItem(
            Uri.parse(videoUrl), asset?.i_id(), play, false, videoAsset.liveStream
        )
        mediaItem.isVideoPlayLocally = (videoAsset.localVideoFilePath != null)
        mediaItem.itemIndex = videoAsset?.itemIndex ?: 0

        mediaItem.title = asset?.i_title()

        //TODO[umesh.isran] - connection quality, isNlfcItem
        mediaItem.isNlfcItem = false
        mediaItem.selectedConnectionQuality =
            ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication())?.name
        mediaItem.prefetchDuration = CacheConfigHelper.getPrefetchDurationConfig(asset?.i_videoAsset())
        mediaItem.contentDuration = asset?.i_videoAsset()?.videoDurationInSecs ?: 0
        mediaItem.setCacheType(ItemCacheType.PREFETCH, 0F)
        mediaItem.setStreamCachedUrl(videoAsset.streamCachedUrl, videoAsset.streamDownloadPercentage ?: 0f,
            videoAsset.variantIndex ?: 0, videoAsset.isForceVariant == true)
        return mediaItem
    }
}