/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import android.os.Bundle;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.notification.NotificationLayoutType;

import java.util.HashSet;

/**
 * Created by anshul on 10/11/17.
 */

public class NotificationLogger {

  private static final String TAG = "NormalNotificationLogger";

  public static void logDummyServiceCreated() {
    Logger.d(TAG, "The service : DummyNotiForegroundService has been created");
  }

  public static void logDummyServiceDestroyed() {
    Logger.d(TAG, "The service : DummyNotiForegroundService has been destroyed");
  }

  public static void logGeneralNotificationInfo(boolean isNetworkAvailable, boolean
      startServiceForImageDownload, boolean isImageForDownload) {
    Logger.d(TAG, "isNetworkAvailable : " + isNetworkAvailable + " , " +
        "canStartServiceForImageDownload : " + startServiceForImageDownload + " , " +
        "isImageForDownload: " + isImageForDownload);
  }

  public static void logPostStopService(int notificationId) {
    Logger.d(TAG, "Posting a message for stopping the service for notification with " +
        "id " + notificationId);
  }

  public static void logPostHeads(int notificationId) {
    Logger.d(TAG, "Posting a message for headsup for notification with " +
        "id " + notificationId);
  }

  public static void logBuildNotification(int notificationId, NotificationLayoutType layoutType) {
    Logger.d(TAG,
        "Building notification for notification id " + notificationId + " for the layout " +
            "type " + layoutType);
  }

  public static void logAddNotificationToTray(int notificationId, boolean headsUpEnabled) {
    Logger.d(TAG, "add the following notification to tray with notificationId " +
        notificationId + " and which has headsUpEnabled " + headsUpEnabled);
  }

  public static void logAddNotificationWithImage(int notificationId) {
    Logger.d(TAG, "adding notification with image: for notificationId " + notificationId);
  }

  public static void logNotificationImageDownloadFailed(String imageLink) {
    Logger.d(TAG, "image Download failed for the  link" + imageLink);
  }

  public static void logNotificationReceived(Bundle data, long notificationTimeStamp) {
    Logger.d(TAG, "Notification Received: " + notificationTimeStamp + "  BundleData: " +
        data);
  }

  public static void removeNotificationFromHeadsUpHandler(int notificationId) {
    Logger.d(TAG, "Removing the notification " + notificationId + " from the heads up handler");
  }

  public static void logStoppingForegroundService() {
    Logger.d(TAG, "Stopping Foreground sevice");
  }

  public static void logStoppingService() {
    Logger.d(TAG, "Stopping  sevice");
  }

  public static void removeNotificationFromStopServiceHandler(int notificationId) {
    Logger.d(TAG, "Removing the notification " + notificationId + " from the stop service handler");
  }

  public static void logNotificationImageDownloadStart(boolean isBigImage, String imageLink) {
    Logger.d(TAG,
        "Is Big Image ? :" + isBigImage + " Starting download for notification image url " +
            imageLink);
  }

  public static void logNotificationImageDownloadSuccess(String imageLink) {
    Logger.d(TAG, "Download success for notification image url " + imageLink);
  }

  public static void logNotificationHeadsUpTimeElapsed(int duration, int notificationId) {
    Logger.d(TAG, "The time " + duration + " for showing heads up notification" + notificationId +
        " has elapsed. " + "Showing" + "the notification as headsup");
  }

  public static void logNotificationStopServiceTimeElapsed(int duration, int notificationId) {
    Logger.d(TAG, "The time " + duration + " for stopping service for notification" +
        notificationId + " has elapsed. " + "Stopping the service");
  }

  public static void logMarkNotificationAsHeadsUpInDB(int notificationId) {
    Logger.d(TAG, "marked notification as headsUp in the DB for the notificationId " +
        notificationId);
  }

  public static void logIsHeadsUpMarkedInDB(int notificationId, boolean headsUp) {
    Logger.d(TAG,
        "The following notification " + notificationId + " has been marked as headsup " + "in " +
            "DB: " + headsUp);
  }

  public static void logNotRepostingNotification(int notificationId) {
    Logger.d(TAG, "Not reposting the position because it is either read or dismissed or grouped");
  }

  public static void logdownloadImageUrl(HashSet hashSet) {
    Logger.d(TAG, "The images to be downloaded are " + hashSet);
  }
}
