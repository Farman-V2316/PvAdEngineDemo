/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bwutil.entity.BwBitrates


/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class BwMeasurementDao {
    @Insert
    abstract fun insert(bw: BwBitrates): Long

    @Query("SELECT * FROM bw_bitrates")
    abstract fun all(): List<BwBitrates>

    @Query("DELETE FROM bw_bitrates WHERE ts < :ts")
    abstract fun deleteOlder(ts: Long)

    /**
     * Precondition [lt] <= [gt]
     * */
    @Query("SELECT COUNT(*) FROM bw_bitrates WHERE bitrate >= :lt AND bitrate <= :gt AND networkName = :nw")
    abstract fun countBitrateBetween(lt: Long, gt: Long, nw: String): Int

    @Query("SELECT bitrate FROM bw_bitrates WHERE networkName = :nw ORDER by ts DESC limit 1")
    abstract fun latestBitrate(nw: String): Long?

    /**
     * mean on hourly basis
     */
    @Query("SELECT bitrate from view_hourly_mean WHERE networkName = :nw")
    abstract fun lifetimeBitratesAveraged(nw: String) : List<Long>

}