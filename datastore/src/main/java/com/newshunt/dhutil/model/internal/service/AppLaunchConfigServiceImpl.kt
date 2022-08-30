package com.newshunt.dhutil.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse
import com.newshunt.dataentity.dhutil.model.entity.upgrade.AcquisitionType
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.internal.rest.AppLaunchRulesAPI
import com.newshunt.dhutil.model.service.AppLaunchConfigService
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * @author santhosh.kc
 */
class AppLaunchConfigServiceImpl : AppLaunchConfigService {
    private val apiEntity: VersionedApiEntity = VersionedApiEntity(VersionEntity.APP_LAUNCH_CONFIG)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<AppLaunchConfigResponse>>()

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<AppLaunchConfigResponse>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<AppLaunchConfigResponse>>(json, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = json.toByteArray(),
                        langCode = UserPreferenceUtil.getUserLanguages(), version = apiResponse.data.version)
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    override fun getAppLaunchConfig() : Observable<AppLaunchConfigResponse> {
        val type = object : TypeToken<ApiResponse<AppLaunchConfigResponse>>() {}.type
        return versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                .map { transform(it) }
    }

    private fun transform(response : ApiResponse<AppLaunchConfigResponse>) : AppLaunchConfigResponse {
        return response.data
    }

    override fun updateDBFromServer() : Observable<AppLaunchConfigResponse> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = apiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val appLaunchRulesAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(AppLaunchRulesAPI::class.java)
            appLaunchRulesAPI.getAppLaunchRules(apiEntity.version,
                    UserPreferenceUtil.getUserLanguages(),
                    PreferenceManager.getPreference(AppStatePreference.ACQUISITION_TYPE, AcquisitionType.ACQ_TYPE_DH))
                    .map { it?.data?:AppLaunchConfigResponse() }
        }

    }

    override fun resetApiVersion() {
        VersionedApiHelper.resetVersion(entityType = apiEntity.entityType)
    }
}