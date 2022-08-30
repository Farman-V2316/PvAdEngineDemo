/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MediatorLiveData
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Correspondence
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.ColdStartEntity
import com.newshunt.dataentity.common.asset.ColdStartEntityItem
import com.newshunt.dataentity.common.asset.CollectionEntity
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.EntityConfig2
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.ItemToFilter
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.EntityPojo
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.social.entity.FetchDataEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dataentity.social.entity.Vote
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.daos.EntityInfoDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.HistoryDao
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.daos.VoteDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.ListDataSource
import com.newshunt.news.model.usecase.ReadCardsUsecase
import com.newshunt.news.model.usecase.readCardPageConfig
import com.newshunt.news.model.usecase.toDislikeEntity2
import io.mockk.MockKAnnotations
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.Executor

/**
 * @author satosh.dhanyamraju
 */
const val TEST_CONTENT_URL = "http://feed.dailyhunt.in/a?${Constants.URL_PARAM_ALLOW_LOCAL_CARD}=${Constants.YES}"
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class FetchDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var dao: FetchDao
    private lateinit var dislikeDao: DislikeDao
    private lateinit var followDao: FollowEntityDao
    private lateinit var voteDao: VoteDao
    private lateinit var pageEntityDao: PageEntityDao
    private lateinit var interactionDao: InteractionDao
    private lateinit var groupFeedDao: GeneralFeedDao
    private lateinit var entityInfoDao: EntityInfoDao
    private lateinit var postDao: PostDao
    private lateinit var historyDao: HistoryDao
    private lateinit var cpDao : CreatePostDao
    private lateinit var cardDao : CardDao

    val f1 = FetchInfoEntity("c1", "loc1", "np1", 0, "np10", section = "news")
    val ff1 = FetchInfoEntity("c1", "loc2", "np1", 0, "np10", section = "news")
    val f2 = FetchInfoEntity("c1", "loc1", "np2", 1, "np21", section = "news")
    val f3 = FetchInfoEntity("c1", "loc1", "np3", 2, "np32", section = "news")
    val f4 = FetchInfoEntity("c2", "locx", "np4", 0, "npx", section = "news")

    private val fetchdataCorrespondence = Correspondence.from({ fd1: FetchDataEntity?, fd2:
    FetchDataEntity? ->
        fd1?.fetchId == fd2?.fetchId &&
                fd1?.pageNum == fd2?.pageNum &&
                fd1?.indexInPage == fd2?.indexInPage &&
                fd1?.storyId == fd2?.storyId &&
                fd1?.format == fd2?.format &&
                fd1?.reqUrl == fd2?.reqUrl
    }, "fetchdata")


    @io.mockk.impl.annotations.MockK
    lateinit var pageEntity: PageEntity


    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        dao = instance.fetchDao()
        followDao = instance.followEntityDao()
        dislikeDao = instance.dislikeDao()
        voteDao = instance.voteDao()
        pageEntityDao = instance.pageEntityDao()
        interactionDao = instance.interactionsDao()
        groupFeedDao = instance.groupDao()
        entityInfoDao = instance.entityInfoDao()
        postDao = instance.postDao()
        historyDao = instance.historyDao()
        cpDao = instance.cpDao()
        cardDao = instance.cardDao()

        instance.pageEntityDao().insReplace(listOf(dummy, dummy.copy(pageEntity = dummy.pageEntity.copy(id = "c1")), dummySourceEntity))
        AppConfig.createInstance(AppConfigBuilder())
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testInsert() {
        var c: List<FetchInfoEntity> = listOf()
        dao.allFetchInfo().observeForever {
            c = it
        }
        assertThat(c).isEmpty()

        val fetchID = dao.insIgnore(f1)
        val fetchid2 = dao.insIgnore(f2)

        assertThat(c).hasSize(1)
        assertThat(c.get(0)).isEqualTo(f1.copy(fetchInfoId = fetchID))
        assertThat(c.get(0)).isNotEqualTo(f2.copy(fetchInfoId = fetchid2))
    }

    @Test
    fun testReplaceFP() {
        // insert fetch info
        // api call success
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(f2.copy(fetchInfoId = fetchId,
                nextPageUrl = f2.npUrlOf1stResponse))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData()))
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        FetchDataEntity(fetchId, f2.currentPageNum, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f2.currentPageNum, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL)
                )

        val values = itemsMatching(f1.entityId, f1.location, f1.section)
                .test()
                .assertValueCount(1)
                .values().get(0)
        assertThat(values)
                .containsExactly(post1.toCard(fetchId), post2.toCard(fetchId))
                .inOrder()
    }

    @Test
    fun testReplaceFPWithCollections() {
        // insert fetch info
        val f1d = dao.insIgnore(f1)
        // api call success
        dao.replaceFirstPage(f2, listOf(post1.copy(moreStories = listOf(post3)), post2), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(f2.copy(fetchInfoId = fetchId,
                nextPageUrl = f2.npUrlOf1stResponse))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData()))
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        FetchDataEntity(fetchId, f2.currentPageNum, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f2.currentPageNum, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL)
                )

        val values = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig())
                .test()
                .assertValueCount(1)
                .values().get(0)
        assertThat(values).hasSize(2)
        assertThat(values[0]!!.i_id()).isEqualTo(post1.id)
        assertThat(values[0]!!.i_moreStories()).hasSize(1)
        assertThat(values[0]!!.i_moreStories()?.get(0)?.i_id()).isEqualTo(post3.id)
        assertThat(values[1]!!.i_id()).isEqualTo(post2.id)
    }


    @Test
    fun testReplaceFP2() {
        // insert fetch info
        dao.insIgnore(f1)
        // api call success
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(f2.copy(fetchInfoId = fetchId,
                nextPageUrl = f2.npUrlOf1stResponse))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData()))
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        FetchDataEntity(fetchId, f2.currentPageNum, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f2.currentPageNum, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL)
                )

        val values = itemsMatching(f1.entityId, f1.location, f1.section).test()
                .assertValueCount(1)
                .values().get(0)
        assertThat(values)
                .containsExactly(post1.toCard(fetchId), post2.toCard(fetchId))
                .inOrder()
    }

    @Test
    fun testCleanUp() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f3, listOf(post3), followDao, TEST_CONTENT_URL)
        val fi = dao.fetchInfo(f1.entityId, f1.location, f1.section)!!
        assertThat(fi).isEqualTo(FetchInfoEntity(fi.entityId, fi.location, f3.nextPageUrl, f3.currentPageNum,
                f2.npUrlOf1stResponse, null, fetchId, fi.section))
        val values = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().assertValueCount(1).values().get(0)
        assertThat(values).hasSize(3)

        val ts: Long = System.currentTimeMillis()
        dao.cleanUpFetch(f1.entityId, f1.location, ts, f1.section)

        assertThat(dao.fetchInfo(f1.entityId, f1.location, f1.section)).isEqualTo(FetchInfoEntity(fi.entityId, fi.location, f2.npUrlOf1stResponse, f2.currentPageNum,
                f2.npUrlOf1stResponse, ts, fetchId, f1.section))
        val values1 = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().assertValueCount(1).values().get(0)
        assertThat(values1).hasSize(2)
    }

    @Test
    fun testappendNextPage() {

        // insert fetch info
        // api call success
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        // next page api call success
        dao.appendNextPage(f3, listOf(post3), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(f3.copy(fetchInfoId = fetchId, npUrlOf1stResponse = f2.npUrlOf1stResponse))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData()))
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        FetchDataEntity(fetchId, f2.currentPageNum, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f2.currentPageNum, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f3.currentPageNum, 0, post3.getUniqueId(fetchId), post3.format, TEST_CONTENT_URL)
                )

        val list = itemsMatching(f1.entityId, f1.location, f1.section).test()
                .assertValueCount(1)
                .values().get(0)
        assertThat(list)
                .containsExactly(post1.toCard(fetchId), post2.toCard(fetchId), post3.toCard(fetchId))
                .inOrder()
    }


    @Test
    fun testappendNextPageWithCollection() {

        // insert fetch info
        dao.insIgnore(f1)
        // api call success
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        // next page api call success
        dao.appendNextPage(f3, listOf(post3.copy(moreStories = listOf(post2))), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(f3.copy(fetchInfoId = fetchId, npUrlOf1stResponse = f2.npUrlOf1stResponse))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData()))
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        FetchDataEntity(fetchId, f2.currentPageNum, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f2.currentPageNum, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f3.currentPageNum, 0, post3.getUniqueId(fetchId), post3.format, TEST_CONTENT_URL)
                )

        val list = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test()
                .assertValueCount(1)
                .values().get(0)
        with(assertThat(list)) {
            hasSize(3)
            assertThat(list.get(2)!!.i_moreStories()?.get(0)?.i_id()).isEqualTo(post2.id)
            with(list.get(2)!!.i_id()) {
                assertThat(this).isEqualTo(post3.id)
            }
        }
    }

    @Test
    fun testappendNextPage2() {

        // insert fetch info
        val fid = dao.insIgnore(f1)
        // api call success
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        // next page api call success
        dao.appendNextPage(f3, listOf(post3), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(f3.copy(npUrlOf1stResponse = f2.npUrlOf1stResponse, fetchInfoId = fid))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData()))
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        FetchDataEntity(fetchId, f2.currentPageNum, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f2.currentPageNum, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, f3.currentPageNum, 0, post3.getUniqueId(fetchId), post3.format, TEST_CONTENT_URL)
                )

        val list = itemsMatching(f1.entityId, f1.location, f1.section).test()
                .assertValueCount(1)
                .values().get(0)
        assertThat(list)
                .containsExactly(post1.toCard(fetchId), post2.toCard(fetchId), post3.toCard(fetchId))
                .inOrder()
    }


    private fun insertPostWith3MoreStories(): Quadraple<String, String, String, String> {
        dao.insIgnore(f1)
        val id = "p"
        val id1 = "c"
        val id2 = "c1"
        val id3 = "c3"
        val p1 = PostEntity(id = id, level = PostEntityLevel.TOP_LEVEL,
                    moreStories = listOf(PostEntity(id = id1), PostEntity(id = id2), PostEntity(id = id3)))
        dao.replaceFirstPage(f1, listOf(p1), followDao, TEST_CONTENT_URL)
        return Quadraple(id, id1, id2, id3)
    }



    @Test
    fun testFPwithRelationships() {
        val p= post1.moreNews("m1", "m2")
                .associations("a1")
                .collectionitems("c1", "c2")

        val fetchId = dao.insIgnore(f1)
        val obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        dao.replaceFirstPage(f2, listOf(p), followDao, TEST_CONTENT_URL)

        assertThat(obs.values().get(0)).isEqualTo(pagedListOf<TopLevelCard>(emptyList()))
        assertThat(obs.values().get(1)).isEqualTo(pagedListOf(listOf(p.toCard(fetchId)!!)))
    }

    @Test
    fun testNPwithRelationships() {
        val fetchId = dao.insIgnore(f1)
        val p1 = post1.moreNews("m1", "m2")
                .associations("a1")
                .collectionitems("c1", "c2")
        val p2 = post2.moreNews("m3", "m4")
                .associations("a2")
                .collectionitems("c3", "c4")
        val obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        dao.replaceFirstPage(f2, listOf(p1), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f3, listOf(p2), followDao, TEST_CONTENT_URL)

        obs.assertValues(pagedListOf(emptyList()), pagedListOf(listOf(p1.toCard(fetchId)!!)),
                pagedListOf(listOf(p1.toCard(fetchId)!!, p2.toCard(fetchId)!!)))
    }

    @Test
    @Ignore("test")
    fun testRelationshipInsertionAndDeletion() {
        TODO("all 1 to many types")
    }


    @Test
    @Ignore("test")
    fun testDeleteRelationships() {
       TODO()
    }



    @Test
    @Ignore("test")
    fun testInsAsociatedContents_positiveFlow() {
        TODO()
    }

    @Test
    fun testDeletionOfChildrenFromPostsTableThroughTrigger() {
        val (pid, m1, m2, m3) = insertPostWith3MoreStories()
        assertThat(LiveDataTestUtil.getValue(postDao.all())!!.map { it.i_id() })
                .containsExactly(pid)

        postDao.deletePost(pid)

        assertThat(LiveDataTestUtil.getValue(postDao.all())!!.map { it.i_id() }).isEmpty()
    }

    @Test
    fun testReadFetchInfo() {
        val f1 = FetchInfoEntity("c", "l", "", 0, "0", section = "news")
        val fetchId = dao.insIgnore(f1)
        dao.update(f1.copy(nextPageUrl = "np1", currentPageNum = 2, fetchInfoId = fetchId))

        val f2 = dao.fetchInfo(f1.entityId, f1.location, f1.section) ?: throw Throwable("no data")

        assertThat(f2.entityId).isEqualTo(f1.entityId)
        assertThat(f2.location).isEqualTo(f1.location)
        assertThat(f2.nextPageUrl).isEqualTo("np1")
        assertThat(f2.currentPageNum).isEqualTo(2)
    }

    @Test
    fun testDislikeStoriesNotIncluded() {
        dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!

        dislikeDao.insReplace(post1.toDislikeEntity2(System.currentTimeMillis()))

        val l = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test()
                .assertValueCount(1)
                .values().get(0)

        assertThat(l).comparingElementsUsing(storyIdAndGroupType)
                .containsExactly(post2)
    }


    private fun followEntity(id: String, type: String, isFollowing: Boolean): FollowSyncEntity {
        return FollowSyncEntity(ActionableEntity(id, type, null, displayName = "n", entityImageUrl = "", handle = "", deeplinkUrl = "", iconUrl = ""), if (isFollowing) FollowActionType.FOLLOW else FollowActionType.BLOCK)
    }

    @Test
    @Ignore("calculated field are not queried")
    fun testFollowingField() {
        dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)
        followDao.insReplace(followEntity(post1.source?.id
                ?: "", post1.source?.entityType!!, true))
        followDao.insReplace(followEntity(post2.source?.id
                ?: "", post2.source?.entityType!!, false))

        val obs = itemsMatching(f1.entityId, f1.location, f1.section).test()

        obs.assertValueCount(2) // 1 for dao, 1 for followDao
        val l0 = obs.values().get(0).filterIsInstance<CommonAsset>()
        val l1 = obs.values().get(1).filterIsInstance<CommonAsset>()

        assertThat(l0.map { it.i_isFollowin() }).containsExactly(false, false, false)
        assertThat(l1.map { it.i_isFollowin() }).containsExactly(true, false, false)
    }

    @Test
    @Ignore("calculated field are not queried")
    fun testlikeTypeField() {
        dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)
        interactionDao.toggleLike(post1.id, "post", "HAPPY")
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!

        val liketypes = itemsMatching(f1.entityId, f1.location, f1.section).test().values().last().filterIsInstance<TopLevelCard>().map { it.i_selectedLikeType() }

        assertThat(liketypes).containsExactly("HAPPY", null, null)
    }

    @Test
    @Ignore("calculated field are not queried")
    fun testPollOptionField() {
        dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)
        val vote = Vote("user1", post2.id, "o2")
        voteDao.insReplace(vote)
        val fetchId = dao.fetchInfo(f1.entityId, f1.location, f1.section)?.fetchInfoId!!
        val cards = itemsMatching(f1.entityId, f1.location, f1.section).test().values().last()
                .filterIsInstance<CommonAsset>()
        assertThat(cards.find { it.i_id() == post1.i_id() }?.i_pollSelectedOptionId()).isNull()
        assertThat(cards.find { it.i_id() == post2.i_id() }?.i_pollSelectedOptionId()).isEqualTo(vote.optionId)
        assertThat(cards.find { it.i_id() == post3.i_id() }?.i_pollSelectedOptionId()).isNull()
    }

    @Test
    fun testUpdateFPFetchInfo() {
        dao.insIgnore(f1)

        dao.updateFpFetchInfo(f1.entityId, f1.location, "pp", "100", section = f1.section)

        with(dao.fetchInfo(f1.entityId, f1.location, f1.section)) {
            this ?: throw java.lang.Exception("non null value")
            assertThat(npUrlOf1stResponse).isEqualTo("pp")
            assertThat(nextPageUrl).isEqualTo("pp")
            assertThat(currentPageNum).isEqualTo(100)
        }
    }

    @Test
    fun test_itemsMatching_returns_only_rows_matching_current_url() {
        val anotherUrl = "http://nothing"
        val groupFeed = GeneralFeed("id22", "curl22", "GET")
        groupFeedDao.insReplace(groupFeed)
        val fetch = FetchInfoEntity(groupFeed.id, "list", section = groupFeed.section)

        val observer = dao.itemsMatching(fetch.entityId, fetch.location, fetch.section).toLiveData(readCardPageConfig()).test() // one empty event

        dao.replaceFirstPage(fetch, listOf(post1, post2), followDao, groupFeed.contentUrl) // 2 rows

        groupFeedDao.insReplace(groupFeed.copy(contentUrl = anotherUrl)) // 0 rows

        dao.replaceFirstPage(fetch, listOf(post3), followDao, anotherUrl) // 1 row

        groupFeedDao.insReplace(groupFeed)//0
        dao.replaceFirstPage(fetch, listOf(post3), followDao, groupFeed.contentUrl) // 1 row

        val p = observer.values().map { it.map { it.i_id() } }
        assertThat(p)
                .containsExactly(
                        listOf<String>(),
                        listOf(post1.id, post2.id),
                        emptyList<String>(),
                        listOf(post3.id),
                        listOf(post1.id, post2.id),
                        listOf(post3.id)
                )
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())!!.size).isEqualTo(2)
    }

    @Test
    fun testUpdateNPFetchInfo() {
        dao.insIgnore(f1)

        dao.updateNpFetchInfo(f1.entityId, f1.location, "pp", "100", section = "news")

        with(dao.fetchInfo(f1.entityId, f1.location, f1.section)) {
            this ?: throw java.lang.Exception("non null value")
            assertThat(nextPageUrl).isEqualTo("pp")
            assertThat(currentPageNum).isEqualTo(100)
        }
    }

    @Test
    fun testDeleteFetchDataMatching() {
        val fetchId = dao.insIgnore(f1)
        val anotherId = f1.entityId + "..."
        pageEntityDao.insReplace(listOf(dummy, dummy.copy(pageEntity = dummy.pageEntity.copy(id = anotherId))))
        val fetchId2 = dao.insIgnore(f1.copy(entityId = anotherId))
        dao.insReplacePosts(listOf(post1.toCard2(), post2.toCard2()))
        val fd1 = FetchDataEntity(fetchId, 0, 0, post1.id, post1.format, TEST_CONTENT_URL)
        val fd2 = FetchDataEntity(fetchId2, 1, 1, post2.id, post2.format, TEST_CONTENT_URL)
        dao.insReplaceFetchData(listOf(fd1, fd2))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())).containsExactly(fd1, fd2)


        dao.deleteFetchDataMatching(fetchId, TEST_CONTENT_URL)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())).containsExactly(fd2)
    }

    @Test
    fun testContentUrlForPage() {
        val pageId = "id22"
        val curl = "curl22"
        pageEntityDao.insReplace(listOf(dummy.copy(pageEntity = dummy.pageEntity.copy(id = pageId, contentUrl = curl))))
        assertThat(dao.contentUrlForPage(pageId)).isEqualTo(curl)
    }

    @Test
    fun testContentUrlForGroup() {
        val pageId = "id22"
        val curl = "curl22"
        groupFeedDao.insReplace(GeneralFeed(pageId, curl, "GET"))
        assertThat(dao.contentUrlForPage(pageId)).isEqualTo(curl)
    }

    @Test
    fun testContentUrlForEntityInfo() {
        val pageId = "id22"
        val curl = "curl22"
        val pe = PageEntity(
                pageId,
                null,
                null,
                "oe",
                null,
                null,
                curl,
                curl,
                null,
                null,
                null,
                null,
            null,
                null,
                null,
                null,
                contentRequestMethod = "GET",
                appIndexDescription = ""
        )
        entityInfoDao.insReplace(EntityPojo(pe, parentId = "pid", section = "news"))
        assertThat(dao.contentUrlForPage(pageId)).isEqualTo(curl)
    }

    @Test
    fun testPageNumbers() {
        val fetchId = dao.insIgnore(f1)
        dao.insReplacePosts(listOf(post1.toCard2()))
        dao.insReplaceFetchData(listOf(FetchDataEntity(fetchId, 20, 0, "0", Format.HTML, TEST_CONTENT_URL)))

        assertThat(dao.maxPageNumInFetchData(fetchId)).isEqualTo(21)
    }



    @Test
    fun testCloneFetchInfo() {
        val fetchId = dao.insIgnore(f1)
        val newloc = "detail"

        val newFetchId = dao.cloneFetchInfoForLocation(fetchId, newloc, "news")

        val list = LiveDataTestUtil.getValue(dao.allFetchInfo())!!
        assertThat(list).containsExactly(f1.copy(fetchInfoId = fetchId), f1.copy(fetchInfoId = newFetchId, location = newloc)).inOrder()
    }

    @Test
    fun testCloneFetchData() {
        val fetchId = dao.insIgnore(f1)
        val fd1 = FetchDataEntity(fetchId, 0, 0, "22", Format.HTML, TEST_CONTENT_URL)
        dao.insReplaceFetchData(listOf(fd1))
        val newloc = "detail"
        val newFetchId = dao.cloneFetchInfoForLocation(fetchId, newloc, "news")

        dao.cloneFetchDataForDiffFetchId(newFetchId, fetchId)

        val list = LiveDataTestUtil.getValue(dao.allFetchData())!!
        assertThat(list).containsExactly(
                fd1,
                fd1.copy(fetchId = newFetchId)
        ).inOrder()
    }

    @Test
    fun testCloneFetch() {
        val fetchId = dao.insIgnore(f1)
        val fd1 = FetchDataEntity(fetchId, 0, 0, "22", Format.HTML, TEST_CONTENT_URL)
        dao.insReplaceFetchData(listOf(fd1))
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())!!.size).isEqualTo(1)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())!!.size).isEqualTo(1)
        dao.cloneFetchForLocation(fetchId, "detail", "news")

        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())!!.size).isEqualTo(2)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())!!.size).isEqualTo(2)

    }


    @Test
    fun testCloneInsertReplace() {
        val f = f1.copy(location = "detail")
        val fetchId = dao.insIgnore(f)
        val fd1 = FetchDataEntity(fetchId, 0, 0, "22", Format.HTML, TEST_CONTENT_URL)
        dao.insReplaceFetchData(listOf(fd1))

        dao.cloneFetchForLocation(fetchId, f.location, f.section) // re-insert the same row

        // insert succes
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())!!.size).isEqualTo(1)
        // due to ondelete cascade, fetchdata would be deleted
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())!!.size).isEqualTo(0)
    }

    @Test
    fun testFullCleanup() {
        dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f3, listOf(post2), followDao, TEST_CONTENT_URL)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())!!.size).isEqualTo(1)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())!!.size).isEqualTo(2)

        dao.fullCleanupFetch(f1.entityId, f1.location, f1.section)

        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())!!.size).isEqualTo(0)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())!!.size).isEqualTo(0)
    }

    @Test
    fun testCardCountIs0Initially() {
        val fetchId = dao.insIgnore(f1)
        assertThat(dao.cardCount(fetchId)).isEqualTo(0)
    }

    @Test
    fun testCardCountIsIncremented() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f2, listOf(post2, post3), followDao, TEST_CONTENT_URL)
        assertThat(dao.cardCount(fetchId)).isEqualTo(3)
    }

    @Test
    fun testUpdaten2ndChunkIsQueriedInCard() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1), followDao, TEST_CONTENT_URL)
        val itemsMatching = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test()
        val content2 = "this is content2 from server"
        val newpost = post1.copy(content2 = content2)
        postDao.updatePost(newpost)
        assertThat(itemsMatching.values().flatMap { it.map { it.i_content2() } })
                .containsExactly((null), (content2))
    }

    @Test
    @Ignore("test")
    fun testInsertAndRetrieveCollections() {

    }

    @Test
    fun testInsertAndRetrieveCollectionsNP() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post3), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f3, listOf(post1c2), followDao, TEST_CONTENT_URL)
        val itemsMatching = itemsMatching(f1.entityId, f1.location, f1.section).test()

        itemsMatching.assertValues(
                pagedListOf(listOf(post3.toCard(fetchId)!!, post1c2.toCard(fetchId)!!))
        )

