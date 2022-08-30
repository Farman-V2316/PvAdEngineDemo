/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.client;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Filters parameters for different analytics client.
 *
 * @author jigar.shah
 */
public class AttributeFilter {

  public static Map<String, Object> filterForNH(
      Map<NhAnalyticsEventParam, ? extends Object> params) {
    if (params == null) {
      return new HashMap<>();
    }
    Map<String, Object> newParams = new HashMap<>();
    for (Map.Entry<NhAnalyticsEventParam, ? extends Object> mapEntry : params.entrySet()) {
      if (mapEntry.getValue() != null) {
        newParams.put(mapEntry.getKey().getName(), mapEntry.getValue());
      }
    }
    return newParams;
  }
}
