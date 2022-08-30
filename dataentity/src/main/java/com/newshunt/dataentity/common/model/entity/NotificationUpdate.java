/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

/**
 * To used in Bus when a new notification is added.
 * It contains the no of unseenNotificationCount in DB.
 *
 * @author bedprakash.rout
 */
public class NotificationUpdate {
  int unseenNotificationCount;

  public NotificationUpdate(int unseenNotificationCount) {
    this.unseenNotificationCount = unseenNotificationCount;
  }

  public int getUnseenNotificationCount() {
    return unseenNotificationCount;
  }
}