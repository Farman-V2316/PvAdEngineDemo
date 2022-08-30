/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.video.ui.view.LocationListener
import com.newshunt.appview.common.video.ui.viewholder.DHHashTagViewHolder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.HastTagAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.news.helper.DefaultNavigatorCallback

/**
 * Created on 08/28/2019.
 */
class DHHashTagAdapter(private val context: Context?, private val tagList: List<HastTagAsset>?,
                       val pageReferrer: PageReferrer?, val locationListener: LocationListener
) :
        RecyclerView.Adapter<DHHashTagViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): DHHashTagViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.dh_tag_view, parent, false)
        return DHHashTagViewHolder(view)
    }

    override fun onBindViewHolder(holder: DHHashTagViewHolder, position: Int) {
        holder.title.text = tagList?.get(position)?.name
        if(tagList?.get(position)?.type == Constants.LOCATION) {
            holder.title.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.vector_location_icon, 0, 0, 0)
            holder.title.compoundDrawablePadding = CommonUtils.getDimension(R.dimen.dimen_2)
        }
        holder.title.setOnClickListener(View.OnClickListener {
            if(tagList?.get(position)?.type == Constants.LOCATION) {
                locationListener?.onLocationHashtagSelected()
            } else if(!CommonUtils.isEmpty(tagList?.get(position)?.url)){
                val url = tagList?.get(position)?.url
                Logger.d("HashtagAdapter", "launching deeplink $url")
                CommonNavigator.launchInternalDeeplink(context, url,
                    PageReferrer(NhGenericReferrer.ORGANIC_SOCIAL), true, DefaultNavigatorCallback()
                )
            }
        })
    }

    override fun getItemCount(): Int {
        return tagList?.size ?: 0
    }

}