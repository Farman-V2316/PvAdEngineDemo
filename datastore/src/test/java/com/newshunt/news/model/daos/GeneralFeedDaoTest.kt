/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.model.sqlite.SocialDB
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class GeneralFeedDaoTest {
    private lateinit var generalFeedDao: GeneralFeedDao
    private lateinit var fetchDao: FetchDao

    val g1 = GeneralFeed("e1", "ci", "GET", "NEWS")
    val g2 = GeneralFeed("e2", "c2", "GET", "VIDEO")


    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        fetchDao = instance.fetchDao()
        generalFeedDao = instance.groupDao()
    }

    private val LOC = "list"

    @Test
    fun testClearRowsNotInFetch_deletesOnlyMatchingRows() {
        generalFeedDao.insReplace(g1)
        generalFeedDao.insReplace(g2)
        fetchDao.insIgnore(FetchInfoEntity(g1.id, LOC, section = g1.section))
        fetchDao.insIgnore(FetchInfoEntity(g1.id, LOC, section = g1.section+"_____")) // change section
        fetchDao.insIgnore(FetchInfoEntity(g1.id+"_____", LOC, section = g1.section)) // change id
        fetchDao.insIgnore(FetchInfoEntity(g1.id+"_____", LOC, section = g1.section+"_____")) // change both
        //now g2
        fetchDao.insIgnore(FetchInfoEntity(g2.id, LOC, section = g2.section+"_____")) // change section
        fetchDao.insIgnore(FetchInfoEntity(g2.id+"_____", LOC, section = g2.section)) // change id
        fetchDao.insIgnore(FetchInfoEntity(g2.id+"_____", LOC, section = g2.section+"_____")) // change both
        Truth.assertThat(generalFeedDao.all()).containsExactly(g1, g2)

        generalFeedDao.clearRowsNotInFetchInfo()

        Truth.assertThat(generalFeedDao.all()).containsExactly(g1)
    }
}