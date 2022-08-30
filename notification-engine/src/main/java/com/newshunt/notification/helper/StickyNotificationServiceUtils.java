/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.helper;

import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset;
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.model.manager.NotiRemoveFromTrayJobManager;
import com.newshunt.notification.model.manager.StickyNotificationsManager;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anshul on 26/08/17.
 */

public class StickyNotificationServiceUtils {

  private static final String TAG = "StickyNotificationServiceUtils";

  public static void startStickyNotificationService(
      StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> stickyNavModel) {
      if(StickyNotificationServiceUtils.isStickyDisabled(stickyNavModel.getStickyType())){
        Logger.d(TAG, "Sticky of type-" + stickyNavModel.getStickyType() + " was disabled, hence not starting the service");
        return;
      }
      Intent intent = new Intent();
      intent.setPackage(AppConfig.getInstance().getPackageName());
      intent.setAction(DailyhuntConstants.STICKY_NOTIFICATION_START_ACTION);
      intent.setClass(CommonUtils.getApplication(),
              StickyNotificationServiceFactory.INSTANCE.getServiceClass(stickyNavModel.getStickyType()));
      intent.putExtra(NotificationConstants.NOTIFICATION_DATA, stickyNavModel);
      CommonUtils.getApplication().startForegroundService(intent);
      Logger.d(StickyNotificationsManager.TAG, "Started Sticky notification service..");

      handleStickyNotiRemoveFromTray(stickyNavModel);
      StickyNotificationLogger.stickyNotificationServicedStarted();
  }

  public static void handleStickyNotiRemoveFromTray(
      StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> stickyNavModel) {

    if (stickyNavModel.getBaseNotificationAsset() == null) {
      Logger.d(TAG, "not adding service to remove from tray, as base notification asset is null");
      return;
    }

    long expiryTime = stickyNavModel.getBaseNotificationAsset().getExpiryTime();
    if (expiryTime == 0) {
      Logger.d(TAG, "expiry time is 0, so not adding service to remove from tray");
      return;
    }

    int notificationId = stickyNavModel.getBaseInfo().getUniqueId();
    if (expiryTime < System.currentTimeMillis()) {
      removeStickyNotiFromTray(notificationId);
    } else {
      StickyNotificationUtilsKt.addNotificationRemoveFromTrayJob(stickyNavModel);
    }
  }

  public static void removeStickyNotiFromTray(int notificationId) {
    if (notificationId == 0) {
      return;
    }
    NotificationUtils.removeNotificationFromTray(notificationId);
  }

  @NonNull
  public static Intent createStickyServiceIntentWithAction(@NonNull String action) {
    Intent intent = new Intent();
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.setAction(action);
    return intent;
  }

  public static void manageStickyNotification(
      StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> stickyNavModel,
      StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> runningServiceStickyModel) {

    if (stickyNavModel == null || stickyNavModel.getBaseInfo() == null) {
      return;
    }

    if (runningServiceStickyModel == null || runningServiceStickyModel.getBaseInfo() == null) {
      startStickyNotificationService(stickyNavModel);
      return;
    }

    int runningServiceNotificationId = runningServiceStickyModel.getBaseInfo().getUniqueId();
    int storedInDbNotificationId = stickyNavModel.getBaseInfo().getUniqueId();
    StickyNotificationLogger.logStickyInfo(runningServiceNotificationId, storedInDbNotificationId);

    //If the service is already running with the same notificationId, then also start the
    //service with the new payload.
    if (runningServiceNotificationId == storedInDbNotificationId) {
      startStickyNotificationService(stickyNavModel);
      return;
    }

    List<BaseModel> stickyNavModelList = new ArrayList<>();
    stickyNavModelList.add(runningServiceStickyModel);
    stickyNavModelList.add(stickyNavModel);

    StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> appropriateStickyNavModel = stickyNavModel;

    if (appropriateStickyNavModel == null || appropriateStickyNavModel.getBaseInfo() == null) {
      return;
    }

    if (appropriateStickyNavModel.getBaseInfo().getUniqueId() != runningServiceNotificationId) {
      startStickyNotificationService(stickyNavModel);
      NotiRemoveFromTrayJobManager manager = new NotiRemoveFromTrayJobManager();
      manager.cancelJob(runningServiceNotificationId);
    }
  }

  public static boolean isStickyDisabled(String type){
    if(!CommonUtils.isEmpty(type) && ((NotificationConstants.STICKY_CRICKET_TYPE.equalsIgnoreCase(type) &&
        (!PreferenceManager.getPreference(AppStatePreference.CRICKET_STICKY_ENABLED_STATE, true))) || (NotificationConstants.STICKY_GENERIC_TYPE.equalsIgnoreCase(type) &&
        (!PreferenceManager.getPreference(AppStatePreference.ELECTION_STICKY_ENABLED_STATE, true))) || (NotificationConstants.STICKY_NEWS_TYPE.equalsIgnoreCase(type) &&
        (!PreferenceManager.getPreference(AppStatePreference.NEWS_STICKY_ENABLED_STATE, true))))){
      return true;
    }
    return false;
  }

  public static void handleStickyStartStopRelatedGroupedNotificationsUpdate(boolean isOngoing, String type){
    Logger.d(TAG, "handleStickyStartStopRelatedGroupedNotificationsUpdate called for type " + type + " and isOngoing is " + isOngoing);
    if(isOngoing){
      NotificationServiceProvider.getNotificationService().stickyStartLedTrayUpdate();
    }
  }

}
