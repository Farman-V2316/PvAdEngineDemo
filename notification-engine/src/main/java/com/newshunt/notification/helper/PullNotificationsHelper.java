/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.helper.KillProcessAlarmManager;
import com.newshunt.notification.analytics.PullNotificationJobAnalyticsHelper;
import com.newshunt.notification.model.entity.PullNotificationJobEvent;
import com.newshunt.notification.model.entity.PullNotificationJobResult;
import com.newshunt.notification.model.entity.PullNotificationRequestStatus;
import com.newshunt.notification.model.entity.PullSyncConfigWrapper;
import com.newshunt.notification.model.entity.server.PullSyncConfig;
import com.newshunt.notification.presenter.PullNotificationsPresenter;
import com.newshunt.notification.util.PullNotificationsUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * @author anshul.jain on 10/24/2016.
 *         <p>
 *         This class is used for saving and getting the pull sync configuration parameters.
 */

public class PullNotificationsHelper {

  /**
   * This function will be called when the user toggles Notification item in the hamburger menu.
   * If the user disables the notification, then cancel the scheduling job.
   * If the user enables the notification, then check the last sync time
   * If the last sync time is greater than the sync interval, then initiate the pull request
   * Else schedule the job.
   *
   * @param enabled
   */
  public static void handleHamburgerNotificationAction(boolean enabled) {
    //User enabled/disabled the notifications from Hamburger menu.
    PullNotificationLogger.logHamburgerMenuAction(enabled);
    enableOrDisablePull(enabled);
  }

  /**
   * This function stores two values from the server in the shared preferences
   *
   * @param enabledByServerCurrentValue : Whether notifications are enabled or disabled by the
   *                                    server. Store this value only if it has changed since the
   *                                    last response.
   * @param firstTimePullDelay          : The time after which the first pull should happen.
   */
  public static void handleDeviceRegisterResponse(boolean enabledByServerCurrentValue,
                                                  int firstTimePullDelay) {

    PullNotificationsDataHelper.persistFirstTimePullDelay(firstTimePullDelay);

    boolean enabledByServerStoredValue = PullNotificationsDataHelper
        .arePullNotificationsEnabled();

    //If the stored value, and the incoming value from server are same, then return.
    if (enabledByServerStoredValue == enabledByServerCurrentValue) {
      return;
    }
    PullNotificationLogger.logServerEnableResponseForPull(enabledByServerCurrentValue,
        firstTimePullDelay);
    PullNotificationsDataHelper.savePullNotificationsEnabled(enabledByServerCurrentValue);
    enableOrDisablePull(enabledByServerCurrentValue);
  }

