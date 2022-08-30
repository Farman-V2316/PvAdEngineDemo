/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.viewmodel

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.group.model.usecase.ReadPendingApprovalCountsUsecase
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.ApprovalTab
import com.newshunt.news.model.usecase.MediatorUsecase
import javax.inject.Inject

/**
 * Handles:
 *   - Fetching the ApprovalTabsInfo config
 *   - Save the tab fetch info in group_feed table.
 *   - relay actions to approve/decline usecase?
 *
 * @author raunak.yadav
 */
class ApprovalsViewModel @Inject constructor(private val getApprovalTabsInfoUseCase: MediatorUsecase<Unit, List<ApprovalTab>>,
                                             private val readPendingApprovalCountsUsecase: ReadPendingApprovalCountsUsecase) : ViewModel(), ClickHandlingViewModel {

    val approvalTabsLiveData = getApprovalTabsInfoUseCase.data()
    val approvalStatusLD = getApprovalTabsInfoUseCase.status()
    val pendingApprovalLiveData  by lazy {
        readPendingApprovalCountsUsecase.data()
    }
    private val errorClickDelegate = ErrorClickDelegate(::fetchApprovalConfig)

    init {
        fetchApprovalConfig()
    }

    override fun onCleared() {
        getApprovalTabsInfoUseCase.dispose()
        super.onCleared()
    }

    fun fetchApprovalConfig() {
        getApprovalTabsInfoUseCase.execute(Unit)
    }

    override fun onViewClick(view: View, item: Any) {
        onViewClick(view, item, null)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        if (item is BaseError) {
            errorClickDelegate.onViewClick(view)
            return
        }
    }

    fun fetchPendingApprovalCounts(userId: String) {
        readPendingApprovalCountsUsecase.execute(userId)
    }

    fun retryGuestLogin(context: Context) {
        errorClickDelegate.retryLogin(context)
    }
}

class ApprovalViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
    @Inject lateinit var viewModel: ApprovalsViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return viewModel as T
    }
}