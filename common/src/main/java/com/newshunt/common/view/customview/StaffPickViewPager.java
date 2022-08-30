/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom staff pick view pager
 *
 * @author santosh.kumar
 */
public class StaffPickViewPager extends ViewPager {

  public StaffPickViewPager(Context context) {
    super(context);
    init();
  }

  public StaffPickViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    this.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        v.getParent().requestDisallowInterceptTouchEvent(true);
        return false;
      }
    });
  }
}
