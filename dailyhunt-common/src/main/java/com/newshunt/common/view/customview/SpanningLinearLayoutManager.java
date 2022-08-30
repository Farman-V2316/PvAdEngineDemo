/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview;

/**
 * Created by umesh.isran on 7/25/2018.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.R;

public class SpanningLinearLayoutManager extends LinearLayoutManager {
  private static final String LOG_TAG = "SpanningLinearLayoutMan";
  int viewWidth;
  public SpanningLinearLayoutManager(Context context) {
    this(context, CommonUtils.getDeviceScreenWidth());
  }

  public SpanningLinearLayoutManager(Context context, int w) {
    super(context);
    viewWidth = w;
    Logger.d(LOG_TAG, "SpanningLinearLayoutManager: w="+w);
  }

  @Override
  public RecyclerView.LayoutParams generateDefaultLayoutParams() {
    return spanLayoutSize(super.generateDefaultLayoutParams());
  }

  @Override
  public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
    return spanLayoutSize(super.generateLayoutParams(c, attrs));
  }

  @Override
  public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    return spanLayoutSize(super.generateLayoutParams(lp));
  }

  @Override
  public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
    return super.checkLayoutParams(lp);
  }

  private RecyclerView.LayoutParams spanLayoutSize(RecyclerView.LayoutParams layoutParams) {
    if (getOrientation() == HORIZONTAL) {
      layoutParams.width = (int) Math.round(getHorizontalSpace() / (double) getItemCount());
    } else if (getOrientation() == VERTICAL) {
      layoutParams.height = (int) Math.round(getVerticalSpace() / (double) getItemCount());
    }
    return layoutParams;
  }

  @Override
  public boolean canScrollVertically() {
    return false;
  }

  @Override
  public boolean canScrollHorizontally() {
    return false;
  }

  private int getHorizontalSpace() {
    return viewWidth - getPaddingRight() - getPaddingLeft() -
        CommonUtils.getDimension(R.dimen.like_popup_margin);
  }

  private int getVerticalSpace() {
    return getHeight() - getPaddingBottom() - getPaddingTop();
  }
}