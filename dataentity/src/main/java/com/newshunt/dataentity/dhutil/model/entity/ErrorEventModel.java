/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.dhutil.analytics.NhAnalyticsCommonEventParam;

import java.util.Map;

/**
 * @author: bedprakash.rout on 27/09/17.
 */

public class ErrorEventModel {

  public static final int OVERFLOW_HASH = -11;

  public int hashValue = -1;
  private int count = 1;
  public NhAnalyticsEvent event;
  public Map<NhAnalyticsEventParam, Object> eventParams;


  public ErrorEventModel(int hashValue,
                         @NonNull NhAnalyticsEvent event,
                         @NonNull Map<NhAnalyticsEventParam, Object> eventParams) {
    this.hashValue = hashValue;
    this.event = event;
    this.eventParams = eventParams;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ErrorEventModel that = (ErrorEventModel) o;

    return hashValue == that.hashValue;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
    eventParams.put(NhAnalyticsCommonEventParam.COUNT, count);
  }

  @Override
  public int hashCode() {
    return hashValue;
  }

  @Override
  public String toString() {
    return hashValue + " count " + count;
  }
}
