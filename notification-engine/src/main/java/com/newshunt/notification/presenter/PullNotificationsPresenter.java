/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.presenter;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.BaseErrorBuilder;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.preference.AppCredentialPreference;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.notification.domain.PullNotificationsUsecaseController;
import com.newshunt.notification.helper.NotificationHandler;
import com.newshunt.notification.helper.NotificationSyncHelperKt;
import com.newshunt.notification.helper.PullNotificationLogger;
import com.newshunt.notification.helper.PullNotificationsDataHelper;
import com.newshunt.notification.helper.PullNotificationsHelper;
import com.newshunt.notification.helper.PullNotificationsJobManager;
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism;
import com.newshunt.notification.model.entity.PullJobFailureReason;
import com.newshunt.notification.model.entity.PullNotificationJobEvent;
import com.newshunt.notification.model.entity.PullNotificationJobResult;
import com.newshunt.notification.model.entity.PullNotificationRequestStatus;
import com.newshunt.notification.model.entity.server.PullNotificationResponse;
import com.newshunt.notification.model.entity.server.PullSyncConfig;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;


/**
 * @author anshul.jain on 10/24/2016.
 * <p>
 * Presenter for sending the request and handling response of pull notifications.
 */

public class PullNotificationsPresenter {

  private final Bus bus;
  private final String NOTIFICATION_DATA_FIELD = "data";

  @Nullable
  private final PullJobStateListener stateListener;

  public PullNotificationsPresenter(Bus bus) {
    this.bus = bus;
    stateListener = null;
  }

  public PullNotificationsPresenter(Bus bus, PullJobStateListener stateListener) {
    this.bus = bus;
    this.stateListener = stateListener;
  }

  public void pullNotifications(PullNotificationJobEvent pullNotificationJobEvent) {

    if (pullNotificationJobEvent == null) {
      pullNotificationJobEvent = new PullNotificationJobEvent();
    }
    pullNotificationJobEvent.setBatteryPercent(CommonUtils.getBatteryPercent());
    pullNotificationJobEvent.setCharging(CommonUtils.isDeviceCharging());
    String connectionTypeInStr = ConnectionInfoHelper.getConnectionType();
    pullNotificationJobEvent.setCurrentNetwork(connectionTypeInStr);

    PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    if (pullSyncConfig != null) {
      pullNotificationJobEvent.setPullSyncConfigVersion(pullSyncConfig.getSyncConfigVersion());
    }
    Date lastSuccessfulPullSync = PullNotificationsDataHelper.getLastSuccessfulSyncedTime();
    pullNotificationJobEvent.setLastSuccessfulPullSyncTime(lastSuccessfulPullSync);
    Date lastPushTimestamp = PullNotificationsDataHelper.getLatestPushNotificationTime();
    pullNotificationJobEvent.setLastPushNotificationTime(lastPushTimestamp);
    Date nextPullJobTime = PullNotificationsDataHelper.getNextJobRunningTime();
    pullNotificationJobEvent.setNextPullJobTime(nextPullJobTime);

    PullNotificationLogger.logPullRequestStarted();

    PullNotificationRequestStatus status = PullNotificationsHelper.requestStatus();
    switch (status) {
      case NO_INTERNET:
        pullNotificationJobEvent.setNetworkAvailable(false);
        pullNotificationJobEvent.setPullFailureReason(PullJobFailureReason.NO_NETWORK);
        break;
      case NOTIFICATIONS_DISABLED_HAMBURGER:
        pullNotificationJobEvent.setEnabledInHamburger(false);
        pullNotificationJobEvent.setPullFailureReason(
            PullJobFailureReason.NOTIFICATIONS_DISABLED_HAMBURGER);
        break;
      case NOTIFICATIONS_DISABLED_SERVER:
        pullNotificationJobEvent.setEnableByServer(false);
        pullNotificationJobEvent.setPullFailureReason(
            PullJobFailureReason.NOTIFICATIONS_DISABLED_SERVER);
        break;
    }

    if (status != PullNotificationRequestStatus.ALLOW) {
      pullNotificationJobEvent.setPullNotificationJobResult(PullNotificationJobResult.PULL_API_NOT_HIT);
      PullNotificationsHelper.logPullNotificationJobEvent(pullNotificationJobEvent);
      try {
        //In case the pull is not successful, we retry it.
        handlePullResponseFailure(BaseErrorBuilder.getBaseError(null, null, null, null));
      } catch (Exception e) {
        Logger.caughtException(e);
      }
      if (stateListener != null) {
        stateListener.onPullJobError();
      }
      return;
    }

    //Register for the bus.
    bus.register(this);
    final PullNotificationJobEvent finalPullNotificationJobEvent = pullNotificationJobEvent;
    //Get all the parameters required to make the request.
    CommonUtils.runInBackground(() -> {
      String salt = PullNotificationsDataHelper.getSalt();
      String syncConfigVersion = null;
      String[] pushNotifications = null;
      PullSyncConfig syncConfig = PullNotificationsDataHelper.getSyncConfiguration();
      if (syncConfig != null) {
        syncConfigVersion = syncConfig.getSyncConfigVersion();
        pushNotifications = PullNotificationsDataHelper.getUnSyncedPushNotificationIds(syncConfig.getMaxPushIds());
      }
      String state = PullNotificationsDataHelper.getState();
      String[] userLanguages = PullNotificationsHelper.getUserLanguages();
      String clientId = AppUserPreferenceUtils.getClientId();
      String gcmId = PreferenceManager.getPreference(AppCredentialPreference.GCM_REG_ID, Constants
              .EMPTY_STRING);

      //Call the controller
      Bus uiBus = BusProvider.getUIBusInstance();
      PullNotificationsUsecaseController controller = new PullNotificationsUsecaseController(
              uiBus, salt, syncConfigVersion, state, pushNotifications, userLanguages,
              clientId, gcmId, finalPullNotificationJobEvent);
      finalPullNotificationJobEvent.setPullNotificationJobResult(PullNotificationJobResult.PULL_API_HIT);
      PullNotificationsHelper.logPullNotificationJobEvent(finalPullNotificationJobEvent);
      controller.execute();
    });
  }

