/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig.HTMLPgiDisplayType.POPUP;
import static com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig.HTMLPgiDisplayType.SWIPEABLE_NO_TOPBAR;
import static com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig.HTMLPgiDisplayType.SWIPEABLE_WITH_BACK;
import static com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig.HTMLPgiDisplayType.SWIPEABLE_WITH_TOPBAR;

/**
 * @author raunak.yadav
 */
public class PgiAdsConfig extends AdsConfig {
  private static final long serialVersionUID = 505258683687173747L;

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({
      SWIPEABLE_NO_TOPBAR,
      SWIPEABLE_WITH_TOPBAR,
      SWIPEABLE_WITH_BACK,
      POPUP
  })
  public @interface HTMLPgiDisplayType {
    @SerializedName("swipeable_no_topbar") String SWIPEABLE_NO_TOPBAR = "swipeable_no_topbar";
    @SerializedName("swipeable_topbar")String SWIPEABLE_WITH_TOPBAR = "swipeable_topbar";
    @SerializedName("swipeable_back")String SWIPEABLE_WITH_BACK = "swipeable_back";
    @SerializedName("popup")String POPUP = "popup";
  }


  /**
   * Swipe count after which an ad is to be requested.
   * Based On Network type.
   */
  private int requestSwipeCountGood;
  private int requestSwipeCountAverage;
  private int requestSwipeCountSlow;

  public int getRequestSwipeCountGood() {
    return requestSwipeCountGood;
  }

  public int getRequestSwipeCountAverage() {
    return requestSwipeCountAverage;
  }

  public int getRequestSwipeCountSlow() {
    return requestSwipeCountSlow;
  }
}