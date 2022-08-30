/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.helper;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;

/**
 * @author satyanarayana.avv
 */
public interface ReferrerProvider {
  PageReferrer getPageReferrer();

  PageReferrer getReferrerLead();

  PageReferrer getReferrerFlow();

  String getReferrerRaw();

  default void updateReferrer(@NonNull PageReferrer referrer) {

  }
}
