/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.entity;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.notification.model.entity.server.PullSyncConfig;
import com.newshunt.sdk.network.connection.ConnectionType;

import java.util.Calendar;
import java.util.Date;

/**
 * @author anshul.jain on 10/24/2016.
 *         <p>
 *         A wrapper class over {@link PullSyncConfig} which converts DND seconds into actual dates and
 *         creates a wrapper over the different network intervals.
 */

public class PullSyncConfigWrapper {

  private final PullSyncConfig syncConfig;

  public PullSyncConfigWrapper(PullSyncConfig syncConfig) {
    this.syncConfig = syncConfig;
  }

  //Interval in seconds for the next schedule
  private long interval;

  // Start Date of Do not disturb.
  private Date dndStartDate;

  // End Date of Do not disturb.
  private Date dndEndDate;

  public int getCurrentNetworkIntervalInSeconds() {
    return (int) interval;
  }

  public long getCurrentNetworkIntervalInMillis() {
    return interval * 1000;
  }

  public void setInterval(long interval) {
    this.interval = interval;
  }

  public Date getDndStartTime() {
    return dndStartDate;
  }

  public void setDndStartTime(Date dndStartDate) {
    this.dndStartDate = dndStartDate;
  }

  public Date getDndEndTime() {
    return dndEndDate;
  }

  public void setDndEndTime(Date dndEndDate) {
    this.dndEndDate = dndEndDate;
  }

  /**
   * This method returns a wrapper over {@link PullSyncConfig}. The wrapper is agnostic to the
   * current network on the user's device for getting the interval. Also wrapper stores the DND
   * startTime and endTime in Date Objects which we receive from the server as string.
   *
   * @return
   */
  public PullSyncConfigWrapper create() {
    if (syncConfig == null) {
      return null;
    }

    String connectionTypeInStr = ConnectionInfoHelper.getConnectionType();
    if (CommonUtils.isEmpty(connectionTypeInStr)) {
      connectionTypeInStr = ConnectionType.NO_CONNECTION.getConnectionType();
    }

    switch (connectionTypeInStr) {
      case "w":
        interval = syncConfig.getIntervalWifi();
        break;
      case "4G":
        interval = syncConfig.getIntervalFourG();
        break;
      case "3G":
      case "3C":
        interval = syncConfig.getIntervalThreeG();
        break;
      case "2G":
      case "2C":
        interval = syncConfig.getIntervalTwoG();
        break;
      default:
        interval = syncConfig.getBaseInterval();
    }

    try {
      putDndTimeIntervals();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return this;
  }

  /**
   * This function is used for setting dndStart and dndEnd from string formats like "HH-MM" to
   * Date objects.
   */
  private void putDndTimeIntervals() throws Exception {
    if (syncConfig.shouldIgnoreDnd()) {
      return;
    }

    int dndStartTimeInSeconds = syncConfig.getDndStartTime();
    int dndEndTimeInSeconds = syncConfig.getDndEndTime();

    if (dndStartTimeInSeconds == dndEndTimeInSeconds) {
      return;
    }

    Calendar calendar = Calendar.getInstance();
    resetCalendar(calendar);
    /**
     * When startHours and greater than end Hours. Consider startHours = "23:00" and endHours =
     * "06:00", then startDndDate will be the current date and endDndDate will be the next date.
     */

    if (dndStartTimeInSeconds > dndEndTimeInSeconds) {
      calendar.set(Calendar.SECOND, dndStartTimeInSeconds);
      setDndStartTime(calendar.getTime());
      resetCalendar(calendar);
      int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
      calendar.set(Calendar.DAY_OF_YEAR, currentDay + 1);
      calendar.set(Calendar.SECOND, dndEndTimeInSeconds);
      setDndEndTime(calendar.getTime());
    }
    /**
     * When startHours and greater than end Hours. Consider startHours = "11:00" and endHours =
     * "16:00", then startDndDate will be the current date and endDndDate will also be the current
     * date.
     */
    else {
      calendar.set(Calendar.SECOND, dndStartTimeInSeconds);
      setDndStartTime(calendar.getTime());
      resetCalendar(calendar);
      calendar.set(Calendar.SECOND, dndEndTimeInSeconds);
      setDndEndTime(calendar.getTime());
    }
  }

  private void resetCalendar(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
  }
}
