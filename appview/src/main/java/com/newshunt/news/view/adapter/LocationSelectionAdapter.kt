/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.news.view.listener.LocationSelectListener
import com.newshunt.news.view.viewholder.SeeAllClickListener


class LocationSelectionAdapter(var mItems: List<EntityItem>, private val
locationSelectListener: LocationSelectListener, val seeAllListener: SeeAllClickListener) :
        RecyclerView
        .Adapter<RecyclerView.ViewHolder>() {
	private val LOCATION_ITEM = 1
    private val SEE_ALL_ITEM = 2
    private val itemCount = mItems.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == LOCATION_ITEM) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.location_item_view, parent, false)
            return LocationitemViewHolder(view, locationSelectListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout
                    .location_see_all_viewholder, parent, false)
            return LocationSeeAllViewHolder(view, parent.context, seeAllListener)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != itemCount - 1) {
            (holder as? LocationitemViewHolder)?.onBind(position)
        }
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    fun setItems(data: List<EntityItem>) {
        this.mItems = data
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            SEE_ALL_ITEM
        } else {
            LOCATION_ITEM
        }
    }

    inner class LocationitemViewHolder(itemView: View, locationSelectListener:
    LocationSelectListener) : RecyclerView.ViewHolder(itemView) {

        private val text: TextView = itemView.findViewById(R.id.text)

        fun onBind(position: Int) {
            text.text = mItems?.get(position)?.i_displayName()
            text.isSelected = mItems?.get(position)?.i_selected()
            text.setOnClickListener {
                text.isSelected = !text.isSelected
                locationSelectListener.onLocationSelected(adapterPosition, text.isSelected, false)
            }
        }

    }

    class LocationSeeAllViewHolder(val view: View, val context: Context?, val seeAllListener:
    SeeAllClickListener) : androidx.recyclerview.widget.RecyclerView.ViewHolder
    (view) {

        init {
            view.setOnClickListener {
                seeAllListener.onSeeAllClicked()
            }
        }
    }


}