/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.entity.server;

import com.google.gson.JsonArray;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.model.entity.BaseError;

/**
 * Response of pull notifications API
 *
 * @author anshul.jain on 10/24/2016.
 */

public class PullNotificationResponse {

  private String salt;

  private JsonArray notifications;

  private String state;

  private long backOffDuration; // in seconds;

  private PullSyncConfig syncConfig;

  private BaseError baseError;

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  public JsonArray getNotifications() {
    return notifications;
  }

  public void setNotifications(JsonArray notifications) {
    this.notifications = notifications;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public PullSyncConfig getSyncConfig() {
    return syncConfig;
  }

  public void setSyncConfig(PullSyncConfig syncConfig) {
    this.syncConfig = syncConfig;
  }

  public int getBackOffDuration() {
    return (int) backOffDuration;
  }

  public void setBackOffDuration(long backOffDuration) {
    this.backOffDuration = backOffDuration;
  }

  public BaseError getBaseError() {
    return baseError;
  }

  public void setBaseError(BaseError baseError) {
    this.baseError = baseError;
  }

  @Override
  public String toString() {
    String configurationStr = Constants.EMPTY_STRING;
    if (syncConfig != null && !CommonUtils.isEmpty(syncConfig.getSyncConfigVersion())) {
      configurationStr = syncConfig.toString();
    }
    return "PullNotificationResponse{" +
        "salt='" + salt + '\'' +
        ", notifications='" + notifications + '\'' +
        ", state='" + state + '\'' +
        ", backOffDuration=" + backOffDuration +
        ", syncConfig=" + configurationStr +
        '}';
  }
}
