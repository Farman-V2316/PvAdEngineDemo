package com.newshunt.appview.common.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.group.model.usecase.ReadGroupInfoUsecase
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.SettingsPostBody
import com.newshunt.news.model.usecase.MediatorUsecase
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by helly.patel on 23/9/19.
 */

class GroupSettingsViewModel @Inject constructor(@Named("updateSettingsMediatorUC")
                                                 private val updateSettingsMediatorUC: MediatorUsecase<SettingsPostBody, GroupInfo>,
                                                 @Named("deleteGroupMediatorUC")
                                                 private val deleteGroupMediatorUC: MediatorUsecase<GroupInfo, Boolean>,
                                                 @Named("leaveGroupMediatorUC")
                                                 private val leaveGroupMediatorUC: MediatorUsecase<GroupBaseInfo, Boolean>,
                                                 private val readGroupInfoMediatorUC: ReadGroupInfoUsecase) :
        ViewModel() {


    val deleteGroupLiveData by lazy {
        deleteGroupMediatorUC.data()
    }

    val leaveGroupLiveData by lazy {
        leaveGroupMediatorUC.data()
    }

    val updateSettingsLiveData by lazy {
        updateSettingsMediatorUC.data()
    }

    val readGroupInfoLiveData by lazy {
        readGroupInfoMediatorUC.data()
    }

    val updateSettingStatusLiveData by lazy {
        updateSettingsMediatorUC.status()
    }

    fun updateSettings(info: SettingsPostBody) {
        updateSettingsMediatorUC.execute(info)
    }

    fun deleteGroup(groupId: String, userId: String) {
        deleteGroupMediatorUC.execute(makeGroupInfo(groupId, userId))
    }

    fun leaveGroup(groupId: String, userId: String) {
        leaveGroupMediatorUC.execute(makeGroupInfo(groupId, userId))
    }

    fun getUpdatedGroup(groupId: String, userId: String) {
        readGroupInfoMediatorUC.execute(makeGroupInfo(groupId,userId))
    }

    override fun onCleared() {
        deleteGroupMediatorUC.dispose()
        updateSettingsMediatorUC.dispose()
        leaveGroupMediatorUC.dispose()
        readGroupInfoMediatorUC.dispose()
        super.onCleared()
    }

    private fun makeGroupInfo(groupId: String, userId: String): GroupInfo {
        return GroupInfo().apply {
            this.id = groupId
            this.userId = userId
        }
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {
        @Inject lateinit var viewModel: GroupSettingsViewModel
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return viewModel as T
        }
    }
}