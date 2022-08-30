/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.ReadLimitedCardsUsecase
import com.newshunt.news.model.utils.test
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [27])
@RunWith(AndroidJUnit4::class)
class BookmarksDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    val be = BookmarkEntity(id = "postId",
            action = BookMarkAction.ADD,
            timestamp = System.currentTimeMillis(),
            format = Format.HTML.name,
            subFormat = SubFormat.ASTRO.name,
            syncStatus = SyncStatus.UN_SYNCED)

    val be2 = BookmarkEntity(id = "postId1",
            action = BookMarkAction.ADD,
            timestamp = System.currentTimeMillis(),
            format = Format.HTML.name,
            subFormat = SubFormat.STORY.name,
            syncStatus = SyncStatus.UN_SYNCED)

    val be3 = BookmarkEntity(id = "postId2",
            action = BookMarkAction.ADD,
            timestamp = System.currentTimeMillis(),
            format = Format.VIDEO.name,
            subFormat = SubFormat.TVVIDEO.name,
            syncStatus = SyncStatus.UN_SYNCED)

    lateinit var bookmarksDao: BookmarksDao
    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        bookmarksDao = instance.bookmarkDao()

    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }


    @Test
    fun testFormatCounts() {
        bookmarksDao.insReplace(listOf(be, be2, be3))
        assertThat(bookmarksDao.getBookmarkIds(BookMarkAction.ADD)).isNotEmpty()
        val f_v = Format.VIDEO.name
        val f_h = Format.HTML.name
        val f_p = Format.PHOTO.name


        val cur = bookmarksDao.countByFormatGrouped(BookMarkAction.ADD, listOf(f_v, f_h, f_p))
                .test()
                .assertValueCount(1)
                .values()
                .first()

        assertThat(cur).containsExactly(ReadLimitedCardsUsecase.FormatWithCount(f_h, 2), ReadLimitedCardsUsecase.FormatWithCount(f_v, 1))
    }
}