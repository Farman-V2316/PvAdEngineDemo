/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.view.service;

import android.os.Bundle;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism;
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper;
import com.newshunt.notification.analytics.NhGCMRegistrationAnalyticsUtility;
import com.newshunt.notification.helper.NotificationHandler;
import com.newshunt.notification.helper.NotificationServiceProvider;
import com.newshunt.notification.helper.NotificationSyncHelperKt;

import java.util.Map;

/**
 * Handles intent and broadcasts intent
 *
 * @author santosh.kulkarni
 */
public class FcmIntentService extends FirebaseMessagingService {

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Map<String, String> dataMap = remoteMessage.getData();
    if (CommonUtils.isEmpty(dataMap)) {
      return;
    }

    Bundle bundle = new Bundle();
    for (String key : dataMap.keySet()) {
      bundle.putString(key, dataMap.get(key));
    }

    NotificationHandler.handleNotificationData(NotificationDeliveryMechanism.PUSH, bundle, false,
        NotificationSyncHelperKt.NOTIFICATION_FILTER_ALL);
  }

  @Override
  public void onNewToken(String s) {
    super.onNewToken(s);
    NhGCMRegistrationAnalyticsUtility.updateGcmIdSentEventReported(false);
    //PANDA: removed manually for testing
    //NotificationServiceProvider.getNotificationService().startNotificationService();

    try {
      String refreshedToken = FirebaseInstanceId.getInstance().getToken();
      if (!CommonUtils.isEmpty(refreshedToken)) {
        AppsFlyerHelper.INSTANCE.refreshGCMToken(refreshedToken);
      }
    } catch (Throwable e) {
      Logger.caughtException(e);
    }
  }
}