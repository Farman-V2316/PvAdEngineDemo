/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.preference;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Type of preference saved on the app.
 *
 * @author shreyas.desai
 */
public enum PreferenceType {
  APP_STATE("appStatePreferences"),
  APP_CREDENTIAL("appCredentialPreferences"),
  APP_RATE("appRatePreferences"),
  ASTRO("astro"),
  USER_DETAIL("userDetailsPreferences"),
  COACH_MARKS("coachMarks"),
  NH_APP_STATE("preferences"),
  NEWS("newsPreferences"),
  BOOKS("booksPreferences"),
  EXAMPREP("examPrepPreferences"),
  ADS("adsPreferences"),
  SSO("ssoPreference"),
  APPSFLYER_EVENTS("appsFlyerEventsPreference"),
  BUZZ("buzz"),
  VIDEO_SESSION_PREFRENCE("videoSessionPreference"),
  BACK_UP("backUp"),
  ACTIONABLE_PAYLOAD_PREFERENCE("actionablePreferene"),
  FOLLOW_BLOCK_PREFERENCE("followBlockPreference"),
  ADJUNCT_LANG("adjunctLang");

  private String fileName;

  PreferenceType(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public static PreferenceType getType(String fileName) {

    for (PreferenceType preferenceType : PreferenceType.values()) {
      if (CommonUtils.equals(preferenceType.fileName, fileName)) {
        return preferenceType;
      }
    }

    return null;
  }
}
