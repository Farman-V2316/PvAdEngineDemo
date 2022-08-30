/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.model.usecases

import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.PrimaryAccountPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.LoginResponse
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.newshunt.sso.model.entity.SSOResult
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Account linking related usecases can be written here
 *
 * @author srikanth on 06/12/2020
 */

/**
 * Usecase implementation to fetch the linked accounts for a given login payload.
 */
class FetchLinkedAccountsUsecase @Inject constructor(private val accountsService: com.newshunt.appview.common.accounts.model.internal.service.AccountsLinkingService)
    : Usecase<LoginPayload, ApiResponse<AvailableAccounts?>> {

    override fun invoke(payload: LoginPayload): Observable<ApiResponse<AvailableAccounts?>> {
        return accountsService.fetchLinkedAccounts(payload)
    }
}

/**
 * Usecase implementation to select a primary account
 */
class SelectPrimaryAccountUsecase @Inject constructor(private val accountsService: com.newshunt.appview.common.accounts.model.internal.service.AccountsLinkingService) : Usecase<PrimaryAccountPayload, ApiResponse<UserLoginResponse?>> {
    override fun invoke(payload: PrimaryAccountPayload): Observable<ApiResponse<UserLoginResponse?>> {
        return accountsService.selectPrimaryAcount(payload).map {
            it.data?.let {
                //User has selected a different account from the currently logged in session.
                SSO.getInstance().setSSOLoginSourceTracker(SSOLoginSourceType.ACCOUNTS_LINKING, LoginMode.USER_EXPLICIT)
                BusProvider.postOnUIBus(LoginResponse(SSOResult.SUCCESS, it))
            }
            it
        }
    }
}
