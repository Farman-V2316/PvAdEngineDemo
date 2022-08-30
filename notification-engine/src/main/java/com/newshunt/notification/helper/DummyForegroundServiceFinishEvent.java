/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

/**
 * Created by anshul on 17/11/17.
 * <p>
 * A bus event to stop DummyNotiForegroundService
 */

public class DummyForegroundServiceFinishEvent {

  private boolean stopOnlyForeground;

  public boolean isStopOnlyForeground() {
    return stopOnlyForeground;
  }

  public void setStopOnlyForeground(boolean stopOnlyForeground) {
    this.stopOnlyForeground = stopOnlyForeground;
  }
}
