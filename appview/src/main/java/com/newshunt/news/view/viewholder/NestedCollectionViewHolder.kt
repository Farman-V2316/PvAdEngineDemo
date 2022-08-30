/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.viewholder

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.CardsAdapterDiffUtilCallback2
import com.newshunt.appview.common.ui.adapter.CollectionViewHolder
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.viewholder.CachedPagerAdapter
import com.newshunt.appview.common.ui.viewholder.CardsViewHolder
import com.newshunt.appview.common.ui.viewholder.CollectionAutoplayViewHolder
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.appview.databinding.CollectionOfCollectionItemBinding
import com.newshunt.appview.databinding.CollectionOfCollectionNestedItemBinding
import com.newshunt.appview.databinding.SimplePostVhCollectionOfCollectionBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.NHWrappedHeightViewPager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.common.asset.PostDisplayType
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.helper.common.SCVEvent
import com.newshunt.dataentity.common.pages.EntityType
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.EventDedupHelper

/**
 * Nested collection classes.
 *
 * @author aman.roy on 1st Aug 2022
 */
private const val LOG_TAG = "NestedCollectionVH"
class NestedCollectionViewHolder(val viewDataBinding: ViewDataBinding,
                                 val vm: ClickHandlingViewModel,
                                 private val section: String,
                                 val eventDedupHelper: EventDedupHelper,
                                 val pageReferrer: PageReferrer?,
                                 viewPool: RecyclerView.RecycledViewPool) : CardsViewHolder(viewDataBinding.root),ViewPager.OnPageChangeListener, CollectionViewHolder, ViewPager.PageTransformer {
    private val collectionViewBinding = viewDataBinding as SimplePostVhCollectionOfCollectionBinding
    private var currentItem:CommonAsset? = null
    private val pagerAdapter:NestedCollectionPagerAdapter = NestedCollectionPagerAdapter(vm,0,viewDataBinding.root.context,-1, viewPool)
    private var currentPosition = -1
    private val visiblePerc = CardsBindUtils.getSecondItemVisiblePercentage()

    init {
        Logger.d(LOG_TAG, "New instance of NestedCollectionVH created")
        initViewPager()
    }

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if (item !is CommonAsset) return
        if(!validateItems(item)) return
        Logger.d(LOG_TAG, "Updating the CoC for itemPosition: $cardPosition")
        lifecycleOwner?.let{ it ->
            collectionViewBinding.lifecycleOwner = it
        }
        val isNewItem = item.i_id() != currentItem?.i_id()
        val isListSizeChanges = item.i_collectionItems()?.size != currentItem?.i_collectionItems()?.size
        pagerAdapter.update(item,item.i_collectionItems(),lifecycleOwner)
        if (isNewItem ||
            pagerAdapter.count != currentItem?.i_collectionItems()?.size) {
            Logger.d(CollectionAutoplayViewHolder.TAG, "new binding ${item.i_id()} old = ${currentItem?.i_id()}")
            currentItem = item
            collectionViewBinding.setVariable(BR.item, item)
            setupViewPager(collectionViewBinding.viewpager, isNewItem,isListSizeChanges)
            Logger.d(LOG_TAG, "Fresh CoC, setupViewPager done, itemPosition: $cardPosition")
        }
        collectionViewBinding.executePendingBindings()
    }

    private fun initViewPager() {
        collectionViewBinding.viewpager.adapter = pagerAdapter
        collectionViewBinding.viewpager.clipToPadding = false
        collectionViewBinding.viewpager.pageMargin = CommonUtils.getPixelFromDP(20,collectionViewBinding.root.context)
        collectionViewBinding.viewpager.setPageTransformer(false, this)
        collectionViewBinding.viewpager.addOnPageChangeListener(this)
    }

    private fun setupViewPager(viewPager: NHWrappedHeightViewPager,
                               isNewBinding: Boolean,
                               isListSizeChanged: Boolean) {
        currentPosition = -1

        if (!isNewBinding && !isListSizeChanged) {
            return
        }
        if (isNewBinding) {
            this.onPageSelected(0)
            viewPager.resetView()
        }
    }

    private fun validateItems(item:CommonAsset): Boolean {
        return !item.i_collectionItems().isNullOrEmpty()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        val postEntity = pagerAdapter.getItem(position) as? PostEntity
        BusProvider.getUIBusInstance().post(SCVEvent(postEntity))
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun transformPage(page: View, position: Float) {
        if(collectionViewBinding.viewpager.currentItem == currentPosition) return
        currentPosition = collectionViewBinding.viewpager.currentItem
        val leftPadding = CommonUtils.getPixelFromDP(20,collectionViewBinding.root.context)
        val topPadding = CommonUtils.getPixelFromDP(16,collectionViewBinding.root.context)
        val bottomPadding = CommonUtils.getPixelFromDP(30,collectionViewBinding.root.context)

        when {
            pagerAdapter.count == 1 -> {
                collectionViewBinding.viewpager.setPadding(leftPadding, topPadding, leftPadding, bottomPadding)
            }
            collectionViewBinding.viewpager.currentItem == 0 -> {
                collectionViewBinding.viewpager.setPadding(leftPadding, topPadding, ((CommonUtils.getDeviceScreenWidth())*(visiblePerc/100f)).toInt(), bottomPadding)
            }
            collectionViewBinding.viewpager.currentItem == pagerAdapter.count - 1 -> {
                collectionViewBinding.viewpager.setPadding(((CommonUtils.getDeviceScreenWidth())*(visiblePerc/100f)).toInt(), topPadding, leftPadding, bottomPadding)
            }
            else -> {
                collectionViewBinding.viewpager.setPadding(leftPadding, topPadding, ((CommonUtils.getDeviceScreenWidth())*(visiblePerc/100f)).toInt(), bottomPadding)
            }
        }
    }

    override fun getViewForAnimationByItemId(storyId: String): View? {
        return pagerAdapter.getViewForItemId(storyId)
    }
}

