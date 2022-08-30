/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author madhuri.pa
 */
interface TickerApi2 {

    @GET
    fun refreshTicker(@Url path: String): Observable<ApiResponse<MultiValueResponse<PostEntity>>>

}