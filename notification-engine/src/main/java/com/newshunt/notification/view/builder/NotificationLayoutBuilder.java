/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.view.builder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FEOutput;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationCtaObj;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationCtaUi;
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.NotificationCtaTypes;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.NotificationPlacementType;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.deeplink.navigator.CommonNavigator;
import com.newshunt.dhutil.analytics.AnalyticsHelper;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.BuildConfig;
import com.newshunt.notification.R;
import com.newshunt.notification.analytics.NhNotificationParam;
import com.newshunt.notification.helper.NotificationDefaultChannelHelperKt;
import com.newshunt.notification.helper.NotificationHandler;
import com.newshunt.notification.helper.NotificationUtils;
import com.newshunt.notification.model.entity.ChannelNotFoundException;
import com.newshunt.notification.model.entity.NotificationChannelGroupPair;
import com.newshunt.notification.model.entity.server.AdjunctLangBaseInfo;
import com.newshunt.notification.model.entity.server.CreatePostBaseInfo;
import com.newshunt.notification.view.receiver.NotificationCtaReceiver;
import com.newshunt.notification.view.receiver.NotificationDismissedReceiver;

import java.util.HashMap;
import java.util.List;

/**
 * It builds notification UI based on layout type.
 *
 * @author santosh.kulkarni
 */
public class NotificationLayoutBuilder {
  private final Context context;
  private final BaseInfo navigationModel;
  private final NotificationLayoutType layoutType;
  private final int uniqueId;
  private final Bitmap notifyImage;
  private final Bitmap bigPicture;
  private boolean isBigText = false;
  private List<BaseModel> notificationList;
  private int notificationTextWidth = 0;
  private boolean isVibratonOn = false;
  private Intent targetIntent;
  private List<NotificationCtaObj> notificationCtaObjs;
  private float notificationFontSize = 0.0f;
  private final String EMPTY_STRING = "";

  public NotificationLayoutBuilder(Context context, BaseInfo navigationModel,
                                   Bitmap notifyImage, Bitmap bigPicture,
                                   NotificationLayoutType layoutType, int uniqueId,
                                   boolean isVibratonOn, Intent targetIntent, float notificationFontSize) {
    this.context = context;
    this.navigationModel = navigationModel;
    this.layoutType = layoutType;
    this.notifyImage = notifyImage;
    this.bigPicture = bigPicture;
    this.uniqueId = uniqueId;
    this.isVibratonOn = isVibratonOn;
    this.targetIntent = targetIntent;
    this.notificationFontSize = notificationFontSize;
    initCta();
  }

  public NotificationLayoutBuilder(Context context, BaseInfo navigationModel,
                                   Bitmap notifyImage, Bitmap bigPicture,
                                   NotificationLayoutType layoutType, int uniqueId,
                                   List<BaseModel> notList, float notificationFontSize) {
    this.context = context;
    this.navigationModel = navigationModel;
    this.layoutType = layoutType;
    this.notifyImage = notifyImage;
    this.bigPicture = bigPicture;
    this.uniqueId = uniqueId;
    notificationList = notList;
    this.notificationFontSize = notificationFontSize;
    initCta();
  }

  private void initCta() {
    notificationCtaObjs = NotificationHandler.getCtaForNotification(navigationModel);
  }


  public NotificationCompat.Builder build(boolean isNotificationUngroupingEnabled) {

    if (layoutType == null) {
      return null;
    }

    switch (layoutType) {
      case NOTIFICATION_TYPE_SMALL:
        return buildNotificationLayoutOfTypeSmall(isNotificationUngroupingEnabled);

      case NOTIFICATION_TYPE_BIG_TEXT:
        return buildNotificationLayoutOfTypeBigText(isNotificationUngroupingEnabled);

      case NOTIFICATION_TYPE_BIG_PICTURE:
        return buildNotificationLayoutOfTypeBigPicture(isNotificationUngroupingEnabled);

        case NOTIFICATION_TYPE_CREATE_POST:
            return buildNotificationLayoutOfProgress(isNotificationUngroupingEnabled);

      case NOTIFICATION_TYPE_BIG_TEXT_INBOX_STYLE:
        return buildNotificationLayoutOfTypeBigTextInboxStyle(isNotificationUngroupingEnabled);

      case NOTIFICATION_TYPE_ADJUNCT_STICKY:
        return buildAdjunctLangNotification(isNotificationUngroupingEnabled);
    }
    return null;
  }

