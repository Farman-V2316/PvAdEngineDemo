/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.identifier;

/**
 * Created by amardeep.kumar on 6/30/2016.
 */
public class SimInfo {
    private String simSubscriberId;
    private String simSerialNumber;
    private String simImei;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimInfo simInfo = (SimInfo) o;

        if (simSubscriberId != null ? !simSubscriberId.equals(simInfo.simSubscriberId) : simInfo.simSubscriberId != null)
            return false;
        if (simSerialNumber != null ? !simSerialNumber.equals(simInfo.simSerialNumber) : simInfo.simSerialNumber != null)
            return false;
        return simImei != null ? simImei.equals(simInfo.simImei) : simInfo.simImei == null;
    }

    @Override
    public int hashCode() {
        int result = simSubscriberId != null ? simSubscriberId.hashCode() : 0;
        result = 31 * result + (simSerialNumber != null ? simSerialNumber.hashCode() : 0);
        result = 31 * result + (simImei != null ? simImei.hashCode() : 0);
        return result;
    }
}
