/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.newshunt.dataentity.common.asset.ColdStartEntityItem
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntity
import org.intellij.lang.annotations.Language
import java.io.Serializable

/**
 * @see PostEntityLevel
 * @author satosh.dhanyamraju
 */
@DatabaseView(value = CARD_Q, viewName = VIEW_TLCard)
data class TopLevelCard(
        @Embedded
        val postEntity: AllLevelCards
) : CommonAsset by postEntity, Serializable {
    override fun copyWith(isFollowin: Boolean?, selectedLikeType: String?, pollSelectedOptionId: String?, isRead: Boolean?, coldStartItems: List<ColdStartEntityItem>?, collectionItems: List<PostEntity>?): CommonAsset {
        return this.copy(postEntity = postEntity?.copyWith(isFollowin, selectedLikeType,
                pollSelectedOptionId, isRead, coldStartItems, collectionItems))
    }
}

/**
 * This class is intended to be used in dao queries returning Livedata<List<TopLevelCard>>
 *
 * A left-join B : if matching condtion does not hold, all columns in B will be null.
 *
 * When output of query is a List<B> (nonnull type B), then type-checker will throw when room tries to create B.
 *
 * So, this class wraps B to allow nullable types.
 *
 * @author satosh.dhanyamraju
 */
data class NullableTopLevelCard(
        @Embedded
        val postEntity: AllLevelCards?
) : Serializable

@DatabaseView(value = """
    SELECT * from  $VIEW_AllLevelCards
""", viewName = VIEW_DetailCard)
data class DetailCard(
        @Embedded
        val postEntity: AllLevelCards) : CommonAsset by postEntity, Serializable


@Language("RoomSql")
private const val CARD_Q = """
     SELECT *
    FROM ${TABLE_CARD} p  WHERE (level = 'TOP_LEVEL' OR level = 'LOCAL')
"""