/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.notification.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * Registration Event
 *
 * @author arun.babu
 */
public enum NhRegistrationEvent implements NhAnalyticsEvent {

  REGISTRATION_ATTEMPT(false);

  private boolean isPageViewEvent;

  NhRegistrationEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  @Override
  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }
}
