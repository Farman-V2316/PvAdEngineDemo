/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

public class NotificationDismissedEvent {
  public final int id;
  public boolean isClicked;

  public NotificationDismissedEvent(int id, boolean isClicked) {
    this.id = id;
    this.isClicked = isClicked;
  }
}