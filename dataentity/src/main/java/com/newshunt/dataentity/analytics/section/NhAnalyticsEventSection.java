/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.analytics.section;

import java.io.Serializable;

/**
 * Different places from where events might be generated.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsEventSection implements Serializable{
  APP("app"),
  NEWS("news"),
  BOOKS("books"),
  ADS("ads"),
  Referrer("referrer"),
  NOTIFICATION("notification"),
  TV("tv"),
  UNKNOWN("unknown"),
  MENU("menu"),
  SERVER_NOTIFICATION("server_notification"),
  DEEPLINK("deeplink"),
  VIRAL("viral"),
  FOLLOW("follow"),
  SEARCH("search"),
  PROFILE("profile"),
  GROUP("group");

  private String eventSection;

  NhAnalyticsEventSection(String eventSection) {
    this.eventSection = eventSection;
  }

  public String getEventSection() {
    return eventSection;
  }

  public void setEventSection(String eventSection) {
    this.eventSection = eventSection;
  }

  public static NhAnalyticsEventSection getSection(String section) {
    if (section == null) {
      return UNKNOWN;
    }
    for (NhAnalyticsEventSection appSection : NhAnalyticsEventSection.values()) {
      if (appSection.getEventSection().equalsIgnoreCase(section)) {
        return appSection;
      }
    }
    return UNKNOWN;
  }
}
