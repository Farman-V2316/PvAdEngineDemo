/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.internal.service

import android.net.Uri
import com.google.common.reflect.TypeToken
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.cachedapi.ReadFromCacheUsecase
import com.newshunt.common.helper.cachedapi.ReadFromCacheUsecaseController
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.DeleteUserInteractionsPostBody
import com.newshunt.dataentity.model.entity.MyProfile
import com.newshunt.dataentity.model.entity.ProfileBaseAPIBody
import com.newshunt.dataentity.model.entity.ProfileUserIdInfo
import com.newshunt.dataentity.model.entity.UpdateProfileBody
import com.newshunt.dataentity.model.entity.UserProfile
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.util.NewsConstants
import com.newshunt.appview.common.profile.model.internal.rest.ProfileAPI
import com.newshunt.dhutil.CacheProvider
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Profile service definitions
 * <p>
 * Created by srikanth.ramaswamy on 06/27/2019.
 */

private const val PATH_PROFILE = "profile"
private const val QUERY_USER_ID = "userId"

interface ProfileService {
    fun fetchUserProfile(postBody: ProfileBaseAPIBody, myUserId: ProfileUserIdInfo): Observable<ApiResponse<UserProfile>>
    fun fetchMyProfile(appLanguage: String): Observable<MyProfile>
    fun deleteUserInteractions(activitiyIds: List<String>? = null, clearAll: Boolean): Observable<Int>
    fun updateMyProfile(appLang: String, postBody: UpdateProfileBody): Observable<ApiResponse<MyProfile>>
}

class ProfileServiceImpl @Inject constructor(private val profileAPI: ProfileAPI,
                                             @Named("gatewayAPI") private val gatewayAPI: ProfileAPI) : ProfileService {
    private val readFromCacheUsecase: ReadFromCacheUsecase<ApiResponse<UserProfile>>

    init {
        val type = object : TypeToken<ApiResponse<UserProfile>>() {
        }.type
        val cacheApiCacheRx = CachedApiCacheRx(CacheProvider.getCachedApiCache(NewsConstants.HTTP_FEED_CACHE_DIR))
        readFromCacheUsecase = ReadFromCacheUsecaseController(cacheApiCacheRx, type)
    }

    override fun fetchUserProfile(postBody: ProfileBaseAPIBody,
                                  myUserId: ProfileUserIdInfo): Observable<ApiResponse<UserProfile>> {
        val cacheUrl = buildCacheUrlForProfileBaseAPI(postBody, myUserId)
                ?: return fetchUserProfileNW(null, postBody.createServerPostBody())
        return Observable.mergeDelayError(fetchUserProfileCache(cacheUrl), fetchUserProfileNW
        (cacheUrl, postBody.createServerPostBody()))
    }

    override fun fetchMyProfile(appLanguage: String): Observable<MyProfile> {
        return profileAPI.fetchMyProfile(appLanguage)
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    it.data
                }
    }

    override fun deleteUserInteractions(activitiyIds: List<String>?, clearAll: Boolean): Observable<Int> {
        val postBody = DeleteUserInteractionsPostBody(clearAll, if (!clearAll) activitiyIds else null)
        return profileAPI.deleteActivities(postBody).map {
            it.code
        }
    }

    override fun updateMyProfile(appLang: String, postBody: UpdateProfileBody): Observable<ApiResponse<MyProfile>> {
        return profileAPI.updateMyProfile(appLang, postBody)
                .map {
                    it
                }
    }

    private fun buildCacheUrlForProfileBaseAPI(postBody: ProfileBaseAPIBody,
                                               myUserId: ProfileUserIdInfo): String? {
        //Use cached API only for his profile. Never cache third party profiles.
        return if (!CommonUtils.equals(postBody.userId, myUserId.userId)) {
            null
        } else {
            Uri.Builder().encodedPath(NewsBaseUrlContainer.getApplicationUrl())
                    .appendPath(Constants.URL_PATH_API)
                    .appendPath(Constants.URL_PATH_API_V2)
                    .appendPath(PATH_PROFILE)
                    .appendQueryParameter(QUERY_USER_ID, myUserId.userId)
                    .appendQueryParameter(Constants.URL_QUERY_APP_LANG, postBody.appLang).build()
                    .toString()
        }
    }

    //Make network request to fetch the user's profile
    private fun fetchUserProfileNW(cacheUrl: String?,
                                   postBody: ProfileBaseAPIBody): Observable<ApiResponse<UserProfile>> {
        return gatewayAPI.fetchUserProfile(cacheUrl, postBody).map {
                ApiResponseUtils.throwErrorIfDataNull(it)
                it.cachedApiResponseSource = CachedApiResponseSource.NETWORK
                it
        }
    }

    //Fetch the user's profile from disk cache
    private fun fetchUserProfileCache(cacheUrl: String): Observable<ApiResponse<UserProfile>> {
        return readFromCacheUsecase.get(cacheUrl).map {
            ApiResponseUtils.throwErrorIfDataNull(it)
            it
        }
    }
}