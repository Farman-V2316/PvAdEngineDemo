/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Specifies menu event params.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsMenuEventParam implements NhAnalyticsEventParam {
  PREVIOUS_LANGUAGE("previous_language"),
  NEW_LANGUAGE("new_language");

  private String name;

  NhAnalyticsMenuEventParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
