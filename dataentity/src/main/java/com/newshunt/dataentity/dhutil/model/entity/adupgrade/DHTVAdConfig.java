/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import com.newshunt.common.helper.common.Constants;

/**
 * class for DHTV Ad handshake params for Instream Ad qualify
 *
 * @author vinod.bc
 */
public class DHTVAdConfig {

  private long adDistanceMs = Constants.VIDEO_MIN_AD_RECENT_PLAYED_DURATION;
  private long ignoreVideoDuration = Constants.VIDEO_IGNORE_ITEM_DURATION;

  public long getAdDistanceMs() {
    return adDistanceMs;
  }

  public long getIgnoreVideoDuration() {
    return ignoreVideoDuration;
  }

}