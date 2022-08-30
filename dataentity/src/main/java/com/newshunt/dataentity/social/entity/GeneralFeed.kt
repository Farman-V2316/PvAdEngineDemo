/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import java.io.Serializable

/**
 * For (non-tabable) pages, that just have a contentUrl.
 *
 *  Entries in this table will be combined with [S_PageEntity] and other similar entities, which
 * forms the input for fetch tables
 * @author satosh.dhanyamraju
 */
@Entity(tableName = TABLE_generalFeed)
data class GeneralFeed(
        @PrimaryKey @ColumnInfo(name = COL_ID) val id: String,
        val contentUrl: String,
        val contentRequestMethod: String,
        val section: String = Constants.GROUP_SECTION
): Serializable {
    companion object {
        const val COL_ID = "id"
    }
}