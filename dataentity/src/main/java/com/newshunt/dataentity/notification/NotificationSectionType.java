package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * Represents different section types of an notification
 *
 * @author santosh.kulkarni
 */
public enum NotificationSectionType implements Serializable {
  APP(0, "app_section"),
  NEWS(1, "news_section"),
  BOOKS(2, "books_section"),
  TV(4, "tv_section"),
  ADS(5, "ads"),
  WEB(6, "web"),
  SSO(7, "sso"),
  LIVETV(8, "livetv_section"),
  EXPLORE_SECTION(10, "explore_section"),
  FOLLOW_SECTION(11, "follow_section"),
  DEEPLINK_SECTION(12, "deeplink_section"),
  PROFILE_SECTION(14, "profile_section"),
  SOCIAL_SECTION(15, "social_section"),
  GROUP_SECTION(16, "group_section"),
  SEARCH_SECTION(17, "search_section"),
  PERMISSIONS(18, "permissions"),
  LOCAL_SECTION(19, "local_section"),
  SILENT(20, "silent");

  private final int index;
  private final String name;

  NotificationSectionType(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public static NotificationSectionType fromName(String name) {
    for (NotificationSectionType sectionType : NotificationSectionType.values()) {
      if (sectionType.name.equalsIgnoreCase(name)) {
        return sectionType;
      }
    }
    return null;
  }

  public static NotificationSectionType getSectionType(String name) {
    for (NotificationSectionType sectionType : NotificationSectionType.values()) {
      if (sectionType.toString().equalsIgnoreCase(name)) {
        return sectionType;
      }
    }
    return null;
  }

}
