/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Enum Class responsible for holding the Nh Preferences
 *
 * @author ranjith.suda
 */
public enum NhAppStatePreference implements SavedPreference {

  SHOW_AIRTEL_AFRICA("show_airtel_africa"),
  TESTPREP_DISPLAY_NAME("testprep_display_name");

  private String name;

  NhAppStatePreference(String name) {
    this.name = name;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return PreferenceType.NH_APP_STATE;
  }

  @Override
  public String getName() {
    return name;
  }
}
