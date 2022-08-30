/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * A RecyclerView.ItemDecoration to give start offset(padding) at the start of the list and also
 * to give end offset(padding0 at the end of the list
 *
 * @author santhosh.kc
 */
public class OffsetItemDecoration extends RecyclerView.ItemDecoration {

  private final int startOffset;
  private final int endOffset;

  public OffsetItemDecoration(int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }

  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State
      state) {
    int viewPosInAdapter = parent.getChildAdapterPosition(view);
    if (viewPosInAdapter == 0) {
      outRect.left = startOffset;
    }

    if (viewPosInAdapter == parent.getAdapter().getItemCount() - 1) {
      //first item could be also last item, if item count is 1, so not using else if condition
      outRect.right = endOffset;
    }
  }
}
