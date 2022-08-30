/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

/**
 * {@inheritDoc}
 * <p/>
 * Width of the popup window is set to wrap content.
 *
 * @author karthik.r on 22/08/2016.
 */
public class NHListPopupWindow extends ListPopupWindow {
  public NHListPopupWindow(Context context) {
    super(context);
  }

  public NHListPopupWindow(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NHListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public NHListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void setAdapter(ListAdapter adapter) {
    super.setAdapter(adapter);
    setContentWidth(getMeasuredWith(adapter));
  }

  private int getMeasuredWith(ListAdapter adapter) {
    if (adapter == null) {
      return 0;
    }

    int maxWidth = 0;

    final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    for (int i = 0; i < adapter.getCount(); i++) {
      View view = adapter.getView(i, null, null);
      view.measure(widthMeasureSpec, heightMeasureSpec);
      if (view.getMeasuredWidth() > maxWidth) {
        maxWidth = view.getMeasuredWidth();
      }
    }

    return maxWidth;
  }
}
