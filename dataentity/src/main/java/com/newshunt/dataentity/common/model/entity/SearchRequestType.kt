/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity

/**
 * Search request types
 *
 * @author manoj.gupta
 *
 */

enum class SearchRequestType(val type: String) {
    NEWS("news"),
    GROUP("group"),
    CREATE_POST("create_post"),
    LOCATION("location") /*used by client-only*/
}