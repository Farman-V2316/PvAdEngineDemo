/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.customview;

import android.content.Context;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.newshunt.common.helper.share.NHShareView;
import com.newshunt.common.view.customview.NotifyingScrollView;

/**
 * A Parallex Scroll view to not to intercept to horizontal child scroll events, when user is
 * intending scroll horizontally
 *
 * @author santhosh.kc
 */
public class NewsDetailScrollView extends NotifyingScrollView {

  private boolean isToolbarVisible = true;

  private Toolbar toolbar;

  private View shareOptions;

  private boolean isShareOptionVisible;

  private boolean disableHidingActionBar;

  public NewsDetailScrollView(Context context, AttributeSet attrs,
                              int defStyle) {
    super(context, attrs, defStyle);
  }

  public NewsDetailScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NewsDetailScrollView(Context context) {
    super(context);
  }

  public void setShareOptions(View shareOptions) {
    this.shareOptions = shareOptions;
    this.shareOptions.setVisibility(GONE);
  }

  public void hideShareWithAnimation() {
    if (isFloatingView() || !isShareOptionVisible) {
      return;
    }
    isShareOptionVisible = false;
    shareOptions.animate().translationY(0).setInterpolator(
        new AccelerateInterpolator(2));
  }

  public void showShareWithAnimation() {
    if (isShareOptionVisible) {
      return;
    }

    isShareOptionVisible = true;
    shareOptions.animate().translationY(shareOptions.getHeight()).setInterpolator(
        new DecelerateInterpolator(2));
  }

  public void showShareOptions() {
    this.shareOptions.setVisibility(VISIBLE);
  }

  private boolean isFloatingView() {
    return shareOptions instanceof NHShareView && ((NHShareView) shareOptions)
        .isShowSingleShareButton();
  }

  public void setToolbar(Toolbar toolbar) {
    this.toolbar = toolbar;
  }

  @Override
  protected void onScrollChanged(int x, int y, int oldX, int oldY) {
    super.onScrollChanged(x, y, oldX, oldY);

    if (toolbar != null) {
      if (y > oldY && (oldY >= 0)) {
        onBottomToTopSwipe();
      } else if (y + 5 < oldY) {
        onTopToBottomSwipe();
      }
    } else if (toolbar == null) {
      if (y > oldY && (oldY >= 0)) {
        onBottomToTopSwipe();
      } else if (y + 5 < oldY) {
        onTopToBottomSwipe();
      }
    }
  }

  private void hideToolbarWithAnimation() {
    toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(
        new AccelerateInterpolator(2));
  }

  private void showToolbarWithAnimation() {
    toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
  }

  private void onTopToBottomSwipe() {
    if (shareOptions != null) {
      if (shareOptions.getVisibility() == GONE) {
        showShareOptions();
      }
      hideShareWithAnimation();
    }

    if (!isToolbarVisible && toolbar != null && !disableHidingActionBar) {
      isToolbarVisible = true;
      showToolbarWithAnimation();
    }
  }

  private void onBottomToTopSwipe() {
    if (shareOptions != null) {
      showShareWithAnimation();
    }
    if (isToolbarVisible && toolbar != null && !disableHidingActionBar) {
      isToolbarVisible = false;
      hideToolbarWithAnimation();
    }
  }

  public void disableHidingActionBar(boolean disableHidingActionBar) {
    this.disableHidingActionBar = disableHidingActionBar;
    if (disableHidingActionBar && !isToolbarVisible) {
      // If not visible, make it visible now
      isToolbarVisible = true;
      showToolbarWithAnimation();
    }
  }
}
