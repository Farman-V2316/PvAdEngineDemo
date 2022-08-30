/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.HeaderRecyclerViewAdapter
import com.newshunt.common.view.customview.HeaderRecyclerViewAdapter.getOffsetValue
import com.newshunt.dhutil.R
import com.newshunt.dhutil.iterator

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source URL - https://gist.github.com/alexfu/0f464fc3742f134ccd1e
 */
class CardListItemDecoration @JvmOverloads constructor(context: Context,
                                                       orientation: Int = RecyclerView.VERTICAL,
                                                       private var thinDivider: Int = CommonUtils.getDimension(R.dimen.divider_height),
                                                       private var thickDivider: Int = 0) :
        RecyclerView.ItemDecoration() {

    private var mDivider: Drawable? = null
    private var mOrientation: Int = 0
    private val collectionViewTypes = emptyList<Int>()


    init {
        mDivider = ContextCompat.getDrawable(context, R.drawable.card_list_recycler_view_divider)
        setOrientation(orientation)
    }

    fun setDividerColor(color: Int) {
        (mDivider as? GradientDrawable)?.setColor(color)
    }

    fun setOrientation(orientation: Int) {
        if (orientation != LinearLayoutManager.HORIZONTAL && orientation != LinearLayoutManager.VERTICAL) {
            throw IllegalArgumentException("invalid orientation")
        }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {

        val adapter = parent.adapter
        if (adapter is HeaderRecyclerViewAdapter) {
            handleHeaderRecyclerView(parent, adapter, c)
        } else if (adapter != null) {
            handleOtherRecyclerView(parent, c)
        }
    }

    fun handleOtherRecyclerView(parent: RecyclerView,
                                canvas: Canvas) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        parent.iterator().forEach { child ->
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.viewAdapterPosition

            val bottom: Int = child.top - params.topMargin
            var top: Int = bottom
            if (position > 0) {
                top = bottom - thinDivider
            }

            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
    }

    fun handleHeaderRecyclerView(parent: RecyclerView,
                                 adapter: HeaderRecyclerViewAdapter,
                                 canvas: Canvas) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount

        for (i in 0 until childCount) {

            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.viewAdapterPosition

            val bottom: Int = child.top - params.topMargin
            var top: Int = bottom

            if (adapter.useHeader()) {
                if (position > 1) {
                    val previousViewType = adapter.getItemViewType(position - 1) - getOffsetValue()
                    val currentViewType = adapter.getItemViewType(position) - getOffsetValue()
                    if (collectionViewTypes.contains(previousViewType) || collectionViewTypes.contains(currentViewType)) {
                        top = bottom - thickDivider
                    } else {
                        top = bottom - thinDivider
                    }
                }
            } else {
                if (position > 0) {
                    val previousViewType = adapter.getItemViewType(position - 1) - getOffsetValue()
                    val currentViewType = adapter.getItemViewType(position) - getOffsetValue()
                    if (collectionViewTypes.contains(previousViewType) || collectionViewTypes.contains(currentViewType)) {
                        top = bottom - thickDivider
                    } else {
                        top = bottom - thinDivider
                    }
                }
            }
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
    }

    fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            val adapter = parent.adapter ?: return
            val params = view.layoutParams as RecyclerView.LayoutParams

            // we want to retrieve the position in the list
            val position = params.viewAdapterPosition

            // and add a separator to any view but the last one
            if (position > 0) {
                outRect.set(0, getTopOffset(adapter.getItemViewType(position - 1) - getOffsetValue(), adapter
                        .getItemViewType(position) - getOffsetValue()), 0, 0)
                // left, top,
                // right, bottom
            } else {
                outRect.setEmpty() // 0, 0, 0, 0
            }
        } else {
            outRect.setEmpty()
        }
    }

    private fun getTopOffset(previousViewType: Int, currentViewType: Int): Int {
        if (collectionViewTypes.contains(currentViewType) || collectionViewTypes.contains
                (previousViewType)) {
            return thickDivider
        } else
            return thinDivider
    }
}