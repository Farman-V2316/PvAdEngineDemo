/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.relatedvideo

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.adengine.view.helper.AdDBHelper
import com.newshunt.appview.common.CardsExternalListener
import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.entity.CardsPojoPagedList
import com.newshunt.appview.common.ui.fragment.TransitionParent
import com.newshunt.appview.common.video.base.BaseVerticalVideoFragment
import com.newshunt.appview.common.video.ui.adapter.VerticalViewPagerAdapter
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ServedButNotPlayedHelper
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.runOnce
import com.newshunt.news.helper.NonLinearStore
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import java.lang.Exception
import javax.inject.Inject

/**
 * Created on Vinod.BC 30/09/2020.
 * Fragment to handle Related Video
 */
class RelatedVideoFragment() : BaseVerticalVideoFragment(), AdDBHelper, DeeplinkableDetail {
    private val TAG = "RelatedVideoFragment"
    private var bundle: Bundle? = null
    var postId: String? = null
    var position: Int = 0
    var isLocalZone: Boolean = false
    private var isLandingStory: Boolean = false
    private var parentStoryId: String? = null
    private var cardPosition = -1
    private var entityId: String? = null
    lateinit var section: String
    lateinit var location: String
    private var postEntityLevel: String? = null
    private var adId: String? = null
    private var currentPageReferrer: PageReferrer? = null
    private var referrerRaw:String? = null
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var timeSpentEventId: Long = 0
    private var card: CommonAsset? = null

    private var feedPage: FeedPage? = null
    private var relatedLoaded: Boolean = false
    private var relatedUrl: String? = null

    private var nonLinearCardList: List<NLFCItem>? = null

    @Inject
    lateinit var cVMF: CardsViewModel.Factory
    lateinit var cVM: CardsViewModel

