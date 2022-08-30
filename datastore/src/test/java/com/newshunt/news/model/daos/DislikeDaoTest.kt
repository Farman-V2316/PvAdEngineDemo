/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.DislikeEntity
import com.newshunt.news.model.sqlite.SocialDB
import io.mockk.MockKAnnotations
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
class DislikeDaoTest {
    private lateinit var dislikeDao: DislikeDao

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        dislikeDao = instance.dislikeDao()
        MockKAnnotations.init(this)

    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testRecent() {
        dislikeDao.insReplace(DislikeEntity("p", options = listOf("L1_NOT_INT"), createdAt = 10))
        dislikeDao.insReplace(DislikeEntity("p2", options = listOf("L1_NOT_INT"), createdAt = 20))

        assertThat(dislikeDao.recentDislikes(15, false).map { it.createdAt }).containsExactly(20L)
        assertThat(dislikeDao.recentDislikes(1, false).map { it.createdAt }).containsExactly(20L, 10L)
        assertThat(dislikeDao.recentDislikes(15, true).map { it.createdAt }).containsExactly(20L)
        assertThat(dislikeDao.recentDislikes(1, false).map { it.createdAt }).containsExactly(20L)
    }
}