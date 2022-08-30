/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.localzone

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.adengine.view.helper.AdDBHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsExternalListener
import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.entity.CardsPojoPagedList
import com.newshunt.appview.common.video.base.BaseVerticalVideoFragment
import com.newshunt.appview.common.video.ui.view.AddedLocationsFragment
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.dbgCode
import com.newshunt.common.view.isNoContentError
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.runOnce
import com.newshunt.dhutil.toArrayList
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import io.reactivex.exceptions.CompositeException
import javax.inject.Inject

/**
 * Created on Vinod.BC 30/09/2020.
 * Fragment to handle Local zone videos
 */
class LocalZoneFragment() : BaseVerticalVideoFragment(), AdDBHelper, ErrorMessageBuilder.ErrorMessageClickedListener
{
    private val LOG_TAG = "LocalZoneFragment"

    private var feedPage: FeedPage? = null
    private var selectedLocation: String? = null
    private var locationLoaded: Boolean = false

    @Inject
    lateinit var cVMF: CardsViewModel.Factory
    lateinit var cVM: CardsViewModel

    @Inject
    lateinit var lVMF: LocalZoneVM.Factory
    lateinit var lzVM: LocalZoneVM
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private  var errorLayoyt: LinearLayout? = null
    private var toolbar : View? = null
//    private var selectedLocationTextView : NHTextView? = null
    private var backButton : ImageView? = null
    private var progressBar : LinearLayout? = null
    private var isFollowingLocEmpty = false
    private var isErrorScreenShown = false
    private var getCurrentSnapshot:  List<Any?>? = null
    private var updateLocationNameFlag = true
    private var prevSelectedLocation: String? = null
    var addedLocationsFragment: AddedLocationsFragment? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        initFromArgs()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        errorLayoyt = this.activity?.findViewById(R.id.error_parent)
        toolbar = this.activity?.findViewById(R.id.toolbar)
//        selectedLocationTextView = toolbar?.findViewById(R.id.selected_location)
        backButton = toolbar?.findViewById(R.id.back_button)
        backButton?.setOnClickListener{
            activity?.onBackPressed()
        }
        progressBar = this.activity?.findViewById(R.id.progressbar_parent)
        if (errorLayoyt != null)
            errorMessageBuilder = ErrorMessageBuilder(errorLayoyt!!,requireContext(), this, this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
        setUpCardLocationTag()
    }

    override fun requestRelatedVideo(relatedUrl: String?) {
        //Do nothing
    }

    override fun requestNextPage(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        Logger.d(LOG_TAG, "requestNextPage visibleItemCount:$visibleItemCount, " +
                "firstVisibleItem:$firstVisibleItem totalItemCount:$totalItemCount")
        if (viewPager2.adapter != null && viewPager2.adapter?.itemCount!! > 0) {
            cVM.updateCurrentCardLocation(1, firstVisibleItem, totalItemCount)
        }
    }

    private fun onCardsReceived(cards: CardsPojoPagedList?) {
        progressBar?.visibility = View.GONE
        toolbar?.visibility = View.GONE
        Logger.d(LOG_TAG, "onCardsReceived() called with: it = $cards")
        val cardsList = cards?.data?.getList()?.filterNotNull() ?: return
        val titles = cardsList.filterIsInstance<CommonAsset>().map { it.i_title() }
        Logger.d(LOG_TAG, "${cardsList.size} cardsList. $titles")

        if (!cardsList.isNullOrEmpty()) {
            getCurrentSnapshot = cards.data?.getSnapshot()
            updateCardsList(cardsList as List<PostEntity>)
            hideErrorScreen()
        }
    }

    override fun getTotalItems(): Int? = 0
    override fun getItemIdBeforeIndex(adPosition: Int): String? = null
    override fun getActivityContext(): Activity? = activity

    private fun onLocationSelectionChanged(location: String) {
        Logger.d(LOG_TAG, "Location Changed - " + location)
        selectedLocation = location
        lzVM.changeLocation(location)
        cVM.pullToRefresh()
        updateCardsList(null)
        progressBar?.visibility = View.VISIBLE
        toolbar?.visibility = View.VISIBLE
        if (isErrorScreenShown) {
            errorLayoyt?.visibility = View.GONE
        }
    }

