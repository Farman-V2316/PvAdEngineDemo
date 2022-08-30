package com.newshunt.dhutil.model.internal.service

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.ShareTextMappingResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.internal.rest.ShareTextMappingsAPI
import com.newshunt.dhutil.model.service.ShareTextMappingService
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * An implementation of [ShareTextMappingService] to get Server configured share text
 * mapping
 *
 * @author shashikiran.nr on 9/28/2017.
 */

class ShareTextMappingServiceImpl : ShareTextMappingService {

    private val apiEntity = VersionedApiEntity(VersionEntity.SHARE_TEXT_MAPPING_INFO)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<ShareTextMappingResponse>>()

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<ShareTextMappingResponse>>() {}.type
            val response = JsonUtils.fromJson<ApiResponse<ShareTextMappingResponse>>(json, type)
            return if (response == null || response.data == null) {
                 Constants.EMPTY_STRING
            } else {
                PreferenceManager.savePreference(GenericAppStatePreference.SHARE_TEXT_MAPPING,
                        JsonUtils.toJson(response.data))
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = json.toByteArray(),
                        version = response.data.version, langCode = UserPreferenceUtil.getUserLanguages())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                response.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    override fun getShareTextMapping(versionMode: VersionMode) : Observable<ShareTextMappingResponse> {
        return if (versionMode == VersionMode.CACHE) {
            val type = object : TypeToken<ApiResponse<ShareTextMappingResponse>>() {}.type
            versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                    .map { it.data }.onErrorResumeNext { t: Throwable -> Observable.empty() }
        } else {
            val storyShareFooterTextAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(ShareTextMappingsAPI::class.java)
            storyShareFooterTextAPI.shareTextMappingInfo.map { it?.data?:ShareTextMappingResponse() }
        }
    }
}