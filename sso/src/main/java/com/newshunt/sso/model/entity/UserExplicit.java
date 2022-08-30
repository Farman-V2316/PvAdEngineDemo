/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

/**
 * Enum for different types of user explicit options
 *
 * @author nayana.hs
 */
public enum UserExplicit {
  YES("Y"),
  NO("N");

  private final String value;

  private UserExplicit(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static UserExplicit fromValue(String value) {
    for (UserExplicit type : UserExplicit.values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return NO;
  }
}
