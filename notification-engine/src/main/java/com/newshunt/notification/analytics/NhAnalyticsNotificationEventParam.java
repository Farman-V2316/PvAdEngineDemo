package com.newshunt.notification.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * NhAnalyticsNotificationEventParam
 *
 * @author arun.babu
 */
public enum NhAnalyticsNotificationEventParam implements NhAnalyticsEventParam {
  NOTIFICATION_ID("notification_id"),
  NOTIFICATION_TYPE("notification_type"),
  NOTIFICATION_LANGUAGE("notification_language"),
  NOTIFICATION_LAYOUT("notification_layout"),
  ITEM_ID("item_id"),
  ITEM_SUB_ID("item_sub_id"),
  TABITEM_ID("tab_item_id"),
  PROMO_ID("promo_id"),
  CHANNEL_ID("channel_id"),
  PRIORITY("priority"),
  GROUP_ID("group_id"),
  STATE_ENABLED("state_enabled"),
  GROUP_NAME("group_name"),
  CHANNEL_NAME("channel_name"),
  NOTIFICATION_MISS_REASON("notification_miss_reason"),
  ITEM_INDEX("item_index");

  private String name;

  NhAnalyticsNotificationEventParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
