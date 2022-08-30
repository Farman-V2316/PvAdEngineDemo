/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.newshunt.common.helper.common.Logger;

/**
 * Header Aware {@link ListUpdateCallback} on difference between old and new lists
 * changed for the contents of {@link HeaderRecyclerViewAdapter}
 *
 * @author santhosh.kc
 */
public class HeaderAwareListUpdateCallback implements ListUpdateCallback {

  private static final String CHECK_TAG = "Social***CommentDiffUpdate";

  @NonNull
  private HeaderRecyclerViewAdapter headerRecyclerViewAdapter;

  public HeaderAwareListUpdateCallback(
      @NonNull HeaderRecyclerViewAdapter headerRecyclerViewAdapter) {
    this.headerRecyclerViewAdapter = headerRecyclerViewAdapter;
  }

  @Override
  public void onInserted(int position, int count) {
    Logger.d(CHECK_TAG, "onInserted " + position + "  " + count);
    position = getNewPosition(position);
    headerRecyclerViewAdapter.notifyItemRangeInserted(position, count);
  }

  @Override
  public void onRemoved(int position, int count) {
    Logger.d(CHECK_TAG, "onRemoved " + position + "  " + count);
    position = getNewPosition(position);
    headerRecyclerViewAdapter.notifyItemRangeRemoved(position, count);
  }

  @Override
  public void onMoved(int fromPosition, int toPosition) {
    Logger.d(CHECK_TAG, "onMoved " + fromPosition + "  " + toPosition);
    fromPosition = getNewPosition(fromPosition);
    toPosition = getNewPosition(toPosition);

    headerRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition);
  }

  @Override
  public void onChanged(int position, int count, Object payload) {
    Logger.d(CHECK_TAG, "onChanged " + position + "  " + count);
    position = getNewPosition(position);
    headerRecyclerViewAdapter.notifyItemRangeChanged(position, count, payload);
  }

  private int getNewPosition(int positionWithoutHeaderConsideration) {
    Logger.d(CHECK_TAG, "getNewPosition " + positionWithoutHeaderConsideration + "  " + headerRecyclerViewAdapter.useHeader());
    if (headerRecyclerViewAdapter.useHeader()) {
      return positionWithoutHeaderConsideration + 1;
    }
    return positionWithoutHeaderConsideration;
  }
}
