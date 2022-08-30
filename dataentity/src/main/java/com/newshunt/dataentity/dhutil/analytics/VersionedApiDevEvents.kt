/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.analytics

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

/**
 * @author satosh.dhanymaraju
 */


class VerApiDevEvent(val evtType: String,
                     val params: Map<NhAnalyticsEventParam, Any>) : NhAnalyticsEvent {
    override fun isPageViewEvent() = false
    override fun toString() = evtType
    fun printString() = "$evtType#$params"
}

enum class EvtType {
    DEV_VER_RESP,
    DEV_VER_RETRY_RESP
}

enum class EvtParam : NhAnalyticsEventParam {
    UNIQUE_ID,
    URL,
    RESP_CODE,
    RESULT,
    SIZE,
    SERV_VERSION;

    override fun getName() = name.toLowerCase()
}

