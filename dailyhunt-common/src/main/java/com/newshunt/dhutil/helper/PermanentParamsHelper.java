/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper;


import com.newshunt.common.helper.preference.AppUserPreferenceUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Helper for adding permanent params to all book requests
 *
 * @author arun.babu
 */
public class PermanentParamsHelper {

  private static final String APP_LANGUAGE = "appLanguage";
  private static final String PRIMARY_LANGUAGE = "primaryLanguage";
  private static final String SECONDARY_LANGUAGES = "secondaryLanguages";

  public static String appendPermanentParams(String path) {
    if (path.contains("?")) {
      path = path + "&";
    } else {
      path = path + "?";
    }

    path = path +
        APP_LANGUAGE + "=" +
        AppUserPreferenceUtils.getUserNavigationLanguage() + "&" + PRIMARY_LANGUAGE + "=" +
        AppUserPreferenceUtils.getUserPrimaryLanguage() + "&" + SECONDARY_LANGUAGES + "=" +
        AppUserPreferenceUtils.getUserSecondaryLanguages();

    return path;
  }

  public static Interceptor getRequestInterceptor() {
    return new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Request request = addPermanentParams(chain.request());
        return chain.proceed(request);
      }
    };
  }

  private static Request addPermanentParams(Request requestFacade) {
    HttpUrl url = requestFacade.url().newBuilder()
        .addQueryParameter(APP_LANGUAGE, AppUserPreferenceUtils.getUserNavigationLanguage())
        .addQueryParameter(PRIMARY_LANGUAGE, AppUserPreferenceUtils.getUserPrimaryLanguage())
        .addQueryParameter(SECONDARY_LANGUAGES, AppUserPreferenceUtils.getUserSecondaryLanguages())
        .build();
    return requestFacade.newBuilder().url(url).build();
  }
}
