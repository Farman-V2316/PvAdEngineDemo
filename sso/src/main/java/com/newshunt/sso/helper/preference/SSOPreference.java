/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * User Detail Preference used for the purpose of SSO
 *
 * @author ranjith.suda
 */
public enum SSOPreference implements SavedPreference {

  NAME("userName"),
  VERSION4_USER_ID("userVersion4UserId"),
  ID("userId"),
  LOGIN_TYPE("userLoginType"),
  PASSWORD("userPassword"),
  SUBTYPE("subType"),
  PROFILEID("profileId"),
  PROFILEPIC("profilepicture"),
  USER_DATA("userData"),
  COUNTRY_CODE("countryCode");

  private final String name;

  SSOPreference(String name) {
    this.name = name;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return PreferenceType.USER_DETAIL;
  }

  @Override
  public String getName() {
    return name;
  }
}
