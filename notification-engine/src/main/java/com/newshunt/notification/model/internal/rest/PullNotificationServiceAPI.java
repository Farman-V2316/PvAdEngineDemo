/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.notification.model.entity.server.PullNotificationResponse;
import com.newshunt.notification.model.internal.rest.server.PullNotificationRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;


/**
 * @author anshul.jain on 10/24/2016.
 */

public interface PullNotificationServiceAPI {

  @POST
  Call<ApiResponse<PullNotificationResponse>> pullNotifications(@Url String requestUrl,
      @Body PullNotificationRequest request);
}
