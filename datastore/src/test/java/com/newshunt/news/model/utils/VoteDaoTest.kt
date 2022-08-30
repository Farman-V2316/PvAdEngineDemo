/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.newshunt.dataentity.common.asset.PollAsset
import com.newshunt.dataentity.common.asset.PollOptions
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.Vote
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.daos.VoteDao
import com.newshunt.news.model.sqlite.SocialDB
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class VoteDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var postDao: PostDao
    lateinit var voteDao: VoteDao
    private val initialOptions = listOf(
            PollOptions("id1", "option1", 0),
            PollOptions("id2", "option2", 0)
    )
    private val finalOptions = listOf(
            PollOptions("id1", "option1", 10),
            PollOptions("id2", "option2", 40)
    )


    private val p = post1.copy(poll = PollAsset(
            "poll title", 2, 3, initialOptions, false, 0, "i_url"
    ))

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        postDao = instance.postDao()
        voteDao = instance.voteDao()
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }


    @Test
    fun testIns() {
        val vote = Vote("uid", "p1", "o1")
        voteDao.insReplace(vote)
        Truth.assertThat(voteDao.all()).containsExactly(vote)
    }

    @Test
    fun testVoteCountsGetUpdated() {
        postDao.insReplace(p)
        val observer = postDao.postByIdLiveData(p.id).test()
        voteDao.answerSubmitted(Vote("user1", p.id, "id1"), p.poll!!.copy(options = finalOptions, responseCount = 50))
        val values = observer.values()
        Truth.assertThat(values.map {
            it?.i_poll()?.responseCount
        }).containsExactly(0, 50).inOrder()

        Truth.assertThat(values.map {
            it?.i_poll()?.options
        }).containsExactly(initialOptions, finalOptions).inOrder()
    }
}