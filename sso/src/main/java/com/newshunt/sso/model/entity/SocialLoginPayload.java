/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;


import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.PasswordEncryption;

/**
 * Payload for communicating social login to SSO back end
 *
 * @author arun.babu
 */
public class SocialLoginPayload extends BasePayload {
  private final String userAccountType;
  private final String token;
  private final String guestUser;
  private final String guestPassword;
  private final String explicit;
  private String subTypes;

  public SocialLoginPayload(String userAccountType, String token, String guestUser, String
      guestPassword, String explicit, String subTypes) {
    this(userAccountType, token, guestUser, guestPassword, explicit);
    this.subTypes = subTypes;
  }

  public SocialLoginPayload(String userAccountType, String token, String guestUser, String
      guestPassword, String explicit) {
    this.userAccountType = userAccountType;
    this.token = token;
    this.guestUser = guestUser;
    this.explicit = explicit;

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

  public String getToken() {
    return token;
  }

  public String getUserAccountType() {
    return userAccountType;
  }

  public String getSubTypes() {
    return subTypes;
  }

  public void setSubTypes(String subTypes) {
    this.subTypes = subTypes;
  }

}
