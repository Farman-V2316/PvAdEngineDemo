/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.analytics

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author shrikant.agrawal
 */

@Entity(tableName = "session-info")
data class SessionInfo(@PrimaryKey val id: String,
                       val startTime: Long = 0L,
                       val endTime: Long = 0L)