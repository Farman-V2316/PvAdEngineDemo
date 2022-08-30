/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.app.analytics;

import androidx.annotation.NonNull;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEvents;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper;
import com.newshunt.notification.analytics.NhNotificationParam;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anshul on 31/05/17.
 * A common class for notification  delivery events.
 */

public class NotificationCommonAnalyticsHelper {

  public static void addDisplayAndExpiryParamsToMap(BaseModel baseModel, Map<NhAnalyticsEventParam,
      Object> map) {

    if (baseModel == null) {
      return;
    }

    addDisplayAndExpiryParamsToMap(baseModel.getBaseInfo(), map);

  }

  public static void addDisplayAndExpiryParamsToMap(BaseInfo baseInfo, Map<NhAnalyticsEventParam,
      Object> map) {

    if (baseInfo == null) {
      return;
    }

    if (map == null) {
      map = new HashMap<>();
    }

    long expiryTime = baseInfo.getExpiryTime();
    long displayTime = baseInfo.getV4DisplayTime();

    if (expiryTime > 0) {
      map.put(NhNotificationParam.NOTIFICATION_EXPIRY_TIME, expiryTime);
    }
    if (displayTime > 0) {
      map.put(NhNotificationParam.NOTIFICATION_DISPLAY_TIME, displayTime);
    }
  }

  public static void logInAppNotificationDisplayEvents(@NonNull BaseModel notification){

    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    if(notification.isLoggingNotificationEventsDisabled()){
    return;
  }
    if (notification.isFullSync()) {
    return;
  }
    if (!CommonUtils.isEmpty(notification.getBaseInfo().getId())) {
    map.put(NhAnalyticsAppEventParam.NOTIFICATION_ID, notification.getBaseInfo().getId());
  }
    map.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, Constants.IN_APP);

    if (notification.getBaseInfo().getDeliveryType() != null) {
    map.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM, notification.getBaseInfo().getDeliveryType().name());
  }
    if (notification.getBaseInfo().getV4DisplayTime() > 0) {
    map.put(NhNotificationParam.NOTIFICATION_IS_DEFERRED, true);
  }

    if (notification.getBaseInfo() != null && notification.getBaseInfo().getNotifType() != null) {
    map.put(NhNotificationParam.NOTIF_TYPE, notification.getBaseInfo().getNotifType());
  }

  Map experimentalParamsMap = null;
    if (notification.getBaseInfo() != null) {
    experimentalParamsMap = notification.getBaseInfo().getExperimentParams();
  }

    NotificationCommonAnalyticsHelper.addDisplayAndExpiryParamsToMap(notification, map);
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_DISPLAYED, NhAnalyticsEventSection.NOTIFICATION, map,
  experimentalParamsMap, false);
    AnalyticsClient.flushPendingEvents();
}

}
