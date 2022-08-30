/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.cachedapi.CacheCompressUtils
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.cachedapi.CacheType
import com.newshunt.news.model.utils.SerializationUtils
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit

abstract class CacheUsecase<U : Serializable>(private val usecase: BundleUsecase<U>,
                                              private val apiCacheProvider: ApiCacheProvider) :
        ((String, Bundle, CacheType) -> Observable<U>) {
    private val LOG_TAG: String = "CacheUsecase"
    override fun invoke(key: String, args: Bundle, cacheType: CacheType):
            Observable<U> {
        val network = usecase.invoke(args)
        if (cacheType == CacheType.NO_CACHE) {
            return network
        } else {
            val networkUpdateCache = network.map {
                writeCache(key, it)
            }
            val cache = apiCacheProvider.getCache(NewsConstants.HTTP_FEED_CACHE_DIR).get(key).map { arr ->
                Logger.d(LOG_TAG, "Getting from cache")
                afterDecompress(
                        SerializationUtils.deserialize(
                                CacheCompressUtils.decompressToByteArray(arr)))
            }

            if (cacheType == CacheType.IGNORE_CACHE_AND_UPDATE) {
                return networkUpdateCache
            } else if (cacheType == CacheType.USE_CACHE_AND_UPDATE) {
                return Observable.mergeDelayError(cache, networkUpdateCache)
            } else if (cacheType == CacheType.NO_NETWORK) {
                return cache
            } else if (cacheType == CacheType.DELAYED_CACHE_AND_NETWORK) {
                val delay = args.getLong(NewsConstants.FEED_CACHE_DELAY, -1L) ?: -1
                return if (delay > 0) {
                    /*We use a Pair.second to distinguish where event is coming from - we can stop
                    * listening after getting an event from networkWithCacheFallback - this prevents
                    * cache event coming from multiple paths */
                    val delayedCache = cache.delay(delay, TimeUnit.MILLISECONDS).map { it to false }
                    val networkWithCacheFallback = networkUpdateCache
                            .onErrorResumeNext(cache).map {
                                it to true
                            }
                    Observable.mergeDelayError(delayedCache, networkWithCacheFallback)
                            .takeUntil { it.second }
                            .map {
                                Logger.d(LOG_TAG, "invoke: combined; ${it.second}")
                                it.first
                            }
                } else {
                    /*like use cache and update */
                    Observable.mergeDelayError(cache, networkUpdateCache)
                }
            } else {
                return cache.onErrorResumeNext(networkUpdateCache)
            }
        }
    }

    open fun afterDecompress(data: U): U {
        return data
    }

    private fun writeCache(key: String, data: U): U {
        Logger.d(LOG_TAG, "Writing in cache")
        val cache = apiCacheProvider.getCache(NewsConstants.HTTP_FEED_CACHE_DIR)
        val disposable = Observable.fromCallable {
            CacheCompressUtils.compress(SerializationUtils.serialize(data))
        }.flatMap { zippedData ->
            cache.addOrUpdate(key, zippedData)
        }.observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Logger.d(LOG_TAG, "write cache finished = $it")
                }, { error ->
                    Logger.d(LOG_TAG, "write error = $error")
                })
        return data
    }
}


private const val LOG_TAG = "CacheUsecase"

interface ApiCacheProvider {
    fun getCache(directory: String): CachedApiCacheRx
}