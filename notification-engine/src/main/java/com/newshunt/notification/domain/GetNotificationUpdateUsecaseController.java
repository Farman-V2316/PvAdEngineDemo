/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.domain;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.CommonBaseUrlsContainer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.ExponentialRetryHelper;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateResponse;
import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateType;
import com.newshunt.notification.analytics.NhGCMRegistrationAnalyticsUtility;
import com.newshunt.notification.analytics.NhRegistrationDestination;
import com.newshunt.notification.analytics.NhRegistrationEventStatus;
import com.newshunt.notification.model.service.NotificationUpdateService;
import com.squareup.otto.Subscribe;

/**
 * Implementation of {@link GetNotificationUpdateUsecase}.
 *
 * @author shreyas.desai
 */
public class GetNotificationUpdateUsecaseController implements GetNotificationUpdateUsecase {
  private static GetNotificationUpdateUsecaseController instance;

  private NotificationUpdateService notificationUpdateService;
  private ExponentialRetryHelper retryHelper;

  private GetNotificationUpdateUsecaseController(
      NotificationUpdateService notificationUpdateService) {
    this.notificationUpdateService = notificationUpdateService;
  }

  public static GetNotificationUpdateUsecaseController getInstance(
      NotificationUpdateService notificationUpdateService) {
    if (instance == null) {
      synchronized (GetNotificationUpdateUsecaseController.class) {
        if (instance == null) {
          instance = new GetNotificationUpdateUsecaseController(notificationUpdateService);
        }
      }
    }
    return instance;
  }

  @Override
  public void registerGCMId(final String clientId, final String gcmId, final boolean enabled, final boolean cricketEnabled) {
    BusProvider.getRestBusInstance().register(this);
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        String baseUrl = CommonBaseUrlsContainer.getInstance().getNotificationTriggerUrl();
        notificationUpdateService.registerGCMId(baseUrl, clientId, gcmId, enabled, cricketEnabled);
      }
    };

    retryHelper = new ExponentialRetryHelper(runnable, Constants.RETRY_INITIAL_INTERVAL,
        Constants.RETRY_MAX_INTERVAL, Constants.RETRY_MAX_ATTEMPT, Constants.RETRY_MULTIPLIER);
    retryHelper.start();
  }

  @Override
  public void updateNotificationStatus(String clientId, boolean enabled, boolean cricketEnabled,
                                       StatusUpdateType requestType) {
    BusProvider.getRestBusInstance().register(this);
    String baseUrl = CommonBaseUrlsContainer.getInstance().getNotificationNewsUrl();
    notificationUpdateService.updateNotificationStatus(baseUrl, clientId, enabled, requestType);
  }

  @Subscribe
  @Override
  public void onStatusUpdate(StatusUpdateResponse statusUpdateResponse) {
    if (null == statusUpdateResponse) {
      onFailure(Constants.EMPTY_STRING, Constants.NULL);
    } else if (statusUpdateResponse.getError() != null) {
      BaseError error = statusUpdateResponse.getError();
      onFailure(Constants.EMPTY_STRING + error.getStatusAsInt(), error.getMessage());
    } else {
      BusProvider.getRestBusInstance().unregister(this);
      onSuccess(Constants.EMPTY_STRING + statusUpdateResponse.getStatus());
    }
  }

  @Override
  public void execute() {
  }

  private void onFailure(String responseCode, String message) {
    if (null == retryHelper) {
      return;
    }

    NhGCMRegistrationAnalyticsUtility.registerAttemptEvent(NhRegistrationDestination.WAKEUP,
        NhRegistrationEventStatus.FAILURE, responseCode, message, retryHelper.getAttemptNumber());

    retryHelper.onFailure(NhRegistrationDestination.WAKEUP.toString());
  }

  private void onSuccess(String responseCode) {
    if (null == retryHelper) {
      return;
    }

    NhGCMRegistrationAnalyticsUtility.registerAttemptEvent(NhRegistrationDestination.WAKEUP,
        NhRegistrationEventStatus.SUCCESS, responseCode, Constants.EMPTY_STRING,
        retryHelper.getAttemptNumber());

    NhGCMRegistrationAnalyticsUtility.updateGcmIdSentEventReported(true);

    retryHelper.onSuccess(NhRegistrationDestination.WAKEUP.toString());
  }
}
