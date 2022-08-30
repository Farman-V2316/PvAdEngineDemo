/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.coolfie_exo.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.dailyhunt.tv.exolibrary.download.HlsManifestDownloader
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.BaseMediaItem
import com.dailyhunt.tv.exolibrary.entities.MediaItem
import com.dailyhunt.tv.exolibrary.entities.PlayerState
import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.offline.DownloadException
import com.google.android.exoplayer2.offline.Downloader
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.ItemCacheType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.set

/**
 * @author vinod.bc
 */
object ExoDownloadHelper {
    private const val TAG = "ExoDownloadHelper"
    private var downloadQueue: HashMap<String, MediaItem> = LinkedHashMap() // DownloadWaiting Queue
    private var downloadPartialList: HashMap<String, MediaItem> = LinkedHashMap() // DownloadedList
    private var currentDownloader: Downloader? = null
    private var currentMediaItem: MediaItem? = null
    private var currentDownloadJob: kotlinx.coroutines.Job? = null

    private fun isCurrentItemDownloading(playingItem: MediaItem?): Boolean {
        if (currentMediaItem != null && playingItem != null &&
                currentMediaItem?.contentId?.equals(playingItem.contentId) == true) {
            return true
        }
        return false
    }

    fun forceDownloadPlayingItem(currentItem: MediaItem?) {
        Logger.d(TAG, "forceDownloadPlayingItem mediaItem : ${currentItem?.contentId}")
        if (currentItem == null || CommonUtils.isEmpty(currentItem.contentId)) {
            return
        }

        if (isCurrentItemDownloading(currentItem)) {
            Logger.d(TAG, "forceDownloadPlayingItem Current Video in downloading state :"
                    + " ${currentItem?.contentId}")
            return
        }

        if (Logger.loggerEnabled()) {
            Logger.d(TAG, "forceDownloadPlayingItem StartDownload contentId : " +
                    "${currentItem?.contentId}")
            Logger.d(TAG, "forceDownloadPlayingItem StartDownload URI : " +
                    "${currentItem?.uri}")
        }

        currentDownloadJob = GlobalScope.launch(Dispatchers.IO) {
            if (ExoCacheHelper.isMediaItemPresentInCache(currentItem) &&
                    currentItem.streamCachedPercentage >= 100.0f) {
                Logger.d(TAG, "forceDownloadPlayingItem mediaItem streamCachedPercentage 100%")
                return@launch
            }

            currentMediaItem = currentItem
            try {
                if (isDownloadHighBitrate(currentItem)) {
                    getHlsManifest(CommonUtils.getApplication(), currentItem)
                } else {
                    startDownloader(CommonUtils.getApplication(), currentItem)
                }
            } catch (e: Exception) {
                Logger.d(TAG, "currentDownloadJob Exception: ${e.message}")
            }
        }
    }

    fun downloadStream(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            return
        }
        Logger.d(TAG, "downloadStream downloadQ size: ${downloadQueue.size}")
        Logger.d(TAG, "downloadStream : ${mediaItem.contentId}, cacheType : " +
                "${mediaItem.cacheType} nlfc : ${mediaItem.isNlfcItem}")
        if (CommonUtils.isEmpty(mediaItem.contentId)) {
            Logger.d(TAG, "return contentId null / empty")
            return
        }

        if (ExoCacheHelper.isMediaItemPresentInCache(mediaItem) &&
                mediaItem.streamCachedPercentage >= 100.0f) {
            Logger.d(TAG, "downloadStream mediaItem.uri present in cache % : ${mediaItem.streamCachedPercentage}")
            return
        } else {
            Logger.d(TAG, "downloadStream mediaItem.uri Not present in cache % : ${mediaItem.streamCachedPercentage}")
        }

        if (mediaItem.cacheType == ItemCacheType.PREFETCH && mediaItem.prefetchDuration <= 0) {
            Logger.d(TAG, "downloadStream return prefetchDuration : ${mediaItem.prefetchDuration}")
            return
        }

        var extension = MediaSourceUtil.getContentType(mediaItem.uri)
        if (extension != C.TYPE_HLS) {
            Logger.d(TAG, "downloadStream Only Hls download supported")
            Logger.d(TAG, "return Uri : ${mediaItem.uri}")
            return
        }

