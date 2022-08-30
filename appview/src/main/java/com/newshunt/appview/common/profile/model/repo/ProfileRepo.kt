/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.repo

import com.newshunt.appview.common.profile.helper.createBookmarkService
import com.newshunt.appview.common.profile.helper.createProfileService
import com.newshunt.appview.common.profile.helper.createSyncBookmarksService
import com.newshunt.appview.common.profile.model.usecase.ClearBookmarksUsecase
import com.newshunt.appview.common.profile.model.usecase.PostBookmarksUsecase
import com.newshunt.appview.common.profile.model.usecase.ResetBookmarksUsecase
import com.newshunt.appview.common.profile.model.usecase.SyncBookmarksUsecase
import com.newshunt.appview.common.profile.model.usecase.SyncLegacyBookmarksUsecase
import com.newshunt.dataentity.model.entity.BookmarkList
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2

/**
 * Profile repository implementation
 * <p>
 * Created by srikanth.ramaswamy on 11/09/2019.
 */
object ProfileRepo {
    private val profileService by lazy {
        createProfileService()
    }

    private val resetBookmarksMediatorUC: MediatorUsecase<Unit, Boolean> by lazy {
        ResetBookmarksUsecase(ClearBookmarksUsecase(SocialDB.instance().bookmarkDao()),
                SyncBookmarksUsecase(createSyncBookmarksService(), SocialDB.instance().bookmarkDao())).toMediator2(true)
    }

    private val postBookmarksUsecase: MediatorUsecase<BookmarkList, Boolean> by lazy {
        PostBookmarksUsecase(createBookmarkService(),
                SocialDB.instance().bookmarkDao()).toMediator2(true)
    }

    private val syncLegacyBookmarkUsecase: MediatorUsecase<BookmarkList, Boolean> by lazy {
        SyncLegacyBookmarksUsecase(createBookmarkService(),
                SyncBookmarksUsecase(createSyncBookmarksService(), SocialDB.instance().bookmarkDao())).toMediator2(true)
    }

    private val syncBookmarkUsecase: MediatorUsecase<Boolean, Boolean> by lazy {
        SyncBookmarksUsecase(createSyncBookmarksService(), SocialDB.instance().bookmarkDao()).toMediator2(true)
    }

    fun postUnsyncdBookmarks() {
        postBookmarksUsecase.execute(BookmarkList(emptyList()))
    }

    fun resetBookmarks() {
        resetBookmarksMediatorUC.execute(Unit)
    }

    fun syncLegacyBookmarks() {
        syncLegacyBookmarkUsecase.execute(BookmarkList(emptyList()))
    }

    fun syncBookmarks(isForced: Boolean) {
        syncBookmarkUsecase.execute(isForced)
    }
}
