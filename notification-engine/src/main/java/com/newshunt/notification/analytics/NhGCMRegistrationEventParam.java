/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.notification.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Registration Attempt Event Parameters
 *
 * @author arun.babu
 */
public enum NhGCMRegistrationEventParam implements NhAnalyticsEventParam {
  ITEM_DESTINATION("destination"),
  ITEM_STATUS("status"),
  ITEM_RESPONSE_STATUS("responseStatus"),
  ITEM_MESSAGE("message"),
  ITEM_ATTEMPT_NUMBER("attemptNumber");

  private String name;

  NhGCMRegistrationEventParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}