/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.paging.EitherList
import com.newshunt.adengine.view.helper.AdDBHelper
import com.newshunt.appview.common.group.createGroupService
import com.newshunt.appview.common.group.model.apis.GroupAPI
import com.newshunt.appview.common.group.model.service.GroupService
import com.newshunt.appview.common.group.model.usecase.ApprovalActionUsecase
import com.newshunt.appview.common.group.model.usecase.JoinGroupUsecase
import com.newshunt.appview.common.profile.helper.createBookmarkAPI
import com.newshunt.appview.common.profile.helper.createGateWayAPIWithCaching
import com.newshunt.appview.common.profile.helper.createProfileAPI
import com.newshunt.appview.common.profile.model.internal.rest.BookmarksAPI
import com.newshunt.appview.common.profile.model.internal.rest.ProfileAPI
import com.newshunt.appview.common.profile.model.internal.service.BookmarkService
import com.newshunt.appview.common.profile.model.internal.service.BookmarkServiceImpl
import com.newshunt.appview.common.profile.model.internal.service.ProfileService
import com.newshunt.appview.common.profile.model.internal.service.ProfileServiceImpl
import com.newshunt.appview.common.viewmodel.CFCountTracker
import com.newshunt.appview.common.viewmodel.ClickDelegate
import com.newshunt.appview.common.viewmodel.ClickDelegateProvider
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.asset.CardInfo
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.UserFollowView
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dhutil.CacheProvider
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.analytics.DefaultInvalidCardsLogger
import com.newshunt.news.di.scopes.PerFragment
import com.newshunt.news.helper.NCCardDBHelper
import com.newshunt.news.model.apis.AnswerPollApi
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.apis.TickerApi2
import com.newshunt.news.model.daos.ListFetchDao
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.daos.RecentArticleTrackerDao
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.*
import com.newshunt.news.model.utils.CardDeserializer
import com.newshunt.news.model.utils.FilteroutUnknownCards
import com.newshunt.news.model.utils.InvalidCardsLogger
import com.newshunt.news.model.utils.TransformNewsList
import com.newshunt.news.util.AuthOrchestrator
import com.newshunt.news.util.NewsConstants
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * @author satosh.dhanyamraju
 */
