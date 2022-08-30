/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.socialfeatures.helper.analytics;

import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;

import java.io.Serializable;

/**
 * @author santhosh.kc
 */
public class SocialTabAnalyticsInfo implements Serializable {
  private static final long serialVersionUID = -6703742863973384649L;

  private NhAnalyticsEventSection section;

  private String tabItemId;

  private String referrer;

  private String referrerId;

  public String getReferrer() {
    return referrer;
  }

  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }

  public String getReferrerId() {
    return referrerId;
  }

  public void setReferrerId(String referrerId) {
    this.referrerId = referrerId;
  }

  public String getTabItemId() {
    return tabItemId;
  }

  public void setTabItemId(String tabItemId) {
    this.tabItemId = tabItemId;
  }

  public NhAnalyticsEventSection getSection() {
    return section;
  }

  public void setSection(NhAnalyticsEventSection section) {
    this.section = section;
  }
}
