/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.upgrade.RegistrationSuccessBody
import com.newshunt.dataentity.model.entity.UserMigrationStatusResponse
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Migration state API declaration
 * <p>
 * Created by srikanth.ramaswamy on 01/02/2020.
 */
interface MigrationStatusAPI {
    @GET("v1/accounts/migration/state")
    fun checkMigrationState(): Observable<ApiResponse<UserMigrationStatusResponse>>

    @POST("/api/v1/accounts/registration/success")
    fun onRegistrationSuccess(@Body regSuccessBody: RegistrationSuccessBody): Observable<ApiResponse<UserLoginResponse?>>
}
