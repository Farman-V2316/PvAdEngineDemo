package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.ui.helper.LiveDataEventHelper
import com.newshunt.appview.common.viewmodel.FollowHomeViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.FollowReferrerSource
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.search.SearchActionType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.appsection.DefaultAppSectionsProvider
import com.newshunt.dhutil.helper.behavior.BehaviorUtils
import com.newshunt.dhutil.helper.behavior.FixedBottomViewGroupBarBehavior
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.view.customview.NHTabView
import com.newshunt.helper.setUpSearchbarHint
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.helper.DefaultNavigatorCallback
import com.newshunt.news.helper.HomeSearchBarView
import com.newshunt.news.util.NewsConstants
import com.newshunt.sso.SSO
import com.newshunt.sso.view.view.ProfilePicHelper
import kotlinx.android.synthetic.main.layout_common_top_bar.nh_notification_icon

class FollowHomeFragment : BaseFragment(), ReferrerProviderlistener {

  companion object {
    @JvmStatic
    fun newInstance(intent: Intent) : FollowHomeFragment {
      val fragment = FollowHomeFragment()
      fragment.arguments = intent.extras
      return fragment
    }
  }

  private val referrerProviderHelper = ReferrerProviderHelper()

  private lateinit var followHomeViewModel: FollowHomeViewModel
  private lateinit var currentSectionId: String
  private lateinit var tabView: NHTabView
  private lateinit var notificationDot: ImageView
  private lateinit var coordinatorLayout: CoordinatorLayout
  private lateinit var appBar: AppBarLayout
  private lateinit var searchView: NHTextView
  private lateinit var logo: ImageView
  private lateinit var searchBarView : HomeSearchBarView
  private lateinit var viewPager: ViewPager

  private var needDoubleBackExitViaDeeplink: Boolean = false
  private var pageReferrer: PageReferrer? = null
  private var isBottomBarFixed = false
  private var acceptableTimeStamp = System.currentTimeMillis()
  private var userId = SSO.getInstance().userDetails?.userID

  override fun onCreate(savedState: Bundle?) {
    super.onCreate(savedState)
    followHomeViewModel = ViewModelProviders.of(this).get(FollowHomeViewModel::class.java)
    setPageReferrerFromIntent()
    isBottomBarFixed = PreferenceManager.getPreference(AppStatePreference.BOTTOM_BAR_FIXED, false)
  }


  private fun setPageReferrerFromIntent() {
    val bundle = arguments

    var pageReferrerFromBundle : PageReferrer? = null
    if (bundle != null) {
      pageReferrerFromBundle = bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
      currentSectionId = bundle.getString(Constants.APP_SECTION_ID, DefaultAppSectionsProvider
          .DefaultAppSection.FOLLOW.id)
      if (CommonNavigator.isFromNotificationTray(pageReferrerFromBundle) || CommonNavigator.isDeeplinkReferrer(pageReferrerFromBundle)) {
        needDoubleBackExitViaDeeplink = bundle.getBoolean(Constants.DEEP_LINK_DOUBLE_BACK_EXIT)
        NewsAnalyticsHelper.updateAppState(pageReferrerFromBundle)
      }
    }

    if (pageReferrerFromBundle == null) {
      pageReferrerFromBundle = PageReferrer(NhGenericReferrer.ORGANIC)
      pageReferrerFromBundle.referrerSource = FollowReferrerSource.FOLLOW_HOME_VIEW
    }

    pageReferrer = pageReferrerFromBundle
    referrerProviderHelper.addReferrerByProvider(pageReferrer)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.activity_follow_tab_parent, container, false)
    val childContainer = view.findViewById(R.id.child_container) as LinearLayout
    LayoutInflater.from(context).inflate(R.layout.activity_follow_home, childContainer)
    initView(view)
//    setUpSearchbarHint(searchView, SearchLocation.FollowHome,
//            { referrerProviderHelper.youngestPageReferrer },
//            SearchPayloadContext(
//                    action = SearchActionType.UNIFIED.name,
//                    section = currentSectionId)
//    )
    return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    acceptableTimeStamp = System.currentTimeMillis()

