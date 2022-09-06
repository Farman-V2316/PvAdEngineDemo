/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.util.permission;


import android.Manifest;

/**
 * Permission Enum
 *
 * @author: by satyanarayana.avv on 03-08-2016.
 */

public enum Permission {

  ACCESS_FINE_LOCATION(Manifest.permission.ACCESS_FINE_LOCATION, PermissionGroup.LOCATION),
  WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionGroup.STORAGE),
  READ_EXTERNAL_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE, PermissionGroup.STORAGE),
  ACCESS_CAMERA(Manifest.permission.CAMERA, PermissionGroup.CAMERA),
  READ_CONTACTS(Manifest.permission.READ_CONTACTS, PermissionGroup.CONTACTS),
  RECORD_AUDIO(Manifest.permission.RECORD_AUDIO, PermissionGroup.MICROPHONE),
  READ_CALENDAR(Manifest.permission.READ_CALENDAR, PermissionGroup.CALENDAR),
  WRITE_CALENDAR(Manifest.permission.WRITE_CALENDAR, PermissionGroup.CALENDAR),
  GET_ACCOUNTS(Manifest.permission.GET_ACCOUNTS, PermissionGroup.CONTACTS),
  ACCESS_COARSE_LOCATION(Manifest.permission.ACCESS_COARSE_LOCATION, PermissionGroup.LOCATION),
  INVALID("INVALID", null);

  Permission(String permission, PermissionGroup permissionGroup) {
    this.permission = permission;
    this.permissionGroup = permissionGroup;
  }

  private String permission;
  private PermissionGroup permissionGroup;

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public PermissionGroup getPermissionGroup() {
    return permissionGroup;
  }

  public static Permission fromName(String permissionString) {
    for (Permission permission : Permission.values()) {
      if (permission.permission.equalsIgnoreCase(permissionString)) {
        return permission;
      }
    }
    return INVALID;
  }
}

