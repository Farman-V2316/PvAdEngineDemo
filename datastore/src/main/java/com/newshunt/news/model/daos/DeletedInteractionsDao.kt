/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.model.entity.COL_SYNC_STATUS
import com.newshunt.dataentity.model.entity.DeletedInteractionsEntity
import com.newshunt.dataentity.model.entity.TABLE_DELETED_INTERACTIONS

/**
 * Dao to interact with deleted interactions to help sync the deletions with B.E APIs
 * <p>
 * Created by srikanth.ramaswamy on 11/08/2019.
 */

@Dao
abstract class DeletedInteractionsDao : BaseDao<DeletedInteractionsEntity> {
    @Query("SELECT * FROM $TABLE_DELETED_INTERACTIONS WHERE $COL_SYNC_STATUS IN (:status)")
    abstract fun getDeletedInteractionsByStatus(status: List<SyncStatus>): List<DeletedInteractionsEntity>

    @Query("UPDATE $TABLE_DELETED_INTERACTIONS SET $COL_SYNC_STATUS = :toStatus WHERE $COL_SYNC_STATUS IN (:fromStatus)")
    abstract fun setStatus(fromStatus: List<SyncStatus>, toStatus: SyncStatus)

    @Query("DELETE FROM $TABLE_DELETED_INTERACTIONS WHERE $COL_SYNC_STATUS = :status")
    abstract fun deleteInteractionsByStatus(status: SyncStatus)

    @Query("DELETE FROM $TABLE_DELETED_INTERACTIONS")
    abstract fun clearDeletedInteractions()
}