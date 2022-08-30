/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.status;

import java.io.Serializable;

/**
 * Provides information how client is using the application.
 *
 * @author shreyas
 */
public class ConnectionInfo implements Serializable {
  private static final long serialVersionUID = -3796123841597809066L;

  private String connection;
  private String cellid;
  private String apnName;
  private String ipAddress;
  private String macAddress;

  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
  }

  public String getCellid() {
    return cellid;
  }

  public void setCellid(String cellid) {
    this.cellid = cellid;
  }

  public String getApnName() {
    return apnName;
  }

  public void setApnName(String apnName) {
    this.apnName = apnName;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }
}
