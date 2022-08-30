package com.dailyhunt.tv.ima.callback;

import com.dailyhunt.tv.ima.entity.model.ContentAdType;
import com.dailyhunt.tv.ima.entity.state.AdState;
import com.google.ads.interactivemedia.v3.api.Ad;

/**
 * Call backs from AD Play for analytics ..
 *
 * @author ranjith
 */

public interface AdPlayerCallBack {

  default void onAdStateChanged(Ad ad, AdState adState, ContentAdType adType) {
    onAdStateChanged(ad, adState, adType, false);
  }

  /**
   * Update ad State for analytics
   *
   * @param ad                -- ad
   * @param adState           -- ad State
   * @param adType            -- adType
   * @param companionAdLoaded -- companion ad loaded or not
   */
  void onAdStateChanged(Ad ad, AdState adState, ContentAdType adType, boolean companionAdLoaded);
}
