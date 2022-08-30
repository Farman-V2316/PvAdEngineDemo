/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.nhcommand;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.model.entity.NHCommand;

/**ø
 * Interface for any nhcommand handler
 *
 * @author maruti.borker
 */
public interface NHActivityCommandHandler {
  /**
   * Checks if it can be handled and handles it
   *
   * @return value if its handled or not
   */
  boolean handle(NHCommand command, String params, Activity parentActivity, Fragment fragment,
                 PageReferrer pageReferrer);
}
