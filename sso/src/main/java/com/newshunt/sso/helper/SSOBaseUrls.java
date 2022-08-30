/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.helper;

import androidx.annotation.NonNull;

/**
 * Utility Class that holds the Base Url's required for the SSO module to work
 *
 * @author ranjith.suda
 */
public class SSOBaseUrls {

  String ssoUrl;

  public SSOBaseUrls() {
    // Empty Conf Built , nothing to be done here..
  }

  public SSOBaseUrls setSSOUrl(@NonNull String url) {
    this.ssoUrl = url;
    return this;
  }
}
