/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.internal.service

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DummyDisposable
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEventsConfigResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.internal.rest.AppsFlyerEventConfigAPI
import com.newshunt.dhutil.model.service.AppsFlyerEventConfigService
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Versioned API implementation for fetching AppsFlyer Events config
 * <p>
 * Created by srikanth.ramaswamy on 09/17/2018.
 */
class AppsFlyerEventConfigServiceImpl(private val uniqueRequestId: Int) : AppsFlyerEventConfigService {

    val apiEntity = VersionedApiEntity(VersionEntity.APPSFLYER_EVENT_CONFIG)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<AppsFlyerEventsConfigResponse>>()

    /**
     * Fetch the AppsFlyer event config versioned API entity
     */
    override fun getEventConfig(): Observable<AppsFlyerEventsConfigResponse> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = apiEntity.entityType)
            if (!CommonUtils.isEmpty(version)) version else Constants.EMPTY_STRING
        }.flatMap {
            val interceptor = VersionedApiInterceptor(this::validate)
            val appsFlyerEventAPI: AppsFlyerEventConfigAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null, interceptor).create(AppsFlyerEventConfigAPI::class.java)
            appsFlyerEventAPI.getEventConfig()
        }.map {
            it?.data?:AppsFlyerEventsConfigResponse()
        }
    }

    /**
     * Fetch the AppsFlyer event config from local versioned DB
     */
    override fun getEventConfigLocal() {
        val type = object : TypeToken<ApiResponse<AppsFlyerEventsConfigResponse>>() {}.type
        versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                .map {
                    if (it.data != null) {
                        val appsFlyerEventsConfigResponse = it.data
                        appsFlyerEventsConfigResponse.uniqueRequestId = uniqueRequestId
                        AndroidUtils.getMainThreadHandler().post {
                            BusProvider.postOnUIBus(appsFlyerEventsConfigResponse)
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(DummyDisposable())
    }

    override fun resetVersion() {
        CommonUtils.runInBackground {VersionedApiHelper.resetVersion(entityType = apiEntity.entityType)}
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }
        try {
            val type = object : TypeToken<ApiResponse<AppsFlyerEventsConfigResponse>>() {}.type
            val response = CommonUtils.GSON.fromJson<ApiResponse<AppsFlyerEventsConfigResponse>>(json, type)
            if (response == null || response.data == null) {
                return Constants.EMPTY_STRING
            }
            val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = json.toByteArray(),
                    langCode = UserPreferenceUtil.getUserLanguages(), version = response.data.version)
            versionedApiHelper.insertVersionDbEntity(versionDbEntity)
            return response.data.version
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        return Constants.EMPTY_STRING
    }
}