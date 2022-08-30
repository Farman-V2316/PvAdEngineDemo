package com.newshunt.analytics.entity

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

enum class NhAnalyticsSearchEventParam(val type:String): NhAnalyticsEventParam {

    SEARCH_ID("search_id"),
    QUERY("query"),
    RESULT_ITEM_COUNT("result_item_count"),
    TYPE("type"),
    CT("ct"), // search context
    CI("ci"),// search id
    HAS_USER_TYPED("has_user_typed"),
    REQUEST_ID("request_id"); // unique request id that is passed to search apis. connects
    // presearch and search events.

    override fun getName(): String {
        return type
    }

}