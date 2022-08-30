/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R

/**
 * Created on 08/28/2019.
 */
class DHDummyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal var title: android.widget.TextView

    init {
        title = itemView.findViewById(R.id.title)
    }
}