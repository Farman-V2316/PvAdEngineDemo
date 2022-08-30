/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.model.entity.APPROVALS_TABLE_NAME
import com.newshunt.dataentity.model.entity.COL_USER_ID
import com.newshunt.dataentity.model.entity.PendingApprovalsEntity
import io.reactivex.Observable

/**
 * Dao to interact with the pending approvals table
 * <p>
 * Created by srikanth.ramaswamy on 12/07/2019.
 */

@Dao
abstract class PendingApprovalsDao : BaseDao<PendingApprovalsEntity> {

    @Query("SELECT * FROM $APPROVALS_TABLE_NAME WHERE $COL_USER_ID = (:userId)")
    abstract fun queryPendingApprovalsLiveData(userId: String): LiveData<PendingApprovalsEntity?>

    @Query("DELETE FROM $APPROVALS_TABLE_NAME")
    abstract fun delete()

    @Transaction
    open fun insert(entity: PendingApprovalsEntity, postDao: PostDao) {
        insReplace(entity)
        val approvalCard = postDao.postIdByFormatAndSubFormat(Format.BANNER, SubFormat.PENDING_APPROVAL)
        if (approvalCard.isNullOrEmpty()) {
            return
        }
        postDao.updateCount(approvalCard[0], Counts2(POST_APPROVALS = entity.approvalCounts?.POST_APPROVALS,
                MEMBER_APPROVALS = entity.approvalCounts?.MEMBER_APPROVALS,
                TOTAL_PENDING_APPROVALS = entity.approvalCounts?.TOTAL_PENDING_APPROVALS,
                INVITES = entity.approvalCounts?.INVITES))
    }
}