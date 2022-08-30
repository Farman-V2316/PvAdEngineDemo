/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.view

import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.PrimaryAccountPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.usecase.UIWrapperUsecase
import com.newshunt.news.model.usecase.toUIWrapper
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import com.newshunt.appview.common.accounts.model.internal.rest.AccountAPI
import com.newshunt.appview.common.accounts.model.internal.service.AccountsLinkingService
import com.newshunt.appview.common.accounts.model.internal.service.AccountsLinkingServiceImpl
import com.newshunt.appview.common.accounts.model.usecases.FetchLinkedAccountsUsecase
import com.newshunt.appview.common.accounts.model.usecases.SelectPrimaryAccountUsecase
import com.newshunt.appview.common.accounts.view.fragment.AccountsLinkingFragment
import dagger.Component
import dagger.Module
import dagger.Provides

/**
 * Dagger dependency modules for Accounts linking features
 *
 * @author srikanth on 06/12/2020
 */

@Component(modules = [AccountsModule::class])
interface AccountsComponent {
    fun inject(accountsLinkFragment: AccountsLinkingFragment)
}

@Module
class AccountsModule {
    @Provides
    fun accountAPI(): AccountAPI {
        val userProfileUrl = NewsBaseUrlContainer.getUserServiceSecuredBaseUrl()
        return RestAdapterContainer.getInstance().getRestAdapter(userProfileUrl,
                Priority.PRIORITY_HIGHEST,
                null,
                HTTP401Interceptor())
                .create(AccountAPI::class.java)
    }

    @Provides
    fun accountsLinkingService(serviceImpl: AccountsLinkingServiceImpl): AccountsLinkingService {
        return serviceImpl
    }

    @Provides
    fun fetchLinkedAccountsUIWrapperUsecase(fetchLinkedAccountsUsecase: FetchLinkedAccountsUsecase): UIWrapperUsecase<LoginPayload, AvailableAccounts?> {
        return fetchLinkedAccountsUsecase.toUIWrapper()
    }

    @Provides
    fun primaryAccountsMediatorUsecase(primaryAccountUsecase: SelectPrimaryAccountUsecase): UIWrapperUsecase<PrimaryAccountPayload, UserLoginResponse?> {
        return primaryAccountUsecase.toUIWrapper()
    }
}