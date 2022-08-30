/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.notification.model.entity.PullSyncConfigWrapper;
import com.newshunt.notification.util.PullNotificationsUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * @author anshul.jain on 11/3/2016.
 *         <p>
 *         This is a helper class for {@link PullNotificationsJobManager}
 */

public class JobManagerHelper {

  /**
   * This method will return the interval when the next job should be scheduled. This method
   * takes into consideration the DND hours and returns the interval accordingly.
   *
   * @param configWrapper  : Wrapper over Pull Notifications configuration.
   * @return
   */
  public static int getIntervalConsideringDnd(PullSyncConfigWrapper configWrapper, int interval)
      throws Exception {
    //Ideally, these should not be null, putting a null check just for the sake of safety.
    // IF interval is zero, it means that it is an immediate pull request. So no need of DND.
    if (configWrapper == null || interval == 0) {
      return 0;
    }

    Date dndStartTime = configWrapper.getDndStartTime();
    Date dndEndTime = configWrapper.getDndEndTime();

    //According to configuration, get a Date object for next schedule.
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, interval);
    //nextJobDate should be the time when the job should be invoked ideally without
    // considering DND
    Date nextJobTime = calendar.getTime();

    boolean isNextJobInDND = CommonUtils.isJobInDND(dndStartTime,
        dndEndTime, nextJobTime);

    //It means that the next job is not scheduled between DND hours
    if (!isNextJobInDND) {
      return interval;
    }

    boolean isCurrentTimeInDND = CommonUtils.isJobInDND(dndStartTime, dndEndTime, new
        Date());

    if (isCurrentTimeInDND) {
      return skipJobsDuringDND(interval, dndStartTime, dndEndTime);
    } else {
      return addIntervaltoDND(dndStartTime, dndEndTime, nextJobTime);
    }
  }

  private static int addIntervaltoDND(Date dndStartTime, Date dndEndTime, Date nextJobTime) {
    Calendar calendar = Calendar.getInstance();
    //It means that the next job is  scheduled between DND hours
    int duration = PullNotificationsUtil.getDiffInSeconds(dndStartTime, nextJobTime);
    calendar.setTime(dndEndTime);
    calendar.add(Calendar.SECOND, duration);
    //In terms of Date object, this is the date when the job should be invoked. This date will
    // definitely fall after DND.
    Date actualScheduledDate = calendar.getTime();

    Date currentDate = new Date();
    int jobIntervalConsideringDnd = PullNotificationsUtil.getDiffInSeconds(currentDate,
        actualScheduledDate);
    return jobIntervalConsideringDnd;
  }

  private static int skipJobsDuringDND(int baseInterval, Date dndStartTime, Date dndEndTime) {
    if (baseInterval == 0) {
      return 0;
    }
    Date currentTime = new Date();
    Calendar calendar = Calendar.getInstance();
    int count = 1;

    //If the currentTime falls between dnd intervals, add baseInterval to current date.
    while (CommonUtils.isJobInDND(dndStartTime, dndEndTime, currentTime)) {
      calendar.add(Calendar.SECOND, baseInterval);
      currentTime = new Date(calendar.getTimeInMillis());
      count++;
    }
    return baseInterval * count;
  }
}
