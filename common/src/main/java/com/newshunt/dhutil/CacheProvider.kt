package com.newshunt.dhutil

import com.newshunt.common.helper.cachedapi.CachedApiCache
import com.newshunt.common.helper.common.Logger
import java.lang.ref.WeakReference

class CacheProvider {
    companion object {
        private var referenceMap: MutableMap<String, WeakReference<CachedApiCache>>? = null

        fun getCachedApiCache(dir: String): CachedApiCache {
            if (referenceMap == null) {
                referenceMap = mutableMapOf()
            }
            referenceMap!!.let {
                val current = it[dir]?.get()
                if (current != null) {
                    Logger.i(LOG_TAG, "Providing existing cache")
                    return current
                } else {
                    Logger.i(LOG_TAG, "Creating new instance of cache")
                    val newInstance = CachedApiCache(dir)
                    it[dir] = WeakReference(newInstance)
                    return newInstance
                }
            }
        }

        fun closeAllCachedApiCache() {
            referenceMap?.iterator()?.forEach { entry ->
                val key = entry.key
                val cacheRef = entry.value
                cacheRef.get()?.let {
                    Logger.i(LOG_TAG, "Closing cache $key")
                    it.close()
                }
            }
            referenceMap?.clear()
        }

        private const val LOG_TAG = "CacheProvider"
    }
}