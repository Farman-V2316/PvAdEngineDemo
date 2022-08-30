/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.internal.service

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DummyDisposable
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.actionablepayload.ActionablePayloadResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.model.entity.ActionableNotiPayload
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.news.model.helper.NotificationActionExecutionHelper
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.internal.rest.ActionablePayloadAPI
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Versioned API implementation to handle Actionable Payload data
 *
 * Created by karthik.r on 03/07/20.
 */
object ActionablePayloadServiceImpl {

    private val versionedApiHelper = VersionedApiHelper<ApiResponse<ActionablePayloadResponse>>()
    private var actionableNotiPayload: ActionableNotiPayload? = null

    init {
        loadLastKnownValues()
    }

    fun refreshData(): Observable<Unit> {
        if (CommonNavigator.isFirstLaunch()) {
            return Observable.empty()
        }

        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.ACTIONABLE_PAYLOAD.name)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val actionablePayloadAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH,
                    null, VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(ActionablePayloadAPI::class.java)
            actionablePayloadAPI.getActionablePayload(version = version)
                    .map { handleResponse(it) }
        }.doOnError {
            Logger.e("ActiPayldServ", "Exception", it)
        }
    }

    private fun handleResponse(apiResponse: ApiResponse<ActionablePayloadResponse>?) {
        if (apiResponse != null && apiResponse.data != null && !CommonUtils.isEmpty(apiResponse.data.version)) {
            // Search for suitable configuration
            if (apiResponse.data != null) {
                actionableNotiPayload = apiResponse.data.configuration
                NotificationActionExecutionHelper.handleActionableNotification(Bundle.EMPTY, actionableNotiPayload)
            }
        }
    }

    private fun loadValues(payload: ActionableNotiPayload) {
        actionableNotiPayload = payload
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }
        try {
            val type = object : TypeToken<ApiResponse<ActionablePayloadResponse>>() {}.type
            val response = CommonUtils.GSON.fromJson<ApiResponse<ActionablePayloadResponse>>(json, type)
            if (response == null || response.data == null) {
                return Constants.EMPTY_STRING
            }
            val versionDbEntity = VersionDbEntity(entityType = VersionEntity.ACTIONABLE_PAYLOAD.name,
                    data = json.toByteArray(),
                    langCode = UserPreferenceUtil.getUserLanguages(),
                    version = response.data.version)
            versionedApiHelper.insertVersionDbEntity(versionDbEntity)
            return response.data.version
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }

        return Constants.EMPTY_STRING
    }

    private fun loadLastKnownValues() {
        try {
            val detailWidgetOrdering =
                    PreferenceManager.getPreference(GenericAppStatePreference.ACTIONABLE_PAYLOAD, Constants.EMPTY_STRING)
            val config = Gson().fromJson(detailWidgetOrdering, ActionablePayloadResponse::class.java)
            loadValues(config.configuration)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }
}