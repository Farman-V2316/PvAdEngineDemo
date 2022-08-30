/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener
import com.newshunt.appview.common.ui.listeners.PageFollowClickListener
import com.newshunt.appview.common.ui.viewholder.AddPageErrorViewHolder
import com.newshunt.appview.common.ui.viewholder.AddPageLocationSimpleViewHolder
import com.newshunt.appview.common.ui.viewholder.AddPageTopicButton
import com.newshunt.appview.common.ui.viewholder.AddPageTopicHeader
import com.newshunt.appview.common.ui.viewholder.AddPageTopicSimpleViewHolder
import com.newshunt.appview.common.viewmodel.PageableTopicViewModel
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils.getString
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.dhutil.view.ErrorMessageBuilder

/**
 * @author priya.gupta
 */
class AddPageTopicListAdapter(private val viewOnItemClickListener: RecyclerViewOnItemClickListener,
                              private val pageableTopicViewModel: PageableTopicViewModel,
                              private val listener: ErrorMessageBuilder.ErrorMessageClickedListener?) : RecyclerView
.Adapter<RecyclerView.ViewHolder>(), LocationFollowClickListener, PageFollowClickListener {


    enum class CardType(val index: Int) {
        HEADERFOLLOW(0),
        HEADERTOP(1),
        LOCATION(2),
        TOPIC(3),
        BUTTONFOLLOW(4),
        BUTTONREC(5),
        HEADERREC(6),
        ERRORREC(7),
        ERRORTOP(8)

    }

    val size: Int = 3
    var fromfollowedList: Boolean = false
        set(value) {
            field = value
        }

    var items: List<PageableTopicsEntity>? = null
        set(value) {
            val old = field
            field = value
            updateVisibleList(value, old, recItems, recItems)


        }

    var recItems: List<Location>? = null
        set(value) {
            val old = field
            field = value
            updateVisibleList(items, items, value, old)


        }
    var errorRec: BaseError? = null
        set(value) {
            field = value
            updateVisibleList(items, items, recItems, recItems)
        }

    var isNewsSection: Boolean = false
    set(value) {
        field = value
    }

    var errorTop: BaseError? = null
        set(value) {
            field = value
            updateVisibleList(items, items, recItems, recItems)
        }

    val visibleList = ArrayList<String>()
    private val visibleListingOrder = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {

            CardType.HEADERTOP.index, CardType.HEADERREC.index ,CardType.HEADERFOLLOW.index -> {
                val view = layoutInflater.inflate(R.layout.add_page_topic_header, parent, false)
                return AddPageTopicHeader(view)
            }

            CardType.BUTTONREC.index ,CardType.BUTTONFOLLOW.index -> {
                val view = layoutInflater.inflate(R.layout.add_page_topic_button, parent, false)
                return AddPageTopicButton(parent.context, view, viewOnItemClickListener)
            }

            CardType.TOPIC.index -> {
                val view = layoutInflater.inflate(R.layout.add_page_topic_simple_item, parent, false)
                return AddPageTopicSimpleViewHolder(view, viewOnItemClickListener, pageableTopicViewModel,this)
            }

            CardType.ERRORTOP.index,CardType.ERRORREC.index -> {
                val view = layoutInflater.inflate(R.layout.add_topic_error_page, parent, false)
                return AddPageErrorViewHolder(view, listener)
            }

            else -> {
                val view = layoutInflater.inflate(R.layout.add_page_topic_simple_item, parent, false)
                return AddPageLocationSimpleViewHolder(view, viewOnItemClickListener,
                        pageableTopicViewModel, this)

            }


        }
    }

    override fun getItemViewType(position: Int): Int {

        when (visibleList.get(position)) {

            CardType.BUTTONREC.name -> return CardType.BUTTONREC.index
            CardType.BUTTONFOLLOW.name -> return CardType.BUTTONFOLLOW.index
            CardType.HEADERTOP.name -> return CardType.HEADERTOP.index
            CardType.HEADERFOLLOW.name -> return CardType.HEADERFOLLOW.index
            CardType.HEADERREC.name -> return CardType.HEADERREC.index
            CardType.ERRORREC.name -> return CardType.ERRORREC.index
            CardType.ERRORTOP.name -> return CardType.ERRORTOP.index
            CardType.LOCATION.name -> return CardType.LOCATION.index
            CardType.TOPIC.name -> return CardType.TOPIC.index
        }
        return -1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (visibleList.get(position)) {
            CardType.LOCATION.name -> {
                return (holder as AddPageLocationSimpleViewHolder).updateTopic(recItems!![position - 1])
            }
            CardType.TOPIC.name -> {
                if(isNewsSection) {
                    return (holder as AddPageTopicSimpleViewHolder).updateTopic(items!![position - 1 -
                            getItemBeforeTopicsCount()])
                }else{
                    return (holder as AddPageTopicSimpleViewHolder).updateTopic(items!![position])
                }
            }
            CardType.BUTTONFOLLOW.name -> {
                val title = getString(R.string.add_topic_button_followed)
                return (holder as AddPageTopicButton).updateButton(title)
            }
            CardType.BUTTONREC.name -> {
                val title = getString(R.string.see_more)
                return (holder as AddPageTopicButton).updateButton(title)
            }

            CardType.HEADERFOLLOW.name -> {
                val title = getString(R.string.header_location_followed)
                return (holder as AddPageTopicHeader).updateHeader(title)
            }
            CardType.HEADERREC.name -> {
                val title = getString(R.string.header_location)
                return (holder as AddPageTopicHeader).updateHeader(title)
            }

            CardType.HEADERTOP.name -> {
//                 Return a ViewHolder for Topic header
                val title = getString(R.string.topics)
                return (holder as AddPageTopicHeader).updateHeader(title)
            }
            CardType.ERRORTOP.name -> {
                return (holder as AddPageErrorViewHolder).updateError(errorTop)
            }
            else -> {
                return (holder as AddPageErrorViewHolder).updateError(errorRec)
            }
        }
    }

    fun getItemBeforeTopicsCount(): Int {

        if (recItems != null && !recItems!!.isEmpty()) {
            return getRecItemCount() + 2
        }else if(errorRec!= null && CommonUtils.isEmpty(recItems)){
            return 1
        } else {
            return 0
        }
    }

    fun getRecItemCount(): Int {
        if (recItems != null)
            return if (recItems!!.size > 3) 3 else recItems!!.size
        else
            return 0
    }


    fun updateVisibleList(items: List<PageableTopicsEntity>?, oldItems: List<PageableTopicsEntity>?,
                          recItems: List<Location>?, oldRecItems: List<Location>?) {
        visibleList.clear()
        if (!CommonUtils.isEmpty(recItems)) {
            val size = getRecItemCount()
            if (fromfollowedList) {
                visibleList.add(CardType.HEADERFOLLOW.name)
            } else {
                visibleList.add(CardType.HEADERREC.name)
            }
            for (i in 1..size)
                visibleList.add(CardType.LOCATION.name)

            if (fromfollowedList) {
                visibleList.add(CardType.BUTTONFOLLOW.name)
            } else {
                visibleList.add(CardType.BUTTONREC.name)
            }

        }
        if (CommonUtils.isEmpty(recItems) && errorRec != null)
            visibleList.add(CardType.ERRORREC.name)

        if (!CommonUtils.isEmpty(items)) {
            if(isNewsSection)
            visibleList.add(CardType.HEADERTOP.name)
            items!!.forEach {
                visibleList.add(CardType.TOPIC.name)
            }
        }

        if (CommonUtils.isEmpty(items) && errorTop != null)
            visibleList.add(CardType.ERRORTOP.name)

        DiffUtil.calculateDiff(TopicsAdapterDiffUtilCallback(visibleListingOrder, visibleList,
                items, oldItems, recItems, oldRecItems))
                .dispatchUpdatesTo(this)

        visibleListingOrder.clear()
        visibleListingOrder.addAll(visibleList)
    }

    override fun getItemCount(): Int {

        return visibleListingOrder.size
    }

    override fun followed(isFollowed: Boolean, location: Location) {
        if (recItems != null) {
            val index = recItems!!.indexOf(location)
            if (index != -1)
                recItems!!.get(index).isFollowed = isFollowed

        }

    }

    override fun followedPage(isFollowed: Boolean, pageableTopicsEntity: PageableTopicsEntity) {
        if (items != null) {
            val index = items!!.indexOf(pageableTopicsEntity)
            if (index != -1)
                items!!.get(index).isFollowed = isFollowed

        }
    }
}

