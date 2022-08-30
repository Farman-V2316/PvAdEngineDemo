/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Created by anshul on 17/2/17.
 */

public enum AstroPreference implements SavedPreference {

  USER_GENDER("user_gender", PreferenceType.ASTRO),
  USER_DOB("user_dob", PreferenceType.ASTRO),
  ASTRO_TOPIC_ID("astro_topic_id", PreferenceType.ASTRO),
  ASTRO_DIALOG_STATUS("astro_dialog_status", PreferenceType.ASTRO),
  ASTRO_VIEW_ORDER("astro_view_order", PreferenceType.ASTRO),
  ASTRO_SUBSCRIBED("astro_subscribed", PreferenceType.ASTRO),
  ASTRO_SUPPORTED_LANGUAGES("astro_supported_languages", PreferenceType.ASTRO),
  ASTRO_PRIORITY_LANGUAGES("astro_priority_languages", PreferenceType.ASTRO);

  @Override
  public PreferenceType getPreferenceType() {
    return preferenceType;
  }

  @Override
  public String getName() {
    return name;
  }

  private final String name;
  private final PreferenceType preferenceType;

  AstroPreference(String name, PreferenceType preferenceType) {
    this.name = name;
    this.preferenceType = preferenceType;
  }

}
