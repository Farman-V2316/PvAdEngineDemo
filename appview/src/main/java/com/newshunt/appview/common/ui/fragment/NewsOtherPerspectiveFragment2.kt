package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dailyhunt.tv.players.customviews.VideoPlayerWrapper
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.appview.databinding.ActivityOtherPerspectiveBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.view.adapter.NHFragmentStatePagerAdapter
import com.newshunt.common.view.customview.NHCarouselViewPager
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.news.di.DetailFullPostModule
import com.newshunt.news.di.DetailsModule2
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.PostDetailsFragment
import com.newshunt.news.viewmodel.DetailsViewModel
import dagger.Component
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Parent fragment for Other Perspective view.
 *
 * Created by priya.gupta on 2019-09-10.
 */
class NewsOtherPerspectiveFragment2 : BaseSupportFragment(),
        androidx.viewpager.widget.ViewPager.OnPageChangeListener, TransitionParent {

    companion object {
        @JvmStatic
        fun newInstance(intent: Intent, videoPlayerProvider: VideoPlayerProvider?): NewsOtherPerspectiveFragment2 {
            val detailContainerFragment = NewsOtherPerspectiveFragment2()
            detailContainerFragment.setPlayerProvider(videoPlayerProvider)
            detailContainerFragment.arguments = Bundle(intent.extras)
            return detailContainerFragment
        }
    }

    private lateinit var binding: ActivityOtherPerspectiveBinding
    @Inject
    lateinit var detailsViewModelF: DetailsViewModel.Factory
    private lateinit var landingStoryId: String
    private lateinit var parentStoryId: String
    private lateinit var viewPager: NHCarouselViewPager
    private var adapter: NewsDetailOPAdapter? = null
    private lateinit var vm: DetailsViewModel
    private lateinit var section: String
    private lateinit var location: String
    private lateinit var entityId: String
    private var cardList: List<TopLevelCard>? = null
    private var loaded: Boolean = false
    private var videoWrapper: VideoPlayerWrapper? = null
    private var videoPlayerProvider: VideoPlayerProvider? = null
    private var timeSpentEventId: Long = 0
    private var pageEntity: PageEntity? = null
    private var pageReferrer: PageReferrer? = null
    private var referrerLead: PageReferrer? = null
    private var referrerFlow: PageReferrer? = null
    @Inject
    lateinit var transitionParentDelegate: TransitionParent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.activity_other_perspective, container, false)
        parentStoryId = arguments?.getString(Constants.PARENT_STORY_ID) ?: ""
        landingStoryId = arguments?.getString(Constants.STORY_ID) ?: ""
        section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
        timeSpentEventId = arguments?.getLong(NewsConstants.TIMESPENT_EVENT_ID, 0L) ?: 0L
        pageReferrer = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
        if(pageReferrer == null){
            pageReferrer = PageReferrer()
        }

        referrerLead = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
        if (referrerLead == null) {
            referrerLead = PageReferrer()
        }
        referrerLead?.referrerAction = NhAnalyticsUserAction.CLICK
        referrerFlow = PageReferrer(referrerLead)
        pageEntity = arguments?.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity?
        viewPager = binding.carouselPager
        ViewUtils.setLinearTransition(viewPager, context)
        viewPager.offscreenPageLimit = 1

        entityId = arguments?.getString(Constants.PAGE_ID) ?: "1"
        location = arguments?.getString(Constants.LOCATION) ?: Constants.FETCH_LOCATION_DETAIL
        viewPager.addOnPageChangeListener(this)

        if (parentStoryId == null) {
            return null
        }

        DaggerOtherPerspectiveFragmentComponent2.builder().detailsModule2(
            DetailsModule2(
                CommonUtils.getApplication(),
                SocialDB.instance(),
                entityId,
                parentStoryId,
                timeSpentEventId, false,
                location,
                lifecycleOwner = this,
                section = section,
                fragmentManager = activity?.supportFragmentManager,
                listLocation = Constants.FETCH_LOCATION_DETAIL,
                referrerFlow = referrerFlow ?: PageReferrer(referrerLead),
                fragment = this,
                fragmentName = "NewsOtherPerspectiveFragment2")).build().inject(this)

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

    fun setPlayerProvider(provider: VideoPlayerProvider?) {
        videoPlayerProvider = provider
        videoWrapper = videoPlayerProvider?.videoPlayerWrapper as? VideoPlayerWrapper?
    }

    private fun loadContent() {

        if (!loaded) {
            loaded = true
            var curPosition: Int = 0

            cardList?.forEachIndexed { index, card ->
                if (card.i_id() == landingStoryId) {
                    curPosition = index
                }
            }

            if (cardList != null && adapter == null) {
                adapter = NewsDetailOPAdapter(childFragmentManager,
                    pageEntity,
                    location,
                    entityId,
                    videoPlayerProvider,
                    pageReferrer,
                    section,
                    landingStoryId,
                    arguments?.getBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, false) ?: false)
                viewPager.adapter = adapter
                adapter?.setParent(parentStoryId)
                adapter?.setPosts(cardList!!)
                viewPager.currentItem = curPosition
            }
        }
    }


    override fun onPageScrollStateChanged(state: Int) {
        // Do nothing
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // Do nothing
    }

    override fun onPageSelected(position: Int) {
        if (adapter?.stories?.isEmpty() == true || !::landingStoryId.isInitialized) return
        adapter?.stories?.let {
            transitionParentDelegate.onPageSwipe(it[position].i_id(), landingStoryId)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val currentFragment = adapter?.getFragmentAtPosition(binding.carouselPager.currentItem)
        if (currentFragment != null) {
            currentFragment.onHiddenChanged(hidden)
        }
    }

    override fun prepareSharedElementTransition(animatedView: View) {
        transitionParentDelegate.prepareSharedElementTransition(animatedView)
    }
}

