/*
 * Created by Rahul Ravindran at 8/1/20 5:19 PM
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.OEmbedResponse
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.asset.PostMeta
import com.newshunt.dataentity.common.asset.PostPollPojo
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.asset.RepostAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.CreatePostID
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.usecase.CpCreationUseCase
import com.newshunt.news.model.usecase.CpImageInsertUseCase
import com.newshunt.news.model.usecase.CpLocationInsertUseCase
import com.newshunt.news.model.usecase.CpPollInsertUseCase
import com.newshunt.news.model.usecase.CpPrivacyUseCase
import com.newshunt.news.model.usecase.CpRepostInsertUseCase
import com.newshunt.news.model.usecase.OEmbedAPI
import com.newshunt.news.model.usecase.OEmbedUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class CpUsecaseTest {
    @get:Rule
    val taskRule = InstantTaskExecutorRule()
    @MockK
    lateinit var dummyCPDao: CreatePostDao
    @MockK
    lateinit var dummyEmbdApi: OEmbedAPI

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        AppConfig.createInstance(AppConfigBuilder())
        MockKAnnotations.init(this,relaxUnitFun = true)
    }


    @Test
    fun `ombedusecase with empty url throw IAE`() {
        val oembedUC = OEmbedUsecase(dummyEmbdApi, dummyCPDao)
        try {
            oembedUC.invoke(bundleOf())
        } catch (e: IllegalStateException) {
            Truth.assertThat(e.message == "Embed url is empty")
        }
    }

    @Test
    fun `ombedusecase without cpID IAE`() {
        val oembedUC = OEmbedUsecase(dummyEmbdApi, dummyCPDao)
        try {
            oembedUC.invoke(bundleOf(
                    OEmbedUsecase.OEMBED_URL to "http://dummy.com"))
                    .test()
        } catch (e: IllegalStateException) {
            Truth.assertThat(e.message == "cpID not passed in bundle")
        }
    }

    @Test
    fun `ombedusecase api and dao call`() {
        val oembedUC = OEmbedUsecase(dummyEmbdApi, dummyCPDao)
        val dummyOEmbdRes  = OEmbedResponse(
                thumbnailWidth = 0,
                thunmbnailHeight = 0,
                thumbnailUrl = "https://google.com")
        val dummyCPEntity = CreatePostEntity(
                cpId = 1,
                language = "",
                postId = "",
                uiMode = CreatePostUiMode.POST)
        every {
            dummyEmbdApi.getOEmbedData("http://youtube.com")
        } returns Observable.just(dummyOEmbdRes)

        every{
            dummyCPDao.cpentityByID(1)
        } returns dummyCPEntity

        every {
            dummyCPDao.update(dummyCPEntity.copy(oemb = dummyOEmbdRes))
        } returns Unit

        oembedUC.invoke(bundleOf(
                OEmbedUsecase.OEMBED_URL to "http://youtube.com",
                OEmbedUsecase.ACTION_TYPE to OEmbedUsecase.Companion.OEMBED_ACTION_TYPE.UPDATE,
                CpImageInsertUseCase.POST_ID to 1L))
                .test()
                .assertNoErrors()
        verify {
            dummyEmbdApi.getOEmbedData("http://youtube.com")
            dummyCPDao.cpentityByID(1)
            dummyCPDao.update(dummyCPEntity.copy(oemb = dummyOEmbdRes))
        }
        confirmVerified(dummyEmbdApi, dummyCPDao)
    }

    @Test
    fun `oembedusecase api error and no dao call`() {
        val oembedUC = OEmbedUsecase(dummyEmbdApi, dummyCPDao)
        every {
            dummyEmbdApi.getOEmbedData("http://youtube.com")
        } returns Observable.error(IOException())

        oembedUC.invoke(bundleOf(
                OEmbedUsecase.OEMBED_URL to "http://youtube.com",
                CpImageInsertUseCase.POST_ID to 1L,
                OEmbedUsecase.ACTION_TYPE to OEmbedUsecase.Companion.OEMBED_ACTION_TYPE.UPDATE))
                .test()
                .assertError(IOException::class.java)

        verify {
            dummyEmbdApi.getOEmbedData("http://youtube.com")
        }
        verify(exactly = 0) {
            dummyCPDao.cpentityByID(any())
            dummyCPDao.update(any())
        }
        confirmVerified(dummyEmbdApi,dummyCPDao)
    }

    @Test @Ignore
    fun `location insert with postlocation null`() {
        val CPLocUC = CpLocationInsertUseCase(dummyCPDao)
        CPLocUC.invoke(bundleOf())
                .test()
                .assertErrorMessage("location not passed in bundle")
    }

    @Test
    fun `location insert with postlocation null should update cpentity row`() {
        val CPLocUC = CpLocationInsertUseCase(dummyCPDao)
        val dummyCPentity = CreatePostEntity(
                cpId = 1,
                language = "",
                postId = "",
                uiMode = CreatePostUiMode.POST)

        every {
            dummyCPDao.cpentityByID(any())
        } returns dummyCPentity

        every {
            dummyCPDao.update(dummyCPentity)
        } returns Unit

        CPLocUC.invoke(bundleOf(
                CpLocationInsertUseCase.LOC_POJO to PostCurrentPlace(
                        longitude = 1.0,
                        latitude = 1.0),
                CpImageInsertUseCase.POST_ID to 1L))
                .test()
                .assertValue(true)

        verify {
            dummyCPDao.cpentityByID(any())
            dummyCPDao.update(dummyCPentity)
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `location remove insert with removal of previous location data`() {
        val CPLocUC = CpLocationInsertUseCase(dummyCPDao)
        val dummyCPentity = CreatePostEntity(
                cpId = 1,
                language = "",
                postId = "",
                userLocation = PostCurrentPlace(),
                uiMode = CreatePostUiMode.POST)

        every {
            dummyCPDao.cpentityByID(any())
        } returns dummyCPentity

        every {
            dummyCPDao.update(entity = dummyCPentity)
        } returns Unit

        CPLocUC.invoke(bundleOf(
                CpLocationInsertUseCase.LOC_POJO to PostCurrentPlace(
                        longitude = 1.0,
                        latitude = 1.0),
                CpImageInsertUseCase.POST_ID to 1L))
                .test()

        verify {
            dummyCPDao.cpentityByID(any())
            dummyCPDao.update(withArg {
                Truth.assertThat(it.userLocation == null)
            })
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `location insert with cpId is empty`() {
        val CPLocUC = CpLocationInsertUseCase(dummyCPDao)
        CPLocUC.invoke(bundleOf(
                CpLocationInsertUseCase.LOC_POJO to PostCurrentPlace(
                        longitude = 1.0,
                        latitude = 1.0)
        )).test()
                .assertErrorMessage("cpID is null")
    }

    @Test
    fun `location insert with entity not present`() {
        val CPLocUC = CpLocationInsertUseCase(dummyCPDao)

        every {
            dummyCPDao.cpentityByID(any())
        } returns null

        CPLocUC.invoke(bundleOf(
                CpLocationInsertUseCase.LOC_POJO to PostCurrentPlace(
                        longitude = 1.0,
                        latitude = 1.0),
                CpImageInsertUseCase.POST_ID to 1L))
                .test()
                .assertResult(false)
    }

    @Test
    fun `location insert with entity`() {
        val CPLocUC = CpLocationInsertUseCase(dummyCPDao).toMediator2(true)
        val dummyCPentity = CreatePostEntity(
                cpId = 1,
                language = "",
                postId = "",
                uiMode = CreatePostUiMode.POST)
        every {
            dummyCPDao.cpentityByID(any())
        } returns dummyCPentity

        every {
            dummyCPDao.update(dummyCPentity)
        } returns Unit

        CPLocUC.execute(bundleOf(
                CpLocationInsertUseCase.LOC_POJO to PostCurrentPlace(
                        longitude = 1.0,
                        latitude = 1.0),
                CpImageInsertUseCase.POST_ID to 1L))

        verify {
            dummyCPDao.cpentityByID(any())
            dummyCPDao.update(dummyCPentity)
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `privacy update with null meta in bundle`() {
        val cpPrivUC = CpPrivacyUseCase(dummyCPDao)
        cpPrivUC.invoke(bundleOf())
                .test()
                .assertErrorMessage("privacy meta is null")
    }

    @Test
    fun `privacy update with no cpID in bundle`() {
        val cpPrivUC = CpPrivacyUseCase(dummyCPDao)
        cpPrivUC.invoke(bundleOf(
                "POST_META_RESULT" to PostMeta(
                        privacyLevel = PostPrivacy.PUBLIC,
                        allowComments = false)))
                .test()
                .assertErrorMessage("cpID id null")
    }

    @Test
    fun `repost update with no asset in bundle`() {
        val cpRepoUC = CpRepostInsertUseCase(dummyCPDao)
        cpRepoUC.invoke(bundleOf())
                .test()
                .assertErrorMessage("No repost asset")
    }

    @Test
    fun `repost update with no cpID in bundle`() {
        val cpRepoUC = CpRepostInsertUseCase(dummyCPDao)
        cpRepoUC.invoke(bundleOf(
                CpRepostInsertUseCase.REPOST_POJO to RepostAsset(linkAsset = null)))
                .test()
                .assertErrorMessage("invalid cpID")
    }

    @Test
    fun `cp update with no poll asset in bundle`() {
        val pollUpUC = CpPollInsertUseCase(dummyCPDao)
        pollUpUC.invoke(bundleOf())
                .test()
                .assertResult(false)
    }

    @Test
    fun `cp update with no cpId in bundle`() {
        val pollUpUC = CpPollInsertUseCase(dummyCPDao)
        pollUpUC.invoke(bundleOf(
                CpPollInsertUseCase.POLL_POJO to PostPollPojo()))
                .test()
                .assertResult(false)
    }

    @Test
    @Ignore
    fun `cp update with null cp entity returned`() {
        val pollUpUC = CpPollInsertUseCase(dummyCPDao).toMediator2(true)
        every {
            dummyCPDao.cpentityByID(any())
        } returns null

        pollUpUC.execute(bundleOf(
                CpPollInsertUseCase.POLL_POJO to PostPollPojo(),
                CpImageInsertUseCase.POST_ID to 1L
        ))

        verify {
            dummyCPDao.cpentityByID(1)
        }

        pollUpUC.data().test().assertResult(Result0.success(false))

        confirmVerified(dummyCPDao)
    }

    @Test
    fun `cp creation with no action`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        cpCreateUC.invoke(bundleOf())
                .test()
                .assertValue(CreatePostID.CP_ID_NOT_FOUND)
    }

    @Test
    fun `cp creation with entity null and action_type new`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.NEW))
                .test()
                .assertValue(CreatePostID.CP_ID_NOT_FOUND)
    }

    @Test
    fun `cp entity creation for new entry in db`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        val dummyCPEntity = CreatePostEntity(
                cpId = 1,
                language = "",
                uiMode = CreatePostUiMode.POST,
                postId = "1")
        every {
            dummyCPDao.replaceCP(dummyCPEntity)
        } returns arrayOf(1)

        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ENTITY to dummyCPEntity,
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.NEW))
                .test()
                .assertValue(CreatePostID(1, CreatePostID.CP_OP.ADD))

        verify {
            dummyCPDao.replaceCP(dummyCPEntity)
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `cp entity delete entry when cpId = -1 avoid db call`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        every {
            dummyCPDao.delete(any())
        } returns Unit
        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.REMOVE))
                .test()
                .assertValue(CreatePostID.CP_ID_NOT_FOUND)
        verify(exactly = 0){
            dummyCPDao.delete(any())
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `cp entity delete entry when cpID passed`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        every {
            dummyCPDao.delete(any())
        } returns Unit
        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.REMOVE,
                CpImageInsertUseCase.POST_ID to 1L))
                .test()
                .assertValue(CreatePostID.CP_ID_NOT_FOUND)
        verify{
            dummyCPDao.delete(any())
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `cp entity update with no text passed`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        val dummyCPEntity = CreatePostEntity(
                cpId = 1,
                language = "",
                postId = "post1",
                uiMode = CreatePostUiMode.POST)
        every {
            dummyCPDao.update(dummyCPEntity)
        } returns Unit
        every {
            dummyCPDao.cpentityByID(1)
        } returns dummyCPEntity

        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.UPDATE,
                CpImageInsertUseCase.POST_ID to 1L))
                .test()
                .assertNoErrors()

        verify{
            dummyCPDao.cpentityByID(1)
            dummyCPDao.update(any())
        }

        confirmVerified(dummyCPDao)
    }

    @Test
    fun `cp entity update with null entity from db`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        every {
            dummyCPDao.cpentityByID(any())
        } returns null
        every {
            dummyCPDao.update(any())
        } returns Unit
        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.UPDATE,
                CpImageInsertUseCase.POST_ID to 1L,
                CpCreationUseCase.CP_BODY to "Hello world"))
                .test()
                .assertValue(CreatePostID.CP_ID_NOT_FOUND)

        verify {
            dummyCPDao.cpentityByID(any())
        }
        verify(exactly = 0){
            dummyCPDao.update(any())
        }
        confirmVerified(dummyCPDao)
    }

    @Test
    fun `cp entity update with entity from db`() {
        val cpCreateUC = CpCreationUseCase(dummyCPDao)
        every {
            dummyCPDao.cpentityByID(any())
        } returns CreatePostEntity(
                cpId = 1,
                language = "",
                uiMode = CreatePostUiMode.POST,
                postId = "post1"
        )
        every {
            dummyCPDao.update(any())
        } returns Unit
        cpCreateUC.invoke(bundleOf(
                CpCreationUseCase.CP_ACTION_TYPE to CpCreationUseCase.Companion.ENTITY_ACTION_TYPE.UPDATE,
                CpImageInsertUseCase.POST_ID to 1L,
                CpCreationUseCase.CP_BODY to "Hello world"))
                .test()
                .assertValue(CreatePostID(1, CreatePostID.CP_OP.UPDATE))
        verify {
            dummyCPDao.cpentityByID(any())
            dummyCPDao.update(any())
        }
        confirmVerified(dummyCPDao)
    }
}