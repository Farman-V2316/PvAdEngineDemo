/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.model.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.asset.VideoAsset
import com.newshunt.dataentity.common.asset.ViralAsset
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import java.util.Date

/**
 * Entities related to Profile are to be defined in this file
 * <p>
 * Created by srikanth.ramaswamy on 11/08/2019.
 */

/**
 * -------------------------------------------------------------------------------------------------
 * Table to maintain deleted assets like userInteraction
 * -------------------------------------------------------------------------------------------------
 */
const val TABLE_DELETED_INTERACTIONS = "deleted_interactions"

const val COL_ID = "id"
const val COL_SYNC_STATUS = "sync_status"

@Entity(tableName = TABLE_DELETED_INTERACTIONS, primaryKeys = [COL_ID])
data class DeletedInteractionsEntity(@ColumnInfo(name = COL_ID) val id: String,
                                     @ColumnInfo(name = COL_SYNC_STATUS) val syncStatus: SyncStatus = SyncStatus.UN_SYNCED)

/**
 * -------------------------------------------------------------------------------------------------
 * Table to maintain sync of Bookmarked items with B.E
 * -------------------------------------------------------------------------------------------------
 */
const val TABLE_BOOKMARKS = "bookmarks"
const val COL_FORMAT = "format"
const val COL_SUB_FORMAT = "sub_format"
const val COL_ACTION = "action"
const val COL_TS = "timestamp"


@Entity(tableName = TABLE_BOOKMARKS, primaryKeys = [COL_ID])
data class BookmarkEntity(@ColumnInfo(name = COL_ID) val id: String,
                          @ColumnInfo(name = COL_FORMAT) val format: String? = null,
                          @ColumnInfo(name = COL_SUB_FORMAT) val subFormat: String? = null,
                          @ColumnInfo(name = COL_ACTION) val action: BookMarkAction = BookMarkAction.ADD,
                          @ColumnInfo(name = COL_TS) val timestamp: Long,
                          @ColumnInfo(name = COL_SYNC_STATUS) val syncStatus: SyncStatus = SyncStatus.SYNCED)

/**
 * -------------------------------------------------------------------------------------------------
 * Table to maintain history of the user
 * -------------------------------------------------------------------------------------------------
 */
const val TABLE_HISTORY = "history"
const val COL_UI_TYPE = "ui_type"
const val COL_IMAGE_URL = "img_url"
const val COL_TITLE = "title"
const val COL_DURATION = "duration"
const val COL_TIMESTAMP = "timestamp"
const val COL_SOURCE_IMG_URL = "src_logo"
const val COL_SOURCE_NAME = "src_name"
const val COL_IS_DELETED = "is_deleted"
const val COL_MARK_DELETED = "is_marked_deleted"
const val COL_IS_NSFW = "is_nsfw"
const val COL_CONTENT = "content"
const val COL_HIDE_CONTROL = "hide_control"

@Entity(tableName = TABLE_HISTORY, primaryKeys = [COL_ID])
data class HistoryEntity(@ColumnInfo(name = COL_ID) val id: String,
                         @ColumnInfo(name = COL_FORMAT) val format: Format,
                         @ColumnInfo(name = COL_SUB_FORMAT) val subFormat: SubFormat?,
                         @ColumnInfo(name = COL_UI_TYPE) val uiType: UiType2?,
                         @ColumnInfo(name = COL_IMAGE_URL) val imgUrl: ImageDetail?,
                         @ColumnInfo(name = COL_TITLE) val title: String?,
                         @ColumnInfo(name = COL_CONTENT) val content: String?,
                         @ColumnInfo(name = COL_DURATION) val duration: String?,
                         @ColumnInfo(name = COL_TIMESTAMP) val timestamp: Date,
                         @ColumnInfo(name = COL_SOURCE_IMG_URL) val srcLogo: String?,
                         @ColumnInfo(name = COL_SOURCE_NAME) val srcName: String?,
                         @ColumnInfo(name = COL_IS_DELETED) val isDeleted: Boolean = false,
                         @ColumnInfo(name = COL_MARK_DELETED) val isMarkedDeleted: Boolean = false,
                         @ColumnInfo(name = COL_IS_NSFW) val isNsfw: Boolean = false,
                         @ColumnInfo(name = COL_HIDE_CONTROL) val hideControl: Boolean? = false): CommonAsset {

    override fun i_id(): String = id

    override fun i_type(): String = Constants.EMPTY_STRING

    override fun i_format(): Format = format

    override fun i_subFormat() = subFormat

    override fun i_uiType() = uiType

    override fun i_thumbnailUrls(): List<String>? {
        return imgUrl?.url?.let {
            listOf(it)
        }
    }

    override fun i_thumbnailUrlDetails(): List<ImageDetail>? {
        return imgUrl?.let {
            listOf(it)
        }
    }

    override fun i_title() = title

    override fun i_source(): PostSourceAsset? {
        return PostSourceAsset(imageUrl = srcLogo, displayName = srcName)
    }

    override fun i_publishTime(): Long? = timestamp.time

    override fun i_videoAsset(): VideoAsset? {
        return if (!duration.isNullOrEmpty() || format == Format.VIDEO || (format == Format.HTML && subFormat == SubFormat.S_W_VIDEO)) {
            VideoAsset(id, duration = duration, hideControl = hideControl ?: false)
        } else null
    }

    override fun i_viral(): ViralAsset? {
        return if (isNsfw) {
            ViralAsset(nsfw = true)
        } else {
            null
        }
    }

    override fun i_content() = content

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryEntity

        if (id != other.id) return false
        if (format != other.format) return false
        if (subFormat != other.subFormat) return false
        if (uiType != other.uiType) return false
        if (imgUrl != other.imgUrl) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (duration != other.duration) return false
        if (timestamp != other.timestamp) return false
        if (srcLogo != other.srcLogo) return false
        if (srcName != other.srcName) return false
        if (isDeleted != other.isDeleted) return false
        if (isMarkedDeleted != other.isMarkedDeleted) return false
        if (isNsfw != other.isNsfw) return false
        if (hideControl != other.hideControl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + (subFormat?.hashCode() ?: 0)
        result = 31 * result + (uiType?.hashCode() ?: 0)
        result = 31 * result + (imgUrl?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (srcLogo?.hashCode() ?: 0)
        result = 31 * result + (srcName?.hashCode() ?: 0)
        result = 31 * result + isDeleted.hashCode()
        result = 31 * result + isMarkedDeleted.hashCode()
        result = 31 * result + isNsfw.hashCode()
        result = 31 * result + (hideControl?.hashCode() ?: 0)
        return result
    }
}

/**
 * -------------------------------------------------------------------------------------------------
 * Table to pending approvals and invitation counts
 * -------------------------------------------------------------------------------------------------
 */
const val APPROVALS_TABLE_NAME = "approvals"
const val COL_USER_ID = "user_id"
@Entity(tableName = APPROVALS_TABLE_NAME, primaryKeys = [COL_USER_ID])
class PendingApprovalsEntity(@ColumnInfo(name = COL_USER_ID) val userId: String,
                             @Embedded(prefix = "count_")
                             val approvalCounts: ApprovalCounts? = null)