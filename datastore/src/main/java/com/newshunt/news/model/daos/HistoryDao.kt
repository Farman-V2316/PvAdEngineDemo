/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.dataentity.model.entity.COL_ID
import com.newshunt.dataentity.model.entity.COL_IS_DELETED
import com.newshunt.dataentity.model.entity.COL_MARK_DELETED
import com.newshunt.dataentity.model.entity.COL_TIMESTAMP
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.model.entity.TABLE_HISTORY

/**
 * Dao to interact with the History table
 * <p>
 * Created by srikanth.ramaswamy on 11/18/2019.
 */
private const val HISTORY_MAX_ENTRIES = 500

@Dao
abstract class HistoryDao : BaseDao<HistoryEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertIgnore(history: HistoryEntity)

    @Query("SELECT* FROM $TABLE_HISTORY WHERE ($COL_TIMESTAMP >= (:fromTime) AND $COL_IS_DELETED = 0 AND $COL_MARK_DELETED = 0) ORDER BY timestamp DESC LIMIT $HISTORY_MAX_ENTRIES")
    abstract fun queryLiveData(fromTime: Long): LiveData<List<HistoryEntity>>

    @Query("SELECT* FROM $TABLE_HISTORY WHERE ($COL_TIMESTAMP >= (:fromTime) AND $COL_IS_DELETED = 0 AND $COL_MARK_DELETED = 0) ORDER BY timestamp DESC LIMIT $HISTORY_MAX_ENTRIES")
    abstract fun queryDataByTime(fromTime: Long): List<HistoryEntity>

    @Query("DELETE FROM $TABLE_HISTORY")
    abstract fun clearForever()

    @Query("UPDATE $TABLE_HISTORY SET $COL_MARK_DELETED = 1 WHERE $COL_ID=:storyId")
    abstract fun markDeleted(storyId: String)

    @Query("UPDATE $TABLE_HISTORY SET $COL_IS_DELETED = 1 WHERE $COL_MARK_DELETED = 1")
    abstract fun deleteMarkedItems()

    @Query("UPDATE $TABLE_HISTORY SET $COL_MARK_DELETED = 0 WHERE $COL_MARK_DELETED = 1")
    abstract fun undoDelete()

    @Query("DELETE FROM $TABLE_HISTORY WHERE $COL_ID NOT IN (SELECT $COL_ID FROM $TABLE_HISTORY ORDER BY $COL_TIMESTAMP DESC LIMIT $HISTORY_MAX_ENTRIES)")
    abstract fun flushOld()

    @Query("SELECT COUNT(*) FROM $TABLE_HISTORY WHERE $COL_MARK_DELETED = 0 AND $COL_MARK_DELETED = 0")
    abstract fun count(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_HISTORY WHERE ($COL_TIMESTAMP >= (:fromTime) AND $COL_IS_DELETED = 0) ORDER BY timestamp DESC LIMIT $HISTORY_MAX_ENTRIES")
    abstract fun countSinceTime(fromTime: Long): LiveData<Int>

    @Query("UPDATE $TABLE_HISTORY SET $COL_IS_DELETED = 1")
    abstract fun clear()

    @Query("SELECT * FROM $TABLE_HISTORY WHERE $COL_ID = (:itemId)")
    abstract fun getItem(itemId: String): HistoryEntity?

    @Query("DELETE FROM $TABLE_HISTORY WHERE $COL_ID IN (:postIds)")
    abstract fun deletePost(postIds: List<String>)
}