/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile

import com.newshunt.appview.common.di.FollowSnackbarModule
import com.newshunt.appview.common.group.ImageUploadModule
import com.newshunt.appview.common.group.SocialHandleModule
import com.newshunt.appview.common.group.model.apis.GroupAPI
import com.newshunt.appview.common.group.model.service.GroupService
import com.newshunt.appview.common.group.model.service.GroupServiceImpl
import com.newshunt.appview.common.group.model.usecase.InsertIntoGroupDaoUsecase
import com.newshunt.appview.common.group.model.usecase.SyncPendingApprovalsUsecase
import com.newshunt.appview.common.profile.helper.createBookmarkAPI
import com.newshunt.appview.common.profile.helper.createGateWayAPIWithCaching
import com.newshunt.appview.common.profile.helper.createProfileAPI
import com.newshunt.appview.common.profile.helper.createSyncBookmarkAPI
import com.newshunt.appview.common.profile.model.internal.rest.BookmarksAPI
import com.newshunt.appview.common.profile.model.internal.rest.ProfileAPI
import com.newshunt.appview.common.profile.model.internal.rest.SyncBookmarksAPI
import com.newshunt.appview.common.profile.model.internal.service.BookmarkService
import com.newshunt.appview.common.profile.model.internal.service.BookmarkServiceImpl
import com.newshunt.appview.common.profile.model.internal.service.ProfileService
import com.newshunt.appview.common.profile.model.internal.service.ProfileServiceImpl
import com.newshunt.appview.common.profile.model.internal.service.SyncBookmarksService
import com.newshunt.appview.common.profile.model.internal.service.SyncBookmarksServiceImpl
import com.newshunt.appview.common.profile.model.usecase.ClearHistoryUsecase
import com.newshunt.appview.common.profile.model.usecase.DeleteHistoryUsecase
import com.newshunt.appview.common.profile.model.usecase.DeleteInteractionUsecase
import com.newshunt.appview.common.profile.model.usecase.MarkHistoryDeletedUsecase
import com.newshunt.appview.common.profile.model.usecase.SyncBookmarksUsecase
import com.newshunt.appview.common.profile.model.usecase.UndoInteractionDeleteUsecase
import com.newshunt.appview.common.profile.model.usecase.UndoMarkDeleteUsecase
import com.newshunt.appview.common.profile.view.activity.EditProfileActivity
import com.newshunt.appview.common.profile.view.activity.ProfileActivity
import com.newshunt.appview.common.profile.view.fragment.ActivityAndResponsesFragment
import com.newshunt.appview.common.profile.view.fragment.HistoryFragment
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.FetchEntityUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named


/**
 * Dagger modules for profile components to be defined here
 * <p>
 * Created by srikanth.ramaswamy on 10/21/2019.
 */
@Component(modules = [ProfileModule::class, FollowSnackbarModule::class])
interface ProfileComponent {
    fun inject(profileActivity: ProfileActivity)
}

@Module
open class BaseProfileModule{
    @Provides
    fun profileAPI(): ProfileAPI {
        return createProfileAPI()
    }

    @Provides
    @Named("gatewayAPI")
    fun profileBaseAPI(): ProfileAPI {
        return createGateWayAPIWithCaching()
    }

    @Provides
    fun service(profileService: ProfileServiceImpl): ProfileService {
        return profileService
    }
}

@Module
class ProfileModule(private val socialDB: SocialDB): BaseProfileModule() {

    @Provides
    fun followEntityDao(): FollowEntityDao {
        return SocialDB.instance().followEntityDao()
    }

    @Provides
    fun deletedInteractiondao() = socialDB.deletedInteractionsDao()

    @Provides
    fun deleteInteractionMediatorUC(deleteInteractionUsecase: DeleteInteractionUsecase): MediatorUsecase<Boolean, Int> {
        return deleteInteractionUsecase.toMediator2()
    }

    @Provides
    fun undoDeleteInteractionMediatorUC(undoInteractionDeleteUsecase: UndoInteractionDeleteUsecase): MediatorUsecase<Unit, Boolean> {
        return undoInteractionDeleteUsecase.toMediator2()
    }