  //Response from the controller.
  @Subscribe
  public void onPullNotificationsResponse(PullNotificationResponse notificationResponse) {

    bus.unregister(this);
    if (notificationResponse == null) {
      return;
    }

    try {
      handlePullNotificationResponse(notificationResponse);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    if (stateListener != null) {
      stateListener.onPullJobComplete();
    }
  }

  private void handlePullNotificationResponse(PullNotificationResponse notificationResponse)
      throws Exception {

    if (notificationResponse.getBaseError() != null) {
      handlePullResponseFailure(notificationResponse.getBaseError());
      if (stateListener != null) {
        stateListener.onPullJobError();
      }
      return;
    }

    //Persist the syncConfiguration in shared preferences.
    PullNotificationsDataHelper.persistSyncConfiguration(notificationResponse.getSyncConfig());
    //Persist the salt in shared preferences.
    PullNotificationsDataHelper.persistSalt(notificationResponse.getSalt());
    //Persist the state in shared preferences.
    PullNotificationsDataHelper.persistState(notificationResponse.getState());
    //Save the current time as the last synced time. This will be helpful for future pull sync
    // requests.
    PullNotificationsDataHelper.persistLastSuccessfulSyncedTime();
    //show the notifications received from the server.
    JsonArray pullNotifications = notificationResponse.getNotifications();
    showNotifications(pullNotifications);
    PullNotificationLogger.logPullRequestResponse(notificationResponse);
    //Schedule the next pull
    PullNotificationsJobManager manager = new PullNotificationsJobManager();
    if (notificationResponse.getBackOffDuration() > 0) {
      PullNotificationLogger.logBackOffInterval(notificationResponse.getBackOffDuration());
      manager.schedule(true/*should change existing job*/, false/*requires battery*/,
          notificationResponse.getBackOffDuration(), false);
    } else {
      manager.schedule(true/*should change existing job*/, false /*requires battery*/);
    }
    CommonUtils.runInBackground(new Runnable() {
      @Override
      public void run() {
        PullNotificationsDataHelper.markAllNotificationsAsSynced();
      }
    });
  }

  private void handlePullResponseFailure(BaseError baseError) throws Exception {
    PullNotificationLogger.logPullResponseError(baseError);
    PullNotificationsJobManager manager = new PullNotificationsJobManager();
    PullSyncConfig syncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    if (syncConfig == null || CommonUtils.isEmpty(syncConfig.getSyncConfigVersion())) {
      int baseInterval = AppConfig.getInstance().getMinBaseInterval();
      manager.scheduleJob(true, false, baseInterval, 0/*tolerance*/, true/*first time pull*/);
    } else {
      manager.schedule(true, false);
    }
    if (stateListener != null) {
      stateListener.onPullJobError();
    }
  }

  private void showNotifications(JsonArray pullNotifications) {
    if (pullNotifications == null) {
      return;
    }
    for (int i = 0; i < pullNotifications.size(); i++) {
      try {
        JsonElement jsonElement = pullNotifications.get(i);
        if (jsonElement == null) {
          continue;
        }
        JsonObject notificationDataJson = jsonElement.getAsJsonObject();
        if (notificationDataJson == null) {
          continue;
        }
        JsonObject notificationJsonObject =
            (JsonObject) notificationDataJson.get(NOTIFICATION_DATA_FIELD);
        Bundle bundle = new Bundle();
        Set<Map.Entry<String, JsonElement>> entries =
            notificationJsonObject.entrySet();
        if (CommonUtils.isEmpty(entries)) {
          return;
        }
        for (Map.Entry<String, JsonElement> entry : entries) {
          String key = entry.getKey();
          JsonElement value = entry.getValue();
          //Based on the answer => http://stackoverflow.com/a/39114357/1237141
          boolean isJsonValueNull = value != null && value.isJsonNull();
          if (key == null || isJsonValueNull) {
            continue;
          }
          try {
            bundle.putString(key, value.getAsString());
          } catch (Exception e) {
            Logger.caughtException(e);
          }
        }
        // saves notification with synced = true.
        NotificationHandler.handleNotificationData(NotificationDeliveryMechanism.PULL,
            bundle, false, NotificationSyncHelperKt.NOTIFICATION_FILTER_ALL);
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }
  }

  /**
   * Listener to use for foreground pull service
   */
  public interface PullJobStateListener {
    /**
     * Pull job complete without any error
     */
    void onPullJobComplete();

    /**
     * Pull job failed
     */
    void onPullJobError();
  }
}
