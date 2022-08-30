/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.content.Context;
import androidx.core.view.MotionEventCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.newshunt.common.util.R;
import com.newshunt.common.helper.common.Logger;

/**
 * SwipeRefreshLayout where we are going to override the touch events
 *
 * @author bheemesh on 9/4/2015.
 */
public class NHSwipeToRefresh extends SwipeRefreshLayout {

  private static final int INVALID_POINTER = -1;
  private float mInitialDownX;
  private float mInitialDownY;
  private boolean mIsSwippedHorizontally;
  private int mActivePointerIdY = INVALID_POINTER;
  private int mTouchSlop;
  private float mYThresholdDown;


  /**
   * Simple constructor to use when creating a SwipeRefreshLayout from code.
   *
   * @param context
   */
  public NHSwipeToRefresh(Context context) {
    super(context);
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    Logger.d("SWIPE", "NHSwipeToRefresh : constructor1 : mTouchSlop : " + mTouchSlop);
    mYThresholdDown = context.getResources().getDimension(R.dimen.swipe_threshold_dimen);
  }

  /**
   * Constructor that is called when inflating SwipeRefreshLayout from XML.
   *
   * @param context
   * @param attrs
   */
  public NHSwipeToRefresh(Context context, AttributeSet attrs) {
    super(context, attrs);
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    mYThresholdDown = context.getResources().getDimension(R.dimen.swipe_threshold_dimen);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    final int action = MotionEventCompat.getActionMasked(ev);
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mActivePointerIdY = MotionEventCompat.getPointerId(ev, 0);
        final float initialDownY = getMotionEventY(ev, mActivePointerIdY);
        final float initialDownX = getMotionEventX(ev, mActivePointerIdY);

        if (initialDownY == -1) {
          return false;
        }
        mInitialDownX = initialDownX;
        mInitialDownY = initialDownY;
        mIsSwippedHorizontally = false;
        break;
      case MotionEvent.ACTION_MOVE:
        if (mActivePointerIdY == INVALID_POINTER) {
          Logger.e("SWIPE", "Got ACTION_MOVE event but don't have an active pointer id.");
          return false;
        }

        final float x = getMotionEventX(ev, mActivePointerIdY);
        final float y = getMotionEventY(ev, mActivePointerIdY);
        final float xDiff = x - mInitialDownX;
        final float yDiff = y - mInitialDownY;

        if (Math.abs(xDiff) > mTouchSlop && isHorizantalMoveState(yDiff)) {
          mIsSwippedHorizontally = true;
        }
        break;

      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        mActivePointerIdY = INVALID_POINTER;
        break;
    }
    Logger.d("SWIPE", "NHSwipeToRefresh : onInterceptTouchEvent : ACTION_MOVE : " +
        "mIsBeingDraggedHan : " + mIsSwippedHorizontally);
    if (!mIsSwippedHorizontally) {
      return super.onInterceptTouchEvent(ev);
    }
    return false;
  }

  private float getMotionEventY(MotionEvent ev, int activePointerId) {
    final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
    if (index < 0) {
      return -1;
    }
    return MotionEventCompat.getY(ev, index);
  }

  private float getMotionEventX(MotionEvent ev, int activePointerId) {
    final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
    if (index < 0) {
      return -1;
    }
    return MotionEventCompat.getX(ev, index);
  }

  /**
   * Method to determine whether We are in Horizantal Swipe Direction
   *
   * @param yDiff -- YDiff between Initial Y and Current Y
   * @return -- true/false
   */
  private boolean isHorizantalMoveState(float yDiff) {
    return Math.abs(yDiff) < mYThresholdDown;
  }
}
