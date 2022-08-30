/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.notification.view.service.PullNotificationJobService;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;

/**
 * A class for creating a firebase job for pull notifications.
 *
 * @author anshul.jain on 10/25/2016.
 */

public class PullNotificationJob {

  private final boolean canReplaceExistingJob, requiresCharging, isFirstTimeForPullNotification;
  private final int nextScheduledDuration;


  public PullNotificationJob(boolean canBeReplaced, boolean requiresCharging, int
      timeFromNowInSeconds,boolean isFirstTimeForPullNotification) {
    this.canReplaceExistingJob = canBeReplaced;
    this.requiresCharging = requiresCharging;
    this.nextScheduledDuration = timeFromNowInSeconds;
    this.isFirstTimeForPullNotification = isFirstTimeForPullNotification;
  }

  public OneTimeWorkRequest create() throws Exception {
    Data inputData = new Data.Builder()
        .putBoolean(Constants.IS_FIRST_TIME_PULL_NOTIFICATION, isFirstTimeForPullNotification)
        .putBoolean(Constants.CAN_BE_REPLACED, canReplaceExistingJob)
        .build();
    Constraints.Builder constraintsBuilder = new Constraints.Builder()
        // The Worker needs Network connectivity
        .setRequiredNetworkType(NetworkType.CONNECTED);
    // Needs the device to be charging
    if (requiresCharging) {
      constraintsBuilder.setRequiresCharging(true);
    }
    return new OneTimeWorkRequest.Builder(PullNotificationJobService.class)
        .setInputData(inputData)
        .setInitialDelay(nextScheduledDuration, TimeUnit.SECONDS)
        .setConstraints(constraintsBuilder.build())
        .addTag(Constants.PULL_NOTIFICATION_JOB_TAG)
        .build();
  }

  @Override
  public String toString() {
    return "Pull Notification Job Created with tag [ " + Constants.PULL_NOTIFICATION_JOB_TAG +
        " ], " + "canReplaceExistingJob [ " + canReplaceExistingJob + "" +
        " ], requiresCharging [ " + requiresCharging + " ] scheduled after [" + " " +
        nextScheduledDuration + " ]";
  }
}
