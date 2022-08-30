/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

/**
 * Event to notify th ui of App UI Lifecycle
 *
 * @author bedprakash.rout
 */
public class LifeCycleEvent {

  public static final int PAUSED = 101;
  public static final int RESUMED = 102;
  public static final int DESTROYED = 103;
  public static final int CREATED = 104;

  @IntDef(value = {PAUSED, RESUMED, DESTROYED, CREATED})
  public @interface ScreenEventType {
  }

  private final int screenId;
  private final int eventType;

  public LifeCycleEvent(@NonNull int screenId, @ScreenEventType int eventType) {
    this.screenId = screenId;
    this.eventType = eventType;
  }

  public int getScreenId() {
    return screenId;
  }

  @ScreenEventType
  public int getEventType() {
    return eventType;
  }

  @Override
  public String toString() {
    return "LifeCycleEvent{" +
        "screenId=" + screenId +
        ", eventType=" + eventType +
        '}';
  }
}
