/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.HastTagAsset
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.news.helper.DefaultNavigatorCallback


class HashtagAdapter(val card: CommonAsset?) : RecyclerView.Adapter<HashtagAdapter.HashitemViewHolder>() {
	private var mItems: List<HastTagAsset>? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagAdapter.HashitemViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.hashcode_item_view, parent, false)
		return HashitemViewHolder(view)
	}

	override fun onBindViewHolder(holder: HashitemViewHolder, position: Int) {
		holder.onBind(position)
	}

	override fun getItemCount(): Int {
		return mItems?.size ?: 0
	}

	fun setItems(data: List<HastTagAsset>?) {
		this.mItems = data
		notifyDataSetChanged()
	}

	inner class HashitemViewHolder constructor(itemView: View) : RecyclerView.ViewHolder
	(itemView) {
		private val text: TextView = itemView.findViewById(R.id.text)

		fun onBind(position: Int) {
			text.text = mItems?.get(position)?.name
			itemView.setOnClickListener(View.OnClickListener {
				val url = mItems?.get(position)?.url
				Logger.d("HashtagAdapter", "launching deeplink $url")
				CommonNavigator.launchInternalDeeplink(itemView.context, url,
					PageReferrer(NhGenericReferrer.STORY_DETAIL, card?.i_id()), true, DefaultNavigatorCallback())
			})
		}
	}

}