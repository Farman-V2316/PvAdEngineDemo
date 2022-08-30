/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

//TODO (refactor)
//Remove the REST logic in the Notification-engine , Move to the dh module

/**
 * Retrofit interface to status related APIs. Helps register device,
 * update GCM and does handshake periodically.
 *
 * @author shreyas.desai
 */
public interface NotificationServiceAPI {
  @FormUrlEncoded
  @POST("api/v2/notification/register")
  Call<ApiResponse<Boolean>> notificationRegistration(
      @Field("clientId") String clientId,
      @Field("deviceId") String deviceId,
      @Field("clientType") String deviceType,
      @Field("enabled") boolean enabled,
      @Field("cricketEnabled") boolean cricketEnabled);

  @FormUrlEncoded
  @POST("api/v2/notification/enable")
  Call<ApiResponse<Boolean>> notificationEnabled(
      @Field("clientId") String clientId,
      @Field("enabled") boolean enabled);
}
