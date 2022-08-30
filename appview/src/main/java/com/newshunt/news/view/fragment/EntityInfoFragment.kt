package com.newshunt.news.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.HomeTabsAdapter
import com.newshunt.appview.common.ui.helper.FragmentTransitionViewProvider
import com.newshunt.appview.common.ui.helper.FragmentTransitionViewProviderHost
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.EntityInfoViewModel
import com.newshunt.appview.common.viewmodel.EntityInfoViewModelFactory
import com.newshunt.appview.common.viewmodel.EntityUIEvent
import com.newshunt.appview.common.viewmodel.FollowNudgeViewModel
import com.newshunt.appview.databinding.LayoutEntityActivityBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.customview.GenericCustomSnackBar.Companion.showSnackBar
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.SnackMeta
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.common.pages.EntityInfoList
import com.newshunt.dataentity.common.pages.EntityInfoView
import com.newshunt.dataentity.common.pages.EntityType
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.analytics.NewsReferrerSource
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.CustomTabsUtil
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.listener.TextDescriptionSizeChangeListener
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.helper.handler.NudgeTooltipWrapper
import com.newshunt.news.util.NewsConstants
import kotlinx.android.synthetic.main.layout_entity_activity.topic_nh_share_view

class EntityInfoFragment : BaseFragment(), ErrorMessageBuilder.ErrorMessageClickedListener, ReferrerProviderlistener, ViewPager.OnPageChangeListener,
  TextDescriptionSizeChangeListener, FragmentTransitionViewProviderHost {


  private var homeTabAdapter: HomeTabsAdapter? = null
  private val referrerProviderHelper = ReferrerProviderHelper()
  private var isTabClicked = false
  private val TAG = "EntityInfo"

  companion object {
    @JvmStatic
    fun newInstance(intent: Intent) : EntityInfoFragment {
      val fragment = EntityInfoFragment()
      fragment.arguments = intent.extras
      return fragment
    }
  }

  private lateinit var binding: LayoutEntityActivityBinding
  private lateinit var entityInfoViewModel : EntityInfoViewModel
  private lateinit var section: String

  private var id : String = Constants.EMPTY_STRING
  private var entityType : String = Constants.EMPTY_STRING
  private var subId:  String = Constants.EMPTY_STRING
  private var launchedDeeplink = false
  private var pageReferrer: PageReferrer? = null
  private var referrerRaw:String? = null
  private var v4BackUrl = Constants.EMPTY_STRING
  private var currentInfoList: EntityInfoList? = null
  private var isInternalDeeplink: Boolean = false
  private var langCode: String? = UserPreferenceUtil.getUserLanguages()
  private var backPressHandled = false
  private lateinit var tooltipWrapper: NudgeTooltipWrapper
  private lateinit var nudgeVM : FollowNudgeViewModel

  override fun onCreate(savedState: Bundle?) {
    super.onCreate(savedState)
    arguments?.let {
      id = it.getString(NewsConstants.ENTITY_KEY) ?: Constants.EMPTY_STRING
      subId = it.getString(NewsConstants.SUB_ENTITY_KEY) ?: Constants.EMPTY_STRING
      entityType = it.getString(NewsConstants.ENTITY_TYPE) ?: Constants.EMPTY_STRING
      pageReferrer = it.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
      referrerRaw = it.getString(Constants.REFERRER_RAW)
      section = it.getString(NewsConstants.DH_SECTION)?: PageSection.NEWS.section
      isInternalDeeplink = it.getBoolean(Constants.IS_INTERNAL_DEEPLINK, false)
      referrerProviderHelper.addReferrerByProvider(pageReferrer)

      if (langCode.isNullOrEmpty()) {
        langCode = it.getString(NewsConstants.LANGUAGE_FROM_DEEPLINK_URL) ?: Constants.ENGLISH_LANGUAGE_CODE
      }

      if (CommonNavigator.isFromNotificationTray(pageReferrer)) {
        v4BackUrl = it.getString(Constants.V4BACKURL, Constants.EMPTY_STRING)
      }
    }

    entityInfoViewModel = ViewModelProviders.of(this, EntityInfoViewModelFactory(id, entityType, section)).
        get(EntityInfoViewModel::class.java)
    entityInfoViewModel.entityLiveData.observe(this, Observer {
      Logger.d(TAG , "Got the callback in the observer in entityInfoModel")
      if (it.isSuccess) {
        loadData(it.getOrNull())
      }
    })
    entityInfoViewModel.updateLangCode(langCode!!)
    entityInfoViewModel.entityErrorLiveData.observe(this, Observer {
      if (it.isFailure) {
        Logger.e(TAG, "Error in entity info response")
        val throwable = it.exceptionOrNull()
        throwable?.let {
          val error = ApiResponseOperator.getError(throwable)
          Logger.d(TAG, "Error is ${error.message}" )
          handleError(error)
        }
      }
    })
    entityInfoViewModel.entityUiLiveData.observe(this, Observer {
      when(it) {
        EntityUIEvent.BACK_BUTTON -> {
          backPressed(false)
          activity?.onBackPressed()
        }
      }
    })
    nudgeVM = ViewModelProviders.of(this).get(FollowNudgeViewModel::class.java)
  }

  private fun showFollowNudge(eventsInfo: EventsInfo) {
    val view: View
    tooltipWrapper = NudgeTooltipWrapper()
    val text = eventsInfo.activity?.attributes?.get("text") ?: Constants.EMPTY_STRING
    val time = eventsInfo.activity?.attributes?.get("tooltipDurationSec") ?: "10"
    if (entityType == EntityType.SOURCE.name || entityType == Constants.SOURCE_EPAPER || entityType == Constants.SOURCE_CHANNEL) {
      view = binding.root.findViewById<ConstraintLayout>(R.id.follow_button_profile)
      tooltipWrapper.showFollowTooltip(requireContext(), R.layout.nudge_tooltip_follow_middle_arrow,
              text,
              time.toLong(), view)
    } else {
      view = binding.root.findViewById<ConstraintLayout>(R.id.follow_button)
      tooltipWrapper.showFollowTooltip(requireContext(), R.layout.nudge_tooltip_follow_right_arrow,
              text,
              time.toLong(), view)
    }
    nudgeVM.nudgeShown(eventsInfo.id)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {

      @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
      fun onLifecycleResume() {
      }
    })
  }

  override fun handleBackPress(): Boolean {
    backPressed(true)
    return false
  }

  private fun backPressed(systemBackKeyPressed: Boolean) {
    if (backPressHandled) {
      backPressHandled = false
    }
    if (!CommonUtils.isEmpty(v4BackUrl)) {
      val pageReferrer = if (!CommonUtils.isEmpty(subId))
        PageReferrer(NewsReferrer.SUB_TOPIC, subId)
      else
        PageReferrer(NewsReferrer.TOPIC, id)
      CommonNavigator.launchDeeplink(requireContext(), v4BackUrl, pageReferrer)
      backPressHandled = true
    } else if (activity != null && NewsNavigator.shouldNavigateToHome(activity, pageReferrer, systemBackKeyPressed,referrerRaw)) {
      if (!isInternalDeeplink || NewsNavigator.isActivityTaskRoot(activity!!)) {
        val pageReferrer = PageReferrer(NewsReferrer.SUB_TOPIC, id)
        pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
        NewsNavigator.navigateToHomeOnLastExitedTab(activity, pageReferrer)
        backPressHandled = true
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = DataBindingUtil.inflate(inflater, R.layout.layout_entity_activity, container,false)
    binding.setVariable(BR.vm, entityInfoViewModel)
    binding.entityNerHeaderView.textChangeListener = this
    arguments?.let {
      val snackData = it.getSerializable(NotificationConstants.SNACK_BAR_META) as SnackMeta?
      snackData?.let { showSnackbarForStickyNotification(binding.root,snackData) }
    }
    hide3DotsIfNoReg()
    return binding.root
  }

  override fun onStart() {
    super.onStart()
    entityInfoViewModel.onViewStarted()
  }


  private fun loadData(entityInfoList: EntityInfoList?) {
    if (entityInfoList == null) {
      Logger.e(TAG , "Entity info is null hence not updating the UI")
      return
    }

    val isKidsChanges = areChildsChanges(oldList = currentInfoList, newList = entityInfoList)
    Logger.d(TAG , "Entity info list received hence updating the UI and kids changes is $isKidsChanges")
    currentInfoList = entityInfoList
    currentInfoList?.parent?.pageEntity?.defaultTabId = "a3617d6e958a315152f3c08c3f2b5f1b.video"
    if (isKidsChanges) {
      val list = if (CommonUtils.isEmpty(entityInfoList.kids)) {
        listOf(entityInfoList.parent)
      } else if (entityInfoList.parent.pageEntity.showParentInTab) {
        val mutList = mutableListOf(entityInfoList.parent)
        mutList.addAll(entityInfoList.kids?: listOf())
        mutList
      } else {
        entityInfoList.kids?: listOf()
      }

      homeTabAdapter = HomeTabsAdapter(childFragmentManager, null,
              videoRequester = VideoRequester(-1), section = section,
              parentEntityId = entityInfoList.parent.parentId, sourceId = id,
              sourceType = entityInfoList.parent.pageEntity.subType,
              nhShareView = topic_nh_share_view, createPost = binding.entityCreatePostView)
      homeTabAdapter?.updateList(list.map { it.pageEntity })
      binding.categoriesPager.pagingEnabled = CustomTabsUtil.tabsSwipeEnabled()
      binding.categoriesPager.adapter = homeTabAdapter
      binding.slidingTabsTopicCategories.setDrawBottomLine(false)
      binding.slidingTabsTopicCategories.setTabTextColor(
        ThemeUtils.getThemeColorByAttribute(context, R.attr.tab_title_select_color),
        ThemeUtils.getThemeColorByAttribute(context, R.attr.tab_title_color))
      binding.categoriesPager.addOnPageChangeListener(this)
      binding.slidingTabsTopicCategories.setViewPager(binding.categoriesPager)
      binding.slidingTabsTopicCategories.setTabClickListener({ v, position -> isTabClicked = true })
      binding.progressbar.visibility = View.GONE

      pageReferrer?.let {
        if (PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)) {
          nudgeVM.nudges(it, entityInfoList.parent.i_isFollowin(), arguments)
            .observe(this, Observer { events ->
              Logger.d(TAG, "loadData: $events")
              events.firstOrNull()?.let { showFollowNudge(it) }
            })
        }
      }
      updateDefaultTabs(list)
    }
    binding.setVariable(BR.entity, entityInfoList)
    binding.executePendingBindings()
  }

  /**
   * Set Default tab from deeplink else set default tab from parent of EntityResponse.
   * If both empty or null then it will be first tab.
   */
  private fun updateDefaultTabs(list:List<EntityInfoView>){
    if(list.size > 1) {
      val id = if (!CommonUtils.isEmpty(subId)) subId else currentInfoList?.parent?.pageEntity?.defaultTabId
      if(!id.isNullOrEmpty()) {
        subId = id
        val index = getIndexOfChild(list)
        if (index != -1) {
          binding.categoriesPager.currentItem = index
        }
      }
    }
  }

  private fun getIndexOfChild(list : List<EntityInfoView>) : Int {
    list.forEachIndexed { index, item ->
      if (item.pageEntity.id == subId || item.pageEntity.legacyKey == subId) return index
    }
    return -1
  }

  private fun handleError(error: BaseError) {
    binding.entityCreatePostView.hide()
    binding.errorParent.visibility = View.VISIBLE
    binding.progressbar.visibility = View.GONE
    val errorMessageBuilder = ErrorMessageBuilder(binding.errorParent, requireContext(), this)
    errorMessageBuilder.showError(error)
  }

  override fun onRetryClicked(view: View?) {
    binding.errorParent.visibility = View.GONE
    binding.progressbar.visibility = View.VISIBLE
    entityInfoViewModel.onViewStarted()
  }

  override fun onNoContentClicked(view: View?) {
    activity?.finish()
    val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.fromName(section))
    val navigationIntent = NavigationEvent(CommonNavigator.getNewsHomeIntent(view?.context,
            false, prevNewsAppSection?.id, prevNewsAppSection?.appSectionEntityKey,
            null,
            false))
    NavigationHelper.navigationLiveData.postValue(navigationIntent)
  }

  private fun areChildsChanges(oldList: EntityInfoList?, newList: EntityInfoList?) : Boolean {

    if (oldList == null && newList != null) {
      return  true
    }

    if (oldList?.kids?.size?:0 != newList?.kids?.size?:0) {
      return true
    }

    oldList?.kids?.forEachIndexed { index, it ->
      newList?.kids?.get(index)?.pageEntity?.let {page ->
        if (!it.pageEntity.equalsForHome(page)) {
          return true
        }
      }

    }
    return false
  }

  override fun getProvidedReferrer(): PageReferrer? {
    return referrerProviderHelper.providedPageReferrer
  }

  override fun getReferrerEventSection(): NhAnalyticsEventSection {
    if (::section.isInitialized)
      return AnalyticsHelper2.getSection(section)
    else
      return NhAnalyticsEventSection.NEWS
  }

  private fun addReferrerByPosition(position: Int) {

    if (homeTabAdapter == null
            || homeTabAdapter?.pageList == null
            || position >= homeTabAdapter?.pageList?.size ?: 0
            || homeTabAdapter?.pageList?.get(position) == null) {
      return
    }
    var referrer: NhAnalyticsReferrer? = null
    val pageType = PageType.fromName(homeTabAdapter?.pageList?.get(position)?.entityType ?: "")
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
              PageReferrer(referrer,
                      homeTabAdapter?.pageList?.get(position)?.id, null)

      referrerProviderHelper.addReferrerByProvider(currentPageReferrer)
    }
  }

  override fun onPageSelected(position: Int) {
    homeTabAdapter?.let {
      val action = if (isTabClicked) NhAnalyticsUserAction.CLICK else NhAnalyticsUserAction.SWIPE
      it.onPageSelected(action)
      NhAnalyticsAppState.getInstance().action = action
      isTabClicked = false
      addReferrerByPosition(position)
      referrerProviderHelper.setAction(action)
    }
  }

  override fun onPageScrollStateChanged(state: Int) {

  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

  }

  override fun onHiddenChanged(hidden: Boolean) {
    super.onHiddenChanged(hidden)
    Logger.i(TAG, "onHiddenChange called $hidden")
    homeTabAdapter?.currentFragment?.onHiddenChanged(hidden)
  }

  fun showSnackbarForStickyNotification(view: View?,snackMeta: SnackMeta) {
    if(!snackMeta.message.isNullOrEmpty()) {
      showSnackBar(view!!, CommonUtils.getApplication(), snackMeta.message!!,
              snackMeta.duration, null, null, snackMeta.ctaText, View.OnClickListener { v: View? ->
        CommonNavigator.launchDeeplink(context, snackMeta.ctaUrl, null)
      }).show()
    }
  }

  private fun hide3DotsIfNoReg() {
    val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)
    binding.toolbar.dislikeIcon.visibility = if(isRegistered) View.VISIBLE else View.GONE
  }

  override fun onDescriptionExpanded(expanded: Boolean, photoId: String?) {
    val fragment = NERDescriptionBottomSheetFragment.instance(currentInfoList?.parent?.pageEntity?.deeplinkUrl)
    fragment.show(childFragmentManager,"NERDescriptionBottomSheetFragment")
  }

  override fun isStoryExpanded(photoId: String?): Boolean {
    return false
  }

  override fun getFragmentTransitionViewProvider(): FragmentTransitionViewProvider? {
    return homeTabAdapter?.currentFragment as? FragmentTransitionViewProvider
  }

}