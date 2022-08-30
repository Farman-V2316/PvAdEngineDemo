/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.apis

import com.newshunt.common.helper.cachedapi.CacheApiKeyBuilder
import com.newshunt.common.helper.cachedapi.CacheCompressUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.ApiCacheProvider
import com.newshunt.news.model.utils.SerializationUtils
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Named

/**
 * This class handles caching for [NewsDetailAPI]
 *
 * Only override methods that need caching, others will be delegated.
 *
 * Caching implementation taken from [CacheUsecase].
 * Not using it directly because it would be backward dependency.
 *
 * @author satosh.dhanyamarju
 */
class NewsDetailAPIProxy @Inject constructor(private val api: NewsDetailAPI,
                                             @Named("apiCacheProvider")
                                             private val apiCacheProvider: ApiCacheProvider)
    : NewsDetailAPI by api {

    /**
     * decide to cache or not, after looking at the response
     */
    val canCacheThisResponse = { response : ApiResponse<PostEntity>? ->
        when {
            response?.data == null -> false
            response.data?.i_format() == Format.POLL -> false /*don't cache polls*/
            else -> true
        }
    }

    override fun getFullPost(postId: String?, useWidgetPosition: Boolean?, sendBothChunk: Boolean?): Observable<Response<ApiResponse<PostEntity>>> {
        val key = with(CacheApiKeyBuilder()) {
            addParam("path", "api/v2/posts/article/content/$postId")
            addParam("useWidgetPosition", useWidgetPosition?.toString() ?: "")
            addParam("sendBothChunk", sendBothChunk?.toString() ?: "")
            addParam("appLanguage",AppUserPreferenceUtils.getUserNavigationLanguage())
            build()
        }
        Logger.d(LOG_TAG, "getFullPost: key[$postId]=$key")
        return useNetworkIfNoCache(key, api.getFullPost(postId, useWidgetPosition, sendBothChunk),
                canCacheThisResponse)
    }

    override fun getFullContent(postId: String?, referrerFlow: String?, referrerFlowId: String?, useWidgetPosition: Boolean?, sendBothChunk: Boolean?): Observable<Response<ApiResponse<PostEntity>>> {
        val key = with(CacheApiKeyBuilder()) {
            addParam("path", "api/v2/posts/article/content/$postId")
            addParam("useWidgetPosition", useWidgetPosition?.toString() ?: "")
            addParam("sendBothChunk", sendBothChunk?.toString() ?: "")
            addParam("appLanguage",AppUserPreferenceUtils.getUserNavigationLanguage())
            build()
        }
        Logger.d(LOG_TAG, "getFullContent: key[$postId]=$key")
        return useNetworkIfNoCache(key, api.getFullContent(postId, referrerFlow, referrerFlowId,
                useWidgetPosition ?: false, sendBothChunk ?: false),
                canCacheThisResponse)
    }

    override fun contentOfPost(url: String): Observable<Response<ApiResponse<PostEntity>>> {
        val key = with(CacheApiKeyBuilder()) {
            addParam("url", url)
            addParam("appLanguage",AppUserPreferenceUtils.getUserNavigationLanguage())
            build()
        }
        return useNetworkIfNoCache(key, api.contentOfPost(url), canCacheThisResponse)
    }

    private fun <T : Serializable> useNetworkIfNoCache(key: String, nw: Observable<Response<T>>, canCache: (T) -> Boolean): Observable<Response<T>> {
        fun writeCache(data: T): T {
            Logger.d(LOG_TAG, "Writing in cache $key")
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

        val cache = apiCacheProvider.getCache(NewsConstants.HTTP_FEED_CACHE_DIR).get(key).map { arr ->
            Logger.d(LOG_TAG, "useNetworkIfNoCache: Getting from cache $key")
            SerializationUtils.deserialize<T>(CacheCompressUtils.decompressToByteArray(arr))
        }
        val networkUpdateCache = nw.lift(ApiResponseOperator()).map {
            Logger.d(LOG_TAG, "useNetworkIfNoCache: Getting from nw $key")
            if(canCache(it)) writeCache(it)
            else {
                Logger.d(LOG_TAG, "not caching $key")
                it
            }
        }
        return cache.onErrorResumeNext(networkUpdateCache).map {
            Response.success(it)
        }
    }

    companion object {
        private val LOG_TAG = "NewsDetailAPIProxy"
        /**
         * If the card exists in DB, read its moreLoadContentUrl and use it as API url.
         * Else, use hardcoded urls in API methods
         */
        fun contentOfPost(newsDetailAPI: NewsDetailAPI,
                          postId: String,
                          entityId: String,
                          location: String,
                          section: String,
                          useWidgetPosition: Boolean?,
                          sendBothChunk: Boolean?,
                          referrerFlow: String? = null,
                          referrerFlowId: String? = null,
                          postDao: PostDao = SocialDB.instance().postDao()
        ): Observable<Response<ApiResponse<PostEntity>>> {
            Logger.d(LOG_TAG, "contentOfPost(newsDetailAPI = $newsDetailAPI, postId = $postId, entityId = $entityId, location = $location, section = $section, useWidgetPosition = $useWidgetPosition, sendBothChunk = $sendBothChunk, referrerFlow = $referrerFlow, referrerFlowId = $referrerFlowId, postDao = $postDao)")
            return Observable.fromCallable {
                val p = postDao.lookupCard(postId, entityId, location, section)
                p.firstOrNull()?.i_moreContentLoadUrl()?:""
            }.flatMap { url ->
                Logger.d(LOG_TAG, "contentOfPost: url=$url")
                when {
                url.isNotEmpty() -> {
                    val map = hashMapOf<String, String>()
                    useWidgetPosition?.let { map[NewsConstants.KEY_USE_WIDGET_POSITION] = it.toString() }
                    sendBothChunk?.let { map[NewsConstants.KEY_SEND_BOTH_CHUNK] = it.toString() }
                    referrerFlow?.let { map[NewsConstants.KEY_REFERRER_FLOW] = it }
                    referrerFlowId?.let { map[NewsConstants.KEY_REFERRER_FLOW_ID] = it }
                    val finalUrl = if(map.isNotEmpty())
                        UrlUtil.getUrlWithQueryParamns(url, map)
                    else url
                    newsDetailAPI.contentOfPost(finalUrl)
                }
                referrerFlow != null || referrerFlowId != null/*either one is present*/ -> {
                    newsDetailAPI.getFullContent(postId, referrerFlow, referrerFlowId, useWidgetPosition, sendBothChunk)
                }
                else -> {
                    newsDetailAPI.getFullPost(postId, useWidgetPosition, sendBothChunk)
                }
            }}
        }
    }
}
