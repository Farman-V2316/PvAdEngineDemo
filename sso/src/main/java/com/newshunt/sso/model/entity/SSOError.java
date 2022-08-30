/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

/**
 * Possible error codes from SSO Rest API
 *
 * @author arun.babu
 */
public enum SSOError {
  CODE_UNKNOWN("unknown"),
  CODE_AUTH_FAILED("AUTE01"),
  CODE_RESET_PASSWORD_FAILED("AUTE02"),
  CODE_LOGIN_RETRIES_EXHAUSTED("AUTE05"),
  CODE_EMAIL_ALREADY_EXISTS("REG01"),
  CODE_INVALID_PASSWORD("REG09"),
  CODE_INVALID_EMAIL_ADDRESS("REG10"),
  CODE_EMPTY_NAME("REG11"),
  CODE_INVALID_NAME("REG12");

  private final String value;

  SSOError(final String value) {
    this.value = value;
  }

  public static SSOError fromValue(String value) {
    for (SSOError type : SSOError.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    return CODE_UNKNOWN;
  }
}
