/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview;

import android.content.Context;
import androidx.core.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Scroll view which notifies on scroll
 *
 * @author sumedh.tambat
 */
public class NotifyingScrollView extends NestedScrollView implements DisableInterceptViewGroup {

  private OnScrollChangedListener onScrollChangedListener;

  protected boolean interceptDisabled;

  public NotifyingScrollView(Context context) {
    super(context);
  }

  public NotifyingScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NotifyingScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);

    if (onScrollChangedListener != null) {
      onScrollChangedListener.onScrollChanged(t);
    }
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    interceptDisabled = false;
  }

  public void setOnScrollChangedListener(OnScrollChangedListener listener) {
    this.onScrollChangedListener = listener;
  }

  @Override
  public void disableIntercept(boolean disable) {
    interceptDisabled = disable;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    return !interceptDisabled && super.onInterceptTouchEvent(event);
  }

  public interface OnScrollChangedListener {
    void onScrollChanged(int scrollY);
  }
}


