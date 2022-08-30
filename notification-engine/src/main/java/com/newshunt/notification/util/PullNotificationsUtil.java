/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.util;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.Date;

/**
 * Utility class for pull notifications.
 *
 * @author anshul.jain on 10/28/2016.
 */

public class PullNotificationsUtil {

  public static int getDiffInSeconds(Date fromDate, Date toDate) {
    if (fromDate == null || toDate == null) {
      return 0;
    }

    long diffInMillis = toDate.getTime() - fromDate.getTime();
    int diffInSeconds = (int) diffInMillis / 1000;
    return diffInSeconds;
  }

  public static boolean hasJobTimeExpired(Date jobTime) {
    if (jobTime == null) {
      return false;
    }
    return (new Date().compareTo(jobTime) > 0);
  }
}
