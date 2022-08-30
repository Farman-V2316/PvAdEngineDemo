/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.coolfie_exo.download

import android.content.Context
import android.net.Uri
import com.dailyhunt.tv.exolibrary.download.others.ExecHelper
import com.dailyhunt.tv.exolibrary.download.ExoCacheListener
import com.dailyhunt.tv.exolibrary.download.ExoLeastRecentCacheEvictor
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.MediaItem
import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.ItemCacheType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * @Author VINOD BC.
 *
 *  Exo Cache Helper
 */
object ExoCacheHelper : ExoCacheListener {
    private const val TAG = "ExoCacheHelper"
    private lateinit var DOWNLOAD_CONTENT_DIRECTORY: String
    private const val FILE_SIZE: Long = 5 * 1024 * 1024
    private var databaseProvider: DatabaseProvider? = null
    private var downloadDirectory: File? = null
    private var simpleCache: Cache? = null
    private var cacheEvictor: ExoLeastRecentCacheEvictor? = null
    private var execHelper = ExecHelper()

    init {
        setDirectoryName()
    }

    @Synchronized
    fun initCache() {
        getCache(CommonUtils.getApplication(), ItemCacheType.PREFETCH)
    }

    fun buildCacheDataSource(upstreamFactory: DataSource.Factory, context: Context,
                             uri: Uri
    ): DataSource.Factory {
        return CacheDataSourceFactory(
                getSimpleCache(context),
                upstreamFactory,
                FileDataSource.Factory(),
                CacheDataSinkFactory(getSimpleCache(context), CacheDataSink
                        .DEFAULT_FRAGMENT_SIZE),
                CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                EventListener(uri))
    }


    /**
     * Used to starting caching a video
     */
    private fun cacheVideo(mediaItem: MediaItem, context: Context) {
        Logger.d(TAG, "cacheVideo contentId : ${mediaItem.contentId} Uri : ${mediaItem.uri}")
        CacheUtil.cache(
                getDataSpec(mediaItem.uri),
                getSimpleCache(context),
                getDataSource(context),
                ProgressListener(mediaItem),
                null
        )
    }

    fun getDataSpec(videoUri: Uri): DataSpec {
        //This will make cache() download only the first 100KB of the file.
        return DataSpec(videoUri, 0, FILE_SIZE, null)
    }

    fun getDataSource(context: Context): DataSource {
        return DefaultDataSourceFactory(context, Util.getUserAgent(context, MediaSourceUtil.getApplicationName(context)))
                .createDataSource()
    }

    private class ProgressListener(val mediaItem: MediaItem) : CacheUtil.ProgressListener {
        override fun onProgress(requestLength: Long, bytesCached: Long, newBytesCached: Long) {
            val downloadPercentage: Double = (bytesCached * 100.0
                    / requestLength)
            Logger.d(TAG, "onProgress requestLength : $requestLength bytesCached : $bytesCached " +
                    "newBytesCached $newBytesCached")
            Logger.d(TAG, "onProgress contentId : ${mediaItem.contentId} " +
                    "downloadPercentage :$downloadPercentage")
        }
    }

    fun isMediaItemPresentInCache(mediaItem: MediaItem?): Boolean {
        if (mediaItem?.uri == null || mediaItem?.contentId == null) {
            return false
        }

        return getCacheEvictor(mediaItem.cacheType).isMediaPresent(mediaItem.uri.toString())
    }

    private fun removeMediaItemFromCache(mediaItem: MediaItem?) {
        mediaItem?.let {
            Logger.d(TAG, "removeMediaItemFromCache contentId : ${mediaItem?.contentId}" +
                    " cacheType : ${mediaItem?.cacheType}")
            val cacheStatus = isMediaItemPresentInCache(mediaItem)
            Logger.d(TAG, "removeMediaItemFromCache cacheStatus : $cacheStatus URL :" +
                    " ${mediaItem.uri}")
            if (cacheStatus) {
//                var cache = getSimpleCache(CommonUtils.getApplication())
//                if (Logger.loggerEnabled()) {
//                    for (key in cache.keys) {
//                        Logger.d(TAG, "Before key : $key")
//                    }
//                }

                val httpDataSourceFactory = ExoDownloadHelper.getHttpDataSourceFactory(CommonUtils.getApplication(),
                        mediaItem)
                val downloaderConstructorHelper = ExoDownloadHelper.getDownloadConstructorHelper(CommonUtils.getApplication(), mediaItem, httpDataSourceFactory)
                val currentDownloader = HlsDownloader(
                        mediaItem.uri,
                        Collections.singletonList(StreamKey(
                                HlsMasterPlaylist.GROUP_INDEX_VARIANT, 0)),
                        downloaderConstructorHelper)
                try {
                    currentDownloader?.remove()
                } catch (e: Exception) {
                    Logger.d(TAG, "removeMediaItemFromCache Exception : ${e.message}")
                }

//                if (Logger.loggerEnabled()) {
//                    for (key in cache.keys) {
//                        Logger.d(TAG, "After key : $key")
//                    }
//                }
                Logger.d(TAG, "removeMediaItemFromCache After Cache Status :" + " ${isMediaItemPresentInCache(mediaItem)}")
            } else {
                Logger.d(TAG, "removeMediaItemFromCache Video not present in Cache URL : " +
                        "${mediaItem.uri}")
            }
        }
    }

