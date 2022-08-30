/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.status;

import java.io.Serializable;

/**
 * Provides base data about client.
 *
 * @author shreyas
 */
public class ClientBaseInfo implements Serializable {

  private static final long serialVersionUID = 6661867896328182517L;

  protected String clientId;
  protected String userId;
  protected String udid;
  protected String androidId;

  public ClientBaseInfo() {
  }

  public ClientBaseInfo(String udid, String clientId) {
    this.udid = udid;
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  public String getUdid() {
    return udid;
  }

  public void setUdid(final String udid) {
    this.udid = udid;
  }

  public String getAndroidId() {
    return androidId;
  }

  public void setAndroidId(final String androidId) {
    this.androidId = androidId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void copy(final ClientBaseInfo clientBaseInfo) {
    if (clientBaseInfo == null) {
      return;
    }
    clientId = clientBaseInfo.clientId;
    udid = clientBaseInfo.udid;
    androidId = clientBaseInfo.androidId;
    userId = clientBaseInfo.userId;
  }

}
