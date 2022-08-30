package com.newshunt.appview.common.postcreation.analytics.entity

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent

enum class DHCreatePostEvent(private val isPageViewEvent: Boolean) : NhAnalyticsEvent {
    CREATE_POST_CLICK(false),
    CREATE_POST_HOME(true),
    UI_POST_ACTION(false),
    POST_PUBLISH(false),
    UI_POST_PUBLISH_STATE(false);

    override fun isPageViewEvent(): Boolean {
        return isPageViewEvent
    }
}