/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bwutil.entity.BwBitrates
import com.bwutil.entity.BwHourlyMean

/**
 * Used for capturing bitrate estimates from exo-player
 * @author satosh.dhanyamraju
 */
@Database(entities = [BwBitrates::class],views = [BwHourlyMean::class], version = 1)
abstract class BwDb : RoomDatabase() {
    abstract fun bwDao(): BwMeasurementDao
}