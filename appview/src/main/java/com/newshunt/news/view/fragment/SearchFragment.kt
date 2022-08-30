/*
 * Created by Rahul Ravindran at 26/9/19 6:58 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.HomeTabsAdapter
import com.newshunt.appview.common.ui.helper.FragmentTransitionViewProvider
import com.newshunt.appview.common.ui.helper.FragmentTransitionViewProviderHost
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.interceptor.HeaderInterceptor
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer.SEARCH
import com.newshunt.dataentity.news.analytics.NewsReferrerSource
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.search.AggrMultivalueResponse
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.CustomTabsUtil
import com.newshunt.dhutil.helper.appsection.DefaultAppSectionsProvider
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.customview.CustomViewPager
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.listener.AssetCountListener
import com.newshunt.news.listener.AssetCountsUpdateListener
import com.newshunt.news.listener.AutoPlayBackEventListener
import com.newshunt.news.listener.AutoPlayCallbackListener
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.news.view.activity.SearchActivity
import com.newshunt.news.view.activity.SearchActvityInterface
import com.newshunt.news.view.customview.SlidingTabLayout
import com.newshunt.news.view.entity.NewsListPayloadType
import com.newshunt.news.view.entity.SearchProps
import com.newshunt.news.view.listener.FragmentScrollListener
import com.newshunt.news.view.listener.MenuListenerProvider
import com.newshunt.news.view.listener.MenuOptionClickListener

/**
 * Shows search results
 * Has Adapter with multiple cards fragments
 * @author satosh.dhanymaraju
 */
