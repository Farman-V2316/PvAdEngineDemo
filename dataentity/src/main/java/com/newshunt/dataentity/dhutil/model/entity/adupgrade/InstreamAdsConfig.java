/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import com.newshunt.common.helper.common.Constants;
import com.google.gson.annotations.SerializedName;

/**
 * Common config across app for Instream ads.
 *
 * @author raunak.yadav
 */
public class InstreamAdsConfig extends AdsConfig {
  // Not to request ads for videos with duration less than this param.
  private long vdoNoAdReqMinLength = Constants.VIDEO_MIN_NO_AD_VIDEO_LENGTH;
  private long minAdWaitForContent = Constants.DEFAULT_WAIT_FOR_AD_DELAY;
  private boolean adRequestForLiveVideos = false;

  @SerializedName("companion")
  private CompanionAdsConfig companionAdsConfig;

  public long getVdoNoAdReqMinLength() {
    return vdoNoAdReqMinLength;
  }

  public long getMinAdWaitForContent() {
    return minAdWaitForContent;
  }

  public CompanionAdsConfig getCompanionAdsConfig() {
    return companionAdsConfig;
  }

  public boolean isAdRequestForLiveVideos() {
    return adRequestForLiveVideos;
  }
}