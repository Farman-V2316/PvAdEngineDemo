/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.asset.PollAssetResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * @author satosh.dhanyamraju
 */
interface AnswerPollApi {
    @POST
    fun postPollResponse(@Url url: String, @Query("id") selectionOption: String): Observable<ApiResponse<PollAssetResponse>>
}