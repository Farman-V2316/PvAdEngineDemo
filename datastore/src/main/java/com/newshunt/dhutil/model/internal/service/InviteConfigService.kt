/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.service

import com.google.gson.reflect.TypeToken
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.model.entity.GroupInviteConfig
import com.newshunt.dhutil.model.internal.rest.InviteConfigAPI
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Versioned API implementation for fetching the invitation config
 * <p>
 * Created by srikanth.ramaswamy on 09/27/2019.
 */

interface InviteConfigService {
    fun getConfig(): Observable<GroupInviteConfig>
    fun getConfigLocal(): Observable<GroupInviteConfig>
    fun getConfigFromNetworkIfNoCache(): Observable<GroupInviteConfig>
    fun resetVersion()
}

private const val LOG_TAG = "InviteConfigService"
class InviteConfigServiceImpl @Inject constructor(): InviteConfigService {
    private val apiEntity = VersionedApiEntity(VersionEntity.INVITE_CONFIG)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<GroupInviteConfig>>()

    override fun getConfig(): Observable<GroupInviteConfig> {
        val interceptor = VersionedApiInterceptor(this::validate)
        val inviteConfigAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_NORMAL,
                null, interceptor).create(InviteConfigAPI::class.java)
        return inviteConfigAPI.getEventConfig().map {
            Logger.d(LOG_TAG, "Returning GroupInviteConfig from N/W")
            it.data
        }
    }

    override fun getConfigLocal(): Observable<GroupInviteConfig> {
        val type = object : TypeToken<ApiResponse<GroupInviteConfig>?>() {}.type
        return Observable.fromCallable {
            versionedApiHelper.getLocalEntity(entityType = apiEntity.entityType, classOfT = type)
        }.map {
            Logger.d(LOG_TAG, "Returning GroupInviteConfig from local DB")
            it.data
        }
    }

    override fun resetVersion() {
        CommonUtils.runInBackground {
            VersionedApiHelper.resetVersion(entityType = apiEntity.entityType)
        }
    }

    override fun getConfigFromNetworkIfNoCache(): Observable<GroupInviteConfig> {
        //First try fetching from cache. If it fails, hit API
        return getConfigLocal()
                .onErrorResumeNext(getConfig())
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }
        try {
            val type = object : TypeToken<ApiResponse<GroupInviteConfig>>() {}.type
            val response = CommonUtils.GSON.fromJson<ApiResponse<GroupInviteConfig>>(json, type)
            if (response == null || response.data == null) {
                return Constants.EMPTY_STRING
            }
            val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType,
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
}