    @Inject
    lateinit var rlVMF: RelatedVideoVM.Factory
    lateinit var rlVM: RelatedVideoVM
    private var getCurrentSnapshot: List<Any?>? = null
    private var backPressedHandled: Boolean = false

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        readBundle()
        initFromArgs()
    }

    private fun initFromArgs() {
        val p1 = arguments ?: return
        feedPage = FeedPage(
                id = "${postId}_related",
                contentUrl = p1.getString(Constants.BUNDLE_CONTENT_URL, Constants.EMPTY_STRING),
                section = section,
                contentRequestMethod = Constants.HTTP_POST
        )

        Logger.d(TAG, "Entityid = ${postId}_related")
    }

    private fun readBundle() {
        bundle = arguments
        bundle?.let {
            isLocalZone = bundle!!.getBoolean(Constants.BUNDLE_IS_LOCAL_ZONE, false)
            postId = bundle!!.getString(Constants.STORY_ID)
            position = bundle!!.getInt(Constants.STORY_POSITION, -1)
            parentStoryId = bundle!!.getString(Constants.PARENT_STORY_ID)
            cardPosition = bundle!!.getInt(NewsConstants.CARD_POSITION, -1)
            entityId = bundle!!.getString(Constants.PAGE_ID) ?: (postId!! +
                    System.currentTimeMillis().toString())
            adId = bundle!!.getString(Constants.BUNDLE_AD_ID)
            postEntityLevel = bundle!!.getString(NewsConstants.POST_ENTITY_LEVEL) ?: PostEntityLevel
                    .TOP_LEVEL.name
            isLandingStory = bundle!!.getBoolean(Constants.IS_LANDING_STORY, false)

            referrerLead = bundle!!.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            referrerRaw = bundle?.getString(Constants.REFERRER_RAW)
            if (referrerLead == null) {
                referrerLead = PageReferrer()
            }
            referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
            currentPageReferrer = PageReferrer(referrerLead)
            referrerFlow = PageReferrer(referrerLead)

        }

        section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.TV.section
        location = arguments?.getString(NewsConstants.BUNDLE_LOC_FROM_LIST)
                ?: Constants.FETCH_LOCATION_DETAIL
    }

    private fun injectDeps(feedPage: FeedPage) {
        val cardsModule = CardsModule(CommonUtils.getApplication(), SocialDB.instance(), feedPage.id, "",
                null, "detail", adDbHelper = this, supportAds = false, lifecycleOwner = this,
                section = feedPage.section, searchQuery = null, performLogin = ::performLogin)
        DaggerRelatedVideoComponent.builder()
                .cardsModule(cardsModule)
                .relatedVideoModule(RelatedVideoModule(feedPage))
                .build().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
    }

    private fun onCardsReceived(cards: CardsPojoPagedList?) {
        Logger.d(TAG, "onCardsReceived() called with: it = $cards")
        val cardsList = cards?.data?.getList()?.filterNotNull() ?: return
        val titles = cardsList.filterIsInstance<CommonAsset>().map { it.i_title() }
        Logger.d(TAG, "${cardsList.size} cardsList. $titles")
        if (!cardsList.isNullOrEmpty()) {
            val newList = cardsList as List<CommonAsset>
            Logger.d(TAG, "newList : ${newList.size}")
            newList.forEach { card ->
                ServedButNotPlayedHelper.addTORelatedServedList(card.i_id())
                Logger.d(TAG, "card postId : ${card.i_id()}")
            }
            getCurrentSnapshot = cards.data.getSnapshot()
            updateCardsList(cardsList)
        }
    }

    override fun requestRelatedVideo(relatedUrl: String?) {
        Logger.d(TAG, "requestRelatedVideo Pos : $position")
        Logger.d(TAG, "requestRelatedVideo relatedUrl : $relatedUrl")
        if (!relatedLoaded && !relatedUrl.isNullOrEmpty()) {
           relatedLoaded = true
            this.relatedUrl = relatedUrl
            rlVM.start(relatedUrl)
            cVM.start()
            Logger.d(TAG, "Related request Made")
        }
    }

    override fun requestNextPage(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        Logger.d(TAG, "requestNextPage visibleItemCount:$visibleItemCount, " +
                "firstVisibleItem:$firstVisibleItem totalItemCount:$totalItemCount")
        //As parent Item added at position 0, next page check should be > 1
        if (viewPager2.adapter != null && viewPager2.adapter?.itemCount!! > 1) {
            cVM.updateCurrentCardLocation(1, firstVisibleItem, totalItemCount)
        }
    }

    private fun initViewModel() {
        injectDeps(feedPage!!)
        cVM = ViewModelProviders.of(this, cVMF).get(CardsViewModel::class.java)
        rlVM = ViewModelProviders.of(this, rlVMF).get(RelatedVideoVM::class.java)

        cVM.mediatorCardsLiveData.observe(viewLifecycleOwner, Observer { cards ->
            if (cVM.fpRequestStatus.value == true && cards.isWaitingForData()) {
                Logger.e("TAG", "mediatorCardsLiveData :ignored")
                return@Observer
            }
            if (cards.dataIsEmptyAndNotYetSeenError()) {
                Logger.e("TAG", "mediatorCardsLiveData : not showing error untill we get error")
                return@Observer
            }
            if (cards.data?.getList().isNullOrEmpty() && cVM.started) {
                return@Observer
            } else if (cards.data != null && cards.error != null) {
                if (cards.tsError ?: 0 > cards.tsData ?: 0 && cVM.started) {
                    //If cards has cached items and server gives 204  do not updated
                    return@Observer
                }
            }
            val hasDataChanged = hasListChangedStructurally(getCurrentSnapshot, cards.data?.getSnapshot())
            //Check if the current received items is same as the previous items
            if (!hasDataChanged) {
                //If same do not update current items
                return@Observer
            }
            fireSLVFor1stResponseFromDB(cards)
            (parentFragment as? CardsExternalListener?)?.onCardsLoaded()
            onCardsReceived(cards)
        })
        cVM.firstpageData.observe(viewLifecycleOwner, Observer {
            it?.let {
                cVM.replaceFP() // both cache and network?
            }
        })

        cVM.nonLinearFeedLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess && isFragmentVisible()) {
                nonLinearCardList = it.getOrNull()
                Logger.d(TAG, "nonLinearCardList size : ${nonLinearCardList?.size}")
                if (nonLinearCardList?.isEmpty() == false) {
                    Logger.d(TAG, "nlfc cards received pos : $position")
                    insertNonLinearCards()
                }
            }
        })
    }

    private fun insertNonLinearCards() {
        try {
            if (CommonUtils.isEmpty(nonLinearCardList) || viewPager2.adapter == null) {
                return
            }
            Logger.d(TAG, "insertNonLinearCards nlfc list : ${nonLinearCardList?.size}")
            //As there is ParentPostEntity at '0' position, Nlfc should add at currentItem place
            val postId = if (viewPager2.currentItem > 0)
                getPostIdAtPosition(viewPager2.currentItem)
            else if (viewPager2.adapter?.itemCount == 1)
                getPostIdAtPosition(viewPager2.currentItem) + "_related"
            else
                getPostIdAtPosition(viewPager2.currentItem + 1)

            if (!postId.isNullOrEmpty()) {
                val nlfcItem = nonLinearCardList!![0]
                Logger.d(TAG, "Adding NLFC below pos = ${viewPager2.currentItem}")
                Logger.d(TAG, "NLFC below postId = $postId")
                Logger.d(TAG, "NLFC Item postId = ${nlfcItem.postId}")
                // considering 1st item(parent) which is part of List not related
                val insertPos = viewPager2.currentItem - 1
                cVM.insertNonLinearAt(nlfcItem = nlfcItem, id = postId, position = insertPos, url = relatedUrl)
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        } finally {
            NonLinearStore.deleteStories()
            cVM.cleanUpNonLinear()
        }
    }

    private fun performLogin(showToast: Boolean, toastMsgId: Int) {
        activity?.let {
            val sso = SSO.getInstance()
            sso.login(it as Activity, LoginMode.USER_EXPLICIT, SSOLoginSourceType.REVIEW)
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
        return if (oldSnap!!.size > newSnap!!.size) {
            //Video disliked , Do not remove in detail
            return false
        } else if (oldSnap.size != newSnap.size) {
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

    fun handleActionBarBackPress(isSystemBackPress : Boolean) : Boolean {
        // To avoid the multiple taps on back button.
        if (backPressedHandled) {
            backPressedHandled = false
            return true
        }
        if (NewsNavigator.shouldNavigateToHome(activity, currentPageReferrer, isSystemBackPress,referrerRaw)) {
            val pageReferrer = PageReferrer(NewsReferrer.VIDEO_DETAIL, postId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            NewsNavigator.navigateToHomeOnLastExitedTab(activity, pageReferrer)
            backPressedHandled = true
            return true
        }
        return false
    }


    override fun getTotalItems(): Int? = 0
    override fun getItemIdBeforeIndex(adPosition: Int): String? = null
    override fun getActivityContext(): Activity? = activity
    override fun deeplinkUrl(): String? {
        val adapter =  viewPager2.adapter as VerticalViewPagerAdapter
        val position = viewPager2.currentItem
        return adapter.getFragmentAtPosition(position)?.card?.i_deeplinkUrl()
    }

    fun getTransitionParentFragment(): TransitionParent? {
        return parentFragment as? TransitionParent
    }
    fun getCurrentPosition(): Int {
        return viewPager2?.currentItem
    }

}