/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.model.sqlite.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.common.model.sqlite.entity.ChannelConfigEntry

/**
 * @author Amitkumar
 */
@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel_entry where 1")
    fun fetchAllChannels(): List<ChannelConfigEntry>

    @Delete
    fun deleteChannel(channelsConfigEntries: List<ChannelConfigEntry>): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateChannel(channelsConfigEntries: List<ChannelConfigEntry>): List<Long>

}