package com.newshunt.common.view.customview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import kotlin.math.abs

/**
 * Webview in a NestedScrollView bug - https://issuetracker.google.com/issues/37077954
 *
 * This WebView is compatible with CoordinatorLayout.
 * The implementation is based on NestedScrollView of design library.
 * Any change to it must involve verifying coordinator bar and bottom bar collapse on scroll,
 * swipe-to-refresh, horizontal & vertical scrolls.
 *
 * Source code ref : https://gist.github.com/alexmiragall/0c4c7163f7a17938518ce9794c4a5236
 */
class NestedScrollWebView @JvmOverloads constructor(context: Context?,
                                                    attrs: AttributeSet? = null,
                                                    defStyleAttr: Int = android.R.attr.webViewStyle) :
    HorizontalSwipeWebView(context, attrs, defStyleAttr), NestedScrollingChild {

    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mLastMotionY = 0
    private val mChildHelper: NestedScrollingChildHelper
    private var mIsBeingDragged = false
    private var velocityTracker: VelocityTracker? = null
    private var mTouchSlop = 0
    private var mActivePointerId = INVALID_POINTER
    private var mNestedYOffset = 0
    private val mScroller = OverScroller(context)
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0

    private fun initScrollView() {
        isFocusable = true
        setWillNotDraw(false)
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        val vtev = MotionEvent.obtain(event)
        val actionMasked = event.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0
        }
        vtev.offsetLocation(0f, mNestedYOffset.toFloat())
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mIsBeingDragged = !mScroller.isFinished
                if (mIsBeingDragged) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                mLastMotionY = event.y.toInt()
                mActivePointerId = event.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = event.findPointerIndex(mActivePointerId)
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=$mActivePointerId in onTouchEvent")
                } else {
                    val y = event.getY(activePointerIndex).toInt()
                    var deltaY = mLastMotionY - y
                    if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                        deltaY -= mScrollConsumed[1]
                        vtev.offsetLocation(0f, mScrollOffset[1].toFloat())
                        mNestedYOffset += mScrollOffset[1]
                    }
                    if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                        val parent = parent
                        parent?.requestDisallowInterceptTouchEvent(true)
                        mIsBeingDragged = true
                        if (deltaY > 0) {
                            deltaY -= mTouchSlop
                        } else {
                            deltaY += mTouchSlop
                        }
                    }
                    if (mIsBeingDragged) {
                        mLastMotionY = y - mScrollOffset[1]
                        val oldY = scrollY

                        //Below code causes horizontal swiping bug in web carousel.
                        /*
                        final int range = getScrollRange();
                       // Calling overScrollByCompat will call onOverScrolled, which
                       // calls onScrollChanged if applicable.
                        if (overScrollByCompat(0, deltaY, 0, getScrollY(), 0, range, 0,
                        0, true) && !hasNestedScrollingParent()) {
                        // Break our velocity if we hit a scroll barrier.
                        mVelocityTracker.clear();
                        }*/
                        val scrolledDeltaY = scrollY - oldY
                        val unconsumedY = deltaY - scrolledDeltaY
                        if (dispatchNestedScroll(0, deltaY, 0, unconsumedY, mScrollOffset)) {
                            mLastMotionY -= mScrollOffset[1]
                            vtev.offsetLocation(0f, mScrollOffset[1].toFloat())
                            mNestedYOffset += mScrollOffset[1]
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsBeingDragged) {
                    velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocity =
                        velocityTracker?.getYVelocity(mActivePointerId)?.toInt() ?: 0
                    if (abs(initialVelocity) > mMinimumVelocity) {
                        flingWithNestedDispatch(-initialVelocity)
                    } else if (mScroller.springBack(scrollX, scrollY, 0, 0, 0, scrollRange)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                mActivePointerId = INVALID_POINTER
                endDrag()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsBeingDragged && childCount > 0) {
                    if (mScroller.springBack(scrollX, scrollY, 0, 0, 0, scrollRange)) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                mActivePointerId = INVALID_POINTER
                endDrag()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                mLastMotionY = event.getY(index).toInt()
                mActivePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                mLastMotionY = event.getY(event.findPointerIndex(mActivePointerId)).toInt()
            }
        }

        velocityTracker?.addMovement(vtev)
        vtev.recycle()
        return super.onTouchEvent(event)
    }

    //Using scroll range of webview instead of childs as NestedScrollView does.
    private val scrollRange: Int
        get() = computeVerticalScrollRange()

    private fun endDrag() {
        mIsBeingDragged = false
        recycleVelocityTracker()
        stopNestedScroll()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionY = ev.getY(newPointerIndex).toInt()
            mActivePointerId = ev.getPointerId(newPointerIndex)
            velocityTracker?.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun flingWithNestedDispatch(velocityY: Int) {
        val canFling = ((scrollY > 0 || velocityY > 0)
                && (scrollY < scrollRange || velocityY < 0))
        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
            dispatchNestedFling(0f, velocityY.toFloat(), canFling)
            if (canFling) {
                fling(velocityY)
            }
        }
    }

    private fun fling(velocityY: Int) {
        if (childCount > 0) {
            val height = height - paddingBottom - paddingTop
            val bottom = getChildAt(0).height
            mScroller.fling(scrollX, scrollY, 0, velocityY, 0, 0, 0,
                Math.max(0, bottom - height), 0, height / 2)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int,
                                      dyConsumed: Int,
                                      dxUnconsumed: Int,
                                      dyUnconsumed: Int,
                                      offsetInWindow: IntArray?): Boolean {
        return mChildHelper.dispatchNestedScroll(dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int,
                                         dy: Int,
                                         consumed: IntArray?,
                                         offsetInWindow: IntArray?): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float,
                                     velocityY: Float,
                                     consumed: Boolean): Boolean {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return ViewCompat.SCROLL_AXIS_NONE
    }

    companion object {
        private const val INVALID_POINTER = -1
        private const val TAG = "NestedWebView"
    }

    init {
        overScrollMode = OVER_SCROLL_NEVER
        initScrollView()
        mChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }
}