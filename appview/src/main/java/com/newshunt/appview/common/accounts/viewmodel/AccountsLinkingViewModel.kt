/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.group.viewmodel.ErrorClickDelegate
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.ConflictingAccountId
import com.newshunt.dataentity.sso.model.entity.DHAccount
import com.newshunt.dataentity.sso.model.entity.DHAccountId
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.PrimaryAccountPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.news.model.usecase.UIWrapperUsecase
import javax.inject.Inject

/**
 * ViewModel implementation for AccountLinking logic
 *
 * @author srikanth on 06/12/2020
 */
class AccountsLinkingViewModel @Inject constructor(private val fetchLinkedAccountsUsecase: UIWrapperUsecase<LoginPayload, AvailableAccounts?>,
                                                   private val primaryAccountUsecase: UIWrapperUsecase<PrimaryAccountPayload, UserLoginResponse?>) : ViewModel(), ClickHandlingViewModel {

    private val errorClickDelegate = ErrorClickDelegate(::retryFetchingLinkedAccounts)
    val accountsLinkLiveData by lazy {
        fetchLinkedAccountsUsecase.data()
    }

    val primaryAccountLiveData by lazy {
        primaryAccountUsecase.data()
    }

    val primaryAccountStatusLiveData by lazy {
        primaryAccountUsecase.status()
    }

    val retryLiveData = MutableLiveData<Boolean>()

    fun fetchLinkedAccounts(payload: LoginPayload) {
        fetchLinkedAccountsUsecase.execute(payload)
    }

    fun retryFetchingLinkedAccounts() {
        retryLiveData.value = true
    }

    fun selectPrimaryAccount(selectedAccount: DHAccount?,
                             unselectedAccount: DHAccount?,
                             selectedConflictAccount: DHAccount?,
                             unselectedConflictAccount: DHAccount?) {
        val id1 = DHAccountId(selectedId = selectedAccount?.keyIdentifier,
                defunctId = unselectedAccount?.keyIdentifier)
        val id2 = if (selectedConflictAccount?.keyIdentifier == null && unselectedConflictAccount?.keyIdentifier == null) {
            null
        } else {
            ConflictingAccountId(DHAccountId(selectedConflictAccount?.keyIdentifier,
                    unselectedConflictAccount?.keyIdentifier))
        }

        val payload = PrimaryAccountPayload(id1, id2)
        primaryAccountUsecase.execute(payload)
    }

    override fun onViewClick(view: View, item: Any) {
        super.onViewClick(view)
        if(item is BaseError) {
            errorClickDelegate.onViewClick(view)
            return
        }
    }

    override fun onCleared() {
        fetchLinkedAccountsUsecase.dispose()
        super.onCleared()
    }
}

class AccountsLinkingVMFactory @Inject constructor() : ViewModelProvider.Factory {
    @Inject
    lateinit var accountsLinkingViewModel: AccountsLinkingViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return accountsLinkingViewModel as T
    }
}