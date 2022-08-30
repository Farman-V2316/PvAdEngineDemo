/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

@file:Suppress("IncorrectScope")

package com.newshunt.news.model.utils

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.EitherList
import androidx.paging.PagedList
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.common.helper.cachedapi.CacheCompressUtils
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PollAssetResponse
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.cachedapi.CacheType
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.Status
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.MenuDictionaryEntity1
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuPayload
import com.newshunt.dataentity.social.entity.Vote
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.scan
import com.newshunt.news.model.apis.AnswerPollApi
import com.newshunt.news.model.apis.MenuApi
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.MenuDao
import com.newshunt.news.model.daos.PullDao
import com.newshunt.news.model.daos.VoteDao
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.usecase.AnswerPollUsecase
import com.newshunt.news.model.usecase.ApiCacheProvider
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.CancelNetworkSDKRequestsUsecase
import com.newshunt.news.model.usecase.CleanUpFetchUsecase
import com.newshunt.news.model.usecase.CloneFetchForNewsDetailUsecase
import com.newshunt.news.model.usecase.FPFetchUseCase
import com.newshunt.news.model.usecase.FPInserttoDBUsecase
import com.newshunt.news.model.usecase.FetchAndInsertMenuOptionsUsecase
import com.newshunt.news.model.usecase.FetchCacheUsecase
import com.newshunt.news.model.usecase.FollowFromMenuUsecase
import com.newshunt.news.model.usecase.GetLatestFollowUsecase
import com.newshunt.news.model.usecase.GetLatestUploadedPostUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResp
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.newshunt.news.model.usecase.NPUsecase
import com.newshunt.news.model.usecase.PostL1Usecase
import com.newshunt.news.model.usecase.ReadCardsUsecase
import com.newshunt.news.model.usecase.ReadLimitedCardsUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.news.model.usecase.toMediator
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.model.utils.LiveDataTestUtil.getValue
import com.newshunt.news.model.utils.UsecaseTest.ApiCall.Companion.BUNDLE_A
import com.newshunt.news.model.utils.UsecaseTest.BuildPayload.Companion.BUNDLE_S
import com.newshunt.sdk.network.NetworkSDK
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import junit.framework.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * @author satosh.dhanyamraju
 */
