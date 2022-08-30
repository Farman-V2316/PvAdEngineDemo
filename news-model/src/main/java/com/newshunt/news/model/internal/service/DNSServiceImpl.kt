/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.internal.service

import android.os.AsyncTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.DNSEntry
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.server.asset.DNSConfig
import com.newshunt.common.model.retrofit.UnifiedDns
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.internal.rest.DNSAPI
import com.newshunt.news.model.service.DNSService
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import java.util.*


/**
 * Implementation of [DNSService]. It takes care of making request to fetch DNS
 * Configuration, process the response while supporting Versioned API service for DNS configuration.
 *
 * @author karthik.r
 */
class DNSServiceImpl : DNSService {
    //Only for testing purpose
    val apiEntity: VersionedApiEntity = VersionedApiEntity(VersionEntity.DNS_CONFIG)
    val versionedApiHelper = VersionedApiHelper<ApiResponse<DNSConfig>>()

    private fun getVersion(json: String): String {
        if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<DNSConfig>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<DNSConfig>>(json, type)

            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, langCode = UserPreferenceUtil.getUserLanguages(),
                        version = apiResponse.data.version, data = json.toByteArray())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }

        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    private fun processDNSConfig(data: DNSConfig?) {
        if (data == null) {
            return
        }

        val disableDNSCaching = data.isDisableDNSCaching
        PreferenceManager.saveBoolean(Constants.DISABLE_DNS_CACHING, disableDNSCaching)

        val firstLevelTTLCache = data.firstLevelCacheTTL
        if (firstLevelTTLCache == 0L) {
            PreferenceManager.remove(Constants.DNS_FIRST_CACHE_TTL)
        } else {
            PreferenceManager.saveLong(Constants.DNS_FIRST_CACHE_TTL, firstLevelTTLCache)
        }
        val secondLevelCacheTTL = data.secondLevelCacheTTL
        if (secondLevelCacheTTL == 0L) {
            PreferenceManager.remove(Constants.DNS_SECOND_CACHE_TTL)
        } else {
            PreferenceManager.saveLong(Constants.DNS_SECOND_CACHE_TTL, secondLevelCacheTTL)
        }
        PreferenceManager.saveLong(Constants.HEARTBEAT_INTERVAL, data.scheduleHeartbeatInterval)
        val dnsLookupTimeout = data.dnsLookupTimeout
        if (dnsLookupTimeout == 0L) {
            PreferenceManager.remove(Constants.DNS_LOOKUP_TIMEOUT)
        } else {
            PreferenceManager.saveLong(Constants.DNS_LOOKUP_TIMEOUT, dnsLookupTimeout)
        }
        val bgDnsLookupTimeout = data.bgDnsLookupTimeout
        if (bgDnsLookupTimeout == 0L) {
            PreferenceManager.remove(Constants.DNS_BG_LOOKUP_TIMEOUT)
        } else {
            PreferenceManager.saveLong(Constants.DNS_BG_LOOKUP_TIMEOUT, dnsLookupTimeout)
        }
        val tpDnsLookupTimeout = data.tpDnsLookupTimeout
        if (tpDnsLookupTimeout == 0L) {
            PreferenceManager.remove(Constants.DNS_TP_LOOKUP_TIMEOUT)
        } else {
            PreferenceManager.saveLong(Constants.DNS_TP_LOOKUP_TIMEOUT, tpDnsLookupTimeout)
        }
        data.dnsServers?.let {
            PreferenceManager.saveString(Constants.DNS_SERVERS , it.joinToString())
        }

        if (!CommonUtils.isEmpty(data.dnsEntries)) {
            val hostnameIPsMap = HashMap<String, DNSEntry>()
            for (dnsEntry in data.dnsEntries) {
                hostnameIPsMap[dnsEntry.hostname] = dnsEntry
            }

            val dnsJson = JsonUtils.toJson<Map<String, DNSEntry>>(hostnameIPsMap)
            PreferenceManager.saveString(Constants.DNS_IP_FROM_SERVER, dnsJson)
        } else {
            PreferenceManager.remove(Constants.DNS_IP_FROM_SERVER)
        }

        UnifiedDns.loadDefaultValuesFromServer()
    }

    /**
     * {@inheritDoc}
     */
    override fun getDNSContent(versionMode: VersionMode) : Observable<DNSConfig> {
       return if (versionMode == VersionMode.CACHE) {
            val type = object : TypeToken<ApiResponse<DNSConfig>>() {}.type
            versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                    .map { if (it == null) throw Exception("data is null") else transformResponse(it) }
                    .onErrorResumeNext { t: Throwable ->  Observable.empty()}
        } else {
           getDnsFromServer()
       }
    }

    private fun getDnsFromServer() : Observable<DNSConfig> {
        return Observable.fromCallable{
            val version = VersionedApiHelper.getLocalVersion(entityType = apiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap {version ->
            val dnsAPI = RestAdapterProvider.getRestAdapter(
                    Priority.PRIORITY_LOW, null, AsyncTask.THREAD_POOL_EXECUTOR,
                    VersionedApiInterceptor({ json: String -> this.getVersion(json) }))
                    .create(DNSAPI::class.java)
            dnsAPI.getDNSConfig(version).map { transformResponse(it) }
        }
    }

    private fun transformResponse(response: ApiResponse<DNSConfig>) : DNSConfig {
        if (response.data != null) {
            processDNSConfig(response.data)
        }
        return response.data
    }
}