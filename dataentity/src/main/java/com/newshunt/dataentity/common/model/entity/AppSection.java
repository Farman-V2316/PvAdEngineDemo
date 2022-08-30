/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

/**
 * Type of application Sections like News , Books
 * <p/>
 *
 * @author: amit.kankani
 */
public enum AppSection {
  NEWS("NEWS", 1, true),
  TV("TV", 2, true),
  NOTIFICATIONINBOX("NOTIFICATIONINBOX", 4, false),
  WEB("WEB", 8, true),
  FOLLOW("FOLLOW", 32, true),
  DEEPLINK("DEEPLINK", 64, false),
  SEARCH("SEARCH", 128, false);

  private String name;
  private boolean isLandingSupported;
  /*
    Adding typeNumber to perform bitwise operations to check if section already exists by masking
     with this typeNumber
   */
  private int typeNumber;

  AppSection(String name, int typeNumber, boolean isLandingSupported) {
    this.name = name;
    this.typeNumber = typeNumber;
    this.isLandingSupported = isLandingSupported;
  }

  public static AppSection fromName(String name) {
    for (AppSection appSection : AppSection.values()) {
      if (appSection.name.equalsIgnoreCase(name)) {
        return appSection;
      }
    }
    // default to NEWS
    return NEWS;
  }

  public String getName() {
    return name;
  }

  public int getTypeNumber() {
    return typeNumber;
  }

  public boolean isLandingSupported() {
    return isLandingSupported;
  }
}
