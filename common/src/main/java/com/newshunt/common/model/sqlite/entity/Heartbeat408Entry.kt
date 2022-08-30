/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.sqlite.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity class
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "heartbeat_entry", primaryKeys = ["host", "network"])
internal data class Heartbeat408Entry(@ColumnInfo(name = "host") val host: String,
                             @ColumnInfo(name = "network") val network: String,
                             @ColumnInfo(name = "heartbeatUrl") val heartbeatUrl: String)