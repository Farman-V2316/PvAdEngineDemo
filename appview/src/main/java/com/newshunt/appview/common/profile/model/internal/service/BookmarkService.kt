/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.internal.service

import com.newshunt.appview.common.profile.model.internal.rest.BookmarksAPI
import com.newshunt.appview.common.profile.model.internal.rest.SyncBookmarksAPI
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.model.entity.BookmarkBody
import com.newshunt.dataentity.model.entity.BookmarkList
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Service declarations and implementations for Bookmark related APIs
 *
 * Created by srikanth.ramaswamy on 01/02/2020
 */
interface SyncBookmarksService {
    fun syncBookmarks(): Observable<MultiValueResponse<BookmarkBody>>
}

class SyncBookmarksServiceImpl @Inject constructor(private val syncBookmarksAPI: SyncBookmarksAPI): SyncBookmarksService {
    override fun syncBookmarks(): Observable<MultiValueResponse<BookmarkBody>> {
        return syncBookmarksAPI.syncBookmarks()
                .map {
                    it.data
                }
    }
}

interface BookmarkService {
    fun bookmark(bookmarks: BookmarkList): Observable<Int>
    fun bookmarkLegacy(bookmarks: BookmarkList): Observable<Int>
}

class BookmarkServiceImpl @Inject constructor(private val bookmarkAPI: BookmarksAPI) : BookmarkService {
    override fun bookmark(bookmarks: BookmarkList): Observable<Int> {
        return bookmarkAPI.bookmark(bookmarks).map { it.code }
    }

    override fun bookmarkLegacy(bookmarks: BookmarkList): Observable<Int> {
        return bookmarkAPI.bookmarkLegacy(bookmarks).map {
            it.code
        }
    }
}