class SearchFragment : BaseSupportFragment(), AutoPlayCallbackListener, AssetCountListener,
        androidx.viewpager.widget.ViewPager.OnPageChangeListener, MenuListenerProvider, View
        .OnTouchListener, ReferrerProviderlistener, FragmentTransitionViewProviderHost {

    private var currentSectionId: String? = null
    private val LOG_TAG = "SearchFragment"
    private lateinit var viewPager: CustomViewPager
    private lateinit var tabsLayout: SlidingTabLayout
    private lateinit var tabsContainer: RelativeLayout
    private lateinit var tabsAdapter: HomeTabsAdapter
    private val referrerProviderHelper = ReferrerProviderHelper()
    private lateinit var searchBox : NHTextView
    private var isTabClicked = false
    private lateinit var errorTitle : TextView
    private lateinit var errorSubTitle : TextView
    private lateinit var errorIcon : NHImageView
    private lateinit var shimmerContainer: ConstraintLayout
    private lateinit var videoRequester : VideoRequester
    private lateinit var crossView: ImageView
    private var videoBackListener : AutoPlayBackEventListener? = null

    private var query: SearchSuggestionItem? = null
    private var activityInterface: SearchActvityInterface? = null
    private var querySubmitTime : Long? = null

    private var searchType: String? = null

    private var referrer: PageReferrer? = null
    private var referrerRaw: String? = null

    // for restoring state
    private var lastTabIndex : Int? = null
    private var completedRequests : ArrayList<String> = arrayListOf()

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        query = arguments?.getSerializable(Constants.BUNDLE_SEARCH_QUERY) as? SearchSuggestionItem
        querySubmitTime = arguments?.getLong(Constants.BUNDLE_QUERY_SUBMIT_TIME)
        searchType = arguments?.getString(Constants.BUNDLE_SEARCH_TYPE)
        referrer = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
        currentSectionId = arguments?.getString(Constants.APP_SECTION_ID,
                DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id)
        referrerProviderHelper.addReferrerByProvider(referrer)
        val searchContext = arguments?.getString(Constants.BUNDLE_SEARCH_CONTEXT)?:""
        val props = SearchProps(NewsListPayloadType.PAYLOAD_SEARCH, query?.suggestion?:"",
                query?.searchParams, searchContext, false, query?.requestId?:"")
        videoRequester = VideoRequester((activity as NewsBaseActivity).activityId)
        /* arguments + overrides */
        val bundleForTabsAdapter = Bundle(arguments).apply {
            putString(NewsConstants.DH_SECTION, PageSection.SEARCH.section) // for cardsfragment
        }
        tabsAdapter = HomeTabsAdapter(
                childFragmentManager,
                FragmentScrollListener { },
                videoRequester,
         { i, npe ->
             val newReferrer = PageReferrer(SEARCH).apply {
                npe?.id?.let { id = it }
            }
            activityInterface?.updateReferrer(newReferrer)
         }, PageSection.SEARCH.section,
                searchQuery = query,
                extraArguments = bundleForTabsAdapter)

        // to prevent crash when activity's oncreate did not finish and viewmodel not initialised
        AndroidUtils.getMainThreadHandler().post {
            observeData()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityInterface = (context as? SearchActvityInterface)
    }

    override fun onDetach() {
        super.onDetach()
        activityInterface = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val inflate = inflater.inflate(R.layout.layout_fragment_search, container, false)

        viewPager = inflate.findViewById<CustomViewPager>(R.id.search_view_pager).apply {
            addOnPageChangeListener(this@SearchFragment)
            adapter = tabsAdapter
        }

        viewPager.pagingEnabled = CustomTabsUtil.tabsSwipeEnabled()

        tabsLayout = inflate.findViewById<SlidingTabLayout>(R.id.search_tabs).apply {
            setTabTextColor(
                ThemeUtils.getThemeColorByAttribute(context, R.attr.tab_title_select_color),
                ThemeUtils.getThemeColorByAttribute(context, R.attr.tab_title_color))
            setDrawBottomLine(false)
            setCustomTabView(R.layout.tab_item, R.id.tab_item_title, R.id.tab_item_image)
            setDisplayDefaultIconForEmptyTitle(true)
            setViewPager(viewPager)
        }
        tabsLayout.setTabClickListener { v, position -> isTabClicked = true }
        tabsContainer = inflate.findViewById(R.id.tabsRL)

        searchBox = inflate.findViewById<NHTextView>(R.id.search_box).apply {
            setText(query?.suggestion)
            setOnClickListener { activityInterface?.editQuery(query) }
        }

        inflate.findViewById<ImageView>(R.id.toolbar_back_button).setOnClickListener {
            if (NewsNavigator.shouldNavigateToHome(activity, referrer, false,referrerRaw)) {
                NewsNavigator.navigateToHomeOnLastExitedTab(activity, PageReferrer(SEARCH))
            } else {
                activity?.onBackPressed()
            }
        }

        errorTitle = inflate.findViewById(R.id.error_title)
        errorSubTitle = inflate.findViewById(R.id.error_subtitle)
        errorIcon = inflate.findViewById(R.id.error_icon)
        shimmerContainer = inflate.findViewById(R.id.shimmer_container)
        updateViews(LoadingStates.LOADING)
        searchBox.setOnTouchListener(this)
        return inflate
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p1?.action) {
            MotionEvent.ACTION_UP -> {
                val clickedOnCross = searchBox.compoundDrawables[2] != null && p1?.rawX!! >= (searchBox.right - searchBox.compoundDrawables[2].bounds.width())
                if (clickedOnCross) {
                    activityInterface?.editQuery(query?.copy(suggestion = ""))
                } else {
                    activityInterface?.editQuery(query)
                }
            }
        }
        return true
    }


    override fun onVideoCardClick(backPressListener: AutoPlayBackEventListener?) {
        this.videoBackListener = backPressListener
    }

    override fun onBuzzDetailBackPress() {
        if (videoBackListener != null && videoBackListener?.handleBackPress() == false) {
            videoBackListener = null
        }
    }

    override fun handleBackPress() : Boolean {

       return if (videoBackListener != null) {
            onBuzzDetailBackPress()
            true
        } else if (NewsNavigator.shouldNavigateToHome(activity, referrer, true,referrerRaw)) {
           NewsNavigator.navigateToHomeOnLastExitedTab(activity, PageReferrer(SEARCH))
           false
       } else {
           false
       }
    }


    override fun setAssetUpdateCountListener(listener: AssetCountsUpdateListener) {}

    override fun onPageSelected(position: Int) {
        val action = if (isTabClicked) NhAnalyticsUserAction.CLICK else NhAnalyticsUserAction.SWIPE
        isTabClicked = false
        referrerProviderHelper.setAction(action)
        addReferrerByPosition(position)
    }

    private fun addReferrerByPosition(position: Int) {
        if (!::tabsAdapter.isInitialized) {
            return
        }
        if (tabsAdapter.pageList == null
                || position >= tabsAdapter.pageList?.size ?: 0
                || tabsAdapter.pageList?.get(position) == null) {
            return
        }
        var referrer: NhAnalyticsReferrer? = null
        val pageType = PageType.fromName(tabsAdapter.pageList?.get(position)?.entityType ?: "")
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
                    PageReferrer(referrer, tabsAdapter.pageList?.get(position)?.id, null)

            referrerProviderHelper.addReferrerByProvider(currentPageReferrer)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): SearchFragment {
            return SearchFragment().apply {
                arguments = bundle
            }
        }
        private val BUNDLE_INDEX = "tab_index"
        private val BUNDLE_COMP_REQ = "completed_requests"
    }

    private fun observeData() {
        activityInterface?.searchViewModel()?.searchResults?.data()?.observe(this, Observer { responseResult ->

            if (responseResult.isFailure) {
                val exception = responseResult.exceptionOrNull()
                val baseError = ApiResponseOperator.getError(exception)
                updateViews(LoadingStates.ERROR)
                errorTitle.text = ""
                errorSubTitle.text = baseError?.message
                        ?: CommonUtils.getString(R.string.error_generic)
                return@Observer
            }

            val response = responseResult.getOrNull()
            if (response?.query == null) {
                Logger.e(LOG_TAG, "query null. ignored response")
                return@Observer
            }
            Logger.i(LOG_TAG, "frag : got ${response.response.aggrs?.values?.size} aggrs," +
                    "${response.response.rows?.size} rows ")
            val aggrResp = response.response
            // As livedata pushes currentValue on subscription, we need to ensure that the we
            // ignore previous event. Want bus like behaviour.
            when {
                response.query != query?.suggestion -> {
                    // this response was for another query
                    Logger.d(LOG_TAG, "ignored ${response?.query}'s response. cur=$query")
                }

                (querySubmitTime ?: -1) > response.responseTs -> {
                    // this response was generated before this query submitted
                    Logger.d(LOG_TAG, "got previous event. ignoring.")
                }

                (aggrResp.emptySearchTitle.isNullOrEmpty() || aggrResp.emptySearchSubTitle.isNullOrEmpty()).not() -> {
                    // error occurred.
                    updateViews(LoadingStates.ERROR)
                    fireEvent(aggrResp)
                    errorTitle.setHtmlText(aggrResp.emptySearchTitle)
                    errorSubTitle.apply {
                        setHtmlText(aggrResp.emptySearchSubTitle)
                        if (aggrResp.correctedSearchParams?.isEmpty() == false) {
                            setOnClickListener { view ->
                                Logger.d(LOG_TAG, "re-init search with ${view.tag}")
                                val params = (view.tag as? Map<String, String>) ?: emptyMap()
                                val q = aggrResp.correctedSearchKeyword ?: ""
                                query = SearchSuggestionItem(q, q, params,
                                        requestId = HeaderInterceptor.generateRequestId()).also {
                                    SearchAnalyticsHelper.logSearchInitiated( it.requestId,
                                            nonNullReferrr(), Constants.SEARCH_HAS_USER_TYPED_NA, aggrResp.experiment)
                                    querySubmitTime = System.currentTimeMillis()
                                    activityInterface?.searchViewModel()?.insertQueryToRecent(q, it)
                                    activityInterface?.searchViewModel()?.submit(it)
                                    searchBox.text = it.suggestion
                                }
                            }
                            tag = aggrResp.correctedSearchParams
                        }
                    }
                    // show error page, but no referrer change.
                }

                (aggrResp.aggrs?.values?.isEmpty() == false) -> {
                    // tabs received.
                    updateViews(LoadingStates.SUCCESS)
                    fireEvent(aggrResp, false)
                    val tabs = aggrResp.aggrs?.values?.map {
                        PageEntity(id= it.id,
                                contentRequestMethod = it.contentRequestMethod,
                                contentUrl = it.contentUrl,
                                name = it.name,
                                entityType = it.entityType?:"",
                                entityLayout = it.entityLayout,
                                viewOrder = it.viewOrder)
                    } ?: emptyList()
                    tabsAdapter.updateList(tabs)
                    viewPager.adapter = tabsAdapter
                    tabsLayout.setViewPager(viewPager)
                    lastTabIndex?.let {
                        viewPager.currentItem = it.coerceIn(0 until tabs.size)
                        lastTabIndex = null
                    }
                }

                aggrResp.error != null -> { // HTTP error or NoConnectivity error
                    Logger.e(LOG_TAG, "Renderable error ${aggrResp.error?.message}")
                    updateViews(LoadingStates.ERROR)
                    fireEvent(aggrResp)
                    errorTitle.text = ""
                    errorSubTitle.text = aggrResp.error?.message
                            ?: CommonUtils.getString(R.string.error_generic)
                }

                else -> {
                    Logger.e(LOG_TAG, "everything is empty. showed generic error")
                    updateViews(LoadingStates.ERROR)
                    fireEvent(aggrResp)
                    errorTitle.text = ""
                    errorSubTitle.text = CommonUtils.getString(R.string.error_generic)
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BUNDLE_INDEX, viewPager?.currentItem)
        outState.putStringArrayList(BUNDLE_COMP_REQ, completedRequests)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lastTabIndex = savedInstanceState?.getInt(BUNDLE_INDEX)
        completedRequests = savedInstanceState?.getStringArrayList(BUNDLE_COMP_REQ) ?: arrayListOf()
    }

    override fun menuOptionClickListener(): MenuOptionClickListener? {
        if (tabsAdapter != null) {
            val currentFragment = tabsAdapter.currentFragment
            if (currentFragment is MenuOptionClickListener) {
                return currentFragment
            }
        }
        return null
    }

    override fun onDestroy() {
        if (query?.requestId !in completedRequests) {
            Logger.i(LOG_TAG, "${query?.suggestion} request cancelled. $completedRequests")
            query?.let {
                val exp = hashMapOf<String, String>(Constants.EXP_SEARCH_NO_RES_REASON to
                        Constants.EXP_SEARCH_NO_RES_REASON_USER_CANCEL)
                SearchAnalyticsHelper.logSearchExecuted(it.requestId, it.suggestion,
                        nonNullReferrr(), -1, searchType?: "query", exp, it.itemId)
            }
        }
        super.onDestroy()
    }

    /**
     * Event should be fired on receiving error also.
     * BE always returns 200 OK, even for no content. 'experiment' field in response should
     * contains reason for error, if any. If not present, client will add 'client_error' in the
     * experiment. This is to cover SocketTimeOut, http 500 and such cases.
     *
     */
    private fun fireEvent(aggrResp: AggrMultivalueResponse, isError: Boolean = true) {
        val query1 = query?: return
        val experiments = aggrResp.experiment?: hashMapOf()
        if(isError)
            experiments.getOrPut(Constants.EXP_SEARCH_NO_RES_REASON) {
                Constants.EXP_SEARCH_NO_RES_REASON_CL_ERR
            }
        SearchAnalyticsHelper.logSearchExecuted(query1.requestId,
                query1.suggestion,
                nonNullReferrr(),
                aggrResp.total ?: -1,
                searchType ?: "query",
                experiments,
                query1.itemId
        )
        completedRequests.add(query1.requestId)
    }

    private fun nonNullReferrr() = (activityInterface?.providedReferrer)
            ?: SearchActivity.SEARCH_REFERRER


    private infix fun View?.visible(visible: Boolean): Unit? {
        return this?.setVisibility((if (visible) View.VISIBLE else View.GONE))
    }

    private fun TextView?.setHtmlText(text: String?) {
        if(this == null || text.isNullOrEmpty()) return
        this.text = android.text.Html.fromHtml(text)
    }


    private fun updateViews(states: LoadingStates) {
        shimmerContainer visible (states == LoadingStates.LOADING)

        errorSubTitle visible (states == LoadingStates.ERROR)
        errorTitle visible (states == LoadingStates.ERROR)
        errorIcon visible (states == LoadingStates.ERROR)

        viewPager visible (states == LoadingStates.SUCCESS)
        tabsLayout visible (states == LoadingStates.SUCCESS)
        tabsContainer visible (states == LoadingStates.SUCCESS)
    }

    enum class LoadingStates {
        SUCCESS, ERROR, LOADING
    }

    override fun getProvidedReferrer(): PageReferrer? {
        return referrerProviderHelper.providedPageReferrer
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.SEARCH
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.i(LOG_TAG, "onHiddenChange called $hidden")
        tabsAdapter?.currentFragment?.onHiddenChanged(hidden)
    }

    override fun getFragmentTransitionViewProvider(): FragmentTransitionViewProvider? {
        return tabsAdapter.currentFragment as? FragmentTransitionViewProvider
    }
}