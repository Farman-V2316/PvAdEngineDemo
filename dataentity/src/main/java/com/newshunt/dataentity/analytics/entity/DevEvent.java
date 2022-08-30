/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.analytics.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Encapsulates information for tracking time spent on differents layers  when making a network call
 *
 * @author satosh.dhanyamraju
 */
public class DevEvent implements NhAnalyticsEvent {

  public final int uniqueId;
  private API api;
  public final long time;
  public final EventType eventType;
  private Map<NhAnalyticsEventParam, Object> paramMap;

  public enum EventType {
    VIEW_START, VIEW_SHOW_DATA, API_REQUEST, API_RESPONSE
  }

  public enum API {
    DEV_NEWS_FIRST_PAGE, DEV_NEWS_2ND_CHUNK, DEV_NEWS_STORY, DEV_NEWS_SHORT_URL
  }


  public enum  EventParam implements NhAnalyticsEventParam {

    VIEW_TIME, API_TIME, TOTAL_TIME, HOSTNAME, TIMEOUT_DURATION;

    @Override
    public String getName() {
      return name();
    }
  }

  public DevEvent(EventType eventType, API api, int uniqueId) {
    this(eventType, api, uniqueId, System.nanoTime());
  }

  public DevEvent(EventType eventType, API api, int uniqueId, long time) {
    this.uniqueId = uniqueId;
    this.api = api;
    this.time = time;
    this.eventType = eventType;
  }

  public DevEvent(EventType eventType, Map<NhAnalyticsEventParam, Object> paramMap) {
    this.eventType = eventType;
    this.paramMap = paramMap;
    this.uniqueId = 0;
    this.time = 0;
  }

  @Override
  public boolean isPageViewEvent() {
    return false;
  }

  public long getTimeMsec() {
    return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
  }

  public String id() {
    return uniqueId + (api == null ? null : api.name());
  }

  public Map<NhAnalyticsEventParam, Object> getParamMap() {
    return paramMap;
  }

  @Override
  public String toString() {
    if (api != null) {
      return api.name() + "_SHOWN";
    }

    return eventType.toString();
  }
}
