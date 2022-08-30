/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.toLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.Discussions
import com.newshunt.dataentity.common.asset.EntityConfig2
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.news.model.entity.server.asset.ShareParam
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.daos.PostDao
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
class TriggersTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var instance: SocialDB
    lateinit var postDao: PostDao
    lateinit var interactionDao: InteractionDao
    lateinit var dao: FetchDao
    lateinit var cpDao : CreatePostDao
    lateinit var followDao: FollowEntityDao
    private val p = post1.copy(counts = Counts2(TOTAL_LIKE = EntityConfig2(" 1 ", 1)))

    val dummy = S_PageEntity(PageEntity(
            id = "",
            name = "",
            displayName = "",
            entityType = "",
            subType = "",
            entityLayout = "",
            contentUrl = TEST_CONTENT_URL,
            entityInfoUrl = "",
            handle = "",
            deeplinkUrl = "",
            moreContentLoadUrl = "",
            entityImageUrl = "",
            shareParams = ShareParam(),
            shareUrl = "",
            nameEnglish = "",
            contentRequestMethod = "GET",
            enableWebHistory = true,
            viewOrder = 1,
            isFollowable = true,
            appIndexDescription = ""), section = PageSection.NEWS.section)

    private val p0 = post2

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        postDao = instance.postDao()
        interactionDao = instance.interactionsDao()
        dao = instance.fetchDao()
        cpDao = instance.cpDao()
        followDao = instance.followEntityDao()
        instance.pageEntityDao().insReplace(listOf(dummy, dummy.copy(pageEntity = dummy.pageEntity.copy(id = "c1"))))
    }


    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testinclike() {
        postDao.insReplace(p, p0)

        interactionDao.toggleLike(p.id, "post", LikeType.ANGRY.name)
        interactionDao.toggleLike(p0.id, "post", LikeType.HAPPY.name)

        Truth.assertThat(postDao.postByIdLiveData(p.id).test()?.values()?.last()?.i_counts()?.TOTAL_LIKE?.value).isEqualTo("2")
        Truth.assertThat(postDao.postByIdLiveData(p0.id).test()?.values()?.last()?.i_counts()?.TOTAL_LIKE?.value).isEqualTo("1")
    }

    @Test
    fun testdeclike() {
        postDao.insReplace(p)

        interactionDao.toggleLike(p.id, "post", LikeType.ANGRY.name)
        interactionDao.toggleLike(p.id, "post", LikeType.ANGRY.name)

        Truth.assertThat(postDao.postByIdLiveData(p.id).test()?.values()?.last()?.i_counts()?.TOTAL_LIKE?.value).isEqualTo("1")
    }

    @Test
    fun testshareTrigger() {
        postDao.insReplace(p)

        interactionDao.share(p.id, "post")
        interactionDao.share(p.id, "post")
        interactionDao.share(p.id, "post") // can share multiple times

        Truth.assertThat(postDao.postByIdLiveData(p.id).test()?.values()?.last()?.i_counts()?.SHARE?.value).isEqualTo("3")
    }
    val f1 = FetchInfoEntity("c1", "loc1", "np1", 0, "np10", section = "news")


    private fun commentCounts() =
            dao.itemsMatching(f1.entityId, f1.location, f1.section)
                    .toLiveData(100)
                    .latestValue()
                    ?.map {
                        it.i_id() to it.i_counts()?.COMMENTS?.value
                    }

    private fun repostCounts() =
            dao.itemsMatching(f1.entityId, f1.location, f1.section)
                    .toLiveData(100)
                    .latestValue()
                    ?.map {
                        it.i_id() to it.i_counts()?.REPOST?.value
                    }

    @Test
    fun `test inserting comment increments count`() {



        val c1 = cp1.copy(cpId = 1 , parentPostId = post1.id, uiMode = CreatePostUiMode.COMMENT)
        val c2 = cp1.copy(cpId = 2 , parentPostId = post1.id, uiMode = CreatePostUiMode.REPLY) // will not affect post1 count

        val c3 = cp1.copy(cpId = 3 , parentPostId = post2.id, uiMode = CreatePostUiMode.COMMENT) // will not affect post1 count


        dao.replaceFirstPage(f1, listOf(post1, post2), followDao , TEST_CONTENT_URL)
        assertThat(commentCounts()).containsExactly(
                post1.id to null,
                post2.id to null
        )

        cpDao.replaceCP(c1)
        cpDao.replaceCP(c2)
        cpDao.replaceCP(c3)

        //count incremented only by 1
        assertThat(commentCounts()).containsExactly(
                post1.id to "1",
                post2.id to "1"
        )
    }


    @Test
    fun `test inserting repost increments count`() {

        val c1 = cp1.copy(cpId = 1 , parentPostId = post1.id, uiMode = CreatePostUiMode.REPOST)
        val c2 = cp1.copy(cpId = 2 , parentPostId = post1.id, uiMode = CreatePostUiMode.REPLY) // will not affect post1 count

        val c3 = cp1.copy(cpId = 3 , parentPostId = post2.id, uiMode = CreatePostUiMode.REPOST) // will not affect post1 count


        dao.replaceFirstPage(f1, listOf(post1, post2), followDao , TEST_CONTENT_URL)
        assertThat(repostCounts()).containsExactly(
                post1.id to null,
                post2.id to null
        )

        cpDao.replaceCP(c1)
        cpDao.replaceCP(c2)
        cpDao.replaceCP(c3)

        //count incremented only by 1
        assertThat(repostCounts()).containsExactly(
                post1.id to "1",
                post2.id to "1"
        )
    }


    @Test
    fun `test deleting comment modifies count`() {

        val c1 = cp1.copy(cpId = 1 , parentPostId = post1.id, uiMode = CreatePostUiMode.COMMENT, postId = "cp1")
        val c2 = cp1.copy(cpId = 2 , parentPostId = post1.id, uiMode = CreatePostUiMode.REPLY, postId = "cp2") // will not affect post1 count
        val c3 = cp1.copy(cpId = 3 , parentPostId = post2.id, uiMode = CreatePostUiMode.COMMENT, postId = "cp3") // will not affect post1 count
        cpDao.replaceCP(c1)
        cpDao.replaceCP(c2)
        cpDao.replaceCP(c3)

        val p1 = post1.copy(counts = Counts2(
            COMMENTS = EntityConfig2("2")
        ))
        dao.replaceFirstPage(f1, listOf(p1, post2), followDao , TEST_CONTENT_URL)
        assertThat(commentCounts()).containsExactly(
                post1.id to "2",
                post2.id to null
        )

        cpDao.deleteAll()
        assertThat(commentCounts()).containsExactly(
                post1.id to "1",
                post2.id to "0"
        )

        cpDao.replaceCP(c1)
        assertThat(commentCounts()).containsExactly(
                post1.id to "2",
                post2.id to "0"
        )

        cpDao.deleteByPostId(c1.postId)
        assertThat(commentCounts()).containsExactly(
                post1.id to "1",
                post2.id to "0"
        )

        cpDao.replaceCP(c3) // c3 is connected to post2
        assertThat(commentCounts()).containsExactly(
                post1.id to "1",
                post2.id to "1"
        )

        cpDao.delete(c3.cpId)
        assertThat(commentCounts()).containsExactly(
                post1.id to "1",
                post2.id to "0"
        )
    }

    @Test
    fun `test deleting repost modifies count`() {

        val c1 = cp1.copy(cpId = 1 , parentPostId = post1.id, uiMode = CreatePostUiMode.REPOST, postId = "cp1")
        val c2 = cp1.copy(cpId = 2 , parentPostId = post1.id, uiMode = CreatePostUiMode.REPLY, postId = "cp2") // will not affect post1 count
        val c3 = cp1.copy(cpId = 3 , parentPostId = post2.id, uiMode = CreatePostUiMode.REPOST, postId = "cp3") // will not affect post1 count
        cpDao.replaceCP(c1)
        cpDao.replaceCP(c2)
        cpDao.replaceCP(c3)

        val p1 = post1.copy(counts = Counts2(
            REPOST = EntityConfig2("2")
        ))
        dao.replaceFirstPage(f1, listOf(p1, post2), followDao , TEST_CONTENT_URL)
        assertThat(repostCounts()).containsExactly(
                post1.id to "2",
                post2.id to null
        )

        cpDao.deleteAll()
        assertThat(repostCounts()).containsExactly(
                post1.id to "1",
                post2.id to "0"
        )

        cpDao.replaceCP(c1)
        assertThat(repostCounts()).containsExactly(
                post1.id to "2",
                post2.id to "0"
        )

        cpDao.deleteByPostId(c1.postId)
        assertThat(repostCounts()).containsExactly(
                post1.id to "1",
                post2.id to "0"
        )

        cpDao.replaceCP(c3) // c3 is connected to post2
        assertThat(repostCounts()).containsExactly(
                post1.id to "1",
                post2.id to "1"
        )

        cpDao.delete(c3.cpId)
        assertThat(repostCounts()).containsExactly(
                post1.id to "1",
                post2.id to "0"
        )
    }

    @Test
    fun testDiscussionTrigger() {
        val p1_c1 = post1.copy("discussion-child-1")
        val p1_c2 = post1.copy("discussion-child-2")
        val p1 = post1.copy(id = "discussion-card-1")
        postDao.insReplace(p1,
                p1_c1.copy(level = PostEntityLevel.DISCUSSION),
                p1_c2.copy(level = PostEntityLevel.DISCUSSION))
        val allRelation = listOf(Discussions(p1.getUniqueId("")
                , p1_c1.getUniqueId(""), index = 0),
                Discussions(p1.getUniqueId(""), p1_c2.getUniqueId(""), index = 1))
        dao.insReplaceDiscussions(allRelation)
        val check1 = postDao.all().test()?.values()?.last()
        Truth.assertThat(check1?.map { it.i_id() }).containsExactly(p1.id, p1_c1.id, p1_c2.id)
        Truth.assertThat(dao.all()).hasSize(3)
        Truth.assertThat(dao.allDiscussionRelation()).containsExactly(
                allRelation[0],
                allRelation[1])
        postDao.deletePost(p1.id)
        Truth.assertThat(dao.all()).isEmpty()
        Truth.assertThat(dao.allDiscussionRelation()).isEmpty()
    }

    /**
     * https://bugzilla.newshunt.com/eterno/show_bug.cgi?id=29055
     * having data in db but because of trigger deleting parent post and adding duplicate
     * relation list size coming zero while fetching
     */
    @Test
    fun testDuplicateRelationDiscussionTrigger() {
        //Test on iserting duplicate entry in relation trigger should not delete original post
        val p1_c1 = post1.copy("discussion-child-1")
        val p1 = post1.copy(id = "discussion-card-1")
        postDao.insReplace(p1,
                p1_c1.copy(level = PostEntityLevel.DISCUSSION),
                p1_c1.copy(level = PostEntityLevel.DISCUSSION))
        val allRelation = listOf(
                Discussions(p1.getUniqueId(""), p1_c1.getUniqueId(""),index = 0),
                Discussions(p1.getUniqueId(""), p1_c1.getUniqueId(""),index = 1))
        dao.insReplaceDiscussions(allRelation)
        val check1 = postDao.all().test()?.values()?.last()
        Truth.assertThat(check1?.map { it.i_id() }).containsExactly(p1.id, p1_c1.id)
        Truth.assertThat(dao.all()).hasSize(2)
        Truth.assertThat(dao.allDiscussionRelation()).containsExactly(
                allRelation[0])
    }

}