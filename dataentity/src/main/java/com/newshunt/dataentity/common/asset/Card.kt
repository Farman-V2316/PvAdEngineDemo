/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dataentity.social.entity.TABLE_CARD

/**
 *
 * @author satosh.dhanyamraju
 */
@Entity(tableName = TABLE_CARD, primaryKeys = ["uniqueId", "level"])
data class Card(
        val uniqueId: String = Constants.EMPTY_STRING,
        val id: String,
        val src_id: String?,
        val src_entityType: String?,
        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        val source: PostSourceAsset?,
        val level: PostEntityLevel,
        val moreStoryCount: Int?,
        val video_assetId: String?,
        val format: Format,
        val mm_includeCollectionInSwipe: Boolean?,
        val thumbnailInfos: List<ImageDetail>?,
        val contentImageInfo: ImageDetail?,
        val shareUrl: String?,
        val title: String?,
        val langCode: String?,
        @Embedded(prefix = "local_")
        val localInfo: LocalInfo?,
        var subFormat: SubFormat = SubFormat.STORY,
        var adId: String? = null,
        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        val postEntity: PostEntity,
        val ignoreSourceBlock: Boolean = false) :  CommonAsset by postEntity {
    fun withNewCounts(counts2: Counts2?): Card {
        val counts3 = counts2?.copy(
                COMMENTS = maxOf(i_counts()?.COMMENTS, counts2.COMMENTS),
                REPOST = maxOf(i_counts()?.REPOST, counts2.REPOST),
                SHARE = maxOf(i_counts()?.SHARE, counts2.SHARE),
                VIEWS = maxOf(i_counts()?.VIEWS, counts2.VIEWS),
                TOTAL_LIKE = maxOf(i_counts()?.TOTAL_LIKE, counts2.TOTAL_LIKE))
        val newpe = postEntity.copy(counts = counts3)
        return copy(postEntity = newpe)
    }

    fun withPoll(pollAsset: PollAsset?): Card {
        return with(postEntity) {
            poll = pollAsset
            toCard2(useUniqueId = uniqueId)
        }
    }

    fun withShownInForyou(): Card {
        return with(postEntity) {
            localInfo = (localInfo?:LocalInfo(creationDate = System.currentTimeMillis())).copy( shownInForyou = true )
            toCard2(useUniqueId = uniqueId)
        }
    }

    fun withLocal(progress: Int,
                  state: PostUploadStatus): Card {
        return with(postEntity) {
            localInfo = (localInfo?:LocalInfo(creationDate = System.currentTimeMillis())).copy(progress, state.name)
            toCard2(useUniqueId = uniqueId)
        }
    }

    fun withLevel(level1: PostEntityLevel): Card {
        return with(postEntity) {
            level = level1
            toCard2(useUniqueId = uniqueId)
        }
    }

    override fun copyWith(isFollowin: Boolean?, selectedLikeType: String?, pollSelectedOptionId:
    String?, isRead: Boolean?, coldStartItems: List<ColdStartEntityItem>?, collectionItems:
                          List<PostEntity>?): Card {
        return this.copy(postEntity = postEntity.copyWith(isFollowin, selectedLikeType,
                pollSelectedOptionId, isRead, coldStartItems, collectionItems))
    }

    override fun i_localInfo(): LocalInfo? {
        return localInfo
    }

    override fun i_level(): PostEntityLevel {
        return level
    }

    override fun i_adId(): String? {
        return adId
    }

    companion object{
        fun maxOf(e1: EntityConfig2?, e2: EntityConfig2?): EntityConfig2? {
            val e1ts = e1?.ts ?: run {
                return e2
            }
            val e2ts = e2?.ts ?: return e1
            return if(e1ts > e2ts) e1 else e2
        }
    }
}