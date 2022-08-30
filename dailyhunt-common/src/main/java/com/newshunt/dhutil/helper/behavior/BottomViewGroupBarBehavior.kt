/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.children
import com.newshunt.dhutil.view.customview.NHTabView
import kotlin.math.max
import kotlin.math.min

/**
 * Bottom bar behavior to coordinate with scrolling
 *
 * @author srikanth.r on 09/06/2019.
 */
private const val LOG_TAG = "BottomViewGroupBarBehavior"

open class BottomViewGroupBarBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {
    private var bottomBarHeight = 0
    private var bottomBarMargin = -1

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
                                     child: View,
                                     directTargetChild: View,
                                     target: View,
                                     axes: Int,
                                     type: Int): Boolean {
        Logger.d(LOG_TAG, "onStartedNestedScroll, axes:$axes")
        //Listen to only vertical axis scroll
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
                                child: View,
                                target: View,
                                dxConsumed: Int,
                                dyConsumed: Int,
                                dxUnconsumed: Int,
                                dyUnconsumed: Int,
                                type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type)
        handleScroll(child, dyConsumed)
    }

    /**
     * Handle hide/show view attached with this behavior based on scroll dy
     */
    protected fun handleScroll(child: View, dy: Int) {
        //Init only once
        if (bottomBarMargin == -1) {
            bottomBarMargin = getBottomMargin(child)
        }
        if (bottomBarHeight == 0) {
            bottomBarHeight = getBottomBarHeight(child)
        }

        val maxDistanceToScroll = bottomBarHeight + bottomBarMargin
        val currentTranslation = child.translationY
        val newTranslation = currentTranslation + dy

        val acceptableTranslation = if (dy < 0) {
            //If moving up, max translation to be 0. So, visible on screen
            max(0f, newTranslation)
        } else {
            /*If moving down, set max positive translation as height of bottom bar. will be below
            screen*/
            min(maxDistanceToScroll.toFloat(), newTranslation)
        }
        if (acceptableTranslation != currentTranslation) {
            child.translationY = acceptableTranslation
        }
        Logger.d(LOG_TAG, "onNestedScroll, currentTrans=$currentTranslation, acceptableTranslation=$acceptableTranslation, dy=$dy, newTranslation=$newTranslation")
    }

    private fun getBottomBarHeight(layout: View): Int {
        if (layout is NHTabView) {
            return layout.height
        }
        if (layout is ViewGroup) {
            val height = layout.height
            layout.children.forEach {
                if (it is NHTabView) {
                    return it.height
                }
            }
            return height
        }
        return -1
    }

    private fun getBottomMargin(child: View): Int {
        (child.layoutParams as? CoordinatorLayout.LayoutParams?)?.let {
            return it.bottomMargin
        }
        //return -1 so it will retry
        return -1
    }
}
