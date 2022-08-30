/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppRatePreference;

import java.util.concurrent.TimeUnit;

/**
 * 1. Helper class to app rate us dialog.
 * 2. Consists various checks to show rate us.
 *
 * @author shashikiran.nr
 */
public class RateUsDialogHelper {
  private static final String TAG = "RATEUS_HELPER";
  private static final int MIN_STORY_VIEWED_PER_SESSION = 10;
  private static final int MIN_STORY_SHARED = 5;
  private static final int SUBSEQUENT_MIN_STORY_SHARED = 5;
  private static final int MIN_BOOK_READ_COUNT = 4;
  private static final int MIN_SESSION_TIME_SECONDS = 10 * 60;
  private static final long MIN_VIDEO_LENGTH_MILLIS = 120000L;

  public static boolean hasUserWatchedMinimumDurationForVideo(long videoDuration) {
    return videoDuration >= MIN_VIDEO_LENGTH_MILLIS;
  }

  public static boolean isTenMinSessionHappened() {
    return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - getAppSessionStartTime()) >
        getMinSessionWaitTime();
  }

  public static boolean isUserOpenedNStories() {
    return getStoryViewedCountPerSession() >= getMinStoryViewedCountPerSession() + 1;
  }

  public static boolean isUserSharedNStory() {
    int diff = getStorySharedCount() - getMinStorySharedCountPerSession();
    return (diff >= 0) && (diff%getSubsequentStorySharedCount() == 0);
  }

  public static int getSubsequentStorySharedCount() {
    int val =  PreferenceManager.getPreference(AppRatePreference.APPRATE_SUBSEQUENT_STORIES_SHARED,SUBSEQUENT_MIN_STORY_SHARED);
    return (val > 0) ? val : SUBSEQUENT_MIN_STORY_SHARED; // Done because RateConfig is written in java and by default gives zero.
  }

  public static boolean isUserExitedBookForNTimes() {
    return getBookReadCount() >= getMinBookReadCount();
  }

  private static long getAppSessionStartTime() {
    return PreferenceManager.getPreference(GenericAppStatePreference.APP_START_TIME, 0L);
  }

  private static int getMinSessionWaitTime() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MAX_SESSION_WAIT_TIME_SECONDS,
        MIN_SESSION_TIME_SECONDS);
  }

  private static int getStoryViewedCountPerSession() {
    return PreferenceManager.getPreference(AppRatePreference.STORY_VIEWED_COUNT_PER_SESSION, 0);
  }

  private static int getStorySharedCount() {
    return PreferenceManager.getPreference(AppRatePreference.STORY_SHARED_COUNT,0);
  }

  private static void setStoryViewedCountPerSession(int count) {
    PreferenceManager.savePreference(AppRatePreference.STORY_VIEWED_COUNT_PER_SESSION, count);
  }

  private static int getMinStoryViewedCountPerSession() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_STORIES_VIEWED_PER_SESSION,
        MIN_STORY_VIEWED_PER_SESSION);
  }

  private static int getMinStorySharedCountPerSession() {
    int val =  PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_STORIES_SHARED,
            MIN_STORY_SHARED);
    return (val > 0) ? val : MIN_STORY_SHARED;
  }

  private static int getBookReadCount() {
    return PreferenceManager.getPreference(AppRatePreference.BOOK_READ_COUNT, 0);
  }

  private static void setBookReadCount(int count) {
    PreferenceManager.savePreference(AppRatePreference.BOOK_READ_COUNT, count);
  }

  private static int getMinBookReadCount() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_BOOKS_READ,
        MIN_BOOK_READ_COUNT);
  }

  private static int getAppLaunchCount() {
    return PreferenceManager.getPreference(AppRatePreference.APP_LAUNCH_COUNT, 0);
  }

  public static void setAppLaunchCount(int count) {
    PreferenceManager.savePreference(AppRatePreference.APP_LAUNCH_COUNT, count);
  }

  public static int getAppLaunchCountAfterUpgrade() {
    return PreferenceManager.getPreference(AppRatePreference.APP_LAUNCH_COUNT_AFTER_UPGRADE,0);
  }

  public static void setAppLaunchCountAfterUpgrade(int count) {
    PreferenceManager.savePreference(AppRatePreference.APP_LAUNCH_COUNT_AFTER_UPGRADE,count);
  }

  public static int getLastAppVersionAfterUpgrade() {
    return PreferenceManager.getPreference(AppRatePreference.LAST_APP_VERSION_AFTER_UPGRADE,0);
  }

  public static void saveLastAppVersionAfterUpgrade(int version) {
    PreferenceManager.savePreference(AppRatePreference.LAST_APP_VERSION_AFTER_UPGRADE,version);
  }

  public static boolean isRateScreenCheckAfterUpgrade() {
    return getRateScreenShownAfterUpgrade() == Constants.RATE_SCREEN_UPGRADE_NOT_SHOWN;
  }

  public static int getRateScreenShownAfterUpgrade() {
    return PreferenceManager.getPreference(AppRatePreference.RATE_SCREEN_SHOWN_AFTER_UPGRADE,Constants.RATE_SCREEN_NO_UPGRADE);
  }

  public static void saveRateScreenShownAfterUpgrade(int update) {
    PreferenceManager.savePreference(AppRatePreference.RATE_SCREEN_SHOWN_AFTER_UPGRADE,update);
  }

  public static void updateRateScreenShownAfterUpgrade() {
    if(hasUpgradeHappenedForRating()) {
      saveRateScreenShownAfterUpgrade(Constants.RATE_SCREEN_UPGRADE_SHOWN);
    }
  }


  public static void performRateOperationsOnUpgrade() {
    saveLastAppVersionAfterUpgrade(AppUserPreferenceUtils.getLastAppVersion());
    resetAppLaunchCountAfterUpgrade();
    saveRateScreenShownAfterUpgrade(Constants.RATE_SCREEN_UPGRADE_NOT_SHOWN);
  }

  public static boolean hasUpgradeHappenedForRating() {
    return getLastAppVersionAfterUpgrade() !=0 ;
  }

  public static int getMinAppLaunchAfterUpgrade() {
    return PreferenceManager.getPreference(AppRatePreference.APPRATE_MIN_APP_LAUNCHES_AFTER_UPGRADE,
            Constants.DEFAULT_MIN_APP_LAUNCHES_POST_UPGRADE);
  }
  public static void incrementAppLaunchCountAfterUpgrade(int increment) {
    if(hasUpgradeHappenedForRating()) {
      setAppLaunchCountAfterUpgrade(getAppLaunchCountAfterUpgrade() + increment);
    }
  }

  public static void resetAppLaunchCountAfterUpgrade() {
    setAppLaunchCountAfterUpgrade(0);
  }

  public static void resetAppLaunchCountOnShowingRateUs() {
    PreferenceManager.savePreference(AppRatePreference.APP_LAUNCH_COUNT, 0);
  }

  public static void resetBookReadCountOnShowingRateUs() {
    PreferenceManager.savePreference(AppRatePreference.BOOK_READ_COUNT, 0);
  }

  public static void resetStoryViewedCountPerSessionOnSessionClose() {
    PreferenceManager.savePreference(AppRatePreference.STORY_VIEWED_COUNT_PER_SESSION, 0);
  }

  public static void resetStorySharedCountPerSessionOnSessionClose() {
    PreferenceManager.savePreference(AppRatePreference.STORY_SHARED_COUNT, 0);
  }

  /**
   * Increment the StoryRead count and save count in preference manager.
   *
   * @return
   */
  public static void incrementStoryViewedCount() {
    int storyViewedCount = getStoryViewedCountPerSession();
    storyViewedCount++;
    setStoryViewedCountPerSession(storyViewedCount);
    Logger.d(TAG, "Number of stories viewed on session " + getStoryViewedCountPerSession());
  }

  /**
   * Increment the app launch count and save count in preference manager.
   *
   * @return
   */
  public static void incrementAppLaunchCount() {
    int launchCount = getAppLaunchCount();
    launchCount++;
    incrementAppLaunchCountAfterUpgrade(1);
    setAppLaunchCount(launchCount);
  }


  /**
   * Increment the book read count and save count in preference manager.
   *
   * @return
   */
  public static void incrementBookReadCount() {
    int readCount = getBookReadCount();
    readCount++;
    setBookReadCount(readCount);
  }

  public static void firstLaunchOrLatestUpgradeTime() {
    long firstOrUpgradeTime =
        PreferenceManager.getPreference(AppRatePreference.FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME, 0L);
    if (firstOrUpgradeTime == 0) {
      PreferenceManager.savePreference(AppRatePreference.FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME,
          System.currentTimeMillis());
    }
  }
}
