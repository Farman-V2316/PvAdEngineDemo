/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.model.entity.MembershipStatus

/**
 *
 * Contains methods to populate calculated fields of a card
 *
 * @author satosh.dhanyamraju
 */
@Dao
abstract class CardDao {

    @Query("SELECT entityId id, entityType type, `action` socialAction FROM follow WHERE `action` = 'FOLLOW' OR `action` = 'BLOCK'")
    abstract fun allFollowsAndBlocks() : LiveData<List<Follow>>

    @Query("SELECT entityId FROM follow WHERE `action` = 'BLOCK'")
    abstract  fun blockedSourceIds(): List<String>

    @Query("""
        SELECT  entity_id, col_action FROM interactions intr 
        WHERE intr.actionToggle = 1
            AND intr.col_action IN ('LIKE', 'LOVE', 'HAPPY', 'WOW', 'SAD', 'ANGRY')
    """)
    abstract fun allLikeTypes(): LiveData<List<Interaction>>

    @Query("""
        SELECT pollId, optionId FROM votes
    """)
    abstract fun allVotes(): LiveData<List<Vote>>

    @Query("SELECT id FROM history")
    abstract fun allReadIds() : LiveData<List<String>>

    @Query("SELECT id FROM history where id=:id")
    abstract fun isNewsRead(id: String): String?

    @Query("SELECT membership,id from groupinfo")
    abstract fun allMemberShip(): LiveData<List<MemberShip>>

    @Query("SELECT * FROM card WHERE uniqueId IN (:ids)")
    abstract fun cardsById(ids: List<String>): LiveData<List<Card>>

    class Interaction(
            val entity_id : String,
            val col_action : String
    )

    class MemberShip(
            val id: String,
            val membership: MembershipStatus
    )

    class Vote(
            val pollId: String,
            val optionId: String
    )

    class Follow(
            val id: String,
            val type: String,
            val socialAction:String? = null,
            val subType: String? = null,
    )
}