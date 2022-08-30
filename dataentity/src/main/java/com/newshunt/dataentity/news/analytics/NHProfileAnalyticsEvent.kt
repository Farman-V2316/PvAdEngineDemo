/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.analytics

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

/**
 * @author priya.gupta
 */
enum class NHProfileAnalyticsEvent : NhAnalyticsEvent {

    PROFILE_VIEW,
    PROFILE_EDIT,
    DIALOGBOX_VIEWED,
    DIALOGBOX_ACTION;

    override fun isPageViewEvent(): Boolean {
        return false
    }
}

enum class NHProfileAnalyticsEventParam(val paramName: String) : NhAnalyticsEventParam {


    USER_ID("user_id"),
    PROFILE_VIEW_TYPE("person_view"),

    FULLNAME_OLD("fullname_old"),
    FULLNAME_NEW("fullname_new"),

    USERNAME_OLD("username_old"),
    USERNAME_NEW("username_new"),

    GENDER_OLD("gender_old"),
    GENDER_NEW("gender_new"),

    PRIVACY_OLD("privacy_old"),
    PRIVACY_NEW("privacy_new"),

    LOCATION_OLD("location_old"),
    LOCATION_NEW("location_new"),

    MOBILE_NUMBER_OLD("mobile_number_old"),
    MOBILE_NUMBER_NEW("mobile_number_new"),

    EMAIL_ID_OLD("email_id_old"),
    EMAIL_ID_NEW("email_id_new"),

    TAGGING_OLD("tagging_old"),
    TAGGING_NEW("tagging_new"),

    INVITE_OLD("invite_old"),
    INVITE_NEW("invite_new"),

    TPV_STATE("TPV_state"),

    //Used to send selected filter for notification inbox view
    FILTER("filter"),
    TARGET_USER_ID("target_user_id"),
    TARGET_USER_TYPE("target_user_type"),
    ACTIVITY_FILTER_TYPE("activity_filtertype"),
    POST_FILTER_TYPE("post_filtertype"),
    TARGET_COMMUNITY_ID("target_community_id"),
    NOTIF_CLICK("notif_click");


    override fun getName(): String {
        return paramName
    }
}
