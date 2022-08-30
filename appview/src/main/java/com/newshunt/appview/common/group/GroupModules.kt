/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group

import com.newshunt.appview.common.di.FollowSnackbarModule
import com.newshunt.appview.common.group.model.apis.GroupAPI
import com.newshunt.appview.common.group.model.apis.ImageAPI
import com.newshunt.appview.common.group.model.service.GroupService
import com.newshunt.appview.common.group.model.service.GroupServiceImpl
import com.newshunt.appview.common.group.model.service.HandleAPI
import com.newshunt.appview.common.group.model.service.HandleValidatorService
import com.newshunt.appview.common.group.model.service.HandleValidatorServiceImpl
import com.newshunt.appview.common.group.model.service.ImageUploadService
import com.newshunt.appview.common.group.model.service.ImageUploadServiceImpl
import com.newshunt.appview.common.group.model.usecase.DeleteGroupUsecase
import com.newshunt.appview.common.group.model.usecase.EditGroupUsecase
import com.newshunt.appview.common.group.model.usecase.FetchGroupInfoUsecase
import com.newshunt.appview.common.group.model.usecase.GetApprovalTabsInfoUseCase
import com.newshunt.appview.common.group.model.usecase.GroupInviteUsecase
import com.newshunt.appview.common.group.model.usecase.JoinGroupUsecase
import com.newshunt.appview.common.group.model.usecase.LeaveGroupUsecase
import com.newshunt.appview.common.group.model.usecase.ReadInviteConfigHybridUsecase
import com.newshunt.appview.common.group.model.usecase.SyncPendingApprovalsUsecase
import com.newshunt.appview.common.group.model.usecase.UpdateSettingsUsecase
import com.newshunt.appview.common.group.ui.activity.ApprovalsActivity
import com.newshunt.appview.common.group.ui.activity.GroupDetailActivity
import com.newshunt.appview.common.group.ui.activity.GroupEditorActivity
import com.newshunt.appview.common.group.ui.activity.GroupInvitationActivity
import com.newshunt.appview.common.group.ui.activity.GroupSettingsActivity
import com.newshunt.appview.common.group.ui.activity.MemberListActivity
import com.newshunt.appview.common.group.ui.activity.PhoneBookActivity
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.ApprovalTab
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.InviteConfigWithGroupInfo
import com.newshunt.dataentity.model.entity.SettingsPostBody
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.model.internal.service.ApprovalTabsInfoService
import com.newshunt.dhutil.model.internal.service.ApprovalTabsInfoServiceImpl
import com.newshunt.dhutil.model.internal.service.InviteConfigService
import com.newshunt.dhutil.model.internal.service.InviteConfigServiceImpl
import com.newshunt.news.model.daos.MemberDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.model.usecase.toUIWrapper
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Component for Group Edit/Create activity
 *
 * @author raunak.yadav
 */
@Component(modules = [ImageUploadModule::class, SocialHandleModule::class,
    GroupBaseModule::class])
interface EditGroupComponent {
    fun inject(editor: GroupEditorActivity)
}

/**
 * Base Module for group related network calls
 *
 * @author raunak.yadav
 */
@Module
class GroupBaseModule(private val socialDB: SocialDB) {

    @Provides
    fun api(): GroupAPI {
        val groupsBaseUrl = NewsBaseUrlContainer.getGroupsBaseUrl()
        return RestAdapterContainer.getInstance().getRestAdapter(groupsBaseUrl,
                Priority.PRIORITY_HIGHEST,
                null, NewsListErrorResponseInterceptor(), HTTP401Interceptor())
                .create(GroupAPI::class.java)
    }

    @Provides
    @Named("gatewayAPI")
    fun getwayApi(): GroupAPI {
        val gatewayUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationSecureUrl())
        return RestAdapterContainer.getInstance().getRestAdapter(gatewayUrl,
                Priority.PRIORITY_HIGHEST,
                null,
                NewsListErrorResponseInterceptor(), HTTP401Interceptor())
                .create(GroupAPI::class.java)
    }

    @Provides
    @Named("appLang")
    fun appLang(): String {
        return AppUserPreferenceUtils.getUserNavigationLanguage()
    }

    @Provides
    fun service(service: GroupServiceImpl): GroupService {
        return service
    }

    @Named("fetchGroupInfoMediatorUC")
    @Provides
    fun fetchGroupInfoMediatorUC(fetchGroupInfoUsecase: FetchGroupInfoUsecase): MediatorUsecase<GroupBaseInfo, GroupInfo> {
        return fetchGroupInfoUsecase.toMediator2()
    }

    @Named("joinGroupMediatorUC")
    @Provides
    fun joinGroupMediatorUC(joinUsecase: JoinGroupUsecase): MediatorUsecase<GroupBaseInfo, GroupInfo> {
        return joinUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)
    }

    @Named("leaveGroupMediatorUC")
    @Provides
    fun leaveGroupMediatorUC(leaveGroupUsecase: LeaveGroupUsecase): MediatorUsecase<GroupBaseInfo, Boolean> {
        return leaveGroupUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)
    }

    @Named("syncPendingApprovalsMediatorUC")
    @Provides
    fun syncPendingApprovalsMediatorUC(syncPendingApprovalsUsecase: SyncPendingApprovalsUsecase): MediatorUsecase<String, Boolean> {
        return syncPendingApprovalsUsecase.toMediator2()
    }

    @Named("updateSettingsMediatorUC")
    @Provides
    fun updateSettingsMediatorUC(updateSettingsUseCase: UpdateSettingsUsecase) : MediatorUsecase<SettingsPostBody, GroupInfo> {
        return updateSettingsUseCase.toMediator2(needAccurateStatusForParallelExecution = true )
    }

    @Named("deleteGroupMediatorUC")
    @Provides
    fun deleteGroupMediatorUC(deleteGroupUsecase: DeleteGroupUsecase): MediatorUsecase<GroupInfo, Boolean> {
        return deleteGroupUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)
    }

    @Provides
    fun pendingApprovalsDao() = socialDB.pendingApprovalsDao()

    @Provides
    fun postDao() = socialDB.postDao()

    @Named("editGroupUsecase")
    @Provides
    fun editGroupUsecase(editGroupUsecase: EditGroupUsecase) = editGroupUsecase.toUIWrapper()
}


