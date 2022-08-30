/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.entity;

import android.os.SystemClock;

import com.newshunt.common.helper.common.Logger;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by anshul on 19/12/16.
 */

public class PullNotificationJobEvent {

  private String pullSyncConfigVersion;
  private String deviceTime;
  private String lastRebootTime;
  private String lastSuccessfulPullSyncTime;
  private String lastPushNotificationTime;
  private String nextPullJobTime;

  private String currentNetwork;
  private String batteryPercent;
  private boolean isCharging;

  private boolean isFirstTimePull; // pass only if true
  private boolean isNetworkAvailable = true; //keeping the default value as true.
  private boolean enabledInHamburger = true; //Keeping the default value as true.
  private boolean enableByServer = true; //Keeping the default value as true.

  private String pullNotificationJobResult;
  private String pullFailureReason;

  private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat
      ("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

  public PullNotificationJobEvent() {
    setDeviceTime();
    setLastRebootTimestamp();
  }

  public String getDeviceTime() {
    return deviceTime;
  }

  public void setDeviceTime() {
    Date date = new Date();
    this.deviceTime = simpleDateFormat.format(date);
  }

  public String getLastRebootTime() {
    return lastRebootTime;
  }

  public void setLastRebootTimestamp() {
    long currentTime = System.currentTimeMillis();
    long timeSinceBoot = SystemClock.elapsedRealtime();
    long timeWhenDeviceBooted = currentTime - timeSinceBoot;
    if (timeWhenDeviceBooted <= 0) {
      return;
    }
    Date date = new Date(timeWhenDeviceBooted);
    this.lastRebootTime = simpleDateFormat.format(date);
  }

  public String getLastSuccessfulPullSyncTime() {
    return lastSuccessfulPullSyncTime;
  }

  public void setLastSuccessfulPullSyncTime(Date lastSuccessfulPullSyncTime) {
    if (lastSuccessfulPullSyncTime != null) {
      this.lastSuccessfulPullSyncTime = simpleDateFormat.format(lastSuccessfulPullSyncTime);
    }
  }

  public String getPullSyncConfigVersion() {
    return pullSyncConfigVersion;
  }

  public void setPullSyncConfigVersion(String syncConfigVersion) {
    this.pullSyncConfigVersion = syncConfigVersion;
  }

  public String getLastPushNotificationTime() {
    return lastPushNotificationTime;
  }

  public void setLastPushNotificationTime(Date lastPushNotificationTime) {
    if (lastPushNotificationTime != null) {
      this.lastPushNotificationTime = simpleDateFormat.format(lastPushNotificationTime);
    }
  }

  public String getBatteryPercent() {
    return batteryPercent;
  }

  public void setBatteryPercent(double batteryPercent) {
    try {
      DecimalFormat decimalFormat = new DecimalFormat(".##");
      this.batteryPercent = decimalFormat.format(batteryPercent);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public String getCurrentNetwork() {
    return currentNetwork;
  }

  public void setCurrentNetwork(String currentNetwork) {
    this.currentNetwork = currentNetwork;
  }

  public boolean isNetworkAvailable() {
    return isNetworkAvailable;
  }

  public void setNetworkAvailable(boolean networkAvailable) {
    isNetworkAvailable = networkAvailable;
  }

  public boolean isCharging() {
    return isCharging;
  }

  public void setCharging(boolean charging) {
    isCharging = charging;
  }

  public String getPullNotificationJobResult() {
    return pullNotificationJobResult;
  }

  public void setPullNotificationJobResult(
      PullNotificationJobResult pullNotificationJobResult) {
    this.pullNotificationJobResult = pullNotificationJobResult.toString();
  }

  public void setIsFirstTimePull(boolean isFirstTimePull) {
    this.isFirstTimePull = isFirstTimePull;
  }

  public boolean isFirstTimePull() {
    return isFirstTimePull;
  }

  public boolean isEnabledInHamburger() {
    return enabledInHamburger;
  }

  public void setEnabledInHamburger(boolean enabledInHamburger) {
    this.enabledInHamburger = enabledInHamburger;
  }

  public boolean isEnableByServer() {
    return enableByServer;
  }

  public void setEnableByServer(boolean enableByServer) {
    this.enableByServer = enableByServer;
  }

  public String getPullFailureReason() {
    return pullFailureReason;
  }

  public void setPullFailureReason(PullJobFailureReason pullFailureReason) {
    this.pullFailureReason = pullFailureReason.toString();
  }

  public String getNextPullJobTime() {
    return nextPullJobTime;
  }

  public void setNextPullJobTime(Date nextPullJobTime) {
    if (nextPullJobTime != null) {
      this.nextPullJobTime = simpleDateFormat.format(nextPullJobTime);
    }
  }
}
