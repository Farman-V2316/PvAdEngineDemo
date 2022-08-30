/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * @author karthik.r
 */
@Entity(tableName = "related_list",
        primaryKeys = ["postId", "id"],
        foreignKeys = [ForeignKey(entity = AdditionalContents::class, parentColumns = ["postId", "id"],
                childColumns = ["postId", "additionalContentsId"], onDelete = ForeignKey.CASCADE)])
data class RelatedList(
        var postId: String,
        val fetchId: Long,
        val indexInPage: Int,
        val additionalContentsId: Long,
        val id: String
)
