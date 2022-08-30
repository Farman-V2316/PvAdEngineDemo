/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.theme;


import com.newshunt.dhutil.R;

/**
 * Enum to define theme type (i.e. Day & Night theme)
 *
 * @author VishalB
 */
public enum ThemeType {
  DAY(R.style.AppThemeDay, "day"),
  NIGHT(R.style.AppThemeNight, "night");

  private int themeId;
  private String name;

  ThemeType(int themeId, String name) {
    this.themeId = themeId;
    this.name = name;
  }

  public int getThemeId() {
    return themeId;
  }

  public String getName() {
    return name;
  }

  public static ThemeType fromName(String name) {
    for (ThemeType themeType : ThemeType.values()) {
      if (themeType.name.equalsIgnoreCase(name)) {
        return themeType;
      }
    }
    return null;
  }


}
