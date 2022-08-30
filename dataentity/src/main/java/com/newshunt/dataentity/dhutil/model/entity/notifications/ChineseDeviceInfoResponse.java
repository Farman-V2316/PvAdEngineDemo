/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.notifications;

import java.io.Serializable;
import java.util.List;

/**
 * @author shashikiran.nr on 3/6/2017.
 */

public class ChineseDeviceInfoResponse implements Serializable {

  private String version;

  private List<ChineseDeviceInfo> deviceInfo;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<ChineseDeviceInfo> getDeviceInfo() {
    return deviceInfo;
  }

  public void setDeviceInfo(List<ChineseDeviceInfo> deviceInfo) {
    this.deviceInfo = deviceInfo;
  }
}
