/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.preference;

import android.text.TextUtils;
import android.util.Pair;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.info.CredentialsHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.util.Arrays;
import java.util.UUID;

/**
 * Generic App User Preference CommonUtils
 *
 * @author ranjith.suda
 */
public class AppUserPreferenceUtils {

  public static String getClientId() {
    String clientId =
        PreferenceManager.getPreference(AppCredentialPreference.CLIENT_ID, Constants.EMPTY_STRING);
    if (DataUtil.isEmpty(clientId)) {
      Pair<String, String> credentials = CredentialsHelper.getCredentialsFromFile();
      if (credentials != null) {
        if (!CommonUtils.isEmpty(credentials.first)) {
          PreferenceManager.savePreference(AppCredentialPreference.CLIENT_ID,
              credentials.first);
        }
        return credentials.first;
      }
    }

    return clientId;
  }

  public static String getUserId() {
    return PreferenceManager.getPreference(AppCredentialPreference.USER_ID, Constants.EMPTY_STRING);
  }

  public static void saveUserId(String userId) {
    PreferenceManager.savePreference(AppCredentialPreference.USER_ID, userId);
  }

  public static String getUserType() {
    return PreferenceManager.getPreference(GenericAppStatePreference.USER_TYPE, Constants.EMPTY_STRING);
  }

  public static void saveUserType(boolean isCreator) {
    PreferenceManager.savePreference(GenericAppStatePreference.USER_TYPE,
        isCreator ? Constants.CREATOR : Constants.USER);
  }

  public static UserAppSection getPreviousAppSection() {
    String appSectionString = PreferenceManager.getPreference(GenericAppStatePreference
        .USER_APP_SECTION_SELECTED, Constants.EMPTY_STRING);
    if (CommonUtils.isEmpty(appSectionString)) {
      AppSection oldVersionAppSection = AppSection.fromName(PreferenceManager.getPreference
          (GenericAppStatePreference.APP_SECTION_SELECTED, Constants.EMPTY_STRING));
      return new UserAppSection.Builder().section(oldVersionAppSection).build();
    }
    return JsonUtils.fromJson(appSectionString, UserAppSection.class);
  }

  public static void setAppSectionSelected(UserAppSection userAppSection) {
    if (userAppSection == null) {
      return;
    }
    String appSectionString = JsonUtils.toJson(userAppSection);
    PreferenceManager.savePreference(GenericAppStatePreference.USER_APP_SECTION_SELECTED,
        appSectionString);
  }

  public static String getUserNavigationLanguage() {
    String navigationLanguage =
        PreferenceManager.getPreference(GenericAppStatePreference.APP_LANGUAGE,
            Constants.EMPTY_STRING);
    if (DataUtil.isEmpty(navigationLanguage)) {
      return Constants.ENGLISH_LANGUAGE_CODE;
    }
    return navigationLanguage;
  }

  public static void saveUserNavigationLanguage(String langCode) {
    PreferenceManager.savePreference(GenericAppStatePreference.APP_LANGUAGE, langCode);
  }

  public static String getUserLanguages() {
    String primaryLanguage = getUserPrimaryLanguage();
    String otherLanguages = getUserSecondaryLanguages();
    if (otherLanguages != null && otherLanguages.length() > 0) {
      String[] userLanguages = (primaryLanguage + Constants.COMMA_CHARACTER + otherLanguages).
          split(Constants.COMMA_CHARACTER);
      Arrays.sort(userLanguages);
      return TextUtils.join(Constants.COMMA_CHARACTER, userLanguages);
    } else {
      return primaryLanguage;
    }
  }

  /**
   * Returns primary language selected by user
   *
   * @return String - primary language code
   */
  public static String getUserPrimaryLanguage() {
    return PreferenceManager.getPreference(GenericAppStatePreference.PRIMARY_LANGUAGE,
        Constants.EMPTY_STRING);
  }


  public static String getUserSecondaryLanguages() {
    return PreferenceManager.getPreference(GenericAppStatePreference.OTHER_LANGUAGES,
        Constants.EMPTY_STRING);
  }

