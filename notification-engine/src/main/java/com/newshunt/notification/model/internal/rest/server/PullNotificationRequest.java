/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.rest.server;

import com.newshunt.notification.model.entity.PullNotificationJobEvent;

import java.util.Map;

/**
 * @author anshul.jain on 11/4/2016.
 * <p>
 * Class used for creating request for {@link com.newshunt.notification.model.internal.rest.PullNotificationServiceAPI}
 */

public class PullNotificationRequest {


  private final String salt;
  private final String syncConfigVersion;
  private final String state;
  private final String[] pushNotificationIds;
  private final String[] userLanguages;
  private final String clientId;
  private final String gcmId;
  private String deviceTime;
  private String lastRebootTimestamp;
  private String lastSuccessfulPullTimestamp;
  private String lastSuccesfulPushTimestamp;
  private String acquisitionType;

  public PullNotificationRequest(String salt, String syncConfigVersion, String state,
                                 String[] pushNotificationIds, String[] userLanguages,
                                 String clientId, String gcmId, String acquisitionType) {
    this.salt = salt;
    this.syncConfigVersion = syncConfigVersion;
    this.state = state;
    this.pushNotificationIds = pushNotificationIds;
    this.userLanguages = userLanguages;
    this.clientId = clientId;
    this.gcmId = gcmId;
    this.acquisitionType = acquisitionType;
  }

  public void addEventRelatedParams(PullNotificationJobEvent pullNotificationJobEvent) {
    if (pullNotificationJobEvent == null) {
      return;
    }
    this.deviceTime = pullNotificationJobEvent.getDeviceTime();
    this.lastRebootTimestamp = pullNotificationJobEvent.getLastRebootTime();
    this.lastSuccessfulPullTimestamp = pullNotificationJobEvent
        .getLastSuccessfulPullSyncTime();
    this.lastSuccessfulPullTimestamp = pullNotificationJobEvent.getLastPushNotificationTime();
  }
}
