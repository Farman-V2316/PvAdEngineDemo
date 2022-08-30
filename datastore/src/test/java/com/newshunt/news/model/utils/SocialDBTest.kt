/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.sqlite.SocialTypeConv
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
class SocialDBTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun instance() {
        Truth.assertThat(createInMryInst().isOpen).isTrue()
    }


    @Test
    fun closeConnection() {
        val instance = createInMryInst()
        SocialDB.closeConnection()
        val instance2 = createInMryInst()
        Truth.assertThat(instance.isOpen).isFalse()
        Truth.assertThat(instance2.isOpen).isTrue()
    }


    @Test
    fun testInstanceIsReused() {
        Truth.assertThat(createInMryInst()).isEqualTo(createInMryInst())
    }

    @Test
    fun `test cp dangling entries removed after three day threshold`() {
        val instance = createInMryInst()
        val dummyCPEntries = arrayOf(CreatePostEntity(
                language = "en",
                uiMode = CreatePostUiMode.POST,
                creationDate = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L),
                CreatePostEntity(
                        language = "en",
                        uiMode = CreatePostUiMode.POST,
                        creationDate = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L),
                CreatePostEntity(
                        language = "en",
                        uiMode = CreatePostUiMode.POST))
        val cpDao = instance.cpDao()
        cpDao.insReplace(dummyCPEntries.toList())
        Truth.assertThat(cpDao.rowCount() == 3)

        try {
            instance.openHelper.writableDatabase.execSQL(CreatePostDao.delete_dangling_cp_entries_new_session)
        } catch (e: Exception) {
            print("exception executing raw query")
            e.printStackTrace()
        }

        Truth.assertThat(cpDao.rowCount() == 1)
        Truth.assertThat(cpDao.test_all()[0].creationDate == dummyCPEntries[2].creationDate)
    }

    @Test
    fun testTypeConverterUsesDummyIfParsingFails() {
        val conv = SocialTypeConv()
        val post1Bytes = conv.peToByteArray(post1)
        // normal case
        assertThat(conv.byteArrayToPE(post1Bytes)).isEqualTo(post1)
        // error case
        assertThat(conv.byteArrayToPE(byteArrayOf(1)).id).isEqualTo(Constants.INVALID_POSTENTITY_ID)
    }

    private fun createInMryInst() = SocialDB.instance(ApplicationProvider.getApplicationContext(),
            true).also {
        it.fetchDao().allFetchData().observeForever {

        }
    }
}

