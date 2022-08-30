/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.UpdateableCollectionChildItemView
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.appview.databinding.PostEntityItemTagBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.ColdStartEntityItem
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.asset.PostDisplayType
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.CardLandingType
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.SimpleItemDecorator
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.news.util.EventKey
import com.newshunt.news.util.NewsConstants

class PostColdStartViewHolder(private val viewBinding: ViewDataBinding,
                              vm: CardsViewModel,
                              private val cardType: Int,
                              private val section: String,
                              private val referrer: PageReferrer? = null,
                              val eventDedupHelper: EventDedupHelper) : CardsViewHolder(viewBinding.root) {
    private val listView: RecyclerView = viewBinding.root.findViewById(R.id.item_list)
    private val decorator = SimpleItemDecorator(horizontalSpacing = getHorizontalSpacing(cardType),
            verticalSpacing = getVerticalSpacing(cardType))
    private val adapter = ColdStartAdapter(vm, cardType, section, referrer, eventDedupHelper)

    init {
        listView.adapter = adapter
        listView.layoutManager = getLayoutManager(cardType, viewBinding.root.context)
        listView.addItemDecoration(decorator)
        if (cardType == PostDisplayType.QMC_CAROUSEL4.index) {
            val snapHelper = androidx.recyclerview.widget.LinearSnapHelper()
            snapHelper.attachToRecyclerView(listView)
        }
    }

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        Logger.d(LOG_TAG, "bind $adapterPosition")
        if (item !is CommonAsset) return
        setupAdapter(item, lifecycleOwner)
        setupRecyclerPadding(item)
        viewBinding.setVariable(BR.item, item)
        if (lifecycleOwner != null) {
            viewBinding.lifecycleOwner = lifecycleOwner
        }
        viewBinding.executePendingBindings()
        triggerCardViewEvent(item)
    }

    private fun setupRecyclerPadding(item: CommonAsset) {
        val isHorizontalScrollable = when (cardType) {
            PostDisplayType.QMC_GRID.index,
            PostDisplayType.QMC_GRID_2.index,
            PostDisplayType.QMC_TAGS.index -> {
                false
            }
            PostDisplayType.QMC_CAROUSEL4.index -> {
                item.i_entityCollection()?.let {
                    it.size > 1
                } ?: false
            }
            else -> {
                true
            }
        }

        if(cardType != PostDisplayType.QMC_CAROUSEL1.index && cardType != PostDisplayType.QMC_CAROUSEL2.index && cardType != PostDisplayType.QMC_CAROUSEL3.index ) {
            if (isHorizontalScrollable) {
                listView.setPadding(
                    CommonUtils.getDimension(R.dimen.story_card_padding_left),
                    0,
                    0,
                    0
                )
            } else {
                listView.setPadding(
                    CommonUtils.getDimension(R.dimen.story_card_padding_left),
                    0,
                    CommonUtils.getDimension(R.dimen.story_card_padding_left),
                    0
                )
            }
        }
    }

    /* spacing for list in horizontal direction.
     (item1)|(item2)|(item3) */
    private fun getHorizontalSpacing(cardType: Int): Int {
        if (cardType == PostDisplayType.QMC_TAGS.index) {
            return CommonUtils.getDimension(R.dimen.entity_item_vertical_spacing)
        }
        return 0
    }

    /* spacing for list in vertical direction.
    (item1)
    -------
    (item2)
    -------
    (item3) */
    private fun getVerticalSpacing(cardType: Int): Int {
        return CommonUtils.getDimension(R.dimen.entity_item_vertical_spacing)
    }

    private fun getLayoutManager(cardType: Int, context: Context,
                                 item: CommonAsset? = null):
            RecyclerView.LayoutManager {
        return when (cardType) {
            PostDisplayType.QMC_GRID.index,
            PostDisplayType.QMC_GRID_2.index ->
                GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            PostDisplayType.QMC_TAGS.index -> {
                FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP)
            }
            PostDisplayType.QMC_CAROUSEL4.index -> {
                val hasMoreThanOneItem: Boolean = item?.i_entityCollection()?.let {
                    it.size > 1
                } ?: false
                if (hasMoreThanOneItem) {
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                } else {
                    GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)
                }
            }
            else -> {
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    private fun isNestedScrollingEnabled(cardType: Int): Boolean {
        return cardType == PostDisplayType.QMC_GRID.index ||
                cardType == PostDisplayType.QMC_GRID_2.index ||
                cardType == PostDisplayType.QMC_TAGS.index
    }

    private fun setupAdapter(item: CommonAsset, lifecycleOwner: LifecycleOwner?) {
        val itemList = item.i_entityCollection() ?: emptyList()
        val oldSize = adapter.itemCount
        val newSize = itemList.size
        adapter.updateData(addFollowMoreForCarousel5(itemList, item), item, lifecycleOwner, adapterPosition)
        if (cardType == PostDisplayType.QMC_CAROUSEL4.index && oldSize != newSize) {
            listView.layoutManager = getLayoutManager(cardType, itemView.context, item)
        }
        listView.isNestedScrollingEnabled = !isNestedScrollingEnabled(cardType)
    }

    private fun addFollowMoreForCarousel5(list: List<EntityItem>, parentItem: CommonAsset?): List<EntityItem> {
        if (parentItem?.i_uiType() == UiType2.CAROUSEL_5 &&
                list.find { it.i_entityId() == ColdStartEntityItem.ENTITY_ID_FOLLOW_MORE } == null) {
            val result = list.toMutableList()
            result.add(0, ColdStartEntityItem(entityId = ColdStartEntityItem
                    .ENTITY_ID_FOLLOW_MORE,
                    displayName = CommonUtils.getString(R.string.follow_more_text)))
            return result
        } else {
            return list
        }
    }

    private fun triggerCardViewEvent(item: CommonAsset) {
        val triggerParam: Map<String, String?> = mapOf(Constants.STORY_ID to item.i_id(),
                NewsConstants.DH_SECTION to section)
        val eventKey = EventKey(NhAnalyticsAppEvent.ENTITY_LIST_VIEW, triggerParam)
        eventDedupHelper.fireEvent(eventKey, Runnable {
            AnalyticsHelper2.logEntityListViewEventForColdStart(item, "inlist", section,
                    referrer,adapterPosition)
        })
    }
}


