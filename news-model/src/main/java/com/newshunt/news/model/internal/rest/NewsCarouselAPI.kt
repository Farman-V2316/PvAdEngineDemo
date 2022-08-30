/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.internal.rest

import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface NewsCarouselAPI {

    @Headers("variableResolution: y")
    @POST
     fun getViewMoreNews2(
            @Url url: String,
            @Query("langCode") langCodes: String,
            @Query("edition") edition: String,
            @Query("appLanguage") appLanguage: String,
            @Body payload: Any): Observable<ApiResponse<MultiValueResponse<PostEntity>>>

}