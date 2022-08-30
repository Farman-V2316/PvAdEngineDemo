/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.NewsAppJSResponse;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Retrofit interface to get the News App Java scripts.
 *
 * @author santhosh.kc
 */
public interface NewsAppJSApi {

  @Headers("variableResolution: y")
  @GET("/api/v2/upgrade/dynamic/version?entity=APP_JS_V2")
  Observable<ApiResponse<NewsAppJSResponse>> getJS(@Query("version") String version,
                                                   @Query("langCode") String userLangCodes);
}
