/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/

package com.newshunt.dataentity.social.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "immersive_ad_rule_stats",
        indices = [Index(value = ["ad_id"] , unique = true)])
data class ImmersiveAdRuleEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name= "played_in_immersive") val playedInImmersive: Boolean = false,
        @ColumnInfo(name= "ad_distance")val adDistance: Int = -1,
        @ColumnInfo(name = "ad_id")val adId: String? = "",
        @ColumnInfo(name = "ad_position") val adPosition:Int = -1,
        val entryTs: Long = System.currentTimeMillis()
): Serializable



