/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "fetch_info",
        indices = [Index(value = ["col_entity_id", "col_disp_loc", "section"], unique = true)]
        // todo newspages is getting deleted, so constraint will fail
//        foreignKeys = [ForeignKey(entity = S_PageEntity::class, parentColumns = ["id"], childColumns = ["col_entity_id"])]
)
data class FetchInfoEntity(
        @ColumnInfo(name = COL_ENTITY_ID) val entityId: String, // might be removed later.
        @ColumnInfo(name = COL_DISP_LOC) val location: String,
        val nextPageUrl: String? = null,
        val currentPageNum: Int = 0,// 0-success, 1-inprog, 2-error, 3-idle
        val npUrlOf1stResponse: String? = null,
        val lastViewDestroyTs: Long? = null,
        @ColumnInfo(name = COL_FETCHINFO_ID) @PrimaryKey(autoGenerate = true) val fetchInfoId: Long = 0L,
        val section: String

) {
    companion object {
        const val COL_ENTITY_ID = "col_entity_id"
        const val COL_DISP_LOC = "col_disp_loc"
        const val COL_FETCHINFO_ID = "col_fetchInfoId"
    }
}