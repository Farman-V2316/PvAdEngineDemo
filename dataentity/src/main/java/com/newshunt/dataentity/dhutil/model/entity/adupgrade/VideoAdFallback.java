/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import java.io.Serializable;

/**
 * @author raunak.yadav
 */
public class VideoAdFallback implements Serializable {

  private static final long serialVersionUID = 1332667520323251603L;
  private String imageUrl;
  private String action;
  private String actionText;
  private String itemTag;
  private String beaconUrl;
  private String landingUrl;
  private String verticalImageUrl;

  public String getImageUrl() {
    return imageUrl;
  }

  public String getAction() {
    return action;
  }

  public String getActionText() {
    return actionText;
  }

  public String getItemTag() {
    return itemTag;
  }

  public String getBeaconUrl() {
    //appending timestamp to make each value unique.
    return beaconUrl + System.currentTimeMillis();
  }

  public String getVerticalImageUrl() {
    return verticalImageUrl;
  }

  public String getLandingUrl() {
    return landingUrl + System.currentTimeMillis();
  }
}
