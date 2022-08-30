/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEventsConfigResponse
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * Retrofit API for fetching AppsFlyer Event config
 * <p>
 * Created by srikanth.ramaswamy on 09/17/2018.
 */
interface AppsFlyerEventConfigAPI {
    @GET("api/v1/upgrade/dynamic/version?entity=APPSFLYER")
    fun getEventConfig() : Observable<ApiResponse<AppsFlyerEventsConfigResponse>>
}