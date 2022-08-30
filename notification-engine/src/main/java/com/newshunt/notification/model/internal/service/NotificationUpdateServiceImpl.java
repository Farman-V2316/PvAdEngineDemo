/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.internal.service;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.track.ApiResponseOperator;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateResponse;
import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateType;
import com.newshunt.common.model.interceptor.GenericRestFailureHandler;
import com.newshunt.common.model.retrofit.RestAdapterContainer;
import com.newshunt.dhutil.helper.retrofit.CallbackWrapper;
import com.newshunt.notification.R;
import com.newshunt.notification.model.internal.rest.NotificationServiceAPI;
import com.newshunt.notification.model.service.NotificationUpdateService;
import com.newshunt.sdk.network.Priority;

import retrofit2.Call;


/**
 * Implementation of {@link NotificationUpdateService}.
 *
 * @author shreyas.desai
 */
public class NotificationUpdateServiceImpl implements NotificationUpdateService {
  // This is always same for all android apps.
  private static final String DEVICE_TYPE = "android";

  private static NotificationUpdateServiceImpl instance;

  private NotificationServiceAPI notificationServiceAPI;

  private NotificationUpdateServiceImpl() {
  }

  public static NotificationUpdateServiceImpl getInstance() {
    if (instance == null) {
      synchronized (NotificationUpdateServiceImpl.class) {
        if (instance == null) {
          instance = new NotificationUpdateServiceImpl();
        }
      }
    }
    return instance;
  }

  @Override
  public void registerGCMId(String baseUrl, String clientId, String gcmId, boolean enabled, boolean cricketEnabled) {
    this.notificationServiceAPI = RestAdapterContainer.getInstance()
        .getRestAdapter(baseUrl, Priority.PRIORITY_HIGHEST, null, false)
        .create(NotificationServiceAPI.class);
    String message = CommonUtils.getString(com.newshunt.common.util.R.string.gcm_registration_info);
    Call<ApiResponse<Boolean>> call = notificationServiceAPI.notificationRegistration(
        clientId, gcmId, DEVICE_TYPE, enabled, cricketEnabled);
    call.enqueue(createStatusUpdateResponseCallback(message, StatusUpdateType.GCM_REGISTRATION));
  }

  @Override
  public void updateNotificationStatus(String baseUrl, String clientId, boolean enabled, StatusUpdateType requestType) {
    this.notificationServiceAPI = RestAdapterContainer.getInstance()
        .getRestAdapter(baseUrl, Priority.PRIORITY_HIGH, null,false)
        .create(NotificationServiceAPI.class);
    String message = CommonUtils.getString(com.newshunt.common.util.R.string.notification_update_status);
    Call<ApiResponse<Boolean>> call = notificationServiceAPI.notificationEnabled(clientId, enabled);
    call.enqueue(createStatusUpdateResponseCallback(message, requestType));
  }


  private CallbackWrapper<ApiResponse<Boolean>> createStatusUpdateResponseCallback(
      final String message, final StatusUpdateType statusUpdateType) {
    return new CallbackWrapper<ApiResponse<Boolean>>() {
      @Override
      public void onSuccess(ApiResponse<Boolean> response) {

        if (response == null) {
          onError(ApiResponseOperator.getError(new IllegalArgumentException("Empty response from server")));
          return;
        }

        Logger.d(NotificationUpdateServiceImpl.class.getName(), message + true);
        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse(Constants.COMMON_ID,
            response.getData(), statusUpdateType, response.getCode());
        BusProvider.getRestBusInstance().post(statusUpdateResponse);
      }

      @Override
      public void onError(BaseError error) {
        Logger.d(NotificationUpdateServiceImpl.class.getName(), message + false);
        StatusUpdateResponse errorResponse = (StatusUpdateResponse)
            GenericRestFailureHandler.handleRestFailure(
                new StatusUpdateResponse(), error);
        if (errorResponse != null) {
          errorResponse.setStatusUpdateType(statusUpdateType);
          BusProvider.getRestBusInstance().post(errorResponse);
        }

      }
    };
  }
}
