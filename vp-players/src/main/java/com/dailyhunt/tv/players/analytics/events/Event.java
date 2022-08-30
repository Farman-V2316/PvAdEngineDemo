/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics.events;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;

import java.lang.ref.WeakReference;
import java.util.Map;

public abstract class Event {
  protected WeakReference<PageReferrer> pageReferrer =
      new WeakReference<PageReferrer>(new PageReferrer());


  protected boolean acceptEvent() {
    return true;
  }

  // order of objects passed to this method is specific to the event only
  abstract protected void transformEvent(Object... object);
  abstract protected NhAnalyticsEventSection getEventSectionType();
  abstract protected Map<NhAnalyticsEventParam, Object> getEventParam();
  abstract protected NhAnalyticsEvent getEvent();

  protected Map<String, String> getDynamicMap() { return null; }

  protected boolean getEventType() { return false; }

  protected boolean logDynamicEvent() { return  false;}
  @NonNull
  protected PageReferrer pageReferrer() {
    return pageReferrer.get();
  }

  protected void setPageReferrer(final PageReferrer pageReferrer) {
    this.pageReferrer.clear();
    this.pageReferrer = new WeakReference<PageReferrer>(pageReferrer);
  }

  protected void publishEvent(final Event event) {
    if (event.acceptEvent()) {
      AnalyticsService.instance().trackEvent(event);
    }
  }

  protected void filler(@Nullable final Object object,
                        final Map<NhAnalyticsEventParam, Object> eventParams) {
  }


}