private class NestedCollectionPagerAdapter(private val vm: ClickHandlingViewModel,
                                           private val parentCardType: Int,
                                           private val context: Context,
                                           private val uniqueScreenId: Int,
                                           private val viewPool: RecyclerView.RecycledViewPool) : CachedPagerAdapter(true) {

    private var lifecycleOwner: LifecycleOwner? = null
    private var adDelegates = HashMap<String, ContentAdDelegate>()
    private var parentItem: CommonAsset? = null
    private val items: MutableList<Any> = mutableListOf()
    override fun getViewBinding(convert: ViewDataBinding?, position: Int, parent: ViewGroup): ViewDataBinding {
        return if (convert != null) {
            Logger.d(LOG_TAG, "reusing page for $position")
            bindView(convert, position)
            convert
        } else {
            val binding = createView(position, parent)
            binding
        }
    }

    override fun canCache(position: Int) = true

    override fun getCount(): Int {
        return items.size
    }

    private fun createView(position: Int, parent: ViewGroup): ViewDataBinding {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<CollectionOfCollectionNestedItemBinding>(inflater,
                R.layout.collection_of_collection_nested_item, parent, false)
        lifecycleOwner?.let {
            binding.lifecycleOwner = it
        }
        binding.recycler.adapter = NestedCollectionRecyclerAdapter(vm,parentCardType,context,uniqueScreenId)
        binding.recycler.layoutManager = LinearLayoutManagerWithoutScroll(context)
        binding.recycler.setRecycledViewPool(viewPool)
        bindView(binding, position)
        return binding
    }


    private fun bindView(viewBinding: ViewDataBinding, position: Int) {
        val collectionBinding = viewBinding as? CollectionOfCollectionNestedItemBinding
        val item = items[position]
        collectionBinding?.item = item as? CommonAsset
        collectionBinding?.vm = vm
        collectionBinding?.isLastPosition = position == items.lastIndex
        var adapter: NestedCollectionRecyclerAdapter? = null
        collectionBinding?.recycler?.let {
            adapter = it.adapter as? NestedCollectionRecyclerAdapter
        }
        (item as? CommonAsset)?.let {
            it.i_collectionItems()?.let {
                if (it.size > 3) {
                adapter?.update(it.subList(0,3),parentItem,item,position)
                } else {
                adapter?.update(it.subList(0,it.size),parentItem,item,position)
             }
            } ?: return
            if(item.i_source()?.entityType != EntityType.SOURCE.name) {
            val bgColorString = if (ThemeUtils.isNightMode()) it.i_carouselProperties()?.backgroundColorNight else it.i_carouselProperties()?.backgroundColorDay
            bgColorString?.let { color ->
                    collectionBinding?.header?.setBackgroundColor(Color.parseColor(color))
                }
            }
        }
        collectionBinding?.executePendingBindings()
    }

    fun update(parent: CommonAsset, list: List<Any>?, lifecycleOwner: LifecycleOwner?) {
        this.lifecycleOwner = lifecycleOwner
        this.parentItem = parent
        val doSoftUpdate = this.parentItem?.i_id() == parent.i_id() && items.size == list?.size
        items.clear()
        list?.let {
            items.addAll(it)
        }
        if (doSoftUpdate) {
            Logger.d(LOG_TAG, "soft update")
            softUpdate()
            return
        }
        Logger.d(LOG_TAG, "notifyDataSetChanged on pager adapter")
        notifyDataSetChanged()
    }

    fun getItem(position: Int):Any {
        return items[position]
    }

    private fun softUpdate() {
        items.forEachIndexed { index, _ ->
            getActivePageViews(index)?.let {
                bindView(it, index)
            }
        }
    }

    fun getViewForItemId(itemId: String): View? {
        if (items.isEmpty()) return null

        var targetCollectionIndex = -1
        items.forEachIndexed { index, it ->
            (it as? CommonAsset)?.let { commonAsset ->
                //First find which vertical list the item belongs to!
                if (!commonAsset.i_collectionItems().isNullOrEmpty()) {
                    commonAsset.i_collectionItems()?.map {
                        it.i_id()
                    }?.let {
                        if (it.contains(itemId)) {
                            targetCollectionIndex = index
                        }
                    }
                }
            }
        }
        if (targetCollectionIndex < 0) return null

        //Get the vertical list item from viewpager based on index found
        getActivePageViews(targetCollectionIndex)?.let {
            (it.root.findViewById(R.id.recycler) as? RecyclerView)?.let { targetRecycler ->
                (targetRecycler.adapter as? NestedCollectionRecyclerAdapter)?.let { recyclerAdapter ->
                    //Find the index of the itemId in the recycler view
                    val itemIndex = recyclerAdapter.getPositionForItem(itemId)
                    if (itemIndex != -1) {
                        targetRecycler.findViewHolderForAdapterPosition(itemIndex)?.let {
                            (it as? NestedCollectionItemViewHolder)?.let { vh ->
                                //Return the image view of the item actually clicked
                                return vh.itemView.findViewById(R.id.thumbnail)
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}

private class NestedCollectionItemViewHolder(
    context:Context,
    private val viewBinding: ViewDataBinding,
    private val vm: ClickHandlingViewModel,
    private val uniqueScreenId: Int): RecyclerView.ViewHolder(viewBinding.root) {

    private var adDelegates = HashMap<String, ContentAdDelegate>()

    fun bindView(position: Int,parentCardType: Int,item: Any?,rootItem: CommonAsset?,parentItem:CommonAsset?,parentPosition: Int) {
        viewBinding.setVariable(BR.vm, vm)
        viewBinding.setVariable(BR.cardTypeIndex, parentCardType)
        var contentAdDelegate: ContentAdDelegate? = null
        if (item is CommonAsset) {
            viewBinding.setVariable(BR.item, ParentIdHolderCommenAsset(parentItem?.i_id(), item))
            contentAdDelegate = adDelegates[item.i_id()] ?: parentItem?.i_adId()?.let { adId ->
                ContentAdDelegate(uniqueScreenId)
                    .also { adDelegates[item.i_id()] = it }
            }
            contentAdDelegate?.bindAd(parentItem?.i_adId(), item.i_id())
        } else {
            contentAdDelegate?.reset()
            viewBinding.setVariable(BR.item, item)
        }
        viewBinding.setVariable(BR.adDelegate, contentAdDelegate)
        viewBinding.setVariable(BR.parentItem, parentItem)
        viewBinding.root.tag = position
        val rootView = viewBinding.root
        rootView.setOnClickListener {
            contentAdDelegate?.onCardClick()
            if (item != null) {
                (vm as? ClickHandlingViewModel)?.onCollectionItemClick(it, item as? CommonAsset,rootItem,parentPosition,parentItem?.i_id())
            }
        }
        viewBinding.executePendingBindings()
    }
}

private class NestedCollectionRecyclerAdapter(private val vm: ClickHandlingViewModel,
                                              private val parentCardType: Int,
                                              private val context: Context,
                                              private val uniqueScreenId: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<Any> = mutableListOf()
    private var parentItem:CommonAsset? = null
    private var rootItem:CommonAsset? = null
    private var parentPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflator = LayoutInflater.from(context)
        val viewDataBinding = DataBindingUtil.inflate<CollectionOfCollectionItemBinding>(inflator, R.layout.collection_of_collection_item, parent, false)
        return NestedCollectionItemViewHolder(context, viewDataBinding, vm, uniqueScreenId)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? NestedCollectionItemViewHolder)?.bindView(position,parentCardType,items[position],rootItem,parentItem,parentPosition)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return PostDisplayType.COLLECTION_OF_COLLECTION_CARD_ITEM.index
    }

    fun update(list: List<Any>,rootItem:CommonAsset?,parentItem: CommonAsset?,parentPosition:Int) {
        this.rootItem = rootItem
        this.parentItem = parentItem
        this.parentPosition = parentPosition
        val result = DiffUtil.calculateDiff(CardsAdapterDiffUtilCallback2(items, list, NestedCollectionDiffCallback()))
        items.clear()
        items.addAll(list)
        result.dispatchUpdatesTo(this)
    }

    fun getPositionForItem(itemId: String): Int {
        if (items.isEmpty()) return -1
        val itemIndex = items.indexOfFirst {
            (it as? CommonAsset)?.i_id() == itemId
        }
        return itemIndex
    }
}

private class LinearLayoutManagerWithoutScroll(context: Context): LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
    override fun canScrollVertically() = false
}

class NestedCollectionDiffCallback: DiffUtil.ItemCallback<Any?>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is CommonAsset && newItem is CommonAsset) {
            return newItem.i_id() == oldItem.i_id()
        }
        return false
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val oldCommonAsset = oldItem as? CommonAsset
        val newCommonAsset = newItem as? CommonAsset
        val oldUrl = oldCommonAsset?.i_contentImageInfo()?.url ?: oldCommonAsset?.i_thumbnailUrls()?.getOrNull(0)
        val newUrl = newCommonAsset?.i_contentImageInfo()?.url ?: newCommonAsset?.i_thumbnailUrls()?.getOrNull(0)
        return oldCommonAsset?.i_title() == newCommonAsset?.i_title() &&
                oldUrl == newUrl &&
                oldCommonAsset?.i_isRead() == newCommonAsset?.i_isRead()
    }
}
