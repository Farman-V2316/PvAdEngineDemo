/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.server.asset;

import com.newshunt.dataentity.common.model.entity.DNSEntry;
import com.newshunt.dataentity.common.model.entity.DNSEntry;

import java.io.Serializable;
import java.util.List;

/**
 * Holds DNS Configuration values
 *
 * @author karthik.r
 */
public class DNSConfig implements Serializable {

    private static final long serialVersionUID = 0L;
    private String version;
    private boolean disableDNSCaching;
    private long firstLevelCacheTTL;
    private long secondLevelCacheTTL;
    private long dnsLookupTimeout;
    private long bgDnsLookupTimeout;
    private long scheduleHeartbeatInterval = 9_00_000L; // 15m; How often to check heartbeat? value is in millsec.
    private List<DNSEntry> dnsEntries;
    private List<String> dnsServers = null;
    private long tpDnsLookupTimeout;


    public boolean isDisableDNSCaching() {
        return disableDNSCaching;
    }

    public void setDisableDNSCaching(boolean disableDNSCaching) {
        this.disableDNSCaching = disableDNSCaching;
    }

    public long getFirstLevelCacheTTL() {
        return firstLevelCacheTTL;
    }

    public void setFirstLevelCacheTTL(long firstLevelCacheTTL) {
        this.firstLevelCacheTTL = firstLevelCacheTTL;
    }

    public long getSecondLevelCacheTTL() {
        return secondLevelCacheTTL;
    }

    public void setSecondLevelCacheTTL(long secondLevelCacheTTL) {
        this.secondLevelCacheTTL = secondLevelCacheTTL;
    }

    public long getDnsLookupTimeout() {
        return dnsLookupTimeout;
    }

    public void setDnsLookupTimeout(long dnsLookupTimeout) {
        this.dnsLookupTimeout = dnsLookupTimeout;
    }

    public List<DNSEntry> getDnsEntries() {
        return dnsEntries;
    }

    public void setDnsEntries(List<DNSEntry> dnsEntries) {
        this.dnsEntries = dnsEntries;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(getClass())
                .append(" [version=").append(version).append("]")
                .append(" [toString=]").append(super.toString()).toString();
    }

    public long getScheduleHeartbeatInterval() {
        return scheduleHeartbeatInterval;
    }

    public long getBgDnsLookupTimeout() {
        return bgDnsLookupTimeout;
    }

    public List<String> getDnsServers() {
        return dnsServers;
    }

    public long getTpDnsLookupTimeout() {
        return tpDnsLookupTimeout;
    }
}
