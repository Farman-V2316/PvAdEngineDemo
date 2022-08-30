/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.retrofit

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.sqlite.DNS_DB
import com.newshunt.common.model.sqlite.dao.DnsDao
import com.newshunt.common.model.sqlite.entity.Heartbeat408Entry
import com.newshunt.sdk.network.Priority
import okhttp3.Dns
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url
import java.net.HttpURLConnection
import java.net.InetAddress
import kotlin.math.min

/**
 * If current network+hostname combination is identified to be bad (received recent 408), then LB
 * IPs provided by a versioned API is used to resolve hostname.
 * Schedules/cancels regular heartbeat check and update networks state based on the response.
 *
 * @author karthik.r
 */
internal class Http408Dns(var dnsValuesFromServer: Map<String, com.newshunt.dataentity.common.model.entity.DNSEntry>,
                          private val callback: DnsDevEventCallback?,
                          private val dao : DnsDao = DNS_DB.dao()) : Dns {

    private val api by lazy {
        // Use System DNS and check quality of network
        val retrofit = RestAdapters.getBuilderWithDNS(AppConfig.getInstance()!!.newsAPIEndPoint, false,
                Priority.PRIORITY_HIGHEST, null, Dns.SYSTEM).build()
        retrofit.create<HeartbeatApi>(HeartbeatApi::class.java)
    }

    private val handler: Handler = Handler(Looper.getMainLooper()) { msg ->
        PreferenceManager.saveLong(PREF_HEARTBEAT_NEXT_SCHEDULE, Long.MAX_VALUE)
        val nw = activeNetwork
        CommonUtils.runInBackground {
            dao.entriesForNetwork(activeNetwork).map {
                Logger.v(LOG_TAG, "[$it] Evaluating quality of network, $activeNetwork, $nw")
                val call = api.check(it.heartbeatUrl)
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>,
                                            response: Response<Void>) {
                            if (response.code() == HttpURLConnection.HTTP_OK) {
                                // Ensure network has not switched between sending request and receiving response
                                if (activeNetwork == nw) CommonUtils.runInBackground {
                                    Logger.d(LOG_TAG, "[$it] recovered")
                                    callback?.domainRecovered(it.host);
                                    dao.delete(it)
                                } else {
                                    Logger.d(LOG_TAG, "[$it] nw changed. $nw, $activeNetwork")
                                }
                            } else {
                                Logger.e(LOG_TAG, "[$it] checkCurrentNetwork: error ${response.code()}")
                                send(withDelay = true)
                            }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                            Logger.e(LOG_TAG, "[$it] checkCurrentNetwork: onFailure: ${t.message}")
                            send(withDelay = true)
                    }
                })

            }
        }
        Logger.d(LOG_TAG, "handler callback completed")
        true
    }

    override fun lookup(hostname: String): MutableList<InetAddress> {
            return dnsValuesFromServer[hostname]?.toInetList()?.toMutableList()
                    ?: mutableListOf()
    }

    fun needsToHandle(hostname: String) =
            dao.entryForNetworkAndHost(activeNetwork, hostname).isNotEmpty()

    /**
     * Cancel any pending jobs for checking network issue.
     */
    internal fun cancelDNSOverrideNecessityCheck() {
        handler.removeMessages(MSG)
        Logger.v(LOG_TAG, "cancelDNSOverrideNecessityCheck")
    }

    /**
     * Callback when server responds with Request timeout.
     */
    internal fun onRequestTimeoutError(request: Request) {
        try {
            val hostname: String? = (request.url()?.host()) ?: return Unit.also {
                Logger.e(LOG_TAG, "onRequestTimeoutError: couldn't parse hostname.")
            }
            hostname ?: return Unit.also {
                Logger.e(LOG_TAG, "onRequestTimeoutErrror: hostname is null")
            }
            val entry = dnsValuesFromServer[hostname] ?: return Unit.also {
                // Don't bother about unknown hosts.
                Logger.e(LOG_TAG, "onRequestTimeoutError: no LB entry for $hostname")
            }
            val url = entry.heartbeatUrl?: return Unit.also {
                Logger.e(LOG_TAG, "heartbeat null")
            }
            if (entry.heartbeatUrl?.isNotEmpty() != true) return Unit.also {
                Logger.e(LOG_TAG, "no heartbeat url")
            } else entry.heartbeatUrl

            dao.put(Heartbeat408Entry(hostname, activeNetwork, url))
            send()
        } catch (ex: Exception) {
            Logger.e(LOG_TAG, "Exception processing request on client timeout")
            return
        }
    }

    /**
     * To be called by other classes. Here, we are not sure whether it needs scheduling, or which
     * thread this gets called on. So, check db if it needs scheduling, in a bg thread.
     */
    internal fun scheduleDNSOverrideNecessityCheck() {
        CommonUtils.runInBackground {
            if(dao.entriesForNetwork(activeNetwork).isNotEmpty())
                send(resuming = true)
            else {
                Logger.d(LOG_TAG, "db returned 0 408-entries for $activeNetwork")
            }
        }
    }

    /**
     * may be called from any thread. just posts a handler message (without checking the DB).
     */
    private fun send(withDelay: Boolean = false, resuming: Boolean = false) {
        val delayed = withDelay || resuming
        val interval = PreferenceManager.getLong(Constants.HEARTBEAT_INTERVAL, 9_00_000L)
        val delay = when {
            delayed.not() -> { // received 408.
                Logger.d(LOG_TAG, "send: running immediately.")
                handler.removeMessages(MSG)
                // on 408, okhttp will retry once. adding small delay two prevent posting 2 times
                // within a gap of few msec.
                handler.sendEmptyMessageDelayed(MSG, 500)
                -1
            }
            handler.hasMessages(MSG) -> { // this is delayed; if already scheduled, ignore.
                Logger.d(LOG_TAG, "send: already running. $withDelay, $resuming")
                -1
            }
            resuming -> {
                val curTime = SystemClock.elapsedRealtime()
                // if time has passsed, will be -ve; schedule immediately(coerce to 0)
                val nextSchedule = (PreferenceManager.getLong(PREF_HEARTBEAT_NEXT_SCHEDULE, Long
                        .MAX_VALUE) - curTime).coerceIn(0, interval)
                min(nextSchedule, interval)
            }
            withDelay -> interval
            else -> {
                Logger.e(LOG_TAG, "send: IllegalState: $withDelay, $resuming, $interval")
                -1
            }
        }
        // if < 0, we don't want delayed scheduling.
        if(delay>=0){
            Logger.d(LOG_TAG, "posting with $delay. $withDelay, $resuming")
            PreferenceManager.saveLong(PREF_HEARTBEAT_NEXT_SCHEDULE, SystemClock.elapsedRealtime() + delay)
            handler.sendEmptyMessageDelayed(MSG, delay)
        }
    }

    companion object {
        private const val MSG = 42
        private const val PREF_HEARTBEAT_NEXT_SCHEDULE = "PREF_HEARTBEAT_NEXT_SCHEDULE"
    }
}


/**
 * Retrofit API for checking network connection
 * @author karthik.r
 */
private interface HeartbeatApi {
    /**
     * we donot know the response format (and we don't need it.). Only response code matters.
     */
    @GET()
    fun check(@Url url: String): Call<Void>
}

private const val LOG_TAG = "Http408Dns"