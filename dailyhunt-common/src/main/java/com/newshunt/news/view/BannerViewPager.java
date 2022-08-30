/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Created by anshul on 02/03/18.
 * An viewpager for showing carousel full cards.
 */

public class BannerViewPager extends ViewPager {


  private static final String TAG = "BannerViewPager";
  private int noOfItems;
  private long autoRefreshTimeInMillis;
  private int lastSelectedPosition = 1;
  private String lastBannerId;
  private boolean isLastBannerAttached;

  private final Handler handler = new Handler(Looper.getMainLooper());

  public BannerViewPager(Context context) {
    super(context);
  }

  public BannerViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setNoOfItems(int noOfItems) {
    this.noOfItems = noOfItems;
  }

  public void setAutoRefreshTimeInMillis(long autoRefreshTimeInMillis) {
    this.autoRefreshTimeInMillis = autoRefreshTimeInMillis;
  }

  public void setLastBannerId(String lastBannerId) {
    isLastBannerAttached = CommonUtils.equals(lastBannerId, this.lastBannerId);
    this.lastBannerId = lastBannerId;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    Logger.d(TAG, "onAttachedToWindow");
    if (noOfItems > 1 && isLastBannerAttached) {
      setCurrentItem(lastSelectedPosition);
      scheduleAutoSlide(lastSelectedPosition, noOfItems, autoRefreshTimeInMillis);
    } else if (noOfItems > 1) {
      setCurrentItem(1);
    } else if (noOfItems < 0) {
      this.noOfItems = 0;
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    stopAutoSlide();
    Logger.d(TAG, "onDetachedFromWindow");
    super.onDetachedFromWindow();
  }

  public void stopAutoSlide() {
    handler.removeCallbacksAndMessages(null);
  }

  public void scheduleAutoSlide(int position, int noOfItems, long autoRefreshTimeInMillis) {
    Log.d(TAG, "scheduleAutoSlide: " + position);
    if (autoRefreshTimeInMillis > 0 && noOfItems > 1) {
      stopAutoSlide();
      handler.postDelayed(() -> {
        handleSetCurrentItem((position + 1) % noOfItems);
      }, autoRefreshTimeInMillis);
    }
  }

  public void handleSetCurrentItem(final int position) {
    lastSelectedPosition = position;
    final int lastPosition = getAdapter().getCount() - 1;
    if (position == 0) {
      setCurrentItem(lastPosition - 1, false);
    } else if (position == lastPosition) {
      setCurrentItem(1, false);
    } else {
      setCurrentItem(position);
    }
  }
}
