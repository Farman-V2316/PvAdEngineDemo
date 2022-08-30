/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

import android.app.Activity;

/**
 * Used for passing permission result via Bus
 *
 * @author: bedprakash.rout on 8/22/2016.
 */

public class PermissionResult {
  public Activity activity;
  public String[] permissions;

  public PermissionResult(Activity activity, String[] permissions) {
    this.activity = activity;
    this.permissions = permissions;
  }
}
