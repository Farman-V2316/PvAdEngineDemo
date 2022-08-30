/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import com.newshunt.common.AppStateChangeEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Static class to main status of the application. Application class is
 * not accessible in different sections of App... Hence needs to put it in shared common.
 *
 * @author shreyas.desai
 */
public class ApplicationStatus {
  private static int aliveActivitiesCount = 0;
  private static AtomicInteger visibleActivitiesCount = new AtomicInteger(0);
  private static boolean isRunning;
  private static boolean canCleanRAM = false;
  private static AppLaunchMode appLaunchMode = null;

  public static boolean canCleanRAM() {
    return canCleanRAM;
  }

  public static void setCanCleanRAM(boolean canCleanRAM) {
    ApplicationStatus.canCleanRAM = canCleanRAM;
  }

  public static boolean isRunning() {
    return isRunning;
  }

  public static void setRunning(boolean status) {
    isRunning = status;
  }

  public static void incActivityCountAndPostEvent() {
    boolean isCreated = aliveActivitiesCount == 0; // creation is when count becomes 0->1 (not 2->1)
    aliveActivitiesCount++;
    BusProvider.getUIBusInstance().post(new AppStateChangeEvent(aliveActivitiesCount, isCreated));
  }

  public static void decActivityCountAndPostEvent() {
    aliveActivitiesCount--;
    AppStateChangeEvent event = new AppStateChangeEvent(aliveActivitiesCount);
    BusProvider.getUIBusInstance().post(event);
    BusProvider.getRestBusInstance().post(event); // required for AppSectionsProvider
  }

  public static void incVisibleActivityCount() {
    visibleActivitiesCount.incrementAndGet();
  }

  public static int decVisibleActivityCount() {
    return visibleActivitiesCount.decrementAndGet();
  }

  public static int getVisibleActiviesCount(){
    return visibleActivitiesCount.get();
  }

  public static enum AppLaunchMode {
    SPLASH,
    NOTIFICATION_CLICK,
    DEEP_LINK,
    WIDGETS,
  }

  public static void setAppLaunchMode(AppLaunchMode mode) {
    if (mode != null) {
      appLaunchMode = mode;
    }
  }

  public static AppLaunchMode getAppLaunchMode() {
    return appLaunchMode;
  }
}
