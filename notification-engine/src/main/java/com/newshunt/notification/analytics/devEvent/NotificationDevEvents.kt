package com.newshunt.notification.analytics.devEvent

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Logger

/**
 * @author santhosh.kc
 */

const val LOG_TAG = "NotificationDevEvent"

class NotificationDevEvent(val evtType : String,
                           val params: Map<NhAnalyticsEventParam, Any?>) : NhAnalyticsEvent {
    override fun isPageViewEvent(): Boolean = false
    override fun toString() = evtType
    fun printString() = "$evtType#$params"
}

enum class NotificationDevEventType {
    META_RESPONSE_MISMATCH
}

enum class NotificationDevEventParam : NhAnalyticsEventParam {
    META_RESPONSE_ID,
    META_RESPONSE_TYPE,
    OPT_IN_ID,
    OPT_IN_TYPE;

    override fun getName() = name.toLowerCase()
}

fun fireNotificationDevEvent(notificationDevEvent: NotificationDevEvent) {
    Logger.d(LOG_TAG, "onNotificationDevEvent: " + notificationDevEvent.printString())
    if (!AndroidUtils.devEventsEnabled()) {
        return
    }
    AnalyticsClient.log(notificationDevEvent, NhAnalyticsEventSection.APP, notificationDevEvent.params)
}