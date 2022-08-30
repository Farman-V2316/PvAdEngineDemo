/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.players.customviews.VideoPlayerWrapper
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.gson.reflect.TypeToken
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.AdFCLimitReachedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativePgiAdAsset
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.helper.AdBinderRepo
import com.newshunt.adengine.view.helper.AdDBHelper
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.adengine.view.helper.PgiAdHelper
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.di.DaggerDetailsListComponent2
import com.newshunt.appview.common.di.DetailLandingItemModule
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.common.profile.helper.mapFilterToTimeLimit
import com.newshunt.appview.common.ui.adapter.NewsDetailAdapter
import com.newshunt.appview.common.ui.helper.*
import com.newshunt.appview.common.video.helpers.ExoRequestHelper
import com.newshunt.appview.common.video.relatedvideo.RelatedVideoFragment
import com.newshunt.appview.common.video.ui.helper.PlayerState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.common.viewmodel.DetailLandingItemViewModel
import com.newshunt.appview.databinding.ActivityNewsDetailsBinding
import com.newshunt.common.helper.analytics.NhAnalyticsUtility
import com.newshunt.common.helper.cachedapi.CacheApiKeyBuilder
import com.newshunt.common.helper.cachedapi.CachedApiHandler
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.util.LangInfoRepo
import com.newshunt.common.view.dbgCode
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NHGenericReferrerSource
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.Edition
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.common.model.entity.cachedapi.CacheType
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiEntity
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.model.entity.TimeFilter
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.news.model.entity.server.asset.PlaceHolderAsset
import com.newshunt.dataentity.onboarding.RegistrationState
import com.newshunt.dataentity.onboarding.RegistrationUpdate
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.deeplinkutils.DeepLinkUtilsCallback
import com.newshunt.dhutil.commons.deeplinkutils.deepLinkUtils
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserDetailPreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.helper.ErrorLogHelper
import com.newshunt.news.helper.NewsDetailUtil
import com.newshunt.news.helper.RateUsCheckHelperNews
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.apis.NewsDetailAPIProxy
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.helper.NotificationActionExecutionHelper
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.ReadFullPostUsecase
import com.newshunt.news.presenter.AdsPrefetchPresenter
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.PgiNativeAdFragment
import com.newshunt.news.view.fragment.TAG
import com.newshunt.news.view.view.PrefetchAdRequestCallback
import com.newshunt.onboarding.presenter.OnBoardingPresenter
import com.newshunt.onboarding.view.view.EditionsView
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.squareup.otto.Subscribe
import io.reactivex.Observable
import kotlinx.android.synthetic.main.photo_slide_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Responsible for hosting paginated list of individual posts(video/ugc/pgc/icc/etc).
 * Also takes care of posts from notification and pagination for posts based on inbox or swipe url.
 *
 * Cases handle:
 * 1. Open a video/post from cards adapter
 * 2. Open a video/post from Notification, Notification Inbox
 * 3. Open a video/post/comment from deeplink
 * 4. Open comment/reply from another post
 * 5. Open a video/post/comment from history
 *
 * All flows need to call {@link observeOtherCards} which eventually calls {@link
 * addStoriesToAdapter} to get all cards added to details fragment pager adapter.
 *
 * In case of card clicked from CardsAdapter will have clone of cards already prepopulated in DB.
 * So just need to query them and listen to live data. So directly call {@link observeOtherCards}
 *
 * In case of loading from history, cards need to be populated based on history using {@link
 * InsertHistoryPosts}
 *
 * In case of loading from notification inbox, cards need to be populated from notification inbox
 * using {@link InsertNotificationPosts}
 *
 * In case of loading from deeplink, cards need to be populated over network using {@link
 * ReadFirstCardUsecase}
 *
 * Created by karthik.r on 2019-10-10.
 */
