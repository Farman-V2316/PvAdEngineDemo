package com.newshunt.dataentity.common.asset

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import java.io.Serializable

@Entity(tableName = "article_time_spent_track")
data class ArticleTimeSpentTrackEntity(
        @PrimaryKey
        val itemId: String = Constants.EMPTY_STRING,
        val chunkwiseTs: String? = null,
        val format: String? = null,
        val subFormat: String? = null,
        val engagementParams: String? = null,
        val timestamp: Long = 0L,
        val totalTimeSpent: Long = 0L,
        var referrer: String = ""
) : Serializable