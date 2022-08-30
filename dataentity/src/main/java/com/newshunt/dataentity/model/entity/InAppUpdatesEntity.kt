/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

/**
 * In app update prompt entity
 *
 * Created by srikanth.ramaswamy on 03/01/2021.
 */
const val TABLE_IN_APP_UPDATES = "in_app_updates"
const val COL_AVAILABLE_VERSION = "available_version"
const val COL_PROMPT_SHOWN_COUNT = "prompt_shown_count"
const val COL_LAST_PROMPT_TIMESTAMP = "last_prompt_ts"

@Entity(tableName = TABLE_IN_APP_UPDATES, primaryKeys = [COL_AVAILABLE_VERSION])
data class InAppUpdatesEntity(@ColumnInfo(name = COL_AVAILABLE_VERSION) val availableVersion: Int,
                              @ColumnInfo(name = COL_PROMPT_SHOWN_COUNT) val promptShownCount: Int = 0,
                              @ColumnInfo(name = COL_LAST_PROMPT_TIMESTAMP) val lastPromptTs: Long): Serializable
