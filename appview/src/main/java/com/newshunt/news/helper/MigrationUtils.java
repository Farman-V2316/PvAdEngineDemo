/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.cookie.PersistentCookieStore;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.model.retrofit.CacheDns;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.status.ClientInfo;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.deeplink.navigator.CommonNavigator;
import com.newshunt.deeplink.navigator.NewsNavigator;
import com.newshunt.dhutil.analytics.AnalyticsHelper;
import com.newshunt.dhutil.helper.RateUsDialogHelper;
import com.newshunt.dhutil.helper.SharableAppDialogHelper;
import com.newshunt.dhutil.helper.nhcommand.NHActivityCommandHandler;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.preference.UserDetailPreference;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.dhutil.helper.theme.ThemeType;
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper;
import com.newshunt.news.helper.handler.NewsNHActivityCommandHandler;
import com.newshunt.news.model.sqlite.MigrationSQLiteHelper;
import com.newshunt.notification.helper.PullNotificationsDataHelper;
import com.newshunt.onboarding.helper.EditionMigrationHelper;
import com.newshunt.onboarding.helper.LanguageMaskAdapter;
import com.newshunt.onboarding.helper.LaunchHelper;
import com.newshunt.onboarding.helper.OldLanguagePrefReader;
import com.newshunt.pref.NewsPreference;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for app module to access news-app code. Mocked out in news-mock to allow for commenting
 * k
 *
 * @author maruti.borker
 */
public class MigrationUtils {

  public static final int DATA_VERSION = 8;

  private static final String APPBAR_ICONS_VERSION_ENTITY = "APPBAR_ICONS";
  private static final String APPBAR_ICONS_SAVE_FOLDER = CommonUtils.getApplication().getFilesDir()
      .getAbsolutePath() + File.separator + "bottombaricons";
  private static final String FOLLOW_PAGE_VERSION_ENTITY = "FOLLOW_PAGE";

  public static void migrateDHToIdeate(ClientInfo clientInfo) {
    if (LaunchHelper.isIdeateFirstLaunch() && clientInfo != null &&
        clientInfo.getClientId() != null) {
      LaunchHelper.disableIdeateFirstLaunch();
    }
  }

