/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationCTAResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.apis.NotificationAPI
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * @author shrikant.agrawal
 */
class NotificationCTAService() {

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }

        try {
            val type = object : TypeToken<ApiResponse<NotificationCTAResponse>>() {}.type
            val response = Gson().fromJson<ApiResponse<NotificationCTAResponse>>(json, type)
            return if (response == null || response.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = VersionEntity.NOTIFICATION_CTA_CONFIG.name, version = response.data.version,
                    langCode = UserPreferenceUtil.getUserLanguages(), data = json.toByteArray())
                val versionedApiHelper = VersionedApiHelper<NotificationCTAResponse>()
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                PreferenceManager.remove(AppStatePreference.NOTIFICATION_CTA)
                if (!CommonUtils.isEmpty(response.data.notificationCta)) {
                    PreferenceManager.savePreference(AppStatePreference.NOTIFICATION_CTA,
                        JsonUtils.toJson(response.data.notificationCta))
                }
                response.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    fun getNotificationCTAFromServer() : Observable<NotificationCTAResponse> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.NOTIFICATION_CTA_CONFIG.name)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val api =  RestAdapterProvider.getRestAdapter(
                Priority.PRIORITY_HIGH, null,
                VersionedApiInterceptor({ json: String -> this.validate(json) }))
                .create(NotificationAPI::class.java)
            api.getNotificationCTA(version).map { it.data}
        }
    }
}