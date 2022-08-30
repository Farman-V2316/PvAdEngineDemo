/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.notification.model.entity.PullSyncConfigWrapper;
import com.newshunt.notification.model.entity.server.PullSyncConfig;

import java.util.Calendar;
import java.util.Date;

import androidx.work.OneTimeWorkRequest;

/**
 * A class for scheduling the pull notification jobs considering the DND times if applicable.
 *
 * @author anshul.jain on 10/24/2016.
 */

public class PullNotificationsJobManager {

  private final PullSyncConfig pullSyncConfig;

  public PullNotificationsJobManager(PullSyncConfig pullSyncConfig) {
    this.pullSyncConfig = pullSyncConfig;
  }

  public PullNotificationsJobManager() {
    pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
  }

  /**
   * This method will be called for schedNuling the next pull sync.
   *
   * @param shouldReplaceExistingJob : If a current job exists with the same tag, whether to replace it or not.
   */
  public void schedule(boolean shouldReplaceExistingJob, boolean requiresCharging) throws
      Exception {
    //Will be true in case of fresh install and Clear Data.
    if (pullSyncConfig == null) {
      PullNotificationLogger.logPullSyncConfigNull();
      return;
    }
    schedule(shouldReplaceExistingJob, requiresCharging, pullSyncConfig.getBaseInterval(), false);
  }

  /**
   * This method will be called when someone explicitly mentions the duration after which the
   * schedule should be taken place. This would happen when the server sends the duration in the
   * pull response mentioning the duration after which sync will take place.
   *
   * @param shouldReplaceExistingJob: If a current job exists with the same tag, whether to replace it or not.
   * @param nextJobInterval:          The time from now in seconds to schedule the task
   */
  public void schedule(boolean shouldReplaceExistingJob, boolean requiresCharging, int
      nextJobInterval, boolean isFirstTimePullNotification) throws Exception {
    if (pullSyncConfig == null) {
      PullNotificationLogger.logPullSyncConfigNull();
      return;
    }

    if (nextJobInterval == 0) {
      return;
    }

    if (!PullNotificationsHelper.isPullJobAllowed()) {
      PullNotificationLogger.logPullJobFailed();
      return;
    }

    int tolerance = pullSyncConfig.getTolerance();
    PullSyncConfigWrapper configWrapper = new PullSyncConfigWrapper(pullSyncConfig).create();
    if (!pullSyncConfig.shouldIgnoreDnd()) {
      nextJobInterval = JobManagerHelper.getIntervalConsideringDnd(configWrapper, nextJobInterval);
    }
    scheduleJob(shouldReplaceExistingJob, requiresCharging, nextJobInterval, tolerance,
        isFirstTimePullNotification);
  }

  public void scheduleJob(boolean shouldReplaceExistingJob, boolean requiresCharging,
                          int nextJobInterval, int tolerance,
                          boolean isFirstTimeForPullNotification) throws Exception {
    if (!PullNotificationsHelper.isPullJobAllowed() || nextJobInterval == 0) {
      return;
    }

    PullNotificationJob pullNotificationJob = new PullNotificationJob(shouldReplaceExistingJob,
        requiresCharging, nextJobInterval, isFirstTimeForPullNotification);
    OneTimeWorkRequest job = pullNotificationJob.create();
    DHWorkManager.beginWork(job, shouldReplaceExistingJob);
    PullNotificationLogger.logNotificationJob(pullNotificationJob);

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, nextJobInterval);
    Date nextJobDate = calendar.getTime();
    PullNotificationsDataHelper.saveNextJobRunningTime(nextJobDate);
    PullNotificationLogger.logNextRunningJob(nextJobDate);
  }

  public void cancelPullNotificationJob() {
    PullNotificationLogger.logCancelPullNotificationJob();
    DHWorkManager.cancelWork(Constants.PULL_NOTIFICATION_JOB_TAG);
  }
}
