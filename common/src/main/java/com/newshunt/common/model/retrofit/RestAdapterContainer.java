/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.retrofit;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.model.interceptor.InternetConnectionInterceptor;
import com.newshunt.common.model.interceptor.ServerDataInterceptor;
import com.newshunt.common.model.interceptor.UserAgentInterceptor;
import com.newshunt.dataentity.common.model.entity.model.TimeoutValues;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.Priority;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Shared singleton to create a single Retrofit Rest Adapter
 *
 * @author maruti.borker
 */
public class RestAdapterContainer {
  private static final String LOG_TAG = "RestAdapterContainer";
  private static volatile RestAdapterContainer instance;
  private Map<String, HttpUrl> baseUrls = new HashMap<>();
  private TimeoutValues timeouts;

  private RestAdapterContainer() {
    String timeoutString =
        PreferenceManager.getPreference(GenericAppStatePreference.NETWORK_TIMEOUTS,
            Constants.EMPTY_STRING);
    timeouts = JsonUtils.fromJson(timeoutString, TimeoutValues.class);
  }

  private void setTimeouts(TimeoutValues timeouts) {
    this.timeouts = timeouts;
  }

  public static RestAdapterContainer getInstance() {
    if (instance == null) {
      synchronized (RestAdapterContainer.class) {
        if (instance == null) {
          instance = new RestAdapterContainer();
        }
      }
    }
    return instance;
  }

  private OkHttpClient getClient(Priority priority, Object tag, boolean isGzipEnabledForAPI,
                                 Interceptor... interceptors) {

    boolean isGzipEnabled = isGzipEnabledForAPI && PreferenceManager.getPreference(
        GenericAppStatePreference.ENABLE_GZIP_ON_POST, true);
    OkHttpClient.Builder okHttpClientBuilder =
        NetworkSDK.clientBuilder(priority, tag, isGzipEnabled)
            .readTimeout(getReadTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(getWriteTimeout(), TimeUnit.MILLISECONDS)
            .connectTimeout(getConnectTimeout(), TimeUnit.MILLISECONDS)
            .dns(UnifiedDns.INSTANCE)
            .addInterceptor(new UserAgentInterceptor())
            .addInterceptor(new InternetConnectionInterceptor())
            .addInterceptor(ServerDataInterceptor.INSTANCE);

    if (interceptors != null && interceptors.length > 0) {
      for (Interceptor interceptor : interceptors) {
        okHttpClientBuilder.addInterceptor(interceptor);
      }
    }

    return okHttpClientBuilder.build();
  }

  private Retrofit.Builder getRestBuilder(String baseHost, Priority priority, Object tag, boolean
      isGzipEnabled, Executor callbackExecutor,Interceptor... interceptors) {
    return getRestBuilder(baseHost, priority, tag, isGzipEnabled, callbackExecutor, new Gson(), interceptors);
  }

  private Retrofit.Builder getRestBuilder(String baseHost, Priority priority, Object tag, boolean
          isGzipEnabled, Executor callbackExecutor, Gson gson, Interceptor... interceptors) {

    baseHost = CommonUtils.formatBaseUrlForRetrofit(baseHost);
    HttpUrl baseUrl = baseUrls.get(baseHost);
    if (baseUrl == null) {
      baseUrl = HttpUrl.parse(baseHost);
      baseUrls.put(baseHost, baseUrl);
    }
    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getClient(priority, tag, isGzipEnabled, interceptors))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create(gson));

    if (callbackExecutor != null) {
      builder.callbackExecutor(callbackExecutor);
    }

    return builder;
  }

  public OkHttpClient.Builder getOkHttpClientBuilder(boolean gzipPostEnabled,
                                                     Priority priority,
                                                     Object tag) {
    OkHttpClient.Builder builder = NetworkSDK.clientBuilder(priority, tag, gzipPostEnabled)
        .connectTimeout(getConnectTimeout(), TimeUnit.MILLISECONDS)
        .readTimeout(getReadTimeout(), TimeUnit.MILLISECONDS)
        .writeTimeout(getWriteTimeout(), TimeUnit.MILLISECONDS)
        .dns(UnifiedDns.INSTANCE)
        .addInterceptor(new UserAgentInterceptor())
        .addInterceptor(new InternetConnectionInterceptor())
        .addInterceptor(ServerDataInterceptor.INSTANCE);
    return builder;
  }

  public Retrofit getDynamicRestAdapterRx(@NonNull String baseUrl, Priority priority, Object tag,
                                          Interceptor... interceptors) {
    return getDynamicRestAdapterRx(baseUrl, priority, tag, new Gson(), interceptors);
  }
  public Retrofit getDynamicRestAdapterRx(@NonNull String baseUrl, Priority priority, Object tag, Gson gson,
                                          Interceptor... interceptors) {
    String baseHost = baseUrl;
    try {
      baseHost = UrlUtil.getHost(baseUrl);
    } catch (MalformedURLException e) {
      Logger.e(LOG_TAG, e.getMessage());
    }
    return getRestBuilder(baseHost, priority, tag, true, null, gson, interceptors).build();
  }

  public Retrofit getRestAdapter(String baseUrl, Priority priority, Object tag,
                                 Interceptor... interceptors) {
    return getRestAdapter(baseUrl, priority, tag, true, null, interceptors);
  }

  public Retrofit getRestAdapter(String baseUrl, Priority priority, Object tag, boolean
      isGzipEnabled, Interceptor... interceptors) {
    return getRestBuilder(baseUrl, priority, tag, isGzipEnabled, null, interceptors).build();
  }

  public Retrofit getRestAdapter(String baseUrl, Priority priority, Object tag, boolean
      isGzipEnabled, Executor callbackExecutor, Interceptor... interceptors) {
    return getRestBuilder(baseUrl, priority, tag, isGzipEnabled, callbackExecutor,
        interceptors).build();
  }

  public Retrofit getTVRestAdapter(Priority priority, Object tag, String baseUrl) {
    return getRestBuilder(baseUrl, priority, tag, true, null).build();
  }

  public Retrofit getVHRestAdapter(Priority priority, Object tag, String baseUrl) {
    return getRestBuilder(baseUrl, priority, tag, true, null).build();
  }

  public Retrofit getLiveTVRestAdapter(Priority priority, Object tag, String baseUrl) {
    return getRestBuilder(baseUrl, priority, tag, true, null).build();
  }

  public Retrofit getDHTVRestAdapter(Priority priority, Object tag, String baseUrl,
                                     Interceptor... interceptors) {
    return getRestBuilder(baseUrl, priority, tag, true, null, interceptors).build();
  }

  public long getReadTimeout() {
    if (timeouts != null && timeouts.getDefault() != null && timeouts.getDefault().getRead() > 0) {
      return timeouts.getDefault().getRead();
    }

    return Constants.DEFAULT_HTTP_CLIENT_TIMEOUT;
  }

  public long getWriteTimeout() {
    if (timeouts != null && timeouts.getDefault() != null && timeouts.getDefault().getWrite() > 0) {
      return timeouts.getDefault().getWrite();
    }

    return Constants.DEFAULT_HTTP_CLIENT_TIMEOUT;
  }

  public long getConnectTimeout() {
    if (timeouts != null && timeouts.getDefault() != null &&
        timeouts.getDefault().getConnect() > 0) {
      return timeouts.getDefault().getConnect();
    }

    return Constants.DEFAULT_HTTP_CONNECT_TIMEOUT;
  }
}
