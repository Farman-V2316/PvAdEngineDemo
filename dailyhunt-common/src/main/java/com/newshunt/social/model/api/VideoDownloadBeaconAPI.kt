/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.socialfeatures.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author umesh on 28/05/19.
 */
interface VideoDownloadBeaconAPI {
    @POST("counter")
    fun hitVideoDownloadBeacon(
            @Query("id") id: String,
            @Query("type") type: String,
            @Query("namespace") namespace: String): retrofit2.Call<ApiResponse<Any>>
}
