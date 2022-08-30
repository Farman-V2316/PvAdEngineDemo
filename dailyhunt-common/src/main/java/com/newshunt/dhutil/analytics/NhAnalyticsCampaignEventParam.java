/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Contains event parameters required for different campaigns.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsCampaignEventParam implements NhAnalyticsEventParam {
  ANDROID_ID("android_id"),
  UDID("uuid"),
  UTM_CAMPAIGN("utm_campaign"),
  UTM_SOURCE("utm_source"),
  UTM_MEDIUM("utm_medium"),
  UTM_TERM("utm_term"),
  UTM_CONTENT("utm_content"),
  URLREFERRER("urlreferrer"),
  REFERRER_RAW("referrer_raw"),
  GCM_ID("gcm_id"),
  GOOGLE_AD_ID("google_ad_id"),
  REGISTRATION_SUCCESS("registration_success"),
  NOTIFICATION_STATUS("notification_status");

  private String name;

  NhAnalyticsCampaignEventParam(String name) {
    this.name = name;
  }

  public static NhAnalyticsCampaignEventParam fromName(String name) {
    for (NhAnalyticsCampaignEventParam nhAnalyticsCampaignEventParam :
        NhAnalyticsCampaignEventParam.values()) {
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
