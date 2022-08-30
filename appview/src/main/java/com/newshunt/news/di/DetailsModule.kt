package com.newshunt.news.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.paging.EitherList
import com.newshunt.adengine.view.helper.AdConsumer
import com.newshunt.appview.common.di.EntityInfoBottomSheetModule
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
import com.newshunt.appview.common.ui.fragment.CollectionLandingFragment2
import com.newshunt.appview.common.ui.fragment.TransitionParent
import com.newshunt.appview.common.ui.fragment.TransitionParentDelegate
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.appview.common.viewmodel.ClickDelegate
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.preference.PreferenceType
import com.newshunt.common.model.apis.InteractionAPI
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.model.usecase.SyncLikeUsecase
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CardInfo
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.common.model.entity.model.LikesResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.common.pages.PageEntity
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
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.analytics.DefaultInvalidCardsLogger
import com.newshunt.news.model.apis.AnswerPollApi
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.apis.PostCreationService
import com.newshunt.news.model.apis.PostDeletionService
import com.newshunt.news.model.apis.PostReportService
import com.newshunt.news.model.apis.TickerApi2
import com.newshunt.news.model.daos.ListFetchDao
import com.newshunt.news.model.daos.RecentArticleTrackerDao
import com.newshunt.news.model.internal.rest.NewsCarouselAPI
import com.newshunt.news.model.internal.service.NewsAppJsProviderServiceImpl
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.model.service.NewsAppJSProviderService
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.*
import com.newshunt.news.model.utils.CardDeserializer
import com.newshunt.news.model.utils.FilteroutUnknownCards
import com.newshunt.news.model.utils.InvalidCardsLogger
import com.newshunt.news.model.utils.TransformNewsList
import com.newshunt.news.util.AuthOrchestrator
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.NERDescriptionBottomSheetFragment
import com.newshunt.news.view.fragment.PostDetailsFragment
import com.newshunt.news.view.present.FetchCarouselMoreStoriesUsecase
import com.newshunt.news.view.present.FetchMoreStoriesUsecase
import com.newshunt.news.view.present.ReadLikesFirstPageUsecase
import com.newshunt.news.view.present.RelatedStoriesUsecase
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.functions.Function
import javax.inject.Named

@Component(modules = [DetailsModule2::class, DetailFullPostModule::class])
interface DetailsComponent2 {
	fun inject(detailsComponent2: PostDetailsFragment)
}

@Component(modules = [EntityInfoBottomSheetModule::class])
interface NERDescriptionComponent {
	fun inject(entityInfoBottomSheet: NERDescriptionBottomSheetFragment)
}

@Component(modules = [DetailsModule2::class, DetailFullPostModule::class])
interface VideoDetailsComponent2 {
	fun inject(detailsComponent2: DHVideoDetailFragment)
}

@Component(modules = [DetailsModule2::class, DetailFullPostModule::class])
interface CollectionsDetailsComponent2 {
	fun inject(detailsComponent2: CollectionLandingFragment2)
}


