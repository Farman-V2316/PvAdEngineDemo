/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.view.builder;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.request.transition.Transition;
import com.newshunt.app.view.view.NotificationServiceCallback;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.R;
import com.newshunt.notification.helper.NotificationLogger;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.sdk.network.image.Image;

import java.util.List;

import static com.newshunt.notification.helper.NotificationDefaultChannelHelperKt.createDefaultChannel;

/**
 * Notification builder will identify different type of notification layout and builds the UI.
 * Even responsible for fetching notification image.
 *
 * @author santosh.kulkarni
 */
public class NotificationBuilder {

  public static final Handler HANDLER = new Handler(Looper.getMainLooper());
  private static final String TAG = NotificationBuilder.class.getName();
  private final BaseInfo baseInfo;
  private final Intent targetIntent;
  private final Context context;
  private final NotificationServiceCallback callback;
  private final BaseModel baseModel;
  private Bitmap notificationImage;
  private Bitmap bigNotificationImage;
  private int uniqueId = Constants.COMMON_ID;
  private float notificationFontSize = 0.0f;

  public NotificationBuilder(Context context, @NonNull BaseModel baseModel,
                             Intent targetIntent, NotificationServiceCallback callback) {
    this.baseModel = baseModel;
    baseInfo = baseModel.getBaseInfo();
    this.targetIntent = targetIntent;
    this.context = context;
    this.callback = callback;
    this.notificationFontSize = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_FONT_SIZE, 0.0f);
    if (baseModel.getBaseInfo() != null) {
      uniqueId = baseModel.getBaseInfo().getUniqueId();
    }
  }

  public NotificationCompat.Builder buildNotification(boolean isNotificationUngroupingEnabled) {

    NotificationLayoutType notificationLayoutType = baseInfo.getLayoutType();
    if (notificationLayoutType == null) {
      return null;
    }

    NotificationCompat.Builder builder = null;
    boolean postAsHeadsUp = false;
    if (notificationLayoutType == NotificationLayoutType.NOTIFICATION_TYPE_SMALL ||
        notificationLayoutType == NotificationLayoutType.NOTIFICATION_TYPE_BIG_PICTURE) {
      String imageLink = baseModel.getBaseInfo().getInboxImageLink();
      if (CommonUtils.isEmpty(imageLink)) {
        imageLink = baseModel.getBaseInfo().getImageLinkV2();
      }
      if (CommonUtils.isEmpty(imageLink)) {
        imageLink = baseModel.getBaseInfo().getImageLink();
      }
      if (CommonUtils.isEmpty(imageLink)) {
        postAsHeadsUp = true;
      }
    }

    createDefaultChannel();

    switch (notificationLayoutType) {
      case NOTIFICATION_TYPE_SMALL:
        builder = buildSmallNotification(postAsHeadsUp, isNotificationUngroupingEnabled);
        break;

      case NOTIFICATION_TYPE_BIG_TEXT:
        builder = buildBigTextNotification(true, isNotificationUngroupingEnabled);
        break;

      case NOTIFICATION_TYPE_BIG_PICTURE:
        builder = buildBigPictureNotification(postAsHeadsUp, isNotificationUngroupingEnabled);
        break;

        case NOTIFICATION_TYPE_CREATE_POST:
            builder = buildProcessProgressNotification(isNotificationUngroupingEnabled);
            break;
    }
    return builder;
  }

  public NotificationCompat.Builder buildInboxStyleNotification(List<BaseModel> notList, boolean isNotificationUngroupingEnabled) {

    // Setting channelId as Backend Configurable DEFAULT CHANNEL GROUPED TRAY NOTIFICATION
    String channelId = PreferenceManager.getPreference(AppStatePreference.DEFAULT_CHANNEL_GROUPED_TRAY_NOTIFICATION,Constants.EMPTY_STRING);
    Logger.d(TAG,"Default notification is "+ channelId);
    baseInfo.setChannelId(channelId);
    NotificationCompat.Builder notificationBuilder = new NotificationLayoutBuilder(
        context,
        baseInfo,
        notificationImage, bigNotificationImage,
        NotificationLayoutType.NOTIFICATION_TYPE_BIG_TEXT_INBOX_STYLE, uniqueId,
        notList, notificationFontSize).build(isNotificationUngroupingEnabled);
    PendingIntent pendingContentIntent = PendingIntent.getActivity(
        context, uniqueId, targetIntent,
        PendingIntent.FLAG_CANCEL_CURRENT);
    notificationBuilder.setAutoCancel(true);
    notificationBuilder.setContentIntent(pendingContentIntent);

    //This means that the notification needs to be shown in the future and not immediately.
    if (baseInfo.isDeferred()) {
      return null;
    }

    return notificationBuilder;
  }

  private NotificationCompat.Builder buildSmallNotification(boolean postAsHeadsUp, boolean isNotificationUngroupingEnabled) {

    if (Constants.COMMON_ID == uniqueId) {
      // Trying to avoid duplicate notification message shown in notification tray from client end by
      // putting a unique id for news and books . Id we are taking it from newsId / bookId
      // In case of any exception we fallback to putting System.currentTimeMillis() as uniqueId.
      uniqueId = baseInfo.getUniqueId();
    }

    NotificationCompat.Builder notificationBuilder = new NotificationLayoutBuilder(
        context,
        baseInfo,
        notificationImage, bigNotificationImage,
        NotificationLayoutType.NOTIFICATION_TYPE_SMALL, uniqueId, postAsHeadsUp, targetIntent, notificationFontSize).build(isNotificationUngroupingEnabled);

    PendingIntent pendingContentIntent = PendingIntent.getActivity(
        context, uniqueId, targetIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notificationBuilder.setAutoCancel(true);
    notificationBuilder.setContentIntent(pendingContentIntent);

    //This means that the notification needs to be shown in the future and not immediately.
    if (baseInfo.isDeferred()) {
      return null;
    }

    if (notificationImage != null && callback != null) {
      callback.addNotificationWithImageToTray(baseModel, notificationBuilder);
    }
    return notificationBuilder;
  }

  private NotificationCompat.Builder buildBigPictureNotification(boolean postAsHeadsUp, boolean isNotificationUngroupingEnabled) {

    if (Constants.COMMON_ID == uniqueId) {
      uniqueId = baseInfo.getUniqueId();
    }
    NotificationCompat.Builder notificationBuilder = new NotificationLayoutBuilder(
        context,
        baseInfo,
        notificationImage, bigNotificationImage,
        NotificationLayoutType.NOTIFICATION_TYPE_BIG_PICTURE, uniqueId, postAsHeadsUp, targetIntent, notificationFontSize).build(isNotificationUngroupingEnabled);
    PendingIntent pendingContentIntent = PendingIntent.getActivity(
        context, uniqueId, targetIntent,
        PendingIntent.FLAG_CANCEL_CURRENT);
    notificationBuilder.setAutoCancel(true);
    notificationBuilder.setContentIntent(pendingContentIntent);

    //This means that the notification needs to be shown in the future and not immediately.
    if (baseInfo.isDeferred()) {
      return null;
    }

    if ((bigNotificationImage != null || notificationImage != null) && callback != null) {
      callback.addNotificationWithImageToTray(baseModel, notificationBuilder);
    }
    return notificationBuilder;
  }

    private NotificationCompat.Builder buildBigTextNotification(boolean postAsHeadsUp, boolean isNotificationUngroupingEnabled) {

        if (Constants.COMMON_ID == uniqueId) {
            uniqueId = baseInfo.getUniqueId();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationLayoutBuilder(
                context,
                baseInfo,
                notificationImage, bigNotificationImage,
                NotificationLayoutType.NOTIFICATION_TYPE_BIG_TEXT, uniqueId, postAsHeadsUp, targetIntent, notificationFontSize).build(isNotificationUngroupingEnabled);
        PendingIntent pendingContentIntent = PendingIntent.getActivity(
                context, uniqueId, targetIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingContentIntent);

        //This means that the notification needs to be shown in the future and not immediately.
        if (baseInfo.isDeferred()) {
            return null;
        }

        return notificationBuilder;
    }

    private NotificationCompat.Builder buildProcessProgressNotification(boolean isNotificationUngroupingEnabled) {
        if (Constants.COMMON_ID == uniqueId) {
            uniqueId = baseInfo.getUniqueId();
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationLayoutBuilder(context, baseInfo, notificationImage, bigNotificationImage,
                        NotificationLayoutType.NOTIFICATION_TYPE_SMALL, uniqueId, false, targetIntent, notificationFontSize).build(isNotificationUngroupingEnabled);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        return notificationBuilder;
    }


  public void getNotificationImageBitmap(final BaseModel baseModel,
                                         final String imageLink, final boolean isBigImageLink, boolean isNotificationUngroupingEnabled) {

    if (CommonUtils.isEmpty(imageLink)) {
      return;
    }
    HANDLER.post(() -> {
      NotificationLogger.logNotificationImageDownloadStart(isBigImageLink, imageLink);
      Image.load(imageLink, true).into(new Image.ImageTarget() {
        @Override
        public void onResourceReady(@NonNull Object bitmap, @Nullable Transition transition) {

          if (!(bitmap instanceof Bitmap)) {
            return;
          }

          NotificationLogger.logNotificationImageDownloadSuccess(imageLink);

          if (callback != null) {
            callback.removeUrlFromSet(imageLink);
          }

          if (isBigImageLink) {
            bigNotificationImage = (Bitmap) bitmap;
            buildBigPictureNotification(true, isNotificationUngroupingEnabled);
          } else if (baseInfo.getLayoutType() ==
              NotificationLayoutType.NOTIFICATION_TYPE_BIG_PICTURE) {
            Bitmap image = (Bitmap) bitmap;
            notificationImage = AndroidUtils.getRoundedBitmap(image, CommonUtils.getDimension(R.dimen.image_size),
                CommonUtils.getDimension(R.dimen.image_size), CommonUtils.getDimension(R.dimen.image_radius));
            buildBigPictureNotification(true, isNotificationUngroupingEnabled);
          } else {
            Bitmap image = (Bitmap) bitmap;
            notificationImage = AndroidUtils.getRoundedBitmap(image, CommonUtils.getDimension(R.dimen.image_size),
                CommonUtils.getDimension(R.dimen.image_size), CommonUtils.getDimension(R.dimen.image_radius));
            buildSmallNotification(true, isNotificationUngroupingEnabled);
          }
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
          super.onLoadFailed(errorDrawable);
          NotificationLogger.logNotificationImageDownloadFailed(imageLink);
          //Do nothing on Failure, As notification is already shown in tray
          if (callback != null) {
            callback.removeUrlFromSet(imageLink);
            callback.notificationImageDownloadFailed(imageLink, uniqueId, baseModel);
          }
        }
      });
    });
  }
}
