package com.newshunt.appview.common.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.AddPageTopicListAdapter
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.*
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.DbgCode
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.ScrollTabHolderFragment
import com.newshunt.recolocations.DaggerTopicsRecommendedLocationsComponent
import com.newshunt.recolocations.RecommendedLocationsModule
import javax.inject.Inject

class PageableTopicFragment : ScrollTabHolderFragment(),
        ErrorMessageBuilder.ErrorMessageClickedListener {

    companion object {
        @JvmStatic
        fun newsInstance(section: String): PageableTopicFragment {
            val fragment = PageableTopicFragment()
            fragment.section = section
            return fragment
        }
    }

    private lateinit var pageableTopicViewModel: PageableTopicViewModel

    private var topicsList: RecyclerView? = null
    private var progressbar: ProgressBar? = null
    private var topicsAdapter: AddPageTopicListAdapter? = null
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private var errorParent: LinearLayout? = null
    private var forceRefresh = false
    private var section: String = PageSection.NEWS.section
    private var isFollowedCalled: Boolean = false
    private var isRecommendedCalled: Boolean = false
    private var isFollowedItems: Boolean = false
    private var isLocationItems: Boolean = false
    private var isTopicitems: Boolean = false
    private var errorLoc: Boolean = false
    private var errorTopic: Boolean = false
    private var locationItems: List<Location>? = null
    private var topicsItems: List<PageableTopicsEntity>? = null
    var errorTop: BaseError? = null
    var errorRec: BaseError? = null


    private lateinit var locationsViewModel: LocationsViewModel

    @Inject
    lateinit var locationsViewModelF: LocationsViewModelFactory

    private val topicClickListener = RecyclerViewOnItemClickListener { intent, position ->
        intent.putExtra(NewsConstants.DH_SECTION, section)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        try {
        } catch (e: ClassCastException) {
            Logger.caughtException(e)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val topicsView = inflater.inflate(R.layout.fragment_topicsgroup_list, container, false)


        DaggerTopicsRecommendedLocationsComponent.builder()
                .recommendedLocationsModule(RecommendedLocationsModule(section, SocialDB.instance
                ())).build().inject(this)

        locationsViewModel = ViewModelProviders.of(this, locationsViewModelF).get(LocationsViewModel::class
                .java)

        if (section == PageSection.NEWS.section) {

            locationsViewModel.allRecomendationData.observe(this, Observer {
                if (it.isSuccess) {
                    val data = it.getOrNull()
                    if (data == null) {
                        showError(BaseError(DbgCode.DbgNoItemsInList(),
                                CommonUtils.getString(R.string.no_content_found)), false)
                    }

                }
                if (it.isFailure) {
                    showError(ApiResponseOperator.getError(it.exceptionOrNull()), false)
                }
            })

            locationsViewModel.recomendationLiveData.observe(this, Observer {
                if (it.isSuccess) {
                    val recommendations = it.getOrNull()
                    if (!isRecommendedCalled && !CommonUtils.isEmpty(recommendations)) {
                        loadRecData(recommendations!!, false)
                        isRecommendedCalled = true
                    }
                }
            })
            locationsViewModel.getAllFollowedLocations().observe(this,
                    Observer<List<FollowSyncEntity?>?> { follows ->

                        if (!isFollowedCalled) {
                            if (follows != null && !follows.isEmpty()) {
                                val recommendations = follows.map { entity ->
                                    entity!!.actionableEntity.toLocationItem().copy(isFollowed = true)
                                }
                                loadRecData(recommendations, true)
                                isFollowedItems = true
                            } else {
                                locationsViewModel.callRecommendationUsecase()
                                isRecommendedCalled = false
                            }
                            isFollowedCalled = true
                        }

                    })

        }

        var addedTopic = mutableListOf<String?>()
        val reorderViewModel = ViewModelProviders.of(this, ReorderViewModelFactory(section)).
        get(ReorderViewModel::class.java)
        reorderViewModel.pageLiveData.observe(this, Observer { result ->
            if (result.isSuccess) {
                result.getOrNull()?.let {
                    addedTopic.clear()
                    it.forEach {
                        addedTopic.add(it.displayName)
                    }
                }
            }
        })


        pageableTopicViewModel = ViewModelProviders.of(this,
                PageableTopicViewModelFactory(section)).get(PageableTopicViewModel::class.java)
        pageableTopicViewModel.pageableTopicLiveData.observe(this, Observer {
            if (it.isSuccess) {
                val data = it.getOrNull()
                data?.let {
                    var filteredList = mutableListOf<PageableTopicsEntity>()
                    for (topic in it){
                        if (!(topic.pageEntity.displayName in addedTopic && topic.pageEntity.isServerDetermined)) {
                            filteredList.add(topic)
                        }
                    }
                    loadData(filteredList)
                }
            }
        })

        pageableTopicViewModel.topicResponseLiveData.observe(this, Observer {
            if (it.isSuccess) {
                val data = it.getOrNull()
                if (data == null) {
                    showError(BaseError(DbgCode.DbgNoItemsInList(),
                            CommonUtils.getString(R.string.no_content_found)), true)
                }

            }
            if (it.isFailure) {
                showError(ApiResponseOperator.getError(it.exceptionOrNull()), true)
            }
        })

        progressbar = topicsView.findViewById(R.id.progressbar)
        //Set up the topics ListView
        topicsList = topicsView.findViewById(R.id.all_topic_list)
        topicsList!!.layoutManager = LinearLayoutManager(activity)

        errorParent = topicsView.findViewById(R.id.error_parent)
        errorMessageBuilder = ErrorMessageBuilder(errorParent!!, activity!!, this, this)
        return topicsView
    }

    override fun onStart() {
        super.onStart()
        if (super.getUserVisibleHint()) {
            pageableTopicViewModel.viewStarted()

        }
    }

    override fun setUserVisibleHint(isVisible: Boolean) {
        super.setUserVisibleHint(isVisible)

        if (isVisible && view != null) {
            ViewUtils.screenChanged()
            pageableTopicViewModel.viewStarted()

        }
    }

    fun showLoading() {
        topicsList!!.visibility = View.GONE
        progressbar!!.visibility = View.VISIBLE
    }

    fun hideLoading() {
        topicsList!!.visibility = View.VISIBLE
        progressbar!!.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
     //   if (isFollowedItems)
            isFollowedCalled = false


    }

    fun showError(error: BaseError, fromTopic: Boolean) {
        /* If topicsFixedAdapter or topicsFeaturedAdapter is not null, it means that we already have
         data from the db. So, even if we get some error response during making network call, then
        we must ignore the error. Only if the server sends No Content Found error, then we must show
        it */

        if (topicsAdapter == null) {
            topicsAdapter = AddPageTopicListAdapter(topicClickListener, pageableTopicViewModel, this)
            topicsList?.adapter = topicsAdapter
        }
        if (section == PageSection.NEWS.section) {
            if (fromTopic && CommonUtils.isEmpty(topicsItems) ) {
                errorTopic = true
                isTopicitems = true
                errorTop = error
            } else {
                isLocationItems = true
                errorLoc = true
                errorRec = error
            }

            if ((isLocationItems && isTopicitems)) {
                topicsAdapter?.recItems = this.locationItems
                topicsAdapter?.items = this.topicsItems
                topicsAdapter?.errorTop = errorTop
                topicsAdapter?.errorRec = errorRec
                hideError()
                hideLoading()
            }

            if (errorTopic && errorLoc) {
                if (topicsAdapter != null && topicsAdapter?.itemCount?:0 >  0) {
                    return
                }
                errorParent!!.visibility = View.VISIBLE
                topicsList!!.visibility = View.GONE
                if (!errorMessageBuilder!!.isErrorShown) {
                    errorMessageBuilder!!.showError(error,true)
                }
            }
        } else {
            if (topicsAdapter != null && topicsAdapter?.itemCount?:0 >  0) {
                return
            }
            errorParent!!.visibility = View.VISIBLE
            topicsList!!.visibility = View.GONE
            if (!errorMessageBuilder!!.isErrorShown) {
                errorMessageBuilder!!.showError(error)
            }
        }
    }


    fun hideError() {
        errorParent!!.visibility = View.GONE
        topicsList!!.visibility = View.VISIBLE
        if (errorMessageBuilder!!.isErrorShown) {
            errorMessageBuilder!!.hideError()
        }
    }

    private fun loadData(list: List<PageableTopicsEntity>) {
        if (topicsAdapter == null) {
            topicsAdapter = AddPageTopicListAdapter(topicClickListener, pageableTopicViewModel, this)
            topicsList?.adapter = topicsAdapter
        }
        this.topicsItems = list
        isTopicitems = true

        if (section == PageSection.NEWS.section) {
            topicsAdapter?.isNewsSection = true
        }

        if ((isLocationItems && isTopicitems) || section != PageSection.NEWS.section) {
            topicsAdapter?.recItems = this.locationItems
            topicsAdapter?.items = this.topicsItems
            topicsAdapter?.errorTop = errorTop
            topicsAdapter?.errorRec = errorRec
            hideError()
            hideLoading()
        }
    }

    private fun loadRecData(list: List<Location>,
                            isFromfollowedList: Boolean) {
        if (topicsAdapter == null) {
            topicsAdapter = AddPageTopicListAdapter(topicClickListener, pageableTopicViewModel, this)
            topicsList?.adapter = topicsAdapter
        }
        this.locationItems = list
        isLocationItems = true
        topicsAdapter?.fromfollowedList = isFromfollowedList

        if (isLocationItems && isTopicitems) {
            topicsAdapter?.recItems = this.locationItems
            topicsAdapter?.items = this.topicsItems
            topicsAdapter?.errorTop = errorTop
            topicsAdapter?.errorRec = errorRec
            hideError()
            hideLoading()
        }

    }

    override fun onRetryClicked(view: View?) {
        hideError()
        pageableTopicViewModel.viewStarted()
        locationsViewModel.viewStarted()
    }

    override fun onNoContentClicked(view: View?) {
        NewsNavigator.navigateToHeadlines(activity)
    }

    override fun adjustScroll(scrollHeight: Int, headerTranslationY: Int) {

    }

    private fun scrollToTop() {
        if (topicsList != null) {
            topicsList!!.scrollTo(0, 0)
        }
    }

    override fun refresh() {
        forceRefresh = true
        // return if the fragment is not attached to the activity or it is not visible
        if (activity == null || !isVisible) {
            return
        }
    }
}