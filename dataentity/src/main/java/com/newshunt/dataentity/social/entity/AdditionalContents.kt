/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import java.io.Serializable

/**
 * @author karthik.r
 */
@Entity(tableName = "additional_contents",
        primaryKeys = ["postId", "id"],
        foreignKeys = [ForeignKey(entity = Card::class, parentColumns = ["uniqueId",PostEntity.COL_LEVEL],
                childColumns = ["postId","level"], onDelete = ForeignKey.CASCADE)])
data class AdditionalContents(
        var postId: String,
        val id: Long,
        val name: String?,
        val title: String?,
        val type: String?,
        val viewType: String?,
        val layoutType: String?,
        val contentType: String?,
        val content: String?,
        val viewOrder: String?,
        val experiment: Map<String, String>?,
        val analyticsDisabled: Boolean,
        val contentRequestMethod: String?,
        var level: PostEntityLevel = PostEntityLevel.TOP_LEVEL) : Serializable