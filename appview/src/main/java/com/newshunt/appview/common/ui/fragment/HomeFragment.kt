package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewpager.widget.ViewPager
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.newshunt.adengine.instream.IAdCacheManager
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.app.analytics.NotificationCommonAnalyticsHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.ui.activity.HomeActivity
import com.newshunt.appview.common.ui.adapter.HomeTabsAdapter
import com.newshunt.appview.common.ui.helper.*
import com.newshunt.appview.common.utils.InAppNotificationUtils
import com.newshunt.appview.common.video.helpers.ExoRequestHelper
import com.newshunt.appview.common.viewmodel.HomeViewModel
import com.newshunt.appview.common.viewmodel.HomeViewModelFactory
import com.newshunt.appview.common.viewmodel.NotificationtemplateViewModel
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.SnackMeta
import com.newshunt.dataentity.common.asset.ConfigType
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.common.model.entity.UserAppSection
import com.newshunt.dataentity.common.pages.*
import com.newshunt.dataentity.common.view.entity.EventActivityType
import com.newshunt.dataentity.news.analytics.NewsReferrerSource
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.notification.InAppNotificationInfo
import com.newshunt.dataentity.notification.InAppNotificationModel
import com.newshunt.dataentity.notification.InAppTemplateInfo
import com.newshunt.dataentity.notification.InAppTemplateResponse
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dataentity.search.SearchActionType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.CustomTabsUtil
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.appsection.DefaultAppSectionsProvider
import com.newshunt.dhutil.helper.behavior.BehaviorUtils
import com.newshunt.dhutil.helper.behavior.FixedBottomViewGroupBarBehavior
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.AstroPreference
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.customview.CustomViewPager
import com.newshunt.dhutil.view.customview.NHTabView
import com.newshunt.helper.setUpSearchbarHint
import com.newshunt.navigation.helper.UpgradeHelper
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.helper.AstroHelper
import com.newshunt.news.helper.BatterDialogHelper
import com.newshunt.news.helper.DefaultNavigatorCallback
import com.newshunt.news.helper.HomeSearchBarView
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.PermissionDialogHelper
import com.newshunt.news.helper.PrivacyDialogHelper
import com.newshunt.news.helper.SearchHintUtils
import com.newshunt.news.helper.SocialCoachmarkHelper
import com.newshunt.news.listener.AssetCountListener
import com.newshunt.news.listener.AssetCountsUpdateListener
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.*
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.customview.SlidingTabLayout
import com.newshunt.news.view.listener.AstroSubscriptionResultListener
import com.newshunt.news.view.listener.FragmentScrollListener
import com.newshunt.news.viewmodel.FollowUpdateViewModel
import com.newshunt.notification.analytics.NotificationActionAnalyticsHelper
import com.newshunt.notification.helper.NotificationTemplateHelper
import com.newshunt.notification.helper.NotificationUtils
import com.newshunt.onboarding.helper.LaunchHelper
import com.newshunt.sdk.network.image.Image
import com.newshunt.sso.view.view.ProfilePicHelper
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_news_tab_parent.home_share_view
import kotlinx.android.synthetic.main.layout_common_top_bar.nh_notification_icon

