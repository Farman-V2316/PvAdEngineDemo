/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener;


/**
 * Listen to list scrolling event.
 *
 * @author nilesh.borkar
 */
public interface CustomScrollListener {

  void adjustScroll(int scrollHeight, int headerTranslationY);

  void onScroll(int scrollY, int pagePosition);

}
