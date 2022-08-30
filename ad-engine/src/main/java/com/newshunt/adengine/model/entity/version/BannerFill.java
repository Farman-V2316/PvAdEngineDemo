/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity.version;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author neeraj  04/10/17.
 */
public enum BannerFill implements Serializable {
  @SerializedName("center")
  CENTER("center"),
  @SerializedName("fill-width")
  FILL_WIDTH("fill-width");
  private final String name;

  BannerFill(String name) {
    this.name = name;
  }

  public static BannerFill fromName(String name) {
    for (BannerFill bannerFill : BannerFill.values()) {
      if (bannerFill.name.equalsIgnoreCase(name)) {
        return bannerFill;
      }
    }
    return FILL_WIDTH;
  }

  public String getName() {
    return name;
  }
}
