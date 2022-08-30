/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * @author arun.babu
 */
public class SharedPreferenceUtils {

  public static String getString(String pref, String key, String defaultValue) {
    SharedPreferences sharedPreferences = CommonUtils.getApplication().getSharedPreferences(pref,
        Context.MODE_PRIVATE);
    return sharedPreferences.getString(key, defaultValue);
  }

  public static boolean getBoolean(String pref, String key, boolean defaultValue) {
    SharedPreferences sharedPreferences = CommonUtils.getApplication().getSharedPreferences(pref,
        Context.MODE_PRIVATE);
    return sharedPreferences.getBoolean(key, defaultValue);
  }

  public static int getInt(String pref, String key, int defaultValue) {
    SharedPreferences sharedPreferences = CommonUtils.getApplication().getSharedPreferences(pref,
        Context.MODE_PRIVATE);
    return sharedPreferences.getInt(key, defaultValue);
  }
}
