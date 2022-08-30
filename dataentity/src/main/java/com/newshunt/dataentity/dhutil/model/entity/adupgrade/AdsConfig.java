/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import java.io.Serializable;

/**
 * @author neeraj.kumar
 */
public class AdsConfig implements Serializable {
  private static final long serialVersionUID = 1146843367031176212L;

  private boolean enabled;
  private boolean enablePrefetch;
  //Unit : Time in second
  private long cacheTTL;

  public boolean isEnablePrefetch() {
    return enablePrefetch;
  }

  public long getCacheTTL() {
    return cacheTTL;
  }

  public boolean isEnabled() {
    return enabled;
  }
}

