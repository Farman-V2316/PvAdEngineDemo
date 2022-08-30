/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.ApprovalTabsAPI
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.model.entity.ApprovalTabsInfo
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Versioned Api for Groups Approval Tabs Info
 *
 * @author raunak.yadav
 */
interface ApprovalTabsInfoService {

    fun getTabsConfig(): Observable<ApprovalTabsInfo?>
    fun getLocalData(): Observable<ApprovalTabsInfo>
    fun getTabsConfigFromNetwork(): Observable<ApprovalTabsInfo>
    fun resetVersion()
}

private const val TAG = "ApprovalTabsInfoServiceImpl"

class ApprovalTabsInfoServiceImpl @Inject constructor() : ApprovalTabsInfoService {

    private val versionedApiHelper = VersionedApiHelper<ApiResponse<ApprovalTabsInfo>>()
    private val apiEntity = VersionedApiEntity(VersionEntity.GROUP_APPROVAL_CONFIG)

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<ApprovalTabsInfo>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<ApprovalTabsInfo>>(json, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType,
                        version = apiResponse.data.version,
                        langCode = UserPreferenceUtil.getUserLanguages(),
                        data = json.toByteArray())

                VersionedApiHelper<ApiResponse<ApprovalTabsInfo>>().insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }

        } catch (e: Exception) {
            Logger.caughtException(e)
        }

        return Constants.EMPTY_STRING
    }

    override fun getTabsConfig(): Observable<ApprovalTabsInfo?> {
        return getLocalData().onErrorResumeNext(getTabsConfigFromNetwork())
    }

    override fun getLocalData(): Observable<ApprovalTabsInfo> {
        val type = object : TypeToken<ApiResponse<ApprovalTabsInfo>?>() {}.type
        return Observable.fromCallable {
            versionedApiHelper.getLocalEntity(entityType = apiEntity.entityType,
                    classOfT = type)
        }.map {
            Logger.d(TAG, "Local config : ${it.data}")
            it.data
        }
    }

    override fun getTabsConfigFromNetwork(): Observable<ApprovalTabsInfo> {
        Logger.d(TAG, "Fetch config from network")
        return RestAdapterProvider.getRestAdapter(Priority.PRIORITY_NORMAL, this,
                VersionedApiInterceptor({ json: String -> this.validate(json) }))
                .create(ApprovalTabsAPI::class.java)
                .getApprovalTabs()
                .map { response ->
                    Logger.d(TAG, "server response map ${response.data}")
                    response.data
                }
    }

    override fun resetVersion() {
        CommonUtils.runInBackground {
            VersionedApiHelper.resetVersion(entityType = apiEntity.entityType)
        }
    }
}