/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.view.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.helper.KillProcessAlarmManager;
import com.newshunt.notification.helper.PullNotificationLogger;
import com.newshunt.notification.helper.PullNotificationsDataHelper;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.NotificationSectionType;

/**
 * This class will be invoked by the Alarm Manager when the alarm is set off.
 *
 * @author anshul.jain on 10/21/2016.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver {

  private final String TAG = "DefaultNotification";

  /*
  This method will be called by the AlarmManager framework.
  If the user has selected the languages, return
  Else display the notification hardcoded in the client or coming from the server.
   */
  @Override
  public void onReceive(Context context, Intent intent) {

    KillProcessAlarmManager.onAppProcessInvokedInBackground();

    String userLanguages = AppUserPreferenceUtils.getUserLanguages();
    if (!CommonUtils.isEmpty(userLanguages) || intent == null) {
      PullNotificationsDataHelper.saveDefaultNotificationShown(true);
      return;
    }

    try {
      Bundle bundle = intent.getExtras();
      String notificationText = bundle.getString(Constants.DEFAULT_NOTIFICATION_TEXT);
      createNotification(notificationText);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }


  private void createNotification(String notificationText) throws Exception {
    if (CommonUtils.isEmpty(notificationText)) {
      return;
    }

    PullNotificationLogger.logReceivedDefaultNotification(notificationText);
    PullNotificationsDataHelper.saveDefaultNotificationShown(true);
    final NavigationModel navigationModel = new NavigationModel();
    navigationModel.setMsg(FontHelper.getFontConvertedString(notificationText));
    navigationModel.setNotificationSectionType(NotificationSectionType.APP);
    navigationModel.setLayoutType(NotificationLayoutType.NOTIFICATION_TYPE_SMALL);
    navigationModel.setsType(String.valueOf(NavigationType.SELF_BOARDING.getIndex()));
    BusProvider.postOnUIBus(navigationModel);
  }
}
