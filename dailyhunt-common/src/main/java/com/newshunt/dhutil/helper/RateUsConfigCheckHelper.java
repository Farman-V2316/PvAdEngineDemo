/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppRatePreference;

import java.util.concurrent.TimeUnit;

/**
 * 1. Check whether Rate us feature is enabled from server side.
 * 2. Check whether user had marked never shown again .
 * 3. Check whether user had already rated before.
 * 4. Method to check if Rate us is showing for first time (Wait for Week(7 days) and 3 app launch).
 * 5. Method to check if Rate us is already shown and yet user not rated (wait for 10 days and 10
 * app launch to show again).
 *
 * @author shashikiran.nr
 */
public class RateUsConfigCheckHelper {
  private static final String TAG = "RATEUS_CONFIG_HELPER";
  private static final int MIN_DAYS_AFTER_LAST_SEEN = 10;
  private static final int MIN_APP_LAUNCH_AFTER_LAST_SEEN = 10;
  private static final int MAX_DAYS_TO_SHOW = 7;
  private static final int MIN_APP_LAUNCH_TO_SHOW = 7;
  private static final int MAX_APPRATE_SHOW_COUNT = 3;
  private static final int MIN_APPRATE_SHOW_RESET_TIME_SECONDS = 30 * 24 * 60 * 60;

  private static boolean isEnabledFromServer() {
    boolean isEnabled =
        PreferenceManager.getPreference(AppRatePreference.IS_APPRATING_DIALOG_ENABLED,
            Boolean.FALSE);
    Logger.d(TAG, "Is Enabled from server : " + isEnabled);
    return isEnabled;
  }

  public static boolean isNeverShowChecked() {
    boolean isNeverShowAgainCheck =
        PreferenceManager.getPreference(AppRatePreference.APPRATE_NEVER_SHOW_AGAIN, Boolean.FALSE);
    Logger.d(TAG, "Is Never Show Again Check : " + isNeverShowAgainCheck);
    return isNeverShowAgainCheck;
  }

  private static boolean isRateNowClicked() {
    boolean isRateNowClicked =
        PreferenceManager.getPreference(AppRatePreference.APPRATE_IS_USER_CLICKED_RATE_NOW,
            Boolean.FALSE);
    Logger.d(TAG, "Is Rate Now Clicked before : " + isRateNowClicked);
    return isRateNowClicked;
  }

