/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.view

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil.inflate
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bwutil.BwEstRepo
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.BaseMediaItem
import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener
import com.dailyhunt.tv.players.analytics.CommonAdsAnalyticsHelper
import com.dailyhunt.tv.players.analytics.CommonVideoAnalyticsHelper
import com.dailyhunt.tv.players.analytics.VideoAnalyticsHelper
import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.customviews.CompanionAdView
import com.dailyhunt.tv.players.customviews.DHPlaybackControlView
import com.dailyhunt.tv.players.customviews.ExoPlayerWrapper2
import com.dailyhunt.tv.players.customviews.VideoPlayerWrapper
import com.dailyhunt.tv.players.customviews.WebPlayerWrapper
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.interfaces.PlayerExoCallbacks
import com.dailyhunt.tv.players.interfaces.PlayerViewDH
import com.dailyhunt.tv.players.managers.PlayerBuilder
import com.dailyhunt.tv.players.managers.PlayerFragmentManager
import com.dailyhunt.tv.players.model.entities.CallState
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.dhtvapp.common.utils.DHAdsViewHolderFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent.FLEX_START
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.newshunt.adengine.instream.IAdCacheManager
import com.newshunt.adengine.instream.IAdCachePlayerCallbacks
import com.newshunt.adengine.instream.IAdLogger
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.AdViewedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.AdsUtil.Companion.getCardTypeForAds
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdConsumer
import com.newshunt.adengine.view.helper.FetchAdsSpec
import com.newshunt.adengine.view.helper.PostAdsHelper
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.app.analytics.MarkStoryCardClickUsecase.Companion.VIDEO_BUFFER_TIME_TAG
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.common.ui.fragment.NewsDetailFragment2
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.NotificationCtaUiHelper
import com.newshunt.appview.common.ui.helper.NotificationUiType
import com.newshunt.appview.common.ui.viewholder.AbstractAutoplayViewHolder
import com.newshunt.appview.common.video.base.BaseVerticalVideoFragment
import com.newshunt.appview.common.video.helpers.ExoRequestHelper
import com.newshunt.appview.common.video.listeners.DHPlaybackControlListener
import com.newshunt.appview.common.video.listeners.DHTouchListener
import com.newshunt.appview.common.video.localzone.LocalZoneFragment
import com.newshunt.appview.common.video.relatedvideo.RelatedVideoFragment
import com.newshunt.appview.common.video.ui.adapter.DHHashTagAdapter
import com.newshunt.appview.common.video.ui.helper.NetworkRetry
import com.newshunt.appview.common.video.ui.helper.PlayerState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.video.ui.helper.VideoWaitCoroutine
import com.newshunt.appview.common.video.utils.DHGestureTap
import com.newshunt.appview.common.video.utils.DHVideoImageUtil
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.video.utils.DownloadUtils
import com.newshunt.appview.common.video.utils.VideoDetailBindUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.FragmentDhVideoDetailBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ServedButNotPlayedHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.model.entity.NonLinearConfigurations
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.server.asset.ContentScale
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.helper.BwEstCfgDataProvider
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.di.DaggerVideoDetailsComponent2
import com.newshunt.news.di.DetailsModule2
import com.newshunt.news.helper.VideoPlayBackTimer
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.NonLinearConsumedUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.news.view.fragment.PostDetailsFragment
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.socialfeatures.model.internal.service.VideoDownloadBeaconImpl
import com.squareup.otto.Subscribe
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created on 08/28/2019.
 */
private const val MASTHEAD_EXPIRY_TIME = 6

