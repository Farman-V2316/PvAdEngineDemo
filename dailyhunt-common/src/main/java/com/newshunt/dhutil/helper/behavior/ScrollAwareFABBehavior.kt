package com.newshunt.dhutil.helper.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import kotlin.math.max
import kotlin.math.min


/**
 * FAB  behavior to coordinate with scrolling
 *
 * @author manoj.gupta on 14/12/2019.
 *
 */

private const val LOG_TAG = "ScrollAwareFABBehavior"

class ScrollAwareFABBehavior(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, attrs) {
    private var bottomBarHeight = 0
    private val bottomBarMargin = CommonUtils.getDimension(com.newshunt.common.util.R.dimen.bottom_bar_height)

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int): Boolean {
        Logger.d(LOG_TAG, "onStartedNestedScroll, axes:$axes")
        //Listen to only vertical axis scroll
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int) {
        super.onNestedScroll(
            coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
            dyUnconsumed, type
        )
        handleScroll(child, dyConsumed)
    }

    /**
     * Handle hide/show view attached with this behavior based on scroll dy
     */
    private fun handleScroll(child: View, dy: Int) {
        //Init only once
        if (bottomBarHeight == 0) {
            bottomBarHeight = child.height
        }

        // FAB icon always remains on screen but will go down along with bottom bar
        val maxDistanceToScroll = bottomBarHeight - bottomBarMargin
        val currentTranslation = child.translationY
        val newTranslation = currentTranslation + dy

        val acceptableTranslation = if (dy < 0) {
            //If moving up, max translation to be 0.
            max(0f, newTranslation)
        } else {
            // If moving down, set max positive translation as height of bottom bar.
            min(maxDistanceToScroll.toFloat(), newTranslation)
        }
        if (acceptableTranslation != currentTranslation) {
            child.translationY = acceptableTranslation
        }
        Logger.d(
            LOG_TAG,
            "onNestedScroll, currentTrans=$currentTranslation, acceptableTranslation=$acceptableTranslation, dy=$dy, newTranslation=$newTranslation"
        )
    }

}