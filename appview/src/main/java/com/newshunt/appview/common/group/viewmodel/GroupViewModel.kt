/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.R
import com.newshunt.appview.common.group.getPreferredApprovalTab
import com.newshunt.appview.common.group.model.usecase.ReadGroupInfoUsecase
import com.newshunt.appview.common.group.model.usecase.ReadPendingApprovalCountsUsecase
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import javax.inject.Inject
import javax.inject.Named

/**
 *  View model to work with group detail use cases
 * <p>
 * Created by srikanth.ramaswamy on 09/18/2019.
 */
class GroupViewModel @Inject constructor(@Named("fetchGroupInfoMediatorUC")
                                         private val fetchGroupInfoMediatorUC: MediatorUsecase<GroupBaseInfo, GroupInfo>,
                                         @Named("joinGroupMediatorUC")
                                         private val joinGroupMediatorUC: MediatorUsecase<GroupBaseInfo, GroupInfo>,
                                         @Named("leaveGroupMediatorUC")
                                         private val leaveGroupMediatorUC: MediatorUsecase<GroupBaseInfo, Boolean>,
                                         private val readGroupInfoMediatorUC: ReadGroupInfoUsecase,
                                         @Named("syncPendingApprovalsMediatorUC")
                                         private val syncPendingApprovalsMediatorUC: MediatorUsecase<String, Boolean>,
                                         private val readPendingApprovalCountsMediatorUC: ReadPendingApprovalCountsUsecase) :
        ViewModel(), ClickHandlingViewModel {
    private var groupInfo: GroupInfo? = null

    val fetchGroupLiveData by lazy {
        fetchGroupInfoMediatorUC.data()
    }

    val showActionBar: MutableLiveData<Boolean> = MutableLiveData()
    var referrerFlow: PageReferrer? = null

    val joinGroupLiveData by lazy {
        joinGroupMediatorUC.data()
    }

    val readGroupMediatorLD: LiveData<Result0<GroupInfo?>> = Transformations.map(readGroupInfoMediatorUC.data()) {
        if(it.isSuccess) {
            groupInfo = it.getOrNull()
        }
        it
    }

    val leaveGroupLiveData by lazy {
        leaveGroupMediatorUC.data()
    }

    val readPendingApprovalsLD by lazy {
        readPendingApprovalCountsMediatorUC.data()
    }

    val fetchStatusLD by lazy {
        fetchGroupInfoMediatorUC.status()
    }

    init {
        showActionBar.value = false
    }

    private var requestedGroup: GroupBaseInfo? = null
    private val errorClickDelegate = ErrorClickDelegate(::fetch)

    fun fetchGroupInfo(groupId: String, userId: String) {
        requestedGroup = makeGroupBaseInfo(groupId, userId, null)
        fetch()
    }

    fun fetchGroupInfoWithHandle(handle: String, userId: String) {
        requestedGroup = makeGroupBaseInfo(null, userId, handle)
        fetch()
    }

    fun joinGroup(groupId: String, userId: String) {
        joinGroupMediatorUC.execute(makeGroupBaseInfo(groupId, userId, null))
    }

    fun leaveGroup(groupId: String, userId: String) {
        leaveGroupMediatorUC.execute(makeGroupBaseInfo(groupId, userId, null))
    }

    fun fetch() {
        requestedGroup?.apply {
            fetchGroupInfoMediatorUC.execute(this)
            readGroupInfoMediatorUC.execute(this)
        }
    }

    fun setRererrerFlow(referrer: PageReferrer?) {
        referrerFlow = referrer
    }

    fun syncApprovalCounts(userId: String) {
        readPendingApprovalCountsMediatorUC.execute(userId)
        syncPendingApprovalsMediatorUC.execute(userId)
    }

    override fun onCleared() {
        fetchGroupInfoMediatorUC.dispose()
        joinGroupMediatorUC.dispose()
        leaveGroupMediatorUC.dispose()
        readGroupInfoMediatorUC.dispose()
        readPendingApprovalCountsMediatorUC.dispose()
        syncPendingApprovalsMediatorUC.dispose()
        super.onCleared()
    }

    override fun onViewClick(view: View, item: Any) {
        super.onViewClick(view, item)
        if (item is BaseError) {
            errorClickDelegate.onViewClick(view)
            return
        }

        when (view.id) {
            R.id.approval_main_card -> {
                handleApprovalClick(view, item)
            }
            else -> {
                //DO NOTHING
            }
        }
    }

    fun retryGuestLogin(context: Context) {
        errorClickDelegate.retryLogin(context)
    }

    private fun makeGroupBaseInfo(groupId: String?, userId: String, handle: String?): GroupBaseInfo {
        return GroupBaseInfo().apply {
            this.id = groupId ?: Constants.EMPTY_STRING
            this.userId = userId
            this.handle = handle
        }
    }

    private fun handleApprovalClick(view: View, item: Any) {
        if (item is ApprovalCounts) {
            groupInfo?.let {
                CommonNavigator.launchApprovalsActivity(view.context, getPreferredApprovalTab(item), PageReferrer(NhGenericReferrer.GROUP_HOME))
                AnalyticsHelper2.logApprovalCardClickEvent(PageReferrer(NhGenericReferrer.GROUP_HOME),
                        NewsExploreButtonType.APPROVAL_CARD, it.id, item.TOTAL_PENDING_APPROVALS?.value)
            }
        }
    }
}

class GroupDetailVMFactory @Inject constructor() : ViewModelProvider.Factory {
    @Inject lateinit var groupViewModel: GroupViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return groupViewModel as T
    }
}