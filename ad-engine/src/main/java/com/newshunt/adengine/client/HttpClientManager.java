/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.client;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.model.interceptor.UserAgentInterceptor;
import com.newshunt.common.model.retrofit.UnifiedDns;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.Priority;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Manages HTTPClient for the ad-engine.
 *
 * @author shreyas.desai
 */
public class HttpClientManager {
  public static String userAgent = System.getProperty("http.agent");

  private static OkHttpClient getClient(Priority priority) {
    OkHttpClient.Builder okHttpClientBuilder = NetworkSDK.clientBuilder(priority, null);
    okHttpClientBuilder.dns(UnifiedDns.INSTANCE);
    okHttpClientBuilder.addInterceptor(new UserAgentInterceptor());
    okHttpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
    return okHttpClientBuilder.build();
  }

  private static Request getRequest(String url, Map<String, String> bodyParams) {

    Request.Builder builder = new Request.Builder();
    builder.addHeader("User-Agent", userAgent);

    if (!CommonUtils.isEmpty(bodyParams)) {
      FormBody.Builder formEncodingBuilder = new FormBody.Builder();
      for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
        formEncodingBuilder.add(entry.getKey(), entry.getValue());
      }
      RequestBody formBody = formEncodingBuilder.build();
      builder.post(formBody);
    }
    return builder.url(url).build();
  }

  public static Call newRequestCall(String url, Priority priority) {
    return newAdRequestCall(url, null, priority);
  }

  public static Call newAdRequestCall(String url,
                                      Map<String, String> bodyParams, Priority priority) {
    if (CommonUtils.isEmpty(url)) {
      return null;
    }

    OkHttpClient client = getClient(priority);
    try {
      return client.newCall(getRequest(url, bodyParams));
    } catch (Exception exception) {
      return null;
    }
  }
}
