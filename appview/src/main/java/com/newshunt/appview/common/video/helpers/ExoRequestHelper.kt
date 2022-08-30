/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.helpers

import com.bwutil.BwEstRepo
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.BaseMediaItem
import com.dailyhunt.tv.exolibrary.entities.MediaItem
import com.dailyhunt.tv.players.utils.MediaUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ConfigType
import com.newshunt.dataentity.common.asset.StreamCacheStatus
import com.newshunt.dataentity.common.asset.VideoAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.connection.ConnectionManager
import java.util.concurrent.ConcurrentHashMap

/**
 * @author vinod.bc
 */
object ExoRequestHelper : ExoDownloadHelper.VideoCacheListener {

    private const val TAG = "ExoDownload::ExoRequestHelper"

    //Use this map for adding - prefetched videos
    private var itemListMap = ConcurrentHashMap<String, CommonAsset>()
    private var completedListMap = ConcurrentHashMap<String, CommonAsset>()

    init {
        itemListMap.clear()
        completedListMap.clear()
    }
    fun isPresentInRequestQueue(contentId: String) : Boolean {
        return itemListMap.containsKey(contentId)
    }

    fun prefetchVideo(position: Int, asset: CommonAsset?, configType: ConfigType) {
        prefetchVideo(position, asset, false, configType)
    }

    fun prefetchVideo(position: Int, asset: CommonAsset?, isCurrentPlaying: Boolean, configType: ConfigType) {
        if (CacheConfigHelper.disableCache) {
            Logger.d(TAG, "prefetchVideo() disableCache == true")
            return
        }
        if (position < 0 || asset == null) {
            return
        }

        if (isCurrentPlaying) {
            //Check do we have enough prefetch available
            var streamCachedDuration = getStreamCachedDuration(asset?.i_id())
            if(streamCachedDuration == 0f) {
                streamCachedDuration = asset?.i_videoAsset()?.streamCachedDuration ?: 0F
            }
            val prefetchDurationInSec = CacheConfigHelper.getPrefetchDurationConfig(asset?.i_videoAsset())
            Logger.d(TAG, "isCurrentPlaying = true, streamCachedDuration : $streamCachedDuration, prefetchDurationInSec : $prefetchDurationInSec")
            if (streamCachedDuration < prefetchDurationInSec) {
                Logger.d(TAG, "prefetchVideo() isCurrentPlaying -> forceDownloadPlayingItem")
                asset.let {
                    if(!itemListMap.containsKey(asset.i_id())) {
                        asset.i_videoAsset()?.configType = configType
                        itemListMap[asset.i_id()] = asset
                    }
                    ExoDownloadHelper.forceDownloadPlayingItem(MediaUtil.getMediaItem(asset, false))
                    Logger.d(TAG, "forceDownloadPlayingItem" + asset?.i_id() +  " title : " + asset?.i_title())
                }
            } else {
                Logger.d(
                    TAG, "prefetchVideo() isCurrentPlaying -> we have enough prefetch" +
                        " streamCachedDuration : $streamCachedDuration")
            }
        } else {
            Logger.d(TAG, "prefetchVideo() adding to Download Queue is : " + asset?.i_id() + " title : " + asset?.i_title())
            asset.let {
                asset.i_videoAsset()?.configType = configType
                itemListMap[asset.i_id()] = asset
                Logger.d(TAG, "downloadStream id : " + asset?.i_id()  + " title : " + asset?.i_title())
                ExoDownloadHelper.downloadStream(MediaUtil.getMediaItem(it, false))
            }
        }
    }

    fun remainingToPrefetch(configType: ConfigType) : Int {
        val connectionSpeed = BwEstRepo.currentConnectionQuality()
        val prefetchConfigCount = CacheConfigHelper.getNumberOfVideoToPrefetch(configType, connectionSpeed)
        val prefetchedCount = getItemCount(configType)
        var numberOfStreamToCacheConfig = prefetchConfigCount- prefetchedCount
        Logger.d(TAG, "configType : $configType, prefetchConfigCount : $prefetchConfigCount, prefetchedCount : $prefetchedCount, numberOfStreamToCacheConfig : $numberOfStreamToCacheConfig")
        return numberOfStreamToCacheConfig
    }