    private fun initFromArgs() {
        val p1 = arguments ?: return
        feedPage = FeedPage(
                id = Constants.LOCAL_ZONE_PAGE_ID,
                contentUrl = p1.getString(Constants.BUNDLE_CONTENT_URL,
                        "http://stage-api-news.dailyhunt.in/api/v2/"),
                section = p1.getString(NewsConstants.DH_SECTION, PageSection.LOCAL.section),
                contentRequestMethod = Constants.HTTP_POST
        )

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

    private fun initViewModel() {
        //TODO::Take care of feedPage null case
        injectDeps(feedPage!!)
        cVM = ViewModelProviders.of(this, cVMF).get(CardsViewModel::class.java)
        lzVM = ViewModelProviders.of(this, lVMF).get(LocalZoneVM::class.java)
        lzVM.curLocalZoneInfo.observe(this, Observer {
            Logger.d(LOG_TAG, "pageUrlChange: ${it?.contentUrl}")
            cVM.pullToRefresh()
        })

        cVM.mediatorCardsLiveData.observe(this, Observer { cards ->
            if (cVM.fpRequestStatus.value == true && cards.isWaitingForData()) {
                Logger.e("TAG", "mediatorCardsLiveData :ignored")
                return@Observer
            }
            if (cards.dataIsEmptyAndNotYetSeenError()) {
                Logger.e("TAG", "mediatorCardsLiveData : not showing error untill we get error")
                return@Observer
            }
            //If no data received show error
            if (cards.data?.getList().isNullOrEmpty() && cVM.started) {
                val exception = getFilteredErrorFromCompositeError(cards.error)
                        ?: ApiResponseUtils.composeListNoContentError()
                val baseError = ApiResponseOperator.getError(exception)

                if (errorLayoyt != null && (exception is ListNoContentException || baseError.dbgCode().get() == "BB04" || (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())))) {
                    updateCardsList(null)
                    if ((!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()))) {
                        showErrorScreen(Constants.ERROR_NO_INTERNET)
                    } else {
                        showErrorScreen(Constants.ERROR_HTTP_NO_CONTENT)
                    }
                    progressBar?.visibility = View.GONE
                    (parentFragment as? CardsExternalListener?)?.onCardsLoadError(true)
                    return@Observer
                }
            }else if (cards.data != null && cards.error != null && prevSelectedLocation != null) {
                val exception = getFilteredErrorFromCompositeError(cards.error)
                        ?: ApiResponseUtils.composeListNoContentError()
                val baseError = ApiResponseOperator.getError(exception)
                if (cards.tsError ?: 0 > cards.tsData ?: 0) {
                    //This is executed when there are cards items cached but the server gives 204
                    if(prevSelectedLocation == selectedLocation){
                        //If items are available in cards and server gives 204 for the same then
                        // should not show error
                        return@Observer
                    }
                    if (baseError.isNoContentError() && cVM.started) {
                        //If Location has changed but cache items available in cards and server gives
                        // 204 then show no content error
                        updateCardsList(null)
                        showErrorScreen(Constants.ERROR_HTTP_NO_CONTENT)
                        progressBar?.visibility = View.GONE
                        return@Observer
                    }
                }
            }
            fireSLVFor1stResponseFromDB(cards)
            val hasDataChanged = hasListChangedStructurally(getCurrentSnapshot, cards.data?.getSnapshot())
            //Check if the current received items is same as the previous items
            if (!hasDataChanged) {
                if (!isErrorScreenShown || selectedLocation != prevSelectedLocation) {    //If item has not changed and previous content was null then dont return
                    Logger.d(LOG_TAG, "Item content not changed")
                    //If same do not update current items
                    return@Observer
                }
            }
            (parentFragment as? CardsExternalListener?)?.onCardsLoaded()
            // On sucessfully updating new items updated previous location to current location
            prevSelectedLocation = selectedLocation
            onCardsReceived(cards)
        })
        cVM.firstpageData.observe(viewLifecycleOwner, Observer {
            it?.let {
                cVM.replaceFP() // both cache and network?
            }
        })

