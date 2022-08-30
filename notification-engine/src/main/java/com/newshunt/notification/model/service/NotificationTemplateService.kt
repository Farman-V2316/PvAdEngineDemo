/*
 *  * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.notification.InAppTemplateResponse
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.notification.model.internal.rest.InAppTemplateApi
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by kajal.kumari on 21/04/22.
 */
interface NotificationTemplateService {
    fun getTemplatesFromServer(): Observable<InAppTemplateResponse>
    fun getStoredTemplates(): Observable<InAppTemplateResponse>
}

class NotificationTemplateServiceImpl @Inject constructor() : NotificationTemplateService {
    private val apiEntity: VersionedApiEntity = VersionedApiEntity(VersionEntity.NOTIFICATION_TEMPLATE)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<InAppTemplateResponse>>()

    override fun getTemplatesFromServer(): Observable<InAppTemplateResponse> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getNullableLocalVersion(apiEntity.entityType) ?: Constants.EMPTY_STRING
            version
        }.flatMap { version ->
            val api = RestAdapterProvider.getRestAdapter(
                Priority.PRIORITY_NORMAL, null,
                VersionedApiInterceptor({ json: String -> this.validate(json) })
            )
                .create(InAppTemplateApi::class.java)
            api.getTemplates(version)
        }
            .map { transform(it) }
    }

    override fun getStoredTemplates(): Observable<InAppTemplateResponse> {
        val type = object : TypeToken<ApiResponse<InAppTemplateResponse>>() {}.type
        return versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
            .map { transform(it) }.onErrorResumeNext { t: Throwable -> getTemplatesFromServer() }
    }

    private fun transform(inAppTemplateResponse: ApiResponse<InAppTemplateResponse>): InAppTemplateResponse {
        return inAppTemplateResponse.data
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<InAppTemplateResponse>>() {}.type
            val response = Gson().fromJson<ApiResponse<InAppTemplateResponse>>(json, type)
            return if (response == null || response.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, version = response.data.version,
                    langCode = UserPreferenceUtil.getUserLanguages(), data = json.toByteArray())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                response.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

}