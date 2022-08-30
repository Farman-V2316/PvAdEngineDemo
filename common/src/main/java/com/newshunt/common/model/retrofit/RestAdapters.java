/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.retrofit;

import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.preference.SavedPreference;
import com.newshunt.common.model.interceptor.InternetConnectionInterceptor;
import com.newshunt.common.model.interceptor.ServerDataInterceptor;
import com.newshunt.common.model.interceptor.UserAgentInterceptor;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.Priority;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Class with all rest Adapters for various APIs
 *
 * @author arun.babu
 */
public class RestAdapters {

  private static Map<String, HttpUrl> baseUrls = new HashMap<>();

  public static OkHttpClient.Builder getOkHttpClientBuilder(boolean gzipPostEnabled,
                                                            long timeoutInMillis,
                                                            long connectTimeoutMs,
                                                            Priority priority,
                                                            Object tag) {
    OkHttpClient.Builder builder = NetworkSDK.clientBuilder(priority, tag, gzipPostEnabled)
        .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(timeoutInMillis, TimeUnit.MILLISECONDS)
        .dns(UnifiedDns.INSTANCE)
        .writeTimeout(timeoutInMillis, TimeUnit.MILLISECONDS)
        .addInterceptor(new UserAgentInterceptor())
        .addInterceptor(new InternetConnectionInterceptor())
        .addInterceptor(ServerDataInterceptor.INSTANCE);
    return builder;
  }

  public static Retrofit.Builder getBuilder(String endPoint, boolean gzipEnabled,
                                            Priority priority, Object tag) {
    OkHttpClient.Builder clientBuilder =
        RestAdapterContainer.getInstance().getOkHttpClientBuilder(gzipEnabled, priority, tag);
    return getBuilder(endPoint, clientBuilder.build());
  }

  public static Retrofit.Builder getBuilder(String endPoint, OkHttpClient client) {
    if (!endPoint.endsWith(Constants.RETROFIT_BASE_URL_END_TOKEN)) {
      endPoint += Constants.RETROFIT_BASE_URL_END_TOKEN;
    }

    HttpUrl baseUrl = baseUrls.get(endPoint);
    if (baseUrl == null) {
      baseUrl = HttpUrl.parse(endPoint);
      baseUrls.put(endPoint, baseUrl);
    }

    return new Retrofit.Builder()
        .client(client)
        .baseUrl(baseUrl)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create());
  }

  public static Retrofit.Builder getBuilderWithDNS(String endPoint, boolean gzipEnabled,
                                                   Priority priority, Object tag,
                                                   Dns dns) {
    OkHttpClient.Builder clientBuilder =
        RestAdapterContainer.getInstance().getOkHttpClientBuilder(gzipEnabled, priority, tag);
    return getBuilder(endPoint, clientBuilder.dns(dns).build());
  }

  public static String getCompleteUrlFrom(String url, Map<String, String> queryParams, SavedPreference prefPath){
    String completeUrl = url;
    if(!CommonUtils.isEmpty(url)){

      String queryUrl = UrlUtil.getQueryUrl(url);
      String baseUrl = UrlUtil.getBaseUrl(url);

      Map<String, String> map= UrlUtil.urlRequestParamToMap(queryUrl);

      if(prefPath != null){
        String pref = PreferenceManager.getPreference(prefPath, "");
        Map<String, String> fromPrefParams = null;
        if(!CommonUtils.isEmpty(pref)){
          try{
            fromPrefParams = JsonUtils.fromJson(pref, new TypeToken<HashMap<String, String>>(){}.getType());
          }catch(Exception ex){
            Logger.caughtException(ex);
          }

          if(!CommonUtils.isEmpty(fromPrefParams)){
            map.putAll(fromPrefParams);
          }
        }
      }

      if(!CommonUtils.isEmpty(queryParams)){
        map.putAll(queryParams);
      }

      completeUrl = UrlUtil.getUrlWithQueryParamns(baseUrl, map);

      return completeUrl;

    }

    return completeUrl;
  }
}
