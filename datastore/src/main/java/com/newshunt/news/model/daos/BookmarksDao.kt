/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkEntity
import com.newshunt.dataentity.model.entity.COL_ACTION
import com.newshunt.dataentity.model.entity.COL_FORMAT
import com.newshunt.dataentity.model.entity.COL_ID
import com.newshunt.dataentity.model.entity.COL_SYNC_STATUS
import com.newshunt.dataentity.model.entity.TABLE_BOOKMARKS
import com.newshunt.news.model.usecase.ReadLimitedCardsUsecase

/**
 * Dao to interact with the Bookmarks table
 * <p>
 * Created by srikanth.ramaswamy on 11/09/2019.
 */

@Dao
abstract class BookmarksDao : BaseDao<BookmarkEntity> {
    @Query("SELECT * FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = (:action) ")
    abstract fun getBookmarks(action: BookMarkAction): List<BookmarkEntity>

    @Query("SELECT $COL_ID FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = (:action)")
    abstract fun getBookmarkIds(action: BookMarkAction): List<String>

    @Query("SELECT * FROM $TABLE_BOOKMARKS WHERE $COL_SYNC_STATUS IN (:status)")
    abstract fun getBookmarksByStatus(status: List<SyncStatus>): MutableList<BookmarkEntity>

    @Query("UPDATE $TABLE_BOOKMARKS SET $COL_SYNC_STATUS=(:toStatus) WHERE $COL_SYNC_STATUS IN  (:fromStatus) AND `$COL_ACTION` = (:action)")
    abstract fun setStatusForAction(fromStatus: List<SyncStatus>, toStatus: SyncStatus, action: BookMarkAction)

    @Query("UPDATE $TABLE_BOOKMARKS SET $COL_SYNC_STATUS=(:toStatus) WHERE $COL_SYNC_STATUS IN (:fromStatus)")
    abstract fun setStatus(fromStatus: List<SyncStatus>, toStatus: SyncStatus)

    @Query("DELETE FROM $TABLE_BOOKMARKS WHERE `$COL_SYNC_STATUS` = (:status) AND `$COL_ACTION` = (:action)")
    abstract fun deleteBookmarksByActionAndStatus(status: SyncStatus, action: BookMarkAction)

    @Query("DELETE FROM $TABLE_BOOKMARKS")
    abstract fun clearBookmarks()

    @Query("SELECT COUNT(*) FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = (:action) AND $COL_FORMAT IN (:formats)")
    abstract fun countByFormatLiveData(action: BookMarkAction, formats: List<String>): LiveData<Int>

    @Query("SELECT COUNT(*) FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = (:action) AND $COL_FORMAT=(:format)")
    abstract fun countByFormat(action: BookMarkAction, format: String): Int

    @Query("SELECT $COL_FORMAT format, COUNT($COL_FORMAT) count FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = (:action) AND $COL_FORMAT in (:format) GROUP BY $COL_FORMAT")
    abstract fun countByFormatGrouped(action: BookMarkAction, format: List<String>): LiveData<List<ReadLimitedCardsUsecase.FormatWithCount>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertIgnore(entity: List<BookmarkEntity>)

    @Query("SELECT $COL_ID FROM $TABLE_BOOKMARKS WHERE `$COL_ACTION` = (:action)")
    abstract fun queryBookmarkIdsLiveData(action: BookMarkAction): LiveData<List<String>>

    @Query("DELETE FROM $TABLE_BOOKMARKS WHERE $COL_ID IN (:postIds)")
    abstract fun deletePost(postIds: List<String>)

    @Query("SELECT count(*) from $TABLE_BOOKMARKS where  $COL_ID = :itemId AND `$COL_ACTION` = 'ADD'")
    abstract fun countByAddedForItem(itemId: String): LiveData<Int>
}