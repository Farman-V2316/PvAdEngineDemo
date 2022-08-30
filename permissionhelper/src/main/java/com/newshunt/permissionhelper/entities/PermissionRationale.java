/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper.entities;

/**
 * Rationale POJO - Contains title and description for rationale
 *
 * @author: satyanarayana.avv on 03-08-2016.
 */

public class PermissionRationale {

  private String rationaleTitle;
  private String rationaleDescription;
  private int iconId;

  public PermissionRationale(String rationaleTitle, String permissionDescription, int iconId) {
    this.rationaleTitle = rationaleTitle;
    this.rationaleDescription = permissionDescription;
    this.iconId = iconId;
  }

  public String getRationaleDescription() {
    return rationaleDescription;
  }

  public void setRationaleDescription(String rationaleDescription) {
    this.rationaleDescription = rationaleDescription;
  }

  public String getRationaleTitle() {
    return rationaleTitle;
  }

  public int getRationaleIconId() {
    return iconId;
  }

  public void setRationaleTitle(String rationaleTitle) {
    this.rationaleTitle = rationaleTitle;
  }
}
