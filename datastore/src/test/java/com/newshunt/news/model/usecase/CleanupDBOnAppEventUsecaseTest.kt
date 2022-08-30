/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkEntity
import com.newshunt.dataentity.model.entity.DeletedInteractionsEntity
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.model.entity.PendingApprovalsEntity
import com.newshunt.dataentity.social.entity.DislikeEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.daos.DeletedInteractionsDao
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.HistoryDao
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.daos.PendingApprovalsDao
import com.newshunt.news.model.daos.PullDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.utils.LiveDataTestUtil
import com.newshunt.news.model.utils.post1
import com.newshunt.news.model.utils.samplePageEntity
import com.newshunt.news.model.utils.test
import io.mockk.MockKAnnotations
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date

/**
 * For testing usecase with in-memory DB
 * @author satosh.dhanyamraju
 */

@Config(sdk = [27])
@RunWith(AndroidJUnit4::class)
class CleanupDBOnAppEventUsecaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var usecase : CleanupDBOnAppEventUsecase
    lateinit var fetchDao: FetchDao
    lateinit var followEntityDao: FollowEntityDao
    lateinit var dislikeDao: DislikeDao
    lateinit var pullDao: PullDao
    lateinit var interactionDao: InteractionDao
    lateinit var pageEntityDao: PageEntityDao
    lateinit var deletedInteractionsDao: DeletedInteractionsDao
    lateinit var bookmarksDao: BookmarksDao
    lateinit var historyDao: HistoryDao
    lateinit var pendingApprovalsDao: PendingApprovalsDao


    @Before
    fun setUp() {
        CommonUtils.IS_IN_TEST_MODE = true
        AppConfig.createInstance(AppConfigBuilder())
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        fetchDao = instance.fetchDao()
        followEntityDao = instance.followEntityDao()
        dislikeDao = instance.dislikeDao()
        pullDao = instance.pullDao()
        interactionDao = instance.interactionsDao()
        pageEntityDao = instance.pageEntityDao()
        deletedInteractionsDao = instance.deletedInteractionsDao()
        bookmarksDao = instance.bookmarkDao()
        historyDao = instance.historyDao()
        pendingApprovalsDao = instance.pendingApprovalsDao()
        usecase = CleanupDBOnAppEventUsecase(instance)
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testAppstart() {
        val f1 = FetchInfoEntity("eid", "list", "np", 1, null, 2000L, 1L, "news")
        fetchDao.insIgnore(f1)
        fetchDao.replaceFirstPage(f1, listOf(post1), followEntityDao, "", null)
        assertThat(fetchDao.fetchInfo(f1.entityId, f1.location, f1.section)?.lastViewDestroyTs).isEqualTo(f1.lastViewDestroyTs)

        usecase.invoke(CleanupDBOnAppEventUsecase.APPSTART_BUNDLE).test()

        assertThat(fetchDao.fetchInfo(f1.entityId, f1.location, f1.section)?.lastViewDestroyTs).isEqualTo(0)
    }


    @Test
    fun testLogout() {

        val f1 = FetchInfoEntity(samplePageEntity.pageEntity.id, "list", "np", 1, null, 2000L, 1L, samplePageEntity.section)
        pageEntityDao.insReplace(samplePageEntity)
        fetchDao.insIgnore(f1)
        fetchDao.replaceFirstPage(f1, listOf(post1), followEntityDao, "", null)
        dislikeDao.insReplace(DislikeEntity(post1.id))
        followEntityDao.toggleFollowItems(FollowSyncEntity(ActionableEntity(post1.source?.id!!, post1.source?.entityType!!), FollowActionType.FOLLOW))
        pullDao.insertPullInfo(f1.entityId, f1.section, 10)
        pullDao.insertOrReplaceRecentTab(f1.entityId, f1.section, 15)
        interactionDao.toggleLike(post1.id, post1.format.name, LikeType.ANGRY.name)
        deletedInteractionsDao.insReplace(listOf(DeletedInteractionsEntity("123", SyncStatus.SYNCED)))
        bookmarksDao.insertIgnore(listOf(BookmarkEntity("1234", Format.HTML.name, SubFormat.HTML
                .name, BookMarkAction.ADD, System.currentTimeMillis(), SyncStatus.SYNCED)))
        historyDao.insReplace(listOf(HistoryEntity(id = "4565",
                format = Format.HTML,
                subFormat = SubFormat.HTML,
                uiType = UiType2.BIG,
                imgUrl = ImageDetail("http://google.com"),
                title = "hello",
                content = "hello world",
                duration = "00:00",
                timestamp = Date(System.currentTimeMillis()),
                srcLogo = "http://google.com",
                srcName = "source")))
        pendingApprovalsDao.insReplace(PendingApprovalsEntity("abgyd123124", ApprovalCounts(null,
                null, null, null)))

        assertThat(LiveDataTestUtil.getValue(dislikeDao.all())).hasSize(1)
        assertThat(followEntityDao.getFollowList()).isNotEmpty()
        assertThat(pullDao.allRecentTabs()).isNotEmpty()
        assertThat(pullDao.allPullInfo()).isNotEmpty()
        interactionDao._allInteractions().test().assertValueAt(0) {
            it.isNotEmpty()
        }

        usecase.invoke(CleanupDBOnAppEventUsecase.LOGOUT_BUNDLE).test()

        assertThat(LiveDataTestUtil.getValue(dislikeDao.all())).isEmpty()
        assertThat(followEntityDao.getFollowList()).isEmpty()
        assertThat(pullDao.allRecentTabs()).isEmpty()
        assertThat(pullDao.allPullInfo()).isEmpty()
        interactionDao._allInteractions().test().assertValueAt(0) {
            it.isEmpty()
        }
        assertThat(LiveDataTestUtil.getValue(fetchDao.allFetchInfo())).isEmpty()

        assertThat(deletedInteractionsDao.getDeletedInteractionsByStatus(listOf(SyncStatus.SYNCED)).isEmpty())
        assertThat(bookmarksDao.countByFormat(BookMarkAction.ADD, Format.HTML.name) == 0)
        historyDao.queryLiveData(0).test().assertValueAt(0) {
            it.isEmpty()
        }
        pendingApprovalsDao.queryPendingApprovalsLiveData("abgyd123124").test().assertValue(null)
    }


    @Test
    fun testLangChange() {
        val f1 = FetchInfoEntity("eid", "list", "np", 1, null, 2000L, 1L, "news")
        fetchDao.insIgnore(f1)
        fetchDao.replaceFirstPage(f1, listOf(post1), followEntityDao, "", null)
        assertThat(fetchDao.fetchInfo(f1.entityId, f1.location, f1.section)?.lastViewDestroyTs).isEqualTo(f1.lastViewDestroyTs)

        usecase.invoke(CleanupDBOnAppEventUsecase.LANGCHANGE_BUNDLE).test()

        assertThat(fetchDao.fetchInfo(f1.entityId, f1.location, f1.section)?.lastViewDestroyTs).isNull()
    }
}