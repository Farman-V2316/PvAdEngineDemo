/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.dataentity.common.asset.NCCImpression
import com.newshunt.dataentity.social.entity.TABLE_NCC_IMPRESSION

/*
* @author Mukesh Yadav
* */
@Dao
abstract class NCCImpressionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun upsertNCCImpression(nccImpression: NCCImpression)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertNCCImpressionList(nccImpressionList: List<NCCImpression>?)

    @Query(" SELECT * FROM $TABLE_NCC_IMPRESSION WHERE status = 'NOT_SYNCED'")
    abstract fun getNCCImpression() :  List<NCCImpression>

    @Query("DELETE FROM $TABLE_NCC_IMPRESSION WHERE status = 'SYNCED'")
    abstract fun deleteSyncedImpressionData()
}