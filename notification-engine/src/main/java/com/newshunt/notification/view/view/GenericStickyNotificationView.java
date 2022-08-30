package com.newshunt.notification.view.view;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.transition.Transition;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dataentity.notification.asset.DataStreamResponse;
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset;
import com.newshunt.dataentity.notification.asset.GenericEntity;
import com.newshunt.dataentity.notification.asset.GenericNotificationAsset;
import com.newshunt.notification.R;
import com.newshunt.notification.helper.NotificationActionAnalytics;
import com.newshunt.notification.helper.NotificationUtils;
import com.newshunt.notification.helper.StickyNotificationServiceUtils;
import com.newshunt.notification.helper.StickyNotificationsAnalyticsHelperKt;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.notification.view.builder.GenericStickyNotificationBuilder;
import com.newshunt.notification.view.service.StickyNotificationRefresher;
import com.newshunt.notification.view.service.StickyNotificationServiceCallback;
import com.newshunt.sdk.network.image.Image;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by priya on 10/04/19.
 */

public class GenericStickyNotificationView implements StickyNotificationView {

  private static final int REQ_CODE_REFRESH = 1001;
  private static final int REQ_CODE_FINISH = 1002;
  private static final int REQ_CODE_DEEPLINK = 1003;
  private static Integer brandingCount = 0;
  private StickyNavModel genericStickyNavModel;
  private String TAG = "StickyNotificationService";
  private StickyNotificationRefresher refresher;
  private StickyNotificationServiceCallback callback;
  private HashMap<String, Bitmap> imageCache = new HashMap<String, Bitmap>();
  private HashMap<String, Bitmap> brandingCache = new HashMap<String, Bitmap>();


  public GenericStickyNotificationView(@NonNull StickyNavModel stickyNavModel, @NonNull
      StickyNotificationRefresher refresher, @NonNull StickyNotificationServiceCallback callback) {
    this.genericStickyNavModel = stickyNavModel;
    this.refresher = refresher;
    this.callback = callback;
    getIconImages();
    getBrandingImages();
  }


