/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity;

/**
 * Event which will be fired when activity stops. Will be used in cases like
 * stopping mraid video ad when user goes out of the activity.
 *
 * @author heena.arora
 */
public class ActivityOnStopEvent {
  private int uniqueRequestId;

  public ActivityOnStopEvent(int uniqueRequestId) {
    this.uniqueRequestId = uniqueRequestId;
  }
}
