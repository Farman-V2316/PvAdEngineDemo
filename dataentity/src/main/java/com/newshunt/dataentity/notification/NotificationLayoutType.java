/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * @author santosh.kulkarni
 */
public enum NotificationLayoutType implements Serializable {
  NOTIFICATION_TYPE_SMALL,
  NOTIFICATION_TYPE_BIG_TEXT,
  NOTIFICATION_TYPE_BIG_PICTURE,
  NOTIFICATION_TYPE_BIG_TEXT_INBOX_STYLE,
  NOTIFICATION_TYPE_STICKY_CRICKET,
    NOTIFICATION_TYPE_CREATE_POST,
  NOTIFICATION_TYPE_STICKY_GENERIC,
  NOTIFICATION_TYPE_ADJUNCT_STICKY,
  NOTIFICATION_TYPE_SILENT;
}
