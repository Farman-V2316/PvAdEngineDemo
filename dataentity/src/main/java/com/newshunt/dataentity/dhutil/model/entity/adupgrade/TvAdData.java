/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import com.google.gson.annotations.SerializedName;

/**
 * class for App Upgrade implementations
 *
 * @author umesh.isran
 */
public class TvAdData {
  private String skipText;

  @SerializedName("skipNow")
  private String skipNowText;

  private String skipInSec;
  private boolean freezeUserOperation;

  @SerializedName("ad-distance")
  private int adDistance;

  @SerializedName("ad-initialOffset")
  private int adInitialOffset;

  private int maxBitrateKbpsGood;
  private int maxBitrateKbpsAverage;
  private int maxBitrateKbpsSlow;

  public int getAdDistance() {
    return adDistance;
  }

  public int getAdInitialOffset() {
    return adInitialOffset;
  }

  public void setAdInitialOffset(int adInitialOffset) {
    this.adInitialOffset = adInitialOffset;
  }

  public String getSkipText() {
    return skipText;
  }

  public boolean isFreezeUserOperation() {
    return freezeUserOperation;
  }

  public String getSkipNowText() {
    return skipNowText;
  }

  public String getSkipInSec() {
    return skipInSec;
  }

  public int getMaxBitrateKbpsGood() {
    return maxBitrateKbpsGood;
  }

  public int getMaxBitrateKbpsAverage() {
    return maxBitrateKbpsAverage;
  }

  public int getMaxBitrateKbpsSlow() {
    return maxBitrateKbpsSlow;
  }
}