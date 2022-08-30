/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.analytics;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;

/**
 * Referrer to be used by SSO module
 *
 * @author ranjith.suda
 */
public enum SSOReferrer implements NhAnalyticsReferrer {

  MENU("MENU"),
  SIGN_IN_PAGE("SIGN_IN_PAGE"),
  SIGN_IN_CLICK("SIGN_IN_CLICK"),
  SIGNIN_NUDGE("SIGNIN_NUDGE");

  private String referrerName;

  SSOReferrer(String referrerName) {
    this.referrerName = referrerName;
  }

  @Override
  public String getReferrerName() {
    return referrerName;
  }

  @Override
  public NHReferrerSource getReferrerSource() {
    return null;
  }
}
