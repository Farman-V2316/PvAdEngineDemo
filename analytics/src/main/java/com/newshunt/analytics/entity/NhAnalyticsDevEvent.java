/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.analytics.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * Class to host common dev events
 * @author: bedprakash.rout on 05/09/17.
 */

public enum NhAnalyticsDevEvent implements NhAnalyticsEvent {

  DEV_SERVER_REQUEST_ERROR,
  DEV_SOCKET_TIMEOUT_ERROR,
  DEV_SERVER_REQUEST_DELAY_ERROR,
  DEV_NETWORK_ERROR_LIMIT_EXCEEDED,
  DEV_DNS_LOOKUP_ERROR,
  DEV_DNS_RECOVERED,
  DEV_CUSTOM_ERROR,
  DEV_NOTIFICATION_PARAMS;

  @Override
  public boolean isPageViewEvent() {
    return false;
  }
}
