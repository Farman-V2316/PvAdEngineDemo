/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.social.entity.GeneralFeed


/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class GeneralFeedDao : BaseDao<GeneralFeed> {
    /**
     * using string concat because, we need to match multiple columns for deletion
     */
    @Query("DELETE FROM general_feed WHERE id||section NOT IN (SELECT col_entity_id||section FROM fetch_info)")
    abstract fun clearRowsNotInFetchInfo()

    @VisibleForTesting
    @Query("SELECT * FROM general_feed")
    abstract fun all(): List<GeneralFeed>
}

