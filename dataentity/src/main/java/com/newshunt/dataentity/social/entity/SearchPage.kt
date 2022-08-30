/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newshunt.dataentity.common.pages.PageSection

/**
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "search_feed")
data class SearchPage(
        @PrimaryKey @ColumnInfo(name = COL_ID) val id: String/* these ids will be numeric and hence will not overlap with news-home and other pages */ = "",
        val contentUrl: String = "",
        val contentRequestMethod: String = "GET",
        val section: String = PageSection.SEARCH.section,
        val name: String = "",
        val entityType: String = "",
        val entityLayout: String = "",
        val viewOrder: Int = 0
) {
    companion object {
        const val COL_ID = "id"
    }
}