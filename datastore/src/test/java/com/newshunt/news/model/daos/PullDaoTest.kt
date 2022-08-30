/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.PullInfoEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.utils.samplePageEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class PullDaoTest {

    lateinit var pullDao: PullDao
    lateinit var pageEntityDao: PageEntityDao
    lateinit var groupFeedDao: GeneralFeedDao

    private val nonExsitentPage = samplePageEntity.copy(pageEntity = samplePageEntity.pageEntity.copy(id = "nonexistent"))


    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        pullDao = instance.pullDao()
        pageEntityDao = instance.pageEntityDao()
        groupFeedDao = instance.groupDao()
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }


    @Test
    fun insertPullInfoWorksOnlyForIdsInPagesTable() {
        pageEntityDao.insReplace(listOf(samplePageEntity))
        val groupFeed = GeneralFeed("gid", "curl", "GET")
        groupFeedDao.insReplace(listOf(groupFeed))
        assertThat(pullDao.allFeedPages().map { it.id }).containsExactly(groupFeed.id, samplePageEntity.pageEntity.id)

        pullDao.insertPullInfo(groupFeed.id, groupFeed.section)
        pullDao.insertPullInfo(samplePageEntity.pageEntity.id, samplePageEntity.section)

        assertThat(pullDao.allPullInfo().map { it.entityId }).containsExactly(samplePageEntity.pageEntity.id)
    }

    @Test
    fun insertPullInfo_canInsertDuplicates() {
        pageEntityDao.insReplace(listOf(samplePageEntity))
        val id1 = samplePageEntity.pageEntity.id
        pullDao.insertPullInfo(id1, samplePageEntity.section)
        pullDao.insertPullInfo(id1, samplePageEntity.section)

        assertThat(pullDao.allPullInfo().map { it.entityId }).containsExactly(id1, id1)
    }

    @Test
    fun pullInfo_withTimeLimit() {
        pageEntityDao.insReplace(listOf(samplePageEntity, nonExsitentPage))
        val id1 = samplePageEntity.pageEntity.id
        val section = samplePageEntity.section
        pullDao.insertPullInfo(id1, section, 10)
        pullDao.insertPullInfo(id1, section, 15)
        pullDao.insertPullInfo(id1, section, 20)
        pullDao.insertPullInfo(id1, section, 30)
        pullDao.insertPullInfo(nonExsitentPage.pageEntity.id, nonExsitentPage.section, 40) // different tab (wont be returned)

        assertThat(pullDao.allPullInfo()).hasSize(5)
        assertThat(pullDao.recentPullInfo(20, id1, section).map { it.timestamp.toInt() }).containsExactly(30, 20).inOrder()
        assertThat(pullDao.recentPullInfo(40, id1, section).map { it.timestamp.toInt() }).isEmpty()
    }

    @Test
    fun incrementLastPullInfoPageCount() {
        pageEntityDao.insReplace(listOf(samplePageEntity))
        val id1 = samplePageEntity.pageEntity.id
        pullDao.insertPullInfo(id1, samplePageEntity.section)
        pullDao.insertPullInfo(id1, samplePageEntity.section)

        pullDao.incrementLastPullInfoPageCount(id1, samplePageEntity.section)
        pullDao.incrementLastPullInfoPageCount(id1, samplePageEntity.section)
        pullDao.incrementLastPullInfoPageCount(id1, samplePageEntity.section)

        assertThat(pullDao.allPullInfo().map { it.entityId to it.pageCount}).containsExactly(id1 to 0, id1 to 3)
    }

    @Test
    fun testRecentTabsInsertion() {
        pageEntityDao.insReplace(listOf(samplePageEntity))
        val id1 = samplePageEntity.pageEntity.id
        pullDao.insertOrReplaceRecentTab(id1, samplePageEntity.section, 1)
        assertThat(pullDao.allRecentTabs().map { it.entityId to it.ts }).containsExactly(id1 to 1L)
    }

    @Test
    fun testRecentTabsInsertionUpdates() {
        pageEntityDao.insReplace(listOf(samplePageEntity))
        val id1 = samplePageEntity.pageEntity.id
        pullDao.insertOrReplaceRecentTab(id1, samplePageEntity.section, 1)
        pullDao.insertOrReplaceRecentTab(id1, samplePageEntity.section, 2)
        pullDao.insertOrReplaceRecentTab(id1, samplePageEntity.section, 3)

        assertThat(pullDao.allRecentTabs().map { it.entityId to it.ts }).containsExactly(id1 to 3L)

    }

    @Test
    fun testPullinfoDeletion() {
        pageEntityDao.insReplace(listOf(samplePageEntity, nonExsitentPage))
        val id1 = samplePageEntity.pageEntity.id
        pullDao.insertPullInfo(id1, samplePageEntity.section, 5)
        pullDao.insertPullInfo(id1, samplePageEntity.section, 10)
        pullDao.insertPullInfo(id1, samplePageEntity.section, 15)
        val id2 = nonExsitentPage.pageEntity.id
        pullDao.insertPullInfo(id2, samplePageEntity.section, 15) // different id

        assertThat(pullDao.allPullInfo()).hasSize(4)
        assertThat(pullDao.pullInfoWithLimitsAndCleanup(11, id1, samplePageEntity.section))
                .hasSize(1)
        assertThat(pullDao.allPullInfo()).hasSize(2)
        assertThat(pullDao.allPullInfo().map { it.entityId }).containsExactly(id1, id2)
    }


    @Test
    fun testRecentTabDeletions() {

        fun S_PageEntity.copyWithDiffId(newid: String ) = copy(pageEntity = pageEntity.copy(id = newid))
        fun S_PageEntity.id() =  pageEntity.id


        val id1 = samplePageEntity.pageEntity.id
        val id2 = samplePageEntity.pageEntity.id+"2"
        val id3 = samplePageEntity.pageEntity.id+"3"
        val id4 = samplePageEntity.pageEntity.id+"4"
        pageEntityDao.insReplace(listOf(samplePageEntity, samplePageEntity.copyWithDiffId(id2),  samplePageEntity.copyWithDiffId(id3),  samplePageEntity.copyWithDiffId(id4)))

        pullDao.insertOrReplaceRecentTab(id1, samplePageEntity.section, 10)
        pullDao.insertOrReplaceRecentTab(id2, samplePageEntity.section, 15)
        pullDao.insertOrReplaceRecentTab(id3, samplePageEntity.section, 20)
        pullDao.insertOrReplaceRecentTab(id4, samplePageEntity.section, 30)

        assertThat(pullDao.allRecentTabs()).hasSize(4)
        assertThat(pullDao.recentTabsWithLimitsAndCleanup(30, true).map { it.ts.toInt() }).containsExactly(30)
        assertThat(pullDao.allRecentTabs()).hasSize(1)
    }

    @Test
    fun testRecentQueryReturnsEntriesMatchingConstraints() {
        fun S_PageEntity.copyWithDiffId(newid: String ) = copy(pageEntity = pageEntity.copy(id = newid))
        fun S_PageEntity.id() =  pageEntity.id


        val id1 = samplePageEntity.pageEntity.id
        val id2 = samplePageEntity.pageEntity.id+"2"
        val id3 = samplePageEntity.pageEntity.id+"3"
        val id4 = samplePageEntity.pageEntity.id+"4"
        pageEntityDao.insReplace(listOf(samplePageEntity, samplePageEntity.copyWithDiffId(id2),  samplePageEntity.copyWithDiffId(id3),  samplePageEntity.copyWithDiffId(id4)))


        pullDao.insertOrReplaceRecentTab(id1, samplePageEntity.section, 10)
        pullDao.insertOrReplaceRecentTab(id2, samplePageEntity.section, 15)
        pullDao.insertOrReplaceRecentTab(id3, samplePageEntity.section, 20)
        pullDao.insertOrReplaceRecentTab(id4, samplePageEntity.section, 30)

        assertThat(pullDao.recentTabsWithLimits(20).map { it.ts.toInt() }).containsExactly(30, 20).inOrder()
        assertThat(pullDao.recentTabsWithLimits(40).map { it.ts.toInt() }).isEmpty()
        assertThat(pullDao.recentTabsWithLimits(20).map { it.ts.toInt() }).containsExactly(30, 20).inOrder()
    }

    @Test
    fun testGroupByForPullInfo() {
        val p1N = PullInfoEntity("eid1", "news", 5, 0)
        val p1B = PullInfoEntity("eid1", "buzz", 5, 0)

        val p2N = PullInfoEntity("eid2", "news", 5, 0)
        val p3N = PullInfoEntity("eid3", "news", 5, 0)
        val p2B = PullInfoEntity("eid2", "buzz", 5, 0)
        val list = listOf(p1N, p1B, p2N, p3N, p2B)
        val grouped = list.groupBy { it.section }
        Truth.assertThat(grouped.keys).containsExactly("news", "buzz")
        Truth.assertThat(grouped.get("news")).containsExactly(p1N, p2N, p3N).inOrder()
        Truth.assertThat(grouped.get("buzz")).containsExactly(p1B, p2B).inOrder()
    }
}