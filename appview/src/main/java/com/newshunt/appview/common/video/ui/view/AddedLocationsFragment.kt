/**
 *  Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.view

/**
 * This class is used to show bottom sheet menu for selected locations in Local zone.
 * @author ajay.gu
 *
 */


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.newshunt.appview.R
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.image.Image

class AddedLocationsFragment(private val latestFollowedLocation: List<FollowSyncEntity>,
                             private val bottomLocMenuListener: BottomLocMenuListener) :
        BottomSheetDialogFragment() {

    private var unfollowedLocations = HashSet<ActionableEntity>()
    private var selecetedLoc: ActionableEntity? = null
    private lateinit var lzLocRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_added_locations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lzLocRecyclerView = view.findViewById(R.id.local_zone_location_menu)
        val addMoreLoc: TextView = view.findViewById(R.id.add_more_loc)
        lzLocRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = MyRecyclerViewAdapter(requireContext(), latestFollowedLocation,
                object : MyRecyclerViewAdapter.OnItemCheckListener {
                    //Remove from unfollowedLocations list
                    override fun onItemCheck(view: View, data: FollowSyncEntity) {
                        unfollowedLocations.remove(data.actionableEntity)
                    }
                    //add to unfollowedLocations list
                    override fun onItemUncheck(view: View, data: FollowSyncEntity) {
                        unfollowedLocations.add(data.actionableEntity)
                    }
                    //set selected location and dismiss the bottom sheet
                    override fun onLocationItemClick(loc: ActionableEntity) {
                        selecetedLoc = loc
                        dialog?.dismiss()
                    }
                })
        lzLocRecyclerView.adapter = adapter
        addMoreLoc.setOnClickListener {
            dialog?.dismiss()
            bottomLocMenuListener.addMoreLocations()
        }
    }

    companion object {
        const val TAG: String = "AddedLocationsFragment"
    }

    interface BottomLocMenuListener {
        fun onDialogDismissUpdateLocations(view: View, unFollowedLocationList: List<ActionableEntity>,
                                           selectedLoc: ActionableEntity?)
        fun addMoreLocations()
    }

    override fun onDestroy() {
        if (selecetedLoc != null || !unfollowedLocations.isNullOrEmpty()) {
            bottomLocMenuListener.onDialogDismissUpdateLocations(lzLocRecyclerView, unfollowedLocations.toList(),
                    selecetedLoc)
            selecetedLoc = null
            unfollowedLocations.clear()
        }
        super.onDestroy()
    }
}

class MyRecyclerViewAdapter(context: Context?, private val data: List<FollowSyncEntity>,
                            private val onItemCheckListener: OnItemCheckListener) : RecyclerView
.Adapter<MyRecyclerViewAdapter.ViewHolder?>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemCheckListener {
        fun onItemCheck(view: View, data: FollowSyncEntity)
        fun onItemUncheck(view: View, data: FollowSyncEntity)
        fun onLocationItemClick(selectedLoc: ActionableEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.added_location_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.locationName.text = data[position].actionableEntity.displayName
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(data[position].actionableEntity.entityImageUrl, 40,
                40)
        Image.load(qualifiedUrl)
                .placeHolder(R.drawable.follow_circle_divider)
                .into(holder.locationIcon)
        holder.locationCheckbox.isChecked = true

        holder.locationCheckbox.setOnClickListener {
            if (holder.locationCheckbox.isChecked) {
                onItemCheckListener.onItemCheck(holder.locationCheckbox, data[position])
            } else {
                onItemCheckListener.onItemUncheck(holder.locationCheckbox, data[position])
            }
        }

        holder.locationItemHolder.setOnClickListener {
            onItemCheckListener.onLocationItemClick(data[position].actionableEntity)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationName: TextView = itemView.findViewById(R.id.location_name)
        val locationIcon: ImageView = itemView.findViewById(R.id.location_icon)
        val locationCheckbox: CheckBox = itemView.findViewById(R.id.loc_check_box)
        val locationItemHolder: LinearLayout = itemView.findViewById(R.id.location_item_holder)

        fun setOnClickListener(onClickListener: View.OnClickListener?) {
            itemView.setOnClickListener(onClickListener)
        }
    }

    fun getItem(id: Int): String {
        return data[id].actionableEntity.entityId
    }
}


