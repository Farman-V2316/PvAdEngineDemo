/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.CountType
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.InteractionPayload
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dataentity.social.entity.TABLE_CARD
import org.intellij.lang.annotations.Language

@Dao
abstract class InteractionDao : BaseDao<Interaction> {

    val likeTypes = LikeType.values().map { it.name }

    /**
     * Triggers to update counts will be written only in columnn updates (to reduce number of triggers). So insert is insertIgnore + toggle
     *
     */
    @Transaction
    open fun toggleLike(entityId: String, entityType: String, likeType: String, parentId: String? = null, recDepth: Int = 0) {
        // if view calls toggle ANGRY and then WOW, it would have 2 rows in DB (no constraint).
        // We want to toggle the ANGRY here
        val exsitingRow = selectInteractionBy(entityId, entityType, likeTypes)
        val (isLiked, actualLikeType) = if (exsitingRow.isEmpty()) {
            _insIgnore(Interaction(entityId, entityType, likeType, false))
            _toggleLike(entityId, entityType, likeType)
            true to likeType
        } else {
            val existingLikeTgl = exsitingRow.first().actionToggle
            _toggleLike(entityId, entityType, likeType)
            existingLikeTgl.not() to (if(existingLikeTgl.not()) likeType else exsitingRow.first().action)
        }
        Logger.d(TAG, "toggleLike: $entityId, $entityType, $likeType, $parentId, $actualLikeType, $isLiked, $recDepth")
        incOrDecCountOf(entityId, isLiked, parentId, actualLikeType)
        // This is for detail page, which allows selecting a different like type when one is already selected.
        if (!isLiked && likeType != actualLikeType && recDepth == 0) {
            Logger.d(TAG, "toggleLike: recursing")
            toggleLike(entityId, entityType, likeType, parentId, 1)
        }
    }

    @Query("SELECT * FROM $TABLE_CARD WHERE id  = :id")
    abstract fun lookupCard(id: String) : List<Card> // is a list because of levels

    @Transaction
    open fun incOrDecCountOf(entityId: String, isInc: Boolean, parentId: String?, actualLikeType: String) {
        val newList: List<Card>
        var newList2: List<Card>? = null
        if (parentId != null) {
            newList2 = lookupCard(parentId).map { parent ->
                val childCards = parent.i_collectionItems()?.filterIsInstance<PostEntity>()?.map {
                    if (it.i_id() == entityId) {
                        val storedCounts = (it.i_counts() ?: Counts2())
                        val newCounts = if (isInc)
                            storedCounts.incrementTotalLikeCount()
                        else storedCounts.decrementTotalLikeCount()
                        it.withNewCounts(newCounts)
                    } else {
                        it
                    }
                }

                val childMoreCards = parent.i_moreStories()?.filterIsInstance<PostEntity>()?.map {
                    if (it.i_id() == entityId) {
                        val storedCounts = (it.i_counts() ?: Counts2())
                        val newCounts = if (isInc)
                            storedCounts.incrementTotalLikeCount()
                        else storedCounts.decrementTotalLikeCount()
                        it.withNewCounts(newCounts)
                    } else {
                        it
                    }
                }

                parent.copy(postEntity = parent.postEntity.copy(collectionAsset = parent
                        .postEntity.collectionAsset?.copy(collectionItem = childCards),
                        moreStories = childMoreCards))
            }
        }
        newList = lookupCard(entityId).map {
            val storedCounts = (it.i_counts() ?: Counts2())
            val totalLike = if (isInc) storedCounts.incCount(storedCounts.TOTAL_LIKE) else storedCounts.decCount(storedCounts.TOTAL_LIKE)
            val newCounts = when {
                actualLikeType == LikeType.LIKE.name -> {
                    storedCounts.copy(
                            TOTAL_LIKE = totalLike,
                            LIKE = if (isInc) storedCounts.incCount(storedCounts.LIKE) else storedCounts.decCount(storedCounts.LIKE))
                }
                actualLikeType == LikeType.SAD.name -> {
                    storedCounts.copy(TOTAL_LIKE = totalLike,
                            SAD = if (isInc) storedCounts.incCount(storedCounts.SAD) else storedCounts.decCount(storedCounts.SAD))
                }
                else -> {
                    storedCounts.copy(TOTAL_LIKE = totalLike,
                            ANGRY = if (isInc) storedCounts.incCount(storedCounts.ANGRY) else storedCounts.decCount(storedCounts.ANGRY))
                }
            }
            it.withNewCounts(newCounts)
        }
        newList2?.let {
            updatePosts(it)
        }
        updatePosts(newList)
    }