    fun start() {
        ExoDownloadHelper.addListener(this)
    }

    fun reset() {
        ExoDownloadHelper.removeListener(this)
    }

    fun destroy(configType: ConfigType?) {
        reset()
        ExoDownloadHelper.resetDownloadQueue()
        clearCachedItems(configType)
    }

    fun clearDetailCachedItems() {
        Logger.d(TAG, "clearDetailCachedItems")
        for(item in itemListMap) {
            if(item.value?.i_videoAsset()?.configType == ConfigType.VIDEO_DETAIL_V) {
                itemListMap.remove(item.key)
                ExoDownloadHelper.removeStreamFromList(item.key)
            }
        }
        for(item in completedListMap) {
            if(item.value?.i_videoAsset()?.configType == ConfigType.VIDEO_DETAIL_V) {
                completedListMap.remove(item.key)
            }
        }
        ExoDownloadHelper.clearCacheListeners()
    }

    fun clearCachedItems(configType: ConfigType?) {
        Logger.d(TAG, "clearCachedItems configType = " + configType)
        if(configType != null) {
            for(item in itemListMap) {
                if(item.value?.i_videoAsset()?.configType == configType) {
                    itemListMap.remove(item.key)
                }
            }
            for(item in completedListMap) {
                if(item.value?.i_videoAsset()?.configType == configType) {
                    completedListMap.remove(item.key)
                }
            }
        } else {
            itemListMap.clear()
            completedListMap.clear()
            ExoDownloadHelper.clearCacheListeners()
        }
    }

    override fun updateVideoUrlFromDownload(mediaItem: BaseMediaItem?,
                                            cacheStatus: ExoDownloadHelper.CacheStatus) {
        if (mediaItem == null || mediaItem.contentId.isNullOrEmpty() || mediaItem.uri == null) {
            return
        }

        var streamCacheStatus = getStreamCachedStatus(cacheStatus)
        Logger.d(
            TAG, "markVideoAsStreamCached id : ${mediaItem.contentId} " +
                "streamCacheStatus : " + "$streamCacheStatus")
        markVideoAsStreamCached(mediaItem, streamCacheStatus, true)
    }

    override fun updateVideoUrlFromExo(mediaItem: BaseMediaItem?,
        cacheStatus: ExoDownloadHelper.CacheStatus) {
        //Do Nothing
    }

    override fun updateVideoCachedPercentage(mediaItem: BaseMediaItem?,
        percentage: Float, downloadedVideoDuration: Float) {
        if (CommonUtils.isEmpty(mediaItem?.contentId)) {
            Logger.d(TAG, "updateVideoCachedPercentage isEmpty id = ${mediaItem?.contentId}")
            return
        }
        val videoAsset = itemListMap[mediaItem?.contentId]?.i_videoAsset()
        videoAsset?.streamDownloadPercentage = percentage
        videoAsset?.streamCachedDuration = downloadedVideoDuration
        Logger.d(TAG, "percentage : $percentage")
        Logger.d(TAG, "downloadedVideoDuration : $downloadedVideoDuration")
    }

    /**
     * Only for log enabled builds
     */
    override fun updateDownloadException(mediaItem: BaseMediaItem?, errorMsg: String?) {
        if (CommonUtils.isEmpty(mediaItem?.contentId)) {
            Logger.d(TAG, "updateDownloadException isEmpty id = ${mediaItem?.contentId}")
            return
        }
        val videoAsset = itemListMap[mediaItem?.contentId]?.i_videoAsset()
        videoAsset?.downloadErrorMsg = errorMsg
    }

