/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.internal.service

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.internal.rest.LoginAPI
import io.reactivex.Observable

/**
 * Model wrapper for implementing Email Login API through BaseService
 *
 * @author arun.babu
 */
class LoginServiceImpl : LoginService {

    override fun login(loginPayload: LoginPayload, baseUrl: String):
            Observable<UserLoginResponse> {
        val loginApi = RestAdapterContainer.getInstance()
                .getRestAdapter(baseUrl, Priority.PRIORITY_HIGHEST, this)
                .create(LoginAPI::class.java)
        return loginApi.login(loginPayload).map { it.data }
    }
}

interface LoginService {

    fun login(loginPayload: LoginPayload, baseUrl: String): Observable<UserLoginResponse>
}

class LogoutServiceImpl : LogoutService {

    override fun logout(baseUrl: String): Observable<UserLoginResponse> {
        val loginApi = RestAdapterContainer.getInstance()
                .getRestAdapter(baseUrl, Priority.PRIORITY_HIGHEST, this)
                .create(LoginAPI::class.java)
        return loginApi.logout().map { it.data }
    }

}

interface LogoutService {

    fun logout(baseUrl: String): Observable<UserLoginResponse>
}

class VerifySessionServiceImpl : VerifySessionService {

    override fun verifySession(baseUrl: String): Observable<ApiResponse<UserLoginResponse?>> {
        val loginApi = RestAdapterContainer.getInstance()
                .getRestAdapter(baseUrl, Priority.PRIORITY_HIGHEST, this)
                .create(LoginAPI::class.java)
        return loginApi.verifySession()
    }
}

interface VerifySessionService {

    fun verifySession(baseUrl: String): Observable<ApiResponse<UserLoginResponse?>>
}


