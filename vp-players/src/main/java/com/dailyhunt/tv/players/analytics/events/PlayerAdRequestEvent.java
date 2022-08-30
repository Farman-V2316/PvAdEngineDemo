/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics.events;

import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams;
import com.dailyhunt.tv.players.analytics.enums.AnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jayanth on 09/05/18.
 */

public class PlayerAdRequestEvent extends Event {
  private Map<NhAnalyticsEventParam, Object> eventParams;

  public PlayerAdRequestEvent(final String sectionType, final String groupKey) {
    transformEvent(sectionType, groupKey);
  }

  @Override
  protected void transformEvent(Object... object) {

    String sectionType = (String) object[0];
    String groupKey = (String) object[1];

    eventParams = new HashMap<>();
    eventParams.put(PlayerAnalyticsEventParams.TYPE, sectionType);
    eventParams.put(PlayerAnalyticsEventParams.GROUP_KEY, groupKey);

    publishEvent(this);
  }

  @Override
  protected NhAnalyticsEventSection getEventSectionType() {
    return NhAnalyticsEventSection.ADS;
  }

  @Override
  protected Map<NhAnalyticsEventParam, Object> getEventParam() {
    return eventParams;
  }

  @Override
  protected NhAnalyticsEvent getEvent() {
    return AnalyticsEvent.AD_REQUEST;
  }
}
