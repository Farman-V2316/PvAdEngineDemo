/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.content.Context
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.disableInterceptTouchForAncestors

private const val TAG = "HorizontalSwipeWebView"

/**
 * @author santhosh.kc
 */
open class HorizontalSwipeWebView : NhWebView, GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetectorCompat
    private var isScrolling = false

    constructor(context: Context?) : super(context) {
        initGestureDetector(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initGestureDetector(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
            attrs, defStyleAttr) {
        initGestureDetector(context)
    }

    private fun initGestureDetector(context: Context?) {
        gestureDetector = GestureDetectorCompat(context, this)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        parent.disableInterceptTouchForAncestors(false)
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //disables ViewPager when user presses down
        Logger.d(TAG, "Action event: ${event.action}")
        val consumed = super.onTouchEvent(event)
        //gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.disableInterceptTouchForAncestors(true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE -> {
                Logger.d(TAG, "Action event: ${event.action} -> so enabling parents intercept")
                parent.disableInterceptTouchForAncestors(false)
                isScrolling = false
            }
        }

        return isScrolling || consumed
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return false
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        val diffX = Math.abs((p0?.rawX ?: 0f) - ((p1?.rawX ?: 0f)))
        val diffY = Math.abs((p0?.rawY ?: 0f) - ((p1?.rawY ?: 0f)))
        if (diffX > diffY) {
            Log.d(TAG, "OnFling detected, so disabling view pager paging")
            isScrolling = true
        } else {
            Logger.d(TAG, "Not appreciable fling detected..")
        }
        return false
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        val diffX = Math.abs((p0?.rawX ?: 0f) - ((p1?.rawX ?: 0f)))
        val diffY = Math.abs((p0?.rawY ?: 0f) - ((p1?.rawY ?: 0f)))
        if (diffX > diffY) {
            Log.d(TAG, "OnScroll detected, so disabling view pager paging")
            isScrolling = true
        } else {
            Logger.d(TAG, "Not appreciable scroll detected..")
        }
        return false
    }

    override fun onLongPress(p0: MotionEvent?) {

    }
}