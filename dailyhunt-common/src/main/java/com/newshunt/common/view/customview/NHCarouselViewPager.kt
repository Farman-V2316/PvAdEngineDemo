/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class NHCarouselViewPager : androidx.viewpager.widget.ViewPager {

    var disableSwipe: Boolean = false
    var mStartDragX: Float = 0f
    private var onSwipeOutofBoundsListener: OnSwipeOutofBoundsListener? = null
    private var previousSupported: Boolean = false

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (disableSwipe) {
            return false
        }

        if (onSwipeOutofBoundsListener != null) {
            val x: Float = ev?.x ?: 0f
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> mStartDragX = x
                MotionEvent.ACTION_MOVE -> if (mStartDragX < x && currentItem == 0) {
                    if (previousSupported) {
                        return onSwipeOutofBoundsListener!!.onSwipeOutAtStart()
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (disableSwipe) {
            return false
        }
        return super.onTouchEvent(ev)
    }

    fun setPreviousSupported(previousSupported: Boolean) {
        this.previousSupported = previousSupported
    }

    fun addOnSwipeOutofBoundsListener(onSwipeOutofBoundsListener: OnSwipeOutofBoundsListener) {
        this.onSwipeOutofBoundsListener = onSwipeOutofBoundsListener
    }

}

interface OnSwipeOutofBoundsListener {
    fun onSwipeOutAtStart(): Boolean
    fun onSwipeOutAtEnd(): Boolean
}