    @Transaction
    open fun incShareCount(entityId: String, parentId: String?) {
        val newList:List<Card>
        var newList2: List<Card>? = null
        newList = lookupCard(entityId).map {
            val storedCounts = (it.i_counts() ?: Counts2())
            it.withNewCounts(storedCounts.incrementShareCount())
        }
        if (parentId != null) {
            newList2 = lookupCard(parentId).map { parent ->
                val childCollectionCards = parent.i_collectionItems()?.filterIsInstance<PostEntity>()?.map {
                    if (it.i_id() == entityId) {
                        val storedCounts = (it.i_counts() ?: Counts2())
                        it.withNewCounts(storedCounts.incrementShareCount())
                    } else {
                        it
                    }
                }

                val childMoreCards = parent.i_moreStories()?.filterIsInstance<PostEntity>()?.map {
                    if (it.i_id() == entityId) {
                        val storedCounts = (it.i_counts() ?: Counts2())
                        it.withNewCounts(storedCounts.incrementShareCount())
                    } else {
                        it
                    }
                }

                val parentPostEntity = parent.postEntity.copy(collectionAsset = parent.postEntity
                        .collectionAsset?.copy(collectionItem = childCollectionCards), moreStories = childMoreCards)

                parent.copy(postEntity = parentPostEntity)
            }
        }
        updatePosts(newList)
        newList2?.let {
            updatePosts(it)
        }
    }

    @Update
    abstract fun updatePosts(list: List<Card>)

    /**
     * Trigger count increment on insert.
     * Subsequent inserts will be ignored.
     *
     * insert into table, allow duplicates?; set isSynced=false; inc counts
     */
    @Transaction
    open fun share(entityId: String, entityType: String, parentId: String?=null) {
        _insIgnore(Interaction(entityId, entityType, CountType.SHARE.name, actionToggle = true, shareTs = System.currentTimeMillis()))
        incShareCount(entityId, parentId)
    }

    /**
     * mark the item as synced where the action time is less than the sync time
     * // TODO(satosh.dhanyamraju):  also delete the value marked as synced
     */
    @Query("""
        UPDATE interactions
        SET isSynced = 1
        WHERE col_action in (:actions)
          AND ts < :syncTime
    """)
    abstract fun markSyncedItems(syncTime: Long, actions: List<String>)

    fun unsyncedAddedLikes(): List<InteractionPayload.InteractionPayloadItem> =
            _q(false, true, likeTypes)

    fun unsyncedDeletedLikes(): List<InteractionPayload.InteractionPayloadItem> =
            _q(false, false, likeTypes)

    fun unsyncedShares(): List<InteractionPayload.InteractionPayloadItem> =
            _q(false, true, listOf(CountType.SHARE.name))


    @Query("""
        SELECT entity_id entityId,
               entity_type entityType,
               col_action "action",
               ts actionTime
        FROM interactions
        WHERE isSynced = :isSynced
          AND actionToggle = :actionToggle
          AND col_action IN (:actions);
    """)
    @VisibleForTesting
    abstract fun _q(isSynced: Boolean, actionToggle: Boolean, actions: List<String>): List<InteractionPayload.InteractionPayloadItem>

    @VisibleForTesting
    @Query("select * from interactions")
    abstract fun _allInteractions(): LiveData<List<Interaction>>


    @VisibleForTesting
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun _insIgnore(vararg interactions: Interaction)


