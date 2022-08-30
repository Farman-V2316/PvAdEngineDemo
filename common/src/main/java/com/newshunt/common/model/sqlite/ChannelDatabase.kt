/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.model.sqlite

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.model.sqlite.dao.ChannelDao
import com.newshunt.common.model.sqlite.entity.ChannelConfigEntry

/**
 * @author Amitkumar
 */
@Database(entities = [ChannelConfigEntry::class], version = 1)
abstract class ChannelDatabase : RoomDatabase() {
    abstract fun dao(): ChannelDao
}

val CHANNEL_DB by lazy {
    Room.databaseBuilder(CommonUtils.getApplication(), ChannelDatabase::class.java, "channel.db").build()
}