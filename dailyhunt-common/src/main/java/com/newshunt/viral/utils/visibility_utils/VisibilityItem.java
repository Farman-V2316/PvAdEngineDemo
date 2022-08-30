/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.viral.utils.visibility_utils;

/**
 * Wrapper to hold visibility item and index
 *
 * @author: bedprakash on 04/12/17.
 */

class VisibilityItem {
  int index = -1;
  final VisibilityAwareViewHolder holder;

  VisibilityItem(int index, VisibilityAwareViewHolder holder) {
    this.index = index;
    this.holder = holder;
  }
}
