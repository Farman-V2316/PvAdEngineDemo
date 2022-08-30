/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;


import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.PasswordEncryption;

/**
 * Payload for user Login API
 *
 * @author arun.babu
 */

public class UserLoginPayload extends BasePayload {
  private final String email;
  private final String password;
  private final String guestUser;
  private final String guestPassword;
  private final String explicit;

  public UserLoginPayload(String email, String password, String guestUser, String guestPassword,
                          String explicit) {
    this.email = email;
    this.guestUser = guestUser;
    this.explicit = explicit;

    try {
      if (!CommonUtils.isEmpty(password)) {
        password = PasswordEncryption.encrypt(password);
      } else {
        password = Constants.EMPTY_STRING;
      }
    } catch (Exception e) {
      password = Constants.EMPTY_STRING;
    }
    this.password = password;

    try {
      if (!CommonUtils.isEmpty(guestPassword)) {
        guestPassword = PasswordEncryption.encrypt(guestPassword);
      } else {
        guestPassword = Constants.EMPTY_STRING;
      }
    } catch (Exception e) {
      guestPassword = Constants.EMPTY_STRING;
    }
    this.guestPassword = guestPassword;
  }
}
