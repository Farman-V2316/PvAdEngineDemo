package com.newshunt.notification.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * Created by bedprakash.rout on 3/22/2016.
 */
public enum NhNotificationEvent implements NhAnalyticsEvent {

  INBOX_LIST_VIEW(true), NOTIFICATION_ACTION(false), STORY_CARD_VIEW(false),
  STORY_CARD_CLICK(false), CARD_DELETE(false), NOTIFICATION_GROUPED(false),
  NOTIFICATION_CHANNEL_MISSING(false),
  NOTIFICATION_CHANNEL_STATE_CHANGE(false),
  NOTIFICATION_GROUP_STATE_CHANGE(false),
  NOTIFICATION_CHANNEL_CREATED(false),
  NOTIFICATION_CHANNEL_DELETED(false),
  NOTIFICATION_GROUP_CREATED(false),
  NOTIFICATION_GROUP_DELETED(false),
  NOTIFICATION_CHANNEL_DISABLED(false),
  NOTIFICATION_MISSED(false);

  private boolean isPageViewEvent;

  NhNotificationEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  @Override
  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }
}
