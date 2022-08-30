/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

/**
 * Created by helly.patel on 30/7/19.
 */

enum class NhAnalyticsSettingsEventParam constructor(private val mName: String) :
        NhAnalyticsEventParam {

    FEEDBACK_ACTION("feedback_action"),
    FEEDBACK_EMPTY_FIELDS("empty_fields"),
    FEEDBACK_INVALID_FIELDS("invalid_fields"),

    SHARE_APP_TYPE("share_type"),
    SHARE_UI("share_ui"),

    UPGRADE_CHOICE("upgrade_choice"),
    UPGRADE_TEST_RESULT("upgrade_test_result"),

    NIGHT_MODE_OLD("mode_old"),
    NIGHT_MODE_NEW("mode_new"),

    NOTIFICATION_PREV_STATE("previous_state"),
    NOTIFICATION_NEW_STATE("new_state"),

    NEW_STATE("new_state"),
    PREVIOUS_STATE("previous_state"),
    OPTION_SELECTED("option_selected"),
    ACTION("action"),
    TYPE("type");

    override fun getName(): String {
        return mName
    }

}
