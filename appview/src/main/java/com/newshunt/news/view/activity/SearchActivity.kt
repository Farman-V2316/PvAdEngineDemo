/*
 * Created by Rahul Ravindran at 26/9/19 6:55 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.SnackbarViewModel
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Constants.SEARCH_CONTEXT_NEWSDETAIL
import com.newshunt.common.helper.common.Constants.SEARCH_TYPE_TAGS
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.interceptor.HeaderInterceptor
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.notification.SearchNavModel
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.transaction
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.di.DaggerSearchComponent
import com.newshunt.news.di.SearchComponent
import com.newshunt.news.di.SearchModule
import com.newshunt.news.helper.listeners.AdsSupportListener
import com.newshunt.news.model.sqlite.SearchDatabase
import com.newshunt.news.view.fragment.PresearchFragment
import com.newshunt.news.view.fragment.SearchFragment
import com.newshunt.news.view.listener.MenuListenerProvider
import com.newshunt.news.view.listener.MenuOptionClickListener
import com.newshunt.search.viewmodel.SearchViewModel
import javax.inject.Inject

/**
 * @author satosh.dhanymaraju
 */
class SearchActivity : NewsBaseActivity(), AdsSupportListener, SearchActvityInterface,
        MenuListenerProvider{

    @Inject lateinit var viewmodel : SearchViewModel
    @Inject
    lateinit var snackbarViewModelFactory: SnackbarViewModel.Factory

    lateinit var component: SearchComponent
    private var searchHint:String = ""
    private var searchContext:String = SEARCH_CONTEXT_NEWSDETAIL
    private var searchPayloadContext: SearchPayloadContext? = null
    private var mSearchNavModel: SearchNavModel? = null
    private var referrer: PageReferrer? = null
    private val searchFragTag = "searchfrag"
    private val referrerProviderHelper = ReferrerProviderHelper()
    private var acceptableTimeStamp = System.currentTimeMillis()

    // for restoring instance
    private var lastQuery : SearchQuery? = null
    private var lastType : String? = null
    private var requestType: SearchRequestType = SearchRequestType.NEWS
    private var launchFromAddPageActivity = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchHint = intent.getStringExtra(Constants.BUNDLE_SEARCH_HINT)?: ""
        searchContext = intent.getStringExtra(Constants.BUNDLE_SEARCH_CONTEXT) ?: SEARCH_CONTEXT_NEWSDETAIL
        searchPayloadContext = intent.getSerializableExtra(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD) as? SearchPayloadContext
        referrer = (intent.getSerializableExtra(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer)
        referrerProviderHelper.addReferrerByProvider(referrer ?: SEARCH_REFERRER)
        requestType = intent.getSerializableExtra(Constants.BUNDLE_SEARCH_REQUEST_TYPE) as?
                SearchRequestType ?: SearchRequestType.NEWS
        /*can not rely on referrer because after user edit query and enter search again referrer
        will change*/
        launchFromAddPageActivity = referrer?.referrer == NewsReferrer.ADD_LOCATION
        setContentView(R.layout.layout_activity_search)
        val module = SearchModule(searchContext, getString(R.string.recent_header_text),
                getString(R.string.trending_header_text), SearchDatabase.instance(), requestType,
                searchPayloadContext)
        component = DaggerSearchComponent.builder().searchModule(module).build()
        component.inject(this)
        val queryText: String? = intent.getStringExtra(Constants.BUNDLE_SEARCH_QUERY)
        val searchNavModel: SearchNavModel? = intent.getSerializableExtra(Constants
                .BUNDLE_SEARCH_MODEL) as? SearchNavModel
        this.mSearchNavModel = searchNavModel
        val launchPresearch: Boolean = searchNavModel?.presearch == true
        if (searchNavModel != null && !CommonUtils.isEmpty(searchNavModel.hint)) {
            searchHint = searchNavModel.hint!!
        }
        // If there is no saved state, show presearch/search fragments.
        // Else system restores fragments and viewstate. Update viewmodel in onRestoreInstanceState
        if (savedInstanceState == null) {
            if (queryText != null && !launchPresearch) {
                // launch search results
                val searchQuery = SearchSuggestionItem(queryText, queryText, requestId =
                HeaderInterceptor.generateRequestId(), searchPayloadContext = searchPayloadContext)
                SearchAnalyticsHelper.logSearchInitiated(searchQuery.requestId, referrer?: SEARCH_REFERRER,
                        Constants.SEARCH_HAS_USER_TYPED_NA)
                submitQuery(searchQuery, SEARCH_TYPE_TAGS)
            } else {
                val queryTextForPresearch = queryText ?: Constants.EMPTY_STRING
                // show presearch screen
                supportFragmentManager.transaction {
                    add(R.id.frag_container, PresearchFragment.newInstance(referrer = providedReferrer,
                            hint = searchHint,
                            query = SearchSuggestionItem(queryTextForPresearch,
                                    queryTextForPresearch,
                                    searchPayloadContext = searchPayloadContext),searchNavModel = mSearchNavModel))
                }
            }
        }
        NavigationHelper.navigationLiveData.observe(this, Observer {
            if (it.timeStamp < acceptableTimeStamp) {
                return@Observer
            } else {
                NavigationHelper.handleNavigationEvents(it, this, R.id.frag_container)
            }
        })
        ViewModelProviders.of(this, snackbarViewModelFactory).get(SnackbarViewModel::class.java)
                .also {
                    it.followChanges.observe(this, Observer {
                        SnackbarViewModel.onFollowChangeEvent(it, findViewById(R.id.frag_container))
                    })
                    it.newPostChanges.observe(this, Observer {res ->
                        SnackbarViewModel.onPostUploaded(res, findViewById(R.id.frag_container),
                                true, null, R.string.view_photo_in_lite_mode_message)
                    })
                    it.start()
                }
    }

    override fun onStart() {
        super.onStart()
        acceptableTimeStamp = System.currentTimeMillis()
    }

    override fun enableP0Ad(): Boolean {
        return false
    }

    override fun getProvidedReferrer(): PageReferrer {
        return referrerProviderHelper.providedPageReferrer
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.SEARCH
    }


    override fun onSaveInstanceState(outState: Bundle) {
        Logger.d(LOG_TAG, "onSaveInstanceState $outState")
        outState?.putSerializable(BUNDLE_QUERY, lastQuery)
        outState?.putString(BUNDLE_TYPE, lastType)
        outState.putSerializable(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD, searchPayloadContext)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val query = savedInstanceState?.getSerializable(BUNDLE_QUERY) as? SearchQuery
        val stype = savedInstanceState?.getString(BUNDLE_TYPE)
        val searchPayloadContext = savedInstanceState?.getSerializable(Constants
                .BUNDLE_SEARCH_CONTEXT_PAYLOAD) as? SearchPayloadContext
        Logger.d(LOG_TAG, "onRestoreInstanceState $query, $stype")
        if(query != null && stype != null) {
            updateViewModel(query, stype, searchPayloadContext)
        }
    }

    override fun submitQuery(query: SearchQuery, searchtype: String) {

        val query1 = updateViewModel(query, searchtype, searchPayloadContext)
        // on every search result, clear the backstack and add searchresult as root.
        while (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()

        // when re-executing the query, delete the oldest referrer.
        // because we can't add duplicate referrer and the original referrer remains in the queue
        // after edit query also.
        if (referrerProviderHelper.referrerQueue?.size == 2) {
            val r = referrerProviderHelper.referrerQueue.removeAt(0)
            Logger.d(LOG_TAG, "submitQuery: removed ${r?.referrer}#${r?.id}")
        }
        supportFragmentManager.transaction {
            val bundle = (intent.extras?: Bundle()).apply {
                    putSerializable(Constants.BUNDLE_SEARCH_QUERY , query1)
                    putString(Constants.BUNDLE_SEARCH_TYPE, searchtype)
                    putString(Constants.APP_SECTION_ID, referrerEventSection.eventSection)
                    putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, providedReferrer)
                    putLong(Constants.BUNDLE_QUERY_SUBMIT_TIME, System.currentTimeMillis())
                    putBoolean(Constants.SHOW_ADD_BUTTON_FOR_ENTITY, launchFromAddPageActivity)
                }
            replace(R.id.frag_container, SearchFragment.newInstance(bundle), searchFragTag)
        }
        AndroidUtils.hideKeyBoard(this)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frag_container)
        if (fragment is BaseFragment && fragment.handleBackPress()) {
            return
        }
        VideoHelper.handleBackPressState.value = (fragment as? BaseFragment)?.hashCode()
        super.onBackPressed()
    }

    override fun editQuery(query: SearchSuggestionItem?) {
        // SearchResult > edit > back should display searchResult. Hence, adding presearch to
        // backstack.
        Logger.d(LOG_TAG, "editQuery : ${query?.suggestion}")
        supportFragmentManager.transaction {
            addToBackStack(null)
            supportFragmentManager.findFragmentByTag(searchFragTag)?.let {
                hide(it)
            }
            add(R.id.frag_container, PresearchFragment.newInstance(query, curReferrer(),
                    searchNavModel = mSearchNavModel))
        }
    }


    override fun searchViewModel(): SearchViewModel {
        return  viewmodel
    }

    override fun curReferrer() = referrerProviderHelper.youngestPageReferrer ?: SEARCH_REFERRER

    override fun updateReferrer(referrer: PageReferrer) {
        referrerProviderHelper.addReferrerByProvider(referrer)
        Logger.d(LOG_TAG, "updateReferrer ${referrerProviderHelper.providedPageReferrer
                ?.referrer}#${referrerProviderHelper.providedPageReferrer?.id}," +
                "${referrerProviderHelper.youngestPageReferrer?.referrer}#${referrerProviderHelper
                        .youngestPageReferrer?.id}")
    }

    override fun menuOptionClickListener(): MenuOptionClickListener? {
        val fragment = supportFragmentManager.findFragmentByTag(searchFragTag) as? SearchFragment
        return  fragment?.menuOptionClickListener()
    }

    private fun updateViewModel(query: SearchQuery,
                                searchtype: String,
                                searchPayloadContext: SearchPayloadContext?):
            SearchSuggestionItem {
        lastQuery = query
        lastType = searchtype
        val query1 = query.copy(searchContext = intent.extras?.getString(Constants
                .BUNDLE_SEARCH_CONTEXT) ?: "", searchPayloadContext = searchPayloadContext)
        viewmodel.submit(query1)
        return query1
    }

    companion object {
        @JvmStatic
        val SEARCH_REFERRER = PageReferrer(NewsReferrer.SEARCH)
        private val LOG_TAG = "SearchActivity"
        private val BUNDLE_QUERY = "squery"
        private val BUNDLE_TYPE = "stype"
    }
}


interface SearchActvityInterface: ReferrerProviderlistener {
    fun searchViewModel(): SearchViewModel
    fun submitQuery(query: SearchQuery, searchtype: String)
    fun editQuery(query: SearchSuggestionItem?)
    fun curReferrer() : PageReferrer
    fun updateReferrer(referrer: PageReferrer)
}

