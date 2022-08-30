/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.dailyhunt.tv.players.customviews.VideoPlayerWrapper
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.appview.R
import com.newshunt.appview.common.helper.ReportAdsMenuFeedbackHelper
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.video.relatedvideo.RelatedVideoFragment
import com.newshunt.appview.common.video.ui.helper.PlayerState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.appview.databinding.ActivityCarouselNewBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.adapter.NHFragmentStatePagerAdapter
import com.newshunt.common.view.customview.NHCarouselViewPager
import com.newshunt.common.view.customview.OnSwipeOutofBoundsListener
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.MoreStoriesPojo
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.news.di.DetailFullPostModule
import com.newshunt.news.di.DetailsModule2
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.PostDetailsFragment
import com.newshunt.news.view.fragment.TAG
import com.newshunt.news.viewmodel.DetailsViewModel
import dagger.Component
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

private const val LOG_TAG = "NewsCarouselFragment2"

class NewsCarouselFragment2 : BaseSupportFragment(), ErrorMessageBuilder.ErrorMessageClickedListener,
    ViewPager.OnPageChangeListener, OnSwipeOutofBoundsListener, View.OnClickListener, OnAdReportedListener, DeeplinkableDetail, TransitionParent {

    companion object {

        @JvmStatic
        fun newInstance(intent: Intent, videoPlayerProvider: VideoPlayerProvider?): NewsCarouselFragment2 {
            val detailContainerFragment = NewsCarouselFragment2()
            detailContainerFragment.setPlayerProvider(videoPlayerProvider)
            detailContainerFragment.arguments = Bundle(intent.extras)
            return detailContainerFragment
        }
    }

    private lateinit var binding: ActivityCarouselNewBinding
    private lateinit var landingStoryId: String
    private lateinit var parentStoryId: String
    private lateinit var section: String
    private lateinit var location: String
    private lateinit var entityId: String
    @Inject
    lateinit var detailsViewModelF: DetailsViewModel.Factory
    private lateinit var vm: DetailsViewModel
    private lateinit var viewPager: NHCarouselViewPager
    private var adapter: NewsDetailCollectionsAdapter? = null
    private var videoWrapper: VideoPlayerWrapper? = null
    private var collectionItems: ArrayList<CommonAsset> = ArrayList()
    private var parentCard: CommonAsset? = null
    private var videoPlayerProvider: VideoPlayerProvider? = null
    private var timeSpentEventId: Long = 0
    private var cardList: List<TopLevelCard>? = null
    private var pageReferrer: PageReferrer ? = null
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    private var isScrollStateIdle: Boolean = true
    private var pageEntity : PageEntity? = null
    private var parentLocation : String? = null
    private var adReportMenuListener: ReportAdsMenuListener? = null
    @Inject
    lateinit var transitionParentDelegate: TransitionParent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        parentStoryId = arguments?.getString(Constants.PARENT_STORY_ID) ?: ""
        landingStoryId = arguments?.getString(Constants.STORY_ID) ?: ""
        section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
        entityId = arguments?.getString(Constants.PAGE_ID) ?: "1"
        location = arguments?.getString(Constants.LOCATION) ?: Constants.FETCH_LOCATION_DETAIL
        timeSpentEventId = arguments?.getLong(NewsConstants.TIMESPENT_EVENT_ID, 0L) ?: 0L
        pageReferrer = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
        if(pageReferrer == null){
            pageReferrer = PageReferrer()
        }

        if (arguments != null &&
                arguments!!.getBoolean(NewsConstants.BUNDLE_FORCE_NIGHT_MODE, false)) {
            val contextThemeWrapper = ContextThemeWrapper(activity, ThemeType.NIGHT.themeId)
            val localInflater = inflater.cloneInContext(contextThemeWrapper)
            binding = DataBindingUtil.inflate(localInflater, R.layout.activity_carousel_new, container, false)
        } else {
            binding = DataBindingUtil.inflate(inflater, R.layout.activity_carousel_new, container, false)
        }

        referrerLead = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
        if (referrerLead == null) {
            referrerLead = PageReferrer()
        }

        referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
        referrerFlow = PageReferrer(referrerLead)
        viewPager = binding.carouselPager
        ViewUtils.setLinearTransition(viewPager, context)
        viewPager.addOnPageChangeListener(this)
        viewPager.addOnSwipeOutofBoundsListener(this)
        pageEntity = arguments?.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity?
        parentLocation = arguments?.getString(Constants.LOCATION)

        if (parentStoryId == null) {
            return null
        }
        DaggerCarousalFragmentComponent2.builder().detailsModule2(DetailsModule2(
                CommonUtils.getApplication(),
                SocialDB.instance(),
                entityId,
                parentStoryId,
                timeSpentEventId, false,
                arguments?.getString(Constants.LOCATION) ?: Constants.FETCH_LOCATION_DETAIL,
                parentEntity = pageEntity,
                parentLocation = parentLocation,
                lifecycleOwner = this, section = section,
                fragmentManager = activity?.supportFragmentManager,
                listLocation = Constants.FETCH_LOCATION_DETAIL,
                referrerFlow = referrerFlow ?: PageReferrer(referrerLead), fragment = this, fragmentName = LOG_TAG)).build().inject(this)

        vm = ViewModelProviders.of(this, detailsViewModelF)[DetailsViewModel::class.java]
        if (pageReferrer != null) {
            vm.pageReferrer = pageReferrer!!
        }
        vm.fetchOP()?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                cardList = it
                loadContent()
            }
        })

        transitionParentDelegate.postponeEnterTransition(savedInstanceState, arguments)
        return binding.root
    }

    private fun loadContent() {
        var initialPosition = 0
        if (cardList != null && (cardList?.size?:0) > 0) {
            collectionItems.clear()
            cardList?.forEachIndexed { index, _ ->
                val currentItem = cardList?.get(index)
                if (currentItem != null) {
                    if (index == 0) {
                        parentCard = currentItem
                    }
                    else {
                        if (currentItem.i_id() == landingStoryId) {
                            initialPosition = index -1
                        }

                        collectionItems.add(currentItem)
                    }
                }
            }
        }

        if (adapter == null && collectionItems.isNotEmpty()) {
            val items = ArrayList<CommonAsset>()
            items.addAll(collectionItems)
            adapter = NewsDetailCollectionsAdapter(childFragmentManager,
                pageEntity,
                videoPlayerProvider,
                location,
                entityId,
                section,
                landingStoryId,
                items,
                arguments?.getBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, false) ?: false)
            adapter?.parentStoryId=parentStoryId
            viewPager.adapter = adapter
            viewPager.currentItem = initialPosition
            viewPager.setCurrentItem(initialPosition, false)
        }
        else {
            adapter?.setPosts(collectionItems.map { ParentIdHolderCommenAsset(parentStoryId,it) })
        }

        parentCard?.i_adId()?.let { adId ->
            if (adReportMenuListener == null) {
                adReportMenuListener = ReportAdsMenuFeedbackHelper(this, this)
            }
        }

        if(ThemeUtils.themeAutoSwitchSnackbarNeededInDetail()){
            val snackbarView = binding.snackbarContainer
            AndroidUtils.getMainThreadHandler().postDelayed({
            ThemeUtils.showThemeSnackbar(snackbarView,Constants.THEME_SNACKBAR_DETAIL,PageReferrer(NewsReferrer.STORY_DETAIL) )
            ThemeUtils.setThemePreferences(ThemeUtils.themeAutoSwitchSnackbarNeededInList(),false, ThemeUtils.themeAutoSwitchToastNeededInList(), ThemeUtils.themeAutoSwitchToastNeededInDetail())}, 500)
        }  else if(ThemeUtils.themeAutoSwitchToastNeededInDetail()){
            ThemeUtils.showThemeToast()
            ThemeUtils.setThemePreferences(ThemeUtils.themeAutoSwitchSnackbarNeededInList(), ThemeUtils.themeAutoSwitchSnackbarNeededInDetail(), ThemeUtils.themeAutoSwitchToastNeededInList(),false)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        VideoHelper.videoStateLiveData.observe(viewLifecycleOwner, Observer {
            handlePlayerState(it)
        })
    }

    private fun handlePlayerState(it: PlayerState) {
        if (!this::viewPager.isInitialized) {
            return
        }
        Logger.d(TAG, "handlePlayerState :: PlayerState ${it.state}")
        when (it.state) {
            PLAYER_STATE.STATE_VIDEO_START -> requestAdsForAdjacentVideos(viewPager.currentItem)
            PLAYER_STATE.STATE_AD_START -> onAdStart()
            PLAYER_STATE.STATE_AD_END -> onAdEnd()
            PLAYER_STATE.STATE_BOTTOM_BAR_VISIBLE -> disableVerticalSwipe()
            PLAYER_STATE.STATE_BOTTOM_BAR_HIDDEN -> enableVerticalSwipe()
            PLAYER_STATE.STATE_FULLSCREEN_ON -> toggleUIForFullScreen(true)
            PLAYER_STATE.STATE_FULLSCREEN_OFF -> toggleUIForFullScreen(false)

//            //AutoScroll will happen on vertical now
//            PLAYER_STATE.STATE_VIDEO_END -> moveToNextVideo(viewPager.currentItem)
//            PLAYER_STATE.STATE_ERROR -> moveToNextVideo(viewPager.currentItem)
        }
    }

    private fun onAdStart() {
        if (!isScrollStateIdle) {
            return
        }

        viewPager?.disableSwipe = true
        (adapter?.getFragmentAtPosition(viewPager?.currentItem ?: 0)
                as? DHVideoDetailFragment)?.handleUI(true)
    }

    private fun onAdEnd() {
        viewPager?.disableSwipe = false
        (adapter?.getFragmentAtPosition(viewPager?.currentItem ?: 0)
                as? DHVideoDetailFragment)?.handleUI(false)
    }

    //Function to enable vertical swipes
    private fun enableVerticalSwipe() {
        viewPager.disableSwipe = false
    }

    //Function to disable vertical swipes
    private fun disableVerticalSwipe() {
        viewPager.disableSwipe = true
    }

    private fun moveToNextVideo(position: Int) {
        if (!isAdded) {
            return
        }
        val nextPos = position + 1
        Logger.d(TAG, "moveToNextVideo :: nextPos $nextPos")
        if (nextPos < adapter?.count?:0) {
            viewPager?.setCurrentItem(nextPos, true)
        }
    }

    private fun requestAdsForAdjacentVideos(position: Int) {
        val nextFragment = adapter?.getFragmentAtPosition(position + 1)
        val prevFragment = adapter?.getFragmentAtPosition(position - 1)
        if (nextFragment is DHVideoDetailFragment) {
            nextFragment.requestInstreamAd()
        }
        if (prevFragment is DHVideoDetailFragment) {
            prevFragment.requestInstreamAd()
        }
    }

    override fun handleBackPress(): Boolean {
        if (!::viewPager.isInitialized) {
            return false
        }
        viewPager.let {
            val curFragment = adapter?.getFragmentAtPosition(viewPager?.currentItem ?: 0)
            if (curFragment is BaseFragment) {
                return curFragment.handleBackPress()
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                Constants.REPORTED_ADS_RESULT_CODE -> {
                    val reportedAdEntity = (intent?.getSerializableExtra(Constants.REPORTED_ADS_ENTITY) as? BaseDisplayAdEntity?)
                    val reportedAdParentUniqueAdIdIfCarousal = intent?.getStringExtra(Constants.PARENT_UNIQUE_ADID_REPORTED_ADS_ENTITY)
                    if (parentCard?.i_adId() == reportedAdParentUniqueAdIdIfCarousal) {
                        onAdReported(reportedAdEntity, reportedAdParentUniqueAdIdIfCarousal)
                        activity?.onBackPressed()
                    }
                }
            }
    }

    override fun onAdReported(reportedAdEntity: BaseAdEntity?,
                              reportedParentAdIdIfCarousel: String?) {
        vm.reportAd(reportedParentAdIdIfCarousel)
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_IDLE, null)
    }

    override fun onPageScrollStateChanged(state: Int) {
        isScrollStateIdle = (state == ViewPager.SCROLL_STATE_IDLE)
        if (isScrollStateIdle) {
            if (adapter?.getFragmentAtPosition(viewPager?.currentItem ?: 0) != null) {
                val currentFragment = adapter?.getFragmentAtPosition(viewPager?.currentItem ?: 0) as? DHVideoDetailFragment
                if (currentFragment?.isAdsPlaying() == true) {
                    onAdStart()
                } else {
                    onAdEnd()
                }
            } else {
                onAdEnd()
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (adapter?.stories?.isEmpty() == true || !::landingStoryId.isInitialized) return
        adapter?.stories?.let {
            transitionParentDelegate.onPageSwipe(it[position].i_id(), landingStoryId)
        }
    }

    override fun onRetryClicked(view: View?) {
        // Do nothing
    }

    override fun onNoContentClicked(view: View?) {
        // Do nothing
    }

    override fun onSwipeOutAtStart(): Boolean {
        return false
    }

    override fun onSwipeOutAtEnd(): Boolean {
        return true
    }

    override fun onClick(v: View?) {
        loadMoreStoriesClicked()
    }

    fun setPlayerProvider(provider: VideoPlayerProvider?) {
        videoPlayerProvider = provider
        videoWrapper = videoPlayerProvider?.videoPlayerWrapper as? VideoPlayerWrapper?
    }

    private fun loadMoreStoriesClicked() {
        val nextPageUrl = parentCard?.i_carouselProperties()?.nextPageUrl
        vm.fetchCarousalMoreStoriesNextPage(nextPageUrl)
        vm.carouselMoreStoriesNP.observe(this, Observer {
            updateCollection(it)
        })
    }

    fun updateCollection(response: MoreStoriesPojo) {
        val stories = response.data
        if (stories == null || stories.isEmpty()) {
            return
        }
        viewPager?.visibility = View.VISIBLE
        adapter?.updateStories(Collections.emptyList())
        adapter?.updateStories(stories.toList())
        adapter?.notifyDataSetChanged()
        val count = stories.size
        if (count > 0) {
            viewPager?.setCurrentItem(0, false)

        }
    }

    fun toggleUIForFullScreen(isFullScreen: Boolean) {
        if(isFullScreen) {
            setViewVisibility(View.GONE)
            viewPager?.disableSwipe = true
        } else {
            setViewVisibility(View.VISIBLE)
            viewPager?.disableSwipe = false
        }
    }

    private fun setViewVisibility(visibility: Int) {
        binding.shimmerContainer.visibility = visibility
        binding.errorScreen.visibility = visibility
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (adapter?.getFragmentAtPosition(viewPager.currentItem) != null) {
            val currentFragment = adapter?.getFragmentAtPosition(viewPager.currentItem)
            currentFragment?.onHiddenChanged(hidden)
        }
    }

    override fun deeplinkUrl(): String? {
        if (collectionItems.isNotEmpty() && collectionItems.size >= viewPager.currentItem) {
            val card = collectionItems[viewPager.currentItem]
            return card.i_deeplinkUrl()
        }
        return null
    }

    override fun prepareSharedElementTransition(animatedView: View) {
        transitionParentDelegate.prepareSharedElementTransition(animatedView)
    }
}

@Component(modules = [DetailsModule2::class, DetailFullPostModule::class])
interface CarousalFragmentComponent2 {
    fun inject(component: NewsCarouselFragment2)
}

class NewsDetailCollectionsAdapter(fm: FragmentManager, val pageEntity: PageEntity?,
                                   private val videoPlayerProvider: VideoPlayerProvider?,
                                   private val location: String?,
                                   private val entityId: String?,
                                   private val section: String?,
                                   private val landingStoryId: String?,
                                   val stories: ArrayList<CommonAsset>,
                                   private val fragmentTransitionNeeded: Boolean = false)
    : NHFragmentStatePagerAdapter(fm) {

    private val fragmentStack = SparseArray<WeakReference<Fragment>>()
    var parentStoryId: String? = null

    override fun getItem(position: Int): Fragment {

        val currentFragment = getFragment(stories[position], videoPlayerProvider)
        val bundle = Bundle()
        bundle.putString(Constants.STORY_ID, stories[position].i_id())
        bundle.putString(Constants.PAGE_ID, entityId)
        bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, location)
        bundle.putString(Constants.LOCATION, location)
        bundle.putInt(Constants.STORY_POSITION, position)
        bundle.putString(Constants.PARENT_STORY_ID, parentStoryId)
        bundle.putString(Constants.BUNDLE_AD_ID, stories[position].i_adId())
        bundle.putBoolean(Constants.IS_LANDING_STORY, (stories[position].i_id() == landingStoryId))
        bundle.putBoolean(NewsConstants.IS_IN_CAROUSEL, true)
        bundle.putString(NewsConstants.POST_ENTITY_LEVEL, stories[position].i_level().name)
        bundle.putSerializable(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
        bundle.putString(NewsConstants.DH_SECTION, section)
        if (fragmentTransitionNeeded && landingStoryId == stories[position].i_id()) {
            bundle.putBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, fragmentTransitionNeeded)
        }
        if (currentFragment is DHVideoDetailFragment)
            stories[position].i_videoAsset()?.liveStream?.let { bundle.putBoolean(Constants.IS_LIVE, it) }
        currentFragment.arguments = bundle
        val fragmentWeakReference: WeakReference<Fragment> = WeakReference<Fragment>(currentFragment)
        fragmentStack.put(position, fragmentWeakReference)
        return currentFragment
    }

    private fun getFragment(card: CommonAsset?, videoPlayerProvider: VideoPlayerProvider?): Fragment {
        if (card?.i_format() == Format.VIDEO && card?.i_videoAsset() != null) {
            val fragment = RelatedVideoFragment()
            fragment.setPlayerProvider(videoPlayerProvider)
            return fragment
        }
        return PostDetailsFragment()
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        fragmentStack.remove(position)
    }

    fun getFragmentAtPosition(position: Int): Fragment? {
        return fragmentStack.get(position)?.get()
    }

    override fun getCount(): Int {
        return stories.size
    }


    fun setPosts(postAssets: List<CommonAsset>) {
        stories.clear()
        stories.addAll(postAssets)
        notifyDataSetChanged()
    }

    fun updateStories(postAssets: List<CommonAsset>) {
        stories.clear()
        postAssets.forEach {
            if (!stories.contains(it)) {
                stories.add(it)

            }
        }
        notifyDataSetChanged()
    }
}


