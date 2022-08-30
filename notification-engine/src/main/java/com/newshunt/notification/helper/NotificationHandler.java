/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper;

import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_ACTION;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_FLUSH;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_STICKY;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_VERSION_TRIGGER;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V2;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V3;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V4;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V5;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V6;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.common.helper.common.ApplicationStatus;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationAttributeItem;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationConfig;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NotificationCtaObj;
import com.newshunt.dataentity.notification.AdjunctLangNavModel;
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.DeeplinkModel;
import com.newshunt.dataentity.notification.FlushNavModel;
import com.newshunt.dataentity.notification.InAppNotificationModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism;
import com.newshunt.dataentity.notification.NotificationSectionType;
import com.newshunt.dataentity.notification.SilentNotificationModel;
import com.newshunt.dataentity.notification.SilentVersionedApiTriggerModel;
import com.newshunt.dataentity.notification.SilentVersionedApiUpdateModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.deeplink.Deeplinker;
import com.newshunt.dhutil.helper.NotificationActionExecutionServiceImpl;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.helper.KillProcessAlarmManager;
import com.newshunt.helper.NotificationUniqueIdGenerator;
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility;
import com.newshunt.notification.model.entity.NotificationFilterType;
import com.newshunt.notification.model.entity.NotificationInvalidType;
import com.newshunt.notification.model.parser.NotificationMessageParser;

import java.net.URLEncoder;
import java.util.List;

import static com.newshunt.dataentity.notification.util.NotificationConstants.MESSAGE_V5;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_ACTION;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_ADJUNCT_LANG_PUSH;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_ADJUNCT_USER_TO_SYS_PUSH;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_FLUSH;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_FLUSH_BLACKLIST_LANGUAGE;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_TYPE_STICKY;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V2;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V3;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V4;
import static com.newshunt.dataentity.notification.util.NotificationConstants.NOTIFICATION_VERSION_V5;


/**
 * This class will be called in case of both PULL and PUSH notifications.
 *
 * @author anshul.jain on 11/7/2016.
 */

public class NotificationHandler {

  private final static String TAG = "NotificationHandler";

  public static void handleNotificationData(NotificationDeliveryMechanism notificationType,
                                            Bundle data, boolean isFullSync, int filterValue) {
    KillProcessAlarmManager.onAppProcessInvokedInBackground();

    if (isActionableNotification(data)) {
      if (NotificationDeliveryMechanism.PULL == notificationType) {
        // Do not handle actions from pull framework
        NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
            NotificationFilterType.INVALID, NotificationInvalidType.PUSH_ACTION.getType());
        return;
      }

      // Do not persist or post actionable notifications
      NotificationActionExecutionServiceImpl.INSTANCE.getInstance().handleActionableNotification(data, null);
      return;
    }
    else {
      NotificationActionExecutionServiceImpl.INSTANCE.getInstance().executePendingAction(GenericAppStatePreference.NEXT_NOTIFICATION_ACTION);
    }

