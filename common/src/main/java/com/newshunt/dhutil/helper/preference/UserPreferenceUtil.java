/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.preference;

import android.text.TextUtils;
import android.util.Pair;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.info.CredentialsHelper;
import com.newshunt.common.helper.preference.AppCredentialPreference;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.newshunt.dhutil.helper.preference.UserPrefUtilKt.langsFirst;
import static com.newshunt.dhutil.helper.preference.UserPrefUtilKt.langsRest;

/**
 * Helper class to get user preferences.
 *
 * @author maruti.borker
 */
public class UserPreferenceUtil {

  private static final String USER_LANG_EMPTY = "user_lang_empty";
  private static String userLangs = USER_LANG_EMPTY;

  public static String getUserLanguages() {
    if (USER_LANG_EMPTY.equals(userLangs)) {
      updateUserLanguages();
    }
    return userLangs;
  }

  public static List<String> getUserLanguagesList(){
    String userLangs = getUserLanguages();
    if(!CommonUtils.isEmpty(userLangs)){
      return Arrays.asList(userLangs.split(Constants.COMMA_CHARACTER));
    }
    return new ArrayList<>();
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

  public static void saveUserPrimaryLanguage(String langCode) {
    PreferenceManager.savePreference(GenericAppStatePreference.PRIMARY_LANGUAGE, langCode);
  }

  public static String getUserLangInfo() {
    return PreferenceManager.getPreference(GenericAppStatePreference.ADJNUCT_LANGUAGE,
            Constants.EMPTY_STRING);
  }

  public static void saveUserLangInfo(String langCode) {
    PreferenceManager.savePreference(GenericAppStatePreference.ADJNUCT_LANGUAGE, langCode);
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

  /*
  * returns true for current user navigation language is an RTL language
  * */
  public static boolean isUserNaviLangRtl() {
    return isRtlLang(getUserNavigationLanguage());
  }

  public static boolean isRtlLang(String lang) {
    // currently only Urdu supported; can easily extend to more langs like arabic
    return (CommonUtils.equals(lang, Constants.URDU_LANGUAGE_CODE));
  }

  public static void saveUserNavigationLanguage(String langCode, Boolean isHardened) {
    if (isHardened) {
      PreferenceManager.savePreference(GenericAppStatePreference.APP_LANGUAGE_HARDENED, true);
    }
    PreferenceManager.savePreference(GenericAppStatePreference.APP_LANGUAGE, langCode);
    updateUserLanguages();
  }

  public static String getUserSecondaryLanguages() {
    return PreferenceManager.getPreference(GenericAppStatePreference.OTHER_LANGUAGES,
        Constants.EMPTY_STRING);
  }

  public static void saveUserSecondaryLanguages(String langCodes) {
    if (CommonUtils.isEmpty(getUserPrimaryLanguage())) {
      saveUserPrimaryLanguage(langsFirst(langCodes));
      PreferenceManager.savePreference(GenericAppStatePreference.OTHER_LANGUAGES, langsRest(langCodes));
    } else {
      PreferenceManager.savePreference(GenericAppStatePreference.OTHER_LANGUAGES, langCodes);
    }
    updateUserLanguages();
  }

  public static String getUserEdition() {
    return PreferenceManager.getPreference(GenericAppStatePreference.EDITION, Constants.EMPTY_STRING);
  }


  public static void saveUserEdition(String edition) {
    PreferenceManager.savePreference(GenericAppStatePreference.EDITION, edition);
  }


  public static String getUserEditionName() {
    return PreferenceManager.getPreference(AppStatePreference.EDITION_NAME, Constants.EMPTY_STRING);
  }


  public static void saveUserEditionName(String edition) {
    PreferenceManager.savePreference(AppStatePreference.EDITION_NAME, edition);
  }

  public static void removeSecondaryLanguages() {
    PreferenceManager.remove(GenericAppStatePreference.OTHER_LANGUAGES);
  }

  public static void saveClientId(String clientId) {
    PreferenceManager.savePreference(AppCredentialPreference.CLIENT_ID, clientId);
    CredentialsHelper.saveClientIdOnFile(clientId);
  }

  public static void saveClientGeneratedClientId(String clientId) {
    PreferenceManager.savePreference(AppCredentialPreference.CLIENT_GENERATED_CLIENT_ID, clientId);

  }

  public static String getClientGeneratedClientId() {
    return PreferenceManager.getPreference(AppCredentialPreference.CLIENT_GENERATED_CLIENT_ID, Constants.EMPTY_STRING);
  }

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

  public static String getUserNavigationLanguageName() {
    return PreferenceManager.getPreference(GenericAppStatePreference.APP_LANGUAGE_NAME,
        Constants.EMPTY_STRING);
  }

  public static void saveUserNavigationLanguageName(String langName) {
    PreferenceManager.savePreference(GenericAppStatePreference.APP_LANGUAGE_NAME, langName);
  }

  private static void updateUserLanguages() {
    String primaryLanguage = getUserPrimaryLanguage();
    String otherLanguages = getUserSecondaryLanguages();
    if (otherLanguages != null && otherLanguages.length() > 0) {
      String[] userLanguages = (primaryLanguage + Constants.COMMA_CHARACTER + otherLanguages).
          split(Constants.COMMA_CHARACTER);
      Arrays.sort(userLanguages);
      userLangs = TextUtils.join(Constants.COMMA_CHARACTER, userLanguages);
    } else {
      userLangs = primaryLanguage;
    }
  }
}