@Module
class CardsModule(private val app: Application, // should be inherited
                  private val socialDB: SocialDB,  // should be inherited
                  private val entityId: String,
                  private val postId: String,
                  private val pageEntity: PageEntity?,
                  private val location: String,
                  private val listType: String? = Constants.LIST_TYPE_POSTS,
                  private val adDbHelper: AdDBHelper,
                  private val ncCardDBHelper: NCCardDBHelper?=null,
                  private val lifecycleCoroutineScope: LifecycleCoroutineScope ?=null,
                  private val nestCollectionUrl: String?= null,
                  private val supportAds: Boolean,
                  private val sourceId: String? = null,
                  private val sourceType: String? = null,
                  private val fragmentId : String? = null,
                  private val lifecycleOwner: LifecycleOwner,
                  private val clickDelegateProvider: ClickDelegateProvider? = null,
                  private val section: String,
                  private val userId: String = Constants.EMPTY_STRING,
                  private val filter: String = Constants.EMPTY_STRING,
                  private val searchQuery: SearchQuery? = null,
                  private val clearFPDataOnEmptyResponse: Boolean = false,
                  private val performLogin: (Boolean, Int) -> Unit,
                  private val menuLocation: MenuLocation = MenuLocation.LIST,
                  private val groupInfo: GroupInfo? = null,
                  private val cardsLimit: Int = Integer.MAX_VALUE,
                  private val disableNpCache: Boolean = false,
                  private val disableFpCache: Boolean = false,
                          private val enableMaxDurToNotFetchFp: Boolean = false,
                  private val listTransformType: ListTransformType = ListTransformType.DEFAULT,
                  private val isMyPostsPage : Boolean = false) {
    private val apiTag: String
        get() = "$entityId::$location::$section"
    @Provides
    @PerFragment
    fun app() = app

    @Provides
    @PerFragment
    @Named("entityId")
    fun entityId(): String = entityId

    @Provides
    @PerFragment
    @Named("pageEntity")
    fun pageEntity(): PageEntity? = pageEntity

    @Provides
    @PerFragment
    @Named("sourceId")
    fun sourceId(): String? = sourceId

    @Provides
    @PerFragment
    @Named("sourceType")
    fun sourceType(): String? = sourceType

    @Provides
    @PerFragment
    @Named("isDetail")
    fun isDetail(): Boolean = false

    @Provides
    @Named("postId")
    fun postId(): String = postId

    @Provides
    @Named("listType")
    @PerFragment
    fun listType(): String = listType?: Constants.LIST_TYPE_POSTS

    @Provides
    @PerFragment
    @Named("timeSpentEventId")
    fun timeSpentEventId(): Long = 0L

    @Provides
    @PerFragment
    @Named("location")
    fun loc(): String = location

    @Provides
    @PerFragment
    @Named("listLocation")
    fun listLoc(): String = location

    @Provides
    @PerFragment
    @Named("section")
    fun section() = section

    @Provides
    @PerFragment
    @Named("userId")
    fun userId() = userId

    @Provides
    @PerFragment
    @Named("filter")
    fun filter() = filter

    @Provides
    @PerFragment
    @Named("adDbHelper")
    fun adDbHelper(): AdDBHelper = adDbHelper

    @Provides
    @PerFragment
    fun ncCardDbHelperProvider() = ncCardDBHelper

    @Provides
    @PerFragment
    fun lifecycleCoroutineScopeProvider() = lifecycleCoroutineScope

    @Provides
    @PerFragment
    @Named("fragmentId")
    fun fragmentIdProvider() = fragmentId

    @Provides
    @PerFragment
    @Named("nestCollectionUrl")
    fun provideNestCollectionUrl() = nestCollectionUrl

    @Provides
    @PerFragment
    @Named("supportAds")
    fun supportAds(): Boolean = supportAds

    @Provides
    @PerFragment
    @Named("disableNpCache")
    fun disableNpCache(): Boolean = disableNpCache

    @Provides
    @Named("disableFpCache")
    fun disableFpCache(): Boolean = disableFpCache

    @Provides
    @PerFragment
    fun fetchDao() = socialDB.fetchDao()

    @Provides
    @PerFragment
    fun provideNCCImpressionsDao() = socialDB.nccImpressionDao()

    @Provides
    @PerFragment
    fun CFCountTracker() = CFCountTracker.INST

    @Provides
    @PerFragment
    fun followDao() = socialDB.followEntityDao()

    @Provides
    @PerFragment
    fun followRepo() = FollowRepo(followDao())

    @Provides
    @PerFragment
    fun groupFeedDao() = socialDB.groupDao()

    @Provides
    @PerFragment
    fun voteDao() = socialDB.voteDao()

    @Provides
    @PerFragment
    fun interactionDao() = socialDB.interactionsDao()

    @Provides
    @PerFragment
    fun memberDao() = socialDB.memberDao()

    @Provides
    @PerFragment
    fun userFollowDao() = socialDB.userFollowDao()

    @Provides
    @PerFragment
    fun groupDao() = socialDB.groupInfoDao()

    @Provides
    @PerFragment
    fun nonLinearPostDao() = socialDB.nonLinearPostDao()

    @Provides
    @PerFragment
    fun listPostDao(): ListFetchDao<TopLevelCard> = fetchDao()

    @Provides
    @PerFragment
    fun listMemberDao(): ListFetchDao<Member> = memberDao()

    @Provides
    @PerFragment
    fun listGroupDao(): ListFetchDao<GroupInfo> = groupDao()

    @Provides
    @PerFragment
    fun listUserFollowDao(): ListFetchDao<UserFollowView> = userFollowDao()

    @Provides
    @PerFragment
    fun cpDao() = SocialDB.instance().cpDao()

    @Provides
    @PerFragment
    fun immersiveRuleDao() = SocialDB.instance().immersiveRuleDao()

    @Provides
    @PerFragment
    fun api() = RestAdapterContainer.getInstance()
            .getDynamicRestAdapterRx(
                    CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
                    Priority.PRIORITY_HIGHEST,
                    apiTag,
                    CardDeserializer.gson(listType, DefaultInvalidCardsLogger),
                    HTTP401Interceptor(),
                    NewsListErrorResponseInterceptor()
            ).create(NewsApi::class.java)


    @Provides
    @PerFragment
    fun apiPolls() = RestAdapterContainer.getInstance()
            .getDynamicRestAdapterRx(
                    CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
                    Priority.PRIORITY_HIGHEST,
                    "",
                    HTTP401Interceptor(),
                    NewsListErrorResponseInterceptor()
            ).create(AnswerPollApi::class.java)


    @Provides
    @PerFragment
    fun tickerRefersh() = RestAdapterContainer.getInstance()
            .getDynamicRestAdapterRx(
                    CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
                    Priority.PRIORITY_NORMAL,
                    "",
                    HTTP401Interceptor(),
                    NewsListErrorResponseInterceptor()
            ).create(TickerApi2::class.java)

    @Provides
    @PerFragment
    fun groupsApi(): GroupAPI {
        val groupsBaseUrl = NewsBaseUrlContainer.getGroupsBaseUrl()
        return RestAdapterContainer.getInstance().getRestAdapter(groupsBaseUrl,
                Priority.PRIORITY_HIGHEST,
                null, NewsListErrorResponseInterceptor(), HTTP401Interceptor())
                .create(GroupAPI::class.java)
    }

    @Provides
    @PerFragment
    fun groupService(): GroupService = createGroupService()

    @Provides
    @PerFragment
    fun profileAPI(): ProfileAPI {
        return createProfileAPI()
    }

    @Provides
    @PerFragment
    fun profileService(profileServiceImpl: ProfileServiceImpl): ProfileService {
        return profileServiceImpl
    }

    @Named("gatewayAPI")
    @Provides
    @PerFragment
    fun provideGatewayProfileApi(): ProfileAPI {
        return createGateWayAPIWithCaching()
    }

    @Provides
    @PerFragment
    fun bookmarksDao() = SocialDB.instance().bookmarkDao()

    @Provides
    @PerFragment
    @Named("fetchUsecase")
    fun fetchUc(fetchCardListFromUrlUsecase: FetchCardListFromUrlUsecase): BundleUsecase<NLResponseWrapper> =
            fetchCardListFromUrlUsecase

    @Provides
    @Named("apiCacheProvider")
    fun provideApiCacheProvider(): ApiCacheProvider {
        return object : ApiCacheProvider {
            override fun getCache(directory: String): CachedApiCacheRx {
                return CachedApiCacheRx(CacheProvider.getCachedApiCache(directory))
            }
        }
    }

    @Provides
    @PerFragment
    @Named("readUsecase")
    fun readUsecase(readUsecase: ReadCardsUsecase<TopLevelCard>,
                    membersUc: ReadCardsUsecase<Member>,
                    groupsUc: ReadCardsUsecase<GroupInfo>,
                    userFollowUc: ReadCardsUsecase<UserFollowView>,
                    readResponseCardsUsecase: ReadResponseCardsUsecase<TopLevelCard>,
                    readLimitCardsUsecase: ReadLimitedCardsUsecase): MediatorUsecase<Bundle, EitherList<Any>> {
        return when (listType) {
            Format.MEMBER.name -> membersUc
            Format.GROUP_INVITE.name -> groupsUc
            Format.ENTITY.name -> userFollowUc
            UiType2.USER_INTERACTION.name -> readResponseCardsUsecase
            Constants.LIST_TYPE_BOOKMARKS -> readLimitCardsUsecase
            else -> readUsecase
        }
    }

    @Provides
    @PerFragment
    @Named("cardsLimit")
    fun cardsLimit() = cardsLimit

    @Provides
    @PerFragment
    @Named("fpUsecase")
    fun fpUsecase(@Named("fpUsecaseB") u : BundleUsecase<NLResponseWrapper> ): MediatorUsecase<Bundle, NLResponseWrapper> {
        return u.toMediator2(listType != Format.MEMBER.name /*
        Except for members autocomplete, disable concurrent FP requests
        */, needSuccessDataSeparately = true)
    }


    @Provides
    @PerFragment
    @Named("fpUsecaseB")
    fun fpUsecaseB(fpUsecase: FPFetchUseCase, membersUc: MembersFPUsecase, groupsUc: GroupsFPUsecase, userFollowUc: GetFirstPageFollowForUserUsecase): BundleUsecase<NLResponseWrapper> {
        return when (listType) {
            Format.MEMBER.name -> membersUc
            Format.GROUP_INVITE.name -> groupsUc
            Format.ENTITY.name -> userFollowUc
            else -> fpUsecase
        }
    }

    @Provides
    @PerFragment
    @Named("FPInserttoDBUsecase")
    fun insertToDBUsecase(fpInserttoDBUsecase: FPInserttoDBUsecase): BundleUsecase<NLResponseWrapper> {
        return when (listType) {
            Format.MEMBER.name -> NoOpFPInserttoDBUsecase()
            Format.GROUP_INVITE.name -> NoOpFPInserttoDBUsecase()
            Format.ENTITY.name -> NoOpFPInserttoDBUsecase()
            else -> fpInserttoDBUsecase
        }
    }

    @Provides
    @PerFragment
    @Named("npUsecase")
    fun npUsecase(npUsecase: NPUsecase, membersUc: MembersNPUsecase, groupsUc: GroupsNPUsecase, userFollowUc: GetNextPageFollowUserUsecase):
            BundleUsecase<NLResp> {
        return when (listType) {
            Format.MEMBER.name -> membersUc
            Format.GROUP_INVITE.name -> groupsUc
            Format.ENTITY.name -> userFollowUc
            else -> npUsecase
        }
    }

    @Provides
    @PerFragment
    fun transf(filteroutUnknownCards: FilteroutUnknownCards): TransformNewsList {
        return filteroutUnknownCards
    }

    @Provides
    @PerFragment
    @Named("clickDelegate")
    fun getCardClickDelegate(): ClickDelegate? {
        return clickDelegateProvider?.getClickDelegate()
    }

    @Provides
    @PerFragment
    fun lifecycleOwner() = lifecycleOwner

    @Provides
    @PerFragment
    @Named("currentPageInfoUsecase")
    fun currentPageInfoUsecase(currentPageInfoUsecase: CurrentPageInfoUsecase): CurrentPageInfoUsecase {
        return currentPageInfoUsecase
    }

    @Provides
    @PerFragment
    fun dislikeDao() = socialDB.dislikeDao()

    @Provides
    @PerFragment
    fun postDao() = socialDB.postDao()

    @Provides
    @PerFragment
    fun pullDao() = socialDB.pullDao()

    @Provides
    @PerFragment
    fun cookieDao() = socialDB.cookieDao()

    @Provides
    @PerFragment
    fun nudgeDao() = socialDB.nudgeDao()

    @Provides
    @PerFragment
    fun deletedInteractiondao() = socialDB.deletedInteractionsDao()

    @Provides
    @PerFragment
    @Named("searchQuery")
    fun provideSearchContextPayload(): SearchSuggestionItem? = searchQuery

    @Provides
    fun provideRecentArticleTrackerDao(): RecentArticleTrackerDao = SocialDB.instance().recentArticleTrackerDao()

    @Provides
    @PerFragment
    fun providePageEntityDao(): PageEntityDao = SocialDB.instance().pageEntityDao()

    @Provides
    @PerFragment
    @Named("buildPayloadUsecase")
    fun provideBuildPayloadUsecase(buildPayloadUsecase: BuildPayloadUsecase,
                                   buildSearchPayloadUsecase: BuildSearchPayloadUsecase,
                                   buildContactSyncLitePayloadUsecase: BuildContactSyncLitePayloadUsecase): BundleUsecase<Any> {

        return if (listType == Format.ENTITY.name && entityId == Constants.IMPORT_FOLLOW_PAGE_ID) {
             buildContactSyncLitePayloadUsecase
        }  else if (searchQuery != null) {
            buildSearchPayloadUsecase
        } else {
            buildPayloadUsecase
        }

    }


    @Provides
    @PerFragment
    @Named("incrementViewcountUsecase")
    fun provideIncrementViewcountUsecase(incrementViewcountUsecase: IncrementViewcountUsecase) =
            incrementViewcountUsecase.toMediator()


    @Provides
    @Named("clearFPDataOnEmptyResponse")
    @PerFragment
    fun clearFPDataOnEmptyResponse() = clearFPDataOnEmptyResponse

    @Provides
    @PerFragment
    fun auth() = AuthOrchestrator(performLogin)

    @Provides
    fun menuLocation(): MenuLocation = menuLocation

    @Provides
    fun provideGroupInfo(): GroupInfo? = groupInfo

    @Provides
    @PerFragment
    @Named("approvalActionMediatorUC")
    fun approvalActionMediatorUC(approvalUsecase: ApprovalActionUsecase): MediatorUsecase<ReviewActionBody, Boolean> {
        return approvalUsecase.toMediator2()
    }

    @Provides
    @PerFragment
    @Named("joinGroupMediatorUC")
    fun joinGroupMediatorUC(joinUsecase: JoinGroupUsecase): MediatorUsecase<GroupBaseInfo, GroupInfo> {
        return joinUsecase.toMediator2(ignoreIfAnotherReqInProgress = false)
    }

    @Provides
    @PerFragment
    @Named("terminateNudgeUc")
    fun terminateNudgeUc(terminateCardNudgeUsecase: TerminateCardNudgeUsecase): MediatorUsecase<CardNudgeTerminateType, Boolean> {
        return terminateCardNudgeUsecase.toMediator2(ignoreIfAnotherReqInProgress = false)
    }

    @Provides
    @PerFragment
    @Named("readNudgesUc")
    fun readNudgesUc(uc: ReadNudgesUsecase): MediatorUsecase<List<CardInfo>, Map<String, CardNudge?>> {
        return uc.toMediator2(ignoreIfAnotherReqInProgress = false)
    }

    @Provides
    @PerFragment
    @Named("markNudgeUc")
    fun markNudgeUc(uc: MarkNudgeShownUsecase): MediatorUsecase<Int, Boolean> {
        return uc.toMediator2(ignoreIfAnotherReqInProgress = false)
    }

    @Provides
    @PerFragment
    @Named("apiTag")
    fun cleanupUsecase(): String {
        return apiTag
    }

    @Provides
    @PerFragment
    fun approvalsDao() = SocialDB.instance().pendingApprovalsDao()

    @Provides
    @PerFragment
    @Named("isForyouPage")
    fun isForyouPage() =
            (PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE, Constants.EMPTY_STRING) == entityId)
                    && section == PageSection.NEWS.section


    @Provides
    @PerFragment
    @Named("isMyPostsPage")
    fun isMyPostsPage() = isMyPostsPage


    @Provides
    @PerFragment
    @Named("localCardTtl")
    fun localCardTtl() = PreferenceManager.getPreference(GenericAppStatePreference.LOCAL_CARD_TTL, Constants.DEFAULT_LOCAL_CARD_TTL)

    @Provides
    @PerFragment
    @Named(NewsConstants.BUNDLE_ENABLE_MAX_DURATION_TO_NOT_FETCH_FP)
    fun enableMaxDurToNotFetchFP() = enableMaxDurToNotFetchFp

    @Provides
    @Named("listTransformType")
    fun listTransformType() = listTransformType

    @Provides
    @PerFragment
    fun bookmarkAPI(): BookmarksAPI = createBookmarkAPI()

    @Provides
    @PerFragment
    fun bookmarkService(bookmarkService: BookmarkServiceImpl): BookmarkService = bookmarkService


    @Provides
    @PerFragment
    fun cardDao()  = socialDB.cardDao()

    @Provides
    @PerFragment
    fun invalidCardLogger() : InvalidCardsLogger = DefaultInvalidCardsLogger

    @Named("followBlockUpdateMediatorUC")
    @Provides
    @PerFragment
    fun updateFollowBlockUsecaseUC(followBlockUpdateUsecase: FollowBlockUpdateUsecase) =
        followBlockUpdateUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Provides
    @PerFragment
    fun getFollowBlockUpdateUsecase(getFollowBlockUpdateUsecase: GetFollowBlockUpdateUsecase) =
        getFollowBlockUpdateUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Provides
    @PerFragment
    fun getMinCardPositionUsecase(minCardPositionUseCase: MinCardPositionUseCase) =
        minCardPositionUseCase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Provides
    @PerFragment
    fun getCardPositionUsecase(CardPositionUseCase: CardPositionUseCase) =
        CardPositionUseCase.toMediator2(ignoreIfAnotherReqInProgress = true)

    @Provides
    @PerFragment
    fun getBuildPayload(buildPayloadUsecase: BuildPayloadUsecase) : BundleUsecase<Any> =
        buildPayloadUsecase

    @Named("explicitFollowBlockTriggerUsecase")
    @Provides
    @PerFragment
    fun getExplicitFollowTriggerUsecase(explicitFollowBlockTriggerUsecase: ExplicitFollowBlockTriggerUsecase):MediatorUsecase<Bundle,ExplicitWrapperObject?>
            = explicitFollowBlockTriggerUsecase.toMediator2(ignoreIfAnotherReqInProgress=true)

    @Provides
    @PerFragment
    fun getColdSignalUseCase(coldSignalUseCase: ColdSignalUseCase):MediatorUsecase<Bundle,Boolean>
            = coldSignalUseCase.toMediator2(ignoreIfAnotherReqInProgress=true)

    @Provides
    @PerFragment
    fun followBlockRecoDao() = SocialDB.instance().followBlockRecoDao()

    @Provides
    @PerFragment
    fun cssRepo(): CardSeenStatusRepo {
        return CardSeenStatusRepo.DEFAULT
    }
}
