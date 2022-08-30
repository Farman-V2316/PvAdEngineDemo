/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper;

import com.google.gson.Gson;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.notification.model.entity.server.PullSyncConfig;
import com.newshunt.notification.sqlite.NotificationDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A helper class for storing all the shared preferences related to pull notifcations.
 *
 * @author anshul.jain on 10/26/2016.
 */

public class PullNotificationsDataHelper {

  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");


  public static final String PULL_NOTIFICATION_SYNC_CONFIG = "pullNotificationSyncConfig";
  public static final String PULL_NOTIFICATION_SALT = "pullNotificationSalt";
  public static final String PULL_NOTIFICATIONS_LAST_SUCCESSFUL_SYNCED_TIME =
      "pullNotificationsLastSuccessfulSyncedTime";
  public static final String LAST_PUSH_NOTIFICATION_TIMESTAMP = "LAST_PUSH_NOTIFICATION_TIMESTAMP";
  public static String PRE_SCHEDULED_NOTIFICATION_SHOWN = "pullDefaultNotificationShown";
  public static final String PULL_NOTIFICATIONS_ENABLED_BY_SERVER =
      "pullNotificationsEnabledByServer";
  public static final String PULL_NEXT_JOB_RUNNING_TIME = "pullNextJobRunningTime";
  public static final String PULL_NOTIFICATION_STATE = "pullNotificationState";
  public static final String PULL_NOTIFICATION_FIRST_TIME_DELAY = "firstTimePullDelay ";

  /**
   * Method for saving sync configuration parameters to shared preferences.
   *
   * @param syncConfiguration : an object of {@link PullSyncConfig}
   */
  public static void persistSyncConfiguration(PullSyncConfig syncConfiguration) throws Exception {
    if (syncConfiguration == null || CommonUtils.isEmpty(syncConfiguration.getSyncConfigVersion())) {
      return;
    }
    Gson gson = new Gson();
    String pullSyncConfigurationStr = gson.toJson(syncConfiguration);

    if (CommonUtils.isEmpty(pullSyncConfigurationStr)) {
      return;
    }
    PreferenceManager.saveString(PULL_NOTIFICATION_SYNC_CONFIG, pullSyncConfigurationStr);
  }

  /**
   * This method will reach a string from shared preferences and convert it into an object of
   * {@link PullSyncConfig}
   *
   * @return
   */
  public static PullSyncConfig getSyncConfiguration() {

    String pullSyncConfigurationStr = PreferenceManager.getString(PULL_NOTIFICATION_SYNC_CONFIG);
    if (CommonUtils.isEmpty(pullSyncConfigurationStr)) {
      return null;
    }

    Gson gson = new Gson();
    PullSyncConfig configuration = null;
    try {
      configuration = gson.fromJson(pullSyncConfigurationStr,
          PullSyncConfig.class);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return configuration;
  }

  public static void persistSalt(String salt) {
    PreferenceManager.saveString(PULL_NOTIFICATION_SALT, salt);
  }

  public static String getSalt() {
    return PreferenceManager.getString(PULL_NOTIFICATION_SALT, Constants.EMPTY_STRING);
  }

  public static void persistFirstTimePullDelay(int firstTimePullDelay) {
    PreferenceManager.saveInt(PULL_NOTIFICATION_FIRST_TIME_DELAY, firstTimePullDelay);
  }

  public static int getFirstTimeFullDelay() {
    return PreferenceManager.getInt(PULL_NOTIFICATION_FIRST_TIME_DELAY, 0);
  }

  public static void persistState(String state) {
    PreferenceManager.saveString(PULL_NOTIFICATION_STATE, state);
  }

  public static String getState() {
    return PreferenceManager.getString(PULL_NOTIFICATION_STATE, Constants.EMPTY_STRING);
  }

  public static void persistLastSuccessfulSyncedTime() {
    Date date = new Date();
    try {
      String dateStr = sdf.format(date);
      PreferenceManager.saveString(PULL_NOTIFICATIONS_LAST_SUCCESSFUL_SYNCED_TIME, dateStr);
      PullNotificationLogger.logLastSyncedTime(dateStr);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static Date getLastSuccessfulSyncedTime() {
    String dateStr = PreferenceManager.getString(PULL_NOTIFICATIONS_LAST_SUCCESSFUL_SYNCED_TIME);
    Date date = null;
    try {
      date = sdf.parse(dateStr);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return date;
  }

  public static void persistLatestPushNotificationTime() {
    Date date = new Date();
    try {
      String dateStr = sdf.format(date);
      PreferenceManager.saveString(LAST_PUSH_NOTIFICATION_TIMESTAMP, dateStr);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static Date getLatestPushNotificationTime() {
    String dateStr = PreferenceManager.getString(LAST_PUSH_NOTIFICATION_TIMESTAMP);
    Date date = null;
    try {
      date = sdf.parse(dateStr);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return date;
  }

  public static String[] getUnSyncedPushNotificationIds(int limit) {
    List<String> ids = NotificationDB.instance().getNotificationDao().getUnsyncedNotificationIdList(limit);
    return ids.toArray(new String[ids.size()]);
  }

  public static void markAllNotificationsAsSynced() {
    NotificationDB.instance().getNotificationDao().markAllNotificationsAsSynced();
  }

  public static boolean isNotificationShown() {
    return PreferenceManager.getBoolean(PRE_SCHEDULED_NOTIFICATION_SHOWN, false);
  }

  public static void saveDefaultNotificationShown(boolean isShown) {
    PreferenceManager.saveBoolean(PRE_SCHEDULED_NOTIFICATION_SHOWN, isShown);
  }

  public static void savePullNotificationsEnabled(boolean pullNotificationsEnabled) {
    PreferenceManager.saveBoolean(PULL_NOTIFICATIONS_ENABLED_BY_SERVER, pullNotificationsEnabled);
  }

  public static boolean arePullNotificationsEnabled() {
    return PreferenceManager.getBoolean(PULL_NOTIFICATIONS_ENABLED_BY_SERVER, false);
  }

  public static void saveNextJobRunningTime(Date date) {
    try {
      String str = sdf.format(date);
      PreferenceManager.saveString(PULL_NEXT_JOB_RUNNING_TIME, str);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static Date getNextJobRunningTime() {
    String str = PreferenceManager.getString(PULL_NEXT_JOB_RUNNING_TIME, Constants.EMPTY_STRING);
    try {
      return sdf.parse(str);
    } catch (ParseException e) {
      try {
        SimpleDateFormat oldSdf = new SimpleDateFormat("dd/MMM/yyyy HH:mm");
        Date date = oldSdf.parse(str);
        saveNextJobRunningTime(date);
        return date;
      } catch (ParseException e1) {
        Logger.caughtException(e);
      }
      Logger.caughtException(e);
    }
    return null;
  }
}
