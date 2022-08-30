package com.newshunt.dhutil.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.detailordering.DetailWidgetOrderingResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by karthik.r on 05/06/20.
 */
interface DetailWidgetOrderingAPI {

    @GET("/api/v2/upgrade/dynamic/version?entity=DETAIL_WIDGET_ORDERING_V3")
    fun getDetailWidgetOrdering(@Query("version") version: String):
            Observable<ApiResponse<DetailWidgetOrderingResponse>>
}