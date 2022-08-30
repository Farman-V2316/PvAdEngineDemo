/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Retrofit interface for saving/reading user preferences
 *
 * @author maruti.borker
 */
public interface PreferenceAPI {

  @POST("api/v2/preferences/user/multiple/{clientId}")
  Call<ApiResponse<Boolean>> addMultiple(@Path("clientId") String clientId,
                                         @Body Map<String, String> preferences);

}
