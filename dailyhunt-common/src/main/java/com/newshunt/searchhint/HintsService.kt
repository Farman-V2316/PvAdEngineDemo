/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.searchhint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DummyDisposable
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.searchhint.entity.HintServiceEntity
import com.newshunt.dataentity.searchhint.entity.SearchHint
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * For fetching hints
 *
 * @author satosh.dhanyamraju
 */
object HintsService {
    private const val LOG_TAG = "HintServiceImpl"
    @JvmStatic
    private val hintLiveData = MutableLiveData<HintServiceEntity>()
    private var atleast1Success = false
    private val apiEntity = VersionedApiEntity(VersionEntity.SEARCH_HINT)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<HintServiceEntity>>()

    @JvmStatic
    @JvmOverloads
    fun performHintSync(searchLocation: SearchLocation = SearchLocation.NewsHome, useNetwork: Boolean = false ): LiveData<List<SearchHint>> {
        Logger.d(LOG_TAG, "performHintSync called with $searchLocation, nw=$useNetwork")
        (if(useNetwork) Observable.concat(getStoredHints(), getHintsFromServer()) else getStoredHints())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribeWith(DummyDisposable())
        return Transformations.map(hintLiveData) {
            it?.hint?.get(UserPreferenceUtil.getUserNavigationLanguage())?.get(searchLocation.name)?: listOf()
        }
    }

    @JvmStatic
    private fun getStoredHints() : Observable<HintServiceEntity> {
        val type = object : TypeToken<ApiResponse<HintServiceEntity>>() {}.type
        return versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                .map { transform(it)  }.onErrorResumeNext { t: Throwable -> Observable.empty() }
    }

    @JvmStatic
    private fun getHintsFromServer() : Observable<HintServiceEntity> {
        return Observable.fromCallable{
            val version = VersionedApiHelper.getLocalVersion(entityType = apiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap {version ->
            val hintApi = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_LOW, null,
                    VersionedApiInterceptor({ json: String -> this.validator(json) }))
                    .create(HintServiceApi::class.java)
            hintApi.getHint(UserPreferenceUtil.getUserNavigationLanguage(), version)
                    .map { transform(it) }
        }
    }


    private fun transform(response: ApiResponse<HintServiceEntity>?) : HintServiceEntity {
        val hintServiceEntity = response?.data ?: HintServiceEntity("", mapOf())
        val itemCount =( hintServiceEntity.hint ?: mapOf()).size
        when {
            itemCount > 0 -> { // always post success response
                hintLiveData.postValue(hintServiceEntity)
                atleast1Success = true
                Logger.d(LOG_TAG, "posting $itemCount items")
            }

            !atleast1Success -> { // don't post error, if altease 1 success - it is cache_and_update
                hintLiveData.postValue(hintServiceEntity)
                Logger.e(LOG_TAG, "not posting error response")
            }
        }
        return hintServiceEntity
    }

    private fun validator(jsonResponse: String): String {
        if (CommonUtils.isEmpty(jsonResponse)) return Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<HintServiceEntity>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<HintServiceEntity>>(jsonResponse, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = jsonResponse.toByteArray(),
                        version = apiResponse.data.version, langCode = UserPreferenceUtil.getUserLanguages())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }
}
