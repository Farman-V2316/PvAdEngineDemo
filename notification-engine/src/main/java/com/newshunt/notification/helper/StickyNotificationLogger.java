/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.helper;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.sdk.network.Priority;

/**
 * Created by anshul on 20/09/17.
 */

public class StickyNotificationLogger {

  private static String TAG = "StickyNotificationLogger";

  public static void stickyNotificationReceived() {
    Logger.d(TAG, "stickyNotificationReceived: ");
  }

  public static void stickyNotificationServicedStarted() {
    Logger.d(TAG, "stickyNotificationServicedStarted: ");
  }

  public static void stickyNotificationAlreadyRemovedFromTray() {
    Logger.d(TAG, "stickyNotificationAlreadyRemovedFromTray. Hence discarding it ");
  }

  public static void stickyNotiAllMatchesDisabledByUser() {
    Logger.d(TAG, "All Matches notifications are disabled by the user. Hence discarding this " +
        "notification" + " ");
  }

  public static void stickyNotificationAddedToTray() {
    Logger.d(TAG, "stickyNotificationAddedToTray: ");
  }

  public static void stickyNotificationAlreadyPosted() {
    Logger.d(TAG,
        "stickyNotificationAlreadyPosted: This notification has already been posted and the service is running. Hence not posting it again.");
  }

  public static void stickyNotificationRemoveFromTrayStart() {
    Logger.d(TAG, "The jobs for removing the sticky notification from the tray has been started ");
  }

  public static void stickyNotificationResponseDelay(long timeInMillis) {
    Logger.d(TAG, "stickyNotificationResponseDelay: " + timeInMillis + " milli seconds ");
  }

  public static void logStickyNotificationCall(String streamUrl, String version,
                                               Priority priority) {
    Logger.d(TAG, "logStickyNotificationRequest: streamUrl  " + streamUrl);
    Logger.d(TAG, "logStickyNotificationRequest: version  " + version);
    Logger.d(TAG, "logStickyNotificationRequest: Priority  " + priority);

  }

  public static void logTrackUrlHit(String streamTrackUrl) {
    Logger.d(TAG, "The following track url for Sticky notifications has been hit" + streamTrackUrl);
  }

  public static void logStickyInfo(int runningServiceNotificationId, int storedInDbNotificationId) {
    Logger.d(TAG,
        "notificationId of the notification already running: " + runningServiceNotificationId);
    Logger.d(TAG, "notificationId of the notification taken from DB " + storedInDbNotificationId);
  }
}