class ColdStartAdapter(private val vm: ClickHandlingViewModel,
                       private val parentCardType: Int,
                       private val section: String,
                       private val referrer: PageReferrer?,
                       private val eventDedupHelper: EventDedupHelper) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val items: MutableList<ParentChild<CommonAsset, EntityItem>> = mutableListOf()
    private var parentItem: CommonAsset? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var parentCardPosition: Int = 0

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(data: List<EntityItem>, parent: CommonAsset, lifecycleOwner: LifecycleOwner?, parentCardPosition: Int) {
        this.parentItem = parent
        this.lifecycleOwner = lifecycleOwner
        this.parentCardPosition = parentCardPosition
        updateData(data.map { ParentChild(parent, it) })
    }

    private fun updateData(data: List<ParentChild<CommonAsset, EntityItem>>) {
        val oldList = mutableListOf<ParentChild<CommonAsset, EntityItem>>()
        oldList.addAll(items)
        items.clear()
        items.addAll(data)
        val diffCallback = EntityItemDiffUtil(oldList, data)
        val result = DiffUtil.calculateDiff(diffCallback)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = getEntityItemViewBinding(viewType, parent, parentItem)
        binding.setVariable(BR.vm, vm)
        binding.setVariable(BR.cardTypeIndex, parentCardType)
        binding.setVariable(BR.parentItem, parentItem)
        binding.lifecycleOwner = lifecycleOwner
        return EntityItemViewHolder(binding, section, referrer, eventDedupHelper)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? UpdateableCollectionChildItemView)?.bind(
                getItem(position).child,
                getItem(position).parent,
                lifecycleOwner, position, parentCardPosition)
    }

    override fun getItemViewType(position: Int): Int {
        return findEntityCardType(parentCardType, getItem(position).child)
    }

    private fun getItem(position: Int): ParentChild<CommonAsset, EntityItem> {
        return items[position]
    }
}

fun findEntityCardType(parentCardType: Int, item: EntityItem): Int {
    if (item.isCreateGroupCard() == true) {
        return PostDisplayType.QMC_CREATE_GROUP.index
    }

    if (item.i_entityId() == ColdStartEntityItem.ENTITY_ID_FOLLOW_MORE) {
        return PostDisplayType.QMC_FOLLOW_MORE.index
    }

    return parentCardType
}

