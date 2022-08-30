/*
 *  Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager


/***
 * @author amit.chaudhary
 */
// this value will be compared with viewpager currentheight and child height if diff is more than
// specified value layout child will be remeasured


open class NHWrappedHeightViewPager : ViewPager {
    private var currentHeight = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        val mScroller = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("mScroller")
        mScroller.isAccessible = true
        mScroller.set(this, CustomScroller(context, LinearOutSlowInInterpolator(), 350))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (shouldMeasureHeight()) {
            var finalHeight = 0
            when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.EXACTLY -> {
                    finalHeight = MeasureSpec.getSize(heightMeasureSpec)
                    val childHeight = finalHeight - paddingTop - paddingBottom
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)

                        if (child.visibility == View.GONE) {
                            continue
                        }
                        child.measure(widthMeasureSpec,
                                View.MeasureSpec.makeMeasureSpec(childHeight,
                                        View.MeasureSpec.EXACTLY))
                        child.layoutParams.height = childHeight
                        break
                    }
                }
                MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> {
                    var maxHeight = Int.MAX_VALUE
                    if (heightMeasureSpec == MeasureSpec.AT_MOST) {
                        maxHeight = MeasureSpec.getSize(heightMeasureSpec)
                    }
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)

                        if (child.visibility == View.GONE) {
                            continue
                        }

                        child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.getMode(heightMeasureSpec)))
                        val h = child.measuredHeight+ paddingTop + paddingBottom
                        finalHeight = Math.min(Math.max(h, finalHeight), maxHeight)
                        break
                    }
                }
            }
            super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY))
            currentHeight = finalHeight
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(currentHeight, MeasureSpec.EXACTLY))
        }
    }


    private fun shouldMeasureHeight(): Boolean {
        return currentHeight == 0
    }

    open fun resetView() {
        currentHeight = 0
    }
}


class CustomScroller(context: Context, interpolator: Interpolator, private val mDuration: Int) : Scroller
(context, interpolator) {

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }
}