/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.model.internal.service;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.preference.SavedPreference;
import com.newshunt.common.track.ApiResponseOperator;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.view.DbgCode;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.notification.asset.DataStreamResponse;
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset;
import com.newshunt.dataentity.notification.asset.NewsStickyDataStreamAsset;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor;
import com.newshunt.notification.R;
import com.newshunt.dataentity.notification.StickyNavModelType;
import com.newshunt.notification.model.internal.rest.StreamAPI;
import com.newshunt.notification.model.service.DataStreamService;
import com.newshunt.common.helper.EmptyCookieJar;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.common.model.retrofit.RestAdapters;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.notification.helper.NotificationUrlUtil;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.notification.helper.StickyNotificationLogger;
import com.newshunt.sdk.network.Priority;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 * Created by anshul on 26/08/17.
 */

public class DataStreamServiceImpl implements DataStreamService {


  private CookieJar emptyCookieJar;
  private String type;

  public DataStreamServiceImpl(String type) {
    emptyCookieJar = new EmptyCookieJar();
    this.type = type;
  }

  @NotNull
  @Override
  public Observable<DataStreamResponse> getStreamData(@NotNull String streamUrl,
                                                      @NotNull Priority priority, @NotNull String version, @Nullable String stickType, @Nullable SavedPreference prefPath) {

    StickyNotificationLogger.logStickyNotificationCall(streamUrl, version, priority);
    OkHttpClient.Builder clientBuilder =
        RestAdapters.getOkHttpClientBuilder(true, 30_000, 30_000, priority, streamUrl).addInterceptor(new NewsListErrorResponseInterceptor());
    clientBuilder.cookieJar(emptyCookieJar);

    streamUrl = NotificationUrlUtil.getCompleteUrl(streamUrl);
    String queryUrl = UrlUtil.getQueryUrl(streamUrl);
    String baseUrl = UrlUtil.getBaseUrl(streamUrl);

    Map map = UrlUtil.urlRequestParamToMap(queryUrl);
    if (map != null && !CommonUtils.isEmpty(version)) {
      map.put(DailyhuntConstants.STREAM_VERSION, version);
    }
    if(!CommonUtils.isEmpty(stickType) && stickType.equals(NotificationConstants.STICKY_NEWS_TYPE)){
      streamUrl = RestAdapters.getCompleteUrlFrom(streamUrl, map, prefPath);
    }else{
      streamUrl = RestAdapters.getCompleteUrlFrom(streamUrl, map, null);
    }

    StreamAPI dataStreamAPI = RestAdapters.getBuilder(UrlUtil.getBaseUrl(streamUrl),
        clientBuilder.build()).build().create(StreamAPI.class);
    if (CommonUtils.equals(StickyNavModelType.CRICKET.getStickyType(), type)) {
      return dataStreamAPI.getStreamData(streamUrl).lift(new ApiResponseOperator()).map(this::transformCommon);
    } else if(CommonUtils.equals(StickyNavModelType.NEWS.getStickyType(), type)) {
      return dataStreamAPI.getNewsStickyItemsData(streamUrl).lift(new ApiResponseOperator()).map(this::transformCommon);
    }else{
      return dataStreamAPI.getGenericNotificationStreamData(streamUrl).lift(new ApiResponseOperator()).map(this::transformCommon);
    }
  }

  private DataStreamResponse transformCommon(Object apiResponse) throws Exception {
    if (CommonUtils.equals(StickyNavModelType.CRICKET.getStickyType(), type)) {
      return transform((ApiResponse<CricketDataStreamAsset>)apiResponse);
    } else if(CommonUtils.equals(StickyNavModelType.NEWS.getStickyType(), type)) {
      return transformNews((ApiResponse<NewsStickyDataStreamAsset>)apiResponse);
    }else{
      return transformGeneric((ApiResponse<GenericDataStreamAsset>)apiResponse);
    }
  }

  private DataStreamResponse transform(ApiResponse<CricketDataStreamAsset> apiResponse) {
    DataStreamResponse dataStreamResponse = new DataStreamResponse();
    if (apiResponse == null || apiResponse.getData() == null) {
      dataStreamResponse.setError(new BaseError(new DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT),
          CommonUtils.getString(R.string.no_content_found)));
    } else {
      dataStreamResponse.setBaseStreamAsset(apiResponse.getData());
    }
    return dataStreamResponse;
  }

  private DataStreamResponse transformGeneric(ApiResponse<GenericDataStreamAsset> apiResponse) {
    DataStreamResponse dataStreamResponse = new DataStreamResponse();
    if (apiResponse == null || apiResponse.getData() == null) {
      dataStreamResponse.setError(new BaseError(new DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT),
          CommonUtils.getString(R.string.no_content_found)));
    } else {
      dataStreamResponse.setBaseStreamAsset(apiResponse.getData());
    }
    return dataStreamResponse;
  }

  private DataStreamResponse transformNews(ApiResponse<NewsStickyDataStreamAsset> apiResponse){
    DataStreamResponse dataStreamResponse = new DataStreamResponse();
    if (apiResponse == null || apiResponse.getData() == null) {
      dataStreamResponse.setError(new BaseError(new DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT),
          CommonUtils.getString(R.string.no_content_found)));
    } else {
      dataStreamResponse.setBaseStreamAsset(apiResponse.getData());
    }
    return dataStreamResponse;
  }

}
