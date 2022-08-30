/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.analytics.referrer;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;

/**
 * @author santhosh.kc
 */
public enum NHGenericReferrerSource implements NHReferrerSource {
  /**
   * Notification Tray
   */
  NOTIFICATION_TRAY,
  /**
   * Notification Inbox view
   */
  NOTIFICATION_INBOX_VIEW,
  /**
   * DEEPLINK
   */
  DEEPLINK,
  /**
   * Splash Screen
   */
  SPLASH_VIEW,
  /**
   * Web home - web section
   */
  WEB_SECTION_VIEW,

  NEWS,

  /**
   * Create post screen
   */
  CREATE_POST_VIEW
}
