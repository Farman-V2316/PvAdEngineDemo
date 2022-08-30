/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DHConstants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dataentity.notification.StickyNavModelType;
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset;
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset;
import com.newshunt.dataentity.notification.asset.DataStreamResponse;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.R;
import com.newshunt.notification.helper.FGServiceUtilKt;
import com.newshunt.notification.helper.NotificationActionAnalytics;
import com.newshunt.notification.helper.NotificationDefaultChannelHelperKt;
import com.newshunt.notification.helper.StickyNotificationLogger;
import com.newshunt.notification.helper.StickyNotificationServiceUtils;
import com.newshunt.notification.helper.StickyNotificationUtilsKt;
import com.newshunt.notification.helper.StickyNotificationsAnalyticsHelperKt;
import com.newshunt.notification.model.internal.service.DataStreamServiceImpl;
import com.newshunt.notification.model.manager.Trigger;
import com.newshunt.notification.view.builder.NotificationBuilder;
import com.newshunt.notification.view.view.StickyNotificationView;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import androidx.core.app.NotificationCompat;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by anshul on 24/08/17.
 * This class is responsible for handling the scheduling of the events like cricket, football.
 * Should be designed in a way that it can handle multiple events at a time.
 */

public abstract class StickyNotificationService extends Service implements StickyNotificationServiceCallback {

  public static final String TAG = "StickyNotificationService";
  protected StickyNotificationRefresher refresher;
  protected StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> stickyNavModel;
  protected StickyNotificationView stickyNotificationView;
  protected int dummyNotificationId = -1;

