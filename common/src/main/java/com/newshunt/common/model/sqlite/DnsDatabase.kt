/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.sqlite

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.model.sqlite.dao.DnsDao
import com.newshunt.common.model.sqlite.entity.Heartbeat408Entry

/**
 * DB to manage  host/network that caused http 408
 * @author satosh.dhanyamraju
 */
@Database(entities = [Heartbeat408Entry::class], version = 1)
internal abstract class DnsDatabase : RoomDatabase() {
    abstract fun dao(): DnsDao
}

internal val DNS_DB by lazy {
    Room.databaseBuilder(CommonUtils.getApplication(), DnsDatabase::class.java, "dns.db").build()
}