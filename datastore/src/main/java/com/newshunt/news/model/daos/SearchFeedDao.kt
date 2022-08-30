/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.social.entity.SearchPage

/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class SearchFeedDao : BaseDao<SearchPage> {
    @Query("DELETE FROM search_feed")
    abstract fun clear()

    @Transaction
    open fun replacePages(pages: List<SearchPage>) {
        clear()
        insReplace(pages)
    }
}