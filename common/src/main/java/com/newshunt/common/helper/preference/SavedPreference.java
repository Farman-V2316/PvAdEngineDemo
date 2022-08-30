/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.preference;

/**
 * Represents each preference that is saved on the app.
 *
 * @author shreyas.desai
 */
public interface SavedPreference {
  PreferenceType getPreferenceType();

  String getName();
}
