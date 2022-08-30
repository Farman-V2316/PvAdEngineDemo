/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.behavior;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Bottom bar behaviour for NHShareView at bottom
 *
 * @author santhosh.kc on 6/10/2016.
 */
public class FixedBottomViewGroupBarBehavior<T extends ViewGroup> extends CoordinatorLayout.Behavior<T> {

  public FixedBottomViewGroupBarBehavior() {
    super();
  }

  public FixedBottomViewGroupBarBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, T child, View dependency) {
    return dependency instanceof AppBarLayout;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, T child,
                                        View dependency) {
    return true;
  }
}
