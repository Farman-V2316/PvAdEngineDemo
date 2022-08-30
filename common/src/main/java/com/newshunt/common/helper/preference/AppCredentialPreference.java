/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.preference;

/**
 * Helps save app credentials on pref manager.
 *
 * @author shreyas.desai
 */
public enum AppCredentialPreference implements SavedPreference {
  DEVICE_ID("udId", PreferenceType.APP_CREDENTIAL),
  GCM_REG_ID("newshuntGCMRegistrationId", PreferenceType.APP_CREDENTIAL),
  CLIENT_ID("clientId", PreferenceType.APP_CREDENTIAL),
  CLIENT_GENERATED_CLIENT_ID("client_generated_clientId", PreferenceType.APP_CREDENTIAL),
  INSTALL_REFERRER("installReferrer", PreferenceType.APP_CREDENTIAL),
  CLIENT_ID_STATE("client_id_state", PreferenceType.APP_CREDENTIAL),
  USER_ID("user_id", PreferenceType.APP_CREDENTIAL),
  UNIQUE_UUID("unique_uuid", PreferenceType.APP_CREDENTIAL);

  private String name;
  private PreferenceType preferenceType;

  AppCredentialPreference(String name, PreferenceType preferenceType) {
    this.name = name;
    this.preferenceType = preferenceType;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return preferenceType;
  }

  @Override
  public String getName() {
    return name;
  }
}