  public static int getNewsLaunchCount() {
    return PreferenceManager.getPreference(GenericAppStatePreference.NEWS_HOME_LAUNCH_COUNT, 0);
  }

  public static void saveNewsLaunchCount(int count) {
    PreferenceManager.savePreference(GenericAppStatePreference.NEWS_HOME_LAUNCH_COUNT, count);
  }

  public static boolean isFirstUseAfterInstall() {
    return PreferenceManager.getPreference(GenericAppStatePreference.IS_FIRST_USE_AFTER_INSTALL,
        true);
  }

  public static void setFirstUseAfterInstall(boolean state) {
    PreferenceManager.savePreference(GenericAppStatePreference.IS_FIRST_USE_AFTER_INSTALL, state);
  }

  public static String getEdition() {
    return PreferenceManager.getPreference(GenericAppStatePreference.EDITION,
        Constants.EMPTY_STRING);
  }

  /**
   * Increment the app launch count and save count in preference manager.
   *
   * @return
   */
  public static void incrementAppLaunchCount() {
    int launchCount = getAppLaunchCount();
    launchCount++;
    setAppLaunchCount(launchCount);
  }

  public static int getAppLaunchCount() {
    return PreferenceManager.getPreference(GenericAppStatePreference.APP_LAUNCH_COUNT, 0);
  }

  public static void setAppLaunchCount(int count) {
    PreferenceManager.savePreference(GenericAppStatePreference.APP_LAUNCH_COUNT, count);
  }

  public static boolean getIsCreatePostWalkthroughShown() {
    return PreferenceManager.getPreference(AppStatePreference.IS_CREATE_POST_WALKTHROUGH_SHOWN,
        false);
  }

  public static void setIsCreatePostWalkthroughShown(Boolean value) {
    PreferenceManager.savePreference(AppStatePreference.IS_CREATE_POST_WALKTHROUGH_SHOWN, value);
  }

  public static String generateRandomUserIdForPost() {
      return AppUserPreferenceUtils.getUserId() + "_" + UUID.randomUUID().toString().replace("-","");
  }

  public static void setContactLiteSyncDone() {
    PreferenceManager.savePreference(AppStatePreference.CONTACT_LITE_SYNC_DONE, true);
  }

  public static boolean isContactLiteSyncDone() {
    return PreferenceManager.getPreference(AppStatePreference.CONTACT_LITE_SYNC_DONE, false);
  }

  public static void flushContactLiteSyncStatus() {
    PreferenceManager.savePreference(AppStatePreference.CONTACT_LITE_SYNC_DONE, false);
  }

  public static boolean shouldShowSwipeUpCoachMark() {
    return PreferenceManager.getBoolean(AppStatePreference.APP_SWIPE_UP_COACH_MARK_STATE.getName(),
        false);
  }

  public static void doneShowingSwipeUpCoachMark() {
    PreferenceManager.saveBoolean(AppStatePreference.APP_SWIPE_UP_COACH_MARK_STATE.getName(), true);
  }

  public static boolean isLocalZoneFirstLaunch() {
    return PreferenceManager.getBoolean(AppStatePreference.IS_LOCAL_ZONE_FIRST_LAUNCH.getName(), true);
  }

  public static void setLocalZoneFirstLaunchDone() {
    PreferenceManager.saveBoolean(AppStatePreference.IS_LOCAL_ZONE_FIRST_LAUNCH.getName(), false);
  }

  public static void setAppsFlyerUID(String appsFlyerUID) {
    PreferenceManager.savePreference(GenericAppStatePreference.APPSFLYER_UID, appsFlyerUID);
  }

  public static String getAppsFlyerUID() {
    return PreferenceManager.getPreference(
            GenericAppStatePreference.APPSFLYER_UID, Constants.EMPTY_STRING);
  }

  public static int getLastAppVersion() {
    return PreferenceManager.getPreference(AppStatePreference.LAST_KNOWN_APP_VERSION, 0);
  }

}
