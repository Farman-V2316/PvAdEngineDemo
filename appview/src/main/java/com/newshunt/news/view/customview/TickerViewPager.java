package com.newshunt.news.view.customview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.news.util.NewsConstants;
import com.newshunt.common.view.customview.NHViewPager;

/**
 * ViewPager which doesn't allow manual swiping of viewpager and does auto scroll.
 *
 * @author maruti.borker
 */
public class TickerViewPager extends NHViewPager {

  private static final String LOG_TAG = "TickerViewPager";

  private int tickerNodeCount = 0;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private boolean isCancelled;
  private long tickerSwipeInterval = NewsConstants.DEFAULT_TICKER_INTERVAL;

  public TickerViewPager(Context context) {
    super(context);
  }

  public TickerViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getAdapter() != null) {
      setAutoSlide(getAdapter().getCount());
    }
    Logger.d(LOG_TAG, "onAttachedToWindow");
  }

  @Override
  protected void onDetachedFromWindow() {
    cancelAutoSlide();
    Logger.d(LOG_TAG, "onDetachedFromWindow");
    super.onDetachedFromWindow();
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    Logger.d(LOG_TAG, "hasWindowFocus : " + hasWindowFocus);
    if (!hasWindowFocus) {
      cancelAutoSlide();
    } else if (isCancelled && getAdapter() != null) {
      setAutoSlide(getAdapter().getCount());
    }
  }

  @Override
  public boolean canScrollHorizontally(int direction) {
    if (tickerNodeCount > 1) {
      return super.canScrollHorizontally(direction);
    }
    return false;
  }

  /**
   * Restart ticker from first
   */
  public void scheduleResetPosition() {
    handler.postDelayed(() -> {
      setCurrentItem(0);
    }, 10);
  }

  /**
   * Schedule the slider to move to next item
   *
   * @param numberOfElements - number of elements in adapter
   */
  public void scheduleSlider(final int numberOfElements) {
    if (tickerNodeCount <= 1 || tickerSwipeInterval <= 0) {
      return;
    }
    final Runnable updateSwipe = new Runnable() {
      public void run() {
        int nextItemPos = getNextItem(numberOfElements);
        setCurrentItem(nextItemPos, true);
      }
    };
    cancelAutoSlide();
    handler.postDelayed(updateSwipe, tickerSwipeInterval);
    isCancelled = false;
  }

  /**
   * cancel the auto slide behaviour
   */
  public void cancelAutoSlide() {
    handler.removeCallbacksAndMessages(null);
    isCancelled = true;
  }

  /**
   * Set auto slide to pager
   *
   * @param size - number of elementes present in adapter
   */
  private void setAutoSlide(final int size) {
    scheduleSlider(size);
  }

  /**
   * Get next item index to be set to adapter
   *
   * @param numberOfElements - number of elements in adapter
   * @return index of next item
   */
  private int getNextItem(int numberOfElements) {
    int nextItem = getCurrentItem() + 1;
    if (nextItem > numberOfElements - 1) {
      return 0;
    }
    return nextItem;
  }

  public void setTickerNodeCount(int tickerNodeCount) {
    this.tickerNodeCount = tickerNodeCount;
  }

  public int getTickerNodeCount() {
    return this.tickerNodeCount;
  }

  public void setTickerSwipeInterval(long tickerSwipeInterval) {
    // 0 is valid value, which means never auto swipe
    this.tickerSwipeInterval =
        tickerSwipeInterval < 0 ? NewsConstants.DEFAULT_TICKER_INTERVAL : tickerSwipeInterval;
  }
}
