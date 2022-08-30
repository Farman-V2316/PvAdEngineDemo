
/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.collection.ArrayMap
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.MASTAdView.MASTAdView
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.newshunt.adengine.listeners.InteractiveAdListener
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.BackUpAdConsumer
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdConsumer
import com.newshunt.adengine.view.helper.PostAdsHelper
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.appview.R
import com.newshunt.appview.common.entity.CardsPojo
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.common.ui.activity.DisclaimerDialogFragment
import com.newshunt.appview.common.ui.adapter.CardsAdapter
import com.newshunt.appview.common.ui.fragment.TransitionParent
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.NavigationHelper.FRAGMENT_TRANSITION_TAG
import com.newshunt.appview.common.ui.helper.NotificationCtaUiHelper
import com.newshunt.appview.common.ui.helper.NotificationUiType
import com.newshunt.appview.common.ui.helper.ObservableDataBinding
import com.newshunt.appview.common.ui.helper.getbindLinkableTextDetail
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.common.viewmodel.FollowNudgeViewModel
import com.newshunt.appview.common.viewmodel.ViewAllCommentsViewModel
import com.newshunt.common.helper.analytics.NhAnalyticsUtility
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.DateFormatter
import com.newshunt.common.helper.common.FileUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.helper.common.NhWebViewClient
import com.newshunt.common.helper.font.FontWeight
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.track.AsyncTrackHandler
import com.newshunt.common.view.customview.*
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.customview.fontview.TextMeasurementUtils
import com.newshunt.common.view.dbgCode
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.DiscussionPojo
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.asset.PhotoChildPojo
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.SuggestedFollowsPojo
import com.newshunt.dataentity.common.asset.i_VC
import com.newshunt.dataentity.common.follow.entity.FollowUnFollowReason
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.common.model.entity.NewsAppJSResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.server.asset.NewsAppJS
import com.newshunt.dataentity.common.model.entity.server.asset.NewsAppJSType
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.dhutil.model.entity.detailordering.PostDetailActionbarVariation
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.news.model.entity.server.asset.AssetType
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.PhotoChild
import com.newshunt.dataentity.social.entity.Position
import com.newshunt.dataentity.social.entity.ReplyCount
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.dhutil.helper.RateUsDialogHelper
import com.newshunt.dhutil.helper.appindexing.AppIndexingHelper
import com.newshunt.dhutil.helper.browser.NHBrowserUtil
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.model.internal.service.DetailOrderingServiceImpl
import com.newshunt.dhutil.toArrayList
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.di.DaggerDetailsComponent2
import com.newshunt.news.di.DetailsModule2
import com.newshunt.news.helper.ActionbarSpaceDecoration
import com.newshunt.news.helper.ErrorLogHelper
import com.newshunt.news.helper.LikeEmojiBindingUtils
import com.newshunt.news.helper.NewsDetailTimespentHelper
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.helper.NonLinearFeedHelper
import com.newshunt.news.helper.PreviousPostIdHelper
import com.newshunt.news.helper.RateUsCheckHelperNews
import com.newshunt.news.helper.handler.NudgeTooltipWrapper
import com.newshunt.news.helper.toMinimizedCommonAsset
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.FollowUtils
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.getPagedListFromList
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.LinearLayoutManagerWrapper
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.news.view.adapter.DetailsAdapter
import com.newshunt.news.view.adapter.HashtagAdapter
import com.newshunt.news.view.adapter.LikedListAdapter
import com.newshunt.news.view.adapter.PerspectiveCarouselCardsAdapter
import com.newshunt.news.view.adapter.SuggestedFollowListAdapter
import com.newshunt.news.view.customview.SlowNetworkImageView
import com.newshunt.news.view.helper.PostDetailActionbarInterface
import com.newshunt.news.view.helper.PostDetailCommentbarInterface
import com.newshunt.news.view.helper.PostDetailUnifiedBottomToolbarHelper
import com.newshunt.news.view.helper.PostDetailUnifiedToolbarHelper
import com.newshunt.news.view.helper.PostDetailUnifiedTopToolbarHelper
import com.newshunt.news.view.listener.NewsDetailPhotoClickListener
import com.newshunt.news.view.view.PrefetchAdRequestCallback
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.news.viewmodel.FollowUpdateViewModel
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.notification.view.receiver.LOG_TAG
import com.newshunt.pref.NewsPreference
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sdk.network.Priority
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import com.newshunt.sdk.network.image.Image
import com.newshunt.socialfeatures.util.SocialFeaturesConstants
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.newshunt.viral.utils.visibility_utils.VisibilityCalculator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.set

/**
 * Created by karthik.r on 2019-09-10.
 */

const val TAG = "PostDetailsFragment"
private const val START_INDEX = 0
private const val MORE_INDEX = 4
private const val TOTAL_DISPLAY_COUNT = MORE_INDEX - START_INDEX + 1
private const val MORE_TEXT = "+%d"
private val TITLE_TOP_LINE_COUNT_THRESHOLD = 4
private const val APP_INDEX_TAG = "News AppIndexing"
private const val MSG_SCREEN_PAUSED = 1005
private const val MSG_TIMESPENT_RELATED_CONDITION = 1007
private const val DISCUSSION_LOADED = 1001
private const val IMPLICIT_FOLLOW_BLOCK_PROMPT = 1008

