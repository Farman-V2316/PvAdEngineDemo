/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.fragment.LocationExpansionFragment
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.common.view.customview.SnackBarActionClickListener
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.transaction
import com.newshunt.news.di.DaggerSearchComponent
import com.newshunt.news.di.SearchModule
import com.newshunt.news.model.sqlite.SearchDatabase
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.SearchActvityInterface
import com.newshunt.news.view.fragment.LocationPresearchFragment
import com.newshunt.search.viewmodel.SearchViewModel
import javax.inject.Inject


/**
 * @author priya.gupta
 * A activity which shows search results for Locations.
 */
class LocationSelectionActivity : NHBaseActivity(), ReferrerProviderlistener, View
.OnClickListener, SnackBarActionClickListener, SearchActvityInterface {

    private lateinit var searchEditBox: NHTextView
    private lateinit var searchDismissContainer: FrameLayout
    private lateinit var searchReloadContainer: FrameLayout
    private lateinit var searchContainer: ConstraintLayout
    private lateinit var nextButton: FrameLayout

    private lateinit var backButtonContainer: FrameLayout
    private var snackbar: Snackbar? = null

    private var isSystemBackKeyPressed = true

    private val searchPayloadContext = SearchPayloadContext(garbage = null, section = "news", entityType = null, entityId = null, postId = null, parentPostId = null, groupId = null, action = "unified")
    private val requestType = SearchRequestType.LOCATION

    @Inject
    lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtils.preferredTheme.themeId)
        setContentView(R.layout.activity_location_selection)

        searchEditBox = findViewById(R.id.search_editText)

        // On the basis of arguments hide or show save button
        nextButton = findViewById(R.id.toolbar_next_button)

        hideOrShowNextButton()

        backButtonContainer = findViewById(R.id.toolbar_back_button)
        searchContainer = findViewById(R.id.search_container)
        searchContainer.setOnClickListener(this)
        searchDismissContainer = findViewById(R.id.search_dismiss_container)
        searchReloadContainer = findViewById(R.id.search_reload_container)
        searchDismissContainer.setOnClickListener(this)
        searchReloadContainer.setOnClickListener(this)
        nextButton.setOnClickListener(this)


        val showFollowButton = intent.getBooleanExtra(Constants.SHOW_FOLLOW_BUTTON, false)
        val locationsGroupFragment = LocationExpansionFragment()

        val bundle = bundleOf(Constants.IS_LOCATION_SEARCH to true,
                Constants.SHOW_FOLLOW_BUTTON to showFollowButton,
                Constants.POST_NEWS_PAGE_ENTITY_CHANGE_EVENTS to true)
        locationsGroupFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
                .replace(R.id.location_list, locationsGroupFragment)
                .commit()

        backButtonContainer.setOnClickListener {
            AnalyticsHelper2.logExploreButtonLocationClickEvent(providedReferrer, "back", PageSection.NEWS)
            isSystemBackKeyPressed = false
            onBackPressed()
        }
        val module = SearchModule("", getString(R.string.recent_header_text),
                getString(R.string.trending_header_text), SearchDatabase.instance(), requestType,
                searchPayloadContext, Constants.ITEM_TYPE_LOCATION_SEARCH)
        DaggerSearchComponent.builder().searchModule(module).build().inject(this)

        SocialDB.instance().followEntityDao().getAllFollowedLocations().observe(this,
                Observer<List<FollowSyncEntity?>?> { follows ->
                    if (follows != null && !follows.isEmpty()) {
                        nextButton.isEnabled = true
                    } else {
                        nextButton.isEnabled = false
                    }

                })

    }

    private fun hideOrShowNextButton() {
        if (intent.getBooleanExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, false)) {
            nextButton.visibility = View.GONE
        } else {
            nextButton.isEnabled = false
        }
    }

    override fun searchViewModel(): SearchViewModel {
        return searchViewModel
    }

    override fun submitQuery(query: SearchQuery, searchtype: String) {
        searchViewModel.submit(query)
    }

    override fun editQuery(query: SearchSuggestionItem?) {
    }

    override fun curReferrer(): PageReferrer {
        return providedReferrer
    }

    override fun updateReferrer(referrer: PageReferrer) {
    }

    override fun getProvidedReferrer(): PageReferrer {

        return PageReferrer(NewsReferrer.LOCATION_SELECTION_PAGE)
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.SEARCH
    }

    override fun onClick(v: View?) {
        if (v == searchDismissContainer) {
            searchEditBox.setText(Constants.EMPTY_STRING)
        } else if (v == searchContainer) {
            supportFragmentManager.transaction {
                add(R.id.frag_location_container, LocationPresearchFragment.newInstance(referrer = providedReferrer,
                        hint = "",
                        query = SearchSuggestionItem("",
                                "",
                                searchPayloadContext = searchPayloadContext), searchNavModel =
                null), "search").addToBackStack("search")
            }
        } else if (v == nextButton) {
            if (intent.getBooleanExtra(Constants.BUNDLE_IS_LOCAL_ZONE, false)) {
                //If from local zone return back to it
                onBackPressed()
            } else {
                launchLocationVideoActivity()
            }
            AnalyticsHelper2.logExploreButtonLocationClickEvent(providedReferrer, "next", PageSection.NEWS)
        }

    }

    private fun launchLocationVideoActivity() {
        val intent = Intent(DHConstants.OPEN_LOCAL_VIDEO)
        intent.setPackage(AppConfig.getInstance().packageName)
        startActivityForResult(intent, NewsConstants
                .REQUEST_CODE_LOCATION_SEARCH)
        finish()
    }

    override fun onSnackBarAction(context: Context, messageDisplayed: String) {
        CommonNavigator.launchFollowingFeed(context, PageReferrer(NhGenericReferrer
                .FOLLOW_SNACKBAR))
    }


    override fun onBackPressed() {

        if (supportFragmentManager.getBackStackEntryCount() > 0) {
            supportFragmentManager.popBackStack();
        } else {
            if (isTaskRoot) {
                CommonNavigator.navigateToLastAppSection(this)
            }
            finish()
            overridePendingTransition(0, 0)
        }
    }

}
