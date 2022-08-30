/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.SearchPage
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.SearchFeedDao
import com.newshunt.news.model.sqlite.SocialDB
import io.mockk.MockKAnnotations
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class SearchPageDaoTest {

    lateinit var fetchDao: FetchDao
    lateinit var searchFeedDao : SearchFeedDao
    val feedElement = SearchPage("id", "curl", "POST", "news","name" , entityType ="", entityLayout ="", viewOrder = 0)

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        searchFeedDao = instance.searchFeedDao()
        fetchDao = instance.fetchDao()
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testInsertWillShowUpInFeedPage() {
        assertThat(fetchDao.lookupPage(feedElement.id, feedElement.section)).isNull()
        searchFeedDao.insReplace(listOf(feedElement))
        assertThat(fetchDao.lookupPage(feedElement.id, feedElement.section)).isNotNull()
    }


    @Test
    fun testDeletion() {
        searchFeedDao.insReplace(listOf(feedElement))
        searchFeedDao.clear()
        assertThat(fetchDao.lookupPage(feedElement.id, feedElement.section)).isNull()
    }


    @Test
    fun testReplace() {
        searchFeedDao.replacePages(listOf(feedElement))
        val feedElement2 = feedElement.copy(id="2")
        searchFeedDao.replacePages(listOf(feedElement2))
        assertThat(fetchDao.lookupPage(feedElement2.id, feedElement.section)?.id).isEqualTo(feedElement2.id)
        assertThat(fetchDao.lookupPage(feedElement.id, feedElement.section)?.id).isNull()
    }
}