    private fun markVideoAsStreamCached(
        mediaItem: BaseMediaItem, streamCacheStatus: StreamCacheStatus, forceVariant: Boolean) {
        Logger.d(TAG, "Video Item marked as stream cached id = ${mediaItem.contentId}")
        if (CommonUtils.isEmpty(mediaItem.contentId)) {
            Logger.d(TAG, "markVideoAsStreamCached isEmpty id = ${mediaItem.contentId}")
            return
        }
        //Update streamCacheUrl and variantIndex to particular item
        val videoAsset = itemListMap[mediaItem.contentId]?.i_videoAsset()
        videoAsset?.streamCachedUrl = mediaItem.uri.toString()
        videoAsset?.variantIndex = mediaItem.variantIndex
        videoAsset?.streamCachedStatus = streamCacheStatus
        videoAsset?.isForceVariant = forceVariant

        videoAsset?.streamCachedDuration = (mediaItem as MediaItem)?.streamCacheDuration
        if (streamCacheStatus == StreamCacheStatus.PARTIAL ||
                streamCacheStatus == StreamCacheStatus.COMPLETE) {
            //Add to completed List
            if (itemListMap.containsKey(mediaItem.contentId)) {
                completedListMap[mediaItem.contentId] = itemListMap[mediaItem.contentId]!!
            }
        }
    }

    fun getStreamCachedStatus(cacheStatus: ExoDownloadHelper.CacheStatus): StreamCacheStatus {
        var streamCacheStatus = StreamCacheStatus.NOT_DOWNLOADED
        when (cacheStatus) {
            ExoDownloadHelper.CacheStatus.COMPLETE -> {
                streamCacheStatus = StreamCacheStatus.COMPLETE
            }
            ExoDownloadHelper.CacheStatus.PARTIAL -> {
                streamCacheStatus = StreamCacheStatus.PARTIAL
            }
            ExoDownloadHelper.CacheStatus.STARTED -> {
                streamCacheStatus = StreamCacheStatus.STARTED
            }
        }
        return streamCacheStatus
    }

    fun getStreamCachedDuration(itemId: String?): Float {
        if(CommonUtils.isEmpty(itemId)) {
            return 0F
        }
        return itemListMap?.get(itemId)?.i_videoAsset()?.streamCachedDuration ?: 0F
    }

    fun getStreamCachedUrl(itemId: String?): String? {
        if(CommonUtils.isEmpty(itemId)) {
            return null
        }
        return itemListMap?.get(itemId)?.i_videoAsset()?.streamCachedUrl
    }

    fun getSelectedQuality(itemId: String?): String? {
        if(CommonUtils.isEmpty(itemId)) {
            return null
        }
        return itemListMap?.get(itemId)?.i_videoAsset()?.selectedQuality
    }

    fun getStreamCachedPercentage(itemId: String?): Float {
        if(CommonUtils.isEmpty(itemId)) {
            return 0F
        }
        return itemListMap?.get(itemId)?.i_videoAsset()?.streamDownloadPercentage ?: 0F
    }

    fun isItemAdded(itemId: String?): Boolean {
        if(CommonUtils.isEmpty(itemId)) {
            return false
        }
        return itemListMap?.containsKey(itemId)
    }

    fun isItemPrefetched(itemId: String?): Boolean {
        if(CommonUtils.isEmpty(itemId)) {
            return false
        }
        return (completedListMap[itemId]?.i_videoAsset()?.streamCachedDuration ?: 0F) > 0F
    }

    fun getItemCount(configType: ConfigType): Int {
        var count = 0
        for(item in itemListMap) {
            if(item.value?.i_videoAsset()?.configType == configType) {
                count++
            }
        }
        return count
    }

    fun getCompletedPrefetchCount(configType: ConfigType) : Int {
        var count = 0
        for(item in completedListMap) {
            if(item.value?.i_videoAsset()?.configType == configType) {
                count++
            }
        }
        return count
    }
}