/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.retrofit

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.DNSEntry
import java.net.InetAddress
import okhttp3.Dns

/**
 *
 * Does not do Dns lookup; takes a map of hostname to IP from BE.
 *
 * For a host, if IP available and valid, it will used
 *
 * @author satosh.dhanyamraju
 */
internal class PrePopulatedDns(var dnsValuesFromServer: Map<String, DNSEntry>) : Dns {
    private val LOG_TAG: String = "PrePopulatedDns"
    fun needsToHandle(hostname: String): Boolean {
        val dnsEntry = dnsValuesFromServer[hostname]
        Logger.d(LOG_TAG, "needsToHandle: ra_enabled=${dnsEntry?.isRaEnabled}")
        return dnsEntry?.ip?.isNotEmpty() == true && !dnsEntry.isRaEnabled
    }

    override fun lookup(hostname: String): MutableList<InetAddress> {
        return dnsValuesFromServer[hostname]?.toInetList()?.toMutableList() ?: mutableListOf()
    }
}