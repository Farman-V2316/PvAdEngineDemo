/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.retrofit

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.DNSEntry
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import java.lang.reflect.Type
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.Dns
import okhttp3.Request

/**
 * For caching dns lookups and fallback to LB when 408 is received.
 *
 * @see CacheDns
 * @see Http408Dns
 *
 * @author satosh.dhanyamraju
 */
object UnifiedDns : Dns {

    @JvmField
    var callback: DnsDevEventCallback? = null

    private val disableDns: Boolean
        get() = PreferenceManager.getBoolean(Constants.DISABLE_DNS_CACHING, false)

    private val lbIps: Map<String, DNSEntry>
        get() {
            val tmp: Map<String, DNSEntry> = readPrefAndConvert(
                    Constants.DNS_IP_FROM_SERVER,
                    object : TypeToken<Map<String, DNSEntry>>() {}.type, mapOf())
            return tmp
            }

    private val cacheDns: CacheDns by lazy {
        CacheDns(lbIps.mapValues { it.value.toInetList() }, callback)
    }

    private val http408Dns by lazy {
        Http408Dns(lbIps, callback).apply {
            scheduleDNSOverrideNecessityCheck()
        }
    }

    private val prePopulatedDns by lazy {
        PrePopulatedDns(lbIps)
    }


    override fun lookup(hostname: String): MutableList<InetAddress> {
        if (disableDns) {
            Logger.d(LOG_TAG, "[$hostname] lookup: DISABLED custom handling")
            return Dns.SYSTEM.lookup(hostname)
        }
        Logger.v(LOG_TAG, "[$hostname] lookup: START")
        val iPList = try {
            when {
                prePopulatedDns.needsToHandle(hostname) -> {
                    Logger.v(LOG_TAG, "[$hostname] lookup: PrePopulatedDns")
                    prePopulatedDns.lookup(hostname)
                }
                http408Dns.needsToHandle(hostname) -> {
                    Logger.v(LOG_TAG, "[$hostname] lookup: 408dns")
                    http408Dns.lookup(hostname)
                }
                else -> {
                    Logger.v(LOG_TAG, "[$hostname] lookup: Cachedns")
                    cacheDns.lookup(hostname)
                }
            }.also {
                Logger.d(LOG_TAG, "[$hostname] lookup: got ${it.size} entries")
            }
        } catch (e: UnknownHostException) {
            Logger.e(LOG_TAG, "[$hostname] lookup: ${e.message}. re-throwing")
            throw e
        } catch (e: Exception) {
            Logger.e(LOG_TAG, "[$hostname] lookup: ${e.message}")
            throw UnknownHostException(hostname)
        } finally {
            Logger.v(LOG_TAG, "[$hostname] lookup: COMPLETED")
        }
        Logger.i(LOG_TAG, "[$hostname] Finally resolved IPs: $iPList")
        return iPList
    }


    @JvmStatic
    fun onRequestTimeoutError(request: Request) {
        if (!disableDns) http408Dns.onRequestTimeoutError(request)
    }

    @JvmStatic
    fun scheduleDNSOverrideNecessityCheck() {
        if (!disableDns) CommonUtils.runInBackground {
            if (!disableDns) http408Dns.scheduleDNSOverrideNecessityCheck()
        }
    }

    @JvmStatic
    fun cancelDNSOverrideNecessityCheck() {
        if (!disableDns) CommonUtils.runInBackground {
            if (!disableDns) http408Dns.cancelDNSOverrideNecessityCheck()
        }

    }

    @JvmStatic
    fun loadDefaultValuesFromServer() {
        cacheDns.initFromPreferences()
        cacheDns.mapOfLb = lbIps.mapValues { it.value.toInetList() }
        http408Dns.dnsValuesFromServer = lbIps
        prePopulatedDns.dnsValuesFromServer = lbIps
        Logger.i(LOG_TAG, "value updated  $disableDns")
    }

    @JvmStatic
    fun resetLastSavedDefaults() = CacheDns.resetLastSavedDefaults()

}

internal fun DNSEntry.toInetList() = ip.map { InetAddress.getByName(it) }

/**
 * Read a string preference, convert that json string to an object, if anything fails, return
 * default value
 */
internal fun <T> readPrefAndConvert(preferenceKey: String, type: Type, defaultValue: T): T {
    val contentString = PreferenceManager.getString(preferenceKey)
    return if (!CommonUtils.isEmpty(contentString)) {
        try {
            JsonUtils.fromJson(contentString, type) ?: defaultValue
        } catch (ex: Exception) {
            defaultValue
        }
    } else {
        defaultValue
    }
}

internal val activeNetwork: String
    get() = NetworkSDKUtils.getActiveNetworkInfoStr(CommonUtils.getApplication())

class NetworkData {
    private val map = hashMapOf<String, AtomicBoolean>()
    fun bgDns(host: String) = map.getOrPut("bg$activeNetwork$host") {
        AtomicBoolean(false)
    }
    fun dnsTimeout(host: String) = map.getOrPut("f$activeNetwork$host") {
        AtomicBoolean(false)
    }
}

private const val LOG_TAG = "UnifiedDns"