/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.DatabaseView
import java.io.Serializable

/**
 *
 * [id] is expected to be unique across all the tables contributing to this view.
 *  @author satosh.dhanyamraju
 */
@DatabaseView(value = Q_FeedPage, viewName = VIEW_FeedPage)
data class FeedPage(
        val id: String,
        val contentUrl: String,
        val contentRequestMethod: String,
        val section: String = "",
        val entityType: String = ""
) : Serializable

const val Q_FeedPage = """SELECT id,
           contentUrl,
           contentRequestMethod,
           section,
           entityType
    FROM pages
    UNION ALL
    SELECT id,
           contentUrl,
           contentRequestMethod,
           section,
           entityType
    FROM entityInfo
    UNION ALL
    SELECT id,
           contentUrl,
           contentRequestMethod,
           section,
           entityType
    FROM search_feed
    UNION ALL
    SELECT id,
           contentUrl,
           contentRequestMethod,
           section,
           '' entityType
    FROM $TABLE_generalFeed
"""