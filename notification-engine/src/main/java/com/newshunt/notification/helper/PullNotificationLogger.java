/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.notification.model.entity.PullSyncConfigWrapper;
import com.newshunt.notification.model.entity.server.PullNotificationResponse;
import com.newshunt.notification.model.entity.server.PullSyncConfig;

import java.util.Calendar;
import java.util.Date;

/**
 * A class for logging about the job scheduling and request and response of Pull notifications.
 *
 * @author anshul.jain on 10/28/2016.
 */

public class PullNotificationLogger {

  private static final String TAG = "PullNotifications";

  public static void logPullRequestResponse(PullNotificationResponse response) {
    if (response == null) {
      return;
    }
    Logger.d(TAG, "PullNotificationsPresenter - Response from the server for pull notifications " +
        "is " + response.toString());
  }

  public static void logScheduleAlarm(long alarmTimeInMillis) {
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.add(Calendar.SECOND, (int) alarmTimeInMillis / 1000);
      Date date = new Date(calendar.getTimeInMillis());
      Logger.d(TAG, " The default notification will be shown at " + AndroidUtils.dateToString(date));
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static void logRescheduleAlarm() {
    Logger.d(TAG, " The parameters for the default notification have been updated ");
  }

  public static void logCancelAlarm() {
    Logger.d(TAG, " The alarm for showing default notifications has been cancelled. ");
  }

  public static void logReceivedDefaultNotification(String message) {
    try {
      String defaultNotificationMessage = message;
      Date date = new Date();
      Logger.d(TAG, "Received default notification with the following message [ "
          + defaultNotificationMessage + " ] at the following time [ " + AndroidUtils.dateToString(date) + " ] ");
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static void logOnStartJob() {
    Date date = new Date();
    Logger.d(TAG, "PullNotificationJobService : " + "Job Started at " + AndroidUtils.dateToString(date));
  }

  public static void logOnStopJobByFramework() {
    Date date = new Date();
    Logger.d(TAG, "PullNotificationJobService : " + "Job stopped by Android framework at " +
        AndroidUtils.dateToString(date));
  }

  public static void logJobCompleted() {
    Date date = new Date();
    Logger.d(TAG, "PullNotificationJobService : " + "Job completed at " + AndroidUtils.dateToString(date));
  }

  public static void logHamburgerMenuAction(boolean enabled) {
    String state = "disabled";
    if (enabled) {
      state = "enabled";
    }
    Logger.d(TAG, "User " + state + " the notifications from the hamburger Menu");
  }

  public static void logServerEnableResponseForPull(boolean enabled, int firstTimeDelay) {

    if (enabled) {
      Logger.d(TAG, "Server enabled. the notifications. The next job will be schedule which " +
          "cannot replace an existing job");
    } else {
      Logger.d(TAG, "Server disabled the notifications.");
    }
    if (firstTimeDelay != 0) {
      Logger.d(TAG, "Server set the delay for getting the pull notifications for the first time " +
          "to " + firstTimeDelay + "seconds");
    }
  }

  public static void logCancelPullNotificationJob() {
    Logger.d(TAG,
        "The Pull Notification Job with tag [ " + Constants.PULL_NOTIFICATION_JOB_TAG + " ]" +
            "has been cancelled");
  }

  public static void logSkipJobDueToCurrentNetwork() {
    try {
      PullSyncConfig pullSyncConfig = new PullSyncConfig();
      PullSyncConfigWrapper wrapper = new PullSyncConfigWrapper(pullSyncConfig).create();
      logSkipJobDueToCurrentNetwork(wrapper);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static void logSkipJobDueToCurrentNetwork(PullSyncConfigWrapper wrapper) {
    try {
      int syncDuration = wrapper.getCurrentNetworkIntervalInSeconds();
      String lastSyncedTime = PullNotificationsDataHelper.getLastSuccessfulSyncedTime().toString();
      Logger.d(TAG, "The immediate pull request cannot be done The current network is [ " +
          ConnectionInfoHelper.getConnectionType() + " ] " + "and " +
          "the sync interval for this network is [" + syncDuration + " ] seconds. Last synced " +
          "time is [ " + lastSyncedTime + " ] ");
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static void logLastSyncedTime(String dateStr) {
    Logger.d(TAG, " The last sync happened at " + dateStr);
  }

  public static void logPushNotificationReceived() {
    Logger.d(TAG, "Push Notification Received");
  }

  public static void logNotificationJob(PullNotificationJob job) {
    if (job == null) {
      return;
    }
    Logger.d(TAG, job.toString());
  }

  public static void logNextJobAfterPushNotification(int nextSyncInSeconds) {
    Logger.d(TAG, "Skipping any syncs scheduled for " + nextSyncInSeconds + " seconds");
  }

  public static void logPullResponseError(BaseError error) {
    if (error == null || error.getMessage() == null) {
      return;
    }
    Logger.d(TAG, "The pull notification response failed with the following error " + error
        .getMessage());
  }

  public static void logPullNotificationFailed() {
    Logger.d(TAG, "The pull request failed because the conditions for pull notification failed");
  }

  public static void logPullRequestFailedReason(String reason) {
    Logger.d(TAG, "The last job service did not do a pull request because \"" + reason + "\"");
  }

  public static void logPullJobFailed() {
    Logger.d(TAG, "The last job service did not do a pull request because the conditions for " +
        "running job were not met");
  }

  public static void logMaxTimeElapsedForExplicitPull(long maxTimeForSync) {
    Logger.d(TAG, "Last succecsful sync happend at " + PullNotificationsDataHelper
        .getLastSuccessfulSyncedTime());
    Logger.d(TAG, "This push notification will not extend the window for scheduling the jobs " +
        "because max time between syncs is " + maxTimeForSync / 1000 + " seconds");
  }

  public static void logNextRunningJob(Date date) {
    if (date == null) {
      return;
    }
    Logger.d(TAG, "The next job is scheduled at " + AndroidUtils.dateToString(date));
  }

  public static void loginitPullJobOnProcessStart() {
    Logger.d(TAG, " The process for the app has started. This will create a job which cannot " +
        "replace an existing job.");
  }

  public static void logPullSyncConfigNull() {
    Logger.d(TAG, " The pull sync config is null. The next job cannot be scheduled");
  }

  public static void logPullRequestStarted() {
    Logger.d(TAG, "The pull request for getting notifications has started.");
  }

  public static void logFirstTimeSyncConf(long initialFirstDelay) {
    Logger.d(TAG, "The sync config is null");
    if (initialFirstDelay == 0) {
      Logger.d(TAG, "Making the first pull request for getting the sync configuration for the " +
          "first time.");
    } else {
      Logger.d(TAG,
          " Scheduling the job after " + initialFirstDelay + " seconds for the getting the" +
              " sync configuration for the first time.");
    }
  }

  public static void logBackOffInterval(long backOffInterval) {
    Logger.d(TAG,
        " Server sent a backOffInterval of " + backOffInterval + " seconds. Therefore this" +
            " duration will be considered while scheduling the next pull.");
  }
}
