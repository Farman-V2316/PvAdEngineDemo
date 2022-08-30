/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.app.view.view;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.newshunt.dataentity.notification.BaseModel;

/**
 * Created by anshul on 18/09/17.
 * An interface for providing communication between NotificationForegroundService and
 * NotificationBuilder.
 */

public interface NotificationServiceCallback {

  void addNotificationWithImageToTray(@NonNull  BaseModel baseModel, @NonNull NotificationCompat.Builder
      notificationBuilder);

  void notificationImageDownloadFailed(String imageLink, int notificationId, @NonNull BaseModel
                                       baseModel);

  void removeUrlFromSet(String imageLink);
}
