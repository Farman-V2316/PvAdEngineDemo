/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.common.asset.ArticleTimeSpentTrackEntity

/**
 * @author amit.chaudhary
 */
@Dao
abstract class RecentArticleTrackerDao : BaseDao<ArticleTimeSpentTrackEntity> {

    @Query("SELECT * from article_time_spent_track WHERE timestamp >= :notOlderThan")
    abstract fun selectRecent(notOlderThan: Long): List<ArticleTimeSpentTrackEntity>

    @Query("DELETE FROM article_time_spent_track WHERE timestamp < :olderThan")
    abstract fun deleteOlder(olderThan: Long)

    @Transaction
    open fun recentViewedArticles(notOlderThan: Long,
                                  deleteOlderEntries: Boolean = true): List<ArticleTimeSpentTrackEntity> {
        if (deleteOlderEntries)
            deleteOlder(notOlderThan)
        return selectRecent(notOlderThan)
    }

    @Query("select referrer from article_time_spent_track WHERE itemId = :id LIMIT 1")
    abstract fun referrerOf(id: String): String?

    // when inserting, if db row has non-null referrer, do not make it null
    open fun customInsert(list: List<ArticleTimeSpentTrackEntity>) {
        list.forEach {
            if (it.referrer.isEmpty()) {
                it.referrer = referrerOf(it.itemId)?:""
            }
        }
        insReplace(list)
    }
}