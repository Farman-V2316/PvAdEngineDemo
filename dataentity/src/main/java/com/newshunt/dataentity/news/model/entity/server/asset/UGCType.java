/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity.server.asset;

/**
 * Enum for user generated content
 *
 * @author piyush.rai
 */
public enum UGCType {

  LIKE(true), COMMENT(true), FAVORITE(true), LAST_ACCESS(false);

  private boolean isCountField;

  UGCType(boolean isCountField) {
    this.isCountField = isCountField;
  }

  public static UGCType fromName(String type) {
    if (type == null) {
      return null;
    }

    for (UGCType ugcType : values()) {
      if (ugcType.name().equalsIgnoreCase(type)) {
        return ugcType;
      }
    }

    return null;
  }

  public boolean isCountField() {
    return isCountField;
  }

  public String getName() {
    return name().toLowerCase();
  }

}
