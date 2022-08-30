/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by anshul on 16/02/18.
 * An item decorator to provide spacing between two items in an horizontal recyclerview.
 */

public class RecyclerViewHorizontalItemDecoration extends RecyclerView.ItemDecoration {

  private final int spacingBetweenItems;
  private final int rightMostItemSpacing;
  private final int leftMostItemSpacing;

  public RecyclerViewHorizontalItemDecoration(int spacingBetweenItems, int rightMostItemSpacing,
                                              int leftMostItemSpacing) {
    this.spacingBetweenItems = spacingBetweenItems;
    this.rightMostItemSpacing = rightMostItemSpacing;
    this.leftMostItemSpacing = leftMostItemSpacing;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                             RecyclerView.State state) {
    if (parent == null || parent.getAdapter() == null) {
      return;
    }

    int viewPosInAdapter = parent.getChildAdapterPosition(view);

    if (viewPosInAdapter == 0) {
      outRect.left = leftMostItemSpacing;
    }
    if (viewPosInAdapter == parent.getAdapter().getItemCount() - 1) {
      outRect.right = rightMostItemSpacing;
    } else {
      outRect.right = spacingBetweenItems;
    }
  }
}