class NewsDetailFragment2 : BaseSupportFragment(), AdDBHelper, ViewPager.OnPageChangeListener,
        OnErrorClickListner, CardsViewModelProvider, PrefetchAdRequestCallback,
        PgiNativeAdFragment.PgiNativeAdFragmentInterface, EditionsView, OnAdReportedListener, DeeplinkableDetail, TransitionParent {

    private var isScrollStateIdle: Boolean = true
    private var prevPos : Int = -1
    private var isLive = false
    private var isInternalDeeplink = false
    private var isFullScreen = false
    private lateinit var configType: ConfigType
    private var isNextItemPrefetchInProgress = false
    private var isHorizontalListPrefetchInProgress = false
    @Inject
    lateinit var transitionParentDelegate: TransitionParent
   /* val handler = Handler(Looper.getMainLooper(), Handler.Callback { msg ->
        if(msg.what == SHOW_COACHMARK)
            CoachMarksHelper.showSwipeCoachMark(viewContext, isFromNotification)
        return@Callback true
    })

    init {
        CoachMarksHelper.communicationEventUseCase = CommunicationEventUsecase().toMediator2()
        CoachMarksHelper.communicationEventUseCase.execute(Any())
        CoachMarksHelper.communicationLiveData = CoachMarksHelper.communicationEventUseCase.data()
    }*/

    override fun onPageSelected(position: Int) {
        Logger.d(TAG, "onPageSelected $position")
        /*val size = getTotalItems()
        if (position<size-1 && isFromNotification){
            if(CoachMarksHelper.canShowCoachMark(getFormat(position), isNewsDetailNotSwipable, getFormat(position+1))){
                val msg = Message()
                msg.what = SHOW_COACHMARK
                handler.sendMessageDelayed(msg, CoachMarksHelper.timeSpendOnDetail)
            }
        }*/
        // Remove any redundant or unused ad if any, at location currentIndex + 2
        adapter?.let {
            if (position + 2 < it.items.size && it.items[position + 2] is
                    NativePgiAdAsset) {
                val nativePgiAdAsset = it.items[position + 2] as NativePgiAdAsset
                if (this.nativePgiAdAsset === nativePgiAdAsset) {
                    this.nativePgiAdAsset = null
                }
                //Remove ad marker from db otherwise it will appear again
                pgiAdHelper.removeAd(nativePgiAdAsset.baseAdEntity)
                it.items.removeAt(position + 2)
                AdBinderRepo.destroyAd(nativePgiAdAsset.id, uniqueScreenId)
                it.notifyDataSetChanged()
            }

            if (prevPos != -1 && position <= it.items.size - 1) {
                it.isSwipeRight = position > prevPos
            }
            if (position <= it.items.size - 1
                && it.items[position].i_format() != Format.AD) {
                // if prevPos == -1 means user just landed here, so don't show PGI ads, just increment the pgi count
                if (prevPos == -1) {
                    pgiAdHandler.updateSwipeCount()
                } else {
                    pgiAdHandler.updatePageInfoAndSwipeCount(activity, reportAdsMenuListener, viewLifecycleOwner)
                }

                // Insert ad only on right swipe and if current position is not zero due to bug as in
                // http://speakman.net.nz/blog/2014/02/20/a-bug-in-and-a-fix-for-the-way-fragmentstatepageradapter-handles-fragment-restoration/
                if (prevPos < position && position != 0 && it.items[position] !is NativePgiAdAsset) {
                    handlePgiNativeAdInsertion(position)
                }
                prevPos = position
            }
            if (::landingStoryId.isInitialized) {
                transitionParentDelegate.onPageSwipe(it.items[position].i_id(), landingStoryId)
            }
        } ?: if (position == 0) pgiAdHandler.updateSwipeCount()
    }

    override fun getViewContext(): Context {
        return context!!
    }

    override fun startActivity(intent: Intent?) {
        super<BaseSupportFragment>.startActivity(intent)
    }

    override fun onPageScrollStateChanged(state: Int) {
        isScrollStateIdle = (state == ViewPager.SCROLL_STATE_IDLE)
        if (isScrollStateIdle) {
            Logger.d(TAG, "onPageScrollStateChanged - SCROLL_STATE_IDLE")
            if (adapter?.getFragmentAtPosition(activityNewsDetailsBinding.newsDetailPager.currentItem) != null) {
                val currentFragment = adapter?.getFragmentAtPosition(activityNewsDetailsBinding.newsDetailPager.currentItem) as? DHVideoDetailFragment
                if (currentFragment?.isAdsPlaying() == true) {
                    onAdStart()
                } else {
                    onAdEnd()
                }
            } else {
                onAdEnd()
            }
           /* handler.removeMessages(SHOW_COACHMARK)
            CoachMarksHelper.resetSpvCount()*/
        }
    }

    /**
     * Inform View Model of page change, to prepare loading upcoming cards over network
     */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (positionOffsetPixels == 0 && adapter != null) {
            vm.updateCurrentCardLocation(1, position, adapter!!.count)
            /*val size = getTotalItems()
            Logger.d(TAG, "onPageScrolled position = $position  size = $size")
            if (position<size-1){
                if(CoachMarksHelper.canShowCoachMark(getFormat(position), isNewsDetailNotSwipable, getFormat(position+1))){
                    val msg = Message()
                    msg.what = SHOW_COACHMARK
                    if (!handler.hasMessages(SHOW_COACHMARK)) {
                        handler.sendMessageDelayed(msg, CoachMarksHelper.timeSpendOnDetail)
                    }

                }
            }*/
        }
    }

    override fun getTotalItems(): Int {
        return adapter?.items?.size?:0
    }

    override fun getItemIdBeforeIndex(adPosition: Int): String? {
        return adapter?.getItemIdBeforeIndex(adPosition)
    }

    override fun getActivityContext(): Activity? {
        return activity
    }

    private lateinit var section: String
    private var sourceId: String? = null
    private var sourceType: String? = null
    private lateinit var location: String
    private var pageReferrer: PageReferrer? = null
    private var referrer_raw: String? = null
    private var v4SwipeUrl: String? = null
    private var v4BackUrl: String? = null
    private var contentURL: String? = null
    private var useAlternativeContentUrlIfAvailable = false
    var groupInfo: GroupInfo? = null
    private var storyIdList: ArrayList<PlaceHolderAsset>? = null
    private var notificationLandingPosition = 0
    private var isDeeplinkToDetail : Boolean = false

    private val error: ObservableDataBinding<BaseError> = ObservableDataBinding()

    companion object {

        const val LOG_TAG = "NDF2"
        const val LOG_TAG_CACHE = "NDF2_CACHE"

        @JvmStatic
        fun newInstance(intent: Intent, videoPlayerProvider: VideoPlayerProvider?): NewsDetailFragment2 {
            val detailContainerFragment = NewsDetailFragment2()
            detailContainerFragment.setPlayerProvider(videoPlayerProvider)
            detailContainerFragment.arguments = intent.extras
            return detailContainerFragment
        }
    }

    @Inject
    lateinit var cardsViewModelF: CardsViewModel.Factory
    @Inject
    lateinit var detailLandingItemViewModelF: DetailLandingItemViewModel.Factory
    private lateinit var vm: CardsViewModel
    private lateinit var fistItemVm: DetailLandingItemViewModel
	private var parentLocation: String? = null

    private var adapter: NewsDetailAdapter? = null
    private lateinit var landingStoryId: String
    private var landingStory: CommonAsset? = null
    private var videoWrapper: VideoPlayerWrapper? = null
    private var videoPlayerProvider: VideoPlayerProvider? = null
    private var landedOnPost = false
    private var landingIndex = -1
    private var pageId: String? = null
    private var isFromHistory: Boolean = false
    private var isFromNotification: Boolean = false
    private var extraPageNeededParam: Boolean = false
    private var isNewsDetailNotSwipable: Boolean = false
    private var entityId: String = ""
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null

    private var searchQuery: SearchQuery? = null
    private lateinit var activityNewsDetailsBinding: ActivityNewsDetailsBinding
    private var activityNewsDetailsBindingInitialized = false
    private var isSearch: Boolean? = false
    //pgi
    private var pgiAdHandler = PgiAdHandler
    private var nativePgiAdAsset: NativePgiAdAsset? = null
    var pageEntity: PageEntity? = null
    @Inject
    lateinit var pgiAdHelper: PgiAdHelper
    private var backUrlIntent: Intent? = null
    private var preFetchRequestMade = false
    private var postEntityLevel: String = ""
    @Inject
    lateinit var adsPrefetchPresenter: AdsPrefetchPresenter
    private var backPressedHandled = false
    private var postLang : String? = null
    private var onBoardingPresenter: OnBoardingPresenter? = null
    private var pendingStartOnboarding = false

    private var notificationIds: HashMap<String, String>? = null

    private var reportAdsMenuListener: ReportAdsMenuListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        try {
            BusProvider.getUIBusInstance().register(this)
        } catch (ex: Exception) {
            // Do nothing
        }

        Logger.d("NDF2" , "onCreateView is called for the newsdetail fragment")
        activityNewsDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.activity_news_details, container, false)
        activityNewsDetailsBinding.lifecycleOwner = this
        activityNewsDetailsBinding.setVariable(BR.baseError, error)
        activityNewsDetailsBinding.setVariable(BR.listener, this)
        activityNewsDetailsBinding.errorActionBar.findViewById<View>(R.id.actionbar_back_button_layout).setOnClickListener {
            if(!handleActionBarBackPress(false)) activity?.onBackPressed()
        }

        activityNewsDetailsBindingInitialized = true

        if (arguments != null) {
            isDeeplinkToDetail = CommonNavigator.isDeeplinkReferrer(arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?) ||
                    CommonNavigator.isFromNotificationTray(arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?)
            isInternalDeeplink = arguments?.getBoolean(Constants.IS_INTERNAL_DEEPLINK, false)?:false
            landingStoryId = arguments?.getString(Constants.STORY_ID) ?: ""
            isLive = arguments?.getBoolean(Constants.IS_LIVE, false) ?: false
            landingStory = arguments?.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset
            Logger.i(LOG_TAG, "Landing story id is $landingStoryId")
            if (!CommonUtils.isEmpty(arguments?.getString(Constants.REFERRER_RAW))) {
                referrer_raw = arguments?.getString(Constants.REFERRER_RAW)
            }

            v4SwipeUrl = arguments?.getString(Constants.V4SWIPEURL)
            v4SwipeUrl?.let {
                v4SwipeUrl = NewsDetailUtil.getCdnContentUrl(it)
            }

            v4BackUrl = arguments?.getString(Constants.V4BACKURL)
            v4BackUrl?.let {
                deepLinkUtils.getTargetIntentFromUrl(it, requireContext(),
                        uniqueScreenId, PageReferrer(PageReferrer()), false,
                        object : DeepLinkUtilsCallback {
                            override fun setDeeplinkTargetIntent(intent: Intent?) {
                                backUrlIntent = intent
                            }
                        })
            }

            pageReferrer = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            pageEntity = arguments?.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity?
            isSearch = arguments?.getBoolean(NewsConstants.SEARCH)
            val pageEntity = arguments?.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity?
            parentLocation = arguments?.getString(Constants.LOCATION)
            searchQuery = arguments?.getSerializable(Constants.BUNDLE_SEARCH_QUERY) as? SearchQuery
            isFromHistory = arguments?.getBoolean(Constants.BUNDLE_IS_FROM_HISTORY, false) == true
            isFromNotification = arguments?.getBoolean(Constants.BUNDLE_IS_FROM_NOTIFICATION,false) == true

            referrerLead = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            if (referrerLead == null) {
                referrerLead = PageReferrer()
            }
            referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
            referrerFlow = PageReferrer(referrerLead)

            if (isFromNotification) {
                // For notification inbox, fetch all ids and load
                // {@link com.newshunt.news.view.fragment.PlaceholderFragment}
                storyIdList =  CommonUtils.bigBundleRemove(arguments?.getLong(NewsConstants.STORIES_EXTRA)) as? ArrayList<PlaceHolderAsset>
                notificationLandingPosition = arguments?.getInt(Constants.NEWS_LIST_SELECTED_INDEX, 0) ?: 0
                if (storyIdList != null && !storyIdList!!.isEmpty()) {
                    notificationIds = HashMap()
                    notificationIds?.let { map ->
                        storyIdList?.forEach {
                            map.put(it.id, it.notificationUniqueId)
                        }
                    }
                }
            }

            isNewsDetailNotSwipable =
                    arguments?.getBoolean(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE, false) == true

            groupInfo = arguments?.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo?
            pageId = arguments?.getString(Constants.PAGE_ID)
            section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
            location = arguments?.getString(NewsConstants.BUNDLE_LOC_FROM_LIST)
                    ?: Constants.FETCH_LOCATION_DETAIL
            postEntityLevel = arguments?.getString(NewsConstants.POST_ENTITY_LEVEL)
                    ?: PostEntityLevel.TOP_LEVEL.name
            sourceId = arguments?.getString(NewsConstants.SOURCE_ID)
            sourceType = arguments?.getString(NewsConstants.SOURCE_TYPE)

            if (pageId == null) {
                entityId = landingStoryId + System.currentTimeMillis().toString()
            } else {
                extraPageNeededParam = true
                entityId = pageId!!
            }
            activityNewsDetailsBinding.newsDetailPager?.let {
                it.addOnPageChangeListener(this)
                /*this is to make sure on page selected(First story clicked from feed does not count towards PGI swipeCount)
                get called after pager view fully created*/
                it.post {
                    onPageSelected(it.currentItem)
                }
            }
            if (postEntityLevel == PostEntityLevel.LOCAL.name) {
                postEntityLevel = PostEntityLevel.TOP_LEVEL.name
            }

            DaggerDetailsListComponent2.builder()
                    .cardsModule(CardsModule(
                            CommonUtils.getApplication(),
                            SocialDB.instance(),
                            entityId,
                            landingStoryId,
                            pageEntity,
                            location,
                            adDbHelper = this,
                            supportAds = false,
                            lifecycleOwner = this,
                            section = section,
                            searchQuery = searchQuery,
                            performLogin = ::performLogin
                    ))
                    .detailLandingItemModule(DetailLandingItemModule(landingStoryId,
                            entityId,
                            postEntityLevel,
                            SocialDB.instance(),
                            referrerFlow ?: PageReferrer(referrerLead), fragment = this, fragmentName = LOG_TAG))
                    .build()
                    .inject(this)
            vm = ViewModelProviders.of(this, cardsViewModelF)[CardsViewModel::class.java]
            vm.callCFDestroy = true
            activityNewsDetailsBinding.vm = this.vm
            fistItemVm = ViewModelProviders.of(this, detailLandingItemViewModelF)[DetailLandingItemViewModel::class.java]
           /* CoachMarksHelper.communicationLiveData.observe(viewLifecycleOwner, Observer {
                var data = it.getOrNull()
                if (data == null)
                    return@Observer
                else
                    coachMarkResSet(data.events)
            })*/
        }

        if (isNewsDetailNotSwipable) {
            extraPageNeededParam = false
        }
        /*if(isFromNotification)CoachMarksHelper.notificationToDetailLandingCounter++
        CoachMarksHelper.feedToDetailLandingCounter++*/

        initContents()
        initPgiAdHandler()
        reportAdsMenuListener = ReportAdsMenuFeedbackHelper(this, this)

        NotificationActionExecutionHelper.executePendingAction(GenericAppStatePreference.NEXT_STORY_OPEN)
        /*CoachMarksHelper.updateSpvCount(CoachMarksPreference.PREFERENCE_SPV_COUNT)*/
        transitionParentDelegate.postponeEnterTransition(savedInstanceState, arguments)

        ExoDownloadHelper.stopVideoDownload()
        configType = if (section == PageSection.TV.section) ConfigType.BUZZ_LIST else ConfigType.NEWS_LIST
        return activityNewsDetailsBinding.root
    }

    override fun onDestroyView() {
       /* CoachMarksHelper.dismissDialog()
        handler.removeMessages(SHOW_COACHMARK)*/
        try {
            BusProvider.getUIBusInstance().unregister(this)
        } catch (ex: Exception) {
            // Do nothing
        }
        Logger.d(Constants.NON_LINEAR_FEED, "on destroy called")
        super.onDestroyView()
    }

    override fun showEditions(editions: MutableList<Edition>?, currentEdition: Edition?) {
        if (onBoardingPresenter != null && currentEdition != null && !CommonUtils.isEmpty(postLang)) {
            postLang?.let { LangInfoRepo.addSystemSelectedLanguage(it) }
            onBoardingPresenter?.completeOnboarding(postLang!!, currentEdition, emptyList<String>())
            PreferenceManager.savePreference(GenericAppStatePreference.SHOW_TERMS_SNACKBAR, true)
            val langSet = HashSet<String>()
            langSet.add(postLang!!)
            AnalyticsHelper.logCurrentLanguageSelection(langSet, false,
                    postLang, true, pageReferrer, true, PreferenceManager.getPreference(AppStatePreference.LANG_SCREEN_TYPE, "") )
            //Get rid of the onboarding presenter. Not needed anymore.
            onBoardingPresenter?.stop()
            onBoardingPresenter?.destroy()
            onBoardingPresenter = null
        }
    }

    @Subscribe
    fun onRegistrationUpdate(registrationUpdate: RegistrationUpdate) {
        if (RegistrationState.REGISTERED
                        .equals(registrationUpdate.getRegistrationState())) {
            completeOnboardingIfNeeded(postLang)
        }
    }

    private fun completeOnboardingIfNeeded(langCode: String?) {
        //If Post does not have language, nothing to do
        if (CommonUtils.isEmpty(langCode) || !isDeeplinkToDetail) {
            return
        }

        val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)
        //If registration is still not done, wait for it to complete before completing onboarding
        if (!isRegistered) {
            postLang = langCode
            return
        }

        val isOnboardingDone = PreferenceManager.getPreference(
                UserDetailPreference.ON_BOARDING_COMPLETED, java.lang.Boolean.FALSE)

        // If onboarding is still not done or user has not yet selected primary language
        if (!isOnboardingDone || CommonUtils.isEmpty(UserPreferenceUtil.getUserPrimaryLanguage())) {
            postLang = langCode
            if (onBoardingPresenter == null) {
                onBoardingPresenter = OnBoardingPresenter(this)
            }

            pendingStartOnboarding = true
        }
    }

    fun initContents() {
        if (pageId != null) {
            // For opening details page from card adapter
            observeOtherCards()
        } else {
            if (isFromHistory) {
                // For opening details page from history section
                observeCardsFromHistoryEntity()
            } else if (isFromNotification) {
                // For opening details page from notification (with/without swipe URL)
                if (!CommonUtils.isEmpty(v4SwipeUrl)) {
                    contentURL = v4SwipeUrl
                }

                observeCardsFromNotificationList()
            } else {
                if (isNewsDetailNotSwipable) {
                    // For opening details page from sources which are not swipeable
                    observeCardsFromFetchDataEntity()
                } else {
                    if (!CommonUtils.isEmpty(v4SwipeUrl)) {
                        // For all cases swipe url is available
                        contentURL = v4SwipeUrl
                        observeCardsFromFetchDataEntity()
                    } else {
                        // For all cases swipe url is not available, eg. Deeplink
                        useAlternativeContentUrlIfAvailable = true
                        val foryouId = PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE, "")
                        if (!CommonUtils.isEmpty(foryouId)) {
                            SocialDB.instance().fetchDao().contentUrlofPageWithId(foryouId).observe(this,
                                    Observer {
                                        contentURL = it
                                        observeCardsFromFetchDataEntity()
                                    })
                        } else {
                            // For all cases swipe url is not available eg. Deeplink and FORYOU
                                // page not found
                            observeCardsFromFetchDataEntity()
                        }
                    }

                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (adapter?.getFragmentAtPosition(activityNewsDetailsBinding.newsDetailPager.currentItem) != null) {
            val currentFragment = adapter?.getFragmentAtPosition(activityNewsDetailsBinding.newsDetailPager.currentItem)
            currentFragment?.onHiddenChanged(hidden)
        }
    }

    private fun addStoriesToAdapter(items: List<BaseDetailList>, extraPageNeededParam: Boolean) {
        val list = ArrayList<BaseDetailList>()
        list.addAll(items)
        if (adapter == null) {
            adapter = NewsDetailAdapter(
                    childFragmentManager,
                    pageEntity,
                    sourceId,
                    sourceType,
                    parentLocation,
                    videoPlayerProvider,
                    pageReferrer,
                    referrer_raw,
                    isSearch,
                    searchQuery,
                    landingStoryId,
                    extraPageNeededParam,
                    this,
                    entityId, location, groupInfo, section, list, notificationIds, isFromHistory, isLive, arguments)

            arguments?.let {
                val notificationUiType = NotificationCtaUiHelper.getNotificationUiType(it)
                adapter?.notificationUiType = notificationUiType
            }
        } else {
            adapter?.addPosts(list, extraPageNeededParam)
            pushVideosInHorizontalListForPrefetch();
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Logger.d(LOG_TAG, "onActivityCreated : pageId is $pageId and location is $location")

        vm.npStatus.observe(viewLifecycleOwner, Observer {
            Logger.e(TAG, "NP Usecase status:" + it)
        })

        fistItemVm.nlfcLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess) {
                Logger.d(Constants.NON_LINEAR_FEED, "Got the nlfc callback in the detail")
                val nlfcItem = it.getOrNull()
                nlfcItem?.let {
                    insertNonLinearCard(it)
                }
            }
        })

        VideoHelper.videoStateLiveData.observe(viewLifecycleOwner, Observer {
            handlePlayerState(it)
        })

        viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onLifecycleResume() {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(ThemeUtils.themeAutoSwitchSnackbarNeededInDetail()){
            val snackbarView = activityNewsDetailsBinding.snackbarContainer
            AndroidUtils.getMainThreadHandler().postDelayed({
                ThemeUtils.showThemeSnackbar(snackbarView, Constants.THEME_SNACKBAR_DETAIL,PageReferrer(NewsReferrer.STORY_DETAIL))
                ThemeUtils.setThemePreferences(false,false, ThemeUtils.themeAutoSwitchToastNeededInList(),ThemeUtils.themeAutoSwitchToastNeededInDetail())
            },500)
        }  else if(ThemeUtils.themeAutoSwitchToastNeededInDetail()){
            ThemeUtils.showThemeToast()
            ThemeUtils.setThemePreferences(ThemeUtils.themeAutoSwitchSnackbarNeededInList(),ThemeUtils.themeAutoSwitchSnackbarNeededInDetail(), false,false)
        }
    }

    override fun getCardsViewModel(): CardsViewModel {
        return vm
    }

    private fun performLogin(showToast: Boolean, toastMsgId: Int) {
        /*if (showToast && toastMsgId > 0) {
            showToast(CommonUtils.getString(toastMsgId), null)
        }*/
        activity?.let {
            val sso = SSO.getInstance()
            sso.login(it as Activity, LoginMode.USER_EXPLICIT, SSOLoginSourceType.REVIEW)
        }
    }

    private fun fetchCompleteFullStory(postId: String, contentURL: String?) {
        Logger.d(LOG_TAG, "Inside fetchCompleteFullStory")
        fistItemVm.fullPost.observe(this, Observer {
            if (it.error != null) {
                val baseError = ApiResponseOperator.getError(it.error)
                error.value = baseError
                Logger.e(TAG, "ERROR fcmpltstory ${it.error} ${it.data}")
                ErrorLogHelper.logNewsDetailErrorEvent(baseError, NhAnalyticsUtility.ErrorViewType.FULLSCREEN, pageReferrer, pageEntity)
                activityNewsDetailsBinding.errorParent.root.visibility = View.VISIBLE
                activityNewsDetailsBinding.errorActionBar.visibility = View.VISIBLE

            } else {
                error.value = null
                activityNewsDetailsBinding.errorParent.root.visibility = View.GONE
                activityNewsDetailsBinding.errorActionBar.visibility = View.GONE
            }
        })

        fistItemVm.fetchFullPost(postId, contentURL)
    }

    override fun onReadMoreOrRefreshClick(error : BaseError?) {
        if (error == null) {
            initContents()
        }
        else if (error.dbgCode().get().equals("BB04")) {
            handleActionBarBackPress(false)
        }
        else if(error.dbgCode().get().equals("AN0")) {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        else {
            initContents()
        }
    }

    override fun prefetchAdRequests() {
        if (preFetchRequestMade || pageReferrer == null) {
            return
        }

        var section: String? = PageSection.NEWS.section
        when (pageReferrer?.referrerSource) {
            NHGenericReferrerSource.NOTIFICATION_TRAY,
            NHGenericReferrerSource.DEEPLINK -> {
                val entityId = if (backUrlIntent == null) {
                    val lastUserAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS)
                    section = lastUserAppSection?.id
                    lastUserAppSection?.appSectionEntityKey
                } else {
                    backUrlIntent?.extras?.getBundle(NewsConstants.EXTRA_PAGE_ADDED)?.let {
                        (it.getSerializable(NewsConstants.BUNDLE_NEWSPAGE) as? PageEntity?)?.id
                    }
                }
                adsPrefetchPresenter.requestPrefetchAds(entityId, section, AdPosition.P0, activity)
                preFetchRequestMade = true
            }
        }
    }

    override fun onDestroy() {
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_IDLE, null)
        adapter?.items?.forEach {
            if (it is NativePgiAdAsset) {
                AdBinderRepo.destroyAd(it.id, uniqueScreenId)
            }
        }

        NotificationActionExecutionHelper.executePendingAction(GenericAppStatePreference.NEXT_STORY_EXIT)
        ExoRequestHelper.clearDetailCachedItems()
        super.onDestroy()
    }

    /**
     * Prepares the shared element transition
     */
    override fun prepareSharedElementTransition(animatedView: View) {
        transitionParentDelegate.prepareSharedElementTransition(animatedView)
    }

    /**
     * Mukesh
     * PGI ads integration code
     * */
    private fun initPgiAdHandler() {
        pageEntity?.let { entity ->
            pgiAdHelper.adSpec.observe(this, Observer {
                val info = PgiAdHandler.PgiAdHandlerInfo(
                        section,
                        entity.id,
                        entity.subType,
                        entity.entityType,
                        sourceId,
                        sourceType,
                        it.getOrNull()?.get(entity.id)
                )
                pgiAdHandler.initPgiAdHandler(uniqueScreenId, activity, pageReferrer, info)
            })
            pgiAdHelper.fetchAdSpec(entity.id)
        } ?: run {
            val info = PgiAdHandler.PgiAdHandlerInfo(
                    section,
                    null,
                    null,
                    null,
                    sourceId,
                    sourceType,
                    null
            )
            pgiAdHandler.initPgiAdHandler(uniqueScreenId, activity, pageReferrer, info)
        }
    }

    private fun handlePgiNativeAdInsertion(position: Int) {
        pgiAdHandler.nativePgiAdAsset.let {
            if (it == nativePgiAdAsset || it == null) {
                nativePgiAdAsset = it
                return
            }
            it.baseAdEntity?.let { ad ->
                if (isPgiAdInsertionAllowed(position + 1, ad)) {
                    nativePgiAdAsset = it

                    AdBinderRepo.add(ad)
                    AdFrequencyStats.onAdInsertedInView(ad, uniqueScreenId)
                    ad.parentIds.add(uniqueScreenId)
                    adapter?.items?.add(position + 1, it)
                    pgiAdHelper.tryInsertAd(ad, position + 1)
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun isPgiAdInsertionAllowed(position: Int, pgiAd: BaseAdEntity?): Boolean {
        pgiAd ?: return false
        if (pgiAd.campaignId?.let { AdsUtil.isFCLimitReachedForAd(pgiAd, uniqueScreenId) } == true) {
            AdLogger.d(LOG_TAG, "Drop Pgi Ad insertion : FC limit reached")
            PgiAdHandler.discardFCExhaustedAd(pgiAd, activity)
            return false
        }
        val dedupDistance = pgiAd.dedupDistance ?: 0
        if (pgiAd.dedupId.isNullOrBlank() || dedupDistance <= 0) {
            return true
        }
        adapter?.items?.let { items ->
            val start = if (position > dedupDistance) position - dedupDistance else 0
            val end = if (position + dedupDistance - 1 < items.size) position + dedupDistance - 1 else items.size - 1

            for (index in start..end) {
                if (items[index].i_adId().isNullOrBlank()) {
                    continue
                }
                if (pgiAd.dedupId == AdBinderRepo.getAdById(items[index].i_adId()!!)?.dedupId) {
                    AdLogger.d(LOG_TAG, "Drop Pgi Ad insertion at Pos : $position, " +
                            "Conflicting index : $index, dedupId : ${pgiAd.dedupId}")
                    return false
                }
            }
        }
        return true
    }

    private fun handleReportedPgiNativeAdDeletion(baseAdEntity: BaseAdEntity) {
        adapter?.items?.removeAt(activityNewsDetailsBinding.newsDetailPager.currentItem)
        pgiAdHelper.removeAd(baseAdEntity, true)
        adapter?.notifyDataSetChanged()
    }

    @Subscribe
    fun onAdFCLimitReachedEvent(event: AdFCLimitReachedEvent) {
        adapter?.items?.filterIsInstance<NativePgiAdAsset>()?.forEach { asset ->
            asset.baseAdEntity?.let { ad ->
                if (!ad.isShown) {
                    val capId = AdsUtil.getCapId(ad, event.type)
                    if (capId == event.capId) {
                        AdLogger.d(LOG_TAG, "FC limit reached. [${ad.adPosition}] Removing " +
                                "${ad.uniqueAdIdentifier} from uid: $uniqueScreenId")
                        pgiAdHelper.removeAd(ad)
                    }
                }
            }
        }
    }

    private fun insertNonLinearCard(nlfcItem: NLFCItem) {
        if (activityNewsDetailsBinding.newsDetailPager == null) return
        val currentPosition = activityNewsDetailsBinding.newsDetailPager!!.currentItem
        val postId = getItemIdBeforeIndex(currentPosition + 1)
        if (postId == null) {
            Logger.e(Constants.NON_LINEAR_FEED, "Cannot insert the card in detail")
            return
        }

        val curFragment = adapter?.getFragmentAtPosition(currentPosition)
        if (curFragment is RelatedVideoFragment) {
            //DO Nothing here
            Logger.d(TAG, "Ignore NLFC for RelatedVideoFragment")
        } else {
            Logger.d(Constants.NON_LINEAR_FEED, "Inserting the card in the detail")
            fistItemVm.insertNonLinearCard(nlfcItem, postId)

        }
    }

    private fun handlePlayerState(it: PlayerState) {
        Logger.d(TAG, "handlePlayerState :: PlayerState ${it.state}")
        when (it.state) {
            PLAYER_STATE.STATE_AD_START -> onAdStart()
            PLAYER_STATE.STATE_AD_END -> onAdEnd()
            PLAYER_STATE.STATE_FULLSCREEN_ON ->  {
                isFullScreen = true
                disableSwipe()
            }
            PLAYER_STATE.STATE_FULLSCREEN_OFF ->  {
                isFullScreen = false
                enableSwipe()
            }
        }
    }

    private fun disableSwipe() {
        activityNewsDetailsBinding.newsDetailPager?.pagingEnabled = false
    }

    private fun enableSwipe() {
        activityNewsDetailsBinding.newsDetailPager?.pagingEnabled = true
    }

    private fun onAdStart() {
        if (!isScrollStateIdle) {
            return
        }
        activityNewsDetailsBinding.newsDetailPager?.pagingEnabled = false
    }

    private fun onAdEnd() {
        activityNewsDetailsBinding.newsDetailPager?.pagingEnabled = true
    }

    fun toggleUIForFullScreen(isFullScreen: Boolean) {
        activityNewsDetailsBinding.newsDetailPager?.pagingEnabled = !isFullScreen
    }

    override fun handleBackPress(): Boolean {
        if(pageReferrer == PageReferrer(NhGenericReferrer.THEME_CHANGE)) {
            handleActionBarBackPress(false)
        }
        if (activityNewsDetailsBindingInitialized) {
            var handledBackPress = false

            activityNewsDetailsBinding.newsDetailPager?.let {
                val curFragment = adapter?.getFragmentAtPosition(activityNewsDetailsBinding
                        .newsDetailPager!!.currentItem)
                if (curFragment is BaseFragment) {
                    handledBackPress =  curFragment.handleBackPress()
                }
            }

            if (!handledBackPress && handleActionBarBackPress(true)) {
                return false
            }

            if (!handledBackPress && !isInternalDeeplink && NewsNavigator.shouldNavigateToHome(activity, pageReferrer, true,referrer_raw)) {
                val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, landingStoryId)
                pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
                NewsNavigator.navigateToHomeOnLastExitedTab(activity, pageReferrer)
            }

            RateUsCheckHelperNews.checkToShowRateUsOnBackPressed()
            return handledBackPress
        }

        // Forcefully remove news detail page to avoid user getting stuck in detail.
        return false
    }

    fun handleActionBarBackPress(isSystemBackPress : Boolean) : Boolean {
        if (pendingStartOnboarding && !isFullScreen) {
            onBoardingPresenter?.start()
        }

        if (backPressedHandled) {
            backPressedHandled = false
            return true
        }
        if (!CommonUtils.isEmpty(v4BackUrl)) {
            val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, landingStoryId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            CommonNavigator.launchDeeplink(requireContext(), v4BackUrl, pageReferrer)
            backPressedHandled = true
            return true
        } else if (NewsNavigator.shouldNavigateToHome(activity, pageReferrer, isSystemBackPress,referrer_raw)) {
            val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, landingStoryId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            NewsNavigator.navigateToHomeOnLastExitedTab(activity, pageReferrer)
            backPressedHandled = true
            return true
        }

        return false
    }

    /**
     * Observe all cards that are to be loaded. When cards are available, add them to adapter.
     * If adapter doesnt exist, create new adapter and add the cards to it.
     */
    private fun observeOtherCards() {
        Logger.d(LOG_TAG, "inside observeOtherCards")
        fistItemVm.detailLists.observe(viewLifecycleOwner, Observer {
            it.let { detailListCards ->
                if (detailListCards.isNotEmpty()) {
                    val tempDetailList: MutableList<BaseDetailList> = mutableListOf()
                    Logger.d(LOG_TAG, "Details Cards Size is ${detailListCards.size}")
                    detailListCards.forEach { detailCard ->
                        val includeCollectionInSwipe = detailCard.i_mm_includeCollectionInSwipe() ?: true

                        /**
                         * convert pgi ad format to NativePgiAdAsset2 type
                         * */
                        if (detailCard.format == Format.AD) {
                            AdBinderRepo.getAdById(detailCard.id)?.let {
                                NativePgiAdAsset(detailCard.id, null, null, detailCard.format, it)
                                        .apply {
                                            if (tempDetailList.size != 0) {
                                                tempDetailList.add(this)
                                            }
                                        }
                            } ?: AdLogger.d(TAG, "PGI ad id not found in AdBinderRepo")
                        } else if (includeCollectionInSwipe
                                && detailCard.format != Format.BANNER
                                && detailCard.format != Format.NATIVE_CARD
                                && detailCard.format != Format.TICKER
                                && !(detailCard.format == Format.COLLECTION && detailCard.subformat == SubFormat.ENTITY)
                                && detailCard.format != Format.LANGUAGE) {
                            tempDetailList.add(detailCard)
                        }
                    }

                    if (!landedOnPost) {
                        for (i in 0 until tempDetailList.size) {
                            val card = tempDetailList[i]
                            if (card.i_id() == landingStoryId) {
                                landingIndex = i
                                postLang = card.i_langCode()
                                completeOnboardingIfNeeded(postLang)
                                Logger.d(LOG_TAG, "Landing index is $landingIndex")
                                break
                            }
                        }
                    }

                    addStoriesToAdapter(tempDetailList, extraPageNeededParam)
                    if (activityNewsDetailsBinding.newsDetailPager?.adapter == null) {
                        activityNewsDetailsBinding.newsDetailPager?.visibility = View.VISIBLE
                        activityNewsDetailsBinding.newsDetailPager?.adapter = adapter
                    }

                    if (!landedOnPost && landingIndex >= 0) {
                        Logger.d(LOG_TAG, "Landing ons $landingIndex")
                        activityNewsDetailsBinding.newsDetailPager?.currentItem = landingIndex
                        landedOnPost = true
                    }
                }
            }
        })
    }

    fun setPlayerProvider(provider: VideoPlayerProvider?) {
        videoPlayerProvider = provider
        videoWrapper = videoPlayerProvider?.videoPlayerWrapper as? VideoPlayerWrapper?
    }

    private fun observeCardsFromFetchDataEntity() {
        Logger.d(LOG_TAG, "inside observeCardsFromFetchDataEntity")
        // Check if card available. If not fetch from network.
        fistItemVm.firstCard.observe(viewLifecycleOwner, Observer {
            if (it.getOrNull() == true) {
                observeOtherCards()
                error.value = null
                activityNewsDetailsBinding.errorParent.root.visibility = View.GONE
                activityNewsDetailsBinding.errorActionBar.visibility = View.GONE
            } else {
                val exception = it.exceptionOrNull()
                if (exception != null) {
                    val baseError = ApiResponseOperator.getError(exception)
                    error.value = baseError
                    Logger.e(TAG, "ERROR cardsfromfetchdata $baseError")
                    ErrorLogHelper.logNewsDetailErrorEvent(baseError, NhAnalyticsUtility.ErrorViewType.FULLSCREEN, pageReferrer, pageEntity)
                    activityNewsDetailsBinding.errorParent.root.visibility = View.VISIBLE
                    activityNewsDetailsBinding.errorActionBar.visibility = View.VISIBLE
                }
            }
        })

        fistItemVm.fetchFirstCard(contentURL, useAlternativeContentUrlIfAvailable)
    }

    private fun observeCardsFromNotificationList() {
        observeOtherCards()
        if (storyIdList != null) {
            fistItemVm.prepareNotificationStories(storyIdList!!)
        }
        else {
            handleActionBarBackPress(false)
        }
    }

    private fun observeCardsFromHistoryEntity() {
        observeOtherCards()
        val historySince = arguments?.getLong(Constants.BUNDLE_HISTORY_SINCE_TIME,
                mapFilterToTimeLimit(TimeFilter.NINETY_DAYS))
                ?: mapFilterToTimeLimit(TimeFilter.NINETY_DAYS)
        fistItemVm.prepareHistoryStories(historySince)
    }

    override fun onFragmentBackPressed() {
        handleActionBarBackPress(false)
    }

    override fun onPgiAdClosed() {
        var currentPos: Int = activityNewsDetailsBinding.newsDetailPager.currentItem
        if (++currentPos < adapter?.items?.size?:0) {
            activityNewsDetailsBinding.newsDetailPager?.currentItem = currentPos
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                Constants.REPORTED_ADS_RESULT_CODE -> {
                    onAdReported(data?.getSerializableExtra(Constants
                            .REPORTED_ADS_ENTITY) as? BaseDisplayAdEntity)
                }
            }
    }

    override fun onAdReported(reportedAdEntity: BaseAdEntity?,
                              reportedParentAdIdIfCarousel:String?) {
        reportedAdEntity ?: return
        handleReportedPgiNativeAdDeletion(reportedAdEntity)
    }

    private fun coachMarkResSet(list : List<EventsInfo>?){
        if (list==null){
            return;
        }
        for (info in list){
            if(info.activity == null){
                continue
            }
            val activityAttributes = info.activity!!.attributes
            /*if(CommonUtils.equalsIgnoreCase(info.resource, Constants.NEWS) && CommonUtils.equalsIgnoreCase(info.event, Constants.SWIPE_COACH_MARKS)){
                CoachMarksHelper.displayText  = activityAttributes.get(Constants.TEXT)?: CommonUtils.getString(R.string.swipe_coach_mark)
                CoachMarksHelper.maxCoachMarksShow = info.precondition?.get(Constants.FREQUENCY_IN_LIFE_TIME)?.toInt() ?: Constants.FREQUENCY_OF_COACH_MARKS_IN_USERS_LIFE_TIME
                CoachMarksHelper.totalSpvCount = info.precondition?.get(Constants.SWIP_NOT_RECORDED_IN_LAST_N_SPV)?.toInt() ?: Constants.LEFT_SWIPE_NOT_RECORDED_IN_LAST_N_SPV
                CoachMarksHelper.totalTimeElapsed = info.precondition?.get(Constants.TIME_GAP_BETWEEN_COACH_MARKS)?.toInt() ?: Constants.MINIMUM_TIME_GAP_FOR_COACH_MARKS
                CoachMarksHelper.timeSpendOnDetail = info.precondition?.get(Constants.TIME_ELAPSED_FOR_FIRST_COACH_MARK)?.toLong() ?: Constants.TIME_ELAPSED_FOR_COACH_MARKS
                CoachMarksHelper.minimumFeedToDetail = info.precondition?.get(Constants.MIN_FEED_TO_DETAIL_IN_ONE_SESSION)?.toInt() ?: Constants.FEED_TO_DETAIL_IN_ONE_SESSION
            }*/
        }
    }

    private fun getFormat(position: Int) : Format?{
        return adapter?.items?.get(position)?.i_format()
    }

    override fun deeplinkUrl(): String? {
        val currFragment = adapter?.getFragmentAtPosition(activityNewsDetailsBinding.newsDetailPager.currentItem)
        if(currFragment is DeeplinkableDetail) {
            return currFragment.deeplinkUrl()
        }
        return null
    }

    fun onRenderedFirstFrame() {
        Logger.d(LOG_TAG_CACHE, "onRenderedFirstFrame")
        prefetchNextHorizontalVideoOnOverFlow();
    }

    /**
     * Add first m videos to prefetch list
     */
    private fun  pushVideosInHorizontalListForPrefetch() {
        if (CacheConfigHelper.disableCache || isHorizontalListPrefetchInProgress) {
            Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch() disableCache : ${CacheConfigHelper.disableCache} "+
                        " in_progress : $isHorizontalListPrefetchInProgress ")
            return
        }
        var noOfVideosToPrefetch = ExoRequestHelper.remainingToPrefetch(configType)
        Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch noOfVideosToPrefetch : $noOfVideosToPrefetch")
        if(noOfVideosToPrefetch <= 0) {
            Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch noOfVideosToPrefetch > 0 return")
            ExoDownloadHelper.resumeVideoDownload()
            return
        }
        //prefetchNext m Videos in horizontal list of videos
        Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch() ")
        isHorizontalListPrefetchInProgress = true
        GlobalScope.launch(Dispatchers.IO) {
            val delayTime = PlayerUtils.getTimeBasedOnNetwork()
            delay(delayTime)
            val cardList = adapter?.items
            if(!CommonUtils.isEmpty(cardList)) {
                var itemAddedCount = 0
                val position = news_detail_pager?.currentItem ?: 0
                for (index in (position + 1) until cardList!!.size) {
                    if(index >= cardList!!.size) {
                        break
                    }
                    val baseDetailAsset = cardList[index] as? BaseDetailList ?: continue
                    var postEntity = vm?.getPostEntityById(baseDetailAsset?.i_id())
                    val asset = postEntity as? CommonAsset ?: continue
                    if(ExoRequestHelper.isItemPrefetched(asset?.i_id())) {
                        noOfVideosToPrefetch--
                        continue
                    }
                    if(DHVideoUtils.isEligibleToPrefetchInDetail(asset) && !ExoRequestHelper.isPresentInRequestQueue(asset.i_id())) {
                        Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch Added to prefetch List index: $index, " +
                                "contentId : " + asset?.i_id())
                        Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch itemAddedCount : $itemAddedCount " +
                                    " && noOfVideosToPrefetch : $noOfVideosToPrefetch")
                        if (noOfVideosToPrefetch > 0) {
                            Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch prefetchVideo >> " + asset?.i_title())
                            noOfVideosToPrefetch--
                            itemAddedCount++
                            ExoRequestHelper.prefetchVideo(index, asset, configType)
                        } else {
                            Logger.d(LOG_TAG_CACHE, "pushVideosInHorizontalListForPrefetch break at Size : " + "$itemAddedCount")
                            break
                        }
                    }
                }
            }
            isHorizontalListPrefetchInProgress = false
        }
    }

    /**
     * If m-config videos are prefetched, prefetch m+1 video if user views video m
     */
    private fun prefetchNextHorizontalVideoOnOverFlow() {
        Logger.d(LOG_TAG_CACHE, "prefetchNextHorizontalVideoOnOverFlow")
        if (CacheConfigHelper.disableCache || isNextItemPrefetchInProgress) {
            Logger.d(
                LOG_TAG_CACHE, "prefetchNextHorizontalVideoOnOverFlow() disableCache : ${CacheConfigHelper.disableCache} "+
                        " in_progress : $isNextItemPrefetchInProgress ")
            return
        }
        val noOfVideosToPrefetch = ExoRequestHelper.remainingToPrefetch(configType)
        Logger.d(LOG_TAG_CACHE, "prefetchNextHorizontalVideoOnOverFlow noOfVideosToPrefetch : $noOfVideosToPrefetch")
        if(noOfVideosToPrefetch > 0) {
            Logger.d(LOG_TAG_CACHE, "prefetchNextHorizontalVideoOnOverFlow noOfVideosToPrefetch > 0 return")
            return
        }
        isNextItemPrefetchInProgress = true
        GlobalScope.launch(Dispatchers.IO) {
            val position = news_detail_pager?.currentItem ?: 0
            val cardList = adapter?.items
            if(!CommonUtils.isEmpty(cardList)) {
                Logger.d(LOG_TAG_CACHE, "prefetchNextHorizontalVideoOnOverFlow size " + cardList?.size + " position : "+ position)
                for (index in (position + 1) until cardList!!.size) {
                    if(index >= cardList.size) {
                        break
                    }
                    val baseDetailAsset = cardList[index] as? BaseDetailList ?: continue
                    var postEntity = vm?.getPostEntityById(baseDetailAsset?.i_id())
                    val asset = postEntity as? CommonAsset ?: continue
                    if (ExoRequestHelper.isItemAdded(asset?.i_id())) {
                        Logger.d(LOG_TAG_CACHE, "Next eligible video is already added for prefetch")
                        break
                    }
                    Logger.d(LOG_TAG_CACHE, "prefetchNextHorizontalVideoOnOverFlow id : " + asset.i_id() +
                                " DHVideoUtils.isEligibleToPrefetch(asset) : " + DHVideoUtils.isEligibleToPrefetchInDetail(asset) +
                                " !ExoRequestHelper.isPresentInRequestQueue(asset.i_id()) " + !ExoRequestHelper.isPresentInRequestQueue(asset.i_id()))

                    if (asset.i_videoAsset()?.configType == null && DHVideoUtils.isEligibleToPrefetchInDetail(asset) &&
                        !ExoRequestHelper.isPresentInRequestQueue(asset.i_id())) {
                        Logger.d(LOG_TAG_CACHE,
                            "prefetchNextHorizontalVideoOnOverFlow Added to prefetch List : 0, contentId : " + asset.i_id())
                        ExoRequestHelper.prefetchVideo(0, asset, configType)
                        break
                    }
                }
            }
            isNextItemPrefetchInProgress = false
        }
    }
}

