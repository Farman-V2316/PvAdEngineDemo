/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.model.internal.rest

import com.newshunt.dataentity.common.model.EntityNERBottomSheetWebResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * API class for bottomsheet description webview for NERs and TPV profiles.
 * <p>
 * Created by aman.roy on 05/30/2022.
 */

interface EntityNERBottomSheetWebAPI {
    @GET
    fun getNERWebBottomSheetForEntity(@Url url: String): Observable<ApiResponse<EntityNERBottomSheetWebResponse>>
}