        downloadQueue[mediaItem.contentId!!] = mediaItem
        Logger.d(TAG, "downloadStream added to Queue")
        if (currentMediaItem == null) {
            downloadVideoFromQueue()
        } else {
            Logger.d(TAG, "Already download happening - schedule later")
        }
    }

    fun cancelAndClearDownloadQueue() {
        Logger.d(TAG, "cancelAndClearDownloadQueue size : ${downloadQueue.size}")
        stopVideoDownload()
        downloadQueue.clear()
        cacheListeners.clear()
        downloadPartialList.clear()
    }

    fun resetDownloadQueue() {
        Logger.d(TAG, "resetDownloadQueue Before : ${downloadQueue.size}")
        if (currentMediaItem?.isCurrentlyPlaying == false) {
            stopVideoDownload()
        }
        downloadQueue.clear()
        Logger.d(TAG, "resetDownloadQueue After : ${downloadQueue.size}")
    }

    fun removeStreamFromDownload(contenId: String) {
        if (CommonUtils.isEmpty(contenId)) {
            return
        }
        Logger.d(TAG, "removeStreamFromDownload contentId : $contenId")
        if (currentMediaItem != null &&
                currentMediaItem?.contentId.equals(contenId)) {
            Logger.d(TAG, "removeStream stopVideoDownload")
            stopVideoDownload()
        }

        if (downloadQueue.containsKey(contenId)) {
            downloadQueue.remove(contenId)
            Logger.d(TAG, "removeStream from downloadQueue : $contenId")
        }
    }

    fun removeStreamFromList(contenId: String) {
        Logger.d(TAG, "removeStream contentId : $contenId")
        if (CommonUtils.isEmpty(contenId)) {
            return
        }
        if (downloadQueue.containsKey(contenId)) {
            downloadQueue.remove(contenId)
            Logger.d(TAG, "removeStream from downloadQueue : $contenId")
        }
        if(downloadPartialList.containsKey(contenId)) {
            downloadPartialList.remove(contenId)
        }
    }

    fun isQueueEmpty(): Boolean {
        return (downloadQueue.size == 0)
    }


    fun resumeVideoDownload() {
        Logger.d(TAG, "resumeVideoDownload contentId : ${currentMediaItem?.contentId}")
        if (currentMediaItem == null) {
            Logger.d(TAG, "resumeVideoDownload check Queue >")
            downloadVideoFromQueue()
        } else {
            Logger.d(TAG, "resumeVideoDownload download inProgress....")
        }
    }

    fun stopVideoDownload() {
        if (currentMediaItem == null) {
            Logger.d(TAG, "stopVideoDownload not running any download")
        } else {
            Logger.d(TAG, "stopVideoDownload contentId : ${currentMediaItem?.contentId}")
        }
        currentMediaItem = null
        currentDownloader?.cancel()
        currentDownloader = null
        currentDownloadJob?.cancel()
        currentDownloadJob = null
    }

    private fun downloadVideoFromQueue() {
        Logger.d(TAG, "downloadVideoFromQueue downloadQueue : " + downloadQueue.size)
        if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
            Logger.d(TAG, "downloadVideoFromQueue No Network!")
            return
        }
        if (downloadQueue.size == 0) {
            Logger.d(TAG, "downloadVideoFromQueue Queue is Empty!")
            return
        }

        if (currentDownloadJob != null) {
            //Stop previous download if any
            stopVideoDownload()
        }

        if (downloadQueue.iterator().hasNext()) {
            var nextItem: MediaItem? = downloadQueue.iterator().next().value
            Logger.d(TAG, "downloadVideoFromQueue nextItem : ${nextItem?.contentId}")
//            while (nextItem != null && (downloadCompleteList.containsKey(nextItem.contentId) ||
//                            downloadPartialList.containsKey(nextItem.contentId))) {
//                Logger.d(TAG, "downloadStream removing already download List of videos " +
//                        " contentId : ${nextItem.contentId}")
//                downloadQueue.remove(nextItem.contentId)
//                nextItem = null //set it null
//                if (downloadQueue.iterator().hasNext()) {
//                    nextItem = downloadQueue.iterator().next().value
//                }
//            }

            if (nextItem != null) {
                if (Logger.loggerEnabled()) {
                    Logger.d(TAG, "downloadVideoFromQueue StartDownload contentId : " +
                            "${nextItem?.contentId}")
                    Logger.d(TAG, "downloadVideoFromQueue StartDownload URI : " +
                            "${nextItem?.uri}")
                }
                currentMediaItem = nextItem
                currentDownloadJob = GlobalScope.launch(Dispatchers.IO) {
                    try {
                        if (isDownloadHighBitrate(nextItem)) {
                            getHlsManifest(CommonUtils.getApplication(), nextItem)
                        } else {
                            startDownloader(CommonUtils.getApplication(), nextItem)
                        }
                    } catch (e: Exception) {
                        Logger.d(TAG, "currentDownloadJob Cancel for : ${nextItem.contentId}")
                    }
                }
            }
        }
    }

    private fun isDownloadHighBitrate(mediaItem: MediaItem): Boolean {
        return CacheConfigHelper.downloadHighBitrateVariant
    }

    private fun getHlsManifest(context: Context, mediaItem: MediaItem) {
        val httpDataSourceFactory = getHttpDataSourceFactory(context, mediaItem)
        val downloaderConstructorHelper = getDownloadConstructorHelper(context, mediaItem,
                httpDataSourceFactory)
        var hlsManifestDownloader = HlsManifestDownloader(mediaItem.uri,
                Collections.singletonList(StreamKey(HlsMasterPlaylist.GROUP_INDEX_VARIANT, 0)),
                downloaderConstructorHelper)
        hlsManifestDownloader.setListener {
            hlsManifestDownloader.cancel()
            Logger.d(TAG, "hlsManifestDownloader contentId : ${mediaItem.contentId}")
            Logger.d(TAG, "hlsManifestDownloader TagSize : ${it.tags.size}")
            var variantIndex = it.tags.size - 1
            if (variantIndex < 0) {
                variantIndex = 0
            }
            Logger.d(TAG, "hlsManifestDownloader playlistIndex : $variantIndex")
            mediaItem.variantIndex = variantIndex
            GlobalScope.launch(Dispatchers.IO) {
                startDownloader(CommonUtils.getApplication(), mediaItem)
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Logger.d(TAG, "Manifest download Start Id : ${mediaItem.contentId}")
                Logger.d(TAG, "Manifest download Start Title : ${mediaItem.title}")
                hlsManifestDownloader.download(null)
            } catch (e: Exception) {
                Logger.d(TAG, "Manifest download Exception : ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun startDownloader(context: Context, mediaItem: MediaItem) {
        Logger.d(TAG, "startDownloader contentId : ${mediaItem.contentId}")
        var extension = MediaSourceUtil.getContentType(mediaItem.uri)

        Logger.d(TAG, "startDownloader Title : ${mediaItem.title}")
        Logger.d(TAG, "startDownloader Uri : ${mediaItem.uri}")
        Logger.d(TAG, "startDownloader extension : $extension")
        when (extension) {
            C.TYPE_DASH -> {
//                Logger.d(TAG, "startDownloader switch : TYPE_DASH")
//                val httpDataSourceFactory = getHttpDataSourceFactory(context, mediaItem)
//                val downloaderConstructorHelper = getDownoadConstructorHelper(context, httpDataSourceFactory)
//                mediaItem.httpDataSourceFactory = httpDataSourceFactory
//                currentMediaItem = mediaItem
//                currentDownloader = DashDownloader(
//                        mediaItem.uri,
//                        Collections.singletonList(StreamKey(0, 0, 0)),
//                        downloaderConstructorHelper)
//                try {
//                    Logger.d(TAG, "Download Start : ${mediaItem.contentId}")
//                    // Perform the download.
//                    currentDownloader?.download(DownloadProgressListener(mediaItem))
//                } catch (e: Exception) {
//                    Logger.d(TAG, "Download Exception : ${e.message}")
//                    Logger.caughtException(e)
//                    downloadVideoFromQueue()
//                }
            }
            C.TYPE_HLS -> {
                Logger.d(TAG, "startDownloader switch : TYPE_HLS")
                // Create a downloader for the first variant in a master playlist.
                val httpDataSourceFactory = getHttpDataSourceFactory(context, mediaItem)
                val downloaderConstructorHelper = getDownloadConstructorHelper(context, mediaItem,
                        httpDataSourceFactory)
                currentMediaItem = mediaItem
                currentDownloader?.cancel()
                currentDownloader = HlsDownloader(
                        mediaItem.uri,
                        Collections.singletonList(StreamKey(
                                HlsMasterPlaylist.GROUP_INDEX_VARIANT, mediaItem.variantIndex)),
                        downloaderConstructorHelper)
                // Perform the download.
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        Logger.d(TAG, "Download Start : ${mediaItem.contentId}")
                        Logger.d(TAG, "Download Start currentDownloader : $currentDownloader")
                        currentDownloader?.download(DownloadProgressListener(mediaItem))
                    } catch (e: IOException) {
                        Logger.d(TAG, "Download IOException : ${e.message}")
                        e.printStackTrace()
                        Logger.caughtException(e)
                        updateDownloadException(mediaItem, "IOException:" + e.message)
                    } catch (e: InterruptedException) {
                        Logger.d(TAG, "Download InterruptedException : ${e.message}")
                        e.printStackTrace()
                        Logger.caughtException(e)
                    } catch (e: DownloadException) {
                        Logger.d(TAG, "DownloadException Exception : ${e.message}")
                        e.printStackTrace()
                        Logger.caughtException(e)
                        updateDownloadException(mediaItem, "DownloadException:" + e.message)
                    } catch (e: Exception) {
                        Logger.d(TAG, "Download Exception : ${e.message}")
                        e.printStackTrace()
                        Logger.caughtException(e)
                        updateDownloadException(mediaItem, "Exception:" + e.message)
                    }
                }
            }
            C.TYPE_OTHER -> {
                Logger.d(TAG, "startDownloader switch : TYPE_OTHER")
//                // Create a downloader for the first variant in a master playlist.
//                val httpDataSourceFactory = getHttpDataSourceFactory(context, mediaItem)
//                val downloaderConstructorHelper = getDownoadConstructorHelper(context, httpDataSourceFactory)
//                mediaItem.httpDataSourceFactory = httpDataSourceFactory
//                currentMediaItem = mediaItem
//                currentDownloader = ProgressiveDownloader(
//                        mediaItem.uri,
//                        null,
//                        downloaderConstructorHelper)
//                try {
//                    Logger.d(TAG, "Download Start : ${mediaItem.contentId}")
//                    // Perform the download.
//                    currentDownloader?.download(DownloadProgressListener(mediaItem))
//                } catch (e: Exception) {
//                    Logger.d(TAG, "Download Exception : ${e.message}")
//                    Logger.caughtException(e)
//                }
            }
            else -> {
                Logger.e(TAG, "startDownloader type not supported")
            }
        }
    }

    private fun updateQueueWithDownloadPercentage(contentId: String, percentDownloaded: Float,
                                                  streamCacheDuration: Float) {
        if (downloadQueue.containsKey(contentId)) {
            downloadQueue[contentId]?.streamCachedPercentage = percentDownloaded
            downloadQueue[contentId]?.streamCacheDuration = streamCacheDuration
        }
    }

    private class ProgressListener(val mediaItem: MediaItem) : CacheUtil.ProgressListener {
        override fun onProgress(requestLength: Long, bytesCached: Long, newBytesCached: Long) {
            val downloadPercentage: Double = (bytesCached * 100.0 / requestLength)
            Logger.d(TAG, "onProgress requestLength : $requestLength bytesCached : $bytesCached " +
                    "newBytesCached $newBytesCached")
            Logger.d(TAG, "onProgress contentId : ${mediaItem.contentId} " +
                    "downloadPercentage :$downloadPercentage")
        }
    }

    internal fun getDownloadConstructorHelper(context: Context, mediaItem: MediaItem,
                                             httpDataSourceFactory: DataSource.Factory)
            : DownloaderConstructorHelper {
        return DownloaderConstructorHelper(ExoCacheHelper.getSimpleCache(context),
                httpDataSourceFactory)
    }

    internal fun getHttpDataSourceFactory(context: Context, mediaItem: MediaItem): DataSource.Factory {
//        return MediaSourceUtil.buildDataSourceFactory(context, mediaItem) //This is not working
//        return DefaultHttpDataSourceFactory(MediaSourceUtil.getUserAgent(context), ExoUtils.BANDWIDTH_METER)
        return MediaSourceUtil.buildForPrefetch(context)
    }

    private class DownloadProgressListener(val mediaItem: MediaItem) : Downloader.ProgressListener {
        var startedMarked = false
        var limitDownloadPercentage = -1.0f
        var downloadTimeOutForPrefetch: Long = 8000  // 8 seconds
        var downloadStarted = false
        var job: Job? = null

        init {
            job = GlobalScope.launch(Dispatchers.IO) {
                delay(downloadTimeOutForPrefetch)
                if (!downloadStarted) {
                    Logger.d(TAG, "TIMEOUT : Download not started for Item : ${
                        mediaItem.contentId
                    }" + " cacheType : ${mediaItem.cacheType}")
                    removeStreamFromDownload(mediaItem.contentId)
                    updateDownloadException(mediaItem, "TIMEOUT")
                    //Download Next Video to cache
                    downloadVideoFromQueue()
                } else {
                    Logger.d(TAG, "TIMEOUT Download Started  : ${mediaItem.contentId}" +
                            " cacheType : ${mediaItem.cacheType}" +
                            " percentage : ${mediaItem.streamCachedPercentage}")
                }
            }
        }

        override fun onProgress(contentLength: Long, bytesDownloaded: Long, percentDownloaded: Float) {
//            Logger.d(TAG, "onProgress contentId : ${mediaItem.contentId} percentDownloaded :$percentDownloaded")
            downloadStarted = true
            job?.cancel() //Cancel Timeout
            job = null

            if(mediaItem.contentDuration == 0) {
                mediaItem.contentDuration = 60
            }

            val videoSize: Float = (bytesDownloaded / percentDownloaded) * 100.0F
            val videoSizePerSec: Float = videoSize / mediaItem.contentDuration
            val downloadedVideoDuration: Float = (bytesDownloaded / videoSizePerSec) / 2
            var requireByteToDownload = videoSizePerSec * mediaItem.prefetchDuration


            if (Logger.loggerEnabled()) {
                Logger.d(TAG, "********* :: KB contentLength :: " + contentLength)
                Logger.d(TAG, "bytesDownloaded :: " + bytesDownloaded)
                Logger.d(TAG, "percentDownloaded :: " + percentDownloaded)

                Logger.d(TAG, "mediaItem.contentDuration : " + mediaItem.contentDuration)
                Logger.d(TAG, "mediaItem.prefetchDuration : " + mediaItem.prefetchDuration)
                Logger.d(TAG, "percentDownloaded : " + percentDownloaded)
                Logger.d(TAG, "videoSize : " + videoSize)
                Logger.d(TAG, "videoSizePerSec : " + videoSizePerSec)
                Logger.d(TAG, "downloadedVideoDuration : " + downloadedVideoDuration)
                Logger.d(TAG, "requireByteToDownload : " + requireByteToDownload)
            }

            if (!startedMarked) {
                startedMarked = true
                markedAsStreamDownload(mediaItem, CacheStatus.STARTED)
                Logger.d(TAG, "DownloadProgressListener DownloadStarted >> ${mediaItem.contentId}" +
                        " percentDownloaded : $percentDownloaded")
                Logger.d(TAG, "DownloadProgressListener limitDownloadPercentage : $limitDownloadPercentage")
            }

            if (!DISABLE_FILE_LOG && Logger.loggerEnabled()) {
                updatePercentageToFileLog(mediaItem, percentDownloaded, bytesDownloaded)
            }

            updateQueueWithDownloadPercentage(mediaItem.contentId, percentDownloaded, downloadedVideoDuration)
            updateDownloadPercentage(mediaItem, percentDownloaded, bytesDownloaded, downloadedVideoDuration)
            if (downloadedVideoDuration >= mediaItem.prefetchDuration) {
                Logger.d(TAG, ">>>>>>> Download completed partially :: stop " + downloadedVideoDuration)
                markedAsStreamDownload(mediaItem, CacheStatus.PARTIAL)
                downloadPartialList[mediaItem.contentId] = mediaItem
                removeStreamFromDownload(mediaItem.contentId)

                if (Logger.loggerEnabled()) {
                    Logger.d(TAG, "DownloadProgressListener  Stopping Download at downloadedVideoDuration : $downloadedVideoDuration"
                            + " percentage : " + percentDownloaded + " contentId : ${mediaItem.contentId}" + "\n Title :: ${mediaItem.title}")
                    Logger.d(TAG, "DownloadProgressListener  downloadQ : ${downloadQueue.size}")
                    Logger.d(TAG, "DownloadProgressListener  downloadPartialList : ${downloadPartialList.size}")
                    Logger.e(TAG, "DownloadProgressListener  downloadPartialList URI : " +
                            "${downloadPartialList.map { it.value.uri }}")
                    Logger.e(TAG, "DownloadProgressListener  downloadPartialList : " +
                            "${downloadPartialList.map { it.value.contentId }}")
                    //Write into Preference for cross check
                    logDownloadList()
                }
                //Download Next Video to cache
                downloadVideoFromQueue()
                return
            }

//            if (percentDownloaded >= 100.0f) {
//                markedAsStreamDownload(mediaItem, CacheStatus.COMPLETE)
//                downloadPartialList[mediaItem.contentId] = mediaItem
//                removeStreamFromDownload(mediaItem.contentId)
//
//                if (Logger.loggerEnabled()) {
//                    Logger.d(TAG, "DownloadProgressListener  download completed contentId : " +
//                            "${mediaItem.contentId}")
//                    Logger.d(TAG, "DownloadProgressListener  percentDownloaded : $percentDownloaded")
//                    Logger.d(TAG, "DownloadProgressListener  downloadQ : ${downloadQueue.size}")
//                    Logger.d(TAG, "DownloadProgressListener  downloadComplete : ${downloadPartialList.size}")
//                    Logger.e(TAG, "DownloadProgressListener  downloadPartialList URI : " +
//                            "${downloadPartialList.map { it.value.uri }}")
//                    Logger.e(TAG, "DownloadProgressListener  downloadPartialList : " +
//                            "${downloadPartialList.map { it.value.contentId }}")
//                    Logger.e(TAG, "DownloadProgressListener  downloadPartialList : " +
//                            "${downloadPartialList.map { it.value.cacheType }}")
////                    Logger.e(TAG, "DownloadProgressListener for ref downloadPartialList : " +
////                            "${downloadPartialList.map { it.value.contentId }}")
//                    //Write into Preference for cross check
//                    logDownloadList()
//                }
//                //Download Next Video to cache
//                downloadVideoFromQueue()
//            }
        }
    }

    private const val DISABLE_FILE_LOG = true
    private fun updatePercentageToFileLog(mediaItem: MediaItem, percentDownloaded: Float,
                                          bytesDownloaded: Long) {
        if (DISABLE_FILE_LOG) {
            return
        }
        if (mediaItem.streamCachedPercentage == -1f || (percentDownloaded - mediaItem.streamCachedPercentage) > 5 || percentDownloaded >= 100) {
            Logger.d("appendLog", "Going to log")

            var data = StringBuilder()
            data.append(mediaItem.contentId)
            data.append("  ")
            data.append(mediaItem.cacheType.name)
            data.append("  ")
            data.append(mediaItem.networkType)
            data.append("  ")
            data.append(mediaItem.selectedConnectionQuality)
            data.append("  ")
            data.append(mediaItem.uri.toString())
            data.append("  ")
            data.append(percentDownloaded)
            data.append("  ")
            data.append(bytesDownloaded)
            data.append("  ")
            data.append(System.currentTimeMillis())
            appendLog(data.toString())
        }
    }

    fun appendLog(text: String?) {
        if (DISABLE_FILE_LOG) {
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            Logger.d("appendLog", "appendLog text :: " + text)
            val log = File(Environment.getExternalStorageDirectory().absolutePath
                    + "/josh_video_logs")
            if (!log.exists()) {
                log.mkdir()
            }
            val logFile = File(Environment.getExternalStorageDirectory().absolutePath
                    + "/josh_video_logs/video_cache_logs.txt")
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                } catch (e: IOException) {
                    Logger.e("appendLog", "EXCEPTION" + e.message)
                    e.printStackTrace()
                }
            } else {
                Logger.d("appendLog", "File exist")
            }
            try {
                Logger.d("appendLog", text)
                //BufferedWriter for performance, true to set append to file flag
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(text)
                buf.newLine()
                buf.close()
            } catch (e: IOException) {
                Logger.e("appendLog", "EXCEPTION :: " + e.message)
                e.printStackTrace()
            }
        }
    }

    private val LOG_DOWNLOADLIST = "EXO_DOWNLOADED_LIST"
    private fun logDownloadList() {
        if (true) {
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            val jsonVideoList = PreferenceManager.getString(LOG_DOWNLOADLIST, "")
            val mediaListType = object : TypeToken<java.util.LinkedHashMap<String?, MediaItem?>?>() {}.type

            Logger.d(TAG, "mediaItemList json: $jsonVideoList")
            var mediaItemList: LinkedHashMap<String, MediaItem>? = null
            if (!CommonUtils.isEmpty(jsonVideoList)) {
                mediaItemList = JsonUtils.fromJson(jsonVideoList, mediaListType)
            }

            if (mediaItemList == null) {
                var mediaItemList2: LinkedHashMap<String, MediaItem> = LinkedHashMap()
                mediaItemList2.putAll(downloadPartialList)
                writeToPreference(mediaItemList2)
                Logger.d(TAG, "mediaItemList2 : ${mediaItemList2.size}")
            } else {
                if (mediaItemList.size > 50) {
                    mediaItemList.clear()
                }
                mediaItemList.putAll(downloadPartialList)
                writeToPreference(mediaItemList)
                Logger.d(TAG, "mediaItemList : ${mediaItemList.size}")
            }
        }
    }

    private fun writeToPreference(mediaItemList: LinkedHashMap<String, MediaItem>) {
        val jsonString = Gson().toJson(mediaItemList, java.util.LinkedHashMap::class.java)
        //    Logger.d(TAG, "jsonString: " + jsonString);
        PreferenceManager.saveString(LOG_DOWNLOADLIST, jsonString)
    }

    fun updateDownloadPercentage(mediaItem: MediaItem, percentage: Float, bytesDownloaded: Long, downloadedVideoDuration: Float) {
        Logger.d(TAG, "updateDownloadPercentage downloadedVideoDuration = $downloadedVideoDuration, percentage = $percentage" )
        mediaItem.streamCachedPercentage = percentage
        mediaItem.byteDownloaded = bytesDownloaded
        mediaItem.streamCacheDuration = downloadedVideoDuration
        for (listener in cacheListeners) {
            listener?.get()?.updateVideoCachedPercentage(mediaItem, percentage, downloadedVideoDuration)
        }
    }

    fun updateDownloadException(mediaItem: MediaItem?, errorMsg: String?) {
        if(!Logger.loggerEnabled()) {
            return
        }
        Logger.d(TAG, "updateDownloadException errorMsg =$errorMsg")
        if (mediaItem != null && !CommonUtils.isEmpty(mediaItem.contentId)) {
            for (listener in cacheListeners) {
                listener?.get()?.updateDownloadException(mediaItem, errorMsg)
            }
        }
    }

    fun markedAsStreamDownload(mediaItem: MediaItem?, cacheStatus: CacheStatus) {
        Logger.d(TAG, "markedAsStreamDownload cacheStatus = $cacheStatus" )
        if (mediaItem != null && !CommonUtils.isEmpty(mediaItem.contentId)) {
            for (listener in cacheListeners) {
                listener?.get()?.updateVideoUrlFromDownload(mediaItem, cacheStatus)
            }
            Logger.d(TAG, "DownloadStatus : $cacheStatus , streamCachedPercentage : ${mediaItem.streamCachedPercentage} + streamCacheDuration : ${mediaItem.streamCacheDuration}" )
        }
    }

    private val cacheListeners = CopyOnWriteArraySet<WeakReference<VideoCacheListener?>>()

    @Synchronized
    fun addListener(listener: VideoCacheListener) {
        removeListener(listener)
        cacheListeners.add(WeakReference(listener))
    }

    @Synchronized
    fun removeListener(listener: VideoCacheListener) {
        for(weakRef in cacheListeners) {
            if (weakRef?.get() == listener) {
                cacheListeners.remove(weakRef)
                break
            }
        }
    }

    fun clearCacheListeners() {
        cacheListeners?.clear()
    }

    interface VideoCacheListener {
        //Updating Stream cache Url from Downloaded Manager so that when user
        // re-visits same video and picks the cached played url (downloaded url)
        fun updateVideoUrlFromDownload(mediaItem: BaseMediaItem?, cacheStatus: CacheStatus)

        //Updating Stream cache Url from Exo Player so that when user re-visits
        // same video and picks the old played url (played previously)
        fun updateVideoUrlFromExo(mediaItem: BaseMediaItem?, cacheStatus: CacheStatus)
        fun updateVideoCachedPercentage(mediaItem: BaseMediaItem?, percentage: Float, cachedDuration: Float)

        fun updateDownloadException(mediaItem: BaseMediaItem?, errorMsg: String?)
    }

    enum class CacheStatus(val value: Int) {
        STARTED(1),
        PARTIAL(10),
        COMPLETE(100)
    }
}