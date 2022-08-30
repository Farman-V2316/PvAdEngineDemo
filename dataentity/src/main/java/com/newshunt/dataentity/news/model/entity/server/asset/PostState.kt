/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity.server.asset

/**
 * Created by karthik.r on 17/06/20.
 */
enum class PostState {
    PUBLISHED,
    TEMPORARILY_TAKEN_DOWN,
    PERMANENTLY_TAKEN_DOWN,
    EXPIRED,
    SENSITIVE,
    UNDER_REVIEW;
}