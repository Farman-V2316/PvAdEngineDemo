/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.upgrade;

import java.io.Serializable;

/**
 * Rate us dialog congif helper.
 *
 * @author shashikiran.nr
 */
public class RateConfig implements Serializable {
  private boolean enable;
  private int preActivitySessionWaitTimeInSeconds;
  private int minNumberOfStoriesViewedPerSession;
  private int minNumberOfStoriesShared;
  private int subsequentNumberOfStoriesShared;
  private int minNumberOfBooksRead;
  private int maxNumberOfTimesToShowRateScreen;
  private int minNumberOfDaysToWaitShowingRateScreen;
  private int minWaitDaysForNewUsersToShowRateScreen;
  private int minLaunchesForNewUsersToShowRateScreen;
  private int minNumberOfDaysUserToWaitAfterLastSeen;
  private int minNumberOfAppLaunchesToWaitAfterLastSeen;
  private int minLaunchesPostUpgradeToShowRateScreen;

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public int getPreActivitySessionWaitTimeInSeconds() {
    return preActivitySessionWaitTimeInSeconds;
  }

  public void setPreActivitySessionWaitTimeInSeconds(int preActivitySessionWaitTimeInSeconds) {
    this.preActivitySessionWaitTimeInSeconds = preActivitySessionWaitTimeInSeconds;
  }

  public int getMinNumberOfStoriesViewedPerSession() {
    return minNumberOfStoriesViewedPerSession;
  }

  public void setMinNumberOfStoriesViewedPerSession(int minNumberOfStoriesViewedPerSession) {
    this.minNumberOfStoriesViewedPerSession = minNumberOfStoriesViewedPerSession;
  }

  public int getMinNumberOfBooksRead() {
    return minNumberOfBooksRead;
  }

  public void setMinNumberOfBooksRead(int minNumberOfBooksRead) {
    this.minNumberOfBooksRead = minNumberOfBooksRead;
  }

  public int getMaxNumberOfTimesToShowRateScreen() {
    return maxNumberOfTimesToShowRateScreen;
  }

  public void setMaxNumberOfTimesToShowRateScreen(int maxNumberOfTimesToShowRateScreen) {
    this.maxNumberOfTimesToShowRateScreen = maxNumberOfTimesToShowRateScreen;
  }

  public int getMinNumberOfDaysToWaitShowingRateScreen() {
    return minNumberOfDaysToWaitShowingRateScreen;
  }

  public void setMinNumberOfDaysToWaitShowingRateScreen(
      int minNumberOfDaysToWaitShowingRateScreen) {
    this.minNumberOfDaysToWaitShowingRateScreen = minNumberOfDaysToWaitShowingRateScreen;
  }

  public int getMinWaitDaysForNewUsersToShowRateScreen() {
    return minWaitDaysForNewUsersToShowRateScreen;
  }

  public void setMinWaitDaysForNewUsersToShowRateScreen(
      int minWaitDaysForNewUsersToShowRateScreen) {
    this.minWaitDaysForNewUsersToShowRateScreen = minWaitDaysForNewUsersToShowRateScreen;
  }

  public int getMinLaunchesForNewUsersToShowRateScreen() {
    return minLaunchesForNewUsersToShowRateScreen;
  }

  public void setMinLaunchesForNewUsersToShowRateScreen(
      int minLaunchesForNewUsersToShowRateScreen) {
    this.minLaunchesForNewUsersToShowRateScreen = minLaunchesForNewUsersToShowRateScreen;
  }

  public int getMinNumberOfDaysUserToWaitAfterLastSeen() {
    return minNumberOfDaysUserToWaitAfterLastSeen;
  }

  public void setMinNumberOfDaysUserToWaitAfterLastSeen(
      int minNumberOfDaysUserToWaitAfterLastSeen) {
    this.minNumberOfDaysUserToWaitAfterLastSeen = minNumberOfDaysUserToWaitAfterLastSeen;
  }

  public int getMinNumberOfAppLaunchesToWaitAfterLastSeen() {
    return minNumberOfAppLaunchesToWaitAfterLastSeen;
  }

  public void setMinNumberOfAppLaunchesToWaitAfterLastSeen(
      int minNumberOfAppLaunchesToWaitAfterLastSeen) {
    this.minNumberOfAppLaunchesToWaitAfterLastSeen = minNumberOfAppLaunchesToWaitAfterLastSeen;
  }

  public int getMinNumberOfStoriesShared() {
    return minNumberOfStoriesShared;
  }

  public void setMinNumberOfStoriesShared(int minNumberOfStoriesShared) {
    this.minNumberOfStoriesShared = minNumberOfStoriesShared;
  }

  public int getSubsequentNumberOfStoriesShared() {
    return subsequentNumberOfStoriesShared;
  }

  public void setSubsequentNumberOfStoriesShared(int subsequentNumberOfStoriesShared) {
    this.subsequentNumberOfStoriesShared = subsequentNumberOfStoriesShared;
  }

  public int getMinLaunchesPostUpgradeToShowRateScreen() {
    return minLaunchesPostUpgradeToShowRateScreen;
  }

  public void setMinLaunchesPostUpgradeToShowRateScreen(int minLaunchesPostUpgradeToShowRateScreen) {
    this.minLaunchesPostUpgradeToShowRateScreen = minLaunchesPostUpgradeToShowRateScreen;
  }
}