  public static void enableOrDisablePull(boolean isEnabled) {
    if (!isEnabled) {
      PullNotificationsJobManager manager = new PullNotificationsJobManager();
      manager.cancelPullNotificationJob();
      return;
    }

    if (PullNotificationsDataHelper.getSyncConfiguration() == null) {
      initFirstPullJob();
      return;
    }
    //User enabled the notifications
    PullNotificationsJobManager manager = new PullNotificationsJobManager();
    if (shouldPullBasedOnNetwork()) {
      try {
        manager.schedule(false /*replace existing job*/, false /*requires charging*/, 60/*base
        Interval*/, false/* pull sync can be null*/);
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    } else {
      PullNotificationLogger.logSkipJobDueToCurrentNetwork();
      try {
        manager.schedule(false/*can replace existing job*/, false /*requires battery*/);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * If the difference between the current time and last synced time is less than the sync
   * interval required for the current network, then return true, else false
   */
  private static boolean shouldPullBasedOnNetwork() {
    PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    PullSyncConfigWrapper syncConfigWrapper = new PullSyncConfigWrapper(pullSyncConfig).create();
    return shouldPullBasedOnNetwork(syncConfigWrapper);
  }

  /**
   * If the difference between the current time and last synced time is less than the sync
   * interval required forthe current network, then return true, else false
   */
  public static boolean shouldPullBasedOnNetwork(PullSyncConfigWrapper syncConfigWrapper) {

    if (syncConfigWrapper == null) {
      return false;
    }

    long syncIntervalOnCurrentNetworkInMillis = syncConfigWrapper
        .getCurrentNetworkIntervalInMillis();

    Date lastSyncedTime = PullNotificationsDataHelper.getLastSuccessfulSyncedTime();
    if (lastSyncedTime == null) {
      return true;
    }

    Date currentTime = new Date();
    long diffBetweenLastSyncedTimeInMillis = currentTime.getTime() - lastSyncedTime.getTime();

    //If the difference is less than what is required, then return false.
    return diffBetweenLastSyncedTimeInMillis >= syncIntervalOnCurrentNetworkInMillis;
  }


  public static void handlePushNotification() {
    PullNotificationLogger.logPushNotificationReceived();
    PullNotificationsHelper.skipJobsAfterPushNotification();
    PullNotificationsDataHelper.persistLatestPushNotificationTime();
  }

  /**
   * This method is called for skipping the sync intervals which lies between the interval
   * mentioned after a push notification.
   */
  public static void skipJobsAfterPushNotification() {
    PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    if (pullSyncConfig == null) {
      return;
    }

    int intervalAfterPushInSeconds = pullSyncConfig.getIntervalAfterPushNotification();

    if (intervalAfterPushInSeconds <= 0) {
      return;
    }

    Date lastSuccessfulSycnTime = PullNotificationsDataHelper.getLastSuccessfulSyncedTime();
    Date currentTime = new Date();

    if (lastSuccessfulSycnTime != null && pullSyncConfig.getMaxTimeForExplicitSync() != 0) {
      long diffInMillis = currentTime.getTime() - lastSuccessfulSycnTime.getTime();
      long maxTimeForSyncInMillis = pullSyncConfig.getMaxTimeForExplicitSync() * 1000;
      if (diffInMillis > maxTimeForSyncInMillis) {
        PullNotificationLogger.logMaxTimeElapsedForExplicitPull(maxTimeForSyncInMillis);
        return;
      }
    }

    //Cancel the current job.
    PullNotificationsJobManager manager = new PullNotificationsJobManager(pullSyncConfig);
    manager.cancelPullNotificationJob();

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, intervalAfterPushInSeconds);
    //If I get a push notification at 1:00 pm and the interval for not allowing the notifications
    // is 5 hours, then my pushNotificationEndTime value should be 6:00pm
    Date pushNotificationEndTime = calendar.getTime();

    int baseInterval = pullSyncConfig.getBaseInterval();
    if (baseInterval == 0) {
      return;
    }
    Date nextJobDate = PullNotificationsDataHelper.getNextJobRunningTime();
    if (nextJobDate == null) {
      calendar.setTimeInMillis(currentTime.getTime());
      nextJobDate = calendar.getTime();
    }

    calendar.setTimeInMillis(nextJobDate.getTime());
    //Keep on looping till the nextJobDate comes out of skipJobDate.
    while (pushNotificationEndTime.compareTo(nextJobDate) > 0) {
      calendar.add(Calendar.SECOND, baseInterval);
      nextJobDate = calendar.getTime();
    }

    int nextJobIntervalInSeconds = PullNotificationsUtil.getDiffInSeconds(currentTime, nextJobDate);
    PullNotificationLogger.logNextJobAfterPushNotification(nextJobIntervalInSeconds);
    try {
      manager.schedule(true, false, nextJobIntervalInSeconds, false);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /**
   * The purposee of this function is to get the pull sync configuration for the first time.
   */
  public static void initFirstPullJob() {
    PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    //Initiate explicit pull request when we don't have the sync configuration with us.
    if (pullSyncConfig != null) {
      return;
    }

    int firstTimeFullDelay = PullNotificationsDataHelper.getFirstTimeFullDelay();

    PullNotificationLogger.logFirstTimeSyncConf(firstTimeFullDelay);
    if (firstTimeFullDelay == 0) {
      firstTimeFullDelay = Constants.FIRST_TIME_PULL_DELAY;
    }

    PullNotificationsJobManager manager = new PullNotificationsJobManager();
    try {
      manager.scheduleJob(false/*should replace existing job*/, false, firstTimeFullDelay, 0, true);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static String[] getUserLanguages() {
    String userLanguages = AppUserPreferenceUtils.getUserLanguages();
    if (CommonUtils.isEmpty(userLanguages)) {
      return null;
    }
    return userLanguages.split(Constants.COMMA_CHARACTER);
  }

  public static void handleAppUpgrade() {
    String state = PullNotificationsDataHelper.getState();
    //Only if the state is not there, then persist it as upgrade state.
    if (CommonUtils.isEmpty(state)) {
      PullNotificationsDataHelper.persistState(Constants.PULL_NOTIFICATIONS_STATE);
    }
    //In case of an app upgrade, default notification should not be shown.
    PullNotificationsDataHelper.saveDefaultNotificationShown(true);

    int firstTimeFullDelay = PullNotificationsDataHelper.getFirstTimeFullDelay();

    if (firstTimeFullDelay != 0) {
      PullNotificationsJobManager manager = new PullNotificationsJobManager();
      try {
        manager.scheduleJob(false/* do not replace a existing job*/, false/*requires charging*/,
            firstTimeFullDelay, 30/*tolerance*/, true);
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }
  }

  /**
   * This function will create a pull notification job and pass it for scheduling. This job cannot
   * replace an already existing job.
   *
   * @throws Exception
   */
  public static void initPullJobOnProcessStart() throws Exception {

    PullNotificationLogger.loginitPullJobOnProcessStart();

    if (!isPullJobAllowed()) {
      PullNotificationLogger.logPullJobFailed();
      return;
    }

    PullNotificationsJobManager manager = new PullNotificationsJobManager();
    Date nextJobTime = PullNotificationsDataHelper.getNextJobRunningTime();

    PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    if (pullSyncConfig == null) {
      boolean isInternetAvailable = CommonUtils.isNetworkAvailable(CommonUtils.getApplication());
      if (isInternetAvailable && PullNotificationsUtil.hasJobTimeExpired
          (nextJobTime)) {
        cancelPullJobAndMakeRequest(manager, true);
      } else {
        initFirstPullJob();
      }
      return;
    }

    if (nextJobTime == null) {
      manager.schedule(false/*can a running job be replaced*/, false/*requires charging*/);
      return;
    }

    if (!PullNotificationsUtil.hasJobTimeExpired(nextJobTime)) {
      int nextSyncInterval = PullNotificationsUtil.getDiffInSeconds(new Date(), nextJobTime);
      manager.schedule(false/*can a running job be replaced*/, false/*requires charging*/,
          nextSyncInterval, false);
    } else if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
      //If nextjobTime has expired and internet is there, do an immediate pull.
      cancelPullJobAndMakeRequest(manager, false);
    }

  }

  private static void cancelPullJobAndMakeRequest(PullNotificationsJobManager manager, boolean
      isFirstTimePull) {
    if (manager == null) {
      return;
    }
    manager.cancelPullNotificationJob();
    KillProcessAlarmManager.onAppProcessInvokedInBackground();
    PullNotificationsPresenter pullNotificationsPresenter = new PullNotificationsPresenter(
        BusProvider.getRestBusInstance());
    PullNotificationJobEvent pullNotificationJobEvent = new PullNotificationJobEvent();
    pullNotificationJobEvent.setIsFirstTimePull(isFirstTimePull);
    pullNotificationsPresenter.pullNotifications(pullNotificationJobEvent);
  }


  public static boolean isPullJobAllowed() {
    boolean enabledByUser =
        !(!PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true));
    boolean enabledByServer = PullNotificationsDataHelper.arePullNotificationsEnabled();
    return enabledByUser && enabledByServer;
  }


  /**
   * Returns one of the {@link PullNotificationRequestStatus} values depending upon the conditions.
   *
   * @return
   */
  public static PullNotificationRequestStatus requestStatus() {
    if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
      return PullNotificationRequestStatus.NO_INTERNET;
    }

    boolean enabledByUser =
        !(!PreferenceManager.getPreference(GenericAppStatePreference.NOTIFICATION_ENABLED, true) ||
            (PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_TRAY_MANAGEMENT_SECTION_WAS_EVER_EXPANDED, false)
                && (PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION, Constants.GROUPED) == Constants.ONLY_LIVE_TICKER)));
    if (!enabledByUser) {
      return PullNotificationRequestStatus.NOTIFICATIONS_DISABLED_HAMBURGER;
    }
    boolean enabledByServer = PullNotificationsDataHelper.arePullNotificationsEnabled();
    if (!enabledByServer) {
      return PullNotificationRequestStatus.NOTIFICATIONS_DISABLED_SERVER;
    }
    return PullNotificationRequestStatus.ALLOW;
  }

  /**
   * This method is used to log an event when the pull job is invoked by the framework.
   *
   * @param pullNotificationJobEvent
   */
  public static void logPullNotificationJobEvent(
      PullNotificationJobEvent pullNotificationJobEvent) {
    PullSyncConfig pullSyncConfig = PullNotificationsDataHelper.getSyncConfiguration();
    logPullNotificationJobEvent(pullNotificationJobEvent, pullSyncConfig);
  }

  /**
   * This method is used to log whether the pull request API  was hit or not depending upon the
   * flags passed by the server
   *
   * @param pullNotificationJobEvent Event to be passed
   * @param pullSyncConfig           - Config which has the enable/disable flags.
   */
  public static void logPullNotificationJobEvent(PullNotificationJobEvent
                                                     pullNotificationJobEvent,
                                                 PullSyncConfig pullSyncConfig) {

    boolean enableEventForAPIHit = true;
    boolean enableEventForNonAPIHit = true;

    if (pullSyncConfig != null) {
      enableEventForAPIHit = pullSyncConfig.enableEventForAPIHit();
      enableEventForNonAPIHit = pullSyncConfig.enableEventForNonAPIHit();
    }

    String jobResult = pullNotificationJobEvent.getPullNotificationJobResult();
    if (CommonUtils.isEmpty(jobResult)) {
      return;
    }
    if (enableEventForAPIHit &&
        jobResult.equals(PullNotificationJobResult.PULL_API_HIT.toString())) {
      PullNotificationJobAnalyticsHelper.logPullNotificationJob(pullNotificationJobEvent);
      return;
    }

    if (enableEventForNonAPIHit && jobResult.equals(PullNotificationJobResult.PULL_API_NOT_HIT
        .toString())) {
      PullNotificationJobAnalyticsHelper.logPullNotificationJob(pullNotificationJobEvent);
      return;
    }

  }
}
