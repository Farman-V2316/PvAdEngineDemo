/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.viral.utils.visibility_utils;

import android.graphics.Rect;
import android.view.View;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Class to calculate visibility percentage of view holders
 *
 * @author: bedprakash on 04/12/17.
 */

public class VisibilityCalculator {

  private final static String TAG = "VisibilityCalculator";
  private RecyclerView recyclerView;
  private LinearLayoutManager layoutManager;
  private final List<VisibilityItem> items = new ArrayList<>();
  private Rect rect = new Rect();

  private boolean started = false;
  private boolean viewComputed = false;

  public VisibilityCalculator(RecyclerView recyclerView,
                              LinearLayoutManager layoutManager) {
    init(recyclerView, layoutManager);
  }

  public VisibilityCalculator() {
  }

  public void init(RecyclerView recyclerView,
                   LinearLayoutManager layoutManager) {
    this.recyclerView = recyclerView;
    this.layoutManager = layoutManager;

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
          case RecyclerView.SCROLL_STATE_IDLE:
            Logger.d(TAG, "*  Scroll: SCROLL_STATE_IDLE");
            computeVisibility();
            break;
          case RecyclerView.SCROLL_STATE_SETTLING:
            Logger.d(TAG, "*  Scroll: SCROLL_STATE_SETTLING");
            computeVisibility();
            break;
        }
      }

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        Logger.d(TAG, "Scrolled : dx->$dx dy->$dy");
        computeVisibility(true, dx, dy);
      }
    });
  }

  private void computeVisibility() {
    computeVisibility(false, 0, 0);
  }

  /**
   * This computes the visibility percentage for all the visibile items of the recycler view one
   * by one.
   */
  private void computeVisibility(boolean isScrollActive, int dx, int dy) {
    if (!started) {
      //Visibility calculator is not marked as started.
      return;
    }
    try {
      int firstVisibleItemIndex = layoutManager.findFirstVisibleItemPosition();
      int lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
      Logger.d(TAG, "** computeVisibility " + firstVisibleItemIndex + " " + lastVisibleItemIndex);

      if (!viewComputed && firstVisibleItemIndex > 0) {
        viewComputed = true;
      }

      markOldItemsInvisible(firstVisibleItemIndex, lastVisibleItemIndex);

      for (int i = firstVisibleItemIndex; i <= lastVisibleItemIndex; i++) {
        if (!isScrollActive) {
          setVisible(i, recyclerView.findViewHolderForAdapterPosition(i));
        } else {
          setScrollingState(i, recyclerView.findViewHolderForAdapterPosition(i), dx, dy);
        }
      }

    } catch (Exception e) {
      Logger.e(TAG, "computeVisibility: ", e);
    }
  }

  /**
   * For the current firstVisibleItemIndex and lastVisibleItemIndex checks if the same item is
   * visible. If not, gives invisible callback.
   *
   * @param firstVisibleItemIndex first visible index
   * @param lastVisibleItemIndex  last visible index
   */
  private void markOldItemsInvisible(int firstVisibleItemIndex, int lastVisibleItemIndex) {
    Logger.d(TAG, "** markOldItemsInvisible " + firstVisibleItemIndex + " " + lastVisibleItemIndex);
    if (CommonUtils.isEmpty(items)) {
      return;
    }
    // mark last visible items invisible
    for (VisibilityItem item : items) {
      // check if same positions are still visible
      if (item == null) {
        Logger.d(TAG, "** item null");
        continue;
      }
      Logger.d(TAG, "** item index " + item.index);

      if (item.index < firstVisibleItemIndex || item.index > lastVisibleItemIndex) {
        // scroll has ended with a different set of items. Deactivate
        item.holder.onInVisible();
        Logger.d(TAG, "*** " + hashCode() + " " + item.holder.hashCode() + " InVisible ");
      }
    }
    Logger.d(TAG, "*  items cleared");
    items.clear();
  }

  private void setScrollingState(int position, RecyclerView.ViewHolder viewHolder, int dx, int dy) {
    if (viewHolder instanceof ScrollAwareViewHolder) {
      items.add(new VisibilityItem(position, (ScrollAwareViewHolder) viewHolder));
      ((ScrollAwareViewHolder) viewHolder).onScrolled(dx, dy);
      Logger.v(TAG,
          "*** " + hashCode() + " " + viewHolder.hashCode() + " Scrolled ");
    }
  }

  private void setVisible(int position, RecyclerView.ViewHolder viewHolder) {
    if (viewHolder instanceof VisibilityAwareViewHolder) {
      int[] a = getVisibilityPercentage(viewHolder.itemView);
      if (a[0] > 0) {
        items.add(new VisibilityItem(position, (VisibilityAwareViewHolder) viewHolder));
        ((VisibilityAwareViewHolder) viewHolder).onVisible(a[0], a[1]);
        Logger.d(TAG,
            "*** " + hashCode() + " " + viewHolder.hashCode()
                + " Visible " + a[0] + "% view: " + a[1] + "%");
      }
    }
  }

  private int[] getVisibilityPercentage(View current) {
    int viewsPortionVisible = 100;
    int screenPercentageTaken;
    int[] displayData = new int[2];

    current.getLocalVisibleRect(rect);
    Logger.v(TAG, "*  getVisibilityPercentage mCurrentViewRect top " + rect.top + ", left " +
        rect.left + ", bottom " + rect.bottom + ", right " + rect.right);

    if (recyclerView.getHeight() <= 0 || recyclerView.getWidth() <= 0) {
      return displayData;
    }

    int height = current.getHeight();
    int width = current.getWidth();
    Logger.v(TAG, "*  getVisibilityPercentage height " + height);
    if (layoutManager != null &&
        layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
      if (rect.left > 0 && width > 0) {
        // view is partially hidden behind the top edge
        viewsPortionVisible = (width - rect.left) * 100 / width;
        screenPercentageTaken = (width - rect.left) * 100 / recyclerView.getWidth();
      } else if (rect.right > 0 && rect.right < width) {
        viewsPortionVisible = rect.right * 100 / width;
        screenPercentageTaken = rect.right * 100 / recyclerView.getWidth();
      } else {
        screenPercentageTaken = width * 100 / recyclerView.getWidth();
      }
      Logger.v(TAG, "*  getVisibilityPercentage, percents " + viewsPortionVisible);
    } else {
      if (rect.top > 0 && height > 0) {
        // view is partially hidden behind the top edge
        viewsPortionVisible = (height - rect.top) * 100 / height;
        screenPercentageTaken = (height - rect.top) * 100 / recyclerView.getHeight();
      } else if (rect.bottom > 0 && rect.bottom < height) {
        viewsPortionVisible = rect.bottom * 100 / height;
        screenPercentageTaken = rect.bottom * 100 / recyclerView.getHeight();
      } else {
        screenPercentageTaken = height * 100 / recyclerView.getHeight();
      }
      Logger.v(TAG, "*  getVisibilityPercentage, percents " + viewsPortionVisible);
    }

    displayData[0] = viewsPortionVisible;
    displayData[1] = screenPercentageTaken;
    return displayData;
  }

  public void computeViewVisibiltyIfNot() {
    if (!viewComputed) {
      computeVisibility();
    }
  }

  /**
   * on the event of screen stop we mark items invisible
   */
  public void stop() {
    markOldItemsInvisible(-1, -1);
    started = false;
  }

  public void start() {
    started = true;
    computeVisibility();
  }

  public void update() {
    if (!started) {
      return;
    }
    computeVisibility();
  }

  public boolean isStarted() {
    return started;
  }

  public void notifyFragmentVisible(final boolean visible) {
    if (!started) {
      return;
    }
    int firstVisibleItemIndex = layoutManager.findFirstVisibleItemPosition();
    int lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
    for (int i = firstVisibleItemIndex; i <= lastVisibleItemIndex; i++) {
      RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
      if (viewHolder instanceof VisibilityAwareViewHolder) {
        if (visible) {
          int[] a = getVisibilityPercentage(viewHolder.itemView);
          ((VisibilityAwareViewHolder) viewHolder).onUserEnteredFragment(a[0], a[1]);
        } else {
          ((VisibilityAwareViewHolder) viewHolder).onUserLeftFragment();
        }
      }
    }
  }
}
