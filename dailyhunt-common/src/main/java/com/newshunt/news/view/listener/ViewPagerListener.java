/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener;

/**
 * Created by anshul on 27/02/18.
 * A listener for telling the changed position of the viewpager to its listeners.
 */

public interface ViewPagerListener {

  void onPageSelected(int position);
}
