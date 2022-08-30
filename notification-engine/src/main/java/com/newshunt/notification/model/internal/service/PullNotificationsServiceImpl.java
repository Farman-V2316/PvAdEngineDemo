/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.internal.service;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.common.model.retrofit.RestAdapterContainer;
import com.newshunt.dhutil.helper.retrofit.CallbackWrapper;
import com.newshunt.notification.model.entity.server.PullNotificationResponse;
import com.newshunt.notification.model.internal.rest.PullNotificationServiceAPI;
import com.newshunt.notification.model.internal.rest.server.PullNotificationRequest;
import com.newshunt.notification.model.service.PullNotificationsService;
import com.newshunt.sdk.network.Priority;

/**
 * Implementation of {@link com.newshunt.notification.model.service.PullNotificationsService}.
 *
 * @author anshul.jain
 */
public class PullNotificationsServiceImpl implements PullNotificationsService {

  private PullNotificationServiceAPI pullNotificationServiceAPI;

  public PullNotificationsServiceImpl() {
  }

  @Override
  public void pullNotifications(String baseUrl,
                                final PullNotificationRequest pullNotificationRequest) {
    this.pullNotificationServiceAPI = RestAdapterContainer.getInstance()
        .getRestAdapter(baseUrl, Priority.PRIORITY_LOW, this, false)
        .create(PullNotificationServiceAPI.class);
    pullNotificationServiceAPI.pullNotifications(baseUrl, pullNotificationRequest).enqueue
        (new CallbackWrapper<ApiResponse<PullNotificationResponse>>() {

          @Override
          public void onSuccess(ApiResponse<PullNotificationResponse> apiResponse) {
            if (apiResponse == null || apiResponse.getData() == null) {
              return;
            }
            PullNotificationResponse pullNotificationResponse = apiResponse.getData();
            if (pullNotificationResponse == null) {
              BaseError baseError = new BaseError("pullNotificationResponse is null");
              onError(baseError);
              return;
            }
            BusProvider.getRestBusInstance().post(pullNotificationResponse);
          }

          @Override
          public void onError(BaseError error) {
            PullNotificationResponse response = new PullNotificationResponse();
            response.setBaseError(error);
            BusProvider.getRestBusInstance().post(response);
          }
        });
  }
}
