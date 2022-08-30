/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.video.ui.viewholder.DHDummyViewHolder

/**
 * Created on 08/28/2019.
 */
class DHDummyAdapter(private val context: Context?, private val tagList: List<String>) :
        RecyclerView.Adapter<DHDummyViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): DHDummyViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.dh_dummy_view, parent, false)
        return DHDummyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DHDummyViewHolder, position: Int) {
        holder.title.text = tagList[position]

        holder.title.setOnClickListener(View.OnClickListener {
            Toast.makeText(context, "Discussion Clicked : " + holder.title.text, Toast.LENGTH_SHORT).show()
        })
    }

    override fun getItemCount(): Int {
        return tagList?.size
    }

}