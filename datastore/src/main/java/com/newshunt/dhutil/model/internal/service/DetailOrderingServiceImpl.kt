package com.newshunt.dhutil.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.storeUserSegToPref
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.detailordering.DetailWidgetOrderingResponse
import com.newshunt.dataentity.dhutil.model.entity.detailordering.PostDetailActionbarVariation
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.internal.rest.DetailWidgetOrderingAPI
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * Created by karthik.r on 05/06/20.
 */
object DetailOrderingServiceImpl {

    private const val TAG = "DetailOrderServ"
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<DetailWidgetOrderingResponse>>()
    private var detailWidgetOrdering: Map<String, List<String>>? = null
    var actionbarVariation: PostDetailActionbarVariation? = null
        private set

    init {
        loadLastKnownValues()
    }

    fun refreshData(): Observable<Unit> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.DETAIL_WIDGET_ORDERING.name)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val detailWidgetOrderingAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(DetailWidgetOrderingAPI::class.java)
            detailWidgetOrderingAPI.getDetailWidgetOrdering(version = version)
                    .map { handleResponse(it) }
        }.doOnError {
            Logger.e(TAG, "Exception", it)
        }
    }

    private fun handleResponse(apiResponse: ApiResponse<DetailWidgetOrderingResponse>?) {
        if (apiResponse != null && apiResponse.data != null && !CommonUtils.isEmpty(apiResponse.data.version)) {
            // Search for suitable configuration
            if (apiResponse.data != null) {
                loadValues(apiResponse.data)
                val detailOrderingResponse = Gson().toJson(apiResponse.data)
                PreferenceManager.savePreference(GenericAppStatePreference
                        .DETAIL_WIDGET_ORDERING, detailOrderingResponse)
                storeUserSegToPref(apiResponse.data.userSeg)
            }
        }
    }

    private fun loadValues(config: DetailWidgetOrderingResponse?) {
        detailWidgetOrdering = config?.rules
        actionbarVariation = config?.actionBarVariation
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }
        try {
            val type = object : TypeToken<ApiResponse<DetailWidgetOrderingResponse>>() {}.type
            val response = CommonUtils.GSON.fromJson<ApiResponse<DetailWidgetOrderingResponse>>(json, type)
            if (response == null || response.data == null) {
                return Constants.EMPTY_STRING
            }
            val versionDbEntity = VersionDbEntity(entityType = VersionEntity.DETAIL_WIDGET_ORDERING.name,
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
                    PreferenceManager.getPreference(GenericAppStatePreference.DETAIL_WIDGET_ORDERING, Constants.EMPTY_STRING)
            val config = Gson().fromJson(detailWidgetOrdering, DetailWidgetOrderingResponse::class.java)
            loadValues(config)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    fun getDetailWidgetOrderingResponse() : Map<String, List<String>>? {
        return detailWidgetOrdering
    }
}