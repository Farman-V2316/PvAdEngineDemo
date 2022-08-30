/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.model.usecase.NLResp
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * @author satosh.dhanyamraju
 */
interface NewsApi {
    @GET
    fun getNews2(@Url url: String): Observable<Response<ApiResponse<NLResp>>>

    @POST
    fun postNews2(@Url url: String, @Body postsPayload: Any,
                  @Header(CardSeenStatusRepo.HEADER_CSS_BATCH_ID) cssBatchId: String?):
            Observable<Response<ApiResponse<NLResp>>>

    @POST("api/v2/posts/content/by/ids")
    fun contentByIds(@Body postsPayload: Any): Observable<Response<ApiResponse<NLResp>>>

}