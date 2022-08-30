/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.download

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheEvictor
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import com.newshunt.common.helper.common.Logger
import java.util.*

/**
 * @author vinod.bc
 */
class ExoLeastRecentCacheEvictor(private val maxBytes: Long,
                                 private val name: String) : CacheEvictor {

    private var leastRecentlyUsed: TreeSet<CacheSpan> = TreeSet { lhs: CacheSpan, rhs: CacheSpan -> compare(lhs, rhs) }

    private var leastRecentlyUsedKeyList: LinkedHashMap<String, Boolean> = LinkedHashMap<String, Boolean>(0)

    private var currentSize: Long = 0

    fun isMediaPresent(key: String): Boolean {
        return leastRecentlyUsedKeyList.containsKey(key)
    }

    override fun requiresCacheSpanTouches(): Boolean {
        return true
    }

    override fun onCacheInitialized() {
        // Do nothing.
    }

    override fun onStartFile(cache: Cache, key: String?, position: Long, length: Long) {
        Logger.d(TAG, "$name onStartFile cache: $cache, key: $key, " +
                "position: $position, length:$length")
        Logger.d(TAG, "$name  onStartFile Tree size: ${leastRecentlyUsed.size}")
        if (length != C.LENGTH_UNSET.toLong()) {
            evictCache(cache, length)
        }
    }

    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
        Logger.d(TAG, "$name onSpanAdded cache: $cache, span: $span")
        Logger.d(TAG, "$name onSpanAdded Tree size: ${leastRecentlyUsed.size}")
        leastRecentlyUsed.add(span)
        leastRecentlyUsedKeyList[span.key] = true
        currentSize += span.length
        evictCache(cache, 0)
    }

    override fun onSpanRemoved(cache: Cache?, span: CacheSpan) {
        Logger.d(TAG, "$name onSpanRemoved cache: $cache, span: $span")
        Logger.d(TAG, "$name onSpanRemoved Tree size before : ${leastRecentlyUsed.size}")
        Logger.d(TAG, "$name onSpanRemoved spanKey: ${span.key}")
        leastRecentlyUsed.remove(span)
        leastRecentlyUsedKeyList.remove(span.key)
        currentSize -= span.length
        Logger.d(TAG, "$name onSpanRemoved Tree size after : ${leastRecentlyUsed.size}")
    }

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        Logger.d(TAG, "$name onSpanTouched cache: $cache, oldSpan: $oldSpan, newSpan: $newSpan")
        Logger.d(TAG, "$name onSpanTouched Tree size: ${leastRecentlyUsed.size}")
        onSpanRemoved(cache, oldSpan)
        onSpanAdded(cache, newSpan)
    }

    private fun evictCache(cache: Cache, requiredSpace: Long) {
        Logger.d(TAG, "$name evictCache cache: $cache, requiredSpace: $requiredSpace")
        Logger.d(TAG, "$name evictCache Tree size before: ${leastRecentlyUsed.size}")
        while (currentSize + requiredSpace > maxBytes && !leastRecentlyUsed.isEmpty()) {
            try {
                Logger.d(TAG, "$name evictCache removeSpan leastRecentlyUsed $leastRecentlyUsed")
                cache.removeSpan(leastRecentlyUsed.first())
                Logger.d(TAG, "$name onStartFile Tree size after: ${leastRecentlyUsed.size}")
            } catch (e: Cache.CacheException) {
                // do nothing.
            }
        }
    }

    companion object {

        private const val TAG = "ExoLeastRecentCacheEvictor"

        private fun compare(lhs: CacheSpan, rhs: CacheSpan): Int {
            val lastTouchTimestampDelta = lhs.lastTouchTimestamp - rhs.lastTouchTimestamp
            if (lastTouchTimestampDelta == 0L) {
                // Use the standard compareTo method as a tie-break.
                return lhs.compareTo(rhs)
            }
            return if (lhs.lastTouchTimestamp < rhs.lastTouchTimestamp) -1 else 1
        }
    }

}