    @Provides
    fun dislikeDao() = socialDB.dislikeDao()

    @Provides
    @Named("syncBookmarksUsecase")
    fun syncBookmarksMediatorUC(syncBookmarkUsecase: SyncBookmarksUsecase): MediatorUsecase<Boolean, Boolean> {
        return syncBookmarkUsecase.toMediator2(true)
    }

    @Named("syncPendingApprovalsMediatorUC")
    @Provides
    fun syncPendingApprovalsMediatorUC(syncPendingApprovalsUsecase: SyncPendingApprovalsUsecase): MediatorUsecase<String, Boolean> {
        return syncPendingApprovalsUsecase.toMediator2()
    }

    @Provides
    fun pendingApprovalsDao() = socialDB.pendingApprovalsDao()

    @Provides
    fun postDao() = socialDB.postDao()

    @Named("insertIntoGroupMediatorUC")
    @Provides
    fun insertIntoGroupMediatorUC(insertIntoGroupDaoUsecase: InsertIntoGroupDaoUsecase) = insertIntoGroupDaoUsecase.toMediator2()

    @Named("fetchEntityMediatorUC")
    @Provides
    fun fetchEntityMediatorUC(fetchEntityUsecase: FetchEntityUsecase) : MediatorUsecase<String, List<FollowSyncEntity?>> = fetchEntityUsecase

    @Named("toggleFollowMediatorUC")
    @Provides
    fun toggleFollowMediatorUC(toggleFollowUseCase: ToggleFollowUseCase) = toggleFollowUseCase.toMediator2()

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
    fun groupService(groupService: GroupServiceImpl): GroupService = groupService

    @Provides
    fun syncBookmarksAPI(): SyncBookmarksAPI = createSyncBookmarkAPI()

    @Provides
    fun syncBookmarksService(syncBookmarksService: SyncBookmarksServiceImpl): SyncBookmarksService = syncBookmarksService

    @Provides
    fun bookmarkAPI(): BookmarksAPI = createBookmarkAPI()

    @Provides
    fun bookmarkService(bookmarkService: BookmarkServiceImpl): BookmarkService = bookmarkService

    @Provides
    fun bookmarksDao(): BookmarksDao = socialDB.bookmarkDao()
}

@Component(modules = [BaseProfileModule::class, SocialHandleModule::class, ImageUploadModule::class])
interface EditProfileComponent {
    fun inject(editProfileActivity: EditProfileActivity)
}

@Module
class HistoryModule(private val socialDB: SocialDB) {
    @Provides
    fun historyDao() = socialDB.historyDao()

    @Named("deleteHistoryMediatorUC")
    @Provides
    fun deleteHistoryMediatorUC(deleteHistoryUsecase: DeleteHistoryUsecase) = deleteHistoryUsecase.toMediator2()

    @Named("undoDeleteHistoryMediatorUC")
    @Provides
    fun undoDeleteHistoryMediatorUC(undoMarkDeleteUsecase: UndoMarkDeleteUsecase) = undoMarkDeleteUsecase.toMediator2()

    @Named("markHistoryDeletedMediatorUC")
    @Provides
    fun markHistoryDeletedMediatorUC(markHistoryDeletedUsecase: MarkHistoryDeletedUsecase) = markHistoryDeletedUsecase.toMediator2()

    @Named("clearHistoryMediatorUC")
    @Provides
    fun clearHistoryMediatorUC(clearHistoryUsecase: ClearHistoryUsecase) = clearHistoryUsecase.toMediator2()

    @Named("clearSourcesOnExecute")
    @Provides
    fun clearSourcesOnExecute() = false
}

@Component(modules = [HistoryModule::class])
interface HistoryComponent {
    fun inject(historyFragment: HistoryFragment)
}

@Component(modules = [ProfileModule::class])
interface ActivityResponsesComponent {
    fun inject(activityAndResponsesFragment: ActivityAndResponsesFragment)
}
