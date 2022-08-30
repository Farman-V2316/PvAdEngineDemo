/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.notification.analytics;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.app.analytics.NotificationCommonAnalyticsHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.notification.helper.NotificationEnableHelper;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.notification.model.entity.NotificationFilterType;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Utility Class to send Notification Analytics Events
 *
 * @author bedprakash.rout on 3/22/2016.
 */
public class NhNotificationAnalyticsUtility {

  public static Map<NhAnalyticsEventParam, Object> getCommonNotificationEventParams(final BaseModel
                                                                                        notification) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    if (notification == null) {
      return map;
    }
    map.put(NhNotificationParam.ITEM_ID, notification.getItemId());
    map.put(NhNotificationParam.ITEM_TYPE, null);
    map.put(NhNotificationParam.ITEM_LANGUAGE, null);
    map.put(NhNotificationParam.ITEM_CATEGORY_ID, notification.getCategoryId());
    map.put(NhNotificationParam.ITEM_PUBLISHER_ID, notification.getPublisherId());
    map.put(NhNotificationParam.TOPIC_ID, notification.getTopicId());

    BaseInfo baseInfo = notification.getBaseInfo();
    if (baseInfo != null) {
      map.put(NhNotificationParam.NOTIFICATION_TIME, baseInfo.getTimeStamp());
      map.put(NhNotificationParam.NOTIFICATION_ID, baseInfo.getId());
    }

