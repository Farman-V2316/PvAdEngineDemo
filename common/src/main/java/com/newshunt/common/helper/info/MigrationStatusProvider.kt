/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.info

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.interceptor.HeaderInterceptor
import okhttp3.HttpUrl
import okhttp3.Response

/**
 * A singleton provider class to help fill the migration header to the API requests which need them
 * Created by srikanth.ramaswamy on 07/18/2019.
 */
private const val LOG_TAG= "MigrationStatusProvider"
object MigrationStatusProvider {
    private const val WILD_CARD = "*"

    //A mapping between host-> list of paths which need migration header
    private var apiNeedingMigrationHeaderMap: Map<String, List<String>>? = null
    set(value) {
        synchronized(this){
            field = value
        }
    }
    get() {
        synchronized(this) {
            return field
        }
    }

    //Read from preference, updated from other API responses [server defined migration status]
    var migrationState: String? = null
        private set(value) {
            synchronized(this) {
                field = value
            }
        }
        get() {
            synchronized(this) {
                return field
            }
        }

    init {
        //Read the list from previous handshake response
        val preferenceStr = PreferenceManager.getPreference(GenericAppStatePreference
                .API_LIST_MIGRATION_STATUS, Constants.EMPTY_STRING)
        if (!CommonUtils.isEmpty(preferenceStr)) {
            apiNeedingMigrationHeaderMap = JsonUtils.fromJson(preferenceStr, object : TypeToken<Map<String, List<String>>>() {}.type)
        }
        if(PreferenceManager.containsPreference(GenericAppStatePreference.API_MIGRATION_STATUS)) {
            //Read the latest migration header from preference.
            migrationState = PreferenceManager.getPreference(GenericAppStatePreference
                    .API_MIGRATION_STATUS, Constants.EMPTY_STRING)
        }
    }

    /**
     * Returns migration status header if applicable for this url
     */
    fun getMigrationStateHeaderIfApplicable(httpUrl: HttpUrl): String? {
        apiNeedingMigrationHeaderMap ?: return null
        if (CommonUtils.isEmpty(migrationState)) {
            return null
        }

        val host = httpUrl.host()
        val apiPath = httpUrl.encodedPath()
        if (CommonUtils.isEmpty(host) || CommonUtils.isEmpty(apiPath)) {
            return null
        }

        val apiList = apiNeedingMigrationHeaderMap!![host]
        if (CommonUtils.isEmpty(apiList)) {
            return null
        }
        if (apiList?.contains(WILD_CARD) == true) {
            return migrationState
        }


        apiList?.forEach {
            if (apiPath.contains(it)) {
                return migrationState
            }
        }
        return null
    }

    /**
     * Update the migration status header in preference and property
     */
    fun updateMigrationStatus(updatedMigrationStatus: String?) {
        updatedMigrationStatus?.let{
            PreferenceManager.savePreference(GenericAppStatePreference.API_MIGRATION_STATUS, updatedMigrationStatus)
            migrationState = updatedMigrationStatus
            return
        }
        PreferenceManager.remove(GenericAppStatePreference.API_MIGRATION_STATUS)
        Logger.d(LOG_TAG, "Cleared the migration header, don't send anymore")
        migrationState = null
    }

    /**
     * Reads migration header from response and updates the preference and property
     */
    fun readMigrationStatusHeaderFromResponse(response: Response) {
        val migrationStatusHeader = response.header(HeaderInterceptor.MIGRATION_STATUS_HEADER)
        if (!CommonUtils.isEmpty(migrationStatusHeader)) {
            Logger.d(LOG_TAG, "Saving new migration response: $migrationStatusHeader")
            updateMigrationStatus(migrationStatusHeader)
        }
    }

    /**
     * Saves the list of APIs with domain -> path list mapping in to preference and also updates
     * the member variable.
     */
    fun updateAPIListNeedingStatusHeader(apisNeedingMigrationHeader: Map<String, List<String>>?) {
        if (!CommonUtils.isEmpty(apisNeedingMigrationHeader)) {
            PreferenceManager.savePreference(GenericAppStatePreference.API_LIST_MIGRATION_STATUS, JsonUtils.toJson<Map<String, List<String>>>(apisNeedingMigrationHeader))
            apiNeedingMigrationHeaderMap = apisNeedingMigrationHeader
        }
    }
}