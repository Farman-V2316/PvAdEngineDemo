/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;


import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.PasswordEncryption;

/**
 * Payload for User SignUp API
 *
 * @author arun.babu
 */
public class UserSignUpPayload extends BasePayload {
  private final String name;
  private final String email;
  private final String password;
  private final String guestUser;
  private final String guestPassword;
  private final String phone;

  public UserSignUpPayload(String name, String email, String password, String guestUser,
                           String guestPassword, String phone) {
    this.name = name;
    this.email = email;
    this.guestUser = guestUser;
    this.phone = phone;

    try {
      password = PasswordEncryption.encrypt(password);
    } catch (Exception e) {
      password = Constants.EMPTY_STRING;
    }
    this.password = password;

    try {
      guestPassword = PasswordEncryption.encrypt(guestPassword);
    } catch (Exception e) {
      guestPassword = Constants.EMPTY_STRING;
    }
    this.guestPassword = guestPassword;
  }
}
