/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * All generic events applicable for app as a whole.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsAppEvent implements NhAnalyticsEvent {

  //New values
  APP_START(false),
  APP_EXIT(false),
  SESSION_START(false),
  SESSION_END(false),
  BACKGROUND(false),

  APP_INSTALL(false),
  USER_ACTIVATION(false),
  DEVICE_GOOGLE_IDS(false),

  HTTP_REQUEST_TIMEOUT_ERROR(false),

  SPLASH_PAGE_VIEW(false),

  //New values
  SPLASH_EXPLORE_CLICK(false),
  SPLASH_SIGN_IN_CLICK(false),

  LANGUAGE_SELECTION_VIEW(false),
  EDITION_SELECT(false),
  LANGUAGES_SELECTED(false),
  CONTINUE_BUTTON_CLICKED(false),

  STORY_PAGE_VIEW(true),
  //New values
  STORY_SHARED(false),
  RELATED_STORY_VIEW(false),
  RELATED_STORY_CLICK(false),
  TIMESPENT_PVACTIVITY(false),

  PULL_NOTIFICATION_JOB(false),
  NOTIFICATION_DELIVERED(false),
  NOTIFICATION_DISPLAYED(false),
  //New values
  NOTIFICATION_ACTION(false),

  APP_LANGUAGE_ITEM_CLICKED(false),
  APP_LANGUAGE_SELECTED(false),

  ERROR_AFTER_SPLASH_LOADED(false),
  RELOAD_BUTTON_CLICK(false),
  VIDEO_PLAYED(false),
  ERRORSCREEN_VIEWED(false),
  WEB_ITEM(true),

  EXPLOREBUTTON_CLICK(false),
  EXPLOREBUTTON_VIEWED(false),
  APPSFLYER_INSTALL(false),
  NOTIFICATION_PERMISSION_TOGGLED(false),
  AUTOPLAY_MODE_CHANGED(false),
  STORY_CARD_SEEN(true),
  STORY_CARD_CLICK(false),
  STORY_CARD_VIEW(false),
  USER_ACQUISITION_REFERRERS(false),
  SEARCH_EXECUTED(false),
  SEARCH_INITIATED(false),
  ENTITY_LIST_VIEW(false),
  ENTITY_CARD_VIEW(false),
  ENTITY_CARD_CLICK(false),
  APPSFLYER_INIT(false),
  APPSFLYER_FAILURE(false),
  FIRST_CONTENT_VIEW_CLIENT(false),
  ITEM_DOWNLOAD_STARTED(false),
  ITEM_DOWNLOAD_FAILED(false),
  ITEM_DOWNLOAD_COMPLETE(false),
  FLOATINGICON_ACTION(false),
  VIDEO_SCROLL_UP(true),
  OG_CLICK(true),
  POLL_CLICK(true),
  FG_SESSION_START(false),
  FG_SESSION_END(false),
  PARTNER_WOKEN_UP(false),
  NETWORK_APP_START(false),
  IMPORT_SHOWN(false),
  IMPORT_ACTION(false),
  ACCOUNT_LINK(false),

  ACTIONABLE_ACTIVITY_VIEWED(true),
  ACTIONABLE_ACTIVITY_CLICKED(false),
  ACTIONABLE_ACTIVITY_CLOSED(false),
  ACTIONABLE_ACTIVITY_BLOCKED(false),
  CONTENT_VIEW(true),
  CONTENT_SWIPE(true),
  IN_APP_UPGRADE(false),
  APP_WAKE_UP(false),
  STICKY_NOTIFICATION_NOT_DISPLAYED(false),
  DEFAULT_SHARE_APP_SELECTED(false),
  SNACKBAR_VIEW(false),
  SNACKBAR_ACTION(false),
  DISPLAY_THEME_CHANGED(false),
  NOTIFICATION_TRAY_MANAGEMENT(false),
  IN_APP_NOTIFICATION_NOT_DISPLAYED(false);


  private boolean isPageViewEvent;

  NhAnalyticsAppEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }
}