/**
 *  Takes care of dependencies related to social handle validation.
 *
 *  @author raunak.yadav
 */
@Module
class SocialHandleModule(private val debounceDelay: Long = 0L) {

    private val DEBOUNCE_DELAY = 250L

    @Provides
    @Named("debounceDelay")
    fun debounceDelay(): Long {
        return if (debounceDelay <= 0L) DEBOUNCE_DELAY else debounceDelay
    }

    @Provides
    fun api(): HandleAPI {
        val baseUrl = NewsBaseUrlContainer.getUserServiceSecuredBaseUrl()
        return RestAdapterContainer.getInstance().getRestAdapter(baseUrl,
                Priority.PRIORITY_HIGHEST,
                null, NewsListErrorResponseInterceptor(), HTTP401Interceptor())
                .create(HandleAPI::class.java)
    }

    @Provides
    fun service(service: HandleValidatorServiceImpl): HandleValidatorService {
        return service
    }
}

/**
 * Takes care of dependencies related to image upload.
 *
 * @author raunak.yadav
 */
@Module
class ImageUploadModule {

    @Provides
    fun api(): ImageAPI {
        val imageBaseUrl = NewsBaseUrlContainer.getImageBaseUrl()
        return RestAdapterContainer.getInstance().getRestAdapter(imageBaseUrl,
                Priority.PRIORITY_HIGHEST,
                null,
                false,
                NewsListErrorResponseInterceptor(), HTTP401Interceptor())
                .create(ImageAPI::class.java)
    }

    @Provides
    fun service(service: ImageUploadServiceImpl): ImageUploadService {
        return service
    }
}

/**
 * Component for Group Approvals
 *
 * @author raunak.yadav
 */
@Component(modules = [ApprovalModule::class])
interface GroupApprovalComponent {
    fun inject(component: ApprovalsActivity)
}

@Module
class ApprovalModule {

    @Provides
    fun service(service: ApprovalTabsInfoServiceImpl): ApprovalTabsInfoService {
        return service
    }

    @Provides
    fun getApprovalTabsInfoUsecase(usecase: GetApprovalTabsInfoUseCase): MediatorUsecase<Unit, List<ApprovalTab>> {
        return usecase.toMediator2(true)
    }

    @Provides
    fun pendingApprovalsDao() = SocialDB.instance().pendingApprovalsDao()
}

/**
 * Component for Group Member list
 *
 * @author raunak.yadav
 */
@Component(modules = [MemberListModule::class, GroupBaseModule::class])
interface GroupMemberListComponent {
    fun inject(component: MemberListActivity)
}

@Module
class MemberListModule(private val groupId: String) {

    @Provides
    @Named("groupId")
    fun groupId(): String {
        return groupId
    }
}


@Component(modules= [GroupBaseModule::class, FollowSnackbarModule::class])
interface GroupDetailComponent {
    fun inject(component: GroupDetailActivity)
}

@Component(modules = [GroupInvitationModule::class, GroupBaseModule::class])
interface GroupInviteComponent {
    fun inject(component: GroupInvitationActivity)
}

@Module
class GroupInvitationModule(private val groupBaseInfo: GroupBaseInfo,
                            private val socialDB: SocialDB) {
    @Named("groupId")
    @Provides
    fun groupId() = groupBaseInfo.id

    @Named("inviteMediatorUc")
    @Provides
    fun inviteMediatorUc(inviteUsecase: GroupInviteUsecase) = inviteUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Named("readInviteConfigMediatorUC")
    @Provides
    fun readInviteConfigMediatorUC(readInviteConfigUsecase: ReadInviteConfigHybridUsecase): MediatorUsecase<GroupBaseInfo, InviteConfigWithGroupInfo> {
        return readInviteConfigUsecase.toMediator2()
    }

    @Provides
    fun service(inviteConfigServiceImpl: InviteConfigServiceImpl) : InviteConfigService {
        return inviteConfigServiceImpl
    }

    @Provides
    fun memberDao(): MemberDao {
        return socialDB.memberDao()
    }

    @Provides
    fun groupBaseInfo() = groupBaseInfo
}

/**
 * Component for Group Settings Update
 *
 * @author helly.patel
 */
@Component(modules = [GroupBaseModule::class])
interface GroupSettingsUpdateComponent {
    fun inject(component: GroupSettingsActivity)
}

/**
 * Component for PhoneBook
 *
 * @author mukesh.yadav
 * */
@Component(modules = [PhoneModule::class])
interface PhoneBookComponent {
    fun inject(component: PhoneBookActivity)
}
@Module
class PhoneModule

