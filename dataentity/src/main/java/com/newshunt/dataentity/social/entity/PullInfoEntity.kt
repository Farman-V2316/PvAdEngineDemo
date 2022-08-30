/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores pull info for sending in payload.
 * @author satosh.dhanyamraju
 */
@Entity(tableName = PullInfoEntity.TABLE)
class PullInfoEntity(
        val entityId: String,
        val section : String,
        val timestamp: Long,
        val pageCount: Int,
        @PrimaryKey(autoGenerate = true) val id  : Int = 0
) {
    companion object {
        const val TABLE = "pull_info"
    }
}