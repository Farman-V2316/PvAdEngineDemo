/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

/**
 * Event to notify when a tab is clicked
 *
 * @author ranjith.suda
 */
public class TabClickEvent {
  private final int newTabPosition;
  private final int oldTabPosition;
  private final int slidingTabId;
  private final long createdAt = System.currentTimeMillis();

  public TabClickEvent(int oldTabPosition, int newTabPosition, int slidingTabId) {
    this.oldTabPosition = oldTabPosition;
    this.newTabPosition = newTabPosition;
    this.slidingTabId = slidingTabId;
  }

  public int getNewTabPosition() {
    return newTabPosition;
  }

  public int getOldTabPosition() {
    return oldTabPosition;
  }

  public int getSlidingTabId() {
    return slidingTabId;
  }

  public long getCreatedAt() {
    return createdAt;
  }
}
