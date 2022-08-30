package com.newshunt.news.helper

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.dataentity.common.helper.common.CommonUtils


class ActionbarSpaceDecoration @JvmOverloads constructor(
                                                         private var topSpace: Int = 0) :  RecyclerView.ItemDecoration(){



    override fun getItemOffsets(outRect: Rect, view: View,
                       parent: RecyclerView, state: RecyclerView.State) {

        val childCount = parent.childCount
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount



        if (itemCount > 0 && itemPosition == 0) {
            outRect.top = topSpace
        }
    }
}