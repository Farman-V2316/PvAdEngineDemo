/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.model.service

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.HandleAvailabilityResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

interface HandleAPI {

    @GET("v2/accounts/me/handle/available")
    fun checkHandle(@Query("handle") handle: String): Observable<ApiResponse<Any?>>
}

/**
 * Any service that requires handle to be validated before assigning.
 */
interface HandleValidatorService {
    fun checkHandle(handle: String): Observable<HandleAvailabilityResponse>
}

class HandleValidatorServiceImpl @Inject constructor(val api: HandleAPI) : HandleValidatorService {

    override fun checkHandle(handle: String): Observable<HandleAvailabilityResponse> {
        return api.checkHandle(handle)
                .map {
                    HandleAvailabilityResponse(handle, it)
                }
    }
}