    /**
     *
     * Toggles a like in the table. Before calling this function, make sure the row exists in the
     * table. Else, it is a no-op.
     *
     * actionToggle -> 1 to 0 , no change in action
     * actionToggle -> 0 to 1, use the passed action
     *
     */
    @Query("""
        UPDATE interactions
        SET isSynced = 0,
            ts = :ts,
            actionToggle = not(actionToggle),
            col_action = CASE
                             WHEN actionToggle = 0 THEN :likeType
                             ELSE col_action
                         END
        WHERE col_action IN ('LIKE',
                             'LOVE',
                             'HAPPY',
                             'WOW',
                             'SAD',
                             'ANGRY')
          AND entity_id = :entityId
          AND entity_type = :entityType
    """)
    abstract fun _toggleLike(entityId: String, entityType: String, likeType: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM interactions WHERE col_action IN (:actions) AND isSynced=0")
    abstract fun deleteUnsyncedInteractions(actions: List<String>)

    @Query("""
        SELECT * FROM interactions WHERE entity_id = :id AND entity_type = :type AND col_action in (:actions)
    """)
    abstract fun selectInteractionBy(id: String, type: String, actions: List<String>): List<Interaction>

    @Query("DELETE FROM interactions")
    abstract fun deleteAll()

    @Query("SELECT * FROM interactions WHERE entity_id IN (:ids) AND actionToggle = 1 " +
            "AND col_action IN (:actions)")
    abstract fun  likes (ids : List<String>, actions: List<String> = likeTypes) : List<Interaction>

    companion object {
        @Language("RoomSql")
        val trigger_inc_total_like = """

        CREATE TRIGGER trigger_inc_total_like AFTER
        UPDATE OF actionToggle ON interactions
        WHEN NEW.isSynced = 0
			AND NEW.col_action IN ('LIKE', 'LOVE', 'HAPPY', 'WOW', 'SAD', 'ANGRY')
			AND OLD.actionToggle = 0 AND NEW.actionToggle = 1
        BEGIN
        UPDATE posts
        SET count_total_like_value =
          (SELECT max(i)
           FROM
             (SELECT CASE
                         WHEN count_total_like_value IS NULL THEN 1
                         WHEN count_total_like_value = cast(count_total_like_value AS INTEGER)
                            THEN cast(count_total_like_value AS INTEGER) + 1
                         ELSE count_total_like_value
                     END i
              FROM posts t1
              WHERE t1.id = NEW.entity_id))
        WHERE id = NEW.entity_id;

        END

       """.trimIndent()

        @Language("RoomSql")
        val trigger_dec_total_like = """

        CREATE TRIGGER trigger_dec_total_like AFTER
        UPDATE OF actionToggle ON interactions
        WHEN NEW.isSynced = 0
			AND NEW.col_action IN ('LIKE', 'LOVE', 'HAPPY', 'WOW', 'SAD', 'ANGRY')
			AND OLD.actionToggle = 1 AND NEW.actionToggle = 0
        BEGIN
        UPDATE posts
        SET count_total_like_value =
          (SELECT max(i)
           FROM
             (SELECT CASE
                         WHEN count_total_like_value = 0 THEN 0
                         WHEN count_total_like_value = cast(count_total_like_value AS INTEGER) 
                            THEN cast(count_total_like_value AS INTEGER) - 1
                         ELSE count_total_like_value
                     END i
              FROM posts t1
              WHERE t1.id = NEW.entity_id))
        WHERE id = NEW.entity_id;

        END

       """.trimIndent()

        @Language("RoomSql")
        val trigger_inc_share_count = """
        CREATE TRIGGER trigger_inc_share_count AFTER INSERT ON interactions
        WHEN NEW.isSynced = 0 AND NEW.col_action = 'SHARE'
        BEGIN
        UPDATE posts
        SET count_share_value =
          (SELECT max(i)
           FROM
             (SELECT CASE
                         WHEN count_share_value IS NULL THEN 1
                         WHEN count_share_value = cast(count_share_value AS INTEGER) 
                            THEN cast(count_share_value AS INTEGER) + 1
                         ELSE count_share_value
                     END i
              FROM posts t1
              WHERE t1.id = NEW.entity_id))
        WHERE id = NEW.entity_id;
        END

       """.trimIndent()

        const val TAG = "InteractionDao"
    }
}