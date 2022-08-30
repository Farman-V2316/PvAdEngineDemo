/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.interceptor;

import android.os.SystemClock;
import androidx.annotation.NonNull;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.NetworkErrorEventHelper;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor to pass error logs as dev events.
 *
 * @author: bedprakash.rout on 04/09/17.
 */

public class NetworkErrorLoggingInterceptor implements Interceptor {

  private static long maxDelayForAPI =
      PreferenceManager.getPreference(GenericAppStatePreference.MAX_API_DELAY,
          Constants.DEFAULT_HTTP_CLIENT_TIMEOUT);
  private static String analyticsBaseUrl;

  @Override
  public Response intercept(@NonNull Chain chain) throws IOException {

    String url = chain.request().url().toString();
    long startTime = SystemClock.elapsedRealtime();

    Response response;
    try {
      response = chain.proceed(chain.request());

      // disabling error logging for analytics pings
      if (url != null && analyticsBaseUrl != null && url.startsWith(analyticsBaseUrl)) {
        return response;
      }

    } catch (SocketTimeoutException e) {
      // disabling error logging for analytics pings
      if (url != null && analyticsBaseUrl != null && url.startsWith(analyticsBaseUrl)) {
        throw e;
      }

      long timeTaken = SystemClock.elapsedRealtime() - startTime;
      NetworkErrorEventHelper.getInstance()
          .fireEvent(NetworkErrorEventHelper.getSocketTimeoutErrorEvent(url, timeTaken, chain));
      throw e;
    }

    long timeTaken = SystemClock.elapsedRealtime() - startTime;

    if (!isSuccessful(response)) {
      NetworkErrorEventHelper.getInstance()
          .fireEvent(NetworkErrorEventHelper.getAPIErrorEvent(url, response, chain));
    }

    if (timeTaken > maxDelayForAPI) {
      NetworkErrorEventHelper.getInstance().fireEvent(NetworkErrorEventHelper
          .logDelayedResponseError(url, response, timeTaken, chain));
    }
    return response;
  }

  private boolean isSuccessful(Response response) {
    return response.code() >= 200 && response.code() < 400 && response.code() != 204;
  }

  public static void setAPIDelay(long delay){
    maxDelayForAPI = delay;
  }

  public static void setAnalyticsBaseUrl(@NonNull String analyticsBaseUrl) {
    NetworkErrorLoggingInterceptor.analyticsBaseUrl = analyticsBaseUrl;
  }
}
