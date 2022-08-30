/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.collection.ArrayMap
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.MASTAdView.MASTAdView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.dailyhunt.tv.players.analytics.VideoAnalyticsHelper
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.android.material.snackbar.Snackbar
import com.newshunt.adengine.instream.IAdCacheManager
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.BackUpAdConsumer
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdDBHelper
import com.newshunt.adengine.view.helper.AdsHelper
import com.newshunt.adengine.view.helper.FetchAdsSpec
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.analytics.entity.NhAnalyticsPVType
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.di.DaggerCardsComponent2
import com.newshunt.appview.common.entity.CardsPojoPagedList
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.common.postcreation.view.service.UploadJobService
import com.newshunt.appview.common.ui.activity.HomeActivity
import com.newshunt.appview.common.ui.adapter.CardsAdapter
import com.newshunt.appview.common.ui.adapter.ListToDetailTransitionHelper
import com.newshunt.appview.common.ui.fragment.ImportFollowFragment
import com.newshunt.appview.common.ui.helper.*
import com.newshunt.appview.common.ui.listeners.AdapterChangeObserver
import com.newshunt.appview.common.ui.viewholder.CommonAssetViewHolder
import com.newshunt.appview.common.utils.FollowBlockSignalUtils
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.video.utils.DownloadUtils
import com.newshunt.appview.common.viewmodel.AdjunctLanguageViewModel
import com.newshunt.appview.common.viewmodel.CFCountTracker
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.common.viewmodel.ClickDelegateProvider
import com.newshunt.appview.databinding.ListShimmerLayoutBindingImpl
import com.newshunt.appview.databinding.SocCardsFragBinding
import com.newshunt.common.helper.analytics.NhAnalyticsUtility
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.ApiResponseUtils.Companion.composeListNoContentError
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.util.LangInfoRepo
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.customview.NoPredAnimLayoutManager
import com.newshunt.common.view.dbgCode
import com.newshunt.common.view.isNoContentError
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.DistancingSpec
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.asset.NLFCItem
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.AdjunctLangResponse
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.common.model.entity.TabClickEvent
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.social.entity.CardsPayload
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.TickerHelper3
import com.newshunt.dhutil.helper.preference.AdjunctLangPreference
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.runOnce
import com.newshunt.dhutil.toArrayList
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.helper.ErrorLogHelper
import com.newshunt.news.helper.NCCardDBHelper
import com.newshunt.news.helper.NHJsInterfaceWithMenuClickHandling
import com.newshunt.news.helper.NestedCollectionCardsHelper
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.helper.NonLinearStore
import com.newshunt.news.helper.SimpleItemDecorator
import com.newshunt.news.helper.handler.CardNudgeHelper
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.HidePostUsecase
import com.newshunt.news.model.usecase.NLResp
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.ListEditInterface
import com.newshunt.news.view.customview.SlidingTabLayout
import com.newshunt.news.view.fragment.FollowingFilterCallback
import com.newshunt.news.view.fragment.ScrollTabHolderFragment
import com.newshunt.news.view.fragment.UserFollowEntityFragment
import com.newshunt.news.viewmodel.FollowUpdateViewModel
import com.newshunt.onboarding.helper.AdjunctLanguageUtils
import com.newshunt.onboarding.view.activity.OnBoardingActivity
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.socialfeatures.model.internal.service.VideoDownloadBeaconImpl
import com.newshunt.socialfeatures.presenter.LifecycleAwareAuth
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.newshunt.viral.utils.visibility_utils.VisibilityCalculator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.sticky_adjunct_card_layout.view.adjunct_lang_cancel
import kotlinx.android.synthetic.main.sticky_adjunct_card_layout.view.adjunct_lang_tick
import kotlinx.android.synthetic.main.sticky_adjunct_card_layout.view.text_in_app_lang
import kotlinx.android.synthetic.main.sticky_adjunct_card_layout.view.text_in_locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.collections.set
import kotlin.math.max

/**
 * 1. Uses ListAdapter + Data binding.
 * 2. Observe [CardsViewModel] and show appr UI
 */
