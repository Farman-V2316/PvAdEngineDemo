/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.entity;

/**
 * Created by anshul on 20/12/16.
 */

public enum PullJobFailureReason {
  NO_NETWORK("No Internet"),
  DND_INTERVAL("Job is invoked in DND hours."),
  CURRENT_NETWORK_PULL_NOT_REQUIRED("Pull is cancelled because of the current network"),
  BATTERY_LESS_DEVICE_CHARGING("Battery is less than required"),
  BATTERY_LESS_DEVICE_NON_CHARGING("Battery is less than required and not charging"),
  NOTIFICATIONS_DISABLED_HAMBURGER("Notifications are disabled by the user from Hamburger Menu."),
  NOTIFICATIONS_DISABLED_SERVER("Pull Notifications are disabled from the server"),
  INVALID_SYNC_CONFIGURATION("The sync configuration is invalid.");

  private String reason;

  PullJobFailureReason(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }
}