@Module
class DetailsModule2(private val app: Application, // should be inherited
					 private val socialDB: SocialDB,  // should be inherited
					 private val entityId: String,
					 private val postId: String,
					 private val timeSpentEventId: Long,
					 private val isInBottomSheet: Boolean,
					 private val location: String,
					 private val listType: String? = Constants.LIST_TYPE_POSTS,
					 private val lifecycleOwner: LifecycleOwner,
					 private val section: String,
					 private val adId: String? = null,
					 private val sourceId: String? = null,
					 private val sourceType: String? = null,
					 private val parentEntity: PageEntity? = null,
					 private val parentLocation: String? = null,
					 private val adConsumer: AdConsumer? = null,
					 private val fragmentManager: FragmentManager?,
					 private val userId: String = Constants.EMPTY_STRING,
					 private val filter: String = Constants.EMPTY_STRING,
					 private val searchQuery: SearchQuery? = null,
					 private val level: String? = null,
					 private val performLogin: (Boolean, Int) -> Unit = { a, b -> Unit },
					 private val disableNpCache: Boolean = false,
					 private val disableFpCache: Boolean = false,
					 private val listLocation: String,
					 private val referrerFlow: PageReferrer,
					 private val locationForMenu: MenuLocation = MenuLocation.DETAIL,
					 private val fragment: Fragment,
					 private val fragmentName: String) {
	@Provides
	fun app() = app

	@Provides
	@Named("postId")
	fun postId(): String = postId

	@Provides
	@Named("isDetail")
	fun isDetail(): Boolean = true

	@Provides
	@Named("listType")
	fun listType(): String = listType?: Constants.LIST_TYPE_POSTS

	@Provides
	@Named("level")
	fun level(): String = level?: PostEntityLevel.TOP_LEVEL.name

	@Provides
	@Named("adId")
	fun adId(): String? = adId

	@Provides
	@Named("timeSpentEventId")
	fun timeSpentEventId(): Long = timeSpentEventId

	@Provides
	@Named("isInBottomSheet")
	fun isInBottomSheet(): Boolean = isInBottomSheet

	@Provides
	@Named("entityId")
	fun entityId(): String = entityId

	@Provides
	@Named("sourceId")
	fun sourceId(): String? = sourceId

	@Provides
	@Named("sourceType")
	fun sourceType(): String? = sourceType

	@Provides
	fun pageEntityDao() = SocialDB.instance().pageEntityDao()

	@Provides
	@Named("location")
	fun loc(): String = location

	@Provides
	@Named("listLocation")
	fun listLoc(): String = listLocation

	@Provides
	@Named("section")
	fun section() = section

	@Provides
	@Named("adConsumer")
	fun adConsumer(): AdConsumer? = adConsumer

	@Provides
	@Named("pageEntity")
	fun pageEntity(): PageEntity? = parentEntity

	@Provides
	@Named("parentLocation")
	fun parentLocation(): String? = parentLocation

	@Provides
	@Named("userId")
	fun userId() = userId

	@Provides
	@Named("filter")
	fun filter() = filter

	@Provides
	@Named("disableNpCache")
	fun disableNpCache(): Boolean = disableNpCache

	@Provides
	@Named("disableFpCache")
	fun disableFpCache(): Boolean = disableFpCache

	@Provides
	@Named("referrerFlow")
	fun referrerFlow(): PageReferrer = referrerFlow

	@Provides
	fun fetchDao() = socialDB.fetchDao()

	@Provides
	fun groupFeedDao() = socialDB.groupDao()

	@Provides
	fun createPostDao() = socialDB.cpDao()

	@Provides
	fun followDao() = socialDB.followEntityDao()

	@Provides
	fun postDao() = socialDB.postDao()

	@Provides
	fun voteDao() = socialDB.voteDao()

	@Provides
	fun additionalContentsDao() = socialDB.additionalContentsDao()

	@Provides
	fun relatedListDao() = socialDB.relatedListDao()

	@Provides
	fun pullDao() = socialDB.pullDao()
	@Provides
	fun cookieDao() = socialDB.cookieDao()



	@Provides
	fun cardDao()  = socialDB.cardDao()

	@Provides
	fun interactionDao() = socialDB.interactionsDao()

	@Provides
	fun memberDao() = socialDB.memberDao()

	@Provides
	fun groupDao() = socialDB.groupInfoDao()

	@Provides
	fun userFollowDao() = socialDB.userFollowDao()

	@Provides
	fun nonLinearPostDao() = socialDB.nonLinearPostDao()

	@Provides
	fun getFragmentManager() = fragmentManager

	@Provides
	fun listPostDao(): ListFetchDao<TopLevelCard> = fetchDao()

	@Provides
	fun listMemberDao(): ListFetchDao<Member> = memberDao()

	@Provides
	fun nudgeDao() = socialDB.nudgeDao()

	@Provides
	fun provideCache(): CachedApiCacheRx {
		return CachedApiCacheRx(CacheProvider.getCachedApiCache(NewsConstants.HTTP_FEED_CACHE_DIR))
	}

	@Provides
	fun newsAppJSProviderService(): NewsAppJSProviderService {
		return NewsAppJsProviderServiceImpl()
	}

	@Provides
	fun listGroupDao(): ListFetchDao<GroupInfo> = groupDao()

	@Provides
	fun listUserFollowDao(): ListFetchDao<UserFollowView> = userFollowDao()

	@Provides
	@Named("liveSharedPref")
	fun getSharedPreference(): SharedPreferences {
		return app.getSharedPreferences(PreferenceType.APP_STATE.fileName, Context.MODE_PRIVATE)
	}

	@Provides
	fun detailAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
			.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
			Priority.PRIORITY_HIGHEST, "").create(NewsDetailAPI::class.java)

	@Provides
	@Named("normalPriorityDetailAPI")
	fun normalPriorityDetailAPI() =
			RestAdapterContainer.getInstance().getDynamicRestAdapterRx(
					CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
					Priority.PRIORITY_LOW, "").create(NewsDetailAPI::class.java)

	@Provides
	fun postService(): PostCreationService = RestAdapterContainer.getInstance().getRestAdapter(
		NewsBaseUrlContainer.getPostCreationBaseUrl(), Priority.PRIORITY_HIGH, null, true, HTTP401Interceptor())
		.create(PostCreationService::class.java)

	@Provides
	fun postDeletionService(): PostDeletionService = RestAdapterContainer.getInstance()
		.getRestAdapter(
			NewsBaseUrlContainer.getPostDeletionBaseUrl(),
			Priority.PRIORITY_HIGH, null, true, HTTP401Interceptor()
		).create(PostDeletionService::class.java)

	@Provides
	fun postReportService(): PostReportService = RestAdapterContainer.getInstance().getRestAdapter(
		NewsBaseUrlContainer.getPostReportBaseUrl(),
		Priority.PRIORITY_HIGH, null, true, HTTP401Interceptor()
	).create(PostReportService::class.java)

	@Provides
	fun interactionsAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
		.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSocialFeaturesBaseUrl()),
		Priority.PRIORITY_HIGH, "").create(InteractionAPI::class.java)

	@Provides
	fun api() = RestAdapterContainer.getInstance()
		.getDynamicRestAdapterRx(
			CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
			Priority.PRIORITY_HIGHEST,
			"",
			CardDeserializer.gson(listType, DefaultInvalidCardsLogger),
			HTTP401Interceptor(),
			NewsListErrorResponseInterceptor()
		).create(NewsApi::class.java)

	@Provides
	fun pollApi() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
		.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
		Priority.PRIORITY_HIGH, "").create(AnswerPollApi::class.java)

	@Provides
	fun newsCarousalApi() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
		.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
		Priority.PRIORITY_HIGH, "").create(NewsCarouselAPI::class.java)

	@Provides
	fun groupsApi(): GroupAPI {
		val groupsBaseUrl = NewsBaseUrlContainer.getGroupsBaseUrl()
		return RestAdapterContainer.getInstance().getRestAdapter(groupsBaseUrl,
			Priority.PRIORITY_HIGHEST,
			null, NewsListErrorResponseInterceptor(), HTTP401Interceptor())
			.create(GroupAPI::class.java)
	}

	@Provides
	fun profileAPI(): ProfileAPI {
		return createProfileAPI()
	}

	@Provides
	fun tickerRefersh() = RestAdapterContainer.getInstance()
		.getDynamicRestAdapterRx(
			CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
				Priority.PRIORITY_NORMAL,
			"",
			HTTP401Interceptor(),
			NewsListErrorResponseInterceptor()
		).create(TickerApi2::class.java)


	@Provides
	fun groupService(): GroupService = createGroupService()

	@Provides
	fun profileService(profileServiceImpl: ProfileServiceImpl): ProfileService {
		return profileServiceImpl
	}

	@Named("gatewayAPI")
	@Provides
	fun provideGatewayProfileApi(): ProfileAPI {
		return createGateWayAPIWithCaching()
	}

	@Provides
	fun bookmarksDao() = SocialDB.instance().bookmarkDao()

	@Provides
	@Named("fetchUsecase")
	fun fetchUc(fetchCardListFromUrlUsecase: FetchCardListFromUrlUsecase): BundleUsecase<NLResponseWrapper> =
		fetchCardListFromUrlUsecase

	@Provides
	@Named("readUsecase")
	fun readUsecase(readUsecase: ReadCardsUsecase<TopLevelCard>,
                    membersUc: ReadCardsUsecase<Member>,
                    groupsUc: ReadCardsUsecase<GroupInfo>,
                    userFollowUc: ReadCardsUsecase<UserFollowView>):
		MediatorUsecase<Bundle, EitherList<Any>> {
		return when (listType) {
			Format.MEMBER.name -> membersUc
			Format.GROUP_INVITE.name -> groupsUc
			Format.ENTITY.name -> userFollowUc
			else -> readUsecase
		}
	}

	@Provides
	@Named("lazyRelatedStoriesUsecase")
	fun lazyRelatedStoriesUsecase(uc: RelatedStoriesUsecase): MediatorUsecase<Bundle, List<TopLevelCard>> {
		return uc
	}

	@Provides
	@Named("lazyFetchMoreStoriesUsecase")
	fun lazyFetchMoreStoriesUsecase(uc: FetchMoreStoriesUsecase): MediatorUsecase<Bundle, MultiValueResponse<CommonAsset>> {
		return uc.toMediator2(true)
	}

	@Provides
	@Named("lazyFetchCarouselMoreStoriesUsecase")
	fun lazyFetchCarouselMoreStoriesUsecase(fcmsuc: FetchCarouselMoreStoriesUsecase): MediatorUsecase<Bundle, MultiValueResponse<CommonAsset>> {
		return fcmsuc.toMediator2(true)
	}

	@Provides
	@Named("lazyReadLikesFirstPageUsecase")
	fun lazyReadLikesFirstPageUsecase(fcmsuc: ReadLikesFirstPageUsecase): MediatorUsecase<Bundle, LikesResponse> {
		return fcmsuc.toMediator2(true)
	}

	@Provides
	@Named("fpUsecase")
	fun fpUsecase(@Named("fpUsecaseB") u : BundleUsecase<NLResponseWrapper> ): MediatorUsecase<Bundle, NLResponseWrapper> {
		return u.toMediator2(listType != Format.MEMBER.name /*
        Except for members autocomplete, disable concurrent FP requests
        */)
	}

	@Provides
	@Named("terminateNudgeUc")
	fun terminateNudgeUc(terminateCardNudgeUsecase: TerminateCardNudgeUsecase): MediatorUsecase<CardNudgeTerminateType, Boolean> {
		return terminateCardNudgeUsecase.toMediator2(ignoreIfAnotherReqInProgress = false)
	}

	@Provides
	@Named("readNudgesUc")
	fun readNudgesUc(uc: ReadNudgesUsecase): MediatorUsecase<List<CardInfo>, Map<String, CardNudge?>> {
		return uc.toMediator2(ignoreIfAnotherReqInProgress = false)
	}

	@Provides
	@Named("markNudgeUc")
	fun markNudgeUc(uc: MarkNudgeShownUsecase): MediatorUsecase<Int, Boolean> {
		return uc.toMediator2(ignoreIfAnotherReqInProgress = false)
	}

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
	@Named("FPInserttoDBUsecase")
	fun insertToDBUsecase(fpInserttoDBUsecase: FPInserttoDBUsecase): BundleUsecase<NLResponseWrapper> {
		return when (listType) {
			null -> fpInserttoDBUsecase
			else -> NoOpFPInserttoDBUsecase()
		}
	}

	@Provides
	fun transf(filteroutUnknownCards: FilteroutUnknownCards): TransformNewsList {
		return filteroutUnknownCards
	}

	@Provides
	fun provideSyncLikeUsecase(): SyncLikeUsecase = SyncLikeUsecase()

	@Provides
	@Named("clickDelegate")
	fun getCardClickDelegate(): ClickDelegate? {
		return null
	}

	@Provides
	fun lifecycleOwner() = lifecycleOwner

	@Provides
	@Named("currentPageInfoUsecase")
	fun currentPageInfoUsecase(currentPageInfoUsecase: CurrentPageInfoUsecase): CurrentPageInfoUsecase {
		return currentPageInfoUsecase
	}

	@Provides
	fun dislikeDao() = socialDB.dislikeDao()

	@Provides
	fun deletedInteractiondao() = socialDB.deletedInteractionsDao()

	@Provides
	fun historyDao() = socialDB.historyDao()

	@Provides
	@Named("searchQuery")
	fun provideSearchContextPayload(): SearchSuggestionItem? = searchQuery

	@Provides
	fun provideRecentArticleTrackerDao(): RecentArticleTrackerDao = SocialDB.instance().recentArticleTrackerDao()

	@Provides
	@Named("buildPayloadUsecase")
	fun provideBuildPayloadUsecase(buildPayloadUsecase: BuildPayloadUsecase,
	                               buildSearchPayloadUsecase: BuildSearchPayloadUsecase):
			BundleUsecase<Any> {
		return (if (searchQuery != null) {
			buildSearchPayloadUsecase
		} else {
			buildPayloadUsecase
		})

	}

	@Provides
	@Named("clearFPDataOnEmptyResponse")
	fun clearFPDataOnEmptyResponse() = false

	@Provides
	fun auth() = AuthOrchestrator(performLogin)

	@Provides
	fun menuLocation() = locationForMenu

	@Provides
	fun groupInfo(): GroupInfo? = null

	@Provides
	@Named("approvalActionMediatorUC")
	fun approvalActionMediatorUC(approvalUsecase: ApprovalActionUsecase): MediatorUsecase<ReviewActionBody, Boolean> {
		return approvalUsecase.toMediator2()
	}

	@Named("joinGroupMediatorUC")
	@Provides
	fun joinGroupMediatorUC(joinUsecase: JoinGroupUsecase): MediatorUsecase<GroupBaseInfo, GroupInfo> {
		return joinUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)
	}


	@Provides
	@Named("apiTag")
	fun cleanupUsecase()= "$postId::$location::$section"

    @Provides
    fun approvalsDao() = SocialDB.instance().pendingApprovalsDao()

	@Provides
	@Named("isForyouPage")
	fun isForyouPage() = false

	@Provides
	@Named("isMyPostsPage")
	fun isMyPostsPage() = false

	@Provides
	@Named("localCardTtl")
	fun localCardTtl() = PreferenceManager.getPreference(GenericAppStatePreference.LOCAL_CARD_TTL, Constants.DEFAULT_LOCAL_CARD_TTL)

	@Provides
	@Named(NewsConstants.BUNDLE_ENABLE_MAX_DURATION_TO_NOT_FETCH_FP)
	fun enableMaxDurToNotFetchFP() = false

    @Provides
    @Named("listTransformType")
    fun listTransformType() = ListTransformType.DEFAULT

	@Provides
	fun bookmarkAPI(): BookmarksAPI = createBookmarkAPI()

	@Provides
	fun bookmarkService(bookmarkService: BookmarkServiceImpl): BookmarkService = bookmarkService

	@Provides
	@Named("incrementViewcountUsecase")
	fun provideIncrementViewcountUsecase(incrementViewcountUsecase: IncrementViewcountUsecase) =
			incrementViewcountUsecase.toMediator()

	@Provides
	fun CFCountTracker() = com.newshunt.appview.common.viewmodel.CFCountTracker.INST

	@Provides
	fun invalidCardLogger() : InvalidCardsLogger = DefaultInvalidCardsLogger

	@Named("followBlockUpdateMediatorUC")
	@Provides
	fun updateFollowBlockUsecaseUC(followBlockUpdateUsecase: FollowBlockUpdateUsecase) =
		followBlockUpdateUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

	@Provides
	fun getFollowBlockUsecases(getFollowBlockUpdateUsecase: GetFollowBlockUpdateUsecase) =
		getFollowBlockUpdateUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

	@Named("implicitFollowMediatorUC")
	@Provides
	fun implicitFollowUsecaseUC(implicitFollowTriggerUsecase: ImplicitFollowTriggerUsecase) =
		implicitFollowTriggerUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

	@Named("implicitBlockMediatorUC")
	@Provides
	fun implicitBlockUsecase(implicitFollowTriggerUsecase: ImplicitBlockTriggerUsecase) =
		implicitFollowTriggerUsecase.toMediator2(ignoreIfAnotherReqInProgress = true)

	@Provides
	fun getColdSignalUseCase(coldSignalUseCase: ColdSignalUseCase):MediatorUsecase<Bundle,Boolean>
			= coldSignalUseCase.toMediator2(ignoreIfAnotherReqInProgress=true)

	@Provides
	fun followBlockRecoDao() = SocialDB.instance().followBlockRecoDao()

	@Provides
	fun cssRepo(): CardSeenStatusRepo {
		return CardSeenStatusRepo.DEFAULT
	}

	@Provides
	fun transitionDelegate(): TransitionParent = TransitionParentDelegate(fragment, fragmentName)
}

