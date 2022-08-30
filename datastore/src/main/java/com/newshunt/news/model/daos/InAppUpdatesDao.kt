/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.model.entity.COL_AVAILABLE_VERSION
import com.newshunt.dataentity.model.entity.InAppUpdatesEntity
import com.newshunt.dataentity.model.entity.TABLE_IN_APP_UPDATES

/**
 * Dao class for the In app update prompts table
 *
 * Created by srikanth.ramaswamy on 03/01/2021.
 */
@Dao
abstract class InAppUpdatesDao : BaseDao<InAppUpdatesEntity> {
    @Query("DELETE FROM $TABLE_IN_APP_UPDATES")
    abstract fun deleteAll()

    @Query("SELECT * FROM $TABLE_IN_APP_UPDATES ORDER BY $COL_AVAILABLE_VERSION DESC LIMIT 1")
    abstract fun queryUpdatePromptsData(): LiveData<List<InAppUpdatesEntity>>

    @Query("DELETE FROM $TABLE_IN_APP_UPDATES WHERE $COL_AVAILABLE_VERSION  NOT IN (SELECT $COL_AVAILABLE_VERSION FROM $TABLE_IN_APP_UPDATES ORDER BY $COL_AVAILABLE_VERSION DESC LIMIT 1)")
    abstract fun cleanupOlderUpdateInfo()
}