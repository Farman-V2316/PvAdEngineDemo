/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.ContactsSyncPayload
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Contact retrofit API declaration
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
interface ContactSyncAPI {
    @POST("cs/formatted")
    fun syncContacts(@Body payload: ContactsSyncPayload): Observable<ApiResponse<Any?>>
}