/**
 * Create a placeholder post item and insert in DB for details page to load.
 */
class InsertNotificationPosts
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao,
                    private val followEntityDao: FollowEntityDao) :
        BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val url = p1.getString(Constants.BUNDLE_CONTENT_URL)
            val storyList =  CommonUtils.bigBundleRemove(p1.getLong(NewsConstants.STORIES_EXTRA)) as? ArrayList<PlaceHolderAsset>
            groupFeedDao.insReplace(GeneralFeed(entityId, url ?: Constants.EMPTY_STRING, "POST", section))
            val fie = FetchInfoEntity(entityId, location, url, 0, null, null, 0, section)
            val posts = framePlaceholderPosts(storyList)
            fetchDao.insertPostBothChunks(fie, posts, followEntityDao)
            true
        }
    }


    private fun framePlaceholderPosts(storyList: ArrayList<PlaceHolderAsset>?): List<PostEntity> {
        val postList = mutableListOf<PostEntity>()
        storyList?.forEach {
            val pe = PostEntity()
            pe.id = it.id
            pe.format = Format.PLACEHOLDER // Placeholder is used to load PlaceHolderFragment.
            pe.experiment = it.experiments
            postList.add(pe)
        }

        return postList
    }
}

/**
 * Fetch posts from history table and insert them in current entity list on which details page is
 * listening.
 */
