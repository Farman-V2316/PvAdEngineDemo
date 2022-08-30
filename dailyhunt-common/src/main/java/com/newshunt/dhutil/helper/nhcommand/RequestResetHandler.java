/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.nhcommand;

import com.newshunt.common.helper.common.AndroidUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles resetting of pending http request on different screens of the app.
 * Async job that runs periodically.
 *
 * @author shreyas.desai
 */
public class RequestResetHandler {
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> scheduleHandle;
  private RequestResetReceiver requestResetReceiver;
  private long duration;
  private TimeUnit timeUnit;
  private int frequency;
  private int count;

  public RequestResetHandler(RequestResetReceiver requestResetReceiver,
                             long duration,
                             TimeUnit timeUnit,
                             int frequency) {

    this.requestResetReceiver = requestResetReceiver;
    this.duration = duration;
    this.timeUnit = timeUnit;
    this.frequency = frequency;
  }

  public void start() {
    scheduler = AndroidUtils.newScheduledThreadExecutor(1, "RequestReset");
    scheduleHandle = scheduler.scheduleWithFixedDelay(getRunnable(), duration, duration, timeUnit);
  }

  private Runnable getRunnable() {
    return new Runnable() {
      @Override
      public void run() {
        count++;
        try {
          requestResetReceiver.onTrigger(count);
        } catch (Exception e) {
        }

        if (count >= frequency) {
          scheduleHandle.cancel(false);
        }
      }
    };
  }

  public void cancel() {
    if (scheduleHandle != null) {
      scheduleHandle.cancel(true);
    }
    if (scheduler != null) {
      scheduler.shutdownNow();
    }
  }

  public interface RequestResetReceiver {
    void onTrigger(int count);
  }
}
