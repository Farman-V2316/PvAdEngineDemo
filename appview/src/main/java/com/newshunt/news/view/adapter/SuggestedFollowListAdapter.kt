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
import com.newshunt.dataentity.common.asset.PostSuggestedFollow


class SuggestedFollowListAdapter : RecyclerView.Adapter<SuggestedFollowVH>() {

	private var items: List<PostSuggestedFollow>? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedFollowVH {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.suggested_follow, parent, false)
		return SuggestedFollowVH(view)
	}

	override fun getItemCount(): Int {
		return items?.size ?: 0
	}

	override fun onBindViewHolder(holder: SuggestedFollowVH, position: Int) {
		holder.textView.text = items?.get(position)?.displayName ?: ""
	}

	fun setItems(data: List<PostSuggestedFollow>?) {
		this.items = data
		notifyDataSetChanged()
	}
}

class SuggestedFollowVH(val view: View) : RecyclerView.ViewHolder(view) {
	val textView = view.findViewById<TextView>(R.id.display_text)
}