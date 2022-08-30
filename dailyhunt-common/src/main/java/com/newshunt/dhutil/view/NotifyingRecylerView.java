/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.newshunt.common.view.customview.DisableInterceptViewGroup;

/**
 * Common base class for RecylerView used in the app
 *
 * @author maruti.borker
 */
public class NotifyingRecylerView extends RecyclerView implements DisableInterceptViewGroup {
  private float xDistance, yDistance, lastX, lastY;

  protected boolean interceptDisabled;

  public NotifyingRecylerView(Context context) {
    super(context);
    init();
  }

  public NotifyingRecylerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public NotifyingRecylerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
  }

  // Required this to fix the issue of having horizontal recyclerView inside a vertical
  // when we do horizontal swipe in recyclerView it conflict with parent and sometime it jerks.
  //ref:: https://stackoverflow.com/questions/37384640/nestedscrollview-not-fling-with-recyclerview-inside

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {

    if (interceptDisabled) {
      return false;
    }

    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        xDistance = yDistance = 0f;
        lastX = ev.getX();
        lastY = ev.getY();
        // This is very important line that fixes
        computeScroll();
        break;
      case MotionEvent.ACTION_MOVE:
        final float curX = ev.getX();
        final float curY = ev.getY();
        xDistance += Math.abs(curX - lastX);
        yDistance += Math.abs(curY - lastY);
        lastX = curX;
        lastY = curY;
        if (xDistance > yDistance) {
          return false;
        }
    }
    return super.onInterceptTouchEvent(ev);
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    interceptDisabled = false;
  }

  @Override
  public void disableIntercept(boolean disable) {
    interceptDisabled = disable;
  }
}