/**
 * For providing dependencies for [ReadFullPostUsecase].
 * Written separately from [DetailsModule2] because it can be used with other components.
 *
 * @author satosh.dhanyamraju
 */
@Module
class DetailFullPostModule() {


	/**
	 * Using Rx functino instead of kotlin's because to incompatible code generation
	 */
	@Provides
	@Named("isViralPost")
	@JvmSuppressWildcards
	fun isViralPost(): Function<PostEntity?, Boolean> = Function { pe: PostEntity? ->
		pe?.let {
			CardsBindUtils.isViralPost(pe)
		} ?: false
	}
}

/**
 * For providing additional dependencies for prefetch notification content.
 * Used in conjunction with [DetailFullPostModule]
 *
 * @author satosh.dhanyamraju
 */
@Module
class DetailFullPostNotificationModule() {

	@Provides
	fun detailAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
			.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
			Priority.PRIORITY_HIGHEST, "").create(NewsDetailAPI::class.java)

	@Provides
	@Named("apiCacheProvider")
	fun provideApiCacheProvider(): ApiCacheProvider {
		return object : ApiCacheProvider {
			override fun getCache(directory: String): CachedApiCacheRx {
				return CachedApiCacheRx(CacheProvider.getCachedApiCache(directory))
			}
		}
	}
}