        if (selectedLocation.isNullOrEmpty()) {
            lzVM.getFollowedLocationsFIFOData().observe(this, Observer {
                //Log to print location names with ID
                if (selectedLocation.isNullOrEmpty()) {
                    for (item in it) {
                        Logger.d(LOG_TAG, "   ${item.actionableEntity?.displayName}    ${item.actionableEntity?.entityId}")
                    }
                }
                if (!it.isNullOrEmpty()) {
                    if (AppUserPreferenceUtils.isLocalZoneFirstLaunch()) {
                        //On first launch select first selected location
                        AppUserPreferenceUtils.setLocalZoneFirstLaunchDone()
                        if (it[it.size - 1] != null) {
                            selectedLocation = it[it.size - 1].actionableEntity.entityId
//                            selectedLocationTextView?.text = it[it.size - 1].actionableEntity.displayName
                            updateLocationNameFlag = false
                        }
                    } else {
                        //select recently selected location
                        if (selectedLocation != it[0].actionableEntity.entityId) {
                            selectedLocation = it[0].actionableEntity.entityId
                        }
                    }
                    if (!locationLoaded) {
                        locationLoaded = true
                        cVM.start()
                        getSelectedLocation()?.let { it1 ->
                            lzVM.start(it1)
                            onLocationSelectionChanged(it1)
                        }
                       }
                }
                else{
                    selectedLocation = null
                    showErrorScreen(Constants.ERROR_HTTP_NO_CONTENT)
                    updateCardsList(null)
                }
                if (!locationLoaded) {
                    locationLoaded = true
                    cVM.start()
                    getSelectedLocation()?.let { it1 ->
                            lzVM.start(it1)
                            onLocationSelectionChanged(it1)
                        }
                }
            })
        } else {
            //Loaded with selected Location
            locationLoaded = true
            cVM.start()
              getSelectedLocation()?.let { it1 ->
                            lzVM.start(it1)
                            onLocationSelectionChanged(it1)
                        }
        }
    }

    private fun getSelectedLocation(): String? = selectedLocation

    private fun injectDeps(feedPage: FeedPage) {
        val cardsModule = CardsModule(CommonUtils.getApplication(), SocialDB.instance(), feedPage.id, "",
                null, "detail", adDbHelper = this, supportAds = false, lifecycleOwner = this,
                section = feedPage.section, searchQuery = null, performLogin = ::performLogin)
        DaggerLocalZoneComponent.builder()
                .cardsModule(cardsModule)
                .localZoneModule(LocalZoneModule(feedPage))
                .build().inject(this)
    }

    private fun performLogin(showToast: Boolean, toastMsgId: Int) {
        activity?.let {
            val sso = SSO.getInstance()
            sso.login(it as Activity, LoginMode.USER_EXPLICIT, SSOLoginSourceType.REVIEW)
        }
    }

    override fun onRetryClicked(view: View?) {
        cVM.pullToRefresh()
    }

    override fun onNoContentClicked(view: View?) {
        locationLoaded = false
        CommonNavigator.openLocationSelection(this.context,false, true)
    }

    private fun showErrorScreen(errorCode: String) {
        isErrorScreenShown = true
        errorLayoyt?.visibility = View.VISIBLE
        var errorMsg = when (errorCode) {
            Constants.ERROR_NO_INTERNET -> {
                CommonUtils.getString(R.string.error_no_connection)
            }
            Constants.ERROR_HTTP_NO_CONTENT -> {
                errorMessageBuilder?.noContentError(true, true, true)
                toolbar?.visibility = View.VISIBLE
                return
            }
            else -> {
                CommonUtils.getString(R.string.error_connectivity)
            }
        }
        errorMessageBuilder?.showError(BaseErrorBuilder.getBaseError(errorMsg,
                errorCode),
                showRetryOnNoContent = true,
                error204Message = null,
                hideButtons = false,
                isDhTv = true)
    }

    private fun hideErrorScreen(){
        errorLayoyt?.removeAllViews()
        errorLayoyt?.visibility = View.GONE
        progressBar?.visibility = View.GONE
        toolbar?.visibility = View.GONE
        isErrorScreenShown = false
    }

    //Function to show bottom sheet of selected locations , allows to select other location,
    // Unfolow selected location and also add more locations
    private fun setUpCardLocationTag() {
//        selectedLocationTextView?.visibility = View.VISIBLE
        lzVM.getFollowedLocationsFIFOData().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                isFollowingLocEmpty =false
//                if(updateLocationNameFlag) {
//                    selectedLocationTextView?.text = it[0].actionableEntity.displayName
//                    selectedLocationTextView?.visibility = View.VISIBLE
//                }
                //Initialize bottom sheet
                addedLocationsFragment = AddedLocationsFragment(it,
                        object : AddedLocationsFragment.BottomLocMenuListener {
                            //This method is invoked when bottom sheet dialog is closed
                            override fun onDialogDismissUpdateLocations(view: View,
                                                                 unFollowedLocationList:
                            List<ActionableEntity>, selectedLoc: ActionableEntity?) {
                                //Update unfollowed location list to the DB
                                if (!unFollowedLocationList.isNullOrEmpty()) {
                                    cVM.onUnFollowLocations(view, unFollowedLocationList)
                                }
                                //If a location item is selected change the current selectedLocation
                                // and update the location tag text
                                if(selectedLoc != null )
                                {
                                    if(selectedLoc.entityId != selectedLocation){
                                        updateLocationNameFlag = false
//                                        selectedLocationTextView?.text = selectedLoc.displayName
                                        sendOtherLocationAnalytics()
                                        onLocationSelectionChanged(selectedLoc.entityId)
                                    }
                                } /* If no location is selected, then select the 1st item from
                                   * the new updated selected locations list
                                   */
                                else if(!unFollowedLocationList.isNullOrEmpty()) {
                                    var  followingLocations = (it.map {it -> it.actionableEntity
                                            .entityId })
                                            .toArrayList()
                                    var unFollowLocations = ((unFollowedLocationList.map {
                                        actionableEntity ->
                                        actionableEntity.entityId })).toArrayList()

                                    //This gives a list for updated following locations
                                    followingLocations.removeAll(unFollowLocations)

                                    //If the current selectedLocation is unfollowed then set the
                                    // first item from followed location as selectedLocation
                                    if(!followingLocations.isNullOrEmpty() && unFollowLocations.contains
                                            (selectedLocation)) {
                                        updateLocationNameFlag = true
                                        onLocationSelectionChanged(followingLocations[0])
                                    }
                                }
                            }
                            // On click add more locations take to the add more location screen
                            override fun addMoreLocations() {
                                sendSeeMoreLocationAnalytics()
                                CommonNavigator.openLocationSelection(requireContext(), false, true)
                                locationLoaded = false
                                updateLocationNameFlag = true
                            }
                        })
            } else {
                //If followingLocations list is empty then hide the locationTag
//                selectedLocationTextView?.visibility = View.GONE
                isFollowingLocEmpty = true
            }
        })

