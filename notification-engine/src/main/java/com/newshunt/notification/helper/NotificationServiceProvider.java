package com.newshunt.notification.helper;

import com.newshunt.notification.model.service.NotificationService;

/**
 * NotificationServiceProvider
 *
 * @author arun.babu
 */
public class NotificationServiceProvider {

  private static NotificationService notificationService;

  public static NotificationService getNotificationService() {
    return notificationService;
  }

  public static void setNotificationService(NotificationService notificationService) {
    NotificationServiceProvider.notificationService = notificationService;
  }
}
