/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;

import android.os.SystemClock;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor to introduce delays in comscore requests
 * @author: bedprakash.rout on 17/07/17.
 */

public class ComscoreDelayInterceptor implements Interceptor {

  private static long lastResponseTime;
  public static long COMSCORE_DELAY =
      PreferenceManager.getPreference(AppStatePreference.COMSCORE_DELAY_IN_MILLS, 0L);

  @Override
  public Response intercept(Chain chain) throws IOException {
    long delayNeeded = calculateDelayNeeded();
    Logger.d(AsyncTrackHandler.TAG + "_" + ComscoreDelayInterceptor.class.getSimpleName(),
        "Posted with delay " + delayNeeded);
    if (delayNeeded > 0) {
      try {
        Thread.sleep(delayNeeded);
      } catch (InterruptedException e) {
        Logger.caughtException(e);
      }
    }
    Response response = chain.proceed(chain.request());
    lastResponseTime = SystemClock.elapsedRealtime();
    return response;
  }

  public static long calculateDelayNeeded() {
    return COMSCORE_DELAY - (SystemClock.elapsedRealtime() - lastResponseTime);
  }
}
