/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.notification.model.entity;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.io.Serializable;

/**
 * A filter type for handling the cancelling of the notification at the client side.
 *
 * @author raunak.yadav
 */
public enum NotificationFilterType implements Serializable {

  DEDUPLICATION("dedup"),
  OLDER_TIMESTAMP("older_timestamp"),
  INVALID_LANGUAGE("lang_invalid"),
  INVALID("invalid"),
  CRASH("crash"),
  EXPIRED("expired"),
  NOTIFICATION_DISABLED_HAMBURGER("notification_disabled_hamburger"),
  NOTIFICATION_DISABLED_SYSTEM("notification_disabled_system"),
  NOTIFICATION_CRICKET_USER_DISABLED("notification_cricket_user_disabled"),
  USER_DISLIKED("user_disliked"),
  USER_READ("user_read"),
  NOTIFICATION_CHANNEL_DISABLED("notification_channel_disabled"),
  NOTIFICATION_GROUP_DISABLED("notification_group_disabled"),
  ACTION_RELEVANCE("action_relevance"),
  NOTIFICATION_SOURCE_BLOCKED("notification_source_blocked");

  private final String value;

  NotificationFilterType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static NotificationFilterType fromFilterType(String filter) {
    for (NotificationFilterType filterType : NotificationFilterType.values()) {
      if (CommonUtils.equals(filterType.value, filter)) {
        return filterType;
      }
    }
    return null;
  }

}