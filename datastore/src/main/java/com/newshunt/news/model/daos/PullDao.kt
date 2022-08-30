/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.PullInfoEntity
import com.newshunt.dataentity.social.entity.RecentTabEntity
import com.newshunt.dataentity.social.entity.VIEW_FeedPage

/**
 * Manages feed and tabs related information; primarily required from building feed payload.
 * Operates on [PullInfoEntity] and [RecentTabEntity]
 *
 * @author satosh.dhanyamraju
 */
@Dao
abstract class PullDao : BaseDao<PullInfoEntity> {
    /**
     * insert only if this belongs to [S_PageEntity]
     */
    @Query("""
        INSERT INTO ${PullInfoEntity.TABLE}(entityId, section, timestamp, pageCount)
        SELECT id entityId, SECTION, :ts, 0
        FROM pages
        WHERE entityId = :entityId AND section = :section
    """)
    abstract fun insertPullInfo(entityId: String, section: String, ts: Long = System.currentTimeMillis())

    /**
     * Look for the latest row matching this (id,section) and increment its pageCount
     */
    @Query("""
        UPDATE pull_info
        SET pageCount =
          (SELECT pageCount + 1
           FROM pull_info
           WHERE entityId = :entityId
             AND SECTION = :section
           ORDER BY timestamp DESC
           LIMIT 1)
        WHERE id =
            (SELECT id
             FROM pull_info
             WHERE entityId = :entityId
               AND SECTION = :section
             ORDER BY timestamp DESC
             LIMIT 1)
    """)
    abstract fun incrementLastPullInfoPageCount(entityId: String, section: String)


    @Transaction
    open fun pullInfoWithLimitsAndCleanup(
            notOlderThan: Long,
            entityId: String,
            section: String,
            deleteUnmatchedEntries: Boolean = true
    ): List<PullInfoEntity> {
        val list =  recentPullInfo(notOlderThan, entityId, section)
        if(deleteUnmatchedEntries)
            deletePullInfoOlderThan(notOlderThan)
        return list
    }


    @Query("""
        INSERT OR REPLACE INTO recent_tabs(entityId, entityType, section, ts)
        SELECT id entityId, entityType, SECTION, :ts
        FROM pages
        WHERE entityId = :entityId AND section = :section
    """)
    abstract fun insertOrReplaceRecentTab(entityId: String, section: String, ts: Long = System.currentTimeMillis())

    /**
     * Cleanup happens automatically. Every-time you read, it deletes the unread ones.
     */
    @Transaction
    open fun recentTabsWithLimitsAndCleanup(notOlderThan: Long, deleteUnmatchedEntries: Boolean = true): List<RecentTabEntity> {
        if(deleteUnmatchedEntries)
            deleteOldRecentTabs(notOlderThan)
        return recentTabsWithLimits(notOlderThan)
    }

    @Query("""
        SELECT * FROM pull_info
        WHERE timestamp >= :notOlderThan AND entityId = :entityId AND section = :section
        ORDER BY timestamp DESC
    """)
    @VisibleForTesting
    internal abstract fun recentPullInfo(notOlderThan: Long, entityId: String, section: String): List<PullInfoEntity>

    @VisibleForTesting
    @Query("DELETE FROM pull_info WHERE timestamp < :time")
    internal abstract fun deletePullInfoOlderThan(time: Long)

    @Query("""
        SELECT * FROM recent_tabs WHERE ts >= :notOlderThan ORDER BY ts DESC
    """)
    internal abstract fun recentTabsWithLimits(notOlderThan: Long): List<RecentTabEntity>

    @VisibleForTesting
    @Query("""
        DELETE
        FROM recent_tabs
        WHERE ts NOT IN
            (SELECT ts
             FROM recent_tabs
             WHERE ts >= :notOlderThan)
    """)
    internal abstract fun deleteOldRecentTabs(notOlderThan: Long)

    @VisibleForTesting
    @Query("SELECT * FROM $VIEW_FeedPage")
    internal abstract fun allFeedPages() : List<FeedPage>

    @VisibleForTesting
    @Query("SELECT * FROM pull_info")
    internal abstract fun allPullInfo() : List<PullInfoEntity>

    @VisibleForTesting
    @Query("SELECT * FROM ${RecentTabEntity.TABLE}")
    internal abstract fun allRecentTabs(): List<RecentTabEntity>

    @Query("DELETE FROM pull_info")
    abstract fun deleteAllPullInfo()

    @Query("DELETE FROM recent_tabs")
    abstract fun deleteAllRecentTabs()
}