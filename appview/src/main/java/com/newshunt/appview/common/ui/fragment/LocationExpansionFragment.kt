/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.UpdateLocationListInterface
import com.newshunt.appview.common.ui.adapter.StateListAdapter
import com.newshunt.appview.common.ui.helper.SnackbarViewModel
import com.newshunt.appview.common.ui.listeners.AddLocationListener
import com.newshunt.appview.common.viewmodel.LocationsViewModel
import com.newshunt.appview.common.viewmodel.LocationsViewModelFactory
import com.newshunt.appview.databinding.FragmentLocationExpantionBinding
import com.newshunt.common.helper.analytics.NhAnalyticsUtility
import com.newshunt.common.helper.common.*
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.DbgCode.DbgNoItemsInList
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ErrorTypes
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2.logExploreButtonLocationClickEvent
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.ErrorMessageBuilder.ErrorMessageClickedListener
import com.newshunt.news.helper.ErrorLogHelper
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.view.fragment.ScrollTabHolderFragment
import com.newshunt.news.view.listener.EntitiesSelectionListener.SelectEntityListener
import com.newshunt.recolocations.DaggerRecommendedLocationsComponent
import com.newshunt.recolocations.RecommendedLocationsModule
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

/**
 * @author priya.gupta
 *
 */

class LocationExpansionFragment() : ScrollTabHolderFragment(), ErrorMessageClickedListener,
        RecyclerViewOnItemClickListener, SelectEntityListener, UpdateLocationListInterface, AddLocationListener {


    private var stateListAdapter: StateListAdapter? = null

    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var forceRefresh: Boolean = false
    private var isEventLogged: Boolean = false
    private var pageReferrer: PageReferrer? = null
    private var referrerProviderlistener: ReferrerProviderlistener? = null
    private var allLocations: List<Locations>? = null
    private var isFromLocationSearch: Boolean = false

    private var showFollowButton: Boolean = false
    private var postNewsPageEntityChangeEvents: Boolean = false
    private val section: String = PageSection.NEWS.section
    private lateinit var locationsViewModel: LocationsViewModel

    @Inject
    lateinit var snackbarViewModelFactory: SnackbarViewModel.Factory

    @Inject
    lateinit var locationsViewModelF: LocationsViewModelFactory

    private lateinit var binding: FragmentLocationExpantionBinding
    private lateinit var selectedLocations: List<Location>

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        if (activity is ReferrerProviderlistener) {
            referrerProviderlistener = activity
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val bundleReceived: Bundle? = arguments
        if (bundleReceived != null) {
            pageReferrer = bundleReceived.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            isFromLocationSearch = bundleReceived.getBoolean(Constants.IS_LOCATION_SEARCH)
            showFollowButton = bundleReceived.getBoolean(Constants.SHOW_FOLLOW_BUTTON)
            postNewsPageEntityChangeEvents = bundleReceived.getBoolean(Constants.POST_NEWS_PAGE_ENTITY_CHANGE_EVENTS)
        }
        if (pageReferrer == null) {
            pageReferrer = PageReferrer(NewsReferrer.LOCATION_SELECTION_PAGE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_location_expantion, container, false)
        errorMessageBuilder = ErrorMessageBuilder(binding.errorParent, activity!!, this, this)
        linearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setLayoutManager(linearLayoutManager)

        DaggerRecommendedLocationsComponent.builder()
                .recommendedLocationsModule(RecommendedLocationsModule(section, SocialDB.instance
                ())).build().inject(this)

        locationsViewModel = ViewModelProviders.of(this, locationsViewModelF).get(LocationsViewModel::class
                .java)


        locationsViewModel.getLocationsNested().observe(this, Observer { data ->

            data?.let {
                loadData(data)
            }
        })

        locationsViewModel.allLocationLiveData.observe(this, Observer {
            if (it.isSuccess) {
                val data = it.getOrNull()
                if (data == null) {
                    val error = BaseError(DbgNoItemsInList(),
                            CommonUtils.getString(R.string.no_content_found))

                    showError(error, Constants.HTTP_SUCCESS)
                }

            }
            if (it.isFailure) {
                showError(ApiResponseOperator.getError(it.exceptionOrNull()), Constants
                        .HTTP_UNKNOWN)
            }
        })


        locationsViewModel.getRcommendedLocations().observe(this, Observer { recommendations ->
            recommendations?.let{
                loadRecomendedItems(recommendations)
            }


        })

        locationsViewModel.allRecomendationData.observe(this, Observer {
            if (it.isSuccess) {
                val data = it.getOrNull()
                if (data == null) {
                    showError(BaseError(DbgCode.DbgNoItemsInList(),
                            CommonUtils.getString(R.string.no_content_found)), Constants.HTTP_SUCCESS)
                }

            }
            if (it.isFailure) {
                showError(ApiResponseOperator.getError(it.exceptionOrNull()), Constants
                        .HTTP_UNKNOWN)
            }
        })

        locationsViewModel.getAllFollowedLocations().observe(this, Observer<List<FollowSyncEntity?>?> { follows ->
                    if (follows != null && !follows.isEmpty()) {
                            selectedLocations = follows.map { entity ->
                            entity!!.actionableEntity.toLocationItem().copy(isFollowed = true)
                        }
                        loadSelectedItems(selectedLocations)
                    } else {
                        loadSelectedItems(emptyList())
                    }
        })

        ViewModelProviders.of(this, snackbarViewModelFactory).get(SnackbarViewModel::class.java)
                .also {
                    it.followChanges.observe(this, Observer {
                        SnackbarViewModel.onFollowChangeEvent(it, binding.root)
                    })

                    it.start()
                }



        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    AndroidUtils.hideKeyBoard(activity)
                }
            }

        })
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (super.getUserVisibleHint()) {
            locationsViewModel.viewStarted()
        }
    }

    override fun onStop() {
        super.onStop()
        BusProvider.getUIBusInstance().unregister(this)
    }

    override fun setUserVisibleHint(isVisible: Boolean) {
        super.setUserVisibleHint(isVisible)
        if (isVisible && view != null) {
            ViewUtils.screenChanged()
            locationsViewModel.viewStarted()
            locationsViewModel.callRecommendationUsecase()
        }

    }