class DHVideoDetailFragment : BaseFragment(), PlayerExoCallbacks, IAdCachePlayerCallbacks,
        DHPlaybackControlView.ControlStateListener, DHPlaybackControlListener, AdConsumer,
        DHTouchListener, ErrorMessageBuilder.ErrorMessageClickedListener, VideoTimeListener,
        OnAdReportedListener, DeeplinkableDetail, LocationListener {

    private val TAG = "DHVideoDetailFragment"
    private val TAG_CACHE = "DHVideoDetailFragment::Cache"
    var postId: String? = null
    var position: Int = 0
    var verticalPosition: Int = -1
    private var isVisibleToUser = false
    private var isBackPressed = false
    private var playerView: PlayerViewDH? = null
    private var contentScale: ContentScale? = null
    var card: CommonAsset? = null
    private var playerAsset: PlayerAsset? = null
    private var videoWrapper: VideoPlayerWrapper? = null
    private var videoProvider: VideoPlayerProvider? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    lateinit var section: String
    lateinit var location: String
    private var timeSpentEventId: Long = 0
    private var isBottomSheetLoaded = false
    private var isVideoBack: Boolean = false
    private var isVideoInFullScreen = false
    private var isVideoStarted: Boolean = false
    private var isInCarousel: Boolean = false
    private var pageReferrer = PageReferrer(NewsReferrer.VIDEO_DETAIL)
    private var isBusRegistered = false
    private var isAutoPlayCard = false
    private var isHideControl = false
    private var isResetToMute = false

    private var adWaitJob: VideoWaitCoroutine? = null
    private var pendingTasksJob: Job? = null
    private var isAdPlaying: Boolean = false
    private var requestForFreshAds: Boolean = false
    private var loggedPageViewEvent: Boolean = false
    private var parentStoryId: String? = null
    private var cardPosition = -1
    private val disposables = CompositeDisposable()
    private var downloadRequestId = 0L
    private var isAdViewed = false
    private lateinit var companionView: CompanionAdView

    private var videoAnalyticsHelper = CommonVideoAnalyticsHelper()
    private var adsAnalyticsHelper = CommonAdsAnalyticsHelper()
    private var videoLoadTime = 0L
    private var videoStartTime = 0L
    var adsTimeSpentOnLPHelper : AdsTimeSpentOnLPHelper? = null

    @Inject
    lateinit var detailsViewModelF: DetailsViewModel.Factory
    private lateinit var vm: DetailsViewModel

    @Inject
    lateinit var cardsViewModelF: CardsViewModel.Factory
    private lateinit var cvm: CardsViewModel

    private lateinit var videoDetailBinding: FragmentDhVideoDetailBinding

    @Inject
    lateinit var adsHelperF: PostAdsHelper.Factory
    private var adsHelperStarted: Boolean = false
    private var adsHelper: PostAdsHelper? = null
    private var contentAdDelegate: ContentAdDelegate? = null

    @Inject
    lateinit var fetchAdsSpec: FetchAdsSpec.Factory

    private var isMastheadAdReady = false
    private var isMastheadAdInserted = false
    private var mastHeadDisposable: Disposable? = null
    private var mastHeadAdEntity: BaseAdEntity? = null
    private var mastHeadAdView: RecyclerView.ViewHolder? = null
    private var isAutoplayVideoContinuation = false
    private var parentEntity: PageEntity? = null
    private var parentLocation: String? = null
    private var groupInfo: GroupInfo? = null
    private var postEntityLevel: String? = null

    private var currentPageReferrer: PageReferrer? = null
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var referrerRaw: String? = null
    private var searchQuery: SearchQuery? = null
    private lateinit var gestureDetector: GestureDetector
    private var playControlVisibility = View.GONE
    private var adsSchedulerDisposable: Disposable? = null
    private var controlsVisibilityDisposable: Disposable? = null
    private var eventSection: NhAnalyticsEventSection = NhAnalyticsEventSection.TV
    private var isCardLoaded = false
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private var nonLinearRequestTime: Int = 15
    private var nonLinearDisplayTime: Int = 20
    private var nonLinearFeedUrl: String? = null
    private var nonLinearItemFetched = false
    private var isMenuShown = false
    private var entityId: String? = null
    private var notificationUniqueId: String? = null
    private var isReloadVideoByNetworkError = false

    // For call interruption
    private var currentCallState: Int = TelephonyManager.CALL_STATE_IDLE
    private val handler = Handler(Looper.getMainLooper())
    private var isFragmentPaused = false
    private var commentsPostDetailsFragment: PostDetailsFragment? = null
    private var isVerticalVideo = false
    private var isNLFCRequestDone = false
    private var isFromHistory = false

    @Inject
    lateinit var nonLinearConsumeUsecase: NonLinearConsumedUsecase
    var landingStoryId: String? = null
    private var notificationUiType: NotificationUiType? = null
    private var reportAdsMenuListener: ReportAdsMenuListener? = null
    private var postAdReportMenuListener: ReportAdsMenuListener? = null
    private var adId: String? = null
    private var isLive = false
    private var isLandingStory = false
    private var isAutoClick = false
    private var isLocalZone = false
    private val nlfHandler = Handler(Looper.getMainLooper())
    private var totalBufferTimer: VideoPlayBackTimer = VideoPlayBackTimer()
    private var totalBufferTime = 0L
    private var loadDelayinMs = 0L
    private var isEnablePrefetchLogs = PreferenceManager.getPreference(GenericAppStatePreference.ENABLE_PREFETCH_LOGS, false)
    private var isEnableBandwidthLogs = PreferenceManager.getPreference(GenericAppStatePreference.ENABLE_BANDWIDTH_LOGS, false)

    private var videoPrefetchCallback: VideoPrefetchCallback? = null
    private var isVideoPausedbyUser = false

    companion object {
        private const val THUMBNAIL_TIMER = 1

        fun getInstance(bundle: Bundle): DHVideoDetailFragment {
            var fragment = DHVideoDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        readBundle()
    }

    private fun readBundle() {
        val bundle = arguments
        bundle?.let {
            postId = bundle.getString(Constants.STORY_ID)
            position = bundle.getInt(Constants.STORY_POSITION, -1)
            verticalPosition = bundle.getInt(Constants.LIST_VERTICAL_POSITION, -1)
            parentStoryId = bundle.getString(Constants.PARENT_STORY_ID)
            cardPosition = bundle.getInt(NewsConstants.CARD_POSITION, -1)
            entityId = arguments?.getString(Constants.PAGE_ID)
                    ?: (postId!! + System.currentTimeMillis().toString())
            location = arguments?.getString(NewsConstants.BUNDLE_LOC_FROM_LIST)
                    ?: Constants.FETCH_LOCATION_DETAIL
            postEntityLevel = bundle.getString(NewsConstants.POST_ENTITY_LEVEL)
                    ?: PostEntityLevel.TOP_LEVEL.name
            pageReferrer.id = postId

            if (bundle.containsKey(Constants.STORY_POSITION)) {
                position = bundle.getInt(Constants.STORY_POSITION)
            }
            parentEntity = bundle.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity?
            notificationUniqueId = bundle.getString(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID)
            parentLocation = bundle.getString(Constants.LOCATION)
            searchQuery = bundle.getSerializable(Constants.BUNDLE_SEARCH_QUERY) as? SearchQuery
            isInCarousel = bundle.getBoolean(NewsConstants.IS_IN_CAROUSEL, false)
            if (!isInCarousel) {
                isInCarousel = bundle.getBoolean(NewsConstants.IS_IN_COLLECTION, false)
            }
            isFromHistory = bundle.getBoolean(Constants.BUNDLE_IS_FROM_HISTORY, false)
            groupInfo = bundle.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo?
            isResetToMute = bundle.getBoolean(Constants.RESET_MUTE_STATE, false)

            referrerLead = bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            if (referrerLead == null) {
                referrerLead = PageReferrer()
            }

            isLocalZone = bundle.getBoolean(Constants.BUNDLE_IS_LOCAL_ZONE, false)
            referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
            if (isLocalZone) {
                //setting referrer for local zone.
                referrerLead?.referrer = NewsReferrer.LOCAL_VIDEO_DETAIL
            }
            currentPageReferrer = PageReferrer(referrerLead)
            referrerFlow = PageReferrer(referrerLead)
            landingStoryId = bundle.getString(Constants.LANDING_STORY_ID)
            isLandingStory = bundle.getBoolean(Constants.IS_LANDING_STORY, false)
            adId = bundle.getString(Constants.BUNDLE_AD_ID)
            isLive = bundle.getBoolean(Constants.IS_LIVE, false)

            val notifCta = bundle.getString(Constants.BUNDLE_NOTIFICATION_CTA_UI_TYPE)
            notifCta?.let { notificationUiType = NotificationUiType.valueOf(it) }
        }

        section = if (isLocalZone) {
            PageSection.LOCAL.section
        } else {
            arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.TV.section
        }

        eventSection = AnalyticsHelper2.getSection(section)

        val nonLinearConfigurations = PreferenceManager.getPreference(GenericAppStatePreference.NON_LINEAR_CONFIGURATIONS, Constants.EMPTY_STRING);
        if (CommonUtils.isEmpty(nonLinearConfigurations)) {
            Logger.i(Constants.NON_LINEAR_FEED, "Non Linear Preferences are not present hence setting it to 0")
            nonLinearRequestTime = 0
            nonLinearDisplayTime = 0
        } else {
            val configurations = JsonUtils.fromJson(nonLinearConfigurations, NonLinearConfigurations::class.java)
            if (configurations != null) {
                nonLinearRequestTime = configurations.dhTVVideo.request
                nonLinearDisplayTime = configurations.dhTVVideo.display
            } else {
                Logger.i(Constants.NON_LINEAR_FEED, "Non Linear Preferences are not present hence returning");
            }
        }
        isNLFCRequestDone =
                (videoProvider as? AbstractAutoplayViewHolder?)?.nlfcRequestStatus ?: false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView : $position & postId : $postId")
        videoDetailBinding = getVideoBinding(inflater, container)
        videoDetailBinding.isHideAllViews = isAutoClick

        initBottomCommentSheetView()
        companionView = videoDetailBinding.companionView
        errorMessageBuilder = ErrorMessageBuilder(videoDetailBinding.errorParent, requireContext(),
                this, this)
        DaggerVideoDetailsComponent2.builder().detailsModule2(DetailsModule2(
                CommonUtils.getApplication(),
                SocialDB.instance(),
                entityId ?: postId!!,
                postId!!,
                timeSpentEventId, true,
                location,
                adId = adId,
                sourceId = arguments?.getString(NewsConstants.SOURCE_ID),
                sourceType = arguments?.getString(NewsConstants.SOURCE_TYPE),
                lifecycleOwner = this, section = section,
                adConsumer = this,
                parentEntity = parentEntity,
                parentLocation = parentLocation,
                fragmentManager = activity?.supportFragmentManager,
                searchQuery = searchQuery,
                level = postEntityLevel,
                listLocation = location + "_" + postId,
                referrerFlow = referrerFlow ?: PageReferrer(referrerLead), fragment = this, fragmentName = TAG)).build().inject(this)

        vm = ViewModelProviders.of(this, detailsViewModelF)[DetailsViewModel::class.java]
        if (currentPageReferrer != null) {
            vm.pageReferrer = currentPageReferrer!!
        }
        cvm = ViewModelProviders.of(this, cardsViewModelF)[CardsViewModel::class.java]
        cvm.setCurrentPageReferrer(pageReferrer, referrerFlow, null)
        videoDetailBinding.vm = cvm

        vm.executeDetailCardUsecase(postEntityLevel!!, adId)
        notificationUniqueId?.let { id ->
            CommonUtils.runInBackground {
                NotificationDB.instance().getNotificationDao().markNotificationAsRead(id)
                NotificationDB.instance().getNotificationPrefetchInfoDao().deleteEntryForNotificationWithId(id)
            }
        }

        videoDetailBinding.constraintLyt.setOnTouchListener { _, p1 ->
            gestureDetector.onTouchEvent(p1)
            true
        }
        videoDetailBinding.playButton.setOnClickListener {
            handlePlayButtonClick()
        }
        videoDetailBinding.bottomSheetLayout.setOnClickListener {
            toggleBottomSheet()
        }

        videoDetailBinding.sourceAndShareContainer.commentCountTv.setOnClickListener {
            toggleBottomSheet()
        }
        videoDetailBinding.sourceAndShareContainer.isLive = isLive
        reportAdsMenuListener = ReportAdsMenuFeedbackHelper(this, this)

        if (isLandingStory && savedInstanceState == null
            && arguments?.getBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, false) == true) {
            parentFragment?.let { pf ->
                val transitionParentFragment = (pf as? RelatedVideoFragment)?.getTransitionParentFragment()
                transitionParentFragment?.let { tpf ->
                    Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "DHVideoDetailFragment calling prepare transition on detail view for story: $postId")
                    videoDetailBinding.playerParent.transitionName = postId
                    tpf.prepareSharedElementTransition(videoDetailBinding.playerParent)
                    videoDetailBinding.root.doOnPreDraw {
                        Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "DHVideoDetailFragment onPreDraw, execute the animation for story: $postId")
                        parentFragment?.parentFragment?.startPostponedEnterTransition()
                    }
                }
            }
        }
        return videoDetailBinding.root
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Logger.d(TAG, "onActivityCreated : $position & postId : $postId")

        vm.readDetailCardUsecase.data().observe(this, Observer { dcResult: Result0<Boolean> ->
            kotlin.runCatching {
                dcResult.getOrNull()
            }.onFailure {
                val exception = (dcResult.getOrNull() as Result0<Boolean>).exceptionOrNull() as? BaseError
                exception ?: return@onFailure
                if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
                    if(!isPrefetchedContentAvailable()) {
                        showErrorScreen(Constants.ERROR_NO_INTERNET)
                    }
                } else {
                    showErrorScreen(Constants.ERROR_HTTP_NO_CONTENT)
                }
                vm.failedNetworkCalls.add(vm.READ_DETAIL_UC)
                return@Observer
            }

            vm.detailCardScan.observe(this, Observer { t ->
                if (t?.data != null) {
                    loadCard(t.data as CommonAsset)
                } else {
                    //This can be used as fallback logic to load the item, when we get null from DB
                    //observeFetchFullCard()
                }
            })
        })

        AndroidUtils.connectionSpeedLiveData.observe(this, Observer {
            Logger.d(TAG, "connectionSpeedLiveData pos $position")
            if (it.connectionSpeed == ConnectionSpeed.NO_CONNECTION
                && !CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
                if(!isPrefetchedContentAvailable()) {
                    playerView?.pause()
                    setVideoEndAction(PlayerVideoEndAction.PAUSE)
                    showErrorScreen(Constants.ERROR_NO_INTERNET)
                }
            } else {
                //Internet is back available
                if (isFragmentVisible()) {
                    //Play the video
                    onRetryClicked(null)
                } else if (videoDetailBinding.errorParent.isVisible && isReloadVideoByNetworkError) {
                    isReloadVideoByNetworkError = false
                    videoDetailBinding.errorParent.visibility = View.GONE
                }
            }
        })
        if (isFromHistory) {
            observeFetchFullCard()
        }

        if (activity is NewsBaseActivity) {
            (activity as NewsBaseActivity).setIsInDetailView(true)
        }
    }

    private fun showSwipeUpCoachMark() {

        Logger.d(TAG, "showing SWIPE_UP_COACH_MARK")
        videoDetailBinding.coachMarkBg.visibility = View.VISIBLE
        videoDetailBinding.coachMark.visibility = View.VISIBLE
        videoDetailBinding.coachmarkText.visibility = View.VISIBLE
        videoDetailBinding.coachMarkBg.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                videoDetailBinding.coachMarkBg.visibility = View.GONE
                videoDetailBinding.coachMark.visibility = View.GONE
                videoDetailBinding.coachmarkText.visibility = View.GONE
                return true
            }

        })
        Glide.with(videoDetailBinding.coachMark).load(R.drawable.su_image).into(videoDetailBinding.coachMark)
    }

    private fun createContentAdManager(card: CommonAsset?) {
        card?.i_adId() ?: adId ?: return
        if (contentAdDelegate == null) {
            contentAdDelegate = ContentAdDelegate(uniqueScreenId, entityId)
            contentAdDelegate?.bindAd(card?.i_adId() ?: adId, card?.i_id())
            videoDetailBinding.adDelegate = contentAdDelegate

            parentFragment?.let { postAdReportMenuListener = ReportAdsMenuFeedbackHelper(it, this) }
            videoDetailBinding.executePendingBindings()
        }
    }

    private fun handlePlayButtonClick() {
        isHideControl = false
        if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) && !isPrefetchedContentAvailable()) {
            showErrorScreen(Constants.ERROR_NO_INTERNET)
            return
        }
        if (card?.i_videoAsset()?.hideControl == false && videoDetailBinding.isHideControl != false) {
            videoDetailBinding.isHideControl = false
        }
        if (playerView != null) {
            showThumbnailWithLoader()
            playerView?.resume()
            showAllViews(View.VISIBLE, false)
        } else if (card != null) {
            loadVideo()
            showAllViews(View.VISIBLE, false)
        }
    }

    private fun loadCard(it: CommonAsset?) {
        Logger.d(TAG, "observe >> pos : $position & it = ${it?.i_id()}")
        if (it?.i_videoAsset() != null) {
            card = it
            val cachedDuration = ExoRequestHelper.getStreamCachedDuration(card?.i_id())
            card?.i_videoAsset()?.streamCachedDuration = cachedDuration

            if(TextUtils.isEmpty(card?.i_videoAsset()?.streamCachedUrl)) {
                val cachedUrl = ExoRequestHelper.getStreamCachedUrl(card?.i_id())
                card?.i_videoAsset()?.streamCachedUrl = cachedUrl
            }
            if(Logger.loggerEnabled()) {
                Log.d(TAG_CACHE, "streamCachedDuration = $cachedDuration")
                Log.d(TAG_CACHE,
                    "loadCard Vertical position : $verticalPosition, position : $position")
                Log.d(TAG_CACHE,
                    "loadCard contentId : " + card?.i_id() + ", videoId : " + card?.i_videoAsset()?.assetId)
                Log.d(TAG_CACHE,
                    "loadCard " + "streamCachedUrl = " + card?.i_videoAsset()?.streamCachedUrl + ", url = " + card?.i_videoAsset()?.url)
            }
            if (!isCardLoaded) {
                if(DHVideoUtils.isExoPlayer(card?.i_videoAsset())) {
                    loadDelayinMs = PreferenceManager.getPreference(
                            AppStatePreference.EXO_PLAYER_LOAD_DELAY, 100)
                } else {
                    loadDelayinMs = PreferenceManager.getPreference(
                            AppStatePreference.OTHER_PLAYER_LOAD_DELAY, 500)
                }
                isCardLoaded = true
                Logger.d(TAG, "observe Post updated!, pos : $position")
                videoDetailBinding.item = card
                playerAsset = DHVideoUtils.getPlayerAsset(it)
                fetchAdsSpec.fetch(card!!)
                nonLinearFeedUrl = card?.i_nonLinearPostUrl()
                Logger.d(TAG, "NLFC URL - " + nonLinearFeedUrl)
                cvm.updateNLFCAsset(card, position, landingStoryId)
                resetLayoutForVerticalVideo()
                loadContent()
                //  showSubTitle(View.VISIBLE)
            }
            if (adsHelper == null) {
                card?.let { post ->
                    adsHelper = adsHelperF.create(post, uniqueScreenId, pageReferrer)
                    startAdsHelper()
                }
            }
            createContentAdManager(card)
            videoDetailBinding.item = it
            videoDetailBinding.sourceAndShareContainer.invalidateAll()
            videoDetailBinding.adsMenuListener = postAdReportMenuListener
            videoDetailBinding.adDelegate = contentAdDelegate
            displayCacheLogs()
        } else {
            Logger.e(TAG, "card or videoAsset is NULL $card")
        }
    }

    private fun observeFetchFullCard() {
        vm.detailFullPost.observe(this, Observer { t ->
            Logger.d("HistoryCard", "observe >> pos : $position & t = $t")
            if (t?.data != null) {
                loadCard(t.data)
            } else {
                showErrorScreen(Constants.ERROR_HTTP_NO_CONTENT)
            }
        })
        vm.fetchFullPost(postId)
    }

    private fun resetLayoutForVerticalVideo() {
        val width = (playerAsset?.width ?: 0).toFloat()
        val height = (playerAsset?.height ?: 0).toFloat()
        Logger.d(TAG, "width = $width height = $height")
        if (width == 0f || height == 0f) {
            return
        }
        val aspectRatio = width / height
        Logger.d(TAG, "aspectRatio = $aspectRatio isVisibleToUser = $isVisibleToUser")
        if (aspectRatio > 1.5) {
            return
        }
        isVerticalVideo = true
        val controllerParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        controllerParams.bottomToTop = videoDetailBinding.bottomGuideline.id
        controllerParams.leftToLeft = videoDetailBinding.startGuideline.id
        controllerParams.rightToLeft = videoDetailBinding.endGuideline.id
        videoDetailBinding.videoControllerContainer.layoutParams = controllerParams
    }

    private fun getVideoBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDhVideoDetailBinding {
        val contextThemeWrapper = ContextThemeWrapper(activity, ThemeType.NIGHT.themeId)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        videoDetailBinding = inflate(localInflater,
                R.layout.fragment_dh_video_detail, container, false)
        return videoDetailBinding
    }

    private fun initBottomCommentSheetView() {
        bottomSheetBehavior = BottomSheetBehavior.from(videoDetailBinding.bottomSheetLayout)
        bottomSheetBehavior?.isGestureInsetBottomIgnored = true
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior
        .BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Check Logs to see how bottom sheets behaves
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Logger.d(TAG, "BottomSheetBehaviour - STATE_COLLAPSED")
                        if (parentFragment is BaseVerticalVideoFragment) {
                            (parentFragment as BaseVerticalVideoFragment).enableVerticalSwipe()
                        }
                        setViewsClickable(true)
                        videoDetailBinding.overlay.visibility = View.GONE
                        if (commentsPostDetailsFragment != null) {
                            commentsPostDetailsFragment?.logVideoDetailCommentsBottomSheetTimeSpentEvent()
                        }
                        if (isVisibleToUser) {
                            playerView?.resume()
                        }
                        commentsPostDetailsFragment?.isBottomSheetExpanded = false

                        if (parentFragment is BaseVerticalVideoFragment) {
                            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE
                                    .STATE_BOTTOM_BAR_HIDDEN, postId)
                        }
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Logger.d(TAG, "BottomSheetBehaviour - STATE_DRAGGING")
                        if (parentFragment is BaseVerticalVideoFragment) {
                            (parentFragment as BaseVerticalVideoFragment).disableVerticalSwipe()
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Logger.d(TAG, "BottomSheetBehaviour - STATE_EXPANDED")
                        if (parentFragment is BaseVerticalVideoFragment) {
                            (parentFragment as BaseVerticalVideoFragment).disableVerticalSwipe()
                        }
                        if (isAdPlaying)  {
                            playerView?.pause()
                            return
                        }
                        videoDetailBinding.overlay.visibility = View.VISIBLE
                        setViewsClickable(false)
                        playerView?.pause()
                        if (isBottomSheetLoaded && commentsPostDetailsFragment != null) {
                            commentsPostDetailsFragment?.resetVideoDetailCommentsBottomSheetTimeSpentTimer()
                        }
                        loadBottomSheet()
                        videoAnalyticsHelper.updateParam(
                                AnalyticsParam.INITIAL_LOAD_TIME.getName(), videoLoadTime.toString())
                        videoAnalyticsHelper.logVPEvent(PlayerVideoEndAction.BOTTOM_SHEET_EXPAND,
                                playerView?.currentDuration ?: 0, card, eventSection)
                        AnalyticsHelper2.logBottomSheetExpand(card, eventSection, pageReferrer, hashMapOf())
                        commentsPostDetailsFragment?.isBottomSheetExpanded = true

                        if (parentFragment is BaseVerticalVideoFragment) {
                            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE
                                    .STATE_BOTTOM_BAR_VISIBLE, postId)
                        }
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Logger.d(TAG, "BottomSheetBehaviour - STATE_HIDDEN")
                        videoDetailBinding.overlay.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
