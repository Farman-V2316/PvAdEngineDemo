/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity
import com.newshunt.dataentity.common.asset.BaseDetailList
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntityLevel

/**
 * @author karthik.r
 */
@Entity(tableName = "photochild",
        primaryKeys = ["postId", "id"])
data class PhotoChild(
        var postId: String,
        val id: String,
        val shareUrl: String?,
        val createdTime: Long?,
        val imgUrl: String?,
        val slowImageUrl: String?,
        val hasThumbnail: Boolean?,
        val description: String?,
        var viewOrder: Int?
) : BaseDetailList {
    override fun i_id(): String {
        return id
    }

    override fun i_video_assetId(): String? {
        return null
    }

    override fun i_mm_includeCollectionInSwipe(): Boolean? {
        return null
    }

    override fun i_format(): Format? {
        return Format.IMAGE
    }

    override fun i_level(): PostEntityLevel? {
        return null
    }
}

