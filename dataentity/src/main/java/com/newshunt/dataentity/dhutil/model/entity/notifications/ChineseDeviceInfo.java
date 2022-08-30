/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.notifications;

import java.io.Serializable;

/**
 * @author shashikiran.nr on 3/2/2017.
 */

public class ChineseDeviceInfo implements Serializable {

  private static final long serialVersionUID = 0L;

  private String manufacturer;

  private String security_app_packagename;

  private String security_app_activityname;

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getSecurity_app_packagename() {
    return security_app_packagename;
  }

  public void setSecurity_app_packagename(String security_app_packagename) {
    this.security_app_packagename = security_app_packagename;
  }

  public String getSecurity_app_activityname() {
    return security_app_activityname;
  }

  public void setSecurity_app_activityname(String security_app_activityname) {
    this.security_app_activityname = security_app_activityname;
  }
}
