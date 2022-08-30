/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.customview;

import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom SwipeRefreshLayout class which allows children to scroll up.
 *
 * @author vishal.bharati
 */

public class WebItemSwipeRefreshLayout extends SwipeRefreshLayout {


  public WebItemSwipeRefreshLayout(Context context) {
    super(context);
  }

  public WebItemSwipeRefreshLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }


  @Override
  public boolean canChildScrollUp() {
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    requestDisallowInterceptTouchEvent(true);
    return super.onTouchEvent(event);
  }

}