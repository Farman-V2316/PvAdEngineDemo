/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.identifier;

import java.util.List;

/**
 * Created by amardeep.kumar on 6/24/2016.
 */
public class UniqueIdentifier {
    private String googleAdId;
    private String wifiMacAddress;
    private List<SimInfo> simInfos;
    private String androidId;
    private String buildSerialNumber;
    private String buildId;

    public String getAdId() {
        return googleAdId;
    }

    public String getWifiMacAddress() {
        return wifiMacAddress;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getBuildSerialNumber() {
        return buildSerialNumber;
    }

    public String getBuildId() {
        return buildId;
    }

    public List<SimInfo> getSimInfos() {
        return simInfos;
    }

    public void setAdId(String adId) {
        this.googleAdId = adId;
    }

    public void setWifiMacAddress(String macAddress) {
        this.wifiMacAddress = macAddress;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public void setBuildSerialNumber(String serialNumber) {
        this.buildSerialNumber = serialNumber;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public void setSimInfos(List<SimInfo> simInfos) {
        this.simInfos = simInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueIdentifier that = (UniqueIdentifier) o;

        if (googleAdId != null ? !googleAdId.equals(that.googleAdId) : that.googleAdId != null)
            return false;
        if (wifiMacAddress != null ? !wifiMacAddress.equals(that.wifiMacAddress) : that.wifiMacAddress != null)
            return false;
        if (simInfos != null ? !simInfos.equals(that.simInfos) : that.simInfos != null)
            return false;
        if (androidId != null ? !androidId.equals(that.androidId) : that.androidId != null)
            return false;
        if (buildSerialNumber != null ? !buildSerialNumber.equals(that.buildSerialNumber) : that.buildSerialNumber != null)
            return false;
        return buildId != null ? buildId.equals(that.buildId) : that.buildId == null;
    }

    @Override
    public int hashCode() {
        int result = googleAdId != null ? googleAdId.hashCode() : 0;
        result = 31 * result + (wifiMacAddress != null ? wifiMacAddress.hashCode() : 0);
        result = 31 * result + (simInfos != null ? simInfos.hashCode() : 0);
        result = 31 * result + (androidId != null ? androidId.hashCode() : 0);
        result = 31 * result + (buildSerialNumber != null ? buildSerialNumber.hashCode() : 0);
        result = 31 * result + (buildId != null ? buildId.hashCode() : 0);
        return result;
    }
}
