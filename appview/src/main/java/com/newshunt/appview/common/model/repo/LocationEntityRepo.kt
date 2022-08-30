/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.model.repo

/*
 * @author priya.gupta
*/


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.AllLocationResponse
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.apis.LocationApi
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

class LocationEntityRepo(val section: String) {

    private val versionedApiHelper = VersionedApiHelper<ApiResponse<AllLocationResponse>>()
    private val apiEntity = VersionedApiEntity(VersionEntity.ALL_LOCATION)
    private val TAG = "LocationEntityRepo"


    fun getLocations(): Observable<List<Locations>> {
        val localData = getLocalData()
        val serverData = getLocationsFromServer()
                .map {
                    val locations = it.rows ?: emptyList()
                    SocialDB.instance().locationsDao().replaceLocations(locations)
                    it
                }
        return Observable.mergeDelayError(localData,serverData)
        .flatMap { main ->
            SocialDB.instance().locationsDao().getLocationsNestedObs()
        }
    }

    fun getLocalData(): Observable<AllLocationResponse> {
        val type = object : TypeToken<ApiResponse<AllLocationResponse>?>() {}.type
        return Observable.fromCallable {
            versionedApiHelper.getLocalEntity(entityType = apiEntity.entityType,
                    classOfT = type)
        }.map {
            Logger.d(TAG, "Local config : ${it.data}")
            it.data
        }
    }


    fun getLocationsFromServer(): Observable<AllLocationResponse> {
        return Observable.fromCallable {
            val localVersion = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.ALL_LOCATION.name)
                    ?: Constants.EMPTY_STRING
            Logger.d(TAG, "getLocationsFromServer: localVersion=$localVersion")
            return@fromCallable localVersion
        }.flatMap { version ->
            val edition = UserPreferenceUtil.getUserEdition()
            val apiEntity = getVersionedApiEntity(UserPreferenceUtil.getUserLanguages(), edition)
            RestAdapterProvider.getRestAdapter(Priority.PRIORITY_NORMAL, this,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(LocationApi::class.java)
                    .getAllLocation(section, UserPreferenceUtil.getUserNavigationLanguage(), apiEntity.languageCode, version)
                    .map { response ->
                        val list = if (response?.data != null) response.data else
                            AllLocationResponse(rows = emptyList())
                        Logger.d(TAG, "server response map ${response.data}")
                        list
                    }.doOnError {
                        Logger.e(TAG, "getLocationsFromServer: ERROR. ${it.message}")
                    }
        }
    }

    private fun getVersionedApiEntity(languages: String?, edition: String): VersionedApiEntity {
        val versionedApiEntity = VersionedApiEntity(VersionEntity.ALL_LOCATION)
        versionedApiEntity.languageCode = languages
        versionedApiEntity.edition = edition
        return versionedApiEntity
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<AllLocationResponse>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<AllLocationResponse>>(json, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType,
                        version = apiResponse.data.version,
                        langCode = UserPreferenceUtil.getUserLanguages(),
                        data = json.toByteArray())
                Logger.d(TAG, "validate: written to local DB")
                VersionedApiHelper<ApiResponse<AllLocationResponse>>().insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

        return Constants.EMPTY_STRING
    }

}