    private NotificationCompat.Builder buildNotificationLayoutOfProgress(boolean isNotificationUngroupingEnabled) {
        NotificationChannelGroupPair channelAndGroup = getChannelIdAndGroupIdForCreatePost();
        long currentTime = System.currentTimeMillis();
        int icon = R.drawable.app_notification_icon;
        String groupId = channelAndGroup.getGroupId();
      //for ungrouped case unique group ids will be always applied, however for grouped case application of unique vs server obtained groupId, will be controlled by static config
      if(isNotificationUngroupingEnabled || !PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
        groupId = String.valueOf(navigationModel.getUniqueId()) + System.currentTimeMillis();
      }
        NotificationCompat.Builder notificationBuilder =
              new NotificationCompat.Builder(context, channelAndGroup.getChannelId())
                      .setSmallIcon(icon)
                      .setGroup(groupId)
                      .setWhen(currentTime)
                      .setPriority(navigationModel.getPriority())
                      .setDeleteIntent(createOnDismissedIntent(context, navigationModel, false));
        try {
          CreatePostBaseInfo model = (CreatePostBaseInfo) navigationModel;
          if (model.getState() == NotificationConstants.POST_UPLOAD_PROGRESS) {
            notificationBuilder.setOngoing(true);
          } else {
            Intent intent = CommonNavigator.getProfileMyPostsIntent(new PageReferrer());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) model.getCpId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setContentIntent(pendingIntent);
          }
          // Set text view based view
          notificationBuilder.setCustomContentView(setProcessProgressWithText(model));

          return notificationBuilder;
        } catch (Exception e) {
          e.printStackTrace();
        }
        if(BuildConfig.DEBUG){
          HashMap<String, String> paramsMap = new HashMap<>();
          paramsMap.put(NhNotificationParam.NOTIF_DEV_EVENT_SUBTYPE.name(), "Notification configs");
          paramsMap.put("channel_id", channelAndGroup.getChannelId());
          paramsMap.put("channel_group_ID", channelAndGroup.getGroupId());
          paramsMap.put("notification_group_id", groupId);
          paramsMap.put("notification_priority", String.valueOf(navigationModel.getPriority()));
          AnalyticsHelper.logNotificationDevEvent(paramsMap);
        }
        return notificationBuilder;
    }

  private NotificationCompat.Builder buildAdjunctLangNotification(boolean isNotificationUngroupingEnabled) {
    NotificationChannelGroupPair channelAndGroup = getChannelIdAndGroupIdForAdjunctLang();
    long currentTime = System.currentTimeMillis();
    int icon = R.drawable.app_notification_icon;
    String groupId = channelAndGroup.getGroupId();
    //for ungrouped case unique group ids will be always applied, however for grouped case application of unique vs server obtained groupId will be controlled by static config
    if(isNotificationUngroupingEnabled || !PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
      groupId = String.valueOf(navigationModel.getUniqueId()) + System.currentTimeMillis();
    }
    NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(context, channelAndGroup.getChannelId())
                    .setSmallIcon(icon)
                    .setGroup(groupId)
                    .setWhen(currentTime)
                    .setPriority(navigationModel.getPriority())
                    .setOngoing(true)
                    .setOnlyAlertOnce(true);
    try {
      AdjunctLangBaseInfo model = (AdjunctLangBaseInfo) navigationModel;
      // Set text view based view
      notificationBuilder.setCustomContentView(setAdjunctLangRemoteViews(model));
      return notificationBuilder;
    } catch (Exception e) {
      e.printStackTrace();
    }
    if(BuildConfig.DEBUG){
      HashMap<String, String> paramsMap = new HashMap<>();
      paramsMap.put(NhNotificationParam.NOTIF_DEV_EVENT_SUBTYPE.name(), "Notification configs");
      paramsMap.put("channel_id", channelAndGroup.getChannelId());
      paramsMap.put("channel_group_ID", channelAndGroup.getGroupId());
      paramsMap.put("notification_group_id", groupId);
      paramsMap.put("notification_priority", String.valueOf(navigationModel.getPriority()));
      AnalyticsHelper.logNotificationDevEvent(paramsMap);
    }
    return notificationBuilder;
  }


  private NotificationCompat.Builder buildNotificationLayoutOfTypeSmall(boolean isNotificationUngroupingEnabled) {
    CharSequence tickerText;
    if (navigationModel.getIsBookDownloadNotification()) {
      tickerText = navigationModel.getTickerMessage();
    } else {
      tickerText = NotificationUtils.getNotificationContentText(navigationModel);
    }

    if(tickerText == null){
      tickerText = EMPTY_STRING;
    }

    long currentTime = System.currentTimeMillis();
    int icon = R.drawable.app_notification_icon;

    NotificationChannelGroupPair channelAndGroup = getChannelIdAndGroupId();
    String groupId = channelAndGroup.getGroupId();
    //for ungrouped case unique group ids will be always applied, however for grouped case application of unique vs server obtained groupId will be controlled by static config
    if(isNotificationUngroupingEnabled || !PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
      groupId = String.valueOf(navigationModel.getUniqueId()) + System.currentTimeMillis();
    }
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(context, channelAndGroup.getChannelId())
            .setSmallIcon(icon)
            .setTicker(AndroidUtils.getTextFromHtml(tickerText.toString()))
            .setGroup(groupId)
            .setWhen(currentTime)
            .setPriority(navigationModel.getPriority())
            .setDeleteIntent(createOnDismissedIntent(context, navigationModel, false));

    try {
      FEOutput output =
          FontHelper.convertToFontIndices(
              AndroidUtils.getTextFromHtml(navigationModel.getUniMsg()));
      if (output.isSupportedLanguageFound()) {
        notificationBuilder.setCustomContentView(getSmallContentView());
        if (!CommonUtils.isEmpty(notificationCtaObjs)) {
          notificationBuilder.setCustomBigContentView(getCtaExpandedViewImage());
        }
      } else {
        // Set text view based view
        notificationBuilder.setCustomContentView(getSmallContentViewWithText());
        if (!CommonUtils.isEmpty(notificationCtaObjs)) {
          notificationBuilder.setCustomBigContentView(getCtaExpandedView());
        }
        notificationBuilder.setContentText(
            AndroidUtils.getRichTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel)));
      }
    } catch (Exception e) {
      // Do not set content text since we dont know if there is any indic text.
      notificationBuilder.setCustomContentView(getSmallContentView());
    }
    if(BuildConfig.DEBUG){
      HashMap<String, String> paramsMap = new HashMap<>();
      paramsMap.put(NhNotificationParam.NOTIF_DEV_EVENT_SUBTYPE.name(), "Notification configs");
      paramsMap.put("channel_id", channelAndGroup.getChannelId());
      paramsMap.put("channel_group_ID", channelAndGroup.getGroupId());
      paramsMap.put("notification_group_id", groupId);
      paramsMap.put("notification_priority", String.valueOf(navigationModel.getPriority()));
      AnalyticsHelper.logNotificationDevEvent(paramsMap);
    }
    return notificationBuilder;
  }

  private RemoteViews getSmallContentViewWithText() {
    RemoteViews contentView = new RemoteViews(context.getPackageName(),
        R.layout.remote_layout_with_textview);
    String displayMessage = NotificationUtils.getNotificationContentText(navigationModel);

    setupNotification(contentView);
    updateCtaForSmallNotification(contentView);

    contentView.setTextViewText(R.id.notification_text, AndroidUtils.getRichTextFromHtml(displayMessage));
    setFontSize(contentView, R.id.notification_text, notificationFontSize);
    return contentView;
  }

  private void updateCtaForSmallNotification(RemoteViews contentView) {
    if (!CommonUtils.isEmpty(notificationCtaObjs) && targetIntent != null && targetIntent.getExtras() != null) {
      for(NotificationCtaObj obj : notificationCtaObjs) {
        NotificationCtaUi notificationCtaUi = getNotificationCtaUiObj(obj, targetIntent, true);
        if (canShowInTray(obj) && notificationCtaUi != null) {
          contentView.setViewVisibility(R.id.notification_cta, View.VISIBLE);
          contentView.setImageViewResource(R.id.notification_cta, notificationCtaUi.getResourceId());
          contentView.setOnClickPendingIntent(R.id.notification_cta, notificationCtaUi.getTargetIntent());
          break;
        }
      }
    }

  }

  private RemoteViews getCtaExpandedView() {
    RemoteViews contentView = new RemoteViews(context.getPackageName(),
            R.layout.remote_layout_with_cta_textview);
    String displayMessage = NotificationUtils.getNotificationContentText(navigationModel);

    setupNotification(contentView);
    contentView.setTextViewText(R.id.notification_text, AndroidUtils.getRichTextFromHtml(displayMessage));
    setFontSize(contentView, R.id.notification_text, notificationFontSize);
    updateCtas(contentView);
    return contentView;
  }

  private RemoteViews getCtaExpandedViewImage() {
    RemoteViews contentView = new RemoteViews(context.getPackageName(),
            R.layout.remote_layout_with_cta);
    String displayMessage = NotificationUtils.getNotificationContentText(navigationModel);

    setupNotification(contentView);

    boolean isUrdu = navigationModel.isUrdu();
    contentView.setImageViewBitmap(R.id.notification_text_image,
            getBitmapOfTextViewWithUnicodeSupport(displayMessage, true, context, isUrdu));
    updateCtasFromImageView(contentView);
    return contentView;
  }

  private void updateCtas(RemoteViews contentView) {

    if (CommonUtils.isEmpty(notificationCtaObjs) || targetIntent == null) {
      return;
    }

    int index = 0;
    for (NotificationCtaObj obj: notificationCtaObjs) {
      if (canShowInTray(obj)) {
        NotificationCtaUi notificationCtaUi = getNotificationCtaUiObj(obj, targetIntent, false);
        if (notificationCtaUi != null) {
          setTextForCta(contentView, notificationCtaUi, index);
          index++;
        }
      }
    }
  }

  private void updateCtasFromImageView(RemoteViews contentView) {

    if (CommonUtils.isEmpty(notificationCtaObjs) || targetIntent == null) {
      return;
    }

    int index = 0;
    for (NotificationCtaObj obj: notificationCtaObjs) {
      if (canShowInTray(obj)) {
        NotificationCtaUi notificationCtaUi = getNotificationCtaUiObj(obj, targetIntent,false);
        if (notificationCtaUi != null) {
          setTextForCtaForImage(contentView, notificationCtaUi, index);
          index++;
        }
      }
    }
  }

  private boolean canShowInTray(@NonNull NotificationCtaObj notificationCtaObj) {
    return CommonUtils.equals(notificationCtaObj.getPlacement(), NotificationPlacementType.TRAY_AND_INBOX.name())
            || CommonUtils.equals(notificationCtaObj.getPlacement(), NotificationPlacementType.TRAY_ONLY.name());
  }

  private NotificationCtaUi getNotificationCtaUiObj(@NonNull NotificationCtaObj notificationCtaObj,
                                                    @NonNull Intent targetIntent, boolean isSmallNotificaiton) {
    NotificationCtaTypes notificationCtaType = NotificationCtaTypes.fromName(notificationCtaObj.getType());
    if (notificationCtaType == null) {
      return null;
    } else {
      switch (notificationCtaType) {
        case REPLY: {
          String ctaString = CommonUtils.getString(com.newshunt.common.util.R.string.notification_cta_reply);
          Intent intent = new Intent();
          if (isSmallNotificaiton) {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPLY_SMALL);
          } else {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPLY);
          }
          intent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
          if (targetIntent.getExtras() != null) {
            intent.putExtras(targetIntent.getExtras());
          }
          intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, uniqueId);
          PendingIntent pendingContentIntent = PendingIntent.getBroadcast(
                  context, uniqueId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
          return new NotificationCtaUi(ctaString, R.drawable.ic_noti_action_reply, pendingContentIntent);
        }
        case SHARE: {
          String ctaString = CommonUtils.getString(com.newshunt.common.util.R.string.notification_cta_share);
          Intent intent = new Intent();
          if (isSmallNotificaiton) {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_SHARE_SMALL);
          } else  {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_SHARE);
          }
          intent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
          if (targetIntent.getExtras() != null) {
            intent.putExtras(targetIntent.getExtras());
          }
          intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, uniqueId);
          PendingIntent pendingContentIntent = PendingIntent.getBroadcast(
                  context, uniqueId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
          return new NotificationCtaUi(ctaString, R.drawable.ic_noti_action_share, pendingContentIntent);
        }
        case FOLLOW: {
          String ctaString = CommonUtils.getString(com.newshunt.common.util.R.string.notification_cta_follow);
          Intent intent = new Intent();
          if (isSmallNotificaiton) {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_FOLLOW_SMALL);
          } else {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_FOLLOW);
          }
          intent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
          if (targetIntent.getExtras() != null) {
            intent.putExtras(targetIntent.getExtras());
          }
          intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, uniqueId);
          PendingIntent pendingContentIntent = PendingIntent.getBroadcast(
                  context, uniqueId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
          return new NotificationCtaUi(ctaString, R.drawable.ic_follow_notif, pendingContentIntent);
        }
        case REPOST: {
          String ctaString = CommonUtils.getString(com.newshunt.common.util.R.string.notification_cta_repost);
          Intent intent = new Intent();
          if (isSmallNotificaiton) {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPOST_SMALL);
          } else {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPOST);
          }
          intent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
          if (targetIntent.getExtras() != null) {
            intent.putExtras(targetIntent.getExtras());
          }
          intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, uniqueId);
          PendingIntent pendingContentIntent = PendingIntent.getBroadcast(
                  context, uniqueId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
          return new NotificationCtaUi(ctaString, R.drawable.ic_noti_action_repost, pendingContentIntent);
        }
        case COMMENT: {
          String ctaString = CommonUtils.getString(com.newshunt.common.util.R.string.notification_cta_comment);
          Intent intent = new Intent();
          if (isSmallNotificaiton) {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_COMMENT_SMALL);
          } else {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_COMMENT);
          }
          intent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
          if (targetIntent.getExtras() != null) {
            intent.putExtras(targetIntent.getExtras());
          }
          intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, uniqueId);
          PendingIntent pendingContentIntent = PendingIntent.getBroadcast(
                  context, uniqueId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
          return new NotificationCtaUi(ctaString, R.drawable.ic_noti_action_comment, pendingContentIntent);
        }
        case JOIN: {
          String ctaString = CommonUtils.getString(com.newshunt.common.util.R.string.notification_cta_join);
          Intent intent = new Intent(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_JOIN);
          if (isSmallNotificaiton) {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_JOIN_SMALL);
          } else {
            intent.setAction(NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_JOIN);
          }
          intent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
          if (targetIntent.getExtras() != null) {
            intent.putExtras(targetIntent.getExtras());
          }
          intent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, uniqueId);
          PendingIntent pendingContentIntent = PendingIntent.getBroadcast(
                  context, uniqueId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
          return new NotificationCtaUi(ctaString, R.drawable.ic_noti_action_join_group, pendingContentIntent);
        }
      }
    }
    return null;
  }

  private void setTextForCta(RemoteViews contentView, NotificationCtaUi notificationCtaUi, int index) {
    if (index == 0) {
      contentView.setViewVisibility(R.id.cta_1, View.VISIBLE);
      contentView.setTextViewText(R.id.cta_1, notificationCtaUi.getCtaString());
      setFontSize(contentView, R.id.cta_1, notificationFontSize);
      contentView.setOnClickPendingIntent(R.id.cta_1, notificationCtaUi.getTargetIntent());
    } else if (index == 1) {
      contentView.setViewVisibility(R.id.cta_2, View.VISIBLE);
      contentView.setTextViewText(R.id.cta_2, notificationCtaUi.getCtaString());
      setFontSize(contentView, R.id.cta_2, notificationFontSize);
      contentView.setOnClickPendingIntent(R.id.cta_2, notificationCtaUi.getTargetIntent());
    } else if (index == 2) {
      contentView.setViewVisibility(R.id.cta_3, View.VISIBLE);
      contentView.setTextViewText(R.id.cta_3, notificationCtaUi.getCtaString());
      setFontSize(contentView, R.id.cta_3, notificationFontSize);
      contentView.setOnClickPendingIntent(R.id.cta_3, notificationCtaUi.getTargetIntent());
    } else if (index == 3) {
      contentView.setViewVisibility(R.id.cta_4, View.VISIBLE);
      contentView.setTextViewText(R.id.cta_4, notificationCtaUi.getCtaString());
      setFontSize(contentView, R.id.cta_4, notificationFontSize);
      contentView.setOnClickPendingIntent(R.id.cta_4, notificationCtaUi.getTargetIntent());
    }
  }

  private void setTextForCtaForImage(RemoteViews contentView, NotificationCtaUi notificationCtaUi, int index) {
    if (index == 0) {
      contentView.setViewVisibility(R.id.cta_1_image, View.VISIBLE);
      contentView.setImageViewBitmap(R.id.cta_1_image, getBitmapForCta(context, notificationCtaUi.getCtaString()));
      contentView.setOnClickPendingIntent(R.id.cta_1_image, notificationCtaUi.getTargetIntent());
    } else if (index == 1) {
      contentView.setViewVisibility(R.id.cta_2_image, View.VISIBLE);
      contentView.setImageViewBitmap(R.id.cta_2_image, getBitmapForCta(context, notificationCtaUi.getCtaString()));
      contentView.setOnClickPendingIntent(R.id.cta_2_image, notificationCtaUi.getTargetIntent());
    } else if (index == 2) {
      contentView.setViewVisibility(R.id.cta_3_image, View.VISIBLE);
      contentView.setImageViewBitmap(R.id.cta_3_image, getBitmapForCta(context, notificationCtaUi.getCtaString()));
      contentView.setOnClickPendingIntent(R.id.cta_3_image, notificationCtaUi.getTargetIntent());
    } else if (index == 3) {
      contentView.setViewVisibility(R.id.cta_4_image, View.VISIBLE);
      contentView.setImageViewBitmap(R.id.cta_4_image, getBitmapForCta(context, notificationCtaUi.getCtaString()));
      contentView.setOnClickPendingIntent(R.id.cta_4_image, notificationCtaUi.getTargetIntent());
    }
  }

  private void setupNotification(RemoteViews contentView) {
    if (notifyImage != null) {
      contentView.setImageViewBitmap(R.id.notify_image, notifyImage);
      contentView.setViewVisibility(R.id.notify_image, View.VISIBLE);
      contentView.setViewVisibility(R.id.notify_default_image,
              View.GONE);
      contentView.setViewVisibility(R.id.notification_logo,
              View.VISIBLE);
    } else {
      contentView.setImageViewResource(R.id.notify_default_image,
              R.drawable.notification_icon);
      contentView.setViewVisibility(R.id.notify_image, View.GONE);
      contentView.setViewVisibility(R.id.notify_default_image, View.VISIBLE);
      contentView.setViewVisibility(R.id.notification_logo, View.GONE);
    }
  }

    private RemoteViews setProcessProgressWithText(CreatePostBaseInfo model) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(),
                R.layout.remoteview_process_progress_layout);
        String displayMessage = NotificationUtils.getNotificationContentText(model);
        contentView.setTextViewText(R.id.process_title, displayMessage);
        setFontSize(contentView,R.id.process_title, notificationFontSize);
        int state = model.getState();
        contentView.setViewVisibility(R.id.retryBtn, View.GONE);
        switch (state) {
          case NotificationConstants.POST_UPLOAD_STATUS_FAILED: {
            contentView.setTextViewCompoundDrawables(R.id.process_title,0, 0, R.drawable.ic_post_error, 0);
            contentView.setViewVisibility(R.id.retryBtn, View.VISIBLE);
            contentView.setOnClickPendingIntent(R.id.retryBtn,
                getCpPendingIntent(model.getCpId(), model.getNotificationId()));
            break;
          }
          case NotificationConstants.POST_UPLOAD_STATUS_SUCCESS: {
            contentView.setTextViewCompoundDrawables(R.id.process_title,0, 0, R.drawable.ic_post_success, 0);
            break;
          }
        }
        contentView.setProgressBar(R.id.prog_indicator, 100, (int) model.getProgress(), false);
        return contentView;
    }

    private RemoteViews setAdjunctLangRemoteViews(AdjunctLangBaseInfo model) {
      RemoteViews contentView = new RemoteViews(context.getPackageName(),R.layout.notification_adjunct_langugae_cta);
      contentView.setImageViewBitmap(R.id.notify_image, notifyImage);
      contentView.setTextViewText(R.id.notification_text,model.getAdjText());
      contentView.setTextViewText(R.id.notification_subText,model.getDefaultText());

      contentView.setOnClickPendingIntent(R.id.cta_1,getAdjunctPendingIntent(model.getUniqueId(),false,model.getCrossDeeplinkUrl()));
      contentView.setOnClickPendingIntent(R.id.cta_2,getAdjunctPendingIntent(model.getUniqueId(),true,model.getTickDeeplinkUrl()));
      return contentView;
    }

  private RemoteViews getSmallContentView() {
    RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.remote_layout);
    String displayMessage = NotificationUtils.getNotificationContentText(navigationModel);

    boolean isUrdu = navigationModel.isUrdu();

    setupNotification(contentView);
    updateCtaForSmallNotification(contentView);

    contentView.setImageViewBitmap(R.id.notification_text_image,
        getBitmapOfTextViewWithUnicodeSupport(displayMessage,
            true, context, isUrdu));

    return contentView;
  }

  private NotificationCompat.Builder buildNotificationLayoutOfTypeBigText(boolean isNotificationUngroupingEnabled) {

    final CharSequence tickerText = AndroidUtils.getTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel));
    long currentTime = System.currentTimeMillis();
    int icon = R.mipmap.app_notification_icon;

    NotificationChannelGroupPair channelAndGroup = getChannelIdAndGroupId();
    String groupId = channelAndGroup.getGroupId();
    //for ungrouped case unique group ids will be always applied, however for grouped case application of unique vs server obtained groupId will be controlled by static config
    if(isNotificationUngroupingEnabled || !PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
      groupId = String.valueOf(navigationModel.getUniqueId()) + System.currentTimeMillis();
    }
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(context, channelAndGroup.getChannelId())
            .setSmallIcon(icon)
            .setTicker(tickerText)
            .setWhen(currentTime)
            .setGroup(groupId)
            .setPriority(navigationModel.getPriority())
            .setDeleteIntent(createOnDismissedIntent(context, navigationModel, false));

    RemoteViews contentView;

    boolean isSupportedLanugageFound = true;

    try {
      FEOutput output =
          FontHelper.convertToFontIndices(
              AndroidUtils.getTextFromHtml(navigationModel.getUniMsg()));
      isSupportedLanugageFound = output.isSupportedLanguageFound();
      output =
          FontHelper.convertToFontIndices(
              AndroidUtils.getTextFromHtml(navigationModel.getBigText()));
      isSupportedLanugageFound &= output.isSupportedLanguageFound();

    } catch (Exception e) {
      // Do not set content text since we dont know if there is any indic text.
    }


    if (isSupportedLanugageFound) {
      contentView = setBigNotificationView();
      notificationBuilder.setCustomContentView(contentView);
      notificationBuilder.setCustomBigContentView(contentView);
    } else {
      // Set text view based view
      contentView = setBigNotificationViewWithText();
      notificationBuilder.setCustomContentView(contentView);
      notificationBuilder.setCustomBigContentView(contentView);
      notificationBuilder.setContentText(AndroidUtils.getRichTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel)));
    }

    contentView.setViewVisibility(R.id.iv_big_picture, View.GONE);

    if (!CommonUtils.isEmpty(navigationModel.getBigText())) {
      isBigText = true;
      contentView.setViewVisibility(R.id.iv_big_Text, View.VISIBLE);
      if (isSupportedLanugageFound) {
        contentView.setImageViewBitmap(R.id.iv_big_Text,
            getBitmapOfTextViewWithUnicodeSupport(navigationModel.getBigText(), true, context,
                navigationModel.isUrdu()));
      } else {
        contentView.setTextViewText(R.id.iv_big_Text, AndroidUtils.getRichTextFromHtml(navigationModel.getBigText()));
        setFontSize(contentView, R.id.iv_big_Text, notificationFontSize);
      }
    } else {
      isBigText = false;
      contentView.setViewVisibility(R.id.iv_big_Text, View.GONE);
    }
    if(BuildConfig.DEBUG){
      HashMap<String, String> paramsMap = new HashMap<>();
      paramsMap.put(NhNotificationParam.NOTIF_DEV_EVENT_SUBTYPE.name(), "Notification configs");
      paramsMap.put("channel_id", channelAndGroup.getChannelId());
      paramsMap.put("channel_group_ID", channelAndGroup.getGroupId());
      paramsMap.put("notification_group_id", groupId);
      paramsMap.put("notification_priority", String.valueOf(navigationModel.getPriority()));
      AnalyticsHelper.logNotificationDevEvent(paramsMap);
    }
    return notificationBuilder;
  }

  private NotificationCompat.Builder buildNotificationLayoutOfTypeBigPicture(boolean isNotificationUngroupingEnabled) {

    final CharSequence tickerText = AndroidUtils.getTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel));
    long currentTime = System.currentTimeMillis();
    int icon = R.mipmap.app_notification_icon;
    NotificationChannelGroupPair channelAndGroup = getChannelIdAndGroupId();
    String groupId = channelAndGroup.getGroupId();
    //for ungrouped case unique group ids will be always applied, however for grouped case application of unique vs server obtained groupId will be controlled by static config
    if(isNotificationUngroupingEnabled || !PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
      groupId = String.valueOf(navigationModel.getUniqueId()) + System.currentTimeMillis();
    }

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(context, channelAndGroup.getChannelId())
            .setSmallIcon(icon)
            .setTicker(tickerText)
            .setGroup(groupId)
            .setWhen(currentTime)
            .setPriority(navigationModel.getPriority())
            .setDeleteIntent(createOnDismissedIntent(context, navigationModel, false));

    /*
     * This vibration code is added to enable heads up notification on devices running on API
     * level 21 or more.the vibration period is set to 0 as we don't want the users to get the
     * vibration.Permission check is added for SecurityException on a Samsung Android 4.
     * [source: http://stackoverflow.com/a/27587188/2014374], Since in few of the devices this
     * setting of vibration require the Vibration permission. Instead of permission will stop
     * Heads up feature for those device where it requires the permission.
     * https://www.fabric.io/verse-innovation-pvt-ltd--bangalore/android/apps/com.eterno/issues/
     * 58952eba0aeb16625bdfe1a8?time=last-thirty-days - Crash fix.
     */
    if (isVibratonOn) {
      notificationBuilder.setVibrate(new long[0]);
    }

    RemoteViews contentView = null;

    try {
      FEOutput output =
          FontHelper.convertToFontIndices(
              AndroidUtils.getTextFromHtml(navigationModel.getUniMsg()));
      if (output.isSupportedLanguageFound()) {
        contentView = setBigNotificationView();
        notificationBuilder.setCustomBigContentView(contentView);
        notificationBuilder.setCustomContentView(getSmallContentView());
      } else {
        // Set text view based view
        contentView = setBigNotificationViewWithText();
        notificationBuilder.setCustomBigContentView(contentView);
        notificationBuilder.setCustomContentView(getSmallContentViewWithText());
        notificationBuilder.setContentText(
            AndroidUtils.getRichTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel)));
      }
    } catch (Exception e) {
      // Do not set content text since we dont know if there is any indic text.
      contentView = setBigNotificationView();
      notificationBuilder.setCustomBigContentView(contentView);
      notificationBuilder.setCustomContentView(getSmallContentView());
    }

    contentView.setViewVisibility(R.id.iv_big_Text, View.GONE);

    if (bigPicture != null) {
      contentView.setImageViewBitmap(R.id.iv_big_picture, bigPicture);
      contentView.setViewVisibility(R.id.iv_big_picture, View.VISIBLE);
    } else {
      contentView.setViewVisibility(R.id.iv_big_picture, View.GONE);
    }

    if(BuildConfig.DEBUG){
      HashMap<String, String> paramsMap = new HashMap<>();
      paramsMap.put(NhNotificationParam.NOTIF_DEV_EVENT_SUBTYPE.name(), "Notification configs");
      paramsMap.put("channel_id", channelAndGroup.getChannelId());
      paramsMap.put("channel_group_ID", channelAndGroup.getGroupId());
      paramsMap.put("notification_group_id", groupId);
      paramsMap.put("notification_priority", String.valueOf(navigationModel.getPriority()));
      AnalyticsHelper.logNotificationDevEvent(paramsMap);
    }
    return notificationBuilder;
  }

  private NotificationCompat.Builder buildNotificationLayoutOfTypeBigTextInboxStyle(boolean isNotificationUngroupingEnabled) {

    final CharSequence tickerText = AndroidUtils.getTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel));
    long currentTime = System.currentTimeMillis();
    int icon = R.mipmap.app_notification_icon;

    NotificationChannelGroupPair channelAndGroup = getChannelIdAndGroupId();
    String groupId = channelAndGroup.getGroupId();
    //for ungrouped case unique group ids will be always applied, however for grouped case application of unique vs server obtained groupId will be controlled by static config
    if(isNotificationUngroupingEnabled || !PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
      groupId = String.valueOf(navigationModel.getUniqueId()) + System.currentTimeMillis();
    }

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(context, channelAndGroup.getChannelId())
            .setSmallIcon(icon)
            .setTicker(tickerText)
            .setGroup(groupId)
            .setWhen(currentTime)
            .setPriority(navigationModel.getPriority())
            .setDeleteIntent(createOnDismissedIntent(context, navigationModel, true));

    RemoteViews contentView = null;
    RemoteViews bigContentView = null;

    boolean isSupportedLanguageFound = true;
    try {
      FEOutput output =
          FontHelper.convertToFontIndices(
              AndroidUtils.getTextFromHtml(navigationModel.getUniMsg()));
      isSupportedLanguageFound = output.isSupportedLanguageFound();
    } catch (Exception ex) {
      // Do nothing. Use image as default
    }

    if (isSupportedLanguageFound) {
      contentView = setGroupedNotificationView(true);
      bigContentView = setGroupedNotificationView(false);
      notificationBuilder.setCustomContentView(contentView);
      notificationBuilder.setCustomBigContentView(bigContentView);
    } else {
      // Set text view based view
      contentView = setGroupedNotificationViewWithText(true);
      bigContentView = setGroupedNotificationViewWithText(false);
      notificationBuilder.setCustomContentView(contentView);
      notificationBuilder.setCustomBigContentView(bigContentView);
      notificationBuilder.setContentText(AndroidUtils.getRichTextFromHtml(NotificationUtils.getNotificationContentText(navigationModel)));
    }

    /*
     * This vibration code is added to enable heads up notification on devices running on API
     * level 21 or more.the vibration period is set to 0 as we don't want the users to get the
     * vibration.Permission check is added for SecurityException on a Samsung Android 4.
     * [source: http://stackoverflow.com/a/27587188/2014374], Since in few of the devices this
     * setting of vibration require the Vibration permission. Instead of permission will stop
     * Heads up feature for those device where it requires the permission.
     * https://www.fabric.io/verse-innovation-pvt-ltd--bangalore/android/apps/com.eterno/issues/
     * 58952eba0aeb16625bdfe1a8?time=last-thirty-days - Crash fix.
     */
    if (isVibratonOn) {
      notificationBuilder.setVibrate(new long[0]);
    }

    if (notificationList != null && notificationList.size() > 0) {
      //Showing msg of previous notification below title
      View secondLineMsg = getViewWithUnicodeSupport(notificationList.get(
          notificationList.size() - 1).getBaseInfo(), false);
      contentView.setViewVisibility(R.id.big_notification_msg_below_title, View.VISIBLE);
      contentView.setImageViewBitmap(R.id.big_notification_msg_below_title,
          secondLineMsg.getDrawingCache());

      //Forming grouped notifications
      bigContentView.setViewVisibility(R.id.iv_big_Text, View.VISIBLE);
      bigContentView.setImageViewBitmap(R.id.iv_big_Text,
          getInboxStyleTextViewWithUnicodeSupport(context, notificationList));
    }
    if(BuildConfig.DEBUG){
      HashMap<String, String> paramsMap = new HashMap<>();
      paramsMap.put(NhNotificationParam.NOTIF_DEV_EVENT_SUBTYPE.name(), "Notification configs");
      paramsMap.put("channel_id", channelAndGroup.getChannelId());
      paramsMap.put("channel_group_ID", channelAndGroup.getGroupId());
      paramsMap.put("notification_group_id", groupId);
      paramsMap.put("notification_priority", String.valueOf(navigationModel.getPriority()));
      AnalyticsHelper.logNotificationDevEvent(paramsMap);
    }
    return notificationBuilder;
  }

  private PendingIntent createOnDismissedIntent(Context context, BaseInfo navigationModel,
                                                boolean isInbox) {
    Intent intent = new Intent(context, NotificationDismissedReceiver.class);
    intent.putExtra(NotificationConstants.NOTIFICATION_MESSAGE_ID, navigationModel);
    if (isInbox) {
      intent.putExtra(NotificationConstants.NOTIFICATION_INBOX, isInbox);
    }
    Long requestCode = System.currentTimeMillis();
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(CommonUtils.getApplication(), requestCode.intValue(), intent, 0);
    return pendingIntent;
  }

  /**
   * Function will convert List of notification to Bitmap
   *
   * @param context current context of an view
   * @param notList List of notification BaseModel to Bundle together
   * @return bitmap which created for notification title
   * @Param isRTLalignment : is for Urdu or not
   */
  private Bitmap getInboxStyleTextViewWithUnicodeSupport(Context context,
                                                         List<BaseModel> notList) {
    LinearLayout llLayout = (LinearLayout) LayoutInflater.from(context).inflate(
        R.layout.notification_inbox_style_layout, null);
    llLayout.setDrawingCacheEnabled(true);
    llLayout.buildDrawingCache();
    for (int i = notList.size() - 1; i >= 0; i--) {
      llLayout.addView(getViewWithUnicodeSupport(notList.get(i).getBaseInfo(), false));
    }
    llLayout.measure(
        View.MeasureSpec.makeMeasureSpec(getNotificationWidth(), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    llLayout.layout(0, 0, getNotificationWidth(), llLayout.getMeasuredHeight());

    return llLayout.getDrawingCache();
  }

  /**
   * Function will convert TextView to Bitmap
   *
   * @param text      text to be put on the Bitmap with NewsHunt Font
   * @param isUnicode is an Unicode Text (to identify english or regional)
   * @param context   current context of an view   *
   * @return bitmap which created for notification title
   * @Param isRTLalignment : is for Urdu or not
   */
  private Bitmap getBitmapOfTextViewWithUnicodeSupport(String text,
                                                       boolean isUnicode, Context context,
                                                       boolean isRTLalignment) {
    // Provide it with a layout params. It should necessarily be wrapping
    // the content as we not really going to have a parent for it.
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);

    // CALCULATION OF OFFSET
    // remote.xml :: notify_image occupies 55dp :: offset made 73dp
    // notification_text_image 2dp margin
    // Left margin given for left 10dp
    // notification small logo size 16dp+3margin = 19dp
    int offset =
        CommonUtils.getPixelFromDP(NotificationConstants.NOTIFICATION_TITLE_START_OFFSET, context);
    int screenWidth = CommonUtils.getDeviceScreenWidth();
    int screenHeight = CommonUtils.getDeviceScreenHeight();

    // Max limit for the notification text width.
    int maxWidth =
        CommonUtils.getPixelFromDP(NotificationConstants.NOTIFICATION_TITLE_MAX_WIDTH, context);

    // Finds the minimum value from maxWidth, screenWidth and screenHeight, to limit the
    // notification text width value.
    int widthImg = Math.min(maxWidth, Math.min(screenWidth, screenHeight)) - offset;
    View textLayout = LayoutInflater.from(context).inflate(R.layout.notification_textview_layout, null);

    TextView textView = textLayout.findViewById(R.id.notification_title);

    if (isRTLalignment) {
      params.gravity = Gravity.RIGHT;
      textView.setLayoutParams(params);
      setRTL(textView);
    } else {
      params.gravity = Gravity.LEFT;
      textView.setLayoutParams(params);
    }

    if (isBigText) {
      textView.setMaxLines(NotificationConstants.NOTIFICATION_BIG_TEXT_MAX_LINES);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
          NotificationConstants.NOTIFICATION_BIG_TEXT_FONT_SIZE);
      textView.setTextColor(context.getResources().getColor(R.color.big_notification_text_color));
    } else {
      textView.setMaxLines(NotificationConstants.NOTIFICATION_TITLE_MAX_LINES);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
          NotificationConstants.NOTIFICATION_TITLE_FONT_SIZE);
    }

    textView.setText(AndroidUtils.getRichTextFromHtml(text));
    textView.measure(
        View.MeasureSpec.makeMeasureSpec(widthImg, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

    int height = textView.getMeasuredHeight();
    textView.layout(0, 0, widthImg, height);
    textView.setGravity(Gravity.CENTER_VERTICAL);
    textView.setEllipsize(TextUtils.TruncateAt.END);
    textView.setDrawingCacheEnabled(true);
    textView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

    return textView.getDrawingCache();
  }

  /**
   * Function will convert LinearLayout to Bitmap
   *
   * @param context   current context of an view   *
   * @return bitmap which created for notification title
   * @Param isRTLalignment : is for Urdu or not
   */
  private Bitmap getBitmapForCta(Context context, String ctaString) {
    // Provide it with a layout params. It should necessarily be wrapping
    // the content as we not really going to have a parent for it.
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);

    // CALCULATION OF OFFSET
    // remote.xml :: notify_image occupies 55dp :: offset made 73dp
    // notification_text_image 2dp margin
    // Left margin given for left 10dp
    // notification small logo size 16dp+3margin = 19dp
//    int offset =
//            CommonUtils.getPixelFromDP(16, context);
    int screenWidth = CommonUtils.getDeviceScreenWidth();
    int screenHeight = CommonUtils.getDeviceScreenHeight();

    // Max limit for the notification text width.
    int maxWidth = CommonUtils.getDeviceScreenWidth()/4;

    // Finds the minimum value from maxWidth, screenWidth and screenHeight, to limit the
    // notification text width value.
    int widthImg = Math.min(maxWidth, Math.min(screenWidth, screenHeight));

    View textLayout = LayoutInflater.from(context).inflate(R.layout.notification_cta_textview_layout, null);
    TextView textView = textLayout.findViewById(R.id.cta_text);
    textView.setText(ctaString);
    if(notificationFontSize > 0){
      textView.setTextSize(notificationFontSize);
    }

    textLayout.setLayoutParams(params);
    textView.measure(
            View.MeasureSpec.makeMeasureSpec(widthImg, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

    int height = textView.getMeasuredHeight();
    textView.layout(0, 0, widthImg, height);

    textView.setDrawingCacheEnabled(true);
    textView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

    return textView.getDrawingCache();
  }

  private void setRTL(TextView textView) {
    textView.setTextDirection(TextView.TEXT_DIRECTION_ANY_RTL);
  }


  /**
   * Big Picture and Big Text View
   */
  private RemoteViews setBigNotificationViewWithText() {
    RemoteViews contentView =
        new RemoteViews(context.getPackageName(), R.layout.big_notification_layout_with_textview);
    String displayMessage = NotificationUtils.getNotificationContentText(navigationModel);

    if (notifyImage != null) {
      contentView.setImageViewBitmap(R.id.notify_image, notifyImage);
      contentView.setViewVisibility(R.id.notify_image, View.VISIBLE);
      contentView.setViewVisibility(R.id.notify_default_image, View.GONE);
      contentView.setViewVisibility(R.id.notification_logo, View.VISIBLE);
    } else {
      contentView.setViewVisibility(R.id.notify_image, View.GONE);
      contentView.setViewVisibility(R.id.notify_default_image, View.VISIBLE);
      contentView.setViewVisibility(R.id.notification_logo, View.GONE);
    }

    //converting Title text to image to support different language strings
    contentView.setTextViewText(R.id.big_notification_text, AndroidUtils.getRichTextFromHtml(displayMessage));
    setFontSize(contentView, R.id.big_notification_text, notificationFontSize);
    return contentView;
  }

  /**
   * Big Picture and Big Text View
   */
  private RemoteViews setBigNotificationView() {
    RemoteViews contentView =
        new RemoteViews(context.getPackageName(), R.layout.big_notification_layout);
    String displayMessage = NotificationUtils.getNotificationContentText(navigationModel);

    boolean isUrdu = navigationModel.isUrdu();

    if (notifyImage != null) {
      contentView.setImageViewBitmap(R.id.notify_image, notifyImage);
      contentView.setViewVisibility(R.id.notify_image, View.VISIBLE);
      contentView.setViewVisibility(R.id.notify_default_image, View.GONE);
      contentView.setViewVisibility(R.id.notification_logo, View.VISIBLE);
    } else {
      contentView.setViewVisibility(R.id.notify_image, View.GONE);
      contentView.setViewVisibility(R.id.notify_default_image, View.VISIBLE);
      contentView.setViewVisibility(R.id.notification_logo, View.GONE);
    }

    //converting Title text to image to support different language strings
    contentView.setImageViewBitmap(R.id.big_notification_text_image,
        getBitmapOfTextViewWithUnicodeSupport(displayMessage, true,
            context,
            isUrdu));
    return contentView;
  }

  private RemoteViews setGroupedNotificationViewWithText(Boolean isTitleLayout) {
    RemoteViews contentView = null;
    if (isTitleLayout) {
      contentView = new RemoteViews(context.getPackageName(), R.layout
          .big_notification_grouped_title_with_textview);
    } else {
      contentView = new RemoteViews(context.getPackageName(), R.layout
          .big_notification_grouped_with_textview);
    }

    contentView.setImageViewResource(R.id.notify_default_image,
        R.drawable.notification_icon);
    contentView.setViewVisibility(R.id.notify_default_image, View.VISIBLE);
    contentView.setImageViewBitmap(R.id.big_notification_text_image,
        getViewWithUnicodeSupport(navigationModel, true).getDrawingCache());

    return contentView;
  }

  /**
   * Grouped Inbox Style Notification View
   */
  private RemoteViews setGroupedNotificationView(Boolean isTitleLayout) {
    RemoteViews contentView = null;
    if (isTitleLayout) {
      contentView = new RemoteViews(context.getPackageName(), R.layout
          .big_notification_grouped_title);
    } else {
      contentView = new RemoteViews(context.getPackageName(), R.layout
          .big_notification_grouped);
    }

    contentView.setImageViewResource(R.id.notify_default_image,
        R.drawable.notification_icon);
    contentView.setViewVisibility(R.id.notify_default_image, View.VISIBLE);

    contentView.setImageViewBitmap(R.id.big_notification_text_image,
        getViewWithUnicodeSupport(navigationModel, true).getDrawingCache());

    return contentView;
  }

  /**
   * Function will convert TextView to Bitmap
   *
   * @param baseInfo basic information of notification
   * @return bitmap which created for notification title
   */

  private View getViewWithUnicodeSupport(BaseInfo baseInfo, Boolean isTitle) {
    FrameLayout flLayout = null;
    if (baseInfo.isUrdu()) {
      flLayout = (FrameLayout) LayoutInflater.from(context).inflate(
          R.layout.notification_text_entry_urdu, null);
    } else {
      flLayout = (FrameLayout) LayoutInflater.from(context).inflate(
          R.layout.notification_text_entry, null);
    }

    TextView textView = flLayout.findViewById(R.id.grouped_textView);
    if (baseInfo.isUrdu()) {
      //Learning: setSingleLine() cause problem for urdu, so using setMaxLines
      textView.setMaxLines(1);
      setRTL(textView);
    } else {
      //Learning: setMaxLines(1) was causing issue of variable line height and hence made it setSingleLine();
      textView.setSingleLine();
    }
    textView.setGravity(Gravity.CENTER_VERTICAL);
    textView.setIncludeFontPadding(false);
    if (isTitle) {
      if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
        textView.setTextColor(CommonUtils.getColor(R.color.big_notification_title_color));
      } else {
        //this is to make compatible with android q dark mode as applied style not working in Custom TextView
        textView.setTextColor(CommonUtils.getColor(R.color.big_notification_text_color));
      }
      textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
          NotificationConstants.NOTIFICATION_TITLE_FONT_SIZE);
    } else {
      textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
          NotificationConstants.NOTIFICATION_GROUPED_TEXT_SIZE);
      textView.setTextColor(CommonUtils.getColor(R.color.big_notification_text_color));
      textView.setPadding(0, 2, 0, 0);
    }

    try{
      textView.setText(AndroidUtils.getRichTextFromHtml(NotificationUtils.getNotificationContentText(baseInfo)));
    } catch(Exception ex) {
      textView.setText(AndroidUtils.getTextFromHtml(NotificationUtils.getNotificationContentText(baseInfo)));
      Logger.caughtException(ex);
    }

    flLayout.measure(
        View.MeasureSpec.makeMeasureSpec(getNotificationWidth(), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    int height = flLayout.getMeasuredHeight();
    flLayout.layout(0, 0, getNotificationWidth(), height);
    flLayout.setDrawingCacheEnabled(true);
    flLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

    return flLayout;
  }

  private int getNotificationWidth() {
    if (notificationTextWidth > 0) {
      return notificationTextWidth;
    }
    // CALCULATION OF OFFSET
    // remote.xml :: notify_image occupies 55dp :: offset made 73dp
    // notification_text_image 2dp margin
    // Left margin given for left 10dp
    // notification small logo size 16dp+3margin = 19dp
    int offset =
        CommonUtils.getPixelFromDP(NotificationConstants.NOTIFICATION_TITLE_START_OFFSET, context);
    int screenWidth = CommonUtils.getDeviceScreenWidth();
    int screenHeight = CommonUtils.getDeviceScreenHeight();

    // Max limit for the notification text width.
    int maxWidth =
        CommonUtils.getPixelFromDP(NotificationConstants.NOTIFICATION_TITLE_MAX_WIDTH, context);

    // Finds the minimum value from maxWidth, screenWidth and screenHeight, to limit the
    // notification text width value.
    notificationTextWidth = Math.min(maxWidth, Math.min(screenWidth, screenHeight)) - offset;

    return notificationTextWidth;
  }

  private void setFontSize(RemoteViews view, int textViewId, float notificationFontSize){
    float fontSize = notificationFontSize;
    if(notificationFontSize <= 0){
      fontSize = CommonUtils.getDimension(R.dimen.default_notification_text_size);
    }
    view.setTextViewTextSize(textViewId, TypedValue.COMPLEX_UNIT_SP, fontSize);
  }

  @NonNull
  private NotificationChannelGroupPair getChannelIdAndGroupId() {
    String channelId = CommonUtils.isEmpty(navigationModel.getChannelId()) ?
        NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID :
        navigationModel.getChannelId();
    String groupId = NotificationConstants.NOTIFICATION_DEFAULT_GROUP_NAME;
    try {
      groupId = NotificationUtils.getGroup(channelId);
    } catch (ChannelNotFoundException e) {
      channelId = NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID;
      Logger.caughtException(e);
    }
    return new NotificationChannelGroupPair(channelId, groupId);
  }

  @NonNull
  private NotificationChannelGroupPair getChannelIdAndGroupIdForCreatePost() {
    String channelName = NotificationConstants.CREATE_POST_NOTIFICATION_CHANNEL_TEXT;
    if (navigationModel instanceof CreatePostBaseInfo) {
      CreatePostBaseInfo model = (CreatePostBaseInfo) navigationModel;
      if (model.isImageAttached()) {
        channelName = NotificationConstants.CREATE_POST_NOTIFICATION_CHANNEL_IMAGE;
      }
    }
    int notificationPriority;
    if (NotificationConstants.CREATE_POST_NOTIFICATION_CHANNEL_IMAGE.equals(channelName)) {
      notificationPriority = NotificationManager.IMPORTANCE_HIGH;
    } else {
      notificationPriority = NotificationManager.IMPORTANCE_LOW;
    }
    NotificationDefaultChannelHelperKt.createPostChannelIfNotExist(channelName,
        notificationPriority);
    return new NotificationChannelGroupPair(channelName, channelName);
  }

  @NonNull
  private NotificationChannelGroupPair getChannelIdAndGroupIdForAdjunctLang() {
    String channelName = NotificationConstants.CREATE_POST_NOTIFICATION_CHANNEL_ADJUNCT_LANG;
    int notificationPriority = NotificationManager.IMPORTANCE_MAX;
    NotificationDefaultChannelHelperKt.createPostChannelIfNotExist(channelName,
            notificationPriority);
    return new NotificationChannelGroupPair(channelName, channelName);
  }

  private PendingIntent getCpPendingIntent(long cpId, int notificationId) {
    Intent retryIntent = new Intent();
    retryIntent.setComponent(new ComponentName(CommonUtils.getApplication().getPackageName(),
        "com.newshunt.appview.common.postcreation.view.receiver.CreatePostReceiver"));
    retryIntent.setAction(NotificationConstants.CREATE_POST_ACTION_RETRY);
    retryIntent.putExtra(NotificationConstants.CREATE_POST_NOTIFICATION_ID, notificationId);
    retryIntent.putExtra(NotificationConstants.CREATE_POST_ID, cpId);
    return PendingIntent.getBroadcast(context, (int) cpId, retryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private PendingIntent getAdjunctPendingIntent(int notiId,boolean tick,String deeplinkUrl) {
    Intent adjunctIntent = new Intent();
    adjunctIntent.setComponent(new ComponentName(CommonUtils.getApplication(), NotificationCtaReceiver.class));
    adjunctIntent.setAction(NotificationConstants.ADJUNCT_LANG_ACTION);
    adjunctIntent.putExtra(NotificationConstants.ADJUNCT_NOTI_ID,notiId);
    adjunctIntent.putExtra(NotificationConstants.ADJUNCT_NOTI_ACTION_TICK,tick);
    adjunctIntent.putExtra(NotificationConstants.ADJUNCT_CTA_DEEPLINK_URL,deeplinkUrl);
    adjunctIntent.putExtras(targetIntent);
    return PendingIntent.getBroadcast(context,(int)System.currentTimeMillis(),adjunctIntent,PendingIntent.FLAG_CANCEL_CURRENT);
  }


}
