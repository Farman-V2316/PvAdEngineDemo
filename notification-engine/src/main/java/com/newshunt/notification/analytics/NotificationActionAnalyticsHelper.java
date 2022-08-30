/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.notification.analytics;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.app.analytics.NotificationCommonAnalyticsHelper;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEvents;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.BaseModelType;
import com.newshunt.dataentity.notification.InAppNotificationModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism;
import com.newshunt.dataentity.analytics.entity.AnalyticsParam;
import com.newshunt.dataentity.notification.StickyNavModelType;
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper;
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam;
import com.newshunt.notification.model.entity.NotificationFilterType;
import com.newshunt.notification.model.entity.NotificationInvalidType;
import com.newshunt.notification.model.entity.NotificationPrefetchEntity;
import com.newshunt.notification.sqlite.NotificationDB;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * NotificationActionAnalyticsHelper - A helper class for logging all the action events for
 * various sections like News, Books, TV etc.
 *
 * @author anshul.jain
 */
public class NotificationActionAnalyticsHelper {


  /**
   * This method is called from the Notification Router Activity to log the notification action.
   * @param baseModel
   * @param overridingNotificationTypeValue
   * @param paramsMap
   * @param forceAsPvEvent
   */
  public static void logNotificationActionEvent(PageReferrer referrer, BaseModel baseModel, String overridingNotificationTypeValue, Map paramsMap, boolean forceAsPvEvent) {
    logNotificationActionEvent(baseModel, NhAnalyticsUserAction.CLICK.name(), paramsMap, overridingNotificationTypeValue, referrer, forceAsPvEvent);
  }