fun getEntityItemViewBinding(viewType: Int, parent: ViewGroup, parentItem: CommonAsset?):
        ViewDataBinding {
    val inflator = LayoutInflater.from(parent.context)

    return when (viewType) {

        PostDisplayType.QMC_FOLLOW_MORE.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(inflator, R.layout.post_entity_item_view_more, parent,
                    false)
        }

        PostDisplayType.QMC_CAROUSEL1.index,
        PostDisplayType.QMC_CAROUSEL5.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.PostEntityItemBinding>(inflator, R.layout.post_entity_item, parent,
                    false)
        }

        PostDisplayType.QMC_GRID.index -> {
            val binding = DataBindingUtil.inflate<com.newshunt.appview.databinding.PostEntityItemBinding>(inflator, R.layout.post_entity_item, parent,
                    false)
            (binding.root.layoutParams as? RecyclerView.LayoutParams)?.let {
                it.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            binding
        }
        PostDisplayType.QMC_CAROUSEL3.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.PostEntityItemBannerBinding>(inflator, R.layout.post_entity_item_banner, parent,
                    false)
        }

        PostDisplayType.QMC_GRID_2.index -> {
            val binding = DataBindingUtil.inflate<com.newshunt.appview.databinding.PostEntityItemBannerBinding>(inflator, R.layout.post_entity_item_banner, parent,
                    false)
            (binding.root.layoutParams as? RecyclerView.LayoutParams)?.let {
                it.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            binding
        }

        PostDisplayType.QMC_CAROUSEL4.index -> {
            val hasMoreThanOneItem: Boolean = parentItem?.i_entityCollection()?.let {
                it.size > 1
            } ?: false
            val binding = DataBindingUtil.inflate<ViewDataBinding>(inflator, R.layout
                    .layout_discovery_card_big_banner, parent,
                    false)
            (binding.root.layoutParams as? RecyclerView.LayoutParams)?.let {
                if (hasMoreThanOneItem) {
                    it.width = CommonUtils.getDeviceScreenWidth() - (3 * CommonUtils.getDimension
                    (R.dimen.story_card_padding_left))
                    it.marginEnd = (CommonUtils.getDimension(R.dimen.story_card_padding_left)) / 2
                } else {
                    it.width = ViewGroup.LayoutParams.MATCH_PARENT
                    it.marginEnd = 0
                }
            }
            binding
        }
        PostDisplayType.QMC_CAROUSEL2.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(inflator, R.layout.post_entity_item_banner_medium, parent,
                    false)
        }
        PostDisplayType.QMC_CREATE_GROUP.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding
            .LayoutCreateGroupCardBinding>(inflator, R.layout.layout_create_group_card, parent, false)
        }
        PostDisplayType.QMC_IMPORT_CONTACTS.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding
            .LayoutCreateGroupCardBinding>(inflator, R.layout.layout_import_contacts_card, parent, false)
        }

        PostDisplayType.QMC_TAGS.index -> {
            DataBindingUtil.inflate<PostEntityItemTagBinding>(inflator, R.layout.post_entity_item_tag,
                    parent, false)
        }
        else -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.PostEntityItemBinding>(inflator, R.layout.post_entity_item, parent,
                    false)
        }
    }

}

class EntityItemViewHolder(private val viewBinding: ViewDataBinding,
                           private val section: String,
                           private val referrer: PageReferrer?,
                           val eventDedupHelper: EventDedupHelper) : RecyclerView.ViewHolder
(viewBinding.root), UpdateableCollectionChildItemView {
    override fun bind(item: Any?,
                      parent: Any?,
                      lifecycleOwner: LifecycleOwner?,
                      position: Int,
                      parentCardPosition: Int) {
        Logger.d(LOG_TAG_ITEM, "bind $adapterPosition")
        if (item !is EntityItem) return
        viewBinding.setVariable(BR.parentItem, parent)
        viewBinding.setVariable(BR.item, item)
        viewBinding.setVariable(BR.position, position)
        viewBinding.executePendingBindings()
        triggerCardViewEvent(item, parent as? CommonAsset, parentCardPosition)
    }

    private fun triggerCardViewEvent(item: EntityItem, parent: CommonAsset?, parentCardPosition: Int) {
        val triggerParam: Map<String, String?> = mapOf(Constants.STORY_ID to item.i_entityId())
        val eventKey = EventKey(NhAnalyticsAppEvent.ENTITY_CARD_VIEW, triggerParam)
        eventDedupHelper.fireEvent(eventKey, Runnable {
            /*
            * For cold start item only supported landing type is DEEPLINK
            */
            AnalyticsHelper2.logEntityCardViewEvent(item, section, referrer, parent,
                    adapterPosition, parentCardPosition,CardLandingType.DEEPLINK)
        })
    }
}

class EntityItemDiffUtil(private val oldList: List<ParentChild<CommonAsset, EntityItem>>,
                          private val newList: List<ParentChild<CommonAsset, EntityItem>>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].child.i_entityId() == newList[newItemPosition].child.i_entityId()
                && oldList[oldItemPosition].parent.i_id() == newList[newItemPosition].parent.i_id()
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].child.i_selected() == newList[newItemPosition].child.i_selected() &&
                oldList[oldItemPosition].child.i_entityImageUrl() == newList[newItemPosition].child.i_entityImageUrl() &&
                oldList[oldItemPosition].child.i_displayName() == newList[newItemPosition].child.i_displayName() &&
                oldList[oldItemPosition].parent.i_selectText() == newList[newItemPosition].parent.i_selectText() &&
                oldList[oldItemPosition].parent.i_unSelectText() == newList[newItemPosition].parent.i_unSelectText()

    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return Bundle()
    }
}

data class ParentChild<P, C>(val parent: P,
                             val child: C)

private const val LOG_TAG_ITEM = "PostColdStartViewHolderItem"
private const val LOG_TAG = "PostColdStartViewHolder"