/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.service;

import android.app.Notification;

/**
 * Created by anshul on 18/09/17.
 */

public interface StickyNotificationServiceCallback {

  void addNotificationToTray(int notificationId ,Notification notification, boolean isUpdate);

  void stopStickyService(boolean isFinished, boolean forceStopped);

  void setupRefresher(boolean userAction);

  boolean showLiveAudioCommentaryOption();

  void updateAudioCommentary(String audioUrl, String audioLanguage);

  void dismissAndReNotifyStickyNotification();

  void buildDummyNotification(int id);
}
