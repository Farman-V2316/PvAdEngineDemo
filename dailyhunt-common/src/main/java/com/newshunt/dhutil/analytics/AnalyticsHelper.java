/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.analytics.entity.NhAnalyticsDevEvent;
import com.newshunt.dataentity.analytics.entity.AnalyticsParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dhutil.helper.CurrentClientInfoHelper;
import com.newshunt.news.analytics.NhAnalyticsAppState;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.analytics.helper.ReferrerDecoder;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.common.helper.analytics.NhAnalyticsUtility;
import com.newshunt.common.helper.common.ApplicationStatus;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.PasswordEncryption;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.common.helper.preference.AppCredentialPreference;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.Edition;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Provides helper methods for analytics.
 *
 * @author shreyas.desai
 */
public class AnalyticsHelper {

  public static void logGoogleDeviceIds(Context context,
                                        String clientId,
                                        String registrationId) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.CLIENT_ID, clientId);

    Map<NhAnalyticsEventParam, Object> referralParams = getOnBoardingParams(context);
    map.putAll(referralParams);

    map.put(NhAnalyticsCampaignEventParam.GCM_ID, registrationId);

    String googleAdId = ClientInfoHelper.getGoogleAdId();
    try {
      map.put(NhAnalyticsCampaignEventParam.GOOGLE_AD_ID, PasswordEncryption.encrypt(googleAdId));
    } catch (Exception e) {
      Logger.caughtException(e);
    }

    String androidId = ClientInfoHelper.getAndroidId();
    try {
      map.put(NhAnalyticsCampaignEventParam.ANDROID_ID, PasswordEncryption.encrypt(androidId));
    } catch (Exception e) {
      Logger.caughtException(e);
    }

    String isNotificationEnabled = Constants.EMPTY_STRING +
        PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true);
    map.put(NhAnalyticsCampaignEventParam.NOTIFICATION_STATUS, isNotificationEnabled);

    AnalyticsClient.log(NhAnalyticsAppEvent.DEVICE_GOOGLE_IDS,
        NhAnalyticsEventSection.NEWS, map);
  }

  public static Map<NhAnalyticsEventParam, Object> getCampaignParams(String referrer) {
    Map<String, String> map = ReferrerDecoder.getCampaignParams(referrer);
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      NhAnalyticsCampaignEventParam eventParam = NhAnalyticsCampaignEventParam
          .fromName(entry.getKey());
      if (eventParam != null) {
        paramsMap.put(eventParam, entry.getValue());
      }
    }

    return paramsMap;
  }

  public static Map<NhAnalyticsEventParam, Object> getOnBoardingParams(Context context) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();

    String gcmId = PreferenceManager.getPreference(AppCredentialPreference.GCM_REG_ID, "");
    if (!DataUtil.isEmpty(gcmId)) {
      map.put(NhAnalyticsCampaignEventParam.GCM_ID, gcmId);
    }

    map.put(NhAnalyticsAppEventParam.DEVICE_ID, DeviceInfoHelper.getDeviceInfo().getDeviceId());
    String macAddress = ConnectionInfoHelper.getMacAddress(context);
    if (!DataUtil.isEmpty(macAddress)) {

      try {
        map.put(NhAnalyticsAppEventParam.MAC_ADDRESS, PasswordEncryption.encrypt(macAddress));
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }

    String referrerString = CurrentClientInfoHelper.getReferrerString();
    if (!DataUtil.isEmpty(referrerString)) {
      map.put(NhAnalyticsCampaignEventParam.REFERRER_RAW, referrerString);
      Map<NhAnalyticsEventParam, Object> referrerParams = getCampaignParams(referrerString);
      map.putAll(referrerParams);
    }

    return map;
  }

  public static void logPrevSessionEnd() {
    String exitStatus =
        PreferenceManager.getPreference(GenericAppStatePreference.APP_EXIT_STATUS,
            Constants.EMPTY_STRING);
    if (!DataUtil.isEmpty(exitStatus)) {
      return;
    }

    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsAppEventParam.END_STATE,
        NhAnalyticsUserAction.FORCE_CLOSE.name());

    long startTime = PreferenceManager.getPreference(GenericAppStatePreference.APP_START_TIME, 0L);
    long endTime = PreferenceManager.getPreference(GenericAppStatePreference.APP_CURRENT_TIME, 0L);
    if (startTime != 0 && endTime != 0 && endTime - startTime > 0) {
      long sessionLengthMillis = endTime - startTime;
      eventParams.put(NhAnalyticsAppEventParam.SESSION_LENGTH, sessionLengthMillis);
    }

    long deviceDataConsumedAtSessionStart =
        PreferenceManager.getPreference(GenericAppStatePreference
            .DEVICE_DATA_CONSUMED, 0L);
    long appDataConsumedAtSessionStart = PreferenceManager.getPreference(GenericAppStatePreference
        .APP_DATA_CONSUMED, 0L);
    PreferenceManager.remove(GenericAppStatePreference.DEVICE_DATA_CONSUMED);
    PreferenceManager.remove(GenericAppStatePreference.APP_DATA_CONSUMED);
    Pair<Long, Long> dataConsumed = DeviceInfoHelper.getDataConsumed();
    if (dataConsumed.first - deviceDataConsumedAtSessionStart > 0 &&
        dataConsumed.second - appDataConsumedAtSessionStart > 0) {
      eventParams.put(NhAnalyticsAppEventParam.USER_SESSION_DATACONSUMED, dataConsumed.first -
          deviceDataConsumedAtSessionStart);
      eventParams.put(NhAnalyticsAppEventParam.DH_SESSION_DATACONSUMED, dataConsumed.second -
          appDataConsumedAtSessionStart);
      eventParams.put(NhAnalyticsAppEventParam.USER_BOOT_DATACONSUMED, dataConsumed.first);
      eventParams.put(NhAnalyticsAppEventParam.DH_BOOT_DATACONSUMED, dataConsumed.second);
    }

    AnalyticsClient.log(NhAnalyticsAppEvent.SESSION_END, NhAnalyticsEventSection.APP, eventParams);
  }

  public static void logPrevAppExit() {
    String exitStatus =
        PreferenceManager.getPreference(GenericAppStatePreference.APP_EXIT_STATUS,
            Constants.EMPTY_STRING);
    if (DataUtil.isEmpty(exitStatus)) {
      exitStatus = NhAnalyticsUserAction.FORCE_CLOSE.name();

      Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
      eventParams.put(NhAnalyticsAppEventParam.EXIT_TYPE, exitStatus);

      String lastPage = PreferenceManager.getPreference(GenericAppStatePreference.APP_CURRENT_PAGE,
          Constants.EMPTY_STRING);
      if (!DataUtil.isEmpty(lastPage)) {
        eventParams.put(NhAnalyticsAppEventParam.LAST_PAGE, lastPage);
      }

      AnalyticsClient.log(NhAnalyticsAppEvent.APP_EXIT, NhAnalyticsEventSection.APP, eventParams);
    } else {
      PreferenceManager.remove(GenericAppStatePreference.APP_EXIT_STATUS);
    }
  }

  public static void logAppStart() {
    AnalyticsClient.log(NhAnalyticsAppEvent.APP_START, NhAnalyticsEventSection.APP, null);
  }

  public static void logSessionStart() {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsAppEventParam.START_STATE, NhAnalyticsAppEvent.APP_START.name());
    Pair<Long, Long> dataConsumed = DeviceInfoHelper.getDataConsumed();
    PreferenceManager.saveLong(GenericAppStatePreference.DEVICE_DATA_CONSUMED, dataConsumed.first);
    PreferenceManager.saveLong(GenericAppStatePreference.APP_DATA_CONSUMED, dataConsumed.second);
    eventParams.put(NhAnalyticsAppEventParam.USER_BOOT_DATACONSUMED, dataConsumed.first);
    eventParams.put(NhAnalyticsAppEventParam.DH_BOOT_DATACONSUMED, dataConsumed.second);
    AnalyticsClient.log(NhAnalyticsAppEvent.SESSION_START, NhAnalyticsEventSection.APP,
        eventParams);
  }

  /**
   * Event to report the OnBoarding language Selection is Complete
   *
   * @param currentEdition   -- Current Edition Selected
   * @param currentLangCodes -- Current List of Languages Codes selected
   */
  public static void logOnBoardingCompletion(Edition currentEdition,
                                             ArrayList<String> currentLangCodes, PageReferrer pageReferrer) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    if (!DataUtil.isEmpty(UserPreferenceUtil.getUserEdition())) {
      map.put(NhAnalyticsAppEventParam.EDITION_SELECTION_OLD,
          UserPreferenceUtil.getUserEdition());
    }
    map.put(NhAnalyticsAppEventParam.EDITION_SELECTED_NEW, currentEdition.getKey());
    String languageStr = UserPreferenceUtil.getUserPrimaryLanguage();
    if (!DataUtil.isEmpty(languageStr)) {
      String secondaryLanguagesStr = UserPreferenceUtil.getUserSecondaryLanguages();
      languageStr = languageStr + Constants.COMMA_CHARACTER + secondaryLanguagesStr;
      map.put(NhAnalyticsAppEventParam.LANGUAGES_OLD, languageStr);
    }
    map.put(NhAnalyticsAppEventParam.LANGUAGES_NEW, DataUtil.parseAsString(currentLangCodes));
    AnalyticsClient.log(NhAnalyticsAppEvent.CONTINUE_BUTTON_CLICKED, NhAnalyticsEventSection.APP,
        map, pageReferrer);
  }

  /**
   * Event to report the Edition Select Event
   *
   * @param currentEdition -- Current Edition
   * @param newEdition     -- new Edition Selected
   */
  public static void logEditionSelectionEvent(Edition currentEdition, Edition newEdition) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.EDITION_SELECTION_OLD, currentEdition.getKey());
    map.put(NhAnalyticsAppEventParam.EDITION_SELECTED_NEW, newEdition.getKey());

    AnalyticsClient.log(NhAnalyticsAppEvent.EDITION_SELECT,
        NhAnalyticsEventSection.APP, map);
  }

  /**
   * Event to log Current Language Selection based on Edition
   *
   * @param languageSelectionCodes -- Set of codes Selected
   */
  public static void logCurrentLanguageSelection(HashSet<String> languageSelectionCodes,
                                                 boolean langAutoSelected,
                                                 String currentSelectedLangCode,
                                                 boolean isSelected,
                                                 PageReferrer pageReferrer,
                                                 boolean recommendedLanguage,
                                                 String screenType) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    String languages = DataUtil.parseAsString(languageSelectionCodes);
    map.put(NhAnalyticsAppEventParam.LANGUAGES, languages);
    map.put(NhAnalyticsAppEventParam.LANG_AUTO_SELECT, langAutoSelected);
    map.put(NhAnalyticsAppEventParam.LANGUAGE_RECOMMENDED, recommendedLanguage);
    if(screenType != null && !CommonUtils.isEmpty(screenType)) {
      map.put(NhAnalyticsAppEventParam.SCREEN_TYPE, screenType);
    }
    if (isSelected) {
      map.put(NhAnalyticsAppEventParam.LANGUAGE_SELECTED, currentSelectedLangCode);
    } else {
      map.put(NhAnalyticsAppEventParam.LANGUAGE_DESELECTED, currentSelectedLangCode);
    }
    AnalyticsClient.log(NhAnalyticsAppEvent.LANGUAGES_SELECTED, NhAnalyticsEventSection.APP, map,
        pageReferrer);
  }

  /**
   * Method to log the Actionable Activity View
   */
  public static void logActionableActivityView(String actionId, String activityType) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap();
    map.put(AnalyticsParam.ACTIVITY_TYPE, activityType);
    map.put(NhAnalyticsAppEventParam.LIST_ACTION_ID, actionId);
    AnalyticsClient.log(NhAnalyticsAppEvent.ACTIONABLE_ACTIVITY_VIEWED,
        NhAnalyticsEventSection.APP, map);
  }

  /**
   * Method to log the Actionable Activity View
   */
  public static void logActionableActivityClicked(String actionId, String activityType) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap();
    map.put(AnalyticsParam.ACTIVITY_TYPE, activityType);
    map.put(NhAnalyticsAppEventParam.LIST_ACTION_ID, actionId);
    AnalyticsClient.log(NhAnalyticsAppEvent.ACTIONABLE_ACTIVITY_CLICKED,
        NhAnalyticsEventSection.APP, map);
  }

  /**
   * Method to log the Actionable Activity View
   */
  public static void logActionableActivityClosed(String actionId, String activityType) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap();
    map.put(AnalyticsParam.ACTIVITY_TYPE, activityType);
    map.put(NhAnalyticsAppEventParam.LIST_ACTION_ID, actionId);
    AnalyticsClient.log(NhAnalyticsAppEvent.ACTIONABLE_ACTIVITY_CLOSED,
        NhAnalyticsEventSection.APP, map);
  }

  /**
   * Method to log the Actionable Activity View
   */
  public static void logActionableActivityBlocked(@NonNull String actionId, String activityType) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap();
    map.put(AnalyticsParam.ACTIVITY_TYPE, activityType);
    map.put(NhAnalyticsAppEventParam.LIST_ACTION_ID, actionId);
    AnalyticsClient.log(NhAnalyticsAppEvent.ACTIONABLE_ACTIVITY_BLOCKED,
        NhAnalyticsEventSection.APP, map);
  }

  /**
   * Method to log the Splash View Event
   *
   * @param context -- Context of the App
   */
  public static void logSplashViewEvent(Context context) {
    Map<NhAnalyticsEventParam, Object> map = getOnBoardingParams(context);
    AnalyticsClient.log(NhAnalyticsAppEvent.SPLASH_PAGE_VIEW, NhAnalyticsEventSection.APP, map);
  }

  /**
   * Method to set Analytics Initial State for every App launch ..
   *
   * @param bundle -- Bundle passed
   */
  public static void setAnalyticsInitialState(Bundle bundle) {
    PageReferrer pageReferrer = null;
    if (bundle != null) {
      pageReferrer = (PageReferrer) bundle.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER);
    }
    NhAnalyticsReferrer referrer = NhGenericReferrer.ORGANIC;
    if (pageReferrer != null) {
      referrer = pageReferrer.getReferrer();
    }

    if (!ApplicationStatus.isRunning()) {
      NhAnalyticsAppState.getInstance().setSessionSource(referrer);
      ApplicationStatus.setRunning(true);
    }
    NhAnalyticsAppState.getInstance().setEventAttribution(referrer);
    NhAnalyticsAppState.getInstance().setReferrer(referrer);
    AnalyticsHelper.logAppStart();
    AnalyticsHelper.logPrevSessionEnd();
    AnalyticsHelper.logSessionStart();
    AnalyticsHelper.logPrevAppExit();
  }

  public static void updateAppState(PageReferrer pageReferrer) {

    if (null == pageReferrer) {
      return;
    }

    NhAnalyticsAppState.getInstance().setEventAttribution(pageReferrer.getReferrer());
    NhAnalyticsAppState.getInstance().setEventAttributionId(pageReferrer.getId());
    NhAnalyticsAppState.getInstance().setReferrer(pageReferrer.getReferrer());
    NhAnalyticsAppState.getInstance().setReferrerId(pageReferrer.getId());

    if (!ApplicationStatus.isRunning()) {
      NhAnalyticsAppState.getInstance().setSessionSource(pageReferrer.getReferrer());
      NhAnalyticsAppState.getInstance().setSourceId(pageReferrer.getId());

      AnalyticsHelper.logAppStart();
      AnalyticsHelper.logPrevSessionEnd();
      AnalyticsHelper.logSessionStart();
      AnalyticsHelper.logPrevAppExit();

      ApplicationStatus.setRunning(true);
    }

    if (!DataUtil.isEmpty(pageReferrer.getSubId())) {
      NhAnalyticsAppState.getInstance().setSubReferrerId(pageReferrer.getSubId());
    }
  }


  public static NhAnalyticsUtility.ErrorResponseCode getErrorResponseCode(String message) {
    if (CommonUtils.isEmpty(message) || CommonUtils.getString(com.newshunt.common.util.R.string.error_no_content_msg).equals(message)
        || CommonUtils.getString(com.newshunt.common.util.R.string.no_content_found).equals(message) ||
        CommonUtils.getString(com.newshunt.common.util.R.string.error_no_content_msg_snackbar).equals(message)) {
      return NhAnalyticsUtility.ErrorResponseCode.CONTENT_ERROR;
    } else if (CommonUtils.getString(com.newshunt.common.util.R.string.error_no_connection).equals(message) ||
        CommonUtils.getString(com.newshunt.common.util.R.string.error_no_connection_snackbar).equals(message)) {
      return NhAnalyticsUtility.ErrorResponseCode.NO_INTERNET;
    } else if (CommonUtils.getString(com.newshunt.common.util.R.string.error_server_issue).equals(message)) {
      return NhAnalyticsUtility.ErrorResponseCode.SERVER_ERROR;
    } else {
      return NhAnalyticsUtility.ErrorResponseCode.NETWORK_ERROR;
    }
  }

  public static void logExploreButtonClickEvent(ExploreButtonType buttonType, int position, String
      title) {
    logExploreButtonClickEvent(buttonType, position, title, null);
  }

  public static void logExploreButtonClickEvent(ExploreButtonType buttonType, int position, String
      title, String triggerAction) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT, false);

    String builder = buttonType.getType() + Constants.UNDERSCORE_CHARACTER + position +
        Constants.UNDERSCORE_CHARACTER + title;
    map.put(NhAnalyticsAppEventParam.TYPE, builder);

    if (!CommonUtils.isEmpty(triggerAction)) {
      map.put(NhAnalyticsAppEventParam.TRIGGER_ACTION, triggerAction);
    }

    AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection.APP, map);
  }

  /**
   * Log the conversion data received in AppsFlyer callback
   *
   * @param conversionData
   */
  public static void logAppsFlyerInstallEvent(final Map<String, String> conversionData) {
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.APPSFLYER_INSTALL, NhAnalyticsEventSection.APP,
        null, conversionData, false);
  }

  public static void logAppsFlyerInitEvent(){
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.APPSFLYER_INIT, NhAnalyticsEventSection.APP,
        null, null, false);
  }

  public static void logAppsFlyerInitFailure(final Map<String, String> params) {
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.APPSFLYER_FAILURE, NhAnalyticsEventSection.APP,
        null, params, false);
  }

  public static void logAppsFlyerDevErrorEvent(HashMap<String, String> params) {
    AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR, NhAnalyticsEventSection.APP,
            null, params, false);
  }

  public static void logDevErrorEvent(String message) {
    AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR, NhAnalyticsEventSection.APP,
        null, Collections.singletonMap("Error", message), false);
  }

  public static void logNotificationDevEvent(HashMap<String, String> dynamicParamsMap){
    AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_NOTIFICATION_PARAMS, NhAnalyticsEventSection.NOTIFICATION, null, dynamicParamsMap, false);
  }

  public static PageReferrer getCurrentAppSectionPageReferrer() {
    UserAppSection currentAppSection = AppUserPreferenceUtils.getPreviousAppSection();
    if (currentAppSection == null) {
      return null;
    }

    RunTimeReferrer runTimeReferrer =
        new RunTimeReferrer(currentAppSection.getType().getName(), null);
    String referrerId = CommonUtils.isEmpty(currentAppSection.getAppSectionEntityKey())?
        currentAppSection.getId() : currentAppSection.getAppSectionEntityKey();
    return new PageReferrer(runTimeReferrer, referrerId);
  }

  public static void logSearchEvent(@NonNull NhAnalyticsAppEvent eventName,
                                    @NonNull Map<NhAnalyticsEventParam, Object> map,
                                    PageReferrer referrer,
                                    Map<String, String> experiments) {
    AnalyticsClient.addStateParamsAndPermanentParams(map);
    AnalyticsClient.logDynamic(eventName, NhAnalyticsEventSection.SEARCH,
        map, experiments, referrer, false);

  }

  public static void logFCVEvent() {
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.FIRST_CONTENT_VIEW_CLIENT,
        NhAnalyticsEventSection.APP, null, null, false);
  }

  public static void logWokenbyPartnerEvent(String source) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.NETWORK_APP_NAME, source);
    map.put(NhAnalyticsAppEventParam.BATTERY, CommonUtils.getBatteryPercent());
    AnalyticsClient.log(NhAnalyticsAppEvent.NETWORK_APP_START,
            NhAnalyticsEventSection.APP, map);
  }

  public static void logPartnerWokenUpEvent(int version) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.PARTNER_CONFIG_VERSION, version);
    AnalyticsClient.log(NhAnalyticsAppEvent.PARTNER_WOKEN_UP,
            NhAnalyticsEventSection.APP, map);
  }

  public static void logDefaultShareAppSelected(String label){
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    PageReferrer referrer = new PageReferrer(NhGenericReferrer.SETTINGS,null);
    map.put(NhAnalyticsAppEventParam.APP_NAME,label);
    AnalyticsClient.log(NhAnalyticsAppEvent.DEFAULT_SHARE_APP_SELECTED, NhAnalyticsEventSection.APP, map,referrer);
  }

  public static void logDisplayThemeEvent(String displayTheme, String prevTheme, String newTheme,PageReferrer referrer){
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.DISPLAY_THEME,displayTheme);
    map.put(NhAnalyticsAppEventParam.NEW_MODE,newTheme);
    map.put(NhAnalyticsAppEventParam.OLD_MODE,prevTheme);
    AnalyticsClient.log(NhAnalyticsAppEvent.DISPLAY_THEME_CHANGED, NhAnalyticsEventSection.APP, map,referrer);
  }
}
