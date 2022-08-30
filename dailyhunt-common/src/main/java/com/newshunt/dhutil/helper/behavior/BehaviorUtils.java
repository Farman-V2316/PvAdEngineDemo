/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.behavior;

import android.view.ViewGroup;

import com.google.android.material.appbar.AppBarLayout;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import org.jetbrains.annotations.Nullable;

import androidx.appcompat.widget.Toolbar;

/**
 * Behavior Utils
 *
 * @author arun.babu
 */
public class BehaviorUtils {

  public static int getHidingBarHeight() {
    return CommonUtils.getDimension(R.dimen.actionbar_height);
  }

  public static int getBottomBarHeight() {
    return CommonUtils.getDimension(R.dimen.bottom_bar_height);
  }

  static int lerp(int startValue, int endValue, float fraction) {
    return startValue + Math.round(fraction * (endValue - startValue));
  }

  /**
   * Helper method to read the handshake configuration of topbar and makes it fixed or scrollable
   * by setting scrollflags in the layout params
   * @param toolbar Toolbar of the activity
   */
  public static void enableTopbarScrolling(@Nullable Toolbar toolbar) {
    if (toolbar == null) {
      return;
    }

    boolean isTopbarFixed = PreferenceManager.getPreference(AppStatePreference.IS_TOPBAR_FIXED,
        true);
    ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
    if (layoutParams instanceof AppBarLayout.LayoutParams) {
      if (isTopbarFixed) {
        ((AppBarLayout.LayoutParams) layoutParams).setScrollFlags(0);
      } else {
        ((AppBarLayout.LayoutParams) layoutParams).setScrollFlags(
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
      }
    }
  }
}
