/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.PollAsset
import com.newshunt.dataentity.social.entity.TABLE_CARD
import com.newshunt.dataentity.social.entity.Vote


/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class VoteDao : BaseDao<Vote> {
    @VisibleForTesting
    @Query("select * from votes")
    abstract fun all(): List<Vote>

    @Query("""
        select * from $TABLE_CARD where id = :id
    """)
    abstract fun getStoredPost(id: String): List<Card>?

    @Update
    abstract fun update(post: Card)

    @Transaction
    open fun answerSubmitted(vote: Vote, asset: PollAsset) {
        insReplace(vote)
        val storedPost = getStoredPost(vote.pollId) ?: return
        storedPost.forEach {
            val updatedPoll = it.i_poll()?.copy(options = asset.options,
                    responseCount =
                    asset.responseCount)
            val newPost = it.withPoll(updatedPoll)
            update(newPost)
        }

    }

    @Query("DELETE FROM votes")
    abstract fun deleteAll()
}