override fun onLocationAdded(isAdded: Boolean, location: Location) {
    locationsViewModel.onLocationFollowed(location)
    if (isAdded) {
        logExploreButtonLocationClickEvent(pageReferrer, "check", PageSection.NEWS)
    }
    else {
        logExploreButtonLocationClickEvent(pageReferrer, "uncheck", PageSection.NEWS)
    }
}


    override fun adjustScroll(scrollHeight: Int, headerTranslationY: Int) {}

    private fun loadData(list: List<Locations>) {
        if (stateListAdapter == null) {
            this.allLocations = list
            stateListAdapter = StateListAdapter(list, emptyList(), emptyList(),this, this, showFollowButton,
                    pageReferrer!!, referrerProviderlistener!!.referrerEventSection)
            stateListAdapter!!.setItems(list)
            binding.recyclerView.adapter = stateListAdapter
        } else {
            stateListAdapter!!.setItems(list)
        }

        hideError()
        hideLoading()
    }

    private fun loadRecomendedItems(list: List<Location>) {
        if (stateListAdapter == null) {

            stateListAdapter = StateListAdapter(emptyList(), list,emptyList(), this, this,
                    showFollowButton, pageReferrer!!, referrerProviderlistener!!
                    .referrerEventSection)
            stateListAdapter!!.setRecommendedItems(list)
            binding.recyclerView.adapter = stateListAdapter
        } else {
            stateListAdapter!!.setRecommendedItems(list)
        }

        hideError()
        hideLoading()
    }

    private fun loadSelectedItems(list: List<Location>) {
        if (stateListAdapter == null) {

            stateListAdapter = StateListAdapter(emptyList(),emptyList(), list, this, this,
                    showFollowButton, pageReferrer!!, referrerProviderlistener!!
                    .referrerEventSection)
            stateListAdapter!!.setSelectedItems(list)
            binding.recyclerView.adapter = stateListAdapter
        } else {
            stateListAdapter!!.setSelectedItems(list)
        }

        hideError()
        hideLoading()
    }

    fun hideError() {
        binding.errorParent.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        if (errorMessageBuilder!!.isErrorShown) {
            errorMessageBuilder!!.hideError();
        }
    }

    fun hideLoading() {
        binding.progressbar.visibility = View.GONE
    }


    fun showError(error: BaseError, status: Int) {

    var error: BaseError? = error
    if (stateListAdapter != null && status != Constants.HTTP_SUCCESS) {
        return
    }
    if (stateListAdapter == null && status == HttpURLConnection.HTTP_NOT_MODIFIED) {
        error = BaseErrorBuilder.getBaseError(ErrorTypes.VERSIONED_API_CORRUPTED,
                CommonUtils.getString(R.string.error_generic))
    }
    binding.errorParent.visibility = View.VISIBLE
    binding.recyclerView.visibility = View.GONE
    if (!errorMessageBuilder!!.isErrorShown) {
        errorMessageBuilder!!.showError(error,true)
    }
}

    override fun onRetryClicked(view: View?) {
        hideError()
    }

    override fun onNoContentClicked(view: View?) {}


    override fun onItemClick(intent: Intent, position: Int) {

    }

    override fun refresh() {
        forceRefresh = true
        // return if the fragment is not attached to the activity or it is not visible
        if (activity == null || !isVisible) {
            return
        }
    }

    override fun onEntitySelected() {}
    override fun updateUIForAddedPages(newsPageEntities: List<PageEntity>) {}
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun showSearchedLocations(list: MutableList<Locations>) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.searchErrorLayout.visibility = View.GONE
        stateListAdapter = StateListAdapter(list, emptyList(),emptyList(),
                this, this, showFollowButton, pageReferrer!!, referrerProviderlistener!!
                .referrerEventSection)
        stateListAdapter!!.setItems(list)
        binding.recyclerView.adapter = stateListAdapter
    }


    override fun showErrorOnSearchFailure(errorTitle: String?,
                                          errorSubTitle: String?) {
        binding.searchErrorLayout.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        if (CommonUtils.isEmpty(errorTitle)) {
            binding.searchErrorLayout.visibility = View.GONE
        } else {
            binding.searchErrorLayout.visibility = View.VISIBLE
            binding.locationSearchErrorSubtitle.text = Html.fromHtml(errorTitle)
        }
        if (CommonUtils.isEmpty(errorSubTitle)) {
            binding.locationSearchErrorSubtitle.visibility = View.GONE
        } else {
            binding.locationSearchErrorSubtitle.visibility = View.VISIBLE
            binding.locationSearchErrorSubtitle.text = errorSubTitle
        }
    }

    override fun userStartedTypingQuery() {
        binding.recyclerView.visibility = View.GONE
        binding.searchErrorLayout.visibility = View.GONE
    }
}