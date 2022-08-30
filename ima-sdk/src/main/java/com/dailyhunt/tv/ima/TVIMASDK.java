package com.dailyhunt.tv.ima;

import com.dailyhunt.tv.ima.helper.ImaUtils;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;

/**
 * Initialization class for IMA SDK for playing Video ADS
 *
 * @author ranjith
 */

public class TVIMASDK {

  private static TVIMASDK TVIMASDK;
  private final String TAG = TVIMASDK.class.getSimpleName();
  private ImaSdkSettings imaSdkSettings;
  private ImaSdkFactory imaSdkFactory;

  private TVIMASDK() {
    IMALogger.d(TAG, "Private Constructor");
    initializeImaSDK();
  }

  public static TVIMASDK getInstance() {
    if (TVIMASDK == null) {
      synchronized (TVIMASDK.class) {
        if (TVIMASDK == null) {
          TVIMASDK = new TVIMASDK();
        }
      }
    }
    return TVIMASDK;
  }

  private void initializeImaSDK() {
    IMALogger.d(TAG, "Initialize SDK");

    imaSdkFactory = ImaSdkFactory.getInstance();
    imaSdkSettings = imaSdkFactory.createImaSdkSettings();

    imaSdkSettings.setEnableOmidExperimentally(ImaUtils.enableOmidExperimentally());

    // Santosh.kulkarni commenting below , as we want to user English only for Skip & Learn more
    // imaSdkSettings.setLanguage(UserPreferenceUtil.getUserPrimaryLanguage());
  }


  ImaSdkSettings getImaSettings() {
    return imaSdkSettings;
  }

  ImaSdkFactory getImaSdkFactory() {
    return imaSdkFactory;
  }
}
