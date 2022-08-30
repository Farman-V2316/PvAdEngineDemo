/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.notification.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.notification.NotificationSectionType;

/**
 * @author bedprakash.rout on 3/22/2016.
 */
public enum NhNotificationParam implements NhAnalyticsEventParam {

  READ_COUNT_NEWS("read_count_news"),
  UNREAD_COUNT_NEWS("unread_count_news"),
  READ_COUNT_BOOKS("read_count_books"),
  UNREAD_COUNT_BOOKS("unread_count_books"),
  READ_COUNT_TESTPREP("read_count_testprep"),
  UNREAD_COUNT_TESTPREP("unread_count_testprep"),

  NOTIFICATION_ID("notification_id"),
  NOTIFICATION_TYPE("notification_type"),
  NOTIFICATION_ACTION("notification_action"),
  NOTIFICATION_FILTER_TYPE("filter_type"),
  NOTIFICATION_FILTER_REASON("filter_reason"),
  NOTIFICATION_DELIVERY_MECHANISM("notification_delivery_mechanism"),
  NOTIFICATION_IS_DEFERRED("isDeferred"),
  NOTIFICATION_LOAD_TIME("load_time"),
  NOTIFICATION_EXPIRY_TIME("expiry_time"),
  NOTIFICATION_DISPLAY_TIME("display_time"),
  NOTIFICATION_PRIORITY("notification_priority"),
  NOTIFICATION_SERVER_REMOVED_TYPE("server_removed_type"),
  NOTIFICATION_CACHED_STATE("is_cached"),
  NOTIFICATION_CHANNEL_ID("notification_channel_id"),

  //TODO(bedprakash.rout): need to move to a common class
  ITEM_ID("item_id"),
  ITEM_SUB_ID("item_sub_id"),
  ITEM_TYPE("item_type"),
  ITEM_LANGUAGE("item_language"),
  ITEM_CATEGORY_ID("item_category_id"),
  ITEM_PUBLISHER_ID("item_publisher_id"),
  PUBLISHER_ID("publisher_id"),
  CATEGORY_ID("category_id"),
  CARD_POSITION("card_position"),
  CARD_TYPE("card_type"),
  TOPIC_ID("topic_id"),
  NOTIFICATION_TIME("notification_time"),
  NUM_GROUPED("num_grouped"),
  DELETE_TYPE("delete_type"),
  NOTIF_TYPE("notif_type"),
  NOTIF_SUBTYPE("notif_subType"),
  NOTIF_DEV_EVENT_SUBTYPE("event_subtype"),
  ADJUNCT_NEWS_LANG("adjunct_newslang"),
  REFERRER_ADJUNCT_NEWSLANG("referrer_adjunct_newslang"),
  NOTIF_UNDELIVERED_REASON("notification_undelivered_reason");


  private String name;

  NhNotificationParam(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public enum CardType {
    NEWS_NOTIFICATION, BOOKS_NOTIFICATION, TESTPREP_NOTIFICATION, APP_NOTIFICATION, TV_NOTIFICATION,
    LIVETV_NOTIFICATION
  }

  public enum CardDeleteType {
    AUTO, USER_TRIGGERED, SWIPE_DELETE
  }

  public static NhNotificationParam.CardType getCardType(NotificationSectionType sectionType) {
    NhNotificationParam.CardType section;
    switch (sectionType) {
      case NEWS:
        section = NhNotificationParam.CardType.NEWS_NOTIFICATION;
        break;
      case TV:
        section = NhNotificationParam.CardType.TV_NOTIFICATION;
        break;
      case LIVETV:
        section = NhNotificationParam.CardType.LIVETV_NOTIFICATION;
        break;
      default:
        section = NhNotificationParam.CardType.APP_NOTIFICATION;
    }
    return section;
  }

}
