/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
*/

package com.newshunt.dhutil.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * This class defines the event property params for auto play toggle event
 * Created by srikanth on 08/12/17.
 */

public enum AutoPlayEventParam implements NhAnalyticsEventParam {
  TYPE("type"),
  NEW_STATE("new_state"),
  PREVIOUS_STATE("previous_state"),
  LOW_MEMORY("low_memory"),
  AUTO_PLAY_ALLOWED("auto_play_allowed");

  private String name;

  AutoPlayEventParam(String name) {this.name = name;}

  @Override
  public String getName() {
    return name;
  }
}
