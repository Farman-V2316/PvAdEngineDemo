/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.model.manager;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.notification.helper.DHWorkManager;

import androidx.work.OneTimeWorkRequest;



/**
 * Created by anshul on 20/09/17.
 */

public class NotiRemoveFromTrayJobManager {

  /**
   * A utility method to schedule a job for removing notification from the tray.
   *
   * @param notificationTrayId
   * @param timeDiffInSeconds
   */
  public void scheduleJob(int notificationTrayId, String id, String type, int timeDiffInSeconds) {
    NotiRemoveFromTrayWorkRequest
        job = new NotiRemoveFromTrayWorkRequest(notificationTrayId, id, type,
        timeDiffInSeconds);

    OneTimeWorkRequest deferredJob;
    try {
      deferredJob = job.create();
    } catch (Exception e) {
      Logger.caughtException(e);
      return;
    }
    if (deferredJob == null) {
      return;
    }
    DHWorkManager.beginWork(deferredJob, true);
  }

  /**
   * A utility method for cancelling a job scheduled for removing notification from the tray.
   *
   * @param notificationId
   */
  public void cancelJob(int notificationId) {
    if (notificationId == 0) {
      return;
    }
    String tag = Integer.toString(notificationId);
    DHWorkManager.cancelWork(DailyhuntConstants.JOB_PREFIX + tag);
  }

}