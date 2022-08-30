/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.newshunt.dataentity.common.asset.Format

/**
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "fetch_data",
        primaryKeys = ["fetchId", "pageNum", "indexInPage", "reqUrl"],
        foreignKeys = [
            ForeignKey(entity = FetchInfoEntity::class,
                    parentColumns = [FetchInfoEntity.COL_FETCHINFO_ID],
                    childColumns = ["fetchId"],
                    onDelete = ForeignKey.CASCADE)
        ])
data class FetchDataEntity(
        val fetchId: Long,
        val pageNum: Int,
        val indexInPage: Int,
        val storyId: String,
        val format: Format, // todo required?
        val reqUrl: String, //Can be used as a filter for this response. Readusecase to filter based on this filter
        val receivedTs: Long = System.currentTimeMillis()
)