/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */


package com.newshunt.news.model.helper

import androidx.annotation.IntRange
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.VideoItem
import com.newshunt.dhutil.helper.preference.AppStatePreference
import java.util.*

/**
 * @author shrikant.agrawal
 * Singleton class to store the recently played video list
 */
object VideoPlayedCache {

    const val MAX_SIZE = 10
    const val DEFAULT_THRESHOLD = 900  //30 minutes

    private var cache: InternalCache

    init {
        cache = createFromPreferences(MAX_SIZE, AppStatePreference.VIDEO_ITEMS_RECENTLY_VIEWED)
    }


    fun createFromPreferences(maxSize : Int, appStatePreference: AppStatePreference) : InternalCache {
        val newCache = InternalCache(maxSize)
        val recentlyViewedString = PreferenceManager.getPreference(appStatePreference, Constants.EMPTY_STRING)
        val type = object : TypeToken<LinkedHashMap<String, VideoItem>>() {

        }.type
        val map = JsonUtils.fromJson<Map<String, VideoItem>>(recentlyViewedString, type)
        if (CommonUtils.isEmpty(map)) {
            return newCache
        }

        for ((key, value) in map!!) {
            newCache.put(key, value)
        }
        return newCache
    }

    @Synchronized
    fun put(videoItem: VideoItem, lastAccessTime: Long) {
        if (!isAccessTimeWithinThreshold(lastAccessTime)) return
        cache.put(videoItem.itemId + videoItem.ts , videoItem) // adding the timestamp so that we will not dedup the entries
        writeToPreferences(cache, AppStatePreference.VIDEO_ITEMS_RECENTLY_VIEWED)
    }

    private fun isAccessTimeWithinThreshold(accessTime: Long): Boolean {
        val thresholdTime = PreferenceManager.getPreference<Int>(AppStatePreference.LAST_ACCESS_VIDEO_THRESHOLD,
                DEFAULT_THRESHOLD)
        return (System.currentTimeMillis() - accessTime) / 1000 <= thresholdTime
    }

    private fun writeToPreferences(cache: InternalCache, appStatePreference: AppStatePreference) {
        val jsonString = JsonUtils.toJson<Map<String, VideoItem>>(cache.snapshot())
        PreferenceManager.savePreference(appStatePreference, jsonString)
    }

    @Synchronized
    fun entries(): List<VideoItem> {
        val collection = cache.snapshot().values
        val recentVidoes = ArrayList<VideoItem>()
        for (value in collection) {
            if (isAccessTimeWithinThreshold(value.ts)) {
                recentVidoes.add(value)
            }
        }

        cache.evictAll()
        for (value in recentVidoes) {
            cache.put(value.itemId + value.ts, value)
        }
        return recentVidoes
    }

    fun resize(@IntRange(from = 1) maxsize: Int) {
        cache.resize(maxsize)
    }


    class InternalCache(val maxSize: Int) : androidx.collection.LruCache<String, VideoItem>(maxSize) {

        override fun sizeOf(key: String, value: VideoItem): Int {
            return 1
        }
    }
}
