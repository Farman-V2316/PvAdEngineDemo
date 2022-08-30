/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.customview;

import android.content.Context;
import android.util.AttributeSet;

import com.newshunt.common.view.customview.CustomNestedScrollView;

/**
 * Scroll view which notifies on scroll
 *
 * @author sumedh.tambat
 */
public class NotifyingNestedScrollView extends CustomNestedScrollView {

  private OnScrollChangedListener onScrollChangedListener;

  public NotifyingNestedScrollView(Context context) {
    super(context);
  }

  public NotifyingNestedScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NotifyingNestedScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);

    if (onScrollChangedListener != null) {
      onScrollChangedListener.onScrollChanged(t);
    }
  }

  public void setOnScrollChangedListener(OnScrollChangedListener listener) {
    this.onScrollChangedListener = listener;
  }

  public interface OnScrollChangedListener {
    void onScrollChanged(int scrollY);
  }
}