//        selectedLocationTextView?.setOnClickListener {
//            sendDropDownMenuAnalytics()
//            addedLocationsFragment?.show(fragmentManager!!, AddedLocationsFragment.TAG)
//        }
    }

    //This method is invoked by the child fragments to show addedLocation bottom sheet
    fun showBottomMenu(fm: FragmentManager) {
        if (isFollowingLocEmpty) {
            sendDropDownMenuAnalytics()
            CommonNavigator.openLocationSelection(requireContext(), false, true)
            locationLoaded = false
        } else {
            sendDropDownMenuAnalytics()
            addedLocationsFragment?.show(fm, AddedLocationsFragment.TAG)
        }
    }

    private var fireSLVFor1stResponseFromDB = runOnce<CardsPojoPagedList?> {
        it?.data?.getSnapshot()?.let { list ->
            true
        } ?: false
    }

    //This method is used to check if the currently received items is same as the existing items
    private fun hasListChangedStructurally(oldSnap: List<Any?>?, newSnap: List<Any?>?): Boolean {
        if (oldSnap == null && newSnap != null) {
            return true
        }
        return if (oldSnap?.size != newSnap?.size) {
            true
        } else {

            val ids = ArrayList<String?>()
            oldSnap?.forEach {
                ids.add(if (it is CommonAsset) it.i_id() else null)
            }
            newSnap?.forEach {
                ids.remove(if (it is CommonAsset) it.i_id() else null)
            }
            ids.isNotEmpty()
        }
    }


    //When other location is selected from the AddedLocation Bottom sheet menu
    private fun sendOtherLocationAnalytics(){
        // Log explore button click for the local zone location tag.
        AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NewsReferrer.LOCAL_VIDEO_DETAIL),
                NewsExploreButtonType.OTHER_LOCATION, NhAnalyticsEventSection.NEWS.name)
    }

    //When add more location is clicked from the AddedLocation Bottom sheet menu
    private fun sendSeeMoreLocationAnalytics(){
        AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NewsReferrer.LOCAL_VIDEO_DETAIL),
                NewsExploreButtonType.ADD_MORE,  NhAnalyticsEventSection.NEWS.name)
    }

    //When AddedLocation Bottom sheet menu is opened by click on location tag
    private fun sendDropDownMenuAnalytics(){
        AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NewsReferrer.LOCAL_VIDEO_DETAIL),
                NewsExploreButtonType.DROPDOWN_MENU,  NhAnalyticsEventSection.NEWS.name)
    }
}