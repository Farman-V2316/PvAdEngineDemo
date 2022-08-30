/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Custom LayoutManager to disable PredictiveItemAnimations. Added to fix crash in recyclerview
 * Reference : http://stackoverflow.com/a/33985508
 *
 * @author satosh.dhanyamraju
 */
public class NoPredAnimLayoutManager extends LinearLayoutManager {
  public NoPredAnimLayoutManager(Context context) {
    super(context);
  }

  public NoPredAnimLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  public NoPredAnimLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public boolean supportsPredictiveItemAnimations() {
    return false;
  }
}
