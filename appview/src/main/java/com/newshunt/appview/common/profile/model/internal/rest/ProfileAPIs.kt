/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.internal.rest

import com.newshunt.common.model.interceptor.CachingInterceptor
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.DeleteUserInteractionsPostBody
import com.newshunt.dataentity.model.entity.MyProfile
import com.newshunt.dataentity.model.entity.ProfileBaseAPIBody
import com.newshunt.dataentity.model.entity.UpdateProfileBody
import com.newshunt.dataentity.model.entity.UserProfile
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * All Profile APIs to be defined here in this file
 * <p>
 * Created by srikanth.ramaswamy on 06/26/2019.
 */

interface ProfileAPI {
    @POST("api/v2/user/profile")
    fun fetchUserProfile(@Header(CachingInterceptor.DevDhHeaders.CACHE_URL_PLAIN)
                         toCacheAgainstUrl: String?,
                         @Body postBody: ProfileBaseAPIBody): Observable<ApiResponse<UserProfile>>

    @GET("v2/accounts/me")
    fun fetchMyProfile(@Query("appLang") appLang: String): Observable<ApiResponse<MyProfile>>

    @POST("v2/profile/me/activity/delete")
    fun deleteActivities(@Body postBody: DeleteUserInteractionsPostBody):
            Observable<ApiResponse<Any>>

    @POST("v2/accounts/me/update")
    fun updateMyProfile(@Query("appLang") appLang: String,
                        @Body postBody: UpdateProfileBody): Observable<ApiResponse<MyProfile>>
}