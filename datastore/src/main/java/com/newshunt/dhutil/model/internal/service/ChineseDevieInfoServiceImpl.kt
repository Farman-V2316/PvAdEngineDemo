/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.notifications.ChineseDeviceInfoResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.internal.rest.ChineseDeviceInfoAPI
import com.newshunt.dhutil.model.service.ChineseDeviceInfoService
import com.newshunt.dhutil.model.sqlite.versionDbInstance
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

/**
 * @author shashikiran.nr on 3/7/2017.
 */

class ChineseDeviceInfoServiceImpl : ChineseDeviceInfoService{

    private val versionApiEntity = VersionedApiEntity(VersionEntity.CHINESE_DEVICE_INFO)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<ChineseDeviceInfoResponse>>()

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<ChineseDeviceInfoResponse>>() {}.type
            val response = Gson().fromJson<ApiResponse<ChineseDeviceInfoResponse>>(json, type)
            return if (response == null || response.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = versionApiEntity.entityType,
                        langCode = UserPreferenceUtil.getUserLanguages(), version = response.data.version,
                        data = json.toByteArray())
                versionDbInstance.versionServiceDao().insertVersionEntity(versionDbEntity)
                response.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

        return Constants.EMPTY_STRING
    }

    override fun getStoredChineseDeviceInfo(versionMode: VersionMode): Observable<ChineseDeviceInfoResponse> {
        return if (versionMode == VersionMode.CACHE) {
            val type = object : TypeToken<ApiResponse<ChineseDeviceInfoResponse>>() {}.type
            versionedApiHelper.fromCacheKt(entityType = versionApiEntity.entityType, classOfT = type)
                    .map { transform(it) }.onErrorResumeNext { t: Throwable ->  Observable.empty() }
        } else {
            val appSectionsAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(ChineseDeviceInfoAPI::class.java)
            appSectionsAPI.chineseDeviceInfo.map { transform(it) }
        }
    }

    private fun transform(response: ApiResponse<ChineseDeviceInfoResponse>?) : ChineseDeviceInfoResponse {
        return if (response == null || response.data == null) {
             getLocalResponse().data
        } else {
            response.data
        }
    }

    private fun getLocalResponse(): ApiResponse<ChineseDeviceInfoResponse> {
        var json: String? = null
        var response: ApiResponse<ChineseDeviceInfoResponse> = ApiResponse()
        val type = object : TypeToken<ApiResponse<ChineseDeviceInfoResponse>>() {

        }.type
        var `is`: InputStream? = null
        try {
            `is` = CommonUtils.getApplication().assets.open("chinese_devices.json")
            val size = `is`!!.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            try {
                `is`?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

        }
        if (json != null) {
            response = JsonUtils.fromJson<ApiResponse<ChineseDeviceInfoResponse>>(json, type)
                    ?: ApiResponse<ChineseDeviceInfoResponse>()
        }
        return response
    }

}