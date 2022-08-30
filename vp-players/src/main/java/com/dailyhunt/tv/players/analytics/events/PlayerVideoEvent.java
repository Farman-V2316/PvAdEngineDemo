/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics.events;

import com.dailyhunt.tv.players.analytics.enums.AnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.helper.SearchAnalyticsHelper;

import java.util.Map;


public class PlayerVideoEvent extends Event {

  //describe the event class
  public static final String TAG_NAME = PlayerVideoEvent.class.getName();
  private static final NhAnalyticsEvent event = AnalyticsEvent.VIDEO_PLAYED;
  private Map<NhAnalyticsEventParam, Object> eventParams;
  private Map<String, String> dynamicParams;
  private NhAnalyticsEventSection section = NhAnalyticsEventSection.TV;

  public PlayerVideoEvent(final Map<NhAnalyticsEventParam, Object> map,
                          final Map<String, String> dynamicParams,
                          final PageReferrer referrer) {

    setPageReferrer(referrer);
    this.dynamicParams = dynamicParams;
    this.eventParams = map;
    transformEvent((Object) null);
  }

  public PlayerVideoEvent(final Map<NhAnalyticsEventParam, Object> map,
                          final Map<String, String> dynamicParams,
                          final PageReferrer referrer, final  NhAnalyticsEventSection section) {
    setPageReferrer(referrer);
    this.dynamicParams = dynamicParams;
    this.eventParams = map;
    SearchAnalyticsHelper.addSearchParams(section, eventParams);
    this.section = section;
    transformEvent((Object) null);

  }

  @Override
  protected void transformEvent(final Object... object) {
    publishEvent(this);
  }

  @Override
  protected NhAnalyticsEventSection getEventSectionType() {
    return section;
  }

  @Override
  protected Map<NhAnalyticsEventParam, Object> getEventParam() {
    return eventParams;
  }

  @Override
  protected NhAnalyticsEvent getEvent() {
    return event;
  }

  @Override
  protected Map<String, String> getDynamicMap() {
    return dynamicParams;
  }

  @Override
  protected boolean logDynamicEvent() {
    return true;
  }
}
