package com.dailyhunt.tv.players.api;

import com.dailyhunt.tv.players.model.entities.server.PlayerErrorInfo;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Jayanth on 09/05/18.
 */
public interface PlayerErrorReportAPI {
  @Headers("Content-Type: application/json; charset=utf-8")
  @POST("item/track")
  Call<ApiResponse<Object>> reportError(@Body PlayerErrorInfo upgradeInfo);
}

