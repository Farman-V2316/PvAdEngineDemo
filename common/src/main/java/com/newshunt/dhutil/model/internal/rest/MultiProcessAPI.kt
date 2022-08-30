/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.multiprocess.MultiProcessConfigurationResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by karthik.r on 11/12/18.
 */
interface MultiProcessAPI {

    @GET("/api/v2/upgrade/dynamic/version?entity=APP_MULTIPROCESS")
    fun getMultiProcessConfiguration(@Query("version") version: String):
            Observable<ApiResponse<MultiProcessConfigurationResponse>>
}