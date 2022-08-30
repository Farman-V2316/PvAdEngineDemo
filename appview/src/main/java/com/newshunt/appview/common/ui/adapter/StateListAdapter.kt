/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.adapter

/**
 * @author priya.gupta
 *
 *
 * This class is used as an adapter for location list.
 */
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.listeners.AddLocationListener
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener
import com.newshunt.appview.common.ui.listeners.StateLocationClickListener
import com.newshunt.appview.common.ui.viewholder.AddPageTopicHeader
import com.newshunt.appview.common.ui.viewholder.StateInfoViewHolder
import com.newshunt.appview.common.ui.viewholder.SuggestedLocationsViewHolder
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.dataentity.common.helper.common.CommonUtils

class StateListAdapter(
        private var allLocations: List<Locations>,
        private var allRecommendedLocations: List<Location>,
        private var allSelectedLocations: List<Location>,
        private val addLocationListener: AddLocationListener,
        private val viewOnItemClickListener: RecyclerViewOnItemClickListener,
        private val showFollowButton: Boolean,
        private val pageReferrer: PageReferrer,
        private val eventSection: NhAnalyticsEventSection) : RecyclerView.Adapter<RecyclerView
.ViewHolder>(), StateLocationClickListener,LocationFollowClickListener {

    enum class CardType(val index: Int) {
        ALLLOCATIONS(0),
        SUGGESTEDLOCATIONS(1),
        HEADERLOC(2),
        HEADERSUG(3),
        HEADERSELECTED(4),
        SELECTEDLOCATIONS(5),
        NOSELECTEDLOC(6)
    }

    val visibleList = ArrayList<String>()
    private val visibleListingOrder = ArrayList<String>()

    private val expandedLocations = ArrayList<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        when (viewType) {

            CardType.HEADERSUG.index,CardType.HEADERLOC.index, CardType.HEADERSELECTED.index  -> {
                val view = inflater.inflate(R.layout.add_page_topic_header, parent, false)
                return AddPageTopicHeader(view)
            }
            CardType.ALLLOCATIONS.index -> {
                val view = inflater.inflate(R.layout.location_list_parent_view, parent,
                        false)
                return StateInfoViewHolder(view, this, viewOnItemClickListener, pageReferrer,
                        addLocationListener, showFollowButton, eventSection, expandedLocations)
            }
            CardType.NOSELECTEDLOC.index -> {
                val view = inflater.inflate(R.layout.no_selected_location, parent,
                        false)
                return NoSelectedLocVH(view)
            }
            CardType.SELECTEDLOCATIONS.index -> {
                val view = inflater.inflate(R.layout.suggested_location_list_parent_view, parent,
                        false)
                return SuggestedLocationsViewHolder(view, viewOnItemClickListener, pageReferrer,
                        addLocationListener, showFollowButton, eventSection,this,true)
            }
            else -> {
                val view = inflater.inflate(R.layout.suggested_location_list_parent_view, parent,
                        false)
                return SuggestedLocationsViewHolder(view, viewOnItemClickListener, pageReferrer,
                        addLocationListener, showFollowButton, eventSection,this,false)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (visibleListingOrder.get(position)) {
            CardType.ALLLOCATIONS.name -> {
                (holder as StateInfoViewHolder).updateStateItem(allLocations[position -
                        getItemBeforeLocationsCount()])
            }
            CardType.SUGGESTEDLOCATIONS.name -> {
                (holder as SuggestedLocationsViewHolder).updateStateItem(allRecommendedLocations)
            }
            CardType.SELECTEDLOCATIONS.name -> {
                (holder as SuggestedLocationsViewHolder).updateStateItem(allSelectedLocations)
            }

            CardType.HEADERLOC.name -> {
                val title = CommonUtils.getString(R.string.header_all_locations)
                return (holder as AddPageTopicHeader).updateHeader(title)
            }

            CardType.HEADERSUG.name -> {
                val title = CommonUtils.getString(R.string.recommended_header)
                return (holder as AddPageTopicHeader).updateHeader(title)

            }
            CardType.HEADERSELECTED.name -> {
                val title = CommonUtils.getString(R.string.selected)
                return (holder as AddPageTopicHeader).updateHeader(title)

            }
        }
    }

    override fun getItemCount(): Int {
        return visibleList.size
    }

    fun getItemBeforeLocationsCount(): Int {
        return 1 + getRecommendedLocationViewCount() + getSelectedLocationViewCount()
    }

    fun getRecommendedLocationViewCount(): Int {
        if (!CommonUtils.isEmpty(allRecommendedLocations)) {
            return 2
        }
        return 0
    }

    fun getSelectedLocationViewCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        when (visibleList.get(position)) {
            CardType.ALLLOCATIONS.name -> return CardType.ALLLOCATIONS.index
            CardType.SUGGESTEDLOCATIONS.name -> return CardType.SUGGESTEDLOCATIONS.index
            CardType.HEADERLOC.name -> return CardType.HEADERLOC.index
            CardType.HEADERSUG.name -> return CardType.HEADERSUG.index
            CardType.HEADERSELECTED.name -> return CardType.HEADERSELECTED.index
            CardType.SELECTEDLOCATIONS.name -> return CardType.SELECTEDLOCATIONS.index
            CardType.NOSELECTEDLOC.name -> return CardType.NOSELECTEDLOC.index
        }
        return -1
    }

    fun setItems(data: List<Locations>) {
        val oldallLocations = allLocations
        allLocations = data
        updateVisibleList(oldallLocations, allLocations, allRecommendedLocations, allRecommendedLocations, allSelectedLocations,allSelectedLocations)
    }

    fun setRecommendedItems(data: List<Location>) {
        val oldAllRecommendedLocations = allRecommendedLocations
        allRecommendedLocations = (data)
        updateVisibleList(allLocations, allLocations, oldAllRecommendedLocations, allRecommendedLocations,allSelectedLocations,allSelectedLocations)
    }

    fun setSelectedItems(data: List<Location>) {
        val oldAllSelectedLocations = allSelectedLocations
        allSelectedLocations = (data)
        updateVisibleList(allLocations, allLocations, allRecommendedLocations, allRecommendedLocations,oldAllSelectedLocations,allSelectedLocations)
    }

    fun updateVisibleList(oldallLocations: List<Locations>, allLocations: List<Locations>,
                          oldAllRecommendedLocations: List<Location>, allRecommendedLocations1:
                          List<Location>,oldAllSelectedLocations: List<Location>,
                          allSelectedLocations1: List<Location>) {

        visibleList.clear()

        if(!CommonUtils.isEmpty(this.allSelectedLocations)) {
            visibleList.add(CardType.HEADERSELECTED.name)
            visibleList.add(CardType.SELECTEDLOCATIONS.name)
        } else {
            visibleList.add(CardType.HEADERSELECTED.name)
            visibleList.add(CardType.NOSELECTEDLOC.name)
        }

        if (!CommonUtils.isEmpty(this.allRecommendedLocations)) {
            visibleList.add(CardType.HEADERSUG.name)
            visibleList.add(CardType.SUGGESTEDLOCATIONS.name)
        }


        if (!CommonUtils.isEmpty(this.allLocations)) {
            visibleList.add(CardType.HEADERLOC.name)
            this.allLocations!!.forEach {
                visibleList.add(CardType.ALLLOCATIONS.name)
            }
        }

        DiffUtil.calculateDiff(StateAdapterDiffUtilCallback(visibleListingOrder, visibleList,
                oldallLocations, allLocations, oldAllRecommendedLocations,
                allRecommendedLocations1,oldAllSelectedLocations, allSelectedLocations1))
                .dispatchUpdatesTo(this)

        visibleListingOrder.clear()
        visibleListingOrder.addAll(visibleList)

    }

    override fun expandLocationList(stateLocation: Location) {
        if (stateLocation == null) {
            return
        }
        if (expandedLocations.contains(stateLocation.id)) {
            expandedLocations.remove(stateLocation.id)
        } else {
            expandedLocations.add(stateLocation.id)
        }
    }

    override fun followed(isFollowed: Boolean, location: Location) {
        if (allRecommendedLocations != null) {
            val index = allRecommendedLocations!!.indexOf(location)
            if (index != -1)
                allRecommendedLocations!!.get(index).isFollowed = isFollowed

        }
    }


}


