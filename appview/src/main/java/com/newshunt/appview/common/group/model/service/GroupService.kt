/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.model.service

import com.newshunt.appview.common.group.model.apis.GroupAPI
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.ChangeRolePostBody
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.InvitationPostBody
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.model.entity.ReviewItem
import com.newshunt.dataentity.model.entity.SettingsPostBody
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Group related API calls
 *
 * @author raunak.yadav
 */
interface GroupService {
    fun create(postBody: GroupBaseInfo): Observable<ApiResponse<GroupInfo>>
    fun getInfo(groupId: String): Observable<ApiResponse<GroupInfo>>
    fun update(postBody: GroupBaseInfo): Observable<ApiResponse<GroupInfo>>
    fun delete(groupId: String): Observable<ApiResponse<Any?>>
    fun join(groupId: String): Observable<ApiResponse<GroupInfo>>
    fun leave(groupId: String): Observable<ApiResponse<GroupInfo>>
    fun updateSetting(postBody: SettingsPostBody): Observable<ApiResponse<GroupInfo>>
    fun reviewItem(postBody: ReviewActionBody): Observable<ApiResponse<ApprovalCounts>>
    fun checkHandle(handle: String): Observable<ApiResponse<Any?>>
    fun invite(body: InvitationPostBody): Observable<ApiResponse<Any?>>
    fun removeUser(postBody: ChangeRolePostBody): Observable<ApiResponse<GroupInfo>>
    fun changeRole(postBody: ChangeRolePostBody): Observable<ApiResponse<Any?>>
    fun getInfoWithHandle(handle: String): Observable<ApiResponse<GroupInfo>>
    fun syncPendingApprovals(): Observable<ApiResponse<ApprovalCounts>>
}

class GroupServiceImpl @Inject constructor(private val groupAPI: GroupAPI,
                                           @Named("gatewayAPI")
                                           private val gatewayAPI: GroupAPI,
                                           @Named("appLang") private val appLang: String) : GroupService {

    override fun create(postBody: GroupBaseInfo): Observable<ApiResponse<GroupInfo>> {
        return groupAPI.create(postBody)
    }

    override fun getInfo(groupId: String): Observable<ApiResponse<GroupInfo>> {
        return groupAPI.getGroupInfo(groupId)
    }

    override fun update(postBody: GroupBaseInfo): Observable<ApiResponse<GroupInfo>> {
        return groupAPI.update(postBody)
    }

    override fun delete(groupId: String): Observable<ApiResponse<Any?>> {
        return groupAPI.delete(groupId)
    }

    override fun join(groupId: String): Observable<ApiResponse<GroupInfo>> {
        return groupAPI.join(groupId)
    }

    override fun leave(groupId: String): Observable<ApiResponse<GroupInfo>> {
        return groupAPI.leave(groupId)
    }

    override fun updateSetting(postBody: SettingsPostBody): Observable<ApiResponse<GroupInfo>> {
        return groupAPI.updateSetting(postBody)
    }

    override fun invite(body: InvitationPostBody): Observable<ApiResponse<Any?>> {
        return groupAPI.invite(body)
    }
    override fun reviewItem(postBody: ReviewActionBody): Observable<ApiResponse<ApprovalCounts>> {
        return when (postBody.reviewItem) {
            ReviewItem.GROUP_MEMBER -> groupAPI.reviewMember(postBody)
            ReviewItem.GROUP_INVITATION -> groupAPI.reviewInvitation(postBody)
            ReviewItem.GROUP_POST -> groupAPI.reviewPost(postBody)
        }
    }

    override fun checkHandle(handle: String): Observable<ApiResponse<Any?>> {
        return Observable.just(ApiResponse())
    }

    override fun removeUser(postBody: ChangeRolePostBody): Observable<ApiResponse<GroupInfo>> {
         return groupAPI.removeUser(postBody)
    }

    override fun changeRole(postBody: ChangeRolePostBody): Observable<ApiResponse<Any?>> {
        return groupAPI.changeMemberRole(postBody)
    }

    override fun getInfoWithHandle(handle: String): Observable<ApiResponse<GroupInfo>> {
        return gatewayAPI.getGroupInfoWithHandle(handle, appLang)
    }

    override fun syncPendingApprovals(): Observable<ApiResponse<ApprovalCounts>> {
        return groupAPI.syncPendingApprovals()
    }
}