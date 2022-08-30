/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.common.helper.common.Constants;

/**
 * To create {@link NhAnalyticsEvent} from any stored String. PV event flag for this is always false
 *
 * @author: bedprakash on 12/12/17.
 */

public class DynamicEvent implements NhAnalyticsEvent {
  public final String eventName;

  public DynamicEvent(String eventName) {
    this.eventName = eventName;
  }

  @Override
  public boolean isPageViewEvent() {
    return false;
  }

  @Override
  public String toString() {
    return eventName == null ? Constants.NULL : eventName;
  }
}
