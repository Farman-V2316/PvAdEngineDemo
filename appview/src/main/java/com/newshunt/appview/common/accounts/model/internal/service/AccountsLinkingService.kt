/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.model.internal.service

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.PrimaryAccountPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Retrofit service & implementation for the Account linking REST APIs
 *
 * @author srikanth on 06/12/2020
 */
interface AccountsLinkingService {
    fun fetchLinkedAccounts(payload: LoginPayload): Observable<ApiResponse<AvailableAccounts?>>
    fun selectPrimaryAcount(payload: PrimaryAccountPayload): Observable<ApiResponse<UserLoginResponse?>>
}

class AccountsLinkingServiceImpl @Inject constructor(private val accountAPI: com.newshunt.appview.common.accounts.model.internal.rest.AccountAPI) : AccountsLinkingService {
    override fun fetchLinkedAccounts(payload: LoginPayload): Observable<ApiResponse<AvailableAccounts?>> {
        return accountAPI.fetchLinkedAccounts(payload)
    }

    override fun selectPrimaryAcount(payload: PrimaryAccountPayload): Observable<ApiResponse<UserLoginResponse?>> {
        return accountAPI.selectPrimaryAccount(payload)
    }
}