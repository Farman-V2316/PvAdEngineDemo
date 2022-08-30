/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Created by anshul on 20/12/16.
 */

public enum NhPullJobParam implements NhAnalyticsEventParam {

  PULL_SYNC_CONFIG_VERSION("pull_sync_config_version"),
  DEVICE_TIME("device_time"),
  DEVICE_REBOOT_TIME("device_reboot_time"),
  LAST_SUCCESSFUL_PULL_TIME("last_successful_pull_time"),
  LAST_SUCCESSFUL_PUSH_TIME("last_successful_push_time"),
  SCHEDULED_PULL_JOB_TIME("scheduled_pull_job_time"),
  CURRENT_NETWORK("current_network"),
  BATTERY_PERCENT("battery_percent"),
  IS_CHARGING("is_charging"),
  FIRST_TIME_PULL("first_time_pull"),
  NETWORK_AVAILABLBE("network_available"),
  NOTIFICATIONS_ENABLED_HAMBURGER("notifications_enabled_hamburger"),
  NOTIFICATIONS_ENABLED_SERVER("notifications_enabled_server"),
  PULL_JOB_RESULT("pull_job_result"),
  PULL_JOB_FAILURE_REASON("pull_job_failure_reason");

  private String name;

  NhPullJobParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
