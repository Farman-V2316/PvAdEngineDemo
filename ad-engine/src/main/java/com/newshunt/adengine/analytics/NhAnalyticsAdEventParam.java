/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Event parameters to sent to analytics for ads.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsAdEventParam implements NhAnalyticsEventParam {
  AD_TYPE("ad_type", true),
  AD_POSITION("ad_position", true),
  AD_UID("ad_uid", true);

  private String name;
  private boolean isFlurry;

  NhAnalyticsAdEventParam(String name, boolean isFlurry) {
    this.name = name;
    this.isFlurry = isFlurry;
  }

  @Override
  public String getName() {
    return name;
  }
}
