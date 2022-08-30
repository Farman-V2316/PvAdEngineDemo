/*
 *  * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.notification.InAppTemplateResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by kajal.kumari on 21/04/22.
 */
interface InAppTemplateApi {
    @GET("api/v2/upgrade/dynamic/version?entity=NOTIFICATION_COLOR_TEMPLATE")
    fun getTemplates(@Query("version") version: String?): Observable<ApiResponse<InAppTemplateResponse>>
}