class StateAdapterDiffUtilCallback(private val oldItems: List<Any>?,
                                   private val newItems: List<Any>?,
                                   private val oldallLocations: List<Locations>,
                                   private val allLocations: List<Locations>,
                                   private val oldAllRecommendedLocations: List<Location>,
                                   private val allRecommendedLocations: List<Location>,
                                   private val oldAllSelectedLocations: List<Location>,
                                   private val allSelectedLocations: List<Location>) :
        DiffUtil.Callback
() {


    private fun isALLLocation(widget: Any): Boolean {
        return StateListAdapter.CardType.ALLLOCATIONS.name.equals(widget)
    }

    private fun isSuggestion(widget: Any): Boolean {
        return StateListAdapter.CardType.SUGGESTEDLOCATIONS.name.equals(widget)
    }

    private fun isSelectedLoc(widget: Any): Boolean {
        return StateListAdapter.CardType.SELECTEDLOCATIONS.name.equals(widget)
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

        if (isALLLocation(oldItem) && isALLLocation(newItem)) {

            val firstOldIndex = oldItems.indexOf(oldItem)
            val firstNewIndex = newItems.indexOf(newItem)
            val oldIndex = oldItemPosition - firstOldIndex
            val newIndex = newItemPosition - firstNewIndex
            if (oldIndex < 0 || newIndex < 0 || oldIndex >= (oldallLocations?.size ?: 0)
                    || newIndex >= (allLocations?.size ?: 0)) {
                return false
            }

            val oldLocation = oldallLocations?.get(oldIndex)
            val newLocation = allLocations?.get(newIndex)
            if(oldLocation.parent?.id != newLocation.parent?.id ||
                    oldLocation.parent?.name != newLocation.parent?.name) {
                return false
            }
            if (!(oldLocation.kids?.size ?: 0).equals((newLocation.kids?.size ?: 0))) {
                return false
            }
            val size = oldLocation.kids?.size ?: 0

            for (i in 0..size-1) {
                if (!(oldLocation.kids?.get(i)?.id ?: "").equals((newLocation.kids?.get(i)?.id
                                ?: ""))
                        || !(oldLocation.kids?.get(i)?.isFollowed
                                ?: false).equals((newLocation.kids?.get(i)?.isFollowed ?: false))) {
                    return false
                }
            }
            return true
        }

        if (isSuggestion(oldItem) && isSuggestion(newItem)) {
            if (oldAllRecommendedLocations.size != allRecommendedLocations.size) {
                return false
            }

            for (i in 0..oldAllRecommendedLocations.size - 1) {
                val oldItem = oldAllRecommendedLocations.get(i)
                val newItem = allRecommendedLocations.get(i)
                if(!((oldItem?.id ?: "").equals(newItem?.id ?: "")) ||!((oldItem?.isFollowed
                                ?: "").equals(newItem?.isFollowed ?: ""))){
                    return false
                }
            }

            return true
        }
        if (isSelectedLoc(oldItem) && isSelectedLoc(newItem)) {
            if (oldAllSelectedLocations.size != allSelectedLocations.size) {
                return false
            }

            for (i in 0..oldAllSelectedLocations.size - 1) {
                val oldItem = oldAllSelectedLocations.get(i)
                val newItem = allSelectedLocations.get(i)
                if(!((oldItem?.id ?: "").equals(newItem?.id ?: "")) ||!((oldItem?.isFollowed
                                ?: "").equals(newItem?.isFollowed ?: ""))){
                    return false
                }
            }

            return true
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

class NoSelectedLocVH(private val view: View) : RecyclerView.ViewHolder(view) {
}
