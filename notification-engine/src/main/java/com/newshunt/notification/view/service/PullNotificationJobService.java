/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.view.service;


import android.content.Context;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.EgEvent;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.helper.KillProcessAlarmManager;
import com.newshunt.notification.helper.DHWorkManager;
import com.newshunt.notification.helper.JobServiceHelper;
import com.newshunt.notification.helper.PullNotificationLogger;
import com.newshunt.notification.helper.PullNotificationsDataHelper;
import com.newshunt.notification.helper.PullNotificationsHelper;
import com.newshunt.notification.helper.PullNotificationsJobManager;
import com.newshunt.notification.model.entity.JobStatus;
import com.newshunt.notification.model.entity.PullJobFailureReason;
import com.newshunt.notification.model.entity.PullNotificationJobEvent;
import com.newshunt.notification.model.entity.PullNotificationJobResult;
import com.newshunt.notification.model.entity.PullSyncConfigWrapper;
import com.newshunt.notification.model.entity.server.PullSyncConfig;
import com.newshunt.notification.presenter.PullNotificationsPresenter;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * @author anshul.jain on 10/24/2016.
 * <p>
 * A service which runs when the scheduler framework schedules our job.
 */

public class PullNotificationJobService extends Worker {
  private final Context mContext;
  private final WorkerParameters mWorkerParameters;

  /**
   * @param appContext   The application {@link Context}
   * @param workerParams Parameters to setup the internal state of this worker
   */
  public PullNotificationJobService(@NonNull Context appContext,
                                @NonNull WorkerParameters workerParams) {
    super(appContext, workerParams);
    mContext = appContext;
    mWorkerParameters = workerParams;
  }

