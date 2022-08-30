/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.google.android.material.tabs.TabLayout
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.CollectionAutoplayAdapter
import com.newshunt.appview.common.ui.adapter.CollectionViewHolder
import com.newshunt.appview.common.video.ui.helper.PlayerState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.LiveTabLayoutBinding
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder

/**
 * Collection of Autoplay Videos
 *
 * Created  on 22/10/19.
*/
class CollectionAutoplayViewHolder(private val viewBinding: ViewDataBinding,
                                   val cardsViewModel: CardsViewModel,
                                   cardType: Int,
                                   val videoRequester: VideoRequester?,
                                   val context: Context?,
                                   val pageRef: PageReferrer,
                                   val displayCardTypeIndex: Int,
                                   val section: String,
                                   val referrerProviderlistener: ReferrerProviderlistener? = null,
                                   val eventDedupHelper: EventDedupHelper,
                                   val uniqueScreenId: Int) : CardsViewHolder(viewBinding.root), VisibilityAwareViewHolder, AutoPlayable, ViewPager.OnPageChangeListener, CollectionViewHolder {
    init {
        Logger.d(TAG, "Create collection card $cardType")
    }

    private val viewPager = viewBinding.root.findViewById<ViewPager>(R.id.viewpager)
    private val liveTabLayout = viewBinding.root.findViewById<TabLayout>(R.id.live_tabs)
    private var currentItem: CommonAsset? = null

    private var viewPagerSelectedPage = 0
    private var adapter: CollectionAutoplayAdapter? = null
    private var autoPlayManager: AutoPlayManager? = null

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        adapter?.onVisible(viewVisibilityPercentage, percentageOfScreen)
    }

    override fun onInVisible() {
        adapter?.onInvisible()
    }

    override fun onUserLeftFragment() {
        adapter?.onUserLeftFragment()
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        adapter?.onUserEnteredFragment(viewVisibilityPercentage, percentageOfScreen)
    }

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        Logger.d(TAG, "bind $adapterPosition")
        if (item !is CommonAsset) return
        //Validation  for required fields in collection display
        if (!validateItem(item)) {
            Logger.e(TAG, "Not valid collection item")
            return
        }

        if (lifecycleOwner != null) {
            viewBinding.lifecycleOwner = lifecycleOwner
        }

        val isNewItem = item.i_id() != currentItem?.i_id()
        if (isNewItem || adapter == null ||
                adapter?.getCount() != currentItem?.i_collectionItems()?.size) {
            Logger.d(TAG, "new binding ${item.i_id()} old = ${currentItem?.i_id()}")
            currentItem = item
            viewBinding.setVariable(BR.item, item)
            viewBinding.executePendingBindings()
            setupViewPager(viewPager, item, isNewItem, lifecycleOwner)
            setupTabLayout(item.i_collectionItems()!!)
        } else {
            adapter?.updateChild(item, lifecycleOwner)
            setupTabLayout(item.i_collectionItems()!!)
        }

        VideoHelper.videoStateLiveData.observe(lifecycleOwner!!, Observer {
            handlePlayerState(it)
        })
    }

    override fun getViewForAnimationByItemId(storyId: String): View? {
        return adapter?.getViewForItemId(storyId)
    }

    private fun handlePlayerState(it: PlayerState) {
        Logger.d(TAG, "handlePlayerState :: PlayerState ${it.state}")
        if (viewPager == null) {
            return
        }
        when (it.state) {
            PLAYER_STATE.STATE_VIDEO_END -> moveToNextVideo(viewPager!!.currentItem)
        }
    }

    private fun moveToNextVideo(position: Int) {
        val nextPos = position + 1
        Logger.d(TAG, "moveToNextVideo :: nextPos $nextPos")
        if (nextPos < adapter?.count?:0) {
            viewPager?.setCurrentItem(nextPos, true)
        }
    }

    private fun setupViewPager(viewPager: ViewPager,
                                    item: CommonAsset?,
                                    isNewBinding: Boolean,
                                    lifecycleOwner: LifecycleOwner?) {
        if(adapter == null) {
            adapter = CollectionAutoplayAdapter(pageRef, context, videoRequester, cardsViewModel,
                    displayCardTypeIndex, lifecycleOwner, section, referrerProviderlistener, eventDedupHelper, uniqueScreenId)
            viewPager.offscreenPageLimit = 2
            adapter?.setAutoPlayManager(autoPlayManager)
        }
        adapter?.update(item, lifecycleOwner)

        if(isNewBinding) {
            viewPager.adapter = adapter
            viewPager.addOnPageChangeListener(this)
        }
    }

    private fun setupTabLayout(list: List<CommonAsset>) {
        liveTabLayout.setupWithViewPager(viewPager)

        for (i in 0 until list.size) {
            val binding = DataBindingUtil
                    .inflate<LiveTabLayoutBinding>(LayoutInflater.from(context),
                            R.layout.live_tab_layout, liveTabLayout, false)
            binding.item = list[i]
            val tab = liveTabLayout.getTabAt(i)//get tab via position
            if (tab != null)
                tab!!.customView = binding.root//set custom view
        }
    }

    private fun validateItem(item: CommonAsset): Boolean {
        item.i_collectionItems() ?: run {
            Logger.e(TAG, "Collection item list is null")
            return false
        }
        if (item.i_collectionItems()!!.isEmpty()) {
            Logger.e(TAG, "Collection item list is empty")
            return false
        }
        return true
    }

    private fun resetViewPager() {
        adapter?.update(null, null)
        viewPager.adapter = adapter
    }

    override fun recycleView() {
        super.recycleView()
        resetViewPager()
    }


    //VIEW PAGER CALLBACK
    override fun onPageScrollStateChanged(state: Int) {
        Logger.d(TAG, "onPageScrollStateChanged state: $state")
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            autoPlayManager?.updateFocusedPlayer()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//        if (viewPager.parent != null) {
//            viewPager.parent.requestDisallowInterceptTouchEvent(true)
//        }
    }

    override fun onPageSelected(position: Int) {
        Logger.d(TAG, "onPageSelected Pos: $position")
        viewPagerSelectedPage = position
        adapter?.updateVisibilty(position)
    }

    companion object {
        const val TAG = "CollectionAutoplayViewHolder"
    }

    override val asset: Any?
        get() =  adapter?.getCurrentHolder()?.asset

    //get() =  currentItem?.i_collectionItems()!![viewPagerSelectedPage]

    override fun setAutoPlayManager(autoPlayManager: AutoPlayManager?) {
        this.autoPlayManager = autoPlayManager
    }

    override fun getAutoplayPriority(fresh: Boolean): Int {
        adapter?.getCurrentHolder()?.let {
            return it.getAutoplayPriority(fresh)
        }
        if(adapter != null) {
            return adapter!!.getVisibilityPercentage()
        }
        return -1
    }

    override fun play() {
        adapter?.getCurrentHolder()?.play()
    }

    override fun pause() {
        adapter?.getCurrentHolder()?.pause()
    }

    override fun canRelease(): Boolean {
        adapter?.getCurrentHolder()?.let {
            return it.canRelease()
        }
        return true
    }

    override fun releaseVideo() {
        adapter?.getCurrentHolder()?.releaseVideo()
    }

    override fun getVisibilityPercentage(): Int {
        adapter?.getCurrentHolder()?.let {
            return it.getVisibilityPercentage()
        }
        return -1
    }

    override fun getPositionInList(): Int {
        adapter?.getCurrentHolder()?.let {
            return it.getPositionInList()
        }
        return -1
    }

    override fun resetVideoState() {
        adapter?.getCurrentHolder()?.resetVideoState()
    }
}
