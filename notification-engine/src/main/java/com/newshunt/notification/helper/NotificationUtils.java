package com.newshunt.notification.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DHConstants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.util.LangInfoRepo;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.BaseModelType;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationLayoutType;
import com.newshunt.dataentity.notification.NotificationSectionType;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dataentity.notification.StickyNavModelType;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dhutil.analytics.AnalyticsHelper;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.helper.NotificationUniqueIdGenerator;
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility;
import com.newshunt.notification.model.entity.ChannelNotFoundException;
import com.newshunt.notification.model.entity.NotificationFilterType;
import com.newshunt.notification.sqlite.NotificationDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by santosh.kumar on 10/1/2015.
 */
public class NotificationUtils {

  public static final String TAG = NotificationUtils.class.getSimpleName();
  /**
   * On Some devices, intent resolution of Pending Intent sometimes fails and the notification
   * does not land on the destination activity. To fix this, we are hardcoding the router
   * activity's class name here! Please ensure this is updated in-case NotificationRoutingActivity
   * is refactored later
   */
  public static final String NOTIFICATION_ROUTER =
      "com.newshunt.app.view.activity.NotificationRoutingActivity";
  private static final String EMPTY_STRING = "";

  public static void removeNotificationFromTray(int notificationId) {
    NotificationManager notificationManager = (NotificationManager) CommonUtils.getApplication()
        .getSystemService(NOTIFICATION_SERVICE);
    if (notificationManager == null) {
      return;
    }
    notificationManager.cancel(notificationId);
    BusProvider.getRestBusInstance().post(new NotificationDismissedEvent(notificationId, false));
  }

  public static void dismissAndReDisplayStickyNotification(){
    Intent intent = new Intent();
    intent.setAction(DailyhuntConstants.STICKY_NOTIFICATION_DISMISS_AND_SHOW_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    CommonUtils.getApplication().sendBroadcast(intent);
  }

  public static NavigationModel getNavModelV2(NavigationModel notification) {
    if (notification == null) {
      Logger.d(TAG, "getNavModelV2: Notification Null");
      return null;
    }
    notification.setBaseInfo(getNotificationBaseInfo(notification));
    return notification;
  }

  public static BaseInfo getNotificationBaseInfo(NavigationModel notification) {

    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setExpiryTime(notification.getExpiryTime());
    baseInfo.setsType(notification.getsType());
    baseInfo.setMessage(notification.getMsg());
    baseInfo.setUniMsg(notification.getUniMsg());
    baseInfo.setUrdu(notification.isUrdu());
    baseInfo.setSectionType(notification.getSectionType());
    baseInfo.setLayoutType(notification.getLayoutType());
    baseInfo.setId(notification.getId());
    baseInfo.setNotifySrc(notification.getNotifySrc());
    baseInfo.setUniqueId((int) System.currentTimeMillis());
    baseInfo.setBigImageLink(notification.getBigImageLink());
    baseInfo.setBigText(notification.getBigText());
    baseInfo.setPriority(notification.getPriority());
    baseInfo.setImageLink(notification.getImageLink());
    baseInfo.setTimeStamp(notification.getTimeStamp());
    baseInfo.setImageLinkV2(notification.getImageLinkV2());
    baseInfo.setBigImageLinkV2(notification.getBigImageLinkV2());
    baseInfo.setLanguage(notification.getLanguage());
    baseInfo.setLanguages(notification.getLanguages());
    baseInfo.setInboxImageLink(notification.getInboxImageLink());
    baseInfo.setDeliveryType(notification.getDeliveryType());
    baseInfo.setIsSynced(notification.isSynced());
    baseInfo.setV4DisplayTime(notification.getV4DisplayTime());
    baseInfo.setV4IsInternetRequired(notification.getV4IsInternetRequired());
    baseInfo.setV4BackUrl(notification.getV4BackUrl());
    baseInfo.setV4SwipeUrl(notification.getV4SwipeUrl());
    baseInfo.setDoNotAutoFetchSwipeUrl(notification.isDoNotAutoFetchSwipeUrl());
    baseInfo.setExpiryTime(notification.getExpiryTime());
    baseInfo.setV4SwipePageLogic(notification.getV4SwipePageLogic());
    baseInfo.setV4SwipePageLogicId(notification.getV4SwipePageLogicId());
    baseInfo.setExperimentParams(notification.getExperimentParams());
    baseInfo.setChannelId(notification.getChannelId());
    baseInfo.setNotifType(notification.getNotifType());
    baseInfo.setNotifSubType(notification.getNotifSubType());
    baseInfo.setChannelGroupId(notification.getChannelGroupId());
    baseInfo.setFilterType(notification.getFilterType());
    return baseInfo;
  }

  public static NewsNavModel getNewsNavModelV2(NavigationModel notification) {
    NewsNavModel newsNavModel = new NewsNavModel();
    newsNavModel.setBaseInfo(getNotificationBaseInfo(notification));

    newsNavModel.setNpKey(notification.getNpKey());
    newsNavModel.setCtKey(notification.getCtKey());
    newsNavModel.setNewsId(notification.getNewsId());
    newsNavModel.setSectionType(notification.getSectionType());
    newsNavModel.setLayoutType(notification.getLayoutType());
    newsNavModel.setPromoId(notification.getPromoId());
    newsNavModel.setUrdu(notification.isUrdu());
    newsNavModel.setTopicKey(notification.getTopicKey());
    newsNavModel.setfKey(notification.getfKey());
    newsNavModel.setsType(notification.getsType());
    newsNavModel.getBaseInfo().setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForNews
        (notification));

    return newsNavModel;
  }