class PostDetailsFragment : BaseSupportFragment(), Toolbar.OnMenuItemClickListener,
        PostActions, AdConsumer, AdEntityReplaceHandler, InteractiveAdListener,
        NativeAdHtmlViewHolder.CachedWebViewProvider, NewsDetailPhotoClickListener,
        OnAdReportedListener, DeeplinkableDetail {


    var position: Int = -1
    var card: CommonAsset? = null
    var bootStrapCard : DetailListCard? = null
    var discussionList: List<AllLevelCards>? = null
    var associationList: List<AllLevelCards>? = null
    var relatedStories: CardsPojo? = null
    var dislikedStories: List<String>? = null
    var suggestedFollow: SuggestedFollowsPojo? = null
    var likedList: LikeListPojo? = null
    var postId: String? = null
    private var adId: String? = null
    var parentStoryId: String? = null
    var isInCollection: Boolean = false
    var isInCarousel: Boolean = false
    var isLandingStory = false
    var landingStoryId: String? = null
    var isFullPageLoaded: Boolean = false
    var groupInfo: GroupInfo? = null
    lateinit var detailList: RecyclerView
    var progressbar: View? = null
    private lateinit var toolbarHelper: PostDetailUnifiedToolbarHelper
    lateinit var toolbar: PostDetailActionbarInterface
    lateinit var commentBarHolder: LinearLayout
    lateinit var postRootView: ConstraintLayout
    lateinit var section: String
    lateinit var location: String
    lateinit var listLocation: String
    var prefetchAdRequestCallback: PrefetchAdRequestCallback? = null
    var isTitleGreater = false
    var isToolbarVisible = true
    var isStoryViewedCountIncremented = false
    private var appIndexingTitle: String? = null
    private var appIndexingUri: Uri? = null
    private var isFollowing: Boolean? = null
    private var loggedPageViewEvent: Boolean = false
    private var currentChunkInView = 0
    private var isEOSReached: Boolean = false
    private var visibilityCalculator: VisibilityCalculator? = null
    private var notificationUniqueId: String? = null
    private lateinit var recyclerViewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener
    private var notificationUiType: NotificationUiType? = null

    @Inject
    lateinit var adsHelperF: PostAdsHelper.Factory

    private var adsHelper: PostAdsHelper? = null
    private var contentAdDelegate: ContentAdDelegate? = null

    @Inject
    lateinit var detailsViewModelF: DetailsViewModel.Factory

    @Inject
    lateinit var nonLinearFeedHelper: NonLinearFeedHelper

    private lateinit var vm: DetailsViewModel
    private var lastKnownMyDiscussionCount: Int = 0
    private var lastKnownMyDiscussionRepliesCount: List<ReplyCount>? = null
    private var lastKnownMyInteraction: Interaction? = null

    @Inject
    lateinit var cardsViewModelF: CardsViewModel.Factory

    @Inject
    lateinit var menuLocation: MenuLocation
    private lateinit var cvm: CardsViewModel
    private lateinit var fragmentCommunicationsViewModel: FragmentCommunicationsViewModel

    @Inject
    lateinit var followBlockModelF: FollowUpdateViewModel.Factory
    private lateinit var followBlockViewModel: FollowUpdateViewModel

    private var detailsAdapter: DetailsAdapter? = null
    private var currentOrder: List<String> = ArrayList()
    private var isInBottomSheet = false
    private var isCardDependedItemsInitialized = false
    private var timeSpentEventId: Long = 0
    private var isTimespentPaused: Boolean = false
    private var currentChunkStartTime: Long = 0
    private var firedTSAfterPause: Boolean = false
    private var cardPosition = -1
    private var collectionCount = -1
    private var isChildFragment: Boolean = false
    private var chunkwiseTimespent: MutableMap<Int, Long> = HashMap()
    private var registeringAddiitonalContentsPending = true
    private var currentPageReferrer: PageReferrer? = null
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var referrer_raw: String? = null
    private var searchQuery: SearchQuery? = null
    private var isInternalDeeplink: Boolean = false
    private var isInteractiveAdOnTop = false
    private var parentEntity: PageEntity? = null
    private lateinit var anchor: View
    private lateinit var nudgeVM: FollowNudgeViewModel

    private var postEntityLevel: String? = null

    //A cache to reuse Webviews to avoid reloading heavy HTML Ads in the list
    private val webViewCache by lazy {
        ArrayMap<String, WeakReference<MASTAdView>>()
    }
    private val error: ObservableDataBinding<BaseError> = ObservableDataBinding()
    private var secondChunkFetched: Boolean = false

    private val DEFAULT_CSS_NON_LITE_MODE = "<style>p{word-wrap:break-word}</style>"
    private val NON_LITE_JS_FILE = "story_photo_click.js"

    private var newsAppJSChunk1: NewsAppJS? = null
    private var newsAppJSChunk2: NewsAppJS? = null
    var isBottomSheetExpanded = false
    private var reportAdsMenuListener: ReportAdsMenuListener? = null
    private var postAdReportMenuListener: ReportAdsMenuListener? = null
    private var sourceIdFollow: String? = null
    private lateinit var sourceLangFollow: String
    private lateinit var toggleFollowUseCase: MediatorUsecase<Bundle, Boolean>
    private var lastSourceFollowBlockEntity: SourceFollowBlockEntity? = null
    private var bottomBarduration: Int = Constants.DEFAULT_IMPLICIT_BOTTOM_BAR_DURATION
    private var isImplicitPopupShown: Boolean = false
    private var isTransparentActionBar = false


    private val HANDLER = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

            if (msg.what == MSG_SCREEN_PAUSED && timeSpentEventId != 0L) {
                if (!chunkwiseTimespent.isEmpty()) {
                    NewsDetailTimespentHelper.getInstance().postSendTimespentEvent(
                        timeSpentEventId, chunkwiseTimespent, true,
                        if (userVisibleHint)
                            NhAnalyticsUserAction.IDLE
                        else
                            NhAnalyticsUserAction.SWIPE
                    )
                }
                chunkwiseTimespent = HashMap()
                firedTSAfterPause = !userVisibleHint
                isTimespentPaused = true
            } else if (msg.what == DISCUSSION_LOADED) {
                vm.discussionLoading.postValue(false)
            } else if (msg.what == IMPLICIT_FOLLOW_BLOCK_PROMPT) {
                val actionText = msg.obj as? String
                if (actionText == Constants.FOLLOW) {
                    hideFollowBlockPopup()
                    FollowBlockPrefUtil.updateImplicitFollowShow()
                } else if (actionText == Constants.BLOCK) {
                    hideFollowBlockPopup()
                    FollowBlockPrefUtil.updateImplicitBlockShow()
                }
            }
        }
    }
    private lateinit var commentBarHelper: PostDetailCommentbarInterface

    private fun updateTSEvent(recyclerView: RecyclerView, scrollY: Int) {
        val scrollHeight = detailList.height
        if (scrollHeight == 0) {
            return
        }

        val newChunk = (scrollY + scrollHeight / 2) / scrollHeight
        if (newChunk != currentChunkInView && currentChunkStartTime != 0L) {
            val currentTime = SystemClock.elapsedRealtime()
            val existingDuration = chunkwiseTimespent[currentChunkInView]
            var duration = currentTime - currentChunkStartTime
            if (existingDuration != null) {
                duration += existingDuration
            }
            chunkwiseTimespent[currentChunkInView] = duration
            currentChunkStartTime = currentTime
            currentChunkInView = newChunk

        } else if (currentChunkStartTime == 0L) {
            currentChunkStartTime = SystemClock.elapsedRealtime()
        }

        if (!isEOSReached) {
            // Evaluate EOS
            val manager = recyclerView.layoutManager
            val itemCount = recyclerView.adapter?.itemCount ?: 0
            val adapter = recyclerView.adapter as DetailsAdapter
            if (manager is LinearLayoutManager && itemCount > 0) {
                val llm: LinearLayoutManager = manager
                val lp = llm.findLastCompletelyVisibleItemPosition()
                val eosReached: Boolean = adapter.getPositionofEndofList() <= lp
                if (eosReached != isEOSReached) {
                    isEOSReached = true
                    NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(
                        timeSpentEventId,
                        NewsDetailTimespentHelper.IS_EOS_REACHED,
                        java.lang.Boolean.toString(isEOSReached)
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View
        isInBottomSheet = arguments?.getBoolean(Constants.IS_IN_BOTTOM_SHEET) ?: false
        if (isInBottomSheet) {
            //Used Incase of bottom sheet opened from video detail
            val contextThemeWrapper = ContextThemeWrapper(activity, ThemeType.NIGHT.themeId)
            val localInflater = inflater.cloneInContext(contextThemeWrapper)
            view = localInflater.inflate(R.layout.post_details_pager_item, container, false)
            view.findViewById<ConstraintLayout>(R.id.post_root_view).layoutTransition = null
        } else {
            view = inflater.inflate(R.layout.post_details_pager_item, container, false)
        }
        NewsListCardLayoutUtil.manageLayoutDirection(view.findViewById(R.id.post_root_view))

        anchor = view
        var parentLocation: String? = null
        val bundle = arguments
        if (bundle != null) {
            postId = bundle.getString(Constants.STORY_ID)
            adId = bundle.getString(Constants.BUNDLE_AD_ID)
            position = bundle.getInt(Constants.STORY_POSITION, -1)
            Logger.d("NDF2", "onCreateView post detail fragment is called for position $position")
            location = arguments?.getString(NewsConstants.BUNDLE_LOC_FROM_LIST)
                ?: Constants.FETCH_LOCATION_DETAIL
            listLocation = arguments?.getString(Constants.LOCATION) ?: (location + "_" + postId)
            parentStoryId = bundle.getString(Constants.PARENT_STORY_ID)
            postEntityLevel =
                bundle.getString(NewsConstants.POST_ENTITY_LEVEL) ?: PostEntityLevel.TOP_LEVEL.name
            isChildFragment = bundle.getBoolean(NewsConstants.CHILD_FRAGMENT, false)
            isInCollection = bundle.getBoolean(NewsConstants.IS_IN_COLLECTION, false)
            isInCarousel = bundle.getBoolean(NewsConstants.IS_IN_CAROUSEL, false)
            isLandingStory = bundle.getBoolean(Constants.IS_LANDING_STORY, false)
            landingStoryId = bundle.getString(Constants.LANDING_STORY_ID)
            timeSpentEventId =
                bundle.getLong(NewsConstants.TIMESPENT_EVENT_ID, System.currentTimeMillis())
            notificationUniqueId = bundle.getString(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID)
            cardPosition = bundle.getInt(NewsConstants.CARD_POSITION, -1)
            collectionCount = bundle.getInt(NewsConstants.COLLECTION_ITEM_COUNT, -1)
            parentEntity = bundle.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity?
            parentLocation = bundle.getString(Constants.LOCATION)
            searchQuery = bundle.getSerializable(Constants.BUNDLE_SEARCH_QUERY) as? SearchQuery
            groupInfo = bundle.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo?
            isInternalDeeplink = bundle.getBoolean(Constants.IS_INTERNAL_DEEPLINK, false)
            if (!CommonUtils.isEmpty(bundle.getString(Constants.REFERRER_RAW))) {
                referrer_raw = bundle.getString(Constants.REFERRER_RAW)
            }
            val notifCta = bundle.getString(Constants.BUNDLE_NOTIFICATION_CTA_UI_TYPE)
            notifCta?.let { notificationUiType = NotificationUiType.valueOf(it) }

            referrerLead = bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            if (referrerLead == null) {
                referrerLead = PageReferrer()
            }
            referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
            currentPageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, postId)
            referrerFlow = PageReferrer(referrerLead)
            bootStrapCard =
                bundle.getSerializable(Constants.BUNDLE_BOOTSTRAP_CARD) as DetailListCard?
        } else {
            handleBackPress()
            return view
        }
        toolbarHelper = if (DetailOrderingServiceImpl.actionbarVariation == PostDetailActionbarVariation.BOTTOM) {
            PostDetailUnifiedBottomToolbarHelper()
        } else {
            PostDetailUnifiedTopToolbarHelper()
        }
        toolbar = toolbarHelper
        commentBarHelper = toolbarHelper
        section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section

        if (postId == null) {
            if (bootStrapCard?.id == null) {
                return null
            } else {
                postId = bootStrapCard?.id
            }
        }

        DaggerDetailsComponent2.builder().detailsModule2(
            DetailsModule2(
                CommonUtils.getApplication(),
                SocialDB.instance(),
                arguments?.getString(Constants.PAGE_ID) ?: postId!! + System.currentTimeMillis()
                    .toString(),
                postId!!,
                timeSpentEventId, isInBottomSheet,
                location,
                adId = adId,
                sourceId = arguments?.getString(NewsConstants.SOURCE_ID),
                sourceType = arguments?.getString(NewsConstants.SOURCE_TYPE),
                lifecycleOwner = this, section = section,
                parentEntity = parentEntity,
                parentLocation = parentLocation,
                adConsumer = this,
                fragmentManager = activity?.supportFragmentManager,
                searchQuery = searchQuery,
                level = postEntityLevel,
                listLocation = listLocation,
                performLogin = ::performLogin,
                referrerFlow = referrerFlow ?: PageReferrer(referrerLead),
                locationForMenu = MenuLocation.DETAIL,
                fragment = this, fragmentName = TAG)).build().inject(this)

        vm = ViewModelProviders.of(this, detailsViewModelF)[DetailsViewModel::class.java]
        if (currentPageReferrer != null) {
            vm.pageReferrer = currentPageReferrer!!
        }

        cvm = ViewModelProviders.of(this, cardsViewModelF)[CardsViewModel::class.java]
        fragmentCommunicationsViewModel =
            ViewModelProviders.of(requireActivity())[FragmentCommunicationsViewModel::class.java]
        cvm.setCurrentPageReferrer(currentPageReferrer, referrerFlow, null)
        cvm.uniqueScreenId = uniqueScreenId
        nudgeVM = ViewModelProviders.of(this).get(FollowNudgeViewModel::class.java)

        followBlockViewModel =
            ViewModelProviders.of(this, followBlockModelF)[FollowUpdateViewModel::class.java]
        val followEntityDao = SocialDB.instance().followEntityDao()
        toggleFollowUseCase = ToggleFollowUseCase(FollowRepo(followEntityDao)).toMediator2()

        val requestTypes = getRequestType()
        vm.isInCollection = isInCollection
        vm.reportCommentTriggered.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                showReportL2Page()
                vm.reportCommentTriggered.postValue(false)
            }
        })

        vm.getNewsAppJSProviderService.appJSScripts.observeOn(AndroidSchedulers.mainThread())
            .map({ apiResponseVersionData -> transform(apiResponseVersionData, requestTypes) })
            .doOnError {
                vm.getNewsAppJSProviderService
                    .updateDBFromServer()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        loadNewsAppJS(it.data.scripts.get(0), it.data.scripts.get(1))
                    }
                transformError(it)
            }.subscribe {
                loadNewsAppJS(it.get(0), it.get(1))
            }

        vm.executeDetailCardUsecase(postEntityLevel!!)

        detailList = view.findViewById(R.id.details_list)
        progressbar = view.findViewById(R.id.progressbar)
        commentBarHolder = view.findViewById(R.id.comments_bar_holder)
        postRootView = view.findViewById(R.id.post_root_view)
        detailList.recycledViewPool.setMaxRecycledViews(DetailCardType.DISCUSSION_HEADER.index, 0)
        detailList.setFocusable(false)
        // Don't try to keep all discussion items. Just limit to few items.
        detailList.setItemViewCacheSize(DetailCardType.values().size)
        detailList.itemAnimator = null

        recyclerViewLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                AndroidUtils.getMainThreadHandler().postDelayed({
                    detailList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }, 300)
                visibilityCalculator?.update()
            }
        }

        reportAdsMenuListener = ReportAdsMenuFeedbackHelper(this, this)
        vm.readDetailCardUsecase.data().observe(viewLifecycleOwner, Observer{
            if (it.getOrNull() == true) {
                vm.failedNetworkCalls.remove(vm.READ_DETAIL_UC)
            } else {
                val baseError = ApiResponseOperator.getError(it.exceptionOrNull())
                error.value = baseError
                loadError(NhAnalyticsUtility.ErrorViewType.FULLSCREEN)
                vm.failedNetworkCalls.add(vm.READ_DETAIL_UC)
                loadContent()
            }
        })

        vm.userFontSizeConfigProgress.observe(viewLifecycleOwner) {
            val fontDiff = it - NewsConstants.DEFAULT_PROGRESS_COUNT
            val fontSize = NewsConstants.DEFAULT_TITLE_SIZE + (fontDiff * 2)
            vm.titleLength = TextMeasurementUtils.getTextLines(getbindLinkableTextDetail(card, null, bootStrapCard), (2 * CommonUtils.getDimensionInDp(R.dimen.story_detail_padding_left)), (2 * CommonUtils.getDimensionInDp(R.dimen.story_detail_padding_left)), FontWeight.SEMIBOLD, card?.i_langCode()?:bootStrapCard?.i_langCode(), fontSize)
            detailsAdapter?.titleLength = vm.titleLength
        }

        toolbar.initActionBar(view, parentFragment, activity, this, layoutInflater)

        if (bootStrapCard != null) {
            createContentAdManager(bootStrapCard?.id)
            loadContent()
        }
        view?.post { detailList?.scrollToPosition(0) }
        NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
            NewsDetailTimespentHelper.IS_AD, (adId != null).toString())

        if (savedInstanceState == null && arguments?.getBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, false) == true) {
            parentFragment?.let { pf ->
                (pf as? TransitionParent)?.let { transitionParentFragment ->
                    Logger.d(FRAGMENT_TRANSITION_TAG, "PostDetailFragment calling prepare transition on detail view for story: $postId")
                    view.transitionName = postId
                    transitionParentFragment.prepareSharedElementTransition(view)
                    view.doOnPreDraw {
                        Logger.d(FRAGMENT_TRANSITION_TAG, "PostDetailFragment onPreDraw, execute the animation for story: $postId")
                        pf.startPostponedEnterTransition()
                    }
                }
            }
        }
        return view
    }

    private fun initFollowBlockListeners() {

        followBlockViewModel.triggerBottomBarDuration()
        followBlockViewModel.getBottomDurationLiveData.observe(viewLifecycleOwner, {
            if (it != null) {
                bottomBarduration = it
            }
        })
        followBlockViewModel.followBlockLiveData.distinctUntilChanged()
            .observe(viewLifecycleOwner, {

                if (it != null && !FollowUtils.isSameFollowBlockObject(
                        lastSourceFollowBlockEntity,
                        it
                    ) && userVisibleHint
                ) {
                    lastSourceFollowBlockEntity = it
                    if (it.updateType == FollowActionType.FOLLOW) {
                        triggerImplicitFollowUsecase(it)
                    } else if (it.updateType == FollowActionType.BLOCK) {
                        triggerBlockFollowUsecase(it)
                    }
                }
            })
        // Trigger for implicit followlive data
        followBlockViewModel.implicitFollowLiveData.distinctUntilChanged()
            .observe(viewLifecycleOwner, {
                it?.let {
                    if (!isImplicitPopupShown) {
                        Logger.d(LOG_TAG, "Show follow UI")
                        isImplicitPopupShown = true
                        followBlockViewModel.followBlockLiveData.value?.let { it1 ->
                            it1.postSourceEntity?.let { it2 ->
                                setFollowBlockButtonState(false, it1)
                                FollowBlockPrefUtil.incrementSoftFollowSessionCount()
                                followBlockViewModel.incrementFollowBlockImplicitDialogCountUsecase(
                                    bundleOf(
                                        Constants.BUNDLE_SOURCE_ID to it1.sourceId,
                                        Constants.BUNDLE_SOURCE_BLOCK to false
                                    )
                                )
                            }
                        }
                    }
                }
            })
        // Trigger for implicit blockLive data
        followBlockViewModel.implicitBlockLiveData.distinctUntilChanged()
            .observe(viewLifecycleOwner, {
                it?.let {
                    if (!isImplicitPopupShown) {
                        Logger.d(LOG_TAG, "Show block UI")
                        isImplicitPopupShown = true
                        followBlockViewModel.followBlockLiveData.value?.let { it1 ->
                            it1.postSourceEntity?.let { it2 ->
                                setFollowBlockButtonState(true, it)
                                FollowBlockPrefUtil.incrementSoftBlockSessionCount()
                                followBlockViewModel.incrementFollowBlockImplicitDialogCountUsecase(
                                    bundleOf(
                                        Constants.BUNDLE_SOURCE_ID to it1.sourceId,
                                        Constants.BUNDLE_SOURCE_BLOCK to true
                                    )
                                )
                            }
                        }
                    }
                }
            })
    }

    private fun showPostDeletedSnackbar() {
        val activity = activity ?: return
        GenericCustomSnackBar.showSnackBar(
            view = activity.window.decorView,
            context = activity,
            text = CommonUtils.getString(R.string.comment_deleted),
            duration = Snackbar.LENGTH_LONG,
            actionType = null,
            errorMessageClickedListener = null,
            action = null,
            customActionClickListener = null,
            bottomBarVisible = false
        ).show()
    }

    private fun triggerImplicitFollowUsecase(sourceFollowBlockEntity: SourceFollowBlockEntity) {
        followBlockViewModel.triggerImplicitFollowUsecase(sourceFollowBlockEntity)
    }

    private fun triggerBlockFollowUsecase(sourceFollowBlockEntity: SourceFollowBlockEntity) {
        followBlockViewModel.triggerImplicitBlockUsecase(sourceFollowBlockEntity)
    }

    fun setFollowBlockButtonState(
        state: Boolean,
        sourceFollowBlockEntity: SourceFollowBlockEntity
    ) {
        val postSourceAsset = sourceFollowBlockEntity.postSourceEntity
        var nhFollowBlockButton =
            view?.findViewById<NHFollowBlockButton>(R.id.nh_follow_block_button)
        nhFollowBlockButton ?: return
        nhFollowBlockButton.visibility = View.VISIBLE
        if (!state) {
            FollowBlockPrefUtil.updateImplicitFollowShow()
            DialogAnalyticsHelper.logDialogBoxViewedEvent(
                DialogBoxType.IMPLICIT_FOLLOW_PROMPT, currentPageReferrer,
                NhAnalyticsEventSection.NEWS, null
            )
        } else {
            FollowBlockPrefUtil.updateImplicitBlockShow()
            DialogAnalyticsHelper.logDialogBoxViewedEvent(
                DialogBoxType.IMPLICIT_BLOCK_PROMPT, currentPageReferrer,
                NhAnalyticsEventSection.NEWS, null
            )
        }

        nhFollowBlockButton.setState(state)
        nhFollowBlockButton.setOnFollowChangeListener(object :
            NHFollowBlockButton.FollowChangeListerner {
            override fun onFollowChange(newstate: Boolean, reason: FollowUnFollowReason?) {
                HANDLER.removeMessages(IMPLICIT_FOLLOW_BLOCK_PROMPT)
                FollowBlockPrefUtil.updateFollowBlockUpdateFromImplicitSignal(true)
                if (newstate) {
                    FollowBlockPrefUtil.updateImplicitFollowShow(0)
                    triggerFollowBlockcase(sourceFollowBlockEntity, FollowActionType.FOLLOW.name)
                    DialogAnalyticsHelper.logDialogBoxActionEvent(
                        DialogBoxType.IMPLICIT_FOLLOW_PROMPT, currentPageReferrer, Constants.FOLLOW,
                        NhAnalyticsEventSection.NEWS, null
                    )
                } else {
                    FollowBlockPrefUtil.updateImplicitBlockShow(0)
                    triggerFollowBlockcase(sourceFollowBlockEntity, FollowActionType.BLOCK.name)
                    DialogAnalyticsHelper.logDialogBoxActionEvent(
                        DialogBoxType.IMPLICIT_BLOCK_PROMPT, currentPageReferrer, Constants.BLOCK,
                        NhAnalyticsEventSection.NEWS, null
                    )
                }
                nhFollowBlockButton.visibility = View.GONE
            }

            override fun showSnackBarOnFollowChange(nhFollowBlockButton: NHFollowBlockButton): NHFollowBlockButton.FollowSnackBarMetaData? {
                return postSourceAsset?.displayName?.let {
                    NHFollowBlockButton.FollowSnackBarMetaData(
                        nhFollowBlockButton = nhFollowBlockButton,
                        title = it,
                        actionMessage = Constants.UNDO,
                        snackBarActionClickListener = this,
                        referrer = currentPageReferrer
                    )
                }
            }

            override fun undoFollowOrBlockAction(
                referrer: PageReferrer?,
                followActionType: FollowActionType
            ) {
                if (followActionType == FollowActionType.UNFOLLOW) {
                    AnalyticsHelper2.logFollowBlockSnackbarUndoEvent(
                        referrer,
                        Constants.FOLLOW,
                        Constants.UNDO
                    )
                } else if (followActionType == FollowActionType.UNBLOCK) {
                    AnalyticsHelper2.logFollowBlockSnackbarUndoEvent(
                        referrer,
                        Constants.BLOCK,
                        Constants.UNDO
                    )
                }
                triggerFollowBlockcase(sourceFollowBlockEntity, followActionType.name, false)
            }

        })
        if (state) {
            nhFollowBlockButton.setToggleText(
                "",
                CommonUtils.getToggelText(
                    getString(R.string.block_text),
                    postSourceAsset?.displayName
                )
            )
        } else {
            nhFollowBlockButton.setToggleText(
                CommonUtils.getToggelText(
                    getString(R.string.follow),
                    postSourceAsset?.displayName
                ),
                ""
            )
        }
        val msg = createMsgForImplicitFB(!state)
        HANDLER.sendMessageDelayed(msg, TimeUnit.SECONDS.toMillis(bottomBarduration.toLong()))
    }

    private fun createMsgForImplicitFB(follow: Boolean): Message {
        val msg = Message()
        msg.what = IMPLICIT_FOLLOW_BLOCK_PROMPT
        if (follow) {
            msg.obj = Constants.FOLLOW
        } else {
            msg.obj = Constants.BLOCK
        }
        return msg
    }

    private fun hideFollowBlockPopup() {
        view?.findViewById<NHFollowBlockButton>(R.id.nh_follow_block_button)?.visibility = View.GONE
    }

    private fun incrementFollowCounterForImplicitTrigger() {
        card ?: return
        card?.i_source()?.id ?: return
        card?.i_langCode() ?: return
        sourceIdFollow = card?.i_source()?.id
        sourceLangFollow = card?.i_langCode() ?: Constants.DEFAULT_LANGUAGE
        sourceIdFollow?.let {
            followBlockViewModel.updateFollowBlockEntity(
                SourceFollowBlockEntity(
                    sourceId = it, pageViewCount = 1, postSourceEntity = card?.i_source(),
                    updateTimeStamp = System.currentTimeMillis(), sourceLang = sourceLangFollow,
                    updateType = FollowActionType.FOLLOW
                )
            )
            HANDLER.postDelayed(
                { initFollowBlockListeners() }, 300
            )
        }
    }

    private fun createContentAdManager(postId: String?) {
        adId ?: return
        postId ?: return
        if (contentAdDelegate == null) {
            contentAdDelegate = ContentAdDelegate(uniqueScreenId, parentEntity?.id)
            contentAdDelegate?.bindAd(adId, postId)
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                NewsDetailTimespentHelper.CONTENT_BOOSTED_AD_LP_URL, contentAdDelegate?.adEntity?.adLPTimeSpentBeaconUrl)
            parentFragment?.let { postAdReportMenuListener = ReportAdsMenuFeedbackHelper(it, this) }
            toolbar.setActionMoreVisible(
                (contentAdDelegate?.adEntity)?.reportAdsMenuFeedBackEntity?.feedbackUrl?.isNotBlank()
                    ?: false
            )
        }
    }

    fun performLogin(showToast: Boolean, toastMsgId: Int) {
        activity?.let {
            val sso = SSO.getInstance()
            sso.login(it as Activity, LoginMode.USER_EXPLICIT, SSOLoginSourceType.REVIEW, null)
        }
    }

    override fun isFirstChunkOnlyPost(): Boolean {
        return vm.isFirstChunkOnlyPost
    }

    private fun showReportL2Page() {
        val reportUrl = PreferenceManager
            .getPreference(GenericAppStatePreference.REPORT_POST_URL, Constants.EMPTY_STRING)
        val fallback = if (reportUrl.isNullOrEmpty()) return else reportUrl
        val browserIntent = NhBrowserNavigator.getTargetIntent()
        browserIntent.putExtra(DailyhuntConstants.URL_STR, fallback)
        browserIntent.putExtra(DailyhuntConstants.USE_WIDE_VIEW_PORT, true)
        browserIntent.putExtra(DailyhuntConstants.CLEAR_HISTORY_ON_PAGE_LOAD, true)
        browserIntent.putExtra(Constants.VALIDATE_DEEPLINK, true)
        activity?.startActivity(browserIntent)
    }

    fun transform(
        apiResponseVersionData: ApiResponse<NewsAppJSResponse>?,
        newsAppJSTypes: List<NewsAppJSType>
    ): List<NewsAppJS> {
        if (apiResponseVersionData == null || apiResponseVersionData.data == null) {
            return defaultJS()
        }

        val appJSResponse = apiResponseVersionData.data
        if (CommonUtils.isEmpty(appJSResponse.scripts)) {
            return defaultJS()
        }

        val list = java.util.ArrayList<NewsAppJS>()
        for (appJS in appJSResponse.scripts) {
            if (newsAppJSTypes.contains(appJS.newsAppJSType)) {
                list.add(appJS)
            }
        }
        return if (CommonUtils.isEmpty(list)) defaultJS() else list
    }

    private fun transformError(error: Throwable): Observable<List<NewsAppJS>> {
        return Observable.just(defaultJS())
    }

    fun defaultJS(): List<NewsAppJS> {
        val jsList = java.util.ArrayList<NewsAppJS>()

        val chunk1JS = NewsAppJS()
        val chunk1Type = NewsAppJSType()
        chunk1Type.type = NewsAppJSType.NEWS_DETAIL_CHUNK_1
        chunk1JS.newsAppJSType = chunk1Type

        val chunk2JS = NewsAppJS()
        val chunk2Type = NewsAppJSType()
        chunk2Type.type = NewsAppJSType.NEWS_DETAIL_CHUNK_2
        chunk2JS.newsAppJSType = chunk2Type

        chunk1JS.css = DEFAULT_CSS_NON_LITE_MODE
        chunk2JS.css = DEFAULT_CSS_NON_LITE_MODE

        try {
            val bytes =
                FileUtil.readData(CommonUtils.getApplication().assets.open(NON_LITE_JS_FILE))
            val nonLiteJS = String(bytes)
            chunk1JS.jsScript = nonLiteJS
            chunk2JS.jsScript = nonLiteJS
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

        jsList.add(chunk1JS)
        jsList.add(chunk2JS)

        return jsList
    }


    fun getRequestType(): List<NewsAppJSType> {
        val newsAppJSTypes = java.util.ArrayList<NewsAppJSType>()
        val chunk1Type = NewsAppJSType()
        chunk1Type.type = NewsAppJSType.NEWS_DETAIL_CHUNK_1
        val chunk2Type = NewsAppJSType()
        chunk2Type.type = NewsAppJSType.NEWS_DETAIL_CHUNK_2
        newsAppJSTypes.add(chunk1Type)
        newsAppJSTypes.add(chunk2Type)
        return newsAppJSTypes
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? NHBaseActivity?)?.setFlexibleUpdateSupported(true)
        nonLinearFeedHelper.lifecycleOwner = viewLifecycleOwner

        /*Small portion loading on offscreen without adds, content2 and more stories */
        vm.detailCardScan.observe(this, Observer { it ->
            vm.detailCardScan.removeObservers(this)
            val t = it.data
            if (t != null) {
                card = t
            }

            createContentAdManager(t?.i_id())

            if (card?.i_type() != AssetType2.COMMENT.name) {
                toolbar.setActionDisclaimerVisible(false)

            } else {
                toolbar.setActionDisclaimerVisible(false)
            }
            if (card?.i_subFormat() == SubFormat.RICH_PHOTOGALLERY) {
                vm.loadRichGalleryContents()
                registerForShortLikes()
                registerForMyDiscussions()
            }

            vm.fetchPhotoChildIfRequired(card)

            if (isInCollection || isInCarousel) {
                Logger.d(
                    Constants.NON_LINEAR_FEED,
                    "Item is in carousel hence not setting nonlinear post url"
                )
            } else {
                Logger.d(Constants.NON_LINEAR_FEED, "the landing story id is $landingStoryId")
                nonLinearFeedHelper.asset = t?.rootPostEntity()
                nonLinearFeedHelper.position = position
                nonLinearFeedHelper.parentId = landingStoryId ?: t?.rootPostEntity()?.i_id()
            }
            card?.let { post ->
                if (adsHelper == null && supportAds(post))
                    adsHelper = adsHelperF.create(post, uniqueScreenId, currentPageReferrer)
            }
            if (!isInBottomSheet && isFragmentVisible()) {
                referrerLead?.let {
                    card?.i_isFollowin()?.let { isFollowing ->
                        if (PreferenceManager.getPreference(
                                AppStatePreference.IS_APP_REGISTERED,
                                false
                            )
                        ) {
                            nudgeVM.nudges(it, isFollowing, arguments)
                                .observe(this, Observer { events ->
                                    Logger.d(TAG, "loadData: $events")
                                    events.firstOrNull()?.let { showFollowNudge(it) }
                                })
                        }
                    }
                }
            }

            loadContent()
            registerDetailStream()
        })
    }

    private fun showFollowNudge(eventsInfo: EventsInfo) {
        val tooltipWrapper = NudgeTooltipWrapper()
        val anc = anchor.findViewById<ConstraintLayout>(R.id.follow_button)
        val text = eventsInfo.activity?.attributes?.get("text") ?: Constants.EMPTY_STRING
        val time = eventsInfo.activity?.attributes?.get("tooltipDurationSec") ?: "10"
        anc?.let {
            if (CommonUtils.equals(
                    UserPreferenceUtil.getUserNavigationLanguage(),
                    NewsConstants.URDU_LANGUAGE_CODE
                )
            ) {
                tooltipWrapper.showFollowTooltip(
                    requireContext(), R.layout
                        .nudge_tooltip_follow_urdu, text, time.toLong(), it
                )
            } else {
                tooltipWrapper.showFollowTooltip(
                    requireContext(), R.layout
                        .nudge_tooltip_follow_right_arrow, text, time.toLong(), it
                )
            }
            nudgeVM.nudgeShown(eventsInfo.id)
        }
    }

    private fun supportAds(post: CommonAsset): Boolean {
        return !isInBottomSheet && post.i_subFormat() != SubFormat.RICH_PHOTOGALLERY
    }

    private fun registerDetailStream() {
        vm.detailCardScan.observe(viewLifecycleOwner, Observer { it ->
            val t = it.data
            Logger.d(
                TAG,
                "registerDetailStream: fromDB: counts=${LikeEmojiBindingUtils.debugCountsString(t)}"
            )
            if (t != null) {
                if (adsHelper == null && supportAds(t)) {
                    adsHelper = adsHelperF.create(t, uniqueScreenId, currentPageReferrer)
                }
                createContentAdManager(t.i_id())
            }

            if (!secondChunkFetched ) {
                vm.fetchMoreDetails(
                    t?.i_moreContentLoadUrl(),
                    postEntityLevel ?: PostEntityLevel.TOP_LEVEL.name
                )
                secondChunkFetched = true
            }

            vm.fetchPhotoChildIfRequired(card)
            var scrollToTop = false
            if (card == null && t != null) {
                scrollToTop = true
            }

            if (t != null) {
                val countsChanged = t.i_counts() == card?.i_counts()
                card = t
                Logger.d(
                    TAG,
                    "registerStream-detailCardScan: ${card?.i_VC()}, countCh=$countsChanged"
                )
                if (countsChanged) detailsAdapter?.updateCard(card, notifyViral = true)
                if (t.i_isDeleted()) {
                    toolbar.hideActionMoreView()
                }
            }

            val uniqueId = t?.postEntity?.postEntity?.uniqueId
            if (uniqueId != null) {
                registerForDiscussion(uniqueId)
                registerForAssociation(uniqueId)
            }

            if (card?.i_type() == AssetType2.COMMENT.name) {
                currentPageReferrer = PageReferrer(NewsReferrer.COMMENT_DETAIL, postId)
                fetchParentCard()
            } else {
                toolbar.setActionDisclaimerVisible(false)
            }

            nonLinearFeedHelper.asset = t?.rootPostEntity()
            nonLinearFeedHelper.position = position
            loadContent()

            if (scrollToTop) {
                detailList.scrollToPosition(0)
            }

            startAdsHelper()
            if (isLandingStory) {
                //Send out the prefetch request
                //adsHelper?.requestAds(activity, AdPosition.MASTHEAD)
            }

            card?.i_content2()?.let {
                if (adsHelperStarted)
                    adsHelper?.requestAds(AdPosition.SUPPLEMENT)
            }
        })

        vm.chunk2.observe(viewLifecycleOwner, Observer {
            if (it.error != null) {
                val baseError = ApiResponseOperator.getError(it.error)
                error.value = baseError
                Logger.e(TAG, "ERROR regDetailStream ${it.error} ${it.data}")
                loadError(NhAnalyticsUtility.ErrorViewType.HALF_PAGE)
            } else {
                if (card == null && it.data != null) {
                    card = it.data
                    loadContent()
                }

                if (adsHelperStarted) {
                    adsHelper?.requestAds(AdPosition.SUPPLEMENT)
                }

                if (vm.isFirstChunkOnlyPost && vm.isFirstChunkLoaded) {
                    onFullPageLoaded()
                }
            }
        })

        vm.deletedPrimaryItem.observe(viewLifecycleOwner, Observer {
            if (it.getOrNull() == true) {
                showPostDeletedSnackbar()
                activity?.onBackPressed()
            }
        })
        vm.titleLengthCount.observe(viewLifecycleOwner, Observer {
            val newIsTitleGreater = it > TITLE_TOP_LINE_COUNT_THRESHOLD
            if (isTitleGreater != newIsTitleGreater) {
                // Change detected. Notify adapter.
                isTitleGreater = newIsTitleGreater
                detailsAdapter?.updateTitlePosition(it)
            }
        })

        vm.suggestedFolows.observe(viewLifecycleOwner, Observer {
            if (it.error != null) {
                Logger.e(TAG, "ERROR ${it.error} ${it.data}")
            } else {
                suggestedFollow = it
                loadSuggestedContent()
            }
        })

        vm.photoChild.observe(viewLifecycleOwner, Observer {
            if (it.error != null) {
                val baseError = ApiResponseOperator.getError(it.error)
                error.value = baseError
                Logger.e(TAG, "ERROR ${it.error} ${it.data}")
                loadError(NhAnalyticsUtility.ErrorViewType.HALF_PAGE)
            } else {
                loadPhotoChild(it)
            }
        })

        vm.discussionLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                HANDLER.removeMessages(DISCUSSION_LOADED)
            } else {
                detailsAdapter?.notifyDiscussionHeader()
            }

            detailsAdapter?.updateVisibleList()
        })

        vm.secondChunkLoading.observe(viewLifecycleOwner, Observer {
            detailsAdapter?.updateVisibleList()
        })
    }

    private fun registerForAssociation(uniqueId: String) {
        vm.fetchAssociation(uniqueId)
        vm.associationScan?.observe(viewLifecycleOwner, Observer {
            associationList = it.data as List<AllLevelCards>?
            detailsAdapter?.updateAssociation(it)
        })
    }

    private fun registerForDiscussion(uniqueId: String) {
        vm.fetchDiscussion(uniqueId)
        vm.discussionScan?.observe(viewLifecycleOwner, Observer {
            var notifyHeader = false
            if (discussionList.isNullOrEmpty() && it.data?.isNotEmpty() == true) {
                notifyHeader = true
            }
            if (discussionList?.isNotEmpty() == true && it.data.isNullOrEmpty()) {
                notifyHeader = true
            }

            if (notifyHeader) {
                detailsAdapter?.notifyDiscussionHeader()
            }

            discussionList = it.data
            if (!isInBottomSheet || isBottomSheetExpanded) {
                detailsAdapter?.updateDiscussion(it)
            }
        })
    }

    private fun fetchParentCard() {
        vm.fetchParentCard()
        vm.parentCard.observe(viewLifecycleOwner, Observer {
            if (detailsAdapter?.parentCard == null && card != null) {
                shortContentLoadMore()
            }

            detailsAdapter?.parentCard = it.data
            detailsAdapter?.updateVisibleList()
            detailsAdapter?.parentCardChange()
        })
    }

    override fun onRetryClicked(speedEvent: ConnectionSpeedEvent?, baseError: BaseError?) {
        if (speedEvent?.connectionSpeed == ConnectionSpeed.NO_CONNECTION) {
            var nwSettingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            activity?.startActivity(nwSettingIntent)
        } else {
            if (baseError.dbgCode().get().equals("BB04")) {
                handleBackPress()
            } else {
                vm.retryFailedUsecases(DetailsBindUtils.isDiscussion(card))
                if (card != null && card?.i_content2() == null) {
                    vm.fetchMoreDetails(
                        card?.i_moreContentLoadUrl(),
                        postEntityLevel ?: PostEntityLevel.TOP_LEVEL.name
                    )
                }
            }

        }
    }

    private fun loadNewsAppJS(newsAppJSChunk1: NewsAppJS?, newsAppJSChunk2: NewsAppJS?) {
        this.newsAppJSChunk1 = newsAppJSChunk1
        this.newsAppJSChunk2 = newsAppJSChunk2

        detailsAdapter?.updateNewsAppJS(newsAppJSChunk1, newsAppJSChunk2)
    }

    private fun loadPhotoChild(photoChildPojo: PhotoChildPojo?) {
        detailsAdapter?.updatePhotoChild(photoChildPojo)
    }

    private fun loadRelatedStories() {
        val dislikedStoriesParam = dislikedStories
        if (dislikedStoriesParam != null) {
            val items = mutableListOf<CommonAsset>()
            relatedStories?.data?.forEach {
                if (it is CommonAsset) {
                    if (!dislikedStoriesParam.contains(it.i_id())) {
                        items.add(it)
                    }
                }
            }

            val filteredRelatedStories = CardsPojo(
                items, relatedStories?.tsData,
                relatedStories?.error, relatedStories?.tsError
            )
            detailsAdapter?.updateRelatedStories(filteredRelatedStories)
        } else {
            detailsAdapter?.updateRelatedStories(relatedStories)
        }
    }

    private fun loadShortLikedList() {
        detailsAdapter?.updateLikedList(likedList)
    }

    private fun loadSuggestedContent() {
        detailsAdapter?.updateSuggestedFollow(suggestedFollow)
    }

    private fun loadContent() {
        hideShimmer()
        if (detailsAdapter == null) {
            var fontSize = NewsConstants.DEFAULT_TITLE_SIZE
            var progress = PreferenceManager.getPreference(
                NewsPreference.USER_PREF_FONT_PROGRESS,
                NewsConstants.DEFAULT_PROGRESS_COUNT)
            vm.userFontSizeConfigProgress.value?.let {
                progress = it
            }

            fontSize = NewsConstants.DEFAULT_TITLE_SIZE + ((progress - NewsConstants.DEFAULT_PROGRESS_COUNT) * 2)
            vm.titleLength = TextMeasurementUtils.getTextLines(getbindLinkableTextDetail(card, null, bootStrapCard), (2 * CommonUtils.getDimensionInDp(R.dimen.story_detail_padding_left)), (2 * CommonUtils.getDimensionInDp(R.dimen.story_detail_padding_left)), FontWeight.SEMIBOLD, card?.i_langCode()?:bootStrapCard?.i_langCode(), fontSize)
            val newIsTitleGreater =vm.titleLength > TITLE_TOP_LINE_COUNT_THRESHOLD
            if (isTitleGreater != newIsTitleGreater) {
                // Change detected. Notify adapter.
                isTitleGreater = newIsTitleGreater
            }
            currentOrder = enrichOrderWithAds(findCardTypeIndex(card))
            detailsAdapter = DetailsAdapter(
                activity = context as AppCompatActivity,
                fragment = this,
                parentLifecycleOwner = viewLifecycleOwner,
                detailsViewModel = vm,
                cvm = cvm,
                detailListingOrder = currentOrder,
                card = card,
                bootStrapCard = bootStrapCard,
                parentCard = null,
                suggedtedFollowsPojo = suggestedFollow,
                likedListPojo = likedList,
                relatedStories = relatedStories,
                isInBottomSheet = isInBottomSheet,
                postListener = this,
                error = error,
                uniqueRequestId = uniqueScreenId,
                adEntityReplaceHandler = this,
                interactiveAdListener = this,
                webCacheProvider = this,
                timeSpentEventId = timeSpentEventId,
                section = section,
                adsHelper = adsHelper,
                contentAdDelegate = contentAdDelegate,
                detailList = detailList,
                reportAdsMenuListener = reportAdsMenuListener,
                titleLength = vm.titleLength,
                titleLengthOld = vm.titleLength
            )

            detailList.adapter = detailsAdapter
            detailsAdapter?.updateNewsAppJS(newsAppJSChunk1, newsAppJSChunk2)
            isTransparentActionBar = DetailsBindUtils.canShowTransparentActionbar(card, bootStrapCard)

            if (!isInBottomSheet && toolbar.isOnTheTop() && !isTransparentActionBar ) {
                val decoration = ActionbarSpaceDecoration(getActionBarHeight())
                detailList.addItemDecoration(decoration)
            }

            val layoutManager =
                LinearLayoutManagerWrapper(this.context, CommonUtils.getDeviceScreenHeight())
            layoutManager.isItemPrefetchEnabled = true
            detailList.layoutManager = layoutManager
            visibilityCalculator = VisibilityCalculator(detailList, layoutManager)
            visibilityCalculator?.start()

            isFollowing = card?.i_isFollowin()
            if (isInBottomSheet) {
                vm.loadSeconChunkContents(DetailsBindUtils.isDiscussion(card))
                if (registeringAddiitonalContentsPending) {
                    registerForRelatedStories()
                    registerForShortLikes()
                    registerForMyDiscussions()
                    registeringAddiitonalContentsPending = false
                    detailsAdapter?.secondChunkLoaded = true
                }
            }

            detailList.visibility = View.VISIBLE
            if (isInBottomSheet) {
                toolbar.updateToolbarVisibility(View.GONE)
                toolbar.hideActionMoreView()
            } else {
                toolbar.updateToolbarVisibility(View.VISIBLE)
            }
            commentBarHolder.visibility = View.VISIBLE
        } else {
            //detailsAdapter?.titleLength = TextMeasurementUtils.getTextLines(getbindLinkableTextDetail(card, null, bootStrapCard), null, 16, 16, FontWeight.SEMIBOLD, card?.i_langCode())
            isFollowing = card?.i_isFollowin()
            currentOrder = enrichOrderWithAds(findCardTypeIndex(card))
            detailsAdapter?.updateCard(card, currentOrder)
            detailsAdapter?.setAdsHelper(adsHelper)
            commentBarHelper.inflateCommentsBar(
                isInBottomSheet, this.activity, card,
                commentBarHolder, layoutInflater, cvm, parentStoryId, postId,
                currentPageReferrer, groupInfo
            )
            if (isInBottomSheet) {
                vm.loadSeconChunkContents(DetailsBindUtils.isDiscussion(card))
            }

            if (card?.i_viral() != null || card?.i_poll() != null) {
                shortContentLoadMore()
            }
        }

        if (!isCardDependedItemsInitialized && card != null) {
            isCardDependedItemsInitialized = true
            setOnScrollListener()
//            initFollowBlockListeners()
            startAppIndexing()
            logStoryPageViewEvent()
            if (userVisibleHint) {
                incrementFollowCounterForImplicitTrigger()
            }
        }

        if (card?.i_subFormat() == SubFormat.RICH_PHOTOGALLERY) {
            secondChunkFetched = true // No second chunk for rich gallery
            detailsAdapter?.secondChunkLoaded = true
        }

        vm.lastestDiscussionLoadingState.observe(viewLifecycleOwner, Observer {
            if (!it) {
                vm.discussionLoading.postValue(false)
            }

            HANDLER.sendEmptyMessageDelayed(DISCUSSION_LOADED, 1000)
        })

        vm.discussionFirstPage.observe(viewLifecycleOwner, Observer {
            HANDLER.sendEmptyMessageDelayed(DISCUSSION_LOADED, 1000)
        })

        if (card != null) {
            notificationUiType?.let {
                NotificationCtaUiHelper.handleNotificationCtaType(
                    it, card,
                    cvm.cardClickDelegate.getShareUsecase(), postId ?: "", location, section
                )
                notificationUiType = null
            }
        }
        if (card?.i_format() == Format.VIDEO) {
            //If is in video detail discussion then set BG as black
            view?.findViewById<ConstraintLayout>(R.id.post_root_view)
                ?.setBackgroundColor(Color.BLACK)

        }
    }

    private fun hideShimmer() {
        progressbar?.let {
            postRootView.removeView(it)
            progressbar = null
        }
    }

    private fun loadError(errorViewType: NhAnalyticsUtility.ErrorViewType) {
        if (detailsAdapter != null && error.value != null) {
            detailsAdapter?.updateError(error)
        }
        ErrorLogHelper.logNewsDetailErrorEvent(
            error.value,
            errorViewType,
            currentPageReferrer,
            parentEntity
        )
    }

    private fun enrichOrderWithAds(elements: MutableList<String>): List<String> {
        return adsHelper?.addAdStubsInPost(elements.mapNotNull {
            try {
                DetailCardType.valueOf(it)
            } catch (ex: Exception) {
                null
            }
        }.toMutableList()) ?: elements
    }

    private fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        activity?.theme?.resolveAttribute(R.attr.actionBarSize, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(
            typedValue.data, resources.displayMetrics
        )
    }

    private fun setOnScrollListener() {
        var scrollY = 0
        var toolColorChanged = false
        detailList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrollY += dy
                if (isTransparentActionBar) {
                    val lManager = recyclerView.layoutManager as LinearLayoutManager?
                    val firstElementPosition = lManager!!.findFirstVisibleItemPosition()
                    if (firstElementPosition == 0) {
                        toolColorChanged = true
                        val alpha =
                            Math.min(1.0F, scrollY.toFloat() / 400)
                        toolbar.setTransparentToolbar(alpha)
                    }
                    if (firstElementPosition >= 1 && toolColorChanged) {
                        toolbar.setToolbar()
                        toolColorChanged = false
                    }
                }
                if (dy > 0) {
                    onBottomToTopSwipe()
                    vm.loadSeconChunkContents(DetailsBindUtils.isDiscussion(card))
                    if (registeringAddiitonalContentsPending && detailsAdapter?.secondChunkLoaded == true) {
                        registerForRelatedStories()
                        registerForShortLikes()
                        registerForMyDiscussions()
                        registeringAddiitonalContentsPending = false
                    }
                } else {
                    onTopToBottomSwipe()
                }

                if (userVisibleHint && scrollY > 0) {
                    updateTSEvent(recyclerView, scrollY)
                }

            }
        })
    }


    private fun triggerFollowBlockcase(
        sourceFollowBlockEntity: SourceFollowBlockEntity,
        action: String,
        postFragmentEvent: Boolean = true
    ) {
        val source = sourceFollowBlockEntity?.postSourceEntity
        val id = source?.id ?: run { Logger.e(TAG, "source id  missing"); return }
        val type = source.entityType ?: AssetType.SOURCE.name
        val entity = ActionableEntity(
            entityId = id,
            entityType = type,
            entitySubType = source.type,
            displayName = source.displayName ?: Constants.EMPTY_STRING,
            entityImageUrl = source.entityImageUrl ?: Constants.EMPTY_STRING,
            iconUrl = source.icon ?: Constants.EMPTY_STRING,
            deeplinkUrl = source.deeplinkUrl ?: Constants.EMPTY_STRING,
            nameEnglish = source.nameEnglish
        )

        toggleFollowUseCase.execute(
            bundleOf(
                ToggleFollowUseCase.B_FOLLOW_ENTITY to entity,
                ToggleFollowUseCase.B_ACTION to action
            )
        )
        val bundle = Bundle()
        bundle.putSerializable(
            Constants.SOURCE_ENTITY, PostEntity(
                id = sourceFollowBlockEntity.sourceId,
                langCode = sourceFollowBlockEntity.sourceLang,
                source = sourceFollowBlockEntity.postSourceEntity
            )
        )
        bundle.putLong(Constants.EVENT_CREATED_AT, System.currentTimeMillis())
        if (postFragmentEvent) {
            fragmentCommunicationsViewModel.fragmentCommunicationLiveData.postValue(
                FragmentCommunicationEvent(
                    hostId = (requireActivity() as NewsBaseActivity).activityId,
                    useCase = Constants.CAROUSEL_LOAD_EXPLICIT_SIGNAL,
                    anyEnum = action,
                    arguments = bundle
                )
            )
        }
    }

    private fun registerForAds() {
        vm.extraCards.observe(viewLifecycleOwner, {
            if (it != null) {
                detailsAdapter?.updateAdsWithExtraMeta(it)
            }
        })
        vm.likeList.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                detailsAdapter?.updateLikeTypeList(it)
            }
        })
    }

    private fun registerForMyDiscussions() {
        vm.mydiscussions?.observe(viewLifecycleOwner, Observer {
            if (lastKnownMyDiscussionCount != it) {
                lastKnownMyDiscussionCount = it
                vm.includeLoadDiscussion()
            }
        })

        vm.mydiscussionsRepliesCount?.observe(viewLifecycleOwner, Observer {
            if (lastKnownMyDiscussionRepliesCount == null) {
                // Ignore initial update
                lastKnownMyDiscussionRepliesCount = it
                detailsAdapter?.updateReplyCount(lastKnownMyDiscussionRepliesCount)
            }

            if (lastKnownMyDiscussionRepliesCount != it) {
                compareAndOpenComment(lastKnownMyDiscussionRepliesCount, it)
                lastKnownMyDiscussionRepliesCount = it
                detailsAdapter?.updateReplyCount(lastKnownMyDiscussionRepliesCount)
            }
        })

        vm.myInteraction?.observe(viewLifecycleOwner, Observer {
            if (lastKnownMyInteraction != it) {
                lastKnownMyInteraction = it
                detailsAdapter?.updateLocalLikes(lastKnownMyInteraction)
            }
        })
    }

    private fun compareAndOpenComment(
        lastKnownReplyCount: List<ReplyCount>?,
        newReplyCount: List<ReplyCount>?
    ) {
        if (lastKnownReplyCount != null && newReplyCount != null) {
            for (replyItem in newReplyCount) {
                if (!lastKnownReplyCount.contains(replyItem)) {
                    openDiscussionDetail(replyItem.parentId)
                    return
                }
            }
        }
    }

    private fun openDiscussionDetail(discussionId: String) {
        discussionList?.forEach {
            if (it.i_id() == discussionId && this.view != null) {
                cvm.onViewClick(
                    this.view!!,
                    it,
                    CardsBindUtils.bundle(Constants.BUNDLE_IN_DETAIL, true)
                )
            }
        }
    }

    private fun loadDiscussionFirstPage() {
        vm.reloadDiscussionFirstPage(DetailsBindUtils.isDiscussion(card))
    }

    private fun registerForRelatedStories() {
        vm.relatedstories?.observe(viewLifecycleOwner, Observer {
            if (it.error != null) {
                Logger.e(TAG, "ERROR fetching related stories ${it.error} ${it.data}")
            } else {
                relatedStories = it
                loadRelatedStories()
            }
        })

        vm.dislikeStories?.observe(viewLifecycleOwner, Observer {
            val changed = dislikedStories?.size != it.size
            dislikedStories = it
            if (changed) {
                loadRelatedStories()
            }
        })
    }

    private fun registerForShortLikes() {
        vm.shortLikes.observe(viewLifecycleOwner, Observer {
            HANDLER.sendEmptyMessageDelayed(DISCUSSION_LOADED, 500)
            if (it.error != null) {
                detailsAdapter?.secondChunkLoaded = true
                val baseError = ApiResponseOperator.getError(it.error)
                error.value = baseError
                Logger.e(TAG, "ERROR ${it.error} ${it.data}")
                loadError(NhAnalyticsUtility.ErrorViewType.HALF_PAGE)
            } else {
                likedList = it
                loadShortLikedList()
            }
        })
    }

    private fun onTopToBottomSwipe() {

        if (!isToolbarVisible) {
            isToolbarVisible = true
            toolbar.showWithAnimation()
        }
    }

    private fun onBottomToTopSwipe() {

        if (isToolbarVisible) {
            isToolbarVisible = false
            toolbar.hideWithAnimation()
        }
    }

    override fun onScrollToOtherPerspective() {
        var position = detailsAdapter?.onClickOtherPerspective() ?: -1
        if (position >= 0) {
            detailList.scrollToPosition(position)
        }

        position = detailsAdapter?.onClickOtherPerspective() ?: -1

        detailList.postDelayed(Runnable {
            // or use other API
            if (position >= 0) {
                detailList.scrollToPosition(position)
            }

            // give a delay of one second
        }, 500)
    }

    override fun handleStoryPhotoClick(url: String?) {
        if (card == null)
            return

        vm.onPostClick(card!!, url)
    }

    override fun shortContentLoadMore() {
        // Fetch entire contents as we need to auto scroll.
        vm.loadSeconChunkContents(DetailsBindUtils.isDiscussion(card))
        if (registeringAddiitonalContentsPending) {
            registerForRelatedStories()
            registerForShortLikes()
            registerForMyDiscussions()
            registeringAddiitonalContentsPending = false
            detailsAdapter?.secondChunkLoaded = true
        }
    }

    override fun onFirstChunkLoaded() {
        vm.isFirstChunkLoaded = true
        if (vm.isFirstChunkOnlyPost) {
            onFullPageLoaded()
        }
    }

    override fun onFullPageLoaded() {
        if (!isFullPageLoaded && detailList.height > 0) {
            isFullPageLoaded = true
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(
                timeSpentEventId,
                NewsDetailTimespentHelper.FULL_PAGE_LOADED,
                java.lang.Boolean.toString(isFullPageLoaded)
            )
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(
                timeSpentEventId,
                NewsDetailTimespentHelper.SCREEN_SIZE,
                Integer.toString(detailList.height)
            )

            if (userVisibleHint) {
                updateTSEvent(detailList, 0)
            }
        }

        detailsAdapter?.secondChunkLoaded = true
        detailsAdapter?.updateVisibleList()

        try {
            vm.loadSeconChunkContents(DetailsBindUtils.isDiscussion(card) && view != null)
            if (registeringAddiitonalContentsPending) {
                registerForRelatedStories()
                registerForShortLikes()
                registerForMyDiscussions()
                registeringAddiitonalContentsPending = false
            }
        } catch (ex: IllegalStateException) {
            Logger.e(TAG, "Exception trying fetch more contents", ex)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.action_more_newsdetail) {
            contentAdDelegate?.adEntity?.let {
                postAdReportMenuListener?.onReportAdsMenuClick(it)
                return true
            }
            card?.let {
                val intent = Intent(Constants.MENU_FRAGMENT_OPEN_ACTION)
                intent.putExtra(
                    Constants.BUNDLE_STORY,
                    it.toMinimizedCommonAsset() as? Serializable
                )
                intent.putExtra(Constants.BUNDLE_MENU_CLICK_LOCATION, menuLocation)
                intent.putExtra(NewsConstants.DH_SECTION, section)
                intent.putExtra(Constants.BUNDLE_LOCATION_ID, "Detail")
                intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW, referrerFlow)
                intent.putExtra(Constants.BUNDLE_GROUP_INFO, groupInfo)
                intent.putExtra(Constants.REFERRER, currentPageReferrer)
                intent.putExtra(Constants.BUNDLE_ENTITY_ID, "")
                NavigationHelper.navigationLiveData.postValue(
                    NavigationEvent(
                        intent,
                        callback = null
                    )
                )
                return true
            }
            return false
        } else if (id == R.id.action_disclaimer_newsdetail) {
            val activityVar = activity
            if (activityVar != null) {
                showDisclaimer(activityVar)
            }
        }
        return false
    }

    private fun showDisclaimer(fragmentActivity: androidx.fragment.app.FragmentActivity) {
        val dialogFragment = DisclaimerDialogFragment()
        val disclaimerUrl = PreferenceManager
            .getPreference(GenericAppStatePreference.DISCLAIMER_URL, Constants.EMPTY_STRING)
        val fallback = if (disclaimerUrl.isNullOrEmpty()) return else disclaimerUrl
        dialogFragment.arguments = bundleOf(Constants.BUNDLE_DISCLAIMER_URL to fallback)
        dialogFragment.show(fragmentActivity.supportFragmentManager, "disclaimer")
    }

    fun startAppIndexing() {
        val title = card?.i_title() ?: ""
        val engTitle = card?.i_englishTitle() ?: ""
        val deeplinkURl = card?.i_deeplinkUrl() ?: ""
        if (!AppIndexingHelper.isAppIndexingEnabled()) {
            return
        }

        this.appIndexingTitle =
            AppIndexingHelper.generateAppIndexingTitle(NewsConstants.NEWS, null, title, engTitle)
        try {
            this.appIndexingUri = AppIndexingHelper.getUri(deeplinkURl)
            AppIndexingHelper.startAppIndexing(appIndexingTitle, appIndexingUri, APP_INDEX_TAG)
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    fun stopAppIndexing() {
        if (null == appIndexingTitle || !AppIndexingHelper.isAppIndexingEnabled()) {
            return
        }

        try {
            AppIndexingHelper.endAppIndexing(
                this.appIndexingTitle,
                this.appIndexingUri,
                APP_INDEX_TAG
            )
        } catch (e: Exception) {
            Logger.caughtException(e)
        }

    }

    override fun onStart() {
        super.onStart()
        val isVisible = super.getUserVisibleHint()
        if (isVisible) {

            startAdsHelper()
        }

        visibilityCalculator?.start()
        visibilityCalculator?.notifyFragmentVisible(isVisible)
    }

    override fun onPause() {
        visibilityCalculator?.stop()

        // Stop timer and schedule and event
        updateLastKnownChunk()
        HANDLER.removeMessages(MSG_TIMESPENT_RELATED_CONDITION)
        HANDLER.sendMessageDelayed(
            Message.obtain(HANDLER, MSG_SCREEN_PAUSED),
            NewsDetailTimespentHelper.TIMESPENT_PAUSE_DELAY.toLong()
        )
        super.onPause()
    }

    private fun updateLastKnownChunk() {
        if (currentChunkStartTime != 0L) {

            val currentTime = SystemClock.elapsedRealtime()
            val existingDuration = chunkwiseTimespent.get(currentChunkInView)
            var duration = currentTime - currentChunkStartTime
            if (existingDuration != null) {
                duration += existingDuration
            }
            chunkwiseTimespent.put(currentChunkInView, duration)
            currentChunkStartTime = 0L

        }
    }

    override fun onResume() {
        super.onResume()

        HANDLER.removeMessages(MSG_SCREEN_PAUSED)
        if (isTimespentPaused) {
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(
                timeSpentEventId,
                NewsDetailTimespentHelper.IS_PAUSED, java.lang.Boolean.FALSE.toString()
            )
            isTimespentPaused = false
            currentChunkStartTime = SystemClock.elapsedRealtime()
        }
        if (userVisibleHint) {
            visibilityCalculator?.start()
        }
    }

    override fun onStop() {
        super.onStop()
        adsHelperStarted = false
        adsHelper?.stop()
        stopAppIndexing()
    }

    override fun onDestroy() {
        super.onDestroy()
        adsHelper?.destroy()

        for (viewWeakReference in webViewCache.values) {
            viewWeakReference?.get()?.let { adView ->
                // Need to keep reference to webview for atleast 1 sec to allow js
                // to trigger sessionFinish event.
                AndroidUtils.getMainThreadHandler().postDelayed(
                    { adView.destroy() },
                    (if (adView.isOMTrackingEnabled) AdConstants.OMID_WEBVIEW_DESTROY_DELAY else 0).toLong()
                )
            }
        }
        webViewCache.clear()
        getAdsMap()?.clear()
        HANDLER.removeCallbacksAndMessages(null)
        if (!isInBottomSheet) {
            if (timeSpentEventId != 0L) {
                if (firedTSAfterPause) {
                    NewsDetailTimespentHelper.getInstance()
                        .postDeleteTimespentEvent(timeSpentEventId)
                } else {
                    // If fragment viewed update time and post timespent event
                    updateLastKnownChunk()
                    if (!chunkwiseTimespent.isEmpty()) {
                        NewsDetailTimespentHelper.getInstance().postSendTimespentEvent(
                            timeSpentEventId,
                            chunkwiseTimespent, false,
                            if (backPressed)
                                NhAnalyticsUserAction.BACK
                            else
                                NhAnalyticsUserAction.SWIPE
                        )
                    }
                }
            }
        }
        try {
            detailList.viewTreeObserver.removeOnGlobalLayoutListener(recyclerViewLayoutListener)
        } catch (e: Exception) {
            Logger.d(TAG, "recyclerViewLayoutListener already removed")
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {

        super.setUserVisibleHint(isVisibleToUser)

        if (!isStoryViewedCountIncremented && isVisibleToUser) {
            RateUsDialogHelper.incrementStoryViewedCount()
            checkForRateUsDisplayDialog()
        }

        if (isVisibleToUser && fragmentManager == null || view == null) {
            return
        }

        if (!isVisibleToUser && activity != null) {
            AndroidUtils.hideKeyBoard(activity)
        }

        if (isVisibleToUser && currentChunkStartTime == 0L) {
            // Start timer if not already started
            currentChunkStartTime = SystemClock.elapsedRealtime()
        } else if (!isVisibleToUser) {
            // Update the chunkwise time when fragment goes out of view
            updateLastKnownChunk()
        }

        if (isVisibleToUser) {
            firedTSAfterPause = false
            startAdsHelper()
            logStoryPageViewEvent()
            incrementFollowCounterForImplicitTrigger()
            if (visibilityCalculator?.isStarted == false) {
                visibilityCalculator?.start()
            }
        } else {
            adsHelperStarted = false
            isStoryViewedCountIncremented = false
        }

        if (isFullPageLoaded && isVisibleToUser) {
            updateTSEvent(detailList, 0)
        }

        visibilityCalculator?.notifyFragmentVisible(isVisibleToUser)
    }

    private var adsHelperStarted: Boolean = false

    private fun startAdsHelper() {
        if (adsHelperStarted || !userVisibleHint) return
        adsHelper?.let {
            registerForAds()
            it.start()
            adsHelperStarted = true
            prefetchAdRequestCallback?.prefetchAdRequests()
        }
    }

    private fun checkForRateUsDisplayDialog() {
        isStoryViewedCountIncremented = true
        RateUsCheckHelperNews.checkToShowRateUsOnOpeningOrSwipeStories(false)

    }

    //TODO(raunak) :to determine correct place for ads in default case.
    private fun findCardTypeIndex(card: CommonAsset?): MutableList<String> {
        val detailOrder: List<String>? = DetailOrderingServiceImpl.getDetailWidgetOrderingResponse()
            ?.get(card?.i_DetailPageWidgetOrderId())
        if (detailOrder != null && detailOrder.isNotEmpty()) {
            return detailOrder.toArrayList()
        }

        val format = card?.i_format() ?: bootStrapCard?.i_format()
        val subformat = card?.i_subFormat() ?: bootStrapCard?.i_subFormat()
        val type = card?.i_type() ?: bootStrapCard?.i_type()
        val sourceType = card?.i_source()?.type ?: bootStrapCard?.source?.type
        val widgets = mutableListOf<String>()

        when (format) {
            Format.HTML -> {
                if (type == AssetType2.COMMENT.name) {
                    widgets.add(DetailCardType.SEEPOST.name)
                    widgets.add(DetailCardType.MAIN_COMMENT.name)
                    widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                    widgets.add(DetailCardType.DISCUSSION.name)
                    widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                    widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                    widgets.add(DetailCardType.READMORE.name)
                } else if (subformat == SubFormat.RICH_PHOTOGALLERY) {
                    widgets.add(DetailCardType.RICH_GALLERY.name)
                    widgets.add(DetailCardType.TIME.name)
                    widgets.add(DetailCardType.TITLE.name)
                    widgets.add(DetailCardType.DISCLAIMER.name)
                    widgets.add(DetailCardType.SOURCE.name)
                    widgets.add(DetailCardType.LIKES_LIST.name)
                    widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                    widgets.add(DetailCardType.DISCUSSION_NS.name)
                    widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                    widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.READMORE.name)
                } else if (sourceType.equals("UGC", true)) {
                    widgets.add(DetailCardType.IMAGE.name)
                    widgets.add(DetailCardType.IMAGE_DYNAMIC.name)
                    widgets.add(DetailCardType.RICH_GALLERY.name)
                    widgets.add(DetailCardType.TIME.name)
                    widgets.add(DetailCardType.LOCATION.name)
                    widgets.add(DetailCardType.TITLE.name)
                    widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                    widgets.add(DetailCardType.CHUNK1.name)
                    widgets.add(DetailCardType.STORYPAGE.name)
                    widgets.add(DetailCardType.CHUNK2.name)
                    widgets.add(DetailCardType.OGCARD.name)
                    widgets.add(DetailCardType.REPOST.name)
                    widgets.add(DetailCardType.DISCLAIMER.name)
                    widgets.add(DetailCardType.SOURCE.name)
                    widgets.add(DetailCardType.OTHER_PERSPECTIVES.name)
                    widgets.add(DetailCardType.LIKES_LIST.name)
                    widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                    widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                    widgets.add(DetailCardType.DISCUSSION_NS.name)
                    widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                    widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                    widgets.add(DetailCardType.HASHTAGS.name)
                    widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                    widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                    widgets.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                    widgets.add(DetailCardType.SUPPLEMENT.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.READMORE.name)

                } else {
                    widgets.add(DetailCardType.IMAGE.name)
                    widgets.add(DetailCardType.IMAGE_DYNAMIC.name)
                    widgets.add(DetailCardType.RICH_GALLERY.name)
                    widgets.add(DetailCardType.SOURCE_TIME.name)
                    widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                    widgets.add(DetailCardType.TITLE.name)
                    widgets.add(DetailCardType.OGCARD.name)
                    widgets.add(DetailCardType.REPOST.name)
                    widgets.add(DetailCardType.CHUNK1.name)
                    widgets.add(DetailCardType.STORYPAGE.name)
                    widgets.add(DetailCardType.CHUNK2.name)
                    widgets.add(DetailCardType.DISCLAIMER.name)
                    widgets.add(DetailCardType.SOURCE.name)
                    widgets.add(DetailCardType.OTHER_PERSPECTIVES.name)
                    widgets.add(DetailCardType.LIKES_LIST.name)
                    widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                    widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                    widgets.add(DetailCardType.DISCUSSION_NS.name)
                    widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                    widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                    widgets.add(DetailCardType.HASHTAGS.name)
                    widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                    widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                    widgets.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                    widgets.add(DetailCardType.SUPPLEMENT.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.READMORE.name)
                }
            }
            Format.EMBEDDED_VIDEO -> {
                widgets.add(DetailCardType.IMAGE.name)
                widgets.add(DetailCardType.IMAGE_DYNAMIC.name)
                widgets.add(DetailCardType.RICH_GALLERY.name)
                widgets.add(DetailCardType.VIRAL.name)
                widgets.add(DetailCardType.SOURCE_TIME.name)
                widgets.add(DetailCardType.TITLE.name)
                widgets.add(DetailCardType.LOCATION.name)
                widgets.add(DetailCardType.CHUNK1.name)
                widgets.add(DetailCardType.STORYPAGE.name)
                widgets.add(DetailCardType.CHUNK2.name)
                widgets.add(DetailCardType.DISCLAIMER.name)
                widgets.add(DetailCardType.SOURCE.name)
                widgets.add(DetailCardType.LIKES_LIST.name)
                widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                widgets.add(DetailCardType.DISCUSSION_NS.name)
                widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                widgets.add(DetailCardType.HASHTAGS.name)
                widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                widgets.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                widgets.add(DetailCardType.SUPPLEMENT.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.READMORE.name)
            }
            Format.IMAGE -> {
                widgets.add(DetailCardType.VIRAL.name)
                widgets.add(DetailCardType.TIME.name)
                widgets.add(DetailCardType.TITLE.name)
                widgets.add(DetailCardType.CHUNK1.name)
                widgets.add(DetailCardType.STORYPAGE.name)
                widgets.add(DetailCardType.CHUNK2.name)
                widgets.add(DetailCardType.DISCLAIMER.name)
                widgets.add(DetailCardType.SOURCE.name)
                widgets.add(DetailCardType.LIKES_LIST.name)
                widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                widgets.add(DetailCardType.DISCUSSION_NS.name)
                widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                widgets.add(DetailCardType.HASHTAGS.name)
                widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                widgets.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                widgets.add(DetailCardType.SUPPLEMENT.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.READMORE.name)
            }
            Format.POLL -> {
                widgets.add(DetailCardType.SOURCE_TIME.name)
                widgets.add(DetailCardType.POLL.name)
                widgets.add(DetailCardType.LOCATION.name)
                widgets.add(DetailCardType.RICH_GALLERY.name)
                widgets.add(DetailCardType.DISCLAIMER.name)
                widgets.add(DetailCardType.SOURCE.name)
                widgets.add(DetailCardType.LIKES_LIST.name)
                widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                widgets.add(DetailCardType.DISCUSSION_NS.name)
                widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                widgets.add(DetailCardType.HASHTAGS.name)
                widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                widgets.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                widgets.add(DetailCardType.SUPPLEMENT.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.READMORE.name)
            }
            Format.VIDEO -> {
                if (isInBottomSheet) {
                    widgets.add(DetailCardType.LIKES_LIST.name)
                    widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                    widgets.add(DetailCardType.DISCUSSION_NS.name)
                    widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                    widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                    widgets.add(DetailCardType.READMORE.name)
                } else {
                    widgets.add(DetailCardType.SOURCE.name)
                    widgets.add(DetailCardType.TITLE.name)
                    widgets.add(DetailCardType.IMAGE.name)
                    widgets.add(DetailCardType.IMAGE_DYNAMIC.name)
                    widgets.add(DetailCardType.TIME.name)
                    widgets.add(DetailCardType.LOCATION.name)
                    widgets.add(DetailCardType.VIRAL.name)
                    widgets.add(DetailCardType.CHUNK1.name)
                    widgets.add(DetailCardType.CHUNK2.name)
                    widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                    widgets.add(DetailCardType.DISCUSSION_NS.name)
                    widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                    widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                    widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                    widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                    widgets.add(DetailCardType.LIKES_LIST.name)
                    widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                    widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                    widgets.add(DetailCardType.DISCLAIMER.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.SPACER.name)
                    widgets.add(DetailCardType.READMORE.name)
                }
            }
            else -> {
                widgets.add(DetailCardType.IMAGE.name)
                widgets.add(DetailCardType.IMAGE_DYNAMIC.name)
                widgets.add(DetailCardType.VIRAL.name)
                widgets.add(DetailCardType.RICH_GALLERY.name)
                widgets.add(DetailCardType.SOURCE_TIME.name)
                widgets.add(DetailCardType.TITLE.name)
                widgets.add(DetailCardType.OGCARD.name)
                widgets.add(DetailCardType.REPOST.name)
                widgets.add(DetailCardType.LOCATION.name)
                widgets.add(DetailCardType.CHUNK1.name)
                widgets.add(DetailCardType.STORYPAGE.name)
                widgets.add(DetailCardType.CHUNK2.name)
                widgets.add(DetailCardType.DISCLAIMER.name)
                widgets.add(DetailCardType.SOURCE.name)
                widgets.add(DetailCardType.LIKES_LIST.name)
                widgets.add(DetailCardType.SUPPLEMENTARY_RELATED.name)
                widgets.add(DetailCardType.DISCUSSION_HEADER.name)
                widgets.add(DetailCardType.DISCUSSION_NS.name)
                widgets.add(DetailCardType.DISCUSSION_LOADER.name)
                widgets.add(DetailCardType.DISCUSSION_SHOW_ALL.name)
                widgets.add(DetailCardType.HASHTAGS.name)
                widgets.add(DetailCardType.SUGGESTED_FOLLOW.name)
                widgets.add(DetailCardType.SECOND_CHUNK_LOADING.name)
                widgets.add(DetailCardType.SEE_IN_VIDEO.name)
                widgets.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                widgets.add(DetailCardType.SUPPLEMENT.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.SPACER.name)
                widgets.add(DetailCardType.READMORE.name)
            }
        }

        return widgets
    }

    override fun getActivityContext(): Activity? {
        return activity
    }

    override fun isFragmentVisible(): Boolean {
        return super.getUserVisibleHint()
    }

    override fun getAdsMap(): MutableMap<String, BaseAdEntity>? {
        return detailsAdapter?.adsMap
    }

    override fun onInteractiveAdCollapsed() {
        isInteractiveAdOnTop = false
    }

    override fun onInteractiveAdExpanded() {
        isInteractiveAdOnTop = true
    }

    override fun replaceAdEntityInViewHolder(adView: UpdateableAdView) {
        var success = false
        val oldAd = adView.adEntity

        //fetch backup ad from cache and update the failed adView
        if (oldAd != null && activity != null) {
            val newAd = adsHelper?.requestBackupAd(oldAd.adPosition!!)
            newAd?.let {
                detailsAdapter?.replaceAd(oldAd, newAd)
                adsHelper?.onAdReplaced(oldAd, newAd)
                success = true
            }
        }
        AdLogger.d(TAG, "Backup Ad insert success : $success for adView $adView")
        //In case of failure, viewholder should update its UI.
        (adView as BackUpAdConsumer).onBackupAdFetched(success)
    }

    override fun insertAd(baseAdEntity: BaseAdEntity, adPositionWithTag: String) {
        if (getAdsMap()?.containsKey(adPositionWithTag) == true) {
            AdLogger.d(TAG, "Ad already inserted: $adPositionWithTag. Return")
            return
        }
        shiftStorypageAdForThinUser(baseAdEntity)
        vm.insertAdCardIfNeeded(baseAdEntity)
        AdFrequencyStats.onAdInsertedInView(baseAdEntity, uniqueScreenId)
        baseAdEntity.parentIds.add(uniqueScreenId)

        // Visibility needs to be updated to fire ad impression for ads in view.
        if (baseAdEntity.adPosition != AdPosition.SUPPLEMENT) {
            detailList.viewTreeObserver.addOnGlobalLayoutListener(recyclerViewLayoutListener)
        }
        detailsAdapter?.updateAd(adPositionWithTag, baseAdEntity)
        if (baseAdEntity.displayPosition == null) {
            baseAdEntity.displayPosition = getDisplayPositionForAd(baseAdEntity.adPosition)
        }
    }

    private fun shiftStorypageAdForThinUser(ad: BaseAdEntity) {
        adsHelper?.shiftAdForThinUser(currentOrder, ad)?.let {
            currentOrder = it
            detailsAdapter?.updateCard(card, currentOrder)
            AdLogger.v(TAG, "After Shift $currentOrder")
        }
    }

    private fun getDisplayPositionForAd(zone: AdPosition?): Position? {
        return zone?.let {
            if (it == AdPosition.STORY) {
                val adCard = adsHelper?.toCardEnum(it.value)?.name ?: return null
                return detailsAdapter?.getDisplayPositionFor(adCard)
            }
            null
        }
    }

    override fun removeSeenAd(adSlot: String) {
        detailsAdapter?.removeAd(adSlot)
    }

    override fun getWebView(key: String): WeakReference<MASTAdView>? {
        return webViewCache[key]
    }

    override fun putWebView(key: String, value: WeakReference<MASTAdView>?) {
        //Lazy Initializing
        webViewCache[key] = value
    }

    private fun logStoryPageViewEvent() {
        card ?: return

        if (!loggedPageViewEvent && activity != null && userVisibleHint) {
            contentAdDelegate?.onCardView(activity)
            vm.contentRead(card, referrerFlow)
            logStoryPageViewEventSync()
            card?.let {
                vm.markNlfcConsumed(it.i_id())
            }
        }
        if (currentChunkStartTime == 0L && userVisibleHint) {
            currentChunkStartTime = SystemClock.elapsedRealtime()
        }

        if (userVisibleHint) {
            PreviousPostIdHelper.previousPostId = postId
            notificationUniqueId?.let { id ->
                CommonUtils.runInBackground {
                    NotificationDB.instance().getNotificationDao().markNotificationAsRead(id)
                    NotificationDB.instance().getNotificationPrefetchInfoDao()
                        .deleteEntryForNotificationWithId(id)
                }
            }
        }
    }

    private fun logStoryPageViewEventSync() {
        val map = HashMap<NhAnalyticsEventParam, Any?>()

        val tabEntity = parentEntity
        if (tabEntity != null) {
            map[NhAnalyticsNewsEventParam.TABNAME] = tabEntity.name
            map[NhAnalyticsNewsEventParam.TABTYPE] = tabEntity.entityType
            map[NhAnalyticsNewsEventParam.TABITEM_ID] = tabEntity.id
        }

        if (isInCollection && parentStoryId != null) {
            map[AnalyticsParam.COLLECTION_ID] = parentStoryId!!
            map[AnalyticsParam.COLLECTION_TYPE] = Constants.MM_CAROUSEL
            map[AnalyticsParam.CARD_POSITION] = cardPosition
            map[AnalyticsParam.COLLECTION_ITEM_COUNT] = collectionCount.toString()
            map[AnalyticsParam.COLLECTION_NAME] = Constants.MORE_STORIES_COLLECTION_NAME
        }

        if (isInCarousel && parentStoryId != null) {
            map[AnalyticsParam.COLLECTION_ID] = parentStoryId!!
        }

        val item = card ?: return
        var referrer = PageReferrer(currentPageReferrer)
        if (!isLandingStory) {
            referrer = PageReferrer(NewsReferrer.STORY_DETAIL, PreviousPostIdHelper.previousPostId)
            referrer.referrerAction = NhAnalyticsUserAction.SWIPE
        } else {
            referrer.referrerAction = NhAnalyticsUserAction.CLICK
        }
        if (section == PageSection.SEARCH.section) {
            SearchAnalyticsHelper.addSearchParams(AnalyticsHelper2.getSection(section), map)
            referrerFlow?.id = parentEntity?.id
        }

        AnalyticsHelper2.logStoryPageTimeSpentViewEvent(
            item, referrerFlow, referrerLead, referrer,
            referrer_raw, map, AnalyticsHelper2.getSection(section), timeSpentEventId
        )

        loggedPageViewEvent = true

        if (item.i_articleTrack() != null) {
            CommonUtils.runInBackground {
                AsyncTrackHandler.getInstance().track(item.i_articleTrack(), true)
            }
        }
    }

    fun logVideoDetailCommentsBottomSheetTimeSpentEvent() {
        if (isInBottomSheet) {
            if (timeSpentEventId != 0L) {
                if (firedTSAfterPause) {
                    NewsDetailTimespentHelper.getInstance()
                        .postDeleteTimespentEvent(timeSpentEventId)
                } else {
                    // If fragment viewed update time and post timespent event
                    updateLastKnownChunk()
                    if (!chunkwiseTimespent.isEmpty()) {
                        NewsDetailTimespentHelper.getInstance().postSendTimespentEvent(
                            timeSpentEventId,
                            chunkwiseTimespent, false,
                            if (backPressed)
                                NhAnalyticsUserAction.BACK
                            else
                                NhAnalyticsUserAction.SWIPE
                        )
                    }
                }
            }
        }
    }


    fun resetVideoDetailCommentsBottomSheetTimeSpentTimer() {
        chunkwiseTimespent = HashMap()
        firedTSAfterPause = !userVisibleHint
        isTimespentPaused = true
        if (currentChunkStartTime == 0L) {
            currentChunkStartTime = SystemClock.elapsedRealtime()
        }

        if (loggedPageViewEvent) {
            NewsDetailTimespentHelper.getInstance().postCreateTimespentEvent(
                timeSpentEventId,
                AnalyticsHelper2.getTimespentParams()
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                Constants.REPORTED_ADS_RESULT_CODE -> {
                    val reportedAdEntity = (intent?.getSerializableExtra(
                        Constants
                            .REPORTED_ADS_ENTITY
                    ) as? BaseDisplayAdEntity?)
                    val reportedAdParentUniqueAdIdIfCarousal =
                        intent?.getStringExtra(Constants.PARENT_UNIQUE_ADID_REPORTED_ADS_ENTITY)
                    onAdReported(
                        reportedAdEntity = reportedAdEntity,
                        reportedParentAdIdIfCarousel = reportedAdParentUniqueAdIdIfCarousal
                    )
                }
            }
    }

    override fun onAdReported(
        reportedAdEntity: BaseAdEntity?,
        reportedParentAdIdIfCarousel: String?
    ) {
        if (adsHelper != null && reportedAdEntity != null) {
            adsHelper?.handleRemovalReportedAdsPost(
                reportedAdEntity as BaseDisplayAdEntity,
                reportedParentAdIdIfCarousel
            )
        }
    }

    override fun deeplinkUrl(): String? {
        return card?.i_deeplinkUrl()
    }
}


class PostWebViewClient(
    private val fragment: BaseSupportFragment,
    private val postListener: PostActions,
    private val vm: DetailsViewModel,
    private val firstChunk: Boolean,
    private val secondChunk: Boolean,
    private val detailList: RecyclerView,
    private val isInBottomSheet: Boolean
) : NhWebViewClient() {

    override fun onPageLoaded(view: WebView?, url: String?) {
        view?.clearHistory()

        if (secondChunk || postListener.isFirstChunkOnlyPost()) {
            postListener.onFullPageLoaded()
        }

        if (firstChunk) {
            postListener.onFirstChunkLoaded()
        }

        if (isInBottomSheet) {
            vm.discussionLoading.postValue(true)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, urlParam: String): Boolean {
        var url = urlParam
        if (url.contains(NewsConstants.TEST_BASEURl)) {
            url = url.replace(NewsConstants.TEST_BASEURl, Constants.EMPTY_STRING)
        }

        if (CommonUtils.isEmpty(url) || !fragment.isAdded) {
            return false
        }

        val shouldOveride =
            NHWebViewUtils.shouldOverrideUrlLoading(view, url, false, vm.pageReferrer)
        if (!shouldOveride) {
            NHBrowserUtil.openWithNHBrowser(fragment.activity, url, true)
        }

        return true
    }
}

interface UpdateableDetailView {
    fun onBindView(
        position: Int, cvm: CardsViewModel?, dvm: DetailsViewModel?,
        card: CommonAsset?, parentCard: CommonAsset?, suggedtedFollowsPojo:
        SuggestedFollowsPojo?, likedListPojo: LikeListPojo?, myInteraction: Interaction?,
        discussionPojo: DiscussionPojo?, relatedStories: CardsPojo?,
        associatedStories: CardsPojo?, photoChildPojo: PhotoChildPojo?,
        isInBottomSheet: Boolean, replyCountList: List<ReplyCount>?
    )
}


class DetailsBindUtils {
    companion object {

        @JvmStatic
        fun getDisplayTimeTextAsStoryCard(item: CommonAsset?): String? {

            val displayTime = item?.i_publishTime()

            if (displayTime != null)
                return DateFormatter.getPublishTimeDateAsString(displayTime).toString()
            else
                return null
        }

        @JvmStatic
        fun getDisplayTimeTextAsStoryWithDotCard(item: CommonAsset?): String? {

            val displayTime = item?.i_publishTime()

            if (displayTime != null)
                return " · " + DateFormatter.getPublishTimeDateAsString(displayTime).toString()
            else
                return null
        }

        @JvmStatic
        fun isDiscussion(item: CommonAsset?): Boolean {
            return item?.i_level() == PostEntityLevel.DISCUSSION
        }

        @JvmStatic
        fun getAllDiscussionBackground(filter: CreatePostUiMode?): Drawable {
            if (filter != CreatePostUiMode.COMMENT && filter != CreatePostUiMode.REPOST) {
                return CommonUtils.getDrawable(R.drawable.rounded_corner_red_shape)
            }

            return CommonUtils.getDrawable(R.drawable.rounded_corner_gray_bg)
        }

        @JvmStatic
        fun isTitleAvailableForViewAll(item: CommonAsset?): Boolean {
            val text = item?.i_title() ?: item?.i_content()
            return !DataUtil.isEmpty(text)
        }

        @JvmStatic
        fun getCommentTitle(card: CommonAsset): String {
            try {
                var count = Integer.parseInt(card.i_counts()?.COMMENTS?.value ?: "0")
                if (count > 0)
                    return CommonUtils.getString(R.string.comments_fragment_name) + " " + count
            } catch (ex: Exception) {
                return return CommonUtils.getString(R.string.comments_fragment_name) + " " + card.i_counts()?.COMMENTS?.value
            }

            return CommonUtils.getString(R.string.comments_fragment_name)
        }

        @JvmStatic
        fun getRepostTitle(card: CommonAsset): String {
            try {
                var count = Integer.parseInt(card.i_counts()?.REPOST?.value ?: "0")
                if (count > 0)
                    return CommonUtils.getString(R.string.repost) + " " + count
            } catch (ex: Exception) {
                return return CommonUtils.getString(R.string.repost) + " " + card.i_counts()?.REPOST?.value
            }

            return CommonUtils.getString(R.string.repost)
        }

        @JvmStatic
        fun getAllDiscussionTitle(card: CommonAsset): String {
            try {
                var count = Integer.parseInt(card.i_counts()?.COMMENTS?.value ?: "0") +
                        Integer.parseInt(card.i_counts()?.REPOST?.value ?: "0")

                if (count > 0)
                    return CommonUtils.getString(R.string.all) + " " + count
            } catch (ex: Exception) {
                return CommonUtils.getString(R.string.all)
            }

            return CommonUtils.getString(R.string.all)
        }

        @JvmStatic
        fun getCommentsTitleCount(card: CommonAsset?): String {
            val c = card?.i_counts()?.COMMENTS?.value ?: "0"
            return if (c == "0") "" else c
        }

        @JvmStatic
        fun commentCountVisiblilty(card: CommonAsset?, discussionPojo: DiscussionPojo?,
                                   isInBottomSheet: Boolean = false, dvm: DetailsViewModel): Int {
            return if (!hasDiscussions(card,discussionPojo,isInBottomSheet,dvm) && getCommentsTitleCount(card) == "") View.GONE else View.VISIBLE
        }

        @JvmStatic
        fun getCommentDiscussionBackground(filter: CreatePostUiMode?): Drawable {
            if (CreatePostUiMode.COMMENT == filter) {
                return CommonUtils.getDrawable(R.drawable.rounded_corner_red_shape)
            }

            return CommonUtils.getDrawable(R.drawable.rounded_corner_gray_bg)
        }

        @JvmStatic
        fun getRepostDiscussionBackground(filter: CreatePostUiMode?): Drawable {
            if (CreatePostUiMode.REPOST == filter) {
                return CommonUtils.getDrawable(R.drawable.rounded_corner_red_shape)
            }

            return CommonUtils.getDrawable(R.drawable.rounded_corner_gray_bg)
        }

        @JvmStatic
        fun getCommentDiscussionTextColor(filter: CreatePostUiMode?): Int {
            if (CreatePostUiMode.COMMENT == filter) {
                return Color.WHITE
            }

            return Color.BLACK
        }

        @JvmStatic
        fun getRepostDiscussionTextColor(filter: CreatePostUiMode?): Int {
            if (CreatePostUiMode.REPOST == filter) {
                return Color.WHITE
            }

            return Color.BLACK
        }

        @JvmStatic
        fun getAllDiscussionTextColor(filter: CreatePostUiMode?): Int {
            if (filter != CreatePostUiMode.COMMENT && filter != CreatePostUiMode.REPOST) {
                return Color.WHITE
            }

            return Color.BLACK
        }

        @JvmStatic
        fun getMyDisplayName(): String {
            return SSO.getInstance().userDetails.userName ?: ""
        }

        @JvmStatic
        fun getMyHandle(): String {
            return SSO.getInstance().userDetails.userLoginResponse?.handle ?: ""
        }

        @JvmStatic
        fun getMyProfileImage(): String? {
            return SSO.getInstance().userDetails.userLoginResponse?.profileImage
        }

        @JvmStatic
        fun getPrivacyfromPostAsset(card: CommonAsset?): String? {
            val privacy = card?.i_postPrivacy()
            if (privacy != null)
                return privacy.name
            else
                return Constants.EMPTY_STRING
        }

        @JvmStatic
        fun canShowTimeText(item: CommonAsset?): Boolean {
            return item?.i_publishTime() != null
        }

        @JvmStatic
        fun canShowImageLayout(item: CommonAsset?, bootStrapCard: DetailListCard?): Boolean {
            return !CommonUtils.isEmpty(item?.i_contentImageInfo()?.url) ||
                    !CommonUtils.isEmpty(bootStrapCard?.imageDetails?.url)
        }

        @JvmStatic
        fun canShowTransparentActionbar(item: CommonAsset?, bootStrapCard: DetailListCard?): Boolean {
            return !CommonUtils.isEmpty(item?.i_contentImageInfo()?.url) ||
                    !CommonUtils.isEmpty(bootStrapCard?.imageDetails?.url)&& bootStrapCard?.format?.equals(Format.POLL) == false && bootStrapCard?.format?.equals(Format.IMAGE) == false &&  bootStrapCard?.subformat?.equals(SubFormat.S_W_VIDEO) == false
        }

        @JvmStatic
        fun getImageExtraCount(item: CommonAsset?): String? {

            item?.i_thumbnailUrls()?.size?.let {
                if (it > 5) {
                    return (it - 5).toString()
                }
            }
            return null
        }

        @JvmStatic
        fun canShowChunk1Layout(item: CommonAsset?): Boolean {

            return !CommonUtils.isEmpty(item?.i_content()) && !(item?.i_richTextChunk1() ?: false)
        }

        @JvmStatic
        fun getDiscussionsTitle(
            card: CommonAsset?, discussionPojo: DiscussionPojo?,
            isInBottomSheet: Boolean = false, dvm: DetailsViewModel
        ): String {
            return CommonUtils.getString(R.string.discussion)
        }

        @JvmStatic
        fun getCommentsTitle(
            card: CommonAsset?, discussionPojo: DiscussionPojo?,
            isInBottomSheet: Boolean = false, dvm: DetailsViewModel
        ): String {
            return CommonUtils.getString(R.string.comments_fragment_name)
        }

        @JvmStatic
        fun hasDiscussions(
            card: CommonAsset?, discussionPojo: DiscussionPojo?,
            isInBottomSheet: Boolean = false, dvm: DetailsViewModel
        ): Boolean {

            if (dvm.discussionLoading.value == true || dvm.failedNetworkCalls.contains(dvm.SHORT_LIKES_UC)) {
                return true
            }
            return (!discussionPojo?.data.isNullOrEmpty())
        }

        @JvmStatic
        fun hasDiscussionsAllowcomment(
                card: CommonAsset?, discussionPojo: DiscussionPojo?,
                isInBottomSheet: Boolean = false, dvm: DetailsViewModel
        ): Boolean {
            if (!CardsBindUtils.canAllowComment(card)) return false
            if (dvm.discussionLoading.value == true || dvm.failedNetworkCalls.contains(dvm.SHORT_LIKES_UC)) {
                return true
            }
            return (!discussionPojo?.data.isNullOrEmpty())
        }

        @JvmStatic
        fun showStartCommenting(
                card: CommonAsset?, discussionPojo: DiscussionPojo?,
                isInBottomSheet: Boolean = false, dvm: DetailsViewModel
        ): Boolean {
            return (discussionPojo?.data.isNullOrEmpty())&& CardsBindUtils.canAllowComment(card)
        }

        @JvmStatic
        fun hasRepost(card: CommonAsset?): Boolean {

            return card?.i_counts()?.REPOST?.value?.toIntOrNull() != 0 && card?.i_counts()?.REPOST?.value?.toIntOrNull() != null
        }

        @JvmStatic
        fun discussionRelevant(
            card: CommonAsset?, discussionPojo: DiscussionPojo?,
            isInBottomSheet: Boolean = false, dvm: DetailsViewModel
        ): Boolean {
            if (dvm.hadDiscussionItems) {
                return true
            }

            dvm.hadDiscussionItems =
                dvm.hadDiscussionItems || (!discussionPojo?.data.isNullOrEmpty())
            return dvm.hadDiscussionItems
        }

        @JvmStatic
        fun isComment(item: CommonAsset?): Boolean {
            return item?.i_type() == AssetType2.COMMENT.name
        }

        @JvmStatic
        fun isComment(cpe: CreatePostEntity?): Boolean {
            return SocialFeaturesConstants.WIDGET_TYPE_COMMENT == cpe?.uiMode?.name
        }

        @JvmStatic
        fun isMyDiscussion(item: CommonAsset?): Boolean {
            return item?.i_source()?.handle != null && SSO.getLoginResponse() != null &&
                    (item.i_source()?.handle == SSO.getLoginResponse()?.handle ||
                            item.i_source()?.handle?.substring(1) == SSO.getLoginResponse()?.handle)
        }

        @JvmStatic
        fun hasLikes(item: CommonAsset?): Boolean {
            val likesCount: String = item?.i_counts()?.TOTAL_LIKE?.value ?: ""
            if (TextUtils.isEmpty(likesCount)) {
                return false
            }

            return !"0".equals(likesCount)
        }

        @JvmStatic
        fun getLikesCountText(item: CommonAsset?): String {
            return CommonUtils.getString(
                R.string.comment_likes_count,
                item?.i_counts()?.TOTAL_LIKE?.value ?: ""
            )
        }

        @JvmStatic
        fun getLikesCountTextOnly(item: CommonAsset?): String {
            val value = item?.i_counts()?.TOTAL_LIKE?.value
            if (value.isNullOrEmpty()) return Constants.EMPTY_STRING
            if (value.trim() == "0") return ""
            return value
        }


        @JvmStatic
        fun hasReplies(discussion: CommonAsset? = null, replyCount: Int): Boolean {
            val commentCount = discussion?.i_counts()?.COMMENTS?.value ?: "0"
            val commentCountVal = Integer.parseInt(commentCount) + replyCount
            return commentCountVal > 0
        }

        @JvmStatic
        fun showFlag(item: CommonAsset?): Boolean {
            return isOthersComment(item) && (item?.i_isReported() == false)
        }

        @JvmStatic
        fun isOthersComment(item: CommonAsset?): Boolean {
            return item?.i_type() == AssetType2.COMMENT.name && SSO.getLoginResponse() != null &&
                    (item.i_source()?.handle != SSO.getLoginResponse()?.handle &&
                            item.i_source()?.handle?.substring(1) != SSO.getLoginResponse()?.handle)
        }

        @JvmStatic
        fun replyToComment(
            view: View,
            comment: CommonAsset?,
            reply: CommonAsset?,
            parent: CommonAsset?,
            dvm: DetailsViewModel
        ) {
            try {
                val intent = CommonNavigator.getAddReplyIntent(
                    parent?.i_id(), comment?.i_id(),
                    SearchSuggestionItem(
                        itemId = reply?.i_source()?.id ?: "",
                        suggestion = reply?.i_source()?.handle ?: "",
                        name = reply?.i_source()?.displayName ?: reply?.i_source()?.handle,
                        typeName = SearchSuggestionType.HANDLE.type
                    ), CreatePostUiMode.REPLY, dvm.getParentReferrer(parent),
                    comment?.i_source()?.id, comment?.i_source()?.type
                )
                if (view.context is Activity) {
                    (view.context as Activity).startActivityForResult(intent, 0)
                } else if (view.context is ContextThemeWrapper) {
                    ((view.context as ContextThemeWrapper).baseContext as Activity).startActivityForResult(
                        intent,
                        0
                    )
                }
            } catch (ex: Exception) {
                Logger.e(TAG, "Error launching reply intent")
            }
        }

        @JvmStatic
        fun replyComment(
            view: View,
            comment: CommonAsset?,
            parent: CommonAsset?,
            dvm: DetailsViewModel
        ) {
            if (parent?.i_type() == AssetType2.COMMENT.name) {
                val parentPostId = parent.i_parentPostId()
                try {
                    val intent = CommonNavigator.getAddReplyIntent(
                        parentPostId, parent.i_id(),
                        SearchSuggestionItem(
                            itemId = comment?.i_source()?.id ?: "",
                            suggestion = comment?.i_source()?.handle ?: "",
                            name = comment?.i_source()?.displayName ?: comment?.i_source()?.handle,
                            typeName = SearchSuggestionType.HANDLE.type
                        ), CreatePostUiMode.REPLY, dvm.getParentReferrer(parent),
                        parent.i_source()?.id, parent.i_source()?.type
                    )
                    if (view.context is Activity) {
                        (view.context as Activity).startActivity(intent)
                    } else if (view.context is ContextThemeWrapper) {
                        ((view.context as ContextThemeWrapper).baseContext as Activity).startActivity(
                            intent
                        )
                    }
                } catch (ex: Exception) {
                    Logger.e(TAG, "Error launching reply intent")
                }

                return
            }

            try {
                val intent = CommonNavigator.getAddReplyIntent(
                    parent?.i_id(), comment?.i_id(),
                    SearchSuggestionItem(
                        itemId = comment?.i_source()?.id ?: "",
                        suggestion = comment?.i_source()?.handle ?: "",
                        name = comment?.i_source()?.displayName ?: comment?.i_source()?.handle,
                        typeName = SearchSuggestionType.HANDLE.type
                    ), CreatePostUiMode.REPLY, dvm.getParentReferrer(parent),
                    comment?.i_source()?.id, comment?.i_source()?.type
                )
                if (view.context is Activity) {
                    (view.context as Activity).startActivityForResult(intent, 0)
                } else if (view.context is ContextThemeWrapper) {
                    ((view.context as ContextThemeWrapper).baseContext as Activity).startActivityForResult(
                        intent,
                        0
                    )
                }
            } catch (ex: Exception) {
                Logger.e(TAG, "Error launching reply intent")
            }
        }

        @JvmStatic
        fun replyComment(
            view: View,
            comment: CommonAsset?,
            parent: CommonAsset?,
            dvm: ViewAllCommentsViewModel
        ) {
            try {
                val intent = CommonNavigator.getAddReplyIntent(
                    parent?.i_id(), comment?.i_id(),
                    SearchSuggestionItem(
                        itemId = comment?.i_source()?.id ?: "",
                        suggestion = comment?.i_source()?.handle ?: "",
                        name = comment?.i_source()?.displayName ?: comment?.i_source()?.handle,
                        typeName = SearchSuggestionType.HANDLE.type
                    ), CreatePostUiMode.REPLY, dvm.getParentReferrer(parent),
                    comment?.i_source()?.id, comment?.i_source()?.type
                )
                if (view.context is Activity) {
                    (view.context as Activity).startActivityForResult(intent, 0)
                } else if (view.context is ContextThemeWrapper) {
                    ((view.context as ContextThemeWrapper).baseContext as Activity).startActivityForResult(
                        intent,
                        0
                    )
                }
            } catch (ex: Exception) {
                Logger.e(TAG, "Error launching reply intent")
            }
        }

        @JvmStatic
        fun hasCards(item: CardsPojo?): Boolean {
            return item?.data?.isEmpty() == false
        }

        @JvmStatic
        fun canShowHashTag(item: CommonAsset): Boolean {

            return !CommonUtils.isEmpty(item.i_hashtags())
        }

        @JvmStatic
        fun canShowOtherperspective(item: CommonAsset): Boolean {
            return !CommonUtils.isEmpty(item.i_moreStories())
        }

        @JvmStatic
        fun canShowRichTextChunk1Marker(item: CommonAsset?): Boolean {
            return item?.i_richTextChunk1() ?: false && Logger.loggerEnabled()
        }

        @JvmStatic
        fun canShowRichTextChunk2Marker(item: CommonAsset?): Boolean {
            return item?.i_richTextChunk2() ?: false && Logger.loggerEnabled()
        }
        @JvmStatic
        fun canShowRichTextChunk1(item: CommonAsset?): Boolean {
            return item?.i_richTextChunk1()?: false
        }

        @JvmStatic
        fun canShowRichTextChunk2(item: CommonAsset?): Boolean {
            return item?.i_richTextChunk2() ?: false
        }

        @JvmStatic
        fun canShowbyline(item: CommonAsset?): Boolean {
            return !CommonUtils.isEmpty(item?.i_byline())
        }

        @JvmStatic
        fun canShowOPLabel(
            item: CommonAsset?,
            bootStrapCard: DetailListCard?,
            isInCollection: Boolean
        ): Boolean {
            if (item == null) {
                return false
            }
            // show other prospective below to title if image is missing. if image is available on top of it
            if (!CommonUtils.isEmpty(item.i_contentImageInfo()?.url) || !CommonUtils.isEmpty(
                    bootStrapCard?.imageDetails?.url
                )
            ) {
                return false
            }

            return (!CommonUtils.isEmpty(item.i_moreStories()) && item.i_contentImageInfo()?.url ==
                    null) && !isInCollection
        }

        @JvmStatic
        fun canShowOPLabelOnImage(
            item: CommonAsset?, bootStrapCard: DetailListCard?,
            isInCollection: Boolean
        ): Boolean {
            return ((!CommonUtils.isEmpty(item?.i_moreStories()) && item?.i_contentImageInfo()?.url != null) ||
                    ((bootStrapCard?.moreStoryCount
                        ?: 0) > 0) && bootStrapCard?.imageDetails?.url != null) &&
                    !isInCollection
        }

        @JvmStatic
        fun canShowImageBg(item: CommonAsset?, bootStrapCard: DetailListCard?): Boolean {
            return (item?.i_contentImageInfo()?.url != null) ||
                    (bootStrapCard?.imageDetails?.url != null)
        }

        @JvmStatic
        fun canShowImageTitle(
            item: CommonAsset?,
            bootStrapCard: DetailListCard?,
            titleLength: Int
        ): Boolean {
            val sourceType = item?.i_source()?.type ?: bootStrapCard?.source?.type
            return !sourceType.equals(
                "UGC",
                true
            ) && (!CommonUtils.isEmpty(item?.i_title()) || !(CommonUtils.isEmpty(bootStrapCard?.title))) && (titleLength <= TITLE_TOP_LINE_COUNT_THRESHOLD)
        }

        @JvmStatic
        fun getImageDimension(cardTypeIndex: Int, imgIndex: Int): Pair<Int, Int>? {
            return when (cardTypeIndex) {
                DetailCardType.IMAGE.index -> {
                    getSimplePostCardImageDimension(imgIndex)
                }

                DetailCardType.IMAGE_DYNAMIC.index -> {
                    getSimplePostCardImageDimension(imgIndex)
                }

                DetailCardType.GALLERY_2.index -> {
                    getGallery2CardImageDimension(imgIndex)
                }

                DetailCardType.GALLERY_3.index -> {
                    getGallery3CardImageDimension(imgIndex)
                }

                DetailCardType.GALLERY_4.index -> {
                    getGallery4CardImageDimension(imgIndex)
                }

                DetailCardType.GALLERY_5.index -> {
                    getGallery5CardImageDimension(imgIndex)
                }
                else -> {
                    null
                }
            }
        }


        /*return image dimension width to height*/
        private fun getSimplePostCardImageDimension(imageIndex: Int): Pair<Int, Int> {

            val imageWidth = CommonUtils.getDeviceScreenWidth()
            val aspectRatio = ImageUrlReplacer.getContentImageAspectRatio()
            val imageHeight: Int
            if (java.lang.Float.compare(aspectRatio, 1.0f) == 0) {
                imageHeight = CommonUtils.getDimensionInDp(R.dimen.news_detail_image_height)
            } else {
                imageHeight = Math.round(imageWidth / ImageUrlReplacer.getContentImageAspectRatio())
            }

            return imageHeight to imageWidth
        }


        private fun getGallery5CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            if (imageIndex == 2) {
                return CommonUtils.getDimension(R.dimen.img_rec_gallery_5_i2_width) to
                        CommonUtils.getDimension(R.dimen.img_rec_gallery_5_i2_height)
            }
            return CommonUtils.getDimension(R.dimen.img_sq_gallery_5_i0_width) to
                    CommonUtils.getDimension(R.dimen.img_sq_gallery_5_i0_height)
        }

        private fun getGallery2CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            return CommonUtils.getDimension(R.dimen.img_rec_gallery_2_i0_width) to
                    CommonUtils.getDimension(R.dimen.img_rec_gallery_2_i0_height)
        }

        private fun getGallery4CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            return CommonUtils.getDimension(R.dimen.post_img_rec_gallery_4_width) to
                    CommonUtils.getDimension(R.dimen.img_rec_gallery_4_height)
        }

        private fun getGallery3CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            if (imageIndex == 0) {
                return CommonUtils.getDimension(R.dimen.img_rec_gallery_3_i0_height) to
                        CommonUtils.getDimension(R.dimen.img_rec_gallery_3_i0_height)
            }
            return CommonUtils.getDimension(R.dimen.img_rec_gallery_3_i1_height) to
                    CommonUtils.getDimension(R.dimen.img_rec_gallery_3_i1_height)
        }

        @JvmStatic
        fun seeAllRepostText(card: CommonAsset?): String {
            val value = card?.i_counts()?.REPOST?.value
            val repostString = CommonUtils.getString(R.string.repost)
            return if (value.isNullOrBlank() || value == "0") {
                repostString
            } else {
                kotlin.runCatching {
                    CommonUtils.getQuantifiedString(
                        R.plurals.q_view_all_reposts_with_count,
                        value.toInt(),
                        value
                    )
                }
                    .getOrDefault(repostString)
            }


        }
    }
}

