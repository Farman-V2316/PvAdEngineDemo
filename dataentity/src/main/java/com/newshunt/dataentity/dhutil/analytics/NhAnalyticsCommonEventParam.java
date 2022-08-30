/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Contains event parameters required for error logging.
 *
 * @author shrikant.agrawal
 */
public enum NhAnalyticsCommonEventParam implements NhAnalyticsEventParam {
  ERROR_CODE("error_code"),
  ERROR_MESSAGE("error_message"),
  ERROR_ROUTE("error_route"),
  ERROR_URL("error_url"),
  RESPONSE_TIME("response_time"),
  COUNT("count"),
  UNIQUE_ID("uniqueId");

  private String name;

  NhAnalyticsCommonEventParam(String name) {
    this.name = name;
  }

  public static NhAnalyticsCommonEventParam fromName(String name) {
    for (NhAnalyticsCommonEventParam nhAnalyticsCampaignEventParam :
        NhAnalyticsCommonEventParam.values()) {
      if (nhAnalyticsCampaignEventParam.name.equalsIgnoreCase(name)) {
        return nhAnalyticsCampaignEventParam;
      }
    }

    return null;
  }

  public String getName() {
    return name;
  }
}