//        val map = itemsMatching.values().last().map { it.multiMediaCollectionList?.map { it.postEntity } }
//        assertThat(map).containsExactly(listOf<PostEntity>(), post1c2.collectionAsset!!.collectionItem?.map { AllLevelCards(it.copy(level = PostEntityLevel.COLLECTION)) })
    }


    @Test
    @Ignore("calculated field are not queried")
    fun testCollectionChildCalculatedFields() {

    }


    @Test
    fun testStoreRetireveAllLevels() {
        val i1 = "p1i"
        val i2 = "p2i"
        val i3 = "p3i"
        val post = PostEntity(id = i1,
                type = "news",
                format = Format.HTML,
                subFormat = SubFormat.ENTITY,
                uiType = UiType2.NORMAL,
                title = "title1",
                source = PostSourceAsset("times", type = "src", entityType = "src"),
                moreStories = listOf(
                        PostEntity(id = i2,
                                type = "news",
                                title = "title2",
                                source = PostSourceAsset("times2", type = "src", entityType = "src"))
                ),
                collectionAsset = CollectionEntity(
                        listOf(
                                PostEntity(id = i3, type = "photo", title = "title3")
                        )
                )
        )
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post), followDao, TEST_CONTENT_URL)

        val itemsMatching = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().values().first()
        assertThat(itemsMatching).hasSize(1)
        val card = itemsMatching.first()
        val more = card.i_moreStories()?.first()
        val col = card.i_collectionItems()?.first()
        assertThat(card.i_id()).isEqualTo(i1)
        assertThat(more?.i_id()).isEqualTo(i2)
        assertThat(col?.i_id()).isEqualTo(i3)

    }


    @Test
    fun testUpdateCounts() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)
        val observer = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test()
        val cfg = EntityConfig2("42", 11L)
        postDao.updateCount(post1.id, Counts2(TOTAL_LIKE = cfg))
        val values = observer.values()
        val counts = values.flatMap {
            it.map { it.i_counts() }
        }.map { it?.TOTAL_LIKE }

        assertThat(counts)
                .containsExactly(null, cfg).inOrder()
    }

    @Test
    fun testDeletingFetchDataWillCauseDeletionOfPosts() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f3, listOf(post3), followDao, TEST_CONTENT_URL)
        assertThat(dao.all()).hasSize(2)
        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())).hasSize(2)

        dao.cleanUpFetch(f1.entityId, f1.location, section = f1.section)

        assertThat(LiveDataTestUtil.getValue(dao.allFetchData())).hasSize(1)
        assertThat(dao.all().map { it.i_id() }).containsExactly(post1.id)
    }

    @Test
    fun testDeletingFetchDataShouldBeDoneBeforeInsertingPosts() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)

        dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().assertValueCount(1)
    }

    @Test
    fun testFetchDataById() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)

        assertThat(dao.fetchDataByPostId(post1.id, fetchId)[0].fetchId).isEqualTo(fetchId)
        assertThat(dao.fetchDataByPostId(post1.id, fetchId)[0].storyId).isEqualTo(post1.getUniqueId(fetchId))
    }

    @Test
    fun testUpdateIndices() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)
        assertThat(dao.fetchDataByPostId(post1.id, fetchId)[0].indexInPage).isEqualTo(0)

        dao.updatePageIndexBy(fetchId, f1.currentPageNum, 1, 2)

        assertThat(dao.fetchDataByPostId(post1.id, fetchId)[0].indexInPage).isEqualTo(0)
        assertThat(dao.fetchDataByPostId(post2.id, fetchId)[0].indexInPage).isEqualTo(1)
        assertThat(dao.fetchDataByPostId(post3.id, fetchId)[0].indexInPage).isEqualTo(4)
    }

    @Test
    fun testInsertAds() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)
        assertThat(dao.cardCount(fetchId)).isEqualTo(3)

        dao.insertAdInDB(ad1.toCard2(), post1.id, System.currentTimeMillis(), f1.entityId, f1.location, f1.section)
        assertThat(dao.cardCount(fetchId)).isEqualTo(4)

        val itemsMatching = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().values().first()
        assertThat(itemsMatching[1]!!.i_id()).isEqualTo(ad1.id)
    }

    @Test
    fun testClearAds() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, ad1, post2, ad2), followDao, TEST_CONTENT_URL)

        dao.removeAd(ad1.id, f1.entityId, f1.location, f1.section)
        val fetchdata = LiveDataTestUtil.getValue(dao.allFetchData())
        assertThat(fetchdata?.map { it.storyId }).containsExactly(
                post1.getUniqueId(fetchId),
                post2.getUniqueId(fetchId),
                ad2.getUniqueId(fetchId)
        ).inOrder()

        dao.clearAds(f1.entityId, f1.location, f1.section)
        assertThat(dao.cardCount(fetchId)).isEqualTo(2)

        val itemsMatching = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().values().first()
        itemsMatching.forEach {
            assertThat(it.i_format()).isNotEqualTo(Format.AD)
        }
    }

    @Test
    fun testDeleteAllAds() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, ad1, post2, ad2), followDao, TEST_CONTENT_URL)

        dao.deleteAllAds()
        val fetchdata = LiveDataTestUtil.getValue(dao.allFetchData())
        assertThat(fetchdata?.map { it.storyId }).containsExactly(
                post1.getUniqueId(fetchId),
                post2.getUniqueId(fetchId)
        ).inOrder()
    }

    @Test
    fun testAdInsertion() {
        val fetchid = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        dao.insertAdInDB(adpost.toCard2(), post1.id, System.currentTimeMillis(), f2.entityId, f2.location, f2.section)

        val last = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().values().last()
        assertThat(last.map { it.i_id() }).containsExactly(post1.id, adpost.id, post2.id).inOrder()

        val fetchdata = LiveDataTestUtil.getValue(dao.allFetchData())
        assertThat(fetchdata?.map { it.indexInPage }).containsExactly(0, 1, 2)

    }

    @Test
    fun testAdReplace() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, ad1, post2), followDao, TEST_CONTENT_URL)

        dao.replaceAd(ad2, ad1.id, f1.entityId, f1.location, f1.section)

        val fetchdata = LiveDataTestUtil.getValue(dao.allFetchData())

        // Indices to be same as before
        assertThat(fetchdata?.map { it.indexInPage }).containsExactly(0, 1, 2)

        //Ids to have ad2 instead of ad1
        assertThat(fetchdata?.map { it.storyId }).containsExactly(post1.getUniqueId(fetchId), ad2.id, post2.getUniqueId(fetchId))
    }

    val fs1 = FollowSyncEntity(ActionableEntity("e1", "SOURCE"), FollowActionType.FOLLOW)
    val fs2 = FollowSyncEntity(ActionableEntity("e2", "HASHTAG", "TOPIC"), FollowActionType.FOLLOW)

    @Test
    fun testColdStartInsert() {
        followDao.toggleFollowItems(fs1)

        val coldstart = post1.copy(id = "e1", coldStartAsset = ColdStartEntity(
                id = "e1",
                itemToFillFilter = ItemToFilter(
                        listOf("SOURCE"),
                        emptyList(),
                        FollowActionType.FOLLOW.name
                )
        ))

        dao.replaceFirstPage(f1, listOf(coldstart), followDao, TEST_CONTENT_URL)
        val v = dao.itemsMatchingLiveList(f1.entityId, f1.location, f1.section).latestValue()
        assertThat(v).hasSize(1)
        assertThat(v?.get(0)?.i_entityCollection()).containsExactly(
                ColdStartEntityItem("e1", "SOURCE"/*, isCreatorCard = false*/)
        )
    }

    @Test
    fun testgetItemsToFillFilter() {
        followDao.toggleFollowItems(fs1)
        followDao.toggleFollowItems(fs2)

        assertThat(followDao.getItemsToFillFilter(fs1.action.name, listOf("SRC"), emptyList()).map {
            it.actionableEntity.entityId
        }
        ).isEmpty()


        assertThat(followDao.getItemsToFillFilter(fs1.action.name, listOf("SOURCE"), emptyList()).map {
            it.actionableEntity.entityId
        }).containsExactly("e1")


        assertThat(followDao.getItemsToFillFilter(fs1.action.name, listOf("HASHTAG"), listOf("TOPIC")).map {
            it.actionableEntity.entityId
        }).containsExactly("e2")
    }

    @Test
    @Ignore("calculated field are not queried")
    fun testReadStatus() {
        val fetchid = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        historyDao.insertIgnore(HistoryEntity(post1.id, post1.format, post1.subFormat, null, null, null, null, null, Date(), null, null))
        val cards = itemsMatching(f1.entityId, f1.location, f1.section)
                .test().values().last().filterIsInstance<TopLevelCard>()

        assertThat(cards.map { it.i_id() to it.i_isRead() })
                .containsExactly(post1.id to true , post2.id to false)
    }

    @Test
    fun testPageWithID() {
        pageEntityDao.insReplace(dummy)

        dao.pageWithId(dummy.pageEntity.id).test().assertValue(listOf(dummy.pageEntity.id))
        dao.pageWithId("1").test().assertValue(emptyList())
    }

    @Test
    fun testPaginationTerminated() {
        val f1id = dao.insIgnore(f1)
        val fn = f1.copy(section = "buzz")
        val f2id = dao.insIgnore(fn)

        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(
                f1.copy(fetchInfoId = f1id),
                fn.copy(fetchInfoId = f2id))

        dao.paginationTerminated(fn.entityId, fn.location, fn.section)

        assertThat(LiveDataTestUtil.getValue(dao.allFetchInfo())).containsExactly(
                f1.copy(fetchInfoId = f1id),
                fn.copy(fetchInfoId = f2id, nextPageUrl = null))
    }


    @Test
    fun testreactivestream() {
        val medatorLD = MediatorLiveData<String>()
        val mockobs = Observable.create<String> {
            it.onNext("ab")
            Thread.sleep(500L)
            it.onNext("abc")
            it.onComplete()
        }

        mockobs.test().assertResult("ab", "abc")

        val obstld = LiveDataReactiveStreams.fromPublisher(mockobs.toFlowable(BackpressureStrategy.BUFFER))

        val fetchid = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1), followDao, "")
