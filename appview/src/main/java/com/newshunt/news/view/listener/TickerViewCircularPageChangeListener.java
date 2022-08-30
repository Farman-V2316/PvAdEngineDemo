package com.newshunt.news.view.listener;

import androidx.viewpager.widget.ViewPager;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.news.view.customview.TickerViewPager;

/**
 * Created by ambar on 12/10/16.
 */

public class TickerViewCircularPageChangeListener implements ViewPager.OnPageChangeListener {
  private final TickerViewPager viewPager;
  private int currentPosition;
  private int scrollState;

  public TickerViewCircularPageChangeListener(TickerViewPager viewPager) {
    this.viewPager = viewPager;
    this.viewPager.setCurrentItem(getFirstItem(), false);
  }

  private int getFirstItem() {
    int tickerNodeCount = viewPager.getTickerNodeCount();
    if (tickerNodeCount > 0) {
      int midPoint = Constants.TICKER_MAX_SIZE / 2;
      int tickerAtMidPoint = midPoint % tickerNodeCount;
      int correctedPosition;
      //From mid point, select nearest position which points to 0th ticker index
      if (tickerAtMidPoint > tickerNodeCount / 2) {
        correctedPosition = midPoint + (tickerNodeCount - tickerAtMidPoint);
      } else {
        correctedPosition = midPoint - tickerAtMidPoint;
      }
      return correctedPosition;
    }
    return Constants.TICKER_MAX_SIZE / 2;
  }

  @Override
  public void onPageSelected(int position) {
    currentPosition = position;

    if (position >= Constants.TICKER_MAX_SIZE - 1) {
      viewPager.scheduleResetPosition();
    }
    else {
      viewPager.scheduleSlider(viewPager.getAdapter().getCount());
    }
  }

  @Override
  public void onPageScrollStateChanged(int state) {
    handleScrollState(state);
    scrollState = state;
  }

  private void handleScrollState(int state) {
    if (state == ViewPager.SCROLL_STATE_IDLE) {
      setNextItemIfNeeded();
    }
  }

  private void setNextItemIfNeeded() {
    if (!isScrollStateSettling()) {
      handleSetNextItem();
    }
  }

  private boolean isScrollStateSettling() {
    return scrollState == ViewPager.SCROLL_STATE_SETTLING;
  }

  private void handleSetNextItem() {
    final int lastPosition = viewPager.getAdapter().getCount() - 1;
    if (currentPosition == 0) {
      viewPager.setCurrentItem(lastPosition, false);
    } else if (currentPosition == lastPosition) {
      viewPager.setCurrentItem(0, false);
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    if (viewPager.getParent() != null) {
      viewPager.getParent().requestDisallowInterceptTouchEvent(true);
    }
  }
}
