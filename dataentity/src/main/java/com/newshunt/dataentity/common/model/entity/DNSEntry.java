/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Holds hostname and correponding IP addresses using by
 * <code>{@link com.newshunt.common.model.retrofit.UnifiedDns}</code>.
 *
 * @author karthik.r
 */
public class DNSEntry {

  private String hostname;

  private List<String> ip;

  @SerializedName("ra_enabled")
  private boolean raEnabled = true;

  @Nullable
  private String heartbeatUrl;

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String domain) {
    this.hostname = domain;
  }

  public List<String> getIp() {
    return ip;
  }

  public void setIp(List<String> ip) {
    this.ip = ip;
  }

  @Nullable
  public String getHeartbeatUrl() {
    return heartbeatUrl;
  }

  public boolean isRaEnabled() {
    return raEnabled;
  }

  public void setRaEnabled(boolean raEnabled) {
    this.raEnabled = raEnabled;
  }
}
