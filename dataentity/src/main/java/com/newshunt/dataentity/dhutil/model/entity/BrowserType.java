/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Specifies different browser types.
 *
 * @author neeraj.kumar
 */
public enum  BrowserType implements Serializable {
  @SerializedName("true")
  NH_BROWSER("true"),
  @SerializedName("false")
  EXTERNAL_BROWSER("false"),
  @SerializedName("customTab")
  CUSTOM_TAB("customTab");

  private String name;

  BrowserType(String name) {
    this.name = name;
  }

  public static BrowserType fromName(String name) {
    for (BrowserType browserType : BrowserType.values()) {
      if (browserType.name.equalsIgnoreCase(name)) {
        return browserType;
      }
    }
    return EXTERNAL_BROWSER;
  }

  public String getName() {
    return name;
  }
}
