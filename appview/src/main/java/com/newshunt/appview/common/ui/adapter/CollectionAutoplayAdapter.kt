/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.viewholder.AbstractAutoplayViewHolder
import com.newshunt.appview.common.ui.viewholder.AutoplayPagerAdapter
import com.newshunt.appview.common.ui.viewholder.ExoAutoplayViewHolder
import com.newshunt.appview.common.ui.viewholder.WebAutoplayViewHolder
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.AutoplayVhBinding
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CollectionProperties
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder

private const val LOG_TAG = "CollectionAutoplayViewHolder"

/**
 * Adapter of Autoplay Carousel
 *
 * Created  on 22/10/19.
 */
class CollectionAutoplayAdapter(private val pageRef: PageReferrer, val context: Context?,
                                private val commonVideoRequester: VideoRequester?,
                                private val cardsViewModel: CardsViewModel,
                                private val displayCardTypeIndex: Int,
                                private val parentLifecycleOwner: LifecycleOwner?,
                                private val section: String,
                                val referrerProviderlistener: ReferrerProviderlistener? = null,
                                val eventDedupHelper: EventDedupHelper,
                                private val uniqueScreenId: Int) : AutoplayPagerAdapter<AbstractAutoplayViewHolder>() {

    private var mCurrentPosition = -1
    private val mViewHolderList = LinkedHashMap<Int, AbstractAutoplayViewHolder>()
    private var currentHolder: AbstractAutoplayViewHolder? = null

    private val supportExtraPage = false
    private var parentItem: CommonAsset? = null
    private var collectionProperties: CollectionProperties? = null
    private val items: MutableList<Any> = mutableListOf()
    private var lifecycleOwner: LifecycleOwner? = null
    private var viewVisibilityPercentage: Int = 0
    private var percentageOfScreen: Float = 0f
    private var autoPlayManager: AutoPlayManager? = null

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): AbstractAutoplayViewHolder {
        Logger.d(LOG_TAG, "onCreateViewHolder")
        val viewDataBinding: ViewDataBinding = DataBindingUtil
                .inflate<AutoplayVhBinding>(LayoutInflater.from(parent.context),
                        R.layout.autoplay_vh, parent, false)
        return if (DHVideoUtils.isExoPlayer((items[pos] as CommonAsset?)?.i_videoAsset())) {
            ExoAutoplayViewHolder(viewDataBinding, pageRef, context!!, commonVideoRequester,
                    true, cardsViewModel, parentLifecycleOwner, section,
                    displayCardTypeIndex = displayCardTypeIndex, parentItem = parentItem, uniqueScreenId = uniqueScreenId)
        } else {
            WebAutoplayViewHolder(viewDataBinding, pageRef, context!!, commonVideoRequester,
                    true, cardsViewModel, parentLifecycleOwner, section,
                    displayCardTypeIndex = displayCardTypeIndex, parentItem = parentItem, uniqueScreenId = uniqueScreenId)

        }.apply {
            setAutoPlayManager(autoPlayManager)
        }
    }

    fun setAutoPlayManager(autoPlayManager: AutoPlayManager?) {
        this.autoPlayManager = autoPlayManager
    }

    private fun updateViewHolderPosition(vh: AbstractAutoplayViewHolder, position: Int) {
        if(mViewHolderList.containsKey(position)) {
            mViewHolderList.remove(position)
        }
        mViewHolderList.put(position, vh)
    }

    override fun onBindViewHolder(viewHolder: AbstractAutoplayViewHolder, position: Int) {
        updateViewHolderPosition(viewHolder, position)
        val item = items[position]
        val bindItem = if (item is CommonAsset) {
            ParentIdHolderCommenAsset(parentItem?.i_id(), item)
        } else {
            item
        }
        viewHolder.updateParentItem(parentItem)
        viewHolder.bind(bindItem, lifecycleOwner, position)
        viewHolder.pos = position
        Logger.d(LOG_TAG, "onBindViewHolder - $position")
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        if(`object` is AbstractAutoplayViewHolder) {
            if(mViewHolderList.containsKey(`object`.cardPosition)) {
                mViewHolderList.remove(`object`.cardPosition)
            }
        }
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun setPrimaryItem(container: ViewGroup, viewPosition: Int, obj: Any) {
        super.setPrimaryItem(container, viewPosition, obj)
        if (mCurrentPosition == viewPosition || (viewPosition == count - 2 && supportExtraPage)) {
            return
        }
        Logger.d(LOG_TAG, "setPrimaryItem viewPosition:$viewPosition && mCurrentPosition:$mCurrentPosition")
        updateVisibilty(viewPosition)
    }

    fun updateChild(parentItem: CommonAsset?, lifecycleOwner: LifecycleOwner?) {
        this.parentItem = parentItem
        this.items.clear()
        parentItem?.i_collectionItems()?.let {
            items.addAll(parentItem.i_collectionItems()!!)
        }
        updateChild(mCurrentPosition, lifecycleOwner)
        updateChild((mCurrentPosition - 1), lifecycleOwner)
        updateChild((mCurrentPosition + 1), lifecycleOwner)
        notifyDataSetChanged()
    }

    private fun updateChild(position: Int, lifecycleOwner: LifecycleOwner?) {
        if(position < 0 || position >= items.size) {
            return
        }
        val item = items[position]
        val bindItem = if (item is CommonAsset) {
            ParentIdHolderCommenAsset(parentItem?.i_id(), item)
        } else {
            item
        }
        getViewHolderAt(position)?.bind(bindItem, lifecycleOwner, position)
    }

    fun updateVisibilty(newPosition: Int) {
        if (mCurrentPosition != newPosition) {
            Logger.d(LOG_TAG, "updateVisibilty, newPosition:$newPosition, " + "mCurrentPosition:$mCurrentPosition")
            currentHolder = getViewHolderAt(mCurrentPosition)
            val newHolder = getViewHolderAt(newPosition)

            if (currentHolder is VisibilityAwareViewHolder) {
                (currentHolder as VisibilityAwareViewHolder)?.onUserLeftFragment()
            }

            //Before onUserEnteredFragment, we need update currentHolder and Posiiton bocs its
            // reference used in callback of AutoPlayManager
            currentHolder = newHolder
            mCurrentPosition = newPosition
            if (newHolder is VisibilityAwareViewHolder) {
                newHolder.onUserEnteredFragment(viewVisibilityPercentage, percentageOfScreen)
            }
        }
    }

    fun getVisibilityPercentage(): Int {
        return viewVisibilityPercentage
    }

    fun onInvisible() {
        Logger.d(LOG_TAG, "onInvisible")
        (currentHolder as? VisibilityAwareViewHolder)?.onInVisible()
    }

    fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        Logger.d(LOG_TAG, "onUserEnteredFragment, curPos : $mCurrentPosition")
        this.viewVisibilityPercentage = viewVisibilityPercentage
        this.percentageOfScreen = percentageOfScreen
        (currentHolder as? VisibilityAwareViewHolder)?.onUserEnteredFragment(viewVisibilityPercentage, percentageOfScreen)
    }

    fun onUserLeftFragment() {
        Logger.d(LOG_TAG, "onUserLeftFragment, curPos : $mCurrentPosition")
        (currentHolder as? VisibilityAwareViewHolder)?.onUserLeftFragment()
    }

    fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        Logger.i(LOG_TAG, "onVisible called $viewVisibilityPercentage")
        this.percentageOfScreen = percentageOfScreen
        this.viewVisibilityPercentage = viewVisibilityPercentage
        if (currentHolder == null) {
            currentHolder = getViewHolderAt(mCurrentPosition)
        }
        (currentHolder as? VisibilityAwareViewHolder)?.onVisible(viewVisibilityPercentage, percentageOfScreen)
    }

    fun getCurrentHolder() : AutoPlayable? {
        return currentHolder as? AutoPlayable
    }

    private fun getViewHolderAt(viewPosition: Int): AbstractAutoplayViewHolder? {
        return mViewHolderList.get(viewPosition)
    }

    fun update(item: CommonAsset?, lifecycleOwner: LifecycleOwner?) {
        this.mCurrentPosition = -1
        this.currentHolder = null
        this.parentItem = item
        this.collectionProperties = item?.i_carouselProperties()
        this.lifecycleOwner = lifecycleOwner
        this.items.clear()
        this.mViewHolderList.clear()
        item?.i_collectionItems()?.let {
            this.items.addAll(item.i_collectionItems()!!)
        }
        notifyDataSetChanged()
    }

    fun getViewForItemId(itemId: String): View? {
        if (items.isEmpty()) return null
        val itemIndex = items.indexOfFirst {
            (it as? CommonAsset)?.i_id() == itemId
        }
        if (itemIndex >= 0) {
            getViewHolderAt(itemIndex)?.let {
                return it.getSharedElementView()
            }
        }
        return null
    }
}
