/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.circular_viewpager;

import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by anshul on 15/02/18.
 * An adapter for circular ViewPager.
 */

public abstract class CircularViewPagerAdapter extends PagerAdapter {

  private final int noOfItems;

  public CircularViewPagerAdapter(int noOfItems) {
    this.noOfItems = noOfItems;
  }

  @Override
  public int getCount() {
    return noOfItems > 1 ? noOfItems + 2 : noOfItems;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return object == view;
  }

  protected abstract Object getItem(final ViewGroup container, final int position);

  @Override
  public Object instantiateItem(ViewGroup container, int position) {

    if (noOfItems == 1) {
      return getItem(container, position);
    }

    int calculatedPosition = position - 1;
    if (position == 0) {
      calculatedPosition = noOfItems - 1;
    } else if (position == noOfItems + 1) {
      calculatedPosition = 0;
    }
    return getItem(container, calculatedPosition);
  }

}