    LiveDataEventHelper.newGroupLiveData.observe(viewLifecycleOwner, Observer {
      if (it.timestamp < acceptableTimeStamp) {
        return@Observer
      }
      //On new group creation or join, refresh the cards fragment to show the new group
      (viewPager.adapter as? FollowHomeAdapter?)?.currentFragment?.refresh()
    })

    viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {

      @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
      fun onLifecycleResume() {
      }
    })
    AppSettingsProvider.getNotificationLiveData().observe(viewLifecycleOwner, Observer {
      nh_notification_icon.onNotificationEventChanged(it)
    })
    SSO.getInstance().userDetailsLiveData.observe(viewLifecycleOwner, Observer {
      //User id changed, refresh the cards fragment.
      if (userId != null && userId != it.userID) {
        (viewPager.adapter as? FollowHomeAdapter?)?.currentFragment?.refresh()
      }
      userId = it.userID
    })
  }

  private fun initView(rootView: View) {
    tabView = rootView.findViewById(R.id.bottom_tab_bar)
    tabView.setLifecycleOwner(this)
    notificationDot = rootView.findViewById(R.id.notification_dot_icon)
    val bottomLayout = rootView.findViewById<LinearLayout>(R.id.scrollable_bottom_container)
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
    tabView.setCurrentSectionId(currentSectionId)
    showOrHideBottomBar(true, rootView)

    coordinatorLayout = rootView.findViewById<View>(R.id.news_home_coordinator) as CoordinatorLayout
    appBar = rootView.findViewById<View>(R.id.app_bar_layout) as AppBarLayout
    logo = rootView.findViewById(R.id.actionbar_image)
    searchView = rootView.findViewById(R.id.global_search)
    searchView.visibility = View.GONE
//    searchBarView = HomeSearchBarView(this, logo, searchView)

    viewPager = rootView.findViewById(R.id.follow_home_view_pager)
    viewPager.adapter = FollowHomeAdapter(childFragmentManager)
    referrerProviderHelper.addReferrerByProvider(PageReferrer(NewsReferrer.FOLLOW_STAR_SECTION))
    BehaviorUtils.enableTopbarScrolling(rootView.findViewById(R.id.news_action_bar))
    ProfilePicHelper(this, rootView.findViewById(R.id.profile_image), getPageReferrer(), this, DefaultNavigatorCallback())
  }

  protected fun showOrHideBottomBar(show: Boolean, rootView: View) {
    if (show) {
      rootView.findViewById<View>(R.id.bottom_tab_bar).visibility = View.VISIBLE
    } else {
      rootView.findViewById<View>(R.id.bottom_tab_bar).visibility = View.GONE
    }
  }

  override fun getProvidedReferrer(): PageReferrer {
    return referrerProviderHelper.providedPageReferrer
  }

  override fun getReferrerEventSection(): NhAnalyticsEventSection {
    return NhAnalyticsEventSection.FOLLOW
  }

  override fun getLatestPageReferrer(): PageReferrer? {
    return getPageReferrer()
  }

  override fun handleBackPress(): Boolean {
    if (!::tabView.isInitialized) return false
    tabView.launchNewsSection(DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id, true)
    return true
  }

  private fun getPageReferrer(): PageReferrer? {
    return referrerProviderHelper.referrerQueue?.yongest
  }

  inner class FollowHomeAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    var currentFragment: CardsFragment? = null

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
      return CardsFragment.create(bundleOf(Constants.PAGE_ID to "follow",
              NewsConstants.DH_SECTION to PageSection.FOLLOW.section,
              Constants.BUNDLE_ACTIVITY_REFERRER_FLOW to getProvidedReferrer(),
              Constants.DISABLE_NP_CACHE to true, CardsFragment.DISABLE_MORE_NEWS_TOOLITP to true))
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, fragment: Any) {
      super.setPrimaryItem(container, position, fragment)
      (fragment as? CardsFragment?)?.let {
        currentFragment = it
      }
    }

    override fun getCount(): Int {
      return 1
    }

  }
}