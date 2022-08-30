/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.listener;


/**
 * A callback to find whether analytics event to be fired for a particular card at a position
 * 
 * @author santhosh.kc
 */
public interface CardEventsCallback {
  /**
   * callback to know if analytics event is disabled at a position
   *
   * @param position - position to find where analytics event is disabled
   * @return - true if disabled, else false
   */
  boolean isAnalyticsEventsDisabled(int position);

  boolean fireCardViewEventOnBind(int position);

  boolean showDislikeButton();

  int getDividerColor();
}
