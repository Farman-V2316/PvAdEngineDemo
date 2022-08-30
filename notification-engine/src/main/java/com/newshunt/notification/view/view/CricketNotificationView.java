/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.view;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.transition.Transition;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset;
import com.newshunt.dataentity.notification.asset.DataStreamResponse;
import com.newshunt.notification.model.entity.server.StickyAudioCommentary;
import com.newshunt.dataentity.notification.asset.TeamAsset;
import com.newshunt.notification.R;
import com.newshunt.notification.helper.NotificationActionAnalytics;
import com.newshunt.notification.helper.NotificationUtils;
import com.newshunt.notification.helper.StickyNotificationServiceUtils;
import com.newshunt.notification.helper.StickyNotificationUtilsKt;
import com.newshunt.notification.helper.StickyNotificationsAnalyticsHelperKt;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.notification.view.builder.StickyNotificationLayoutBuilder;
import com.newshunt.notification.view.service.StickyNotificationRefresher;
import com.newshunt.notification.view.service.StickyNotificationServiceCallback;
import com.newshunt.sdk.network.image.Image;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.newshunt.dhutil.helper.common.DailyhuntConstants.STICKY_NOTIFICATION_REFRESH_ACTION;

/**
 * Created by anshul on 18/09/17.
 */

public class CricketNotificationView implements StickyNotificationView {

  private static final int REQ_CODE_REFRESH = 1001;
  private static final int REQ_CODE_FINISH = 1002;
  private static final int REQ_CODE_DEEPLINK = 1003;
  private static final int REQ_CODE_PLAY = 1004;
  private static final int REQ_CODE_STOP = 1005;
  private static Integer brandingCount = 0;
  private StickyNavModel cricketStickyNavModel;
  private Bitmap team1FlagBitmap, team2FlagBitmap;
  private String TAG = "StickyNotificationService";
  private StickyNotificationRefresher refresher;
  private StickyNotificationServiceCallback callback;
  private HashMap<String, Bitmap> brandingCache = new HashMap<String, Bitmap>();

  public CricketNotificationView(@NonNull StickyNavModel stickyNavModel, @NonNull
      StickyNotificationRefresher refresher, @NonNull StickyNotificationServiceCallback callback) {
    this.cricketStickyNavModel = stickyNavModel;
    this.refresher = refresher;
    this.callback = callback;
    getTeamFlagsBitmap();
    getBrandingImages();
  }

  public void setServiceCallbackAsNull() {
    callback = null;
  }

