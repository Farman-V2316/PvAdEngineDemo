/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade;

import java.util.List;

/**
 * @author raunak.yadav
 */
public class SupplementAdsConfig extends AdsConfig {

  /**
   * Order of tags for supplement ad slots.
   */
  private List<String> tagOrder;
  /**
   * Whether ADS view is to be displayed in StorySupplementView.
   */
  private boolean useSupplementAds;
  /**
   * Whether TABOOLA webview is to be displayed in StorySupplementView.
   */
  private boolean useTaboolaWeb;

  public List<String> getTagOrder() {
    return tagOrder;
  }

  public boolean useSupplementAds() {
    return useSupplementAds;
  }

  public boolean useTaboolaWeb() {
    return useTaboolaWeb;
  }
}
