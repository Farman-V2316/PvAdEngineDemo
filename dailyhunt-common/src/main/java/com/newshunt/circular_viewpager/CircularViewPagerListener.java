/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.circular_viewpager;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;

import com.newshunt.news.view.BannerViewPager;
import com.newshunt.news.view.listener.ViewPagerListener;

/**
 * Created by anshul on 15/02/18.
 * An listener for circular ViewPager.
 */


public class CircularViewPagerListener implements ViewPager.OnPageChangeListener {

  private BannerViewPager viewPager;
  private TabLayout tabLayout;
  private int noOfItems;
  private long autoRefreshTimeInMillis;
  private static final String TAG = "CircularViewPagerListen";
  private ViewPagerListener viewPagerListener;

  public CircularViewPagerListener(final BannerViewPager viewPager, TabLayout tabLayout,
                                   int noOfItems,
                                   long autoRefreshTimeInMillis,
                                   ViewPagerListener viewPagerListener) {
    this.viewPager = viewPager;
    this.tabLayout = tabLayout;
    this.noOfItems = noOfItems;
    this.autoRefreshTimeInMillis = autoRefreshTimeInMillis;
    this.viewPagerListener = viewPagerListener;

    populateTabs();
  }

  private void populateTabs() {
    if (tabLayout == null) {
      return;
    }
    tabLayout.removeAllTabs();
    for (int i = 0; i < noOfItems; i++) {
      TabLayout.Tab tab = tabLayout.newTab();
      tabLayout.addTab(tab);
    }
  }


  @Override
  public void onPageSelected(final int position) {
    Log.d(TAG, "onPageSelected: " + position);
    if (viewPager == null) {
      return;
    }
    setCurrentTabItemAsSelected(position - 1);
    viewPager.handleSetCurrentItem(position);
    viewPager.scheduleAutoSlide(position, noOfItems, autoRefreshTimeInMillis);
    if (viewPagerListener != null) {
      viewPagerListener.onPageSelected(position - 1);
    }
  }

  private void setCurrentTabItemAsSelected(int pos) {
    Log.d(TAG, "setCurrentTabItemAsSelected: " + pos);
    if (tabLayout == null) {
      return;
    }
    if (pos >= 0 && pos < noOfItems) {
      TabLayout.Tab tab = tabLayout.getTabAt(pos);
      if (tab != null) {
        tab.select();
      }
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
  }

  @Override
  public void onPageScrollStateChanged(int state) {

  }
}
