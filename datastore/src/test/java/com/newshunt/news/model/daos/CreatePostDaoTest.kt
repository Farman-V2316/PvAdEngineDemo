/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.utils.TEST_CONTENT_URL
import com.newshunt.news.model.utils.cp1
import com.newshunt.news.model.utils.dummy
import com.newshunt.news.model.utils.latestValue
import com.newshunt.news.model.utils.post1
import com.newshunt.news.model.utils.post2
import com.newshunt.news.model.utils.post3
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class CreatePostDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    lateinit var cpDao: CreatePostDao
    lateinit var fetchDao : FetchDao
    lateinit var interactionDao: InteractionDao
    lateinit var followDao : FollowEntityDao

    @Before
    fun setUp() {
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        AppConfig.createInstance(AppConfigBuilder())
        cpDao = instance.cpDao()
        fetchDao = instance.fetchDao()
        interactionDao = instance.interactionsDao()
        followDao = instance.followEntityDao()
        instance.pageEntityDao().insReplace(listOf(dummy, dummy.copy(pageEntity = dummy.pageEntity.copy(id = "c1"))))

    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }


    @Test
    fun testLocalPostInsertionDeletionWithTrigger() {
        val cp2 = cp1.copy(cpId = cp1.cpId + 1)
        cpDao.insReplace(cp1, cp2)
        fetchDao.insertLocalPost(listOf(post1.copy(id  = cp1.postId)), followDao)
        assertThat(fetchDao.allLocalPosts()).isNotEmpty()

        cpDao.delete(cp1.cpId)
        assertThat(fetchDao.allLocalPosts()).isEmpty()
    }


    @Test
    fun testTrigLocalPostDeletion_OnlyMatchingIdAndLevelIsDeleted() {
        val cp2 = cp1.copy(cpId = cp1.cpId + 1, postId = "postid2")
        cpDao.insReplace(cp1, cp2)
        fetchDao.insertLocalPost(listOf(post1.copy(id = cp1.postId)), followDao)
        fetchDao.insertLocalPost(listOf(post2.copy(id = cp2.postId)), followDao) // different id
        assertThat(fetchDao.allLocalPosts()).hasSize(2)
        fetchDao.insReplacePosts(listOf(post3.toCard2()))
        assertThat(fetchDao.all().map { it.i_id() }).containsExactly(
                cp1.postId.toString(),
                cp2.postId.toString(),
                post3.id
        )

        cpDao.delete(cp1.cpId)
        assertThat(cpDao.allLocalPost().map { it.postEntity.id }).containsExactly(cp2.postId.toString())
        assertThat(fetchDao.all().map { it.i_id() }).containsExactly(
                cp2.postId.toString(),
                post3.id
        )
    }
    val f1 = FetchInfoEntity("c1", "loc1", "np1", 0, "np10", section = "news")

    @Test
    fun `test discarding a post will cascade to fetchdata`() {
        val p1 = post1.copy(id = cp1.postId, level = PostEntityLevel.LOCAL)
        cpDao.insReplace(cp1)
        val fetchid = fetchDao.insIgnore(f1)
        fetchDao.replaceFirstPage(f1, listOf(post2), followDao, TEST_CONTENT_URL)
        fetchDao.insertLocalPost(listOf(p1), followDao, false, f1.entityId, f1.location, f1.section) // will be inserted on top of foryou
        val c1 = fetchDao.itemsMatchingLiveList(f1.entityId, f1.location, f1.section).latestValue()
        assertThat(c1?.map { it.i_id() }).containsExactly(p1.id , post2.id)

        cpDao.delete(cp1.cpId)
        val c2 = fetchDao.itemsMatchingLiveList(f1.entityId, f1.location, f1.section).latestValue()
        assertThat(c2?.map { it.i_id() }).containsExactly(post2.id)
    }

    @Test
    fun testLocalCardShownWithOldestToLatestOrder() {
        val cp00 = cp1.copy(creationDate = 10L)
        val cp01  = cp1.copy(cpId = cp1.cpId + 1, postId = "p22",  creationDate = 20)
        val cp02  = cp1.copy(cpId = cp1.cpId + 2, postId = "p23", creationDate = 30)
        cpDao.insReplace(cp00)
        cpDao.insReplace(cp01)
        cpDao.insReplace(cp02)

        fetchDao.insertLocalPost(listOf(
                post1.copy(id = cp00.postId),
                post1.copy(id = cp01.postId),
                post1.copy(id = cp02.postId)
        ), followDao)

        assertThat(fetchDao.allLocalPosts()?.map { it.id })
                .containsExactly(cp00.postId, cp01.postId, cp02.postId).inOrder()
    }
}