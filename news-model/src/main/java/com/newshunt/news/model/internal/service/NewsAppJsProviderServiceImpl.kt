/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.NewsAppJSResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.internal.rest.NewsAppJSApi
import com.newshunt.news.model.service.NewsAppJSProviderService
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * @author santhosh.kc
 */

class NewsAppJsProviderServiceImpl : NewsAppJSProviderService {

    private val apiEntity = VersionedApiEntity(VersionEntity.APP_JS)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<NewsAppJSResponse>>()

    override fun updateDBFromServer() : Observable<ApiResponse<NewsAppJSResponse>> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = apiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap {
            val newsAppJSApi = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }
                            )).create(NewsAppJSApi::class.java)
            newsAppJSApi.getJS(it, UserPreferenceUtil.getUserLanguages())
        }
    }

    override fun getAppJSScripts(): Observable<ApiResponse<NewsAppJSResponse>> {
        val type = object : TypeToken<ApiResponse<NewsAppJSResponse>>() {}.type
        return versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                .subscribeOn(Schedulers.io())
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<NewsAppJSResponse>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<NewsAppJSResponse>>(json, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = json.toByteArray(),
                        version = apiResponse.data.version, langCode = UserPreferenceUtil.getUserLanguages())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }
}