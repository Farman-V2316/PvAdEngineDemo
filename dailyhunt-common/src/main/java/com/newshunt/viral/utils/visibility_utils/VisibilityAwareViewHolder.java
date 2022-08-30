/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.viral.utils.visibility_utils;

/**
 * Visibility callback
 *
 * @author: bedprakash on 04/12/17.
 */

public interface VisibilityAwareViewHolder {

  void onVisible(int viewVisibilityPercentage, float percentageOfScreen);

  void onInVisible();

  void onUserLeftFragment();

  void onUserEnteredFragment(int viewVisibilityPercentage, float percentageOfScreen);
}