//        dao.itemsMatching(f1.entityId, f1.location, f1.section).subscribeOn(Schedulers.io()).subscribe {
//            println("callback comes in \'${Thread.currentThread().name}\'")
//        }
    }

    @Test
    fun testDedupesAcrossPages() {
        val f = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1), followDao, TEST_CONTENT_URL)
        dao.appendNextPage(f1, listOf(post2, post1), followDao, TEST_CONTENT_URL)
        dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test().assertValueAt(0) {
            it.map { it.i_id()  } == listOf(post1.id, post2.id)
        }
    }

    val li1 = LocalInfo(
            20,
            "UPLOADING",
            "a",
            "loc",
            "news"

    )

    val li2 = LocalInfo(
            100,
            "SUCCESS",
            "a",
            "loc",
            "news"
    )

    @Test
    @Ignore
    fun `test updating count from query gives response faster than that of through trigger`() {
        TODO("not implemented")
    }



    @Test
    @Ignore
    fun `test nlfc insertion foryou`() {
        TODO("re think api interfaces. how can replace fp use it as well")
    }

    @Test
    @Ignore
    fun `test nlfc insertion myposts`() {
        TODO("re think api interfaces. how can replace fp use it as well")
    }

    @Test
    fun `test replaceFP does not insert local cards to posts table`() {
        val p2 = post2.copy(level = PostEntityLevel.LOCAL, localInfo = li1)
        dao.replaceFirstPage(f1, listOf(post1, p2), followDao, TEST_CONTENT_URL)
        assertThat(dao.all().map{it.i_id()}).containsExactly(post1.id) // not post2.id
    }

    @Test
    fun `test replaceFPLocal MyPosts inserts localCards at the top`() {
        dao.insertLocalPost(listOf(post2.copy(
                localInfo = LocalInfo(
                        50,
                        PostUploadStatus.UPLOADING.name,
                        "",
                        creationDate = System.currentTimeMillis()
                )
        )),followDao)
        dao.replaceFirstPageWithLocalCards(f1, listOf(post1), followDao, TEST_CONTENT_URL, null , false, true)

        val map = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_id()
        } ?:throw Throwable("should get non null list")

        assertThat(map).containsExactly(post2.id, post1.id).inOrder()
    }

    @Test
    fun `test replaceFPLocal MyPosts inserts localCards not expired regardless of upload state`() {
        dao.insertLocalPost(listOf(post2.copy(
                localInfo = LocalInfo(
                        50,
                        PostUploadStatus.UPLOADING.name,
                        "",
                        creationDate = System.currentTimeMillis()
                )
        ), post3.copy(
                localInfo = LocalInfo(
                        100,
                        PostUploadStatus.SUCCESS.name,
                        "",
                        creationDate = System.currentTimeMillis()
                )
        )), followDao)
        dao.replaceFirstPageWithLocalCards(f1, listOf(post1), followDao, TEST_CONTENT_URL, null , false, true)

        val map = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_id()
        } ?:throw Throwable("should get non null list")

        assertThat(map).containsExactly(post2.id, post3.id, post1.id).inOrder()
    }


    @Test
    fun `test replaceFPLocal MyPosts dedups localCards if present in server response`() {
        dao.insertLocalPost(listOf(post2.copy(
                localInfo = LocalInfo(
                        50,
                        PostUploadStatus.UPLOADING.name,
                        "",
                        creationDate = System.currentTimeMillis()
                )
        ), post3.copy(
                localInfo = LocalInfo(
                        100,
                        PostUploadStatus.SUCCESS.name,
                        "",
                        creationDate = System.currentTimeMillis()
                )
        )), followDao)
        dao.replaceFirstPageWithLocalCards(f1, listOf(post1, post2), followDao, TEST_CONTENT_URL, null , false, true)

        val map = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_id()
        } ?:throw Throwable("should get non null list")

        assertThat(map).containsExactly(post3.id, post1.id, post2.id).inOrder()
    }

    @Test
    fun `test replaceFPLocal Foryou inserts localCards at the top`() {
        dao.insertLocalPost(listOf(post1.copy(localInfo = LocalInfo(creationDate = 5L)), post2.copy(localInfo = li2.copy(creationDate = 10L))),followDao)
        assertThat(dao.getLocalCardsForForyou()).hasSize(2)

        dao.replaceFirstPageWithLocalCards(f1, listOf(post3), followDao, TEST_CONTENT_URL, null , true, false)
        val map = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_id()
        } ?:throw Throwable("should get non null list")

        assertThat(map).containsExactly(post2.id, post1.id, post3.id).inOrder()
        assertThat(dao.getLocalCardsForForyou()).isEmpty()
    }


    @Test
    fun `test replaceFPLocal Foryou inserts localCards only if not prsent in the response `() {
        dao.insertLocalPost(listOf(post1, post2.copy(localInfo = li2.copy(creationDate = 10L))),followDao)
        assertThat(dao.getLocalCardsForForyou()).hasSize(2)

        dao.replaceFirstPageWithLocalCards(f1, listOf(post1, post3), followDao, TEST_CONTENT_URL, null , true, false)
        val map = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_id()
        } ?:throw Throwable("should get non null list")

        assertThat(map).containsExactly(post2.id, post1.id, post3.id).inOrder()
        assertThat(dao.getLocalCardsForForyou()).isEmpty()
    }

    @Test
    fun `test saving and retrieving local info`() {
        val localPost = post1.copy(localInfo = li1)
        dao.replaceFirstPage(f1, listOf(localPost), followDao, TEST_CONTENT_URL)

        assertThat(dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_localInfo()
        }).containsExactly(li1)
    }

    @Test
    fun `test localCards for foryou`() {
        val p1 = post1.copy(id= "p11", localInfo = li1.copy(creationDate = 10))
        val p2 = post2.copy(id= "p12", localInfo = li2.copy(shownInForyou = true, creationDate = 15)) // will not be queried
        val p3 = post2.copy(id= "p13", localInfo = li2.copy(shownInForyou = false, creationDate = 20))
        dao.insertLocalPost(listOf(p1,p2, p3), followDao)
        assertThat(dao.getLocalCardsForForyou().toIdLevelAndLocal())
                .containsExactly(Triple(p3.id, PostEntityLevel.LOCAL, p3.localInfo.fetchedFormServ(false)),
                        Triple(p1.id, PostEntityLevel.LOCAL, p1.localInfo.fetchedFormServ(false)))
                .inOrder() // reverse order; latest first
    }


    @Test
    fun `test insertPendingLocalCardsForyou inserts at the top`() {
        val l1 = post1.copy(id= "localcard1", localInfo = li1.copy(creationDate = 10, shownInForyou = false), level = PostEntityLevel.LOCAL)
        val l2 = post1.copy(id= "localcard2", localInfo = li1.copy(creationDate = 11, shownInForyou = false), level = PostEntityLevel.LOCAL)
        val l3 = post1.copy(id= "localcard3", localInfo = li2.copy(shownInForyou = false, creationDate = 20), level = PostEntityLevel.LOCAL)

        val posts = listOf(l1, l2, l3)
        dao.insertLocalPost(posts,followDao)
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, l3), followDao, TEST_CONTENT_URL)

        val fdpost1 = FetchDataEntity(fetchId, 0, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL)
        val fdl3 = FetchDataEntity(fetchId, 0, 1, l3.getUniqueId(fetchId), l3.format, TEST_CONTENT_URL)

        assertThat(dao.allFetchData().latestValue())
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(fdpost1, fdl3)
                .inOrder()


        dao.insertLocalFetchDataAtTopOrInTheMiddle(f1.entityId, f1.location, f1.section, listOf(l1, l2))


        assertThat(dao.allFetchData().latestValue())
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(
                        // latest first
                        FetchDataEntity(fetchId, 0, 0, l2.getUniqueId(""), l2.format, TEST_CONTENT_URL),
                        FetchDataEntity(fetchId, 0, 1, l1.getUniqueId(""), l1.format, TEST_CONTENT_URL),
                        fdpost1.copy(indexInPage = 2),
                        fdl3.copy(indexInPage = 3)
                )
    }

    @Test
    fun `test getLocalCardsForForyouNotPresentIn`() {
        val p1 = post1.copy(id = "p11", localInfo = li1.copy(creationDate = 10), level = PostEntityLevel.LOCAL)
        val p2 = post2.copy(id = "p12", localInfo = li2.copy(shownInForyou = true, creationDate = 15), level = PostEntityLevel.LOCAL) // will not be queried
        val p3 = post2.copy(id = "p13", localInfo = li2.copy(shownInForyou = false, creationDate = 20), level = PostEntityLevel.LOCAL) // will be inserted (as normal card)
        dao.insertLocalPost(listOf(p1, p2, p3), followDao)
        dao.replaceFirstPage(f1, listOf(p3), followDao, TEST_CONTENT_URL)

        assertThat(dao.getLocalCardsForForyouNotPresentIn(f1.entityId, f1.location, f1.section)
                .toIdLevelAndLocal())
                .containsExactly(Triple(p1.id, PostEntityLevel.LOCAL, p1.localInfo.fetchedFormServ(false)))
    }

    private fun LocalInfo?.fetchedFormServ(v : Boolean?)= this?.copy(fetchedFromServer = v)

    @Test
    fun `test mark as shown in foryou`() {
        val p1 = post1.copy(localInfo = li1)
        val p2 = post2.copy(localInfo = li2.copy(shownInForyou = true))
        dao.insertLocalPost(listOf(p1,p2), followDao)
        dao.markAsShownInForyou(listOf(p1.id))
        assertThat(dao.getLocalCardsForForyou()).isEmpty()
    }

    @Test
    fun `test localCards for myposts`() {
        val creationDate = System.currentTimeMillis()
        val l1 = li1.copy(creationDate = creationDate)
        val p1 = post1.copy(localInfo = l1)
        val l2 = li2.copy(status = PostUploadStatus.SUCCESS.name, creationDate = creationDate)
        val p2 = post2.copy(localInfo = l2)
        dao.insertLocalPost(listOf(p1, p2), followDao)
        assertThat(dao.getLocalCardsForMyPosts(creationDate - 10).toIdLevelAndLocal())
                .containsExactly(Triple(p1.id, PostEntityLevel.LOCAL, l1.fetchedFormServ(false)),
                        Triple(p2.id, PostEntityLevel.LOCAL, l2.fetchedFormServ(false)))
    }
