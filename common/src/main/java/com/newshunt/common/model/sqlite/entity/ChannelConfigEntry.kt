/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.model.sqlite.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * @author Amitkumar
 */
@Entity(tableName = "channel_entry", primaryKeys = ["id"])
data class ChannelConfigEntry(@ColumnInfo(name = "id") val id: String,
                              @ColumnInfo(name = "priority") val imp: Int,
                              @ColumnInfo(name = "isGroup") val isGroup: Boolean)