    if (baseInfo != null && baseInfo.getDeliveryType() != null) {
      map.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM,
          baseInfo.getDeliveryType().name());
    }
    NotificationCommonAnalyticsHelper.addDisplayAndExpiryParamsToMap(notification, map);
    return map;
  }

  public static void logGroupNotificationActionEvent() {
    //TODO(bedprakash.rout): Need to revisit this. Need to make a common class for these common
    //TODO: events for the shared verticals.
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhNotificationParam.NOTIFICATION_ID, NotificationConstants.GROUP_NOTIFICATION_ID);

    map.put(NhNotificationParam.NOTIFICATION_TYPE, NotificationConstants.NOTIFICATION_TYPE_GROUP);
    map.put(NhNotificationParam.NOTIFICATION_ACTION, NhAnalyticsUserAction.CLICK.name());
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_ACTION,
        NhAnalyticsEventSection.NOTIFICATION, map);
  }

  public static void deployCardViewEvent(final BaseModel notification, final int position) {
    AnalyticsHandlerThread.getInstance().post(new Runnable() {
      @Override
      public void run() {
        Map<NhAnalyticsEventParam, Object> map = getCommonNotificationEventParams(notification);
        map.put(NhNotificationParam.CARD_POSITION, position);

        if (notification != null && notification.getBaseInfo() != null &&
            notification.getBaseInfo().getSectionType() != null) {
          map.put(NhNotificationParam.CARD_TYPE, NhNotificationParam
              .getCardType(notification.getBaseInfo().getSectionType()).toString());
        }

        AnalyticsClient.log(NhNotificationEvent.STORY_CARD_VIEW,
            NhAnalyticsEventSection.NOTIFICATION, map,
            new PageReferrer(NhGenericReferrer.NOTIFICATION_INBOX));
      }
    });
  }

  public static void deployCardClick(BaseModel notification, int position) {
    Map<NhAnalyticsEventParam, Object> map = getCommonNotificationEventParams(notification);
    map.put(NhNotificationParam.CARD_POSITION, position);
    int navigationTypeCode = DataUtil.parseInt(notification.getsType(), -1);
    NavigationType navigationType = null;
    if (navigationTypeCode != -1) {
      navigationType = NavigationType.fromIndex(navigationTypeCode);
    }

    String cardType;
    if (navigationType != null) {
      cardType = navigationType.name();
    } else {
      cardType = NhNotificationParam
          .getCardType(notification.getBaseInfo().getSectionType()).toString();
    }
    map.put(NhNotificationParam.CARD_TYPE, cardType);
    AnalyticsClient.log(NhNotificationEvent.STORY_CARD_CLICK, NhAnalyticsEventSection.NOTIFICATION,
        map, new PageReferrer(NhGenericReferrer.NOTIFICATION_INBOX));

  }

  public static void deployCardDeleteEvent(BaseModel notification) {
    Map<NhAnalyticsEventParam, Object> map = getCommonNotificationEventParams(notification);
    map.put(NhNotificationParam.CARD_TYPE, NhNotificationParam
        .getCardType(notification.getBaseInfo().getSectionType()).toString());
    map.put(NhNotificationParam.DELETE_TYPE, NhNotificationParam.CardDeleteType.USER_TRIGGERED);
    //TODO(bedprakash.rout): Not keeping track of position. Need to talk to rajeev
//    map.put(NhNotificationParam.CARD_POSITION, cardPos);

    AnalyticsClient.log(NhNotificationEvent.CARD_DELETE, NhAnalyticsEventSection.NOTIFICATION,
        map, new PageReferrer(NhGenericReferrer.NOTIFICATION_INBOX));
  }

  public static void deployNotificationGroupedEvent(int size) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhNotificationParam.NUM_GROUPED, size);
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_GROUPED,
        NhAnalyticsEventSection.NOTIFICATION,
        map);
  }


  /**
   * method for notification Screen view event.
   */
  public static void deployNotificationInboxViewEvent(int newsCountRead,
                                                      int newsCountUnread,
                                                      @Nullable
                                                          Map<NhAnalyticsEventParam, Object> paramsMap,
                                                      @Nullable
                                                          PageReferrer pageReferrer) {
    if (paramsMap == null) {
      paramsMap = new HashMap<>();
    }
    paramsMap.put(NhNotificationParam.READ_COUNT_NEWS, newsCountRead);
    paramsMap.put(NhNotificationParam.UNREAD_COUNT_NEWS, newsCountUnread);
    AnalyticsClient.log(NhNotificationEvent.INBOX_LIST_VIEW,
        NhAnalyticsEventSection.NOTIFICATION, paramsMap, pageReferrer);
  }

  /**
   * Analytics for filtered notifications.
   *
   * @param notification
   * @param filterType
   */
  public static void deployNotificationFilteredEvent(final BaseModel notification,
                                                     NotificationFilterType filterType) {
    if(notification != null && notification.isLoggingNotificationEventsDisabled()){
      return;
    }
    Map<NhAnalyticsEventParam, Object> map = getCommonNotificationEventParams(notification);
    map.put(NhNotificationParam.NOTIFICATION_ACTION, NhAnalyticsUserAction.CLIENT_FILTER.name());
    if (filterType != null) {
      map.put(NhNotificationParam.NOTIFICATION_FILTER_TYPE, filterType.getValue());
    }
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_ACTION,
        NhAnalyticsEventSection.NOTIFICATION, map);
  }

  /**
   * Analytics for filtered notifications.
   *
   * @param filterType
   */
  public static void deployNotificationFilteredEvent(NotificationFilterType filterType) {
    deployNotificationFilteredEvent(filterType, Constants.EMPTY_STRING);
  }

  /**
   * A utility method to log analytics for filtered notifications with the filtered reason.
   */
  public static void deployNotificationFilteredEvent(NotificationFilterType filterType, String
      filterReason) {
    deployNotificationFilteredEvent(filterType, filterReason, null);
  }

  /**
   * A utility method to log analytics for filtered notifications with the filtered reason.
   */
  public static void deployNotificationFilteredEvent(NotificationFilterType filterType, String
      filterReason, BaseModel baseModel) {
    if(baseModel != null && baseModel.isLoggingNotificationEventsDisabled()){
      return;
    }
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    if (baseModel != null && baseModel.getBaseInfo() != null) {
      map = getCommonNotificationEventParams(baseModel);
    }
    map.put(NhNotificationParam.NOTIFICATION_ACTION, NhAnalyticsUserAction.CLIENT_FILTER.name());
    map.put(NhNotificationParam.NOTIFICATION_FILTER_TYPE, filterType.getValue());
    if (!CommonUtils.isEmpty(filterReason)) {
      map.put(NhNotificationParam.NOTIFICATION_FILTER_REASON, filterReason);
    }
    AnalyticsClient.log(NhNotificationEvent.NOTIFICATION_ACTION,
        NhAnalyticsEventSection.NOTIFICATION, map);
  }

  public static void fireSystemNotificationBlockedIfApplicable(BaseModel baseModel) {
    if (baseModel == null) {
      return;
    }
    if (!NotificationEnableHelper.getsInstance().isNotificationEnabled()) {
      deployNotificationFilteredEvent(baseModel,
          NotificationFilterType.NOTIFICATION_DISABLED_SYSTEM);
    }
  }
}
