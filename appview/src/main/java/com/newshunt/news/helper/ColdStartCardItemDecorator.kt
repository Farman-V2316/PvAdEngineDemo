/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

/*
* @author amitchaudhary
* */

class ColdStartCardItemDecorator(private val spacing: Int, private val offsetLeft: Int = 0) :
        androidx.recyclerview.widget.RecyclerView
        .ItemDecoration() {
    private var displayMode: Int = -1
    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val position = parent.getChildViewHolder(view).adapterPosition
        setSpacingForDirection(outRect, parent.layoutManager, position, state.itemCount)
    }

    private fun setSpacingForDirection(
            outRect: Rect,
            layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?,
            position: Int,
            itemCount: Int) {

        if (displayMode == -1) {
            displayMode = resolveDisplayMode(layoutManager)
        }

        when (displayMode) {
            HORIZONTAL -> {
                if (position == 0) {
                    outRect.left = offsetLeft
                } else if (position == itemCount - 1) {
                    outRect.right = offsetLeft
                    outRect.left = spacing
                } else {
                    outRect.left = spacing
                }
            }

            GRID -> {
                outRect.left = spacing/2
                outRect.right = spacing/2
                outRect.bottom = spacing
            }
        }
    }

    private fun resolveDisplayMode(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?): Int {
        layoutManager ?: return VERTICAL
        if (layoutManager is androidx.recyclerview.widget.GridLayoutManager) return GRID
        return if (layoutManager.canScrollHorizontally()) HORIZONTAL else VERTICAL
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val GRID = 2
    }
}