@Suppress("IncorrectScope")
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class UsecaseTest {
    val cssRepo = CardSeenStatusRepo.DEFAULT
    @get:Rule
    val rule = InstantTaskExecutorRule()

    class BuildPayload : BundleUsecase<List<String>> {
        override fun invoke(p1: Bundle): Observable<List<String>> {
            return Observable.fromCallable { listOf(p1.getString(BUNDLE_S) ?: "", "aa") }
        }

        companion object {
            const val BUNDLE_S = "a"
        }
    }

    class ApiCall(val buildPayload: BuildPayload) : BundleUsecase<List<String>> {
        override fun invoke(p1: Bundle): Observable<List<String>> {
            return buildPayload(p1).map { p ->
                p.map {
                    "$it + ${p1.getString(BUNDLE_A)} + 42"
                }
            }
        }

        companion object {
            const val BUNDLE_A = "b"
        }
    }

    val nlResponseWrapper = NLResponseWrapper(TEST_CONTENT_URL, NLResp().apply {
        rows = listOf(
                PostEntity(id = "id1", type = "news"),
                PostEntity(id = "id2", type = "buzz"))
    }, TEST_CONTENT_URL)

    val cacheNlResponseWrapper = NLResponseWrapper(TEST_CONTENT_URL, NLResp().apply {
        rows = listOf(
                PostEntity(id = "id3", type = "news"))
    }, TEST_CONTENT_URL)
    val cacheResp = CacheCompressUtils.compress(SerializationUtils.serialize(
            cacheNlResponseWrapper
    ))

    private val apiThrowable = Throwable("This should not be called")

    class MockableApiCallUsecase(private val shouldThrow: Boolean = false, private val nlResponseWrapper: NLResponseWrapper) : BundleUsecase<NLResponseWrapper> {
        override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
            if (shouldThrow) return Observable.error(Throwable("This should not be called")) //apiThrowable
            return Observable.just(nlResponseWrapper)
        }
    }



    class DelayedIncUsecase : Usecase<Int, Int> {
        override fun invoke(p1: Int): Observable<Int> {
            return Observable.just(p1)
                    .delay(1, TimeUnit.SECONDS)
                    .map { it + 1 }
        }
    }

    @io.mockk.impl.annotations.MockK
    lateinit var fetchDao: FetchDao
    @io.mockk.impl.annotations.MockK
    lateinit var cpDao : CreatePostDao
    @io.mockk.impl.annotations.MockK
    lateinit var followDao: FollowEntityDao
    @io.mockk.impl.annotations.MockK
    lateinit var followRepo: FollowRepo
    @io.mockk.impl.annotations.MockK
    lateinit var newsApi: NewsApi
    @io.mockk.impl.annotations.MockK
    lateinit var voteDao: VoteDao
    @io.mockk.impl.annotations.MockK
    lateinit var answerPollApi: AnswerPollApi
    @io.mockk.impl.annotations.MockK
    lateinit var menuDao: MenuDao
    @io.mockk.impl.annotations.MockK
    lateinit var pullDao: PullDao
    @io.mockk.impl.annotations.MockK
    lateinit var menuApi: MenuApi
    @io.mockk.impl.annotations.MockK
    lateinit var cache: CachedApiCacheRx
    @io.mockk.impl.annotations.MockK
    lateinit var mockBookmackDao: BookmarksDao
    @io.mockk.impl.annotations.MockK
    lateinit var mockReadCardsUc : ReadCardsUsecase<List<Any>>
    @io.mockk.impl.annotations.MockK
    lateinit var fetchUsecase: BundleUsecase<NLResponseWrapper>
    @io.mockk.impl.annotations.MockK
    lateinit var fetchCacheUsecase: BundleUsecase<NLResponseWrapper>
    @io.mockk.impl.annotations.MockK
    lateinit var mockReadLimitedCardUsecase: ReadLimitedCardsUsecase
    val entityId = "c1"
    val location = "list"
    val section = "news"
    private val cacheProvider = object : ApiCacheProvider {
        override fun getCache(directory: String): CachedApiCacheRx {
            return cache
        }

    }
    val emptyTransformList = object : TransformNewsList {
        override fun transf(list: List<AnyCard>): List<AnyCard> {
            return list
        }

    }


    @Before
    fun setUp() {
        CommonUtils.IS_IN_TEST_MODE = true
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
        AppConfig.createInstance(AppConfigBuilder())
        NetworkSDK.init(ApplicationProvider.getApplicationContext(), null, false)
    }

    @Test
    fun testTheyComposeWell() {
        val apiCall = ApiCall(BuildPayload())
        val toMediator = apiCall.toMediator()
        var cur: Result0<List<String>> = Result0.failure(Exception(""))
        toMediator.execute(bundleOf(BUNDLE_A to "baa", BUNDLE_S to "e"))
        toMediator.data().observeForever {
            //println(it)
            cur = it
        }
        toMediator.execute(bundleOf(BUNDLE_A to "beee", BUNDLE_S to "fff"))
        Truth.assertThat(cur).isNotNull()
        Truth.assertThat(cur.isSuccess).isTrue()
        val list = cur.getOrNull()
        Truth.assertThat(list)
                .containsExactly("fff + beee + 42", "aa + beee + 42")
    }

    @LargeTest
    @Test
    fun testMediatorStatus() {
        val delayedIncUsecase = DelayedIncUsecase().toMediator<Int, Int>()
        val status = delayedIncUsecase.status()

        delayedIncUsecase.execute(1)

        assertThat(getValue(status, 0)).isTrue()
        Thread.sleep(500)
        assertThat(getValue(status, 0)).isTrue()
        Thread.sleep(600)
        assertThat(getValue(status, 0)).isFalse()
    }

    @LargeTest // takes 2s to run, testing threads. flaky.
    @Test
    fun testMediator2IgnoresConcurrentRequestsBasedOnFlag() {
        val delayedIncUsecase = DelayedIncUsecase().toMediator2(true, Schedulers.io())
        delayedIncUsecase.execute(1)
        delayedIncUsecase.execute(2) // will be ignored
        delayedIncUsecase.execute(3)// will be ignored
        assertThat(getValue(delayedIncUsecase.data(), 0)).isNull()
        Thread.sleep(1059) // 1s passed. Must have posted and now it will take more events
        assertThat(getValue(delayedIncUsecase.data())?.getOrNull()).isEqualTo(2)
        delayedIncUsecase.execute(4)
        Thread.sleep(1019)
        assertThat(getValue(delayedIncUsecase.data())?.getOrNull()).isEqualTo(5)
    }

    /**
     * simulate FPUsescase. todo change to seperate tests.
     */
    fun buildFpUsecase(): MediatorUsecase<Bundle, NLResponseWrapper> {
        val f = object : BundleUsecase<NLResponseWrapper> {
            val fetch = FPFetchUseCase(entityId, location, section, true,
                    getMockableCacheApiUsecase(), fetchDao
                    , followDao, false, true)

            val ins = FPInserttoDBUsecase(entityId, location, section, fetchDao, followDao, pullDao, cpDao, false, false, DEF_TTL, cssRepo)

            override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
                return fetch(Bundle()).flatMap {
                    it.nlResp.isFromNetwork = true
                    ins(bundleOf(FPInserttoDBUsecase.B_RESP to it))
                }
            }
        }

        return f.toMediator()
    }

    @Test
    fun testBasicCardsUsecaseFP() {
        val basicCardsUsecase =
                buildFpUsecase()
        every {
            fetchDao.contentUrlForPage(entityId)
        } returns "contenturl"
        val f1 = FetchInfoEntity(entityId, location, section = section)
        every {
            fetchDao.insIgnore(f1)
        } returns 1L

        every {
            fetchDao.lookupPage(entityId, section)
        } returns FeedPage(entityId, "curl", Constants.HTTP_GET)

        every {
            fetchDao.fetchInfo(entityId, location, section)
        } returns null

        every { fetchDao.cardCount(-1) } returns 0

        every { pullDao.insertOrReplaceRecentTab(entityId, section) } returns Unit
        every { pullDao.insertPullInfo(entityId, section) } returns Unit

        basicCardsUsecase.execute(bundleOf("page" to 0))

        verify {
            fetchDao.insIgnore(f1)
            fetchDao.fetchInfo(entityId, location, section)
            fetchDao.cardCount(-1)
            fetchDao.lookupPage(entityId, section)
            fetchDao.replaceFirstPage(any(), any(), followDao, TEST_CONTENT_URL, any())
            pullDao.insertOrReplaceRecentTab(entityId, section, any())
            pullDao.insertPullInfo(entityId, section, any())
        }
        confirmVerified(fetchDao, pullDao)
    }

    @Test
    fun testFPUsecaseDoesnotRequestWithin60Sec() {
        val fe = slot<FetchInfoEntity>()
        val l = slot<List<PostEntity>>()

        val basicCardsUsecase =
                FPFetchUseCase(entityId, location, section, true,
                        getMockableCacheApiUsecase(true), fetchDao
                        , followDao, false, true).toMediator()

        every {
            fetchDao.insIgnore(FetchInfoEntity(entityId, location, section = section))
        } returns 1L
        val fetchId = 12L
        every {
            fetchDao.fetchInfo(entityId, location, section)
        } returns FetchInfoEntity(entityId, location, "oe", lastViewDestroyTs = System.currentTimeMillis(), section = section, fetchInfoId = fetchId)

        basicCardsUsecase.execute(bundleOf("page" to 0))

        verify {
            fetchDao.insIgnore(FetchInfoEntity(entityId, location, section = section))
            fetchDao.fetchInfo(entityId, location, section)
            fetchDao.cardCount(fetchId)
        }
        confirmVerified(fetchDao, pullDao)
    }

    @Test
    fun testFPUsecase60SecGapIsIgnoredForPullToRefresh() {

        val fetchId = 12L
        val f1 = FetchInfoEntity(entityId, location, section = section)

        val fp =
                FPFetchUseCase(f1.entityId, f1.location, f1.section, true,
                        getMockableCacheApiUsecase(), fetchDao
                        , followDao, false, true).toMediator()
        every {
            fetchDao.contentUrlForPage(entityId)
        } returns "contenturl"
        every {
            fetchDao.insIgnore(f1)
        } returns 1L

        every {
            fetchDao.lookupPage(entityId, section)
        } returns FeedPage(entityId, "curl", Constants.HTTP_GET)

        every {
            fetchDao.fetchInfo(entityId, location, section)
        } returns FetchInfoEntity(entityId, location, "oe", lastViewDestroyTs = System.currentTimeMillis(), section = section, fetchInfoId = fetchId)

        every {
            fetchDao.cardCount(fetchId)
        } returns 10

        fp.execute(bundleOf("page" to 0, FPFetchUseCase.B_PULL_TO_RFRSH to true))

        verify {
            fetchDao.insIgnore(f1)
            fetchDao.fetchInfo(entityId, location, section)
            fetchDao.cardCount(fetchId)
            fetchDao.lookupPage(entityId, section)
        }
        confirmVerified(fetchDao, pullDao)
    }


    @Test
    fun testFPUsecase60SecGapIsIgnoredAsPerFlag() {

        val fetchId = 12L
        val f1 = FetchInfoEntity(entityId, location, section = section)

        val fp =
                FPFetchUseCase(f1.entityId, f1.location, f1.section, true,
                        getMockableCacheApiUsecase(), fetchDao
                        , followDao, false, false).toMediator()
        every {
            fetchDao.contentUrlForPage(entityId)
        } returns "contenturl"
        every {
            fetchDao.insIgnore(f1)
        } returns 1L

        every {
            fetchDao.lookupPage(entityId, section)
        } returns FeedPage(entityId, "curl", Constants.HTTP_GET)

        every {
            fetchDao.fetchInfo(entityId, location, section)
        } returns FetchInfoEntity(entityId, location, "oe", lastViewDestroyTs = System.currentTimeMillis(), section = section, fetchInfoId = fetchId)

        every {
            fetchDao.cardCount(fetchId)
        } returns 10

        fp.execute(bundleOf("page" to 0, FPFetchUseCase.B_PULL_TO_RFRSH to true))

        verify {
            fetchDao.insIgnore(f1)
            fetchDao.fetchInfo(entityId, location, section)
            fetchDao.cardCount(fetchId)
            fetchDao.lookupPage(entityId, section)
        }
        confirmVerified(fetchDao, pullDao)
    }

    private val DEF_TTL = 600_000L

    @Test
    fun tesstInsertToDBUsecase() {

        val uc = FPInserttoDBUsecase(entityId, location, section, fetchDao, followDao, pullDao, cpDao, false, false, DEF_TTL, cssRepo).toMediator2()
        val nlR = NLResponseWrapper(TEST_CONTENT_URL, NLResp(true).apply {
            rows = emptyList()
        }, "key")

        uc.execute(bundleOf(FPInserttoDBUsecase.B_RESP to nlR))

        verify {
            fetchDao.replaceFirstPage(any(), emptyList(), followDao, nlR.reqUrl)
            pullDao.insertOrReplaceRecentTab(entityId, section, any())
            pullDao.insertPullInfo(entityId, section, any())
        }
        confirmVerified(fetchDao, pullDao, cpDao)
    }

    @Test
    fun tesstInsertToDBUsecase_setIsCacheFlag() {
        val fromNetwork = false
        val uc = FPInserttoDBUsecase(entityId, location, section, fetchDao, followDao, pullDao, cpDao, false, false, DEF_TTL, cssRepo).toMediator2()
        val nlR = NLResponseWrapper(TEST_CONTENT_URL, NLResp(true).apply {
            rows = listOf(post1)
            isFromNetwork = fromNetwork
        }, "key")

        uc.execute(bundleOf(FPInserttoDBUsecase.B_RESP to nlR))

        verify {
            fetchDao.replaceFirstPage(any(), listOf(
                    post1.updateCacheFlagAndGet(fromNetwork)
            ), followDao, nlR.reqUrl)
            pullDao.insertOrReplaceRecentTab(entityId, section, any())
            // pullDao.insertPullInfo(entityId, section, any()) -> this will not be called for cache response
        }
        confirmVerified(fetchDao, pullDao, cpDao)
    }

    @Test
    fun tesstInsertToDBUsecaseOfForyou() {

        val uc = FPInserttoDBUsecase(foryouid, location, section, fetchDao, followDao, pullDao, cpDao, true, false, DEF_TTL, cssRepo).toMediator2()
        val nlR = NLResponseWrapper(TEST_CONTENT_URL, NLResp(true).apply {
            rows = emptyList()
        }, "key")

        uc.execute(bundleOf(FPInserttoDBUsecase.B_RESP to nlR))

        verify {
            fetchDao.replaceFirstPageWithLocalCards(any(), emptyList(), followDao, nlR.reqUrl, null, true, false, localCardTtl = DEF_TTL)
            pullDao.insertOrReplaceRecentTab(foryouid, section, any())
            pullDao.insertPullInfo(foryouid, section, any())
            cpDao.markLocalCardsAsShown()
        }
        confirmVerified(fetchDao, pullDao, cpDao)
    }

    @Test
    fun tesstInsertToDBUsecaseOfMyPosts() {

        val uc = FPInserttoDBUsecase(foryouid, location, section, fetchDao, followDao, pullDao, cpDao, false, true , DEF_TTL, cssRepo).toMediator2()
        val nlR = NLResponseWrapper(TEST_CONTENT_URL, NLResp(true).apply {
            rows = emptyList()
        }, "key")

        uc.execute(bundleOf(FPInserttoDBUsecase.B_RESP to nlR))

        verify {
            fetchDao.replaceFirstPageWithLocalCards(any(), emptyList(), followDao, nlR.reqUrl, null, false, true,localCardTtl =  DEF_TTL)
            pullDao.insertOrReplaceRecentTab(foryouid, section, any())
            pullDao.insertPullInfo(foryouid, section, any())
            cpDao.markLocalCardsAsShown()
        }
        confirmVerified(fetchDao, pullDao, cpDao)
    }

    @Test
    fun testFPUsecase60SecGapIsIgnoredIfNoFetchDataInDB() {
        val fetchId = 12L

        val basicCardsUsecase =
                FPFetchUseCase(entityId, location, section, true,
                        getMockableCacheApiUsecase(true), fetchDao
                        , followDao, false, true).toMediator2()
        every {
            fetchDao.contentUrlForPage(entityId)
        } returns "contenturl"
        val f1 = FetchInfoEntity(entityId, location, section = section)
        every {
            fetchDao.insIgnore(f1)
        } returns 1L

        every {
            fetchDao.lookupPage(entityId, section)
        } returns FeedPage(entityId, "curl", Constants.HTTP_GET)

        every {
            fetchDao.fetchInfo(entityId, location, section)
        } returns FetchInfoEntity(entityId, location, "oe", lastViewDestroyTs = System.currentTimeMillis(), section = section, fetchInfoId = fetchId)

        every {
            fetchDao.cardCount(fetchId)
        } returns 0

        basicCardsUsecase.execute(bundleOf("page" to 0))

        verify {
            fetchDao.insIgnore(f1)
            fetchDao.fetchInfo(entityId, location, section)
            fetchDao.cardCount(fetchId)
            fetchDao.lookupPage(entityId, section)
        }
        confirmVerified(fetchDao, pullDao)
    }

    @Test
    fun testBasicCardsUsecaseNP() {
        //arrange
        val fe = slot<FetchInfoEntity>()
        val l = slot<List<PostEntity>>()
        val id = "c1"
        val location = "list"
        val basicCardsUsecase = NPUsecase(
                id, location, section, false,
                FetchCacheUsecase(cacheProvider, MockableApiCallUsecase(nlResponseWrapper = nlResponseWrapper), emptyTransformList),
                fetchDao, followDao, pullDao, cssRepo).toMediator()
        every {
            fetchDao.appendNextPage(
                    fe = capture(fe),
                    postEntityList = capture(l), followEntityDao = followDao,
                    requestUrl = TEST_CONTENT_URL
            )
        } answers { Unit }

        val key = slot<String>()
        val byteData = slot<ByteArray>()
        every {
            cache.get(capture(key))
        } returns Observable.error(Exception(Constants.NOT_FOUND_IN_CACHE))
        every {
            cache.addOrUpdate(capture(key), capture(byteData))
        } returns Observable.just(true)

        every {
            fetchDao.fetchInfo(id, location, section)
        } returns FetchInfoEntity(id, location, "http://api-news/pg/2", 0, "http://api-news/pg/2", section = section)
        every {
            fetchDao.lookupPage(id, section)
        } returns FeedPage(id, location, "GET")
        every { pullDao.incrementLastPullInfoPageCount(id, section) } returns Unit

        //act
        basicCardsUsecase.execute(bundleOf("page" to 1))
        //assert
        verify {
            fetchDao.fetchInfo(id, location, section)
            fetchDao.lookupPage(id, section)
            fetchDao.appendNextPage(fe.captured, l.captured, followDao, TEST_CONTENT_URL)
            pullDao.incrementLastPullInfoPageCount(id, section)
        }
        confirmVerified(fetchDao)
    }

    @Test
    fun testReadCards() {
        /*val url = "http://api-news"
        val post1 = PostEntity(id = "id", type = "news").toCard()!!
        val post2 = PostEntity(id = "id2", type = "news").toCard()!!
        val ld = MutableLiveData<PagedList<Card?>>()
        val ld2 = MutableLiveData<List<CreatePost>>()
        every { fetchDao.itemsMatching(url, location, section).toLiveData(readCardPageConfig())
                as LiveData<PagedList<Card?>> } returns ld
        every { cpDao.localCards() } returns ld2

        val usecase = ReadCardsUsecase(url, location, section, fetchDao)

        usecase.execute(Bundle.EMPTY)

        val observer = usecase.data().test()
        val list = listOf(post1, post2)
        ld.postValue(pagedListOf(list))
        ld.postValue(pagedListOf(list)) //duplicates would be filtered out
        observer.assertValueCount(1)
        val ls = observer.values().get(0).getOrNull()
        assertThat(ls).comparingElementsUsing(storyIdAndGroupType)
                .containsExactly(post1, post2)
                .inOrder()


        //now post local cards... nothing happens
        val lc1 = CreatePost(cp1)
        val lc2 = CreatePost(cp1.copy(cpId = cp1.cpId+1))
        val localCards = listOf(lc1, lc2)
        ld2.postValue(localCards)

        observer.assertValueCount(1)*/
    }

    @Test
    fun testLDScan() {
        val initial: Pair<Int?, Throwable?> = null to null
        val source = MutableLiveData<Result<Int>>()
        val acc = source.scan(initial) { acc1, r ->
            if (r.isSuccess) {
                acc1.copy(first = r.getOrNull())
            } else {
                acc1.copy(second = r.exceptionOrNull())
            }
        }
        getValue(acc, 0)// to begin emitting
        val throwable = Throwable("oops")
        source.postValue(Result.failure(throwable))
        source.postValue(Result.success(2))
        source.postValue(Result.success(42)) // will overwrite
        val value = getValue(acc, 0)
        assertThat(value?.first).isEqualTo(42)
        assertThat(value?.second).isEqualTo(throwable)
    }


    @Test
    fun testToggleUsecase() {
        val fe = FollowSyncEntity(ActionableEntity("id", "et", null, displayName = "n", entityImageUrl = ""), FollowActionType.FOLLOW)
        every {
            followRepo.toggleFollow(fe.actionableEntity, FollowActionType.FOLLOW.name)
        } returns Observable.just(1)


        val useCase = ToggleFollowUseCase(followRepo)

        useCase.invoke(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to fe.actionableEntity,
                ToggleFollowUseCase.B_ACTION to FollowActionType.FOLLOW.name)).subscribe()

        verify {
            followRepo.toggleFollow(fe.actionableEntity, FollowActionType.FOLLOW.name)
        }
    }


    @Test
    fun testAnswerPollUsecase() {
        val vote = Vote("uid", "pollID", "optionId")
        val dummyAsset = PollAssetResponse(interactionUrl = "")
        every { voteDao.insReplace(vote) } returns Unit
        every {
            answerPollApi.postPollResponse("url", "optionId")
        } returns Observable.just(ApiResponse<PollAssetResponse>().apply { data = dummyAsset ;code = HttpURLConnection.HTTP_OK})

        AnswerPollUsecase(voteDao, answerPollApi)(bundleOf(AnswerPollUsecase.B_VOTE to vote,
                AnswerPollUsecase.B_INTERACTIONURL to "url")).test().assertResult("")

        verify { voteDao.answerSubmitted(vote, dummyAsset.toPollAsset()) }
        confirmVerified(voteDao)
    }

    @Test
    fun testAnswerPollUsecaseOverwritesOptionFromServerResponse() {
        val vote = Vote("uid", "pollID", "optionId")
        val dummyAsset = PollAssetResponse(interactionUrl = "", selectedOption = "optionIdBE")
        every { voteDao.insReplace(vote) } returns Unit
        every {
            answerPollApi.postPollResponse("url", "optionId")
        } returns Observable.just(ApiResponse<PollAssetResponse>().apply {
            data = dummyAsset
            status = Status("403", "nah", null)
            code = 403
        })

        AnswerPollUsecase(voteDao, answerPollApi)(bundleOf(AnswerPollUsecase.B_VOTE to vote,
                AnswerPollUsecase.B_INTERACTIONURL to "url")).test().assertResult("nah")

        verify { voteDao.answerSubmitted(vote.copy(optionId = "optionIdBE"), dummyAsset.toPollAsset()) }
        confirmVerified(voteDao)
    }

    @Test
    fun testAnswerPollUsecaseApiFailureDoesnotInsertToDB() {
        val vote = Vote("uid", "pollID", "optionId")
        every { voteDao.insReplace(vote) } returns Unit
        val throwable = Throwable("testing")
        every {
            answerPollApi.postPollResponse("url", "optionId")
        } returns Observable.error(throwable)

        AnswerPollUsecase(voteDao, answerPollApi)(bundleOf(AnswerPollUsecase.B_VOTE to vote,
                AnswerPollUsecase.B_INTERACTIONURL to "url")).test().assertFailure(Throwable::class.java)

        confirmVerified(voteDao)
    }

    @Test
    fun testAnswerPollUsecaseInvalidInput() {
        val result = runCatching { assertThat(AnswerPollUsecase(voteDao, answerPollApi)(bundleOf())) }
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("missing bundle arg")
    }


    @Test
    fun testCleanupFetchList() {
        CleanUpFetchUsecase(entityId, location, section, fetchDao, CancelNetworkSDKRequestsUsecase(""))(bundleOf()).test()
                .assertResult(true)
        val slot = slot<Long>()
        verify { fetchDao.fullCleanupFetch(entityId, location, section) }
        confirmVerified(fetchDao)
    }


    @Test
    fun testCleanupFetchDetail() {
        CleanUpFetchUsecase(entityId, "detail", section, fetchDao, CancelNetworkSDKRequestsUsecase(""))(bundleOf()).test()
                .assertResult(true)
        val slot = slot<Long>()
        verify { fetchDao.fullCleanupFetch(entityId, "detail", section) }
        confirmVerified(fetchDao)
    }

    @Test
    fun testCloneFetchReturnsFalseIfNoEntityId() {
        CloneFetchForNewsDetailUsecase(section, fetchDao, "detail").invoke(bundleOf()).test().assertResult(Constants.EMPTY_STRING)
        confirmVerified(fetchDao)
    }

    @Test
    fun testCloneFetchReturnsFalseItCannotFindFetchInfo() {
        val location1 = "list"
        every { fetchDao.fetchInfo("1", location1, section) } returns null

        CloneFetchForNewsDetailUsecase(section, fetchDao, location1).invoke(bundleOf(CloneFetchForNewsDetailUsecase.B_ENTITY_ID to "1")).test().assertResult(Constants.EMPTY_STRING)
        verify {
            fetchDao.fetchInfo("1", location1, section)
        }
        confirmVerified(fetchDao)
    }

    @Test
    fun testCloneFetchReturnsNormalFlow() {
        val entityId1 = "1"
        val location1 = "list"
        val clonedLocation  = CloneFetchForNewsDetailUsecase.cloneLocationForDetail(location1)
        every { fetchDao.fetchInfo(entityId1, location1, section) } returns FetchInfoEntity(entityId1, "location", fetchInfoId = 1, section = section)

        CloneFetchForNewsDetailUsecase(section, fetchDao, location1).invoke(bundleOf(CloneFetchForNewsDetailUsecase.B_ENTITY_ID to entityId1)).test().assertResult(clonedLocation)

        verify {
            fetchDao.fetchInfo(entityId1, location1, section)
            fetchDao.cloneFetchForLocation(1, clonedLocation, section)
        }
        confirmVerified(fetchDao)
    }


    @Test
    fun testMenuFetchAndSave() {
        val d = MenuDictionaryEntity1()
        val dictionary = ApiResponse<MenuDictionaryEntity1>().apply {
            data = d
        }
        val V = "0"
        val L = "en"
        every {
            menuApi.getMenuDictionary(L, V)
        } returns Observable.just(dictionary)
        every { menuDao.clearAndinsert1(d) } returns 42
        val mm = MenuDictionaryEntity1(version = V)
        every { menuDao.fetchMenuMeta() } returns listOf(mm)
        FetchAndInsertMenuOptionsUsecase(menuDao, menuApi).invoke(bundleOf()).subscribe()

        verify {
            menuDao.fetchMenuMeta()
            menuApi.getMenuDictionary(L, version = V)
            menuDao.clearAndinsert1(d)
        }
        confirmVerified(menuApi, menuDao)
    }


    @Test
    fun testFollowFromMenuUsecase() {
        val slot = slot<FollowSyncEntity>()
        every {
            followDao.insReplace(capture(slot))
        } returns Unit

        every {
            followDao.getUnsyncedFollows()
        } returns emptyList()

        val pojo = FollowFromMenuUsecase.Pojo("eid", "ueht", "subnt", FollowActionType.FOLLOW)
        FollowFromMenuUsecase(followDao).invoke(FollowFromMenuUsecase.createBundle(pojo)).test()

        verify {
            followDao.insReplace(slot.captured)
            followDao.getUnsyncedFollows()
        }
        val pojo1 = with(slot.captured) {
            FollowFromMenuUsecase.Pojo(actionableEntity.entityId, actionableEntity.entityType, actionableEntity.entitySubType, action)
        }
        confirmVerified(followDao)
        assertThat(pojo).isEqualTo(pojo1)

    }

    @Test
    fun testFollowFromMenuUsecaseError() {
        FollowFromMenuUsecase(followDao).invoke(bundleOf()).test()
                .assertError(Throwable::class.java)
        confirmVerified(followDao)
    }


    @Test
    fun testPostL1UsecasePolls() {
        val URL = "U"
        every {
            menuDao.postUrl()
        } returns URL

        val payload = MenuPayload("i1", Format.HTML, SubFormat.STORY, UiType2.NORMAL, "oe",
                displayLocation = MenuLocation.LIST, option = "l1")
        PostL1Usecase(menuApi, menuDao).invoke(PostL1Usecase.create(payload)).test()
        verify {
            menuDao.postUrl()
            menuApi.postL1(URL, payload)
        }

        confirmVerified(menuDao, menuApi)

    }

    @Test
    fun `test NPCache Usecase when key present in cache`() {
        val npFetchCacheUsecase = FetchCacheUsecase(cacheProvider, fetchUsecase, emptyTransformList)

        val p1 = NLResp()
        p1.total = 1
        p1.rows = listOf(Member())
        val p1Wrapper = NLResponseWrapper(TEST_CONTENT_URL, p1, TEST_CONTENT_URL)
        val nlCacheData = CacheCompressUtils.compress(SerializationUtils.serialize(p1Wrapper))
        every {
            cache.get("key1")
        } returns Observable.just(nlCacheData)

        val arg1 = Bundle()
        arg1.putString("pageIndex", "1")

        every {
            fetchUsecase(arg1)
        } returns Observable.just(p1Wrapper)

        npFetchCacheUsecase.invoke("key1", arg1, CacheType.USE_NETWORK_IF_NO_CACHE).test()

        verify {
            cache.get("key1")
        }

        confirmVerified(cache)
    }

    @Test
    fun `test NPCache Usecase when key not present in cache`() {
        val npFetchCacheUsecase = FetchCacheUsecase(cacheProvider, fetchUsecase, emptyTransformList)

        val p2 = NLResp()
        p2.total = 1
        p2.rows = listOf(Member(1))

        val p2Wrapper = NLResponseWrapper(TEST_CONTENT_URL, p2, TEST_CONTENT_URL)

        val nlRespCachedByteArray = CacheCompressUtils.compress(SerializationUtils.serialize(p2Wrapper))

        every {
            cache.get("key2")
        } returns Observable.error(Exception(Constants.NOT_FOUND_IN_CACHE))

        val arg2 = Bundle()
        arg2.putString("pageIndex", "2")

        every {
            fetchUsecase(arg2)
        } returns Observable.just(p2Wrapper)

        npFetchCacheUsecase.invoke("key2", arg2, CacheType.USE_NETWORK_IF_NO_CACHE).test()

        verify {
            cache.get("key2")
            fetchUsecase(arg2)
            cache.addOrUpdate("key2", nlRespCachedByteArray)
        }

        confirmVerified(cache, fetchUsecase)
    }


    @Test
    fun testFollowChangesEmpty() {
        val ld = MutableLiveData<List<FollowSyncEntity>>()
        ld.value = emptyList()
        every {
            followDao.getFollowedNames(any())
        } returns ld

        val uc1 = GetLatestFollowUsecase(followDao)

        val obs1 = with(uc1){
            execute(bundleOf(Constants.BUNDLE_CREATION_TIME to 1L))
            data()
        }.test()
        obs1.assertValueCount(1)
        assertThat(obs1.values().first().getOrNull()).isEmpty()
    }

    @Test
    fun testFollowChangesDedups() {
        val l_a = listOf(FollowSyncEntity(ActionableEntity("id", "et", null, displayName = "n", entityImageUrl = ""), FollowActionType.FOLLOW))
        val d = l_a.first().actionableEntity.displayName
        val ld = MutableLiveData<List<FollowSyncEntity>>()
        every {
            followDao.getFollowedNames(any())
        } returns ld
        ld.value = l_a
        ld.value = l_a

        val uc1 = GetLatestFollowUsecase(followDao)

        val obs1 = with(uc1) {
            execute(bundleOf(Constants.BUNDLE_CREATION_TIME to 1L))
            data()
        }.test()
        obs1.assertValueCount(1)
        assertThat(obs1.values().first().getOrNull()).isEqualTo(l_a)
    }


    @Test
    fun testreadLimited() {
        val ld1 = MediatorLiveData<Result0<EitherList<Any>>>()

        every {
            mockReadLimitedCardUsecase.execute(any())
        } returns true

        every {
            mockReadLimitedCardUsecase.data()
        } returns ld1

        val obs = with(mockReadLimitedCardUsecase) {
            execute(Bundle.EMPTY)
            data().test()
        }
        val gson = CardDeserializer.gson(null, object: InvalidCardsLogger{
            override fun log(message: String, pojo: Any) {

            }
        })
        val pe = gson.fromJson(sampleSaveCarousel, PostEntity::class.java)

        val pCard = pe.toCard(1)!!
        obs.assertValueCount(0)
        ld1.postValue(Result0.success(EitherList(pagedListOf(listOf(pCard)) as PagedList<Any?>)))
        val CNT = 6
        obs.assertValueCount(1)
        val posts = obs.values().first()
        if (posts.isSuccess) {

            posts.getOrNull()?.let {
                assertThat( it.getList()?.filterIsInstance<CommonAsset>()
                        ?.map { it.i_counts()?.STORY?.value })
                        ?.containsExactly(CNT.toString())
            }
        } else fail("expected success")
    }

    @Test
    fun testgetlatestuploadedposts_dedupes() {
        val ld = MutableLiveData<CreatePostEntity?>()
        every { cpDao.latestUploadedPostNotInsertedInForyouNewerThan(any()) } returns ld
        val usecase = GetLatestUploadedPostUsecase(cpDao).also {
            it.execute(bundleOf(Constants.BUNDLE_CREATION_TIME to 0))
        }
        val observer = usecase.data().test()

        ld.value = CreatePostEntity(cpId = 1, uiMode = CreatePostUiMode.ALL, language = null)
        ld.value = CreatePostEntity(cpId = 2, uiMode = CreatePostUiMode.ALL, language = null)
        ld.value = CreatePostEntity(cpId = 2, uiMode = CreatePostUiMode.ALL, language = null)
        ld.value = CreatePostEntity(cpId = 3, uiMode = CreatePostUiMode.ALL, language = null)

        assertThat(observer.values().map {
            it.getOrNull()
        }.map {
            it?.cpId
        }).containsExactly(1, 2, 3)
                .inOrder()

        verify { cpDao.latestUploadedPostNotInsertedInForyouNewerThan(any()) }
        confirmVerified(cpDao)
    }


    @Test
    fun testCacheUsecase_NoCache() {
        val fetchCacheUsecase = getMockableCacheApiUsecase(false, nlResponseWrapper)
        val testObs = fetchCacheUsecase.invoke("k1", Bundle.EMPTY, CacheType.NO_CACHE).test()
        testObs.assertValue(nlResponseWrapper)

    }

    @Test
    fun testCacheUsecase_IgnoreCacheAndUpdate() {
        val fetchCacheUsecase = getMockableCacheApiUsecase(false, nlResponseWrapper)
        every { cache.get("k1") } returns Observable.fromCallable { cacheResp }

        val testObs = fetchCacheUsecase.invoke("k1", Bundle.EMPTY, CacheType.IGNORE_CACHE_AND_UPDATE).test()
        testObs.assertValue(nlResponseWrapper)

    }
    @Test
    fun testCacheUsecase_UseCacheAndUpdate() {
        val fetchCacheUsecase = getMockableCacheApiUsecase(false, nlResponseWrapper)
        every { cache.get("k1") } returns Observable.fromCallable { cacheResp }

        val testObs = fetchCacheUsecase.invoke("k1", Bundle.EMPTY, CacheType.USE_CACHE_AND_UPDATE).test()
        testObs.assertValueCount(2)
                .assertValueAt(0) { it == cacheNlResponseWrapper }
                .assertValueAt(1) {it == nlResponseWrapper}
    }

    @Test
    fun testCacheUsecase_UseCacheAndUpdate_cacheError() {
        val fetchCacheUsecase = getMockableCacheApiUsecase(false, nlResponseWrapper)
        val exception = Exception("something")
        every { cache.get("k1") } returns Observable.error(exception)

        val testObs = fetchCacheUsecase.invoke("k1", Bundle.EMPTY, CacheType.USE_CACHE_AND_UPDATE).test()
        testObs.assertValueCount(1)
                .assertValueAt(0) {it == nlResponseWrapper}
                .assertError(exception)
    }

    @Test
    fun testCacheUsecase_UseCacheAndUpdate_nwError() {
        val fetchCacheUsecase = getMockableCacheApiUsecase(true, nlResponseWrapper)
        every { cache.get("k1") } returns Observable.fromCallable { cacheResp }

        val testObs = fetchCacheUsecase.invoke("k1", Bundle.EMPTY, CacheType.USE_CACHE_AND_UPDATE).test()
        testObs.assertValueCount(1)
                .assertValueAt(0) {it == cacheNlResponseWrapper}
                .assertError { it.message == apiThrowable.message }
    }

    @Test
    fun testCacheUsecase_UseCacheAndUpdate_BothError() {
        val fetchCacheUsecase = getMockableCacheApiUsecase(true, nlResponseWrapper)
        val exception = Exception("something")
        every { cache.get("k1") } returns Observable.error(exception)

        val testObs = fetchCacheUsecase.invoke("k1", Bundle.EMPTY, CacheType.USE_CACHE_AND_UPDATE).test()
        testObs.assertValueCount(0)
                .assertError { it is CompositeException }
    }

    @Test
    fun test_mergeDelayError_withTakeUntil() {
        val plainCache = Observable.fromCallable { "c" }
        val delayedCache = plainCache.delay(1, TimeUnit.SECONDS)
        val plainCacheNwFallback = plainCache.map { it+"n" }
        val nw = Observable.fromCallable{"n"}
        val nwErrThenCache = nw.map { " "+(1/0) }.onErrorResumeNext { t: Throwable -> plainCacheNwFallback }
        val nwSucessThenNoCache = nw.onErrorResumeNext(plainCacheNwFallback)
        val nwLongerThanDelayedCache = nw.delay(2, TimeUnit.SECONDS)

        with(Observable.mergeDelayError(nwLongerThanDelayedCache, delayedCache)
                .takeUntil { it.contains("n") }
                .test()) {
            awaitTerminalEvent()
            assertResult("c", "n")
        }

        with(Observable.mergeDelayError(nwSucessThenNoCache, delayedCache)
                .takeUntil { it.contains("n") }
                .test()) {
            awaitTerminalEvent()
            assertResult("n")
        }

        with(Observable.mergeDelayError(nwErrThenCache, delayedCache)
                .takeUntil { it.contains("n") }
                .test()) {
            awaitTerminalEvent()
            assertResult("cn")
        }

with(Observable.mergeDelayError(nw, plainCache).takeUntil { it.contains("n") }.test()) {
            awaitTerminalEvent()
            assertResult("n")
        }

        with(Observable.mergeDelayError(plainCache, nw).takeUntil { it.contains("n") }.test()) {
            awaitTerminalEvent()
            assertResult("c", "n")
        }

        with(Observable.mergeDelayError(delayedCache, nw)
                .takeUntil { it.contains("n") }.test()) {
            awaitTerminalEvent()
            assertResult("n")
        }

    }

    // TODO(satosh.dhanyamraju): write test cases for new cachetype
    private fun getMockableCacheApiUsecase(shouldThrow: Boolean = false, resp: NLResponseWrapper? = null): FetchCacheUsecase {
        return FetchCacheUsecase(cacheProvider, MockableApiCallUsecase(shouldThrow, resp?:nlResponseWrapper), emptyTransformList)
    }
}