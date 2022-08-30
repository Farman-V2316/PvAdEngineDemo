/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */


package com.newshunt.dhutil.helper.preference

import com.newshunt.common.helper.preference.PreferenceType
import com.newshunt.common.helper.preference.SavedPreference

/**
 * Preference to store the state of the events to be fired to AppsFlyer
 * <p>
 * Created by srikanth.ramaswamy on 09/17/2018.
 */
enum class AppsFlyerEventPreference(private val preferenceName: String) : SavedPreference {
    CONTENT_CONSUMED("content_consumed"),
    DAYS_OPENED_WITHIN_THRESHOLD("days_opened"),
    AD_IMPRESSION("ad_impression"),
    VIDEO_AD_IMPRESSION("video_ad_impression"),
    DAY_1_OPENED_CONSUMED("day_1_opened_consumed"),
    SPV_CONSUMED("spv_consumed"),
    APPSFLYER_INIT_FIRED("appsflyer_init_fired"),
    LANGUAGE_SELECTION_CONSUMED("language_selection_consumed"),
    USER_LOGIN_CONSUMED("user_login_consumed"),
    FIRST_CONTENT_VIEW_CONSUMED("first_content_view_consumed"),
    APP_LAUNCH_COUNT_CONSUMED("app_launch_count_consumed"),
    FIRST_LAUNCH_IN_24_TO_48_HOURS_CONSUMED("first_launch_24_to_48_hours_consumed"),
    FIRST_LAUNCH_IN_72_TO_96_HOURS_CONSUMED("first_launch_72_to_96_hours_consumed"),
    EVENT_TIMESPENT_TOP_20_CONSUMED("timespent_top_20_consumed"),
    EVENT_TIMESPENT_TOP_40_CONSUMED("timespent_top_40_consumed"),
    EVENT_TIMESPENT_TOP_60_CONSUMED("timespent_top_60_consumed"),
    EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD_CONSUMED("timespent_top_20_smaller_threshold_consumed"),
    EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD_CONSUMED("timespent_top_40_smaller_threshold_consumed"),
    EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD_CONSUMED("timespent_top_60_smaller_threshold_consumed"),
    EVENT_TIMESPENT_FIRST_SESSION_CONSUMED("timespent_first_session_consumed"),
    EVENT_APP_OPENED_DAY1_CONSUMED("app_opened_day1_consumed"),
    EVENT_APP_OPENED_DAY3_CONSUMED("app_opened_day3_consumed"),
    EVENT_APP_OPENED_DAY7_CONSUMED("app_opened_day7_consumed"),
    EVENT_APP_OPENED_DAY15_CONSUMED("app_opened_day15_consumed"),
    EVENT_APP_OPENED_DAY30_CONSUMED("app_opened_day30_consumed"),
    EVENT_APP_OPENED_AFTER_DAY_3_CONSUMED("app_opened_after_day_3_consumed"),
    EVENT_NOTIFICATION_DELIVERY_CONSUMED("notification_delivery_consumed"),
    EVENT_NOTIFICATION_CLICK_CONSUMED("notification_click_consumed"),
    EVENT_NEW_USER_INSTALL_CONSUMED("event_new_user_install_consumed"),
    EVENT_USER_RE_INSTALL_CONSUMED("event_user_re_install_consumed");


    override fun getPreferenceType(): PreferenceType {
        return PreferenceType.APPSFLYER_EVENTS
    }

    override fun getName(): String {
        return preferenceName
    }
}