/* Local card should not be inserted to group feed. Keeping it for future reference.
    @Test
    fun `test localCards for groups nlfc insertion`() {
        val fetchId = dao.insIgnore(f1)
        fun cards() = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(50).latestValue()
        assertThat(cards()).isEmpty()
        val creationDate = System.currentTimeMillis()
        val l1 = li1.copy(creationDate = creationDate, pageId = f1.entityId, section = f1.section, isCreatedFromOpenGroup = true)
        val p1 = post1.copy(localInfo = l1)
        val l2 = li2.copy(status = PostUploadStatus.SUCCESS.name, creationDate = creationDate, pageId = f1.entityId+",,,", section = f1.section, isCreatedFromOpenGroup = true)
        val p2 = post2.copy(localInfo = l2)
        dao.insertLocalPost(listOf(p1, p2), followDao)
        assertThat(dao.getLocalCardsForGroup(f1.entityId, f1.section, creationDate - 10).toIdLevelAndLocal())
                .containsExactly(
                        Triple(p1.id, PostEntityLevel.LOCAL, l1.fetchedFormServ(false))
                        */
/*Triple(p2.id, PostEntityLevel.LOCAL, l2.fetchedFormServ(false))*//*
)

        assertThat(cards()?.map {
            it.i_id()
        }).containsExactly(post1.id)
    }
*/

    @Test
    fun `test ins local card changes level to local before inserting`() {
        dao.insertLocalPost(listOf(post1.copy(level = PostEntityLevel.TOP_LEVEL),
                post2.copy(level = PostEntityLevel.LOCAL)), followDao)

        assertThat(dao.all()?.map {
            it.i_id() to it.i_level()
        }).containsExactly(
                post1.id to PostEntityLevel.LOCAL,
                post2.id to PostEntityLevel.LOCAL
        )
    }

    @Test
    fun `test updating local postEntity upload state`() {

        fun allLocalInfos() = dao.all()?.map { Triple(it.i_id() , it.i_level(), it.i_localInfo() )}

        val p1= post1.copy(localInfo = li1)
        val p2 = post2.copy(localInfo = li2)

        assertThat(allLocalInfos()).isEmpty()
        dao.insertLocalPost(listOf(p1, p2), followDao)
        // also insert through fetchdao -> it will be top level
        dao.replaceFirstPage(f1, listOf(post1), followDao, TEST_CONTENT_URL)
        assertThat(allLocalInfos()).containsExactly(
                Triple(post1.id ,PostEntityLevel.LOCAL, li1.fetchedFormServ(false)),
                Triple(post2.id ,PostEntityLevel.LOCAL, li2.fetchedFormServ(false)),
                Triple(post1.id ,PostEntityLevel.TOP_LEVEL, null)
        )


        dao.updateLocalProgressStatebyPostid(p1.id+p2.id,42, PostUploadStatus.CREATE ) // no op
        assertThat(allLocalInfos()).containsExactly(
                Triple(post1.id ,PostEntityLevel.LOCAL, li1.fetchedFormServ(false)),
                Triple(post2.id ,PostEntityLevel.LOCAL, li2.fetchedFormServ(false)),
                Triple(post1.id ,PostEntityLevel.TOP_LEVEL, null)
        )

        dao.updateLocalProgressStatebyPostid(p1.id,42, PostUploadStatus.CREATE ) // only post1 is affected

        assertThat(allLocalInfos()).containsExactly(
                Triple(post1.id ,PostEntityLevel.LOCAL, li1.copy(progress = 42, status = PostUploadStatus.CREATE.name, fetchedFromServer = false)),
                Triple(post2.id ,PostEntityLevel.LOCAL, li2.fetchedFormServ(false)),
                Triple(post1.id ,PostEntityLevel.TOP_LEVEL, null)
        )
    }

    @Test
    fun `test insertLocalPost maintains the level to local card`() {
        val fetchId= dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        val l2 = li1.copy(nextCardId = post2.id)
        val p3 = post3.copy(localInfo = l2)
        dao.insertLocalPost(listOf(p3), followDao, foryouPageId = f1.entityId, foryouLocation = f1.location,
                foryouSection = f1.section)

        val obs = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test()
        val l3 = l2.copy(fetchedFromServer = false, shownInForyou = true)
        val tripleP1 = Triple(post1.id, PostEntityLevel.TOP_LEVEL, null)
        val tripleP2 = Triple(post2.id, PostEntityLevel.TOP_LEVEL, null)
        assertThat(obs.assertValueCount(1).values().last().map {
            it.toIdLevelAndLocal()
        }).containsExactly(
                tripleP1,
                Triple(p3.id, PostEntityLevel.LOCAL, l3),
                tripleP2
        )

        dao.insertLocalPost(listOf(p3), followDao, fetchedFromServer = true, foryouPageId = f1.entityId, foryouLocation = f1.location,
                foryouSection = f1.section)
        val l4 = l3.copy(fetchedFromServer = true)
        assertThat(obs.assertValueCount(2).values().last().map {
            it.toIdLevelAndLocal()
        }).containsExactly(
                tripleP1,
                Triple(p3.id, PostEntityLevel.LOCAL, l4),
                tripleP2
        )

    }

    @Test
    fun `test delete local posts`() {
        val time = System.currentTimeMillis()
        dao.insertLocalPost(listOf(post2.copy(
                localInfo = LocalInfo(
                        50,
                        PostUploadStatus.UPLOADING.name,
                        "",
                        creationDate = time
                )
        ), post3.copy(
                localInfo = LocalInfo(
                        100,
                        PostUploadStatus.SUCCESS.name,
                        "",
                        creationDate = time - 10
                )
        )), followDao)
        dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1.copy(localInfo = LocalInfo(creationDate = time - 10))), followDao, TEST_CONTENT_URL) /*only local level will be deleted*/

        assertThat(dao.all().map { it.id })
                .containsExactly(post1.id, post2.id, post3.id)

        dao.deleteExpiredLocalCards(time - 9)

        assertThat(dao.all().map { it.id })
                .containsExactly(post1.id, post2.id)

    }

    @Test
    fun `test updating local postEntity upload state may modify null values`() {

        fun allLocalInfos() = dao.all()?.map { Triple(it.i_id(), it.i_level(), it.i_localInfo()?.copy(creationDate = null)) }

        assertThat(allLocalInfos()).isEmpty()
        dao.insertLocalPost(listOf(post1), followDao)
        val l = LocalInfo(fetchedFromServer = false)
        assertThat(allLocalInfos()).containsExactly(Triple(post1.id, PostEntityLevel.LOCAL, l))

        dao.updateLocalProgressStatebyPostid(post1.id, 42, PostUploadStatus.CREATE)
        assertThat(allLocalInfos()).containsExactly(
                Triple(post1.id, PostEntityLevel.LOCAL, l.copy(progress = 42, status = PostUploadStatus.CREATE.name))
        )
    }


    private fun CommonAsset?.toIdLevelAndLocal(resetDate: Boolean = false) = this?.let {
        Triple(it.i_id() , it.i_level(), if(resetDate) it.i_localInfo()?.copy(creationDate = null) else it.i_localInfo())
    }

    private fun List<CommonAsset>?.toIdLevelAndLocal(resetDate: Boolean = false) = this?.map { it.toIdLevelAndLocal(resetDate) }

    @Test
    fun `test gets livedata events as localcard is modified`() {
        val observer = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).test()
        dao.insertLocalPost(listOf(post2), followDao)
        dao.replaceFirstPageWithLocalCards(f1, listOf(post1), followDao, TEST_CONTENT_URL, null, true, false)

        val local1 = LocalInfo(shownInForyou = true,
                fetchedFromServer = false)
        assertThat(observer.assertValueCount(3).values().last().toIdLevelAndLocal(true))
                .containsExactly(Triple(post1.id, PostEntityLevel.TOP_LEVEL, null),
                        Triple(post2.id, PostEntityLevel.LOCAL, local1))

        dao.updateLocalProgressStatebyPostid(post2.id, 42, PostUploadStatus.UPLOADING)
        assertThat(observer.assertValueCount(4).values().last().toIdLevelAndLocal(true))
                .containsExactly(
                Triple(post1.id, PostEntityLevel.TOP_LEVEL, null),
                Triple(post2.id, PostEntityLevel.LOCAL, local1.copy(progress = 42, status = PostUploadStatus.UPLOADING.name)))
    }

    @Test
    fun testitemsmatching1() {
        val obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        assertThat(obs.values()).hasSize(1)
        assertThat(obs.values().get(0)).isEmpty()

        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f2, listOf(post1), followDao, TEST_CONTENT_URL)

        assertThat(obs.values()).hasSize(3)
        assertThat(obs.values().last()).containsExactly(post1.toCard(fetchId))
    }

    @Test
    fun testItemsMatchingBlockedSources(){
        dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        followDao.insReplace(followEntity(post1.source?.id
                ?: "", post1.source?.entityType!!, true))
        followDao.insReplace(followEntity(post2.source?.id
                ?: "", post2.source?.entityType!!, true))

        var obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        obs.assertValueCount(1)
        assertThat((obs.values().get(0)).size == 2)

        followDao.insReplace(followEntity(post2.source?.id
                ?: "", post2.source?.entityType!!, false))
        followDao.insReplace(followEntity(post1.source?.id
                ?: "", post1.source?.entityType!!, false))
        obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        obs.assertValueCount(1)
        assertThat((obs.values().last()).isEmpty())
    }


    @Test
    fun testItemsMatchingSourceBrowsing(){

        dao.replaceFirstPage(f1, listOf(post1), followDao, TEST_CONTENT_URL)
        followDao.insReplace(followEntity(post1.source?.id
                ?: "", post1.source?.entityType!!, false))

        var obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        assertThat(obs.values()).hasSize(1)
        assertThat(obs.values().get(0)).isEmpty()

        val fetchId = dao.insIgnore(f4)
        dao.replaceFirstPage(f4, listOf(post2), followDao, TEST_CONTENT_URL)
        followDao.insReplace(followEntity(post4.source?.id
                ?: "", post4.source?.entityType!!, false))

        obs = itemsMatching(f4.entityId, f4.location, f4.section).test()
        assertThat(obs.values()).hasSize(1)
        assertThat(obs.values().last()).containsExactly(post2.toCard(fetchId))

    }

    @Test
    fun testItemsMatchingIgnoreSourceBlock(){
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1), followDao, TEST_CONTENT_URL)
        followDao.insReplace(followEntity(post1.source?.id
                ?: "", post1.source?.entityType!!, false))

        var obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        assertThat(obs.values()).hasSize(1)
        assertThat(obs.values().get(0)).isEmpty()


        dao.replaceFirstPage(f1, listOf(post4), followDao, TEST_CONTENT_URL)
        followDao.insReplace(followEntity(post4.source?.id
                ?: "", post4.source?.entityType!!, false))

        obs = itemsMatching(f1.entityId, f1.location, f1.section).test()
        assertThat(obs.values()).hasSize(1)
        assertThat(obs.values().last()).containsExactly(post4.toCard(fetchId))
    }



    @Test
    fun `test no local cards inserted`() {
        assertThat(dao.allLocalPosts()).isEmpty()
    }


    @Test
    fun `test insertPendingLocalCardsForyou only At top`() {
        val fetchId = dao.insIgnore(f1)
        val postEntityList = listOf(post1, post2, post3)
        dao.replaceFirstPage(f1, postEntityList, followDao, TEST_CONTENT_URL)
        val p4= post1.copy(id  = "p4", localInfo = LocalInfo(creationDate = 10), level = PostEntityLevel.LOCAL)
        val p5 = post1.copy(id  = "p5", localInfo = LocalInfo(creationDate = 15), level = PostEntityLevel.LOCAL)
        dao.insertLocalPost(listOf(p4, p5),followDao)


        dao.insertLocalFetchDataAtTopOrInTheMiddle(f1.entityId, f1.location, f1.section, listOf(p4,p5))
        assertThat(dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData
        (readCardPageConfig()).latestValue()
                ?.map { it.i_id() })
                .containsExactly(p5.id, p4.id, post1.id, post2.id, post3.id)
                .inOrder()
    }

    @Test
    fun `test insertPendingLocalCardsForyou only inthe middle`() {
        val fetchId = dao.insIgnore(f1)
        val postEntityList = listOf(post1, post2, post3)
        dao.replaceFirstPage(f1, postEntityList, followDao, TEST_CONTENT_URL)
        val p4= post1.copy(id  = "p4", localInfo = LocalInfo(creationDate = 10, nextCardId = post2.id))
        val p5 = post1.copy(id  = "p5", localInfo = LocalInfo(creationDate = 15, nextCardId = post3.id))
        dao.insertLocalPost(listOf(p4, p5),followDao, foryouPageId = f1.entityId, foryouSection = f1.section,
                foryouLocation = f1.location)

        assertThat(dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig())
                .latestValue()
                ?.map { it.i_id() })
                .containsExactly(post1.id, p4.id, post2.id, p5.id, post3.id)
                .inOrder()
    }

    @Test
    fun `test insertPendingLocalCardsForyou only 2 inthe middle and 1 at the top`() {
        val fetchId = dao.insIgnore(f1)
        val postEntityList = listOf(post1, post2, post3)
        dao.replaceFirstPage(f1, postEntityList, followDao, TEST_CONTENT_URL)
        val p4= post1.copy(id  = "p4", localInfo = LocalInfo(creationDate = 10, nextCardId = post2.id))
        val p5 = post1.copy(id  = "p5", localInfo = LocalInfo(creationDate = 15, nextCardId = p4.id))
        val p6 = post1.copy(id  = "p6", localInfo = LocalInfo(creationDate = 15))
        dao.insertLocalPost(listOf(p4, p5, p6),followDao, foryouPageId = f1.entityId, foryouSection = f1.section,
                foryouLocation = f1.location)

//        dao.insertLocalFetchDataAtTopOrInTheMiddle(f1.entityId, f1.location, f1.section, postEntityList)


        assertThat(dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig())
                .latestValue()
                ?.map { it.i_id() })
                .containsExactly(p6.id, post1.id,  p5.id, p4.id, post2.id, post3.id)
                .inOrder()
    }


    @Test
    fun `test localcard fetchdata does not get deleted when replaced with server card`() {
        val p4= post1.copy(id  = "p4", localInfo = LocalInfo(creationDate = 10), level = PostEntityLevel.LOCAL)
        val p5 = post1.copy(id  = "p5", localInfo = LocalInfo(creationDate = 15), level = PostEntityLevel.LOCAL)
        dao.replaceFirstPage(f1, listOf(post1),followDao, TEST_CONTENT_URL)
        dao.insertLocalPost(listOf(p4, p5), followDao, false, f1.entityId, f1.location, f1.section)

        assertThat(dao.itemsMatchingLiveList(f1.entityId, f1.location, f1.section).latestValue()
                ?.map { it.i_id() })
                .containsExactly(p5.id , p4.id, post1.id)

        val p4server = p4.copy(format = Format.HTML)
        dao.insertLocalPost(listOf(p4), followDao, true, f1.entityId, f1.location, f1.section)
        // still remains
        assertThat(dao.itemsMatchingLiveList(f1.entityId, f1.location, f1.section).latestValue()
                ?.map { it.i_id() })
                .containsExactly(p5.id , p4.id, post1.id)
    }

    @Test
    fun `insertFetchDataAtTop works`() {
        val fetchid = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1),followDao, TEST_CONTENT_URL)
        dao.insertLocalPost(listOf(post2), followDao)
        dao.insertLocalFetchDataAtTopOrInTheMiddle(f1.entityId, f1.location, f1.section, listOf(post2))

        val ids = dao.allFetchData().latestValue()?.map {
            Triple(it.pageNum, it.indexInPage, it.storyId)
        }

        assertThat(ids)
                .containsExactly(Triple(0, 0, post2.getUniqueId(fetchid)),
                        Triple(0, 1, post1.getUniqueId(fetchid)))

    }
    @Test
    fun `insertFetchDataAtTop can insert even if no fetchdata`() {
        val fetchid = dao.insIgnore(f1)
        dao.insertLocalPost(listOf(post2), followDao)
        dao.insertLocalFetchDataAtTopOrInTheMiddle(f1.entityId, f1.location, f1.section, listOf(post2))

        val ids = dao.allFetchData().latestValue()?.map {
            Triple(it.pageNum, it.indexInPage, it.storyId)
        }

        assertThat(ids)
                .containsExactly(Triple(0, 0, post2.getUniqueId(fetchid)))

    }

