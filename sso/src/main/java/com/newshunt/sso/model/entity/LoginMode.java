/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

/**
 * Enum for login mode
 *
 * @author arun.babu
 */
public enum LoginMode {
  //for cases like background MyProduct sync without any UI display
  BACKGROUND_ONLY,
  //for new or session expired login cases with UI
  NORMAL,
  //for login cases which starts form choosing login option, generally triggered by user
  USER_EXPLICIT
}
