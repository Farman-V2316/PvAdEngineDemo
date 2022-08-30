/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import io.reactivex.Observable
import retrofit2.http.GET
import java.io.Serializable

/**
 * @author anshul.jain
 */
interface FetchUserProfilesAPI {

    @GET("v1/accounts/registered/list")
    fun getUserProfiles(): Observable<ApiResponse<FetchUserProfilesResponse>>
}

data class FetchUserProfilesResponse(val userImageList: List<String>?, val totalWeekCount:
String?) : Serializable