  @Override
  public void buildNotification(boolean isUpdate, boolean enableHeadsUpNotification, String state) {
    if (callback == null || genericStickyNavModel == null || genericStickyNavModel.getBaseInfo() ==
        null) {
      return;
    }
    //Refresh action will be delivered via a broadcast.
    Intent refreshIntent = new Intent();
    refreshIntent.setAction(DailyhuntConstants.STICKY_NOTIFICATION_REFRESH_ACTION);
    refreshIntent.setPackage(AppConfig.getInstance().getPackageName());
    PendingIntent refreshPendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), REQ_CODE_REFRESH, refreshIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);

    //Close button will launch Settings screen.
    Intent dismissedIntent = new Intent();
    dismissedIntent.setAction(DailyhuntConstants.STICKY_NOTIFICATION_CLOSE_ACTION);
    dismissedIntent.setPackage(AppConfig.getInstance().getPackageName());
    if (genericStickyNavModel.getBaseNotificationAsset() != null) {
      dismissedIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID,
          genericStickyNavModel.getBaseNotificationAsset().getId());
      dismissedIntent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE,
          genericStickyNavModel.getStickyType());
      if(genericStickyNavModel.getOptOutMeta() != null) {
        if(genericStickyNavModel.getOptOutMeta().getDeeplink() != null) {
          dismissedIntent.putExtra(
              NotificationConstants.INTENT_STICKY_NOTIFICATION_OPT_OUT_DEEPLINK,
              genericStickyNavModel.getOptOutMeta().getDeeplink());
        }
        if(genericStickyNavModel.getOptOutMeta().getSnackMeta() != null) {
          dismissedIntent.putExtra(NotificationConstants.SNACK_BAR_META,
              genericStickyNavModel.getOptOutMeta().getSnackMeta());
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

    GenericNotificationAsset mNotificationAsset = (GenericNotificationAsset)
        genericStickyNavModel.getBaseNotificationAsset();

    if (mNotificationAsset != null && !CommonUtils.isEmpty(mNotificationAsset.getBranding())) {
      brandingCount++;
      brandingCount = brandingCount % mNotificationAsset.getBranding().size();
    }


    GenericStickyNotificationBuilder stickyNotificationLayoutBuilder = new
        GenericStickyNotificationBuilder(CommonUtils.getApplication(), genericStickyNavModel,
        NotificationLayoutType.NOTIFICATION_TYPE_STICKY_GENERIC, targetPendingIntent,
        refreshPendingIntent, dismissedPendingIntent, brandingCount);

    stickyNotificationLayoutBuilder.setBitmapCache(imageCache);
    stickyNotificationLayoutBuilder.setBrandingCache(brandingCache);


    Notification notification = stickyNotificationLayoutBuilder.build(enableHeadsUpNotification,
        state);

    if (notification == null) {
      Logger.d(TAG, "Notification is null, so not adding to tray");
      return;
    }

    callback.addNotificationToTray(genericStickyNavModel.getBaseInfo().getUniqueId(),
        notification, isUpdate);

  }

  @Override
  public void handleStreamDataSuccess(DataStreamResponse dataStreamResponse) {
    if (!(dataStreamResponse.getBaseStreamAsset() instanceof GenericDataStreamAsset)) {
      return;
    }
    handleStreamResponse(dataStreamResponse);
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

  @Override
  public void handleAction(String action, Intent intent) {
    if (CommonUtils.isEmpty(action)) {
      return;
    }

    if (DailyhuntConstants.STICKY_NOTIFICATION_REFRESH_ACTION.equalsIgnoreCase(action)) {
      handleRefreshClick();
    } else if (DailyhuntConstants.STICKY_NOTIFICATION_CLICK_ACTION.equalsIgnoreCase(action)) {
      callback.setupRefresher(true);
      StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(genericStickyNavModel,
          NotificationActionAnalytics.CLICK, System.currentTimeMillis());
      Intent notificationRouterIntent = NotificationUtils.getNotificationRouterIntent
          (genericStickyNavModel);
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


  private void handleRefreshClick() {
    if (NetworkSDKUtils.isLastKnownConnection()) {
      if (refresher != null && refresher.isManualRequestValid()) {
        String state = CommonUtils.getString(com.newshunt.common.util.R.string.sticky_notification_updating);
        buildNotification(true, false, state);
        callback.setupRefresher(true);
        StickyNotificationsAnalyticsHelperKt.logStickyNotificationActionEvent(genericStickyNavModel,
            NotificationActionAnalytics.REFRESH, System.currentTimeMillis());
        Logger.d(TAG, "Refreshing score, call refresher");
      } else {
        Logger.d(TAG, "Ignoring the manual refresh because the last request was made less than 5 " +
            "seconds from now");
      }
    } else {
      String state = CommonUtils.getString(com.newshunt.common.util.R.string.no_connection_error);
      buildNotification(true, false, state);
    }
  }

  public void setServiceCallbackAsNull() {
    callback = null;
  }

  @Override
  public void handleAudioChanged(Intent intent) {

  }


  private void handleStreamResponse(DataStreamResponse dataStreamAssetResponse) {
    GenericDataStreamAsset dataStreamAsset = (GenericDataStreamAsset) dataStreamAssetResponse
        .getBaseStreamAsset();
    genericStickyNavModel.setBaseStreamAsset(dataStreamAsset);

    final long expiryTime = dataStreamAsset.getExpiryTime();
    if (dataStreamAsset.isExpired() ||
        (expiryTime > 0 && System.currentTimeMillis() > expiryTime)) {
      handleExpiryTime(dataStreamAsset.isExpired() && expiryTime > System.currentTimeMillis());
      return;
    }

    if (refresher != null && (refresher.getPreviousAutoRefreshIntervalMs() != dataStreamAsset
        .getAutoRefreshInterval() * 1000)) {
      callback.setupRefresher(false);
    }

    buildNotification(true, false, null);
  }

  private void handleExpiryTime(boolean forceExpired) {
    stopStickyNotificationService(true, forceExpired);
  }

  private void getIconImages() {
    GenericNotificationAsset mNotificationAsset = (GenericNotificationAsset)
        genericStickyNavModel.getBaseNotificationAsset();

    if (mNotificationAsset == null) {
      return;
    }
    List<GenericEntity> entities = mNotificationAsset.getValues().getEntities();


    for (int i = 0; i <= entities.size() - 1; i++) {

      if (!CommonUtils.isEmpty(entities.get(i).getIcon())) {

        String url = entities.get(i).getIcon();
        com.newshunt.sdk.network.image.Image.load(url, true)
            .into(new Image.ImageTarget() {

              @Override
              public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                Logger.e(TAG, "Failure while downloading image  ");
              }

              @Override
              public void onResourceReady(@NonNull Object bitmap, @Nullable Transition transition) {

                if (!(bitmap instanceof Bitmap)) {
                  return;
                }

                imageCache.put(url, (Bitmap) bitmap);
                Logger.d(TAG, "onSuccess:downloading image  ");

              }
            });
      }


    }

  }


  private void getBrandingImages() {

    GenericNotificationAsset mNotificationAsset = (GenericNotificationAsset)
        genericStickyNavModel.getBaseNotificationAsset();

    if (mNotificationAsset == null) {
      return;
    }
    List<String> urls = mNotificationAsset.getBranding();
    if (!CommonUtils.isEmpty(urls)) {

      for (int i = 0; i <= urls.size() - 1; i++) {
        if (!CommonUtils.isEmpty(urls.get(i))) {

          String url = urls.get(i);
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

  private void stopStickyNotificationService(boolean isFinished, boolean forceStopped) {
    callback.stopStickyService(isFinished, forceStopped);
  }


}