class InsertHistoryPosts
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao,
                    private val followEntityDao: FollowEntityDao) :
        BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val sinceTime = p1.getLong(Constants.BUNDLE_HISTORY_SINCE_TIME)
        return Observable.fromCallable {
            val url = p1.getString(Constants.BUNDLE_CONTENT_URL)
            val historyDao = SocialDB.instance().historyDao()
            val data = historyDao.queryDataByTime(sinceTime)
            groupFeedDao.insReplace(GeneralFeed(entityId, url ?: Constants.EMPTY_STRING, "POST", section))
            val fie = FetchInfoEntity(entityId, location, url, 0, null, null, 0, section)
            val posts = framePlaceholderPosts(data)
            fetchDao.insertPostBothChunks(fie, posts, followEntityDao)
            true
        }
    }

    private fun framePlaceholderPosts(historyList: List<HistoryEntity>?): List<PostEntity> {
        val postList = mutableListOf<PostEntity>()
        historyList?.forEach {
            val pe = PostEntity()
            pe.id = it.id
            pe.format = Format.PLACEHOLDER
            postList.add(pe)
        }

        return postList
    }
}

@Deprecated("use ReadFullPostUsecase. Used only for reference. Will be removed.")
class ReadFullPostUsecase_
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val api: NewsDetailAPI,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao,
                    private val followEntityDao: FollowEntityDao,
                    private val postDao : PostDao) : BundleUsecase<PostEntity?> {

    override fun invoke(p1: Bundle): Observable<PostEntity?> {
        var url = p1.getString(Constants.BUNDLE_CONTENT_URL)
        val contentUrlOptional = p1.getBoolean(Constants.BUNDLE_CONTENT_URL_OPTIONAL, false)
        val cache: Observable<PostEntity?> = Observable.fromCallable {
            val cacheApiKeyBuilder = CacheApiKeyBuilder()
            cacheApiKeyBuilder.addParam("class", "newsDetailAPI")
            cacheApiKeyBuilder.addParam("storyId", postId)

            val cachedApiEntity = CachedApiEntity()
            cachedApiEntity.key = cacheApiKeyBuilder.build()
            cachedApiEntity.cacheType = CacheType.USE_NETWORK_IF_NO_CACHE

            val type = object : TypeToken<ApiResponse<PostEntity>>() {}.type
            val cachedApiHandler =
                    CachedApiHandler<ApiResponse<PostEntity>>(cachedApiEntity, null, type)
            val apiResp: ApiResponse<PostEntity>? = cachedApiHandler.dataFromCache
            if (apiResp != null) {
                if (contentUrlOptional && CardsBindUtils.isViralPost(apiResp.data)) {
                    val contentUrl = SocialDB.instance().pageEntityDao().getAllPages(AppSection.NEWS.name.toLowerCase())?.filter {
                        it.pageEntity.contentUrl?.contains(PageType.VIRAL.pageType) == true
                    }?.getOrNull(0)?.pageEntity?.contentUrl

                    if (!CommonUtils.isEmpty(contentUrl)) {
                        url = contentUrl
                    }
                }

                groupFeedDao.insReplace(GeneralFeed(entityId, "", "POST", section))
                val fie = FetchInfoEntity(entityId, location, url, 0, null, null, 0, section)
                val posts = Collections.singletonList(apiResp.data)
                fetchDao.insertPostBothChunks(fie, posts, followEntityDao)
            }

            apiResp?.data
        }

        val network: Observable<PostEntity?> = NewsDetailAPIProxy.contentOfPost(api, postId,
                entityId, location, section, false, true, null, null, postDao)
                .lift(ApiResponseOperator()).map {
            if (contentUrlOptional && CardsBindUtils.isViralPost(it.data)) {
                val contentUrl = SocialDB.instance().pageEntityDao().getAllPages(AppSection.NEWS.name.toLowerCase())?.filter {
                    // TODO : karthik.r change logic to check page type
                    it.pageEntity.contentUrl?.contains(PageType.VIRAL.pageType) == true
                }?.get(0)?.pageEntity?.contentUrl

                if (!CommonUtils.isEmpty(contentUrl)) {
                    url = contentUrl
                }
            }

            groupFeedDao.insReplace(GeneralFeed(entityId, "", "POST", section))
            val fie = FetchInfoEntity(entityId, location, url, 0, null, null, 0, section)
            val posts = Collections.singletonList(it.data)
            fetchDao.insertPostBothChunks(fie, posts, followEntityDao)
            it.data
        }.onErrorReturn {
            throw it
        }

        return Observable.merge(cache, network)
    }
}