class CardsFragment : ScrollTabHolderFragment(), AdDBHelper, NCCardDBHelper, ListEditInterface, AdEntityReplaceHandler,
        NativeAdHtmlViewHolder.CachedWebViewProvider, LifecycleObserver, OnAdReportedListener, FragmentTransitionViewProvider {
    private fun logTag() = "CF2[${this.tabPosition}]@$additionalLogTag"

    private var entityId: String = "1"
    private var location: String = Constants.EMPTY_STRING
    private var slidingTabId: Int? = null
    @Inject
    lateinit var cardsViewModelF: CardsViewModel.Factory
    @Inject
    lateinit var followUpdateViewModelF: FollowUpdateViewModel.Factory
    @Inject
    lateinit var adsHelperF: AdsHelper.Factory
    @Inject
    lateinit var tickerHelper3: TickerHelper3
    @Inject
    lateinit var fetchDao: FetchDao
    @Inject
    lateinit var auth: LifecycleAwareAuth
    @Inject
    lateinit var fetchAdsSpec: FetchAdsSpec.Factory
    @Inject
    lateinit var cfCountTracker : CFCountTracker

    @Inject
    lateinit var nhJsInterfaceWithMenuClickHandling: NHJsInterfaceWithMenuClickHandling

    private var createdAt: Long = Long.MAX_VALUE

    private lateinit var cardsModule : CardsModule
    private lateinit var vm: CardsViewModel
    private var isColdSignalCarouselAPITrig = false
    private var isColdSignalCarousalShown = false
    private var isExplicitSignalCarouselAPITrig = false
    private var isExplicitSignalCarousalShown = false
    private var explicitSignalPosition = 0
    private  var explicitSignalCardPosition:Int = 0
    private  var coldSignalCardPosition:Int = 0
    private lateinit var vmFollowUpdate: FollowUpdateViewModel

    private lateinit var adjunctLangVm: AdjunctLanguageViewModel
    private lateinit var adjunctVmFactory:AdjunctLanguageViewModel.AdjunctLanguageViewModelF
    private lateinit var section: String
    private var supportAds: Boolean = false
    private var videoRequester: VideoRequester? = null
    private var visibilityCalculator: VisibilityCalculator? = null
    private var autoPlayManager: AutoPlayManager? = null
    private var isAutoplayClickToDetial: Boolean = false
    private var editModeOn = false
    private var listType: String? = null
    /* to be used for fragements that show filtered data - changing filter causes reload of data,
    * due to change in URL(cache would be invalid). This is to prevent momentary error-screen that comes when changing filters.*/
    private var delayShowingError: Boolean = false;
    private var additionalLogTag : String = ""
    private var useGrid: Boolean = false
    private var nonLinearCardList: List<NLFCItem>? = null
    private var videoIndex = 0

    private var startTime: Long = -1
    private val uniqueIdentifierId = UniqueIdHelper.getInstance().generateUniqueId()
    private var nudges: Map<String, CardNudge?>? = null

    private var adsHelper: AdsHelper? = null
    @Inject
    lateinit var nccHelper : NestedCollectionCardsHelper
    private lateinit var cardsAdapter: CardsAdapter
    private lateinit var adapterObserver: AdapterChangeObserver

    private val error: ObservableDataBinding<BaseError> = ObservableDataBinding()
    private lateinit var linLayoutManager: LinearLayoutManager
    private lateinit var socCardsFragBinding: SocCardsFragBinding
    private var recyclerViewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var pageEntity: FeedPage? = null
    private var errorLayoutId: Int? = null
    private var groupInfo: GroupInfo? = null
    private var itemLocation: String? = null
    private var pageEntityData: PageEntity? = null
    private var currentPageReferrer: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var referrerFlowParent: PageReferrer? = null
    private var providedReferrer: PageReferrer? = null
    private var tabPosition = -1
    private var hideNoContentSnackbar = false
    private var searchQuery: SearchQuery? = null
    private var lastRefreshTime: Long? = null
    private var errorView: ViewDataBinding? = null
    private var enableDivider: Boolean = true
    protected val disposables = CompositeDisposable()
    private var isStopped = false
    private var adapterDirty: Boolean = false
    private lateinit var fragmentCommunicationsViewModel: FragmentCommunicationsViewModel
    private val adapterDirtyResetHandler = Handler(Looper.getMainLooper())
    private var doesPrevListHasLocalCard: Boolean = false
    //A cache to reuse Webviews to avoid reloading heavy HTML Ads in the list
    private val webViewCache by lazy {
        ArrayMap<String, WeakReference<MASTAdView>>()
    }
    private var isEndReached = false
    private var isFirstPageData = false
    private var reportAdsMenuListener: ReportAdsMenuListener?= null
    private val nudgeHelper = CardNudgeHelper()
    private var canInsertLanguageSelectionCard = false
    private var adjunctLangResponse: AdjunctLangResponse ?= null
    private lateinit var followBlockutil: FollowBlockSignalUtils

    private var fireSLVFor1stResponseFromDB = runOnce<CardsPojoPagedList?> {
        it?.data?.getSnapshot()?.let { list ->
            /*     logStoryListViewEvent(true, NLResp().apply {
                     rows = list.map { item ->
                         when {
                             item is AnyCard -> item
                             item is CommonAsset -> item.toAnyCard()
                             else -> null
                         }
                     }.filterIsInstance<AnyCard>()
                     pageNumber = 0
                 })*/
            true
        } ?: false
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        private var handlingEdgeScroll = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE, RecyclerView.SCROLL_STATE_SETTLING -> {
                    updateCurrentCardLocation()
                    handlingEdgeScroll = false
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) onNudgeEvent()
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (handlingEdgeScroll || dy < 0) {
                return
            }
            handlingEdgeScroll = true
            recyclerView.postDelayed({
                if (activity?.isFinishing != false) {
                    return@postDelayed
                }
                if (!userVisibleHint) {
                    return@postDelayed
                }
                Logger.i(LOG_TAG_SCROLLER, "checking end")
                handlingEdgeScroll = false
                if (!recyclerView.canScrollVertically(1)) {
                    Logger.i(LOG_TAG_SCROLLER, "stop scroll")
                    recyclerView.stopScroll()
                }
            }, 100)
            // delay is to reduce frequency of check and handle last call onScrolled
        }

        fun updateCurrentCardLocation() {
            if (activity?.isFinishing == true) {
                return
            }
            // Rows visible on the screen
            val visibleItemCount = linLayoutManager.childCount

            // Number of the first row visible on the screen
            val firstVisibleItem = linLayoutManager.findFirstVisibleItemPosition()

            // Total rows in the view
            val totalItemCount = linLayoutManager.itemCount
            //todo @rekha need to replace with config value.


            val totalItemsExAd = cardsAdapter.getItemCountExEmpAd()
            vm.cardsAdapterSize.value= totalItemsExAd

            (activity as? CardsExternalListener)?.updateCurrentCardPosition(visibleItemCount,
                    firstVisibleItem, totalItemCount)
            (parentFragment as? CardsExternalListener)?.updateCurrentCardPosition(visibleItemCount, firstVisibleItem, totalItemCount)
            vm.updateCurrentCardLocation(visibleItemCount, firstVisibleItem,
                    totalItemCount)
            adsHelper?.tryinsertP1Ad(visibleItemCount, firstVisibleItem, totalItemCount)
            nccHelper.tryInsertNCCard(visibleItemCount,firstVisibleItem,totalItemCount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createdAt = System.currentTimeMillis()
        val clickDelegateProvider = activity as? ClickDelegateProvider?
        section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
        pageEntityData = arguments?.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity
        hideNoContentSnackbar = true /*arguments?.getBoolean(Constants.HIDE_NO_CONTENT_SNACKBAR, false)
                ?: false*/
        tabPosition = arguments?.getInt(NewsConstants.BUNDLE_ADAPTER_POSITION) ?: 0
        supportAds = arguments?.getBoolean(Constants.SUPPORT_ADS) ?: false
        listType = arguments?.getString(Constants.LIST_TYPE)
        delayShowingError = arguments?.getBoolean(Constants.BUNDLE_DELAY_SHOWING_FPE)?:false
        arguments?.getString(Constants.BUNDLE_ADDITIONAL_LOGTAG, "")?.let {
            additionalLogTag = it
        }
        errorLayoutId = arguments?.getInt(Constants.BUNDLE_ERROR_LAYOUT_ID, -1)
        slidingTabId = arguments?.getInt(NewsConstants.BUNDLE_SLIDING_TAB_ID)
        groupInfo = arguments?.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo
        referrerFlow = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW) as? PageReferrer
        referrerFlowParent = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW_PARENT) as? PageReferrer
        enableDivider = arguments?.getBoolean(Constants.CAN_SHOW_ITEM_DECORATION, false) ?: false
        if (CommonUtils.equals(listType, Format.PHOTO.name)) {
            useGrid = true
        }

        searchQuery = arguments?.getSerializable(Constants
                .BUNDLE_SEARCH_QUERY) as? SearchQuery

        entityId = arguments?.getString(Constants.PAGE_ID) ?: "1"
        currentPageReferrer = getCurrentPageReferrer()
        location = location()
        cardsModule = CardsModule(
                CommonUtils.getApplication(),
                SocialDB.instance(),
                entityId,
                "list",
                pageEntityData,
                location,
                arguments?.getString(Constants.LIST_TYPE),
                this,this,lifecycleScope,pageEntityData?.carouselUrl, supportAds, arguments?.getString(NewsConstants.SOURCE_ID),
                arguments?.getString(NewsConstants.SOURCE_TYPE),
                 fragmentId = id.toString(),
                this, clickDelegateProvider,
                section,
                arguments?.getString(Constants.BUNDLE_USER_ID)
                        ?: SSO.getInstance().userDetails.userID ?: Constants.EMPTY_STRING,
                arguments?.getString(Constants.BUNDLE_FILTER) ?: Constants.EMPTY_STRING,
                searchQuery,
                arguments?.getBoolean(Constants.BUNDLE_CLEAR_ON_NO_CONTENT, false) ?: false,
                ::performLogin,
                (arguments?.getSerializable(Constants.BUNDLE_MENU_CLICK_LOCATION) as? MenuLocation)
                        ?: MenuLocation.LIST,
                groupInfo,
                arguments?.getInt(Constants.BUNDLE_CARDS_LIMIT, Integer.MAX_VALUE)
                        ?: Integer.MAX_VALUE,
                arguments?.getBoolean(Constants.DISABLE_NP_CACHE) ?: false,
                arguments?.getBoolean(Constants.DISABLE_FP_CACHE) ?: false,
                arguments?.getBoolean(NewsConstants.BUNDLE_ENABLE_MAX_DURATION_TO_NOT_FETCH_FP, false)
                        ?: false,
                arguments?.getSerializable(Constants.LIST_TRANSFORM_TYPE) as? ListTransformType?
                        ?: ListTransformType.DEFAULT,
                arguments?.getBoolean(Constants.BUNDLE_IS_MY_POSTS_PAGE) ?: false
        )
        DaggerCardsComponent2.builder().cardsModule(cardsModule).build()
                .inject(this)
        arguments?.getString(Constants.ITEM_LOCATION)?.let {
            itemLocation = it
        }
        if (supportAds) {
            adsHelper = adsHelperF.create(uniqueIdentifierId, currentPageReferrer)
        }
        fetchAdsSpec.fetchParentContext()

        vm = ViewModelProviders.of(this, cardsViewModelF)[CardsViewModel::class.java]
        vmFollowUpdate = ViewModelProviders.of(this, followUpdateViewModelF)[FollowUpdateViewModel::class.java]
        fragmentCommunicationsViewModel = ViewModelProviders.of(activity!!).get(FragmentCommunicationsViewModel::class.java)
        vm.setCurrentPageReferrer(currentPageReferrer, referrerFlow,getReferrerProviderListener())
        vm.setUniqueId(uniqueIdentifierId)
        cfCountTracker.event(Lifecycle.State.CREATED,
                entityId, location, section)
        reportAdsMenuListener = ReportAdsMenuFeedbackHelper(this, this)
        adjunctVmFactory = AdjunctLanguageViewModel.AdjunctLanguageViewModelF()
        adjunctLangVm = ViewModelProviders.of(this,adjunctVmFactory).get(AdjunctLanguageViewModel::class.java)

    }

    private fun startObservingChanges() {
        fragmentCommunicationsViewModel.fragmentCommunicationLiveData.observe(viewLifecycleOwner, Observer {
            //todo add hostId check.

            if (it.useCase == Constants.CAROUSEL_LOAD_EXPLICIT_SIGNAL && isGreaterThanCreatedAt(it)) {
                if(cardsModule.isForyouPage()) {
                    val bundle = it.arguments
                    val anyNum = it.anyEnum
                    val sourceAsset =
                        bundle?.getSerializable(Constants.SOURCE_ENTITY) as? CommonAsset
                    followBlockutil.triggerExplicitSignal(anyNum.toString(), sourceAsset)
                }
            }

            if (it.hostId != uniqueIdentifierId) {
                return@Observer
            }
            if (it.useCase == Constants.DELETE_LOCAL_CARD_USECASE
                    && it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                vm.onDialogDiscardLocal(it.arguments)
            }

            if (it.anyEnum == CommonMessageEvents.NEGATIVE_CLICK) {
                val bundle = it.arguments
                val postId = bundle?.getString(Constants.BUNDLE_LOCAL_CARD_ID)?.toLong()
                if (postId != null) {
                    val eventParams = HashMap<NhAnalyticsEventParam, Any>()
                    eventParams[NhAnalyticsNewsEventParam.TYPE] = Constants.LOCAL_CARD_RETRY
                    AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection
                            .APP, eventParams, PageReferrer(NhGenericReferrer.LOCAL_CARD))
                    UploadJobService.retry(postId)
                }
            }
        })

        adjunctLangVm.adjunctResponseLiveData.observe(this, {
            val data = it.getOrNull()
            if(it.isSuccess) {
                data?.let {
                    adjunctLangResponse = it
                }
            }
        })
        adjunctLangVm.getAdjunctLanguageInfo()

        vm.mediatorCardsLiveData.observe(viewLifecycleOwner, Observer { cards ->
            Logger.d(logTag(), "mediatorCardsLiveData :${vm.fpRequestStatus.value}, $cards, waitingdata = ${cards.isWaitingForData()}")
            if (vm.fpRequestStatus.value != false && cards.isWaitingForData()) {
                Logger.e(logTag(), "mediatorCardsLiveData :ignored")
                return@Observer
            }
            if (delayShowingError && cards.dataIsEmptyAndNotYetSeenError()) {
                Logger.e(logTag(), "mediatorCardsLiveData : not showing error untill we get error")
                return@Observer
            }
            if (cards.data?.getList().isNullOrEmpty() && vm.started) {
                val exception = getFilteredErrorFromCompositeError(cards.error)
                        ?: composeListNoContentError()
                val baseError = ApiResponseOperator.getError(exception)
                error.value = baseError
                Logger.e(logTag(), "mediatorCardsLiveData: ERROR ${cards.error} ${cards.data?.getList()?.size}")
                if (errorLayoutId != null && errorLayoutId != -1 && (exception is
                                ListNoContentException || baseError.dbgCode().get() == "BB04" || baseError.status == Constants.ERROR_NO_FEED_ITEMS_BECAUSE_OF_BLOCKED_SOURCES)) {
                    val inflater = LayoutInflater.from(activity)
                    errorView = DataBindingUtil.inflate<ViewDataBinding>(inflater, errorLayoutId!!,
                            socCardsFragBinding.errorParent.root,
                            false)
                    errorView!!.setVariable(BR.vm, vm)
                    errorView!!.setVariable(BR.item, groupInfo)

                    if (parentFragment is UserFollowEntityFragment) {
                        (parentFragment as UserFollowEntityFragment).updateCount(0)
                    }
                } else {
                    val inflater = LayoutInflater.from(activity)
                    errorView = DataBindingUtil.inflate(inflater, R.layout.full_page_error_layout,
                            socCardsFragBinding.errorParent.root, false)
                    errorView!!.setVariable(BR.baseError, error.value)
                    errorView!!.setVariable(BR.vm, vm)

                    errorView!!.lifecycleOwner = this
                    errorView!!.executePendingBindings()
                }
                bindErrorLayout?.invoke(errorView!!)
                socCardsFragBinding.errorParent.root.removeAllViews()
                socCardsFragBinding.errorParent.root.addView(errorView?.root)
                socCardsFragBinding.errorParent.root.visibility = View.VISIBLE
                autoPlayManager?.reset()
                (parentFragment as? CardsExternalListener?)?.onCardsLoadError(true)
                ErrorLogHelper.logErrorEvent(baseError, NhAnalyticsUtility.ErrorViewType.FULLSCREEN,
                NhAnalyticsUtility.ErrorPageType.STORY_LIST, pageEntityData, currentPageReferrer, tabPosition)
                return@Observer
            } else if (cards.data != null && cards.error != null) {
                val exception = getFilteredErrorFromCompositeError(cards.error)
                        ?: composeListNoContentError()
                val baseError = ApiResponseOperator.getError(exception)

                if (cards.tsError ?: 0 > cards.tsData ?: 0) {
                    if (lifecycle.currentState == Lifecycle.State.RESUMED && userVisibleHint) {
                        activity?.findViewById<View>(android.R.id.content)?.let { rootView ->
                            val is204WithFewCards = listFitsTheScreen() && baseError.isNoContentError()
                            val hideSnackbar = hideNoContentSnackbar && (baseError.isNoContentError() || (baseError.status == Constants.ERROR_NO_FEED_ITEMS_BECAUSE_OF_BLOCKED_SOURCES))
                            if (!is204WithFewCards && !hideSnackbar) {
                                ErrorHelperUtils.showErrorSnackbar(baseError, rootView).show()
                                ErrorLogHelper.logErrorEvent(
                                    baseError,
                                    NhAnalyticsUtility.ErrorViewType.SNACKBAR,
                                    NhAnalyticsUtility.ErrorPageType.STORY_LIST,
                                    pageEntityData,
                                    currentPageReferrer,
                                    tabPosition
                                )
                            }
                        }
                    }
                } else if (cards.tsData ?: 0 > cards.tsError ?: 0) {
                    socCardsFragBinding.errorParent.root.visibility = View.GONE
                    errorView?.let {
                        socCardsFragBinding.errorParent.root.removeView(it.root)
                    }
                    if(visibilityCalculator?.isStarted() == false ||
                                autoPlayManager?.isStarted() == false) {
                        resumeVisibilityCalculation()
                    }
                }
            } else {
                error.value = null
                socCardsFragBinding.errorParent.root.visibility = View.GONE
                errorView?.let {
                    socCardsFragBinding.errorParent.root.removeView(it.root)
                }
                if(visibilityCalculator?.isStarted() == false ||
                            autoPlayManager?.isStarted() == false) {
                    resumeVisibilityCalculation()
                }
            }
            Logger.d(logTag(), "mediatorCardsLiveData: Got response updating list : ${cards.data?.getList()?.size}")
            fireSLVFor1stResponseFromDB(cards)
            val currList = cards.data?.getSnapshot()
            adapterDirty = hasListChangedStructurally(cards.data?.getSnapshot(), cardsAdapter.getSnapshot())
            val currentList = cards?.data?.getList()
            val firstposition = linLayoutManager.findFirstVisibleItemPosition()
            (parentFragment as? CardsExternalListener?)?.onCardsLoaded()
            vm.fetchNudges(currentList)
            cardsAdapter.submitList(currentList, Runnable {
                if (!doesPrevListHasLocalCard && CardsBindUtils.hasLocalInfo(currList?.getOrNull
                        (0)) && firstposition <= 1) {
                    socCardsFragBinding.recycler.smoothScrollToPosition(0)
                }
                doesPrevListHasLocalCard = CardsBindUtils.hasLocalInfo(currList?.getOrNull(0))
            }, isFirstPageData)
            if( tabPosition == 0 && canInsertLanguageSelectionCard && section == PageSection.NEWS.section) {
                PreferenceManager.savePreference(GenericAppStatePreference.IS_FY_LOAD_SUCCESS,true)
            }
            isFirstPageData = false
            scheduleAdapterDirtyReset()
            adsHelper?.setTickerAvailability(cardsAdapter.getSnapshot())
            if (adsHelper?.isPP1AdsInserted == false) {
                val prevInsertedAd = currentList?.find { it -> it is CommonAsset && it.i_adId() == adsHelper?.getProcessingAdId() }
                prevInsertedAd?.let {
                    if (it is CommonAsset) {
                        it.i_adId()?.let {
                            adsHelper?.insertedPP1IdList?.add(it)
                        }
                    }
                }
            }
            if (userVisibleHint) {
                adsHelper?.insertP0AdinList(cardsAdapter.itemCount)
                adsHelper?.tryinsertPP1Ads(cardsAdapter.itemCount)
            }
        })

        vm.fpRequestStatus.observe(viewLifecycleOwner, Observer { inProgress: Boolean? ->
            socCardsFragBinding.swipeRefreshLayout.isRefreshing = inProgress ?: false
            if (inProgress != true) {
                checkVisibilityWithDelayAndLoadPosts()
            }
        })

        vm.npStatus.observe(viewLifecycleOwner, Observer {
            //      cardsAdapter show/hide footer
            cardsAdapter.showFooter(it)
            if (isEndReached) {
                if (arguments?.getBoolean(Constants.BUNDLE_SHOW_GUEST_FOOTER) == true) {
                    cardsAdapter.showGuestUserFooter(arguments?.getLong(Constants.BUNDLE_FOLLOWERS_COUNT)
                            ?: 0L)
                }
                isEndReached = false
            }
        })

        vm.fpStatus.observe(viewLifecycleOwner, Observer {
            Logger.d(logTag(), "startObservingChanges:fpStatus: $it ")
            if (it == View.GONE) {
                runCatching {
                    val rootView = socCardsFragBinding.root.parent as? ViewGroup
                    val shimmer = rootView?.findViewById<ConstraintLayout>(R.id.cards_list_shimmer_parent)
                    val parent = shimmer?.parent as? ViewGroup
                    if (shimmer != null && parent != null) {
                        parent.removeView(shimmer)
                        Logger.d(logTag(), "startObservingChanges: removed")
                    }
                }
            } else if (it == View.VISIBLE) {
                val shimmer = (socCardsFragBinding.root.parent as? ViewGroup)?.findViewById<ConstraintLayout>(R.id.cards_list_shimmer_parent)
                if (activity != null && ::socCardsFragBinding.isInitialized && shimmer == null) {
                    Logger.d(logTag(), "startObservingChanges: creating ")
                    DataBindingUtil.inflate<ListShimmerLayoutBindingImpl>(LayoutInflater.from(activity), R.layout.list_shimmer_layout, socCardsFragBinding.listParent, true)
                }
            }
        })

        vm.npData.observe(viewLifecycleOwner, Observer {
            it?.let {
                canInsertLanguageSelectionCard = false
                logStoryListViewEvent(false, it)
                requestAdsForVideo(it)
            }
            if (it != null) {
                isFirstPageData = false
            }

            if (it == null || CommonUtils.isEmpty(it.rows)) {
                Logger.e(logTag(), "End of the list is reached")
                isEndReached = true
            }
        })

        vm.fpData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                isFirstPageData = true
                /*val list = cardsAdapter.getPagedListWrapper()?.clone()
                list?.removeAllExtraItem()
                cardsAdapter.submitList(list)*/
                Logger.d(logTag(), "First page response is received followers counts is ${it.nlResp.entityCount}")
                arguments?.putLong(Constants.BUNDLE_FOLLOWERS_COUNT, it.nlResp.entityCount)
	            if (parentFragment is UserFollowEntityFragment) {
		            (parentFragment as? UserFollowEntityFragment)?.updateCount(it.nlResp.entityCount)
	            } else if (parentFragment is ImportFollowFragment) {
                    (parentFragment as? ImportFollowFragment)?.markImportSuccess()
                }
            }
        })

        vm.currentPageInfoLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess) {
                val entity = it.getOrNull()
                entity?.let { pageEntity ->
                    if (this.pageEntity == null) {
                        this.pageEntity = pageEntity
                        Logger.d(logTag(), "page entity is not initialized, initialized")
                        return@Observer
                    } else if (this.pageEntity != pageEntity) {
                        Logger.d(logTag(), "page entity changed, refresh the tab")
                        this.pageEntity = pageEntity
                        videoIndex = 0
                        vm.pullToRefresh()
                    }
                }
            }
        })

        vm.firstpageData.observe(viewLifecycleOwner, Observer {
            it?.let{
                canInsertLanguageSelectionCard = it.nlResp.isFromNetwork
                adapterDirty = true
                if (it.nlResp.isFromNetwork) {
                    checkAndShowMoreNews()
                } else {
                    replaceAndLogevent()
                }
                adsHelper?.onFPResponse(it.nlResp, userVisibleHint)
                nccHelper.onFPResponse(it.nlResp)
                val adjunctLang = it.nlResp.stickyBannerLang
                if(adjunctLang != null && isVisible && activity is HomeActivity && !LangInfoRepo.isUserOrSystemOrBlackListedLanguage(adjunctLang)) {
                    bindStickyLangBanner(adjunctLang)
                } else {
                    socCardsFragBinding.stickyAdjunctLangBanner.visibility = View.GONE
                }
                scheduleAdapterDirtyReset()
            }
        })

        vm.approvalLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isFailure) {
                activity?.findViewById<View>(android.R.id.content)?.let { rootView ->
                    ErrorHelperUtils.showErrorSnackbar(it.exceptionOrNull(), rootView)
                }
            }
        })

        vm.joinGroupUseCaseLD.observe(viewLifecycleOwner, Observer {
            if (it.isFailure) {
                activity?.findViewById<View>(android.R.id.content)?.let { rootView ->
                    ErrorHelperUtils.showErrorSnackbar(it.exceptionOrNull(), rootView)
                }
            } else {
                val groupInfo = it.getOrNull()
                activity?.findViewById<View>(android.R.id.content)?.let { rootView ->
                    if (groupInfo?.memberApproval == SettingState.NOT_REQUIRED) {
                        GenericCustomSnackBar.showSnackBar(rootView, rootView.context,
                                CommonUtils.getString(R.string.group_joined, groupInfo.name),
                                Snackbar.LENGTH_LONG).show()
                    } else {
                        GenericCustomSnackBar.showSnackBar(rootView, rootView.context,
                                CommonUtils.getString(R.string.request_sent, groupInfo?.name),
                                Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })

        SlidingTabLayout.tabClickEventLiveData.postValue(null)
        SlidingTabLayout.tabClickEventLiveData.observe(viewLifecycleOwner, Observer {
            handleTabEvent(it)
        })
        CardClickEventHelper.reposts.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                repostClicked(it)
            }
        })

        NavigationHelper.autoplayInDetail.observe(viewLifecycleOwner, Observer {
            isAutoplayClickToDetial = it
        })

        VideoHelper.menuStateLiveData.observe(viewLifecycleOwner, Observer {
            activity?.runOnUiThread { autoPlayManager?.setMenuState(it.isShowing, it.isHideCard) }
        })

        vm.nudges.observe(this, Observer{
            nudges = it?: hashMapOf()
            onNudgeEvent()
        })

    }

    private fun isGreaterThanCreatedAt(fragmentCommunicationEvent: FragmentCommunicationEvent): Boolean {
        val ts = fragmentCommunicationEvent.arguments?.get(Constants.EVENT_CREATED_AT) as? Long
        return ((ts != null) && (ts > createdAt))
     }

    fun initColdSignalListner() {
        followBlockutil.coldSignalLiveData.observe(viewLifecycleOwner, {
            if (it) {
                setNonLinearCardsForColdSignalFollow()
            }

        })
        followBlockutil.initColdSignal()

    }

    private fun initExplicitSignalListner() {
        followBlockutil.initExplicitSignalListner()
        followBlockutil.explictSignalLiveData.observe(viewLifecycleOwner, {
            triggerExplicitUsecase(it?.action,it?.data)
        })
    }

    private fun triggerExplicitUsecase(action: String?, commonAsset: CommonAsset?) {
        commonAsset?.i_source()?.let { it ->
            if (action != null) {
                val layoutManager = socCardsFragBinding.recycler.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    explicitSignalPosition = layoutManager.findLastVisibleItemPosition()
                }
                setNonLinearCardsforExplicitSignal(it, action)
            }
        }
    }


    fun setNonLinearCardsforExplicitSignal(source: PostSourceAsset, action: String) {

        var followBlockRequest = CardsPayload.FollowBlockRequest(
            source.id,
            source.displayName,
            source.entityType,
            source.type,
            Constants.FOLLOW
        )
        val queryUrl = if (action == FollowActionType.FOLLOW.name) Constants.FOLLOW_RECOMMENED_URL
        else
            Constants.BLOCK_RECOMMENED_URL

        vm.cardsAdapterSize.observe(viewLifecycleOwner, {

            val layoutManager = socCardsFragBinding.recycler.layoutManager
            if (layoutManager is LinearLayoutManager) {
                val position = max(explicitSignalPosition,explicitSignalCardPosition)
                if (it > (position) && !isExplicitSignalCarouselAPITrig) {
                    isExplicitSignalCarouselAPITrig = true
                    vm.updateNLFCAsset(
                        cardsAdapter.getItem(position) as? CommonAsset,
                        position,
                        cardsAdapter.getItemIdBeforeIndex(position)
                    )
                    vm.getNonLinearFeedCardForExplicitSignals(
                        url = NewsBaseUrlContainer.getApplicationUrl() + queryUrl,
                        id = id.toString(),
                        followBlockRequest
                    )
                    val currentTime = System.currentTimeMillis()
                    if (action == FollowActionType.FOLLOW.name) {
                        FollowBlockPrefUtil.setExplicitFollowlastShownTimestamp(currentTime)
                    }
                    else {
                        FollowBlockPrefUtil.setExplicitBlocklastShownTimestamp(currentTime)
                    }

                }

            }
        })

    }

    private fun handleColdStartNonLinearItems() {
        try {
            isColdSignalCarousalShown = true
            if (nonLinearCardList?.isNotEmpty() == true) {
                val id = cardsAdapter.getItemIdBeforeIndexExEmpAd(coldSignalCardPosition)
                id?.let {
                    vm.insertNonLinear(nlfcItem = nonLinearCardList!![0], id = it)
                }
            }
        } finally {
            NonLinearStore.deleteStories()
            vm.cleanUpNonLinear()
        }
    }

    fun setNonLinearCardsForColdSignalFollow() {

        vm.cardsAdapterSize.observe(viewLifecycleOwner, {
            val layoutManager = socCardsFragBinding.recycler.layoutManager
            if (layoutManager is LinearLayoutManager) {
                if (it > coldSignalCardPosition && !isColdSignalCarouselAPITrig) {
                    isColdSignalCarouselAPITrig = true
                    vm.updateNLFCAsset(
                        cardsAdapter.getItem(coldSignalCardPosition) as? CommonAsset,
                        coldSignalCardPosition,
                        cardsAdapter.getItemIdBeforeIndex(coldSignalCardPosition)
                    )
                    vm.getNonLinearFeedCardForExplicitSignals(
                        url = NewsBaseUrlContainer.getApplicationUrl() + Constants.FOLLOW_RECOMMENED_URL,
                        id = id.toString(),
                        null

                    )
                }
            }
        })
    }


    /**
     * finds visible viewholders. check data of each on them and try to show a nudge.
     * If atleast one is shown, it will abort and refresh nudges
     */
    private fun onNudgeEvent() {
        // show nudge only when list is settled.
        if(socCardsFragBinding.recycler.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            return
        }
        val n = nudges?.filterValues { it != null } ?: return
        val first = linLayoutManager.findFirstVisibleItemPosition()
        val last = linLayoutManager.findLastVisibleItemPosition()
        if(!::socCardsFragBinding.isInitialized) return
        val r = Rect()
        socCardsFragBinding.recycler.getGlobalVisibleRect(r)
        val nudgeViewHolders = (first..last)
                .mapNotNull { socCardsFragBinding.recycler.findViewHolderForAdapterPosition(it) as? CommonAssetViewHolder }
        Logger.d(logTag(), "onNudgeEvent: Got ${n.size} nudges: ${n.keys}, fst=$first, lst=$last, vhsize=${nudgeViewHolders.size}")
        for (nudgeVh in nudgeViewHolders) {
            val (view, commonAsset) = nudgeVh.curData()
            val shownNudge = nudgeHelper.showNudge(n, view, commonAsset, r) {nudge,  showing ->
                Logger.d(logTag(), "onNudgeEvent: showNudge: $showing")
                onHiddenChanged(showing)
                if(showing) {
                    vm.markNudgeShown(nudge)
                    vm.fetchNudges(cardsAdapter.getSnapshot())
                }
            }
            if(shownNudge != null) break
        }
    }

    override fun getWebView(key: String): WeakReference<MASTAdView>? {
        return webViewCache[key]
    }

    override fun putWebView(key: String, value: WeakReference<MASTAdView>?) {
        //Lazy Initializing
        webViewCache[key] = value
    }

    private fun hasListChangedStructurally(oldSnap: List<Any?>?, newSnap: List<Any?>?): Boolean {
        oldSnap ?: return false
        newSnap ?: return false
        return if (oldSnap.size != newSnap.size) {
            true
        } else {
            val ids = ArrayList<String?>()
            oldSnap.forEach {
                ids.add(if (it is CommonAsset) it.i_id() else null)
            }
            newSnap.forEach {
                ids.remove(if (it is CommonAsset) it.i_id() else null)
            }
            ids.isNotEmpty()
        }
    }

    private fun getFilteredErrorFromCompositeError(error: Throwable?): Throwable? {
        error ?: return null
        val baseError = error as? BaseError
        val originalError = baseError?.originalError ?: error
        if (originalError is CompositeException && originalError.exceptions != null) {
            return originalError.exceptions?.find {
                (it as? BaseError)?.dbgCode() !is DbgCode.DbgNotFoundInCache
            }
        }
        if (baseError.dbgCode() is DbgCode.DbgNotFoundInCache)
            return null
        return error
    }

    fun performLogin(showToast: Boolean, toastMsgId: Int) {
        /*if (showToast && toastMsgId > 0) {
            showToast(CommonUtils.getString(toastMsgId), null)
        }*/
        activity?.let {
            val sso = SSO.getInstance()
            sso.login(it as Activity, LoginMode.USER_EXPLICIT, SSOLoginSourceType.REVIEW, getReferrerProviderListener())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.fragmentBundle = arguments
        vmFollowUpdate.triggerMinCardPosition()
        vmFollowUpdate.triggerCardPosition()
        vmFollowUpdate.getCardPostionLiveData.observe(viewLifecycleOwner, {
            coldSignalCardPosition = it
        })
        followBlockutil = FollowBlockSignalUtils(vmFollowUpdate,viewLifecycleOwner)
        vmFollowUpdate.getMinCardPostionLiveData.observe(viewLifecycleOwner, {
               explicitSignalCardPosition = it
           })
        if (cardsModule.isForyouPage()) {
            initColdSignalListner()
            initExplicitSignalListner()
        }

        vm.nonLinearFeedLiveData.observe(viewLifecycleOwner, Observer {

            if (it.isSuccess && isVisible) {
                Logger.d(Constants.NON_LINEAR_FEED, "Got the callback for the nlfc cards for position $tabPosition")
                nonLinearCardList = it.getOrNull()
                if (linLayoutManager.itemCount >= coldSignalCardPosition && !isColdSignalCarousalShown && isColdSignalCarouselAPITrig) {
                    handleColdStartNonLinearItems()
                    return@Observer
                }
                if(nonLinearCardList?.isEmpty() == false) {
                     insertNonLinearCards()
                }
            }
        })
        observeDownloadStart()
        startObservingChanges()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        socCardsFragBinding = DataBindingUtil.inflate(inflater, R.layout.soc_cards_frag, container, false)
        socCardsFragBinding.vm = this.vm
        socCardsFragBinding.lifecycleOwner = this
        NewsListCardLayoutUtil.manageLayoutDirection(socCardsFragBinding.root)

        if (useGrid) {
            linLayoutManager = GridLayoutManager(context, 2)
            val marginLayoutParams = socCardsFragBinding.recycler.layoutParams as ViewGroup.MarginLayoutParams
            marginLayoutParams.setMargins(CommonUtils.getDimensionInDp(R.dimen.grid_view_padding),
                    CommonUtils.getDimensionInDp(R.dimen.grid_view_padding_top),
                    CommonUtils.getDimensionInDp(R.dimen.grid_view_padding), 0)
        } else {
            linLayoutManager = NoPredAnimLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        // creating autoplayManager first so as to receive onScroll event after Visibility
        // calculations. (It is sent in LIFO order)
        autoPlayManager = AutoPlayManager(socCardsFragBinding.recycler, linLayoutManager)
        autoPlayManager?.registerLifecycle(viewLifecycleOwner)

        visibilityCalculator = VisibilityCalculator(socCardsFragBinding.recycler,
         linLayoutManager)

        videoRequester?.setPageEntity(pageEntityData)


        socCardsFragBinding.swipeRefreshLayout.setOnRefreshListener {
            val diff = System.currentTimeMillis() - (lastRefreshTime ?: 0)
            if (diff > 5000) {
                lastRefreshTime = System.currentTimeMillis()
                videoIndex = 0
                vm.pullToRefresh()
            } else {
                socCardsFragBinding.swipeRefreshLayout.isRefreshing = false
            }
        }

        socCardsFragBinding.recycler.doOnLayout { view ->
            Logger.d(logTag(), "onCreateView: doonlayout: ${view.width} ${view.height}")
            if (::cardsAdapter.isInitialized) {
                val aw = if(view.width == 0) CommonUtils.getDeviceScreenWidth() else view.width
                val ah = if(view.height==0) CommonUtils.getDeviceScreenHeight() else view.height
                cardsAdapter.availableWidth = aw - (2 * CommonUtils.getDimension(R.dimen.story_card_padding_left))
                cardsAdapter.availableHeight = ah
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(this)

        val eventDedupHelper = EventDedupHelper(mapOf(Constants.BUNDLE_ENTITY_ID to entityId,
                Constants.BUNDLE_LOCATION_ID to location,
                NewsConstants.DH_SECTION to section))
        cardsAdapter = CardsAdapter(context = context,
                cardsViewModel = vm,
                parentLifeCycle = viewLifecycleOwner, videoRequester = videoRequester,
                autoPlayManager = this.autoPlayManager, listEditInterface = this,
                itemLocation = itemLocation, tabType = listType, uniqueRequestId = uniqueIdentifierId,
                deeplinkUrl = pageEntityData?.deeplinkUrl,
                adEntityReplaceHandler = this, webCacheProvider = this,
                pageReferrer = currentPageReferrer, section = section,
                showAddPageButton = /*(arguments?.getBoolean(Constants.SHOW_ADD_BUTTON_FOR_ENTITY)
                        ?: false)*/false,
                nhJsInterfaceWithMenuClickHandling =
                nhJsInterfaceWithMenuClickHandling,
                tickerHelper3 = tickerHelper3,
                eventDedupHelper = eventDedupHelper,
                referrerProviderlistener = getReferrerProviderListener(),
                reportAdsMenuListener = reportAdsMenuListener)
        lifecycle.addObserver(eventDedupHelper)
        adapterObserver = AdapterChangeObserver {
            if (adapterDirty && cardsAdapter.itemCount > 0) {
                adapterDirty = false
                adsHelper?.insertP0AdinList(cardsAdapter.itemCount)
                adsHelper?.tryinsertPP1Ads(cardsAdapter.itemCount)
            }
        }
        cardsAdapter.registerAdapterDataObserver(adapterObserver)
        with(socCardsFragBinding.recycler) {
            val horzPadding = arguments?.getInt(Constants.LIST_HORZ_PADDING, 0) ?: 0
            setPadding(horzPadding, 0, horzPadding, 0)
            adapter = cardsAdapter
            layoutManager = linLayoutManager

            val activityManager = CommonUtils.getApplication().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (!activityManager.isLowRamDevice) {
                val preloader = RecyclerViewPreloader(Glide.with(CommonUtils.getApplication()), cardsAdapter,
                        cardsAdapter,
                        Constants.PREFETCH_IMAGE_COUNT)
                this.addOnScrollListener(preloader)
            }

            if (!useGrid && enableDivider) {
                val defaultDividerHeight = CommonUtils.getDimension(R.dimen.divider_height)
                val decoration = SimpleItemDecorator(0,
                        arguments?.getInt(Constants.LIST_DIVIDER_HEIGHT, defaultDividerHeight)
                                ?: defaultDividerHeight,
                        ContextCompat.getDrawable(context, R.drawable.card_list_recycler_view_divider))
                addItemDecoration(decoration)
            }
            addOnScrollListener(scrollListener)
        }

        recyclerViewLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                socCardsFragBinding.recycler.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                // TODO(satosh.dhanyamraju): check for leaks. Below line is causing NPE.
                //recyclerViewLayoutListener = null
                if (!isAutoplayClickToDetial && !isStopped && userVisibleHint
                        && !socCardsFragBinding.errorParent.root.isShown) {
                    visibilityCalculator?.update()
                    autoPlayManager?.start()
                    cardsAdapter?.startVideoPrefetch()
                }
            }
        }
        socCardsFragBinding.recycler.postDelayed(Runnable { checkVisibilityCalculation() }, 2000)
        socCardsFragBinding.swipeRefreshLayout.isEnabled = arguments?.getBoolean(Constants.CARDS_FRAG_DISABLE_PULL_TO_REFRESH, false)?.not()
                ?: true
        socCardsFragBinding.executePendingBindings()
        return socCardsFragBinding.root
    }

    override fun onStart() {
        super.onStart()
        if (userVisibleHint) {
            adsHelper?.start()
        }
    }

    override fun onStop() {
        adsHelper?.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        startVMIfVisibleAndResumed()
        if(isVisible && activity is HomeActivity) {
            if (ThemeUtils.themeAutoSwitchSnackbarNeededInList()) {
                val snackbarView = socCardsFragBinding.snackbarContainer
                AndroidUtils.getMainThreadHandler().postDelayed({
                ThemeUtils.showThemeSnackbar(snackbarView, Constants.THEME_SNACKBAR_LIST,PageReferrer(NewsReferrer.HASHTAG))
                ThemeUtils.setThemePreferences(false, ThemeUtils.themeAutoSwitchSnackbarNeededInDetail(), ThemeUtils.themeAutoSwitchToastNeededInList(), ThemeUtils.themeAutoSwitchToastNeededInDetail())}, 2000)
            } else if (ThemeUtils.themeAutoSwitchToastNeededInList()) {
                ThemeUtils.showThemeToast()
                ThemeUtils.setThemePreferences(ThemeUtils.themeAutoSwitchSnackbarNeededInList(), ThemeUtils.themeAutoSwitchSnackbarNeededInDetail(), false, ThemeUtils.themeAutoSwitchToastNeededInDetail())
            }
        }
        activity?.registerReceiver(downloadReceiver, IntentFilter(DownloadManager
                .ACTION_DOWNLOAD_COMPLETE))
        if (isVisible && parentFragment?.isHidden == true) {
            view?.post { onHiddenChanged(true) }
        }
        if (userVisibleHint) {
            startTimespentTimer()
        }

    }

    private fun startVMIfVisibleAndResumed() {
        val resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (resumed && userVisibleHint) {
            Logger.v(logTag(), "started VM")
            vm.start()
        } else
            Logger.v(logTag(), "Not started VM, r=$resumed, v=$userVisibleHint")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (view == null || activity == null) {
            Logger.d(logTag(), "setUserVisibleHint view is NULL")
            return
        }
        if (isVisibleToUser) {
            resumeVisibilityCalculation()
            startTimespentTimer()
        } else {
            pauseVisibilityCalculation()
            logTimeSpentEvent(NhAnalyticsUserAction.SWIPE)
            cardsAdapter?.clearCachedItems()
        }
    }

    override fun isViewVisible(): Boolean {
        return userVisibleHint
    }

    override fun getTotalItems(): Int? {
        return if (adapterDirty) null else cardsAdapter.itemCount
    }

    override fun getItemIdBeforeIndex(position: Int): String? {
        return if (adapterDirty) null else cardsAdapter.getItemIdBeforeIndex(position)
    }

    override fun validateAndGetPosition(nccPosition: Int, distancingSpec: DistancingSpec?): Int {
        return if (adapterDirty) -1 else cardsAdapter.validateAndGetPosition(nccPosition, distancingSpec)
    }

    override fun insertAdInList(baseAdEntity: BaseAdEntity, adapterPos: Int): Boolean {
        if (adapterDirty) return false

        baseAdEntity.parentIds.add(uniqueIdentifierId)
        with(socCardsFragBinding.recycler) {
            AndroidUtils.getMainThreadHandler().postDelayed({
                viewTreeObserver.addOnGlobalLayoutListener(recyclerViewLayoutListener)
                val first = (layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: -1
                if (adapterPos == first || adapterPos == (first - 1) || (first == -1 && adapterPos == 0)) {
                    scrollToPosition(adapterPos)
                }
            }, 100)
        }
        return cardsAdapter.insertAd(baseAdEntity, adapterPos)
    }

    override fun getActivityContext(): Activity? {
        return activity
    }

    override fun goToEditMode() {
        handleEditModeChange(true)
    }

    override fun onEditModeDone() {
        handleEditModeChange(false)
    }

    override fun onEditModeCancel() {
        handleEditModeChange(false)
    }

    override fun clearAllInList() {
        refresh()
    }

    override fun isInEditMode(): Boolean {
        return editModeOn
    }

    private fun handleEditModeChange(editMode: Boolean) {
        editModeOn = editMode
        vm.enableNpUsecase = !editMode
        socCardsFragBinding.swipeRefreshLayout.isEnabled = !editMode
        cardsAdapter.notifyDataSetChanged()
    }

    companion object {
        private fun log(message: String) {
            Logger.d("CardsFragment", message)
        }

        fun create(bundle: Bundle, videoRequester: VideoRequester? = null, bindErrorFunction: ((ViewDataBinding) -> Unit)? = null):
                CardsFragment {
            val fragment = CardsFragment()
            fragment.apply { arguments = bundle }
            fragment.videoRequester = videoRequester
            fragment.bindErrorLayout = bindErrorFunction
            return fragment
        }

        private const val SMOOTH_SCROLL_JUMP_POSITION: Int = 10
        const val DISABLE_MORE_NEWS_TOOLITP = "disable_more_news_toolitp"
        private const val LOG_TAG_SCROLLER = "CardScroller"
        private const val TIME_DELAY_FOR_LIST_UPDATE: Long = 1000L //Delay is to wait for list to
        // update new items.
    }

    private fun insertNonLinearCards() {

        try {
            if (CommonUtils.isEmpty(nonLinearCardList)) {
                return
            }

            val list = socCardsFragBinding.recycler
            val layoutManager = list.layoutManager
            if (layoutManager is LinearLayoutManager) {

                val index = layoutManager.findFirstVisibleItemPosition()
                Logger.i(Constants.NON_LINEAR_FEED, "Visible index = $index")
                val lastIndex = layoutManager.findLastVisibleItemPosition()
                var id: String? = null

                val position = max(explicitSignalPosition,explicitSignalCardPosition)

                if (linLayoutManager.itemCount >= (position) && !isExplicitSignalCarousalShown && isExplicitSignalCarouselAPITrig) {
                    isExplicitSignalCarousalShown = true
                    var id = cardsAdapter.getItemIdBeforeIndex(position)
                    id?.let {
                        vm.insertNonLinear(nlfcItem = nonLinearCardList!![0], id = it)
                    }
                } else if (index >= lastIndex) {
                    id = cardsAdapter.getItemIdBeforeIndex(index + 1)
                    id?.let {
                        vm.insertNonLinear(nlfcItem = nonLinearCardList!![0], id = it)

                    }
                } else {
                     val nlfcItem = nonLinearCardList!![0]
                     for (i in index..lastIndex) {
                         val postId = cardsAdapter.getItemIdBeforeIndex(i + 1)
                         if (nlfcItem.parentPostId == postId) {
                             id = postId
                         }
                     }
                     if (id == null) {
                         id = cardsAdapter.getItemIdBeforeIndex(index + 1)
                     }
                     id?.let {
                         vm.insertNonLinear(nlfcItem = nonLinearCardList!![0], id = it)

                     }
                }
            }
        } finally {
            NonLinearStore.deleteStories()
            vm.cleanUpNonLinear()
        }
    }

    fun nextCardForLocalCard() : Serializable? {
        if (!::cardsModule.isInitialized) {
            Logger.d(logTag(), "nextCardForLocalCard: cardsmodule not initialized")
            return null
        }
        val nextCard = nextCardForVisibleAreaInsertion()
        return LocalInfo(
                pageId = entityId,
                location = location,
                section = section,
                nextCardId = nextCard,
                isCreatedFromMyPosts = cardsModule.isMyPostsPage(),
                creationDate = System.currentTimeMillis()
        )
    }

    fun nextCardForVisibleAreaInsertion(): String? {
        val recyclerView = socCardsFragBinding.recycler
        val layoutManager = recyclerView.layoutManager
        val nextCard = when {
            cardsModule.isForyouPage().not() -> {
                Logger.d(logTag(), "nextCardForLocalCard: not a foryou tab")
                null
            }
            layoutManager is LinearLayoutManager -> {
                val completelyVisibleindex = layoutManager.findFirstCompletelyVisibleItemPosition()
                // it is possible that there a big card which can't be shown completely,
                // or 2 cards - neither of which is completely shown. In this case, insert at lastVisible
                val insertIndex = if (completelyVisibleindex != -1) completelyVisibleindex
                else layoutManager.findLastVisibleItemPosition()
                val id=  (cardsAdapter.getSnapshot()?.getOrNull(insertIndex) as? CommonAsset)?.i_id()
                Logger.d(logTag(), "nextCardForLocalCard: completely visible index $insertIndex, id=$id")
                id
            }
            else -> {
                Logger.e(logTag(), "nextCardForLocalCard: not LinearLayoutManager")
                null
            }
        }
        return nextCard
    }

    private fun repostClicked(item: CommonAsset) {
        val nextItemID = if (cardsModule.isForyouPage()) {
            nextCardForVisibleAreaInsertion() ?: item.i_id()
        } else null
        val local = LocalInfo(
                pageId = entityId,
                location = location,
                section = section,
                nextCardId = nextItemID,
                isCreatedFromMyPosts = cardsModule.isMyPostsPage(),
                creationDate = System.currentTimeMillis()
        )
        val repostIntent = CommonNavigator.getPostCreationIntent(
                item.i_id(), CreatePostUiMode.REPOST, null, getCurrentPageReferrer(),
                local, item.i_source()?.id, item.i_source()?.type, item.i_parentPostId(),groupInfo)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(repostIntent))
    }

    var bindErrorLayout: ((ViewDataBinding) -> Unit)? = null

    private fun getCurrentPageReferrer(): PageReferrer {
        return arguments?.getSerializable(Constants.REFERRER) as? PageReferrer? ?: run {
            when {
                section == PageSection.FOLLOW.section -> PageReferrer(NewsReferrer.FOLLOW_STAR_SECTION)
                section == PageSection.PROFILE.section -> PageReferrer(ProfileReferrer.PROFILE)
                section == PageSection.GROUP.section -> PageReferrer(NhGenericReferrer.GROUP_HOME,
                        groupInfo?.id)
                section == PageSection.SEARCH.section -> PageReferrer(NewsReferrer.SEARCH,
                        pageEntityData?.viewOrder.toString())
                pageEntityData == null -> PageReferrer(NewsReferrer.HEADLINES, null)
                else -> when (pageEntityData?.entityType) {
                    PageType.HASHTAG.pageType -> {
                        PageReferrer(NewsReferrer.HASHTAG, pageEntityData?.id)
                    }
                    PageType.SOURCE.pageType, PageType.SOURCECAT.pageType -> {
                        PageReferrer(NewsReferrer.CATEGORY, pageEntityData?.id)
                    }
                    PageType.LOCATION.pageType -> {
                        PageReferrer(NewsReferrer.LOCATION, pageEntityData?.id)
                    }
                    else -> {
                        PageReferrer(NewsReferrer.HEADLINES, null)
                    }
                }
            }
        }
    }

    fun startTimespentTimer() {
        if (startTime == -1L) {
            startTime = SystemClock.elapsedRealtime()
        }
    }

    override fun logTimeSpentEvent(exitAction: NhAnalyticsUserAction) {
        val startTimespent = startTime
        if (startTimespent != -1L) {
            val referrerProviderlistener = if (parentFragment is ReferrerProviderlistener)
                parentFragment as ReferrerProviderlistener?
            else
                null
            val providedReferrer = referrerProviderlistener?.providedReferrer
            val listSection = NewsAnalyticsHelper.getReferrerEventSectionFrom(referrerProviderlistener)

            AnalyticsHelper2.logStoryListTimeSpentEvent(
                    pageEntity = pageEntityData,
                    currentPageReferrer = currentPageReferrer,
                    providedReferrer = providedReferrer,
                    tabIndex = tabPosition,
                    startTime = startTimespent,
                    pvActivity = NhAnalyticsPVType.STORY_LIST,
                    exitAction = exitAction,
                    section = listSection,
                    fetchDao = fetchDao,
                    sectionId = section,
                    referrerFlow = getReferrerProviderListener()?.providedReferrer
            )
            startTime = -1
        }
    }


    fun logStoryListViewEvent(isFirstPage: Boolean, nlResp: NLResp) {
        Logger.d(logTag(), "logStoryListViewEvent() called with: isFirstPage = $isFirstPage, fromNet = ${nlResp.isFromNetwork}, took=${nlResp.timeTakenToFetch}")
        val isFromCache = nlResp.isFromNetwork.not()
        if (nlResp.rows.size <= 0) {
            //Not to log story list view event for empty list.
            // bug fix : https://bugzilla.newshunt.com/eterno/show_bug.cgi?id=27339
            return
        }

        if (parentFragment is FollowingFilterCallback) {
            (parentFragment as FollowingFilterCallback).logEntityListViewEvent()
            return
        }

        // TODO(satosh.dhanyamraju): check all parentFragmnents are implementing it. EntityInfo
        //  is not.
        if (parentFragment !is ReferrerProviderlistener && activity !is ReferrerProviderlistener) {
            return
        }

        val referrerProviderlistener = getReferrerProviderListener()

        val listSection = NewsAnalyticsHelper.getReferrerEventSectionFrom(referrerProviderlistener)

        val pageNumber = nlResp.pageNumber

        val referrer = if (isFirstPage)
            referrerProviderlistener?.providedReferrer ?: currentPageReferrer
        else
            currentPageReferrer

        val selectedReferrerFlow = if (isFirstPage)
            referrerFlowParent
        else
            referrerFlow

        if(listType == Format.MEMBER.name || listType == Format.GROUP_INVITE.name) {
            listType?.let { type ->
                referrer?.let {
                    val tabType = arguments?.getString(Constants.LIST_TAB_TYPE, type) ?: type
                    AnalyticsHelper2.logEntityListViewForMemberLists(tabType, it, groupInfo)
                }
            }
            return
        }

        if (listType == Format.ENTITY.name && section == PageSection.SEARCH.section) {
            AnalyticsHelper2.logSearchEntityListView(referrer, section, pageEntityData, selectedReferrerFlow)
        }

        if (!(listType == null || listType == Format.PHOTO.name || listType == Constants.LIST_TYPE_BOOKMARKS)) {
            // Don't fire STORY_LIST_VIEW for non-news lists
            return
        }

        if (!isFirstPage) {
            referrer?.referrerAction = NhAnalyticsUserAction.SCROLL
        }

        val section = listSection ?: referrerProviderlistener!!.referrerEventSection
        // TODO(satosh.dhanyamraju): need viral section?

        var referrerRaw: String? = Constants.EMPTY_STRING

        if (activity != null && activity!!.intent != null) {
            referrerRaw = activity!!.intent.getStringExtra(Constants.REFERRER_RAW)
        }
        val timeForFPFetch = if (!isFromCache) nlResp.timeTakenToFetch else 0
        if (section == NhAnalyticsEventSection.GROUP) {
            groupInfo?.id?.let {
                val groupEntityData = PageEntity(id = it, entityType = Constants.GROUP,
                        displayName = groupInfo?.name, name = groupInfo?.name, contentUrl = Constants.EMPTY_STRING, entityLayout = null)
                AnalyticsHelper2.logStoryListViewEvent(
                        groupEntityData, referrer, tabPosition,
                        pageNumber.toString(),
                        isFromCache, nlResp.rows.size, pageNumber.toString(), section,
                        referrerRaw, null, referrerProviderlistener, selectedReferrerFlow, timeForFPFetch
                )
            }
        } else {
            AnalyticsHelper2.logStoryListViewEvent(
                    pageEntityData, referrer, tabPosition,
                    pageNumber.toString(),
                    isFromCache, nlResp.rows.size, pageNumber.toString(), section,
                    referrerRaw, null, referrerProviderlistener, selectedReferrerFlow, timeForFPFetch
            )
        }
    }

    private fun getReferrerProviderListener(): ReferrerProviderlistener? {
        if (parentFragment is ReferrerProviderlistener) {
            return parentFragment as ReferrerProviderlistener?
        } else if (activity is ReferrerProviderlistener) {
            return activity as ReferrerProviderlistener?
        } else {
            return null
        }
    }

    fun requestAdsForVideo(nlResp: NLResp) {
        Logger.d(logTag(), "requestAdsForVideo size : ${nlResp.rows.size}, Cache : ${nlResp.isFromNetwork.not()}")
        if (nlResp.rows.size <= 0 || nlResp.isFromNetwork.not()) {
            return
        }
        val posts = nlResp.rows.filterIsInstance<PostEntity>()
        if (!CommonUtils.isEmpty(posts)) {
            GlobalScope.launch(Dispatchers.Main) {
                fetchAdsSpec.fetch(posts)
            }
            GlobalScope.launch(Dispatchers.Default) {
                fireInstreamAdRequest(posts, nlResp.pageNumber)
            }
        }
    }

    private fun fireInstreamAdRequest(postEntityList: List<CommonAsset>, pageNumber: Int) {
        Logger.d(logTag(), "fireInstreamAdRequest pageNumber : $pageNumber, " +
                " list : ${postEntityList.size}")

        postEntityList.forEach { post ->
            if (post.i_videoAsset() != null && post.i_uiType() == UiType2.AUTOPLAY &&
                    post.i_videoAsset()?.isGif == false && post.i_videoAsset()?.loopCount == 0 &&
                    DHVideoUtils.isExoPlayer(post.i_videoAsset())) {
                val playerAsset = DHVideoUtils.getPlayerAsset(post)
                if (playerAsset is ExoPlayerAsset) {
                    Logger.d(logTag(), "fireInstreamAdRequest vIndex : $videoIndex, " +
                            " Title : ${post.i_title()}")
                    IAdCacheManager.requestInstreamAd(activity, playerAsset,
                            PlayerUtils.getInstreamAdParams(playerAsset, videoIndex, true),
                            videoIndex, null, post, pageEntityData, section)
                }
            }
            videoIndex++
        }
    }


    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(downloadReceiver)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.i(logTag(), "onHiddenChanged called with $hidden")
        if (hidden) {
            logTimeSpentEvent(NhAnalyticsUserAction.CLICK)
            pauseVisibilityCalculation()
        } else {
            startTimespentTimer()
            resumeVisibilityCalculation()
        }
    }


    fun checkVisibilityCalculation() {
        if (activity != null && isAdded && userVisibleHint) {
            Logger.d(logTag(), "checkVisibilityCalculation -> computeViewVisibiltyIfNot")
            visibilityCalculator?.computeViewVisibiltyIfNot()
        }
    }

    private fun pauseVisibilityCalculation() {
        Logger.d(logTag(), "pauseVisibilityCalculation : $tabPosition")
        visibilityCalculator?.notifyFragmentVisible(false)
        visibilityCalculator?.stop()
        autoPlayManager?.stop()
        cardsAdapter?.stopVideoPrefetch()
    }

    private fun resumeVisibilityCalculation() {
        Logger.d(logTag(), "resumeVisibilityCalculation : $tabPosition")
        AndroidUtils.getMainThreadHandler().postDelayed(Runnable {
            if (activity == null || activity?.isFinishing == true || isStopped
                    || socCardsFragBinding.errorParent.root.isShown) {
                Logger.d(logTag(), "resumeVisibilityCalculation return >> ${socCardsFragBinding.errorParent.root.isShown}")
                return@Runnable
            }
            if (userVisibleHint) {
                Logger.d(logTag(), "resumeVisibilityCalculation autoPlayManager.start")
                visibilityCalculator?.start()
                visibilityCalculator?.notifyFragmentVisible(true)

                autoPlayManager?.restart()
                adsHelper?.start()
                cardsAdapter?.startVideoPrefetch()
                startVMIfVisibleAndResumed()
                linLayoutManager?.let {
                    cardsAdapter?.pushForVideoPrefetch(it.findFirstVisibleItemPosition())
                }
            }
        }, 500)
    }

    override fun getItemAndPosition(): Pair<Any?, Int> {
        return Pair(pageEntityData, tabPosition)
    }

    override fun replaceAdEntityInViewHolder(adView: UpdateableAdView) {
        if (activity?.isFinishing == true) {
            return
        }
        var success = false
        val oldAd = adView.adEntity

        //fetch backup ad from cache and update the failed adView
        if (oldAd != null && activity != null) {
            val newAd = adsHelper?.requestBackupAd(oldAd.adPosition!!)
            newAd?.let {
                // Visibility needs to be updated to fire ad impression.
                socCardsFragBinding.recycler
                        .viewTreeObserver
                        .addOnGlobalLayoutListener(recyclerViewLayoutListener)
                success = cardsAdapter.replaceAdsMapping(oldAd, newAd)
                adsHelper?.onAdReplaced(oldAd, newAd)
            }
        }
        Logger.d(logTag(), "Backup Ad insert success : $success for adView $adView")
        //In case of failure, viewholder should update its UI.
        (adView as BackUpAdConsumer).onBackupAdFetched(success)
    }

    override fun refresh() {
        if (!::vm.isInitialized) {
            return
        }
        videoIndex = 0
        vm.pullToRefresh()
    }

    private fun checkAndShowMoreNews() {
        when {
            arguments?.getBoolean(DISABLE_MORE_NEWS_TOOLITP, false) == true -> replaceAndLogevent()
            userStartedScrolling() -> {
                Logger.d(logTag(), "Showing more news")
                socCardsFragBinding.more.root.visibility = View.VISIBLE
                socCardsFragBinding.more.moreNewsContainer.setOnClickListener {
                    smoothScrollToTop()
                    replaceAndLogevent()
                    socCardsFragBinding.more.root.visibility = View.GONE
                }
            }
            else -> replaceAndLogevent()
        }
    }

    private fun replaceAndLogevent() {
        vm.lastFetchedFpResp?.nlResp?.let {
            logStoryListViewEvent(true, it)
            requestAdsForVideo(it)
        }
        vm.replaceFP()
        AndroidUtils.getMainThreadHandler().postDelayed(Runnable {
            if (activity == null || activity?.isFinishing == true) {
                return@Runnable
            }
            if (userVisibleHint) {
                socCardsFragBinding.recycler.smoothScrollToPosition(0)
                autoPlayManager?.reset()
                videoRequester?.clearPlayerInstances()
                resumeVisibilityCalculation()
            }
        }, 200)
    }

    private fun userStartedScrolling(): Boolean {
        val layoutManager = socCardsFragBinding.recycler.layoutManager as?
                LinearLayoutManager ?: return false
        return layoutManager.findFirstVisibleItemPosition() > 1
    }

    private fun handleTabEvent(tabClickEvent: TabClickEvent?) {
        tabClickEvent ?: return
        if (!isAdded || tabPosition != tabClickEvent.newTabPosition
                || slidingTabId != null && slidingTabId != tabClickEvent.slidingTabId
                || createdAt > tabClickEvent.createdAt) {
            return
        }
        Logger.d(logTag(), "handleTabEvent")
        if (userStartedScrolling() || searchQuery != null) {
            smoothScrollToTop()
        } else {
            scrollToTopAndRefresh()
            AnalyticsHelper2.logExploreButtonClickEvent(
                    currentPageReferrer,
                    NewsExploreButtonType.TAB_REFRESH, section)
        }
    }

    override fun scrollToTopAndRefresh() {
        smoothScrollToTop()
        videoIndex = 0
        vm.pullToRefresh()
    }

    private fun smoothScrollToTop() {
        socCardsFragBinding.recycler?.let { storyList ->
            val layoutManager = storyList.layoutManager as? LinearLayoutManager ?: return@let
            if (layoutManager.findFirstVisibleItemPosition() > SMOOTH_SCROLL_JUMP_POSITION) {
                storyList.scrollToPosition(SMOOTH_SCROLL_JUMP_POSITION)
            }
            storyList.smoothScrollToPosition(0)
            //TODO : hide back to top tooltip
        }
    }

    private fun observeDownloadStart() {
        if (!userVisibleHint || activity == null) {
            return
        }
        DownloadUtils.downloadRequestEvent.observe(viewLifecycleOwner, Observer {
            var card = it.asset as CommonAsset
            cardsAdapter?.updateDownloadIdForCard(it.requestId, card?.i_id())
            AnalyticsHelper2.logDownloadEvent(NhAnalyticsAppEvent.ITEM_DOWNLOAD_STARTED,
                    AnalyticsHelper2.getSection(section),
                    VideoAnalyticsHelper.getCardParams(HashMap(), it.asset, true),
                    currentPageReferrer, it.asset?.i_experiments())
            VideoDownloadBeaconImpl(it.asset?.i_id()).hitDownloadBeacon()
        })
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Logger.d("Downloader", "download state updated: downloadId - $downloadId")
            DownloadUtils.checkDownloadStatus(activity, downloadId, AnalyticsHelper2.getSection(section),
                    cardsAdapter?.getCardByDownloadId(downloadId), disposables, PageReferrer(NewsReferrer.TOPIC))
        }
    }

    private fun checkVisibilityWithDelayAndLoadPosts() {
        AndroidUtils.getMainThreadHandler().postDelayed(Runnable {
            if (activity == null || activity?.isFinishing == true) {
                return@Runnable
            }
            if (userStartedScrolling()) {
                //User already started scrolling
                return@Runnable
            }
            if (userVisibleHint) {
                Logger.d(logTag(), "Calculating visibile cards")
                scrollListener.updateCurrentCardLocation()
            }
        }, TIME_DELAY_FOR_LIST_UPDATE)
    }

    override fun onDestroy() {
        if(recyclerViewLayoutListener != null)
            socCardsFragBinding.recycler.viewTreeObserver.removeOnGlobalLayoutListener(recyclerViewLayoutListener)

        if (!this::cardsAdapter.isInitialized) {
            super.onDestroy() // super call is mandatory
            return
        }
        cardsAdapter.unregisterAdapterDataObserver(adapterObserver)
        cardsAdapter.destroy()
        adsHelper?.destroy()
        nccHelper.destroy()
        logTimeSpentEvent(NhAnalyticsUserAction.NORMAL_EXIT)
        for (viewWeakReference in webViewCache.values) {
            viewWeakReference?.get()?.let { adView ->
                // Need to keep reference to webview for atleast 1 sec to allow js
                // to trigger sessionFinish event.
                AndroidUtils.getMainThreadHandler().postDelayed(
                        { adView.destroy() },
                        (if (adView.isOMTrackingEnabled) AdConstants.OMID_WEBVIEW_DESTROY_DELAY else 0).toLong())
            }
        }
        webViewCache.clear()
        autoPlayManager?.reset()
        autoPlayManager?.destroy()
        autoPlayManager = null
        cardsAdapter?.stopVideoPrefetch()
        adapterDirtyResetHandler.removeCallbacksAndMessages(null)
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        cfCountTracker.event(Lifecycle.State.DESTROYED,
                entityId, location, section)

        Logger.d(logTag(), "onDestroy")
        super.onDestroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopped() {
        Logger.d(logTag(), "onStop lifecycle event is called $tabPosition")
        pauseVisibilityCalculation()
        isStopped = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun paused() {
        Logger.d(logTag(), "onPause lifecycle event is called $tabPosition")
        pauseVisibilityCalculation()
        isStopped = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resumed() {
        Logger.d(logTag(), "onResumed lifecycle event is called $tabPosition")
        isStopped = false
        if (userVisibleHint) {
            resumeVisibilityCalculation()
        }
    }

    private fun location(): String {
        val fromArgs = arguments?.getString(Constants.LOCATION)
        val isInHome = arguments?.getBoolean(NewsConstants.BUNDLE_U_R_IN_HOME, false)
        val loc = when {
            isInHome == true -> Constants.FETCH_LOCATION_LIST
            fromArgs != null -> fromArgs
            else -> Constants.FETCH_LOCATION_LIST + SystemClock.elapsedRealtime()
        }
        Logger.d(logTag(), "location: $fromArgs, $isInHome = $loc")
        return loc
    }

    // Schedule reset to cover any unknown scenario.
    private fun scheduleAdapterDirtyReset(delayMs: Long = 2000L) {
        if (adapterDirty) {
            adapterDirtyResetHandler.removeCallbacksAndMessages(null)
            adapterDirtyResetHandler.postDelayed({
                adapterDirty = false
                adsHelper?.insertP0AdinList(cardsAdapter.itemCount, false)
                adsHelper?.tryinsertPP1Ads(cardsAdapter.itemCount)
            }, delayMs)
            AndroidUtils.getMainThreadHandler().postDelayed(Runnable {
                if (activity == null || activity?.isFinishing == true || isStopped
                        || socCardsFragBinding.errorParent.root.isShown) {
                    return@Runnable
                }
                if (userVisibleHint) {
                    Logger.d(logTag(), "scheduleAdapterDirtyReset > autoPlayManager.start")
                    visibilityCalculator?.update()
                    //resetting the AutoPlayManager
                    autoPlayManager?.restart()
                    cardsAdapter?.startVideoPrefetch()
                }
            }, 500)
        }
    }

    private fun listFitsTheScreen() : Boolean {
        if (!::cardsAdapter.isInitialized || !::linLayoutManager.isInitialized) {
            return false
        }
        val first = linLayoutManager.findFirstCompletelyVisibleItemPosition()
        val last = linLayoutManager.findLastVisibleItemPosition()
        val isListFitsScreen = first == 0 && last == cardsAdapter.itemCount -1
        return isListFitsScreen
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK)
            when (requestCode) {
                Constants.REPORTED_ADS_RESULT_CODE -> {
                    val reportedAdEntity = (intent?.getSerializableExtra(Constants
                            .REPORTED_ADS_ENTITY) as? BaseDisplayAdEntity?)
                    val parentUniqueAdIdentifier = intent?.getStringExtra(Constants.PARENT_UNIQUE_ADID_REPORTED_ADS_ENTITY)
                    onAdReported(reportedAdEntity = reportedAdEntity,
                            reportedParentAdIdIfCarousel = parentUniqueAdIdentifier)
                }
            }
    }

    override fun onAdReported(reportedAdEntity: BaseAdEntity?,
                              reportedParentAdIdIfCarousel:String?) {
        reportedAdEntity ?: return

        if (!CommonUtils.isEmpty(reportedParentAdIdIfCarousel)) {
            HidePostUsecase(fetchDao).invoke(bundleOf(
                    Constants.BUNDLE_POST_IDS to
                            listOf(reportedParentAdIdIfCarousel).toArrayList(),
                    Constants.BUNDLE_LOCATION_ID to location,
                    NewsConstants.DH_SECTION to section,
                    Constants.BUNDLE_ENTITY_ID to entityId))
            adsHelper?.removeAdFromDb(reportedParentAdIdIfCarousel, reported = true)
        } else {
            HidePostUsecase(fetchDao).invoke(bundleOf(
                    Constants.BUNDLE_POST_IDS to
                            listOf(reportedAdEntity.uniqueAdIdentifier).toArrayList(),
                    Constants.BUNDLE_LOCATION_ID to location,
                    NewsConstants.DH_SECTION to section,
                    Constants.BUNDLE_ENTITY_ID to entityId))
            adsHelper?.removeAdFromDb(reportedAdEntity.uniqueAdIdentifier, reported = true)
        }
    }

    private fun bindStickyLangBanner(adjunctLang: String){
        socCardsFragBinding.stickyAdjunctLangBanner.visibility = View.VISIBLE
        val primaryLanguage = UserPreferenceUtil.getUserPrimaryLanguage()
        val adjMsgText = adjunctLangResponse?.bannerTextMap?.get(adjunctLang+Constants
                .COMMA_CHARACTER+adjunctLang)
        val defaultMsgText = adjunctLangResponse?.bannerTextMap?.get(adjunctLang+Constants
                .COMMA_CHARACTER+primaryLanguage)
        socCardsFragBinding.stickyAdjunctLangBanner.text_in_locale.text = adjMsgText
        socCardsFragBinding.stickyAdjunctLangBanner.text_in_app_lang.text = defaultMsgText
        socCardsFragBinding.stickyAdjunctLangBanner.adjunct_lang_tick.setOnClickListener {
            AnalyticsHelper2.logAdjunctLangCardCtaClickEvent(adjunctLang,Constants.YES,Constants.ADJUNCT_LANGUAGE_STICKY_BANNER)
            it.isSelected = true
            PreferenceManager.savePreference(AdjunctLangPreference.PENDING_USER_WRITE_FLAG, true)
            AdjunctLanguageUtils.addUserSelectedLanguage(adjunctLang, true,Constants.ADJUNCT_LANGUAGE_STICKY_BANNER)
            AdjunctLanguageUtils.setUserActedOnAdjunctLang(true)
        }
        socCardsFragBinding.stickyAdjunctLangBanner.adjunct_lang_cancel.setOnClickListener {
            AdjunctLanguageUtils.setUserActedOnAdjunctLang(true)
            val languageIntent = Intent(context, OnBoardingActivity::class.java)
            languageIntent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, true)
            languageIntent.putExtra(Constants.IS_FROM_ADJUNCT_CROSS, true)
            languageIntent.putExtra(Constants.ADJUNCT_LANG_FROM_TICK_CROSS, adjunctLang)
            languageIntent.putExtra(Constants.ADJUNCT_LANG_FLOW,Constants.ADJUNCT_LANGUAGE_STICKY_BANNER)
            startActivity(languageIntent)
        }
        AnalyticsHelper2.logAdjunctLangCardViewEvent(adjunctLang,Constants.ADJUNCT_LANGUAGE_STICKY_BANNER)
    }

    override fun getViewForAnimationByItemId(parentStoryId: String, childStoryId: String): View? {
        var itemIndex = -1
        itemIndex = cardsAdapter.getSnapshot()?.indexOfFirst {
            (it as? CommonAsset)?.i_id() == parentStoryId
        } ?: itemIndex
        return if (itemIndex >= 0) {
            socCardsFragBinding.recycler.findViewHolderForAdapterPosition(itemIndex)?.let {
                ListToDetailTransitionHelper.findViewForAnimation(it, parentStoryId, childStoryId)
            } ?: kotlin.run {
                null
            }
        } else null
    }

    override fun isAtTheTop(): Boolean {
        if (!::socCardsFragBinding.isInitialized) return true
        return cardsAdapter.itemCount <= 0 || socCardsFragBinding.recycler.getChildAt(0)?.top == 0
    }
}


interface CardsExternalListener {
fun updateCurrentCardPosition(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int)
fun onCardsLoaded() {}
fun onCardsLoadError(fullPageError : Boolean) {}
}
