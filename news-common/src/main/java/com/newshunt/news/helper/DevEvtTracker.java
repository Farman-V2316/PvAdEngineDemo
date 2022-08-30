/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.dataentity.analytics.entity.DevEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.dhutil.analytics.VerApiDevEvent;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.newshunt.dataentity.analytics.entity.DevEvent.EventType.API_REQUEST;
import static com.newshunt.dataentity.analytics.entity.DevEvent.EventType.API_RESPONSE;
import static com.newshunt.dataentity.analytics.entity.DevEvent.EventType.VIEW_SHOW_DATA;
import static com.newshunt.dataentity.analytics.entity.DevEvent.EventType.VIEW_START;

/**
 * Subscribes to {@link DevEvent} and performs analytics logging
 * for dev verification
 *
 * @author satosh.dhanyamraju
 */
public class DevEvtTracker {
  private final int newsFPTypesLen;
  private final HashMap<String, DevEvent[]> map;

  private static final String LOG_TAG = "DevEventTracker";

  public DevEvtTracker(Bus restBus) {
    map = new HashMap<>();
    newsFPTypesLen = DevEvent.EventType.values().length;
    restBus.register(this);
  }

  @Subscribe
  public void onDevEvent(DevEvent event) {
    try {
      Logger.d(LOG_TAG, "onDevEvent: -" + event.id() + " " + printableMap());
      if (!PreferenceManager.getPreference(AppStatePreference.ENABLE_PERFORMANCE_ANALYTICS,
          false)) {
        return;
      }

      if (event.eventType == null) {
        //null eventtype means, view is destroying. So clear the entry from hashmap
        map.remove(event.id());
        Logger.d(LOG_TAG,
            "onDevEvent: destroy event. deleted=" + event.id() + " , new map=" + printableMap());
        return;
      }

      String id = event.id();
      int y = event.eventType.ordinal();
      // put in the map, if not already present
      if (map.get(id) == null) {
        map.put(id, new DevEvent[newsFPTypesLen]);
      }
      // fill the slot for this event
      map.get(id)[y] = event;
      // after data shown event received, calculate time diffs and log event.
      if (event.eventType == VIEW_SHOW_DATA) {
        try {
          logAnalytics(event, map.get(id));
        } catch (NullPointerException e) {
          // can happen if data is returned from cache
          Logger.caughtException(e);
        } finally {
          map.remove(id);
        }
        Logger.d(LOG_TAG, "onDevEvent: after logging " + printableMap());
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  @Subscribe
  public void onFeedInboxDevEvent(FeedInboxDevEvent event) {
    Logger.d(LOG_TAG, "onFeedInboxDevEvent: " + event.toDebugString());
    if (!AndroidUtils.devEventsEnabled()) {
      return;
    }
    AnalyticsClient.log(event, NhAnalyticsEventSection.NEWS, event.getParamsMap());
  }

  @Subscribe
  public void onVerApiDevEvent(VerApiDevEvent event) {
    Logger.d(LOG_TAG, "onVerApiDevEvent: " + event.printString());
    if (!AndroidUtils.devEventsEnabled()) {
      return;
    }
    AnalyticsClient.log(event, NhAnalyticsEventSection.NEWS, event.getParams());
  }

  private void logAnalytics(DevEvent curEvent, DevEvent[] allEvents) {
    try { // causing exception in scheduler.
      long apiTimeMs =
          allEvents[API_RESPONSE.ordinal()].getTimeMsec() -
              allEvents[API_REQUEST.ordinal()].getTimeMsec();
      long totalTimeMs =
          allEvents[VIEW_SHOW_DATA.ordinal()].getTimeMsec() -
              allEvents[VIEW_START.ordinal()].getTimeMsec();
      long exclViewTimeMs = totalTimeMs - apiTimeMs;
      Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
      paramsMap.put(DevEvent.EventParam.API_TIME, apiTimeMs);
      paramsMap.put(DevEvent.EventParam.VIEW_TIME, exclViewTimeMs);
      paramsMap.put(DevEvent.EventParam.TOTAL_TIME, totalTimeMs);
      AnalyticsClient.log(curEvent, NhAnalyticsEventSection.NEWS, paramsMap);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String printableMap() {
    StringBuilder builder = new StringBuilder("[");
    Map<String, DevEvent[]> paramMap = Collections.unmodifiableMap(map);
    for (DevEvent[] a : paramMap.values()) {
      builder.append(Arrays.toString(a));
    }
    builder.append("]");
    return builder.toString();
  }
}
