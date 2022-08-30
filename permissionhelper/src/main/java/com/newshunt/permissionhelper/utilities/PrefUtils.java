/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Preference Utility class for library
 * @author   bedprakash.rout on 7/29/2016.
 */
public class PrefUtils {
  private static final String PREFERENCE_NAME = "permission_details";

  private PrefUtils() {
  }

  public static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
  }

  private static SharedPreferences.Editor getEditor(Context context) {
    return getSharedPreferences(context).edit();
  }

  public static boolean getBoolean(Context context, String key) {
    return getSharedPreferences(context).getBoolean(key, false);
  }

  static boolean checkIfAlreadyAsked(Context context, String permission) {
    return getBoolean(context, permission);
  }

  private static void markPermissonAsked(Context context, String permission) {
    putBoolean(context, permission);
  }

  private static void removeBoolean(Context context, String key) {
    getEditor(context).remove(key).commit();
  }

  private static void putBoolean(Context context, String permission) {
    getEditor(context).putBoolean(permission, true).commit();
  }

  public static void markPermissionsAskedOnce(Context context, String[] permissions) {
    if (permissions == null) {
      return;
    }
    for (String permission : permissions) {
      markPermissonAsked(context, permission);
    }
  }
}
