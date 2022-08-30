package com.dailyhunt.tv.ima.service;

import com.dailyhunt.tv.ima.entity.state.AdState;
import com.google.ads.interactivemedia.v3.api.Ad;

/**
 * Listener from the Ad Player when the Ad state is changed
 *
 * @author ranjith
 */

public interface AdStateListenerService {

  /**
   * Call back from the Ad Player , saying that Ad has been loaded
   *
   * @param ad -- ad
   */
  void onAdLoaded(Ad ad);

  /**
   * Call back from Ad Player , saying that Ad has been started
   *
   * @param ad -- ad
   */
  void onAdPlayStart(Ad ad, boolean companionAdLoaded);

  /**
   * Call back from Ad player saying , Ad has been completed / removed from player
   *
   * @param ad -- ad
   */
  void onAdPlayEnded(Ad ad);

  /**
   * Call back from Ad player when ad is resumed
   *
   * @param ad -- ad
   */
  void onAdResumed(Ad ad);

  /**
   * Call back from Ad Player , when ad is paused
   *
   * @param ad -- ad
   */
  void onAdPaused(Ad ad);

  /**
   * Call back from Ad Player , when ad is tapped
   *
   * @param ad -- ad
   */
  void onAdTapped(Ad ad);

  /**
   * Call back from Ad Player , when ad is clicked
   *
   * @param ad -- ad
   */
  void onAdClicked(Ad ad);

  /**
   * Call back from Ad Player , when ad is clicked
   *
   * @param ad -- ad
   */
  void onCompanionAdClicked(Ad ad);

  /**
   * Any Error caused during Ad Play back
   */
  void onAdError();

  /**
   * Call back when all Ads for corresponding Url are done
   */
  void onAllAdsCompleted();

  /**
   * Method to return current AdState
   *
   * @return -- AdState
   */
  AdState getAdState();

}
