/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import android.app.Application
import android.os.Bundle
import com.newshunt.appview.common.profile.helper.createBookmarkAPI
import com.newshunt.appview.common.profile.helper.createGateWayAPIWithCaching
import com.newshunt.appview.common.profile.helper.createProfileAPI
import com.newshunt.appview.common.profile.model.internal.rest.ProfileAPI
import com.newshunt.appview.common.profile.model.internal.service.BookmarkService
import com.newshunt.appview.common.profile.model.internal.service.BookmarkServiceImpl
import com.newshunt.appview.common.profile.model.internal.service.ProfileService
import com.newshunt.appview.common.profile.model.internal.service.ProfileServiceImpl
import com.newshunt.appview.common.profile.model.usecase.PostBookmarksUsecase
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.model.usecase.ShareUsecase
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.model.entity.BookmarkList
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.apis.MenuApi
import com.newshunt.news.model.apis.PostCreationService
import com.newshunt.news.model.apis.PostDeletionService
import com.newshunt.news.model.apis.PostReportService
import com.newshunt.news.model.daos.MenuDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.*
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * @author amit.chaudhary
 */
@Module
class MenuModule2 @JvmOverloads constructor(private val context: Application,
                                            private val menuLocation: MenuLocation,
                                            private val uniqueScreenId: Int = 0,
                                            private val arguments: Bundle = Bundle()) {

    @Provides
    fun app() = context

    @Provides
    fun menuLocation(): MenuLocation = menuLocation

    @Provides
    @Named("arguments")
    fun provideArguments(): Bundle = arguments

    @Provides
    fun api(): MenuApi = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(
            CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()), Priority.PRIORITY_HIGHEST, ""
    ).create(MenuApi::class.java)

    @Provides
    fun menuDao() = SocialDB.instance().menuDao()

    @Provides
    fun postDao() = SocialDB.instance().postDao()

    @Provides
    fun dislikeDao() = SocialDB.instance().dislikeDao()

    @Provides
    fun fetchDao() = SocialDB.instance().fetchDao()

    @Provides
    fun menuMetaUsecase(dao: MenuDao): MenuMetaUsecase {
        return MenuMetaUsecase(dao)
    }

    @Provides
    fun followdao() = SocialDB.instance().followEntityDao()

    @Provides
    @Named("followUsecase")
    fun followusecase(followFromMenuUsecase: FollowFromMenuUsecase): MediatorUsecase<Bundle, Boolean> = followFromMenuUsecase.toMediator2()

    @Provides
    @Named("postL1")
    fun postL1Usecase(postL1Usecase: PostL1Usecase): MediatorUsecase<Bundle, Boolean> = postL1Usecase.toMediator2()

    @Provides
    @Named("dislikeUsecase")
    fun dislikeUsecase(dislikeUsecase: DislikeUsecase): MediatorUsecase<Bundle, Boolean> {
        return dislikeUsecase.toMediator2()
    }

    @Named("hideUsecase")
    @Provides
    fun provideMediatorHideUsecase(hidePostUsecase: HidePostUsecase): MediatorUsecase<Bundle, Boolean> {
        return hidePostUsecase.toMediator2()
    }

    @Named("canAutoPlayVideo")
    @Provides
    fun canAutoPlayVideo(): Boolean {
        return AutoPlayHelper.isAutoPlayAllowed()
    }

    @Named("gatewayAPI")
    @Provides
    fun provideGatewayProfileApi(): ProfileAPI {
        return createGateWayAPIWithCaching()
    }

    @Provides
    fun provideProfileApi(): ProfileAPI {
        return createProfileAPI()
    }

    @Provides
    fun bookmarksDao() = SocialDB.instance().bookmarkDao()

    @Named("saveUnsaveUsecase")
    @Provides
    fun providePostBookmarksUsecase(postBookmarkUsecase: PostBookmarksUsecase): MediatorUsecase<BookmarkList, Boolean> {
        return postBookmarkUsecase.toMediator2()
    }

    @Provides
    fun profileService(profileServiceImpl: ProfileServiceImpl): ProfileService {
        return profileServiceImpl
    }

    @Provides
    @Named("uniqueScreenId")
    fun provideUniqueScreenId(): Int = uniqueScreenId

    @Provides
    @Named("deletePostUsecase")
    fun provideDeletePostUsecase(deletePostUsecase: DeletePostUsecase): MediatorUsecase<Bundle, Boolean> =
            deletePostUsecase.toMediator2()

    @Provides
    @Named("reportPostUsecase")
    fun provideReportPostUsecase(reportPostUsecase: ReportPostUsecase):
            MediatorUsecase<Bundle, Boolean> = reportPostUsecase.toMediator2()

    @Provides
    fun postService(): PostCreationService = RestAdapterContainer.getInstance().getRestAdapter(
            NewsBaseUrlContainer.getPostCreationBaseUrl(), Priority.PRIORITY_HIGH, null, true, HTTP401Interceptor())
            .create(PostCreationService::class.java)

    @Provides
    fun postDeletionService(): PostDeletionService =
        RestAdapterContainer.getInstance().getRestAdapter(
            NewsBaseUrlContainer.getPostDeletionBaseUrl(),
            Priority.PRIORITY_HIGHEST, null, true, HTTP401Interceptor()
        ).create(PostDeletionService::class.java)

    @Provides
    fun postReportService(): PostReportService =
        RestAdapterContainer.getInstance().getRestAdapter(
            NewsBaseUrlContainer.getPostReportBaseUrl(),
            Priority.PRIORITY_HIGHEST, null, true, HTTP401Interceptor()
        ).create(PostReportService::class.java)

    @Provides
    fun bookmarkAPI() = createBookmarkAPI()

    @Provides
    fun bookmarkService(bookmarkService: BookmarkServiceImpl): BookmarkService = bookmarkService

    @Provides
    fun interactionDao() = SocialDB.instance().interactionsDao()

    @Provides
    @Named("shareUsecase")
    fun shareUsecase(shareUsecase: ShareUsecase): MediatorUsecase<Bundle, Boolean> = shareUsecase.toMediator2()

    @Provides
    fun historyDao() = SocialDB.instance().historyDao()

    @Provides
    @Named("targetNavigationId")
    fun targetId() = arguments.getLong(Constants.BUNDLE_TARGET_NAVIGATION_ID, -1L)

    @Named("followBlockUpdateMediatorUC")
    @Provides
    fun updateFollowBlockUsecaseUC(followBlockUpdateUsecase: FollowBlockUpdateUsecase) =
        followBlockUpdateUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Named("coldSignalUseCase")
    @Provides
    fun getColdSignalUseCase(coldSignalUseCase: ColdSignalUseCase):MediatorUsecase<Bundle,Boolean>
            = coldSignalUseCase.toMediator2(ignoreIfAnotherReqInProgress=true)
    @Provides
    fun getFollowBlockUsecaseUC(getFollowBlockUpdateUsecase: GetFollowBlockUpdateUsecase) =
        getFollowBlockUpdateUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Provides
    fun followBlockRecoDao() = SocialDB.instance().followBlockRecoDao()

}