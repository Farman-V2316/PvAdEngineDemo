/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.launch;

import com.newshunt.dataentity.common.model.entity.BaseDataResponse;

import java.io.Serializable;
import java.util.List;

/**
 * @author santhosh.kc
 */
public class AppLaunchConfigResponse extends BaseDataResponse implements Serializable {

  private static final long serialVersionUID = -8612864986386197026L;
  private String version;

  private List<AppLaunchRule> rules;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<AppLaunchRule> getLaunchRules() {
    return rules;
  }

  public void setLaunchRules(List<AppLaunchRule> launchRules) {
    rules = launchRules;
  }
}