class TopicsAdapterDiffUtilCallback(private val oldItems: List<Any>?,
                                    private val newItems: List<Any>?,
                                    private val pageItems: List<PageableTopicsEntity>?,
                                    private val oldPageItems: List<PageableTopicsEntity>?,
                                    private val recItems: List<Location>?,
                                    private val oldRecItems: List<Location>?) : DiffUtil.Callback
() {


    private fun isLocation(widget: Any): Boolean {
        return AddPageTopicListAdapter.CardType.LOCATION.name.equals(widget)
    }

    private fun isPage(widget: Any): Boolean {
        return AddPageTopicListAdapter.CardType.TOPIC.name.equals(widget)
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        if (oldItems == null && newItems == null) return true

        oldItems ?: return false
        newItems ?: return false

        if (oldItems[oldItemPosition].javaClass != newItems[newItemPosition].javaClass) {
            return false
        }
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]

        if (isLocation(oldItem) && isLocation(newItem)) {

            val firstOldIndex = oldItems.indexOf(oldItem)
            val firstNewIndex = newItems.indexOf(newItem)
            val oldIndex = oldItemPosition - firstOldIndex
            val newIndex = newItemPosition - firstNewIndex
            if (oldIndex < 0 || newIndex < 0 || oldIndex >= (oldRecItems?.size ?: 0)
                    || newIndex >= (recItems?.size ?: 0)) {
                return false
            }

            val oldLocItem = oldRecItems?.get(oldIndex)
            val newLocItem = recItems?.get(newIndex)
            return ((oldLocItem?.id ?: "").equals(newLocItem?.id ?: "")) && ((oldLocItem?.isFollowed
                    ?: "").equals(newLocItem?.isFollowed ?: ""))

        }

        if (isPage(oldItem) && isPage(newItem)) {
            val firstOldIndex = oldItems.indexOf(oldItem)
            val firstNewIndex = newItems.indexOf(newItem)
            val oldIndex = oldItemPosition - firstOldIndex
            val newIndex = newItemPosition - firstNewIndex

            val oldPageItem = oldPageItems?.get(oldIndex)
            val newPageItem = pageItems?.get(newIndex)

            return ((oldPageItem?.pageEntity?.id ?: "").equals(newPageItem?.pageEntity?.id ?: ""))&& ((oldPageItem?.isFollowed
                ?: "").equals(newPageItem?.isFollowed ?: "")) && ((oldPageItem?.isFavorite
                ?: "").equals(newPageItem?.isFavorite ?: ""))
        }

        return oldItem.equals(newItem)
    }

    override fun getNewListSize(): Int {
        return newItems?.size ?: 0
    }

    override fun getOldListSize(): Int {
        return oldItems?.size ?: 0
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems?.get(oldItemPosition)
        val newItem = newItems?.get(newItemPosition)
        return oldItem == newItem
    }
}

