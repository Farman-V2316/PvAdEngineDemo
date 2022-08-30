package com.newshunt.sso.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * @author anshul.jain
 */

interface LoginAPI {

    @POST("v1/accounts/login")
    fun login(@Body body: LoginPayload): Observable<ApiResponse<UserLoginResponse>>

    @DELETE("v1/accounts/logout")
    fun logout(): Observable<ApiResponse<UserLoginResponse>>

    @GET("v1/accounts/vSession")
    fun verifySession(): Observable<ApiResponse<UserLoginResponse?>>


}