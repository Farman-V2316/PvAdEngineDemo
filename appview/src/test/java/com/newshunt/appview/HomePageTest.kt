/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.news.model.repo.PageSyncRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.StoreHomePagesUsecase
import com.newshunt.news.model.usecase.toMediator
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author shrikant.agrawal
 */
@RunWith(AndroidJUnit4::class)
class HomePageTest {

  @get:Rule
  val rule = InstantTaskExecutorRule()

  private lateinit var pageSyncRepo: PageSyncRepo

  @Before
  fun setUp() {
    pageSyncRepo= PageSyncRepo("news")
    CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
    SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
  }

  @After
  fun tearDown() {
    SocialDB.closeConnection()
  }

  @Test
  fun getHomePagesTest() {
    val storeHomePagesUsecase = StoreHomePagesUsecase(pageSyncRepo)
    val med = storeHomePagesUsecase.toMediator()
    med.execute("")
    med.data().observeForever(Observer {
      println(it.isSuccess)
      println(it.isFailure)
      println(it.getOrNull()?.version?:"null")
    })

  }
}