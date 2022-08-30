/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package android.support.design.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;

import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.newshunt.common.helper.common.LimitedQueue;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.view.customview.CustomNestedScrollView;

/**
 * AppBarLauput behavior customized for Dailyhunt based on
 * http://stackoverflow.com/a/32454407/5485796
 *
 * @author arun.babu
 */
public class NewshuntAppBarLayoutBehavior extends AppBarLayout.Behavior {
  private boolean enableScroll = true;
  private static final float FRICTION = 0.3f;
  private static final Handler HANDLER = new Handler();
  private static final int DY_COUNT_FOR_CONSIDERATION = 3;
  private final LimitedQueue<Integer> dyQueue = new LimitedQueue<>(DY_COUNT_FOR_CONSIDERATION);
  private ValueAnimator mAnimator;

  /*
   * Required for things to work properly. This is called via reflection
   */
  public NewshuntAppBarLayoutBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /*
   * Copied from AppBarLayout.Behavior to repeat mAnimator usage.
   */
  public boolean onStartNestedScroll(
      CoordinatorLayout coordinatorLayout, AppBarLayout child,
      View directTargetChild, View target, int nestedScrollAxes) {
    if (!enableScroll) {
      return false;
    }
    boolean started = (nestedScrollAxes & 2) != 0 && child.getTotalScrollRange() != 0 &&
        coordinatorLayout.getHeight() - target.getHeight() <= child.getHeight();
    if (started && this.mAnimator != null) {
      this.mAnimator.cancel();
    }

    return started;
  }

  /**
   * Fix for the scroll issue for support library version 26.0.2
   * Ref :- https://stackoverflow.com/questions/46452465/android-the-item-inside-recyclerview-cant-be-clicked-after-scroll
   * Ref :- https://gist.github.com/chrisbanes/8391b5adb9ee42180893300850ed02f2
   *
   * @param coordinatorLayout
   * @param child
   * @param target
   * @param dxConsumed
   * @param dyConsumed
   * @param dxUnconsumed
   * @param dyUnconsumed
   * @param type
   */
  @Override
  public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
  }

  @Override
  public void onNestedPreScroll(
      CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    stopNestedScrollIfNeeded(dy, child, target, type);
  }

  private void stopNestedScrollIfNeeded(int dy, AppBarLayout child, View target, int type) {
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      final int currOffset = getTopAndBottomOffset();
      if ((dy < 0 && currOffset == 0)
          || (dy > 0 && currOffset == -child.getTotalScrollRange())) {
        ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
      }
    }
  }

  /*
       * Increment preScroll fields with the scroll
       */
  @Override
  public void onNestedPreScroll(
      CoordinatorLayout coordinatorLayout, AppBarLayout child,
      View target, int dx, int dy, int[] consumed) {
    if (!enableScroll) {
      return ;
    }
    dyQueue.add(dy);
    Logger.d("NewshuntAppBarLayoutBehavior", "onNestedPreScroll dy = " + dy);
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
  }

  /*
   * Overridden to workaround recyclerview wrong fling issue when used with AppBarLayout
   */
  @Override
  public boolean onNestedPreFling(
      CoordinatorLayout coordinatorLayout, AppBarLayout child,
      View target, float velocityX, float velocityY) {
    if (!enableScroll) {
      return false;
    }
    Logger.d("NewshuntAppBarLayoutBehavior", "onNestedFling velocityX = " + velocityX +
        ", velocityY = " + velocityY);

    velocityY = (velocityY != 0) ? velocityY : 1; // Just to avoid divide-by-zero next line
    boolean changed = totalDy() / velocityY < 0; // checking if both negative or positive
    dyQueue.clear();

    if (changed) {
      velocityY = velocityY * -1;
      // onNestedFling to let AppBarLayout finish animation properly
      onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, false);
      // post smoothScrollBy and let it complete animation in correct direction
      postsmoothScrollBy(target, (int) velocityX, (int) velocityY);
      return true;
    }

    return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
  }

  public void showAppBar(CoordinatorLayout coordinatorLayout, AppBarLayout child, boolean show) {
    if (null == coordinatorLayout || null == child) {
      return;
    }

    if (show) {
      animateOffsetTo(coordinatorLayout, child, 0);
    } else {
      animateOffsetTo(coordinatorLayout, child, -child.getTotalScrollRange());
    }
  }

  private void postsmoothScrollBy(final View target, final int velocityX,
                                  final int velocityY) {
    HANDLER.post(new Runnable() {
      @Override
      public void run() {
        Logger.d("NewshuntAppBarLayoutBehavior", "smoothScrollBy");
        if (target instanceof RecyclerView) {
          ((RecyclerView) target).smoothScrollBy(0, (int) (velocityY * FRICTION));
        } else if (target instanceof CustomNestedScrollView) {
          ((CustomNestedScrollView) target).smoothScrollBy(0, (int) (velocityY * FRICTION));
        } else if (target instanceof NestedScrollView) {
          ((NestedScrollView) target).smoothScrollBy(0, (int) (velocityY * FRICTION));
        } else {
          //Do nothing. Let it stop abruptly, but not scroll in wrong direction
        }
      }
    });
  }

  private int totalDy() {
    int result = 0;
    for (int dy : dyQueue) {
      result += dy;
    }

    return result;
  }

  /**
   * Taken from source code of HeaderBehavior class
   * @param parent
   * @param header
   * @param newOffset
   * @param minOffset
   * @param maxOffset
   * @return
   */
  private int setHeaderTopBottomOffset(CoordinatorLayout parent, AppBarLayout header, int newOffset,
                                    int minOffset, int maxOffset) {
    int curOffset = this.getTopAndBottomOffset();
    int consumed = 0;
    if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
      newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
      if (curOffset != newOffset) {
        this.setTopAndBottomOffset(newOffset);
        consumed = curOffset - newOffset;
      }
    }

    return consumed;
  }

  private void animateOffsetTo(final CoordinatorLayout coordinatorLayout, final AppBarLayout child,
                               int offset) {
    if (!enableScroll) {
      return ;
    }
    if (this.mAnimator == null) {
      this.mAnimator = new ValueAnimator();
      this.mAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
      this.mAnimator.addUpdateListener(
          animator -> setHeaderTopBottomOffset(coordinatorLayout, child, (Integer)animator.getAnimatedValue()
              , Integer.MIN_VALUE, Integer.MAX_VALUE));
    } else {
      this.mAnimator.cancel();
    }

    this.mAnimator.setIntValues(this.getTopAndBottomOffset(), offset);
    this.mAnimator.start();
  }

  public void setEnableScroll(boolean enableScroll) {
    this.enableScroll = enableScroll;
  }
}
