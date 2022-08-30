/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.adapter

import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.BaseMediaItem
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.appview.common.video.helpers.ExoRequestHelper
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ConfigType
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.news.util.NewsConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.HashSet


/**
 * Created on Vinod.BC 30/09/2020.
 * ViewPager2 Adapter to vertical videos
 */
class VerticalViewPagerAdapter(private val fragment: Fragment, private val finalBundle: Bundle,
                               private val parentPostId: String?,
                               private val parentPosition: Int,
                               private val playerProvider: VideoPlayerProvider?) :
        FragmentStateAdapter(fragment), VideoPrefetchCallback, ExoDownloadHelper.VideoCacheListener {

    companion object {
        const val TAG: String = "VerticalViewPagerAdapter"
        const val LOG_TAG_CACHE = "VerticalViewPagerAdapter::Cache"
    }

    private val items: ArrayList<String> = ArrayList()
    private var uniqueList: HashSet<String> = HashSet()
    private var relatedCardList: ArrayList<CommonAsset> = ArrayList()
    private val fragmentStack = SparseArray<WeakReference<DHVideoDetailFragment>>()
    private var isLocalZone: Boolean = false
    private val configType = ConfigType.VIDEO_DETAIL_V
    var position = 0
    private var isNextVideoPrefetchInProgress = false
    private var isRelatedVideosPrefetchInProgress = false

    init {

        if (!parentPostId.isNullOrEmpty()) {
            items.add(parentPostId)
        }
        isLocalZone = finalBundle.getBoolean(Constants.BUNDLE_IS_LOCAL_ZONE, false)

        registerFragmentTransactionCallback(object :
                FragmentStateAdapter.FragmentTransactionCallback() {

            override fun onFragmentMaxLifecyclePreUpdated(fragment: Fragment, maxLifecycleState: Lifecycle.State): OnPostEventListener {
                Logger.d(TAG, "onFragmentMaxLifecyclePreUpdated parentPos : $parentPosition")
                Logger.d(TAG, "onFragmentMaxLifecyclePreUpdated parentmaxLifecycleStatePos : $maxLifecycleState")
                if ((maxLifecycleState == Lifecycle.State.STARTED ||
                                maxLifecycleState == Lifecycle.State.RESUMED) &&
                        fragment is DHVideoDetailFragment) {
                    Logger.d(TAG, "onFragmentPreAdded at verticalPosition ${fragment.verticalPosition}")
                    val fragmentWeakReference: WeakReference<DHVideoDetailFragment> = WeakReference(fragment)
                    fragmentStack.put(fragment.verticalPosition, fragmentWeakReference)
                }
                return super.onFragmentMaxLifecyclePreUpdated(fragment, maxLifecycleState)
            }

            override fun onFragmentPreRemoved(fragment: Fragment): OnPostEventListener {
                Logger.d(TAG, "onFragmentPreRemoved parentPos : $parentPosition")
                if (fragment is DHVideoDetailFragment) {
                    Logger.d(TAG, "onFragmentPreRemoved at ${fragment.verticalPosition}")
                    fragmentStack.remove(fragment.verticalPosition)
                }
                return super.onFragmentPreRemoved(fragment)
            }

        })
    }

    override fun createFragment(position: Int): Fragment {
        return getVideoDetailInstance(position)
    }

    private fun getVideoDetailInstance(verticalPosition: Int): DHVideoDetailFragment {
        Logger.d(TAG, "getVideoDetailInstance parentPos : $parentPosition")
        Logger.d(TAG, "getVideoDetailInstance verticalPosition : $verticalPosition")
        if (getFragmentAtPosition(verticalPosition) != null &&
                getFragmentAtPosition(verticalPosition)?.postId == getPostId(verticalPosition)) {
            Logger.d(TAG, "getVideoDetailInstance Fragment from cache")
            return getFragmentAtPosition(verticalPosition)!!
        }
        val videoFragment = DHVideoDetailFragment()
        var newBundle = Bundle(finalBundle)
        newBundle.putInt(Constants.LIST_VERTICAL_POSITION, verticalPosition)
        if (verticalPosition == 0 && !isLocalZone) {
            videoFragment.setPlayerProvider(playerProvider)
        }

        videoFragment.setPrefetchCallback(this)
        updateBundleParams(verticalPosition, newBundle)

        videoFragment.arguments = newBundle
        return videoFragment
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return  items[position].hashCode().toLong()
//        return if (isLocalZone) {
//            items[position].hashCode().toLong()
//        } else {
//            super.getItemId(position)
//        }
    }

    override fun containsItem(itemId: Long): Boolean {
        return items.any { it.hashCode().toLong() == itemId }
//        return if (isLocalZone) {
//            items.any { it.hashCode().toLong() == itemId }
//        } else {
//            super.containsItem(itemId)
//        }
    }

    fun update(cardList: List<CommonAsset>?) {
        if (cardList.isNullOrEmpty()) {
            items.clear()
            fragmentStack.clear()
            if (!parentPostId.isNullOrEmpty()) {
                items.add(parentPostId)
            }
            notifyDataSetChanged()
            return
        }

        var itemsChanged = false
        var itemSize = items.size
        if (!parentPostId.isNullOrEmpty()) {
            itemSize -= 1
        }

        Logger.d(TAG, "parentPostId id = $parentPostId")
        val uniqueCardList = ArrayList<CommonAsset>()
        relatedCardList.clear()
        uniqueList.clear()
        if (!parentPostId.isNullOrEmpty()) {
            uniqueList.add(parentPostId)
        }
        cardList.forEach {
            Logger.d(TAG, "Item id = ${it.i_id()}")
            if (!uniqueList.contains(it.i_id()) && it.i_format() == Format.VIDEO) {
                uniqueList.add(it.i_id())
                uniqueCardList.add(it)
                relatedCardList.add(it)
            } else {
                Logger.d(TAG, "AAA Duplicate Item id = ${it.i_id()}")
            }
        }

        Logger.d(TAG, "items size $itemSize and cards size is ${cardList?.size}")
        Logger.d(TAG, "items size $itemSize and uniquicCardList size is ${uniqueCardList?.size}")

        if (itemSize != uniqueCardList?.size) {
            itemsChanged = true
        } else {
            uniqueCardList.forEach {
                // Check if item removed
                if (!items.contains(it.i_id())) {
                    Logger.d(TAG, "AAA ${it.i_id()} did not match")
                    itemsChanged = true
                }
            }
        }
        Logger.d(TAG, "itemsChanged : $itemsChanged")
        if (itemsChanged) {
            items.clear()
            fragmentStack.clear()
            if (!parentPostId.isNullOrEmpty()) {
                items.add(parentPostId)
            }
            if (uniqueCardList != null) {
                items.addAll(uniqueCardList.map { it.i_id() })
            }
        } else {
            // Check for new items
            uniqueCardList?.forEach {
                itemsChanged = addOrReplace(it) || itemsChanged
            }
        }

        if (itemsChanged) {
            notifyDataSetChanged()
            //In case, video renders and then related items response is received
            GlobalScope.launch(Dispatchers.IO) {
                prefetchNextVideoOnOverFlow(position, -1)
            }
        }
    }

    private fun addOrReplace(card: CommonAsset): Boolean {
        val index = items.indexOf(card.i_id())
        if (index < 0) {
            // New item added
            items.add(card.i_id())
            return true
        }
        return false
    }

    private fun updateBundleParams(pos: Int, bundle: Bundle) {
        if (pos < items.size) {
            bundle.putString(Constants.STORY_ID, items.get(pos))
            if (isLocalZone) {
                //Changing the entityId for related items
                bundle.putString(Constants.PAGE_ID, Constants.LOCAL_ZONE_PAGE_ID)
                bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, Constants.FETCH_LOCATION_DETAIL)
            } else if (pos > 0 && !parentPostId.isNullOrEmpty()) {
                //This are the Items related to First items
                bundle.putBoolean(Constants.IS_LANDING_STORY, false)
                //Changing the entityId for related items
                bundle.putString(Constants.PAGE_ID, parentPostId + "_related")
                bundle.putString(NewsConstants.BUNDLE_LOC_FROM_LIST, Constants.FETCH_LOCATION_DETAIL)
                bundle.putBoolean(Constants.RESET_MUTE_STATE, false)
            }
        }
    }

    fun getFragmentAtPosition(position: Int): DHVideoDetailFragment? {
        return fragmentStack.get(position)?.get()
    }

    fun getPostId(position: Int): String? {
        return if (position >= 0 && position < items.size) {
            items[position]
        } else null
    }

    fun destroy() {
        Logger.d(TAG, "Vertical Viewpager is destroy")
        items.clear()
        fragmentStack.clear()
        relatedCardList.clear()
        notifyDataSetChanged()
    }

    /**
     * 1. Prefetch current item on priority - if not prefetched
     * 2. If m-config videos are already prefetched - prefetch m + 1, if user view video m
     */
    override fun onRenderedFirstFrame(position: Int, asset: CommonAsset?) {
        if(CacheConfigHelper.disableCache || isNextVideoPrefetchInProgress) {
            Logger.d(LOG_TAG_CACHE, "onRenderedFirstFrame disableCache : ${CacheConfigHelper.disableCache}" +
                    "isNextVideoPrefetchInProgress : $isNextVideoPrefetchInProgress");
            return
        }
        isNextVideoPrefetchInProgress = true
        GlobalScope.launch(Dispatchers.IO) {
            delay(500)
            if (DHVideoUtils.isEligibleToPrefetchInDetail(asset)) {
                ExoRequestHelper.prefetchVideo(position, asset, true, configType)
                Logger.d(LOG_TAG_CACHE, "onRenderedFirstFrame() cacheVideo")
            } else {
                Logger.d(LOG_TAG_CACHE, "onRenderedFirstFrame() isEligibleToPrefetch = false")
                ExoDownloadHelper.resumeVideoDownload()
            }
            prefetchNextVideoOnOverFlow(position, 1)
            isNextVideoPrefetchInProgress = false
        }
    }

    /**
     * If m-config videos are prefetched, prefetch m+1 video if user views video m
     */
    private fun prefetchNextVideoOnOverFlow(position: Int, loopCount: Int) {
        Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow position " + position)
        var noOfVideosToPrefetch = ExoRequestHelper.remainingToPrefetch(configType)
        if(noOfVideosToPrefetch > 0) {
            Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow return")
            return
        }

        if(!CommonUtils.isEmpty(relatedCardList)) {
            for (index in (position + 1) until relatedCardList.size) {
                if(index >= relatedCardList.size) {
                    Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow $index >= ${relatedCardList.size} ")
                    break
                }
                val asset = relatedCardList[index]
                if(ExoRequestHelper.isItemAdded(asset?.i_id())) {
                    Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow Next eligible video is already added for prefetch at pos : $index, id : " + asset?.i_id())
                    break
                }
                if(Logger.loggerEnabled()) {
                    Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow id : " + asset?.i_id() + ", configType : " + asset?.i_videoAsset()?.configType +
                            ", DHVideoUtils.isEligibleToPrefetch(asset) : " + DHVideoUtils.isEligibleToPrefetchInDetail(asset) +
                            ", !ExoRequestHelper.isPresentInRequestQueue(asset.i_id()) : " + !ExoRequestHelper.isPresentInRequestQueue(asset.i_id()))
                }

                if(asset?.i_videoAsset()?.configType == null && DHVideoUtils.isEligibleToPrefetchInDetail(asset) &&
                    !ExoRequestHelper.isPresentInRequestQueue(asset.i_id())) {
                    Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow Added to prefetch List : 0, contentId : " + asset?.i_id() + " title : " + asset?.i_title())
                    ExoRequestHelper.prefetchVideo(0, asset, configType)
                    break
                }
                if(loopCount == 1) {
                    break
                }
            }
        }
    }

    override fun onCardVisibility(position: Int, asset: CommonAsset?) {

    }

    /**
     * Add first m videos to prefetch list
     */
    fun pushForVideoPrefetch(startPosition: Int) {
        if (CacheConfigHelper.disableCache || isRelatedVideosPrefetchInProgress || startPosition < 0) {
            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch() disableCache == true" +
                    " in_progress : $isRelatedVideosPrefetchInProgress and startPosition : $startPosition")
            return
        }

        var noOfVideosToPrefetch = ExoRequestHelper.remainingToPrefetch(configType)

        Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch() startPosition = $startPosition" +
                " noOfVideosToPrefetch = $noOfVideosToPrefetch" +
                " cardList.size = ${relatedCardList?.size}")

        if (noOfVideosToPrefetch <= 0) {
            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch() return prefetch is full")
            return
        }

        isRelatedVideosPrefetchInProgress = true
        var itemAddedCount = 0
        GlobalScope.launch(Dispatchers.IO) {
            val delayTime = PlayerUtils.getTimeBasedOnNetwork()
            delay(delayTime)
            if(!CommonUtils.isEmpty(relatedCardList)) {
                for (index in 0 until relatedCardList.size) {
                    if(index >= relatedCardList.size) {
                        break
                    }
                    val asset = relatedCardList[index]
                    if(asset is CommonAsset && DHVideoUtils.isEligibleToPrefetchInDetail(asset) &&
                        !ExoRequestHelper.isPresentInRequestQueue(asset.i_id())) {
                        Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch Added to prefetch List index: $index, contentId : " + asset?.i_id())
                        Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch itemAddedCount : $itemAddedCount " +
                                " && noOfVideosToPrefetch : $noOfVideosToPrefetch")
                        if (noOfVideosToPrefetch > 0) {
                            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch prefetchVideo >> " + asset?.i_title())
                            noOfVideosToPrefetch--
                            itemAddedCount++
                            ExoRequestHelper.prefetchVideo(index, asset, configType)
                        } else {
                            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch break at Size : " + "$itemAddedCount")
                            break
                        }
                    }
                }
            }
        }
        isRelatedVideosPrefetchInProgress = false
    }

    fun stopVideoPrefetch() {
        ExoRequestHelper?.reset()
        ExoDownloadHelper.removeListener(this)
    }

    fun startVideoPrefetch() {
        ExoRequestHelper?.start()
        ExoDownloadHelper.addListener(this)
    }

    fun clearCachedItems() {
        ExoRequestHelper.clearCachedItems(ConfigType.VIDEO_DETAIL_V)
    }

    override fun updateVideoUrlFromDownload(
        mediaItem: BaseMediaItem?, cacheStatus: ExoDownloadHelper.CacheStatus) {
        Logger.d(LOG_TAG_CACHE, " updateVideoUrlFromDownload : id = " + mediaItem?.contentId)

        if(position < 0 || items?.size == 0) {
            return
        }
        if(mediaItem?.contentId == items[position]) {
            val fragment: DHVideoDetailFragment? = getFragmentAtPosition(position)
            fragment?.markVideoAsStreamCached(
                mediaItem, ExoRequestHelper.getStreamCachedStatus(cacheStatus), true
            )
        }
    }

    override fun updateVideoUrlFromExo(
        mediaItem: BaseMediaItem?, cacheStatus: ExoDownloadHelper.CacheStatus) {
        Logger.d(LOG_TAG_CACHE, " updateVideoUrlFromExo : id = " + mediaItem?.contentId)

        if(position < 0 || items?.size == 0) {
            return
        }
        if(mediaItem?.contentId == items[position]) {
            val fragment: DHVideoDetailFragment? = getFragmentAtPosition(position)
            fragment?.markVideoAsStreamCached(
                mediaItem, ExoRequestHelper.getStreamCachedStatus(cacheStatus), false
            )
        }
    }

    override fun updateVideoCachedPercentage(mediaItem: BaseMediaItem?, percentage: Float, downloadedVideoDuration: Float) {
        if(position < 0 || items?.size == 0) {
            Logger.d(LOG_TAG_CACHE, "return emptylist parentPosition : " + parentPosition)
            return
        }

        Logger.d(LOG_TAG_CACHE, "updateVideoCachedPercentage position = " + position + ", " + mediaItem?.contentId + " == " + items[position]  + ", parentPosition : " + parentPosition + ", downloadedVideoDuration = $downloadedVideoDuration");
        if(mediaItem?.contentId == items[position]) {
            val fragment:DHVideoDetailFragment? = getFragmentAtPosition(position)
            Logger.d(LOG_TAG_CACHE, " updateVideoCachedPercentage : id = " + mediaItem?.contentId + ", percentage = $percentage, downloadedVideoDuration = $downloadedVideoDuration");
            fragment?.updateDownloadPercentage(mediaItem?.contentId, percentage, downloadedVideoDuration)
            ExoRequestHelper.updateVideoCachedPercentage(mediaItem, percentage, downloadedVideoDuration)
        }
    }

    /**
     * Only for log enabled builds
     */
    override fun updateDownloadException(mediaItem: BaseMediaItem?, errorMsg: String?) {
        Logger.d(LOG_TAG_CACHE, " updateDownloadException : id = " + mediaItem?.contentId + ", errorMsg = $errorMsg");
        if(position < 0 || items?.size == 0) {
            return
        }
        if(mediaItem?.contentId == items[position]) {
            val fragment:DHVideoDetailFragment? = getFragmentAtPosition(position)
            Logger.d(LOG_TAG_CACHE, " updateDownloadException : id = " + mediaItem?.contentId + ", errorMsg = $errorMsg");
            fragment?.updateDownloadException(mediaItem?.contentId, errorMsg)
        }
    }

}