  public static Boolean isNotificationOnTrayForUpdate(int uniqueId) {
    BaseModel object = NotificationDB.instance().getNotificationDao().getNotification(uniqueId, false);
    if (object == null) {
      //No Entry found in DB
      return true;
    }
    BaseInfo baseInfo = object.getBaseInfo();
    return baseInfo != null && !baseInfo.isRemovedFromTray() && !baseInfo.isGrouped() &&
        !baseInfo.isRead();
  }

  public static void setLayoutType(BaseInfo baseInfo) {
    if (baseInfo == null) {
      return;
    }
    NotificationLayoutType layoutType = NotificationLayoutType.NOTIFICATION_TYPE_SMALL;

    if (!CommonUtils.isEmpty(baseInfo.getBigImageLinkV2()) ||
        !CommonUtils.isEmpty(baseInfo.getBigImageLink())) {
      layoutType = NotificationLayoutType.NOTIFICATION_TYPE_BIG_PICTURE;
    } else if (!CommonUtils.isEmpty(baseInfo.getBigText())) {
      layoutType = NotificationLayoutType.NOTIFICATION_TYPE_BIG_TEXT;
    }
    baseInfo.setLayoutType(layoutType);
  }

  /**
   * To check if notification contains atleast one language chosen by the user.
   * Not filtering out in case some data is absent.
   */
  public static boolean isNotificationLanguageValid(BaseModel baseModel) {
    if (baseModel == null || baseModel.getBaseInfo() == null) {
      return true;
    }

    String[] userLanguages = PullNotificationsHelper.getUserLanguages();
    String[] notificationLanguages = baseModel.getBaseInfo().getLanguages();

    if (userLanguages == null || userLanguages.length == 0 || notificationLanguages == null ||
        notificationLanguages.length == 0) {
      return true;
    }

    Set<String> userLanguageSet = new HashSet<>(Arrays.asList(userLanguages));
    for (String langCode : notificationLanguages) {
      if (userLanguageSet.contains(langCode)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method will tell whether the given notification has expired or not.
   *
   * @param baseModel
   * @return
   */
  public static boolean hasNotificationExpired(BaseModel baseModel) {
    if (baseModel == null || baseModel.getBaseInfo() == null) {
      return false;
    }

    long expiryTime = baseModel.getBaseInfo().getExpiryTime();
    if (expiryTime <= 0) {
      return false;
    }

    Date notificationExpiryDate = new Date(expiryTime);
    if (notificationExpiryDate != null && (new Date().compareTo(notificationExpiryDate) > 0)) {
      return true;
    }
    return false;
  }

  public static void markNotificationPriorityExpired() {

    long expiryTime = System.currentTimeMillis();
    NotificationDB.instance().getNotificationDao().markNotificationDePriority(expiryTime);
  }

  /**
   * This method will tell whether the given notification is a deferred notification or not.
   *
   * @param baseModel
   * @return
   */
  public static boolean isNotificationDeferred(BaseModel baseModel) {
    if (baseModel == null || baseModel.getBaseInfo() == null) {
      return false;
    }

    return (baseModel.getBaseInfo().getV4DisplayTime() > 0);
  }

  /**
   * This function is used to convert wrong value of expiryTime stored in the previous client.
   * eg expiryTime148983403343 will be converted to 148983403343.
   * For V1 notifications, the server is sending string as ~expiryTime148983403343~. The older
   * clients upto 8.2.11 which store the string as it is. But the new client after 8.2.11 will
   * store only 148983403343. This function is to handle the upgrade from 8.2.11 to newer clients.
   *
   * @param navigationModel
   * @param dataJson
   * @param gson
   * @return
   */
  public static BaseModel handleWrongExpiryTimeValue(BaseModel navigationModel,
                                                     NotificationSectionType
                                                         sectionType, String dataJson, Gson gson) {
    String BASE_INFO = "baseInfo";
    String EXPIRY_TIME = "expiryTime";
    try {
      JSONObject jsonObject = new JSONObject(dataJson);
      JSONObject baseInfoJson = (JSONObject) jsonObject.get(BASE_INFO);
      if (baseInfoJson.has(EXPIRY_TIME)) {
        baseInfoJson.remove(EXPIRY_TIME);
        jsonObject.put(BASE_INFO, baseInfoJson);
        dataJson = jsonObject.toString();
        switch (sectionType) {
          case APP:
            navigationModel = gson.fromJson(dataJson, NavigationModel.class);
            break;
          case NEWS:
            navigationModel = gson.fromJson(dataJson, NewsNavModel.class);
            break;
          case TV:
            navigationModel = gson.fromJson(dataJson, TVNavModel.class);
            break;
          case LIVETV:
            navigationModel = gson.fromJson(dataJson, LiveTVNavModel.class);
            break;
          case ADS:
            navigationModel = gson.fromJson(dataJson, AdsNavModel.class);
            break;
        }
      }
    } catch (JSONException e1) {
      Logger.caughtException(e1);
    } catch (Exception e1) {
      Logger.caughtException(e1);
    }
    return navigationModel;
  }

  public static Intent getNotificationRouterIntent(BaseModel notificationModel) {
    Intent targetIntent = new Intent(Constants.NOTIFICATION_ROUTER_OPEN);
    targetIntent.setPackage(AppConfig.getInstance().getPackageName());
    targetIntent.setClassName(CommonUtils.getApplication(), NOTIFICATION_ROUTER);
    targetIntent.putExtra(Constants.NOTIFICATION_BASE_MODEL,
        BaseModelType.convertModelToString(notificationModel));
    if (notificationModel instanceof StickyNavModel) {
      targetIntent.putExtra(Constants.NOTIFICATION_BASE_MODEL_STICKY_TYPE,
          ((StickyNavModel) notificationModel).getStickyType());
    }
    if (notificationModel.getBaseModelType() != null) {
      targetIntent.putExtra(Constants.NOTIFICATION_BASE_MODEL_TYPE,
          notificationModel.getBaseModelType().name());
    }
    return targetIntent;
  }

  public static Intent getSettingsIntent(){
    Intent notificationActivityIntent = new Intent(DHConstants.OPEN_NOTIFICATION_ACTIVITY);
    notificationActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
    return notificationActivityIntent;
  }

  public static void startForegroundServiceForPrefetchNotificationInfo() {
    try {
      Intent intent = getIntentForPreFetchInfoForegroundService(DailyhuntConstants.PREFETCH_NOTIFICATION_SERVICE_START_ACTION);
      Logger.d(TAG, "startForegroundServiceForPrefetchNotificationInfo: starting fg");
      CommonUtils.getApplication().startForegroundService(intent);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static void stopForegroundServiceForPrefetchNotificationInfo(){
    try {

      Intent intent = getIntentForPreFetchInfoForegroundService(DailyhuntConstants.PREFETCH_NOTIFICATION_SERVICE_STOP_ACTION);
      CommonUtils.getApplication().startService(intent);
      Logger.d(TAG, "stopForegroundServiceForPrefetchNotificationInfo: stopping service");

    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static Intent getIntentForPreFetchInfoForegroundService(String action){
    Intent intent = new Intent();
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.setAction(action);
    return intent;
  }

  public static void showNotificationOnTray(final int notificationId,
                                            @NonNull NotificationCompat.Builder notificationBuilder,
                                            boolean showAsHeadsUp, boolean headsUpDBValue, long notiPreviousDisplayedAtTime, boolean forceNonHeadsUp) {
    boolean shouldNotAddToTray = (PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_TRAY_MANAGEMENT_SECTION_WAS_EVER_EXPANDED, false) &&
        (PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION , -1) == Constants.ONLY_LIVE_TICKER)) ||
        !PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true);
    if(shouldNotAddToTray){
      Logger.d(TAG, "Normal Notifications were disabled hence not adding to tray");
      return;
    }
    //Added to help with notifications which might be updated without removing and the existing checks as below doesn't help
    if(forceNonHeadsUp){
      notificationBuilder.setOnlyAlertOnce(true);
    }
    if (headsUpDBValue) {
      notificationBuilder.setVibrate(null);
    } else if (showAsHeadsUp) {
      notificationBuilder.setVibrate(new long[0]);
      //This function is already called from a background thread.
      NotificationDB.instance().getNotificationDao().markNotificationAsHeadsUp(notificationId);
      notificationBuilder.setOnlyAlertOnce(true);
    }

    //Mark display Time and schedule remove from tray job only if not done already
    if(notiPreviousDisplayedAtTime == -1){
      NotificationRemoveFromTrayHelper.scheduleNotificationRemovalJobFor(notificationId, System.currentTimeMillis() ,PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_AUTO_TRAY_DELETE_DURATION, -1l));
    }else{
      Logger.d(TAG, "Not scheduling job for notification with id" + notificationId);
    }
    NotificationManager notificationManager = (NotificationManager) CommonUtils.getApplication()
        .getSystemService(NOTIFICATION_SERVICE);
    try {
      notificationManager.notify(notificationId, notificationBuilder.build());
      Logger.d("Notification_prefetch","Shown as Heads Up with Id " +notificationId+ " value "+showAsHeadsUp + " headsupDB value" + headsUpDBValue);
      Logger.d("NotificationController","Shown as Heads Up with Id " +notificationId+ " value "+showAsHeadsUp + " headsupDB value" + headsUpDBValue);
      NotificationLogger.logAddNotificationToTray(notificationId, showAsHeadsUp);
    } catch (SecurityException e) {
      //While posting if encounter SecurityException, then post is without headsup.
      try {
        Logger.caughtException(e);
        notificationBuilder.setVibrate(null);
        notificationBuilder.setDefaults(~NotificationCompat.DEFAULT_ALL);
        notificationManager.notify(notificationId, notificationBuilder.build());
        NotificationLogger.logAddNotificationToTray(notificationId, showAsHeadsUp);
      } catch (Exception ex) {
        // Do not log exception
        AnalyticsHelper.logDevErrorEvent("Notification Retry Failed " + ex.getMessage());
      }
    } catch (Exception e) {
      Logger.caughtException(e);
      AnalyticsHelper.logDevErrorEvent("Notification Failed " + e.getMessage());
    }
    updateNewsSticky(notificationId);
  }

  public static void updateNewsSticky(int notificationId){
    try{
      Intent intent = new Intent(NotificationConstants.INTENT_ACTION_NOTIFICATION_RECEIVED);
      intent.setPackage(CommonUtils.getApplication().getPackageName());
      intent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE, StickyNavModelType.NEWS.getStickyType());
      intent.putExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID, notificationId);
      CommonUtils.getApplication().sendBroadcast(intent);
    }catch(Exception ex){
      Logger.caughtException(ex);
    }
  }

  public static String getGroup(String channelId) throws ChannelNotFoundException {
    String groupKey = null;
    NotificationManager mgr =
        (NotificationManager) CommonUtils.getApplication()
            .getSystemService(Context.NOTIFICATION_SERVICE);
    final NotificationChannel channel = mgr.getNotificationChannel(channelId);
    if (channel == null) {
      throw new ChannelNotFoundException(channelId);
    }
    groupKey = channel.getGroup();

    if (groupKey == null) {
      groupKey = NotificationConstants.NOTIFICATION_DEFAULT_GROUP_NAME;
    }

    return groupKey;
  }

  public static boolean isMi9Device() {
    return NotificationConstants.MANUFACTURER_XIAOMI.equals(
        DeviceInfoHelper.getDeviceInfo().getManufacturer());
  }

  public static String getNotificationContentText(BaseInfo baseInfo) {
    String contentText = EMPTY_STRING;
    if(baseInfo != null) {
      if(baseInfo.getMessage() != null) {
        contentText = baseInfo.getMessage();
      } else if(baseInfo.getUniMsg() != null) {
        contentText = baseInfo.getUniMsg();
      }
    }
    return contentText;
  }

  public static void convertNewsStickyItemsToNormalNotificationsInBackground(boolean isFromInbox){
    try {
      CommonUtils.runInBackground(new Runnable() {
        @Override
        public void run() {
          convertNewsStickyNotificationsToNormalNotifications(isFromInbox);

        }
      });

    } catch (Exception ex) {
      Logger.caughtException(ex);
    }
  }

  public static void convertNewsStickyNotificationsToNormalNotifications(boolean isFromInbox){
    List<BaseModel> listOfStickyItems = NotificationDB.instance().getNotificationDao().getStickyNotifications(NotificationConstants.STICKY_NEWS_TYPE, Integer.MAX_VALUE);
    if(!CommonUtils.isEmpty(listOfStickyItems)){
      for(int i = 0 ; i < listOfStickyItems.size(); i++){
        BaseModel item = listOfStickyItems.get(i);
        if (item.getBaseInfo() != null) {
          NotificationDB.instance().getNotificationDao().deleteIdFromNotificationTableAndNotificationInfoTable(item.getBaseInfo().getUniqueId());
          item.setStickyItemType(NotificationConstants.STICKY_NONE_TYPE);
          item.getBaseInfo().setTimeStamp(System.currentTimeMillis() + (i * 10L));
          if (item.getBaseInfo().isRead() || item.getBaseInfo().wasSkippedByUser() || isFromInbox) {
            Logger.d("StickyNotification", "Item:- " + item.getItemId() + "will be added to db");
            NotificationDB.instance().getNotificationDao().addNotification(item, false, item.getBaseInfo().getState());
          } else {
            Logger.d("StickyNotification", "Item:- " + item.getItemId() + "will be posted to bus");
            BusProvider.postOnUIBus(item);
          }
          //Thread.sleep(100);
        }
      }
    }
  }

  public static boolean filteredNotificationBasedOnLangCheck(BaseModel baseModel){
    //if language is not one of user or system selected, ignore
    if(baseModel != null && baseModel.getBaseInfo() != null && !CommonUtils.isEmpty(baseModel.getBaseInfo().getLanguage()) && isInvalidLanguage(baseModel.getBaseInfo().getLanguage(), baseModel.getBaseInfo().isDisableLangFilter())) {
      NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(baseModel,
          NotificationFilterType.INVALID_LANGUAGE);
      return true;
    }

    return false;
  }

  public static boolean isInvalidLanguage(String language, boolean isLangCheckDisabled){
    if(!CommonUtils.isEmpty(language) && !isLangCheckDisabled && !LangInfoRepo.isUserOrSystemSelectedLanguage(language.toLowerCase())){
      return true;
    }
    return false;
  }

}