/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.status;

/**
 * Tells what device we are dealing with
 */
public enum DeviceType {
  WEB("web"),
  IOS("ios"),
  ANDROID("android"),
  WIN("win");

  private String id;

  DeviceType(String id) {
    this.id = id;
  }

  public static DeviceType fromValue(String deviceType) {
    if (deviceType == null) {
      return WEB;
    }

    for (DeviceType type : values()) {
      if (type.getId().equalsIgnoreCase(deviceType)) {
        return type;
      }
    }

    return WEB;
  }

  public static DeviceType fromString(String deviceType) {
    if (deviceType == null) {
      return WEB;
    }

    for (DeviceType type : values()) {
      if (type.name().equalsIgnoreCase(type.getId())) {
        return type;
      }
    }

    return WEB;
  }

  public String getId() {
    return id;
  }
}
