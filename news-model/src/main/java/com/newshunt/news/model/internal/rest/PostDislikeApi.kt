/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.news.model.entity.MenuEntity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * @author satosh.dhanymaraju
 */
interface PostDislikeApi {
    @POST()
    fun postDislike(@Body menuEntity: MenuEntity, @Url url: String):
            Call<ApiResponse<Any>>
}