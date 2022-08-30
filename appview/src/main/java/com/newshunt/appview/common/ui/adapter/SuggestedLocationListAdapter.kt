/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.listeners.AddLocationListener
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener
import com.newshunt.appview.common.ui.viewholder.EntityItemDiffUtil
import com.newshunt.appview.common.ui.viewholder.ParentChild
import com.newshunt.appview.common.ui.viewholder.SuggestedLocationInfoViewHolder
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.Locations


/**
 * @author  Aman Roy
 * An adapter for child locations in the state.
 */

class SuggestedLocationListAdapter(
        val viewOnItemClickListener: RecyclerViewOnItemClickListener,
        val pageReferrer: PageReferrer?,
        val addLocationListener: AddLocationListener,
        val showFollowButton: Boolean,
        val eventSection: NhAnalyticsEventSection,
        val  locationFollowClickListener :LocationFollowClickListener) : androidx.recyclerview.widget
.RecyclerView
.Adapter<SuggestedLocationInfoViewHolder>() {


    private val childLocations: MutableList<Location> = mutableListOf()

    init {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedLocationInfoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.suggested_location_list_view,
                parent, false)
        return SuggestedLocationInfoViewHolder(view, addLocationListener, viewOnItemClickListener,
                showFollowButton, pageReferrer, eventSection, locationFollowClickListener)
    }

    override fun getItemCount() = childLocations.size

    fun setItems(locations: List<Location>) {
        val oldList = mutableListOf<Location>()
        oldList.addAll(childLocations)
        childLocations.clear()
        childLocations.addAll(locations)
        val diffCallback = LocationItemDiffUtil(oldList, locations)
        val result = DiffUtil.calculateDiff(diffCallback)
        result.dispatchUpdatesTo(this)

    }

    override fun onBindViewHolder(holder: SuggestedLocationInfoViewHolder, position: Int) {
        holder.updateLocation(childLocations[position])
    }




}

class LocationItemDiffUtil(private val oldList: List<Location>,
                         private val newList: List<Location>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id

    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isFollowed == newList[newItemPosition].isFollowed &&
                oldList[oldItemPosition].displayName == newList[newItemPosition].displayName

    }

}
