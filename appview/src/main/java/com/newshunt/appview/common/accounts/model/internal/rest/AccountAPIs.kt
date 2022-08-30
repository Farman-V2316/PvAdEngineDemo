/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.accounts.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.PrimaryAccountPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API definition for account linking REST APIs
 *
 * @author srikanth on 06/12/2020
 */
interface AccountAPI {
    @POST("v2/link/accounts/linkAccountVerification")
    fun fetchLinkedAccounts(@Body payload: LoginPayload): Observable<ApiResponse<AvailableAccounts?>>

    @POST("v2/link/accounts/selectMainAccount")
    fun selectPrimaryAccount(@Body payload: PrimaryAccountPayload): Observable<ApiResponse<UserLoginResponse?>>
}
