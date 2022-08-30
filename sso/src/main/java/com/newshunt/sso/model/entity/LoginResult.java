/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

import com.newshunt.sso.SSO;

import java.util.ArrayList;

/**
 * Single Sign On Login Response
 *
 * @author arun.babu
 */
public class LoginResult {
  /**
   * SSO Result after Sign In Call Backs.
   */
  private final SSOResult result;
  /**
   * User Details for the Existing User
   */
  private final SSO.UserDetails userDetails;
  /**
   * Flag indicating , whether existing userId has changed
   */
  private final boolean userChanged;

  /**
   * Login Source Type , Who has Initiated the Login
   */
  private final ArrayList<SSOLoginSourceType> loginSourceTypes;

  public LoginResult(SSOResult result, SSO.UserDetails userDetails, boolean userChanged,
                     ArrayList<SSOLoginSourceType> loginSourceTypes) {
    this.result = result;
    this.userDetails = userDetails;
    this.userChanged = userChanged;
    this.loginSourceTypes = loginSourceTypes;
  }

  public SSOResult getResult() {
    return result;
  }

  public SSO.UserDetails getUserDetails() {
    return userDetails;
  }

  public boolean isUserChanged() {
    return userChanged;
  }

  public ArrayList<SSOLoginSourceType> getLoginSourceType() {
    return loginSourceTypes;
  }
}