/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.notification.model.entity.JobStatus;
import com.newshunt.notification.model.entity.PullSyncConfigWrapper;
import com.newshunt.notification.model.entity.server.PullSyncConfig;

import java.util.Date;

/**
 * @author anshul.jain on 11/3/2016.
 *         <p>
 *         A helper class for {@link com.newshunt.notification.view.service.PullNotificationJobService}
 */

public class JobServiceHelper {


  /**
   * This method will returns one of the {@link JobStatus} objects, based on the battery percent
   * and whether the battery is charging or not.
   *
   * @param pullSyncConfig : Configuration for the pull mechanism
   * @return
   */
  public static JobStatus getJobStatusBasedOnBattery(PullSyncConfig pullSyncConfig) {
    long minBatteryPercent = pullSyncConfig.getBatteryPercent();
    long currentBatteryPercent = (long) CommonUtils.getBatteryPercent();
    boolean isBatteryCharging = CommonUtils.isDeviceCharging();
    if (currentBatteryPercent < minBatteryPercent) {

      if (isBatteryCharging) {
        return JobStatus.REJECT_PULL;
      } else {
        return JobStatus.REJECT_PULL_JOB_REQUIRES_CHARGING;
      }
    }
    return JobStatus.ALLOW;
  }

  /**
   * This method returns true if the current job is invoked during DND hours. false otherwise
   *
   * @param syncConfigWrapper : Configuration for the pull mechanism
   * @return
   */
  public static boolean isJobInvokedInDND(PullSyncConfigWrapper syncConfigWrapper) {
    Date dndStartDate = syncConfigWrapper.getDndStartTime();
    Date dndEndDate = syncConfigWrapper.getDndEndTime();
    Date currentDate = new Date();

    return CommonUtils.isJobInDND(dndStartDate, dndEndDate, currentDate);
  }
}
