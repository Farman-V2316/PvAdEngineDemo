/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.ColdStartEntityItem
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntity
import java.io.Serializable

/**
 * Entity + calculated fileds + relationships
 * @author satosh.dhanyamraju
 */

const val Q_All_Level_cards = """SELECT 
    p.uniqueId AS uniqueId,
    p.id AS id,
    p.src_id AS src_id,
    p.src_entityType AS src_entityType,
    p.source AS source,
    p.level AS level,
    p.moreStoryCount AS moreStoryCount,
    p.video_assetId AS video_assetId,
    p.format AS format,
    p.mm_includeCollectionInSwipe AS mm_includeCollectionInSwipe,
    p.thumbnailInfos AS thumbnailInfos,
    p.shareUrl AS shareUrl,
    p.title AS title,
    p.subFormat AS subFormat,
    p.adId AS adId,
    p.postEntity AS postEntity,
    p.local_progress AS local_progress,
    p.local_status AS local_status,
    p.local_pageId AS local_pageId,
    p.local_location AS local_location,
    p.local_section AS local_section,
    p.local_shownInForyou AS local_shownInForyou,
    p.local_creationDate AS local_creationDate,
    p.local_cpId AS local_cpId,
    p.local_nextCardId AS local_nextCardId,
    p.local_fetchedFromServer AS local_fetchedFromServer,
    p.local_isCreatedFromMyPosts AS local_isCreatedFromMyPosts,
    p.local_isCreatedFromOpenGroup AS local_isCreatedFromOpenGroup,
           CASE
               WHEN fl.entityId IS NULL THEN 0
               ELSE (CASE
                         WHEN fl.`action`='FOLLOW' THEN 1
                         ELSE 0
                     END)
           END isFollowin,
           v.optionId pollSelectedOptionId,
           intr.col_action selectedLikeType,
           EXISTS(select id from history where id = p.id) isRead
    FROM ${TABLE_CARD} p
    LEFT JOIN follow fl ON fl.entityId = p.src_id
    AND fl.entityType = p.src_entityType
    LEFT JOIN votes v ON p.id = v.pollId
    LEFT JOIN interactions intr ON p.id = intr.entity_id
    AND intr.actionToggle = 1
    AND intr.col_action IN ('LIKE',
                            'LOVE',
                            'HAPPY',
                            'WOW',
                            'SAD',
                            'ANGRY')
"""

