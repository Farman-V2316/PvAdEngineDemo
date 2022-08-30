/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.content.Context
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.newshunt.common.helper.common.Logger

/**
 * @author santhosh.kc
 */

private const val SWIPE_THRESHOLD = 20f;
private const val LOG_TAG = "MultimediaCarouselViewPager"

enum class LongPress{
    PRESSED, RELEASED
}

interface MultimediaViewPagerCallback {
    fun onLongPress(longPressEvent: LongPress, longPressedPosition : Int)
}

class MultimediaCarouselViewPager : NHWrappedHeightViewPager, GestureDetector.OnGestureListener{

    private var isLongPressed : Boolean = false
    private var longPressedPosition : Int = -1
    private var multimediaViewPagerCallback : MultimediaViewPagerCallback? = null
    private lateinit var gestureDetector : GestureDetectorCompat
    private var userSwiped = false

    constructor(context: Context) : super(context) {
        initGestureDetector(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initGestureDetector(context)
    }

    fun setCallback(multimediaViewPagerCallback: MultimediaViewPagerCallback) {
        this.multimediaViewPagerCallback = multimediaViewPagerCallback
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        Logger.d(LOG_TAG, "onTouchEvent action=${event?.action}")
        gestureDetector.onTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                if (isLongPressed) {
                    sendCallback(LongPress.RELEASED)
                    resetInitialState()
                    return true
                }
                resetInitialState()
            }
        }
        Logger.d(LOG_TAG, "<<<<<<<<<<< on Intercept before return: isLongPressed? $isLongPressed")

        return isLongPressed || super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isLongPressed) {
            return super.onTouchEvent(event)
        }
        when (event?.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                if (isLongPressed) {
                    sendCallback(LongPress.RELEASED)
                }
                resetInitialState()
            }
        }
        return true
    }

    private fun resetInitialState() {
        isLongPressed = false
        longPressedPosition = -1
        userSwiped = false
    }

    override fun onShowPress(event: MotionEvent?) {
    }

    override fun onSingleTapUp(event: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(event: MotionEvent?): Boolean {
        resetInitialState()
        longPressedPosition = currentItem
        Logger.d(LOG_TAG, "onDown")
        return true
    }

    override fun onFling(event: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Logger.d(LOG_TAG, "onFling")
        if (!userSwiped) {
            userSwiped = Math.abs((event?.rawX ?: 0f) - ((p1?.rawX ?: 0f))) >= SWIPE_THRESHOLD
        }
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Logger.d(LOG_TAG, "onScroll")
        if (!userSwiped) {
            userSwiped = Math.abs((p0?.rawX ?: 0f) - ((p1?.rawX ?: 0f))) >= SWIPE_THRESHOLD
        }
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
        Logger.d(LOG_TAG, "onLongPress")
        isLongPressed = true
        sendCallback(LongPress.PRESSED)
    }

    private fun initGestureDetector(context: Context) {
        gestureDetector = GestureDetectorCompat(context, this)
    }

    private fun sendCallback(longPressEvent: LongPress) {
        if (longPressEvent == LongPress.RELEASED) {
            isLongPressed = false
        }

        if (longPressedPosition != currentItem) {
            //If current Item is not same as item position on ACTION_DOWN due to auto swipe, we send
            //LongPress.RELEASED call back to item at position during ACTION_DOWN
            Logger.d(LOG_TAG,"<<<<<<< oncallback, current item " + currentItem + " is not " +
                    "matching with longPressed Item = " + longPressedPosition + " so sending " +
                    "RELEASED callback")
            multimediaViewPagerCallback?.onLongPress(LongPress.RELEASED, longPressedPosition)
            return
        }
        Logger.d(LOG_TAG,"sending LongPress: " + longPressEvent.name)
        multimediaViewPagerCallback?.onLongPress(longPressEvent,currentItem)
    }


    fun checkUserSwiped(): Boolean {
        return userSwiped
    }

    fun isLongPressed(): Boolean {
        return isLongPressed
    }

    override fun resetView() {
        super.resetView()
        userSwiped = false
    }
}