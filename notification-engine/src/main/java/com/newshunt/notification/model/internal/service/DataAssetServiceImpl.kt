/*
 *
 *  * Copyright (c) 2019 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.model.internal.service

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.EmptyCookieJar
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.common.helper.preference.SavedPreference
import com.newshunt.common.model.retrofit.RestAdapters
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.notification.StickyNavModelType
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset
import com.newshunt.dataentity.notification.asset.GenericNotificationAsset
import com.newshunt.dataentity.notification.asset.NewsStickyNotificationAsset
import com.newshunt.dataentity.notification.asset.STICKY_NAV_MODEL_TYPE_FIELD
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.notification.model.internal.rest.StreamAPI
import com.newshunt.notification.model.service.DataAssetService
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import okhttp3.CookieJar


class DataAssetServiceImpl : DataAssetService {

    private val emptyCookieJar: CookieJar = EmptyCookieJar()

    companion object {
        const val INVALID_DATA_MESSAGE = "Meta Data is incomplete or not valid"
    }

    override fun getMetaData(streamUrl: String, stickyType: String?, prefPath: SavedPreference?): Observable<BaseNotificationAsset> {

        val clientBuilder = RestAdapters.getOkHttpClientBuilder(
                true, 30000, 30000, Priority.PRIORITY_HIGHEST, streamUrl).addInterceptor(NewsListErrorResponseInterceptor())
        clientBuilder.cookieJar(emptyCookieJar)
        var completeStreamUrl = streamUrl
        if(!stickyType.isNullOrEmpty() && stickyType.equals(NotificationConstants.STICKY_NEWS_TYPE)){
            completeStreamUrl = RestAdapters.getCompleteUrlFrom(streamUrl, null, prefPath)
        }

        val dataStreamAPI = RestAdapters.getBuilder(UrlUtil.getBaseUrl(streamUrl),
                clientBuilder.build()).build().create(StreamAPI::class.java)
        return dataStreamAPI.getMetaData(completeStreamUrl)
                .lift(ApiResponseOperator()).map {it: Any? ->
                    (it as? ApiResponse<Any>)?.let { transform((it)) } ?: throw BaseError(java.lang.Exception("Unkown Error"), "Api Error")
        }
    }

    private fun transform(apiResponse: ApiResponse<Any>?): BaseNotificationAsset {
        val childMap = apiResponse?.data as? Map<*, *> ?: throw java.lang.Exception()
        val type: String = childMap[STICKY_NAV_MODEL_TYPE_FIELD] as? String ?: throw java.lang.Exception(INVALID_DATA_MESSAGE)

        val stickyModelType = StickyNavModelType.from(type)
        val json = JsonUtils.toJson(childMap)

        return when (stickyModelType) {
            StickyNavModelType.CRICKET -> JsonUtils.fromJson(json, object :
                    TypeToken<CricketNotificationAsset>() {}.type) ?: throw java.lang.Exception(INVALID_DATA_MESSAGE)
            StickyNavModelType.GENERIC -> JsonUtils.fromJson(json, object :
                    TypeToken<GenericNotificationAsset>() {}.type) ?: throw java.lang.Exception(INVALID_DATA_MESSAGE)
            StickyNavModelType.NEWS -> transformNews(json)
            else -> throw java.lang.Exception(INVALID_DATA_MESSAGE)
        }
    }

    private fun transformNews(jsonStr: String): NewsStickyNotificationAsset{
        try{
            val asset  = JsonUtils.fromJson(jsonStr, NewsStickyNotificationAsset::class.java)
            asset.autoRefreshInterval = asset.refreshInterval.toInt() /1000
            asset.streamUrl = asset.url
            asset.id = NotificationConstants.NEWS_STICKY_OPTIN_ID
            return asset
        }catch (ex: Exception){
            Logger.caughtException(ex)
            throw java.lang.Exception(INVALID_DATA_MESSAGE)
        }
    }
}
