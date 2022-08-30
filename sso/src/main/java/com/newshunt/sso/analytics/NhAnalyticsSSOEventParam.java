/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Events Params required for SSO
 *
 * @author ranjith.suda
 */
public enum NhAnalyticsSSOEventParam implements NhAnalyticsEventParam {

  FAILURE_REASON("failure_reason"),
  NAME_FIELD("name_field"),
  PHONE_FIELD("phone_field"),
  SIGN_IN_METHOD("sign_in_method"),
  SELECTION("selection"),
  SIGNOUT_FLOW("signout_flow"),
  SUCCESS("success");

  private String name;

  NhAnalyticsSSOEventParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
