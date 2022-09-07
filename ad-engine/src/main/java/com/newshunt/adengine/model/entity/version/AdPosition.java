/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity.version;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Specifies different ad types.
 *
 * @author shreyas.desai
 */
public enum AdPosition implements Serializable {
  @SerializedName("storypage")
  STORY("storypage"),
  @SerializedName("supplement")
  SUPPLEMENT("supplement"),
  @SerializedName("list-ad")
  LIST_AD("list-ad"),
  @SerializedName("pgi")
  PGI("pgi"),
  @SerializedName("vdo-pgi")
  VDO_PGI("vdo-pgi"),
  @SerializedName("splash")
  SPLASH("splash"),
  @SerializedName("splash-default")
  SPLASH_DEFAULT("splash-default"),
  @SerializedName("card-p0")
  P0("card-p0"),
  @SerializedName("inline-vdo")
  INLINE_VIDEO("inline-vdo"),
  @SerializedName("instream-vdo")
  INSTREAM_VIDEO("instream-vdo"),
  @SerializedName("masthead")
  MASTHEAD("masthead"),
  @SerializedName("dhtv-masthead")
  DHTV_MASTHEAD("dhtv-masthead"),
  @SerializedName("card-pp1")
  PP1("card-pp1"),
  EVERGREEN("evergreen"),
  @SerializedName("splash-exit")
  EXIT_SPLASH("splash-exit");

  private String value;

  AdPosition(String value) {
    this.value = value;
  }

  public static AdPosition fromName(String value) {
    for (AdPosition adPosition : AdPosition.values()) {
      if (adPosition.value.equalsIgnoreCase(value)) {
        return adPosition;
      }
    }

    return null;
  }

  public String getValue() {
    return value;
  }
}
