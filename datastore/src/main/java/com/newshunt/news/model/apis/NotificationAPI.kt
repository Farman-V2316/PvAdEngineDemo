/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationCTAResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author shrikant.agrawal
 */
interface NotificationAPI {

    @GET("api/v2/upgrade/dynamic/version?entity=NOTIFICATION_CTA")
    fun getNotificationCTA(@Query("version") version: String) : Observable<ApiResponse<NotificationCTAResponse>>
}