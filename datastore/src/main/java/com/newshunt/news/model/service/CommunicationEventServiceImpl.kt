/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.apis.CommunicationEventsAPI
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * @author shrikant.agrawal
 */
class CommunicationEventServiceImpl : CommunicationEventsService {

    private val versionApiEntity = VersionedApiEntity(VersionEntity.COMMUNICATION_EVENTS)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<CommunicationEventsResponse>>()
    private val nudgeDao = SocialDB.instance().nudgeDao()

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json))  return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<CommunicationEventsResponse>>() {}.type
            val response = Gson().fromJson<ApiResponse<CommunicationEventsResponse>>(json, type)
            return if (response == null || response.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = versionApiEntity.entityType, version = response.data.version,
                        langCode = UserPreferenceUtil.getUserLanguages(), data = json.toByteArray())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                // TODO(satosh.dhanyamraju): getting called 2times. Expected?
                response.data?.events?.let { nudgeDao.updateFrom(it) }
                response.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    override fun getCommunicationEvents(): Observable<CommunicationEventsResponse> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(versionApiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val eventsAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(CommunicationEventsAPI::class.java)
            eventsAPI.getEvents(version, UserPreferenceUtil.getUserNavigationLanguage(),
                    UserPreferenceUtil.getUserLanguages(), UserPreferenceUtil.getUserEdition())
                    .map{ if (it == null) throw Exception() else transform(it) }
        }
    }

    private fun transform(communicationEventsResponseApiResponse: ApiResponse<CommunicationEventsResponse>): CommunicationEventsResponse {
        return communicationEventsResponseApiResponse.data
    }

    override fun getStoredCommunicationResponse(): Observable<CommunicationEventsResponse> {
        val type = object : TypeToken<ApiResponse<CommunicationEventsResponse>>() {}.type
        return versionedApiHelper.fromCacheKt(entityType = versionApiEntity.entityType, classOfT = type)
                .map { transform(it) }
    }
}