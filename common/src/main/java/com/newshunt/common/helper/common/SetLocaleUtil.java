/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Set Locale for selected language for entire application.
 *
 * @author datta.vitore
 */
public class SetLocaleUtil {

  public static final Set<String> supportedLanguages = new HashSet<>(Arrays.asList("en","hi","mr","gu","pa", "bn","kn","ta","te","ml","or","ur","bh")); // Language codes supported by client on app language page.

  public static void updateLanguage() {
    /**
     *  first check for if user selected app lang is supported by client else use English as default app language.
     */
    String navigationLanguage = AppUserPreferenceUtils.getUserNavigationLanguage();
    if(supportedLanguages.contains(navigationLanguage)) {
      updateLanguage(navigationLanguage);
    } else {
      updateLanguage(Constants.DEFAULT_LANGUAGE);
    }
  }

  public static void updateLanguage(String preferredLanguage) {
    if (TextUtils.isEmpty(preferredLanguage)) {
      return;
    }
    if(!supportedLanguages.contains(preferredLanguage)) {
      preferredLanguage = Constants.DEFAULT_LANGUAGE;
    }
    Resources resources = CommonUtils.getApplication().getResources();
    Configuration configuration = resources.getConfiguration();
    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
    Locale newLocale = new Locale(preferredLanguage);
    try {
      //TODO configuration.locale is deprecated in Android version 24, lets remove it and handle
      // RTL properly
      configuration.locale = newLocale;
    } catch (Exception e) {
      Logger.caughtException(e);
      configuration.setLocale(newLocale);
    }

    resources.updateConfiguration(configuration, displayMetrics);
    FontHelper.initializeFont(CommonUtils.getApplication(), preferredLanguage);
  }


  public static Context updateResources(@NonNull Context context) {
    String preferredLanguage = AppUserPreferenceUtils.getUserNavigationLanguage();

    if (TextUtils.isEmpty(preferredLanguage)) {
      return context;
    }
    if(!supportedLanguages.contains(preferredLanguage)) {
      preferredLanguage = Constants.DEFAULT_LANGUAGE;
    }
    Locale newLocale = new Locale(preferredLanguage);
    Resources res = context.getResources();
    Configuration configuration = res.getConfiguration();
    try {
      //TODO configuration.locale is deprecated in Android version 24, lets remove it and handle
      // RTL properly
      configuration.locale = newLocale;
    } catch (Exception e) {
      Logger.caughtException(e);
      configuration.setLocale(newLocale);
    }
    return context.createConfigurationContext(configuration);
  }
}