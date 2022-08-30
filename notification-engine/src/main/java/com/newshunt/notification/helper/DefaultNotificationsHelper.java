/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import com.newshunt.common.helper.common.Logger;

/**
 * @author anshul.jain on 10/28/2016.
 *         <p>
 *         A helper class used for interacting with {@link NotificationAlarmManager} for scheduling,
 *         rescheduling or cancelling the alarms related to default notifications.
 */

public class DefaultNotificationsHelper {

  /**
   * This method will be called when the user has opened the application and has not selected any
   * languages in 'x' seconds. This will be shown only one time.
   */
  public static void scheduleAlarm() {
    if (PullNotificationsDataHelper.isNotificationShown()) {
      return;
    }
    try {
      NotificationAlarmManager manager = new NotificationAlarmManager();
      manager.scheduleAlarm();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /**
   * This will be called when the user has done onboarding before showing the notification
   */
  public static void cancelAlarm() {
    if (PullNotificationsDataHelper.isNotificationShown()) {
      return;
    }
    NotificationAlarmManager manager = new NotificationAlarmManager();
    try {
      manager.cancelAlarm();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /**
   * This method will be called when server updates the value of message and interval in Register
   * API.
   *
   * @param defaultNotificationMessage: notification text
   * @param interval                    : interval
   */
  public static void rescheduleAlarm(String defaultNotificationMessage,
                                     long interval) {
    if (PullNotificationsDataHelper.isNotificationShown() || interval == 0) {
      return;
    }
    NotificationAlarmManager manager = new NotificationAlarmManager();
    try {
      int intervalInMillils = (int) interval * 1000;
      manager.updateAlarmParameters(defaultNotificationMessage, intervalInMillils);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }
}
