/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common;

/**
 * Bus event class that holds infromation about currently alive app activities
 *
 * @author satosh.dhanyamraju
 */
public class AppStateChangeEvent {
  private final int aliveActivitiesCount;
  private boolean isCreated;

  public AppStateChangeEvent(int aliveActivitiesCount) {
    this(aliveActivitiesCount, false);
  }

  public AppStateChangeEvent(int aliveActivitiesCount, boolean isCreated) {
    this.aliveActivitiesCount = aliveActivitiesCount;
    this.isCreated = isCreated;
  }

  public boolean isFirstActivityCreated() {
    return isCreated && aliveActivitiesCount == 1;
  }

  public boolean isLastActivityFinishing() {
    return aliveActivitiesCount == 0;
  }
}
