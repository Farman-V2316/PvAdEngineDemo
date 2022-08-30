/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Dialog parameters to be send to analytics server.
 *
 * @author shashikiran.nr on 3/2/2016.
 */
public enum NhAnalyticsDialogEventParam implements NhAnalyticsEventParam {

  ACTION_TAKEN("action_taken"),
  DIALOG_TYPE("dialog_type"),
  TYPE("type"),
  ACTION("action"),
  TRIGGER_ACTION("trigger_action"),
  NEVERSHOW("neverShow"),
  RATING("rating"),
  USER_PROFILE("user_profile");

  private String name;

  NhAnalyticsDialogEventParam(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}
