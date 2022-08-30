/*
 * Created by Rahul Ravindran at 24/2/20 6:17 PM
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetLockBehaviour<V : View> : BottomSheetBehavior<V> {
    constructor() : super()
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)


    var swipeEnabled = true

    override fun onInterceptTouchEvent(
            parent: CoordinatorLayout,
            child: V,
            event: MotionEvent
    ): Boolean {
        return if (swipeEnabled) {
            super.onInterceptTouchEvent(parent, child, event)
        } else {
            false
        }
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return if (swipeEnabled) {
            super.onTouchEvent(parent, child, event)
        } else {
            false
        }
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int
    ): Boolean {
        return if (swipeEnabled) {
            super.onStartNestedScroll(
                    coordinatorLayout,
                    child,
                    directTargetChild,
                    target,
                    axes,
                    type
            )
        } else {
            false
        }
    }

    override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
            type: Int
    ) {
        if (swipeEnabled) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            type: Int
    ) {
        if (swipeEnabled) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }
    }

    override fun onNestedPreFling(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            velocityX: Float,
            velocityY: Float
    ): Boolean {
        return if (swipeEnabled) {
            super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        } else {
            false
        }
    }


    companion object {
        fun <V : View> from(view: V): BottomSheetLockBehaviour<V> {
            val params = view.layoutParams
            require(params is CoordinatorLayout.LayoutParams) { "The view is not a child of CoordinatorLayout" }
            val behavior = (params as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams).behavior
            return if (!(behavior is BottomSheetBehavior<*>)) {
                throw IllegalArgumentException("The view is not associated with BottomSheetBehavior")
            } else {
                behavior as BottomSheetLockBehaviour<V>
            }
        }
    }

}