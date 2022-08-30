/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.analytics;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;

/**
 * @author santhosh.kc
 */
public enum NewsReferrerSource implements NHReferrerSource {
  /**
   * News Detail Activity
   **/
  NEWS_DETAIL_VIEW,
  /**
   * Topics Activity
   **/
  TOPIC_PREVIEW,
  /**
   * Location Activity
   **/
  LOCATION_PREVIEW,
  /**
   * Add Page Activity
   **/
  ADD_PAGE_VIEW,
  /**
   * NewsPaper Activity
   **/
  NEWS_PAPER_VIEW,
  /**
   * News Home Activity
   **/
  NEWS_HOME_VIEW,
  /**
   * Reorder Tabs Activity
   **/
  TABS_REORDER_VIEW,
  /**
   * Saved Articles Activity
   **/
  SAVED_ARTICLES_VIEW,
  /**
   * Source Group Activity
   */
  SOURCE_GROUP_VIEW,
  /**
   * Widget BroadCast Receiver
   */
  WIDGET_BROADCAST_RECEIVER,
  /**
   * Search home activity
   */
  SEARCH_HOME_VIEW,

  VIDEO_DETAIL,

  ENTITY_PREVIEW
}
