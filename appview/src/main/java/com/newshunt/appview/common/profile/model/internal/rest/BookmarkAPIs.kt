/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.model.entity.BookmarkBody
import com.newshunt.dataentity.model.entity.BookmarkList
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API Declaration for bookmark sync, add/remove bookmarks
 * <p>
 * Created by srikanth.ramaswamy on 01/02/2020.
 */
interface SyncBookmarksAPI {
    @GET("v2/profile/me/saved/items/all")
    fun syncBookmarks(): Observable<ApiResponse<MultiValueResponse<BookmarkBody>>>
}

interface BookmarksAPI {
    @POST("v2/profile/me/saved/list/operation")
    fun bookmark(@Body postBody: BookmarkList): Observable<ApiResponse<Any?>>

    @POST("v2/profile/me/saved/old/list/operation")
    fun bookmarkLegacy(@Body postBody: BookmarkList): Observable<ApiResponse<Any?>>
}