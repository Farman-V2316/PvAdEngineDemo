/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics

import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent

/**
 * Created by helly.patel on 23/7/19.
 */

enum class NhAnalyticsSettingsEvent constructor(private val isPageViewEvent: Boolean) : NhAnalyticsEvent {

    APP_LANGUAGE_ITEM_CLICKED(false),

    NIGHT_MODE_TOGGLED(false),
    NOTIFICATION_PERMISSION_TOGGLED(false),

    FEEDBACK_CLICK(false),
    FEEDBACK_SCREEN_VIEW(false),
    FEEDBACK_SUBMIT(false),

    SHARE_APP_SELECT(false),
    SHARE_CLICK(false),

    SETTINGS_UPGRADE_SELECT(false),
    UPGRADE_DIALOGBOX_VIEW(false),
    UPGRADE_DIALOGBOX_CHOICE(false),

    FAQ_CLICK(false),
    RATE_APP(false),
    ABOUT_US(false),
    SAVE_ARTICLES_CLICKED(false),
    ADVERTISING_CLICK(false),

    SETTINGS_CLICKED(false),
    MY_FAVORITES_CLICKED(false),
    HELP_CLICKED(false),
    CARD_TOGGLED(false),
    NOTIFICATION_TOGGLED(false);

    override fun isPageViewEvent(): Boolean {
        return isPageViewEvent
    }
}