  private static int getMaxDaysToShowRateUsForFirstTime() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MAX_WAIT_DAYS_NEWUSERS_SHOW,
        MAX_DAYS_TO_SHOW);
  }

  private static int getMinAppLaunchToShowForFirstTime() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_LAUNCHES_NEWUSERS_SHOW,
        MIN_APP_LAUNCH_TO_SHOW);
  }

  private static long getFirstLaunchOrLatestUpgradeTime() {
    return PreferenceManager.getPreference(AppRatePreference.FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME,
        0L);
  }

  private static int getAppLaunchCount() {
    return PreferenceManager.getPreference(AppRatePreference.APP_LAUNCH_COUNT, 0);
  }

  private static long getRateUsShownTime() {
    return PreferenceManager.getPreference(AppRatePreference.IS_APPRATE_DIALOG_SHOWN, 0L);
  }

  public static void setRateUsShownTime(long rateUsShownTime) {
    PreferenceManager.savePreference(AppRatePreference.IS_APPRATE_DIALOG_SHOWN, rateUsShownTime);
  }

  private static int getMinDaysAfterRateUsLastSeen() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_DAYS_USER_AFTER_SHOWN,
        MIN_DAYS_AFTER_LAST_SEEN);
  }

  private static int getMinAppLaunchAfterRateUsLastSeen() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_APP_LAUNCHES_AFTER_SHOWN,
        MIN_APP_LAUNCH_AFTER_LAST_SEEN);
  }

  private static int getAppRateShowCount() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_SHOW_COUNT, 0);
  }

  private static void setAppRateShowCount(int count) {
    PreferenceManager.savePreference(AppRatePreference.APPRATE_SHOW_COUNT, count);
  }

  private static long getAppRateShowStartDate() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_SHOW_START_DATE, 0L);
  }

  private static void setAppRateShowStartDate() {
    PreferenceManager.savePreference(AppRatePreference.APPRATE_SHOW_START_DATE,
        System.currentTimeMillis());
  }

  private static int getMaxAppRateShowCount() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MAX_TIMES_SHOW,
        MAX_APPRATE_SHOW_COUNT);
  }

  public static void incrementAppRateShowCount() {
    if (getAppRateShowStartDate() == 0L) {
      resetAppRateShowParams();
    }
    setAppRateShowCount(getAppRateShowCount() + 1);
  }

  private static void resetAppRateShowParams() {
    setAppRateShowCount(0);
    setAppRateShowStartDate();
  }

  /**
   * Checks if max number of rate us pop show has been reached. Also resets the show count
   * parameters if reset time has been exceeded.
   *
   * @return false if the limit hasn't been reached, true otherwise
   */
  private static boolean isAppRateShowMaxCountReached() {
    if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - getAppRateShowStartDate()) >
        MIN_APPRATE_SHOW_RESET_TIME_SECONDS) {
      resetAppRateShowParams();
      return false;
    }
    if (getAppRateShowCount() < getMaxAppRateShowCount()) {
      return false;
    }
    return true;
  }

  /**
   * checks for first 7 days and 7 app launches
   *
   * @return can show or not
   */
  public static boolean isFirstTimeToShowRateUs() {
    if (getRateUsShownTime() > 0L) {
      return false;
    }
    int currentDays = (int) TimeUnit.MILLISECONDS.toDays(
        System.currentTimeMillis() - getFirstLaunchOrLatestUpgradeTime());
    return getRateUsShownTime() == 0 && currentDays >= getMaxDaysToShowRateUsForFirstTime() &&
        getAppLaunchCount() >= getMinAppLaunchToShowForFirstTime();
  }

  /**
   * if already shown then checks till next 10 days
   *
   * @return can show or not
   */
  public static boolean canShowAfterAppUsage() {
    int currentDays = (int) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() -
        getRateUsShownTime());
    return getRateUsShownTime() != 0L && (currentDays >= getMinDaysAfterRateUsLastSeen() &&
        getAppLaunchCount() >= getMinAppLaunchAfterRateUsLastSeen());
  }

  /**
   * if already shown then checks till next 10 days or 10 app launch count
   *
   * @return true if eligible to show after app usage or not eligible.
   */
  public static boolean canShowAfterAppUsageForUpgrade() {
    int currentDays = (int) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() -
            getRateUsShownTime());
    return getRateUsShownTime() != 0L && (currentDays >= getMinDaysAfterRateUsLastSeen() ||
            getAppLaunchCount() >= getMinAppLaunchAfterRateUsLastSeen());
  }

  public static boolean canShowAfterUpgrade() {
    boolean isUpgradeDone = RateUsDialogHelper.hasUpgradeHappenedForRating();
    return (!isUpgradeDone || RateUsDialogHelper.getAppLaunchCountAfterUpgrade() >
            RateUsDialogHelper.getMinAppLaunchAfterUpgrade());
  }

  public static boolean canShowAppRateDialog() {
    if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
      // Network unavailable
      return false;
    }

    if (!isEnabledFromServer()) {
      // server disabled
      return false;
    }

    if (isNeverShowChecked()) {
      // user disabled
      return false;
    }

    if (isRateNowClicked()) {
      // already rated
      return false;
    }

    // for these check method docs
    boolean isUpgradeCheck = RateUsDialogHelper.isRateScreenCheckAfterUpgrade();
    if(!isUpgradeCheck) {
       return !isAppRateShowMaxCountReached() && (RateUsConfigCheckHelper.isFirstTimeToShowRateUs() ||
              RateUsConfigCheckHelper.canShowAfterAppUsage());
    } else {
      return !isAppRateShowMaxCountReached() && (RateUsConfigCheckHelper.isFirstTimeToShowRateUs() ||
              RateUsConfigCheckHelper.canShowAfterAppUsageForUpgrade())
              && canShowAfterUpgrade();
    }
  }
}