/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.download.config

import android.text.TextUtils
import com.newshunt.common.helper.UserConnectionHolder
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NHJsonTypeAdapter
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.ConfigType
import com.newshunt.dataentity.common.asset.VideoAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Configuration for Cache Config
 *
 * @author umesh.isran
 */

object CacheConfigHelper {

    private const val TAG = "CacheConfigHelper"
    private const val DEFAULT_PREFETCH_COUNT = 2
    private const val DEFAULT_PREFETCH_DURATION = 12F

    var disableCache = false
    var cacheDirectoryMaxSizeInMB = 200 //Max video Download directory for prefetch
    var prefetchConfigBuzzList: Map<String, PrefetchDownloadCountConfig?>? = null
    var prefetchConfigNewsList: Map<String, PrefetchDownloadCountConfig?>? = null
    var prefetchConfigVideoDetailV: Map<String, PrefetchDownloadCountConfig?>? = null

    var downloadHighBitrateVariant = false //Ensure to pick highest bitrate from manifest

    fun initData() {
        Logger.d(TAG, "init >>")
        readCacheConfigValueFromCache()
    }

    //Function to read CacheConfig from preference
    private fun readCacheConfigValueFromCache() {
        var cacheStr = PreferenceManager.getString(GenericAppStatePreference.CACHE_CONFIG.name, "")
        Logger.d(TAG, "readCacheConfigValueFromCache CacheConfig from pref : $cacheStr")
        if (CommonUtils.isEmpty(cacheStr)) {
            cacheStr = CommonUtils.readFromAsset("cache_config.txt")
            Logger.d(TAG, "readCacheConfigValueFromCache CacheConfig from local assets : $cacheStr")
        }
        if (!CommonUtils.isEmpty(cacheStr)) {
            val cacheConfig = JsonUtils.fromJson(
                cacheStr, CacheConfig::class.java,
                NHJsonTypeAdapter<Any?>(CacheConfig::class.java)
            )
            Logger.d(TAG, "readValueFromCache and update cacheConfig : $cacheConfig")
            if (cacheConfig != null) {
                updateCacheConfig(cacheConfig)
            }
        }
    }

    fun updateCacheConfig(cacheConfig: CacheConfig?) {
        if (cacheConfig != null) {
            disableCache = cacheConfig.disableCache
            cacheDirectoryMaxSizeInMB = cacheConfig.cacheDirectoryMaxSizeInMb
            prefetchConfigBuzzList = cacheConfig.prefetchConfigBuzzList
            prefetchConfigNewsList = cacheConfig.prefetchConfigNewsList
            prefetchConfigVideoDetailV = cacheConfig.prefetchConfigVideoDetailV
            downloadHighBitrateVariant = cacheConfig.downloadHighBitrateVariant ?: false
        }
        if (Logger.loggerEnabled()) {
            Logger.d(TAG, "cachePrefetchDirectoryMaxSizeInMB : $cacheDirectoryMaxSizeInMB")
            Logger.d(TAG, "prefetchConfigBuzzList : $prefetchConfigBuzzList")
            Logger.d(TAG, "prefetchConfigNewsList : $prefetchConfigNewsList")
            Logger.d(TAG, "prefetchConfigVideoDetailV : $prefetchConfigVideoDetailV")
            Logger.d(TAG, "downloadHighBitrateVariant : $downloadHighBitrateVariant")
        }
    }


    private fun getPrefetchDuration(connectionSpeed: String, configType: ConfigType?): Float {
        if(configType != null && !TextUtils.isEmpty(connectionSpeed)) {
            val prefetchDownloadConfig = getPrefetchConfig(configType)
            prefetchDownloadConfig?.let {
                val prefetchDownloadCountConfig: PrefetchDownloadCountConfig? =
                    it[connectionSpeed]
                return prefetchDownloadCountConfig?.prefetchDurationInSec
                    ?: DEFAULT_PREFETCH_DURATION
            }
        }
        Logger.d(TAG, "getPrefetchDuration : $DEFAULT_PREFETCH_DURATION")
        return DEFAULT_PREFETCH_DURATION
    }

    fun getPrefetchDurationConfig(videoAsset: VideoAsset?): Float {
        var prefetchDurationInSec = videoAsset?.prefetchDurationInSec
        if(prefetchDurationInSec != null && prefetchDurationInSec > 0){
            return prefetchDurationInSec
        }
        return getPrefetchDuration(
            UserConnectionHolder.userConnectionQuality, videoAsset?.configType)
    }

    private fun getPrefetchConfig(configType: ConfigType): Map<String, PrefetchDownloadCountConfig?>? {
        return when (configType) {
            ConfigType.BUZZ_LIST -> {
                Logger.d(TAG,
                    "getPrefetchConfig prefetchConfigBuzzList : $prefetchConfigBuzzList"
                )
                prefetchConfigBuzzList
            }
            ConfigType.NEWS_LIST -> {
                Logger.d(TAG,
                    "getPrefetchConfig prefetchConfigNewsList : $prefetchConfigNewsList"
                )
                prefetchConfigNewsList
            }
            ConfigType.VIDEO_DETAIL_V -> {
                Logger.d(TAG,
                    "getPrefetchConfig prefetchConfigVideoDetailV : $prefetchConfigVideoDetailV"
                )
                prefetchConfigVideoDetailV
            }
            else -> {
                null
            }
        }
        Logger.d(TAG, "getPrefetchConfig : " + return null)
        return null
    }

    fun getNumberOfVideoToPrefetch(configType: ConfigType, connectionSpeed: String?): Int {
        if (connectionSpeed.isNullOrEmpty()) {
            Logger.d(TAG,
                "getNumberOfVideoToPrefetch connectionSpeed is EMPTY, Return Default")
            return DEFAULT_PREFETCH_COUNT
        }

        val tempPrefetchDownloadConfig = when (configType) {
            ConfigType.BUZZ_LIST -> {
                Logger.d(TAG,
                    "getNumberOfVideoToPrefetch prefetchConfigBuzzList : " + prefetchConfigBuzzList)
                prefetchConfigBuzzList
            }
            ConfigType.NEWS_LIST -> {
                Logger.d(TAG,
                    "getNumberOfVideoToPrefetch prefetchConfigNewsList : " + prefetchConfigNewsList)
                prefetchConfigNewsList
            }
            ConfigType.VIDEO_DETAIL_V -> {
                Logger.d(TAG,
                    "getNumberOfVideoToPrefetch prefetchConfigVideoDetailV : " + prefetchConfigVideoDetailV)
                prefetchConfigVideoDetailV
            }
            else -> {
                null
            }
        }
        tempPrefetchDownloadConfig?.let {
            if (tempPrefetchDownloadConfig.containsKey(connectionSpeed.lowercase())) {
                val prefetchConfig =
                    tempPrefetchDownloadConfig?.get(connectionSpeed.lowercase())
                Logger.d(TAG, "prefetchConfig.noOfVideos return : " + prefetchConfig?.noOfVideos)
                return prefetchConfig?.noOfVideos ?: 0
            }
        }
        Logger.d(TAG, "getNumberOfVideoToPrefetch : return DEFAULT_PREFETCH_COUNT")
        return DEFAULT_PREFETCH_COUNT
    }
}

