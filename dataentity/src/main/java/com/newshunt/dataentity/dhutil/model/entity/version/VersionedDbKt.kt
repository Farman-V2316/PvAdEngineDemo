/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.version

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newshunt.common.helper.common.Constants
import java.io.Serializable

/**
 * @author shrikant.agrawal
 * Entity class for the version db
 */
@Entity(tableName = "version_data", indices = arrayOf(Index("entity_type","parent_type","parent_id", unique = true)))
data class VersionDbEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "parent_type") val parentType: String = Constants.EMPTY_STRING,
    @ColumnInfo(name = "parent_id") val parentId: String = Constants.EMPTY_STRING,
    @ColumnInfo(name = "version") var version:String,
    @ColumnInfo(name = "language_code") val langCode:String = Constants.EMPTY_STRING,
    @ColumnInfo(name = "last_updated") val lastUpdated:Long = System.currentTimeMillis(),
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB) val data: ByteArray) :Serializable