const val Q_All_Level_cards_6 = """SELECT 
    p.uniqueId AS uniqueId,
    p.id AS id,
    p.src_id AS src_id,
    p.src_entityType AS src_entityType,
    p.source AS source,
    p.level AS level,
    p.moreStoryCount AS moreStoryCount,
    p.video_assetId AS video_assetId,
    p.format AS format,
    p.mm_includeCollectionInSwipe AS mm_includeCollectionInSwipe,
    p.thumbnailInfos AS thumbnailInfos,
    p.shareUrl AS shareUrl,
    p.title AS title,
    p.subFormat AS subFormat,
    p.adId AS adId,
    p.postEntity AS postEntity,
    p.local_progress AS local_progress,
    p.local_status AS local_status,
    p.local_pageId AS local_pageId,
    p.local_location AS local_location,
    p.local_section AS local_section,
    p.local_shownInForyou AS local_shownInForyou,
    p.local_creationDate AS local_creationDate,
    p.local_cpId AS local_cpId,
    p.local_nextCardId AS local_nextCardId,
    p.local_fetchedFromServer AS local_fetchedFromServer,
    p.local_isCreatedFromMyPosts AS local_isCreatedFromMyPosts,
    p.local_isCreatedFromOpenGroup AS local_isCreatedFromOpenGroup,
    p.ignoreSourceBlock AS ignoreSourceBlock,
           CASE
               WHEN fl.entityId IS NULL THEN 0
               ELSE (CASE
                         WHEN fl.`action`='FOLLOW' THEN 1
                         ELSE 0
                     END)
           END isFollowin,
           v.optionId pollSelectedOptionId,
           intr.col_action selectedLikeType,
           EXISTS(select id from history where id = p.id) isRead
    FROM ${TABLE_CARD} p
    LEFT JOIN follow fl ON fl.entityId = p.src_id
    AND fl.entityType = p.src_entityType
    LEFT JOIN votes v ON p.id = v.pollId
    LEFT JOIN interactions intr ON p.id = intr.entity_id
    AND intr.actionToggle = 1
    AND intr.col_action IN ('LIKE',
                            'LOVE',
                            'HAPPY',
                            'WOW',
                            'SAD',
                            'ANGRY')
"""
const val Q_All_Level_cards_7 = """SELECT 
    p.uniqueId AS uniqueId,
    p.id AS id,
    p.src_id AS src_id,
    p.src_entityType AS src_entityType,
    p.source AS source,
    p.level AS level,
    p.moreStoryCount AS moreStoryCount,
    p.video_assetId AS video_assetId,
    p.format AS format,
    p.mm_includeCollectionInSwipe AS mm_includeCollectionInSwipe,
    p.thumbnailInfos AS thumbnailInfos,
    p.shareUrl AS shareUrl,
    p.title AS title,
    p.subFormat AS subFormat,
    p.adId AS adId,
    p.postEntity AS postEntity,
    p.local_progress AS local_progress,
    p.local_status AS local_status,
    p.local_pageId AS local_pageId,
    p.local_location AS local_location,
    p.local_section AS local_section,
    p.local_shownInForyou AS local_shownInForyou,
    p.local_creationDate AS local_creationDate,
    p.local_cpId AS local_cpId,
    p.local_nextCardId AS local_nextCardId,
    p.local_fetchedFromServer AS local_fetchedFromServer,
    p.local_isCreatedFromMyPosts AS local_isCreatedFromMyPosts,
    p.local_isCreatedFromOpenGroup AS local_isCreatedFromOpenGroup,
    p.ignoreSourceBlock AS ignoreSourceBlock,
           CASE
               WHEN fl.entityId IS NULL THEN 0
               ELSE (CASE
                         WHEN fl.`action`='FOLLOW' THEN 1
                         ELSE 0
                     END)
           END isFollowin,
             CASE
               WHEN r.entityId IS NULL THEN 0
               ELSE 1
           END isReported,
           v.optionId pollSelectedOptionId,
           intr.col_action selectedLikeType,
           EXISTS(select id from history where id = p.id) isRead
    FROM ${TABLE_CARD} p
    LEFT JOIN follow fl ON fl.entityId = p.src_id
    AND fl.entityType = p.src_entityType
    LEFT JOIN votes v ON p.id = v.pollId
    LEFT JOIN report r ON p.id = r.entityId
    LEFT JOIN interactions intr ON p.id = intr.entity_id
    AND intr.actionToggle = 1
    AND intr.col_action IN ('LIKE',
                            'LOVE',
                            'HAPPY',
                            'WOW',
                            'SAD',
                            'ANGRY')
"""

@DatabaseView(value = Q_All_Level_cards_7, viewName = VIEW_AllLevelCards)
data class AllLevelCards(
        @Embedded
        val postEntity: Card,
        val pollSelectedOptionId: String? = null,
        val isFollowin: Boolean? = false,
        val isReported: Boolean? = false,
        val selectedLikeType: String? = null,
        val isRead: Boolean? = false
) : CommonAsset by postEntity, Serializable {
    override fun i_isFollowin(): Boolean? = isFollowin
    override fun i_isReported(): Boolean? = isReported
    override fun i_selectedLikeType(): String? = selectedLikeType
    override fun i_pollSelectedOptionId(): String? = pollSelectedOptionId
    override fun i_isRead(): Boolean? = isRead

    override fun copyWith(isFollowin: Boolean?, selectedLikeType: String?, pollSelectedOptionId:
    String?, isRead: Boolean?, coldStartItems: List<ColdStartEntityItem>?, collectionItems:
                          List<PostEntity>?): AllLevelCards {
        return this.copy(
                isFollowin = isFollowin,
                selectedLikeType = selectedLikeType,
                pollSelectedOptionId = pollSelectedOptionId,
                isRead = isRead,
                postEntity = postEntity.copyWith(isFollowin, selectedLikeType,
                        pollSelectedOptionId, isRead, coldStartItems, collectionItems)
        )
    }
}