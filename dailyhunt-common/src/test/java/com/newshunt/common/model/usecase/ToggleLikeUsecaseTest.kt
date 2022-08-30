/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.NetworkSDK
import io.mockk.MockKAnnotations
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToggleLikeUsecaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()
    @io.mockk.impl.annotations.MockK
    lateinit var syncLikeUsecase: SyncLikeUsecase
    lateinit var interationDao : InteractionDao
    lateinit var cpDao: CreatePostDao

    lateinit var usecase: ToggleLikeUsecase
    @Before
    fun setUp() {
        val db = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        interationDao = db.interactionsDao()
        cpDao = db.cpDao()
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
        AppConfig.createInstance(AppConfigBuilder())
        NetworkSDK.init(ApplicationProvider.getApplicationContext(), null, false)
        usecase = ToggleLikeUsecase(interationDao, cpDao, syncLikeUsecase)
    }

    @Test
    fun `test sync like usecase is called`() {
        val args = ToggleLikeUsecase.args("eid", "TOPIC" , "LIKE")
        usecase.invoke(args).test()
        verify { syncLikeUsecase.invoke(any()) }
    }
}
