package com.newshunt.appview.common.ui.adapter

import android.os.Bundle
import android.os.SystemClock
import android.util.SparseArray
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.newshunt.appview.common.ui.fragment.CollectionLandingFragment2
import com.newshunt.appview.common.ui.helper.NavigationHelper.FRAGMENT_TRANSITION_NEEDED
import com.newshunt.appview.common.ui.helper.NotificationUiType
import com.newshunt.appview.common.video.relatedvideo.RelatedVideoFragment
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.adapter.NHFragmentStatePagerAdapter
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.BaseDetailList
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.PgiNativeAdFragment
import com.newshunt.news.view.fragment.PhotoSlideDetailFragment
import com.newshunt.news.view.fragment.PlaceholderFragment
import com.newshunt.news.view.fragment.PostDetailLastPageFragment
import com.newshunt.news.view.fragment.PostDetailsFragment
import com.newshunt.news.view.view.PrefetchAdRequestCallback
import java.lang.ref.WeakReference

class  NewsDetailAdapter(fm: FragmentManager, val pageEntity: PageEntity?,
                        private var sourceId: String? = null,
                        private var sourceType: String? = null,
                        private val parentLocation: String?,
                        private val videoPlayerProvider: VideoPlayerProvider?,
                        val pageReferrer: PageReferrer?, var referrer_raw: String?,
                        val search: Boolean?, private val searchQuery: SearchQuery? = null,
                        private val landingStoryId: String,
                        var extraPageNeeded: Boolean = false,
                        private val prefetchAdRequestCallback: PrefetchAdRequestCallback,
                        private val pageId: String?,
                        private val location: String,
                        private val groupInfo: GroupInfo?,
                        private val section: String? = null,
                        val items: ArrayList<BaseDetailList>,
                        private val notificationIds: HashMap<String, String>?,
                        val isFromHistory: Boolean,
                        var isLive: Boolean,
                        val arguments: Bundle?
) :
        NHFragmentStatePagerAdapter(fm) {
    var notificationUiType: NotificationUiType? = null
    private val fragmentStack = SparseArray<WeakReference<Fragment>>()
    var isSwipeRight: Boolean = true

    override fun getItem(position: Int): Fragment {
        if (isExtraLastPage(position)) {
            return PostDetailLastPageFragment()
        }

        val timeSpentEventId = SystemClock.elapsedRealtime()
        val currentFragment = getFragment(items[position], videoPlayerProvider, search, position,
                timeSpentEventId, prefetchAdRequestCallback)
        val fragmentWeakReference: WeakReference<Fragment> = WeakReference(currentFragment)
        fragmentStack.put(position, fragmentWeakReference)
        return currentFragment
    }

    private fun getFragment(card: BaseDetailList?, videoPlayerProvider: VideoPlayerProvider?,
                            search: Boolean?, position: Int, timeSpentEventId: Long,
                            prefetchAdRequestCallback: PrefetchAdRequestCallback): Fragment {
        if (card?.i_format() == Format.VIDEO && card.i_video_assetId() != null) {
            val fragment = RelatedVideoFragment()
            val bundle = Bundle(arguments)
            bundle.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            bundle.putString(Constants.STORY_ID, items[position].i_id())
            bundle.putString(Constants.REFERRER_RAW, referrer_raw)
            bundle.putInt(Constants.STORY_POSITION, position)
            bundle.putBoolean(Constants.IS_LANDING_STORY, (items[position].i_id() == landingStoryId))
            bundle.putString(Constants.LANDING_STORY_ID, landingStoryId)
            bundle.putBoolean(Constants.RESET_MUTE_STATE, false)
            bundle.putSerializable(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
            bundle.putSerializable(Constants.BUNDLE_SEARCH_QUERY, searchQuery)
            bundle.putString(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, notificationIds?.get(items[position].i_id()))
            bundle.putString(Constants.LOCATION, parentLocation)
            bundle.putString(Constants.PAGE_ID, pageId)
            bundle.putString(NewsConstants.SOURCE_ID, sourceId)
            bundle.putString(NewsConstants.SOURCE_TYPE, sourceType)
            bundle.putString(Constants.BUNDLE_AD_ID, items[position].i_adId())
            bundle.putLong(NewsConstants.TIMESPENT_EVENT_ID, timeSpentEventId)
            bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, location)
            bundle.putBoolean(NewsConstants.CHILD_FRAGMENT, true)
            bundle.putBoolean(Constants.BUNDLE_IS_FROM_HISTORY, isFromHistory)
            bundle.putString(NewsConstants.POST_ENTITY_LEVEL, items[position].i_level()?.name)
            bundle.putSerializable(Constants.BUNDLE_GROUP_INFO, groupInfo)
            bundle.putString(NewsConstants.DH_SECTION, section)
            bundle.putBoolean(Constants.IS_LIVE, isLive)
            if (items[position].i_id() == landingStoryId) {
                fragment.setPlayerProvider(videoPlayerProvider)
            }
            if (notificationUiType != null) {
                bundle.putString(Constants.BUNDLE_NOTIFICATION_CTA_UI_TYPE, notificationUiType?.name)
                notificationUiType = null
            }
            if (isSwipeRight != null && items[position].i_id() != landingStoryId
                    && section?.equals(PageSection.TV.section) == false) {
                if (isSwipeRight) {
                    if ((position - 1) >= 0 && items[position - 1].i_format() != Format.VIDEO) {
                        bundle.putBoolean(Constants.RESET_MUTE_STATE, true)
                    }
                } else if ((position + 1) <= (items.size - 1)
                        && items[position + 1].i_format() != Format.VIDEO) {
                    bundle.putBoolean(Constants.RESET_MUTE_STATE, true)
                }
            }
            fragment.arguments = bundle
            return fragment
        } else if (card?.i_format() == Format.POST_COLLECTION) {
            val fragment = CollectionLandingFragment2()
            val bundle = Bundle(arguments)
            bundle.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            bundle.putString(Constants.STORY_ID, items[position].i_id())
            bundle.putString(Constants.REFERRER_RAW, referrer_raw)
            bundle.putInt(Constants.STORY_POSITION, position)
            bundle.putSerializable(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
            bundle.putString(Constants.PAGE_ID, pageId)
            bundle.putString(Constants.BUNDLE_AD_ID, items[position].i_adId())
            bundle.putString(NewsConstants.SOURCE_ID, sourceId)
            bundle.putString(NewsConstants.SOURCE_TYPE, sourceType)
            bundle.putString(Constants.LOCATION, parentLocation)
            bundle.putString(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, notificationIds?.get(items[position].i_id()))
            bundle.putLong(NewsConstants.TIMESPENT_EVENT_ID, timeSpentEventId)
            bundle.putBoolean(NewsConstants.CHILD_FRAGMENT, true)
            bundle.putString(NewsConstants.DH_SECTION, section)
            fragment.arguments = bundle
            return fragment
        } else if (search == true) {
            val cardDetailListCard: DetailListCard? = items[position] as? DetailListCard
            val fragment = PhotoSlideDetailFragment()
            val bundle = Bundle()
            bundle.putString(Constants.BUNDLE_POST_ID, items[position].i_id())
            bundle.putString(Constants.BUNDLE_IMAGE_URL, cardDetailListCard?.imageDetails?.url)
            bundle.putString(Constants.BUNDLE_DESCRIPTION, cardDetailListCard?.title)
            bundle.putSerializable(Constants.BUNDLE_SOURCE_ID, cardDetailListCard?.source?.id)
            bundle.putString(Constants.BUNDLE_SOURCE_NAME, cardDetailListCard?.source?.displayName)
            bundle.putString(Constants.BUNDLE_SOURCE_URL, cardDetailListCard?.source?.icon)
            bundle.putString(Constants.BUNDLE_SHARE_URL, cardDetailListCard?.shareUrl)
            bundle.putString(Constants.BUNDLE_AD_ID, cardDetailListCard?.i_adId())
            bundle.putString(NewsConstants.DH_SECTION, section)
            fragment.arguments = bundle
            return fragment
        } else if (card?.i_format() == Format.AD) {
            return PgiNativeAdFragment.newInstance(items[position], pageReferrer)
        } else if (card?.i_format() == Format.PLACEHOLDER) {
            val bundle = Bundle(arguments)
            val fragment = PlaceholderFragment()
            fragment.prefetchAdRequestCallback = prefetchAdRequestCallback
            bundle.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            bundle.putString(Constants.STORY_ID, items[position].i_id())
            bundle.putString(Constants.PAGE_ID, pageId)
            bundle.putString(Constants.REFERRER_RAW, referrer_raw)
            bundle.putInt(Constants.STORY_POSITION, position)
            bundle.putSerializable(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
            bundle.putString(Constants.LOCATION, parentLocation)
            bundle.putBoolean(Constants.IS_LANDING_STORY, (items[position].i_id() == landingStoryId))
            bundle.putLong(NewsConstants.TIMESPENT_EVENT_ID, timeSpentEventId)
            bundle.putBoolean(NewsConstants.CHILD_FRAGMENT, true)
            bundle.putString(NewsConstants.POST_ENTITY_LEVEL, items[position].i_level()?.name)
            bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, location)
            bundle.putSerializable(Constants.BUNDLE_GROUP_INFO, groupInfo)
            fragment.arguments = bundle
            return fragment
        }

        val bundle = Bundle(arguments)
        if (items[position].i_id() == landingStoryId) {
            bundle.putBoolean(FRAGMENT_TRANSITION_NEEDED, arguments?.getBoolean(FRAGMENT_TRANSITION_NEEDED, false) ?: false)
        } else {
            bundle.remove(FRAGMENT_TRANSITION_NEEDED)
        }
        val fragment = PostDetailsFragment()
        fragment.prefetchAdRequestCallback = prefetchAdRequestCallback
        bundle.putSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
        bundle.putString(Constants.STORY_ID, items[position].i_id())
        bundle.putString(Constants.PAGE_ID, pageId)
        bundle.putString(Constants.BUNDLE_AD_ID, items[position].i_adId())
        bundle.putString(NewsConstants.SOURCE_ID, sourceId)
        bundle.putString(NewsConstants.SOURCE_TYPE, sourceType)
        bundle.putString(Constants.REFERRER_RAW, referrer_raw)
        bundle.putInt(Constants.STORY_POSITION, position)
        bundle.putSerializable(NewsConstants.NEWS_PAGE_ENTITY, pageEntity)
        bundle.putString(Constants.LOCATION, parentLocation)
        bundle.putBoolean(Constants.IS_LANDING_STORY, (items[position].i_id() == landingStoryId))
        bundle.putString(Constants.LANDING_STORY_ID, landingStoryId)
        bundle.putLong(NewsConstants.TIMESPENT_EVENT_ID, timeSpentEventId)
        bundle.putString(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, notificationIds?.get(items[position].i_id()))
        bundle.putBoolean(NewsConstants.CHILD_FRAGMENT, true)
        bundle.putString(NewsConstants.POST_ENTITY_LEVEL, items[position].i_level()?.name)
        bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, location)
        bundle.putSerializable(Constants.BUNDLE_GROUP_INFO, groupInfo)
        bundle.putSerializable(Constants.BUNDLE_BOOTSTRAP_CARD, items[position])
        bundle.putString(NewsConstants.DH_SECTION, section)
        if (notificationUiType != null) {
            bundle.putString(Constants.BUNDLE_NOTIFICATION_CTA_UI_TYPE, notificationUiType?.name)
            notificationUiType = null
        }
        fragment.arguments = bundle
        return fragment
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        fragmentStack.remove(position)
    }

    override fun getItemPosition(obj: Any): Int {
        var postId: String? = null
        var fragmentPosition = 0
        if (obj is PostDetailsFragment) {
            postId = obj.postId
            fragmentPosition = obj.position
        } else if (obj is DHVideoDetailFragment) {
            postId = obj.postId
            fragmentPosition = obj.position
        } else if (obj is RelatedVideoFragment) {
            postId = obj.postId
            fragmentPosition = obj.position
        }

        if (fragmentPosition >= 0 && items.size > fragmentPosition) {
            val post = items[fragmentPosition]
            if (post.i_id() != postId) {
                return POSITION_NONE
            }
        }
        return POSITION_UNCHANGED
    }

    fun getFragmentAtPosition(position: Int): Fragment? {
        return fragmentStack.get(position)?.get()
    }

    override fun getCount(): Int {
        return items.size + if (extraPageNeeded) 1 else 0 // One extra for further browsing page or progress page
    }

    private fun isExtraLastPage(position: Int): Boolean {
        // Reorder below condition in probability of true/false
        return extraPageNeeded && (position == count - 1)
    }

    private fun addOrReplace(card: BaseDetailList): Boolean {
        val index = items.indexOf(card)
        if (index < 0) {
            // New item added
            items.add(card)
            return true
        }

        return false
    }

    fun addPosts(cards: List<BaseDetailList>?, extraPageNeededParam: Boolean) {
        var itemsChanged = false

        Logger.d("NDF2", "items size ${items.size} and cards size is $cards?.size}")

        if (items.size != cards?.size) {
            itemsChanged = true
        } else {
            items.forEach {
                // Check if item removed
                if (!cards.contains(it)) {
                    itemsChanged = true
                }
            }
        }

        if (itemsChanged) {
            items.clear()
            if (cards != null) {
                items.addAll(cards)
            }
        } else {
            // Check for new items
            cards?.forEach {
                itemsChanged = addOrReplace(it) || itemsChanged
            }
        }

        if (extraPageNeededParam != extraPageNeeded) {
            extraPageNeeded = extraPageNeededParam
            itemsChanged = true
        }

        if (itemsChanged) {
            notifyDataSetChanged()
        }
    }

    // For Ads and  NLFC
    /**
     * Returns id of post present at index <= index
     * null implies empty list or 0th position.
     */
    fun getItemIdBeforeIndex(index: Int): String? {
        return if (items.isEmpty() || index == 0)
            null
        else {
            val item = if (items.size >= index) items[index - 1] else items.last()
            return item.i_id()
        }
    }

}