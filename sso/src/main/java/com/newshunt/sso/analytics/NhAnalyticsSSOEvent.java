/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * SSO Analytics Events across the Application
 *
 * @author ranjith.suda
 */
public enum NhAnalyticsSSOEvent implements NhAnalyticsEvent {

  SIGN_IN_PAGE_VIEW(false),
  SIGN_IN_CLICK(false),
  SIGN_IN_STATUS(false),
  MENU_SIGN_IN(false),
  MENU_SIGN_OUT(false),
  MENU_VIEW(false);

  private boolean isPageViewEvent;

  NhAnalyticsSSOEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }
}