  private Disposable disposable;
  private final BroadcastReceiver commonReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent == null || CommonUtils.isEmpty(intent.getAction()) || stickyNavModel == null ||
          stickyNotificationView == null) {
        return;
      }
      String action = intent.getAction();
      String type = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE);
      if(type == null){
        type = Constants.EMPTY_STRING;
      }

      switch (action) {
        case Intent.ACTION_TIME_CHANGED:
        case Intent.ACTION_TIMEZONE_CHANGED:
        case Intent.ACTION_DATE_CHANGED:
          onTimeSettingsChanged();
          break;
        case Intent.ACTION_SCREEN_OFF:
          Logger.d(TAG, "Screen turned OFF, stop the refresher");
          stopRefresher();
          break;

        case Intent.ACTION_SCREEN_ON:
          Logger.d(TAG, "Screen turned ON, start the refresher");
          setupRefresher(false);
          break;

        case ConnectivityManager.CONNECTIVITY_ACTION:
          if (NetworkSDKUtils.isNetworkAvailable(StickyNotificationService.this)) {
            //If its a sticky broadcast, we dont have to dispose and create a new Observable
            if (!isInitialStickyBroadcast()) {
              Logger.d(TAG, "Network available again, start the refresher");
              setupRefresher(false, true, 0);
            }
          } else {
            Logger.d(TAG, "No internet, stop the refresher");
            stopRefresher();
          }
          break;
        default:
          if (CommonUtils.equals(NotificationConstants.INTENT_STICKY_NOTIFICATION_CANCEL_ONGOING,
              action) && CommonUtils.equals(stickyNavModel.getBaseNotificationAsset().getType(), type)) {
            stickyNotificationView.handleAction(action, intent);

            stopStickyNotificationService();
            Trigger trigger =
                (Trigger) intent.getSerializableExtra(
                    NotificationConstants.INTENT_EXTRA_STICKY_NOTIFICATION_CANCEL_TRIGGER);
            if (trigger != null && stickyNavModel != null) {
              StickyNotificationsAnalyticsHelperKt
                  .logStickyNotificationActionEvent(stickyNavModel, trigger.getAction(),
                      System.currentTimeMillis());
            }
          } else if (DHConstants.INTENT_STICKY_AUDIO_COMMENTARY_STATE_CHANGED.equalsIgnoreCase(
              action) && CommonUtils.equals(stickyNavModel.getBaseNotificationAsset().getType(), type)) {
            Logger.d(TAG, "Received intent with action: STICKY_AUDIO_COMMENTARY_STATE_CHANGED");
            if (stickyNotificationView != null) {
              stickyNotificationView.handleAudioChanged(intent);
            }
          } else if (CommonUtils.equals(NotificationConstants.INTENT_ACTION_PLAY_STICKY_AUDIO, action) && CommonUtils.equals(stickyNavModel.getBaseNotificationAsset().getType(), type)) {
            Logger.d(TAG, "INTENT_ACTION_PLAY_STICKY_AUDIO");
            onAudioPlayEvent();
          } else if (CommonUtils.equals(NotificationConstants.INTENT_ACTION_STOP_STICKY_AUDIO, action) && CommonUtils.equals(stickyNavModel.getBaseNotificationAsset().getType(), type)) {
            Logger.d(TAG, "com.eterno.stopStickyAudio");
            onAudioStopEvent();
          } else if (stickyNotificationView != null) {
            stickyNotificationView.handleAction(action, intent);
          }

      }
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
    intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
    intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    intentFilter.addAction(DailyhuntConstants.STICKY_NOTIFICATION_REFRESH_ACTION);
    intentFilter.addAction(DailyhuntConstants.STICKY_NOTIFICATION_CLICK_ACTION);
    intentFilter.addAction(NotificationConstants.INTENT_STICKY_NOTIFICATION_CANCEL_ONGOING);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_PLAY_STICKY_AUDIO);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_STOP_STICKY_AUDIO);
    intentFilter.addAction(DHConstants.INTENT_STICKY_AUDIO_COMMENTARY_STATE_CHANGED);
    intentFilter.addAction(DailyhuntConstants.STICKY_NOTIFICATION_DISMISS_AND_SHOW_ACTION);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_GO_TO_NEXT_ITEM);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_GO_TO_PREV_ITEM);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_NEWS_STICKY_ITEM_CLICK);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_NEWS_STICKY_GO_TO_SETTINGS);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CLEAR_ALL);
    intentFilter.addAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_RECEIVED);
    registerReceiver(commonReceiver, intentFilter);
    Logger.d(TAG, "StickyNotificationService onCreate");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    //start dummy Notification at begining of start service too
    buildDummyNotification(dummyNotificationId);
    if (intent == null || CommonUtils.isEmpty(intent.getAction()) || !intent.getAction().equalsIgnoreCase
        (DailyhuntConstants.STICKY_NOTIFICATION_START_ACTION)) {
      Logger.d(TAG, "stopStickyNotificationService1");
      stopStickyNotificationService();
      return START_NOT_STICKY;
    }

    Logger.d(TAG,
        "StickyNotificationService onStartCommand action = " + intent.getAction() + " flags " +
            flags);

    StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset> prevStickyNavModel = stickyNavModel;

    //Use this action to start the service
    stickyNavModel =
        (StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>)
            intent.getSerializableExtra(NotificationConstants.NOTIFICATION_DATA);

    if (prevStickyNavModel != null &&
        !StickyNotificationUtilsKt.areStickyNotificationsSame(prevStickyNavModel, stickyNavModel)) {
      StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(prevStickyNavModel,
          NotificationActionAnalytics.OVERRIDDEN, System.currentTimeMillis());
      clearPreviousStateForNewNotification(prevStickyNavModel);
    }

    if (stickyNavModel == null || stickyNavModel.getBaseInfo() == null) {
      stopStickyNotificationService();
      return START_NOT_STICKY;
    }

    StickyNotificationRefresher.initStateVariables();
    if(stickyNavModel.getStickyType().equals(StickyNavModelType.NEWS.getStickyType())){
      if(stickyNavModel.getBaseNotificationAsset() != null && stickyNavModel.getBaseNotificationAsset().getAutoRefreshInterval() > 0){
        setupRefresher(false, true, stickyNavModel.getBaseNotificationAsset().getAutoRefreshInterval());
      }else{
        setupRefresher(false, true, PreferenceManager.getPreference(AppStatePreference.NEWS_STICKY_AUTO_REFRESH_INTERVAL, 0));
      }
    }else{
      setupRefresher(true, true, 0);
    }


    stickyNotificationView = inflateNotificationView();
    //If system kills our service, we want the same intent to be delivered to us while restarting
    return START_REDELIVER_INTENT;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Logger.d(TAG, "StickyNotificationService onDestroy");
    try {
      stopRefresher();
      StickyNotificationRefresher.freeStateVariables();
      unregisterReceiver(commonReceiver);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  private void onTimeSettingsChanged() {
    if (stickyNavModel == null || stickyNavModel.getBaseInfo() == null ||
        stickyNavModel.getBaseNotificationAsset() == null) {
      return;
    }

    Logger.d(TAG, "Setting time changed");

    if (stickyNavModel.getBaseInfo().getExpiryTime() <= System.currentTimeMillis()) {
      Logger.d(TAG, "ongoing notification is expired on time settings changed, so stopping " +
          "service");
      stopStickyService(true, false);
    } else if (stickyNavModel.getBaseNotificationAsset().getStartTime() >
        System.currentTimeMillis()) {
      Logger.d(TAG, "ongoing notification's start time has not yet come, so rescheduling the " +
          "notification by firing broadcast");
      StickyNotificationUtilsKt.fireNotificationRescheduledBroadcast(this, stickyNavModel,
          stickyNavModel.getBaseNotificationAsset().getStartTime());
      stopStickyService(false, false);
    }
  }

  private void stopRefresher() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  private void setupRefresher(final boolean userAction, final boolean isNewRefresher, long initialDelay) {
    if (stickyNavModel == null || stickyNavModel.getBaseNotificationAsset() == null) {
      return;
    }
    if (refresher == null || isNewRefresher) {
      refresher = new StickyNotificationRefresher(stickyNavModel.getBaseNotificationAsset(),
          new DataStreamServiceImpl(stickyNavModel.getStickyType()), stickyNavModel);
    }
    stopRefresher();
    Observable<DataStreamResponse> obs = refresher.refresh(userAction, initialDelay);
    disposable = obs.observeOn(AndroidSchedulers.mainThread()).
        subscribeOn(Schedulers.io())
        .subscribe(dataStreamAssetResponse -> {
          if (dataStreamAssetResponse == null || dataStreamAssetResponse.getError() != null) {
            // In case of null response or error response, treat it as error.
            stickyNotificationView.handleStreamDataError(dataStreamAssetResponse);
            return;
          }

          stickyNotificationView.handleStreamDataSuccess(dataStreamAssetResponse);
        }, throwable -> {
          if ((refresher.getExpiryTime() > 0 &&
              System.currentTimeMillis() > refresher.getExpiryTime())) {
            stickyNotificationView.handleStreamDataError(null);
          }
        });
  }

  private void clearPreviousStateForNewNotification(StickyNavModel<BaseNotificationAsset,
      BaseDataStreamAsset> prevStickyNavModel) {
    StickyNotificationRefresher.freeStateVariables();
    stickyNotificationView = null;
    stopRefresher();
    removeNotificationFromTray(prevStickyNavModel);
  }

  private void stopStickyNotificationService() {
    if (stickyNotificationView != null) {
      stickyNotificationView.setServiceCallbackAsNull();
    }
    serviceStopping();
    StickyNotificationRefresher.freeStateVariables();
    stickyNotificationView = null;
    stopRefresher();
    stopSelf();

    removeNotificationFromTray(stickyNavModel);
  }

  @Override
  public void dismissAndReNotifyStickyNotification(){
    stopForeground(true);
    if(stickyNotificationView != null){
      stickyNotificationView.buildNotification(false, false, null);
    }
  }

  private void removeNotificationFromTray(StickyNavModel<BaseNotificationAsset,
      BaseDataStreamAsset> stickyModelToRemove) {
    if (stickyModelToRemove == null) {
      return;
    }

    if (stickyModelToRemove.getBaseInfo() != null) {
      com.newshunt.notification.helper.StickyNotificationServiceUtils
          .removeStickyNotiFromTray(stickyModelToRemove.getBaseInfo().getUniqueId());
    }
    if (stickyModelToRemove.getBaseNotificationAsset() != null) {
      StickyNotificationUtilsKt.cancelNotificationRemoveFromTrayJob(
          stickyModelToRemove.getBaseNotificationAsset().getId(),
          stickyModelToRemove.getStickyType());
    }
  }

  @Override
  public void addNotificationToTray(int notificationId, Notification notification, boolean
      isUpdate) {

    if (notification == null) {
      return;
    }

    Logger.d(TAG, "Adding notification to tray");

    StickyNotificationLogger.stickyNotificationAddedToTray();
    if (isUpdate) {
      NotificationManager notificationManager = (NotificationManager) getSystemService
          (NOTIFICATION_SERVICE);
      CommonUtils.runInBackground(new Runnable() {
        @Override
        public void run() {
          notificationManager.notify(notificationId, notification);
        }
      });
    } else {
      try {
        StickyNotificationUtilsKt.fireNotificationStartedBroadCast(this, stickyNavModel);
        stickyNavModel.setTrayDisplayTime(System.currentTimeMillis());
        if (stickyNavModel != null) {
          StickyNotificationsAnalyticsHelperKt.logStickyNotificationDeliveredEvent(stickyNavModel);
        }
        startForeground(notificationId, notification);
      } catch (SecurityException e) {
        Logger.caughtException(e);
        stickyNotificationView.buildNotification(false, false, null);
      }
    }
  }

  @Override
  public void stopStickyService(boolean isFinished, boolean forceStopped) {
    Logger.d(TAG, "stopStickyService");
    stopStickyNotificationService();
    if (isFinished) {
      StickyNotificationUtilsKt.fireNotificationCompleteBroadcast(this, stickyNavModel);
      StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(stickyNavModel,
          forceStopped ? NotificationActionAnalytics.FORCE_EXPIRE :
              NotificationActionAnalytics.SYSTEM_EXPIRE, System.currentTimeMillis());
    }
  }

  /**
   * A callback method from the view which tells to set up the refresher.
   *
   * @param userAction
   */
  @Override
  public void setupRefresher(boolean userAction) {
    setupRefresher(userAction, false, 0);
  }

  @Override
  public boolean showLiveAudioCommentaryOption() {
    return false;
  }

  @Override
  public void updateAudioCommentary(String audioUrl, String audioLanguage) { }

  protected abstract StickyNotificationView inflateNotificationView();

  protected void serviceStopping() {}

  protected void onAudioPlayEvent() {}

  protected void onAudioStopEvent() {}

  @Override
  public void buildDummyNotification(int id) {

    Logger.d(TAG, "building dummyNotification");
    try{
      dummyNotificationId = id;
      Logger.d(TAG, "is fg service allowed" + FGServiceUtilKt.callStartFgFromOnCreateAndShowDummyNotiForSticky());
      if(FGServiceUtilKt.callStartFgFromOnCreateAndShowDummyNotiForSticky()){
        NotificationDefaultChannelHelperKt.createCustomChannelIfNotExist(NotificationConstants.UPDATES_DEFAULT_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(CommonUtils.getApplication(), NotificationConstants.UPDATES_DEFAULT_CHANNEL_ID)
            .setContentText(CommonUtils.getString(com.newshunt.common.util.R.string.default_foreground_noti_msg))
            .setSmallIcon(R.drawable.app_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW);


        startForeground(dummyNotificationId, notificationBuilder.build());
      }else{
        Logger.d(TAG, "Starting dummy notification is not allowed");
      }
    }catch(Exception ex){
      Logger.d(TAG, "Caught exception while building dummyNotification " + ex.getMessage());
    }
  }
}
