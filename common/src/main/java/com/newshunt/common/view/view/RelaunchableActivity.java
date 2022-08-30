/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.view;

/**
 * Enable restarting activity when more time is spent in background.
 * Activities implementing this go through soft reset.
 *
 * @author satosh.dhanyamraju
 */
public interface RelaunchableActivity {
  /**
   *
   * @param relaunchToHomeTab  - true, if we want to navigate to home tab; false, otherwise.
   */
  void relaunch(boolean relaunchToHomeTab);

  /**
   * Same activity may be used with different fragments. This is to communicate whether current
   * instance of the activity supports relaunching (soft-refresh)
   */
  boolean canRelaunchInCurrentScreen();
}