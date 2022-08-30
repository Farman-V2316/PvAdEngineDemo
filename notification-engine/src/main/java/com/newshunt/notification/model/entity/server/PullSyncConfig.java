/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.entity.server;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;

/**
 * @author anshul.jain on 10/24/2016.
 *         <p>
 *         The configuration received from the server with pull notifications response.
 */

public class PullSyncConfig {

  //The version for maintaining the sync configuration for pull notifications
  private String syncConfigVersion;

  //The interval after which the next job should be scheduled on the device irrespective of the
  // network.
  private long baseInterval;

  //Interval in seconds when the next pull will be initiated when the user in on Wifi
  private long intervalWifi;

  //Interval in seconds when the next pull will be initiated when the user in on 4g
  private long intervalFourG;

  //Interval in seconds when the next pull will be initiated when the user in on 3g
  private long intervalThreeG;

  //Interval in seconds when the next pull will be initiated when the user in on 2g
  private long intervalTwoG;

  //Tolerance in seconds for the next schedule.
  private long tolerance;

  //If we get a push notification, then cancel immediate syncs between this duration
  private long intervalAfterPushNotification;

  // minimum battery percent required for sync.
  private int batteryPercent;

  //Start of Do not disturb time for the user in "HH/MM" or "HH-MM" or "HH:MM"
  private int dndStartTime;

  //End of Do not disturb time for the user in "HH/MM" or "HH-MM" or "HH:MM"
  private int dndEndTime;

  //Whether Do not disturb is enabled for the user.
  private boolean ignoreDnd;

  //The maximum number of push ids that should be sent in the request
  private int maxPushIds;

  //Time after which sync will be initiated.
  private long maxTimeForExplicitSync;

  //Whether the event should be generated when the pull request is hit
  private boolean enableEventForAPIHit;

  //Whether the event should be generated when the pull request did not get hit.
  private boolean enableEventForNonAPIHit;

  public String getSyncConfigVersion() {
    return syncConfigVersion;
  }

  public void setSyncConfigVersion(String syncConfigVersion) {
    this.syncConfigVersion = syncConfigVersion;
  }

  public long getIntervalWifi() {
    int minDuration = AppConfig.getInstance().getMinBaseInterval();
    return intervalWifi < minDuration ? minDuration : intervalWifi;
  }

  public void setIntervalWifi(long intervalWifi) {
    this.intervalWifi = intervalWifi;
  }

  public long getIntervalFourG() {
    int minDuration = AppConfig.getInstance().getMinBaseInterval();
    return intervalFourG < minDuration ? minDuration : intervalFourG;
  }

  public void setIntervalFourG(long intervalFourG) {
    this.intervalFourG = intervalFourG;
  }

  public long getIntervalThreeG() {
    int minDuration = AppConfig.getInstance().getMinBaseInterval();
    return intervalThreeG < minDuration ? minDuration : intervalThreeG;
  }

  public void setIntervalThreeG(long intervalThreeG) {
    this.intervalThreeG = intervalThreeG;
  }

  public long getIntervalTwoG() {
    int minDuration = AppConfig.getInstance().getMinBaseInterval();
    return intervalTwoG < minDuration ? minDuration : intervalTwoG;
  }

  public void setIntervalTwoG(long intervalTwoG) {
    this.intervalTwoG = intervalTwoG;
  }

  public int getTolerance() {
    return (int) tolerance;
  }

  public void setTolerance(long tolerance) {
    this.tolerance = tolerance;
  }

  public int getDndStartTime() {
    return dndStartTime;
  }

  public void setDndStartTime(int dndStartTime) {
    this.dndStartTime = dndStartTime;
  }

  public int getDndEndTime() {
    return dndEndTime;
  }

  public void setDndEndTime(int dndEndTime) {
    this.dndEndTime = dndEndTime;
  }

  public boolean shouldIgnoreDnd() {
    return ignoreDnd;
  }

  public void setIgnoreDnd(boolean ignoreDnd) {
    this.ignoreDnd = ignoreDnd;
  }

  public int getIntervalAfterPushNotification() {
    return (int) intervalAfterPushNotification;
  }

  public void setIntervalAfterPushNotification(long intervalAfterPushNotification) {
    this.intervalAfterPushNotification = intervalAfterPushNotification;
  }

  public int getBatteryPercent() {
    return batteryPercent == 0 ? Constants.MIN_BATTERY_PERCENT : batteryPercent;
  }

  public void setBatteryPercent(int batteryPercent) {
    this.batteryPercent = batteryPercent;
  }

  public int getBaseInterval() {
    int minDuration = AppConfig.getInstance().getMinBaseInterval();
    return (int) (baseInterval < minDuration ? minDuration : baseInterval);
  }

  public void setBaseInterval(long baseInterval) {
    this.baseInterval = baseInterval;
  }

  public int getMaxPushIds() {
    return maxPushIds;
  }

  public void setMaxPushIds(int maxPushIds) {
    this.maxPushIds = maxPushIds;
  }

  public long getMaxTimeForExplicitSync() {
    return maxTimeForExplicitSync;
  }

  public void setMaxTimeForExplicitSync(long maxTimeForExplicitSync) {
    this.maxTimeForExplicitSync = maxTimeForExplicitSync;
  }

  public boolean enableEventForAPIHit() {
    return enableEventForAPIHit;
  }

  public void setEnableEventForAPIHit(boolean enableEventForAPIHit) {
    this.enableEventForAPIHit = enableEventForAPIHit;
  }

  public boolean enableEventForNonAPIHit() {
    return enableEventForNonAPIHit;
  }

  public void setEnableEventForNonAPIHit(boolean enableEventForNonAPIHit) {
    this.enableEventForNonAPIHit = enableEventForNonAPIHit;
  }

  @Override
  public String toString() {
    return "PullSyncConfig{" +
        "syncConfigVersion='" + syncConfigVersion + '\'' +
        ", baseInterval=" + baseInterval +
        ", intervalWifi=" + intervalWifi +
        ", intervalFourG=" + intervalFourG +
        ", intervalThreeG=" + intervalThreeG +
        ", intervalTwoG=" + intervalTwoG +
        ", tolerance=" + tolerance +
        ", intervalAfterPushNotification=" + intervalAfterPushNotification +
        ", batteryPercent=" + batteryPercent +
        ", dndStartTime=" + dndStartTime +
        ", dndEndTime=" + dndEndTime +
        ", ignoreDnd=" + ignoreDnd +
        ", maxPushIds=" + maxPushIds +
        ", maxTimeForExplicitSync=" + maxTimeForExplicitSync +
        '}';
  }
}