  /**
   * This method is called from the Notification Router Activity to log the notification action.
   * @param baseModel
   * @param overridingNotificationTypeValue
   * @param referrer
   * @param forceAsPvEvent
   */
  public static void logNotificationActionEvent(@Nullable BaseModel baseModel,
                                                @Nullable String action,
                                                @Nullable Map paramsMap, @Nullable String overridingNotificationTypeValue, PageReferrer referrer, boolean forceAsPvEvent) {

    if (baseModel == null || baseModel.getBaseModelType() == null) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
          NotificationFilterType.INVALID, NotificationInvalidType.BASE_MODEL_NULL.getType(),
          baseModel);
      return;
    }

    AppsFlyerHelper.INSTANCE.trackEvent(AppsFlyerEvents.EVENT_NOTIFICATION_CLICK, null);

    if (!CommonUtils.isEmpty(action)) {
      action = action.toLowerCase();
    }

    if(paramsMap == null){
      paramsMap = new HashMap<NhAnalyticsEventParam, Object>();
    }

    if(baseModel.getBaseInfo() != null && overridingNotificationTypeValue != null && !overridingNotificationTypeValue.equals(StickyNavModelType.NEWS.getAnalyticsStickyType())){
      try{
        NotificationPrefetchEntity notification = NotificationDB.instance().getNotificationPrefetchInfoDao().getPrefetchEntryForNotificationWithId(baseModel.getBaseInfo().getUniqueId());
        if(notification != null){
          paramsMap.put(NhNotificationParam.NOTIFICATION_CACHED_STATE, notification.isNotificationCached());
        }
        else{
          paramsMap.put(NhNotificationParam.NOTIFICATION_CACHED_STATE, false);
        }
      }catch(Exception ex){
        Logger.caughtException(ex);
      }
    }else{
      paramsMap.put(NhNotificationParam.NOTIFICATION_CACHED_STATE, false);
    }

    if(baseModel.getBaseModelType() == BaseModelType.NEWS_MODEL) {
      NewsNavModel model = (NewsNavModel) baseModel;
      if(model.isAdjunct()) {
        paramsMap.put(NhNotificationParam.ADJUNCT_NEWS_LANG,model.getLanguage());
        paramsMap.put(NhNotificationParam.NOTIF_SUBTYPE, Constants.ADJUNCT_LANGUAGE_SUBTYPE);
      }
    }

    switch (baseModel.getBaseModelType()) {
      case NEWS_MODEL:
      case TV_MODEL:
      case LIVETV_MODEL:
      case WEB_MODEL:
      case STICKY_MODEL:
      case EXPLORE_MODEL:
      case FOLLOW_MODEL:
      case PROFILE_MODEL:
      case DEEPLINK_MODEL:
      case SOCIAL_COMMENTS_MODEL:
      case SEARCH_MODEL:
        logActionEventForNewsAndTvAndWeb(baseModel, action, paramsMap, overridingNotificationTypeValue, referrer, forceAsPvEvent);
        break;
      case NAVIGATION_MODEL:
        logActionEventForNavigationModel(baseModel, action, overridingNotificationTypeValue, referrer, forceAsPvEvent);
        break;
      case ADJUNCT_MESSAGE:
        logActionEventForAdjunctStickyNotification(baseModel,action,overridingNotificationTypeValue,referrer,forceAsPvEvent);
        break;
      case IN_APP:
        logActionEventForInAppNotification(baseModel, action, paramsMap, overridingNotificationTypeValue, referrer, forceAsPvEvent);
    }
  }

  /**
   * Helper method for logging all the analytic events related to notification action for books.
   * @param baseModel - an instance of BaseModel.
   * @param overridingNotificationTypeValue
   * @param referrer
   * @param forceAsPvEvent
   */
  private static void logActionEventForNavigationModel(BaseModel baseModel, String action, String overridingNotificationTypeValue, PageReferrer referrer, boolean forceAsPvEvent) {

    if (baseModel == null) {
      return;
    }
    int navigationTypeCode = DataUtil.parseInt(baseModel.getsType(), -1);
    if (navigationTypeCode == -1) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
          NotificationFilterType.INVALID, NotificationInvalidType.INVALID_S_TYPE.getType(),
          baseModel);
      return;
    }

    NavigationType navigationType = NavigationType.fromIndex(navigationTypeCode);
    if (navigationType == NavigationType.SELF_BOARDING) {
      logActionEventForNewsAndTvAndWeb(baseModel, action, null, overridingNotificationTypeValue, referrer, forceAsPvEvent);
    }
  }

  private static void logActionEventForAdjunctStickyNotification(BaseModel baseModel, String action, String overridingNotificationTypeValue,
                                                                 PageReferrer referrer, boolean forceAsPvEvent) {

    if (baseModel == null || baseModel.getBaseInfo() == null) {
      return;
    }
    int navigationTypeCode = DataUtil.parseInt(baseModel.getsType(), -1);
    if (navigationTypeCode == -1) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
              NotificationFilterType.INVALID, NotificationInvalidType.INVALID_S_TYPE.getType(),
              baseModel);
      return;
    }
    Map<NhAnalyticsEventParam,Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.ADJUNCT_NEWS_LANG,baseModel.getBaseInfo().getLanguage());
    map.put(NhNotificationParam.ITEM_ID,baseModel.getBaseInfo().getId());
    logActionEventForNewsAndTvAndWeb(baseModel, action, map, overridingNotificationTypeValue, referrer, forceAsPvEvent);
  }

  public static void logAdjunctStickyNotificationExploreEvent(BaseModel adjunctLangStickyNavModel, Boolean isTick) {
    if(adjunctLangStickyNavModel.getBaseInfo() == null) {
      return;
    }
    Map<NhAnalyticsEventParam,Object> eventParams = new HashMap<>();
    if (isTick) {
      eventParams.put(NhNotificationParam.NOTIFICATION_ACTION, Constants.YES);
    } else {
      eventParams.put(NhNotificationParam.NOTIFICATION_ACTION, Constants.NO);
    }
    if(adjunctLangStickyNavModel.getBaseInfo().getLanguage()!=null) {

        eventParams.put(NhNotificationParam.REFERRER_ADJUNCT_NEWSLANG,adjunctLangStickyNavModel.getBaseInfo().getLanguage());
    }
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK,
            NhAnalyticsEventSection.NOTIFICATION, eventParams, null,
            new PageReferrer(NhGenericReferrer.TYPE_OPEN_NEWSITEM_ADJUNCT_STICKY,
                    adjunctLangStickyNavModel.getBaseInfo().getId()), false);
  }

  /**
   * This method is used for logging notification action events related to news and TV.
   * @param baseModel - The notification
   * @param action    - The notification action.
   * @param map       - The map which the key/value pairs.
   * @param overridingNotificationTypeValue
   * @param referrer
   * @param forceAsPvEvent
   */
  private static void logActionEventForNewsAndTvAndWeb(BaseModel baseModel, String action,
                                                       Map<NhAnalyticsEventParam, Object>
                                                           map, String overridingNotificationTypeValue, PageReferrer referrer, boolean forceAsPvEvent) {

    if (baseModel == null || baseModel.getBaseInfo() == null) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
          NotificationFilterType.INVALID, NotificationInvalidType.BASE_INFO_NULL.getType(),
          baseModel);
      return;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    String notificationId = baseInfo.getId();
    String itemId = baseModel.getItemId();
    if(CommonUtils.isEmpty(itemId)){
      itemId = notificationId;
    }
    PageReferrer pageReferrer = new PageReferrer(
        NhGenericReferrer.NOTIFICATION, notificationId);

    if(referrer != null){
      pageReferrer = referrer;
    }
    if (map == null) {
      map = new HashMap<>();
    }

    map.put(NhAnalyticsAppEventParam.NOTIFICATION_ID, notificationId);
    NotificationDeliveryMechanism deliveryMechanism = NotificationDB.instance().getNotificationDao()
        .getNotificationDeliveryTypeByBaseInfoId(notificationId);
    if (deliveryMechanism != null) {
      map.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM, deliveryMechanism);
    }

    boolean deferredForAnalytics = baseInfo.isDeferredForAnalytics();
    if (deferredForAnalytics) {
      map.put(NhNotificationParam.NOTIFICATION_IS_DEFERRED, deferredForAnalytics);
    }

    int navigationTypeCode = DataUtil.parseInt(baseModel.getsType(), -1);
    if (navigationTypeCode != -1) {
      NavigationType navigationType = NavigationType.fromIndex(navigationTypeCode);
      if (navigationType != null && !DataUtil.isEmpty(navigationType.name())) {
        addReferrerId(pageReferrer, navigationType.name(), map, itemId);
        map.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, navigationType);
      }
    }
    map.put(NhNotificationParam.NOTIFICATION_ACTION, action);
    NotificationCommonAnalyticsHelper.addDisplayAndExpiryParamsToMap(baseModel, map);

    Map experimentalParamsMap = baseInfo.getExperimentParams();
    if(!CommonUtils.isEmpty(overridingNotificationTypeValue)){
      map.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, overridingNotificationTypeValue);
    }

    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_ACTION,
        NhAnalyticsEventSection.NOTIFICATION, map, experimentalParamsMap, referrer, forceAsPvEvent);
  }

  private static void addReferrerId(PageReferrer pageReferrer, String navigationType,
                                    Map<NhAnalyticsEventParam, Object> map, String itemId) {
    NavigationType type = NavigationType.fromString(navigationType);
    if (type == null || pageReferrer == null) {
      return;
    }
    switch (type) {
      case TYPE_OPEN_NEWSITEM:
      case TYPE_OPEN_VIRAL_ITEM:
        map.put(AnalyticsParam.ITEM_ID, itemId);
        break;
      case TYPE_OPEN_NEWS_LIST:
        map.put(NhAnalyticsNewsEventParam.PUBLISHER_ID, pageReferrer.getId());
        break;
      case TYPE_OPEN_NEWS_LIST_CATEGORY:
        map.put(NhAnalyticsNewsEventParam.PUBLISHER_ID, pageReferrer.getId());
        map.put(NhAnalyticsNewsEventParam.CATEGORY_ID, pageReferrer.getSubId());
        break;
    }

  }

  public static void logInAppNotificationNotDisplayedEvent(@NonNull InAppNotificationModel notification, String reason){
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, Constants.IN_APP);
    map.put(NhAnalyticsAppEventParam.NOTIFICATION_UNDELIVERED_REASON, reason);
    if (!CommonUtils.isEmpty(notification.getBaseInfo().getId())) {
      map.put(NhAnalyticsAppEventParam.NOTIFICATION_ID, notification.getBaseInfo().getId());
    }
    Map experimentalParamsMap = notification.getBaseInfo().getExperimentParams();
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.IN_APP_NOTIFICATION_NOT_DISPLAYED, NhAnalyticsEventSection.NOTIFICATION, map,
            experimentalParamsMap, false);
  }

  private static void logActionEventForInAppNotification(BaseModel baseModel, String action,
                                                       Map<NhAnalyticsEventParam, Object>
                                                               map, String overridingNotificationTypeValue, PageReferrer referrer, boolean forceAsPvEvent) {

    if (baseModel == null || baseModel.getBaseInfo() == null) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
              NotificationFilterType.INVALID, NotificationInvalidType.BASE_INFO_NULL.getType(),
              baseModel);
      return;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    String notificationId = baseInfo.getId();
    String itemId = baseModel.getItemId();
    if(CommonUtils.isEmpty(itemId)){
      itemId = notificationId;
    }
    PageReferrer pageReferrer = new PageReferrer(
            NhGenericReferrer.NOTIFICATION, notificationId);

    if(referrer != null){
      pageReferrer = referrer;
    }
    if (map == null) {
      map = new HashMap<>();
    }

    map.put(NhAnalyticsAppEventParam.NOTIFICATION_ID, notificationId);
    NotificationDeliveryMechanism deliveryMechanism = baseInfo.getDeliveryType();
    if (deliveryMechanism != null) {
      map.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM, deliveryMechanism);
    }

    boolean deferredForAnalytics = baseInfo.isDeferredForAnalytics();
    if (deferredForAnalytics) {
      map.put(NhNotificationParam.NOTIFICATION_IS_DEFERRED, deferredForAnalytics);
    }

    int navigationTypeCode = DataUtil.parseInt(baseModel.getsType(), -1);
    if (navigationTypeCode != -1) {
      NavigationType navigationType = NavigationType.fromIndex(navigationTypeCode);
      if (navigationType != null && !DataUtil.isEmpty(navigationType.name())) {
        addReferrerId(pageReferrer, navigationType.name(), map, itemId);
        map.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, navigationType);
      }
    }
    map.put(NhNotificationParam.NOTIFICATION_ACTION, action);
    NotificationCommonAnalyticsHelper.addDisplayAndExpiryParamsToMap(baseModel, map);

    Map experimentalParamsMap = baseInfo.getExperimentParams();
    if(!CommonUtils.isEmpty(overridingNotificationTypeValue)){
      map.put(NhAnalyticsAppEventParam.NOTIFICATION_TYPE, overridingNotificationTypeValue);
    }

    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_ACTION,
            NhAnalyticsEventSection.NOTIFICATION, map, experimentalParamsMap, referrer, forceAsPvEvent);
  }

}
