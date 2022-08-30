/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.retrofit

import android.annotation.SuppressLint
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ConnectionInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.sdk.network.connection.ConnectionType
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import okhttp3.Dns
import org.xbill.DNS.Address
import org.xbill.DNS.ExtendedResolver
import org.xbill.DNS.Lookup
import org.xbill.DNS.SimpleResolver

/**
 * Caches every lookup for a configurable time. Keep updating cache in background, with every
 * lookup.
 *
 * @author karthik.r
 */

class CacheDns(var mapOfLb: Map<String, List<InetAddress>>,
                       private val callback: DnsDevEventCallback? = null) : Dns {


    private val dnsCache: MutableMap<String, TimeToAddr> = readPrefAndConvert(
            Constants.DNS_LOOKUP_CACHE,
            object : TypeToken<Map<String, TimeToAddr>>() {}.type, hashMapOf())

    private val networkData = NetworkData()
    // same as Executors.newFixedThreadPool(), except we have threads alive for 60sec, and allow
    // core threads to terminate (when idle).
    private val executors: ExecutorService = ThreadPoolExecutor(4, 4, 60, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>()).apply { allowCoreThreadTimeOut(true) }
    private var firstCacheTTL: Long = FIRST_CACHE_TTL
    private var secondCacheTTL: Long = SECOND_CACHE_TTL
    private var tpDnsLookupTimeout: Long = SECOND_CACHE_TTL
    private val resolversInit by lazy {
        try {
            val dnsServersFromPref = PreferenceManager.getString(Constants.DNS_SERVERS)
            val dnsServers = if (dnsServersFromPref?.trim().isNullOrBlank())
                "8.8.8.8, 8.8.4.4" /*Google Public DNS*/
            else dnsServersFromPref
            val prefs = dnsServers?.split(",")?.map { it.trim() } ?: emptyList()
            if (prefs.isEmpty()) {
                Logger.d(LOG_TAG, "tplookup INIT no server ips ${Thread.currentThread().name}")
                false
            } else {
                Lookup.setDefaultResolver(ExtendedResolver(prefs.map {
                    SimpleResolver(it)
                }.toTypedArray()))
                Logger.d(LOG_TAG, "tplookup INIT $prefs ${Thread.currentThread().name}")
                true
            }
        } catch (throwable: Throwable) {
            Logger.caughtException(throwable)
            false
        }
    }

    private var defaultDnsLookupTimeout: Long = DEFAULT_DNS_LOOKUP_TIMEOUT
    private var bgDnsLookupTimeout: Long = DEFAULT_BG_DNS_LOOKUP_TIMEOUT

    init {
        initFromPreferences()
    }

    internal fun initFromPreferences() {
        firstCacheTTL = PreferenceManager.getLong(Constants.DNS_FIRST_CACHE_TTL, FIRST_CACHE_TTL)
        secondCacheTTL = PreferenceManager.getLong(Constants.DNS_SECOND_CACHE_TTL, SECOND_CACHE_TTL)
        defaultDnsLookupTimeout = PreferenceManager.getLong(Constants.DNS_LOOKUP_TIMEOUT, DEFAULT_DNS_LOOKUP_TIMEOUT)
        bgDnsLookupTimeout = PreferenceManager.getLong(Constants.DNS_BG_LOOKUP_TIMEOUT, DEFAULT_DNS_LOOKUP_TIMEOUT)
        tpDnsLookupTimeout = PreferenceManager.getLong(Constants.DNS_TP_LOOKUP_TIMEOUT, DEFAULT_TP_DNS_LOOKUP_TIMEOUT)
    }

    override fun lookup(hostname: String): MutableList<InetAddress> {
        val timeToAddr = dnsCache[hostname]
        val cacheEntryTimeDiff = System.currentTimeMillis() - (timeToAddr?.time ?: -1)
        val backup = (if (cacheEntryTimeDiff < secondCacheTTL)
            timeToAddr?.addr else mapOfLb[hostname])?.toMutableList()
        return when {
            timeToAddr?.addr?.toMutableList()?.isNotEmpty() == true && cacheEntryTimeDiff < firstCacheTTL -> {
                Logger.d(LOG_TAG, "[$hostname] lookup: resolved from 1st-level-cache")
                timeToAddr.addr.toMutableList().also {
                    bgLookup(hostname)
                }
            }
            backup?.isNotEmpty() == true -> {
                Logger.d(LOG_TAG, "[$hostname] lookup: 2nd level ENTERED ")
                val address =
                        if (networkData.dnsTimeout(hostname).get()) null
                        else attemptTimedWaitParallel(hostname)
                Logger.d(LOG_TAG, "[$hostname] lookup: 2nd level GOT addr=$address, bkp=$backup")
                if (address?.isNotEmpty() == true) address else backup.also { bgLookup(hostname, true) }
            }
            else -> {
                finalStepParallel(hostname)
            }
        }
    }

    @Deprecated("use finalStepParallel")
    private fun finalStep(hostname: String): MutableList<InetAddress> {
        Logger.d(LOG_TAG, "[$hostname] lookup: System wait. no timeout. may throw")
        val l3 = systemLookup(hostname, true)
        val l2 = tpLookup(hostname, true).timeout(tpDnsLookupTimeout, MILLISECONDS, l3)
        val l1 = systemLookup(hostname, true).timeout(defaultDnsLookupTimeout, MILLISECONDS, l2)
        return try {
            l1.onErrorResumeNext(l2).blockingFirst()
        } catch (e: Exception) {
            throw (if (e is UnknownHostException) e
            else UnknownHostException("failed to resolve $hostname. root cause: ${e.message}"))
        }
    }

    private fun finalStepParallel(hostname: String): MutableList<InetAddress> {
        Logger.d(LOG_TAG, "[$hostname] lookup: finalStepParallel: System wait. no timeout. may throw")
        val l2 = systemLookup(hostname, true).subscribeOn(Schedulers.io())
        val l1 = Observable.mergeDelayError(
            tpLookup(hostname, true).timeout(tpDnsLookupTimeout, MILLISECONDS).subscribeOn(Schedulers.io()),
            systemLookup(hostname, true).timeout(defaultDnsLookupTimeout, MILLISECONDS).subscribeOn(Schedulers.io()))
        return try {
            l1.onErrorResumeNext(l2)
                .doOnNext { Logger.v(LOG_TAG, "[$hostname] finalStepParallel : Succ; $it") }
                .doOnError { Logger.v(LOG_TAG, "[$hostname] finalStepParallel : Err; $it") }
                .blockingFirst()
        } catch (e: Exception) {
            throw (if (e is UnknownHostException) e
            else UnknownHostException("failed to resolve $hostname. root cause: ${e.message}"))
        }
    }

    @Deprecated("use attemptTimedWaitParallel")
    private fun attemptTimedWait(hostname: String) = try {
      var subscribedAt = System.currentTimeMillis()
      val l2 = tpLookup(hostname, true).timeout(tpDnsLookupTimeout, MILLISECONDS)
      val l1 = systemLookup(hostname, true).timeout(defaultDnsLookupTimeout, MILLISECONDS, l2)
      l1.doOnSubscribe { subscribedAt = System.currentTimeMillis() }
          .doOnError {
            Logger.e(LOG_TAG, "[$hostname] timedwait : Got $it ${it.message}")
            if ((it is TimeoutException || it is InterruptedException)
                && ConnectionInfoHelper.getConnectionType() != ConnectionType.NO_CONNECTION.connectionType) {
              callback?.dnsLookupTimeout(hostname, System.currentTimeMillis() - subscribedAt)
              networkData.dnsTimeout(hostname).set(true)
            } else {
              Logger.v(LOG_TAG, "[$hostname]  timedwait : not fired event. $it, " +
                  ConnectionInfoHelper.getConnectionType())
            }
          }
          .onErrorReturnItem(mutableListOf())
          .blockingFirst(mutableListOf())
    } catch (t: Throwable) {
        null
    }

    private fun attemptTimedWaitParallel(hostname: String) = try {
      var subscribedAt = System.currentTimeMillis()
      val l2 = tpLookup(hostname, true).timeout(tpDnsLookupTimeout, MILLISECONDS)
          .doOnNext { Logger.v(LOG_TAG, "[$hostname] timedwaitP : tpLookup; Succ; $it") }
          .doOnError { Logger.v(LOG_TAG, "[$hostname] timedwaitP : tpLookup; Err; $it") }
          .subscribeOn(Schedulers.io())
      val l1 = systemLookup(hostname, true).timeout(defaultDnsLookupTimeout, MILLISECONDS)
          .doOnNext { Logger.v(LOG_TAG, "[$hostname] timedwaitP : sysLookup; Succ; $it") }
          .doOnError { Logger.v(LOG_TAG, "[$hostname] timedwaitP : sysLookup; Err; $it") }
          .subscribeOn(Schedulers.io())
      Observable
          .mergeDelayError(l1, l2)
          .doOnSubscribe { subscribedAt = System.currentTimeMillis() }
          .doOnError {
            Logger.e(LOG_TAG, "[$hostname] timedwaitP : Got $it ${it.message}")
            if ((it is TimeoutException || it is InterruptedException)
                && ConnectionInfoHelper.getConnectionType() != ConnectionType.NO_CONNECTION.connectionType) {
              callback?.dnsLookupTimeout(hostname, System.currentTimeMillis() - subscribedAt)
              networkData.dnsTimeout(hostname).set(true)
            } else {
              Logger.v(LOG_TAG, "[$hostname]  timedwaitP : not fired event. $it, " +
                  ConnectionInfoHelper.getConnectionType())
            }
          }
          .onErrorReturnItem(mutableListOf())
          .blockingFirst(mutableListOf())
    } catch (t: Throwable) {
        null
    }


    private fun cachePut(hostname: String, ad: MutableList<InetAddress>) {
        dnsCache[hostname] = TimeToAddr(ad)
        try {
            val contentString = JsonUtils.toJson<Map<String, TimeToAddr>>(dnsCache)
            PreferenceManager.saveString(Constants.DNS_LOOKUP_CACHE, contentString)
        } catch (e: Throwable) {
            Logger.caughtException(e)
        }
    }

    private fun systemLookup(hostname: String, fg: Boolean = false):
            Observable<MutableList<InetAddress>> {
        val sysLookup = Observable.fromCallable {
            Logger.i(LOG_TAG, "[$hostname][fg$fg] systemLookup: Dns.SYSTEM lookup entered")
            Dns.SYSTEM.lookup(hostname)
        }.doOnNext { cachePut(hostname, it); networkData.dnsTimeout(hostname).set(false) }
            .doOnError { Logger.d(LOG_TAG, "[$hostname][fg$fg] systemLookup:  threw $it") }
      return sysLookup
    }

    /**
     * 3rd party lookup. Always timed.
     */
    private fun tpLookup(hostname: String, fg: Boolean = false): Observable<MutableList<InetAddress>> {
        return Observable.fromCallable {
            Logger.d(LOG_TAG, "[$hostname][fg$fg] tplookup : entered")
            if (resolversInit) {
                Address.getAllByName(hostname).filterNotNull().toMutableList()
            } else {
                throw Exception("tplookup : no 3rd party dns servers to lookup")
            }
        }.doOnNext {
            Logger.d(LOG_TAG, "[$hostname][fg$fg] tplookup : returned ${it}")
            cachePut(hostname, it); networkData.dnsTimeout(hostname).set(false)
        }.doOnError {
            Logger.e(LOG_TAG, "[$hostname][fg$fg] tplookup : threw $it; ${it.message}; $hostname")
        }
    }

    /**
     * Returns immediately. Limited Bg threads
     */
    @SuppressLint("CheckResult")
    private fun bgLookup(hostname: String, onlySystem: Boolean = false) {
        if (networkData.bgDns(hostname).compareAndSet(false, true)) {
            Logger.d(LOG_TAG, "[$hostname] bgUpdate: creating")
          val l3 = systemLookup(hostname)
          val l2 = tpLookup(hostname, false).timeout(tpDnsLookupTimeout, MILLISECONDS, l3)
          val l1 = systemLookup(hostname, false).timeout(bgDnsLookupTimeout, MILLISECONDS, l2)
            val observable =
                    if (onlySystem) systemLookup(hostname)
                    else l1
            observable.subscribeOn(Schedulers.from(executors))
                    .subscribe({
                    }, {
                        Logger.v(LOG_TAG, "[$hostname] bgUpdate: ${it.message}")
                        networkData.bgDns(hostname).set(false)
                    }, {
                        Logger.v(LOG_TAG, "[$hostname] bgUpdate: Done")
                        networkData.bgDns(hostname).set(false)
                    }, {
                        Logger.v(LOG_TAG, "[$hostname] bgUpdate: Started")
                        networkData.bgDns(hostname).set(true)
                    })
        } else {
            Logger.v(LOG_TAG, "[$hostname] bgUpdate: not started. should be running.")
        }
    }

    companion object {
        const val DEFAULT_DNS_LOOKUP_TIMEOUT = (5 * 1000).toLong()
        const val DEFAULT_TP_DNS_LOOKUP_TIMEOUT = (5 * 1000).toLong()
        const val DEFAULT_BG_DNS_LOOKUP_TIMEOUT = (60 * 1000).toLong()
        const val FIRST_CACHE_TTL = (15 * 60 * 1000).toLong()
        const val SECOND_CACHE_TTL = (24 * 60 * 60 * 1000).toLong()

        /**
         * Reset last saved values to defaults.
         */
        internal fun resetLastSavedDefaults() {
            Logger.e(LOG_TAG, "resetLastSavedDefaults")
            PreferenceManager.saveLong(Constants.DNS_FIRST_CACHE_TTL, FIRST_CACHE_TTL)
            PreferenceManager.saveLong(Constants.DNS_SECOND_CACHE_TTL, SECOND_CACHE_TTL)
            PreferenceManager.saveLong(Constants.DNS_LOOKUP_TIMEOUT, DEFAULT_DNS_LOOKUP_TIMEOUT)
            PreferenceManager.saveLong(Constants.DNS_BG_LOOKUP_TIMEOUT, DEFAULT_BG_DNS_LOOKUP_TIMEOUT)
            PreferenceManager.saveLong(Constants.DNS_TP_LOOKUP_TIMEOUT, DEFAULT_TP_DNS_LOOKUP_TIMEOUT)
        }
    }
}

/**
 * Callback to track DNS lookup timeout events.
 */
interface DnsDevEventCallback {
    fun dnsLookupTimeout(hostname: String, timeoutDuration: Long)

    fun domainRecovered(hostname: String)
}

data class TimeToAddr(val addr: MutableList<InetAddress> = mutableListOf() ,
                      val  time: Long = System.currentTimeMillis())

private const val LOG_TAG = "CacheDns"