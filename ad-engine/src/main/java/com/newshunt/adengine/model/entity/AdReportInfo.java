/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity;

import java.io.Serializable;

/**
 * Contains info to be sent with beacon to ad server.
 *
 * @author raunak.yadav
 */
public class AdReportInfo implements Serializable {

  private String adTitle;
  private String adDescription;
  private String advertiser;
  private String category;

  public String getAdTitle() {
    return adTitle;
  }

  public void setAdTitle(String adTitle) {
    this.adTitle = adTitle;
  }

  public String getAdDescription() {
    return adDescription;
  }

  public void setAdDescription(String adDescription) {
    this.adDescription = adDescription;
  }

  public String getAdvertiser() {
    return advertiser;
  }

  public void setAdvertiser(String advertiser) {
    this.advertiser = advertiser;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
}