//                Log.e(TAG, "onSlide : " + slideOffset.absoluteValue)
            }
        })
    }

    private fun setViewsClickable(clickable: Boolean) {
        //When bottom sheet is open, below views should be click disabled
//        videoDetailBinding.header.isClickable = clickable
        videoDetailBinding.toolbar.backButton.isClickable = clickable
    }

    private fun loadBottomSheet() {
        if (isBottomSheetLoaded) {
            return
        }
        isBottomSheetLoaded = true
        commentsPostDetailsFragment = PostDetailsFragment()
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, location)
        bundle.putBoolean(Constants.IS_IN_BOTTOM_SHEET, true)
        bundle.putString(Constants.STORY_ID, postId)
        bundle.putString(Constants.PAGE_ID, entityId)
        bundle.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
        bundle.putString(Constants.REFERRER_RAW, referrerRaw)
        bundle.putInt(Constants.STORY_POSITION, position)
        bundle.putString(Constants.LOCATION, parentLocation)
        bundle.putString(NewsConstants.POST_ENTITY_LEVEL, postEntityLevel)
        bundle.putLong(NewsConstants.TIMESPENT_EVENT_ID, timeSpentEventId)
        bundle.putSerializable(Constants.BUNDLE_GROUP_INFO, groupInfo)
        bundle.putString(NewsConstants.DH_SECTION, section)
        commentsPostDetailsFragment?.arguments = bundle
        fragmentTransaction.replace(R.id.container_bs, commentsPostDetailsFragment!!)
        fragmentTransaction.commitAllowingStateLoss()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Removing previous PlayerFragment if any
        PlayerFragmentManager.removeChildFragments(this)
        videoDetailBinding.videoController.setControlStateListener(this)
        timeSpentEventId = SystemClock.elapsedRealtime()

        videoDetailBinding.toolbar.backButton.setOnClickListener {
            if (parentFragment is NewsDetailFragment2) {
                (parentFragment as? NewsDetailFragment2)?.handleActionBarBackPress(false)
            }
            if (parentFragment is RelatedVideoFragment) {
                (parentFragment as? RelatedVideoFragment)?.handleActionBarBackPress(false)
            }

            activity?.onBackPressed()
        }
        observeDownloadStart()
        gestureDetector = GestureDetector(context, DHGestureTap(this))
        setVideoStartAction(PlayerVideoStartAction.CLICK)

        val dhVDBottomSheetHeight = CommonUtils.getScreenHeight(activity) - (activity!!.resources
                .getDimensionPixelSize(R.dimen.dh_video_details_action_bar) + CommonUtils
                .getStatusBarHeight(context))
        videoDetailBinding.bottomSheetLayout.updateLayoutParams {
            height = dhVDBottomSheetHeight
        }

        if (!AppUserPreferenceUtils.shouldShowSwipeUpCoachMark() && !isLive && !isInCarousel) {
            showSwipeUpCoachMark()
            AppUserPreferenceUtils.doneShowingSwipeUpCoachMark()
            Logger.d(TAG, "Done showing SWIPE_UP_COACH_MARK")
        }
        if(isEnablePrefetchLogs) {
            videoDetailBinding.exoLogsParent.visibility = View.VISIBLE
        }
    }

    private fun displayCacheLogs() {
        if(!Logger.loggerEnabled() || !isEnablePrefetchLogs) {
            return
        }
        var connectionSpeed = BwEstRepo.currentConnectionQuality()

        val isEligibleToPrefetch = DHVideoUtils.isEligibleToPrefetchInDetail(card)
        val cachedUrl = if(isEligibleToPrefetch) ExoRequestHelper.getStreamCachedUrl(card?.i_id()) else null
        videoDetailBinding?.isPrefetch.text = "IsEligibleForPrefetch =  ${isEligibleToPrefetch}" +
                ", IsCached =  ${ExoRequestHelper.isItemPrefetched(card?.i_id())}, downloadError = ${card?.i_videoAsset()?.downloadErrorMsg}" +
            "\nId =  ${card?.i_id()}, selectedQuality :  ${ExoRequestHelper.getSelectedQuality(card?.i_id())}, Conn Info :  $connectionSpeed" +
                "\n\nDisableCacheConfig =  ${CacheConfigHelper.disableCache}" +
                ", IsPrefetchConfig =  ${card?.i_videoAsset()?.isPrefetch}" +
                ", IsAdded =  ${ExoRequestHelper.isItemAdded(card?.i_id())}"
        videoDetailBinding.prefetchDuration.text =
            "PrefetchDuration Config:  ${CacheConfigHelper.getPrefetchDurationConfig(card?.i_videoAsset())}" +
                    ",  CachedDuration :  ${card?.i_videoAsset()?.streamCachedDuration}"

        val configType = if (section == PageSection.TV.section) ConfigType.BUZZ_LIST else ConfigType.NEWS_LIST

        videoDetailBinding.prefetchCount.text =
            "VideoDetailV Prefetch\n" +
                    "Config count :  ${CacheConfigHelper.getNumberOfVideoToPrefetch(ConfigType.VIDEO_DETAIL_V, connectionSpeed)}" +
                    ", Added count :  ${ExoRequestHelper.getItemCount(ConfigType.VIDEO_DETAIL_V)}" +
                    ", Cached count :  ${ExoRequestHelper.getCompletedPrefetchCount(ConfigType.VIDEO_DETAIL_V)}" +
        "\n\nVideoDetailH Prefetch\n" +
                    "Config count:  ${CacheConfigHelper.getNumberOfVideoToPrefetch(configType, connectionSpeed)}" +
                    ", Added count :  ${ExoRequestHelper.getItemCount(configType)}" +
                    ", Cached :  ${ExoRequestHelper.getCompletedPrefetchCount(configType)}" +
        "\n\nVideoUrl =  ${card?.i_videoAsset()?.url}" +
                "\nCached Url =  $cachedUrl"

    }

    private fun displayBandwidthLogs() {
        if(!Logger.loggerEnabled() || !isEnableBandwidthLogs) {
            return
        }

        if (!videoDetailBinding.exoLogsParent.isShown) {
            videoDetailBinding.exoLogsParent.visibility = View.VISIBLE
        }
        val curCQParams = BwEstRepo.INST?.curCQParams()

        videoDetailBinding.isPrefetch.text = "\nBandwidth Estimation logs"

        val bandwidthStr = "\nN/w Sdk =   " + (curCQParams?.fbBitrate) + " Kbps" +
                "\nExo Bitrate =   " + (curCQParams?.exoBitrate) + " Kbps" +
                "\nFormula =   ${curCQParams?.exoBitrate} * ${BwEstCfgDataProvider.exoWeightage} " +
                "+ ${(curCQParams?.fbBitrate)} * ${BwEstCfgDataProvider.networkWeightage}" +
                "\nCalculated Bitrate =   " + (curCQParams?.resultBitrate) + " Kbps" +
                "\nConnectionSpeed =   " + curCQParams?.resultBitrateQuality;
        videoDetailBinding.prefetchDuration.text = bandwidthStr
        videoDetailBinding.prefetchCount.visibility = View.GONE

    }

    override fun onSingleTap() {
        if (playControlVisibility == View.GONE) {
            showAllViews(View.VISIBLE, true)
        } else {
            showAllViews(View.GONE, false)
        }
    }

    private fun showPausePlayButton() {
        if (playerAsset !is ExoPlayerAsset) {
            return
        }
        if (videoDetailBinding.playPauseButton.visibility == View.VISIBLE) {
            if (!videoDetailBinding.playPauseButton.isSelected) {
                videoDetailBinding.playPauseButton.visibility = View.GONE
            }
        } else {
            videoDetailBinding.playPauseButton.visibility = View.VISIBLE
        }
    }


    private fun handlePlayPauseClick() {
        if (videoDetailBinding.videoController.player == null) {
            return
        }
        if (videoDetailBinding.videoController.player.playWhenReady) {
            videoDetailBinding.videoController.player.playWhenReady = false
            videoDetailBinding.videoController.isPauseByHaptickFeedBack = true
            videoDetailBinding.playPauseButton.isSelected = true
            setVideoEndAction(PlayerVideoEndAction.PAUSE)
            showVideoLoading(false)
            nlfHandler.removeCallbacksAndMessages(null)
            isVideoPausedbyUser = true
        } else {
            //TODO:: vinod :: Handle audioFocus
            videoDetailBinding.videoController.player.playWhenReady = true
            videoDetailBinding.playPauseButton.isSelected = false
            videoDetailBinding.videoController.isPauseByHaptickFeedBack = false
            setVideoStartAction(PlayerVideoStartAction.RESUME)
            startVideoEvent()
            isVideoPausedbyUser = false
        }
    }

    private fun loadContent() {
        Logger.d(TAG, "loadContent - pos = $position")
        videoDetailBinding.sourceAndShareContainer.isLive = card?.i_videoAsset()?.liveStream
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = FLEX_START

        videoDetailBinding.tagsRecyclerView.layoutManager = layoutManager

        val tagAdapter = DHHashTagAdapter(context, VideoDetailBindUtils.getHashTags(card), pageReferrer, this)
        videoDetailBinding.tagsRecyclerView.adapter = tagAdapter
        contentScale = DHVideoImageUtil.getScaledParamsForData(card?.i_videoAsset()!!)
        val parentParams = getVideoLayoutParams()
        videoDetailBinding.playerHolder.layoutParams = parentParams
        videoDetailBinding.thumbnail.layoutParams = parentParams
        videoDetailBinding.playerParent.layoutParams = getPlayerParentLayoutParams()

        if (playerAsset is ExoPlayerAsset) {
            videoDetailBinding.fullscreen.setOnClickListener {
                onFullScreenClick()
            }
            videoDetailBinding.playPauseButton.setOnClickListener {
                handlePlayPauseClick()
            }
            videoDetailBinding.constraintLyt.setOnClickListener {
                showPausePlayButton()
            }
        }

        Logger.d(TAG, "loadContent isVisibleToUser : $isVisibleToUser pos = $position")
        Logger.d(TAG, "loadContent isLandingStory  $isLandingStory")
        if (isFragmentVisible()) {
            if (isLandingStory || AutoPlayHelper.isAutoPlayAllowed()) {
                Logger.d(TAG, "loadContent > loadVideo")
                loadVideo()
            } else {
                Logger.d(TAG, "loadContent > AutoPlay is off not allowed to play")
                showAutoplayOffState()
            }
        }

        pendingTasksJob = GlobalScope.launch { executePendingTasksWithDelay() }

        if (card != null) {
            notificationUiType?.let {
                NotificationCtaUiHelper.handleNotificationCtaType(it, card,
                        cvm.cardClickDelegate.getShareUsecase(), postId ?: "", location, section)
                notificationUiType = null
            }
        }
    }

    private fun showSelectedLocations() {
        if (parentFragment is LocalZoneFragment && fragmentManager != null) {
            (parentFragment as LocalZoneFragment).showBottomMenu(fragmentManager!!)
        }
    }

    private fun logStoryPageViewEvent() {
        if (loggedPageViewEvent || activity == null || !userVisibleHint) {
            return
        }
        card?.let {
            nonLinearConsumeUsecase.toMediator2().execute(it.i_id())
        }
        loggedPageViewEvent = true
    }

    private fun getVideoLayoutParams(): ConstraintLayout.LayoutParams {
        val params = ConstraintLayout.LayoutParams(contentScale?.width!!, contentScale?.height!!)
        params.bottomToBottom = videoDetailBinding.playerParent.id
        params.topToTop = videoDetailBinding.playerParent.id
        params.leftToLeft = videoDetailBinding.playerParent.id
        params.rightToRight = videoDetailBinding.playerParent.id
        return params
    }

    private fun getPlayerParentLayoutParams(): ConstraintLayout.LayoutParams {
        val params = ConstraintLayout.LayoutParams(contentScale?.width!!, contentScale?.height!!)
        params.bottomToBottom = videoDetailBinding.constraintLyt.id
        params.topToTop = videoDetailBinding.constraintLyt.id
        params.leftToLeft = videoDetailBinding.constraintLyt.id
        params.rightToRight = videoDetailBinding.constraintLyt.id
        return params
    }

    private fun getVideoWrapLayoutParams(): ConstraintLayout.LayoutParams {
        val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        params.bottomToBottom = videoDetailBinding.playerParent.id
        params.topToTop = videoDetailBinding.playerParent.id
        params.leftToLeft = videoDetailBinding.playerParent.id
        params.rightToRight = videoDetailBinding.playerParent.id
        return params
    }

    override fun requestInstreamAd(): Boolean {
        return requestInstreamAd(null)
    }

    private fun requestInstreamAd(adCallbacks: IAdCachePlayerCallbacks?): Boolean {
        Logger.d(TAG, "requestInstreamAd : $position")
        if (playerAsset is ExoPlayerAsset && (card?.i_videoAsset()?.isGif == false &&
                        card?.i_videoAsset()?.loopCount == 0)) {
            return IAdCacheManager.requestInstreamAd(activity, playerAsset as ExoPlayerAsset,
                    PlayerUtils.getInstreamAdParams(playerAsset as ExoPlayerAsset, position, false),
                    position, adCallbacks, card, parentEntity, section)
        }
        return false
    }

    override fun proceedWithStartVideo() {
        Logger.d(TAG, "proceedWithStartVideo : $position")
        adWaitJob?.cancelJob()
        createPlayer()
    }


    private fun loadVideo() {
        if (postId != null) {
            if (parentFragment is LocalZoneFragment || parentFragment is RelatedVideoFragment) {
                if (isLocalZone)
                    ServedButNotPlayedHelper.removeFromlocalZoneServedList(postId!!)
                else
                    ServedButNotPlayedHelper.removeFromRelatedServedList(postId!!)
            }
        }
        Logger.d(TAG, "loadVideo position : $position")
        if (activity?.isFinishing != false || !isAdded || !isVisibleToUser || isBackPressed ||
                view == null) {
            Logger.d(TAG, "loadVideo return : $position")
            return
        }

        if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) && !isPrefetchedContentAvailable()) {
            showErrorScreen(Constants.ERROR_NO_INTERNET)
            return
        }

        card?.let {

            if (isAutoClick && AutoPlayHelper.isAutoPlayAllowed()) {
                Logger.d(TAG, "isAutoClick > setImmersiveModeAsConsumed")
                PlayerControlHelper.isImmersiveMuteMode = PlayerControlHelper.isListMuteMode
                videoProvider?.setImmersiveModeAsConsumed()
            }

            videoDetailBinding.isExoPlayer = (playerAsset is ExoPlayerAsset)
            if (playerAsset is ExoPlayerAsset) {
                if (card?.i_videoAsset()?.hideControl == true) {
                    videoDetailBinding.videoControllerContainer.visibility = View.GONE
                    videoDetailBinding.isHideControl = true
                } else {
                    videoDetailBinding.videoControllerContainer.visibility = View.VISIBLE
                    videoDetailBinding.isHideControl = false
                }
            }

            if (videoWrapper == null && isLandingStory &&
                    parentFragment is BaseVerticalVideoFragment) {
                Logger.d(TAG, "loadVideo Corner case > setting PlayerProvider")
                //Corner case, when user scroll back to same item
                setPlayerProvider( (parentFragment as BaseVerticalVideoFragment).getPlayerProvider())
            }

            if (!DHVideoUtils.isYoutubePlayer(card?.i_videoAsset()) && videoWrapper != null &&
                    videoWrapper?.getAutoplayVideoId().equals(card?.i_id())) {
                Logger.d(TAG, "loadVideo clicked autoplay ID : ${videoWrapper?.getAutoplayVideoId()}")
                playerView = addVideoPlayerWrapperView(videoWrapper)
                if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) && !isPrefetchedContentAvailable()) {
                    showErrorScreen(Constants.ERROR_NO_INTERNET)
                }
                observePlayerStates()
                observeMenuState()
                return
            }

            showThumbnailWithLoader()

            val wait = requestInstreamAd(this) // Exo player check present inside
            if (wait) {
                Logger.d(TAG, "loadVideo adWaitJob : $position")
                adWaitJob?.cancelJob()
                adWaitJob = VideoWaitCoroutine(viewLifecycleOwner, position)
                adWaitJob?.runDelay(3000)
                adWaitJob?.jobComplete?.observe(viewLifecycleOwner, Observer {
                    Logger.d(TAG, "loadVideo jobComplete observe : $position")
                    proceedWithStartVideo()
                })
                return
            }
            createPlayer()
        }
    }

    private fun createPlayer() {
        Logger.d(TAG, "createPlayer position : $position")

        if (activity?.isFinishing != false || !isAdded || !isVisibleToUser || isBackPressed ||
                view == null) {
            Logger.d(TAG, "createPlayer return : $position")
            return
        }

        if (videoDetailBinding.playerHolder.childCount > 0) {
            if (playerView != null) {
                Logger.d(TAG, "startVideoLoad player already added return >> : $position")
                return
            }
            Logger.d(TAG, "startVideoLoad removeAllViews : $position")
            videoDetailBinding.playerHolder.removeAllViews()
        }

        when {
            playerAsset is ExoPlayerAsset -> {
                adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
                playerView = PlayerBuilder.createExoplayer(CommonUtils.getApplication(), this, adsTimeSpentOnLPHelper,
                    playerAsset as ExoPlayerAsset, pageReferrer, eventSection, referrerFlow, referrerLead)
                videoDetailBinding.playerHolder.addView(playerView as ExoPlayerWrapper2)
            }
            DHVideoUtils.isEmbedPlayer(playerAsset) -> {
                playerView = PlayerBuilder.createEmbedplayer(CommonUtils.getApplication(),
                        this, playerAsset!!)
                videoDetailBinding.playerHolder.addView(playerView as WebPlayerWrapper)
            }
            else -> {
                playerView = PlayerFragmentManager(
                        DHVideoUtils.getPlayerAsset(card),
                        videoDetailBinding.playerHolder, this, this,
                        pageReferrer, NewsAnalyticsHelper.getReferrerEventSectionFromActivity(activity)).playerFragment
            }
        }

        observePlayerStates()
        observeMenuState()
        videoStartTime = System.currentTimeMillis()

        (playerView as? VideoPlayerWrapper?)?.setVideoTimeListener(this)
    }

    private fun observeMenuState() {
        VideoHelper.menuStateLiveData.observe(viewLifecycleOwner, Observer {
            Logger.d(TAG, "observeMenuState pos : $position, visible : $isVisibleToUser")
            if (isFragmentVisible()) {
                if (it.isShowing) {
                    isMenuShown = true
                    playerView?.pause()
                } else if (isMenuShown) {
                    Logger.d(TAG, "observeMenuState pos : $position > video resume")
                    isMenuShown = false
                    if (!isFragmentPaused) {
                        playerView?.resume()
                    }
                }
            } else if (isMenuShown) {
                //Just change the flag but don't resume
                isMenuShown = false
            }
        })
    }

    private fun observePlayerStates() {
        playerView?.playerStateLiveData?.value = PlayerEvent(PLAYER_STATE.STATE_IDLE, card?.i_id())
        playerView?.playerStateLiveData?.observe(viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    handlePlayerState(it)
                })
    }

    private fun addVideoPlayerWrapperView(videoPlayerWrapper: VideoPlayerWrapper?): PlayerViewDH? {
        Logger.d(TAG, "addVideoPlayerWrapperView")
        if (videoPlayerWrapper == null) {
            return null
        }

        val exoWrapper = videoPlayerWrapper.getPlayerView()
        if (videoPlayerWrapper.getParentView() != null) {
            videoPlayerWrapper.getParentView()!!.removeView(exoWrapper)
            exoWrapper.visibility = View.VISIBLE
        }

        val companionAdView = exoWrapper.findViewById<CompanionAdView>(R.id.companion_view)
        companionAdView?.let { newCompanion ->
            (newCompanion.parent as? ViewGroup)?.removeView(newCompanion)
            val parent = videoDetailBinding.companionParent as ViewGroup
            parent.removeAllViews()
            parent.addView(newCompanion)
            newCompanion.showIfFilled()
            companionView = newCompanion
        }

        isAutoPlayCard = true
        videoPlayerWrapper.getPlayerCallbacks()?.onVideoInDetail()
        videoPlayerWrapper.resetCallbacks(this, videoPlayerWrapper.getReferrerProvider())
        videoPlayerWrapper.setVideoTimeListener(this)
        videoDetailBinding.playerHolder.removeAllViews()
        videoDetailBinding.playerHolder.addView(exoWrapper)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        params.gravity = Gravity.CENTER
        videoPlayerWrapper.setLayoutParamsForWrapper(params)
        vm.contentRead(card, referrerFlow)
        hideThumbnailAndLoader()
        videoPlayerWrapper.onAttachToBuzzDetail()
        setPlayer(videoPlayerWrapper.getPlayer())
        if (videoPlayerWrapper.isAdDisplaying()) {
            onAdStart()
        }

        handler.postDelayed(Runnable { videoProvider?.loadThumbnailForBackup() }, 100)
        if (videoProvider?.videoAnalyticsHelper is CommonVideoAnalyticsHelper) {
            videoAnalyticsHelper = videoProvider?.videoAnalyticsHelper as CommonVideoAnalyticsHelper
            isAutoplayVideoContinuation = true
            setVideoStartAction(videoAnalyticsHelper.videoStartAction)
            //This two params are only added for a video coming from autoplay in list
            videoAnalyticsHelper.updateParam(PlayerAnalyticsEventParams.IS_AP_CARRIED.getName(), true.toString())
            videoAnalyticsHelper.updateParam(PlayerAnalyticsEventParams.AP_DURATION.getName(), videoAnalyticsHelper.getPlayBackDuration().toString())
            videoLoadTime = videoAnalyticsHelper?.videoLoadTime
        }
        if (videoPlayerWrapper.hasVideoEnded()) {
            videoPlayerWrapper.restartVideo()
        } else if (isReloadVideoByNetworkError && DHVideoUtils.isEmbedPlayer(playerAsset)) {
            videoPlayerWrapper.resumeOnNetworkError(false)
        } else if(CommonUtils.isNetworkAvailable(CommonUtils.getApplication())  || isPrefetchedContentAvailable()) {
            videoPlayerWrapper.resume()
        }
        isReloadVideoByNetworkError = false

        if (parentFragment is BaseVerticalVideoFragment) {
            //Updating State for Parent Fragment
            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_VIDEO_START, postId)
        }
        return videoPlayerWrapper
    }

    private fun showAutoplayOffState() {
        Logger.d(TAG, "handleAutoplayOff >")
        isHideControl = true
        videoDetailBinding.isHideControl = true
        showThumbnailWithPlayIndicator()
        videoDetailBinding.videoControllerContainer.visibility = View.GONE
    }

    private fun resumeAfterDelay() {
        GlobalScope.launch(context = Dispatchers.Main) {
            delay(loadDelayinMs)
            VideoHelper.timeSinceWebPlayer = 0L
            if (isAdded && isVisibleToUser && view != null) {
                if (playerView != null && playerView!!.hasVideoEnded()) {
                    Logger.d(TAG, "resumeAfterDelay restartVideo $loadDelayinMs")
                    playerView!!.restartVideo()
                } else if (playerView != null) {
                    Logger.d(TAG, "resumeAfterDelay resume $loadDelayinMs")
                    hideThumbnailAndLoader()
                    playerView!!.resume()
                } else {
                    Logger.d(TAG, "resumeAfterDelay loadVideo $loadDelayinMs")
                    loadVideo()
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisible: Boolean) {
        super.setUserVisibleHint(isVisible)
        if (isVisible && fragmentManager == null || view == null) {
            return
        }
        if (isVisible && parentFragment is BaseVerticalVideoFragment &&
                !(parentFragment as BaseVerticalVideoFragment).isFragmentVisible()) {
            Logger.d(TAG, "setUserVisibleHint Parent not visible, return at :$position")
            return
        }
        Logger.d(TAG, "setUserVisibleHint isVisibleToUser $isVisible")
        setFragmentVisibility(isVisible)
    }

    private fun setFragmentVisibility(isVisible: Boolean) {
        this.isVisibleToUser = isVisible
        if (view == null || !isAdded || activity == null || card == null) {
            Logger.d(TAG, "setFragmentVisiblity view is NULL at pos : $position")
            playerView?.pause()
            return
        }
        Logger.d(TAG, "setFragmentVisiblity isVisibleToUser : $isVisibleToUser, pos : $position")
        if ((parentFragment is BaseVerticalVideoFragment) &&
                (parentFragment as BaseVerticalVideoFragment).isFragmentVisible() && isVisible) {
            if (isResetToMute) {
                PlayerControlHelper.isDetailMuteMode = true
                isResetToMute = false
            }

            if (videoDetailBinding.errorParent.visibility == View.VISIBLE) {
                return
            }
            contentAdDelegate?.onCardView()
            currentPageReferrer = PageReferrer(NewsReferrer.VIDEO_DETAIL, card?.i_id())
            if (isLocalZone) {
                currentPageReferrer = PageReferrer(NewsReferrer.LOCAL_VIDEO_DETAIL, card?.i_id())
            }
            currentPageReferrer?.referrerAction = NhAnalyticsUserAction.SWIPE
            if (playerView == null && !AutoPlayHelper.isAutoPlayAllowed()) {
                Logger.d(TAG, "AutoPlay not allowed on Swipe")
                showAutoplayOffState()
                return
            }
            resumeAfterDelay()
            showVideoLoading(true)
            showAllViews(View.VISIBLE, true)
            if (playerAsset is ExoPlayerAsset) {
                videoDetailBinding.fullscreen.visibility = View.VISIBLE
            }
            // Next fragment, if news, may prefetch masthead ad.
            AndroidUtils.getMainThreadHandler().postDelayed({
                val event = AdViewedEvent(Constants.EMPTY_STRING, uniqueScreenId, null, AdPosition.MASTHEAD, null)
                BusProvider.getUIBusInstance().post(event)
            }, 200)
        } else {
            if (DHVideoUtils.isYoutubePlayer(card?.i_videoAsset())) {
                playerView?.partiallyReleasePlayer()
            } else {
                playerView?.pause()
                if (DHVideoUtils.isEmbedPlayer(playerAsset)) {
                    VideoHelper.timeSinceWebPlayer = System.currentTimeMillis()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nlfHandler.removeCallbacksAndMessages(null)
        isFragmentPaused = true
        Logger.d(TAG, "onPause pos : $position verticalPos: $verticalPosition")
        setFragmentVisibility(false)
        if (!isVideoBack) {
            playerView?.pause()
            setVideoEndAction(PlayerVideoEndAction.MINIMIZE)
        }
        if (isBusRegistered) {
            isBusRegistered = false
            BusProvider.getUIBusInstance().unregister(this)
        }
        if (videoDetailBinding.coachMarkBg.visibility == View.VISIBLE) {
            videoDetailBinding.coachMarkBg.visibility = View.GONE
            videoDetailBinding.coachMark.visibility = View.GONE
            videoDetailBinding.coachmarkText.visibility = View.GONE
        }
        activity?.unregisterReceiver(downloadReceiver)
        executePendingTasks()
        handler.removeCallbacksAndMessages(null)

        // Trigger the time spent event onPause and if bottom sheet is expended.
        if (isBottomSheetLoaded && bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
                && commentsPostDetailsFragment != null) {
            Logger.d("CommentsBottomSheet", "onpause & bottom sheet is opened")
            commentsPostDetailsFragment?.logVideoDetailCommentsBottomSheetTimeSpentEvent()
        }
    }

    override fun onStop() {
        Logger.d(TAG, "onStop pos : $position verticalPos: $verticalPosition")
        if (!isVideoBack) {
            playerView?.releaseAndSetReload()
        }
        adsHelper?.stop()
        if (isVideoInFullScreen) {
            toggleUIForFullScreen(false)
            videoAnalyticsHelper.updateParam(
                    PlayerAnalyticsEventParams.FULL_SCREEN_MODE.getName(), true.toString())
        }
        super.onStop()
    }

    override fun handleBackPress(): Boolean {
        Logger.d(TAG, "handleBackPress")
        if (!::videoDetailBinding.isInitialized) {
            return false
        }
        if (videoDetailBinding.overlay.isShown) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }
        if (isVideoInFullScreen) {
            onFullScreenClick()
            return true
        }
        isVideoBack = true
        if(playerView is ExoPlayerWrapper2) {
            videoDetailBinding.thumbnail.visibility = View.VISIBLE
            val bitmap = (playerView as ExoPlayerWrapper2).takeScreenshot()
            if(bitmap != null) {
                videoDetailBinding.thumbnail.setImageBitmap(bitmap)
            }
        }

        setVideoEndAction(PlayerVideoEndAction.APP_BACK)
        playerView?.pause()
        videoProvider?.let {
            Logger.d(TAG, "handleVideoBack into List")
            videoProvider?.handleVideoBack(videoAnalyticsHelper)
            GlobalScope.launch(context = Dispatchers.Main) {
                videoDetailBinding.playerHolder.removeAllViews()
            }
        }
        isBackPressed = true
        return false
    }

    override fun onResume() {
        super.onResume()
        isFragmentPaused = false
        Logger.d(TAG, "onResume pos : $position verticalPos: $verticalPosition")
        if (isFragmentVisible()) {
            setFragmentVisibility(true)
        }
        if (!isMenuShown && isFragmentVisible()) {
            videoAnalyticsHelper.updateParam(
                    PlayerAnalyticsEventParams.FULL_SCREEN_MODE.getName(), isVideoInFullScreen.toString())
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed(Runnable {
                kotlin.run {
                    if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED
                            && errorMessageBuilder?.isErrorShown == false) {
                        playerView?.resume()
                    }
                }
            }, 400)
        }
        videoAnalyticsHelper.onFragmentResume()
        adsAnalyticsHelper.onFragmentResume()

        if (isBusRegistered.not()) {
            isBusRegistered = true
            BusProvider.getUIBusInstance().register(this)
        }

        activity?.registerReceiver(downloadReceiver, IntentFilter(DownloadManager
                .ACTION_DOWNLOAD_COMPLETE))

        // start time spent timer on Onresume() and only if the bottom sheet is expanded.
        if (isBottomSheetLoaded && bottomSheetBehavior?.state == BottomSheetBehavior
                        .STATE_EXPANDED && commentsPostDetailsFragment != null) {
            //Do it form here.
            commentsPostDetailsFragment?.resetVideoDetailCommentsBottomSheetTimeSpentTimer()
        }
        adsTimeSpentOnLPHelper?.stopAdsTimeSpentOnLPTimerAndTriggerEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy pos : $position verticalPos: $verticalPosition")
        if (!isAutoPlayCard) {
            playerView?.releasePlayer()
            videoWrapper = null
        } else if (!isBackPressed) {
            videoWrapper?.resetCallbacks(null, null)
        }
        //if (videoWrapper?.getPlayerCallbacks()?.isVideoInNewsList == false) {
        (playerView as? VideoPlayerWrapper?)?.setVideoTimeListener(null)
       // }
        if (!isAutoplayVideoContinuation) {
            videoAnalyticsHelper.onFragmentDestroy()
            adsAnalyticsHelper.onFragmentDestroy()
        }
        if (::videoDetailBinding.isInitialized) {
            videoDetailBinding.playerHolder.removeAllViews()
            videoDetailBinding.mastheadAdViewContainer.removeAllViews()
            videoDetailBinding.companionParent.removeAllViews()
            videoDetailBinding.containerBs.removeAllViews()
            videoDetailBinding.videoController.removeListener()
        }
        playerView = null
        adsHelper?.destroy()

        if (mastHeadDisposable?.isDisposed != true) {
            mastHeadDisposable?.dispose()
        }
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        if (adsSchedulerDisposable?.isDisposed != true) {
            adsSchedulerDisposable?.dispose()
        }
        if (controlsVisibilityDisposable?.isDisposed != true) {
            controlsVisibilityDisposable?.dispose()
        }
        if (activity is NewsBaseActivity) {
            (activity as NewsBaseActivity).setIsInDetailView(false)
        }
        if(totalBufferTimer != null) {
            totalBufferTimer.reset()
        }
        totalBufferTime = 0
    }

    override fun setPlayer(exoPlayer: SimpleExoPlayer?) {
        if (card!!.i_videoAsset()?.hideControl == true) {
            videoDetailBinding.videoController.setIsHideControl(true)
        }
        videoDetailBinding.videoController.player = exoPlayer
        if (card?.i_videoAsset()?.liveStream == true) {
            videoDetailBinding.videoController.setLive(true)
        }
    }

    private fun handlePlayerState(playerEvent: PlayerEvent) {
        Logger.d(TAG, "handlePlayerState - ${playerEvent.playerState}, pos : $position")
        if (!isResumed) {
            Logger.d(TAG, "handlePlayerState - return")
            return
        }
        when (playerEvent.playerState) {
            PLAYER_STATE.STATE_IDLE,
            PLAYER_STATE.STATE_BUFFERING -> onBuffering(true)
            PLAYER_STATE.STATE_READY -> onVideoReady()
            PLAYER_STATE.STATE_PLAYING -> onVideoPlaying()
            PLAYER_STATE.STATE_PAUSED -> onVideoPaused(PlayerVideoEndAction.PAUSE)
            PLAYER_STATE.STATE_VIDEO_START -> onVideoStart()
            PLAYER_STATE.STATE_VIDEO_END -> onVideoEnd()
            PLAYER_STATE.STATE_AD_START -> onAdStart()
            PLAYER_STATE.STATE_AD_END -> onAdEnd()
            PLAYER_STATE.STATE_AD_PAUSED -> onAdPaused()
            PLAYER_STATE.STATE_AD_SKIPPED -> onAdSkipped()
            PLAYER_STATE.STATE_ERROR -> onVideoError(playerEvent.msg)
        }
    }

    private fun onBuffering(isBufferStart: Boolean) {
        if(isBufferStart) {
            totalBufferTimer.start()
            showVideoLoading(true)
            Logger.d(VIDEO_BUFFER_TIME_TAG, "onBuffering onBufferStart: " + totalBufferTimer.totalTime)
        } else {
            totalBufferTimer.stop()
            totalBufferTime = totalBufferTimer.totalTime
            totalBufferTimer.reset()
            Logger.d(VIDEO_BUFFER_TIME_TAG, "onBuffering onBufferStop: " + totalBufferTime)
            videoAnalyticsHelper?.updateBufferDuration(totalBufferTime)
        }
    }

    private fun onAdPaused() {
        setVideoEndAction(PlayerVideoEndAction.PAUSE)
    }

    private fun onAdSkipped() {
        setVideoEndAction(PlayerVideoEndAction.SKIP)
    }

    private fun onAdResumed() {
        startAdEvent()
    }

    fun getRelatedUrl(): String? {
        Logger.d(TAG, "ImmersiveUrl : ${card?.i_immersiveUrl()}")
        return card?.i_immersiveUrl()
    }

    private fun computeCardPosition(): Int {
        return if (parentFragment is BaseVerticalVideoFragment) {
            verticalPosition
        } else {
            position
        }
    }

    fun isAdsPlaying(): Boolean {
        Logger.d(TAG, "isAdsPlaying isAdPlaying : $isAdPlaying at position : $position")
        if (playerView != null) {
            Logger.d(TAG, "isAdsPlaying playerView : ${playerView!!.isAdDisplaying}")
            return playerView!!.isAdDisplaying
        }
        return isAdPlaying
    }

    private fun onAdEnd() {
        if (!isAdPlaying) {
            return
        }
        isAdPlaying = false
        if (!isAdded || !isVisibleToUser || view == null) {
            return
        }
        Logger.d(TAG, "onAdEnd position : $position")
        GlobalScope.launch(context = Dispatchers.Main) {
            delay(200)
            videoDetailBinding.playerHolder.layoutParams = getVideoLayoutParams()
        }
        videoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_AD_PLAYING.getName(), false.toString())

        adsAnalyticsHelper.logVPEvent(PlayerVideoEndAction.COMPLETE,
                playerView?.currentDuration ?: 0)
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_AD_END, postId)
    }

    private fun onAdStart() {
        Logger.d(TAG, "onAdStart position : $position")
        if (!isAdded || !isVisibleToUser || view == null ||
                bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            return
        }
        videoDetailBinding.playerHolder.layoutParams = getVideoWrapLayoutParams()
        videoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_AD_PLAYING.getName(), true.toString())
        onVideoPaused(PlayerVideoEndAction.AD_START)
        isAdPlaying = true
        Logger.d(TAG, "onAdStart isAdPlaying : $isAdPlaying")
        videoDetailBinding.mastheadAdViewContainer.visibility = View.GONE
        startAdEvent()
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_AD_START, postId)
        isAdViewed = true
    }

    fun handleUI(adPlaying: Boolean) {
        Logger.d(TAG, "handleUI adPlaying : $adPlaying at position : $position")
        Logger.d(TAG, "handleUI isAdPlaying : $isAdPlaying")

        checkNetworkAndHideError()

        if (adPlaying) {
            if (videoDetailBinding.isHideAllViews == true)
                videoDetailBinding.isHideAllViews = false
            videoDetailBinding.isAdPlaying = true
            removeAndRequestMastHeadAds(true)
            hideThumbnailAndLoader()

            if (playerAsset is ExoPlayerAsset) {
                videoDetailBinding.fullscreen.visibility = View.GONE
                showAllViews(View.GONE, false)
                //User start dragging up and ads start playing
                if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                if (!requestForFreshAds && playerAsset != null) {
                    requestForFreshAds = true
                    IAdLogger.d(TAG, "onAdDisplayed Remove from Cache : ${playerAsset!!.id}")
                    IAdCacheManager.removeFromCache(playerAsset!!.id)
                }
            }
        } else {
            videoDetailBinding.mastheadAdViewContainer.visibility = View.VISIBLE

            //It may be hidden when ad is displayed
            if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            videoDetailBinding.isAdPlaying = false
            if (playerAsset is ExoPlayerAsset) {
                if (videoDetailBinding.thumbnail.visibility == View.VISIBLE &&
                        videoDetailBinding.playButton.visibility == View.VISIBLE) {
                    videoDetailBinding.fullscreen.visibility = View.GONE
                } else {
                    videoDetailBinding.fullscreen.visibility = View.VISIBLE
                }

                showAllViews(View.VISIBLE, false)
            } else {
                showSubTitle(View.VISIBLE)
            }
        }
    }

    private fun onVideoPlaying() {
        onBuffering(false)
        Logger.d(TAG, "onVideoPlaying")
        if (videoDetailBinding.errorParent.visibility == View.VISIBLE) {
            videoDetailBinding.errorParent.visibility = View.GONE
            if (!isVideoInFullScreen) {
                videoDetailBinding.bottomSheetLayout.visibility = View.VISIBLE
            }
        }
        if (!DHVideoUtils.isExoPlayer(card?.i_videoAsset())) {
            hideThumbnailAndLoader()
        }
        startVideoEvent()
        if (!isVideoStarted) {
            isVideoStarted = true
            showAllViews(View.VISIBLE, false)
        }
        Logger.d(TAG, "isEmbedPlayer : ${DHVideoUtils.isEmbedPlayer(playerAsset)}")
        if (DHVideoUtils.isEmbedPlayer(playerAsset)) {
            startNlfTimer()
        }
    }

    private fun onVideoPaused(endAction: PlayerVideoEndAction) {
        setVideoEndAction(endAction)
        nlfHandler.removeCallbacksAndMessages(null)
    }

    private fun onVideoReady() {
        videoLoadTime = System.currentTimeMillis() - videoStartTime
        showVideoLoading(false)
    }

    private fun onVideoStart() {
        Logger.d(TAG, "onVideoStart")
        if (videoDetailBinding.errorParent.visibility == View.VISIBLE) {
            videoDetailBinding.errorParent.visibility = View.GONE
            if (!isVideoInFullScreen) {
                videoDetailBinding.bottomSheetLayout.visibility = View.VISIBLE
            }
        }
        if (!isVideoStarted) {
            isVideoStarted = true
            showAllViews(View.VISIBLE, false)
        }
        vm.contentRead(card, referrerFlow)
        if (!DHVideoUtils.isExoPlayer(card?.i_videoAsset())) {
            hideThumbnailAndLoader()
        }

        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_VIDEO_START, postId)

        executePendingTasks()
        startVideoEvent()
        Logger.d(TAG, "isEmbedPlayer : ${DHVideoUtils.isEmbedPlayer(playerAsset)}")
        if (DHVideoUtils.isEmbedPlayer(playerAsset)) {
            startNlfTimer()
        }
    }

    private fun startVideoEvent() {
        videoAnalyticsHelper.startVPEvent(playerView?.currentDuration ?: 0,
                videoLoadTime, updateAdditionCardParams() as MutableMap<String, Any>)
        videoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.AD_VIEWED.getName(), isAdViewed.toString())
        videoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_MUTED.getName(), PlayerControlHelper.isDetailMuteMode.toString())
        videoAnalyticsHelper.updateParam(AnalyticsParam.IN_DETAIL.getName(), true.toString())
        videoAnalyticsHelper.updateParam(AnalyticsParam.AUTO_TRANSITION.getName(), isAutoClick.toString())

        if (isInCarousel && parentStoryId != null) {
            videoAnalyticsHelper.updateParam(AnalyticsParam.COLLECTION_ID.name, parentStoryId)
            videoAnalyticsHelper.updateParam(AnalyticsParam.COLLECTION_TYPE.name, Constants.MM_CAROUSEL)
        } else if (isInCarousel && cardPosition != -1) {
            videoAnalyticsHelper.updateParam(AnalyticsParam.CARD_POSITION.name, cardPosition.toString())
        }
        videoAnalyticsHelper.updateParam(
            PlayerAnalyticsEventParams.IS_PREFETCH.getName(), DHVideoUtils.isEligibleToPrefetch(card).toString())
        videoAnalyticsHelper.updateParam(
            PlayerAnalyticsEventParams.IS_CACHED.getName(), ExoRequestHelper.isItemPrefetched(card?.i_id()).toString())
        videoAnalyticsHelper.updateParam(
            PlayerAnalyticsEventParams.USER_CONNECTION_QUALITY_SELECTED.getName(), card?.i_videoAsset()?.selectedQuality)
        videoAnalyticsHelper.updateParam(
            PlayerAnalyticsEventParams.DISABLE_CACHE.getName(), CacheConfigHelper.disableCache.toString())
        videoAnalyticsHelper.updateParam(
            PlayerAnalyticsEventParams.CACHED_VIDEO_URL.getName(), ExoRequestHelper.getStreamCachedUrl(card?.i_id()))
        videoAnalyticsHelper.updateParam(PlayerAnalyticsEventParams.CACHED_DURATION.getName(),
            ExoRequestHelper.getStreamCachedDuration(card?.i_id()).toString())
        videoAnalyticsHelper.updateParam(PlayerAnalyticsEventParams.CACHED_PERCENTAGE.getName(),
            ExoRequestHelper.getStreamCachedDuration(card?.i_id()).toString())
    }

    private fun startAdEvent() {
        val hashMap = HashMap<NhAnalyticsEventParam, Any?>()
        VideoAnalyticsHelper.addReferrerParams(hashMap, referrerFlow, referrerLead, pageReferrer)
        val map = VideoAnalyticsHelper.addCardParams(hashMap, card, false, false, true)
        adsAnalyticsHelper.startVPEvent(videoWrapper?.currentDuration
                ?: 0, map as MutableMap<String, Any>)
    }

    private fun onVideoEnd() {
        showVideoLoading(false)
        showThumbnailWithPlayIndicator()
        setVideoEndAction(PlayerVideoEndAction.COMPLETE)
        if (isVideoInFullScreen) {
            toggleUIForFullScreen(false)
        }
        AndroidUtils.getMainThreadHandler().postDelayed(Runnable {
            if (activity == null || activity?.isFinishing == true || !isAdded || view == null) {
                return@Runnable
            }
            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_VIDEO_END, postId)
        }, 500)

    }

    override fun showVideoLoading(state: Boolean?) {
        if (playerView?.hasVideoEnded() == true) {
            return
        }
        videoDetailBinding.videoLoader.visibility = if (state == true && !isAdPlaying) View.VISIBLE
        else View.GONE
    }

    override fun getCompanionAdView(): CompanionAdView? {
        return companionView
    }

    private fun onMastheadAdInserted() {
        companionView.hideAd()
    }

    override fun isViewInForeground(): Boolean {
        if (activity == null || !isAdded || isFragmentPaused || view == null) {
            return false
        }
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
                || isMenuShown) {
            return false
        }
        return isFragmentVisible()
    }

    override fun getExperiment(): Map<String, String>? {
        return card?.i_experiments()
    }

    override fun getLanguageKey(): String? {
        Logger.d(TAG, "getLanguageKey")
        return card?.i_langCode()
    }

    private fun showThumbnailWithLoader() {
        Logger.d(TAG, "showThumbnailWithLoader")
        videoDetailBinding.thumbnail.visibility = View.VISIBLE
        videoDetailBinding.videoLoader.visibility = View.VISIBLE
        videoDetailBinding.playButton.visibility = View.GONE
    }

    private fun hideThumbnailAndLoader() {
        Logger.d(TAG, "hideThumbnailAndLoader")
        videoDetailBinding.thumbnail.visibility = View.INVISIBLE
        videoDetailBinding.videoLoader.visibility = View.GONE
        videoDetailBinding.playButton.visibility = View.GONE
    }

    private fun showThumbnailWithPlayIndicator() {
        Logger.d(TAG, "showThumbnailWithPlayIndicator")
        videoDetailBinding.thumbnail.visibility = View.VISIBLE
        videoDetailBinding.playButton.visibility = View.VISIBLE
        videoDetailBinding.videoLoader.visibility = View.GONE
    }

    override fun onRenderedFirstFrame() {
        scheduleThumbnailTimer()
        videoPrefetchCallback?.onRenderedFirstFrame(verticalPosition, card)
        Logger.d(TAG_CACHE, " onRenderedFirstFrame : position = " + verticalPosition + ", id = ${card?.i_id()}");

        (parentFragment?.parentFragment as? NewsDetailFragment2)?.onRenderedFirstFrame()
    }

    private fun scheduleThumbnailTimer() {
        videoThumbnailTimerHandler.removeCallbacksAndMessages(null)

        if (!videoDetailBinding.thumbnail.isShown) {
            //Thumbnail is not shown, we dont need of this timer
            Logger.d(TAG, "scheduleThumbnailTimer return >")
            return
        }
        val initialVideoThumbnailDelay = PreferenceManager.getLong(
                        AppStatePreference.INITIAL_VIDEO_THUMBNAIL_DELAY.getName(), 0)
        Logger.d(TAG, "scheduleThumbnailTimer sendEmptyMessageDelayed " + initialVideoThumbnailDelay)
        videoThumbnailTimerHandler.sendEmptyMessageDelayed(THUMBNAIL_TIMER, initialVideoThumbnailDelay)
    }

    private val videoThumbnailTimerHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            removeCallbacksAndMessages(null)
            if (playerView != null) {
                val currentProgressPos = playerView!!.currentDuration ?: 0
                if (videoDetailBinding.thumbnail.isShown && currentProgressPos > 10) {
                    hideThumbnailAndLoader()
                    Logger.d(TAG, "videoThumbnailTimerHandler hideThumbnailAndLoader > ")
                } else if (videoDetailBinding.thumbnail.isShown) {
                    //Thumbnail is not shown, we don't need of this timer
                    val videoThumbnailDelay = PreferenceManager.getLong(
                            AppStatePreference.VIDEO_THUMBNAIL_DELAY.getName(), 100)
                    Logger.d(TAG, "videoThumbnailTimerHandler sendEmptyMessageDelayed " + videoThumbnailDelay)
                    sendEmptyMessageDelayed(THUMBNAIL_TIMER, videoThumbnailDelay)
                }
            }
        }
    }

    private fun onVideoError(errMsg: String?) {
        Logger.d(TAG, "onVideoError : $errMsg")
        if (isAdPlaying) {
            onAdEnd()
        }

        if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
            showErrorScreen(Constants.ERROR_NO_INTERNET)
        } else {
            // other error jump to next video.
            showThumbnailWithPlayIndicator()
            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_ERROR, postId)
        }
    }

    private fun isPrefetchedContentAvailable(): Boolean {
        val playedDuration = playerView?.currentDuration ?: videoWrapper?.currentDuration ?: 0
        val streamCachedDuration = if(card == null) {
            ExoRequestHelper.getStreamCachedDuration(postId)
        } else {
            card?.i_videoAsset()?.streamCachedDuration ?: 0F
        }
        if((playedDuration / 1000) < streamCachedDuration) {
            return true
        }
        return false
    }

    private fun showErrorScreen(errorCode: String) {
        videoDetailBinding.mastheadAdViewContainer.visibility = View.GONE
        videoDetailBinding.bottomSheetLayout.visibility = View.GONE
        isReloadVideoByNetworkError = true
        var errorMsg = when (errorCode) {
            Constants.ERROR_NO_INTERNET -> {
                CommonUtils.getString(R.string.error_no_connection)
            }
            Constants.ERROR_HTTP_NO_CONTENT -> {
                CommonUtils.getString(R.string.error_connectivity)

            }
            else -> {
                CommonUtils.getString(R.string.error_connectivity)
            }
        }
        errorMessageBuilder?.showError(BaseErrorBuilder.getBaseError(errorMsg,
                errorCode),
                showRetryOnNoContent = true,
                error204Message = null,
                hideButtons = false,
                isDhTv = true)
        videoDetailBinding.errorParent.visibility = View.VISIBLE
    }

    override fun onRetryClicked(view: View?) {
        if (vm.failedNetworkCalls.isNotEmpty()) {
            vm.retryFailedUsecases(false)
            if (!isVideoInFullScreen) {
                videoDetailBinding.bottomSheetLayout.visibility = View.VISIBLE
            }
            videoDetailBinding.errorParent.visibility = View.GONE
            return
        }

        if (isFromHistory && !isVideoStarted) {
            vm.fetchFullPost(postId)
            return
        }
        activity?.runOnUiThread(Runnable {
            errorMessageBuilder?.hideError()
            if (!isVideoInFullScreen) {
                videoDetailBinding.bottomSheetLayout.visibility = View.VISIBLE
            }
            videoDetailBinding.errorParent.visibility = View.GONE

            showAllViews(View.VISIBLE, false)
            if (isLandingStory || AutoPlayHelper.isAutoPlayAllowed()) {
                if (playerView == null) {
                    loadVideo()
                } else {
                    playerView?.resumeOnNetworkError(isVideoPausedbyUser)
                }
            } else {
                if (playerView != null) {
                    playerView?.resumeOnNetworkError(isVideoPausedbyUser)
                } else
                    showAutoplayOffState()
            }

            BusProvider.getUIBusInstance().post(NetworkRetry(true))
        })
    }

    override fun onNoContentClicked(view: View?) {
        if (isLocalZone) {
            CommonNavigator.openLocationSelection(this.context, true, true)
        }
    }

    override fun updateAdditionCardParams(): Map<String, Any>? {
        val hashMap = HashMap<NhAnalyticsEventParam, Any?>()
        VideoAnalyticsHelper.addReferrerParams(hashMap, referrerFlow, referrerLead, currentPageReferrer)
        SearchAnalyticsHelper.addSearchParams(eventSection, hashMap)
        val map = VideoAnalyticsHelper.addCardParams(hashMap, card, true, true)
        VideoAnalyticsHelper.addExperimentParams(card, map)
        return map
    }

    private fun onFullScreenClick() {
        toggleUIForFullScreen(!isVideoInFullScreen)
    }

    override fun toggleUIForFullScreen(isFullScreen: Boolean) {
        isVideoInFullScreen = isFullScreen
        if (isFullScreen) {
            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_FULLSCREEN_ON,
                    postId)
        } else {
            VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_FULLSCREEN_OFF,
                    postId)
        }
        if (DHVideoUtils.isYoutubePlayer(card?.i_videoAsset())) {
            //Youtube handles fullscreen by itself
            if (!isFullScreen) {
                playerView?.onBackPressed()
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            return
        }
        videoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.FULL_SCREEN_MODE.getName(), isVideoInFullScreen.toString())
        Logger.d(TAG, "toggleUIForFullScreen")
        if (videoDetailBinding.isHideAllViews == true)
            videoDetailBinding.isHideAllViews = false
        videoDetailBinding.isFullScreen = isFullScreen
        val parentParams: CoordinatorLayout.LayoutParams?
        val playerParentParams: ConstraintLayout.LayoutParams?
        val videoParams: ConstraintLayout.LayoutParams?
        val controllerParams: ConstraintLayout.LayoutParams?
        if (isFullScreen) {
            val c_w: Int?
            val c_h: Int?
            if (card?.i_videoAsset()?.width!! >= card?.i_videoAsset()?.height!!) {
                c_w = CommonUtils.getDeviceScreenHeight()
                c_h = CommonUtils.getDeviceScreenWidth()
                videoDetailBinding.constraintLyt.animate().rotation(90F)
                videoDetailBinding.videoController.setRotationAngle(90f)
            } else {
                c_w = CommonUtils.getDeviceScreenWidth()
                c_h = CommonUtils.getDeviceScreenHeight()
            }
            parentParams = CoordinatorLayout.LayoutParams(c_w, c_h)
            parentParams.gravity = Gravity.CENTER

            playerParentParams = ConstraintLayout.LayoutParams(c_w, c_h)
            playerParentParams.bottomToBottom = videoDetailBinding.constraintLyt.id
            playerParentParams.leftToLeft = videoDetailBinding.constraintLyt.id
            playerParentParams.rightToRight = videoDetailBinding.constraintLyt.id
            playerParentParams.topToTop = videoDetailBinding.constraintLyt.id

            videoParams = ConstraintLayout.LayoutParams(c_w, c_h)
            videoParams.bottomToBottom = videoDetailBinding.playerParent.id
            videoParams.leftToLeft = videoDetailBinding.playerParent.id
            videoParams.rightToRight = videoDetailBinding.playerParent.id
            videoParams.topToTop = videoDetailBinding.playerParent.id

            controllerParams = ConstraintLayout.LayoutParams(c_w, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            controllerParams.bottomToBottom = videoDetailBinding.playerParent.id
            controllerParams.leftToLeft = videoDetailBinding.startGuideline.id
            controllerParams.bottomMargin = CommonUtils.getDimension(R.dimen.vd_margin_small)
            controllerParams.rightToLeft = videoDetailBinding.endGuideline.id
            videoDetailBinding.fullscreen.isSelected = true
            companionView.visibility = View.GONE
            showSubTitle(View.GONE)
            toggleBackgroundColor(View.GONE)
        } else {
            videoDetailBinding.constraintLyt.animate().rotation(0F)
            videoDetailBinding.videoController.setRotationAngle(0f)
            parentParams =
                    CoordinatorLayout.LayoutParams(CommonUtils.getDeviceScreenWidth(),
                            ConstraintLayout.LayoutParams.MATCH_PARENT)
            videoParams = getVideoLayoutParams()
            playerParentParams = getPlayerParentLayoutParams()
            controllerParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            controllerParams.bottomToTop = videoDetailBinding.bottomGuideline.id
            controllerParams.leftToRight = videoDetailBinding.startGuideline.id
            controllerParams.rightToLeft = videoDetailBinding.endGuideline.id


            videoDetailBinding.fullscreen.isSelected = false
            playerView?.onBackPressed()
            companionView.showIfFilled()
            showAllViews(View.VISIBLE, false)
        }
        videoDetailBinding.constraintLyt.layoutParams = parentParams
        videoDetailBinding.playerHolder.layoutParams = videoParams
        videoDetailBinding.playerParent.layoutParams = playerParentParams
        if (DHVideoUtils.isExoPlayer(card?.i_videoAsset())) {
            videoDetailBinding.videoControllerContainer.layoutParams = controllerParams
        }

        if (activity is NewsBaseActivity) {
            (activity as NewsBaseActivity).toggleUIForFullScreen(isFullScreen)
        }
    }

    fun setPlayerProvider(provider: VideoPlayerProvider?) {
        videoProvider = provider
        videoWrapper = provider?.videoPlayerWrapper as? VideoPlayerWrapper?
        isAutoClick = provider?.isAutoImmersiveMode ?: false
    }

    fun setPrefetchCallback(videoPrefetchCallback: VideoPrefetchCallback?) {
        this.videoPrefetchCallback = videoPrefetchCallback
    }

    override fun isVideoInNewsList(): Boolean {
        Logger.d(TAG, "isVideoInNewsList")
        return false
    }

    override fun getLifeCycleOwner(): LifecycleOwner? {
        return viewLifecycleOwner
    }

    override fun setPlayButtonState(isSelected: Boolean) {
        Logger.d(TAG, "setPlayButtonState")
        if (isVisibleToUser && playerAsset is ExoPlayerAsset) {
            videoDetailBinding.playPauseButton.isSelected = isSelected
        }
    }

    override fun muteModeChanged(isMuteMode: Boolean) {
        videoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_MUTED.getName(), isMuteMode.toString())
    }

    override fun onStart() {
        Logger.d(TAG, "onStart pos: $position, visible : $isVisibleToUser")
        super.onStart()
        if (isVideoStarted) {
            setVideoStartAction(PlayerVideoStartAction.RESUME)
        }
        if (isVisibleToUser) {
            startAdsHelper()
        }
    }

    override fun setVideoStartAction(startAction: PlayerVideoStartAction) {
        playerView?.setStartAction(startAction)
        videoAnalyticsHelper.videoStartAction = startAction
    }

    override fun setVideoEndAction(endAction: PlayerVideoEndAction) {
        if (isAutoplayVideoContinuation && isVideoBack) {
            //Dont log VP, as video continues to play in CardsFragment
            return
        }
        playerView?.setEndAction(endAction)

        if (isAdPlaying) {
            adsAnalyticsHelper.logVPEvent(endAction, playerView?.currentDuration ?: 0)
        } else {
            videoAnalyticsHelper.updateParam(NhAnalyticsAppEventParam.REFERRER.getName(), currentPageReferrer?.referrer?.referrerName)
            videoAnalyticsHelper.updateParam(NhAnalyticsAppEventParam.REFERRER_ID.getName(), currentPageReferrer?.id)
            videoAnalyticsHelper.updateParam(
                    AnalyticsParam.INITIAL_LOAD_TIME.getName(), videoLoadTime.toString())
            videoAnalyticsHelper.logVPEvent(endAction, playerView?.currentDuration
                    ?: 0, card, eventSection)

        }
    }

    private fun startAdsHelper() {
        if (adsHelperStarted || !userVisibleHint) return
        adsHelper?.let {
            Logger.d(TAG, "MastHead Requested")
            it.startDHTVMastHead()
            adsHelperStarted = true
        }
    }

    @Subscribe
    fun onReceive(callState: CallState) {
        if (!isVisibleToUser) {
            return
        }
        Logger.d(TAG, "onReceive : $isVisibleToUser return, pos - $position")
        when (callState.state) {
            TelephonyManager.CALL_STATE_RINGING, TelephonyManager.CALL_STATE_OFFHOOK -> {
                currentCallState = TelephonyManager.CALL_STATE_RINGING
                setVideoEndAction(PlayerVideoEndAction.PAUSE)
                playerView?.pause()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (currentCallState != TelephonyManager.CALL_STATE_IDLE) {
                    setVideoStartAction(PlayerVideoStartAction.RESUME)
                    playerView?.resume()
                    Logger.d(TAG, "onReceive : resumeVideo, pos - $position")
                }
                currentCallState = TelephonyManager.CALL_STATE_IDLE
            }
        }
    }

    @Subscribe
    fun onNetworkRetry(network: NetworkRetry) {
        if (isVisibleToUser) {
            //returning here cos :: Post by same Fragment on retry click to inform adjustant
            // Fragments to clear the error screen.
            return
        }
        checkNetworkAndHideError()
    }

    private fun checkNetworkAndHideError() {
        if (videoDetailBinding.errorParent.isShown && card != null &&
                CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
            //Remove Error Screen if shown for No-Internet case
            errorMessageBuilder?.hideError()
            if (!isVideoInFullScreen) {
                videoDetailBinding.bottomSheetLayout.visibility = View.VISIBLE
            }
            videoDetailBinding.errorParent.visibility = View.GONE
        }
    }

    private fun showMastHeadAds() {
        if (activity?.isFinishing != false || !isAdded || !isVisibleToUser || view == null ||
                bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED ||
                isAdPlaying || DHVideoUtils.isYoutubePlayer(card?.i_videoAsset()) ||
                errorMessageBuilder?.isErrorShown == true) return

        if (!isMastheadAdReady) {
            removeAndRequestMastHeadAds(true)
            return
        }

        if (adsSchedulerDisposable?.isDisposed != true) {
            adsSchedulerDisposable?.dispose()
        }
        isMastheadAdInserted = true
        isMastheadAdReady = false
        videoDetailBinding.mastheadAdViewContainer.visibility = View.VISIBLE
        mastHeadAdEntity?.let {
            (mastHeadAdView as? UpdateableAdView?)?.apply {
                updateView(activity, it)
                onCardView(it)
            }
        }
        if (mastHeadAdEntity is BaseDisplayAdEntity) {
            val adEntity = mastHeadAdEntity as BaseDisplayAdEntity
            val spanInt = AdsUtil.getIntValue(adEntity.span, MASTHEAD_EXPIRY_TIME)
            val timeOnScreens = maxOf(spanInt, MASTHEAD_EXPIRY_TIME)
            mastHeadDisposable = Completable.complete()
                    .delay(timeOnScreens.toLong(), TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        removeAndRequestMastHeadAds(true)
                        startShowMastheadTimer()
                    }
                    .subscribe()
        }
        onMastheadAdInserted()
    }

    private fun removeAndRequestMastHeadAds(requestAds: Boolean = true) {
        if (activity?.isFinishing != false || !isAdded || view == null) return
        if (mastHeadDisposable?.isDisposed != true) {
            mastHeadDisposable?.dispose()
        }
        if (isMastheadAdReady) return
        if (requestAds) {
            adsHelperStarted = false
            adsHelper?.resetAdRequestStatus(AdPosition.DHTV_MASTHEAD)
            startAdsHelper()
        }

        if (!isMastheadAdInserted) {
            return
        }
        (mastHeadAdView as? UpdateableAdView?)?.onDestroy()
        videoDetailBinding.mastheadAdViewContainer.visibility = View.GONE
        mastHeadAdView = null
        mastHeadAdEntity = null
        isMastheadAdInserted = false
    }

    override fun insertAd(baseAdEntity: BaseAdEntity, adPositionWithTag: String) {
        if (adPositionWithTag != AdPosition.DHTV_MASTHEAD.value || activity == null ||
                activity?.isFinishing != false || !isAdded || view == null) {
            return
        }

        val cardType = getCardTypeForAds(baseAdEntity)
        val vb = DHAdsViewHolderFactory.getViewBinding(cardType, LayoutInflater.from(activity),
                videoDetailBinding.mastheadAdViewContainer)
        if (vb != null) {
            mastHeadAdView = DHAdsViewHolderFactory.getViewHolder(cardType, vb, uniqueScreenId,
                    viewLifecycleOwner, reportAdsMenuListener = reportAdsMenuListener)
            videoDetailBinding.mastheadAdViewContainer.removeAllViews()
            videoDetailBinding.mastheadAdViewContainer.addView(mastHeadAdView?.itemView)
        }

        if (mastHeadAdView != null) {
            isMastheadAdReady = true
            mastHeadAdEntity = baseAdEntity
        }
    }

    private fun showAllViews(_visibility: Int, requestMastheadAd: Boolean) {
        var visibility = _visibility
        if (isAutoClick) {
            visibility = View.GONE
        }
        if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_HIDDEN &&
                bottomSheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED) {
            Logger.d(TAG, "showAllViews return as BottomSheetBehavior.STATE_HIDDEN")
            return
        }
        if (controlsVisibilityDisposable?.isDisposed != true) {
            controlsVisibilityDisposable?.dispose()
        }
        if (visibility == View.VISIBLE) {
            if (adsSchedulerDisposable?.isDisposed != true) {
                adsSchedulerDisposable?.dispose()
            }
            if (isAdPlaying) return
            companionView.hideAd()
            if (requestMastheadAd) {
                removeAndRequestMastHeadAds(true)
            }
            controlsVisibilityDisposable = Completable.complete()
                    .delay(6, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete {
                        showAllViews(View.GONE, false)
                    }
                    .subscribe()
        } else {
            startShowMastheadTimer()
        }
        playControlVisibility = visibility
        if (DHVideoUtils.isExoPlayer(card?.i_videoAsset()) && !isHideControl
                && card?.i_videoAsset()?.hideControl == false) {
            videoDetailBinding.playPauseButton.visibility = visibility
            videoDetailBinding.videoControllerContainer.visibility = visibility
            videoDetailBinding.videoController.show()
        }
        if (card?.i_videoAsset() != null && card?.i_subFormat() == SubFormat.TVGIF) {
            videoDetailBinding.playPauseButton.visibility = visibility
        }
        if (isVideoInFullScreen) {
            return
        } else {
            toggleBackgroundColor(visibility)
        }
        videoDetailBinding.toolbar.toolbarLyt.visibility = visibility
        showSubTitle(visibility)
        videoDetailBinding.title.visibility = visibility
        videoDetailBinding.tagsRecyclerView.visibility = visibility
        videoDetailBinding.bottomSheetLayout.visibility = visibility
        videoDetailBinding.topFadeOverlay.visibility = visibility
        videoDetailBinding.bottomFadeOverlay.visibility = visibility

        if (isAutoClick) {
            isAutoClick = false
        }
    }

    private fun startShowMastheadTimer() {
        if (adsSchedulerDisposable?.isDisposed != true) {
            adsSchedulerDisposable?.dispose()
        }
        //TODO[umesh.isran] - take delay from handshake
        adsSchedulerDisposable = Completable.complete()
                .delay(6, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    showMastHeadAds()
                }
                .subscribe()
    }

    private fun showSubTitle(visibility: Int) {
        videoDetailBinding.promoted.visibility = visibility
        if (!CommonUtils.isEmpty(CardsBindUtils.showTimeStamp(card))) {
            videoDetailBinding.subTitle.visibility = visibility
        } else {
            videoDetailBinding.subTitle.visibility = View.GONE
            videoDetailBinding.promoted.visibility = View.GONE
        }
    }

    override fun removeSeenAd(adSlot: String) {

    }

    override fun getActivityContext(): Activity? {
        return activity
    }

    override fun isFragmentVisible(): Boolean {
        if (fragmentManager == null || view == null) {
            return false
        }
        if (parentFragment is BaseVerticalVideoFragment &&
                !(parentFragment as BaseVerticalVideoFragment).isFragmentVisible()) {
            return false
        }
        if (parentFragment is BaseVerticalVideoFragment &&
                (parentFragment as BaseVerticalVideoFragment).isFragmentVisible()) {
            return isResumed
        }

        return if (isLocalZone)
            userVisibleHint
        else
            isVisibleToUser
    }

    override fun getAdsMap(): Map<String, BaseAdEntity>? {
        return null
    }

    private fun observeDownloadStart() {
        DownloadUtils.downloadRequestEvent.observe(viewLifecycleOwner, Observer {
            if (it.requestId != null) {
                downloadRequestId = it.requestId
                AnalyticsHelper2.logDownloadEvent(NhAnalyticsAppEvent.ITEM_DOWNLOAD_STARTED,
                        eventSection,
                        VideoAnalyticsHelper.getCardParams(HashMap(), card, true),
                        pageReferrer, card?.i_experiments())
                VideoDownloadBeaconImpl(card?.i_id()).hitDownloadBeacon()
            }
        })
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Logger.d("Downloader", "downloadId - $downloadId")

            if (downloadId == downloadRequestId) {
                DownloadUtils.checkDownloadStatus(activity, downloadRequestId,
                        AnalyticsHelper2.getSection(section), card, disposables, pageReferrer)
            }
        }
    }

    private suspend fun executePendingTasksWithDelay() {
        kotlinx.coroutines.delay(PlayerUtils.getTimeBasedOnNetwork())
        executePendingTasks()
    }

    /**
     * Tasks to be executed once the videos starts OR after time 't'
     * This is to prioritize video request over other api requests
     */
    private fun executePendingTasks() {
        pendingTasksJob?.cancel()
        logStoryPageViewEvent()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.d(TAG, "onHiddenChanged pos: $position, hidden : $hidden")
        if (isVisibleToUser && hidden) {
            playerView?.partiallyReleasePlayer()
        }
    }

    override fun onTimeUpdate(time: String?, position: Long) {
        displayCacheLogs()
        displayBandwidthLogs()
        nlfHandler.removeCallbacksAndMessages(null)
        if (!isAdded || !isResumed || view == null) {
            return
        }
        if (videoDetailBinding.videoLoader.isVisible && playerView?.isPlaying == true) {
            hideThumbnailAndLoader()
        }
        if (!isInCarousel && !isNLFCRequestDone && nonLinearRequestTime > 0 &&
                position >= nonLinearRequestTime * 1000 && !nonLinearItemFetched
                && !CommonUtils.isEmpty(nonLinearFeedUrl)) {
            requestForNlf()
        }
    }

    private fun requestForNlf() {
        if (!isInCarousel && !isNLFCRequestDone && nonLinearRequestTime > 0 &&
                !nonLinearItemFetched && !CommonUtils.isEmpty(nonLinearFeedUrl)) {
            Logger.d(TAG, "NLFC requested - " + nonLinearFeedUrl)
            isNLFCRequestDone = true
            (videoProvider as? AbstractAutoplayViewHolder?)?.nlfcRequestStatus = true
            nonLinearItemFetched = true
            cvm.getNonLinearFeedCard(nonLinearFeedUrl!!, card?.i_id()
                    ?: Constants.EMPTY_STRING)
        }
    }

    private fun startNlfTimer() {
        Logger.d(TAG, "NLFC startNlfTimer nonLinearFeedUrl :" + nonLinearFeedUrl)
        if (!isInCarousel && !isNLFCRequestDone && nonLinearRequestTime > 0 &&
                !nonLinearItemFetched && !CommonUtils.isEmpty(nonLinearFeedUrl)) {
            Logger.d(TAG, "NLFC startNlfTimer started >>")
            val timerDelay: Long = (nonLinearRequestTime * 1000).toLong()
            nlfHandler.removeCallbacksAndMessages(null)
            nlfHandler.postDelayed(Runnable {
                kotlin.run {
                    requestForNlf()
                }
            }, timerDelay)
        }
    }

    override fun showTimeLeft(isShowRemaingTime: Boolean) {
        //Nothing here
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                Constants.REPORTED_ADS_RESULT_CODE -> {
                    val reportedAdEntity = (intent?.getSerializableExtra(Constants
                            .REPORTED_ADS_ENTITY) as? BaseDisplayAdEntity?)
                    onAdReported(reportedAdEntity)
                }
            }
    }

    override fun onAdReported(reportedAdEntity: BaseAdEntity?,
                              reportedParentAdIdIfCarousel: String?) {
        if (adsHelper != null && reportedAdEntity != null) {
            if (reportedAdEntity.adPosition == AdPosition.DHTV_MASTHEAD) {
                removeAndRequestMastHeadAds(false)
            }
        }
    }

    private fun showNoContentErrorScreen() {
        errorMessageBuilder?.noContentError(
                hideButtons = false,
                isDhTv = true, isLocalZone = false)
        videoDetailBinding.errorParent.visibility = View.VISIBLE
    }

    private fun hideNoContentErrorScreen() {
        errorMessageBuilder?.hideError()
        videoDetailBinding.errorParent.visibility = View.GONE
    }

    private fun toggleBackgroundColor(visibility: Int) {
        if(isAdded) {
            val background: Drawable = videoDetailBinding.constraintLyt.background
            val backgroundColor = (background as ColorDrawable).getColor()
            val newBackgroundColor: Int
            if (visibility == View.VISIBLE) {
                newBackgroundColor = ContextCompat.getColor(requireContext(), R.color.video_detail_bg_grey)
            } else {
                newBackgroundColor = ContextCompat.getColor(requireContext(), R.color.black)
            }
            ObjectAnimator.ofObject(
                    videoDetailBinding.constraintLyt, "backgroundColor", ArgbEvaluator(),
                    backgroundColor,
                    newBackgroundColor
            ).apply {
                duration = 300
                start()
            }
        }
    }

    //This method is invoked when device volume is raised to unmute the video
    @Subscribe
    fun onPlaySettingsChangedEvent(event: PlaySettingsChangedEvent) {
        if (playerView != null) {
            PlayerControlHelper.isDetailMuteMode = false
            playerView!!.unmutePlayerOnDeviceVolumeRaised()
            videoDetailBinding.videoController.volumeButtonUnmuteTriggered()
        }
    }

    override fun deeplinkUrl(): String? {
        return card?.i_deeplinkUrl()
    }

    fun updateVideoUrlFromDownload(
        mediaItem: BaseMediaItem?, cacheStatus: ExoDownloadHelper.CacheStatus) {
        Logger.d(TAG_CACHE, " updateVideoUrlFromDownload : id = " + mediaItem?.contentId)
        markVideoAsStreamCached(
            mediaItem, ExoRequestHelper.getStreamCachedStatus(cacheStatus), true)
    }

    fun updateVideoUrlFromExo(
        mediaItem: BaseMediaItem?, cacheStatus: ExoDownloadHelper.CacheStatus) {
        Logger.d(TAG_CACHE, " updateVideoUrlFromExo : id = " + mediaItem?.contentId)
        markVideoAsStreamCached(
            mediaItem, ExoRequestHelper.getStreamCachedStatus(cacheStatus), false)
    }

    fun updateVideoCachedPercentage(mediaItem: BaseMediaItem?, percentage: Float, downloadedVideoDuration: Float) {
        Logger.d(TAG_CACHE, " updateVideoCachedPercentage : id = " + mediaItem?.contentId + ", percentage = $percentage");
        updateDownloadPercentage(mediaItem?.contentId, percentage, downloadedVideoDuration)
    }

    @Synchronized
    fun updateDownloadException(itemId: String?, errorMsg: String?) {
        if(CommonUtils.isEmpty(itemId)) {
            return
        }
        if(!TextUtils.isEmpty(errorMsg)) {
            card?.i_videoAsset()?.downloadErrorMsg = errorMsg
        } else {
            card?.i_videoAsset()?.downloadErrorMsg = "unknown"
        }
    }

    @Synchronized
    fun updateDownloadPercentage(itemId: String?, percentage: Float?, downloadedVideoDuration: Float) {
        if(CommonUtils.isEmpty(itemId)) {
            return
        }
        Logger.d(TAG_CACHE, "updateDownloadPercentage id: ${card?.i_id()}, percentage:  $percentage" + ", cachedDuration:  $downloadedVideoDuration")
        card?.i_videoAsset()?.streamDownloadPercentage = percentage ?: 0F
        card?.i_videoAsset()?.streamCachedDuration = downloadedVideoDuration
    }

    @Synchronized
    fun markVideoAsStreamCached(
        mediaItem: BaseMediaItem?, cacheStatus: StreamCacheStatus, forceVariant: Boolean) {
        if (mediaItem == null || CommonUtils.isEmpty(mediaItem.contentId) ||
            CommonUtils.isEmpty(mediaItem.uri.toString())) {
            return
        }
        if(card == null) {
            return
        }
        try {
            if (card?.i_id()?.equals(mediaItem.contentId, true) == true) {
                val videoAsset = card?.i_videoAsset()
                videoAsset?.streamCachedUrl = mediaItem.uri.toString()
                videoAsset?.streamCachedStatus = cacheStatus
                if (forceVariant) {
                    videoAsset?.variantIndex = mediaItem.variantIndex
                    videoAsset?.isForceVariant = forceVariant
                    Logger.d(TAG_CACHE, "markVideoAsStreamCached PREFETCH Item updated at pos : $verticalPosition")
                    Logger.d(TAG_CACHE, "markVideoAsStreamCached PREFETCH id : " + card?.i_id())
                    Logger.d(
                        TAG_CACHE, ("markVideoAsStreamCached PREFETCH Status : " + videoAsset?.streamCachedStatus
                            .toString() + " streamDownloadPercentage : " + videoAsset?.streamDownloadPercentage)
                                + " streamCachedDuration : " + videoAsset?.streamCachedDuration)
                }
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    override fun onLocationHashtagSelected() {
        showSelectedLocations()
    }
}

interface LocationListener {
    fun onLocationHashtagSelected();
}

