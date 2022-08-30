/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.service

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.dhutil.model.entity.launch.TimeWindow
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.notification.asset.NewsStickyOptInEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.isValidUrl
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.notification.model.internal.rest.StreamAPI
import com.newshunt.notification.model.manager.NewsStickyPushScheduler
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import java.util.*
import kotlin.math.max

/**
 * Versioned API implementation for News Sticky Opt in API
 *
 * Created by srikanth.r on 12/3/21.
 */
private const val LOG_TAG = "NewsStickyOptInService"
class NewsStickyOptInServiceImpl(private val versionApiEntity: VersionedApiEntity,
                                 private val versionedApiHelper: VersionedApiHelper<ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>>) {
    private val api = RestAdapterProvider.getRestAdapter(
        Priority.PRIORITY_HIGH, null,
        VersionedApiInterceptor({ json: String -> this.newsStickyOptInReceived(json) }))
        .create(StreamAPI::class.java)

    fun getNewsStickyOptInLocalObs(): Observable<NewsStickyOptInEntity> {
        val type = object : TypeToken<ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>?>() {}.type
        return Observable.fromCallable {
            versionedApiHelper.getLocalEntity(versionApiEntity.entityType, classOfT = type)
        }.map {
            Logger.d(LOG_TAG, "Returning NewsStickyOptInEntity from local DB")
            mapResponse(it)
        }
    }

    fun getNewsStickyOptInLocal(): NewsStickyOptInEntity {
        val type = object : TypeToken<ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>?>() {}.type
        val response = versionedApiHelper.getLocalEntity(versionApiEntity.entityType, classOfT = type)
        return mapResponse(response)
    }

    /**
     * Common method to save the raw response to Versioned DB
     */
    fun newsStickyOptInReceived(json: String) : String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }
        try {
            val type = object : TypeToken<ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>>() {}.type
            val response = JsonUtils.fromJson<ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>>(json, type)
            return if (response == null || response.data == null || response.data.rows.isNullOrEmpty()) {
                Constants.EMPTY_STRING
            } else {
                Logger.d(LOG_TAG, "Received news sticky opt in config from server, version: ${response.data.version}")
                validateResponse(response)?.let { validatedResponse ->
                    val versionDbEntity = VersionDbEntity(entityType = versionApiEntity.entityType, version = validatedResponse.data.version,
                        langCode = UserPreferenceUtil.getUserLanguages(), data = JsonUtils.toJson(validatedResponse).toByteArray())
                    val insertedRows = versionedApiHelper.insertAfterVersionValidation({
                        isVersionUpdateNeeded(it, validatedResponse.data.version)
                    }, versionDbEntity)
                    if (insertedRows != 0L) {
                        validatedResponse.data.rows?.first()?.let { newsStickyOptInEntity ->
                            NewsStickyPushScheduler.cancelAll()
                            //If the config was inserted into DB, we might have some scheduling to do!
                            NewsStickyPushScheduler.scheduleNextBestTimeWindow(null, newsStickyOptInEntity)
                        }
                    } else {
                        Logger.e(LOG_TAG, "Ignored version API response. Invalid or stale response")
                    }
                    validatedResponse.data?.version ?: Constants.EMPTY_STRING
                } ?: Constants.EMPTY_STRING
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    private fun refreshNewsStickyOptIn(version: String?): Observable<NewsStickyOptInEntity> {
        return api.getNewsStickyOptInConfig(version)
            .map {
                mapResponse(it)
            }
    }

    private fun mapResponse(response: ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>?): NewsStickyOptInEntity {
        return if (response == null || response.data == null || response.data.rows.isNullOrEmpty()) {
            NewsStickyOptInEntity()
        } else {
            response.data.rows.first()
        }
    }

    private fun validateResponse(response: ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>): ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>? {
        response.data.rows?.first()?.let {
            val stickyLangs = it.languages
            val hasUserSelectedLang = UserPreferenceUtil.getUserLanguagesList().isNullOrEmpty().not()
            if (hasUserSelectedLang) {
                /**
                 * If user has selected a language, all the languages in the response must be part of
                 * user selected language set for the response to be honoured.
                 * Else discard the response.
                 */
                if (stickyLangs.isNullOrEmpty() ||
                    !UserPreferenceUtil.getUserLanguagesList().containsAll(stickyLangs)) {
                    Logger.e(LOG_TAG, "Discarding api response, serverLang: $stickyLangs, userLangs: ${UserPreferenceUtil.getUserLanguagesList()}")
                    return null
                }
            }
            it.timeWindows?.let { timeWindows ->
                it.timeWindows = mergeOverlappingTimeWindows(timeWindows)
            }
        }
        return response
    }

    /**
     * Helper method to sort the time window array and merge the overlapping time windows
     */
    private fun mergeOverlappingTimeWindows(timeWindows: List<TimeWindow>): List<TimeWindow> {
        if (timeWindows.isEmpty() || timeWindows.size == 1) {
            return timeWindows
        }
        val outputList = LinkedList<TimeWindow>()
        timeWindows.sortedBy {
            it.startTimeMs
        }.let { sortedList ->
            sortedList.forEach { item ->
                if (outputList.isEmpty() || item.startTimeMs > outputList.last.endTimeMs) {
                    outputList.add(item.copy())
                } else {
                    outputList.last.endTimeMs = max(item.endTimeMs, outputList.last.endTimeMs)
                }
            }
        }
        return outputList
    }

    companion object {
        @JvmStatic
        fun newInstance(): NewsStickyOptInServiceImpl {
            return NewsStickyOptInServiceImpl(VersionedApiEntity(VersionEntity.NEWS_STICKY_OPTIN),
                VersionedApiHelper())
        }

        @JvmStatic
        fun refreshNewsStickyOptIn(localVersion: String?, serverVersion: String?): Observable<NewsStickyOptInEntity> {
            return if (isVersionUpdateNeeded(localVersion, serverVersion)) {
                newInstance().refreshNewsStickyOptIn(localVersion)
            } else {
                Observable.empty()
            }
        }

        fun isNewsStickyOptinValid(entity: NewsStickyOptInEntity): Boolean {
            return !entity.timeWindows.isNullOrEmpty() &&
                    entity.startTime > 0 &&
                    entity.expiryTime > 0 &&
                    entity.expiryTime > entity.startTime &&
                    entity.metaUrl.isValidUrl()
        }

        /**
         * The versions are longs in string format. Since we get the response from more than 1 path,
         * check the version in local DB < server version before updating the DB.
         */
        private fun isVersionUpdateNeeded(localVersion: String?, serverVersion: String?): Boolean {
            var localV: Long = -1
            var serverV: Long = -1
            try {
                localV = localVersion?.toLong() ?: -1
                serverV = serverVersion?.toLong() ?: -1
            } catch (exception: NumberFormatException) {
                Logger.caughtException(exception)
            }
            return (localV == -1L || serverV == -1L || serverV > localV)
        }
    }
}