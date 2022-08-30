/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.newshunt.dhutil.iterator
import kotlin.math.roundToInt

class SimpleItemDecorator(private val horizontalSpacing: Int,
                          private val verticalSpacing: Int,
                          private val drawable: Drawable? = null) :
        RecyclerView.ItemDecoration() {
    private var displayMode: Int = -1

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildViewHolder(view).adapterPosition
        setSpacingForDirection(outRect, parent.layoutManager, position, state.itemCount)
    }

    private fun setSpacingForDirection(
            outRect: Rect,
            layoutManager: RecyclerView.LayoutManager?,
            position: Int,
            itemCount: Int
    ) {

        if (displayMode == -1) {
            displayMode = resolveDisplayMode(layoutManager)
        }

        when (displayMode) {
            HORIZONTAL -> {
                if (position > 0) {
                    outRect.left = horizontalSpacing
                }
            }
            VERTICAL -> {
                if (position > 0) {
                    outRect.top = verticalSpacing
                }
            }
            GRID -> if (layoutManager is GridLayoutManager) {
                val gridLayoutManager = layoutManager as GridLayoutManager?
                val cols = gridLayoutManager!!.spanCount

                val currentCol = position % cols
                if (currentCol > 0) {
                    outRect.left = horizontalSpacing
                }

                val currentRow = position / cols
                if (currentRow > 0) {
                    outRect.top = verticalSpacing
                }
            }

            FLEX -> {
                /*Unable to get line counts properly from flexBoxLayoutManager*/
                outRect.left = (horizontalSpacing / 2f).roundToInt()
                outRect.right = (horizontalSpacing / 2f).roundToInt()
                outRect.top = (verticalSpacing / 2f).roundToInt()
                outRect.bottom = (verticalSpacing / 2f).roundToInt()
            }
        }
    }

    private fun resolveDisplayMode(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager is GridLayoutManager) return GRID
        if (layoutManager is FlexboxLayoutManager) return FLEX
        return if (layoutManager!!.canScrollHorizontally()) HORIZONTAL else VERTICAL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (drawable == null || displayMode != VERTICAL) {
            super.onDraw(c, parent, state)
        } else {
            /*ONLY FOR VERTICAL LIST*/
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            parent.iterator().forEach { child ->
                val params = child.layoutParams as RecyclerView.LayoutParams
                val position = params.viewAdapterPosition

                val bottom: Int = child.top - params.topMargin
                var top: Int = bottom
                if (position > 0) {
                    top = bottom - verticalSpacing
                }

                drawable.setBounds(left, top, right, bottom)
                drawable.draw(c)
            }
        }
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val GRID = 2
        const val FLEX = 3
    }
}