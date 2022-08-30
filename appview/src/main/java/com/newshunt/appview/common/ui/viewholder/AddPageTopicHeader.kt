/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.common.view.customview.fontview.NHTextView

/**
 * ViewHolder for header in Add Page activity.
 *
 * @author aman.roy
 */

class AddPageTopicHeader(private val view: View) : RecyclerView.ViewHolder(view) {
    private val topicHeaderTitle: NHTextView = view.findViewById(R.id.topic_header_title)

    fun updateHeader(text: String) {
        topicHeaderTitle.text = text
    }
}