/**
 * Read the first card for this entity list and insert it for news detail fragment to get.
 */
class ReadFirstCardUsecase
@Inject constructor(
        @Named("entityId") private val entityId: String,
        @Named("location") private val location: String,
        @Named("section") private val section: String,
        @Named("postId") private val postId: String,
        private val groupFeedDao: GeneralFeedDao,
        private val readFullPostUsecase: ReadFullPostUsecase,
        private val fetchDao: FetchDao) : BundleUsecase<Boolean> {

    override fun invoke(p1: Bundle): Observable<Boolean> {
        var url = p1.getString(Constants.BUNDLE_CONTENT_URL)
        val contentUrlOptional = p1.getBoolean(Constants.BUNDLE_CONTENT_URL_OPTIONAL, false)
        val fromNw = readFullPostUsecase.invoke(p1)
        val obs = Observable.fromCallable {
            fetchDao.lookupById(postId)
        }.map {
            Logger.d("NDF2", "ReadFirstCardUsecase ${it.format}")
            AnalyticsHelper2.logDevCustomErrorEvent(("ReadFirstCardUsecase post is in post table: ${it.id}"))
            if (contentUrlOptional && CardsBindUtils.isViralPost(it)) {
                val contentUrl = SocialDB.instance().pageEntityDao().getAllPages(AppSection.NEWS.name.toLowerCase())?.filter {
                            it.pageEntity.contentUrl?.contains(PageType.VIRAL.pageType) == true
                        }?.getOrNull(0)?.pageEntity?.contentUrl

                if (!CommonUtils.isEmpty(contentUrl)) {
                    url = contentUrl
                }
            }

            groupFeedDao.insReplace(GeneralFeed(entityId, "", "POST", section))
            val fie = FetchInfoEntity(entityId, location, url, 0, null, null, 0, section)
            fetchDao.insertPostInFetch(fie, it)
            true
        }.onErrorResumeNext { t: Throwable ->
            Logger.d("NDF2", "ReadFirstCardUsecase Error $t")
            fromNw.map {
                if(it.content2 == null) {
                    Logger.d("NDF2", "ReadFirstCardUsecase  from network ${it.id}")
                    AnalyticsHelper2.logDevCustomErrorEvent(("ReadFirstCardUsecase from networkand content2 null: ${it.id}"))
                }
                true
            }.onErrorReturn {
                Logger.d("NDF2", "Network Level Error $it")
                throw it
            }
        }.onErrorReturn {
            Logger.d("NDF2", "Second Level Error $it")
            throw it
        }

        return obs
    }
}


data class FullPagePojo(
        val data: PostEntity? = null, // may/may-not contain loader
        val tsData: Long? = null,
        val error: Throwable? = null, // should be BaseError?
        val tsError: Long? = null
)


interface OnErrorClickListner {
    fun onReadMoreOrRefreshClick(error : BaseError?)
}

interface CardsViewModelProvider {
    fun getCardsViewModel(): CardsViewModel
}

interface TransitionParent {
    fun prepareSharedElementTransition(animatedView: View)
    fun postponeEnterTransition(savedInstanceState: Bundle?, arguments: Bundle?) {}
    fun onPageSwipe(currentItemId: String, landingStoryId: String) {}
}