    if (data.isEmpty()) {
      Logger.d(TAG, "Received notification bundle as empty");
      AnalyticsClient.log(NhAnalyticsAppEvent.NOTIFICATION_DELIVERED,
          NhAnalyticsEventSection.NOTIFICATION, null);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
          NotificationFilterType.INVALID, NotificationInvalidType.EMPTY_BUNDLE.getType());
      return;
    }

    //We're saving timeStamp immediately on receive, to avoid any miss ordering
    long notificationTimeStamp = System.currentTimeMillis();
    NotificationLogger.logNotificationReceived(data, notificationTimeStamp);
    String version = data.getString("version");
    String msgType = data.getString("type");
    // Don't process notification if user has disabled it
    boolean isEnabled =
        !(!PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true));
    if (!isEnabled && !NOTIFICATION_VERSION_V6.equals(version)) {
      if (!isFullSync) {
        Logger.d(TAG, "Notification discarded as user disabled from settings");
        AnalyticsClient.log(NhAnalyticsAppEvent.NOTIFICATION_DELIVERED,
            NhAnalyticsEventSection.NOTIFICATION, null);
        NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
            NotificationFilterType.NOTIFICATION_DISABLED_HAMBURGER);
      } else {
        Logger.i(TAG, "Inbox pull should not trigger deliver event");
      }
      return;
    }

    if (null == ApplicationStatus.getAppLaunchMode()) {
      setIsPushNotificationWorkingInBg(true);
    }

    if (notificationType.equals(NotificationDeliveryMechanism.PUSH)) {
      PullNotificationsHelper.handlePushNotification();
    }

    if (NOTIFICATION_VERSION_V5.equalsIgnoreCase(version) &&
        NOTIFICATION_TYPE_STICKY.equals(msgType)) {
      Logger.d(TAG, "Discarding invalid notification. Sticky not supported by client");
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
          .INVALID, NotificationInvalidType.INVALID_NOTIFICATION_TYPE + " Sticky ", null);
    } else if (NOTIFICATION_VERSION_V5.equalsIgnoreCase(version) && NOTIFICATION_TYPE_FLUSH.equals(msgType)) {
      Logger.d(TAG, "Received the flush notification");
      handleFlushNotification(notificationType, data, isFullSync);
    } else if(NOTIFICATION_VERSION_V5.equalsIgnoreCase(version) && (NOTIFICATION_TYPE_ADJUNCT_LANG_PUSH.equals(msgType) ||
            NOTIFICATION_TYPE_ADJUNCT_USER_TO_SYS_PUSH.equals(msgType) || NOTIFICATION_TYPE_FLUSH_BLACKLIST_LANGUAGE.equals(msgType)))  {
      Logger.d(TAG, "Received the adjunct lang update notification");
      handleAdjunctLangSilentNotification(notificationType,data,isFullSync,msgType);
    } else if (CommonUtils.equals(version,
        NOTIFICATION_VERSION_V3) || CommonUtils.equals
        (version, NOTIFICATION_VERSION_V4) || CommonUtils.equals(version, NOTIFICATION_VERSION_V5)) {
      handleDeepLinkNotifications(notificationType, data, isFullSync,
          filterValue);
    } else if (CommonUtils.equals(version, NOTIFICATION_VERSION_V2)) {
      handleV2Notifications(notificationType, data, notificationTimeStamp, msgType);
    } else if(CommonUtils.equals(version, NOTIFICATION_VERSION_V6)) {
      if (NOTIFICATION_TYPE_STICKY.equals(msgType)) {
        handleV6Notification(notificationType, data, isFullSync, notificationTimeStamp, filterValue);
      } else if (CommonUtils.equalsIgnoreCase(NotificationConstants.NOTIFICATION_TYPE_VERSION_UPDATE, msgType)) {
        handleSilentVersionApiUpdate(data);
      } else if(CommonUtils.equalsIgnoreCase(NotificationConstants.NOTIFICATION_TYPE_IN_APP,msgType)) {
        handleInAppNotification(notificationType,data,isFullSync);
      }else if(CommonUtils.equalsIgnoreCase(NotificationConstants.NOTIFICATION_TYPE_ADJUNCT_STICKY,msgType)) {
        handleAdjunctStickyNotification(notificationType,data,isFullSync);
      } else if (CommonUtils.equalsIgnoreCase(NOTIFICATION_TYPE_VERSION_TRIGGER, msgType)) {
        handleVersionedApiUpdateTrigger(data);
      }

    } else {
      handleV1AndDefaultNotifications(notificationType, data, notificationTimeStamp);
    }
  }

  private static boolean isActionableNotification(Bundle data) {
    if (data == null || !data.containsKey("type")) {
      return false;
    }

    String msgType = data.getString("type");
    return NOTIFICATION_TYPE_ACTION.equals(msgType);
  }

  private static void handleV1AndDefaultNotifications(
      NotificationDeliveryMechanism notificationType, Bundle data, long notificationTimeStamp) {
    final NavigationModel notification =
        NotificationMessageParser.parseNotificationMessage(data, notificationTimeStamp);
    if (notification == null || notification.getNotificationSectionType() == null ||
        NotificationSectionType.BOOKS.equals(notification.getNotificationSectionType())) {
      Logger.d(TAG, "Discarding invalid notification");
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
          .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }

    notification.setDeliveryType(notificationType);
    notification.setIsSynced(notificationType == NotificationDeliveryMechanism.PULL);
    BusProvider.postOnUIBus(notification);
  }

  private static void handleV2Notifications(NotificationDeliveryMechanism notificationType,
                                            Bundle data, long notificationTimeStamp,
                                            String msgType) {
    if (msgType != null) {
      switch (msgType) {
        case NotificationConstants.NOTIFICATION_TYPE_NEWS: {
          final NewsNavModel notification =
              NotificationMessageParser.parseNewsNotificationMessage(data, notificationTimeStamp);
          if (null == notification || notification.getBaseInfo() == null) {
            String payload = JsonUtils.getJsonString(data);
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
                    .INVALID,
                NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
            return;
          }
          notification.getBaseInfo().setDeliveryType(notificationType);
          notification.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism
              .PULL);
          if (notification.getBaseInfo().isApplyLanguageFilter() && !NotificationUtils
              .isNotificationLanguageValid(notification)) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(notification,
                NotificationFilterType.INVALID_LANGUAGE);
            return;
          }
          BusProvider.postOnUIBus(notification);
          break;
        }
        case NotificationConstants.NOTIFICATION_TYPE_BOOKS: {
          Logger.d(TAG, "Discarding invalid notification");
          NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
              .INVALID, NotificationInvalidType.INVALID_SECTION_TYPE + " Books ", null);
          break;
        }
        case NotificationConstants.NOTIFICATION_TYPE_LIVETV:
          final LiveTVNavModel liveTvNotification =
              NotificationMessageParser.parseLiveTVNotificationMessage(data,
                  notificationTimeStamp);
          if (null == liveTvNotification || liveTvNotification.getBaseInfo() == null) {
            return;
          }
          liveTvNotification.getBaseInfo().setDeliveryType(notificationType);
          liveTvNotification.getBaseInfo()
              .setIsSynced(notificationType == NotificationDeliveryMechanism
                  .PULL);
          if (liveTvNotification.getBaseInfo().isApplyLanguageFilter() && !NotificationUtils
              .isNotificationLanguageValid(liveTvNotification)) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(liveTvNotification,
                NotificationFilterType.INVALID_LANGUAGE);
            return;
          }
          BusProvider.postOnUIBus(liveTvNotification);
          break;
        case NotificationConstants.NOTIFICATION_TYPE_TV:
          final TVNavModel notification =
              NotificationMessageParser.parseTVNotificationMessage(data,
                  notificationTimeStamp);
          if (null == notification || notification.getBaseInfo() == null) {
            String payload = JsonUtils.getJsonString(data);
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
                    .INVALID,
                NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
            return;
          }
          notification.getBaseInfo().setDeliveryType(notificationType);
          notification.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism
              .PULL);
          if (notification.getBaseInfo().isApplyLanguageFilter() && !NotificationUtils
              .isNotificationLanguageValid(notification)) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(notification,
                NotificationFilterType.INVALID_LANGUAGE);
            return;
          }
          BusProvider.postOnUIBus(notification);
          break;
      }
    }
  }

  private static void handleDeepLinkNotifications(NotificationDeliveryMechanism notificationType,
                                                  Bundle data, boolean isFullSync,
                                                  int filterValue) {
    final DeeplinkModel notification = NotificationMessageParser
        .parseDeeplinkNotificationMessage(data);
    if (notification == null || notification.getBaseInfo() == null) {
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
          .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }
    notification.getBaseInfo().setDeliveryType(notificationType);
    notification.getBaseInfo().setType(data.getString(NotificationConstants.TYPE));
    notification.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism
        .PULL);
    if (notification.getBaseInfo().isApplyLanguageFilter() && !NotificationUtils
        .isNotificationLanguageValid(notification)) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(notification,
          NotificationFilterType.INVALID_LANGUAGE);
      return;
    }

    notification.setFullSync(isFullSync);
    notification.setFilterValue(filterValue);
    BusProvider.postOnUIBus(notification);
  }

  public static BaseModel handleDeepLinkNotifications(NotificationDeliveryMechanism notificationType, String data, DeeplinkModel parsedModel){
    try{

      DeeplinkModel deeplinkModel;
      if(CommonUtils.isEmpty(data) && parsedModel == null) {
        return null;
      }

      if(CommonUtils.isEmpty(data)) {
        deeplinkModel = parsedModel;
      }else {
        deeplinkModel = JsonUtils.fromJson(data, DeeplinkModel.class);
      }

      NotificationUtils.setLayoutType(deeplinkModel.getBaseInfo());
      deeplinkModel.getBaseInfo().setDeliveryType(notificationType);
      deeplinkModel.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism
          .PULL);
      if (deeplinkModel.getBaseInfo().isApplyLanguageFilter() && !NotificationUtils
          .isNotificationLanguageValid(deeplinkModel)) {
        NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(deeplinkModel,
            NotificationFilterType.INVALID_LANGUAGE);
        return null;
      }

      deeplinkModel.setFullSync(false);
      deeplinkModel.setFilterValue(NotificationSyncHelperKt.NOTIFICATION_FILTER_ALL);
      String deeplinkUrl = deeplinkModel.getDeeplinkUrl();

      BaseModel baseModel = Deeplinker.parseDeeplinkModel(deeplinkModel.getDeeplinkUrl(), deeplinkModel);
      baseModel.setFilterValue(NotificationSyncHelperKt.NOTIFICATION_FILTER_ALL);
      baseModel.setDescription(deeplinkModel.getDescription());
      baseModel.getBaseInfo().setLanguage(deeplinkModel.getBaseInfo().getLanguage());
      return baseModel;
    }catch(Exception ex){
      Logger.caughtException(ex);
      return null;

    }
  }

  private static void handleFlushNotification(NotificationDeliveryMechanism notificationType,
                                              Bundle data, boolean isFullSync) {
    if (isFullSync) {
      Logger.d(TAG, "Flush notification in the full sync is ignored");
      return;
    }

    final FlushNavModel flushNavModel = NotificationMessageParser.parseFlushNotificationMessage(data);
    if (flushNavModel == null || flushNavModel.getBaseInfo() == null) {
      Logger.d(TAG, "Parsing the flush notification failed");
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
              .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }
    flushNavModel.getBaseInfo().setDeliveryType(notificationType);
    flushNavModel.getBaseInfo().setType(data.getString(NotificationConstants.TYPE));
    flushNavModel.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism.PULL);
    flushNavModel.setFullSync(isFullSync);
    flushNavModel.setsType(Constants.EMPTY_STRING+NavigationType.DELETE_NOTIFICATIONS.getIndex());
    BusProvider.postOnUIBus(flushNavModel);

  }

  private static void handleV6Notification(NotificationDeliveryMechanism notificationType, Bundle data, boolean isFullSync, long timestamp, int filterValue){
    final SilentNotificationModel notification = NotificationMessageParser.parseSilentNotification(data, timestamp);
    if (notification == null || notification.getBaseInfo() == null) {
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
          .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }

    notification.getBaseInfo().setDeliveryType(notificationType);
    notification.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism
        .PULL);
    notification.setFullSync(isFullSync);
    notification.setFilterValue(filterValue);
    notification.setStickyItemType(NotificationConstants.STICKY_NONE_TYPE);
    BusProvider.postOnUIBus(notification);
  }

  public static void handleSilentVersionApiUpdate(Bundle data) {
    SilentVersionedApiUpdateModel model = NotificationMessageParser.parseSilentVersionedApiUpdateModel(data);
    if (model == null || model.getBaseInfo() == null) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType.INVALID,
              NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType());
      return;
    }
    BusProvider.postOnUIBus(model);
  }

  public static void handleVersionedApiUpdateTrigger(Bundle data) {
    SilentVersionedApiTriggerModel model = NotificationMessageParser.parseSilentVersionedApiTriggerModel(data);
    if (model == null || model.getBaseInfo() == null) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType.INVALID,
              NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType());
      return;
    }
    BusProvider.postOnUIBus(model);
  }

  public static void handleAdjunctStickyNotification(NotificationDeliveryMechanism notificationType,
                                                     Bundle data,boolean isFullSync) {
    if(isFullSync) {
      Logger.d(TAG,"Adjunct lang update notification in full sync is ignored");
      return;
    }
    final AdjunctLangStickyNavModel adjunctLangNavModel = NotificationMessageParser.parseAdjunctLangStickyNotificationMessage(data);
    if(adjunctLangNavModel == null || adjunctLangNavModel.getBaseInfo() == null) {
      Logger.d(TAG, "Parsing the adjunct silent notification failed");
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
              .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }
    adjunctLangNavModel.getBaseInfo().setDeliveryType(notificationType);
    adjunctLangNavModel.getBaseInfo().setType(data.getString(NotificationConstants.TYPE));
    adjunctLangNavModel.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism.PULL);
    adjunctLangNavModel.setFullSync(isFullSync);
    adjunctLangNavModel.setsType(Constants.EMPTY_STRING+NavigationType.TYPE_OPEN_NEWSITEM_ADJUNCT_STICKY.getIndex());
    adjunctLangNavModel.getBaseInfo().setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForAdjunctSticky(adjunctLangNavModel));
    BusProvider.postOnUIBus(adjunctLangNavModel);
  }

  public static void handleInAppNotification(NotificationDeliveryMechanism notificationType,
                                                     Bundle data,boolean isFullSync) {
    if(isFullSync) {
      Logger.d(TAG,"In App notification in full sync is ignored");
      return;
    }
    final InAppNotificationModel inAppNotificationModel = NotificationMessageParser.parseInAppNotificationMessage(data);
    if(inAppNotificationModel == null || inAppNotificationModel.getBaseInfo() == null) {
      Logger.d(TAG, "Parsing the in app silent notification failed");
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
              .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }
    inAppNotificationModel.getBaseInfo().setDeliveryType(notificationType);
    inAppNotificationModel.getBaseInfo().setType(data.getString(NotificationConstants.TYPE));
    inAppNotificationModel.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism.PULL);
    inAppNotificationModel.setFullSync(isFullSync);
    inAppNotificationModel.setsType(Constants.EMPTY_STRING+NavigationType.IN_APP.getIndex());
    inAppNotificationModel.getBaseInfo().setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForInApp(inAppNotificationModel));
    BusProvider.postOnUIBus(inAppNotificationModel);
  }


  private static void handleAdjunctLangSilentNotification(NotificationDeliveryMechanism notificationType,
                                                          Bundle data, boolean isFullSync,String msgType) {
    if(isFullSync) {
      Logger.d(TAG,"Adjunct lang update notification in full sync is ignored");
      return;
    }
    final AdjunctLangNavModel adjunctLangNavModel = NotificationMessageParser.parseAdjunctLangNotificationMessage(data,msgType);
    if(adjunctLangNavModel == null || adjunctLangNavModel.getBaseInfo() == null) {
      Logger.d(TAG, "Parsing the adjunct silent notification failed");
      String payload = JsonUtils.getJsonString(data);
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType
              .INVALID, NotificationInvalidType.NOTIFICATION_PARSING_FAILED.getType() + payload);
      return;
    }
    adjunctLangNavModel.getBaseInfo().setDeliveryType(notificationType);
    adjunctLangNavModel.getBaseInfo().setType(data.getString(NotificationConstants.TYPE));
    adjunctLangNavModel.getBaseInfo().setIsSynced(notificationType == NotificationDeliveryMechanism.PULL);
    adjunctLangNavModel.setFullSync(isFullSync);
    adjunctLangNavModel.setsType(Constants.EMPTY_STRING+NavigationType.DELETE_NOTIFICATIONS.getIndex());
    BusProvider.postOnUIBus(adjunctLangNavModel);
  }

  public static boolean getIsPushNotificationWorkingInBg() {
    return PreferenceManager.getPreference(
        GenericAppStatePreference.IS_PUSH_NOTIFICATION_WORKING_IN_BG, false);
  }

  public static void setIsPushNotificationWorkingInBg(Boolean working) {
    PreferenceManager.savePreference(GenericAppStatePreference.IS_PUSH_NOTIFICATION_WORKING_IN_BG,
        working);
  }

  public static List<NotificationCtaObj> getCtaForNotification(BaseInfo baseInfo) {
    if (baseInfo == null || CommonUtils.isEmpty(baseInfo.getNotifSubType()) || CommonUtils.isEmpty(baseInfo.getNotifType())) {
      return null;
    }

    String savedConfig = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_CTA, Constants.EMPTY_STRING);
    if (CommonUtils.isEmpty(savedConfig)) {
      return null;
    }

    List<NotificationConfig> configList = JsonUtils.fromJson(savedConfig, new TypeToken<List<NotificationConfig>>() {
    }.getType());

    if (CommonUtils.isEmpty(configList)) {
      return null;
    }

    String notifType = baseInfo.getNotifType();
    String notifSubType = baseInfo.getNotifSubType();

    for (NotificationConfig config: configList) {
      if (CommonUtils.equals(notifType, config.getNotifType())) {
        List<NotificationAttributeItem> attributes = config.getAttributes();
        for (NotificationAttributeItem item: attributes) {
          if (CommonUtils.equals(notifSubType,item.getNotifSubType())) {
            return item.getCta();
          }
        }
        break;
      }
    }
    return null;
  }
}