@Component(modules = [DetailsModule2::class, DetailFullPostModule::class])
interface OtherPerspectiveFragmentComponent2 {
    fun inject(component: NewsOtherPerspectiveFragment2)
}

class NewsDetailOPAdapter(fm: FragmentManager, val pageEntity: PageEntity?,
                          private val location: String?, private val entityId: String?,
                          private val videoPlayerProvider: VideoPlayerProvider?,
                          val pageReferrer: PageReferrer?, private val section: String,
                          private val landingStoryId: String?,
                          private val fragmentTransitionNeeded: Boolean = false) :
        NHFragmentStatePagerAdapter(fm) {

    private val fragmentStack = SparseArray<WeakReference<Fragment>>()

    override fun getItem(position: Int): Fragment {
        val currentFragment = getFragment(stories[position], videoPlayerProvider)
        val bundle = Bundle()
        bundle.putString(Constants.STORY_ID, stories[position].i_id())
        bundle.putString(Constants.PAGE_ID, entityId)
        bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, location)
        bundle.putString(Constants.LOCATION, location)
        bundle.putString(Constants.PARENT_STORY_ID, parentStoryId)
        bundle.putInt(NewsConstants.CARD_POSITION, position)
        bundle.putInt(NewsConstants.COLLECTION_ITEM_COUNT, stories.size - 1)
        bundle.putBoolean(Constants.IS_LANDING_STORY, (stories[position].i_id() == landingStoryId))
        bundle.putBoolean(NewsConstants.IS_IN_COLLECTION, true)
        bundle.putString(NewsConstants.POST_ENTITY_LEVEL, stories[position].i_level().name)
        bundle.putSerializable(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
        bundle.putString(NewsConstants.DH_SECTION, section)
        bundle.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
        currentFragment.arguments = bundle
        val fragmentWeakReference: WeakReference<Fragment> = WeakReference(currentFragment)
        fragmentStack.put(position, fragmentWeakReference)
        if (fragmentTransitionNeeded && landingStoryId == stories[position].i_id()) {
            bundle.putBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, fragmentTransitionNeeded)
        }
        return currentFragment
    }

    private fun getFragment(card: CommonAsset?, videoPlayerProvider: VideoPlayerProvider?): Fragment {
        if (card?.i_format() == Format.VIDEO && card?.i_videoAsset() != null) {
            val fragment = DHVideoDetailFragment()
            fragment.setPlayerProvider(videoPlayerProvider)
            return fragment
        }

        return PostDetailsFragment()
    }

    val stories: ArrayList<TopLevelCard> = ArrayList()
    var parentStoryId: String? = null
    override fun getCount(): Int {
        return stories.size
    }

    fun setPosts(cards: List<TopLevelCard>) {
        stories.clear()
        stories.addAll(cards)
        notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return stories[position].i_title()
    }

    fun getSourceName(position: Int): String? {
        return stories[position].i_source()?.displayName
    }

    fun setParent(data: String) {
        this.parentStoryId = data

    }

    fun getFragmentAtPosition(position: Int): Fragment? {
        return fragmentStack.get(position)?.get()
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        fragmentStack.remove(position)
    }

}