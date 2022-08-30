/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.social.entity.DislikeEntity

@Dao
abstract class DislikeDao : BaseDao<DislikeEntity> {
    @Query("select * from dislikes")
    abstract fun all(): LiveData<List<DislikeEntity>>

    @Query("select postId from dislikes")
    abstract fun allIds(): LiveData<List<String>>

    @Query("DELETE FROM dislikes WHERE postId IN (:postIds)")
    abstract fun delete(postIds: List<String>)

    @Query("SELECT postId FROM dislikes WHERE postId IN (:postIds)")
    abstract fun getAllDislikedFrom(postIds: List<String>): List<String>

    @Query("DELETE FROM dislikes")
    abstract fun deleteAll()

    @Query("SELECT postId FROM dislikes WHERE postId=:id")
    abstract fun isDisliked(id: String) : String?

    @Query("""
        SELECT * from dislikes WHERE options IS NOT NULL AND createdAt >= :notOlderThan
        AND markedForPayload=1
        """)
    abstract fun selectRecent(notOlderThan: Long): List<DislikeEntity>

    @Query("DELETE FROM dislikes WHERE options IS NOT NULL AND createdAt < :olderThan")
    abstract fun deleteOlder(olderThan: Long)

    @Transaction
    open fun recentDislikes(notOlderThan: Long, deleteOlderEntries : Boolean = false/*
    default is false because we want to filter them out from the feed.
    */
    ): List<DislikeEntity> {
        if(deleteOlderEntries)
            deleteOlder(notOlderThan)
        return selectRecent(notOlderThan)
    }

    @Query("DELETE FROM dislikes WHERE format = :format AND subFormat = :subFormat")
    abstract fun removeAstroCards(format: String = Format.NATIVE_CARD.name,
                                  subFormat : String = SubFormat.ASTRO.name)
}