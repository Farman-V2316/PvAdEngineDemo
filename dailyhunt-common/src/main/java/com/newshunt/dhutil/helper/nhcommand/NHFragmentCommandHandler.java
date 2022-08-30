/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.nhcommand;

import androidx.fragment.app.Fragment;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.model.entity.NHCommand;

/**
 * Interface for any fragment nhcommand handler
 *
 * @author anshul.jain
 */
public interface NHFragmentCommandHandler {
  /**
   * Checks if it can be handled and handles it
   *
   * @return value if its handled or not
   */
  boolean handle(NHCommand command, String params, Fragment parentFragment,
                 PageReferrer pageReferrer);
}