class HomeFragment: BaseFragment(), ViewPager.OnPageChangeListener, FragmentScrollListener,
    AssetCountListener, AstroSubscriptionResultListener, ReferrerProviderlistener, ErrorMessageBuilder.ErrorMessageClickedListener, HomeNavigation,
  FragmentTransitionViewProviderHost {

  /**
   *  - will be used for soft refresh. value is current tab shown in adapter
   *  - cannot use [preferredTabId] because its value may not always represent current tab;
   *   for example, if we add a page, it will be id of newly added tab
   *  - we may not always want to relaunch to current tab; hence not using [onSaveInstanceState] flow
   */
  private var currentTabId : String? = null
  private var forYouTabId : String? = null

  private val LOG_TAG = "HomePage"

  private lateinit var drawerLayout: DrawerLayout
  private lateinit var actionBar: Toolbar
  private lateinit var personalizedText: NHTextView
  private lateinit var homeTabLayout : SlidingTabLayout
  private lateinit var homePager : CustomViewPager
  private lateinit var profileIcon: ImageView
  private lateinit var searchView: NHTextView
  private lateinit var tabView: NHTabView
  private lateinit var addPageButton: FrameLayout
  private lateinit var socialCoachMarkView: ConstraintLayout
  private lateinit var errorParent: LinearLayout
  private var termsAndConditionsTxt : NHTextView? = null
  private var currentPagePosition = -1
  private var searchHintUtils: SearchHintUtils? = null
  private var logo: ImageView? = null

  private var paramsWithMarginTop: FrameLayout.LayoutParams? = null
  private var paramsWithNoMarginTop: FrameLayout.LayoutParams? = null
  private var homeTabsAdapter : HomeTabsAdapter? = null

  private lateinit var currentSectionId: String
  private lateinit var homeViewModel: HomeViewModel
  private var videoRequester: VideoRequester? = null

  private val referrerProviderHelper = ReferrerProviderHelper()
  private var needDoubleBackExitViaDeeplink: Boolean = false
  private var isBottomBarFixed = false
  private var preferredTabId: String? = null
  private var selectedDeeplinkUrl: String? = null
  private var pageReferrer: PageReferrer? = null
  private var isTabClicked = false
  private var isShown = false
  private var sectionIdFromDeeplink = Constants.EMPTY_STRING

  private var isCommunicationDialogVisible = false
  private var isAstroDialogShown = false
  private var uiHandler = Handler()
  private var pageList : List<PageEntity>? = null
  private var firstAddPage : AddPageEntity? = null
  lateinit var followUpdateViewModelF: FollowUpdateViewModel.Factory
  private lateinit var vmFollowUpdate: FollowUpdateViewModel
  private var lastSourceFollowBlockEntity:SourceFollowBlockEntity? = null
  private var isFirstSourceFollowBlockCallbackDone:Boolean = false
  private var bottomBarduration: Int = Constants.DEFAULT_IMPLICIT_BOTTOM_BAR_DURATION
  private var notificationtemplateResponse: InAppTemplateResponse?= null
  private lateinit var notificationTemplateVm: NotificationtemplateViewModel
  private lateinit var notificationTemplateVmFactory: NotificationtemplateViewModel.NotificationTemplateViewModelF
  private var inAppNotificationModel: InAppNotificationModel? = null
  private var inAppTextView: NHTextView?= null
  private var inAppCtaTextView: NHTextView ?= null
  private var inAppTextBg: ConstraintLayout?= null
  private var inAppTopLayout: CardView?= null
  private var inAppBottomLayout: CardView?= null
  private var inAppNotificationIcon: NHImageView ?= null
  private var isPaused = false
  private var showInAppOnResume = false

  companion object {
    private val BUNDLE_HOME_PREFERRED_TAB_ID = "bundle_home_preferred_tab_id"
    @JvmStatic
    fun newInstance(intent: Intent, videoRequester: VideoRequester) : HomeFragment {
      val fragment = HomeFragment()
      var arguments = intent.extras

      if (arguments == null) {
        arguments = Bundle()
      }

      val bundle = intent.getBundleExtra(NewsConstants.EXTRA_PAGE_ADDED)
      if (bundle != null) {
        fragment.preferredTabId = (bundle.getSerializable(NewsConstants.BUNDLE_NEWSPAGE) as PageEntity?)?.id
      }
      if(fragment.preferredTabId == null) { // for soft refresh case
        fragment.preferredTabId = intent.getStringExtra(BUNDLE_HOME_PREFERRED_TAB_ID)
      }
      if (fragment.preferredTabId == null) {
        fragment.preferredTabId = intent.getStringExtra(Constants.APP_SECTION_LAUNCH_ENTITY)
      }
      fragment.selectedDeeplinkUrl = intent.getStringExtra(Constants.SELECTED_DEEP_LINK_URL)
      fragment.videoRequester = videoRequester
      arguments.putBoolean(NewsConstants.BUNDLE_LAND_ON_HOME_TAB, intent.getBooleanExtra(NewsConstants.BUNDLE_LAND_ON_HOME_TAB, false))
      fragment.arguments = arguments
      fragment.sectionIdFromDeeplink = intent.getStringExtra(Constants.SECTION_ID) ?:Constants.EMPTY_STRING
      return fragment
    }
  }

  override fun onCreate(savedState: Bundle?) {
    super.onCreate(savedState)

    setPageReferrerFromIntent()
    Logger.d(LOG_TAG, "Current section id is $currentSectionId")
    homeViewModel = ViewModelProviders.of(this,
        HomeViewModelFactory(section = currentSectionId)).get(HomeViewModel::class.java)
    val followRecoDao = SocialDB.instance().followBlockRecoDao()
    val followEntityDao = SocialDB.instance().followEntityDao()
    followUpdateViewModelF = FollowUpdateViewModel.Factory(
      CommonUtils.getApplication(),
      followBlockUpdateUsecase = FollowBlockUpdateUsecase(followRecoDao),
      ImplicitFollowTriggerUsecase(followRecoDao, followEntityDao),
      ImplicitBlockTriggerUsecase(followRecoDao, followEntityDao),
      ExplicitFollowBlockTriggerUsecase(followRecoDao, followEntityDao),
      GetFollowBlockUpdateUsecase(followRecoDao),
      ColdSignalUseCase(followRecoDao,followEntityDao),
      MinCardPositionUseCase(followRecoDao),
      UpdateFollowBlockImplictDialogCountUsecase(followRecoDao),
      CardPositionUseCase(followRecoDao),
      BottomBarDurationUseCase(followRecoDao)

    );
    vmFollowUpdate = ViewModelProviders.of(this,followUpdateViewModelF).get(FollowUpdateViewModel::class.java)
    isBottomBarFixed = PreferenceManager.getPreference(AppStatePreference.BOTTOM_BAR_FIXED, false)
    UpgradeHelper.handleUpgradeDialog(requireContext())

    if(PreferenceManager.getPreference(GenericAppStatePreference.IS_FY_LOAD_SUCCESS,false)) {
      PreferenceManager.saveInt(GenericAppStatePreference.SUCCESSFUL_PREV_FEED_LOAD_SESSION_COUNT.name, PreferenceManager.getInt(GenericAppStatePreference.SUCCESSFUL_PREV_FEED_LOAD_SESSION_COUNT.name, 0) + 1)
      PreferenceManager.savePreference(GenericAppStatePreference.IS_FY_LOAD_SUCCESS,false)
    }

    notificationTemplateVmFactory = NotificationtemplateViewModel.NotificationTemplateViewModelF()
    notificationTemplateVm = ViewModelProviders.of(this,notificationTemplateVmFactory).get(NotificationtemplateViewModel::class.java)

    homeViewModel.firstAddPageLiveData.observe(this, Observer {
      firstAddPage = it.getOrNull()
    })
  }

  private fun startObservingFollowBlockRecommendation() {
    vmFollowUpdate.triggerBottomBarDuration()
    vmFollowUpdate.getBottomDurationLiveData.observe(viewLifecycleOwner,{
      if (it!= null) {
         bottomBarduration = it
      }
    })

    vmFollowUpdate.followBlockLiveData.observe(viewLifecycleOwner, {
      if (it != null && isForYouPage()) {
        if (!isFirstSourceFollowBlockCallbackDone) {
          isFirstSourceFollowBlockCallbackDone = true
          return@observe
        }
        if (FollowUtils.isSameFollowBlockObject(lastSourceFollowBlockEntity, it) || !isVisible) {
          return@observe
        }
        lastSourceFollowBlockEntity = it
        // triggering usecase only basis last action
        if(it.updateType == FollowActionType.FOLLOW) {
          triggerImplicitFollowCase(it)
        } else if(it.updateType == FollowActionType.BLOCK) {
          triggerImplicitBlockCase(it)
        }
      }
    })
    vmFollowUpdate.implicitBlockLiveData.observe(viewLifecycleOwner, {
      it?.let {
        if (isForYouPage()) {
        val fragment=  FollowBlockDialogFragment.newInstance(it,getPageReferrer(), Constants.FOLLOW)
          fragment.show(childFragmentManager, Constants.FOLLOW)

          DialogAnalyticsHelper.logDialogBoxViewedEvent(DialogBoxType.IMPLICIT_BLOCK_PROMPT,getPageReferrer(),
            NhAnalyticsEventSection.NEWS,null)
           dismissDialog(fragment);
           vmFollowUpdate.incrementFollowBlockImplicitDialogCountUsecase(
            bundleOf(
              Constants.BUNDLE_SOURCE_ID to it.sourceId,
              Constants.BUNDLE_SOURCE_BLOCK to true
            )
          )
//        }
        }
      }
    })
    vmFollowUpdate.implicitFollowLiveData.observe(viewLifecycleOwner, {
      it?.let {
        if (isForYouPage()) {
          val fragment = FollowBlockDialogFragment.newInstance(it,getPageReferrer(), Constants.BLOCK)
          fragment.show(childFragmentManager, Constants.BLOCK)
          dismissDialog(fragment);
          vmFollowUpdate.incrementFollowBlockImplicitDialogCountUsecase(
            bundleOf(
              Constants.BUNDLE_SOURCE_ID to it.sourceId,
              Constants.BUNDLE_SOURCE_BLOCK to false
            )
          )

          DialogAnalyticsHelper.logDialogBoxViewedEvent(
                  DialogBoxType.IMPLICIT_FOLLOW_PROMPT,getPageReferrer(),
          NhAnalyticsEventSection.NEWS,null)


        }
      }

    })
  }

  private fun dismissDialog(fragment:FollowBlockDialogFragment) {
    if (fragment != null) {
      uiHandler.postDelayed(Runnable {
        if(fragment.dialog?.isShowing == true)
          fragment.dismiss()
      },TimeUnit.SECONDS.toMillis(bottomBarduration.toLong()))
    }

  }

  private fun isForYouPage():Boolean {
    return currentTabId == forYouTabId
  }

  private fun triggerImplicitBlockCase(sourceFollowBlockEntity: SourceFollowBlockEntity) {
    vmFollowUpdate.triggerImplicitBlockUsecase(sourceFollowBlockEntity)

  }

  private fun triggerImplicitFollowCase(sourceFollowBlockEntity: SourceFollowBlockEntity) {
    vmFollowUpdate.triggerImplicitFollowUsecase(sourceFollowBlockEntity)
  }

  override fun onStart() {
    super.onStart()
    // get the data from the db and then update from the server
    homeViewModel.viewStarted()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)


    notificationTemplateVm.templateLiveData.observe(viewLifecycleOwner, {
      val data = it.getOrNull()
      if(it.isSuccess) {
        data?.let {
          Logger.e("InAppNotiifcation", "notification template response is not null1")
          notificationtemplateResponse = it
        }
      } else {
        Logger.e("InAppNotiifcation", "notification template response is null")
        notificationtemplateResponse = NotificationTemplateHelper.getDefaultColorTemplate()
      }
        if(inAppNotificationModel != null) {
          showInAppNotification(inAppNotificationModel!!)
        }
    })
    notificationTemplateVm.getTemplateInfo()

    homeViewModel.pageLiveData.observe(viewLifecycleOwner, Observer {
      Logger.d(LOG_TAG , "Got the result in the lifecycle observer")
      if (it.isSuccess) {
        Logger.d(LOG_TAG , "Got the success result")
        val data = it.getOrNull()
        if (data != null) {
          if (isPageChanges(data)) {
            Logger.d(LOG_TAG, "Updating pages")
            updatePages(data)
            setSearchHintText()
          } else {
            Logger.d(LOG_TAG, "Pages not changes hence ignoring the response")
          }

        }
      }
    })

    homeViewModel.nwLiveData.observe(viewLifecycleOwner, Observer {
      Logger.d(LOG_TAG, "Received response from network")
      if (it.isFailure && (homeTabsAdapter == null || homeTabsAdapter?.count == 0)) {
        val throwable = it.exceptionOrNull()
        throwable?.let {
          val error = ApiResponseOperator.getError(throwable)
          Logger.d(LOG_TAG, "Error is ${error.message}" )
          showError(error)
        }
      }
    })

    homeViewModel.communicationLiveData.observe(viewLifecycleOwner, Observer {
      if (it.isSuccess) {
        Logger.d(LOG_TAG, "Process the communication response")
        val data = it.getOrNull()
        if (data == null) {
          displayInAppNotification()
          return@Observer
        }
        else
          handleCommunicationResponse(data)
      } else {
        displayInAppNotification()
      }
    })

    viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {

      @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
      fun onLifecycleResume() {
        isPaused = false
        if(showInAppOnResume && inAppNotificationModel != null) {
          showInAppNotification(inAppNotificationModel!!)
        }
      }

      @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
      fun onLifecyclePause() {
        isPaused = true
      }
    })

    InAppNotificationUtils.inAppNotificationLiveData.observe(viewLifecycleOwner, Observer {
      inAppNotificationModel = it
      inAppNotificationModel?.let { inApp ->
        if(notificationtemplateResponse != null) {
          showInAppNotification(inApp)
        }
      }
    })

    AppSettingsProvider.getNotificationLiveData().observe(viewLifecycleOwner, Observer {
      nh_notification_icon.onNotificationEventChanged(it)
    })
    setSearchHintText()
    startObservingFollowBlockRecommendation();
  }

  private fun isPageChanges(newList: List<PageEntity>?) : Boolean {
    if (pageList == null || newList == null) {
      return  true
    } else if (pageList?.size != newList.size) {
      return true
    } else {
      pageList?.forEachIndexed { index, pageEntity ->
        if (!pageEntity.equalsForHome(newList[index])) {
          return true
        }
      }
    }
    return false
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.activity_news_tab_parent, container,false)
    initView(view)
    return view
  }

  private fun setPageReferrerFromIntent() {
    val bundle = arguments

    if (bundle != null) {
      pageReferrer = bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
      currentSectionId = bundle.getString(Constants.APP_SECTION_ID,
          DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id)

      if (CommonNavigator.isFromNotificationTray(pageReferrer) || CommonNavigator.isDeeplinkReferrer(pageReferrer)) {
        needDoubleBackExitViaDeeplink = bundle.getBoolean(Constants.DEEP_LINK_DOUBLE_BACK_EXIT)
        NewsAnalyticsHelper.updateAppState(pageReferrer)
      }
    }

    if (pageReferrer == null) {
      pageReferrer = PageReferrer(NhGenericReferrer.ORGANIC)
      pageReferrer?.referrerSource = NewsReferrerSource.NEWS_HOME_VIEW
    }
    referrerProviderHelper.addReferrerByProvider(pageReferrer)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    arguments?.let {
      val snackData = it.getSerializable(NotificationConstants.SNACK_BAR_META) as SnackMeta?
      snackData?.let { showSnackbarForStickyNotification(view, snackData) }
    }
  }

  private fun initView(rootView: View) {
    val childContainer = rootView.findViewById(R.id.child_container) as LinearLayout
    LayoutInflater.from(context).inflate(R.layout.news_home_tab_activity, childContainer)
    setupCustomActionBar(rootView)

    tabView = rootView.findViewById(R.id.bottom_tab_bar)
    tabView.setCurrentSectionId(currentSectionId)
    tabView.setSectionFromDeeplink(sectionIdFromDeeplink)
    tabView.setLifecycleOwner(this)

    val bottomLayout = rootView.findViewById(R.id.scrollable_bottom_container) as LinearLayout
    bottomLayout.visibility = View.VISIBLE

    if (isBottomBarFixed) {
      val layoutParams = bottomLayout.layoutParams as CoordinatorLayout.LayoutParams
      layoutParams.behavior = FixedBottomViewGroupBarBehavior<CoordinatorLayout>()
      bottomLayout.layoutParams = layoutParams
      bottomLayout.requestLayout()
      rootView.findViewById<View>(R.id.fixed_empty_area).visibility = View.VISIBLE
    } else {
      rootView.findViewById<View>(R.id.fixed_empty_area).visibility = View.GONE
    }

    socialCoachMarkView = rootView.findViewById(R.id.cm_social)
    socialCoachMarkView.setOnClickListener {
      socialCoachMarkView.visibility = View.GONE
    }

    showOrHideBottomBar(true, rootView)

    homeTabLayout = rootView.findViewById(R.id.news_home_tabs)
    homeTabLayout.setTabTextColor(ThemeUtils.getThemeColorByAttribute(context, R.attr.tab_title_select_color),
            ThemeUtils.getThemeColorByAttribute(context, R.attr.tab_title_color))
    homeTabLayout.setDrawBottomLine(true)
    homeTabLayout.setCustomTabView(R.layout.home_tab_item, R.id.tab_item_title, R.id.tab_item_image, R.id.tab_item_icon)
    homeTabLayout.setDisplayDefaultIconForEmptyTitle(true)
    homeTabLayout.setTabClickListener({ v, position -> isTabClicked = true })

    homePager = rootView.findViewById(R.id.news_home_view_pager)
    homePager.addOnPageChangeListener(this)
    homePager.pagingEnabled = CustomTabsUtil.tabsSwipeEnabled()

    addPageButton = rootView.findViewById(R.id.page_add_view)
    addPageButton.setOnClickListener{
      val intent = Intent(NewsConstants.INTENT_ACTION_LAUNCH_ADD_PAGE)
      intent.putExtra(NewsConstants.DH_SECTION, currentSectionId)
      NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
      AnalyticsHelper2.logExploreButtonClickEvent(
            if (referrerProviderHelper.referrerQueue != null) referrerProviderHelper.referrerQueue.yongest else null,
            NewsExploreButtonType.ADD, currentSectionId)
      AnalyticsHelper2.logTabSelectionViewEvent(
          if (referrerProviderHelper.referrerQueue != null) referrerProviderHelper.referrerQueue.yongest else null,
          if (preferredTabId != null && homeTabsAdapter != null) homeTabsAdapter?.getPageType(preferredTabId!!) else null,
          currentSectionId)
    }

    profileIcon = rootView.findViewById(R.id.profile_image)

    logo = rootView.findViewById(R.id.actionbar_image)
    searchView = rootView.findViewById(R.id.global_search)
    searchView.visibility = View.GONE
//    HomeSearchBarView(this, logo, searchView, HomeSearchBarView.ICON_POSITION_LEFT)
//    setUpSearchbarHint(searchView, SearchLocation.NewsHome, { referrerProviderHelper.youngestPageReferrer },
//            searchPayloadContext = getSearchPayloadContext())

      if (isBottomBarFixed) {
          val fabLayout: View = rootView.findViewById(R.id.fab_layout)
          val layoutParams = fabLayout.layoutParams as CoordinatorLayout.LayoutParams
          layoutParams.behavior = FixedBottomViewGroupBarBehavior<CoordinatorLayout>()
          fabLayout.layoutParams = layoutParams
          fabLayout.requestLayout()
      }

    errorParent = rootView.findViewById(R.id.error_parent)

    inAppTopLayout = rootView.findViewById(R.id.in_app)
    inAppBottomLayout = rootView.findViewById(R.id.in_app_bottom)
  }

  private fun setSearchHintText() {
    if (searchHintUtils == null) {
      searchHintUtils = SearchHintUtils(searchView, this)
    }
    val entityPage = homeTabsAdapter?.getPage(homePager.currentItem) ?: return
    searchHintUtils?.updateHint(SearchLocation.NewsHome, entityPage.id, entityPage.entityType)
  }

  private fun getSearchPayloadContext(): SearchPayloadContext? {
    val currentPageEntity = homeTabsAdapter?.getPage(homePager.currentItem)
    return SearchPayloadContext(
            garbage = null,
            action = SearchActionType.UNIFIED.name,
            entityId = currentPageEntity?.id,
            entityType = currentPageEntity?.entityType,
            groupId = null,
            parentPostId = null,
            postId = null,
            section = currentSectionId
    )
  }

  private fun setupCustomActionBar(rootView:View) {
    actionBar = rootView.findViewById(R.id.news_action_bar)
    //setSupportActionBar(actionBar)
    //supportActionBar!!.setDisplayShowTitleEnabled(false)

    if (!AppConfig.getInstance()!!.isGoBuild) {
      drawerLayout = rootView.findViewById(R.id.drawer_layout)

      paramsWithMarginTop = drawerLayout.layoutParams as FrameLayout.LayoutParams
      paramsWithNoMarginTop = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT)
      paramsWithNoMarginTop!!.setMargins(0, 0, 0, 0)
    }

    personalizedText = rootView.findViewById<View>(R.id.personalize_view) as NHTextView
    personalizedText.text = CommonUtils.getString(R.string.personalize_view_text)

    termsAndConditionsTxt = rootView.findViewById<View>(R.id.termsView) as NHTextView
    termsAndConditionsTxt?.visibility = View.GONE

    BehaviorUtils.enableTopbarScrolling(rootView.findViewById(R.id.news_action_bar))
    ProfilePicHelper(this, rootView.findViewById(R.id.profile_image), getPageReferrer(), this, DefaultNavigatorCallback())
  }

  private fun showOrHideBottomBar(show: Boolean, rootView:View) {
    if (show) {
      rootView.findViewById<View>(R.id.bottom_tab_bar).visibility = View.VISIBLE
    } else {
      rootView.findViewById<View>(R.id.bottom_tab_bar).visibility = View.GONE
    }
  }

  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (!isVisibleToUser) {
      termsAndConditionsTxt?.visibility = View.GONE
    }
  }

  private fun updatePages(data : List<PageEntity>) {
    if (homeTabsAdapter == null) {
      val extraArgs = Bundle()
      extraArgs.putInt(NewsConstants.BUNDLE_SLIDING_TAB_ID, homeTabLayout.hashCode())
      extraArgs.putBoolean(NewsConstants.BUNDLE_ENABLE_MAX_DURATION_TO_NOT_FETCH_FP, true)
      extraArgs.putBoolean(NewsConstants.BUNDLE_U_R_IN_HOME, true)
      homeTabsAdapter = HomeTabsAdapter(childFragmentManager, this,
          videoRequester, section = currentSectionId,extraArguments = extraArgs, nhShareView = home_share_view, createPost = null)
    }
    Logger.d(LOG_TAG, "Setting the list of tabs with count ${data.size}")
    homePager.adapter = homeTabsAdapter
    homeTabsAdapter?.updateList(data)
    homeTabLayout.setViewPager(homePager)
    homeTabsAdapter?.notifyDataSetChanged()
    val index = setIndex(data)
    homePager.currentItem = index
    if (index == 0) {
      uiHandler.post { onPageSelected(0) }
    }
    pageList = data
  }

  private fun setIndex(data: List<PageEntity>) : Int {
    if (arguments?.getBoolean(NewsConstants.BUNDLE_LAND_ON_HOME_TAB, false) == true) {
      return 0
    }
    if (preferredTabId != null) {
      data.forEachIndexed { index, item ->
        if (preferredTabId == item.id) {
          return index
        }
      }
    }
    return 0
  }

  override fun onPageScrollStateChanged(state: Int) {
    // If user clicks on tab then we shouldn't count it as swipe also
    if (!NhAnalyticsUserAction.CLICK.equals(NhAnalyticsAppState.getInstance().action)) {
      NhAnalyticsAppState.getInstance().action = NhAnalyticsUserAction.SWIPE
    }
  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

  }

  override fun onPageSelected(position: Int) {
    Logger.d(LOG_TAG, "onPageSelected: old=$currentPagePosition, new=$position, adp=$homeTabsAdapter")
    CardSeenStatusRepo.extractAndUpdateState(homePager, position, currentPagePosition)
    homeTabsAdapter?.let {
      val action = if (isTabClicked) NhAnalyticsUserAction.CLICK else NhAnalyticsUserAction.SWIPE
      it.onPageSelected(action)
      NhAnalyticsAppState.getInstance().action = action
      isTabClicked = false
      addReferrerByPosition(position)
      referrerProviderHelper.setAction(action)
      preferredTabId = homeTabsAdapter?.getPage(position)?.id
      currentTabId = preferredTabId
      preferredTabId?.let {
        AppSectionsProvider.updateAppSectionInfo(UserAppSection.Builder()
            .section(AppSection.NEWS)
            .sectionId(currentSectionId)
            .entityKey(it).build())
      }
    }

    setSearchHintText()
    if (currentPagePosition != position) {
      currentPagePosition = position
      if (LaunchHelper.isFirstTimeTermsVisible() && context != null) {
        termsAndConditionsTxt?.setSpannableText(
                Html.fromHtml(context?.getString(R.string.user_terms_condition)) as Spannable,
                getString(R.string.user_terms_condition))
        termsAndConditionsTxt?.movementMethod = LinkMovementMethod.getInstance()
        termsAndConditionsTxt?.visibility = View.VISIBLE
        LaunchHelper.disableFirstTimeTermsVisible()
      }
      else {
        termsAndConditionsTxt?.visibility = View.GONE
      }
    }
    forYouTabId = PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE, "")
  }

  override fun onPause() {
    termsAndConditionsTxt?.visibility = View.GONE
    super.onPause()
  }

  private fun handleCommunicationResponse(communicationEventsResponse: CommunicationEventsResponse) {
    val eventList = communicationEventsResponse.events
      val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)
    // handle the upgrade first
    eventList?.let {
      for (info in eventList) {
        if (currentSectionId != info.resource || CommonUtils.isEmpty(info.precondition)) {
          continue
        }
        val enableIfRegistered = info.precondition!![NewsConstants.COMM_ENABLE_IF_REGISTERED].equals(true.toString())
        if (!isRegistered && enableIfRegistered) {
          continue
        }

        // get linkedin share config first
        handleLinkedInShareCommunication(info)

        val isUpgradeType = NewsConstants.COMM_EVENT_UPGRADE == info.event
        if (isUpgradeType) {
          if (handleUpgradeTypeCommunication(info)) {
            isCommunicationDialogVisible = true
            return@let
          }
        }
      }
    }
    if (!isCommunicationDialogVisible) {
      eventList?.let {
        for (info in eventList) {
          if (currentSectionId != info.resource || CommonUtils.isEmpty(info.precondition)) {
            continue
          }

          val isEventTypeLaunch = NewsConstants.COMM_EVENT_LAUNCH == info.event || NewsConstants.COMM_EVENT_APP_LAUNCH == info.event
          if (!isEventTypeLaunch) {
            continue
          }

          val minLaunch = info.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
            NewsConstants.COMM_MIN_OCCURENCES_GO
          else
            NewsConstants.COMM_MIN_OCCURENCES]
          if (CommonUtils.isEmpty(minLaunch) || !CommonUtils.isValidInteger(minLaunch)) {
            continue
          }
          val minimumLaunchCount = Integer.parseInt(minLaunch!!)

          val type = info.activity!!.type
          if (CommonUtils.isEmpty(type)) {
            continue
          }

          val enableIfRegistered = info.precondition!![NewsConstants.COMM_ENABLE_IF_REGISTERED].equals(true.toString())
          if (!isRegistered && enableIfRegistered) {
            continue
          }
          val eventActivityType = EventActivityType.getEventActivityType(type) ?: continue

          when (eventActivityType) {
            EventActivityType.INVALID, EventActivityType.WALKTHROUGH -> {
              // do nothing
            }

            EventActivityType.ASTRO -> {
              if (isAstroDialogShown) {
                return
              }
              if (AstroHelper.handleAstroEvent(info, minimumLaunchCount, activity!!, fragmentId, this)) {
                isCommunicationDialogVisible = true
                isAstroDialogShown = true
                return
              }
            }
            EventActivityType.PERMISSION ->
              if (PermissionDialogHelper.handlePermissionEvent(info, minimumLaunchCount, activity!!)) {
                isCommunicationDialogVisible = true
                return
              }
            EventActivityType.PRIVACY_V2 ->
              if (PrivacyDialogHelper.handlePrivacyEvent(info, minimumLaunchCount, activity!!, fragmentId)) {
                isCommunicationDialogVisible = true
                return
              }
            EventActivityType.BATTERY_OPTIMIZATION_DIALOG ->
              if (BatterDialogHelper.handleBatteryOptimizationEvent(info, activity!!)) {
                isCommunicationDialogVisible = true
                return
              }
            EventActivityType.SOCIAL_COACHMARK ->
              if (SocialCoachmarkHelper.handleSocialCoachMark(info, minimumLaunchCount, socialCoachMarkView)) {
                isCommunicationDialogVisible = true
                return
              }
          }
        }
      }
    }

    if(isCommunicationDialogVisible && InAppNotificationUtils.inAppNotificationState != InAppNotificationUtils.InAppState.OTHER_IN_APP_SHOWN) {
      InAppNotificationUtils.inAppNotificationState = InAppNotificationUtils.InAppState.COMMUNICATION_API_PRIORITIZED
      InAppNotificationUtils.handleInAppNotShown()
    }

    if(InAppNotificationUtils.inAppNotificationState != InAppNotificationUtils.InAppState.COMMUNICATION_API_PRIORITIZED && InAppNotificationUtils.inAppNotificationState != InAppNotificationUtils.InAppState.OTHER_IN_APP_SHOWN) {
      displayInAppNotification()
    }
  }

  private fun displayInAppNotification(){
    InAppNotificationUtils.inAppNotificationState = InAppNotificationUtils.InAppState.CAN_SHOW
    Logger.d("InAppNotification","show in-App Notification")
    InAppNotificationUtils.handleInAppNotification()
  }

  private fun handleUpgradeTypeCommunication(info: EventsInfo)  : Boolean {
    if (!PreferenceManager.getPreference(GenericAppStatePreference.USER_UPGRADED_SOCIAL, false)) {
      return false
    }

    val minLaunch = info.precondition!![if (AppConfig.getInstance()!!.isGoBuild)
      NewsConstants.COMM_MIN_OCCURENCES_GO
    else
      NewsConstants.COMM_MIN_OCCURENCES]
    if (CommonUtils.isEmpty(minLaunch) || !CommonUtils.isValidInteger(minLaunch)) {
      return false
    }
    val minimumLaunchCount = Integer.parseInt(minLaunch!!)
    val launchCount = AppUserPreferenceUtils.getNewsLaunchCount()

    if (launchCount < minimumLaunchCount) {
      return false
    }

    val type = info.activity!!.type
    if (CommonUtils.isEmpty(type)) {
      return false
    }
    val eventActivityType = EventActivityType.getEventActivityType(type) ?: return false

    if (eventActivityType == EventActivityType.WALKTHROUGH) {
      return if (PreferenceManager.getPreference(GenericAppStatePreference.SOCIAL_WALKTHROUGH_SHOWN, false)) {
         false
      } else {
        val intent = Intent(Constants.WALKTHROUGH_ACTION)
        val importContacts =  info.activity?.attributes?.get(NewsConstants.COMM_IMPORT_CONTACTS)
        val toImport = importContacts?.toBoolean() ?: false
        intent.putExtra(NewsConstants.EXTRA_SHOW_IMPORT_CONTACTS, toImport)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
        true
      }

    }

    return false

  }

  private fun handleLinkedInShareCommunication(info: EventsInfo) {
    info.activity?.type?.let {
      val eventActivityType = EventActivityType.getEventActivityType(it)
      if (eventActivityType == EventActivityType.LINKEDIN_SHARE) {
        info.precondition?.let { map ->
          for((key, value) in map) {
            if (key.isNullOrBlank() || value.isNullOrBlank()) {
              continue
            }
            if (Constants.MAX_NUMBER_OF_TIMES_TO_SHOW_LINKEDIN_DIALOG.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_SHARE_MAX_TIMES_SHOW,value.toInt())
            }
            if (Constants.MIN_DAYS_TO_SHOW_LINKEDIN_DIALOG_UPGRADED_USERS.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_MIN_WAIT_DAYS_UPGRADED_USERS,value.toInt())
            }
            if (Constants.MIN_DAYS_TO_SHOW_LINKEDIN_DIALOG_NEW_USERS.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_MIN_WAIT_DAYS_NEW_USERS,value.toInt())
            }
            if (Constants.MIN_LAUNCHES_TO_SHOW_LINKEDIN_DIALOG_UPGRADED_USERS.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_MIN_LAUNCHES_UPGRADED_USERS,value.toInt())
            }
            if (Constants.MIN_DAYS_USER_TO_WAIT_AFTER_LAST_SEEN.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_MIN_DAYS_AFTER_LAST_SEEN,value.toInt())
            }
            if (Constants.SHOW_LINKEDIN_SHARE_DIALOG.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_SHARE_ENABLED,value.toBoolean())
            }
          }
        }
        info.activity?.attributes?.let { map->
          for((key, value) in map) {
            if(key.isNullOrBlank() || value.isNullOrBlank()) {
              continue
            }
            if (Constants.DEFAULT_SHARE_APP_PACKAGE_NAME.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_SHARE_PKG_NAME,value)
            }
            if (Constants.LINKEDIN_DIALOG_TITLE.equals(key, ignoreCase = true)) {
              PreferenceManager.savePreference(AppStatePreference.LINKEDIN_SHARE_DIALOG_TITLE,value)
            }
          }
        }
      }
    }
  }

  override fun smoothScrollToolBar() {

  }

  override fun setAssetUpdateCountListener(listener: AssetCountsUpdateListener) {

  }

  override fun onAstroSubscriptionSuccess() {
    AstroHelper.handleAstroSuccessfulSubscription()
    activity?.let {
      val message = CommonUtils.getString(R.string.astro_subscription_success)
      FontHelper.showCustomFontToast(it, message, Toast.LENGTH_LONG)
    }
    AstroHelper.fireAstroSubscriptionEvent(DailyhuntConstants.ASTRO_PROMPT)
    preferredTabId = PreferenceManager.getPreference(AstroPreference.ASTRO_TOPIC_ID, Constants.EMPTY_STRING)
  }

  override fun onAstroSubscriptionFailed(failureReason: String) {
    uiHandler.post {
      activity?.let {
        FontHelper.showCustomFontToast(it, failureReason, Toast.LENGTH_LONG)
      }
    }
  }

  override fun getFragmentTransitionViewProvider(): FragmentTransitionViewProvider? {
    return homeTabsAdapter?.currentFragment as? FragmentTransitionViewProvider
  }

  private fun addReferrerByPosition(position: Int) {

    if (homeTabsAdapter == null
        || homeTabsAdapter?.pageList == null
        || position >= homeTabsAdapter?.pageList?.size?:0
        || homeTabsAdapter?.pageList?.get(position) == null) {
      return
    }
    var referrer: NhAnalyticsReferrer? = null
    val pageType = PageType.fromName(homeTabsAdapter?.pageList?.get(position)?.entityType?:"")
    if (pageType != null) {
      val pageReferrer = PageType.getPageReferrer(pageType)
      if (pageReferrer != null) {
        if (pageReferrer.referrerSource == null) {
          pageReferrer.referrerSource = NewsReferrerSource.NEWS_HOME_VIEW
        }
        referrer = pageReferrer.referrer
      }
    }


    referrer?.let {
      val currentPageReferrer =
          PageReferrer(referrer, homeTabsAdapter?.pageList?.get(position)?.id, null)

      referrerProviderHelper.addReferrerByProvider(currentPageReferrer)
    }
  }

  private fun getPageReferrer(): PageReferrer? {
    return referrerProviderHelper.referrerQueue?.yongest
  }

  override fun onDestroy() {
    super.onDestroy()
    InAppNotificationUtils.inAppNotificationLiveData.postValue(null)
    uiHandler.removeCallbacksAndMessages(null)
    ExoRequestHelper.destroy(null)
  }

  override fun onHiddenChanged(hidden: Boolean) {
    if (!hidden) {
      homeViewModel.syncPage()
      if (firstAddPage != null) {
        preferredTabId = firstAddPage?.id
      }
    }

    super.onHiddenChanged(hidden)
    Logger.i(LOG_TAG, "onHiddenChange called $hidden")
    homeTabsAdapter?.currentFragment?.onHiddenChanged(hidden)
    if (hidden) {
      termsAndConditionsTxt?.visibility = View.GONE
    }
  }

  override fun getProvidedReferrer(): PageReferrer {
    return referrerProviderHelper.providedPageReferrer
  }

  override fun getReferrerEventSection(): NhAnalyticsEventSection {
    return if (PageSection.TV.section == currentSectionId) {
      NhAnalyticsEventSection.TV
    } else {
      NhAnalyticsEventSection.NEWS
    }
  }

  override fun getLatestPageReferrer(): PageReferrer? {
    return getPageReferrer()
  }

  override fun handleBackPress(): Boolean {
    if (socialCoachMarkView.visibility == View.VISIBLE) {
      socialCoachMarkView.visibility = View.GONE
      return true
    }
    return false
  }

  override fun onStop() {
    super.onStop()
    ExoDownloadHelper.stopVideoDownload()
    if (activity?.isFinishing == true) {
      videoRequester?.clearPlayerInstances()
      IAdCacheManager.clearInstance()
    }
  }

  private fun showError(error: BaseError) {
    errorParent.visibility = View.VISIBLE
    val errorMessageBuilder = ErrorMessageBuilder(errorParent, requireContext(), this, this)
    errorMessageBuilder.showError(error)
  }

  override fun onRetryClicked(view: View?) {
    errorParent.visibility = View.GONE
    homeViewModel.viewStarted()
  }

  override fun onNoContentClicked(view: View?) {
    // do nothing
  }

  override fun isHomeSection(): Boolean {
    return currentSectionId == DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id
  }

  override fun handleHomeNavigation(): Boolean {
    if (!::tabView.isInitialized) return false
    //If the current HomeFragment is not home section, navigate to home section
    if (!isHomeSection()) {
      tabView.launchNewsSection(DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id, true)
      return true
    }

    //If current tab index == 0, means we are in home tab.
    if (homePager.currentItem == 0) {
      homeTabsAdapter?.currentFragment?.let { cf ->
        //If at the top of home tab, we can allow exiting the app. return false
        if (cf.isAtTheTop) {
          return false
        } else {
          //If user has scrolled, pull down to refresh
          cf.scrollToTopAndRefresh()
        }
      } ?: run {
        //Could not find the top fragment. Allow exit
        return false
      }
    } else {
      //If we are on some other tab, switch to home tab.
      homePager.currentItem = 0
    }
    return true
  }

  fun showSnackbarForStickyNotification(view: View,snackMeta: SnackMeta) {
    if(!snackMeta.message.isNullOrEmpty()) {
      GenericCustomSnackBar.showSnackBar(view, CommonUtils.getApplication(), snackMeta.message!!,
              snackMeta.duration, null, null, snackMeta.ctaText, View.OnClickListener { v: View? ->
        CommonNavigator.launchDeeplink(context, snackMeta.ctaUrl, null)
      }).show()
    }
  }

  private fun showInAppNotification(inAppNotificationModel: InAppNotificationModel) {
    // check if in-app can be shown

    if(InAppNotificationUtils.inAppNotificationState == InAppNotificationUtils.InAppState.CANNOT_SHOW) {
      return
    }

    inAppNotificationModel.baseInfo?.inAppInfo?.endTimeMs?.let {
      if(it < System.currentTimeMillis()) {
        InAppNotificationUtils.markShownInAppNotificationStatus(inAppNotificationModel.baseInfo.uniqueId.toString(), Constants.EXPIRED)
        NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(inAppNotificationModel,Constants.NO_USER_SESSION)
        return
      }
    }

    if(InAppNotificationUtils.inAppNotificationState == InAppNotificationUtils.InAppState.COMMUNICATION_API_PRIORITIZED){
      NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(inAppNotificationModel,Constants.COMMUNICATION_API_PRIORITIZED)
      return
    }

    if(InAppNotificationUtils.inAppNotificationState == InAppNotificationUtils.InAppState.OTHER_IN_APP_SHOWN){
      NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(inAppNotificationModel,Constants.OTHER_IN_APP_PRIORITIZED)
      return
    }

    if(NotificationUtils.isInvalidLanguage(inAppNotificationModel.baseInfo.language, inAppNotificationModel.baseInfo.isDisableLangFilter)) {
      return
    }

    if(isPaused){
      Logger.e(LOG_TAG,"Paused hence returning")
      showInAppOnResume = true
      return
    }

    if(!(activity as HomeActivity).isCurFragmentHome) {
//      NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(inAppNotificationModel,Constants.MINI_VIDEO_PLAYER_PRESENT)
      return
    }

    showInAppOnResume = false
    val inAppInfo = inAppNotificationModel.baseInfo.inAppInfo

    // if cta is to land to bottom bar, check if section exixts
    if(inAppInfo.notificationCtaLink?.contains("appsection") == true) {
      val sectionId = InAppNotificationUtils.parseSectionId(inAppInfo.notificationCtaLink)
      sectionId?.let {
        val sectionInfo = tabView.checkIfSectionExists(sectionId)
        if(sectionInfo == null) {
          Logger.e("InAppNotiifcation","section doesn't exists")
          return
        }
        Logger.e("InAppNotiifcation","section exists")
      }
    }

    NotificationCommonAnalyticsHelper.logInAppNotificationDisplayEvents(inAppNotificationModel)
    // decide position
    if(inAppInfo.pos == Constants.HOME_FEED_TOP) {
      inAppTopLayout?.visibility = View.VISIBLE
      inAppBottomLayout?.visibility = View.GONE
      inAppTextView = inAppTopLayout?.findViewById(R.id.notificationText)
      inAppCtaTextView = inAppTopLayout?.findViewById(R.id.notificationCtaText)
      inAppTextBg = inAppTopLayout?.findViewById(R.id.in_app_text)
      inAppNotificationIcon = inAppTopLayout?.findViewById(R.id.in_app_img)
    } else {
      inAppBottomLayout?.visibility = View.VISIBLE
      inAppTopLayout?.visibility = View.GONE
      inAppTextView = inAppBottomLayout?.findViewById(R.id.notificationText)
      inAppCtaTextView = inAppBottomLayout?.findViewById(R.id.notificationCtaText)
      inAppTextBg = inAppBottomLayout?.findViewById(R.id.in_app_text)
      inAppNotificationIcon = inAppBottomLayout?.findViewById(R.id.in_app_img)
    }

    // if cta present
    inAppTextView?.text = AndroidUtils.getRichTextFromHtml(inAppInfo.notificationText)
    if(!inAppInfo.notificationCta.isNullOrEmpty()) {
      inAppCtaTextView?.text = AndroidUtils.getRichTextFromHtml(inAppInfo.notificationCta)
      val ctaLink = inAppInfo.notificationCtaLink
      inAppCtaTextView?.setOnClickListener {
        NotificationActionAnalyticsHelper.logNotificationActionEvent(null,inAppNotificationModel,Constants.IN_APP,null,false)
        CommonNavigator.launchDeeplink(it.context, ctaLink, null)
        inAppTopLayout?.visibility = View.GONE
        inAppBottomLayout?.visibility = View.GONE
      }
    } else {
      inAppCtaTextView?.visibility = View.GONE
    }

    //setting notification icon
    if(ThemeUtils.preferredTheme == ThemeType.DAY) {
      Image.load(inAppInfo.notificationIconUrl).placeHolder(R.drawable.info_icon).into(inAppNotificationIcon)
    } else {
      Image.load(inAppInfo.notificationIconDarkUrl).placeHolder(R.drawable.info_icon_dark).into(inAppNotificationIcon)
    }

    // setting templates
    if(notificationtemplateResponse != null) {
      setTemplate(inAppInfo)
    }

    val inAppDisplayDuration = PreferenceManager.getPreference(AppStatePreference.IN_APP_DISPLAY_DURATION,5000L)
    AndroidUtils.getMainThreadHandler().postDelayed({
      inAppTopLayout?.visibility = View.GONE
      inAppBottomLayout?.visibility = View.GONE
    },inAppDisplayDuration)

    // delete shown in-app notification
    InAppNotificationUtils.markShownInAppNotificationStatus(inAppNotificationModel.baseInfo.uniqueId.toString(), Constants.SEEN)

    InAppNotificationUtils.inAppNotificationState = InAppNotificationUtils.InAppState.OTHER_IN_APP_SHOWN
  }

  private fun setTemplate(inAppInfo: InAppNotificationInfo) {
    // for text
    var textTemplateInfo = inAppInfo.notificaitonTextColorTemplateId?.let { getTemplateInfoIfExists(it, notificationtemplateResponse!!.rows) } ?:NotificationTemplateHelper.getDefaultColorTemplate().rows?.get(0)
      if(ThemeUtils.preferredTheme == ThemeType.DAY) {
        setTextTemplate(textTemplateInfo?.dayTextColor, textTemplateInfo?.dayBgColor)
      } else {
        setTextTemplate(textTemplateInfo?.darkTextColor, textTemplateInfo?.darkBgColor)
      }

    // for cta
    val ctaTemplateInfo = inAppInfo.ctaTextColorTemplateId?.let { getTemplateInfoIfExists(it, notificationtemplateResponse!!.rows) } ?: NotificationTemplateHelper.getDefaultColorTemplate().rows?.get(1)
      if(ThemeUtils.preferredTheme == ThemeType.DAY) {
        setCtaTemplate(ctaTemplateInfo?.dayTextColor, ctaTemplateInfo?.dayBgColor)
      } else {
        setCtaTemplate(ctaTemplateInfo?.darkTextColor, ctaTemplateInfo?.darkBgColor)
      }

  }

  private fun getTemplateInfoIfExists(templateId: String, inAppTemplateResponse: List<InAppTemplateInfo>?): InAppTemplateInfo? {
    inAppTemplateResponse?.forEach {
      if(templateId == it.id) {
        return it
      }
    }
    return null
  }

  private fun setTextTemplate(tVColor: String?, bgColor: String?) {
    bgColor?.let {
      inAppTextBg?.setBackgroundDrawable(CommonUtils.getLeftCorneredtBackgroundDrawable(Color.parseColor(bgColor)))
      inAppTextBg?.setBackgroundDrawable(CommonUtils.getLeftCorneredtBackgroundDrawable(Color.parseColor(bgColor)))
      inAppTextView?.setBackgroundColor(Color.parseColor(bgColor))
    }
    tVColor?.let {
      val textToShow = "<span style=\"color:${tVColor};\">${inAppNotificationModel?.baseInfo?.inAppInfo?.notificationText}</span>"
      inAppTextView?.text = AndroidUtils.getRichTextFromHtml(textToShow)
    }
  }

  private fun setCtaTemplate(tVColor: String?, bgColor: String?) {
    bgColor?.let {
      inAppCtaTextView?.setBackgroundDrawable(CommonUtils.getRightCorneredCtaBackgroundDrawable(Color.parseColor(bgColor)))
    }
    tVColor?.let {
      val textToShow = "<span style=\"color:${tVColor};\">${inAppNotificationModel?.baseInfo?.inAppInfo?.notificationCta}</span>"
      inAppCtaTextView?.text = AndroidUtils.getRichTextFromHtml(textToShow)
    }
  }

  fun bundleForSoftRefresh() : Bundle =
          bundleOf(BUNDLE_HOME_PREFERRED_TAB_ID to (currentTabId ?: preferredTabId))
}