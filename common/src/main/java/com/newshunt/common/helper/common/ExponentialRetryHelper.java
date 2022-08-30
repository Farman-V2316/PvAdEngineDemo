/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Helper to support exponential backoff.
 *
 * @author karthik.r
 */
public class ExponentialRetryHelper {
  private static final Map<Runnable, ExponentialRetryHelper> scheduledTask = new HashMap<>();
  private final Runnable runnable;
  private final long maxInterval;
  private final int maxAttempt;
  private final double exponentialFactor;
  private final ScheduledExecutorService scheduler = AndroidUtils.newScheduledThreadExecutor(1,
      "ExponentialRetry");
  private long currentInterval;
  private int retryCount;
  private String TAG = "ExponentialRetryHelper";

  /**
   * @param runnable          runnable to be executed
   * @param initialInterval   interval(in seconds) for the first retry attempt
   * @param maxInterval       maximum value at this successive intervals will be capped
   * @param maxAttempt        maxiumum number of retry attempts before terminating
   * @param exponentialFactor exponential factor for computing successive intervals
   */
  public ExponentialRetryHelper(Runnable runnable, long initialInterval, long maxInterval,
                                int maxAttempt, double exponentialFactor) {
    this.runnable = runnable;
    this.currentInterval = initialInterval;
    this.maxInterval = maxInterval;
    this.maxAttempt = maxAttempt;
    this.exponentialFactor = exponentialFactor;
  }

  public static ExponentialRetryHelper getRetryHelper(Runnable runnable) {
    return scheduledTask.get(runnable);
  }

  /**
   * Starts first execution of provided runnable.
   */
  public void start() {
    scheduledTask.put(runnable, this);
    scheduler.execute(runnable);
  }

  /**
   * If retry count is within specified max limit, schedule runnable to execute at interval based
   * on exponential backoff.
   * <p>
   * If retry count exceeds max attempts allowed, do not schedule.
   */
  public void onFailure(String event) {
    retryCount++;

    currentInterval = (long) (currentInterval * exponentialFactor);

    if (currentInterval > maxInterval) {
      currentInterval = maxInterval;
    }

    if (retryCount > maxAttempt) {
      scheduledTask.remove(runnable);
      return;
    }

    Logger.d(TAG, "Retry count " + retryCount + " for event " + event);
    Logger.d(TAG, "Scheduling the api hit after " + currentInterval + " seconds");
    scheduler.schedule(runnable, currentInterval, TimeUnit.SECONDS);
  }

  /**
   * Clean up helper instance corresponding to executing runnable.
   */
  public void onSuccess(String event) {
    Logger.d(TAG, "event " + event + " success after " + retryCount + " counts");
    scheduledTask.remove(runnable);
  }

  /*
   * Return current attempt number
   */
  public int getAttemptNumber() {
    return retryCount + 1;
  }
}