    fun clearCache(context: Context, cacheType: ItemCacheType) {
        //TODO::cache.release to try instead of removeSpan
        execHelper.runIO {
            val cache = getCache(context, cacheType)
            Logger.d(TAG, "clearCache cacheType : $cacheType cacheSpace : ${cache.cacheSpace}" +
                    " keys : ${cache.keys}")
            for (key in cache.keys) {
                for (cacheSpan in cache.getCachedSpans(key)) {
                    cache.removeSpan(cacheSpan)
                }
            }
            Logger.d(TAG, "clearCache After cacheType : $cacheType cacheSpace : ${cache.cacheSpace}" +
                    " keys : ${cache.keys}")
        }
    }

    fun getSimpleCache(context: Context): Cache {
//        Logger.d(TAG, "getSimpleCache contentId : ${mediaItem?.contentId} cacheType : " +
//                "${mediaItem?.cacheType}, url : ${mediaItem?.url}")
        return getCache(context, ItemCacheType.PREFETCH)
    }

    private fun getCacheEvictor(cacheType: ItemCacheType?): ExoLeastRecentCacheEvictor {
//        Logger.d(TAG, "getCacheEvictor cacheType : $cacheType")
        if (simpleCache == null) {
            Logger.d(TAG, "getCacheEvictor cacheEvictor : $cacheType")
            cacheEvictor = ExoLeastRecentCacheEvictor(
                    (CacheConfigHelper.cacheDirectoryMaxSizeInMB * 1024 * 1024).toLong(),
                    cacheType?.type ?: "network")
        }
        return cacheEvictor!!
    }

    private fun getCache(context: Context, cacheType: ItemCacheType?): Cache {
//        Logger.d(TAG, "getCache cacheType : $cacheType")
        if (simpleCache == null) {
            Logger.d(TAG, "simpleCache CacheType : $cacheType")
            val downloadContentDirectory = File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            simpleCache = SimpleCache(downloadContentDirectory, getCacheEvictor(cacheType), getDatabaseProvider(context))
            //          new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor(), getDatabaseProvider(context));
            if (Logger.loggerEnabled()) {
                Logger.d(TAG, "simpleCache Path : " + downloadContentDirectory.absolutePath)
                Logger.d(TAG, "simpleCache cacheSpace : ${simpleCache?.cacheSpace}")
                Logger.d(TAG, "simpleCache Size : ${simpleCache?.keys?.size}")
            }
        }
        return simpleCache!!
    }

    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        if (databaseProvider == null) {
            databaseProvider = ExoDatabaseProvider(context)
        }
        return databaseProvider!!
    }

    private fun setDirectoryName() {
        DOWNLOAD_CONTENT_DIRECTORY = "exo_cache"
    }

    private fun getDownloadDirectory(): File {
        if (downloadDirectory == null) {
            downloadDirectory = CommonUtils.getApplication().getExternalFilesDir(null)
            if (downloadDirectory == null) {
                downloadDirectory = CommonUtils.getApplication().filesDir
            }
            Logger.d(TAG, "getDownloadDirectory Path : ${downloadDirectory!!.absolutePath}")
        }
        return downloadDirectory!!
    }

    private class EventListener(val uri: Uri) : CacheDataSource.EventListener {
        override fun onCachedBytesRead(cacheSizeBytes: Long, cachedBytesRead: Long) {
            Logger.d(TAG, "uri : $uri onCachedBytesRead cacheSizeBytes : $cacheSizeBytes" +
                    " cachedBytesRead : " + cachedBytesRead)
        }

        override fun onCacheIgnored(reason: Int) {
            Logger.e(TAG, "onCacheIgnored onCacheIgnored reason : $reason")
        }
    }

    override fun deleteFromCache(mediaItem: MediaItem?) {
        Logger.d(TAG, "deleteFromCache asset : ${mediaItem?.contentId}")
        mediaItem?.let {
            execHelper.runIO {
                removeMediaItemFromCache(it)
            }
        }
    }

    override fun deleteFromCache(assetList: ConcurrentHashMap<String, MediaItem>) {
        Logger.d(TAG, "deleteFromCache assetList : ${assetList.size}")
    }

    override fun isMediaPresentInCacheDirectory(mediaItem: MediaItem?): Boolean {
        mediaItem?.let {
            return isMediaItemPresentInCache(it)
        }
        return false
    }

}

