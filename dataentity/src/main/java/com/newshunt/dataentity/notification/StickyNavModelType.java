/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dataentity.notification;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Created by anshul on 26/08/17.
 */

public enum StickyNavModelType {

    CRICKET("cricket", "STICKY_CRICKET"),

    GENERIC("generic", "STICKY_GENERIC"),

    NEWS("news", "TYPE_OPEN_NEWSITEM_STICKY"),

    TOPIC("topic", "STICKY_TOPIC");

    private String stickyType;

    private String analyticsStickyType;

    StickyNavModelType(String stickyType, String analyticsStickyType) {
        this.stickyType = stickyType;
        this.analyticsStickyType = analyticsStickyType;
    }

    public String getStickyType() {
        return stickyType;
    }

    public String getAnalyticsStickyType() {
        return analyticsStickyType;
    }

    public static StickyNavModelType from(String navModelTypeString) {
        if (CommonUtils.isEmpty(navModelTypeString)) {
            return GENERIC;
        }

        for (StickyNavModelType navModelType : StickyNavModelType.values()) {
            if (CommonUtils.equalsIgnoreCase(navModelType.stickyType, navModelTypeString)) {
                return navModelType;
            }
        }
        return GENERIC;
    }
}
