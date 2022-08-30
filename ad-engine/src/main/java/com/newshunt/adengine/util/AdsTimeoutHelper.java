/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.util;

import com.newshunt.common.helper.common.AndroidUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provides function to get timeout for ad request based on connection type.
 *
 * @author heena.arora
 */
public class AdsTimeoutHelper {

  public interface TimeoutListener {
    void onTimeOutOccurred();
  }

  private static final String TAG = "AdsTimeoutHelper";

  private final ScheduledExecutorService cancelRequestExecutorService =
      AndroidUtils.newScheduledThreadExecutor(1, "AdsTimeout");
  private ScheduledFuture scheduledFuture;
  private TimeoutListener listener;
  private final int timeOut;
  private boolean timerShutdown;

  public AdsTimeoutHelper(TimeoutListener listener, int timeOut) {
    this.listener = listener;
    this.timeOut = timeOut;
  }

  public void startTimer() {
    if (listener == null || timerShutdown) {
      return;
    }
    scheduledFuture = cancelRequestExecutorService.schedule(() -> {
      listener.onTimeOutOccurred();
      stopTimer();
    }, timeOut, TimeUnit.SECONDS);
  }

  public void stopTimer() {
    if (timerShutdown) {
      return;
    }
    listener = null;
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
    cancelRequestExecutorService.shutdown();
    timerShutdown = true;
  }

}
