/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.newshunt.appview.R
import com.newshunt.appview.common.video.ui.adapter.VerticalViewPagerAdapter
import com.newshunt.appview.common.video.ui.helper.PlayerState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.video.ui.view.DHVideoDetailFragment
import com.newshunt.appview.databinding.FragmentBaseVerticalVideosBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.NLFCItem
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.helper.theme.ThemeType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Created on Vinod.BC 30/09/2020.
 * BaseFragment to handle vertical videos with ViewPager2
 */
abstract class BaseVerticalVideoFragment : BaseFragment() {
    private val TAG = "BaseVerticalVideoFragment"
    private var isScrollStateIdle: Boolean = true
    private var bundle: Bundle? = null
    private var postId: String? = null
    private var position: Int = 0
    private var isLandingStory: Boolean = false
    private var prevVerticalPos = -1
    private var isEndActionComplete = false
    private var isLocalZone = false
    private var isVisibleToUser = false
    private var isRelatedRequestDone = false
    private var isScrollSettled = true

    companion object {
        private const val PREFETCH_ITEM_THRESHOLD = 3
    }

    private lateinit var verticalVideoBinding: FragmentBaseVerticalVideosBinding
    protected lateinit var viewPager2: ViewPager2
    private var playerProvider: VideoPlayerProvider? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        readBundle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        verticalVideoBinding = getVideoBinding(inflater, container)
        createVerticalPager()
        return verticalVideoBinding.root
    }

    protected fun updateCardsList(cards: List<CommonAsset>?) {
        Logger.d(TAG, "updateCardsList size : ${cards?.size}")
        (viewPager2.adapter as? VerticalViewPagerAdapter)?.let {
            if (it.itemCount == 0) {
                createVerticalPager()
            }
        }
        (viewPager2.adapter as? VerticalViewPagerAdapter)?.update(cards)
        if(isFragmentVisible()) {
            Logger.d(TAG, "updateCardsList pushForVideoPrefetch : $position")
            (viewPager2.adapter as? VerticalViewPagerAdapter)?.startVideoPrefetch()
            (viewPager2.adapter as? VerticalViewPagerAdapter)?.pushForVideoPrefetch(position)
        } else {
            (viewPager2.adapter as? VerticalViewPagerAdapter)?.stopVideoPrefetch()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        VideoHelper.videoStateLiveData.observe(viewLifecycleOwner, Observer {
            handlePlayerState(it)
        })
    }

    private fun createVerticalPager() {
        viewPager2.adapter = VerticalViewPagerAdapter(this, bundle!!,
                postId, position, playerProvider)
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    sendVideoStartAction()
                    isScrollSettled = true
                }
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if(isScrollSettled) {
                    isScrollSettled = false
                    GlobalScope.launch {
                        viewPager2?.adapter.let {
                            requestNextPage(
                                position, viewPager2.currentItem, viewPager2.adapter!!.itemCount
                            )
                        }
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                setVideoEndAction(position)
                prevVerticalPos = position
                (viewPager2.adapter as VerticalViewPagerAdapter).position = position
                super.onPageSelected(position)
            }
        })
    }

    private fun getVideoBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBaseVerticalVideosBinding {
        val contextThemeWrapper = ContextThemeWrapper(activity, ThemeType.NIGHT.themeId)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        verticalVideoBinding = DataBindingUtil.inflate(localInflater,
                R.layout.fragment_base_vertical_videos, container, false)
        viewPager2 = verticalVideoBinding.viewPager2
        return verticalVideoBinding
    }

    private fun readBundle() {
        bundle = arguments
        bundle?.let {
            postId = bundle!!.getString(Constants.STORY_ID)
            position = bundle!!.getInt(Constants.STORY_POSITION, -1)
            isLandingStory = bundle!!.getBoolean(Constants.IS_LANDING_STORY, false)
            isLocalZone = bundle!!.getBoolean(Constants.BUNDLE_IS_LOCAL_ZONE, false)
        }
    }

    open fun setPlayerProvider(provider: VideoPlayerProvider?) {
        playerProvider = provider
    }

    open fun getPlayerProvider(): VideoPlayerProvider? {
        return playerProvider
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart pos: $position, visible : $userVisibleHint")
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "onResume pos: $position & hashCode :: ${hashCode()}")
        if (isLocalZone) {
            isVisibleToUser = true
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.d(TAG, "onPause pos: $position")
        if (isLocalZone) {
            isVisibleToUser = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy pos: $position & hashCode :: ${hashCode()}")
        if(this::viewPager2.isInitialized) {
            (viewPager2.adapter as? VerticalViewPagerAdapter)?.destroy()
        }
    }

    override fun setUserVisibleHint(isVisible: Boolean) {
        super.setUserVisibleHint(isVisible)
        isVisibleToUser = isVisible
        if (view == null || !isAdded || activity == null) {
            Logger.d(TAG, "setUserVisibleHint return isAdded : $isAdded")
            return
        }
        Logger.d(TAG, "setUserVisibleHint isVisibleToUser : $isVisible, position : $position")
        if (this::viewPager2.isInitialized && viewPager2.adapter is VerticalViewPagerAdapter) {
            val fragment = (viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(viewPager2.currentItem)
            Logger.d(TAG, "setUserVisibleHint fragment : $fragment")
            Logger.d(TAG, "setUserVisibleHint calling to Child Fragment at ${viewPager2.currentItem}")
            Logger.d(TAG, "setUserVisibleHint  Adapter Size : ${viewPager2.adapter?.itemCount}")
            if (fragment == null) {
                createVerticalPager()
            } else if (fragment != null) {
                try {
                    fragment.setMenuVisibility(isVisible)
                    fragment.setUserVisibleHint(isVisible)
                    if (isVisible) {
                        fragment.setVideoStartAction(PlayerVideoStartAction.SWIPE)
                    } else {
                        fragment.setVideoEndAction(PlayerVideoEndAction.SWIPE)
                    }

                    if(!isVisible) {
                        (viewPager2.adapter as VerticalViewPagerAdapter)?.clearCachedItems()
                    }
                } catch (e: Exception) {
                    Logger.d(TAG, "setUserVisibleHint Exception : ${e.message}")
                }
            }
        }
    }

    override fun handleBackPress(): Boolean {
        Logger.d(TAG, "handleBackPress position : $position")
        if (this::viewPager2.isInitialized && viewPager2.adapter is VerticalViewPagerAdapter) {
            val fragment = (viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(viewPager2.currentItem)
            Logger.d(TAG, "handleBackPress fragment : $fragment")
            if (fragment != null) {
                Logger.d(TAG, "handleBackPress calling to Child Fragment")
                return fragment.handleBackPress()
            }
        }
        return false
    }

    fun getFragmentAtPostion(position: Int): DHVideoDetailFragment? {
        if (!isAdded || viewPager2.adapter == null) {
            return null
        }
        return (viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(position)
    }

    fun getPostIdAtPosition(position: Int): String? {
        if (!isAdded || viewPager2.adapter == null) {
            return null
        }
        return (viewPager2.adapter as VerticalViewPagerAdapter).getPostId(position)
    }

    private fun handlePlayerState(it: PlayerState) {
        Logger.d(TAG, "handlePlayerState :: PlayerState ${it.state}")
        if (it.id.isNullOrEmpty() || it.state == PLAYER_STATE.STATE_IDLE){
            Logger.d(TAG, "handlePlayerState :: return id is null")
            return
        }
        val currentFragment = getFragmentAtPostion(viewPager2.currentItem)
        if (currentFragment !is DHVideoDetailFragment || it.id != currentFragment.postId) {
            if (it.state == PLAYER_STATE.STATE_VIDEO_END) {
                Logger.d(TAG, "handlePlayerState :: VIDEO_END :: corner case")
                moveToNextVideo(viewPager2.currentItem)
                VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_IDLE, null)
            }
            Logger.d(TAG, "handlePlayerState :: return Frag = $currentFragment")
            return
        }
        when (it.state) {
            PLAYER_STATE.STATE_VIDEO_START -> onVideoStart(viewPager2.currentItem)
            PLAYER_STATE.STATE_VIDEO_END -> moveToNextVideo(viewPager2.currentItem)
            PLAYER_STATE.STATE_ERROR -> moveToNextVideo(viewPager2.currentItem)
            PLAYER_STATE.STATE_AD_START -> onAdStart()
            PLAYER_STATE.STATE_AD_END -> onAdEnd()
            PLAYER_STATE.STATE_BOTTOM_BAR_VISIBLE -> disableVerticalSwipe()
            PLAYER_STATE.STATE_BOTTOM_BAR_HIDDEN -> enableVerticalSwipe()
            PLAYER_STATE.STATE_FULLSCREEN_ON -> disableVerticalSwipe()
            PLAYER_STATE.STATE_FULLSCREEN_OFF -> enableVerticalSwipe()
        }
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_IDLE, null)
    }

    private fun onVideoStart(position: Int) {
        if (!isAdded || position != viewPager2.currentItem || viewPager2.adapter == null) {
            return
        }
        val curFragment = (viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(position)
        //Checking is parent postId with child
        if (!isLocalZone && curFragment is DHVideoDetailFragment && postId == curFragment.postId
                && !curFragment.getRelatedUrl().isNullOrEmpty()) {
            if ( viewPager2.adapter!!.itemCount <= 5 || !isRelatedRequestDone) {
                isRelatedRequestDone = true
                requestRelatedVideo(curFragment.getRelatedUrl())
            }
        }
        requestAdsForAdjacentVideos(position)
    }

    private fun requestAdsForAdjacentVideos(position: Int) {
        if (viewPager2.adapter == null) {
            return
        }
        val nextFragment = (viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(position + 1)
        val prevFragment = (viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(position - 1)
        if (nextFragment is DHVideoDetailFragment) {
            nextFragment.requestInstreamAd()
        }
        if (prevFragment is DHVideoDetailFragment) {
            prevFragment.requestInstreamAd()
        }
    }

    private fun moveToNextVideo(position: Int) {
        if (!isAdded || position != viewPager2.currentItem || viewPager2.adapter == null) {
            return
        }
        val nextPos = position + 1
        Logger.d(TAG, "moveToNextVideo :: nextPos $nextPos")
        if (nextPos < viewPager2.adapter?.itemCount ?: 0) {
            viewPager2.setCurrentItem(nextPos, true)
        }
    }

    private fun onAdStart() {
        if (!isScrollStateIdle || viewPager2.adapter == null) {
            return
        }

        viewPager2.isUserInputEnabled = false
        ((viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(viewPager2.currentItem))?.handleUI(true)
    }

    private fun onAdEnd() {
        if (viewPager2.adapter == null) {
            return
        }
        viewPager2.isUserInputEnabled = true
        ((viewPager2.adapter as VerticalViewPagerAdapter).getFragmentAtPosition(viewPager2.currentItem))?.handleUI(false)
    }

    fun onPageScrollStateChanged(state: Int) {
        isScrollStateIdle = (state == ViewPager.SCROLL_STATE_IDLE)
        if (isScrollStateIdle) {
            Logger.d(TAG, "onPageScrollStateChanged - SCROLL_STATE_IDLE")
            if ((viewPager2.adapter as VerticalViewPagerAdapter)?.getFragmentAtPosition(viewPager2.currentItem) != null) {
                val currentFragment = (viewPager2.adapter as VerticalViewPagerAdapter)?.getFragmentAtPosition(viewPager2.currentItem) as? DHVideoDetailFragment
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

    open fun isFragmentVisible(): Boolean {
        Logger.d(TAG, "isFragmentVisible isVisibleToUser : $isVisibleToUser, position : $position")
        return isVisibleToUser
    }

    private fun setVideoEndAction(curPos: Int) {
        if ((viewPager2.adapter as VerticalViewPagerAdapter)?.getFragmentAtPosition(prevVerticalPos) != null) {
            val currentFragment = (viewPager2.adapter as VerticalViewPagerAdapter)?.getFragmentAtPosition(prevVerticalPos) as? DHVideoDetailFragment
            if (prevVerticalPos != -1) {
                currentFragment?.setVideoEndAction(PlayerVideoEndAction.VERTICAL_FLIP)
                isEndActionComplete = true
            }
        }
    }

    private fun sendVideoStartAction() {
        if (isEndActionComplete) {
            if ((viewPager2.adapter as VerticalViewPagerAdapter)?.getFragmentAtPosition(viewPager2.currentItem) != null) {
                val currentFragment = (viewPager2.adapter as VerticalViewPagerAdapter)?.getFragmentAtPosition(viewPager2.currentItem) as? DHVideoDetailFragment
                currentFragment?.setVideoStartAction(PlayerVideoStartAction.VERTICAL_FLIP)
                isEndActionComplete = false
            }
        }
    }

    //Function to enable vertical swipes
    fun enableVerticalSwipe() {
        viewPager2.isUserInputEnabled = true
    }

    //Function to disable vertical swipes
    fun disableVerticalSwipe() {
        viewPager2.isUserInputEnabled = false
    }

    abstract fun requestNextPage(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int)
    abstract fun requestRelatedVideo(relatedUrl: String?)
}
