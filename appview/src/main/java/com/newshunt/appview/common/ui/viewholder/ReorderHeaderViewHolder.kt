package com.newshunt.appview.common.ui.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.dataentity.common.helper.common.CommonUtils

class ReorderHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

  init {

    val text = view.findViewById<View>(R.id.reorder_header_text) as TextView
    text.text = CommonUtils.getString(R.string.reorder_header)
  }

}