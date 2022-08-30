/**  Copyright (c) 2019 Newshunt. All rights reserved.*/
package com.newshunt.notification.model.service;

import android.content.Context;
import android.content.Intent;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.notification.helper.DHWorkManager;
import com.newshunt.notification.model.internal.dao.StickyNotificationEntity;
import com.newshunt.notification.model.internal.dao.StickyNotificationsDatabaseKt;
import com.newshunt.notification.model.manager.NotiRemoveFromTrayJobManager;
import com.newshunt.notification.helper.NotificationUtils;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.notification.view.receiver.StickyNotificationFinishReceiver;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * @author anshul.jain on 20/09/2017.
 * <p>
 * A service which runs when the scheduler framework schedules the job to remove a sticky
 * notification from the tray.
 */

public class NotiRemoveFromTrayJobService extends Worker {

  private static String TAG = "StickyNotificationRemoveFromTrayJobService";
  WorkerParameters workerParameters;

  public NotiRemoveFromTrayJobService(@NonNull Context context,
                                      @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
    workerParameters = workerParams;
  }

  @NonNull
  @Override
  public Result doWork() {
    NotiRemoveFromTrayJobHandler handler = new NotiRemoveFromTrayJobHandler(workerParameters);
    handler.run();
    return Result.success();
  }

  private class NotiRemoveFromTrayJobHandler implements Runnable {

    private final WorkerParameters workerParameters;

    NotiRemoveFromTrayJobHandler(WorkerParameters workerParameters) {
      this.workerParameters = workerParameters;
    }

    @Override
    public void run() {
      try {
        startJob();
      } catch (Exception e) {
        Logger.d(TAG, "Caught Exception:- " + e.getMessage().toString());
        Logger.caughtException(e);
      }
    }

    private void startJob() throws Exception {

      Logger.d(TAG, "start Job enter..");
      if (null == workerParameters) {
        Logger.d(TAG, "job parameters or job extras are null, so returning");
        jobCompleted(workerParameters);
        return;
      }

      Data inputData = workerParameters.getInputData();
      int notificationTrayId =
          inputData.getInt(NotificationConstants.INTENT_STICKY_NOTIFICATION_TRAY_ID, 1);
      String id = inputData.getString(NotificationConstants.INTENT_EXTRA_STICKY_ID);
      String stickyType = inputData.getString(NotificationConstants.INTENT_EXTRA_STICKY_TYPE);

      if (CommonUtils.isEmpty(id) || CommonUtils.isEmpty(stickyType)) {
        Logger.d(TAG, "id is empty or stickyType is empty so returning..");
        jobCompleted(workerParameters);
        return;
      }

      StickyNotificationEntity dbEntity =
          StickyNotificationsDatabaseKt.getStickyNotificationsDBInstance().stickyNotificationDao()
              .getNotificationByIdAndType(id, stickyType);

      if (dbEntity == null || dbEntity.getExpiryTime() == null) {
        Logger.d(TAG, "dbEntity is null or dbExpiry time is null, so returning");
        jobCompleted(workerParameters);
        return;
      }

      long expiryDurationStoredInDB = dbEntity.getExpiryTime();

      if ((expiryDurationStoredInDB/1000) > (System.currentTimeMillis()/1000)) {
        int diffInSeconds = (int) (expiryDurationStoredInDB - System.currentTimeMillis()) / 1000;
        NotiRemoveFromTrayJobManager manager = new NotiRemoveFromTrayJobManager();
        Logger.d(TAG, "Expiry is greater than currentTime scheduling in;- '" + diffInSeconds);
        manager.scheduleJob(notificationTrayId, id, stickyType, diffInSeconds);
        return;
      }

      Intent intent =
          new Intent(CommonUtils.getApplication(), StickyNotificationFinishReceiver.class);
      intent.setPackage(CommonUtils.getApplication().getPackageName());
      intent.setAction(NotificationConstants.INTENT_ACTION_REMOVE_FROM_TRAY_JOB_DONE);
      intent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID, id);
      intent.putExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE, stickyType);

      CommonUtils.getApplication().sendBroadcast(intent);
      Logger.d(TAG, "sending the broadcast to remove from DB");

      //TODO(santhosh.kc) or to call directly?
      //StickyNotificationsManager.INSTANCE.onNotificationRemovedFromTrayJobDone(id, stickyType);

      NotificationUtils.removeNotificationFromTray(notificationTrayId);
    }
  }

  private void jobCompleted(WorkerParameters workerParameters) {
    DHWorkManager.cancelWork(workerParameters.getTags().iterator().next());
  }
}