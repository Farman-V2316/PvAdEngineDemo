/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import java.io.Serializable;

/**
 * @author raunak.yadav
 */
public class AdsSdkTimeout implements Serializable {

  private static final long serialVersionUID = 2113859584365844605L;

  private int adsTimeoutGood;
  private int adsTimeoutAverage;
  private int adsTimeoutSlow;

  public int getAdsTimeoutGood() {
    return adsTimeoutGood;
  }

  public int getAdsTimeoutAverage() {
    return adsTimeoutAverage;
  }

  public int getAdsTimeoutSlow() {
    return adsTimeoutSlow;
  }
}