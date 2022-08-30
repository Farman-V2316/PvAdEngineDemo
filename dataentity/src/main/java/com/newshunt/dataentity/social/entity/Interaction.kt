/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "interactions", primaryKeys = [Interaction.COL_ENTITY_ID,
    Interaction.COL_ENTITY_TYPE, Interaction.COL_ACTION, Interaction.COL_SHARE_TS])
data class Interaction(
        @ColumnInfo(name = COL_ENTITY_ID) val entityId: String,
        @ColumnInfo(name = COL_ENTITY_TYPE) val entityType: String,
        /*
        SAVE / DELETE,
        SHARE (no need in payload), (one way sync)
        LIKE / DISLIKE / SMILE / SAD / LOVE / ANGRY / WOW / HAPPY,
        REPOST (maybe ; no sync at all. only counts) */
        @ColumnInfo(name = COL_ACTION) val action: String,
        val actionToggle: Boolean = true,
        val isSynced: Boolean = false,
        val ts: Long? = System.currentTimeMillis(),
        @ColumnInfo(name = COL_SHARE_TS) val shareTs: Long = 0
) {
    companion object {
        const val COL_ENTITY_ID = "entity_id"
        const val COL_ENTITY_TYPE = "entity_type"
        const val COL_SHARE_TS = "COL_SHARE_TS"
        const val COL_ACTION = "col_action"
    }
}