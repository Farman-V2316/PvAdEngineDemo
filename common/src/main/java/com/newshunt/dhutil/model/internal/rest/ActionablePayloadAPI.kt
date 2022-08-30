/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.actionablepayload.ActionablePayloadResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API to fetch actionable payload version data
 *
 * Created by karthik.r on 03/07/20.
 */
interface ActionablePayloadAPI {

    @GET("/api/v2/upgrade/actionable/payload")
    fun getActionablePayload(@Query("version") version: String):
            Observable<ApiResponse<ActionablePayloadResponse>>
}