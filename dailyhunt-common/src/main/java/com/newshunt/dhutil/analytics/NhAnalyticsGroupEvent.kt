/*
 * Copyright (c) 2019 . All rights reserved.
 */

package com.newshunt.dhutil.analytics

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent

/**
 * Created by helly.patel on 22/11/19.
 */

enum class NhAnalyticsGroupEvent(private val isPageViewEvent: Boolean) : NhAnalyticsEvent {
    UI_CREATE_GROUP(false),
    CREATE_GROUP_SHOWN(false),
    GROUP_SETTING(false),
    INVITE_SCREEN_SHOWN(false),
    GROUP_INVITE(false),
    GROUP_HOME(false);

    override fun isPageViewEvent(): Boolean {
        return isPageViewEvent
    }
}