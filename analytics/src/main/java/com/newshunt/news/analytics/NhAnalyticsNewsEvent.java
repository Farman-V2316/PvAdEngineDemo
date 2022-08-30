/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * All event types that application supports.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsNewsEvent implements NhAnalyticsEvent {

  TICKER_CLICK(false),
  TICKER_VIEW(false),
  CATEGORY_WEB_ITEM(true),
  SAVELIST_DELETEARTICLE(false),
  STORY_LIST_VIEW(true),

  EXPLOREBUTTON_CLICK(false),
  TABSELECTION_VIEW(false),
  TABITEM_ADDED(false),
  TABITEM_REMOVED(false),
  HOMETABS_REORDERED(false),

  TOPIC_WEB_ITEM(true),
  SUBSCRIBED(false),
  CARD_WIDGET_VIEW(false),
  COLLECTION_PREVIEW_VIEW(false),
  COLLECTION_PREVIEW_CLICK(false),
  FEATURE_NUDGE(false),
  WIDGET_PFP_VIEW(false),
  SIGN_IN_SKIP(false);

  private boolean isPageViewEvent;
  private String eventName;

  NhAnalyticsNewsEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  NhAnalyticsNewsEvent(boolean isPageViewEvent, String name) {
    this.isPageViewEvent = isPageViewEvent;
    this.eventName = name;
  }

  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }

  @Override
  public String toString() {
    return eventName == null ? super.toString() : eventName;
  }
}
