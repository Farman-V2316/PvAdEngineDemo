/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.apis;

import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * @author shrikant.agrawal
 */
public interface CommunicationEventsAPI {

  @GET("api/v2/upgrade/definitions/events")
  Observable<ApiResponse<CommunicationEventsResponse>> getEvents(@Query("version") String version,
                                                                 @Query("appLanguage") String appLanguage,
                                                                 @Query("langCode") String langCode,
                                                                 @Query("edition") String edition);

  @GET("api/v2/upgrade/definitions/events/applaunch")
  Call<ApiResponse<CommunicationEventsResponse>> getAppLaunchConfig(
      @Query("version") String version, @Query("appLanguage") String appLanguage);
}
