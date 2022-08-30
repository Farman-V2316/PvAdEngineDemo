/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.model.apis

import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.ChangeRolePostBody
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.InvitationPostBody
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.model.entity.SettingsPostBody
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * All apis for group functions
 *
 * @author raunak.yadav
 */
interface GroupAPI {

    @POST("groups/create")
    fun create(@Body groupInfo: GroupBaseInfo): Observable<ApiResponse<GroupInfo>>

    @PUT("groups/update")
    fun update(@Body groupInfo: GroupBaseInfo): Observable<ApiResponse<GroupInfo>>

    @GET("groups/info")
    fun getGroupInfo(@Query("groupId") groupId: String): Observable<ApiResponse<GroupInfo>>

    @DELETE("groups/delete")
    fun delete(@Query("groupId") groupId: String): Observable<ApiResponse<Any?>>

    @POST("groups/join")
    fun join(@Query("groupId") groupId: String): Observable<ApiResponse<GroupInfo>>

    @POST("groups/leave")
    fun leave(@Query("groupId") groupId: String): Observable<ApiResponse<GroupInfo>>

    @PUT("groups/settings/update")
    fun updateSetting(@Body postBody: SettingsPostBody): Observable<ApiResponse<GroupInfo>>

    @POST("invites/users/invite")
    fun invite(@Body postBody: InvitationPostBody): Observable<ApiResponse<Any?>>

    @POST("groups/member/action")
    fun reviewMember(@Body postBody: ReviewActionBody): Observable<ApiResponse<ApprovalCounts>>

    @POST("groups/post/action")
    fun reviewPost(@Body postBody: ReviewActionBody): Observable<ApiResponse<ApprovalCounts>>

    @POST("invites/user/action")
    fun reviewInvitation(@Body postBody: ReviewActionBody): Observable<ApiResponse<ApprovalCounts>>

    @POST("groups/member/remove")
    fun removeUser(@Body postBody: ChangeRolePostBody): Observable<ApiResponse<GroupInfo>>

    @POST("groups/member/role")
    fun changeMemberRole(@Body postBody: ChangeRolePostBody): Observable<ApiResponse<Any?>>

    @GET("api/v2/user/group/{handle}")
    fun getGroupInfoWithHandle(@Path("handle") handle: String,
                               @Query("appLanguage") appLang: String): Observable<ApiResponse<GroupInfo>>

    @GET("groups/awaited/counts")
    fun syncPendingApprovals(): Observable<ApiResponse<ApprovalCounts>>
}