  private static void cleanUpAppBarIconsEntity() {
    List<String> versionList = new ArrayList<>();
    versionList.add(APPBAR_ICONS_VERSION_ENTITY);

    VersionedApiHelper.cleanUpVersionedData(versionList);
    File dir = new File(APPBAR_ICONS_SAVE_FOLDER);

    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    for (File file : dir.listFiles()) {
      if (file.isFile() && file.exists()) {
        try {
          file.delete();
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    }

    try {
      dir.delete();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  private static void cleanUpFollowPagesEntity() {
    List<String> versionList = new ArrayList<>();
    versionList.add(FOLLOW_PAGE_VERSION_ENTITY);

    VersionedApiHelper.cleanUpVersionedData(versionList);
  }

  public static NHActivityCommandHandler getNewsNHCommandHandler() {
    return new NewsNHActivityCommandHandler();
  }

  public static Intent getTargetIntent(Context context, NewsNavModel navigationModel,
                                       PageReferrer referrer) {
    return NewsNavigator.getNotificationTargetIntent(context, navigationModel, referrer);
  }

  public static boolean needsPageSyncOnUpgrade() {
    return PreferenceManager.getPreference(NewsPreference.APP_UPGRADE_PAGE_SYNC, false);
  }

  public static void checkAndSetIfPageSyncNeeded() {
    if (CommonNavigator.isFirstLaunch()) {
      return;
    }
    int previousVersionCode =
        PreferenceManager.getPreference(AppStatePreference.LAST_KNOWN_APP_VERSION, 0);
    if (previousVersionCode == AppConfig.getInstance().getAppVersionCode()) {
      return;
    }
    setNeedsPageSyncedOnUpgrade(true);
  }

  public static void setNeedsPageSyncedOnUpgrade(boolean needToSync) {
    PreferenceManager.savePreference(NewsPreference.APP_UPGRADE_PAGE_SYNC, needToSync);
  }

  public static void migrateDBPreferences(ClientInfo clientInfo, Context context) {
    if (CommonNavigator.isFirstLaunch() && clientInfo != null && clientInfo.getClientId() != null) {
      PreferenceManager.savePreference(UserDetailPreference.IS_UPGRADE_USER, true);
      migrateNHtoDBPreferences(clientInfo.getClientId(), context);
      RateUsDialogHelper.firstLaunchOrLatestUpgradeTime();
    }
  }

  private static void migrateNHtoDBPreferences(String clientId, Context context) {
    LaunchHelper.logTime("MigrationUtils: migrate NH to DH: Entry");
    MigrationSQLiteHelper migrationSQLiteHelper = new MigrationSQLiteHelper(context);
    migrationSQLiteHelper.cleanOldData();

    int langMask = OldLanguagePrefReader.getSavedLangMask(context);
    if (langMask > 0) {
      String languages = LanguageMaskAdapter.getLanguages(langMask);
      // Only one language. Can safely save as primary.
      if (!DataUtil.isEmpty(languages) && !languages.contains(",")) {
        UserPreferenceUtil.saveUserPrimaryLanguage(languages);
      } else if (!DataUtil.isEmpty(languages) && languages.contains(",")) {
        List<String> languageList = DataUtil.parsAsList(languages, ",");
        String primaryLanguage = languageList.get(0);
        UserPreferenceUtil.saveUserPrimaryLanguage(primaryLanguage);
        languageList.remove(primaryLanguage);
        String secondaryLanguages = DataUtil.parseAsString(languageList);
        UserPreferenceUtil.saveUserSecondaryLanguages(secondaryLanguages);
      }
      //Preference to save , whether this is Nh -> Dh upgrade
      PreferenceManager.savePreference(AppStatePreference.IS_NH_2_DH_UPGRADE, true);
    }

    String editionKey = OldLanguagePrefReader.getSavedEdition();
    if (!DataUtil.isEmpty(editionKey)) {
      UserPreferenceUtil.saveUserEdition(editionKey);
    }

    final int WHITE_COLOR_HEX_CODE = 0xffffffff;
    int themeColor = OldLanguagePrefReader.getThemeColor(context);
    if (themeColor != WHITE_COLOR_HEX_CODE) {
      PreferenceManager.savePreference(AppStatePreference.APPLIED_THEME, ThemeType.NIGHT.name());
    }
    PreferenceManager.savePreference(AppStatePreference.OLD_PREFERENCE_SAVED, Boolean.TRUE);
    LaunchHelper.logTime("MigrationUtils: migrate NH to DH : Exit");
  }

  public static void migrateSharedPreferences() {
    LaunchHelper.logTime("MigrationUtils: migrateSharedPreferences: Entry");

    // TV - Deprecated Constants
    String KEY_SEARCH_ENABLE_CONFIG = "key_search_enable_config";
    String TV_LASTLOGGEDTIME = "TV_LASTLOGGEDTIME";
    String VIDEO_START_SYSTEM_TIME = "video_start_system_time";
    String CHANNELS_PREFS_CACHE = "channels_prefs_cache";
    String CHANNELS_PREFS = "channels_prefs";
    String MY_PLAYLISTS_PREFS = "my_playlists_prefs";
    String TV_APP_LANGUAGE = "tv_app_language";
    String TV_LANGCODE = "tv_langcode";
    String TV_APP_VERSION = "tv_app_version";
    String KEY_CHANNEL_TAB_JSON_DATA = "key_channel_tab_json_data";
    String KEY_GROUP_TABS_JSON_DATA = "key_group_tabs_json_data";
    String FBUSERID = "fbclientId";
    String DH_ALL_PLAYER_INFO = "DH_ALL_PLAYER_INFO";
    String VIDEO_SETTING_USER = "VideoSettingUser";
    String IMAGE_SETTINGS_NETWORK = "ImageSettingNetwork";
    String IMAGE_SETTINGS_USER = "ImageSettingUser";
    String KEY_IMAGE_DIMENSION_JSON_DATA = "key_image_dimension_json_data";
    String VIDEO_SETTINGS_NETWORK = "VideoSettingNetwork";
    String CHECK_WHATSAPP_PREFS = "enableWhatsappPrefs";
    String CHECK_FACEBOOK_PREFS = "enableFacebookPrefs";
    String CHECK_FACEBOOK_PACKAGE = "enableFacebookPackage";

    Map<String, Boolean> booleansToMigrate = new HashMap<>();
    booleansToMigrate.put(Constants.LOG_COLLECTION_IN_PROGRESS, false);
    booleansToMigrate.put(Constants.DISABLE_DNS_CACHING, false);
    booleansToMigrate.put(Constants.LOG_COLLECTION_UPLOADING_PENDING, false);
    booleansToMigrate.put(Constants.APPSFLYER_DEEP_LINK_HANDLED, false);
    booleansToMigrate.put(Constants.GAID_OPT_OUT_STATUS, false);
    booleansToMigrate.put(PullNotificationsDataHelper.PULL_NOTIFICATIONS_ENABLED_BY_SERVER, false);
    booleansToMigrate.put(PullNotificationsDataHelper.PRE_SCHEDULED_NOTIFICATION_SHOWN, false);
    booleansToMigrate.put(KEY_SEARCH_ENABLE_CONFIG, true);
    booleansToMigrate.put(CHECK_FACEBOOK_PREFS, false);
    booleansToMigrate.put(CHECK_WHATSAPP_PREFS, false);

    Map<String, Integer> integersToMigrate = new HashMap<>();
    integersToMigrate.put(PullNotificationsDataHelper.PULL_NOTIFICATION_FIRST_TIME_DELAY, 0);

    Map<String, Long> longsToMigrate = new HashMap<>();
    longsToMigrate.put(Constants.DNS_LOOKUP_TIMEOUT, CacheDns.DEFAULT_DNS_LOOKUP_TIMEOUT);
    longsToMigrate.put(Constants.DNS_FIRST_CACHE_TTL, CacheDns.FIRST_CACHE_TTL);
    longsToMigrate.put(Constants.DNS_SECOND_CACHE_TTL, CacheDns.SECOND_CACHE_TTL);
    longsToMigrate.put(Constants.LOG_COLLECTION_END_TIME, System.currentTimeMillis());
    longsToMigrate.put(Constants.UNLOGGED_CACHE_BROWSED_TIME, 0L);
    longsToMigrate.put(TV_LASTLOGGEDTIME, 0L);
    longsToMigrate.put(VIDEO_START_SYSTEM_TIME, 0L);

    Map<String, String> stringToMigrate = new HashMap<>();
    stringToMigrate.put(Constants.LOG_COLLECTION_LAST_COLLECTION_TIME, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.LOG_COLLECTION_AUTH_TOKEN, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.APPSFLYER_DEEP_LINK_RESPONSE, Constants.EMPTY_STRING);
    stringToMigrate.put(PullNotificationsDataHelper.PULL_NOTIFICATIONS_LAST_SUCCESSFUL_SYNCED_TIME,
        Constants.EMPTY_STRING);
    stringToMigrate.put(PullNotificationsDataHelper.PULL_NEXT_JOB_RUNNING_TIME,
        Constants.EMPTY_STRING);
    stringToMigrate.put(PullNotificationsDataHelper.PULL_NOTIFICATION_SALT, Constants.EMPTY_STRING);
    stringToMigrate.put(PullNotificationsDataHelper.LAST_PUSH_NOTIFICATION_TIMESTAMP,
        Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.DNS_IP_FROM_SERVER, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.APPS_ON_DEVICE, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.FIREBASE_DEEP_LINK_URL, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.ADD_ID, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.UNIQUE_ID, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.DNS_LOOKUP_CACHE, Constants.EMPTY_STRING);
    stringToMigrate.put(Constants.LOG_COLLECTION_UPLOAD_URL_VALUE, Constants.EMPTY_STRING);
    stringToMigrate.put(PullNotificationsDataHelper.PULL_NOTIFICATION_STATE,
        Constants.EMPTY_STRING);
    stringToMigrate.put(PullNotificationsDataHelper.PULL_NOTIFICATION_SYNC_CONFIG,
        Constants.EMPTY_STRING);
    stringToMigrate.put(CHANNELS_PREFS_CACHE, Constants.EMPTY_STRING);
    stringToMigrate.put(CHANNELS_PREFS, Constants.EMPTY_STRING);
    stringToMigrate.put(MY_PLAYLISTS_PREFS, Constants.EMPTY_STRING);
    stringToMigrate.put(TV_APP_LANGUAGE, Constants.EMPTY_STRING);
    stringToMigrate.put(TV_LANGCODE, Constants.EMPTY_STRING);
    stringToMigrate.put(TV_APP_VERSION, Constants.EMPTY_STRING);
    stringToMigrate.put(KEY_CHANNEL_TAB_JSON_DATA, Constants.EMPTY_STRING);
    stringToMigrate.put(KEY_GROUP_TABS_JSON_DATA, Constants.EMPTY_STRING);
    stringToMigrate.put(FBUSERID, Constants.EMPTY_STRING);
    stringToMigrate.put(DH_ALL_PLAYER_INFO, Constants.EMPTY_STRING);
    stringToMigrate.put(VIDEO_SETTING_USER, Constants.EMPTY_STRING);
    stringToMigrate.put(IMAGE_SETTINGS_NETWORK, Constants.EMPTY_STRING);
    stringToMigrate.put(IMAGE_SETTINGS_USER, Constants.EMPTY_STRING);
    stringToMigrate.put(KEY_IMAGE_DIMENSION_JSON_DATA, Constants.EMPTY_STRING);
    stringToMigrate.put(VIDEO_SETTINGS_NETWORK, Constants.EMPTY_STRING);
    stringToMigrate.put(CHECK_FACEBOOK_PACKAGE, Constants.EMPTY_STRING);

    Context context = CommonUtils.getApplication();

    for (String key : booleansToMigrate.keySet()) {
      boolean defaultValue = booleansToMigrate.get(key);
      SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
      boolean value = sharedPreferences.getBoolean(key, defaultValue);
      PreferenceManager.saveBoolean(key, value);
    }

    for (String key : integersToMigrate.keySet()) {
      int defaultValue = integersToMigrate.get(key);
      SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
      int value = sharedPreferences.getInt(key, defaultValue);
      PreferenceManager.saveInt(key, value);
    }

    for (String key : longsToMigrate.keySet()) {
      long defaultValue = longsToMigrate.get(key);
      SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
      long value = sharedPreferences.getLong(key, defaultValue);
      PreferenceManager.saveLong(key, value);
    }

    for (String key : stringToMigrate.keySet()) {
      String defaultValue = stringToMigrate.get(key);
      SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
      String value = sharedPreferences.getString(key, defaultValue);
      PreferenceManager.saveString(key, value);
    }

    LaunchHelper.logTime("MigrationUtils: migrateSharedPreferences: Exit");
  }

  /**
   * All data migration until V1
   */
  public static void migrateV0toV1(ClientInfo clientInfo, Context context) {
    LaunchHelper.logTime("NewsHuntAppController: migrateV0toV1: Entry");

    // updating to Ideate. Clearing the default data and adding the versioned data.
    MigrationUtils.migrateDHToIdeate(clientInfo);

    MigrationUtils.migrateSharedPreferences();

    LaunchHelper.logTime("NewsHuntAppController: migrateV0toV1: Exit");
  }

  /**
   * Delete all disk cache files of Picasso
   */
  public static void migrateV1toV2() {
    LaunchHelper.logTime("NewsHuntAppController: migrateV1toV2: Entry");
    File fp = NetworkSDKUtils.createDefaultCacheDir(Constants.PICASSO_DISK_CACHE);
    if (fp != null) {
      fp.delete();
    }
    LaunchHelper.logTime("NewsHuntAppController: migrateV1toV2: Exit");
  }

  public static void migrate(ClientInfo clientInfo, Context context) {
    int appDataVersion = PreferenceManager.getPreference(AppStatePreference.APP_DATA_VERSION, 0);

    for (int i = appDataVersion; i < DATA_VERSION; i++) {
      switch (i) {
        case 0:
          MigrationUtils.migrateV0toV1(clientInfo, context);
          // Save state of latest app data version before proceeding to next
          PreferenceManager.savePreference(AppStatePreference.APP_DATA_VERSION, i);
          break;
        case 1:
          MigrationUtils.migrateV1toV2();
          PreferenceManager.savePreference(AppStatePreference.APP_DATA_VERSION, i);
          break;
          //never add case 2, as EditionMigration will execute twice.
        case 3:
          EditionMigrationHelper.INSTANCE.migrate();
          break;
        case 4:
          cleanUpAppBarIconsEntity();
          break;
        case 5:
          PersistentCookieStore.getInstance().migrateHalfBakedCookies("http://newshuntads.com");
          PersistentCookieStore.getInstance().migrateHalfBakedCookies("http://dailyhunt.in");
          PersistentCookieStore.getInstance().migrateHalfBakedCookies("http://newshunt.com");
          PersistentCookieStore.getInstance().migrateHalfBakedCookies("http://newshunt.in");
          break;
        case 6:
          cleanUpFollowPagesEntity();
          break;
        case 7:
          cleanupPreSocialDBs();
          break;
      }
    }

    PreferenceManager.savePreference(AppStatePreference.APP_DATA_VERSION, DATA_VERSION);
  }

  private static void cleanupPreSocialDBs() {
    try {
      //Delete the profile DB
      CommonUtils.getApplication().deleteDatabase("profile-db");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }

    try {
      //Delete the profile DB
      CommonUtils.getApplication().deleteDatabase("newshunt.news.newspage");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }

    try {
      //Delete the profile DB
      CommonUtils.getApplication().deleteDatabase("newshunt.news.follow");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }

    try {
      //Delete the profile DB
      CommonUtils.getApplication().deleteDatabase("newshunt.news.pullinfo");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }

    try {
      //Delete the profile DB
      CommonUtils.getApplication().deleteDatabase("newshunt.news");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }

    try {
      //Delete the profile DB
      CommonUtils.getApplication().deleteDatabase("newshunt.news.social");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }

    try {
      CommonUtils.getApplication().deleteDatabase("UiEvents");
    } catch (Throwable ex) {
      Logger.caughtException(ex);
    }
  }
}
