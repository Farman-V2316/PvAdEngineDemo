/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.analytics;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;

import java.io.Serializable;

/**
 * @author santhosh.kc
 */
public class RunTimeReferrer implements NhAnalyticsReferrer, Serializable {
  private static final long serialVersionUID = 7209171137955225917L;

  private final String referrerName;

  private final NHReferrerSource referrerSource;

  public RunTimeReferrer(String referrerName, NHReferrerSource referrerSource) {
    this.referrerName = referrerName;
    this.referrerSource = referrerSource;
  }

  @Override
  public String getReferrerName() {
    return referrerName;
  }

  @Override
  public NHReferrerSource getReferrerSource() {
    return referrerSource;
  }
}
