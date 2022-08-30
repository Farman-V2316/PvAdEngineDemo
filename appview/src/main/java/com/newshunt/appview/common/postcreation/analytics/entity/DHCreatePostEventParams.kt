package com.newshunt.appview.common.postcreation.analytics.entity

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

enum class DHCreatePostEventParams(val value: String): NhAnalyticsEventParam {
    PARENT_ID("parent_id"),
    ITEM_ID("item_id"),
    COMMENT_ID("comment_id"),
    ACTION_TYPE("action_type"),
    ATTACHMENT_TYPE("attachment_type"),
    PUBLISH_STATUS("publish_status"),
    ERROR_CODE("error_code"),
    ERROR_REASON("error_reason"),
    TARGET_USER_ID("target_user_id");

    override fun getName(): String {
        return value
    }
}