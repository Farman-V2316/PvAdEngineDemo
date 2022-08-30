/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.newshunt.dataentity.social.entity.TABLE_NCC_IMPRESSION
import io.reactivex.Single

/**
 *  CSS = CardSeenState
 *  File contains Room related classes for managing CardSeenState data
 *  @author satosh.dhanyamraju@verse.in
 */
@Entity(tableName = CSSDao.TBL_CSSE)
data class CSSEntity(
        @PrimaryKey() val id: String,
        val fetch_id : Long,
        val state: Int = CSSDao.CSS_UNKNOWN,
        val batch_id: String = "",
        val ts: Long = System.currentTimeMillis()
)

@Dao
abstract class CSSDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun ins(items: List<CSSEntity>): List<Long>

    @Query("UPDATE $TBL_CSSE SET state = $CSS_SEEN WHERE id = :cardId")
    abstract fun markSeen(cardId: String): Int

    @Query("UPDATE $TBL_CSSE SET state = $CSS_DISCARDED WHERE state = $CSS_UNKNOWN AND fetch_id = :fetchId")
    abstract fun markDiscardedFromFetchId(fetchId: Long): Int

    @Query("UPDATE $TBL_CSSE SET state = $CSS_DISCARDED WHERE state = $CSS_UNKNOWN AND fetch_id in (select col_fetchInfoId  from fetch_info where col_disp_loc = 'list' AND col_entity_id IN (:ids) )")
    abstract fun markDiscardedForEntities(ids: List<String>): Single<Int>

    @Query("UPDATE $TBL_CSSE SET state = $CSS_DISCARDED WHERE state = $CSS_UNKNOWN")
    abstract fun markAllUnknownAsDiscarded(): Int

    @Query("SELECT id FROM $TBL_CSSE WHERE state = :st AND batch_id LIKE '%' || :bid || '%'")
    abstract fun batchOf(st : Int, bid: String): List<String>

    @Query("SELECT * FROM $TBL_CSSE WHERE batch_id LIKE '%' || :bid || '%'")
    abstract fun batchOf(bid: String): List<CSSEntity>

    @Query("UPDATE $TBL_CSSE SET batch_id = batch_id || :bid WHERE state = :st")
    abstract fun markBatch(st: Int, bid : String) : Int

    @Query("DELETE FROM $TBL_CSSE WHERE batch_id LIKE '%' || :bid || '%'")
    abstract fun deleteBatch(bid : String) : Int

    @Update
    abstract fun _update(list: List<CSSEntity>): Int

    fun rollbackBatch(bid: String): Int {
        return _update(batchOf(bid).map {
            it.copy(batch_id = it.batch_id.replace(bid, ""))
        })
    }

    fun tagSeenWith(bid: String): List<String> {
        markBatch(CSS_SEEN, bid)
        return batchOf(CSS_SEEN, bid)
    }

    fun tagDiscardedWith(bid: String): List<String> {
        markBatch(CSS_DISCARDED, bid)
        return batchOf(CSS_DISCARDED, bid)
    }

    @Query("DELETE FROM $TBL_CSSE WHERE ts < :ts")
    abstract fun deleteOlderThan(ts: Long)

    @Query("SELECT count(*) from $TBL_CSSE WHERE state = :st")
    abstract fun countByState(st: Int) : Int

    @Query("SELECT count(*) from $TBL_CSSE")
    abstract fun countAll() : Int

    companion object {
        const val CSS_SEEN = 1
        const val CSS_DISCARDED = 2
        const val CSS_UNKNOWN = 0 /*CardSeenState_UNKNOWN*/
        const val TBL_CSSE = "css_entities"
        const val mark_unknown_discarded = "UPDATE $TBL_CSSE SET state = $CSS_DISCARDED WHERE state = $CSS_UNKNOWN"
        const val clear_batch_id = "UPDATE $TBL_CSSE SET batch_id = ''"
        const val delete_older_items = "DELETE FROM $TBL_CSSE WHERE ts < %s AND state = $CSS_UNKNOWN"
        const val mark_ncc_not_synced = "UPDATE $TABLE_NCC_IMPRESSION SET status = 'NOT_SYNCED' WHERE status = 'SYNCING'"
    }
}