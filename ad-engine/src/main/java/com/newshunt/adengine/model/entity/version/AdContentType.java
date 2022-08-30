/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity.version;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Indicates content type of the ad. Parser will use this to determine
 * what parameters to look for in the XML.
 *
 * @author shreyas.desai
 */
public enum AdContentType implements Type, Serializable {
  @SerializedName("imgLink")
  IMAGE_LINK("imgLink"),
  @SerializedName("native-banner")
  NATIVE_BANNER("native-banner"),
  @SerializedName("appDownload")
  APP_DOWNLOAD("appDownload"),
  @SerializedName("mraid-zip")
  MRAID_ZIP("mraid-zip"),
  @SerializedName("mraid-external")
  MRAID_EXTERNAL("mraid-external"),
  @SerializedName("empty")
  EMPTY_AD("empty"),
  @SerializedName("external-sdk")
  EXTERNAL_SDK("external-sdk"),
  @SerializedName("pgi-zip")
  PGI_ZIP("pgi-zip"),
  @SerializedName("pgi-external")
  PGI_EXTERNAL("pgi-external"),
  @SerializedName("pgi-native-article")
  PGI_ARTICLE_AD("pgi-native-article"),
  @SerializedName("contentAd")
  CONTENT_AD("contentAd");

  private String name;

  AdContentType(String name) {
    this.name = name;
  }

  public static AdContentType fromName(String name) {
    for (AdContentType adContentType : AdContentType.values()) {
      if (adContentType.name.equalsIgnoreCase(name)) {
        return adContentType;
      }
    }

    return null;
  }

  public String getName() {
    return name;
  }
}
