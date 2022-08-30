/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.view.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.newshunt.analytics.FirebaseAnalyticsHelper;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.app.analytics.NotificationCommonAnalyticsHelper;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.helper.KillProcessAlarmManager;
import com.newshunt.notification.analytics.NhNotificationParam;
import com.newshunt.notification.helper.NotificationClearAllHandler;
import com.newshunt.notification.helper.NotificationDismissedEvent;
import com.newshunt.notification.helper.NotificationPrefetchWorkCancelEvent;
import com.newshunt.notification.helper.NotificationRemoveFromTrayHelper;
import com.newshunt.notification.helper.NotificationServiceProvider;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.notification.sqlite.NotificationDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created specifically to capture notification cancellation event.
 *
 * @author shreyas.desai
 */
public class NotificationDismissedReceiver extends BroadcastReceiver {
  private static final String LOG_TAG = "NotificationDismissedRe";
  @Override
  public void onReceive(Context context, Intent intent) {
    // Santosh : Noticed some crash on dimiss event. . It was happening when you upgraded from
    // DH version to TP version and some old notifications exist. Dismiss event was coming with
    // NavigationModel , where in we were expecting BaseInfo for new versions. So put try catch
    // to handle that situation.

    KillProcessAlarmManager.onAppProcessInvokedInBackground();
    try {
      NotificationClearAllHandler.INSTANCE.onNotificationSwipeDismissed();
      // TODO(Santosh.kulkarni) Check the type whether its News / Books / TestPrep Nav model
      // accordingly post events. Need to confirm only will notificationId works ?
      BaseInfo baseInfo = (BaseInfo) intent.getExtras().getSerializable(
          NotificationConstants.NOTIFICATION_MESSAGE_ID);
      boolean isInbox = intent.getBooleanExtra(NotificationConstants.NOTIFICATION_INBOX, false);
      if (isInbox) {
        CommonUtils.runInBackground(new Runnable() {
          @Override
          public void run() {
            List<BaseModel> groupedNotifications = NotificationDB.instance().getNotificationDao().getGroupedNonDeferredNonStickyNotifications();
            for(int i = 0; i < groupedNotifications.size(); i++){
              BaseModel baseModel = groupedNotifications.get(i);
              NotificationDB.instance().getNotificationPrefetchInfoDao().deleteEntryForNotificationWithId(String.valueOf(baseModel.getBaseInfo().getUniqueId()));
              BusProvider.getRestBusInstance().post(new NotificationPrefetchWorkCancelEvent(String.valueOf(baseModel.getBaseInfo().getUniqueId())));
            }
            NotificationDB.instance().getNotificationDao().markGroupedNotificationAsDeletedFromTray();

          }
        });
      } else {
        CommonUtils.runInBackground(new Runnable() {
          @Override
          public void run() {
            NotificationDB.instance().getNotificationDao().markNotificationAsDeletedFromTray(baseInfo.getUniqueId());
            NotificationDB.instance().getNotificationPrefetchInfoDao().deleteEntryForNotificationWithId(String.valueOf(baseInfo.getUniqueId()));
            BusProvider.getRestBusInstance().post(new NotificationPrefetchWorkCancelEvent(String.valueOf(String.valueOf(baseInfo.getUniqueId()))));
            NotificationRemoveFromTrayHelper.cancelTrayRemovalJobFor(baseInfo.getUniqueId());
          }
        });
      }
      Logger.d(LOG_TAG, "onReceive: Posting "+baseInfo.getUniqueId()+", "+baseInfo.getUniMsg());
      BusProvider.getRestBusInstance().post(new NotificationDismissedEvent(baseInfo.getUniqueId(), false));
      //Analytics Post..
      if (baseInfo != null) {
        Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
        paramsMap.put(NhNotificationParam.NOTIFICATION_ACTION, NhAnalyticsUserAction.DELETE.name());

        if (!CommonUtils.isEmpty(baseInfo.getId())) {
          paramsMap.put(NhAnalyticsAppEventParam.NOTIFICATION_ID, baseInfo.getId());
        }

        if (baseInfo.getLayoutType() != null && !CommonUtils.isEmpty(baseInfo.getLayoutType().name())) {
          paramsMap.put(NhAnalyticsAppEventParam.NOTIFICATION_LAYOUT,
              baseInfo.getLayoutType().name());
        }
        paramsMap.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM, baseInfo
            .getDeliveryType().name());

        int navigationTypeCode = DataUtil.parseInt(baseInfo.getsType(), -1);
        if (navigationTypeCode != -1) {
          NavigationType navigationType = NavigationType.fromIndex(navigationTypeCode);
          if (navigationType != null) {
            paramsMap.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, navigationType.name());
          }
        }

        if (NotificationConstants.NOTIFICATION_TRAY_ID_TO_OPEN_INBOX == baseInfo.getUniqueId()) {
          paramsMap.put(NhNotificationParam.NOTIFICATION_TYPE,
              NotificationConstants.NOTIFICATION_TYPE_GROUP);
        }

        if (baseInfo.isDeferredForAnalytics()) {
          paramsMap.put(NhNotificationParam.NOTIFICATION_IS_DEFERRED, true);
        }

        NotificationCommonAnalyticsHelper.addDisplayAndExpiryParamsToMap(baseInfo, paramsMap);
        Map experimentalParamsMap = baseInfo.getExperimentParams();

        AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_ACTION,
            NhAnalyticsEventSection.NOTIFICATION, paramsMap, experimentalParamsMap, false);
//        FirebaseAnalyticsHelper.INSTANCE.logContentSwipeEvent(new HashMap(paramsMap));
      }

      NotificationServiceProvider.getNotificationService().showSummary();
    } catch (Exception e) {
      Logger.d(LOG_TAG,"Notification Dismiss Receiver Exception");
      Logger.caughtException(e);
    }
  }

}
