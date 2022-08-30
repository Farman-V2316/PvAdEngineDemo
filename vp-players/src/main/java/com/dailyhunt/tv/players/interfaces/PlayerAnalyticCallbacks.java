package com.dailyhunt.tv.players.interfaces;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vinod on 13/11/17.
 */

public interface PlayerAnalyticCallbacks {

  default void updateAdditionalEventParams(Map<NhAnalyticsEventParam, Object> map) {}

  Map<String, String> getExperiment();

  String getLanguageKey();

  default Map<String, Object> updateAdditionCardParams() {
    return new HashMap();
  }
}
