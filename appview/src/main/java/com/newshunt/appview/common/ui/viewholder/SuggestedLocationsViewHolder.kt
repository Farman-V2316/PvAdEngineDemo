/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.SuggestedLocationListAdapter
import com.newshunt.appview.common.ui.listeners.AddLocationListener
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Location
import kotlinx.android.synthetic.main.suggested_location_list_parent_view.view.horizontal_view

/**
 * @author aman.roy
 */

class SuggestedLocationsViewHolder(itemView: View,
                                   viewOnItemClickListener: RecyclerViewOnItemClickListener,
                                   pageReferrer: PageReferrer,
                                   addLocationListener: AddLocationListener,
                                   showFollowButton: Boolean,
                                   eventSection: NhAnalyticsEventSection,
                                   val  locationFollowClickListener : LocationFollowClickListener,
                                   val selectedLoc: Boolean) : RecyclerView.ViewHolder(itemView) {
    private val suggestedLocationContainer: ConstraintLayout
    private val childLocationList: RecyclerView
    private val pageReferrer: PageReferrer
    private val viewOnItemClickListener: RecyclerViewOnItemClickListener
    private val addLocationListener: AddLocationListener
    private val showFollowButton: Boolean
    private val eventSection: NhAnalyticsEventSection

    private var adapter: SuggestedLocationListAdapter? = null
    fun updateStateItem(locations: List<Location>) {
        if(selectedLoc) {
           itemView.horizontal_view.visibility = View.GONE
        }
        childLocationList.visibility = View.VISIBLE
        val layoutManager: LinearLayoutManager = LinearLayoutManager(itemView.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        childLocationList.layoutManager = layoutManager

        if (adapter == null) {
            adapter = SuggestedLocationListAdapter(
                    viewOnItemClickListener, pageReferrer, addLocationListener,
                    showFollowButton, eventSection,locationFollowClickListener)
            childLocationList.adapter = adapter
        }
        adapter!!.setItems(locations)


    }

    init {
        suggestedLocationContainer = itemView.findViewById(R.id.suggested_location_container)

//        Get the data in this list using ViewModel
        childLocationList = itemView.findViewById(R.id.child_suggested_location_list)
        this.pageReferrer = pageReferrer
        this.viewOnItemClickListener = viewOnItemClickListener
        this.addLocationListener = addLocationListener
        this.showFollowButton = showFollowButton
        this.eventSection = eventSection
    }
}