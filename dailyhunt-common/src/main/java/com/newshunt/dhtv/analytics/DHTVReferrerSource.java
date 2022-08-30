/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhtv.analytics;


import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;

import java.io.Serializable;

/**
 * @Author Rahul Ravindra.
 */

public enum DHTVReferrerSource implements NHReferrerSource, Serializable {
  /**
   * landing/details home activity
   */
  DHTV_HOME_VIEW,
  /**
   * Add Channel Activity
   **/
  ADD_CHANNEL_VIEW,
}
