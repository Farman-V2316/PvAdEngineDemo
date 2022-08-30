/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper.Callbacks;

import com.newshunt.permissionhelper.entities.PermissionRationale;
import com.newshunt.permissionhelper.utilities.PermissionGroup;

/**
 * Interface used in rationale dialog
 *
 * @author: bedprakash.rout on 8/12/2016.
 */

public interface PermissionRationaleProvider {

  /**
   * Get Permission Rationale for given Permission
   */
  PermissionRationale getRationaleString(PermissionGroup permissionGroup);

  /**
   * Get title for Permission Rationale dialog
   */
  String getRationaleTitle();

  /**
   * Get description for Permission Rationale dialog
   */
  String getRationaleDesc();

  /**
   * Get message to open Settings UI
   */
  String getOpenSettingsMessage();

  /**
   * Get action name to open Setting UI
   */
  String getOpenSettingsAction();

  /**
   * Get text for positive button
   */
  String getPositiveBtn();

  /**
   * Get text for negative button
   */
  String getNegativeBtn();
}
