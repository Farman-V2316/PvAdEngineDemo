/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.notification.analytics;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Utility Class to send Analytics Events in a background thread
 *
 * @author bedprakash.rout on 3/22/2016.
 */
public class AnalyticsHandlerThread extends HandlerThread {
  private static AnalyticsHandlerThread analyticsHandlerThread;
  private static Handler handler;

  private AnalyticsHandlerThread(String name) {
    super(name);
    start();
    handler = new Handler(getLooper());
  }

  public static AnalyticsHandlerThread getInstance() {
    if (analyticsHandlerThread == null) {
      synchronized (AnalyticsHandlerThread.class) {
        if (analyticsHandlerThread == null) {
          analyticsHandlerThread =
              new AnalyticsHandlerThread(AnalyticsHandlerThread.class.getSimpleName());
        }
      }
    }
    return analyticsHandlerThread;
  }

  public void post(Runnable runnable) {
    if (runnable != null && handler != null) {
      handler.post(runnable);
    }
  }

  public static void quitThread() {
    if (analyticsHandlerThread == null) {
      return;
    }
    analyticsHandlerThread.quitSafely();
    analyticsHandlerThread = null;
  }
}