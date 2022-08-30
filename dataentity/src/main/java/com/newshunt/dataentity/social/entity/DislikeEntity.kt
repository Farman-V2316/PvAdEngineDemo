/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import org.jetbrains.annotations.NotNull
import java.io.Serializable

/**
 * @author amit.chaudhary
 */
@Entity(tableName = "dislikes", primaryKeys = ["postId"])
data class DislikeEntity(
        @NotNull
        val postId: String = Constants.EMPTY_STRING,
        val format: Format? = null,
        val subFormat: SubFormat? = null,
        val createdAt: Long? = null,
        val sourceId: String? = null,
        val sourceEntityType: String? = null,
        val sourceSubType: String? = null,
        val options: List<String>? = null,
        val optionsL2: List<String>? = null,
        val eventParam: String? = null, // Not required for reco payload
        val markedForPayload: Boolean = true
) : Serializable