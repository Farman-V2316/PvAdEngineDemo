/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.notification.model.entity.NotificationChannelGroupInfo
import com.newshunt.notification.model.entity.NotificationChannelResponse
import com.newshunt.notification.model.internal.rest.NotificationChannelServiceAPI
import com.newshunt.notification.model.internal.rest.server.ConfigUpdatePayload
import com.newshunt.notification.model.internal.rest.server.NotificationChannelPriorityDelta
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * @author Amitkumar
 */
class NotificationChannelServiceImpl : NotificationChannelService {
    private val versionApiEntity = VersionedApiEntity(VersionEntity.NOTIFICATION_CHANNEL)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<NotificationChannelGroupInfo>>()

    override fun requestChannelInfo(baseUrl: String):
            Observable<NotificationChannelResponse?> {
        val serverApi = RestAdapterContainer.getInstance().getRestAdapter(baseUrl, Priority.PRIORITY_HIGH, null,
                VersionedApiInterceptor({ json: String ->
                    this.validate(json)
                }))
                .create(NotificationChannelServiceAPI::class.java)
        Logger.d(LOG_TAG, "Requesting Channel config")
        return serverApi.requestNotificationsChannels().map(ApiResponse<NotificationChannelResponse>::getData)
    }

    override fun updateChannelPriorityConfig(priorityConfig: NotificationChannelPriorityDelta,
                                             appNotificationEnabled: Boolean,
                                             systemNotificationEnabled: Boolean): Observable<NotificationChannelPriorityDelta> {
        Logger.d(LOG_TAG, "Updating Channel priority $priorityConfig")
        val serverApi = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null).create(NotificationChannelServiceAPI::class.java)

        return serverApi.updateNotificationsChannelsPriority(
                ConfigUpdatePayload(priorityConfig,
                        systemNotificationEnabled,
                        appNotificationEnabled)).flatMap {
            Observable.just(priorityConfig)
        }
    }

    fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<NotificationChannelResponse>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<NotificationChannelResponse>>(json, type)
            return if (apiResponse?.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = versionApiEntity.entityType, langCode =
                UserPreferenceUtil.getUserLanguages(),
                        version = apiResponse.data.version, data = json.toByteArray())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    companion object {
        private const val LOG_TAG = "NChannelService"
    }

}