/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import com.newshunt.appview.R
import com.newshunt.appview.common.ui.listeners.AddLocationListener
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener
import com.newshunt.appview.common.ui.viewholder.CityInfoViewHolder
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.Locations


/**
 * @author priya.gupta
 * An adapter for child locations in the state.
 */
class CityListAdapter(var childLocations: List<Location>,
                      val viewOnItemClickListener: RecyclerViewOnItemClickListener?,
                      val pageReferrer: PageReferrer?,
                      val addLocationListener: AddLocationListener?,
                      val eventSection: NhAnalyticsEventSection?) : androidx.recyclerview.widget.RecyclerView
.Adapter<CityInfoViewHolder>(), LocationFollowClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityInfoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_child_view,
                parent, false)
        return CityInfoViewHolder(view, addLocationListener, viewOnItemClickListener,
                pageReferrer, eventSection, this)
    }

    override fun getItemCount() = childLocations.size

    override fun onBindViewHolder(holder: CityInfoViewHolder, position: Int) {
        holder.updateLocation(childLocations[position])
    }


    fun setItems(data: List<Location>) {

        this.childLocations = data
        notifyDataSetChanged()

    }

    override fun followed(isFollowed: Boolean, location: Location) {
        if (childLocations != null) {
            val index = childLocations!!.indexOf(location)
            if (index != -1)
                childLocations!!.get(index).isFollowed = isFollowed

        }
    }
}
