/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import android.annotation.SuppressLint;
import android.content.Context;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.view.customview.NotificationEnableDialog;

/**
 * Notification enabling dialog helper class.Decides whether to show diaolg or not.
 * method 1): updateAndHandleNotificationEnableDialog is used for reset the preference
 * when ever system notification sets to enable.
 * method 2): canShowNotificationEnableDialog is used for showing dialog upon check done
 * by above method, InApp notification setting and isLastTimeDialogShown status.
 * <p/>
 * // Reference logic link http://stackoverflow.com/a/30108004/3060057
 *
 * @author shashikiran.nr on 1/22/2016.
 */
public class NotificationEnableHelper {

  private static NotificationEnableHelper sInstance = null;

  public static NotificationEnableHelper getsInstance() {
    if (sInstance == null) {
      synchronized (NotificationEnableHelper.class) {
        if (sInstance == null) {
          sInstance = new NotificationEnableHelper();
        }
      }
    }
    return sInstance;
  }

  private NotificationEnableHelper() {
  }

  // Method which gets the notification status.
  @SuppressLint({"NewApi", "WrongConstant"})
  public boolean isNotificationEnabled() {
   return AndroidUtils.areNotificationsEnabled();
  }

  public void updateAndHandleNotificationEnableDialog(Context context) {
    if (isNotificationEnabled()) {
      PreferenceManager.savePreference(
          GenericAppStatePreference.SYSTEM_NOTIFICATION_ENABLE_DIALOG_SHOWN, false);
    }
    if (canShowSystemNotificationEnableDialog()) {
      new NotificationEnableDialog(context).show();
      PreferenceManager.savePreference(
          GenericAppStatePreference.SYSTEM_NOTIFICATION_ENABLE_DIALOG_SHOWN, true);
    }
  }

  private boolean canShowSystemNotificationEnableDialog() {
    boolean isInAppNotificationEnabled =
        PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true);
    boolean isLastTimeDialogShown = PreferenceManager.getPreference(
        GenericAppStatePreference.SYSTEM_NOTIFICATION_ENABLE_DIALOG_SHOWN, false);
    return (isInAppNotificationEnabled && !isNotificationEnabled() && !isLastTimeDialogShown);
  }
}
