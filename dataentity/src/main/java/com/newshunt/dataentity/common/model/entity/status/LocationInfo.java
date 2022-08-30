/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.status;

import java.io.Serializable;

/**
 * Provides location info to application.
 *
 * @author shreyas
 */
public class LocationInfo implements Serializable {
  private static final long serialVersionUID = -2620209688148696862L;

  private String lat;
  private String lon;
  private boolean isGPSLocation;

  public String getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = lat;
  }

  public String getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = lon;
  }

  public boolean getIsGPSLocation() {
    return isGPSLocation;
  }

  public void setIsGPSLocation(boolean isGPSLocation) {
    this.isGPSLocation = isGPSLocation;
  }
}
