package com.newshunt.dataentity.common.asset

import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "cookie_table", primaryKeys = ["location"])
data class CookieEntity(
        val location: String,
        val cookie: String
) : Serializable