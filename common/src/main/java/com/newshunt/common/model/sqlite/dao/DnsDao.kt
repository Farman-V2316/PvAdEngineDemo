/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.sqlite.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.common.model.sqlite.entity.Heartbeat408Entry

/**
 * Room dao for [Heartbeat408Entry]
 * @author satosh.dhanyamraju
 */
@Dao
internal interface DnsDao {

    @Query("SELECT * from HEARTBEAT_ENTRY where network = :network")
    fun entriesForNetwork(network: String): List<Heartbeat408Entry>

    @Query("SELECT * from HEARTBEAT_ENTRY where network = :network and host = :host")
    fun entryForNetworkAndHost(network: String, host: String): List<Heartbeat408Entry>

    @Delete()
    fun delete(entry: Heartbeat408Entry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(entry: Heartbeat408Entry)
}