  @NonNull
  @Override
  //this method runs on background thread
  public ListenableWorker.Result doWork() {
    if (mWorkerParameters != null) {
      Data data = mWorkerParameters.getInputData();
      KillProcessAlarmManager.onAppProcessInvokedInBackground();
      boolean isFirstTimeForPullNotification =
          data.getBoolean(Constants.IS_FIRST_TIME_PULL_NOTIFICATION, false);
      NotificationWorkerHandler notificationWorkerHandler =
          new NotificationWorkerHandler(mWorkerParameters);
      try {
        notificationWorkerHandler.startPullNotificationWork(isFirstTimeForPullNotification);
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }
    return ListenableWorker.Result.success();
  }

  private class NotificationWorkerHandler {

    //private final WorkerParameters workerParameters;
    private final WorkerParameters workerParameters;

    NotificationWorkerHandler(WorkerParameters workerParameter) {
      workerParameters = workerParameter;
    }

    /**
     * When the work request is invoked by the framework, the following things should be checked.
     * 1) Check the current network and based on it initiate a pull request.
     * 2) Check the current battery status. Based on the battery status schedule the work and
     * initiate the pull request.
     * 3) Check if the current work is falling in DND hours. If yes, then reschedule the work
     * request.
     */

    private void startPullNotificationWork(boolean isFirstTimeForPullSyncConfig) throws Exception {

      PullNotificationLogger.logOnStartJob();
      PullNotificationJobEvent pullNotificationJobEvent = new PullNotificationJobEvent();
      pullNotificationJobEvent.setPullNotificationJobResult(
          PullNotificationJobResult.PULL_API_NOT_HIT);


      if (isFirstTimeForPullSyncConfig) {
        pullNotificationJobEvent.setIsFirstTimePull(true);
        startPullNotifications(pullNotificationJobEvent);
        return;
      }

      PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
      if (pullSyncConfig == null) {
        PullNotificationLogger.logPullSyncConfigNull();
        setFailureReason(
            PullJobFailureReason.INVALID_SYNC_CONFIGURATION, pullNotificationJobEvent);
        return;
      }

      PullNotificationsJobManager workManager =
          new PullNotificationsJobManager(pullSyncConfig);
      PullSyncConfigWrapper syncConfigWrapper = new PullSyncConfigWrapper(pullSyncConfig).create();
      if (syncConfigWrapper == null) {
        setFailureReason(PullJobFailureReason.INVALID_SYNC_CONFIGURATION,
            pullNotificationJobEvent);
        return;
      }

      pullNotificationJobEvent.setIsFirstTimePull(false);

      /** 1st condition.
       *  If case of No Internet, tell the framework that the work request is completed.
       */
      if (!CommonUtils.isNetworkAvailable(mContext)) {
        pullNotificationJobEvent.setNetworkAvailable(false);
        setFailureReason(PullJobFailureReason.NO_NETWORK, pullNotificationJobEvent);
        PullNotificationsHelper.logPullNotificationJobEvent(pullNotificationJobEvent,
            pullSyncConfig);
        workManager.schedule(true /*should replace current job*/, false/*requires battery*/);
        workCompleted(workerParameters);
        return;
      }

      /** 2dn condition
       * If the difference between the current time and last synced time is less than the sync
       * interval required for the current network, then return. E.g. for Wifi if the sync
       * interval is 15 minutes and the last sync happened before 10 minutes, then skip.
       */
      if (!PullNotificationsHelper.shouldPullBasedOnNetwork(syncConfigWrapper)) {
        setFailureReason(
            PullJobFailureReason.CURRENT_NETWORK_PULL_NOT_REQUIRED, pullNotificationJobEvent);
        PullNotificationsHelper.logPullNotificationJobEvent(pullNotificationJobEvent,
            pullSyncConfig);
        PullNotificationLogger.logSkipJobDueToCurrentNetwork(syncConfigWrapper);
        workManager.schedule(true /*should replace current job*/, false/*requires battery*/);
        workCompleted(workerParameters);
        return;
      }


      /** 3nd condition
       If the battery is greater than x%, continue with the sync and schedule next work request.
       If the battery is less than x% then, do not initiate a pull sync
       If the device is in charging condition, schedule the next work request as usual.
       If the device is not in charging condition, then, schedule the next work request
       immediately with
       only “requiresCharging” and “requiresInternet” condition, but without the time interval.
       */
      JobStatus jobStatus = JobServiceHelper.getJobStatusBasedOnBattery(pullSyncConfig);
      switch (jobStatus) {
        case REJECT_PULL_JOB_REQUIRES_CHARGING:
          //do it immediately. But since Google suggests to do the task atleast after 30 seconds,
          // so we set the next pull at 60 seconds
          workManager.schedule(true, true, 30/*seconds*/, false);
          setFailureReason(
              PullJobFailureReason.BATTERY_LESS_DEVICE_NON_CHARGING, pullNotificationJobEvent);
          break;
        case REJECT_PULL:
          workManager.schedule(true, false);
          setFailureReason(
              PullJobFailureReason.BATTERY_LESS_DEVICE_CHARGING, pullNotificationJobEvent);
          break;
      }

      if (jobStatus != JobStatus.ALLOW) {
        workCompleted(workerParameters);
        PullNotificationsHelper.logPullNotificationJobEvent(pullNotificationJobEvent,
            pullSyncConfig);
        return;
      }

      /**4rd condition
       * If this work request is invoked during dndHours, then return
       */
      boolean isJobInvokedDuringDND = JobServiceHelper.isJobInvokedInDND(syncConfigWrapper);
      if (isJobInvokedDuringDND) {
        setFailureReason(PullJobFailureReason.DND_INTERVAL, pullNotificationJobEvent);
        PullNotificationsHelper.logPullNotificationJobEvent(pullNotificationJobEvent,
            pullSyncConfig);
        workManager.schedule(true, false);
        workCompleted(workerParameters);
        return;
      }

      startPullNotifications(pullNotificationJobEvent);
      //prefetch evergreen ads
      BusProvider.getAdBusInstance().post(new EgEvent(Constants.PULL_NOTIFICATION_TAG));
    }

    private void startPullNotifications(PullNotificationJobEvent pullNotificationJobEvent) {
      PullNotificationsPresenter presenter =
          new PullNotificationsPresenter(BusProvider.getRestBusInstance());
      presenter.pullNotifications(pullNotificationJobEvent);
      //Whether pull notifications will be successful or not, tell the scheduling framework that
      // the work is done. We won't retry if the pull notification fails due to any reason. We
      // will simply wait for next work cycle for the next pull to happen.
      workCompleted(workerParameters);
    }

    private void workCompleted(WorkerParameters workerParameter) {
      PullNotificationLogger.logJobCompleted();
      //Tell the framework explicitly that the work for this work cycle has completed and there is
      // no need to reschedule
      DHWorkManager.cancelWork(workerParameter.getTags().iterator().next());
    }

    private void setFailureReason(PullJobFailureReason pullJobFailureReason,
                                  PullNotificationJobEvent event) {
      if (event == null || pullJobFailureReason == null) {
        return;
      }
      event.setPullFailureReason(pullJobFailureReason);
      PullNotificationLogger.logPullRequestFailedReason(pullJobFailureReason.getReason());

    }
  }
}
