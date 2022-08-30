/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import java.io.Serializable

/**
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "votes", primaryKeys = ["userId", "pollId"])
data class Vote(
        val userId: String,
        val pollId: String,
        val optionId: String,
        val ts: Long = System.currentTimeMillis()
) : Serializable