/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.social.entity.Vote
import com.newshunt.news.model.apis.AnswerPollApi
import com.newshunt.news.model.daos.VoteDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Stores answer locally.
 *
 * @author satosh.dhanyamraju

 */
class AnswerPollUsecase @Inject constructor(private val voteDao: VoteDao,
                                            private val api: AnswerPollApi) : BundleUsecase<String> {

    override fun invoke(p1: Bundle): Observable<String> {
        val vote = (p1.getSerializable(B_VOTE) as? Vote)
                ?: throw IllegalArgumentException("missing bundle arg $B_VOTE")
        val url = p1.getString(B_INTERACTIONURL, null)
                ?: throw IllegalArgumentException("missing bundle arg $B_INTERACTIONURL")
        return api.postPollResponse(url, vote.optionId).map {
            if (it.data != null) {
                val updatedVote = it.data.selectedOption?.let {
                    vote.copy(optionId = it)
                } ?: vote
                voteDao.answerSubmitted(updatedVote, it.data.toPollAsset())
            }
            it.status?.message ?: ""
        }
    }

    companion object {
        const val B_VOTE = "b_vote"
        const val B_INTERACTIONURL = "b_interactionurl"
    }
}