@BindingAdapter("bind:imageUrl", "bind:iwidth", "bind:iheight", requireAll = true)
fun loadImageUrl(view: ImageView, url: String?, width: Int, height: Int) {
    url ?: return
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(
        url, width,
        height
    )
    Image.load(qualifiedUrl)
            .placeHolder(R.color.empty_image_color)
            .into(view)
}

@BindingAdapter("bind:suggestedFollow")
fun bindSuggestedFollow(view: RecyclerView, suggestedFollow: SuggestedFollowsPojo?) {
    if (view.adapter == null) {
        view.adapter = SuggestedFollowListAdapter()
        view.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
        val decoration = RecyclerViewHorizontalItemDecoration(
            0, CommonUtils.getDimension(
                R.dimen
                    .similar_sources_right_item_padding
            ), CommonUtils.getDimension(R.dimen.story_card_padding_left)
        )
        view.addItemDecoration(decoration)
    }

    (view.adapter as SuggestedFollowListAdapter).setItems(suggestedFollow?.data)
}

@BindingAdapter("bind:likedList", "bind:dvm", "bind:vc", "bind:item", requireAll = true)
fun bindLikedList(
    view: RecyclerView, likedListPojo: LikeListPojo?, dvm: DetailsViewModel?,
    vc: VisibilityCalculator?, item: CommonAsset
) {
    if (view.adapter == null) {
        view.adapter = LikedListAdapter(dvm, likedListPojo)
        val llm = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
        view.layoutManager = llm
        vc?.init(view, llm)
        vc?.start()
    }

    val extraCount = (likedListPojo?.total ?: 0) - (likedListPojo?.data?.size ?: 0)
    (view.adapter as LikedListAdapter).setExtraCount(if (extraCount > 0) extraCount else 0)
    (view.adapter as LikedListAdapter).setParentItem(item)
    (view.adapter as LikedListAdapter).setItems(likedListPojo?.data)
}