  @Override
  public void buildNotification(boolean isUpdate, boolean enableHeadsUpNotification, String state) {

    if (callback == null || cricketStickyNavModel == null || cricketStickyNavModel.getBaseInfo() ==
        null) {
      return;
    }
    //Refresh action will be delivered via a broadcast.
    Intent refreshIntent = new Intent();
    refreshIntent.setAction(STICKY_NOTIFICATION_REFRESH_ACTION);
    refreshIntent.setPackage(AppConfig.getInstance().getPackageName());
    PendingIntent refreshPendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), REQ_CODE_REFRESH, refreshIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);


    Intent playIntent = new Intent();
    playIntent.setAction(NotificationConstants.INTENT_ACTION_PLAY_STICKY_AUDIO);
    playIntent.setPackage(AppConfig.getInstance().getPackageName());
    PendingIntent playPendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), REQ_CODE_PLAY, playIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);


    Intent stopIntent = new Intent();
    stopIntent.setAction(NotificationConstants.INTENT_ACTION_STOP_STICKY_AUDIO);
    stopIntent.setPackage(AppConfig.getInstance().getPackageName());
    PendingIntent stopPendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), REQ_CODE_STOP, stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);

    //Close button will launch Settings screen.
    Intent dismissedIntent = new Intent();
    dismissedIntent.setAction(DailyhuntConstants.STICKY_NOTIFICATION_CLOSE_ACTION);
    dismissedIntent.setPackage(AppConfig.getInstance().getPackageName());

    if (cricketStickyNavModel.getBaseNotificationAsset() != null) {
      dismissedIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID,
          cricketStickyNavModel.getBaseNotificationAsset().getId());
      dismissedIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE,
          cricketStickyNavModel.getStickyType());

      if(cricketStickyNavModel.getOptOutMeta() != null) {
        if(cricketStickyNavModel.getOptOutMeta().getDeeplink() != null) {
          dismissedIntent.putExtra(
              NotificationConstants.INTENT_STICKY_NOTIFICATION_OPT_OUT_DEEPLINK,
              cricketStickyNavModel.getOptOutMeta().getDeeplink());
        }
        if(cricketStickyNavModel.getOptOutMeta().getSnackMeta() != null) {
          dismissedIntent.putExtra(NotificationConstants.SNACK_BAR_META,
              cricketStickyNavModel.getOptOutMeta().getSnackMeta());
        }
      }
    }

    PendingIntent dismissedPendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), REQ_CODE_FINISH, dismissedIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);


    //Replace this with a local broadcast.
    Intent targetIntent =
        StickyNotificationServiceUtils.createStickyServiceIntentWithAction(
            DailyhuntConstants.STICKY_NOTIFICATION_CLICK_ACTION);
    PendingIntent targetPendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), REQ_CODE_DEEPLINK, targetIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);

    if (cricketStickyNavModel.getBaseNotificationAsset() instanceof CricketNotificationAsset) {
      CricketNotificationAsset cricketNotificationAsset = (CricketNotificationAsset)
          cricketStickyNavModel.getBaseNotificationAsset();

      if (cricketNotificationAsset != null &&
          !CommonUtils.isEmpty(cricketNotificationAsset.getBranding())) {
        brandingCount++;
        brandingCount = brandingCount % cricketNotificationAsset.getBranding().size();
      }
    }


    StickyNotificationLayoutBuilder stickyNotificationLayoutBuilder = new
        StickyNotificationLayoutBuilder(CommonUtils.getApplication(), cricketStickyNavModel,
        NotificationLayoutType.NOTIFICATION_TYPE_STICKY_CRICKET, targetPendingIntent,
        refreshPendingIntent, dismissedPendingIntent, playPendingIntent, stopPendingIntent,
        brandingCount, team1FlagBitmap, team2FlagBitmap, callback.showLiveAudioCommentaryOption());

    stickyNotificationLayoutBuilder.setBrandingCache(brandingCache);
    Notification notification = stickyNotificationLayoutBuilder.build(enableHeadsUpNotification,
        state);

    if (notification == null) {
      Logger.d(TAG, "Notification is null, so not adding to tray");
      return;
    }

    callback.addNotificationToTray(cricketStickyNavModel.getBaseInfo().getUniqueId(),
        notification, isUpdate);

  }

  @Override
  public void handleStreamDataSuccess(DataStreamResponse dataStreamResponse) {

    if (!(dataStreamResponse.getBaseStreamAsset() instanceof CricketDataStreamAsset)) {
      return;
    }
    handleCricketResponse(dataStreamResponse);
  }

  @Override
  public void handleStreamDataError(DataStreamResponse dataStreamResponse) {

    if (dataStreamResponse == null || dataStreamResponse.getError() != null) {
      buildNotification(true, false, null);
    } else if (refresher != null &&
        (refresher.getExpiryTime() > 0 && System.currentTimeMillis() > refresher.getExpiryTime())) {
      handleExpiryTime(false);
    }
  }

  private void stopStickyNotificationService(boolean isFinished, boolean forceStopped) {
    callback.stopStickyService(isFinished, forceStopped);
  }

  @Override
  public void handleAction(String action, Intent intent) {
    if (CommonUtils.isEmpty(action)) {
      return;
    }

    if (STICKY_NOTIFICATION_REFRESH_ACTION.equalsIgnoreCase(action)) {
      handleRefreshClick();
    } else if (DailyhuntConstants.STICKY_NOTIFICATION_CLICK_ACTION.equalsIgnoreCase(action)) {
      callback.setupRefresher(true);
      StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(cricketStickyNavModel,
          NotificationActionAnalytics.CLICK, System.currentTimeMillis());
      Intent notificationRouterIntent = NotificationUtils.getNotificationRouterIntent
          (cricketStickyNavModel);
      notificationRouterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      CommonUtils.getApplication().startActivity(notificationRouterIntent);
    } else if (DailyhuntConstants.STICKY_NOTIFICATION_CLOSE_ACTION.equalsIgnoreCase(action)) {
      stopStickyNotificationService(false, true);
    }else if (DailyhuntConstants.STICKY_NOTIFICATION_DISMISS_AND_SHOW_ACTION.equalsIgnoreCase(action)) {
      if(callback != null){
        callback.dismissAndReNotifyStickyNotification();
      }
    }
  }


  @Override
  public void handleAudioChanged(Intent intent) {

    Logger.d(TAG, "handleAudioChanged");

    StickyAudioCommentary stickyAudioCommentary =
        (StickyAudioCommentary) intent.getSerializableExtra(
            NotificationConstants.INTENT_EXTRA_STICKY_AUDIO_STATE);
    if (stickyAudioCommentary != null) {
      cricketStickyNavModel
          .getBaseNotificationAsset().setState(stickyAudioCommentary.getState());
      buildNotification(true, false, null);
    }

  }


  private void handleRefreshClick() {
    if (NetworkSDKUtils.isLastKnownConnection()) {
      if (refresher != null && refresher.isManualRequestValid()) {
        String state = CommonUtils.getString(R.string.sticky_notification_updating);
        buildNotification(true, false, state);
        callback.setupRefresher(true);
        StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(cricketStickyNavModel,
            NotificationActionAnalytics.REFRESH, System.currentTimeMillis());
        Logger.d(TAG, "Refreshing score, call refresher");
      } else {
        Logger.d(TAG, "Ignoring the manual refresh because the last request was made less than 5 " +
            "seconds from now");
      }
    } else {
      String state = CommonUtils.getString(R.string.no_connection_error);
      buildNotification(true, false, state);
    }
  }

  private void handleCricketResponse(DataStreamResponse dataStreamAssetResponse) {

    Logger.d(TAG, "inside handleCricketResponse");

    CricketDataStreamAsset dataStreamAsset = (CricketDataStreamAsset) dataStreamAssetResponse
        .getBaseStreamAsset();

    cricketStickyNavModel.setBaseStreamAsset(dataStreamAsset);

    final long expiryTime = dataStreamAsset.getExpiryTime();
    //expiry bit to be given more priority
    //if nextStartTime > currentTime & suspended bit is set only in this case sticky will be rescheduled else suspended bit will be ignored
    long nextStartTime = dataStreamAsset.getNextStartTime();
    if (dataStreamAsset.isExpired() ||
        (expiryTime > 0 && System.currentTimeMillis() > expiryTime)) {
      handleExpiryTime(dataStreamAsset.isExpired() && expiryTime > System.currentTimeMillis());
      return;
    } else if ((nextStartTime > System.currentTimeMillis()) && dataStreamAsset.isSuspended()) {
      cricketStickyNavModel.getBaseInfo().setV4DisplayTime(nextStartTime);
      stopStickyNotificationService(false, false);
      StickyNotificationUtilsKt.fireNotificationRescheduledBroadcast(CommonUtils.getApplication(),
          cricketStickyNavModel, nextStartTime);
      StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(cricketStickyNavModel, NotificationActionAnalytics.SUSPENDED, System.currentTimeMillis());
      return;
    }

    if (refresher != null && (refresher.getPreviousAutoRefreshIntervalMs() != dataStreamAsset
        .getAutoRefreshInterval() * 1000)) {
      callback.setupRefresher(false);
    }

    CricketNotificationAsset notificationAsset = (CricketNotificationAsset) cricketStickyNavModel
        .getBaseNotificationAsset();

    if (notificationAsset != null) {
      String title = dataStreamAsset.getTitle();
      String liveTitle = dataStreamAsset.getLiveTitle();
      String line1Text = dataStreamAsset.getLine1Text();
      String line2Text = dataStreamAsset.getLine2Text();

      String currentTitle = notificationAsset.getTitle();
      String currentLiveTitle = notificationAsset.getLiveTitle();
      String currentLine1Text = notificationAsset.getLine1Text();
      String currentLine2Text = notificationAsset.getLine2Text();

      if (!CommonUtils.isEmpty(title) && !CommonUtils.equals(title, currentTitle)) {
        notificationAsset.setTitle(title);
      }

      if (!CommonUtils.isEmpty(liveTitle) && !CommonUtils.equals(liveTitle, currentLiveTitle)) {
        notificationAsset.setLiveTitle(liveTitle);
      }

      if (!CommonUtils.isEmpty(line1Text) && !CommonUtils.equals(line1Text, currentLine1Text)) {
        notificationAsset.setLine1Text(line1Text);
      }

      if (!CommonUtils.isEmpty(line2Text) && !CommonUtils.equals(line2Text, currentLine2Text)) {
        notificationAsset.setLine2Text(line2Text);
      }
    }

    buildNotification(true, false, null);
    callback.updateAudioCommentary(CommonUtils.isEmpty(dataStreamAsset.getAudioUrl()) ||
            dataStreamAsset.isAudioCommentaryForceStopped() ? null : dataStreamAsset.getAudioUrl(),
        dataStreamAsset.getAudioLanguage());
  }

  private void handleExpiryTime(boolean forceExpired) {
    stopStickyNotificationService(true, forceExpired);
  }

  private void getTeamFlagsBitmap() {
    CricketNotificationAsset cricketNotificationAsset = (CricketNotificationAsset)
        cricketStickyNavModel.getBaseNotificationAsset();

    if (cricketNotificationAsset == null) {
      return;
    }
    TeamAsset team1Asset = cricketNotificationAsset.getTeam1();

    if (team1Asset != null && !CommonUtils.isEmpty(team1Asset.getTeamIcon())) {
      com.newshunt.sdk.network.image.Image.load(team1Asset.getTeamIcon(), true)
          .into(new Image.ImageTarget() {

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
              super.onLoadFailed(errorDrawable);
              Logger.e(TAG, "Failure while downloading image of team1 ");
            }

            @Override
            public void onResourceReady(@NonNull Object bitmap, @Nullable Transition transition) {

              if (!(bitmap instanceof Bitmap)) {
                return;
              }

              team1FlagBitmap = (Bitmap) bitmap;
              Logger.d(TAG, "onSuccess: Team1 flag downloaded success");
              if (team2FlagBitmap != null) {
                buildNotification(true, false, null);
              }
            }
          });
    }

    TeamAsset team2Asset = cricketNotificationAsset.getTeam2();
    if (team2Asset != null && !CommonUtils.isEmpty(team2Asset.getTeamIcon())) {
      com.newshunt.sdk.network.image.Image.load(team2Asset.getTeamIcon(), true)
          .into(new Image.ImageTarget() {
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
              super.onLoadFailed(errorDrawable);
              Logger.e(TAG, "Failure while downloading image of team2 ");
            }

            @Override
            public void onResourceReady(@NonNull Object bitmap, @Nullable Transition transition) {

              if (!(bitmap instanceof Bitmap)) {
                return;
              }

              Logger.d(TAG, "onSuccess: Team2 flag downloaded success");
              team2FlagBitmap = (Bitmap) bitmap;
              if (team1FlagBitmap != null) {
                buildNotification(true, false, null);
              }
            }
          });
    }
  }


  private void getBrandingImages() {
    CricketNotificationAsset mNotificationAsset = (CricketNotificationAsset)
        cricketStickyNavModel.getBaseNotificationAsset();

    if (mNotificationAsset == null) {
      return;
    }
    List<String> urls = mNotificationAsset.getBranding();

    if (!CommonUtils.isEmpty(urls)) {
      for (int i = 0; i <= urls.size() - 1; i++) {
        String url = urls.get(i);
        if (!CommonUtils.isEmpty(urls.get(i))) {
          com.newshunt.sdk.network.image.Image.load(url, true)
              .into(new Image.ImageTarget() {

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                  super.onLoadFailed(errorDrawable);
                  Logger.e(TAG, "Failure while downloading image  ");
                }

                @Override
                public void onResourceReady(@NonNull Object bitmap,
                                            @Nullable Transition transition) {

                  if (!(bitmap instanceof Bitmap)) {
                    return;
                  }

                  brandingCache.put(url, (Bitmap) bitmap);
                  Logger.d(TAG, "onSuccess:downloading image  ");

                }
              });

        }
      }
    }

  }


}
