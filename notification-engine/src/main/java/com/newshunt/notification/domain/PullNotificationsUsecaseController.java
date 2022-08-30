/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.domain;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.CommonBaseUrlsContainer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.model.entity.PullNotificationJobEvent;
import com.newshunt.notification.model.entity.server.PullNotificationResponse;
import com.newshunt.notification.model.internal.rest.server.PullNotificationRequest;
import com.newshunt.notification.model.internal.service.PullNotificationsServiceImpl;
import com.newshunt.notification.model.service.PullNotificationsService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Implementation of {@link GetNotificationUpdateUsecase}.
 *
 * @author anshul.jain
 */
public class PullNotificationsUsecaseController implements PullNotificationUsecase {

  private final PullNotificationsService pullNotificationService;
  private final Bus uiBus;
  private final String salt, syncConfigVersion, state, clientId, gcmId;
  private final String[] pushNotificationIds, userLanguages;
  private final PullNotificationJobEvent pullNotificationJobEvent;

  public PullNotificationsUsecaseController(Bus uiBus, String salt,
                                            String syncConfigVersion, String state,
                                            String[] pushNotificationIds,
                                            String[] userLanguages, String clientId,
                                            String gcmId,
                                            PullNotificationJobEvent pullNotificationJobEvent) {

    this.pullNotificationService = new PullNotificationsServiceImpl();
    this.uiBus = uiBus;
    this.salt = salt;
    this.syncConfigVersion = syncConfigVersion;
    this.state = state;
    this.userLanguages = userLanguages;
    this.clientId = clientId;
    this.gcmId = gcmId;
    this.pushNotificationIds = pushNotificationIds;
    this.pullNotificationJobEvent = pullNotificationJobEvent;
  }


  @Override
  public void pullNotifications() {
    BusProvider.getRestBusInstance().register(this);
    String baseUrl = CommonBaseUrlsContainer.getInstance().getPullNotificationUrl();
    String acquisitionType = PreferenceManager.getPreference(AppStatePreference
        .ACQUISITION_TYPE, Constants.EMPTY_STRING);
    PullNotificationRequest
        request = new PullNotificationRequest(salt, syncConfigVersion, state,
        pushNotificationIds, userLanguages, clientId, gcmId, acquisitionType);
    request.addEventRelatedParams(pullNotificationJobEvent);
    pullNotificationService.pullNotifications(baseUrl, request);
  }

  @Subscribe
  public void onPullNotificationsResponse(PullNotificationResponse pullNotificationResponse) {
    uiBus.post(pullNotificationResponse);
    BusProvider.getRestBusInstance().unregister(this);
  }

  @Override
  public void execute() {
    pullNotifications();
  }
}