@BindingAdapter("bind:typeIcon", "bind:typeIconVisibility", requireAll = true)
fun bindTypeIcon(view: NHImageView, discussion: CommonAsset?, dvm: DetailsViewModel?) {
    if (SocialFeaturesConstants.WIDGET_TYPE_COMMENT == discussion?.i_type()) {
        view.setImageResource(R.drawable.ic_all_comments)
    } else {
        view.setImageResource(R.drawable.ic_repost)
    }

    if (dvm?.isAllFilter() == true) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:typeIcon", "bind:typeIconVisibility", requireAll = true)
fun bindTypeIcon(view: NHImageView, discussion: CommonAsset?, vavm: ViewAllCommentsViewModel?) {
    if (SocialFeaturesConstants.WIDGET_TYPE_COMMENT == discussion?.i_type()) {
        view.setImageResource(R.drawable.ic_all_comments)
    } else {
        view.setImageResource(R.drawable.ic_repost)
    }

    if (vavm?.discussionMode?.get() == CreatePostUiMode.ALL && !vavm.isDummyPost) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:likedCount")
fun bindLikedCount(view: NHTextView, likedListPojo: LikeListPojo?) {
    view.text = CommonUtils.getString(R.string.likes_count, likedListPojo?.total ?: "")
}

@BindingAdapter("bind:hashtags")
fun bindHashTags(view: RecyclerView, item: CommonAsset?) {
    if (view.adapter == null) {
        view.adapter = HashtagAdapter(item)
        val layoutManager = FlexboxLayoutManager(view.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        view.layoutManager = layoutManager
    }
    (view.adapter as HashtagAdapter).setItems(item?.i_hashtags())
}

@BindingAdapter("bind:relatedstories", "bind:cvm", "bind:lifecycle", requireAll = true)
fun bindRelatedStories(
    view: RecyclerView, cardsPojo: CardsPojo?, cvm: CardsViewModel?,
    lifeCycleOwner: LifecycleOwner?
) {
    Logger.d(TAG, "Binding Related Stories")
    if (cardsPojo?.data != null && cvm != null) {
        var pageReferrer: PageReferrer? = null
        if (cvm.pageReferrer != null) {
            pageReferrer = PageReferrer(cvm.pageReferrer)
        } else {
            pageReferrer = PageReferrer()
        }

        pageReferrer.referrer = NewsReferrer.WIDGET_PFP
        pageReferrer.id = Constants.WIDGET_TYPE_DETAIL
        val adapter = CardsAdapter(
            view.context, cvm, lifeCycleOwner, true, null, null, pageReferrer =
            pageReferrer, uniqueRequestId = cvm.uniqueScreenId
        )
        adapter.submitList(getPagedListFromList(cardsPojo.data))
        view.adapter = adapter
        view.layoutManager = LinearLayoutManager(view.context)
        view.adapter!!.notifyDataSetChanged()
    }
}

@BindingAdapter("bind:otherPerspective", "bind:vm", requireAll = true)
fun bindOtherPerspective(view: RecyclerView, item: CommonAsset?, vm: CardsViewModel?) {
    if (view.adapter == null) {
        view.adapter = PerspectiveCarouselCardsAdapter(vm)
        view.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
    }

    (view.adapter as PerspectiveCarouselCardsAdapter).setItems(item?.i_moreStories())
    (view.adapter as PerspectiveCarouselCardsAdapter).setParent(item)
}

@BindingAdapter("bind:attachPhoto")
fun attachPhoto(view: NHImageView, item: PhotoChild?) {
    item?.let {
        val url = NewsListCardLayoutUtil.getNewsContentImageUrl(it.imgUrl, false)
        // TODO : Use slow image view to handle cache image
        val slowUrl = NewsListCardLayoutUtil.getNewsContentImageUrl(it.slowImageUrl, true)
        Image.load(url).placeHolder(R.color.empty_image_color).into(view)
    }
}

@BindingAdapter("bind:moreCount", "bind:dvm", requireAll = true)
fun bindMoreCount(view: NHTextView, card: CommonAsset?, dvm: DetailsViewModel) {
    if ((card?.i_childCount() ?: 0) > TOTAL_DISPLAY_COUNT) {
        view.text = String.format(
            MORE_TEXT,
            (card?.i_childCount() ?: 0) - TOTAL_DISPLAY_COUNT
        )
    } else {
        view.visibility = View.GONE
        return
    }

    view.setOnClickListener {
        if (card != null) {
            dvm.onPostClick(it, card, 4)
        }
    }
}

@BindingAdapter("bind:attachRichPhoto", "bind:dvm", requireAll = true)
fun attachRichPhoto(view: SlowNetworkImageView, card: CommonAsset?, dvm: DetailsViewModel) {
    val url = NewsListCardLayoutUtil.getNewsContentImageUrl(card?.i_thumbnailUrls()?.get(0), false)
    val slowUrl =
        NewsListCardLayoutUtil.getNewsContentImageUrl(card?.i_thumbnailUrls()?.get(0), true)

    val callback = object : SlowNetworkImageView.Callback {
        override fun onPhotoDownloadSuccess(slowNetworkImageView: SlowNetworkImageView?) {
        }

        override fun onPhotoDownloadFailure(slowNetworkImageView: SlowNetworkImageView?) {
        }

        override fun onPhotoSaveSuccess(slowNetworkImageView: SlowNetworkImageView?) {
        }

        override fun onPhotoSaveFailure(slowNetworkImageView: SlowNetworkImageView?) {
        }

        override fun onPhotoTouch(
            slowNetworkImageView: SlowNetworkImageView?,
            regularPhotoRequested: Boolean
        ) {
            if (card != null) {
                dvm.onPostClick(slowNetworkImageView!!, card, 0)
            }
        }
    }

    view.startLoading(
        slowUrl, url,
        callback, FIT_TYPE.TOP_CROP, FIT_TYPE.TOP_CROP, Priority
            .PRIORITY_NORMAL, Priority.PRIORITY_HIGHEST, true, true
    )
}

@BindingAdapter("bind:attachThumb", "bind:thumbIndex", "bind:dvm", requireAll = true)
fun attachThumb(view: SlowNetworkImageView, card: CommonAsset?, index: Int, dvm: DetailsViewModel) {
    if ((card?.i_thumbnailUrls()?.size ?: 0) > index) {
        val thumbImageDimension = NewsListCardLayoutUtil.getGalleryTile3ThumbnailImageDimensions()
        val url = ImageUrlReplacer.getQualifiedUrl(
            card?.i_thumbnailUrls()?.get(index) ?: "",
            thumbImageDimension
        )
        val callback = object : SlowNetworkImageView.Callback {
            override fun onPhotoDownloadSuccess(slowNetworkImageView: SlowNetworkImageView?) {
            }

            override fun onPhotoDownloadFailure(slowNetworkImageView: SlowNetworkImageView?) {
            }

            override fun onPhotoSaveSuccess(slowNetworkImageView: SlowNetworkImageView?) {
            }

            override fun onPhotoSaveFailure(slowNetworkImageView: SlowNetworkImageView?) {
            }

            override fun onPhotoTouch(
                slowNetworkImageView: SlowNetworkImageView?,
                regularPhotoRequested: Boolean
            ) {
                if (card != null) {
                    dvm.onPostClick(slowNetworkImageView!!, card, index)
                }
            }
        }

        view.startLoading(
            url, url, callback, FIT_TYPE.TOP_CROP, FIT_TYPE.TOP_CROP, Priority
                .PRIORITY_NORMAL, Priority.PRIORITY_HIGHEST, true, true
        )
    }
}

class DetailAdapterDiffUtilCallback(
    private val mOldItemList: List<Any>,
    private val mNewItemList: List<Any>,
    private val oldDiscussionPojo: DiscussionPojo?,
    private val discussionPojo: DiscussionPojo?,
    private val oldCard: CommonAsset?,
    private val card: CommonAsset?,
    private val isTitleGreater: Int,
    private val isTitleGreaterOld: Int
) : DiffUtil.Callback() {
    private val LOG_TAG: String = "DetailAdapterDiffUtilCa"
    override fun getOldListSize(): Int {
        return mOldItemList.size
    }

    override fun getNewListSize(): Int {
        return mNewItemList.size
    }

    private fun isDiscussion(widget: Any): Boolean {
        return DetailCardType.DISCUSSION.name.equals(widget) ||
                DetailCardType.DISCUSSION_NS.name.equals(widget)
    }

    private fun isImage(widget: Any): Boolean {
        return DetailCardType.IMAGE.name.equals(widget) ||
                DetailCardType.IMAGE_DYNAMIC.name.equals(widget)
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val postItemOld = mOldItemList[oldItemPosition]
        val postItemNew = mNewItemList[newItemPosition]

        if (isDiscussion(postItemOld) && isDiscussion(postItemNew)) {
            val firstOldIndex = mOldItemList.indexOf(postItemOld)
            val firstNewIndex = mNewItemList.indexOf(postItemNew)
            val oldIndex = oldItemPosition - firstOldIndex
            val newIndex = newItemPosition - firstNewIndex
            if (oldIndex < 0 || newIndex < 0 || oldIndex >= (oldDiscussionPojo?.data?.size ?: 0)
                || newIndex >= (discussionPojo?.data?.size ?: 0)
            ) {
                return false
            }

            val oldDiscussion = oldDiscussionPojo?.data?.get(oldIndex)
            val newDiscussion = discussionPojo?.data?.get(newIndex)
            return (oldDiscussion?.i_id() ?: "").equals((newDiscussion?.i_id() ?: ""))
        }
        if (postItemOld == postItemNew
            && (postItemOld == DetailCardType.POLL.name || postItemOld == DetailCardType.POLL_RESULT.name)
            && oldCard?.i_poll() != card?.i_poll()
        ) {
            Logger.d(LOG_TAG, "areItemsTheSame: poll: oldCard != card")
            return false
        }

        if (DetailCardType.DISCUSSION_HEADER.name.equals(postItemOld) &&
            DetailCardType.DISCUSSION_HEADER.name.equals(postItemNew)
        ) {
            return oldDiscussionPojo?.data?.size == discussionPojo?.data?.size &&
                    oldCard?.i_counts()?.COMMENTS?.value == card?.i_counts()?.COMMENTS?.value &&
                    oldCard?.i_counts()?.REPOST?.value == card?.i_counts()?.REPOST?.value
        }
//        isImage(postItemOld) && isImage(postItemNew) &&
        if (isTitleGreater != isTitleGreaterOld) {
            return false
        }

        return postItemOld.equals(postItemNew)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val postItemOld = mOldItemList[oldItemPosition]
        val postItemNew = mNewItemList[newItemPosition]

        if (DetailCardType.SOURCE.name.equals(postItemOld) && DetailCardType.SOURCE
                .name.equals(postItemNew)
        ) {

            val oldFollowing = oldCard?.i_isFollowin() ?: false
            val newFollowing = card?.i_isFollowin() ?: false

            return oldFollowing.equals(newFollowing)
        }

        if (DetailCardType.DISCUSSION_HEADER.name.equals(postItemOld) &&
            DetailCardType.DISCUSSION_HEADER.name.equals(postItemNew)
        ) {
            return oldDiscussionPojo?.data?.size == discussionPojo?.data?.size &&
                    oldCard?.i_counts()?.COMMENTS?.value == card?.i_counts()?.COMMENTS?.value &&
                    oldCard?.i_counts()?.REPOST?.value == card?.i_counts()?.REPOST?.value
        }

        if (isDiscussion(postItemOld) && isDiscussion(postItemNew)) {
            val firstOldIndex = mOldItemList.indexOf(postItemOld)
            val firstNewIndex = mNewItemList.indexOf(postItemNew)
            val oldIndex = oldItemPosition - firstOldIndex
            val newIndex = newItemPosition - firstNewIndex
            val oldDiscussion = oldDiscussionPojo?.data?.get(oldIndex)
            val newDiscussion = discussionPojo?.data?.get(newIndex)
            if (oldIndex < 0 || newIndex < 0 || oldIndex >= (oldDiscussionPojo?.data?.size ?: 0)
                || newIndex >= (discussionPojo?.data?.size ?: 0)
            ) {
                return false
            }

            return (oldDiscussion?.i_id() ?: "").equals((newDiscussion?.i_id() ?: "")) &&
                    oldDiscussion?.selectedLikeType == newDiscussion?.selectedLikeType && oldDiscussion?.isReported == newDiscussion?.isReported
        }
        if (postItemOld == postItemNew
            && (postItemOld == DetailCardType.POLL.name || postItemOld == DetailCardType.POLL_RESULT.name)
            && oldCard?.i_poll() != card?.i_poll()
        ) {
            Logger.d(LOG_TAG, "areContentsTheSame: ${oldCard?.i_poll()} != ${card?.i_poll()}")
            return false
        }


        if (isImage(postItemOld) && isImage(postItemNew) && isTitleGreater != isTitleGreaterOld) {
            return false
        }
        return postItemOld.equals(postItemNew)
    }

}

interface PostActions {
    fun onScrollToOtherPerspective()

    fun onFullPageLoaded()

    fun onFirstChunkLoaded() {
        // Default Implementation. Do nothing
    }

    fun onRetryClicked(speedEvent: ConnectionSpeedEvent?, baseError: BaseError?)

    fun shortContentLoadMore()

    fun isFirstChunkOnlyPost(): Boolean {
        return false
    }
}



