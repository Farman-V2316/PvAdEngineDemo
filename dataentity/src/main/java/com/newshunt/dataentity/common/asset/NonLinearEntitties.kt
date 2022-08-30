package com.newshunt.dataentity.common.asset

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @author shrikant.agrawal
 */
@Entity(tableName = "nlfc")
data class NLFCItem(
	@PrimaryKey val postId: String,
	val parentPostId: String,
	val isInserted: Boolean = false,
	val isConsumed: Boolean = false,
	val format: Format = Format.HTML) : Serializable