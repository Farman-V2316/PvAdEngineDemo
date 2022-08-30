package com.dailyhunt.tv.ima.protocol;


import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

/**
 * Protocol defining the action between Content player View and Ad Player View
 *
 * @author ranjith
 */

public interface AdPlayerProtocol {

  /**
   * Initialize the Ad Player View..
   */
  void initialize();

  /**
   * Sets the adsManager to the ad View to call functions like play/pause via IMA sdk.
   */
  void setAdsManager(AdsManager adsManager);

  /**
   * Helper method to set Input data to Ad Player ..
   */
  void setInputData(String adUrl);

  void savePosition();

  void restorePosition();

  /**
   * Helper method to generate the Ad Request
   *
   * @param imaSdkFactory -- ImaSDK Factory
   * @return -- Ad request generated
   */
  AdsRequest buildAdRequest(ImaSdkFactory imaSdkFactory);

  AdDisplayContainer getAdDisplaycontainer(ImaSdkFactory mSdkFactory, boolean showCompanion);

  /**
   * PLayer release call back
   */
  void releasePlayer();

  /**
   * Method to modify the Ad Container visibility
   *
   * @param visible -- to make AdContainer Visible or not
   */
  void setAdVisibility(boolean visible);


  default void setQualifiesImmersive(boolean state) {}

  default void setImmersiveSpan(int span) {}

  default void setCompanionRefreshTime(int refreshTime) {}

}
