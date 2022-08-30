/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.retrofit;

import com.newshunt.common.model.retrofit.RestAdapterContainer;
import com.newshunt.sdk.network.Priority;

import java.util.concurrent.Executor;

import okhttp3.Interceptor;
import retrofit2.Retrofit;

/**
 * @author arun.babu
 */
public class RestAdapterProvider {


  public static Retrofit getRestAdapter(Priority priority, Object tag,
                                        Interceptor... interceptors) {
    return RestAdapterContainer.getInstance().getRestAdapter(
        NewsBaseUrlContainer.getApplicationUrl(), priority, tag, true, interceptors);
  }

  public static Retrofit getRestAdapter(Priority priority, Object tag, Executor
      callbackExecutor, Interceptor... interceptors) {
    return RestAdapterContainer.getInstance().getRestAdapter(
        NewsBaseUrlContainer.getApplicationUrl(), priority, tag, true, callbackExecutor,
        interceptors);
  }

  public static Retrofit getRestAdapter(Priority priority, Object tag, boolean
      isGzipEnabled, Interceptor... interceptors) {
    return RestAdapterContainer.getInstance().getRestAdapter(
        NewsBaseUrlContainer.getApplicationUrl(), priority, tag, isGzipEnabled, interceptors);
  }

}
