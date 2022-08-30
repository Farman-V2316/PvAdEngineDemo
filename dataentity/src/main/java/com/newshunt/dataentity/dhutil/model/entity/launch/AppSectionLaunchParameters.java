/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.launch;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.model.entity.UserAppSection;

/**
 * @author santhosh.kc
 */
public class AppSectionLaunchParameters {
  private final UserAppSection userAppSection;
  private final boolean skipAnimation;
  private final PageReferrer pageReferrer;

  private AppSectionLaunchParameters(UserAppSection userAppSection, boolean skipAnimation,
                                     PageReferrer pageReferrer) {
    this.userAppSection = userAppSection;
    this.skipAnimation = skipAnimation;
    this.pageReferrer = pageReferrer;
  }

  public boolean isSkipAnimation() {
    return skipAnimation;
  }

  public UserAppSection getUserAppSection() {
    return userAppSection;
  }

  public PageReferrer getPageReferrer() {
    return pageReferrer;
  }

  public static class Builder {
    private UserAppSection userAppSection;
    private boolean skipAnimation;
    private PageReferrer pageReferrer;

    public Builder setUserAppSection(UserAppSection userAppSection) {
      this.userAppSection = userAppSection;
      return this;
    }

    public Builder setSkipAnimation(boolean skipAnimation) {
      this.skipAnimation = skipAnimation;
      return this;
    }

    public Builder setPageReferrer(PageReferrer pageReferrer) {
      this.pageReferrer = pageReferrer;
      return this;
    }

    public AppSectionLaunchParameters build() {
      return new AppSectionLaunchParameters(userAppSection, skipAnimation, pageReferrer);
    }
  }
}
