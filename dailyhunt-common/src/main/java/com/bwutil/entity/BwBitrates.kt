/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.entity

import androidx.room.Entity

/**
 * Room entity. Insert only through BwMeasurmentDao as BwEstRepo has memory optimizations that
 * depend on it
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "bw_bitrates", primaryKeys = ["ts"])
data class BwBitrates(
        val ts: Long,
        val day: Int,
        val hour: Int,
        val networkName: String, // wifi, mobile etc
        val bitrate: Long
)
