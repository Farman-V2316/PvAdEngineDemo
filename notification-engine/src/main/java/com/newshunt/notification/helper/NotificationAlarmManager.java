/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.notification.R;
import com.newshunt.notification.view.receiver.NotificationAlarmReceiver;

import java.util.Calendar;

/**
 * A manager class for scheduling alarms  for default notifications.
 *
 * @author anshul.jain on 10/21/2016.
 */

public class NotificationAlarmManager {

  private final int requestCode = 1;
  private AlarmManager alarmManager;
  private static long alarmStartedTimeInMillis;

  public NotificationAlarmManager() {
    alarmManager = (AlarmManager) CommonUtils.getApplication().getSystemService(Context.ALARM_SERVICE);
  }

  /**
   * This function will schedule an alarm with either the default time and message or from server.
   *
   * @throws Exception : Generic exception when the scheduling fails
   */
  public void scheduleAlarm() throws Exception {

    if (PullNotificationsDataHelper.isNotificationShown()) {
      return;
    }

    String notificationText = CommonUtils.getString(R.string.default_notification_text);
    //Notice the second argument PendingIntent.FLAG_NO_CREATE, which tells AlarmManager to not
    // create a pendingIntent if it already exists.
    PendingIntent pendingIntent =
        getNotificationPendingIntent(notificationText, PendingIntent.FLAG_NO_CREATE);
    //If the alarm has already been scheduled, then don't schedule a new alarm.
    if (pendingIntent == null) {
      //Note the time when the alarm was scheduled.
      alarmStartedTimeInMillis = System.currentTimeMillis();
      int alarmDuration = Constants.DEFAULT_NOTIFICATION_DURATION;

      pendingIntent = getNotificationPendingIntent(notificationText, 0);
      triggerAlarm(alarmDuration, pendingIntent);
    }
  }

  public void rescheduleAlarm(String message, int alarmDuration) throws Exception {
    PendingIntent pendingIntent =
        getNotificationPendingIntent(message, PendingIntent.FLAG_UPDATE_CURRENT);
    triggerAlarm(alarmDuration, pendingIntent);
  }

  /**
   * This task is used to cancel the alarm.
   *
   * @throws Exception : Generic exception when the scheduling fails
   */
  public void cancelAlarm() throws Exception {
    PullNotificationLogger.logCancelAlarm();
    PullNotificationsDataHelper.saveDefaultNotificationShown(true);
    PendingIntent pendingIntent = getNotificationPendingIntent();
    alarmManager.cancel(pendingIntent);
  }

  /**
   * From Kitkat onwards alarmManager.set does not work at the exact time for battery saving
   * purposes. Therefore use method alarmManager.setExactN
   *
   * @param alarmDuration
   * @param pendingIntent
   */
  private void triggerAlarm(int alarmDuration, PendingIntent pendingIntent) throws Exception {
    if (PullNotificationsDataHelper.isNotificationShown()) {
      return;
    }
    PullNotificationLogger.logScheduleAlarm(alarmDuration);
    long triggerTimeInMillis = getTriggerTimeInMillis(alarmDuration);
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
  }

  /**
   * @return The time in milliseconds at which the alarm should be triggered
   */
  private long getTriggerTimeInMillis(int alarmDuration) throws Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.add(Calendar.MILLISECOND, alarmDuration);
    return calendar.getTimeInMillis();
  }

  /**
   * Server will update the duration in registerAPi
   * If the duration is less from the time which elapsed when alarm was set, set the duration to 0.
   * Otherwise set the duration to the difference between received duration and the time elapsed.
   *
   * @param durationInMillis
   */
  public void updateAlarmParameters(String message, long durationInMillis) throws Exception {

    if (alarmStartedTimeInMillis == 0 || PullNotificationsDataHelper.isNotificationShown()) {
      return;
    }
    PullNotificationLogger.logRescheduleAlarm();

    int alarmDuration = 0;

    long currentTimeInMillis = System.currentTimeMillis();
    long diffInMillis = currentTimeInMillis - alarmStartedTimeInMillis;

    if (diffInMillis < durationInMillis) {
      alarmDuration = (int) (durationInMillis - diffInMillis);
    }

    try {
      rescheduleAlarm(message, alarmDuration);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  private PendingIntent getNotificationPendingIntent() throws Exception {
    Intent intent = new Intent(CommonUtils.getApplication(), NotificationAlarmReceiver.class);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), requestCode, intent, 0);
    return pendingIntent;
  }

  private PendingIntent getNotificationPendingIntent(String message, int flags) throws Exception {
    if (CommonUtils.isEmpty(message)) {
      message = CommonUtils.getString(R.string.default_notification_text);
    }
    Intent intent = new Intent(CommonUtils.getApplication(), NotificationAlarmReceiver.class);
    Bundle bundle = new Bundle();
    bundle.putString(Constants.DEFAULT_NOTIFICATION_TEXT, message);
    intent.putExtras(bundle);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), requestCode, intent, flags);
    return pendingIntent;
  }
}