/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.analytics;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.notification.model.entity.PullNotificationJobEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anshul on 20/12/16.
 */

public class PullNotificationJobAnalyticsHelper {

  public static void logPullNotificationJob(PullNotificationJobEvent pullNotificationJobEvent) {
    if (pullNotificationJobEvent == null) {
      return;
    }

    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhPullJobParam.PULL_SYNC_CONFIG_VERSION, pullNotificationJobEvent
        .getPullSyncConfigVersion());
    map.put(NhPullJobParam.DEVICE_TIME, pullNotificationJobEvent.getDeviceTime());
    map.put(NhPullJobParam.DEVICE_REBOOT_TIME, pullNotificationJobEvent.getLastRebootTime());
    map.put(NhPullJobParam.LAST_SUCCESSFUL_PULL_TIME, pullNotificationJobEvent
        .getLastSuccessfulPullSyncTime());
    map.put(NhPullJobParam.LAST_SUCCESSFUL_PUSH_TIME, pullNotificationJobEvent
        .getLastPushNotificationTime());
    map.put(NhPullJobParam.SCHEDULED_PULL_JOB_TIME, pullNotificationJobEvent.getNextPullJobTime());
    map.put(NhPullJobParam.CURRENT_NETWORK, pullNotificationJobEvent.getCurrentNetwork());
    double batteryPercent = 0;
    try {
      batteryPercent = Double.parseDouble(pullNotificationJobEvent.getBatteryPercent());
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    map.put(NhPullJobParam.BATTERY_PERCENT, batteryPercent);
    map.put(NhPullJobParam.IS_CHARGING, pullNotificationJobEvent.isCharging());
    map.put(NhPullJobParam.NETWORK_AVAILABLBE, pullNotificationJobEvent.isNetworkAvailable());
    map.put(NhPullJobParam.NOTIFICATIONS_ENABLED_HAMBURGER, pullNotificationJobEvent
        .isEnabledInHamburger());
    map.put(NhPullJobParam.NOTIFICATIONS_ENABLED_SERVER, pullNotificationJobEvent.isEnableByServer
        ());
    map.put(NhPullJobParam.PULL_JOB_RESULT,
        pullNotificationJobEvent.getPullNotificationJobResult());
    map.put(NhPullJobParam.PULL_JOB_FAILURE_REASON,
        pullNotificationJobEvent.getPullFailureReason());

    if (pullNotificationJobEvent.isFirstTimePull()) {
      map.put(NhPullJobParam.FIRST_TIME_PULL, pullNotificationJobEvent.isFirstTimePull());
    }
    AnalyticsClient.log(NhAnalyticsAppEvent.PULL_NOTIFICATION_JOB,
        NhAnalyticsEventSection.NOTIFICATION, map);
  }
}
