/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.launch;

import com.newshunt.dataentity.common.model.entity.UserAppSection;

/**
 * @author santhosh.kc
 */
public class AppSectionLaunchResult {

  private UserAppSection userAppSection;

  private boolean launched;

  public AppSectionLaunchResult(UserAppSection userAppSection, boolean launched) {
    this.userAppSection = userAppSection;
    this.launched = launched;
  }

  public UserAppSection getAppSection() {
    return userAppSection;
  }

  public void setAppSection(UserAppSection userAppSection) {
    this.userAppSection = userAppSection;
  }

  public boolean isLaunched() {
    return launched;
  }

  public void setLaunched(boolean launched) {
    this.launched = launched;
  }
}