/* Local card should not be inserted to group feed. Keeping it for future reference.
    @Test
    fun testReplaceFPGroupFeed() {
        dao.insertLocalPost(listOf(post2.copy(
                localInfo = LocalInfo(
                        100,
                        PostUploadStatus.SUCCESS.name,
                        f1.entityId,
                        section = f1.section,
                        creationDate = System.currentTimeMillis(),
                        isCreatedFromOpenGroup = true
                )
        ), post3.copy(
                localInfo = LocalInfo(
                        100,
                        PostUploadStatus.SUCCESS.name,
                        f1.entityId+",,,,", // this would not match
                        section = f1.section,
                        creationDate = System.currentTimeMillis(),
                        isCreatedFromOpenGroup = true
                )
        )),followDao)
        dao.replaceFirstPageWithLocalCards(f1, listOf(post1), followDao, TEST_CONTENT_URL, null , false, false, true)

        val map = dao.itemsMatching(f1.entityId, f1.location, f1.section).toLiveData(readCardPageConfig()).latestValue()?.map {
            it.i_id()
        } ?:throw Throwable("should get non null list")
        assertThat(map).containsExactly(post2.id, post1.id).inOrder()
    }
*/



    @Test
    @Ignore
    fun `test comments and replies will not be local cards`() {
        TODO("not implemented")
    }

    @Ignore
    @Test
    fun testFetchingAndInsertingLocalPost() {
        TODO()
    }

    @Ignore
    @Test
    fun testLikingFullLocalCardWorks() {
        TODO()
    }

    @Test
    fun testUserHasFollows() {
        assertThat(followDao.userHasAnyFollows()).isFalse()

        followDao.toggleFollowItems(FollowSyncEntity(
                ActionableEntity("eid", "SOURCE"),
                FollowActionType.FOLLOW
        ))
        assertThat(followDao.userHasAnyFollows()).isTrue()
    }

    @Test
    fun cloningDoesNotCloneAds() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, ad1), followDao, TEST_CONTENT_URL)
        val fd = {
            dao.allFetchData().latestValue()?.map {
                it.indexInPage to it.storyId
            }
        }
        assertThat(fd()).containsExactly(
                0 to post1.getUniqueId(fetchId),
                1 to ad1.getUniqueId(fetchId)
        )
        dao.cloneFetchForLocation(fetchId, "detail", f1.section)
        assertThat(fd()).containsExactly(
                0 to post1.getUniqueId(fetchId),
                1 to ad1.getUniqueId(fetchId),
                0 to post1.getUniqueId(fetchId))
    }

    @Test
    fun cloningDoesNotCloneDislikedItems() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        val fd = {
            dao.allFetchData().latestValue()?.map {
                it.indexInPage to it.storyId
            }
        }
        assertThat(fd()).containsExactly(
                0 to post1.getUniqueId(fetchId),
                1 to post2.getUniqueId(fetchId)
        )
        dislikeDao.insReplace(post2.toDislikeEntity2(System.currentTimeMillis()))
        dao.cloneFetchForLocation(fetchId, "detail", f1.section)
        assertThat(fd()).containsExactly(
                0 to post1.getUniqueId(fetchId),
                1 to post2.getUniqueId(fetchId),
                0 to post1.getUniqueId(fetchId)) // post2 is not cloned
    }

    @Test
    fun cloningAlwaysIncludeMustIncludeItems() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2, post3), followDao, TEST_CONTENT_URL)
        val fd = {
            dao.allFetchData().latestValue()?.map {
                it.indexInPage to it.storyId
            }
        }
        assertThat(fd()).containsExactly(
                0 to post1.getUniqueId(fetchId),
                1 to post2.getUniqueId(fetchId),
                2 to post3.getUniqueId(fetchId)
        )
        dislikeDao.insReplace(post2.toDislikeEntity2(System.currentTimeMillis()))
        dislikeDao.insReplace(post3.toDislikeEntity2(System.currentTimeMillis()))

        dao.cloneFetchForLocation(fetchId, "detail", f1.section, listOf(post3.id))
        assertThat(fd()).containsExactly(
                0 to post1.getUniqueId(fetchId),
                1 to post2.getUniqueId(fetchId),
                2 to post3.getUniqueId(fetchId),
                0 to post1.getUniqueId(fetchId),
                2 to post3.getUniqueId(fetchId)) // post2 is not cloned
    }

    @Test
    fun testDeletion() {
        val fetchId = dao.insIgnore(f1)
        dao.replaceFirstPage(f1, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        val fd1 = FetchDataEntity(fetchId, 0, 0, post1.getUniqueId(fetchId), post1.format, TEST_CONTENT_URL)
        val fd2 = FetchDataEntity(fetchId, 0, 1, post2.getUniqueId(fetchId), post2.format, TEST_CONTENT_URL)
        assertThat(dao.allFetchData().latestValue())
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(fd1, fd2)
        // pass id; no-op
        dao.deleteFetchDataForPostMatching(fetchId, listOf(post1.id))
        assertThat(dao.allFetchData().latestValue())
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(fd1, fd2)
        // pass uniqueid
        dao.deleteFetchDataForPostMatching(fetchId, listOf(post1.getUniqueId(fetchId)))
        assertThat(dao.allFetchData().latestValue())
                .comparingElementsUsing(fetchdataCorrespondence)
                .containsExactly(fd2)
        // pass id, different method
        dao.deleteFetchDataOfPostIds(fetchId, listOf(post2.id))
        assertThat(dao.allFetchData().latestValue()).isEmpty()
    }

    @Test
    fun testLookupCard() {
        val fetchid = dao.insIgnore(f1)
        val fetchid2 = dao.insIgnore(ff1) // has no effect.
        dao.replaceFirstPage(f1, listOf(post1, post2),followDao, TEST_CONTENT_URL)
        dao.replaceFirstPage(ff1, listOf(post1, post2), followDao, TEST_CONTENT_URL)
        assertThat(postDao.lookupCard(post1.id, f1.entityId, f1.location, f1.section))
                .containsExactly(post1.toCard2(fetchid))
        assertThat(postDao.lookupCard(post3.id, f1.entityId, f1.location, f1.section)).isEmpty()
    }

    @Test
    fun testCardsById_observeSingleCard() {
        val fetchId: Long = 1
        val p1obs = cardDao.cardsById(listOf(post1.getUniqueId(fetchId))).test()
        p1obs.assertValueCount(1).assertValueAt(0, {it.isEmpty()})
        val list1 = listOf(post1.toCard2(fetchId))
        dao.insIgnore(list1) // ins 1
        p1obs.assertValueCount(2)
        p1obs.assertValueAt(1) { it == list1 }
        val list2 = listOf(post2.toCard2(fetchId))
        dao.insIgnore(list2) // ins 2
        p1obs.assertValueCount(3)
        p1obs.assertValueAt(2) {it == list1}
    }


    @Test
    fun testCardsById_observeMultipleCards() {
        val fetchId: Long = 1
        val obs = cardDao.cardsById(listOf(post1.getUniqueId(fetchId), post2.getUniqueId(fetchId))).test()
        obs.assertValueCount(1).assertValueAt(0, {it.isEmpty()})
        val list1 = listOf(post1.toCard2(fetchId)) // ins 1
        dao.insIgnore(list1)
        obs.assertValueCount(2)
        obs.assertValueAt(1) { it == list1 }
        val list2 = listOf(post2.toCard2(fetchId))
        dao.insIgnore(list2) // ins 2
        obs.assertValueCount(3)
        obs.assertValueAt(2) {it == (list1 + list2)}
        postDao.deletePost(post1.id) // del 1
        obs.assertValueCount(4)
        obs.assertValueAt(3) { it == list2 }
    }


    private fun itemsMatching(entityId: String,
                              location: String,
                              section: String) =
    dao.itemsMatching(entityId, location, section)
    .map {
        if (it != null && ReadCardsUsecase.filterCards(it as Any, object :InvalidCardsLogger{
                    override fun log(message: String, pojo: Any) {
                        println(message)
                    }
                })) {
            it
        } else {
            null
        }
    }.toLiveData(Config(pageSize = 20,
    prefetchDistance = 10,
    maxSize = 80,
    enablePlaceholders = true))

}

fun <T> pagedListOf(list: List<T>): PagedList<T?> {
    return PagedList.Builder(ListDataSource(list), readCardPageConfig()).setFetchExecutor(getExecutor()).setNotifyExecutor(getExecutor()).build()
}

fun getExecutor(): Executor {
    return Executor { r ->
        r.run()
    }
}